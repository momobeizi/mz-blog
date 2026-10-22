import { useEffect } from 'react'
import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { Spin } from 'antd'
import { useAuthStore } from '@/stores/auth'

export function ProtectedRoute() {
  const { user, initialized, bootstrap } = useAuthStore()
  const location = useLocation()

  useEffect(() => {
    if (!initialized) void bootstrap()
  }, [initialized, bootstrap])

  if (!initialized) {
    return (
      <div className="full-loading">
        <Spin size="large" />
        <p>正在进入后台...</p>
      </div>
    )
  }
  if (!user) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />
  }
  return <Outlet />
}
