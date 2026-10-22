import { useEffect, useState } from 'react'
import { Button, Empty, Modal, Pagination, Space, Upload, message } from 'antd'
import { DeleteOutlined, LinkOutlined, UploadOutlined } from '@ant-design/icons'
import { mediaApi } from '@/api'
import { errorMessage } from '@/api/client'
import { PageTitle } from '@/components/PageTitle'
import type { Media, PageResult } from '@/types'
import { useAuthStore } from '@/stores/auth'

export function MediaPage() {
  const can = useAuthStore(state => state.can)
  const [data, setData] = useState<PageResult<Media>>({ records: [], page: 1, size: 24, total: 0, pages: 0 })
  const [page, setPage] = useState(1)
  const [loading, setLoading] = useState(false)
  const [uploading, setUploading] = useState(false)

  const load = async () => {
    setLoading(true)
    try { setData(await mediaApi.page({ page, size: 24 })) } catch (error) { message.error(errorMessage(error)) } finally { setLoading(false) }
  }
  useEffect(() => { void load() }, [page])

  const upload = async (file: File) => {
    setUploading(true)
    try {
      await mediaApi.upload(file)
      message.success('图片已上传')
      setPage(1)
      void load()
    } catch (error) { message.error(errorMessage(error)) } finally { setUploading(false) }
  }

  const copy = async (url: string) => {
    await navigator.clipboard.writeText(url)
    message.success('图片地址已复制')
  }

  const remove = (media: Media) => Modal.confirm({
    title: '删除媒体', content: `确定删除“${media.originalName}”的记录吗？`, okButtonProps: { danger: true },
    onOk: async () => { try { await mediaApi.remove(media.id); message.success('媒体已删除'); void load() } catch (error) { message.error(errorMessage(error)) } },
  })

  return (
    <>
      <PageTitle title="媒体库" eyebrow="Media" description="统一管理文章配图、封面与相册素材。" extra={can('content:media:upload') && <Upload accept="image/*" showUploadList={false} customRequest={({ file, onSuccess, onError }) => { upload(file as File).then(() => onSuccess?.({})).catch(onError) }}><Button type="primary" loading={uploading} icon={<UploadOutlined />}>上传图片</Button></Upload>} />
      <div className="panel"><div className="panel-body">
        {!loading && !data.records.length ? <Empty description="媒体库还是空的" /> : (
          <div className="photo-grid">
            {data.records.map(media => (
              <div className="photo-card" key={media.id}>
                <img src={media.thumbUrl || media.url} alt={media.originalName} />
                <div className="photo-meta" title={media.originalName}>{media.originalName}</div>
                <div className="photo-actions">
                  <Space size={4}>
                    <Button size="small" icon={<LinkOutlined />} onClick={() => copy(media.url)} />
                    {can('content:media:delete') && <Button size="small" danger icon={<DeleteOutlined />} onClick={() => remove(media)} />}
                  </Space>
                </div>
              </div>
            ))}
          </div>
        )}
        <div style={{ display: 'flex', justifyContent: 'flex-end', marginTop: 22 }}>
          <Pagination current={page} pageSize={24} total={data.total} onChange={setPage} showSizeChanger={false} showTotal={total => `共 ${total} 个媒体`} />
        </div>
      </div></div>
    </>
  )
}
