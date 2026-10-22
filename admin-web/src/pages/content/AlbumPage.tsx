import { useEffect, useState } from 'react'
import { Button, Form, Input, InputNumber, Modal, Select, Space, Table, Upload, message } from 'antd'
import type { ColumnsType } from 'antd/es/table'
import { DeleteOutlined, EditOutlined, MinusCircleOutlined, PlusOutlined, UploadOutlined } from '@ant-design/icons'
import { albumApi, mediaApi } from '@/api'
import { errorMessage } from '@/api/client'
import { PageTitle } from '@/components/PageTitle'
import { EnabledTag } from '@/components/StatusTag'
import type { Album, AlbumPhoto } from '@/types'
import { useAuthStore } from '@/stores/auth'

export function AlbumPage() {
  const can = useAuthStore(state => state.can)
  const [rows, setRows] = useState<Album[]>([])
  const [loading, setLoading] = useState(false)
  const [open, setOpen] = useState(false)
  const [editing, setEditing] = useState<Album | null>(null)
  const [saving, setSaving] = useState(false)
  const [form] = Form.useForm()

  const load = async () => {
    setLoading(true)
    try { setRows(await albumApi.list()) } catch (error) { message.error(errorMessage(error)) } finally { setLoading(false) }
  }
  useEffect(() => { void load() }, [])

  const openModal = (row?: Album) => {
    setEditing(row || null)
    form.resetFields()
    form.setFieldsValue(row ? { ...row, photos: row.photos || [] } : { status: 1, sort: 0, photos: [] })
    setOpen(true)
  }

  const submit = async () => {
    try {
      const values = await form.validateFields()
      const photos = (values.photos || []).filter((photo: AlbumPhoto) => photo?.imageUrl)
      const payload = { ...values, photos, coverUrl: values.coverUrl || photos[0]?.imageUrl }
      setSaving(true)
      if (editing) await albumApi.update(editing.id, payload)
      else await albumApi.create(payload)
      message.success(editing ? '相册已更新' : '相册已创建')
      setOpen(false)
      void load()
    } catch (error) {
      if ((error as { errorFields?: unknown }).errorFields) return
      message.error(errorMessage(error))
    } finally { setSaving(false) }
  }

  const remove = (row: Album) => Modal.confirm({
    title: '删除相册', content: `确定删除“${row.title}”及其中照片记录吗？`, okButtonProps: { danger: true },
    onOk: async () => { try { await albumApi.remove(row.id); message.success('相册已删除'); void load() } catch (error) { message.error(errorMessage(error)) } },
  })

  const columns: ColumnsType<Album> = [
    { title: '相册', dataIndex: 'title', render: (value, row) => <div style={{ display: 'flex', gap: 11, alignItems: 'center' }}><img className="table-cover" src={row.coverUrl || '/assets/covers/cover-3.svg'} alt="" /><div className="title-cell"><strong>{value}</strong><small>/{row.slug}</small></div></div> },
    { title: '描述', dataIndex: 'description', render: value => value || <span className="muted">-</span> },
    { title: '照片数', dataIndex: 'photos', width: 90, align: 'right', render: (photos: AlbumPhoto[]) => photos?.length || 0 },
    { title: '排序', dataIndex: 'sort', width: 75, align: 'center' },
    { title: '状态', dataIndex: 'status', width: 85, render: value => <EnabledTag enabled={value} /> },
    { title: '操作', width: 150, render: (_, row) => <Space size={0}>{can('content:album:update') && <Button type="link" size="small" icon={<EditOutlined />} onClick={() => openModal(row)}>编辑</Button>}{can('content:album:delete') && <Button type="link" size="small" danger icon={<DeleteOutlined />} onClick={() => remove(row)} />}</Space> },
  ]

  return (
    <>
      <PageTitle title="相册管理" eyebrow="Content" description="收集光线、街道与日常里不经意的片刻。" extra={can('content:album:create') && <Button type="primary" icon={<PlusOutlined />} onClick={() => openModal()}>新建相册</Button>} />
      <div className="panel"><div className="panel-body"><Table rowKey="id" loading={loading} columns={columns} dataSource={rows} pagination={false} /></div></div>
      <Modal title={editing ? '编辑相册' : '新建相册'} open={open} onCancel={() => setOpen(false)} onOk={submit} confirmLoading={saving} okText="保存" cancelText="取消" width={720}>
        <Form form={form} layout="vertical" style={{ marginTop: 18 }}>
          <Space size="large" style={{ display: 'flex' }}>
            <Form.Item label="相册标题" name="title" rules={[{ required: true, message: '请输入标题' }]} style={{ flex: 1 }}><Input /></Form.Item>
            <Form.Item label="别名" name="slug" style={{ flex: 1 }}><Input addonBefore="/albums/" /></Form.Item>
          </Space>
          <Form.Item label="描述" name="description"><Input.TextArea autoSize={{ minRows: 2, maxRows: 4 }} /></Form.Item>
          <Space size="large" style={{ display: 'flex' }}>
            <Form.Item label="封面地址" name="coverUrl" style={{ flex: 1 }}><Input placeholder="留空使用第一张照片" /></Form.Item>
            <Form.Item label="排序" name="sort"><InputNumber min={0} /></Form.Item>
            <Form.Item label="状态" name="status"><Select style={{ width: 110 }} options={[{ label: '启用', value: 1 }, { label: '停用', value: 0 }]} /></Form.Item>
          </Space>
          <div className="section-strip" style={{ marginBottom: 12 }}>照片列表</div>
          <Form.List name="photos">
            {(fields, { add, remove }) => (
              <>
                {fields.map(field => (
                  <div key={field.key} style={{ display: 'grid', gridTemplateColumns: '1fr 1fr auto', gap: 8, alignItems: 'start', marginBottom: 8 }}>
                    <Form.Item name={[field.name, 'imageUrl']} rules={[{ required: true, message: '请输入图片地址' }]} style={{ margin: 0 }}><Input placeholder="图片 URL" /></Form.Item>
                    <Form.Item name={[field.name, 'caption']} style={{ margin: 0 }}><Input placeholder="照片说明" /></Form.Item>
                    <Button type="text" danger icon={<MinusCircleOutlined />} onClick={() => remove(field.name)} />
                  </div>
                ))}
                <Space wrap>
                  <Button type="dashed" icon={<PlusOutlined />} onClick={() => add({ imageUrl: '', caption: '' })}>添加图片地址</Button>
                  <Upload
                    accept="image/*"
                    showUploadList={false}
                    customRequest={async ({ file, onSuccess, onError }) => {
                      try {
                        const media = await mediaApi.upload(file as File)
                        add({ imageUrl: media.url, thumbUrl: media.thumbUrl, mediaId: media.id, caption: media.originalName })
                        onSuccess?.(media)
                        message.success('图片已上传并加入相册')
                      } catch (error) {
                        onError?.(error as Error)
                        message.error(errorMessage(error))
                      }
                    }}
                  >
                    <Button icon={<UploadOutlined />}>上传并添加</Button>
                  </Upload>
                </Space>
              </>
            )}
          </Form.List>
        </Form>
      </Modal>
    </>
  )
}
