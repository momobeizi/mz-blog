import { useEffect, useState } from 'react'
import { Button, Form, Input, InputNumber, Modal, Select, Space, Table, message } from 'antd'
import type { ColumnsType } from 'antd/es/table'
import { DeleteOutlined, EditOutlined, PlusOutlined, SearchOutlined } from '@ant-design/icons'
import { linkApi } from '@/api'
import { errorMessage } from '@/api/client'
import { PageTitle } from '@/components/PageTitle'
import { StatusTag } from '@/components/StatusTag'
import type { LinkItem } from '@/types'
import { useAuthStore } from '@/stores/auth'

const typeLabels: Record<string, string> = { FRIEND: '友情链接', COMMON: '常用链接' }

export function LinkPage() {
  const can = useAuthStore(state => state.can)
  const [rows, setRows] = useState<LinkItem[]>([])
  const [linkType, setLinkType] = useState<string | undefined>()
  const [keyword, setKeyword] = useState('')
  const [open, setOpen] = useState(false)
  const [editing, setEditing] = useState<LinkItem | null>(null)
  const [form] = Form.useForm()

  const load = async () => {
    try { setRows(await linkApi.list({ linkType, keyword })) } catch (error) { message.error(errorMessage(error)) }
  }
  useEffect(() => { void load() }, [linkType])

  const openModal = (row?: LinkItem) => {
    setEditing(row || null); form.resetFields()
    form.setFieldsValue(row || { linkType: 'FRIEND', groupName: '默认分组', sort: 0, status: 1, openType: '_blank', nofollow: 0 })
    setOpen(true)
  }

  const submit = async () => {
    try {
      const values = await form.validateFields()
      if (editing) await linkApi.update(editing.id, values)
      else await linkApi.create(values)
      message.success(editing ? '链接已更新' : '链接已创建')
      setOpen(false); void load()
    } catch (error) {
      if ((error as { errorFields?: unknown }).errorFields) return
      message.error(errorMessage(error))
    }
  }

  const remove = (row: LinkItem) => Modal.confirm({
    title: '删除链接', content: `确定删除“${row.name}”吗？`, okButtonProps: { danger: true },
    onOk: async () => { try { await linkApi.remove(row.id); message.success('链接已删除'); void load() } catch (error) { message.error(errorMessage(error)) } },
  })

  const columns: ColumnsType<LinkItem> = [
    { title: '链接', dataIndex: 'name', render: (value, row) => <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>{row.logoUrl ? <img className="table-cover" src={row.logoUrl} alt="" /> : <div style={{ width: 46, height: 34, borderRadius: 5, background: '#edf5f5' }} />}<div className="title-cell"><strong>{value}</strong><small>{row.url}</small></div></div> },
    { title: '类型', dataIndex: 'linkType', width: 110, render: value => <StatusTag value={value} /> },
    { title: '分组', dataIndex: 'groupName', width: 110 },
    { title: '描述', dataIndex: 'description', render: value => value || <span className="muted">-</span> },
    { title: '排序', dataIndex: 'sort', width: 70, align: 'center' },
    { title: '操作', width: 140, render: (_, row) => <Space size={0}>{can('content:link:update') && <Button type="link" size="small" icon={<EditOutlined />} onClick={() => openModal(row)}>编辑</Button>}{can('content:link:delete') && <Button type="link" size="small" danger icon={<DeleteOutlined />} onClick={() => remove(row)} />}</Space> },
  ]

  return (
    <>
      <PageTitle title="链接管理" eyebrow="Navigation" description="整理友情链接与常用的外部入口。" extra={can('content:link:create') && <Button type="primary" icon={<PlusOutlined />} onClick={() => openModal()}>新建链接</Button>} />
      <div className="panel"><div className="panel-body">
        <div className="table-toolbar">
          <div className="table-toolbar-filters">
            <Select allowClear placeholder="链接类型" value={linkType} onChange={setLinkType} options={[{ label: '友情链接', value: 'FRIEND' }, { label: '常用链接', value: 'COMMON' }]} />
            <Input allowClear placeholder="名称或描述" value={keyword} onChange={e => setKeyword(e.target.value)} onPressEnter={() => void load()} />
          </div>
          <Button type="primary" icon={<SearchOutlined />} onClick={() => void load()}>搜索</Button>
        </div>
        <Table rowKey="id" columns={columns} dataSource={rows} pagination={false} />
      </div></div>
      <Modal title={editing ? '编辑链接' : '新建链接'} open={open} onCancel={() => setOpen(false)} onOk={submit} okText="保存" cancelText="取消">
        <Form form={form} layout="vertical" style={{ marginTop: 18 }}>
          <Form.Item label="类型" name="linkType" rules={[{ required: true }]}><Select options={[{ label: '友情链接', value: 'FRIEND' }, { label: '常用链接', value: 'COMMON' }]} /></Form.Item>
          <Form.Item label="名称" name="name" rules={[{ required: true, message: '请输入名称' }]}><Input /></Form.Item>
          <Form.Item label="地址" name="url" rules={[{ required: true, message: '请输入链接地址' }]}><Input placeholder="https://example.com" /></Form.Item>
          <Form.Item label="分组" name="groupName"><Input /></Form.Item>
          <Form.Item label="Logo 地址" name="logoUrl"><Input /></Form.Item>
          <Form.Item label="描述" name="description"><Input.TextArea autoSize={{ minRows: 2, maxRows: 4 }} /></Form.Item>
          <Space size="large">
            <Form.Item label="排序" name="sort"><InputNumber min={0} /></Form.Item>
            <Form.Item label="状态" name="status"><Select style={{ width: 100 }} options={[{ label: '启用', value: 1 }, { label: '停用', value: 0 }]} /></Form.Item>
            <Form.Item label="打开方式" name="openType"><Select style={{ width: 110 }} options={[{ label: '新窗口', value: '_blank' }, { label: '当前窗口', value: '_self' }]} /></Form.Item>
          </Space>
        </Form>
      </Modal>
    </>
  )
}
