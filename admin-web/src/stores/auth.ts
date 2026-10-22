import { create } from 'zustand'
import { authApi } from '@/api'
import type { SessionUser } from '@/types'

interface AuthState {
  user: SessionUser | null
  initialized: boolean
  loading: boolean
  bootstrap: () => Promise<void>
  login: (username: string, password: string) => Promise<void>
  logout: () => Promise<void>
  can: (permission?: string) => boolean
}

export const useAuthStore = create<AuthState>((set, get) => ({
  user: null,
  initialized: false,
  loading: false,
  bootstrap: async () => {
    if (get().loading) return
    set({ loading: true })
    try {
      const user = await authApi.me()
      set({ user, initialized: true, loading: false })
    } catch {
      set({ user: null, initialized: true, loading: false })
    }
  },
  login: async (username, password) => {
    const user = await authApi.login({ username, password })
    set({ user, initialized: true })
  },
  logout: async () => {
    try {
      await authApi.logout()
    } finally {
      set({ user: null })
    }
  },
  can: (permission) => {
    if (!permission) return true
    const user = get().user
    return !!user && (user.roles.includes('SUPER_ADMIN') || user.permissions.includes(permission))
  },
}))
