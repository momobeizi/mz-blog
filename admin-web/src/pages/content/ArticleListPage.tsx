import { useCallback, useEffect, useState } from 'react'
import { Button, Input, Modal, Select, Space, Table, Tag, message } from 'antd'
import type { ColumnsType } from 'antd/es/table'
import { DeleteOutlined, EditOutlined, PlusOutlined, SearchOutlined } from '@ant-design/icons'
import { useNavigate } from 'react-router-dom'
import dayjs from 'dayjs'
import { articleApi, categoryApi, tagApi } from '@/api'
import { errorMessage } from '@/api/client'
import { PageTitle } from '@/components/PageTitle'
import { StatusTag } from '@/components/StatusTag'
import { TableToolbar } from '@/components/TableToolbar'
import type { Article, Category, PageResult, Tag as TagType } from '@/types'
import { useAuthStore } from '@/stores/auth'

export function ArticleListPage() {
  const navigate = useNavigate()
  const can = useAuthStore(state => state.can)
  const [data, setData] = useState<PageResult<Article>>({ records: [], page: 1, size: 20, total: 0, pages: 0 })
  const [loading, setLoading] = useState(false)
  const [categories, setCategories] = useState<Category[]>([])
  const [tags, setTags] = useState<TagType[]>([])
  const [filters, setFilters] = useState({ keyword: '', status: undefined as string | undefined, categoryId: undefined as string | undefined, tagId: undefined as string | undefined })
  const [applied, setApplied] = useState(filters)
  const [page, setPage] = useState(1)
  const [size, setSize] = useState(20)

  const load = useCallback(async () => {
    setLoading(true)
    try {
      const result = await articleApi.page({ page, size, ...applied })
      setData(result)
    } catch (error) {
      message.error(errorMessage(error))
    } finally {
      setLoading(false)
    }
  }, [page, size, applied])

  useEffect(() => {
    Promise.all([categoryApi.list(), tagApi.list()])
      .then(([categoryList, tagList]) => { setCategories(categoryList); setTags(tagList) })
      .catch(error => message.error(errorMessage(error)))
  }, [])

  useEffect(() => { void load() }, [load])

  const changeStatus = async (article: Article, status: string) => {
    try {
      await articleApi.status(article.id, status)
      message.success(status === 'PUBLISHED' ? '文章已发布' : '文章已下线')
      void load()
    } catch (error) {
      message.error(errorMessage(error))
    }
  }

  const remove = (article: Article) => {
    Modal.confirm({
      title: '删除文章',
      content: `确定删除《${article.title}》吗？删除后不会出现在前台。`,
      okText: '删除',
      okButtonProps: { danger: true },
      cancelText: '取消',
      onOk: async () => {
        try {
          await articleApi.remove(article.id)
          message.success('文章已删除')
          void load()
        } catch (error) {
          message.error(errorMessage(error))
        }
      },
    })
  }

  const columns: ColumnsType<Article> = [
    {
      title: '文章', dataIndex: 'title', width: 340,
      render: (_, row) => (
        <div style={{ display: 'flex', gap: 10, alignItems: 'center' }}>
          <img className="table-cover" src={row.coverUrl || '/assets/covers/cover-1.svg'} alt="" />
          <div className="title-cell">
            <strong>{row.title} {row.isTop === 1 && <Tag color="gold" bordered={false}>置顶</Tag>}</strong>
            <small>/{row.slug}</small>
          </div>
        </div>
      ),
    },
    { title: '分类', dataIndex: 'categoryName', width: 110, render: value => value || '-' },
    { title: '标签', dataIndex: 'tagNames', width: 170, render: (value: string[]) => value?.length ? value.slice(0, 2).map(tag => <Tag key={tag} bordered={false}>{tag}</Tag>) : <span className="muted">-</span> },
    { title: '状态', dataIndex: 'status', width: 90, render: value => <StatusTag value={value} /> },
    { title: '浏览', dataIndex: 'viewCount', width: 72, align: 'right' },
    { title: '发布时间', dataIndex: 'publishedAt', width: 140, render: value => value ? dayjs(value).format('YYYY-MM-DD HH:mm') : <span className="muted">未发布</span> },
    {
      title: '操作', width: 190, fixed: 'right',
      render: (_, row) => (
        <Space size={0}>
          <Button type="link" size="small" icon={<EditOutlined />} onClick={() => navigate(`/content/articles/${row.id}/edit`)}>编辑</Button>
          {can('content:article:publish') && row.status !== 'PUBLISHED' && <Button type="link" size="small" onClick={() => changeStatus(row, 'PUBLISHED')}>发布</Button>}
          {can('content:article:publish') && row.status === 'PUBLISHED' && <Button type="link" size="small" onClick={() => changeStatus(row, 'OFFLINE')}>下线</Button>}
          {can('content:article:delete') && <Button type="link" size="small" danger icon={<DeleteOutlined />} onClick={() => remove(row)} />}
        </Space>
      ),
    },
  ]

  return (
    <>
      <PageTitle title="文章管理" eyebrow="Content" description="写下来，让时间拥有可以被翻阅的形状。" extra={can('content:article:create') && <Button type="primary" icon={<PlusOutlined />} onClick={() => navigate('/content/articles/new')}>新建文章</Button>} />
      <div className="panel">
        <div className="panel-body">
          <TableToolbar extra={<Space><Button type="primary" icon={<SearchOutlined />} onClick={() => { setPage(1); setApplied(filters) }}>搜索</Button><Button onClick={() => { const empty = { keyword: '', status: undefined, categoryId: undefined, tagId: undefined }; setFilters(empty); setApplied(empty); setPage(1) }}>清除参数</Button></Space>}>
            <Input allowClear placeholder="文章标题" value={filters.keyword} onChange={e => setFilters({ ...filters, keyword: e.target.value })} onPressEnter={() => setApplied(filters)} />
            <Select allowClear placeholder="文章状态" value={filters.status} onChange={value => setFilters({ ...filters, status: value })} options={[{ label: '草稿', value: 'DRAFT' }, { label: '已发布', value: 'PUBLISHED' }, { label: '已下线', value: 'OFFLINE' }]} />
            <Select allowClear placeholder="选择分类" value={filters.categoryId} onChange={value => setFilters({ ...filters, categoryId: value })} options={categories.map(item => ({ label: item.name, value: item.id }))} />
            <Select allowClear placeholder="选择标签" value={filters.tagId} onChange={value => setFilters({ ...filters, tagId: value })} options={tags.map(item => ({ label: item.name, value: item.id }))} />
          </TableToolbar>
          <Table
            rowKey="id"
            loading={loading}
            columns={columns}
            dataSource={data.records}
            scroll={{ x: 1120 }}
            pagination={{
              current: page, pageSize: size, total: data.total, showSizeChanger: true,
              showTotal: total => `共 ${total} 篇文章`,
              onChange: (nextPage, nextSize) => { setPage(nextPage); setSize(nextSize) },
            }}
          />
        </div>
      </div>
    </>
  )
}
