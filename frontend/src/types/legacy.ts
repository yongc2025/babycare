// 用户相关类型
export interface User {
  id: string
  username: string
  email: string
  phone: string
  avatar?: string
  nickname: string
  city?: string
  role: 'PARENT' | 'ADMIN'
  createdAt: string
  updatedAt: string
}

// 家庭相关类型
export interface Family {
  id: string
  name: string
  inviteCode: string
  babies: Baby[]
  members: FamilyMember[]
  createdAt: string
  updatedAt: string
}

export interface FamilyMember {
  id: string
  userId: string
  familyId: string
  role: 'CREATOR' | 'PARENT' | 'GRANDPARENT' | 'RELATIVE'
  nickname: string
  avatar?: string
  joinedAt: string
}

// 宝宝相关类型
export interface Baby {
  id: string
  name: string
  gender: 'MALE' | 'FEMALE'
  birthday: string
  avatar?: string
  familyId: string
  createdAt: string
  updatedAt: string
}

// 机构相关类型
export interface Organization {
  id: string
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
  status: 'ACTIVE' | 'DISABLED'
  statusDescription?: string
  createdBy: string
  createdAt: string
  updatedAt: string
}

export interface RegulatoryExportRow {
  category: string
  fieldName: string
  fieldCode: string
  value: string
  status: 'READY' | 'MISSING' | string
}

export interface RegulatoryReport {
  organizationId: string
  organizationName: string
  registrationNo?: string
  licenseNo?: string
  legalRepresentative?: string
  supervisorDepartment?: string
  organizationLevel?: string
  operationType?: string
  periodStart: string
  periodEnd: string
  classroomCount: number
  totalCapacity: number
  activeEnrollmentCount: number
  capacityUsageRate: number
  staffCount: number
  directorCount: number
  teacherCount: number
  caregiverCount: number
  financeCount: number
  attendanceRecordCount: number
  leaveRecordCount: number
  healthObservationCount: number
  abnormalObservationCount: number
  followUpObservationCount: number
  safetyLedgerCount: number
  openSafetyLedgerCount: number
  closedSafetyLedgerCount: number
  overdueSafetyLedgerCount: number
  missingRegulatoryFields: string[]
  exportRows: RegulatoryExportRow[]
}

export interface HardwareDevice {
  id: string
  organizationId: string
  organizationName?: string
  classroomId?: string
  classroomName?: string
  deviceCode: string
  name: string
  deviceType:
    | 'ATTENDANCE_TERMINAL'
    | 'HEALTH_CHECK_ROBOT'
    | 'CAMERA'
    | 'ACCESS_CONTROL'
    | 'SMART_CLASS_CARD'
    | 'OTHER'
  deviceTypeDescription?: string
  vendor?: string
  model?: string
  location?: string
  integrationMode?: string
  status: 'ACTIVE' | 'DISABLED' | 'MAINTENANCE'
  statusDescription?: string
  lastSeenAt?: string
  remark?: string
  createdAt: string
  updatedAt: string
}

export interface HardwareEvent {
  id: string
  deviceId: string
  deviceCode: string
  deviceName?: string
  organizationId: string
  organizationName?: string
  classroomId?: string
  classroomName?: string
  enrollmentId?: string
  babyName?: string
  eventType:
    | 'ATTENDANCE_CHECK_IN'
    | 'ATTENDANCE_CHECK_OUT'
    | 'HEALTH_MEASUREMENT'
    | 'FACE_CAPTURE'
    | 'PICKUP_VERIFY'
    | 'SAFETY_ALERT'
    | 'RAW_MESSAGE'
  eventTypeDescription?: string
  eventTime: string
  eventKey?: string
  subjectRef?: string
  confidence?: number
  payload?: string
  status: 'RECEIVED' | 'IGNORED' | 'PROCESSED' | 'FAILED'
  statusDescription?: string
  processedAt?: string
  processRemark?: string
  createdAt: string
  updatedAt: string
}

export interface Classroom {
  id: string
  organizationId: string
  organizationName?: string
  name: string
  ageRangeMinMonths?: number
  ageRangeMaxMonths?: number
  capacity?: number
  status: 'ACTIVE' | 'DISABLED'
  statusDescription?: string
  createdAt: string
  updatedAt: string
}

export interface Staff {
  id: string
  organizationId: string
  organizationName?: string
  userId: string
  username: string
  nickname: string
  phone?: string
  email?: string
  role: 'DIRECTOR' | 'TEACHER' | 'CAREGIVER' | 'FINANCE'
  roleDescription?: string
  status: 'ACTIVE' | 'DISABLED'
  statusDescription?: string
  createdAt: string
  updatedAt: string
}

export interface Enrollment {
  id: string
  babyId: string
  babyName: string
  babyGender: 'MALE' | 'FEMALE'
  babyBirthday: string
  familyId: string
  organizationId: string
  organizationName?: string
  classroomId: string
  classroomName?: string
  status: 'PENDING' | 'ACTIVE' | 'SUSPENDED' | 'WITHDRAWN'
  statusDescription?: string
  enrolledAt?: string
  allergyNotes?: string
  medicalNotes?: string
  specialCareNotes?: string
  emergencyContactName?: string
  emergencyContactPhone?: string
  createdAt: string
  updatedAt: string
}

export interface AttendanceRecord {
  id: string
  enrollmentId: string
  babyId: string
  babyName: string
  classroomId: string
  classroomName?: string
  organizationId: string
  organizationName?: string
  attendanceDate: string
  status: 'ABSENT' | 'CHECKED_IN' | 'CHECKED_OUT' | 'LEAVE'
  statusDescription?: string
  checkInAt?: string
  checkOutAt?: string
  temperature?: number
  pickupPersonName?: string
  pickupRelationship?: string
  pickupPhone?: string
  source?: string
  remark?: string
  recordedById?: string
  recordedByName?: string
  createdAt: string
  updatedAt: string
}

export interface LeaveRequest {
  id: string
  enrollmentId: string
  babyId: string
  babyName: string
  classroomId: string
  classroomName?: string
  organizationId: string
  organizationName?: string
  startDate: string
  endDate: string
  type: 'SICK' | 'PERSONAL' | 'OTHER'
  typeDescription?: string
  status: 'PENDING' | 'APPROVED' | 'REJECTED' | 'CANCELLED'
  statusDescription?: string
  reason?: string
  requestedById: string
  requestedByName?: string
  reviewedById?: string
  reviewedByName?: string
  reviewedAt?: string
  reviewRemark?: string
  createdAt: string
  updatedAt: string
}

export interface CareRecord {
  id: string
  enrollmentId: string
  babyId: string
  babyName: string
  classroomId: string
  classroomName?: string
  organizationId: string
  organizationName?: string
  recordDate: string
  recordTime: string
  type: 'FEEDING' | 'WATER' | 'SLEEP' | 'TOILET' | 'TEMPERATURE' | 'MOOD' | 'ACTIVITY'
  typeDescription?: string
  valueText?: string
  amount?: number
  unit?: string
  startedAt?: string
  endedAt?: string
  remark?: string
  source?: string
  recordedById?: string
  recordedByName?: string
  createdAt: string
  updatedAt: string
}

export interface DailyReport {
  id: string
  enrollmentId: string
  babyId: string
  babyName: string
  classroomId: string
  classroomName?: string
  organizationId: string
  organizationName?: string
  reportDate: string
  status: 'DRAFT' | 'PUBLISHED'
  statusDescription?: string
  summary?: string
  attendanceSummary?: string
  careSummary?: string
  healthSummary?: string
  activitySummary?: string
  teacherComment?: string
  aiDraftContent?: string
  publishedAt?: string
  publishedById?: string
  publishedByName?: string
  createdAt: string
  updatedAt: string
}

export interface HealthObservation {
  id: string
  enrollmentId: string
  babyId: string
  babyName: string
  classroomId: string
  classroomName?: string
  organizationId: string
  organizationName?: string
  observationDate: string
  observationTime: string
  type: 'MORNING_CHECK' | 'NOON_CHECK' | 'FULL_DAY_OBSERVATION'
  typeDescription?: string
  temperature?: number
  touchStatus?: string
  lookStatus?: string
  askStatus?: string
  checkStatus?: string
  symptoms?: string
  actionTaken?: string
  abnormal: boolean
  followUpRequired: boolean
  source?: string
  recordedById?: string
  recordedByName?: string
  createdAt: string
  updatedAt: string
}

export interface PickupPerson {
  id: string
  enrollmentId: string
  babyId: string
  babyName: string
  classroomId: string
  classroomName?: string
  organizationId: string
  organizationName?: string
  name: string
  relationship?: string
  phone?: string
  identityNo?: string
  photoUrl?: string
  status: 'ACTIVE' | 'DISABLED'
  statusDescription?: string
  remark?: string
  createdById?: string
  createdByName?: string
  createdAt: string
  updatedAt: string
}

export interface PickupDelegation {
  id: string
  enrollmentId: string
  babyId: string
  babyName: string
  classroomId: string
  classroomName?: string
  organizationId: string
  organizationName?: string
  pickupDate: string
  pickupPersonName: string
  pickupRelationship?: string
  pickupPhone?: string
  reason?: string
  status: 'PENDING' | 'APPROVED' | 'REJECTED' | 'CANCELLED'
  statusDescription?: string
  pickupCode?: string
  requestedById: string
  requestedByName?: string
  reviewedById?: string
  reviewedByName?: string
  reviewedAt?: string
  reviewRemark?: string
  createdAt: string
  updatedAt: string
}

export interface AllergyTag {
  id: string
  enrollmentId: string
  babyId: string
  babyName: string
  allergen: string
  reaction?: string
  severity: 'MILD' | 'MODERATE' | 'SEVERE'
  severityDescription?: string
  status: 'ACTIVE' | 'INACTIVE'
  statusDescription?: string
  remark?: string
  createdById?: string
  createdByName?: string
  createdAt: string
  updatedAt: string
}

export interface MedicationAdministration {
  id: string
  medicationRequestId: string
  administeredAt: string
  actualDosage?: string
  reactionObserved: boolean
  remark?: string
  administeredById: string
  administeredByName?: string
  createdAt: string
  updatedAt: string
}

export interface MedicationRequest {
  id: string
  enrollmentId: string
  babyId: string
  babyName: string
  classroomId: string
  classroomName?: string
  organizationId: string
  organizationName?: string
  medicineName: string
  dosage?: string
  frequency?: string
  startDate: string
  endDate: string
  instructions?: string
  status: 'PENDING' | 'APPROVED' | 'REJECTED' | 'CANCELLED'
  statusDescription?: string
  requestedById: string
  requestedByName?: string
  reviewedById?: string
  reviewedByName?: string
  reviewedAt?: string
  reviewRemark?: string
  administrations?: MedicationAdministration[]
  createdAt: string
  updatedAt: string
}

export interface IncidentReport {
  id: string
  enrollmentId: string
  babyId: string
  babyName: string
  classroomId: string
  classroomName?: string
  organizationId: string
  organizationName?: string
  type: 'HEALTH_ABNORMAL' | 'INJURY' | 'SAFETY_EVENT' | 'BEHAVIOR' | 'OTHER'
  typeDescription?: string
  severity: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL'
  severityDescription?: string
  status: 'OPEN' | 'PROCESSING' | 'CLOSED'
  statusDescription?: string
  occurredAt: string
  location?: string
  title: string
  description?: string
  handlingProcess?: string
  followUpPlan?: string
  parentNotified: boolean
  parentNotifiedAt?: string
  parentConfirmed: boolean
  parentConfirmedAt?: string
  reportedById: string
  reportedByName?: string
  handledById?: string
  handledByName?: string
  confirmedById?: string
  confirmedByName?: string
  createdAt: string
  updatedAt: string
}

export interface Announcement {
  id: string
  organizationId: string
  organizationName?: string
  classroomId?: string
  classroomName?: string
  scope: 'ORGANIZATION' | 'CLASSROOM'
  scopeDescription?: string
  status: 'DRAFT' | 'PUBLISHED'
  statusDescription?: string
  title: string
  content: string
  requireReceipt: boolean
  publishedAt?: string
  publishedById?: string
  publishedByName?: string
  readByCurrentUser: boolean
  readAt?: string
  receiptCount: number
  createdAt: string
  updatedAt: string
}

export interface FeeItem {
  id: string
  organizationId: string
  organizationName?: string
  name: string
  description?: string
  amount: number
  status: 'ACTIVE' | 'DISABLED'
  statusDescription?: string
  createdAt: string
  updatedAt: string
}

export interface BillingStatement {
  id: string
  organizationId: string
  organizationName?: string
  enrollmentId: string
  babyId: string
  babyName: string
  feeItemId?: string
  feeItemName?: string
  title: string
  amount: number
  dueDate?: string
  status: 'UNPAID' | 'PAID' | 'CANCELLED'
  statusDescription?: string
  paidAt?: string
  paymentMethod?: string
  remark?: string
  createdById?: string
  createdByName?: string
  paidById?: string
  paidByName?: string
  createdAt: string
  updatedAt: string
}

export interface DirectorDashboard {
  organizationId: string
  organizationName: string
  date: string
  classroomCount: number
  activeEnrollmentCount: number
  expectedAttendanceCount: number
  checkedInCount: number
  leaveCount: number
  attendanceRate: number
  openIncidentCount: number
  unpaidBillCount: number
  unpaidBillAmount: number
  publishedAnnouncementCount: number
}

export interface AdmissionLead {
  id: string
  organizationId: string
  organizationName?: string
  intendedClassroomId?: string
  intendedClassroomName?: string
  childName: string
  childGender?: string
  childBirthday?: string
  guardianName: string
  guardianPhone: string
  source: 'ONLINE' | 'REFERRAL' | 'OPEN_DAY' | 'WALK_IN' | 'OTHER'
  sourceDescription?: string
  intentionLevel: 'HIGH' | 'MEDIUM' | 'LOW' | 'LOST'
  intentionLevelDescription?: string
  status:
    | 'NEW'
    | 'FOLLOWING'
    | 'APPLIED'
    | 'APPROVED'
    | 'REJECTED'
    | 'TRIALING'
    | 'TRIAL_COMPLETED'
    | 'ENROLLED'
    | 'LOST'
  statusDescription?: string
  preferredStartDate?: string
  remark?: string
  reviewedById?: string
  reviewedByName?: string
  reviewedAt?: string
  reviewRemark?: string
  trialStartDate?: string
  trialEndDate?: string
  trialFeedback?: string
  createdAt: string
  updatedAt: string
}

export interface MealIntakeRecord {
  id: string
  mealPlanId: string
  enrollmentId: string
  babyId: string
  babyName: string
  intakeLevel: 'ALL' | 'MOST' | 'HALF' | 'LESS' | 'NONE'
  intakeLevelDescription?: string
  allergyReaction: boolean
  reactionNotes?: string
  remark?: string
  recordedById?: string
  recordedByName?: string
  createdAt: string
  updatedAt: string
}

export interface MealPlan {
  id: string
  organizationId: string
  organizationName?: string
  mealDate: string
  mealType: 'BREAKFAST' | 'MORNING_SNACK' | 'LUNCH' | 'AFTERNOON_SNACK' | 'DINNER'
  mealTypeDescription?: string
  title: string
  foodItems?: string
  allergenNotes?: string
  nutritionNotes?: string
  status: 'DRAFT' | 'PUBLISHED'
  statusDescription?: string
  createdById?: string
  createdByName?: string
  intakeRecords?: MealIntakeRecord[]
  createdAt: string
  updatedAt: string
}

export interface SafetyLedger {
  id: string
  organizationId: string
  organizationName?: string
  relatedIncidentId?: string
  relatedIncidentTitle?: string
  ledgerDate: string
  ledgerType:
    | 'DISINFECTION'
    | 'FOOD_SAMPLE'
    | 'FACILITY_INSPECTION'
    | 'FIRE_SAFETY'
    | 'SAFETY_EDUCATION'
    | 'INCIDENT_FOLLOWUP'
    | 'OTHER'
  ledgerTypeDescription?: string
  title: string
  content?: string
  location?: string
  responsiblePerson?: string
  dueAt?: string
  completedAt?: string
  status: 'OPEN' | 'PROCESSING' | 'CLOSED' | 'OVERDUE'
  statusDescription?: string
  handleRemark?: string
  createdById?: string
  createdByName?: string
  handledById?: string
  handledByName?: string
  createdAt: string
  updatedAt: string
}

export interface ChildDevelopmentAssessment {
  id: string
  enrollmentId: string
  babyId: string
  babyName: string
  organizationId: string
  organizationName?: string
  classroomId: string
  classroomName?: string
  assessmentDate: string
  childAgeMonths: number
  assessmentMode: 'TODDLER_MILESTONE' | 'PRESCHOOL_DOMAIN'
  assessmentModeDescription?: string
  title: string
  grossMotorScore?: number
  fineMotorScore?: number
  languageScore?: number
  cognitiveScore?: number
  socialEmotionalScore?: number
  healthScore?: number
  scienceScore?: number
  artScore?: number
  maxScore: number
  overallLevel: 'ADVANCED' | 'AGE_APPROPRIATE' | 'NEEDS_SUPPORT' | 'DELAY_RISK'
  overallLevelDescription?: string
  summary?: string
  recommendation?: string
  radarData?: string
  assessedById?: string
  assessedByName?: string
  createdAt: string
  updatedAt: string
}

// 成长记录相关类型
export interface GrowthRecord {
  id: string
  babyId: string
  type: 'PHOTO' | 'VIDEO' | 'DIARY' | 'MILESTONE'
  title: string
  content?: string
  mediaUrls: string[]
  tags: string[]
  createdBy: string
  createdAt: string
  updatedAt: string
}

export interface Milestone {
  id: string
  babyId: string
  title: string
  description: string
  achievedAt: string
  category: 'MOTOR' | 'LANGUAGE' | 'COGNITIVE' | 'SOCIAL'
  photos: string[]
  createdAt: string
}

// AI育儿相关类型
export interface AIChat {
  id: string
  userId: string
  babyId?: string
  messages: ChatMessage[]
  topic?: string
  createdAt: string
  updatedAt: string
}

export interface ChatMessage {
  id: string
  role: 'USER' | 'ASSISTANT'
  content: string
  timestamp: string
}

export interface DevelopmentAssessment {
  id: string
  babyId: string
  ageInMonths: number
  areas: AssessmentArea[]
  overallScore: number
  recommendations: string[]
  assessedAt: string
  nextAssessmentDate: string
}

export interface AssessmentArea {
  name: 'GROSS_MOTOR' | 'FINE_MOTOR' | 'LANGUAGE' | 'COGNITIVE' | 'SOCIAL_EMOTIONAL'
  score: number
  maxScore: number
  skills: AssessmentSkill[]
}

export interface AssessmentSkill {
  name: string
  achieved: boolean
  expectedAgeRange: [number, number] // 预期达成年龄范围（月）
  description: string
}

// 教育规划相关类型
export interface EducationPlan {
  id: string
  babyId: string
  title: string
  description: string
  targetAgeRange: [number, number]
  activities: Activity[]
  progress: PlanProgress
  createdAt: string
  updatedAt: string
}

export interface Activity {
  id: string
  title: string
  description: string
  type: 'PLAY' | 'LEARNING' | 'EXERCISE' | 'ART' | 'MUSIC'
  duration: number // 分钟
  materials: string[]
  instructions: string[]
  completed: boolean
  completedAt?: string
}

export interface PlanProgress {
  totalActivities: number
  completedActivities: number
  progressPercentage: number
  lastActivity?: string
}

// 家庭协作相关类型
export interface FamilyPost {
  id: string
  familyId: string
  authorId: string
  authorName: string
  authorAvatar?: string
  content: string
  images: string[]
  videos: string[]
  likes: string[] // 点赞用户ID列表
  comments: Comment[]
  createdAt: string
  updatedAt: string
}

export interface Comment {
  id: string
  postId: string
  authorId: string
  authorName: string
  authorAvatar?: string
  content: string
  createdAt: string
}

export interface FamilyTask {
  id: string
  familyId: string
  title: string
  description: string
  assignedTo: string[]
  assignedBy: string
  dueDate?: string
  priority: 'LOW' | 'MEDIUM' | 'HIGH'
  status: 'PENDING' | 'IN_PROGRESS' | 'COMPLETED'
  category: 'FEEDING' | 'DIAPER' | 'BATH' | 'PLAY' | 'EDUCATION' | 'MEDICAL' | 'OTHER'
  completedAt?: string
  completedBy?: string
  createdAt: string
  updatedAt: string
}

// 专家咨询相关类型
export interface Expert {
  id: string
  name: string
  title: string
  specialties: string[]
  avatar: string
  rating: number
  reviewCount: number
  experience: number // 年
  price: {
    textConsultation: number
    voiceConsultation: number
    videoConsultation: number
  }
  availability: boolean
}

export interface Consultation {
  id: string
  expertId: string
  userId: string
  babyId?: string
  type: 'TEXT' | 'VOICE' | 'VIDEO'
  topic: string
  description: string
  status: 'PENDING' | 'ACCEPTED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED'
  scheduledAt?: string
  startedAt?: string
  endedAt?: string
  messages: ConsultationMessage[]
  price: number
  rating?: number
  review?: string
  createdAt: string
  updatedAt: string
}

export interface ConsultationMessage {
  id: string
  consultationId: string
  senderId: string
  senderType: 'USER' | 'EXPERT'
  content: string
  type: 'TEXT' | 'VOICE' | 'VIDEO' | 'IMAGE' | 'FILE'
  mediaUrl?: string
  timestamp: string
}

// API响应类型
export interface ApiResponse<T = any> {
  success: boolean
  data?: T
  message?: string
  error?: string
  code?: number
}

export interface PaginatedResponse<T> {
  items: T[]
  total: number
  page: number
  pageSize: number
  totalPages: number
}

// 表单类型
export interface LoginForm {
  emailOrUsername: string
  password: string
  remember?: boolean
}

export interface RegisterForm {
  username: string
  password: string
  confirmPassword: string
  email?: string
  phone?: string
  nickname?: string
  agreement?: boolean
}

export interface BabyForm {
  name: string
  gender: 'MALE' | 'FEMALE'
  birthday: string
  avatar?: File
}

export interface FamilyForm {
  name: string
}

export interface OrganizationForm {
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
  status?: 'ACTIVE' | 'DISABLED'
}

export interface HardwareDeviceForm {
  organizationId: string
  classroomId?: string
  deviceCode: string
  name: string
  deviceType?:
    | 'ATTENDANCE_TERMINAL'
    | 'HEALTH_CHECK_ROBOT'
    | 'CAMERA'
    | 'ACCESS_CONTROL'
    | 'SMART_CLASS_CARD'
    | 'OTHER'
  vendor?: string
  model?: string
  location?: string
  integrationMode?: string
  status?: 'ACTIVE' | 'DISABLED' | 'MAINTENANCE'
  remark?: string
}

export interface HardwareEventIngestForm {
  deviceId?: string
  organizationId?: string
  deviceCode?: string
  classroomId?: string
  enrollmentId?: string
  eventType?:
    | 'ATTENDANCE_CHECK_IN'
    | 'ATTENDANCE_CHECK_OUT'
    | 'HEALTH_MEASUREMENT'
    | 'FACE_CAPTURE'
    | 'PICKUP_VERIFY'
    | 'SAFETY_ALERT'
    | 'RAW_MESSAGE'
  eventTime?: string
  eventKey?: string
  subjectRef?: string
  confidence?: number
  payload?: string
  rawOnly?: boolean
}

export interface HardwareEventStatusForm {
  status: 'RECEIVED' | 'IGNORED' | 'PROCESSED' | 'FAILED'
  processRemark?: string
}

export interface ClassroomForm {
  organizationId: string
  name: string
  ageRangeMinMonths?: number
  ageRangeMaxMonths?: number
  capacity?: number
  status?: 'ACTIVE' | 'DISABLED'
}

export interface StaffForm {
  organizationId: string
  userId: string
  role: 'DIRECTOR' | 'TEACHER' | 'CAREGIVER' | 'FINANCE'
  status?: 'ACTIVE' | 'DISABLED'
}

export interface EnrollmentForm {
  babyId: string
  organizationId: string
  classroomId: string
  enrolledAt?: string
  status?: 'PENDING' | 'ACTIVE' | 'SUSPENDED' | 'WITHDRAWN'
  allergyNotes?: string
  medicalNotes?: string
  specialCareNotes?: string
  emergencyContactName?: string
  emergencyContactPhone?: string
}

export interface AttendanceCheckInForm {
  enrollmentId: string
  attendanceDate?: string
  checkInAt?: string
  temperature?: number
  remark?: string
}

export interface AttendanceCheckOutForm {
  enrollmentId: string
  attendanceDate?: string
  checkOutAt?: string
  pickupPersonName: string
  pickupRelationship?: string
  pickupPhone?: string
  remark?: string
}

export interface AttendanceStatusForm {
  enrollmentId: string
  attendanceDate?: string
  remark?: string
}

export interface LeaveRequestForm {
  enrollmentId: string
  startDate: string
  endDate: string
  type: 'SICK' | 'PERSONAL' | 'OTHER'
  reason?: string
}

export interface LeaveReviewForm {
  reviewRemark?: string
}

export interface CareRecordForm {
  enrollmentId: string
  recordDate?: string
  recordTime?: string
  type: 'FEEDING' | 'WATER' | 'SLEEP' | 'TOILET' | 'TEMPERATURE' | 'MOOD' | 'ACTIVITY'
  valueText?: string
  amount?: number
  unit?: string
  startedAt?: string
  endedAt?: string
  remark?: string
  source?: string
}

export interface CareRecordUpdateForm {
  recordDate?: string
  recordTime?: string
  type?: 'FEEDING' | 'WATER' | 'SLEEP' | 'TOILET' | 'TEMPERATURE' | 'MOOD' | 'ACTIVITY'
  valueText?: string
  amount?: number
  unit?: string
  startedAt?: string
  endedAt?: string
  remark?: string
  source?: string
}

export interface DailyReportGenerateForm {
  enrollmentId: string
  reportDate?: string
  teacherComment?: string
}

export interface DailyReportUpdateForm {
  summary?: string
  attendanceSummary?: string
  careSummary?: string
  healthSummary?: string
  activitySummary?: string
  teacherComment?: string
  aiDraftContent?: string
}

export interface HealthObservationForm {
  enrollmentId: string
  observationDate?: string
  observationTime?: string
  type: 'MORNING_CHECK' | 'NOON_CHECK' | 'FULL_DAY_OBSERVATION'
  temperature?: number
  touchStatus?: string
  lookStatus?: string
  askStatus?: string
  checkStatus?: string
  symptoms?: string
  actionTaken?: string
  abnormal?: boolean
  followUpRequired?: boolean
  source?: string
}

export interface HealthObservationUpdateForm {
  observationDate?: string
  observationTime?: string
  type?: 'MORNING_CHECK' | 'NOON_CHECK' | 'FULL_DAY_OBSERVATION'
  temperature?: number
  touchStatus?: string
  lookStatus?: string
  askStatus?: string
  checkStatus?: string
  symptoms?: string
  actionTaken?: string
  abnormal?: boolean
  followUpRequired?: boolean
  source?: string
}

export interface PickupPersonForm {
  enrollmentId: string
  name: string
  relationship?: string
  phone?: string
  identityNo?: string
  photoUrl?: string
  remark?: string
}

export interface PickupPersonUpdateForm {
  name?: string
  relationship?: string
  phone?: string
  identityNo?: string
  photoUrl?: string
  status?: 'ACTIVE' | 'DISABLED'
  remark?: string
}

export interface PickupDelegationForm {
  enrollmentId: string
  pickupDate: string
  pickupPersonName: string
  pickupRelationship?: string
  pickupPhone?: string
  reason?: string
}

export interface PickupDelegationReviewForm {
  reviewRemark?: string
}

export interface AllergyTagForm {
  enrollmentId: string
  allergen: string
  reaction?: string
  severity?: 'MILD' | 'MODERATE' | 'SEVERE'
  status?: 'ACTIVE' | 'INACTIVE'
  remark?: string
}

export interface MedicationRequestForm {
  enrollmentId: string
  medicineName: string
  dosage?: string
  frequency?: string
  startDate: string
  endDate: string
  instructions?: string
}

export interface MedicationReviewForm {
  reviewRemark?: string
}

export interface MedicationAdministrationForm {
  medicationRequestId: string
  administeredAt?: string
  actualDosage?: string
  reactionObserved?: boolean
  remark?: string
}

export interface IncidentReportForm {
  enrollmentId: string
  type: 'HEALTH_ABNORMAL' | 'INJURY' | 'SAFETY_EVENT' | 'BEHAVIOR' | 'OTHER'
  severity?: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL'
  occurredAt?: string
  location?: string
  title: string
  description?: string
  handlingProcess?: string
  followUpPlan?: string
}

export interface IncidentReportUpdateForm {
  type?: 'HEALTH_ABNORMAL' | 'INJURY' | 'SAFETY_EVENT' | 'BEHAVIOR' | 'OTHER'
  severity?: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL'
  status?: 'OPEN' | 'PROCESSING' | 'CLOSED'
  occurredAt?: string
  location?: string
  title?: string
  description?: string
  handlingProcess?: string
  followUpPlan?: string
  parentNotified?: boolean
}

export interface AnnouncementForm {
  organizationId: string
  classroomId?: string
  scope?: 'ORGANIZATION' | 'CLASSROOM'
  title: string
  content: string
  requireReceipt?: boolean
}

export interface AnnouncementUpdateForm {
  title?: string
  content?: string
  requireReceipt?: boolean
}

export interface FeeItemForm {
  organizationId: string
  name: string
  description?: string
  amount: number
  status?: 'ACTIVE' | 'DISABLED'
}

export interface BillingStatementForm {
  enrollmentId: string
  feeItemId?: string
  title?: string
  amount?: number
  dueDate?: string
  remark?: string
}

export interface BillingPaymentForm {
  paymentMethod?: string
  remark?: string
}

export interface AdmissionLeadForm {
  organizationId: string
  intendedClassroomId?: string
  childName: string
  childGender?: string
  childBirthday?: string
  guardianName: string
  guardianPhone: string
  source?: 'ONLINE' | 'REFERRAL' | 'OPEN_DAY' | 'WALK_IN' | 'OTHER'
  intentionLevel?: 'HIGH' | 'MEDIUM' | 'LOW' | 'LOST'
  status?:
    | 'NEW'
    | 'FOLLOWING'
    | 'APPLIED'
    | 'APPROVED'
    | 'REJECTED'
    | 'TRIALING'
    | 'TRIAL_COMPLETED'
    | 'ENROLLED'
    | 'LOST'
  preferredStartDate?: string
  remark?: string
}

export interface AdmissionReviewForm {
  result: 'APPROVED' | 'REJECTED'
  reviewRemark?: string
}

export interface AdmissionTrialForm {
  trialStartDate: string
  trialEndDate: string
  trialFeedback?: string
}

export interface MealPlanForm {
  organizationId: string
  mealDate: string
  mealType: 'BREAKFAST' | 'MORNING_SNACK' | 'LUNCH' | 'AFTERNOON_SNACK' | 'DINNER'
  title: string
  foodItems?: string
  allergenNotes?: string
  nutritionNotes?: string
  status?: 'DRAFT' | 'PUBLISHED'
}

export interface MealIntakeForm {
  mealPlanId: string
  enrollmentId: string
  intakeLevel?: 'ALL' | 'MOST' | 'HALF' | 'LESS' | 'NONE'
  allergyReaction?: boolean
  reactionNotes?: string
  remark?: string
}

export interface SafetyLedgerForm {
  organizationId: string
  relatedIncidentId?: string
  ledgerDate: string
  ledgerType:
    | 'DISINFECTION'
    | 'FOOD_SAMPLE'
    | 'FACILITY_INSPECTION'
    | 'FIRE_SAFETY'
    | 'SAFETY_EDUCATION'
    | 'INCIDENT_FOLLOWUP'
    | 'OTHER'
  title: string
  content?: string
  location?: string
  responsiblePerson?: string
  dueAt?: string
  status?: 'OPEN' | 'PROCESSING' | 'CLOSED' | 'OVERDUE'
  handleRemark?: string
}

export interface SafetyLedgerHandleForm {
  handleRemark?: string
}

export interface ChildDevelopmentAssessmentForm {
  enrollmentId: string
  assessmentDate: string
  childAgeMonths: number
  assessmentMode: 'TODDLER_MILESTONE' | 'PRESCHOOL_DOMAIN'
  title: string
  grossMotorScore?: number
  fineMotorScore?: number
  languageScore?: number
  cognitiveScore?: number
  socialEmotionalScore?: number
  healthScore?: number
  scienceScore?: number
  artScore?: number
  maxScore?: number
  overallLevel?: 'ADVANCED' | 'AGE_APPROPRIATE' | 'NEEDS_SUPPORT' | 'DELAY_RISK'
  summary?: string
  recommendation?: string
  radarData?: string
}

// 路由类型
export interface RouteConfig {
  path: string
  component: React.ComponentType
  exact?: boolean
  protected?: boolean
  title?: string
  icon?: React.ReactNode
}
