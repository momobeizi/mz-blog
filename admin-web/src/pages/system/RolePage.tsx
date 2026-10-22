import { useEffect, useState } from 'react'
import { Button, Form, Input, InputNumber, Modal, Select, Space, Table, Tag, Tree, message } from 'antd'
import type { ColumnsType } from 'antd/es/table'
import type { DataNode } from 'antd/es/tree'
import { DeleteOutlined, EditOutlined, PlusOutlined } from '@ant-design/icons'
import { menuApi, roleApi } from '@/api'
import { errorMessage } from '@/api/client'
import { PageTitle } from '@/components/PageTitle'
import { EnabledTag } from '@/components/StatusTag'
import type { AdminMenu, Role } from '@/types'
import { useAuthStore } from '@/stores/auth'

function menuTreeData(items: AdminMenu[]): DataNode[] {
  return items.map(item => ({
    key: item.id,
    title: <span>{item.name} {item.type === 'BUTTON' && <Tag bordered={false} style={{ fontSize: 9 }}>按钮</Tag>}</span>,
    children: item.children?.length ? menuTreeData(item.children) : undefined,
  }))
}

export function RolePage() {
  const can = useAuthStore(state => state.can)
  const [rows, setRows] = useState<Role[]>([])
  const [menus, setMenus] = useState<AdminMenu[]>([])
  const [open, setOpen] = useState(false)
  const [editing, setEditing] = useState<Role | null>(null)
  const [checkedKeys, setCheckedKeys] = useState<string[]>([])
  const [form] = Form.useForm()

  const load = async () => { try { setRows(await roleApi.list()) } catch (error) { message.error(errorMessage(error)) } }
  useEffect(() => {
    void load()
    menuApi.tree().then(setMenus).catch(error => message.error(errorMessage(error)))
  }, [])

  const openModal = async (row?: Role) => {
    setEditing(row || null); form.resetFields()
    form.setFieldsValue(row || { status: 1, sort: 0 })
    if (row) {
      try { setCheckedKeys((await roleApi.get(row.id)).menuIds) } catch (error) { message.error(errorMessage(error)) }
    } else setCheckedKeys([])
    setOpen(true)
  }

  const submit = async () => {
    try {
      const values = await form.validateFields()
      const payload = { ...values, menuIds: checkedKeys }
      if (editing) await roleApi.update(editing.id, payload)
      else await roleApi.create(payload)
      message.success(editing ? '角色已更新' : '角色已创建')
      setOpen(false); void load()
    } catch (error) {
      if ((error as { errorFields?: unknown }).errorFields) return
      message.error(errorMessage(error))
    }
  }

  const remove = (row: Role) => Modal.confirm({
    title: '删除角色', content: `确定删除角色“${row.name}”吗？`, okButtonProps: { danger: true },
    onOk: async () => { try { await roleApi.remove(row.id); message.success('角色已删除'); void load() } catch (error) { message.error(errorMessage(error)) } },
  })

  const columns: ColumnsType<Role> = [
    { title: '角色名称', dataIndex: 'name', render: (value, row) => <div className="title-cell"><strong>{value}</strong><small>{row.code}</small></div> },
    { title: '描述', dataIndex: 'description', render: value => value || <span className="muted">-</span> },
    { title: '用户数', dataIndex: 'userCount', width: 90, align: 'right' },
    { title: '排序', dataIndex: 'sort', width: 70, align: 'center' },
    { title: '状态', dataIndex: 'status', width: 80, render: value => <EnabledTag enabled={value} /> },
    { title: '类型', dataIndex: 'builtIn', width: 90, render: value => value === 1 ? <Tag color="gold" bordered={false}>内置</Tag> : <Tag bordered={false}>自定义</Tag> },
    { title: '操作', width: 150, render: (_, row) => <Space size={0}>{can('system:role:update') && <Button type="link" size="small" icon={<EditOutlined />} onClick={() => openModal(row)}>编辑</Button>}{can('system:role:delete') && row.builtIn !== 1 && <Button type="link" size="small" danger icon={<DeleteOutlined />} onClick={() => remove(row)} />}</Space> },
  ]

  return (
    <>
      <PageTitle title="角色管理" eyebrow="System" description="以角色组织权限，再把角色分配给后台用户。" extra={can('system:role:create') && <Button type="primary" icon={<PlusOutlined />} onClick={() => openModal()}>新建角色</Button>} />
      <div className="panel"><div className="panel-body"><Table rowKey="id" columns={columns} dataSource={rows} pagination={false} /></div></div>
      <Modal title={editing ? '编辑角色' : '新建角色'} open={open} onCancel={() => setOpen(false)} onOk={submit} okText="保存" cancelText="取消" width={700}>
        <Form form={form} layout="vertical" style={{ marginTop: 18 }}>
          <Space size="large" style={{ display: 'flex' }}>
            <Form.Item label="角色名称" name="name" rules={[{ required: true, message: '请输入角色名称' }]} style={{ flex: 1 }}><Input /></Form.Item>
            <Form.Item label="角色编码" name="code" rules={[{ required: true, message: '请输入角色编码' }]} style={{ flex: 1 }}><Input disabled={editing?.builtIn === 1} placeholder="例如 EDITOR" /></Form.Item>
          </Space>
          <Form.Item label="描述" name="description"><Input.TextArea autoSize={{ minRows: 2, maxRows: 3 }} /></Form.Item>
          <Space size="large">
            <Form.Item label="排序" name="sort"><InputNumber min={0} /></Form.Item>
            <Form.Item label="状态" name="status"><Select style={{ width: 110 }} options={[{ label: '启用', value: 1 }, { label: '停用', value: 0 }]} /></Form.Item>
          </Space>
          <div className="section-strip" style={{ marginBottom: 10 }}>菜单与操作权限</div>
          <div style={{ maxHeight: 320, overflow: 'auto', border: '1px solid #e7eef0', borderRadius: 7, padding: 12 }}>
            <Tree
              checkable
              defaultExpandAll
              treeData={menuTreeData(menus)}
              checkedKeys={checkedKeys}
              onCheck={keys => setCheckedKeys((Array.isArray(keys) ? keys : keys.checked).map(String))}
            />
          </div>
        </Form>
      </Modal>
    </>
  )
}
