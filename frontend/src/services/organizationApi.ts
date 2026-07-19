import { request } from './http'

export const organizationAPI = {
  createOrganization: (data: {
    name: string
    description?: string
    contactPhone?: string
    address?: string
    registrationNo?: string
    licenseNo?: string
    legalRepresentative?: string
    supervisorDepartment?: string
    organizationLevel?: string
    operationType?: string
  }) => request.post('/organization/create', data),
  getMyOrganizations: () => request.get('/organization/my-organizations'),
  getOrganizationDetail: (organizationId: number | string) =>
    request.get(`/organization/${organizationId}`),
  updateOrganization: (
    organizationId: number | string,
    data: {
      name?: string
      description?: string
      contactPhone?: string
      address?: string
      registrationNo?: string
      licenseNo?: string
      legalRepresentative?: string
      supervisorDepartment?: string
      organizationLevel?: string
      operationType?: string
      dailyReportApprovalRequired?: boolean
      status?: 'ACTIVE' | 'DISABLED'
    },
  ) => request.put(`/organization/${organizationId}`, data),
}

export const classroomAPI = {
  createClassroom: (data: {
    organizationId: number | string
    name: string
    ageRangeMinMonths?: number
    ageRangeMaxMonths?: number
    capacity?: number
  }) => request.post('/classroom/create', data),
  getOrganizationClassrooms: (organizationId: number | string) =>
    request.get(`/classroom/organization/${organizationId}`),
  getClassroomDetail: (classroomId: number | string) =>
    request.get(`/classroom/${classroomId}`),
  updateClassroom: (
    classroomId: number | string,
    data: {
      name?: string
      ageRangeMinMonths?: number
      ageRangeMaxMonths?: number
      capacity?: number
      status?: 'ACTIVE' | 'DISABLED'
    },
  ) => request.put(`/classroom/${classroomId}`, data),
}

export const staffAPI = {
  createStaff: (data: {
    organizationId: number | string
    userId: number | string
    role: 'DIRECTOR' | 'TEACHER' | 'CAREGIVER' | 'HEALTH_WORKER' | 'HEALTH_DOCTOR' | 'FINANCE' | 'SAFETY_OFFICER' | 'LOGISTICS_STAFF' | 'OPERATIONS_STAFF' | 'ADMISSIONS_OFFICER'
  }) => request.post('/staff/create', data),
  getOrganizationStaff: (organizationId: number | string) =>
    request.get(`/staff/organization/${organizationId}`),
  getStaffDetail: (staffId: number | string) =>
    request.get(`/staff/${staffId}`),
  updateStaff: (
    staffId: number | string,
    data: {
      role?: 'DIRECTOR' | 'TEACHER' | 'CAREGIVER' | 'HEALTH_WORKER' | 'HEALTH_DOCTOR' | 'FINANCE' | 'SAFETY_OFFICER' | 'LOGISTICS_STAFF' | 'OPERATIONS_STAFF' | 'ADMISSIONS_OFFICER'
      status?: 'ACTIVE' | 'DISABLED'
    },
  ) => request.put(`/staff/${staffId}`, data),
  assignToClassroom: (data: {
    staffId: number | string
    classroomId: number | string
    assignmentType?: string
  }) => request.post('/staff/assign-to-classroom', data),
  removeFromClassroom: (staffId: number | string, classroomId: number | string) =>
    request.delete(`/staff/classroom-assignment?staffId=${staffId}&classroomId=${classroomId}`),
  getClassroomAssignments: (classroomId: number | string) =>
    request.get(`/staff/classroom-assignments/${classroomId}`),
  getStaffAssignments: (staffId: number | string) =>
    request.get(`/staff/staff-assignments/${staffId}`),
}

export const enrollmentAPI = {
  createEnrollment: (data: {
    babyId: number | string
    organizationId: number | string
    classroomId: number | string
    enrolledAt?: string
    allergyNotes?: string
    medicalNotes?: string
    specialCareNotes?: string
    emergencyContactName?: string
    emergencyContactPhone?: string
  }) => request.post('/enrollment/create', data),
  getClassroomEnrollments: (classroomId: number | string) =>
    request.get(`/enrollment/classroom/${classroomId}`),
  getBabyEnrollments: (babyId: number | string) =>
    request.get(`/enrollment/baby/${babyId}`),
  getEnrollmentDetail: (enrollmentId: number | string) =>
    request.get(`/enrollment/${enrollmentId}`),
  updateEnrollment: (
    enrollmentId: number | string,
    data: {
      classroomId?: number | string
      status?: 'PENDING' | 'ACTIVE' | 'SUSPENDED' | 'WITHDRAWN'
      enrolledAt?: string
      allergyNotes?: string
      medicalNotes?: string
      specialCareNotes?: string
      emergencyContactName?: string
      emergencyContactPhone?: string
    },
  ) => request.put(`/enrollment/${enrollmentId}`, data),
  reviewEnrollment: (enrollmentId: number | string, data: { action: 'APPROVE' | 'REJECT'; reason?: string }) =>
    request.post(`/enrollment/${enrollmentId}/review`, data),
  healthCheckEnrollment: (enrollmentId: number | string, data: { passed: boolean; remark?: string }) =>
    request.post(`/enrollment/${enrollmentId}/health-check`, data),
  getHealthCheckPendingEnrollments: (organizationId: number | string) =>
    request.get(`/enrollment/health-check-pending/${organizationId}`),
  transferClassroom: (enrollmentId: number | string, data: { newClassroomId: number | string; reason?: string }) =>
    request.post(`/enrollment/${enrollmentId}/transfer`, data),
  withdrawEnrollment: (enrollmentId: number | string, data?: { reason?: string }) =>
    request.post(`/enrollment/${enrollmentId}/withdraw`, data || {}),

  // ========== 暂停与复托（T077） ==========
  suspendEnrollment: (enrollmentId: number | string, data?: { reason?: string }) =>
    request.post(`/enrollment/${enrollmentId}/suspend`, data || {}),
  reactivateEnrollment: (enrollmentId: number | string) =>
    request.post(`/enrollment/${enrollmentId}/reactivate`),
  getEnrollmentHistory: (enrollmentId: number | string) =>
    request.get(`/enrollment/${enrollmentId}/history`),

  // ========== 家长资料补充与确认（T076） ==========
  getSupplementStatus: (enrollmentId: number | string) =>
    request.get(`/enrollment/${enrollmentId}/supplement`),
  saveSupplement: (enrollmentId: number | string, data: {
    babyIdCard?: string
    babyBirthCertificateNo?: string
    guardianIdCard?: string
    guardianOccupation?: string
    guardianPhone?: string
    allergyNotes?: string
    medicalNotes?: string
    specialCareNotes?: string
    emergencyContactName?: string
    emergencyContactPhone?: string
  }) => request.put(`/enrollment/${enrollmentId}/supplement`, data),
  confirmSupplement: (enrollmentId: number | string) =>
    request.post(`/enrollment/${enrollmentId}/confirm`),

  // ========== 监护人绑定管理 ==========
  addGuardian: (enrollmentId: number | string, data: {
    userId: number | string
    relationship?: string
    isPrimary?: boolean
    guardianPhone?: string
    remark?: string
  }) => request.post(`/enrollment/${enrollmentId}/guardians`, data),
  removeGuardian: (enrollmentId: number | string, guardianId: number | string) =>
    request.delete(`/enrollment/${enrollmentId}/guardians/${guardianId}`),
  getEnrollmentGuardians: (enrollmentId: number | string) =>
    request.get(`/enrollment/${enrollmentId}/guardians`),
  getMyEnrollments: () => request.get('/enrollment/my-enrollments'),
  generateInviteCode: (enrollmentId: number | string) =>
    request.post(`/enrollment/${enrollmentId}/invite-code`),
  bindByInviteCode: (data: {
    inviteCode: string
    relationship?: string
    isPrimary?: boolean
    guardianPhone?: string
  }) => request.post('/enrollment/bind-by-code', data),
}

// ========== 家长申请管理 ==========
export const parentApplicationAPI = {
  getMyApplications: () => request.get('/parent/my-applications'),
  createLeaveRequest: (data: {
    enrollmentId: number | string
    startDate: string
    endDate: string
    type: 'SICK' | 'PERSONAL' | 'OTHER'
    reason?: string
  }) => request.post('/parent/leave-request', data),
  createMedicationRequest: (data: {
    enrollmentId: number | string
    medicineName: string
    dosage?: string
    frequency?: string
    startDate: string
    endDate: string
    instructions?: string
  }) => request.post('/parent/medication-request', data),
  createPickupDelegation: (data: {
    enrollmentId: number | string
    pickupDate: string
    pickupPersonName: string
    pickupRelationship?: string
    pickupPhone?: string
    reason?: string
  }) => request.post('/parent/pickup-delegation', data),
  cancelApplication: (data: {
    applicationType: 'LEAVE' | 'MEDICATION' | 'PICKUP'
    applicationId: number | string
    reason?: string
  }) => request.post('/parent/cancel-application', data),
  getMyBills: () => request.get('/parent/my-bills'),
}
