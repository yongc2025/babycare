import { message } from 'antd'
import { create } from 'zustand'
import { persist } from 'zustand/middleware'
import { authAPI } from '../services/authService'

export interface User {
  id: string
  username: string
  email: string
  phone: string
  avatar?: string
  nickname: string
  city?: string
  role: 'PARENT' | 'ADMIN' | 'ELDER'
  createdAt: string
  updatedAt: string
}

interface AuthState {
  user: User | null
  token: string | null
  isLoading: boolean
  error: string | null
  login: (emailOrUsername: string, password: string) => Promise<void>
  register: (userData: {
    username?: string
    password: string
    email?: string
    phone?: string
    nickname?: string
    phoneVerified?: boolean
  }) => Promise<void>
  phoneLogin: (phone: string, password?: string, code?: string) => Promise<void>
  logout: () => void
  updateUser: (userData: Partial<User>) => void
  setLoading: (loading: boolean) => void
  setError: (error: string | null) => void
  clearError: () => void
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      user: null,
      token: null,
      isLoading: false,
      error: null,

      login: async (emailOrUsername: string, password: string) => {
        set({ isLoading: true, error: null })

        try {
          const response = await authAPI.login({ emailOrUsername, password })

          if (response.success && response.data) {
            set({
              user: response.data.user,
              token: response.data.token,
              isLoading: false,
              error: null,
            })
            return
          }

          throw new Error(response.message || '登录失败')
        } catch (error: any) {
          const errorMessage = error?.response?.data?.message || error.message || '登录失败'
          set({
            isLoading: false,
            error: errorMessage,
          })
          message.error(errorMessage)
          throw error
        }
      },

      phoneLogin: async (phone: string, password?: string, code?: string) => {
        set({ isLoading: true, error: null })

        try {
          const response = await authAPI.phoneLogin({
            phone,
            ...(code ? { code } : { password }),
          })

          if (response.success && response.data) {
            set({
              user: response.data.user,
              token: response.data.token,
              isLoading: false,
              error: null,
            })
            return
          }

          throw new Error(response.message || '登录失败')
        } catch (error: any) {
          const errorMessage = error?.response?.data?.message || error.message || '登录失败'
          set({ isLoading: false, error: errorMessage })
          message.error(errorMessage)
          throw error
        }
      },

      register: async (userData) => {
        set({ isLoading: true, error: null })

        try {
          const response = await authAPI.register({
            username: userData.username,
            email: userData.email || '',
            password: userData.password,
            phone: userData.phone || '',
            nickname: userData.nickname || '',
            confirmPassword: userData.password || '',
            agreement: true,
            phoneVerified: userData.phoneVerified || false,
          })

          if (response.success && response.data) {
            set({
              user: response.data.user,
              token: response.data.token,
              isLoading: false,
              error: null,
            })
            message.success('注册成功')
            return
          }

          throw new Error(response.message || '注册失败')
        } catch (error: any) {
          const errorMessage = error?.response?.data?.message || error.message || '注册失败'
          set({
            isLoading: false,
            error: errorMessage,
          })
          message.error(errorMessage)
          throw error
        }
      },

      logout: () => {
        set({
          user: null,
          token: null,
          error: null,
        })
        localStorage.removeItem('auth-storage')
      },

      updateUser: (userData) => {
        const currentUser = get().user
        if (currentUser) {
          set({
            user: { ...currentUser, ...userData },
          })
        }
      },

      setLoading: (loading) => set({ isLoading: loading }),
      setError: (error) => set({ error }),
      clearError: () => set({ error: null }),
    }),
    {
      name: 'auth-storage',
      partialize: (state) => ({
        user: state.user,
        token: state.token,
      }),
    },
  ),
)
