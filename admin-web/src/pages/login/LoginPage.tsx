import { useState } from 'react'
import { Button, Form, Input, message } from 'antd'
import { LockOutlined, UserOutlined } from '@ant-design/icons'
import { Navigate, useLocation, useNavigate } from 'react-router-dom'
import { useAuthStore } from '@/stores/auth'
import { errorMessage } from '@/api/client'

export function LoginPage() {
  const [loading, setLoading] = useState(false)
  const { login, user } = useAuthStore()
  const navigate = useNavigate()
  const location = useLocation()

  if (user) return <Navigate to="/dashboard" replace />

  const submit = async (values: { username: string; password: string }) => {
    setLoading(true)
    try {
      await login(values.username, values.password)
      message.success('欢迎回来')
      navigate((location.state as { from?: string } | null)?.from || '/dashboard', { replace: true })
    } catch (error) {
      message.error(errorMessage(error))
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="login-page">
      <div className="login-art">
        <div className="login-art-inner">
          <div className="brand-mark large">mz<span>.</span></div>
          <p className="login-kicker">CONTENT STUDIO</p>
          <h1>把每一次记录，<br />变成可以回来的地方。</h1>
          <p>文章、页面、相册与站点配置，都在这片安静的工作台里。</p>
          <div className="login-wave" />
        </div>
      </div>
      <div className="login-panel">
        <div className="login-card">
          <div className="login-heading">
            <span>后台管理</span>
            <h2>欢迎回来</h2>
            <p>登录后继续管理你的数字花园。</p>
          </div>
          <Form layout="vertical" onFinish={submit} initialValues={{ username: 'admin', password: 'Admin@123' }}>
            <Form.Item name="username" label="用户名" rules={[{ required: true, message: '请输入用户名' }]}>
              <Input size="large" prefix={<UserOutlined />} placeholder="请输入用户名" autoComplete="username" />
            </Form.Item>
            <Form.Item name="password" label="密码" rules={[{ required: true, message: '请输入密码' }]}>
              <Input.Password size="large" prefix={<LockOutlined />} placeholder="请输入密码" autoComplete="current-password" />
            </Form.Item>
            <Button type="primary" htmlType="submit" loading={loading} block size="large">进入后台</Button>
          </Form>
          <p className="login-hint">初始账号 admin · 密码 Admin@123</p>
        </div>
      </div>
    </div>
  )
}
