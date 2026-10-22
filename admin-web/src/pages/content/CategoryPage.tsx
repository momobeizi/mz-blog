import { useEffect, useState } from 'react'
import { Button, Form, Input, InputNumber, Modal, Select, Space, Table, message } from 'antd'
import type { ColumnsType } from 'antd/es/table'
import { DeleteOutlined, EditOutlined, PlusOutlined } from '@ant-design/icons'
import { categoryApi } from '@/api'
import { errorMessage } from '@/api/client'
import { PageTitle } from '@/components/PageTitle'
import { EnabledTag } from '@/components/StatusTag'
import type { Category } from '@/types'
import { useAuthStore } from '@/stores/auth'

export function CategoryPage() {
  const can = useAuthStore(state => state.can)
  const [rows, setRows] = useState<Category[]>([])
  const [loading, setLoading] = useState(false)
  const [open, setOpen] = useState(false)
  const [editing, setEditing] = useState<Category | null>(null)
  const [saving, setSaving] = useState(false)
  const [form] = Form.useForm()

  const load = async () => {
    setLoading(true)
    try { setRows(await categoryApi.list()) } catch (error) { message.error(errorMessage(error)) } finally { setLoading(false) }
  }
  useEffect(() => { void load() }, [])

  const openModal = (row?: Category) => {
    setEditing(row || null)
    form.resetFields()
    form.setFieldsValue(row || { parentId: '0', sort: 0, status: 1 })
    setOpen(true)
  }

  const submit = async () => {
    try {
      const values = await form.validateFields()
      setSaving(true)
      if (editing) await categoryApi.update(editing.id, values)
      else await categoryApi.create(values)
      message.success(editing ? '分类已更新' : '分类已创建')
      setOpen(false)
      void load()
    } catch (error) {
      if ((error as { errorFields?: unknown }).errorFields) return
      message.error(errorMessage(error))
    } finally { setSaving(false) }
  }

  const remove = (row: Category) => Modal.confirm({
    title: '删除分类', content: `确定删除分类“${row.name}”吗？`, okButtonProps: { danger: true },
    onOk: async () => {
      try { await categoryApi.remove(row.id); message.success('分类已删除'); void load() } catch (error) { message.error(errorMessage(error)) }
    },
  })

  const columns: ColumnsType<Category> = [
    { title: '分类名称', dataIndex: 'name', render: (value, row) => <div className="title-cell"><strong>{value}</strong><small>/{row.slug}</small></div> },
    { title: '描述', dataIndex: 'description', render: value => value || <span className="muted">-</span> },
    { title: '文章数', dataIndex: 'articleCount', width: 90, align: 'right' },
    { title: '排序', dataIndex: 'sort', width: 75, align: 'center' },
    { title: '状态', dataIndex: 'status', width: 85, render: value => <EnabledTag enabled={value} /> },
    { title: '操作', width: 145, render: (_, row) => <Space size={0}>{can('content:category:update') && <Button type="link" size="small" icon={<EditOutlined />} onClick={() => openModal(row)}>编辑</Button>}{can('content:category:delete') && <Button type="link" size="small" danger icon={<DeleteOutlined />} onClick={() => remove(row)} />}</Space> },
  ]

  return (
    <>
      <PageTitle title="分类管理" eyebrow="Content" description="为文章建立清晰、稳定的路径。" extra={can('content:category:create') && <Button type="primary" icon={<PlusOutlined />} onClick={() => openModal()}>新建分类</Button>} />
      <div className="panel"><div className="panel-body"><Table rowKey="id" loading={loading} columns={columns} dataSource={rows} pagination={false} /></div></div>
      <Modal title={editing ? '编辑分类' : '新建分类'} open={open} onCancel={() => setOpen(false)} onOk={submit} confirmLoading={saving} okText="保存" cancelText="取消">
        <Form form={form} layout="vertical" style={{ marginTop: 18 }}>
          <Form.Item label="分类名称" name="name" rules={[{ required: true, message: '请输入分类名称' }]}><Input /></Form.Item>
          <Form.Item label="别名" name="slug" extra="留空则根据名称生成"><Input addonBefore="/categories/" /></Form.Item>
          <Form.Item label="上级分类" name="parentId"><Select options={[{ label: '顶级分类', value: '0' }, ...rows.filter(item => item.id !== editing?.id).map(item => ({ label: item.name, value: item.id }))]} /></Form.Item>
          <Form.Item label="描述" name="description"><Input.TextArea autoSize={{ minRows: 2, maxRows: 4 }} /></Form.Item>
          <Form.Item label="封面地址" name="coverUrl"><Input placeholder="/assets/covers/cover-1.svg" /></Form.Item>
          <Space size="large">
            <Form.Item label="排序" name="sort"><InputNumber min={0} /></Form.Item>
            <Form.Item label="状态" name="status"><Select style={{ width: 120 }} options={[{ label: '启用', value: 1 }, { label: '停用', value: 0 }]} /></Form.Item>
          </Space>
          <Form.Item label="SEO 标题" name="seoTitle"><Input /></Form.Item>
          <Form.Item label="SEO 关键词" name="seoKeywords"><Input /></Form.Item>
          <Form.Item label="SEO 描述" name="seoDescription"><Input.TextArea autoSize={{ minRows: 2, maxRows: 3 }} /></Form.Item>
        </Form>
      </Modal>
    </>
  )
}
