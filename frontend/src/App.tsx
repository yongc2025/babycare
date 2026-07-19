import { lazy, Suspense } from 'react'
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom'
import { Layout, Spin } from 'antd'
import MainLayout from './components/Layout/MainLayout'
import { useAuthStore } from './stores/authStore'

const Login = lazy(() => import('./pages/Auth/Login'))
const Register = lazy(() => import('./pages/Auth/Register'))
const Dashboard = lazy(() => import('./pages/Dashboard/Dashboard'))
const GrowthRecord = lazy(() => import('./pages/GrowthRecord/GrowthRecord'))
const AIParenting = lazy(() => import('./pages/AIParenting/AIParenting'))
const EducationPlanning = lazy(() => import('./pages/EducationPlanning/EducationPlanning'))
const FamilyCollaboration = lazy(() => import('./pages/FamilyCollaboration/FamilyCollaboration'))
const FamilyManagement = lazy(() => import('./pages/FamilyManagement/FamilyManagement'))
const ParentReports = lazy(() => import('./pages/ParentReports/ParentReports'))
const ElderMode = lazy(() => import('./pages/ElderMode/ElderMode'))
const OrganizationManagement = lazy(() => import('./pages/OrganizationManagement/OrganizationManagement'))
const TeacherWorkbench = lazy(() => import('./pages/TeacherWorkbench/TeacherWorkbench'))
const DailyReportManagement = lazy(() => import('./pages/DailyReportManagement/DailyReportManagement'))
const HealthSafety = lazy(() => import('./pages/HealthSafety/HealthSafety'))
const OperationsRegulatory = lazy(() => import('./pages/OperationsRegulatory/OperationsRegulatory'))
const SystemManagement = lazy(() => import('./pages/SystemManagement/SystemManagement'))
const BossDashboard = lazy(() => import('./pages/BossDashboard/BossDashboard'))
const PrincipalWorkbench = lazy(() => import('./pages/PrincipalWorkbench/PrincipalWorkbench'))
const Profile = lazy(() => import('./pages/Profile/Profile'))
const ParentApplications = lazy(() => import('./pages/ParentApplications/ParentApplications'))

const PageLoading = () => (
  <div style={{ display: 'grid', minHeight: '60vh', placeItems: 'center' }}>
    <Spin tip="页面加载中" />
  </div>
)

function App() {
  const { user } = useAuthStore()

  const ProtectedRoute = ({ children }: { children: React.ReactNode }) => {
    return user ? <MainLayout>{children}</MainLayout> : <Navigate to="/login" />
  }

  return (
    <Router>
      <Layout style={{ minHeight: '100vh' }}>
        <Suspense fallback={<PageLoading />}>
          <Routes>
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
            <Route path="/" element={<ProtectedRoute><Dashboard /></ProtectedRoute>} />
            <Route path="/dashboard" element={<ProtectedRoute><Dashboard /></ProtectedRoute>} />
            <Route path="/growth-record" element={<ProtectedRoute><GrowthRecord /></ProtectedRoute>} />
            <Route path="/ai-parenting" element={<ProtectedRoute><AIParenting /></ProtectedRoute>} />
            <Route path="/education-planning" element={<ProtectedRoute><EducationPlanning /></ProtectedRoute>} />
            <Route path="/family-collaboration" element={<ProtectedRoute><FamilyCollaboration /></ProtectedRoute>} />
            <Route path="/parent-reports" element={<ProtectedRoute><ParentReports /></ProtectedRoute>} />
            <Route path="/elder-mode" element={<ProtectedRoute><ElderMode /></ProtectedRoute>} />
            <Route path="/family-management" element={<ProtectedRoute><FamilyManagement /></ProtectedRoute>} />
            <Route path="/organization-management" element={<ProtectedRoute><OrganizationManagement /></ProtectedRoute>} />
            <Route path="/teacher-workbench" element={<ProtectedRoute><TeacherWorkbench /></ProtectedRoute>} />
            <Route path="/daily-report-management" element={<ProtectedRoute><DailyReportManagement /></ProtectedRoute>} />
            <Route path="/health-safety" element={<ProtectedRoute><HealthSafety /></ProtectedRoute>} />
            <Route path="/operations-regulatory" element={<ProtectedRoute><OperationsRegulatory /></ProtectedRoute>} />
            <Route path="/system-management" element={<ProtectedRoute><SystemManagement /></ProtectedRoute>} />
            <Route path="/boss-dashboard" element={<ProtectedRoute><BossDashboard /></ProtectedRoute>} />
            <Route path="/principal-workbench" element={<ProtectedRoute><PrincipalWorkbench /></ProtectedRoute>} />
            <Route path="/profile" element={<ProtectedRoute><Profile /></ProtectedRoute>} />
            <Route path="/parent-applications" element={<ProtectedRoute><ParentApplications /></ProtectedRoute>} />
            <Route path="*" element={<Navigate to="/" />} />
          </Routes>
        </Suspense>
      </Layout>
    </Router>
  )
}

export default App
