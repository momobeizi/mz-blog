import { useEffect, useState } from 'react'
import { Button, Form, Input, Modal, Space, Table, message } from 'antd'
import type { ColumnsType } from 'antd/es/table'
import { DeleteOutlined, EditOutlined, PlusOutlined } from '@ant-design/icons'
import { tagApi } from '@/api'
import { errorMessage } from '@/api/client'
import { PageTitle } from '@/components/PageTitle'
import type { Tag } from '@/types'
import { useAuthStore } from '@/stores/auth'

export function TagPage() {
  const can = useAuthStore(state => state.can)
  const [rows, setRows] = useState<Tag[]>([])
  const [loading, setLoading] = useState(false)
  const [open, setOpen] = useState(false)
  const [editing, setEditing] = useState<Tag | null>(null)
  const [form] = Form.useForm()

  const load = async () => {
    setLoading(true)
    try { setRows(await tagApi.list()) } catch (error) { message.error(errorMessage(error)) } finally { setLoading(false) }
  }
  useEffect(() => { void load() }, [])

  const openModal = (row?: Tag) => {
    setEditing(row || null)
    form.resetFields()
    form.setFieldsValue(row || {})
    setOpen(true)
  }

  const submit = async () => {
    try {
      const values = await form.validateFields()
      if (editing) await tagApi.update(editing.id, values)
      else await tagApi.create(values)
      message.success(editing ? '标签已更新' : '标签已创建')
      setOpen(false)
      void load()
    } catch (error) {
      if ((error as { errorFields?: unknown }).errorFields) return
      message.error(errorMessage(error))
    }
  }

  const remove = (row: Tag) => Modal.confirm({
    title: '删除标签', content: `确定删除“${row.name}”吗？已被文章使用的标签不能删除。`, okButtonProps: { danger: true },
    onOk: async () => { try { await tagApi.remove(row.id); message.success('标签已删除'); void load() } catch (error) { message.error(errorMessage(error)) } },
  })

  const columns: ColumnsType<Tag> = [
    { title: '标签', dataIndex: 'name', render: (value, row) => <div className="title-cell"><strong>#{value}</strong><small>/{row.slug}</small></div> },
    { title: '描述', dataIndex: 'description', render: value => value || <span className="muted">-</span> },
    { title: '文章数', dataIndex: 'articleCount', width: 100, align: 'right' },
    { title: '操作', width: 150, render: (_, row) => <Space size={0}>{can('content:tag:update') && <Button type="link" size="small" icon={<EditOutlined />} onClick={() => openModal(row)}>编辑</Button>}{can('content:tag:delete') && <Button type="link" size="small" danger icon={<DeleteOutlined />} onClick={() => remove(row)} />}</Space> },
  ]

  return (
    <>
      <PageTitle title="标签管理" eyebrow="Content" description="用轻盈的关键词把不同文章连接起来。" extra={can('content:tag:create') && <Button type="primary" icon={<PlusOutlined />} onClick={() => openModal()}>新建标签</Button>} />
      <div className="panel"><div className="panel-body"><Table rowKey="id" loading={loading} columns={columns} dataSource={rows} pagination={false} /></div></div>
      <Modal title={editing ? '编辑标签' : '新建标签'} open={open} onCancel={() => setOpen(false)} onOk={submit} okText="保存" cancelText="取消">
        <Form form={form} layout="vertical" style={{ marginTop: 18 }}>
          <Form.Item label="标签名称" name="name" rules={[{ required: true, message: '请输入标签名称' }]}><Input placeholder="例如：设计" /></Form.Item>
          <Form.Item label="别名" name="slug" extra="留空则根据名称生成"><Input addonBefore="/tags/" /></Form.Item>
          <Form.Item label="描述" name="description"><Input.TextArea autoSize={{ minRows: 2, maxRows: 4 }} /></Form.Item>
        </Form>
      </Modal>
    </>
  )
}
