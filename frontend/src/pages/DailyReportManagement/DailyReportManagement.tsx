import React, { useEffect, useMemo, useState } from 'react'
import { Alert, Button, Card, DatePicker, Empty, Form, Input, List, Modal, Select, Skeleton, Space, Tag, Typography, message } from 'antd'
import { CheckCircleOutlined, CloseCircleOutlined, FileTextOutlined, RobotOutlined, SendOutlined, SyncOutlined } from '@ant-design/icons'
import dayjs, { Dayjs } from 'dayjs'
import {
  classroomAPI,
  dailyReportAPI,
  enrollmentAPI,
  organizationAPI,
} from '../../services/api'
import type { Classroom, DailyReport, Enrollment, Organization } from '../../types'
import './DailyReportManagement.css'

const { Title, Paragraph, Text } = Typography

interface ApiResponse<T> {
  success: boolean
  data?: T
  message?: string
}

const unwrap = <T,>(response: ApiResponse<T> | T): T => {
  if (response && typeof response === 'object' && 'success' in response) {
    const apiResponse = response as ApiResponse<T>
    if (!apiResponse.success || apiResponse.data === undefined) {
      throw new Error(apiResponse.message || '请求失败')
    }
    return apiResponse.data
  }
  return response as T
}

const DailyReportManagement: React.FC = () => {
  const [organizations, setOrganizations] = useState<Organization[]>([])
  const [organizationId, setOrganizationId] = useState<string>()
  const [orgDetail, setOrgDetail] = useState<Organization | null>(null)
  const [classrooms, setClassrooms] = useState<Classroom[]>([])
  const [classroomId, setClassroomId] = useState<string>()
  const [enrollments, setEnrollments] = useState<Enrollment[]>([])
  const [selectedEnrollmentId, setSelectedEnrollmentId] = useState<string>()
  const [reports, setReports] = useState<DailyReport[]>([])
  const [activeReport, setActiveReport] = useState<DailyReport | null>(null)
  const [selectedDate, setSelectedDate] = useState<Dayjs>(dayjs())
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [rejectModalOpen, setRejectModalOpen] = useState(false)
  const [rejectReason, setRejectReason] = useState('')
  const [form] = Form.useForm()

  const reportDate = selectedDate.format('YYYY-MM-DD')
  const selectedEnrollment = useMemo(
    () => enrollments.find((item) => item.id === selectedEnrollmentId),
    [enrollments, selectedEnrollmentId],
  )
  const approvalRequired = orgDetail?.dailyReportApprovalRequired

  const loadOrganizations = async () => {
    setLoading(true)
    try {
      const data = unwrap<Organization[]>(await organizationAPI.getMyOrganizations())
      setOrganizations(data)
      setOrganizationId((current) => current || data[0]?.id)
    } catch (err) {
      setError(err instanceof Error ? err.message : '机构信息加载失败')
    } finally {
      setLoading(false)
    }
  }

  const loadOrgDetail = async (id: string) => {
    try {
      const data = unwrap<Organization>(await organizationAPI.getOrganizationDetail(id))
      setOrgDetail(data)
    } catch {
      setOrgDetail(null)
    }
  }

  const loadClassrooms = async (id: string) => {
    setLoading(true)
    try {
      const data = unwrap<Classroom[]>(await classroomAPI.getOrganizationClassrooms(id))
      setClassrooms(data)
      setClassroomId((current) => current || data[0]?.id)
    } catch (err) {
      setClassrooms([])
      setError(err instanceof Error ? err.message : '班级信息加载失败')
    } finally {
      setLoading(false)
    }
  }

  const loadClassroomReports = async (id: string) => {
    setLoading(true)
    setError(null)

    const [enrollmentResult, reportResult] = await Promise.allSettled([
      enrollmentAPI.getClassroomEnrollments(id),
      dailyReportAPI.getClassroomReports(id, reportDate),
    ])

    const enrollmentData = enrollmentResult.status === 'fulfilled'
      ? unwrap<Enrollment[]>(enrollmentResult.value)
      : []
    const reportData = reportResult.status === 'fulfilled'
      ? unwrap<DailyReport[]>(reportResult.value)
      : []

    setEnrollments(enrollmentData)
    setReports(reportData)
    setSelectedEnrollmentId((current) => current || enrollmentData[0]?.id)
    setActiveReport(reportData.find((item) => item.enrollmentId === selectedEnrollmentId) || reportData[0] || null)

    const failed = [enrollmentResult, reportResult].some((item) => item.status === 'rejected')
    setError(failed ? '部分日报数据暂时无法加载，请稍后重试' : null)
    setLoading(false)
  }

  useEffect(() => {
    loadOrganizations()
  }, [])

  useEffect(() => {
    if (organizationId) {
      loadOrgDetail(organizationId)
      loadClassrooms(organizationId)
    }
  }, [organizationId])

  useEffect(() => {
    if (classroomId) {
      loadClassroomReports(classroomId)
    }
  }, [classroomId, reportDate])

  useEffect(() => {
    const report = reports.find((item) => item.enrollmentId === selectedEnrollmentId) || null
    setActiveReport(report)
  }, [reports, selectedEnrollmentId])

  useEffect(() => {
    form.setFieldsValue({
      summary: activeReport?.summary,
      attendanceSummary: activeReport?.attendanceSummary,
      careSummary: activeReport?.careSummary,
      healthSummary: activeReport?.healthSummary,
      activitySummary: activeReport?.activitySummary,
      teacherComment: activeReport?.teacherComment,
      aiDraftContent: activeReport?.aiDraftContent,
    })
  }, [activeReport, form])

  const reload = () => classroomId && loadClassroomReports(classroomId)

  const updateReportState = (report: DailyReport) => {
    setActiveReport(report)
    setReports((items) => [report, ...items.filter((item) => item.id !== report.id)])
  }

  const handleGenerate = async () => {
    if (!selectedEnrollmentId) return
    const report = unwrap<DailyReport>(await dailyReportAPI.generateReport({ enrollmentId: selectedEnrollmentId, reportDate }))
    updateReportState(report)
    message.success('日报草稿已生成')
  }

  const handleGenerateAiDraft = async () => {
    if (!selectedEnrollmentId) return
    const report = unwrap<DailyReport>(await dailyReportAPI.generateAiDraft({ enrollmentId: selectedEnrollmentId, reportDate }))
    updateReportState(report)
    message.success('AI 辅助草稿已生成')
  }

  const handleSave = async () => {
    if (!activeReport) return
    const values = await form.validateFields()
    const report = unwrap<DailyReport>(await dailyReportAPI.updateReport(activeReport.id, values))
    updateReportState(report)
    message.success('日报已保存')
  }

  const handlePublish = async () => {
    if (!activeReport) return
    const report = unwrap<DailyReport>(await dailyReportAPI.publishReport(activeReport.id))
    updateReportState(report)
    if (approvalRequired && report.status === 'PENDING_APPROVAL') {
      message.success('日报已提交审核，等待园长审批')
    } else {
      message.success('日报已发布')
    }
  }

  const handleSubmitForReview = async () => {
    if (!activeReport) return
    const report = unwrap<DailyReport>(await dailyReportAPI.submitForReview(activeReport.id))
    updateReportState(report)
    message.success('日报已提交审核')
  }

  const handleApprove = async () => {
    if (!activeReport) return
    const report = unwrap<DailyReport>(await dailyReportAPI.approveReport(activeReport.id))
    updateReportState(report)
    message.success('日报审核通过，已发布')
  }

  const handleReject = async () => {
    if (!activeReport || !rejectReason.trim()) return
    const report = unwrap<DailyReport>(await dailyReportAPI.rejectReport(activeReport.id, rejectReason))
    updateReportState(report)
    setRejectModalOpen(false)
    setRejectReason('')
    message.success('日报已驳回')
  }

  const getStatusTag = (status: string, statusDescription?: string) => {
    const label = statusDescription || status
    switch (status) {
      case 'PUBLISHED':
        return <Tag color="green">{label}</Tag>
      case 'PENDING_APPROVAL':
        return <Tag color="orange">{label}</Tag>
      case 'DRAFT':
        return <Tag color="blue">{label}</Tag>
      default:
        return <Tag color="default">{label}</Tag>
    }
  }

  const canEdit = activeReport && activeReport.status === 'DRAFT'
  const canPublish = activeReport && activeReport.status === 'DRAFT'
  const canApprove = activeReport && activeReport.status === 'PENDING_APPROVAL'
  const isPublished = activeReport?.status === 'PUBLISHED'

  return (
    <div className="daily-report-page">
      <Space direction="vertical" size={4} className="page-title">
        <Title level={2}>日报闭环</Title>
        <Paragraph type="secondary">
          基于真实考勤、照护和健康观察生成日报草稿，支持 AI 辅助、编辑和发布。
          {approvalRequired && <Tag color="orange" style={{ marginLeft: 8 }}>审核发布模式</Tag>}
        </Paragraph>
      </Space>

      {error && <Alert type="warning" showIcon message={error} />}

      <Card>
        <Space wrap>
          <Select className="report-select" value={organizationId} placeholder="选择机构"
            options={organizations.map((item) => ({ label: item.name, value: item.id }))} onChange={setOrganizationId} />
          <Select className="report-select" value={classroomId} placeholder="选择班级"
            options={classrooms.map((item) => ({ label: item.name, value: item.id }))} onChange={setClassroomId} />
          <DatePicker value={selectedDate} onChange={(value) => value && setSelectedDate(value)} />
          <Button icon={<SyncOutlined />} onClick={reload}>刷新</Button>
        </Space>
      </Card>

      {loading ? (
        <Card><Skeleton active paragraph={{ rows: 8 }} /></Card>
      ) : !classroomId ? (
        <Card><Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="请先选择班级" /></Card>
      ) : (
        <div className="report-grid">
          <Card title="班级幼儿">
            <List
              dataSource={enrollments}
              locale={{ emptyText: <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="当前班级暂无入托幼儿" /> }}
              renderItem={(item) => {
                const report = reports.find((entry) => entry.enrollmentId === item.id)
                return (
                  <List.Item onClick={() => setSelectedEnrollmentId(item.id)} className={selectedEnrollmentId === item.id ? 'active-report-row' : ''}>
                    <List.Item.Meta title={item.babyName} description={item.classroomName || '当前班级'} />
                    {report ? getStatusTag(report.status, report.statusDescription) : <Tag color="orange">未生成</Tag>}
                  </List.Item>
                )
              }}
            />
          </Card>

          <Card
            title={<><FileTextOutlined /> {selectedEnrollment?.babyName || '日报编辑'}</>}
            extra={
              <Space wrap>
                <Button icon={<FileTextOutlined />} disabled={!selectedEnrollmentId} onClick={handleGenerate}>生成草稿</Button>
                <Button icon={<RobotOutlined />} disabled={!selectedEnrollmentId} onClick={handleGenerateAiDraft}>AI 草稿</Button>
                <Button disabled={!canEdit} onClick={handleSave}>保存</Button>
                {approvalRequired ? (
                  <>
                    <Button type="primary" icon={<SendOutlined />} disabled={!canPublish} onClick={handleSubmitForReview}>
                      提交审核
                    </Button>
                    {canApprove && (
                      <>
                        <Button type="primary" icon={<CheckCircleOutlined />} style={{ background: '#52c41a', borderColor: '#52c41a' }} onClick={handleApprove}>
                          审核通过
                        </Button>
                        <Button danger icon={<CloseCircleOutlined />} onClick={() => setRejectModalOpen(true)}>
                          驳回
                        </Button>
                      </>
                    )}
                  </>
                ) : (
                  <Button type="primary" icon={<SendOutlined />} disabled={!canPublish} onClick={handlePublish}>
                    发布
                  </Button>
                )}
              </Space>
            }
          >
            {!selectedEnrollmentId ? (
              <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="请选择幼儿" />
            ) : !activeReport ? (
              <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="当前日期暂无日报，请先生成草稿" />
            ) : (
              <Form form={form} layout="vertical">
                <Alert
                  className="report-status"
                  type={isPublished ? 'success' : activeReport.status === 'PENDING_APPROVAL' ? 'warning' : 'info'}
                  showIcon
                  message={activeReport.statusDescription || activeReport.status}
                  description={
                    isPublished
                      ? `${activeReport.publishedAt ? `发布时间：${dayjs(activeReport.publishedAt).format('YYYY-MM-DD HH:mm')}` : ''}`
                      : activeReport.status === 'PENDING_APPROVAL'
                        ? '日报已提交审核，等待园长审批'
                        : '草稿可继续编辑'
                  }
                />
                {activeReport.reviewComment && (
                  <Alert
                    type="error"
                    showIcon
                    message="驳回原因"
                    description={activeReport.reviewComment}
                    style={{ marginBottom: 16 }}
                  />
                )}
                <Form.Item name="summary" label="日报摘要"><Input.TextArea rows={2} disabled={isPublished} /></Form.Item>
                <Form.Item name="attendanceSummary" label="考勤摘要"><Input.TextArea rows={2} disabled={isPublished} /></Form.Item>
                <Form.Item name="careSummary" label="照护摘要"><Input.TextArea rows={3} disabled={isPublished} /></Form.Item>
                <Form.Item name="healthSummary" label="健康摘要"><Input.TextArea rows={2} disabled={isPublished} /></Form.Item>
                <Form.Item name="activitySummary" label="活动摘要"><Input.TextArea rows={2} disabled={isPublished} /></Form.Item>
                <Form.Item name="teacherComment" label="老师评语"><Input.TextArea rows={3} disabled={isPublished} /></Form.Item>
                <Form.Item name="aiDraftContent" label="AI 辅助草稿"><Input.TextArea rows={5} disabled={isPublished} /></Form.Item>
                <Text type="secondary">
                  {approvalRequired ? 'AI 草稿不会自动发布，老师确认后提交审核，园长审批后正式发布。' : 'AI 草稿不会自动发布，老师确认后再保存和发布。'}
                </Text>
              </Form>
            )}
          </Card>
        </div>
      )}

      <Modal
        title="驳回日报"
        open={rejectModalOpen}
        onCancel={() => { setRejectModalOpen(false); setRejectReason('') }}
        onOk={handleReject}
        okText="确认驳回"
        okButtonProps={{ danger: true, disabled: !rejectReason.trim() }}
      >
        <div style={{ marginBottom: 8 }}>请输入驳回原因：</div>
        <Input.TextArea
          rows={3}
          value={rejectReason}
          onChange={(e) => setRejectReason(e.target.value)}
          placeholder="请填写驳回原因，老师将根据意见修改后重新提交"
        />
      </Modal>
    </div>
  )
}

export default DailyReportManagement
