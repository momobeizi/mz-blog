import { useEffect, useState } from 'react'
import { Button, Form, Input, InputNumber, Modal, Select, Space, Table, message } from 'antd'
import type { ColumnsType } from 'antd/es/table'
import { DeleteOutlined, EditOutlined, PlusOutlined } from '@ant-design/icons'
import { navApi } from '@/api'
import { errorMessage } from '@/api/client'
import { PageTitle } from '@/components/PageTitle'
import { EnabledTag } from '@/components/StatusTag'
import type { NavMenu } from '@/types'
import { useAuthStore } from '@/stores/auth'

const typeLabels: Record<string, string> = { HOME: '首页', CATEGORY: '分类', PAGE: '单页', ALBUM: '相册', CUSTOM: '自定义' }

export function NavMenuPage() {
  const can = useAuthStore(state => state.can)
  const [rows, setRows] = useState<NavMenu[]>([])
  const [flat, setFlat] = useState<NavMenu[]>([])
  const [targets, setTargets] = useState<Record<string, { label: string; value: string }[]>>({})
  const [open, setOpen] = useState(false)
  const [editing, setEditing] = useState<NavMenu | null>(null)
  const [form] = Form.useForm()
  const menuType = Form.useWatch('menuType', form)

  const flatten = (items: NavMenu[]): NavMenu[] => items.flatMap(item => [item, ...flatten(item.children || [])])
  const load = async () => {
    try {
      const [tree, targetData] = await Promise.all([navApi.tree(), navApi.targets()])
      setRows(tree); setFlat(flatten(tree)); setTargets(targetData)
    } catch (error) { message.error(errorMessage(error)) }
  }
  useEffect(() => { void load() }, [])

  const openModal = (row?: NavMenu) => {
    setEditing(row || null)
    form.resetFields()
    form.setFieldsValue(row || { parentId: '0', menuType: 'CUSTOM', sort: 0, visible: 1, status: 1, openType: '_self' })
    setOpen(true)
  }

  const submit = async () => {
    try {
      const values = await form.validateFields()
      if (editing) await navApi.update(editing.id, values)
      else await navApi.create(values)
      message.success(editing ? '导航已更新' : '导航已创建')
      setOpen(false); void load()
    } catch (error) {
      if ((error as { errorFields?: unknown }).errorFields) return
      message.error(errorMessage(error))
    }
  }

  const remove = (row: NavMenu) => Modal.confirm({
    title: '删除导航', content: `确定删除“${row.name}”吗？`, okButtonProps: { danger: true },
    onOk: async () => { try { await navApi.remove(row.id); message.success('导航已删除'); void load() } catch (error) { message.error(errorMessage(error)) } },
  })

  const columns: ColumnsType<NavMenu> = [
    { title: '菜单名称', dataIndex: 'name', render: value => <strong style={{ fontSize: 12 }}>{value}</strong> },
    { title: '类型', dataIndex: 'menuType', width: 100, render: value => typeLabels[value] || value },
    { title: '目标', dataIndex: 'resolvedUrl', render: value => <span className="mono muted">{value || '-'}</span> },
    { title: '排序', dataIndex: 'sort', width: 70, align: 'center' },
    { title: '可见', dataIndex: 'visible', width: 80, render: value => <EnabledTag enabled={value} /> },
    { title: '操作', width: 140, render: (_, row) => <Space size={0}>{can('content:nav:update') && <Button type="link" size="small" icon={<EditOutlined />} onClick={() => openModal(row)}>编辑</Button>}{can('content:nav:delete') && <Button type="link" size="small" danger icon={<DeleteOutlined />} onClick={() => remove(row)} />}</Space> },
  ]

  return (
    <>
      <PageTitle title="前台菜单" eyebrow="Navigation" description="配置访客在站点顶部看到的导航路径。" extra={can('content:nav:create') && <Button type="primary" icon={<PlusOutlined />} onClick={() => openModal()}>新建菜单</Button>} />
      <div className="panel"><div className="panel-body"><Table rowKey="id" columns={columns} dataSource={rows} pagination={false} defaultExpandAllRows /></div></div>
      <Modal title={editing ? '编辑导航' : '新建导航'} open={open} onCancel={() => setOpen(false)} onOk={submit} okText="保存" cancelText="取消">
        <Form form={form} layout="vertical" style={{ marginTop: 18 }}>
          <Form.Item label="菜单名称" name="name" rules={[{ required: true, message: '请输入菜单名称' }]}><Input /></Form.Item>
          <Form.Item label="菜单类型" name="menuType" rules={[{ required: true }]}>
            <Select options={Object.entries(typeLabels).map(([value, label]) => ({ value, label }))} />
          </Form.Item>
          {['CATEGORY', 'PAGE', 'ALBUM'].includes(menuType) && <Form.Item label="目标内容" name="targetId" rules={[{ required: true, message: '请选择目标内容' }]}><Select options={targets[menuType] || []} /></Form.Item>}
          {menuType === 'CUSTOM' && <Form.Item label="链接地址" name="url" rules={[{ required: true, message: '请输入链接地址' }]}><Input placeholder="/articles 或 https://example.com" /></Form.Item>}
          <Form.Item label="上级菜单" name="parentId"><Select options={[{ label: '顶级菜单', value: '0' }, ...flat.filter(item => item.id !== editing?.id).map(item => ({ label: item.name, value: item.id }))]} /></Form.Item>
          <Space size="large">
            <Form.Item label="打开方式" name="openType"><Select style={{ width: 120 }} options={[{ label: '当前窗口', value: '_self' }, { label: '新窗口', value: '_blank' }]} /></Form.Item>
            <Form.Item label="排序" name="sort"><InputNumber min={0} /></Form.Item>
            <Form.Item label="可见" name="visible"><Select style={{ width: 100 }} options={[{ label: '显示', value: 1 }, { label: '隐藏', value: 0 }]} /></Form.Item>
          </Space>
        </Form>
      </Modal>
    </>
  )
}
