import { useEffect, useState } from 'react'
import { Button, Spin, message } from 'antd'
import {
  FileDoneOutlined, FileTextOutlined, FolderOpenOutlined, PictureOutlined, PlusOutlined, RightOutlined,
} from '@ant-design/icons'
import { useNavigate } from 'react-router-dom'
import dayjs from 'dayjs'
import { dashboardApi } from '@/api'
import { errorMessage } from '@/api/client'
import { PageTitle } from '@/components/PageTitle'
import { StatusTag } from '@/components/StatusTag'
import type { Article } from '@/types'

interface DashboardData {
  articleCount: number
  publishedCount: number
  draftCount: number
  pageCount: number
  categoryCount: number
  tagCount: number
  albumCount: number
  recentArticles: Article[]
}

export function DashboardPage() {
  const [data, setData] = useState<DashboardData | null>(null)
  const [loading, setLoading] = useState(true)
  const navigate = useNavigate()

  useEffect(() => {
    dashboardApi.get()
      .then(value => setData(value as unknown as DashboardData))
      .catch(error => message.error(errorMessage(error)))
      .finally(() => setLoading(false))
  }, [])

  return (
    <>
      <PageTitle title="仪表盘" eyebrow="Overview" description="今天也留下一点值得被记住的内容。" extra={<Button type="primary" icon={<PlusOutlined />} onClick={() => navigate('/content/articles/new')}>写文章</Button>} />
      <div className="dashboard-hero">
        <span className="hero-kicker">MZ.BLOG CONTENT STUDIO</span>
        <h2>在喧闹的世界里，留下一块安静的地方。</h2>
        <p>内容不是数字的堆积，而是一条可以被重新走回的路。</p>
      </div>
      {loading ? <div style={{ padding: 80, textAlign: 'center' }}><Spin size="large" /></div> : (
        <>
          <div className="stat-grid">
            <div className="stat-card"><span className="stat-label">全部文章</span><strong className="stat-value">{data?.articleCount ?? 0}</strong><span className="stat-note">已发布 {data?.publishedCount ?? 0} 篇</span></div>
            <div className="stat-card"><span className="stat-label">草稿等待完成</span><strong className="stat-value">{data?.draftCount ?? 0}</strong><span className="stat-note">灵感值得被认真对待</span></div>
            <div className="stat-card"><span className="stat-label">独立页面</span><strong className="stat-value">{data?.pageCount ?? 0}</strong><span className="stat-note">关于、隐私与更多</span></div>
            <div className="stat-card"><span className="stat-label">分类 / 标签</span><strong className="stat-value">{data?.categoryCount ?? 0} / {data?.tagCount ?? 0}</strong><span className="stat-note">为内容建立路径</span></div>
          </div>
          <div className="dashboard-grid">
            <section className="panel">
              <div className="panel-title"><div><h3>最近文章</h3><p>最新创建或更新的内容</p></div><Button type="link" onClick={() => navigate('/content/articles')}>全部文章 <RightOutlined /></Button></div>
              <div className="recent-list">
                {data?.recentArticles?.length ? data.recentArticles.map(article => (
                  <div className="recent-item" key={article.id}>
                    <img src={article.coverUrl || '/assets/covers/cover-1.svg'} alt="" />
                    <div>
                      <strong>{article.title}</strong>
                      <small>{article.createdAt ? dayjs(article.createdAt).format('YYYY-MM-DD HH:mm') : '-'} · {(article as Article & { categoryName?: string }).categoryName || '未分类'}</small>
                    </div>
                    <StatusTag value={article.status} />
                  </div>
                )) : <div style={{ padding: 30, color: '#88989e' }}>还没有文章，去写下第一篇吧。</div>}
              </div>
            </section>
            <section className="panel">
              <div className="panel-title"><div><h3>快捷入口</h3><p>常用的内容工作</p></div></div>
              <div className="quick-links">
                <button className="quick-link" onClick={() => navigate('/content/articles/new')}><FileTextOutlined /><strong>新建文章</strong></button>
                <button className="quick-link" onClick={() => navigate('/content/pages')}><FileDoneOutlined /><strong>管理单页</strong></button>
                <button className="quick-link" onClick={() => navigate('/content/categories')}><FolderOpenOutlined /><strong>整理分类</strong></button>
                <button className="quick-link" onClick={() => navigate('/content/albums')}><PictureOutlined /><strong>更新相册</strong></button>
              </div>
            </section>
          </div>
        </>
      )}
    </>
  )
}
