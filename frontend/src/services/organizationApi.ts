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
    role: 'DIRECTOR' | 'TEACHER' | 'CAREGIVER' | 'FINANCE'
  }) => request.post('/staff/create', data),
  getOrganizationStaff: (organizationId: number | string) =>
    request.get(`/staff/organization/${organizationId}`),
  getStaffDetail: (staffId: number | string) =>
    request.get(`/staff/${staffId}`),
  updateStaff: (
    staffId: number | string,
    data: {
      role?: 'DIRECTOR' | 'TEACHER' | 'CAREGIVER' | 'FINANCE'
      status?: 'ACTIVE' | 'DISABLED'
    },
  ) => request.put(`/staff/${staffId}`, data),
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
}
