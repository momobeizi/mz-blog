import { useEffect, useState } from 'react'
import { Button, Input, Modal, Space, Table, message } from 'antd'
import type { ColumnsType } from 'antd/es/table'
import { DeleteOutlined, EditOutlined, PlusOutlined, SearchOutlined } from '@ant-design/icons'
import { useNavigate } from 'react-router-dom'
import dayjs from 'dayjs'
import { pageApi } from '@/api'
import { errorMessage } from '@/api/client'
import { PageTitle } from '@/components/PageTitle'
import { StatusTag } from '@/components/StatusTag'
import { TableToolbar } from '@/components/TableToolbar'
import type { PageDocument, PageResult } from '@/types'
import { useAuthStore } from '@/stores/auth'

export function PageListPage() {
  const navigate = useNavigate()
  const can = useAuthStore(state => state.can)
  const [data, setData] = useState<PageResult<PageDocument>>({ records: [], page: 1, size: 20, total: 0, pages: 0 })
  const [keyword, setKeyword] = useState('')
  const [loading, setLoading] = useState(false)
  const [page, setPage] = useState(1)

  const load = async () => {
    setLoading(true)
    try { setData(await pageApi.page({ page, size: 20, keyword })) } catch (error) { message.error(errorMessage(error)) } finally { setLoading(false) }
  }
  useEffect(() => { void load() }, [page])

  const remove = (row: PageDocument) => Modal.confirm({
    title: '删除单页', content: `确定删除“${row.title}”吗？`, okButtonProps: { danger: true },
    onOk: async () => { try { await pageApi.remove(row.id); message.success('页面已删除'); void load() } catch (error) { message.error(errorMessage(error)) } },
  })

  const columns: ColumnsType<PageDocument> = [
    { title: '页面标题', dataIndex: 'title', render: (value, row) => <div className="title-cell"><strong>{value}</strong><small>/p/{row.slug}</small></div> },
    { title: '状态', dataIndex: 'status', width: 100, render: value => <StatusTag value={value} /> },
    { title: '格式', dataIndex: 'contentFormat', width: 105, render: value => <span className="mono">{value}</span> },
    { title: '排序', dataIndex: 'sort', width: 75, align: 'center' },
    { title: '更新时间', dataIndex: 'updatedAt', width: 150, render: value => value ? dayjs(value).format('YYYY-MM-DD HH:mm') : '-' },
    { title: '操作', width: 170, render: (_, row) => <Space size={0}>{can('content:page:update') && <Button type="link" size="small" icon={<EditOutlined />} onClick={() => navigate(`/content/pages/${row.id}/edit`)}>编辑</Button>}{can('content:page:delete') && <Button type="link" size="small" danger icon={<DeleteOutlined />} onClick={() => remove(row)} />}</Space> },
  ]

  return (
    <>
      <PageTitle title="单页面" eyebrow="Content" description="关于、隐私、联系等不进入文章流的独立内容。" extra={can('content:page:create') && <Button type="primary" icon={<PlusOutlined />} onClick={() => navigate('/content/pages/new')}>新建单页</Button>} />
      <div className="panel"><div className="panel-body">
        <TableToolbar extra={<Button type="primary" icon={<SearchOutlined />} onClick={() => { setPage(1); void load() }}>搜索</Button>}>
          <Input allowClear placeholder="页面标题或别名" value={keyword} onChange={e => setKeyword(e.target.value)} onPressEnter={() => { setPage(1); void load() }} />
        </TableToolbar>
        <Table rowKey="id" loading={loading} columns={columns} dataSource={data.records} pagination={{ current: page, pageSize: 20, total: data.total, onChange: setPage, showTotal: total => `共 ${total} 个页面` }} />
      </div></div>
    </>
  )
}
