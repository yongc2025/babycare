import { create } from 'zustand'
import { familyAPI } from '../services/api'
import type { Baby, Family, FamilyMember } from '../types'

type BabyCreateData = Pick<Baby, 'name' | 'gender' | 'birthday' | 'avatar'>

interface FamilyState {
  currentFamily: Family | null
  currentBaby: Baby | null
  families: Family[]
  isLoading: boolean
  error: string | null
  createFamily: (familyData: { name: string; description?: string }) => Promise<void>
  joinFamily: (inviteCode: string) => Promise<void>
  switchFamily: (familyId: string) => void
  addBaby: (babyData: BabyCreateData) => Promise<void>
  switchBaby: (babyId: string) => void
  updateBaby: (babyId: string, babyData: Partial<Baby>) => Promise<void>
  inviteMember: (email: string, role: FamilyMember['role']) => Promise<void>
  loadFamilies: () => Promise<void>
  setLoading: (loading: boolean) => void
  setError: (error: string | null) => void
}

interface ApiResponse<T> {
  success: boolean
  data?: T
  message?: string
}

interface ApiBaby {
  id: number | string
  name: string
  gender: 'MALE' | 'FEMALE'
  birthday: string
  avatar?: string
  familyId: number | string
  createdAt?: string
  updatedAt?: string
}

interface ApiFamilyMember {
  id: number | string
  userId: number | string
  username?: string
  nickname?: string
  avatar?: string
  role: FamilyMember['role']
  joinedAt?: string
}

interface ApiFamily {
  id: number | string
  name: string
  description?: string
  inviteCode: string
  babies?: ApiBaby[]
  members?: ApiFamilyMember[]
  createdAt?: string
  updatedAt?: string
}

const unwrap = <T>(response: ApiResponse<T> | T): T => {
  if (response && typeof response === 'object' && 'success' in response) {
    const apiResponse = response as ApiResponse<T>
    if (!apiResponse.success || apiResponse.data === undefined) {
      throw new Error(apiResponse.message || '请求失败')
    }
    return apiResponse.data
  }

  return response as T
}

const toTextId = (id: number | string | undefined) => String(id ?? '')

const normalizeBaby = (baby: ApiBaby): Baby => ({
  id: toTextId(baby.id),
  name: baby.name,
  gender: baby.gender,
  birthday: baby.birthday,
  avatar: baby.avatar,
  familyId: toTextId(baby.familyId),
  createdAt: baby.createdAt || '',
  updatedAt: baby.updatedAt || baby.createdAt || '',
})

const normalizeMember = (member: ApiFamilyMember, familyId: string): FamilyMember => ({
  id: toTextId(member.id),
  userId: toTextId(member.userId),
  familyId,
  role: member.role,
  nickname: member.nickname || member.username || '家庭成员',
  avatar: member.avatar,
  joinedAt: member.joinedAt || '',
})

const normalizeFamily = (family: ApiFamily): Family => {
  const familyId = toTextId(family.id)

  return {
    id: familyId,
    name: family.name,
    inviteCode: family.inviteCode,
    babies: (family.babies || []).map(normalizeBaby),
    members: (family.members || []).map((member) => normalizeMember(member, familyId)),
    createdAt: family.createdAt || '',
    updatedAt: family.updatedAt || family.createdAt || '',
  }
}

const pickCurrentBaby = (family: Family | null, previousBabyId?: string) => {
  if (!family) return null
  return family.babies.find((baby) => baby.id === previousBabyId) || family.babies[0] || null
}

export const useFamilyStore = create<FamilyState>((set, get) => ({
  currentFamily: null,
  currentBaby: null,
  families: [],
  isLoading: false,
  error: null,

  createFamily: async (familyData) => {
    set({ isLoading: true, error: null })

    try {
      const family = normalizeFamily(unwrap<ApiFamily>(await familyAPI.createFamily(familyData)))

      set((state) => ({
        families: [...state.families.filter((item) => item.id !== family.id), family],
        currentFamily: family,
        currentBaby: pickCurrentBaby(family),
        isLoading: false,
      }))
    } catch (error) {
      set({
        isLoading: false,
        error: error instanceof Error ? error.message : '创建家庭失败',
      })
      throw error
    }
  },

  joinFamily: async (inviteCode) => {
    set({ isLoading: true, error: null })

    try {
      const family = normalizeFamily(unwrap<ApiFamily>(await familyAPI.joinFamily(inviteCode)))

      set((state) => ({
        families: [...state.families.filter((item) => item.id !== family.id), family],
        currentFamily: family,
        currentBaby: pickCurrentBaby(family),
        isLoading: false,
      }))
    } catch (error) {
      set({
        isLoading: false,
        error: error instanceof Error ? error.message : '加入家庭失败',
      })
      throw error
    }
  },

  switchFamily: (familyId) => {
    const family = get().families.find((item) => item.id === familyId)
    if (!family) return

    set({
      currentFamily: family,
      currentBaby: pickCurrentBaby(family),
    })
  },

  addBaby: async (babyData) => {
    set({ isLoading: true, error: null })

    try {
      const currentFamily = get().currentFamily
      if (!currentFamily) {
        throw new Error('请先选择家庭')
      }

      const baby = normalizeBaby(
        unwrap<ApiBaby>(
          await familyAPI.addBaby(currentFamily.id, {
            name: babyData.name,
            gender: babyData.gender,
            birthday: babyData.birthday,
            avatar: babyData.avatar,
          }),
        ),
      )

      set((state) => {
        const families = state.families.map((family) =>
          family.id === currentFamily.id
            ? {
                ...family,
                babies: [...family.babies.filter((item) => item.id !== baby.id), baby],
              }
            : family,
        )
        const nextFamily = families.find((family) => family.id === currentFamily.id) || null

        return {
          families,
          currentFamily: nextFamily,
          currentBaby: baby,
          isLoading: false,
        }
      })
    } catch (error) {
      set({
        isLoading: false,
        error: error instanceof Error ? error.message : '添加宝宝失败',
      })
      throw error
    }
  },

  switchBaby: (babyId) => {
    const baby = get().currentFamily?.babies.find((item) => item.id === babyId)
    if (baby) {
      set({ currentBaby: baby })
    }
  },

  updateBaby: async () => {
    set({
      error: '当前后端暂未提供宝宝资料更新接口',
    })
    throw new Error('当前后端暂未提供宝宝资料更新接口')
  },

  inviteMember: async () => {
    set({
      error: '当前后端暂未提供家庭成员邀请接口',
    })
    throw new Error('当前后端暂未提供家庭成员邀请接口')
  },

  loadFamilies: async () => {
    set({ isLoading: true, error: null })

    try {
      const families = unwrap<ApiFamily[]>(await familyAPI.getMyFamilies()).map(normalizeFamily)
      const previousFamilyId = get().currentFamily?.id
      const previousBabyId = get().currentBaby?.id
      const currentFamily =
        families.find((family) => family.id === previousFamilyId) || families[0] || null

      set({
        families,
        currentFamily,
        currentBaby: pickCurrentBaby(currentFamily, previousBabyId),
        isLoading: false,
      })
    } catch (error) {
      set({
        families: [],
        currentFamily: null,
        currentBaby: null,
        isLoading: false,
        error: error instanceof Error ? error.message : '加载家庭列表失败',
      })
      throw error
    }
  },

  setLoading: (loading) => set({ isLoading: loading }),
  setError: (error) => set({ error }),
}))
