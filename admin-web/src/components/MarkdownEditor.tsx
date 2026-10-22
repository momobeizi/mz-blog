import { Button, Input } from 'antd'
import { BoldOutlined, CodeOutlined, LinkOutlined, UnorderedListOutlined } from '@ant-design/icons'
import ReactMarkdown from 'react-markdown'
import remarkGfm from 'remark-gfm'

interface Props {
  value?: string
  onChange?: (value: string) => void
}

export function MarkdownEditor({ value = '', onChange }: Props) {
  const insert = (before: string, after = '', placeholder = '') => {
    const next = `${value}${value && !value.endsWith('\n') ? '\n' : ''}${before}${placeholder}${after}`
    onChange?.(next)
  }

  return (
    <div className="content-editor">
      <div className="editor-toolbar">
        <Button type="text" size="small" icon={<BoldOutlined />} onClick={() => insert('**', '**', '加粗文字')}>加粗</Button>
        <Button type="text" size="small" icon={<UnorderedListOutlined />} onClick={() => insert('- ', '', '列表项')}>列表</Button>
        <Button type="text" size="small" onClick={() => insert('## ', '', '标题')}>标题</Button>
        <Button type="text" size="small" icon={<CodeOutlined />} onClick={() => insert('```\n', '\n```', '代码')}>代码</Button>
        <Button type="text" size="small" icon={<LinkOutlined />} onClick={() => insert('[', '](https://)', '链接文字')}>链接</Button>
        <Button type="text" size="small" onClick={() => insert('| 列 1 | 列 2 |\n| --- | --- |\n| ', ' |  |', '内容')}>表格</Button>
        <span className="divider" />
        <span className="muted" style={{ fontSize: 11 }}>支持 GFM 表格、任务列表、脚注与代码块</span>
      </div>
      <div className="markdown-editor">
        <Input.TextArea
          value={value}
          onChange={event => onChange?.(event.target.value)}
          placeholder="用 Markdown 开始写作..."
          autoSize={{ minRows: 20, maxRows: 34 }}
        />
        <div className="markdown-preview">
          {value ? <ReactMarkdown remarkPlugins={[remarkGfm]}>{value}</ReactMarkdown> : <span className="muted">预览会显示在这里</span>}
        </div>
      </div>
    </div>
  )
}
