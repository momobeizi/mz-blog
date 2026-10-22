import { useEffect, useState } from 'react'
import { Button, Form, Input, InputNumber, Select, Space, Spin, message } from 'antd'
import { ArrowLeftOutlined, FileDoneOutlined, SaveOutlined, SendOutlined } from '@ant-design/icons'
import { useNavigate, useParams } from 'react-router-dom'
import { pageApi } from '@/api'
import { errorMessage } from '@/api/client'
import { MarkdownEditor } from '@/components/MarkdownEditor'
import { PageTitle } from '@/components/PageTitle'
import { RichTextEditor } from '@/components/RichTextEditor'

export function PageEditorPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const [form] = Form.useForm()
  const [loading, setLoading] = useState(!!id)
  const [saving, setSaving] = useState(false)
  const [format, setFormat] = useState<'HTML' | 'MARKDOWN'>('HTML')
  const status = Form.useWatch('status', form) || 'DRAFT'

  useEffect(() => {
    if (!id) { form.setFieldsValue({ contentFormat: 'HTML', status: 'DRAFT', sort: 0, content: '' }); return }
    pageApi.get(id).then(page => {
      form.setFieldsValue(page)
      setFormat(page.contentFormat)
    }).catch(error => message.error(errorMessage(error))).finally(() => setLoading(false))
  }, [id, form])

  const save = async (nextStatus?: string) => {
    try {
      const values = await form.validateFields()
      setSaving(true)
      const payload = { ...values, status: nextStatus || values.status || 'DRAFT' }
      if (id) await pageApi.update(id, payload)
      else {
        const created = await pageApi.create(payload)
        message.success('单页已创建')
        navigate(`/content/pages/${created.id}/edit`, { replace: true })
        return
      }
      message.success('单页已保存')
    } catch (error) {
      if ((error as { errorFields?: unknown }).errorFields) return
      message.error(errorMessage(error))
    } finally { setSaving(false) }
  }

  if (loading) return <div style={{ padding: 100, textAlign: 'center' }}><Spin size="large" /></div>

  return (
    <>
      <PageTitle title={id ? '编辑单页' : '新建单页'} eyebrow="Content / Page" description="独立页面不会出现在文章时间流中，但可以被导航自由挂载。" extra={<Space><Button icon={<ArrowLeftOutlined />} onClick={() => navigate('/content/pages')}>返回</Button><Button icon={<SaveOutlined />} loading={saving} onClick={() => save()}>保存</Button><Button type="primary" icon={<SendOutlined />} loading={saving} onClick={() => save('PUBLISHED')}>发布</Button></Space>} />
      <Form form={form} component={false}>
        <div className="editor-layout">
          <div className="editor-main">
            <div className="panel"><div className="editor-form">
              <div className="section-strip"><FileDoneOutlined /> 页面信息</div>
              <div style={{ height: 18 }} />
              <Form.Item label="页面标题" name="title" rules={[{ required: true, message: '请输入页面标题' }]}><Input placeholder="例如：关于本站" /></Form.Item>
              <Form.Item label="别名" name="slug" extra="留空将根据标题自动生成"><Input addonBefore="/p/" /></Form.Item>
              <Form.Item label="正文格式" name="contentFormat" rules={[{ required: true }]}>
                <Select disabled={!!id} onChange={value => setFormat(value)} options={[{ label: '富文本 HTML', value: 'HTML' }, { label: 'Markdown', value: 'MARKDOWN' }]} />
              </Form.Item>
              <Form.Item label="正文" name="content" rules={[{ required: true, message: '请输入正文内容' }]}>
                {format === 'MARKDOWN' ? <MarkdownEditor /> : <RichTextEditor />}
              </Form.Item>
            </div></div>
          </div>
          <div className="editor-side">
            <div className="panel side-field">
              <h3>页面设置</h3>
              <Form.Item label="状态" name="status"><Select options={[{ label: '草稿', value: 'DRAFT' }, { label: '已发布', value: 'PUBLISHED' }, { label: '已下线', value: 'OFFLINE' }]} /></Form.Item>
              <Form.Item label="排序" name="sort"><InputNumber min={0} style={{ width: '100%' }} /></Form.Item>
              <div className="muted" style={{ fontSize: 11, lineHeight: 1.7 }}>当前状态：{status === 'PUBLISHED' ? '已发布，可被前台访问' : '尚未发布'}</div>
            </div>
            <div className="panel side-field">
              <h3>SEO 设置</h3>
              <Form.Item label="SEO 标题" name="seoTitle"><Input /></Form.Item>
              <Form.Item label="SEO 关键词" name="seoKeywords"><Input /></Form.Item>
              <Form.Item label="SEO 描述" name="seoDescription"><Input.TextArea autoSize={{ minRows: 2, maxRows: 4 }} /></Form.Item>
            </div>
          </div>
        </div>
      </Form>
    </>
  )
}
