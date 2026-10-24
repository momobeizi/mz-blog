export interface ApiResponse<T> {
  code: number
  message: string
  data: T
  traceId: string
}

export interface PageResult<T> {
  records: T[]
  page: number
  size: number
  total: number
  pages: number
}

export interface SessionUser {
  id: string
  username: string
  nickname: string
  avatarUrl?: string
  roles: string[]
  permissions: string[]
}

export interface AdminMenu {
  id: string
  parentId: string
  name: string
  type: 'DIR' | 'MENU' | 'BUTTON'
  path?: string
  componentKey?: string
  permissionCode?: string
  icon?: string
  sort: number
  visible: number
  status: number
  children: AdminMenu[]
}

export interface Article {
  id: string
  title: string
  slug: string
  summary?: string
  coverUrl?: string
  content: string
  contentHtml?: string
  contentFormat: 'HTML' | 'MARKDOWN'
  categoryId: string
  categoryName?: string
  authorId: string
  authorName?: string
  status: 'DRAFT' | 'PUBLISHED' | 'OFFLINE'
  isTop: number
  isRecommended: number
  viewCount: number
  likeCount: number
  publishedAt?: string
  seoTitle?: string
  seoKeywords?: string
  seoDescription?: string
  tagIds: string[]
  tagNames: string[]
  version: number
  createdAt: string
  updatedAt: string
}

export interface Category {
  id: string
  parentId: string
  name: string
  slug: string
  description?: string
  coverUrl?: string
  sort: number
  status: number
  articleCount?: number
}

export interface Tag {
  id: string
  name: string
  slug: string
  description?: string
  articleCount?: number
}

export interface PageDocument {
  id: string
  title: string
  slug: string
  content: string
  contentFormat: 'HTML' | 'MARKDOWN'
  status: 'DRAFT' | 'PUBLISHED' | 'OFFLINE'
  sort: number
  seoTitle?: string
  seoKeywords?: string
  seoDescription?: string
  createdAt: string
  updatedAt: string
}

export interface NavMenu {
  id: string
  parentId: string
  name: string
  menuType: string
  targetId?: string
  url?: string
  icon?: string
  sort: number
  visible: number
  openType: string
  status: number
  resolvedUrl?: string
  children: NavMenu[]
}

export interface LinkItem {
  id: string
  linkType: 'FRIEND' | 'COMMON'
  groupName: string
  name: string
  url: string
  logoUrl?: string
  description?: string
  sort: number
  nofollow: number
  openType: string
  status: number
}

export interface AlbumPhoto {
  id?: string
  mediaId?: string
  imageUrl: string
  thumbUrl?: string
  caption?: string
  sort?: number
}

export interface Album {
  id: string
  title: string
  slug: string
  coverUrl?: string
  description?: string
  sort: number
  status: number
  photos: AlbumPhoto[]
}

export interface Media {
  id: string
  originalName: string
  url: string
  thumbUrl?: string
  mimeType: string
  extension: string
  sizeBytes: number
  width?: number
  height?: number
  createdAt: string
}

export interface Role {
  id: string
  code: string
  name: string
  description?: string
  sort: number
  status: number
  builtIn: number
  userCount?: number
}

export interface User {
  id: string
  username: string
  nickname: string
  avatarUrl?: string
  email?: string
  phone?: string
  status: number
  lastLoginAt?: string
  lastLoginIp?: string
  remark?: string
  createdAt: string
  roleIds: string[]
  roleNames: string[]
}

export interface DictType {
  id: string
  dictCode: string
  name: string
  status: number
  remark?: string
}

export interface DictItem {
  id: string
  dictTypeId: string
  label: string
  value: string
  sort: number
  status: number
  remark?: string
}

export interface SiteConfig {
  id: string
  configKey: string
  configValue?: string
  valueType: string
  groupCode: string
  label: string
  description?: string
  isPublic: number
  sort: number
}
