import { useEffect, useState } from 'react'
import { Button, Form, Input, InputNumber, List, Modal, Select, Space, Table, message } from 'antd'
import type { ColumnsType } from 'antd/es/table'
import { DeleteOutlined, EditOutlined, PlusOutlined } from '@ant-design/icons'
import { dictApi } from '@/api'
import { errorMessage } from '@/api/client'
import { PageTitle } from '@/components/PageTitle'
import { EnabledTag } from '@/components/StatusTag'
import type { DictItem, DictType } from '@/types'
import { useAuthStore } from '@/stores/auth'

export function DictPage() {
  const can = useAuthStore(state => state.can)
  const [types, setTypes] = useState<DictType[]>([])
  const [selected, setSelected] = useState<DictType | null>(null)
  const [items, setItems] = useState<DictItem[]>([])
  const [typeOpen, setTypeOpen] = useState(false)
  const [itemOpen, setItemOpen] = useState(false)
  const [editingType, setEditingType] = useState<DictType | null>(null)
  const [editingItem, setEditingItem] = useState<DictItem | null>(null)
  const [typeForm] = Form.useForm()
  const [itemForm] = Form.useForm()

  const loadTypes = async (preferId?: string) => {
    try {
      const list = await dictApi.types()
      setTypes(list)
      const target = list.find(item => item.id === (preferId || selected?.id)) || list[0] || null
      setSelected(target)
      if (target) setItems(await dictApi.items(target.id))
      else setItems([])
    } catch (error) { message.error(errorMessage(error)) }
  }
  useEffect(() => { void loadTypes() }, [])

  const selectType = async (type: DictType) => {
    setSelected(type)
    try { setItems(await dictApi.items(type.id)) } catch (error) { message.error(errorMessage(error)) }
  }

  const openType = (row?: DictType) => {
    setEditingType(row || null); typeForm.resetFields()
    typeForm.setFieldsValue(row || { status: 1 })
    setTypeOpen(true)
  }
  const submitType = async () => {
    try {
      const values = await typeForm.validateFields()
      if (editingType) await dictApi.updateType(editingType.id, values)
      else await dictApi.createType(values)
      message.success(editingType ? '字典已更新' : '字典已创建')
      setTypeOpen(false); void loadTypes()
    } catch (error) {
      if ((error as { errorFields?: unknown }).errorFields) return
      message.error(errorMessage(error))
    }
  }
  const removeType = (row: DictType) => Modal.confirm({
    title: '删除字典', content: `确定删除字典“${row.name}”及其全部字典项吗？`, okButtonProps: { danger: true },
    onOk: async () => { try { await dictApi.removeType(row.id); message.success('字典已删除'); void loadTypes() } catch (error) { message.error(errorMessage(error)) } },
  })

  const openItem = (row?: DictItem) => {
    if (!selected) return
    setEditingItem(row || null); itemForm.resetFields()
    itemForm.setFieldsValue(row || { dictTypeId: selected.id, sort: 0, status: 1 })
    setItemOpen(true)
  }
  const submitItem = async () => {
    if (!selected) return
    try {
      const values = await itemForm.validateFields()
      if (editingItem) await dictApi.updateItem(editingItem.id, values)
      else await dictApi.createItem(selected.id, values)
      message.success(editingItem ? '字典项已更新' : '字典项已创建')
      setItemOpen(false); setItems(await dictApi.items(selected.id))
    } catch (error) {
      if ((error as { errorFields?: unknown }).errorFields) return
      message.error(errorMessage(error))
    }
  }
  const removeItem = (row: DictItem) => Modal.confirm({
    title: '删除字典项', content: `确定删除“${row.label}”吗？`, okButtonProps: { danger: true },
    onOk: async () => { if (!selected) return; try { await dictApi.removeItem(row.id); message.success('字典项已删除'); setItems(await dictApi.items(selected.id)) } catch (error) { message.error(errorMessage(error)) } },
  })

  const columns: ColumnsType<DictItem> = [
    { title: '标签', dataIndex: 'label', render: value => <strong style={{ fontSize: 12 }}>{value}</strong> },
    { title: '值', dataIndex: 'value', render: value => <span className="mono">{value}</span> },
    { title: '排序', dataIndex: 'sort', width: 75, align: 'center' },
    { title: '状态', dataIndex: 'status', width: 85, render: value => <EnabledTag enabled={value} /> },
    { title: '备注', dataIndex: 'remark', render: value => value || <span className="muted">-</span> },
    { title: '操作', width: 145, render: (_, row) => <Space size={0}>{can('system:dict:update') && <Button type="link" size="small" icon={<EditOutlined />} onClick={() => openItem(row)}>编辑</Button>}{can('system:dict:delete') && <Button type="link" size="small" danger icon={<DeleteOutlined />} onClick={() => removeItem(row)} />}</Space> },
  ]

  return (
    <>
      <PageTitle title="字典管理" eyebrow="System" description="把状态、类型等枚举统一收拢到可维护的数据字典。" extra={can('system:dict:create') && <Button type="primary" icon={<PlusOutlined />} onClick={() => openType()}>新建字典</Button>} />
      <div className="dict-layout">
        <div className="panel">
          <div className="panel-title"><div><h3>字典类型</h3><p>{types.length} 个字典</p></div></div>
          <List dataSource={types} locale={{ emptyText: '暂无字典' }} renderItem={type => (
            <List.Item className={`dict-type-item ${selected?.id === type.id ? 'active' : ''}`} onClick={() => void selectType(type)} actions={[
              can('system:dict:update') ? <Button key="edit" type="text" size="small" icon={<EditOutlined />} onClick={e => { e.stopPropagation(); openType(type) }} /> : null,
              can('system:dict:delete') ? <Button key="del" type="text" size="small" danger icon={<DeleteOutlined />} onClick={e => { e.stopPropagation(); removeType(type) }} /> : null,
            ].filter(Boolean)}>
              <List.Item.Meta title={<strong>{type.name}</strong>} description={<small>{type.dictCode}</small>} />
            </List.Item>
          )} />
        </div>
        <div className="panel"><div className="panel-body">
          <div className="table-toolbar">
            <div><strong style={{ fontSize: 13 }}>{selected ? selected.name : '请选择字典'}</strong>{selected && <span className="mono muted" style={{ marginLeft: 9 }}>{selected.dictCode}</span>}</div>
            {selected && can('system:dict:create') && <Button type="primary" icon={<PlusOutlined />} onClick={() => openItem()}>新增字典项</Button>}
          </div>
          <Table rowKey="id" columns={columns} dataSource={items} pagination={false} />
        </div></div>
      </div>
      <Modal title={editingType ? '编辑字典' : '新建字典'} open={typeOpen} onCancel={() => setTypeOpen(false)} onOk={submitType} okText="保存" cancelText="取消">
        <Form form={typeForm} layout="vertical" style={{ marginTop: 18 }}>
          <Form.Item label="字典名称" name="name" rules={[{ required: true, message: '请输入名称' }]}><Input /></Form.Item>
          <Form.Item label="字典编码" name="dictCode" rules={[{ required: true, message: '请输入编码' }]}><Input placeholder="article_status" /></Form.Item>
          <Form.Item label="状态" name="status"><Select options={[{ label: '启用', value: 1 }, { label: '停用', value: 0 }]} /></Form.Item>
          <Form.Item label="备注" name="remark"><Input.TextArea autoSize={{ minRows: 2, maxRows: 4 }} /></Form.Item>
        </Form>
      </Modal>
      <Modal title={editingItem ? '编辑字典项' : '新增字典项'} open={itemOpen} onCancel={() => setItemOpen(false)} onOk={submitItem} okText="保存" cancelText="取消">
        <Form form={itemForm} layout="vertical" style={{ marginTop: 18 }}>
          <Form.Item label="标签" name="label" rules={[{ required: true, message: '请输入标签' }]}><Input /></Form.Item>
          <Form.Item label="值" name="value" rules={[{ required: true, message: '请输入值' }]}><Input /></Form.Item>
          <Space size="large">
            <Form.Item label="排序" name="sort"><InputNumber min={0} /></Form.Item>
            <Form.Item label="状态" name="status"><Select style={{ width: 110 }} options={[{ label: '启用', value: 1 }, { label: '停用', value: 0 }]} /></Form.Item>
          </Space>
          <Form.Item label="备注" name="remark"><Input /></Form.Item>
        </Form>
      </Modal>
    </>
  )
}
