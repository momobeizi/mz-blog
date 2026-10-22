import { createBrowserRouter, Navigate } from 'react-router-dom'
import { ProtectedRoute } from './guards'
import { AdminLayout } from '@/layouts/AdminLayout'
import { LoginPage } from '@/pages/login/LoginPage'
import { DashboardPage } from '@/pages/dashboard/DashboardPage'
import { ArticleListPage } from '@/pages/content/ArticleListPage'
import { ArticleEditorPage } from '@/pages/content/ArticleEditorPage'
import { CategoryPage } from '@/pages/content/CategoryPage'
import { TagPage } from '@/pages/content/TagPage'
import { PageListPage } from '@/pages/content/PageListPage'
import { PageEditorPage } from '@/pages/content/PageEditorPage'
import { AlbumPage } from '@/pages/content/AlbumPage'
import { MediaPage } from '@/pages/content/MediaPage'
import { NavMenuPage } from '@/pages/navigation/NavMenuPage'
import { LinkPage } from '@/pages/navigation/LinkPage'
import { SiteSettingsPage } from '@/pages/settings/SiteSettingsPage'
import { UserPage } from '@/pages/system/UserPage'
import { RolePage } from '@/pages/system/RolePage'
import { MenuPage } from '@/pages/system/MenuPage'
import { DictPage } from '@/pages/system/DictPage'
import { ProfilePage } from '@/pages/profile/ProfilePage'

export const router = createBrowserRouter([
  { path: '/login', element: <LoginPage /> },
  {
    element: <ProtectedRoute />,
    children: [
      {
        element: <AdminLayout />,
        children: [
          { index: true, element: <Navigate to="/dashboard" replace /> },
          { path: '/dashboard', element: <DashboardPage /> },
          { path: '/content/articles', element: <ArticleListPage /> },
          { path: '/content/articles/new', element: <ArticleEditorPage /> },
          { path: '/content/articles/:id/edit', element: <ArticleEditorPage /> },
          { path: '/content/categories', element: <CategoryPage /> },
          { path: '/content/tags', element: <TagPage /> },
          { path: '/content/pages', element: <PageListPage /> },
          { path: '/content/pages/new', element: <PageEditorPage /> },
          { path: '/content/pages/:id/edit', element: <PageEditorPage /> },
          { path: '/content/albums', element: <AlbumPage /> },
          { path: '/content/media', element: <MediaPage /> },
          { path: '/navigation/menus', element: <NavMenuPage /> },
          { path: '/navigation/links', element: <LinkPage /> },
          { path: '/settings/site', element: <SiteSettingsPage /> },
          { path: '/system/users', element: <UserPage /> },
          { path: '/system/roles', element: <RolePage /> },
          { path: '/system/menus', element: <MenuPage /> },
          { path: '/system/dictionaries', element: <DictPage /> },
          { path: '/profile', element: <ProfilePage /> },
          { path: '*', element: <Navigate to="/dashboard" replace /> },
        ],
      },
    ],
  },
], { basename: '/admin' })
