import { api, http } from './client'
import type {
  AdminMenu, Album, Article, Category, DictItem, DictType, LinkItem, Media, NavMenu,
  PageDocument, PageResult, Role, SessionUser, SiteConfig, Tag, User,
} from '@/types'

export const authApi = {
  login: (body: { username: string; password: string }) => api<SessionUser>(http.post('/auth/login', body)),
  me: () => api<SessionUser>(http.get('/auth/me')),
  logout: () => api<void>(http.post('/auth/logout')),
}

export const dashboardApi = {
  get: () => api<Record<string, unknown>>(http.get('/dashboard')),
}

export const articleApi = {
  page: (params: Record<string, unknown>) => api<PageResult<Article>>(http.get('/content/articles', { params })),
  get: (id: string) => api<Article>(http.get(`/content/articles/${id}`)),
  create: (body: unknown) => api<Article>(http.post('/content/articles', body)),
  update: (id: string, body: unknown) => api<Article>(http.put(`/content/articles/${id}`, body)),
  remove: (id: string) => api<void>(http.delete(`/content/articles/${id}`)),
  status: (id: string, status: string) => api<void>(http.put(`/content/articles/${id}/status`, { status })),
  preview: (content: string, format: string) => api<{ html: string; text: string }>(
    http.post('/content/articles/render-preview', { content, format }),
  ),
}

export const categoryApi = {
  list: () => api<Category[]>(http.get('/content/categories')),
  get: (id: string) => api<Category>(http.get(`/content/categories/${id}`)),
  create: (body: unknown) => api<void>(http.post('/content/categories', body)),
  update: (id: string, body: unknown) => api<void>(http.put(`/content/categories/${id}`, body)),
  remove: (id: string) => api<void>(http.delete(`/content/categories/${id}`)),
}

export const tagApi = {
  list: () => api<Tag[]>(http.get('/content/tags')),
  create: (body: unknown) => api<void>(http.post('/content/tags', body)),
  update: (id: string, body: unknown) => api<void>(http.put(`/content/tags/${id}`, body)),
  remove: (id: string) => api<void>(http.delete(`/content/tags/${id}`)),
}

export const pageApi = {
  page: (params: Record<string, unknown>) => api<PageResult<PageDocument>>(http.get('/content/pages', { params })),
  get: (id: string) => api<PageDocument>(http.get(`/content/pages/${id}`)),
  create: (body: unknown) => api<PageDocument>(http.post('/content/pages', body)),
  update: (id: string, body: unknown) => api<PageDocument>(http.put(`/content/pages/${id}`, body)),
  remove: (id: string) => api<void>(http.delete(`/content/pages/${id}`)),
}

export const navApi = {
  list: () => api<NavMenu[]>(http.get('/content/nav-menus')),
  tree: () => api<NavMenu[]>(http.get('/content/nav-menus/tree')),
  targets: () => api<Record<string, { label: string; value: string }[]>>(http.get('/content/nav-menus/targets')),
  create: (body: unknown) => api<void>(http.post('/content/nav-menus', body)),
  update: (id: string, body: unknown) => api<void>(http.put(`/content/nav-menus/${id}`, body)),
  remove: (id: string) => api<void>(http.delete(`/content/nav-menus/${id}`)),
}

export const linkApi = {
  list: (params?: Record<string, unknown>) => api<LinkItem[]>(http.get('/content/links', { params })),
  create: (body: unknown) => api<void>(http.post('/content/links', body)),
  update: (id: string, body: unknown) => api<void>(http.put(`/content/links/${id}`, body)),
  remove: (id: string) => api<void>(http.delete(`/content/links/${id}`)),
}

export const albumApi = {
  list: () => api<Album[]>(http.get('/content/albums')),
  get: (id: string) => api<Album>(http.get(`/content/albums/${id}`)),
  create: (body: unknown) => api<Album>(http.post('/content/albums', body)),
  update: (id: string, body: unknown) => api<Album>(http.put(`/content/albums/${id}`, body)),
  remove: (id: string) => api<void>(http.delete(`/content/albums/${id}`)),
}

export const mediaApi = {
  page: (params: Record<string, unknown>) => api<PageResult<Media>>(http.get('/content/media', { params })),
  upload: (file: File) => {
    const data = new FormData()
    data.append('file', file)
    return api<Media>(http.post('/content/media/upload', data))
  },
  remove: (id: string) => api<void>(http.delete(`/content/media/${id}`)),
}

export const configApi = {
  list: () => api<Record<string, SiteConfig[]>>(http.get('/settings/site')),
  save: (values: { configKey: string; configValue: string }[]) => api<void>(http.put('/settings/site', { values })),
}

export const menuApi = {
  current: () => api<AdminMenu[]>(http.get('/system/menus/current')),
  all: () => api<AdminMenu[]>(http.get('/system/menus')),
  tree: () => api<AdminMenu[]>(http.get('/system/menus/tree')),
  create: (body: unknown) => api<void>(http.post('/system/menus', body)),
  update: (id: string, body: unknown) => api<void>(http.put(`/system/menus/${id}`, body)),
  remove: (id: string) => api<void>(http.delete(`/system/menus/${id}`)),
}

export const userApi = {
  page: (params: Record<string, unknown>) => api<PageResult<User>>(http.get('/system/users', { params })),
  get: (id: string) => api<{ user: User; roleIds: string[] }>(http.get(`/system/users/${id}`)),
  create: (body: unknown) => api<void>(http.post('/system/users', body)),
  update: (id: string, body: unknown) => api<void>(http.put(`/system/users/${id}`, body)),
  remove: (id: string) => api<void>(http.delete(`/system/users/${id}`)),
  resetPassword: (id: string, password: string) => api<void>(http.put(`/system/users/${id}/password`, { password })),
  roles: () => api<Role[]>(http.get('/system/users/role-options')),
}

export const roleApi = {
  list: (keyword?: string) => api<Role[]>(http.get('/system/roles', { params: { keyword } })),
  get: (id: string) => api<{ role: Role; menuIds: string[] }>(http.get(`/system/roles/${id}`)),
  create: (body: unknown) => api<void>(http.post('/system/roles', body)),
  update: (id: string, body: unknown) => api<void>(http.put(`/system/roles/${id}`, body)),
  remove: (id: string) => api<void>(http.delete(`/system/roles/${id}`)),
}

export const dictApi = {
  types: (keyword?: string) => api<DictType[]>(http.get('/system/dicts', { params: { keyword } })),
  items: (id: string) => api<DictItem[]>(http.get(`/system/dicts/${id}/items`)),
  createType: (body: unknown) => api<void>(http.post('/system/dicts', body)),
  updateType: (id: string, body: unknown) => api<void>(http.put(`/system/dicts/${id}`, body)),
  removeType: (id: string) => api<void>(http.delete(`/system/dicts/${id}`)),
  createItem: (id: string, body: unknown) => api<void>(http.post(`/system/dicts/${id}/items`, body)),
  updateItem: (itemId: string, body: unknown) => api<void>(http.put(`/system/dicts/items/${itemId}`, body)),
  removeItem: (itemId: string) => api<void>(http.delete(`/system/dicts/items/${itemId}`)),
}
