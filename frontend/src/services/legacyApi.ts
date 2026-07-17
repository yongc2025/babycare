import { request } from './http'

export const growthRecordAPI = {
  createRecord: (data: any) => request.post('/growth-record/create', data),
  updateRecord: (recordId: number, data: any) => request.put(`/growth-record/${recordId}`, data),
  deleteRecord: (recordId: number) => request.delete(`/growth-record/${recordId}`),
  getBabyRecords: (babyId: number, page: number, size: number) =>
    request.get(`/growth-record/baby/${babyId}?page=${page}&size=${size}`),
  getRecordsByType: (babyId: number, type: string) =>
    request.get(`/growth-record/baby/${babyId}/type/${type}`),
  getBabies: () => request.get('/family/my-families'),
}

export const educationPlanAPI = {
  createPlan: (data: any) => request.post('/education-plan/create', data),
  updatePlan: (planId: number, data: any) => request.put(`/education-plan/${planId}`, data),
  deletePlan: (planId: number) => request.delete(`/education-plan/${planId}`),
  getBabyPlans: (babyId: number, page = 0, size = 10) =>
    request.get(`/education-plan/baby/${babyId}?page=${page}&size=${size}`),
  getActivePlans: (babyId: number) => request.get(`/education-plan/baby/${babyId}/active`),
  startPlan: (planId: number) => request.post(`/education-plan/${planId}/start`),
  completePlan: (planId: number) => request.post(`/education-plan/${planId}/complete`),
  createActivity: (data: any) => request.post('/education-plan/activity/create', data),
  getPlanActivities: (planId: number, page = 0, size = 20) =>
    request.get(`/education-plan/${planId}/activities?page=${page}&size=${size}`),
  completeActivity: (activityId: number, data: { notes?: string; rating?: number }) =>
    request.post(`/education-plan/activity/${activityId}/complete`, data),
}

export const familyAPI = {
  createFamily: (data: { name: string; description?: string }) =>
    request.post('/family/create', data),
  joinFamily: (inviteCode: string) => request.post(`/family/join/${inviteCode}`),
  getMyFamilies: () => request.get('/family/my-families'),
  getFamilyDetail: (familyId: number | string) => request.get(`/family/${familyId}`),
  addBaby: (
    familyId: number | string,
    data: { name: string; gender: 'MALE' | 'FEMALE'; birthday: string; avatar?: string },
  ) => request.post(`/family/${familyId}/babies`, data),
  getFamilyBabies: (familyId: number) => request.get(`/family/${familyId}/babies`),
}

export const familyPostAPI = {
  createPost: (data: any) => request.post('/family-post/create', data),
  updatePost: (postId: number, data: any) => request.put(`/family-post/${postId}`, data),
  deletePost: (postId: number) => request.delete(`/family-post/${postId}`),
  getFamilyPosts: (familyId: number, page = 0, size = 20) =>
    request.get(`/family-post/family/${familyId}?page=${page}&size=${size}`),
  likePost: (postId: number) => request.post(`/family-post/${postId}/like`),
  unlikePost: (postId: number) => request.delete(`/family-post/${postId}/like`),
}

export const familyTaskAPI = {
  createTask: (data: any) => request.post('/family-task/create', data),
  updateTask: (taskId: number, data: any) => request.put(`/family-task/${taskId}`, data),
  deleteTask: (taskId: number) => request.delete(`/family-task/${taskId}`),
  getFamilyTasks: (familyId: number, page = 0, size = 20) =>
    request.get(`/family-task/family/${familyId}?page=${page}&size=${size}`),
  getMyTasks: (page = 0, size = 20) => request.get(`/family-task/my-tasks?page=${page}&size=${size}`),
  startTask: (taskId: number) => request.post(`/family-task/${taskId}/start`),
  completeTask: (taskId: number, data: { completionNotes?: string }) =>
    request.post(`/family-task/${taskId}/complete`, data),
  cancelTask: (taskId: number) => request.post(`/family-task/${taskId}/cancel`),
}
