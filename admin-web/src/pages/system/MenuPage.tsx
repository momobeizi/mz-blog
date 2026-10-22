import { useEffect, useState } from 'react'
import { Button, Form, Input, InputNumber, Modal, Select, Space, Table, Tag, TreeSelect, message } from 'antd'
import type { ColumnsType } from 'antd/es/table'
import { DeleteOutlined, EditOutlined, PlusOutlined } from '@ant-design/icons'
import { menuApi } from '@/api'
import { errorMessage } from '@/api/client'
import { PageTitle } from '@/components/PageTitle'
import { EnabledTag } from '@/components/StatusTag'
import type { AdminMenu } from '@/types'
import { useAuthStore } from '@/stores/auth'

function treeSelectData(items: AdminMenu[]): { title: string; value: string; children?: ReturnType<typeof treeSelectData> }[] {
  return items.filter(item => item.type !== 'BUTTON').map(item => ({
    title: item.name, value: item.id, children: item.children?.length ? treeSelectData(item.children) : undefined,
  }))
}

const typeLabels: Record<string, string> = { DIR: '目录', MENU: '菜单', BUTTON: '按钮权限' }

export function MenuPage() {
  const can = useAuthStore(state => state.can)
  const [rows, setRows] = useState<AdminMenu[]>([])
  const [open, setOpen] = useState(false)
  const [editing, setEditing] = useState<AdminMenu | null>(null)
  const [form] = Form.useForm()
  const type = Form.useWatch('type', form)

  const load = async () => {
    try { setRows(await menuApi.tree()) } catch (error) { message.error(errorMessage(error)) }
  }
  useEffect(() => { void load() }, [])

  const openModal = (row?: AdminMenu) => {
    setEditing(row || null); form.resetFields()
    form.setFieldsValue(row ? {
      parentId: row.parentId, name: row.name, type: row.type, path: row.path, componentKey: row.componentKey,
      permissionCode: row.permissionCode, icon: row.icon, sort: row.sort, visible: row.visible, status: row.status,
    } : { parentId: '0', type: 'MENU', sort: 0, visible: 1, status: 1 })
    setOpen(true)
  }

  const submit = async () => {
    try {
      const values = await form.validateFields()
      if (editing) await menuApi.update(editing.id, values)
      else await menuApi.create(values)
      message.success(editing ? '菜单已更新' : '菜单已创建')
      setOpen(false); void load()
    } catch (error) {
      if ((error as { errorFields?: unknown }).errorFields) return
      message.error(errorMessage(error))
    }
  }

  const remove = (row: AdminMenu) => Modal.confirm({
    title: '删除菜单', content: `确定删除“${row.name}”吗？存在子菜单时需要先删除子菜单。`, okButtonProps: { danger: true },
    onOk: async () => { try { await menuApi.remove(row.id); message.success('菜单已删除'); void load() } catch (error) { message.error(errorMessage(error)) } },
  })

  const columns: ColumnsType<AdminMenu> = [
    { title: '菜单名称', dataIndex: 'name', render: (value, row) => <div className="title-cell"><strong>{value}</strong>{row.permissionCode && <small className="mono">{row.permissionCode}</small>}</div> },
    { title: '类型', dataIndex: 'type', width: 100, render: value => <Tag bordered={false} color={value === 'DIR' ? 'blue' : value === 'BUTTON' ? 'default' : 'cyan'}>{typeLabels[value]}</Tag> },
    { title: '路由地址', dataIndex: 'path', render: value => value ? <span className="mono">{value}</span> : <span className="muted">-</span> },
    { title: '组件标识', dataIndex: 'componentKey', width: 180, render: value => value ? <span className="mono muted">{value}</span> : '-' },
    { title: '图标', dataIndex: 'icon', width: 170, render: value => value ? <span className="mono muted">{value}</span> : '-' },
    { title: '排序', dataIndex: 'sort', width: 70, align: 'center' },
    { title: '可见', dataIndex: 'visible', width: 75, render: value => <EnabledTag enabled={value} /> },
    { title: '操作', width: 145, render: (_, row) => <Space size={0}>{can('system:menu:update') && <Button type="link" size="small" icon={<EditOutlined />} onClick={() => openModal(row)}>编辑</Button>}{can('system:menu:delete') && <Button type="link" size="small" danger icon={<DeleteOutlined />} onClick={() => remove(row)} />}</Space> },
  ]

  return (
    <>
      <PageTitle title="菜单管理" eyebrow="System" description="后台菜单同时承载导航结构与按钮级权限标识。" extra={can('system:menu:create') && <Button type="primary" icon={<PlusOutlined />} onClick={() => openModal()}>新建菜单</Button>} />
      <div className="panel"><div className="panel-body"><Table rowKey="id" columns={columns} dataSource={rows} pagination={false} defaultExpandAllRows /></div></div>
      <Modal title={editing ? '编辑菜单' : '新建菜单'} open={open} onCancel={() => setOpen(false)} onOk={submit} okText="保存" cancelText="取消">
        <Form form={form} layout="vertical" style={{ marginTop: 18 }}>
          <Space size="large" style={{ display: 'flex' }}>
            <Form.Item label="菜单名称" name="name" rules={[{ required: true, message: '请输入名称' }]} style={{ flex: 1 }}><Input /></Form.Item>
            <Form.Item label="类型" name="type" rules={[{ required: true }]}><Select style={{ width: 130 }} options={Object.entries(typeLabels).map(([value, label]) => ({ value, label }))} /></Form.Item>
          </Space>
          <Form.Item label="上级菜单" name="parentId"><TreeSelect treeDefaultExpandAll treeData={[{ title: '顶级菜单', value: '0', children: treeSelectData(rows) }]} /></Form.Item>
          {type !== 'BUTTON' && <>
            <Form.Item label="路由地址" name="path" rules={[{ required: type !== 'DIR', message: '请输入路由地址' }]}><Input placeholder="/content/articles" /></Form.Item>
            <Form.Item label="组件标识" name="componentKey" extra="React 前端通过显式注册表映射，不直接执行后端代码。"><Input placeholder="content/article/list" /></Form.Item>
          </>}
          <Form.Item label="权限标识" name="permissionCode"><Input placeholder="content:article:list" /></Form.Item>
          {type !== 'BUTTON' && <Form.Item label="图标名称" name="icon"><Input placeholder="FileTextOutlined" /></Form.Item>}
          <Space size="large">
            <Form.Item label="排序" name="sort"><InputNumber min={0} /></Form.Item>
            <Form.Item label="可见" name="visible"><Select style={{ width: 100 }} options={[{ label: '显示', value: 1 }, { label: '隐藏', value: 0 }]} /></Form.Item>
            <Form.Item label="状态" name="status"><Select style={{ width: 100 }} options={[{ label: '启用', value: 1 }, { label: '停用', value: 0 }]} /></Form.Item>
          </Space>
        </Form>
      </Modal>
    </>
  )
}
