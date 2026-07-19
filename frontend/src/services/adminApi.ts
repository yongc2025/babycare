import { request } from './http'

const adminApi = {
  // ========== 角色管理 ==========
  listRoles: () => request.get('/api/admin/rbac/roles'),
  getRole: (id: number) => request.get(`/api/admin/rbac/roles/${id}`),
  createRole: (data: any) => request.post('/api/admin/rbac/roles', data),
  updateRole: (id: number, data: any) => request.put(`/api/admin/rbac/roles/${id}`, data),
  deleteRole: (id: number) => request.delete(`/api/admin/rbac/roles/${id}`),

  // ========== 用户-角色分配 ==========
  assignUserRoles: (data: any) => request.post('/api/admin/rbac/user-roles', data),
  getUserRoles: (userId: number) => request.get(`/api/admin/rbac/user-roles/${userId}`),

  // ========== 角色-菜单分配 ==========
  assignRoleMenus: (data: any) => request.post('/api/admin/rbac/role-menus', data),
  getRoleMenus: (roleId: number) => request.get(`/api/admin/rbac/role-menus/${roleId}`),

  // ========== 角色-权限分配 ==========
  assignRolePermissions: (data: any) => request.post('/api/admin/rbac/role-permissions', data),
  getRolePermissions: (roleId: number) => request.get(`/api/admin/rbac/role-permissions/${roleId}`),

  // ========== 权限管理 ==========
  listPermissions: () => request.get('/api/admin/rbac/permissions'),
  createPermission: (data: any) => request.post('/api/admin/rbac/permissions', data),
  deletePermission: (id: number) => request.delete(`/api/admin/rbac/permissions/${id}`),

  // ========== 菜单管理 ==========
  listMenus: () => request.get('/api/admin/rbac/menus'),
  createMenu: (data: any) => request.post('/api/admin/rbac/menus', data),
  deleteMenu: (id: number) => request.delete(`/api/admin/rbac/menus/${id}`),

  // ========== 用户管理 ==========
  listUsers: () => request.get('/api/admin/rbac/users'),

  // ========== 审计日志 ==========
  queryAuditLogs: (params: any) => request.get('/api/admin/audit-log', { params }),

  // ========== 数据字典 ==========
  listDictTypes: () => request.get('/api/admin/data-dict/types'),
  listDictByType: (dictType: string) => request.get(`/api/admin/data-dict/type/${dictType}`),
  createDict: (data: any) => request.post('/api/admin/data-dict', data),
  updateDict: (id: number, data: any) => request.put(`/api/admin/data-dict/${id}`, data),
  deleteDict: (id: number) => request.delete(`/api/admin/data-dict/${id}`),

  // ========== 系统配置 ==========
  listConfigs: () => request.get('/api/admin/config'),
  getConfig: (key: string) => request.get(`/api/admin/config/${key}`),
  updateConfig: (key: string, data: any) => request.put(`/api/admin/config/${key}`, data),
}

export default adminApi
