import { useEffect, useRef } from 'react'
import { Button, Tooltip } from 'antd'
import {
  BoldOutlined, CodeOutlined, ItalicOutlined, LinkOutlined, OrderedListOutlined,
  PictureOutlined, RedoOutlined, StrikethroughOutlined, UndoOutlined, UnorderedListOutlined,
} from '@ant-design/icons'

interface Props {
  value?: string
  onChange?: (value: string) => void
  minHeight?: number
}

export function RichTextEditor({ value = '', onChange, minHeight = 360 }: Props) {
  const ref = useRef<HTMLDivElement>(null)

  useEffect(() => {
    if (ref.current && ref.current.innerHTML !== (value || '')) {
      ref.current.innerHTML = value || ''
    }
  }, [value])

  const command = (name: string, arg?: string) => {
    ref.current?.focus()
    document.execCommand(name, false, arg)
    emit()
  }

  const emit = () => onChange?.(ref.current?.innerHTML || '')

  const insertLink = () => {
    const url = window.prompt('输入链接地址')
    if (url) command('createLink', url)
  }

  const insertImage = () => {
    const url = window.prompt('输入图片地址')
    if (url) command('insertImage', url)
  }

  return (
    <div className="content-editor">
      <div className="editor-toolbar">
        <Tooltip title="加粗"><Button type="text" size="small" icon={<BoldOutlined />} onMouseDown={e => e.preventDefault()} onClick={() => command('bold')} /></Tooltip>
        <Tooltip title="斜体"><Button type="text" size="small" icon={<ItalicOutlined />} onMouseDown={e => e.preventDefault()} onClick={() => command('italic')} /></Tooltip>
        <Tooltip title="删除线"><Button type="text" size="small" icon={<StrikethroughOutlined />} onMouseDown={e => e.preventDefault()} onClick={() => command('strikeThrough')} /></Tooltip>
        <span className="divider" />
        <Button type="text" size="small" onMouseDown={e => e.preventDefault()} onClick={() => command('formatBlock', 'h2')}>H2</Button>
        <Button type="text" size="small" onMouseDown={e => e.preventDefault()} onClick={() => command('formatBlock', 'h3')}>H3</Button>
        <Button type="text" size="small" onMouseDown={e => e.preventDefault()} onClick={() => command('formatBlock', 'blockquote')}>引用</Button>
        <span className="divider" />
        <Tooltip title="无序列表"><Button type="text" size="small" icon={<UnorderedListOutlined />} onMouseDown={e => e.preventDefault()} onClick={() => command('insertUnorderedList')} /></Tooltip>
        <Tooltip title="有序列表"><Button type="text" size="small" icon={<OrderedListOutlined />} onMouseDown={e => e.preventDefault()} onClick={() => command('insertOrderedList')} /></Tooltip>
        <Tooltip title="代码块"><Button type="text" size="small" icon={<CodeOutlined />} onMouseDown={e => e.preventDefault()} onClick={() => command('formatBlock', 'pre')} /></Tooltip>
        <span className="divider" />
        <Tooltip title="插入链接"><Button type="text" size="small" icon={<LinkOutlined />} onMouseDown={e => e.preventDefault()} onClick={insertLink} /></Tooltip>
        <Tooltip title="插入图片"><Button type="text" size="small" icon={<PictureOutlined />} onMouseDown={e => e.preventDefault()} onClick={insertImage} /></Tooltip>
        <span className="divider" />
        <Tooltip title="撤销"><Button type="text" size="small" icon={<UndoOutlined />} onMouseDown={e => e.preventDefault()} onClick={() => command('undo')} /></Tooltip>
        <Tooltip title="重做"><Button type="text" size="small" icon={<RedoOutlined />} onMouseDown={e => e.preventDefault()} onClick={() => command('redo')} /></Tooltip>
      </div>
      <div
        ref={ref}
        className="rich-surface"
        contentEditable
        suppressContentEditableWarning
        style={{ minHeight }}
        onInput={emit}
      />
    </div>
  )
}
