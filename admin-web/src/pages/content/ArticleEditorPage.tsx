import { useEffect, useState } from 'react'
import { Button, Collapse, Form, Input, Select, Space, Spin, Switch, message } from 'antd'
import { ArrowLeftOutlined, EyeOutlined, FileTextOutlined, SaveOutlined, SendOutlined } from '@ant-design/icons'
import { useNavigate, useParams } from 'react-router-dom'
import { articleApi, categoryApi, tagApi } from '@/api'
import { errorMessage } from '@/api/client'
import { MarkdownEditor } from '@/components/MarkdownEditor'
import { PageTitle } from '@/components/PageTitle'
import { RichTextEditor } from '@/components/RichTextEditor'
import type { Category, Tag } from '@/types'

interface FormValues {
  title: string
  slug?: string
  summary?: string
  coverUrl?: string
  content: string
  contentFormat: 'HTML' | 'MARKDOWN'
  categoryId: string
  status: 'DRAFT' | 'PUBLISHED' | 'OFFLINE'
  isTop: boolean
  isRecommended: boolean
  tagIds?: string[]
  seoTitle?: string
  seoKeywords?: string
  seoDescription?: string
}

export function ArticleEditorPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const [form] = Form.useForm<FormValues>()
  const [categories, setCategories] = useState<Category[]>([])
  const [tags, setTags] = useState<Tag[]>([])
  const [loading, setLoading] = useState(!!id)
  const [saving, setSaving] = useState(false)
  const [format, setFormat] = useState<'HTML' | 'MARKDOWN'>('HTML')
  const coverUrl = Form.useWatch('coverUrl', form)

  useEffect(() => {
    Promise.all([categoryApi.list(), tagApi.list()])
      .then(([categoryList, tagList]) => { setCategories(categoryList); setTags(tagList) })
      .catch(error => message.error(errorMessage(error)))
  }, [])

  useEffect(() => {
    if (!id) {
      form.setFieldsValue({ contentFormat: 'HTML', status: 'DRAFT', isTop: false, isRecommended: false, content: '' })
      return
    }
    articleApi.get(id).then(article => {
      form.setFieldsValue({
        ...article,
        isTop: article.isTop === 1,
        isRecommended: article.isRecommended === 1,
        tagIds: article.tagIds || [],
        contentFormat: article.contentFormat,
      })
      setFormat(article.contentFormat)
    }).catch(error => message.error(errorMessage(error))).finally(() => setLoading(false))
  }, [id, form])

  const save = async (status: 'DRAFT' | 'PUBLISHED' | 'OFFLINE') => {
    try {
      const values = await form.validateFields()
      setSaving(true)
      const payload = {
        ...values,
        status,
        isTop: values.isTop ? 1 : 0,
        isRecommended: values.isRecommended ? 1 : 0,
      }
      if (id) await articleApi.update(id, payload)
      else {
        const created = await articleApi.create(payload)
        message.success(status === 'PUBLISHED' ? '文章已发布' : '草稿已保存')
        navigate(`/content/articles/${created.id}/edit`, { replace: true })
        return
      }
      message.success(status === 'PUBLISHED' ? '文章已发布' : '内容已保存')
    } catch (error) {
      if ((error as { errorFields?: unknown }).errorFields) return
      message.error(errorMessage(error))
    } finally {
      setSaving(false)
    }
  }

  if (loading) return <div style={{ padding: 100, textAlign: 'center' }}><Spin size="large" /></div>

  return (
    <>
      <PageTitle
        title={id ? '编辑文章' : '新建文章'}
        eyebrow="Content / Article"
        description={id ? '修改内容后保存，发布状态会同步到前台。' : '选择一种你喜欢的写作方式，剩下的交给系统。'}
        extra={<Space><Button icon={<ArrowLeftOutlined />} onClick={() => navigate('/content/articles')}>返回</Button>{id && <Button icon={<EyeOutlined />} onClick={() => window.open(`/admin/preview/articles/${id}`, '_blank')}>预览</Button>}<Button icon={<SaveOutlined />} loading={saving} onClick={() => save('DRAFT')}>保存草稿</Button><Button type="primary" icon={<SendOutlined />} loading={saving} onClick={() => save('PUBLISHED')}>发布</Button></Space>}
      />
      <Form form={form} component={false}>
        <div className="editor-layout">
          <div className="editor-main">
            <div className="panel">
              <div className="editor-form">
                <div className="section-strip"><FileTextOutlined /> 文章信息</div>
                <div style={{ height: 18 }} />
                <Form.Item label="标题" name="title" rules={[{ required: true, message: '请输入文章标题' }]}>
                  <Input placeholder="给这篇文章一个标题" maxLength={200} showCount />
                </Form.Item>
                <Form.Item label="别名" name="slug" extra="留空将根据标题自动生成，用于文章访问地址。">
                  <Input addonBefore="/articles/" placeholder="a-garden-to-return" />
                </Form.Item>
                <Form.Item label="摘要" name="summary">
                  <Input.TextArea autoSize={{ minRows: 2, maxRows: 4 }} placeholder="用一两句话介绍这篇文章，留空会自动摘取正文。" maxLength={500} />
                </Form.Item>
                <Form.Item label="正文格式" name="contentFormat" rules={[{ required: true }]}>
                  <Select
                    disabled={!!id}
                    onChange={value => setFormat(value)}
                    options={[{ label: '富文本 HTML（适合图文混排）', value: 'HTML' }, { label: 'Markdown（适合技术写作）', value: 'MARKDOWN' }]}
                  />
                </Form.Item>
                <Form.Item label="正文" name="content" rules={[{ required: true, message: '请输入正文内容' }]}>
                  {format === 'MARKDOWN' ? <MarkdownEditor /> : <RichTextEditor />}
                </Form.Item>
              </div>
            </div>
            <div className="panel">
              <Collapse ghost items={[{
                key: 'seo', label: 'SEO 与分享设置',
                children: (
                  <div style={{ padding: '0 8px 10px' }}>
                    <Form.Item label="SEO 标题" name="seoTitle"><Input placeholder="留空则使用文章标题" /></Form.Item>
                    <Form.Item label="SEO 关键词" name="seoKeywords"><Input placeholder="使用英文逗号分隔" /></Form.Item>
                    <Form.Item label="SEO 描述" name="seoDescription"><Input.TextArea autoSize={{ minRows: 2, maxRows: 4 }} /></Form.Item>
                  </div>
                ),
              }]} />
            </div>
          </div>
          <div className="editor-side">
            <div className="panel side-field">
              <h3>发布设置</h3>
              <Form.Item label="分类" name="categoryId" rules={[{ required: true, message: '请选择分类' }]}>
                <Select placeholder="选择分类" options={categories.map(item => ({ label: item.name, value: item.id }))} />
              </Form.Item>
              <Form.Item label="标签" name="tagIds">
                <Select mode="multiple" placeholder="选择标签" options={tags.map(item => ({ label: item.name, value: item.id }))} />
              </Form.Item>
              <div className="switch-row"><span>置顶文章</span><Form.Item name="isTop" valuePropName="checked" noStyle><Switch /></Form.Item></div>
              <div className="switch-row"><span>推荐到首页</span><Form.Item name="isRecommended" valuePropName="checked" noStyle><Switch /></Form.Item></div>
            </div>
            <div className="panel side-field">
              <h3>封面</h3>
              <Form.Item name="coverUrl" noStyle><Input placeholder="/assets/covers/cover-1.svg" /></Form.Item>
              <img className="cover-preview" src={coverUrl || '/assets/covers/cover-1.svg'} alt="封面预览" />
              <div className="muted" style={{ fontSize: 10, marginTop: 8 }}>可填写媒体库地址或外链图片</div>
            </div>
          </div>
        </div>
      </Form>
    </>
  )
}
