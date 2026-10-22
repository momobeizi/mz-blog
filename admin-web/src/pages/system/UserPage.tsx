import { useEffect, useState } from 'react'
import { Avatar, Button, Form, Input, Modal, Select, Space, Table, Tag, message } from 'antd'
import type { ColumnsType } from 'antd/es/table'
import { DeleteOutlined, EditOutlined, KeyOutlined, PlusOutlined, SearchOutlined } from '@ant-design/icons'
import dayjs from 'dayjs'
import { userApi } from '@/api'
import { errorMessage } from '@/api/client'
import { PageTitle } from '@/components/PageTitle'
import { EnabledTag } from '@/components/StatusTag'
import { TableToolbar } from '@/components/TableToolbar'
import type { PageResult, Role, User } from '@/types'
import { useAuthStore } from '@/stores/auth'

export function UserPage() {
  const can = useAuthStore(state => state.can)
  const [data, setData] = useState<PageResult<User>>({ records: [], page: 1, size: 20, total: 0, pages: 0 })
  const [roles, setRoles] = useState<Role[]>([])
  const [loading, setLoading] = useState(false)
  const [keyword, setKeyword] = useState('')
  const [status, setStatus] = useState<number | undefined>()
  const [page, setPage] = useState(1)
  const [open, setOpen] = useState(false)
  const [editing, setEditing] = useState<User | null>(null)
  const [resetUser, setResetUser] = useState<User | null>(null)
  const [form] = Form.useForm()
  const [passwordForm] = Form.useForm()

  const load = async () => {
    setLoading(true)
    try { setData(await userApi.page({ page, size: 20, keyword, status })) } catch (error) { message.error(errorMessage(error)) } finally { setLoading(false) }
  }
  useEffect(() => { userApi.roles().then(setRoles).catch(error => message.error(errorMessage(error))) }, [])
  useEffect(() => { void load() }, [page])

  const openModal = (row?: User) => {
    setEditing(row || null); form.resetFields()
    form.setFieldsValue(row || { status: 1, roleIds: [] })
    setOpen(true)
  }

  const submit = async () => {
    try {
      const values = await form.validateFields()
      if (editing) await userApi.update(editing.id, values)
      else await userApi.create(values)
      message.success(editing ? '用户已更新' : '用户已创建')
      setOpen(false); void load()
    } catch (error) {
      if ((error as { errorFields?: unknown }).errorFields) return
      message.error(errorMessage(error))
    }
  }

  const remove = (row: User) => Modal.confirm({
    title: '删除用户', content: `确定删除用户“${row.username}”吗？`, okButtonProps: { danger: true },
    onOk: async () => { try { await userApi.remove(row.id); message.success('用户已删除'); void load() } catch (error) { message.error(errorMessage(error)) } },
  })

  const resetPassword = async () => {
    if (!resetUser) return
    try {
      const values = await passwordForm.validateFields()
      await userApi.resetPassword(resetUser.id, values.password)
      message.success('密码已重置')
      setResetUser(null); passwordForm.resetFields()
    } catch (error) {
      if ((error as { errorFields?: unknown }).errorFields) return
      message.error(errorMessage(error))
    }
  }

  const columns: ColumnsType<User> = [
    { title: '用户', dataIndex: 'nickname', render: (value, row) => <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}><Avatar src={row.avatarUrl}>{value?.[0]}</Avatar><div className="title-cell"><strong>{value}</strong><small>@{row.username}</small></div></div> },
    { title: '角色', dataIndex: 'roleNames', render: (value: string[]) => value?.length ? value.map(name => <Tag key={name} bordered={false} color="cyan">{name}</Tag>) : <span className="muted">未分配</span> },
    { title: '邮箱', dataIndex: 'email', render: value => value || <span className="muted">-</span> },
    { title: '状态', dataIndex: 'status', width: 80, render: value => <EnabledTag enabled={value} /> },
    { title: '最后登录', dataIndex: 'lastLoginAt', width: 150, render: value => value ? dayjs(value).format('YYYY-MM-DD HH:mm') : <span className="muted">从未登录</span> },
    { title: '创建时间', dataIndex: 'createdAt', width: 120, render: value => value ? dayjs(value).format('YYYY-MM-DD') : '-' },
    { title: '操作', width: 225, render: (_, row) => <Space size={0}>{can('system:user:update') && <Button type="link" size="small" icon={<EditOutlined />} onClick={() => openModal(row)}>编辑</Button>}{can('system:user:reset-password') && <Button type="link" size="small" icon={<KeyOutlined />} onClick={() => { setResetUser(row); passwordForm.resetFields() }}>重置密码</Button>}{can('system:user:delete') && row.username !== 'admin' && <Button type="link" size="small" danger icon={<DeleteOutlined />} onClick={() => remove(row)} />}</Space> },
  ]

  return (
    <>
      <PageTitle title="用户管理" eyebrow="System" description="管理可以进入后台的账号、角色与登录状态。" extra={can('system:user:create') && <Button type="primary" icon={<PlusOutlined />} onClick={() => openModal()}>新建用户</Button>} />
      <div className="panel"><div className="panel-body">
        <TableToolbar extra={<Button type="primary" icon={<SearchOutlined />} onClick={() => { setPage(1); void load() }}>搜索</Button>}>
          <Input allowClear placeholder="用户名 / 昵称 / 邮箱" value={keyword} onChange={e => setKeyword(e.target.value)} onPressEnter={() => { setPage(1); void load() }} />
          <Select allowClear placeholder="账号状态" value={status} onChange={setStatus} options={[{ label: '启用', value: 1 }, { label: '停用', value: 0 }]} />
        </TableToolbar>
        <Table rowKey="id" loading={loading} columns={columns} dataSource={data.records} scroll={{ x: 1000 }} pagination={{ current: page, pageSize: 20, total: data.total, onChange: setPage, showTotal: total => `共 ${total} 个用户` }} />
      </div></div>
      <Modal title={editing ? '编辑用户' : '新建用户'} open={open} onCancel={() => setOpen(false)} onOk={submit} okText="保存" cancelText="取消">
        <Form form={form} layout="vertical" style={{ marginTop: 18 }}>
          <Form.Item label="用户名" name="username" rules={[{ required: true, message: '请输入用户名' }]}><Input disabled={editing?.username === 'admin'} /></Form.Item>
          <Form.Item label="昵称" name="nickname"><Input /></Form.Item>
          {!editing && <Form.Item label="初始密码" name="password" extra="留空使用默认密码 Admin@123"><Input.Password placeholder="至少 8 位" /></Form.Item>}
          <Form.Item label="角色" name="roleIds"><Select mode="multiple" options={roles.map(role => ({ label: role.name, value: role.id }))} /></Form.Item>
          <Form.Item label="邮箱" name="email"><Input /></Form.Item>
          <Form.Item label="手机号" name="phone"><Input /></Form.Item>
          <Form.Item label="头像地址" name="avatarUrl"><Input /></Form.Item>
          <Form.Item label="状态" name="status"><Select options={[{ label: '启用', value: 1 }, { label: '停用', value: 0 }]} /></Form.Item>
          <Form.Item label="备注" name="remark"><Input.TextArea autoSize={{ minRows: 2, maxRows: 4 }} /></Form.Item>
        </Form>
      </Modal>
      <Modal title={`重置 ${resetUser?.nickname || ''} 的密码`} open={!!resetUser} onCancel={() => setResetUser(null)} onOk={resetPassword} okText="重置" cancelText="取消">
        <Form form={passwordForm} layout="vertical" style={{ marginTop: 18 }}>
          <Form.Item label="新密码" name="password" rules={[{ required: true, message: '请输入新密码' }, { min: 8, message: '密码至少 8 位' }]}><Input.Password /></Form.Item>
        </Form>
      </Modal>
    </>
  )
}
