import { Avatar, Button, Descriptions, Tag } from 'antd'
import { GlobalOutlined, SettingOutlined, SafetyCertificateOutlined } from '@ant-design/icons'
import { useNavigate } from 'react-router-dom'
import { PageTitle } from '@/components/PageTitle'
import { useAuthStore } from '@/stores/auth'

export function ProfilePage() {
  const navigate = useNavigate()
  const user = useAuthStore(state => state.user)
  return (
    <>
      <PageTitle title="个人资料" eyebrow="Account" description="当前登录账号与权限信息。" extra={<Button icon={<SettingOutlined />} onClick={() => navigate('/settings/site')}>站点配置</Button>} />
      <div style={{ display: 'grid', gridTemplateColumns: '300px minmax(0,1fr)', gap: 20 }}>
        <div className="panel" style={{ padding: 30, textAlign: 'center' }}>
          <Avatar size={86} src={user?.avatarUrl} style={{ fontSize: 32 }}>{user?.nickname?.[0]}</Avatar>
          <h2 style={{ fontFamily: 'Georgia, "Songti SC", serif', fontWeight: 500, margin: '18px 0 5px' }}>{user?.nickname}</h2>
          <p className="muted" style={{ fontSize: 12 }}>@{user?.username}</p>
          <div style={{ marginTop: 15 }}>{user?.roles.map(role => <Tag key={role} color="cyan" bordered={false}>{role}</Tag>)}</div>
          <Button block icon={<GlobalOutlined />} style={{ marginTop: 22 }} onClick={() => window.open('/', '_blank')}>访问前台站点</Button>
        </div>
        <div className="panel">
          <div className="panel-title"><div><h3>账号信息</h3><p>账号资料由系统管理中的用户管理维护</p></div><SafetyCertificateOutlined style={{ color: '#2f8e89', fontSize: 18 }} /></div>
          <div className="panel-body">
            <Descriptions column={1} size="small" bordered>
              <Descriptions.Item label="用户 ID"><span className="mono">{user?.id}</span></Descriptions.Item>
              <Descriptions.Item label="用户名">{user?.username}</Descriptions.Item>
              <Descriptions.Item label="昵称">{user?.nickname}</Descriptions.Item>
              <Descriptions.Item label="角色">{user?.roles.join(' / ')}</Descriptions.Item>
              <Descriptions.Item label="权限数量">{user?.permissions.length} 项</Descriptions.Item>
            </Descriptions>
            <h3 style={{ fontSize: 13, margin: '26px 0 12px' }}>权限清单</h3>
            <div style={{ display: 'flex', flexWrap: 'wrap', gap: 6 }}>
              {user?.permissions.map(permission => <Tag key={permission} bordered={false} style={{ fontFamily: 'monospace', fontSize: 10 }}>{permission}</Tag>)}
            </div>
          </div>
        </div>
      </div>
    </>
  )
}
