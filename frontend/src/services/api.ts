import { request, uploadFile, uploadFiles } from './http'
import type {
  AdmissionLeadForm,
  AdmissionReviewForm,
  AdmissionTrialForm,
  AttendanceCheckInForm,
  AttendanceCheckOutForm,
  AttendanceStatusForm,
  CareRecordForm,
  CareRecordUpdateForm,
  ChildDevelopmentAssessmentForm,
  DailyReportGenerateForm,
  DailyReportUpdateForm,
  HealthObservationForm,
  HealthObservationUpdateForm,
  HardwareDeviceForm,
  HardwareEventIngestForm,
  HardwareEventStatusForm,
  AllergyTagForm,
  InfectiousDiseaseCreateForm,
  InfectiousDiseaseUpdateForm,
  LeaveRequestForm,
  LeaveReviewForm,
  MealIntakeForm,
  MealPlanForm,
  MedicationAdministrationForm,
  MedicationRequestForm,
  MedicationReviewForm,
  IncidentReportForm,
  IncidentReportUpdateForm,
  AnnouncementForm,
  AnnouncementUpdateForm,
  BillingPaymentForm,
  BillingStatementForm,
  BossDashboard,
  DirectorDashboard,
  DirectorWorkbench,
  UserStaffInfo,
  FeeItemForm,
  PickupDelegationForm,
  PickupDelegationReviewForm,
  PickupPersonForm,
  PickupPersonUpdateForm,
  RegulatoryReport,
  SafetyLedgerForm,
  SafetyLedgerHandleForm,
  SafetyLedgerTemplateForm,
} from '@/types'

export { request, uploadFile, uploadFiles }

export * from './legacyApi'
export * from './organizationApi'

export const attendanceAPI = {
  checkIn: (data: AttendanceCheckInForm) => request.post('/attendance/check-in', data),
  checkOut: (data: AttendanceCheckOutForm) => request.post('/attendance/check-out', data),
  markAbsent: (data: AttendanceStatusForm) => request.post('/attendance/absent', data),
  getClassroomAttendance: (classroomId: number | string, date?: string) => {
    const query = date ? `?date=${date}` : ''
    return request.get(`/attendance/classroom/${classroomId}${query}`)
  },
  getBabyAttendance: (babyId: number | string, startDate?: string, endDate?: string) => {
    const params = new URLSearchParams()
    if (startDate) params.set('startDate', startDate)
    if (endDate) params.set('endDate', endDate)
    const query = params.toString() ? `?${params.toString()}` : ''
    return request.get(`/attendance/baby/${babyId}${query}`)
  },
  createLeaveRequest: (data: LeaveRequestForm) => request.post('/attendance/leave/request', data),
  approveLeave: (leaveRequestId: number | string, data: LeaveReviewForm) =>
    request.post(`/attendance/leave/${leaveRequestId}/approve`, data),
  rejectLeave: (leaveRequestId: number | string, data: LeaveReviewForm) =>
    request.post(`/attendance/leave/${leaveRequestId}/reject`, data),
  getClassroomLeaveRequests: (classroomId: number | string) =>
    request.get(`/attendance/leave/classroom/${classroomId}`),
  getBabyLeaveRequests: (babyId: number | string) =>
    request.get(`/attendance/leave/baby/${babyId}`),
}

export const careRecordAPI = {
  createRecord: (data: CareRecordForm) => request.post('/care-record/create', data),
  updateRecord: (recordId: number | string, data: CareRecordUpdateForm) =>
    request.put(`/care-record/${recordId}`, data),
  deleteRecord: (recordId: number | string) => request.delete(`/care-record/${recordId}`),
  getClassroomRecords: (classroomId: number | string, date?: string) => {
    const query = date ? `?date=${date}` : ''
    return request.get(`/care-record/classroom/${classroomId}${query}`)
  },
  getBabyRecords: (babyId: number | string, date?: string) => {
    const query = date ? `?date=${date}` : ''
    return request.get(`/care-record/baby/${babyId}${query}`)
  },
  getEnrollmentRecords: (enrollmentId: number | string, date?: string) => {
    const query = date ? `?date=${date}` : ''
    return request.get(`/care-record/enrollment/${enrollmentId}${query}`)
  },
}

export const dailyReportAPI = {
  generateReport: (data: DailyReportGenerateForm) => request.post('/daily-report/generate', data),
  generateAiDraft: (data: DailyReportGenerateForm) =>
    request.post('/daily-report/ai-draft/generate', data),
  updateReport: (reportId: number | string, data: DailyReportUpdateForm) =>
    request.put(`/daily-report/${reportId}`, data),
  publishReport: (reportId: number | string) =>
    request.post(`/daily-report/${reportId}/publish`),
  submitForReview: (reportId: number | string) =>
    request.post(`/daily-report/${reportId}/submit`),
  approveReport: (reportId: number | string) =>
    request.post(`/daily-report/${reportId}/approve`),
  rejectReport: (reportId: number | string, reason: string) =>
    request.post(`/daily-report/${reportId}/reject`, { reason }),
  getBabyReport: (babyId: number | string, date?: string) => {
    const query = date ? `?date=${date}` : ''
    return request.get(`/daily-report/baby/${babyId}${query}`)
  },
  getBabyReports: (babyId: number | string) =>
    request.get(`/daily-report/baby/${babyId}/list`),
  getClassroomReports: (classroomId: number | string, date?: string) => {
    const query = date ? `?date=${date}` : ''
    return request.get(`/daily-report/classroom/${classroomId}${query}`)
  },
}

export const healthObservationAPI = {
  createObservation: (data: HealthObservationForm) =>
    request.post('/health-observation/create', data),
  updateObservation: (observationId: number | string, data: HealthObservationUpdateForm) =>
    request.put(`/health-observation/${observationId}`, data),
  deleteObservation: (observationId: number | string) =>
    request.delete(`/health-observation/${observationId}`),
  getClassroomObservations: (classroomId: number | string, date?: string) => {
    const query = date ? `?date=${date}` : ''
    return request.get(`/health-observation/classroom/${classroomId}${query}`)
  },
  getBabyObservations: (babyId: number | string, date?: string) => {
    const query = date ? `?date=${date}` : ''
    return request.get(`/health-observation/baby/${babyId}${query}`)
  },
  getEnrollmentObservations: (enrollmentId: number | string, date?: string) => {
    const query = date ? `?date=${date}` : ''
    return request.get(`/health-observation/enrollment/${enrollmentId}${query}`)
  },
}

export const pickupAPI = {
  createPickupPerson: (data: PickupPersonForm) => request.post('/pickup/person/create', data),
  updatePickupPerson: (personId: number | string, data: PickupPersonUpdateForm) =>
    request.put(`/pickup/person/${personId}`, data),
  getBabyPickupPersons: (babyId: number | string) =>
    request.get(`/pickup/person/baby/${babyId}`),
  getClassroomPickupPersons: (classroomId: number | string) =>
    request.get(`/pickup/person/classroom/${classroomId}`),
  createDelegation: (data: PickupDelegationForm) =>
    request.post('/pickup/delegation/create', data),
  approveDelegation: (delegationId: number | string, data: PickupDelegationReviewForm) =>
    request.post(`/pickup/delegation/${delegationId}/approve`, data),
  rejectDelegation: (delegationId: number | string, data: PickupDelegationReviewForm) =>
    request.post(`/pickup/delegation/${delegationId}/reject`, data),
  getBabyDelegations: (babyId: number | string) =>
    request.get(`/pickup/delegation/baby/${babyId}`),
  getClassroomDelegations: (classroomId: number | string, date?: string) => {
    const query = date ? `?date=${date}` : ''
    return request.get(`/pickup/delegation/classroom/${classroomId}${query}`)
  },

  // ========== 长辈接送确认（T073） ==========
  elderConfirmDelegation: (delegationId: number | string) =>
    request.post(`/pickup/delegation/${delegationId}/elder-confirm`),
}

export const medicationCareAPI = {
  createAllergyTag: (data: AllergyTagForm) =>
    request.post('/medication-care/allergy/create', data),
  updateAllergyTag: (allergyId: number | string, data: AllergyTagForm) =>
    request.put(`/medication-care/allergy/${allergyId}`, data),
  getBabyAllergies: (babyId: number | string) =>
    request.get(`/medication-care/allergy/baby/${babyId}`),
  createMedicationRequest: (data: MedicationRequestForm) =>
    request.post('/medication-care/request/create', data),
  approveMedication: (medicationRequestId: number | string, data: MedicationReviewForm) =>
    request.post(`/medication-care/request/${medicationRequestId}/approve`, data),
  rejectMedication: (medicationRequestId: number | string, data: MedicationReviewForm) =>
    request.post(`/medication-care/request/${medicationRequestId}/reject`, data),
  recordAdministration: (data: MedicationAdministrationForm) =>
    request.post('/medication-care/administration/create', data),
  getBabyMedications: (babyId: number | string) =>
    request.get(`/medication-care/request/baby/${babyId}`),
  getClassroomMedications: (classroomId: number | string) =>
    request.get(`/medication-care/request/classroom/${classroomId}`),
}

export const incidentReportAPI = {
  createReport: (data: IncidentReportForm) => request.post('/incident-report/create', data),
  updateReport: (reportId: number | string, data: IncidentReportUpdateForm) =>
    request.put(`/incident-report/${reportId}`, data),
  closeReport: (reportId: number | string) => request.post(`/incident-report/${reportId}/close`),
  confirmByParent: (reportId: number | string) =>
    request.post(`/incident-report/${reportId}/parent-confirm`),
  getBabyReports: (babyId: number | string) => request.get(`/incident-report/baby/${babyId}`),
  getClassroomReports: (classroomId: number | string, status?: string) => {
    const query = status ? `?status=${status}` : ''
    return request.get(`/incident-report/classroom/${classroomId}${query}`)
  },
}

export const announcementAPI = {
  createAnnouncement: (data: AnnouncementForm) => request.post('/announcement/create', data),
  updateAnnouncement: (announcementId: number | string, data: AnnouncementUpdateForm) =>
    request.put(`/announcement/${announcementId}`, data),
  publishAnnouncement: (announcementId: number | string) =>
    request.post(`/announcement/${announcementId}/publish`),
  markRead: (announcementId: number | string) =>
    request.post(`/announcement/${announcementId}/read`),
  getOrganizationAnnouncements: (organizationId: number | string) =>
    request.get(`/announcement/organization/${organizationId}`),
  getClassroomAnnouncements: (classroomId: number | string) =>
    request.get(`/announcement/classroom/${classroomId}`),
}

export const billingAPI = {
  createFeeItem: (data: FeeItemForm) => request.post('/billing/fee-item/create', data),
  updateFeeItem: (feeItemId: number | string, data: FeeItemForm) =>
    request.put(`/billing/fee-item/${feeItemId}`, data),
  getOrganizationFeeItems: (organizationId: number | string) =>
    request.get(`/billing/fee-item/organization/${organizationId}`),
  createBill: (data: BillingStatementForm) => request.post('/billing/bill/create', data),
  markPaid: (billId: number | string, data: BillingPaymentForm) =>
    request.post(`/billing/bill/${billId}/paid`, data),
  cancelBill: (billId: number | string, data: BillingPaymentForm) =>
    request.post(`/billing/bill/${billId}/cancel`, data),
  getOrganizationBills: (organizationId: number | string) =>
    request.get(`/billing/bill/organization/${organizationId}`),
  getBabyBills: (babyId: number | string) =>
    request.get(`/billing/bill/baby/${babyId}`),
  getFinanceWorkbench: (organizationId: number | string) =>
    request.get(`/billing/finance-workbench/${organizationId}`),
}

export const directorDashboardAPI = {
  getOrganizationOverview: (organizationId: number | string, date?: string) => {
    const query = date ? `?date=${date}` : ''
    return request.get<DirectorDashboard>(`/director-dashboard/organization/${organizationId}${query}`)
  },
  getWorkbench: (organizationId: number | string) => {
    return request.get<DirectorWorkbench>(`/director-dashboard/workbench/${organizationId}`)
  },
}

export const userStaffAPI = {
  getMyStaffInfo: () => {
    return request.get<UserStaffInfo>('/user/my-staff-info')
  },
}

export const bossDashboardAPI = {
  getOverview: () => {
    return request.get<BossDashboard>('/api/boss/dashboard/overview')
  },
}

export const admissionLeadAPI = {
  createLead: (data: AdmissionLeadForm) => request.post('/admission-lead/create', data),
  updateLead: (leadId: number | string, data: AdmissionLeadForm) =>
    request.put(`/admission-lead/${leadId}`, data),
  reviewApplication: (leadId: number | string, data: AdmissionReviewForm) =>
    request.post(`/admission-lead/${leadId}/review`, data),
  startTrial: (leadId: number | string, data: AdmissionTrialForm) =>
    request.post(`/admission-lead/${leadId}/trial/start`, data),
  finishTrial: (leadId: number | string, data: AdmissionTrialForm) =>
    request.post(`/admission-lead/${leadId}/trial/finish`, data),
  getOrganizationLeads: (organizationId: number | string, status?: string) => {
    const query = status ? `?status=${status}` : ''
    return request.get(`/admission-lead/organization/${organizationId}${query}`)
  },
  getLeadDetail: (leadId: number | string) => request.get(`/admission-lead/${leadId}`),
  addFollowUp: (leadId: number | string, data: any) =>
    request.post(`/admission-lead/${leadId}/follow-up`, data),
  getFollowUps: (leadId: number | string) =>
    request.get(`/admission-lead/${leadId}/follow-ups`),
  getFunnelStats: (organizationId: number | string) =>
    request.get(`/admission-lead/funnel/${organizationId}`),
  convertToEnrollment: (leadId: number | string, data: {
    classroomId: number | string
    enrolledAt?: string
    allergyNotes?: string
    medicalNotes?: string
    specialCareNotes?: string
    emergencyContactName?: string
    emergencyContactPhone?: string
  }) => request.post(`/admission-lead/${leadId}/convert`, data),
}

export const mealPlanAPI = {
  createMealPlan: (data: MealPlanForm) => request.post('/meal-plan/create', data),
  updateMealPlan: (mealPlanId: number | string, data: MealPlanForm) =>
    request.put(`/meal-plan/${mealPlanId}`, data),
  publishMealPlan: (mealPlanId: number | string) =>
    request.post(`/meal-plan/${mealPlanId}/publish`),
  getOrganizationMeals: (
    organizationId: number | string,
    options?: { date?: string; startDate?: string; endDate?: string },
  ) => {
    const params = new URLSearchParams()
    if (options?.date) params.set('date', options.date)
    if (options?.startDate) params.set('startDate', options.startDate)
    if (options?.endDate) params.set('endDate', options.endDate)
    const query = params.toString() ? `?${params.toString()}` : ''
    return request.get(`/meal-plan/organization/${organizationId}${query}`)
  },
  recordIntake: (data: MealIntakeForm) => request.post('/meal-plan/intake/record', data),
  getMealIntakes: (mealPlanId: number | string) =>
    request.get(`/meal-plan/${mealPlanId}/intakes`),
  getEnrollmentIntakes: (enrollmentId: number | string) =>
    request.get(`/meal-plan/intake/enrollment/${enrollmentId}`),
  getNutritionAnalysis: (
    organizationId: number | string,
    options?: { startDate?: string; endDate?: string },
  ) => {
    const params = new URLSearchParams()
    if (options?.startDate) params.set('startDate', options.startDate)
    if (options?.endDate) params.set('endDate', options.endDate)
    const query = params.toString() ? `?${params.toString()}` : ''
    return request.get(`/meal-plan/analysis/organization/${organizationId}${query}`)
  },
}

export const infectiousDiseaseAPI = {
  createRecord: (data: InfectiousDiseaseCreateForm) =>
    request.post('/infectious-disease/create', data),
  updateRecord: (recordId: number | string, data: InfectiousDiseaseUpdateForm) =>
    request.put(`/infectious-disease/${recordId}`, data),
  getClassroomRecords: (classroomId: number | string) =>
    request.get(`/infectious-disease/classroom/${classroomId}`),
  getOrganizationRecords: (organizationId: number | string) =>
    request.get(`/infectious-disease/organization/${organizationId}`),
  getRecordDetail: (recordId: number | string) =>
    request.get(`/infectious-disease/${recordId}`),
  getActiveCount: (organizationId: number | string) =>
    request.get(`/infectious-disease/organization/${organizationId}/active-count`),
}

export const safetyLedgerAPI = {
  createLedger: (data: SafetyLedgerForm) => request.post('/safety-ledger/create', data),
  updateLedger: (ledgerId: number | string, data: SafetyLedgerForm) =>
    request.put(`/safety-ledger/${ledgerId}`, data),
  markProcessing: (ledgerId: number | string, data?: SafetyLedgerHandleForm) =>
    request.post(`/safety-ledger/${ledgerId}/processing`, data),
  closeLedger: (ledgerId: number | string, data?: SafetyLedgerHandleForm) =>
    request.post(`/safety-ledger/${ledgerId}/close`, data),
  getOrganizationLedgers: (
    organizationId: number | string,
    options?: { startDate?: string; endDate?: string; type?: string; status?: string },
  ) => {
    const params = new URLSearchParams()
    if (options?.startDate) params.set('startDate', options.startDate)
    if (options?.endDate) params.set('endDate', options.endDate)
    if (options?.type) params.set('type', options.type)
    if (options?.status) params.set('status', options.status)
    const query = params.toString() ? `?${params.toString()}` : ''
    return request.get(`/safety-ledger/organization/${organizationId}${query}`)
  },
  getLedgerDetail: (ledgerId: number | string) => request.get(`/safety-ledger/${ledgerId}`),
  // 台账模板
  createTemplate: (data: SafetyLedgerTemplateForm) => request.post('/safety-ledger/template/create', data),
  updateTemplate: (templateId: number | string, data: SafetyLedgerTemplateForm) =>
    request.put(`/safety-ledger/template/${templateId}`, data),
  deleteTemplate: (templateId: number | string) =>
    request.post(`/safety-ledger/template/${templateId}/delete`),
  getOrganizationTemplates: (organizationId: number | string) =>
    request.get(`/safety-ledger/template/organization/${organizationId}`),
  // 周期任务生成与逾期检测
  generateTasks: (organizationId: number | string) =>
    request.post(`/safety-ledger/generate-tasks/${organizationId}`),
  checkOverdue: (organizationId: number | string) =>
    request.post(`/safety-ledger/check-overdue/${organizationId}`),
  getOverdueCount: (organizationId: number | string) =>
    request.get(`/safety-ledger/overdue-count/${organizationId}`),
}

export const regulatoryReportAPI = {
  getOrganizationReport: (
    organizationId: number | string,
    options?: { startDate?: string; endDate?: string },
  ) => {
    const params = new URLSearchParams()
    if (options?.startDate) params.set('startDate', options.startDate)
    if (options?.endDate) params.set('endDate', options.endDate)
    const query = params.toString() ? `?${params.toString()}` : ''
    return request.get<RegulatoryReport>(`/regulatory-report/organization/${organizationId}${query}`)
  },
  getOrganizationExportRows: (
    organizationId: number | string,
    options?: { startDate?: string; endDate?: string },
  ) => {
    const params = new URLSearchParams()
    if (options?.startDate) params.set('startDate', options.startDate)
    if (options?.endDate) params.set('endDate', options.endDate)
    const query = params.toString() ? `?${params.toString()}` : ''
    return request.get(`/regulatory-report/organization/${organizationId}/export-rows${query}`)
  },
}

export const hardwareIntegrationAPI = {
  createDevice: (data: HardwareDeviceForm) =>
    request.post('/hardware-integration/device/create', data),
  updateDevice: (deviceId: number | string, data: HardwareDeviceForm) =>
    request.put(`/hardware-integration/device/${deviceId}`, data),
  getDeviceDetail: (deviceId: number | string) =>
    request.get(`/hardware-integration/device/${deviceId}`),
  getOrganizationDevices: (
    organizationId: number | string,
    options?: { deviceType?: string; status?: string },
  ) => {
    const params = new URLSearchParams()
    if (options?.deviceType) params.set('deviceType', options.deviceType)
    if (options?.status) params.set('status', options.status)
    const query = params.toString() ? `?${params.toString()}` : ''
    return request.get(`/hardware-integration/organization/${organizationId}/devices${query}`)
  },
  ingestEvent: (data: HardwareEventIngestForm) =>
    request.post('/hardware-integration/event/ingest', data),
  updateEventStatus: (eventId: number | string, data: HardwareEventStatusForm) =>
    request.put(`/hardware-integration/event/${eventId}/status`, data),
  getDeviceEvents: (deviceId: number | string) =>
    request.get(`/hardware-integration/device/${deviceId}/events`),
  getOrganizationEvents: (
    organizationId: number | string,
    options?: { startDate?: string; endDate?: string },
  ) => {
    const params = new URLSearchParams()
    if (options?.startDate) params.set('startDate', options.startDate)
    if (options?.endDate) params.set('endDate', options.endDate)
    const query = params.toString() ? `?${params.toString()}` : ''
    return request.get(`/hardware-integration/organization/${organizationId}/events${query}`)
  },
}

export const childDevelopmentAssessmentAPI = {
  createAssessment: (data: ChildDevelopmentAssessmentForm) =>
    request.post('/child-development-assessment/create', data),
  updateAssessment: (assessmentId: number | string, data: ChildDevelopmentAssessmentForm) =>
    request.put(`/child-development-assessment/${assessmentId}`, data),
  getBabyAssessments: (babyId: number | string) =>
    request.get(`/child-development-assessment/baby/${babyId}`),
  getEnrollmentAssessments: (enrollmentId: number | string, assessmentMode?: string) => {
    const query = assessmentMode ? `?assessmentMode=${assessmentMode}` : ''
    return request.get(`/child-development-assessment/enrollment/${enrollmentId}${query}`)
  },
  getAssessmentDetail: (assessmentId: number | string) =>
    request.get(`/child-development-assessment/${assessmentId}`),
}


