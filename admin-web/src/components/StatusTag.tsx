import { Tag } from 'antd'

const map: Record<string, { color: string; text: string }> = {
  PUBLISHED: { color: '#e4f5ec', text: '已发布' },
  DRAFT: { color: '#f0f1f3', text: '草稿' },
  OFFLINE: { color: '#fff1df', text: '已下线' },
  FRIEND: { color: '#e3f6f5', text: '友情链接' },
  COMMON: { color: '#edf1fb', text: '常用链接' },
}

export function StatusTag({ value }: { value?: string }) {
  const item = map[value || ''] || { color: '#f0f1f3', text: value || '-' }
  return <Tag bordered={false} style={{ background: item.color, color: item.text, margin: 0 }}>{item.text}</Tag>
}

export function EnabledTag({ enabled }: { enabled: number | undefined }) {
  return enabled === 1
    ? <Tag bordered={false} style={{ background: '#e4f5ec', color: '#277a51' }}>启用</Tag>
    : <Tag bordered={false} style={{ background: '#f0f1f3', color: '#758087' }}>停用</Tag>
}
