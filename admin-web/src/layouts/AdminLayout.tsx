import { useEffect, useMemo, useState } from 'react'
import { Avatar, Breadcrumb, Button, Drawer, Dropdown, Layout, Menu, Spin, Tooltip } from 'antd'
import {
  ApartmentOutlined, BookOutlined, CompassOutlined, DashboardOutlined, FileDoneOutlined,
  FileTextOutlined, FolderOpenOutlined, HomeOutlined, LinkOutlined, LogoutOutlined,
  MenuFoldOutlined, MenuOutlined, PictureOutlined, ReadOutlined, SettingOutlined,
  TagsOutlined, TeamOutlined, UserOutlined,
} from '@ant-design/icons'
import { Outlet, useLocation, useNavigate } from 'react-router-dom'
import type { MenuProps } from 'antd'
import type { AdminMenu } from '@/types'
import { menuApi } from '@/api'
import { useAuthStore } from '@/stores/auth'

const { Sider, Content } = Layout

const iconMap: Record<string, React.ReactNode> = {
  DashboardOutlined: <DashboardOutlined />,
  FileTextOutlined: <FileTextOutlined />,
  ReadOutlined: <ReadOutlined />,
  FolderOpenOutlined: <FolderOpenOutlined />,
  TagsOutlined: <TagsOutlined />,
  FileDoneOutlined: <FileDoneOutlined />,
  PictureOutlined: <PictureOutlined />,
  CompassOutlined: <CompassOutlined />,
  MenuOutlined: <MenuOutlined />,
  LinkOutlined: <LinkOutlined />,
  SettingOutlined: <SettingOutlined />,
  TeamOutlined: <TeamOutlined />,
  SafetyCertificateOutlined: <BookOutlined />,
  ApartmentOutlined: <ApartmentOutlined />,
  BookOutlined: <BookOutlined />,
}

const pageNames: Record<string, string> = {
  dashboard: '仪表盘',
  content: '内容管理',
  articles: '文章管理',
  categories: '分类管理',
  tags: '标签管理',
  pages: '单页面',
  albums: '相册管理',
  media: '媒体库',
  navigation: '导航管理',
  menus: '前台菜单',
  links: '链接管理',
  settings: '网站配置',
  site: '网站配置',
  system: '系统管理',
  users: '用户管理',
  roles: '角色管理',
  dictionaries: '字典管理',
  profile: '个人资料',
  new: '新建',
  edit: '编辑',
}

function toMenuItems(menus: AdminMenu[]): MenuProps['items'] {
  return menus.filter(item => item.visible === 1).map(item => ({
    key: item.path || String(item.id),
    icon: item.icon ? iconMap[item.icon] : undefined,
    label: item.name,
    children: item.children?.length ? toMenuItems(item.children) : undefined,
  }))
}

export function AdminLayout() {
  const navigate = useNavigate()
  const location = useLocation()
  const { user, logout } = useAuthStore()
  const [menus, setMenus] = useState<AdminMenu[]>([])
  const [loadingMenus, setLoadingMenus] = useState(true)
  const [drawerOpen, setDrawerOpen] = useState(false)

  useEffect(() => {
    menuApi.current()
      .then(setMenus)
      .catch(() => setMenus([]))
      .finally(() => setLoadingMenus(false))
  }, [])

  const crumbs = useMemo(() => {
    const parts = location.pathname.split('/').filter(Boolean)
    return parts.map((part, index) => ({
      title: pageNames[part] || (/^\d+$/.test(part) ? '详情' : part),
      path: '/' + parts.slice(0, index + 1).join('/'),
    }))
  }, [location.pathname])

  const sidebar = (
    <div className="admin-sidebar-inner">
      <div className="admin-brand">
        <div className="brand-mark">mz<span>.</span></div>
        <div>
          <strong>后台管理</strong>
          <small>CONTENT STUDIO</small>
        </div>
      </div>
      <div className="admin-brand-rule" />
      {loadingMenus ? <div className="menu-loading"><Spin size="small" /></div> : (
        <Menu
          mode="inline"
          items={toMenuItems(menus)}
          selectedKeys={[location.pathname]}
          defaultOpenKeys={menus.filter(menu => menu.children?.some(child => location.pathname.startsWith(child.path || '~'))).map(menu => menu.path || String(menu.id))}
          onClick={({ key }) => {
            if (key.startsWith('/')) navigate(key)
            setDrawerOpen(false)
          }}
        />
      )}
      <div className="sidebar-foot">
        <span>mz.blog CMS</span>
        <span>v0.1</span>
      </div>
    </div>
  )

  return (
    <Layout className="admin-shell">
      <Sider width={236} className="admin-sider" breakpoint="lg" collapsedWidth={0} trigger={null}>
        {sidebar}
      </Sider>
      <Drawer placement="left" open={drawerOpen} onClose={() => setDrawerOpen(false)} width={246} styles={{ body: { padding: 0 } }}>
        {sidebar}
      </Drawer>
      <Layout className="admin-main">
        <header className="admin-topbar">
          <Button className="mobile-menu-button" type="text" icon={<MenuFoldOutlined />} onClick={() => setDrawerOpen(true)} />
          <Breadcrumb items={[{ title: <><HomeOutlined /> 后台</> }, ...crumbs]} />
          <div className="topbar-right">
            <Tooltip title="访问前台">
              <Button type="text" icon={<CompassOutlined />} onClick={() => window.open('/', '_blank')} />
            </Tooltip>
            <Dropdown
              menu={{
                items: [
                  { key: 'profile', icon: <UserOutlined />, label: '个人资料', onClick: () => navigate('/profile') },
                  { type: 'divider' },
                  { key: 'logout', icon: <LogoutOutlined />, label: '退出登录', danger: true, onClick: async () => { await logout(); navigate('/login') } },
                ],
              }}
            >
              <button className="user-chip">
                <Avatar size={32} src={user?.avatarUrl}>{user?.nickname?.[0]}</Avatar>
                <span><strong>{user?.nickname}</strong><small>{user?.roles?.includes('SUPER_ADMIN') ? '超级管理员' : '内容编辑'}</small></span>
              </button>
            </Dropdown>
          </div>
        </header>
        <Content className="admin-content">
          <Outlet />
        </Content>
      </Layout>
    </Layout>
  )
}
