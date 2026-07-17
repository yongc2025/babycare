import React, { useEffect, useMemo, useState } from 'react'
import { Alert, Card, DatePicker, Empty, List, Skeleton, Space, Tag, Timeline, Typography } from 'antd'
import {
  BellOutlined,
  CalendarOutlined,
  CheckCircleOutlined,
  ClockCircleOutlined,
  FileTextOutlined,
  HeartOutlined,
  MedicineBoxOutlined,
  SafetyCertificateOutlined,
} from '@ant-design/icons'
import dayjs, { Dayjs } from 'dayjs'
import {
  announcementAPI,
  attendanceAPI,
  careRecordAPI,
  dailyReportAPI,
  enrollmentAPI,
  medicationCareAPI,
  pickupAPI,
} from '../../services/api'
import { useFamilyStore } from '../../stores/familyStore'
import type {
  Announcement,
  AttendanceRecord,
  CareRecord,
  DailyReport,
  Enrollment,
  MedicationRequest,
  PickupDelegation,
  PickupPerson,
} from '../../types'

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

const getCareLabel = (record: CareRecord) => {
  const value = record.amount !== undefined ? `${record.amount}${record.unit || ''}` : record.valueText
  return [record.typeDescription || record.type, value, record.remark].filter(Boolean).join(' · ')
}

const ParentReports: React.FC = () => {
  const { currentBaby, loadFamilies } = useFamilyStore()
  const [selectedDate, setSelectedDate] = useState<Dayjs>(dayjs())
  const [enrollment, setEnrollment] = useState<Enrollment | null>(null)
  const [dailyReport, setDailyReport] = useState<DailyReport | null>(null)
  const [attendanceRecords, setAttendanceRecords] = useState<AttendanceRecord[]>([])
  const [careRecords, setCareRecords] = useState<CareRecord[]>([])
  const [announcements, setAnnouncements] = useState<Announcement[]>([])
  const [pickupPersons, setPickupPersons] = useState<PickupPerson[]>([])
  const [pickupDelegations, setPickupDelegations] = useState<PickupDelegation[]>([])
  const [medications, setMedications] = useState<MedicationRequest[]>([])
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const dateText = useMemo(() => selectedDate.format('YYYY-MM-DD'), [selectedDate])

  useEffect(() => {
    loadFamilies().catch(() => {
      setError('家庭与宝宝信息加载失败')
    })
  }, [loadFamilies])

  useEffect(() => {
    if (!currentBaby) return

    const loadParentData = async () => {
      setIsLoading(true)
      setError(null)

      const startDate = selectedDate.subtract(6, 'day').format('YYYY-MM-DD')
      const enrollmentResult = await Promise.resolve(enrollmentAPI.getBabyEnrollments(currentBaby.id))
        .then((response) => unwrap<Enrollment[]>(response))
        .catch(() => [])
      const activeEnrollment = enrollmentResult.find((item) => item.status === 'ACTIVE') || enrollmentResult[0] || null
      setEnrollment(activeEnrollment)

      const results = await Promise.allSettled([
        dailyReportAPI.getBabyReport(currentBaby.id, dateText),
        attendanceAPI.getBabyAttendance(currentBaby.id, startDate, dateText),
        careRecordAPI.getBabyRecords(currentBaby.id, dateText),
        pickupAPI.getBabyPickupPersons(currentBaby.id),
        pickupAPI.getBabyDelegations(currentBaby.id),
        medicationCareAPI.getBabyMedications(currentBaby.id),
        activeEnrollment ? announcementAPI.getClassroomAnnouncements(activeEnrollment.classroomId) : Promise.resolve([]),
      ])

      setDailyReport(results[0].status === 'fulfilled' ? unwrap<DailyReport>(results[0].value) : null)
      setAttendanceRecords(results[1].status === 'fulfilled' ? unwrap<AttendanceRecord[]>(results[1].value) : [])
      setCareRecords(results[2].status === 'fulfilled' ? unwrap<CareRecord[]>(results[2].value) : [])
      setPickupPersons(results[3].status === 'fulfilled' ? unwrap<PickupPerson[]>(results[3].value) : [])
      setPickupDelegations(results[4].status === 'fulfilled' ? unwrap<PickupDelegation[]>(results[4].value) : [])
      setMedications(results[5].status === 'fulfilled' ? unwrap<MedicationRequest[]>(results[5].value) : [])
      setAnnouncements(results[6].status === 'fulfilled' ? unwrap<Announcement[]>(results[6].value) : [])

      const hasHardFailure = results.slice(1).some((result) => result.status === 'rejected')
      setError(hasHardFailure ? '部分数据暂时无法加载，请稍后重试' : null)
      setIsLoading(false)
    }

    loadParentData()
  }, [currentBaby, dateText, selectedDate])

  if (!currentBaby) {
    return (
      <Card>
        <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="请先在家庭管理中添加或选择宝宝" />
      </Card>
    )
  }

  return (
    <div>
      <Space direction="vertical" size={4} style={{ marginBottom: 20 }}>
        <Title level={2} style={{ margin: 0 }}>家长日报</Title>
        <Paragraph type="secondary" style={{ margin: 0 }}>
          聚合宝宝已发布日报、考勤照护、通知、接送和用药信息。
        </Paragraph>
      </Space>

      <Card style={{ marginBottom: 16 }}>
        <Space wrap size={12}>
          <Tag color="blue" icon={<HeartOutlined />}>{currentBaby.name}</Tag>
          {enrollment && <Tag color="cyan">{enrollment.classroomName || '已入托'}</Tag>}
          <DatePicker value={selectedDate} onChange={(value) => value && setSelectedDate(value)} />
        </Space>
      </Card>

      {error && <Alert type="warning" showIcon message={error} style={{ marginBottom: 16 }} />}

      <div style={{ display: 'grid', gridTemplateColumns: 'minmax(0, 1.2fr) minmax(280px, 0.8fr)', gap: 16 }}>
        <Space direction="vertical" size={16}>
          <Card title={<><FileTextOutlined /> 结构化日报</>}>
            {isLoading ? <Skeleton active paragraph={{ rows: 5 }} /> : dailyReport ? (
              <Space direction="vertical" size={14} style={{ width: '100%' }}>
                <Text strong>{dailyReport.summary || `${dailyReport.reportDate} 日报`}</Text>
                <Paragraph>{dailyReport.attendanceSummary || '暂无考勤摘要'}</Paragraph>
                <Paragraph>{dailyReport.careSummary || '暂无照护摘要'}</Paragraph>
                <Paragraph>{dailyReport.healthSummary || '暂无健康摘要'}</Paragraph>
                <Paragraph>{dailyReport.activitySummary || '暂无活动摘要'}</Paragraph>
                {dailyReport.teacherComment && <Alert type="info" showIcon message="老师评语" description={dailyReport.teacherComment} />}
                <Tag color="green" icon={<CheckCircleOutlined />}>{dailyReport.statusDescription || '已发布'}</Tag>
              </Space>
            ) : <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="当天暂无已发布日报" />}
          </Card>

          <Card title={<><ClockCircleOutlined /> 当天照护记录</>}>
            {isLoading ? <Skeleton active paragraph={{ rows: 4 }} /> : (
              <List
                dataSource={careRecords}
                locale={{ emptyText: <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="暂无照护记录" /> }}
                renderItem={(record) => (
                  <List.Item>
                    <List.Item.Meta title={getCareLabel(record)} description={dayjs(record.recordTime).format('HH:mm')} />
                  </List.Item>
                )}
              />
            )}
          </Card>

          <Card title={<><BellOutlined /> 班级通知</>}>
            <List
              loading={isLoading}
              dataSource={announcements}
              locale={{ emptyText: <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="暂无班级通知" /> }}
              renderItem={(item) => (
                <List.Item>
                  <List.Item.Meta title={item.title} description={item.publishedAt ? dayjs(item.publishedAt).format('YYYY-MM-DD HH:mm') : '未发布'} />
                  <Tag color={item.readByCurrentUser ? 'green' : 'orange'}>{item.readByCurrentUser ? '已读' : '未读'}</Tag>
                </List.Item>
              )}
            />
          </Card>
        </Space>

        <Space direction="vertical" size={16}>
          <Card title={<><CalendarOutlined /> 近 7 天考勤</>}>
            {isLoading ? <Skeleton active paragraph={{ rows: 6 }} /> : attendanceRecords.length > 0 ? (
              <Timeline
                items={attendanceRecords.map((record) => ({
                  color: record.status === 'CHECKED_OUT' || record.status === 'CHECKED_IN' ? 'green' : 'orange',
                  children: (
                    <Space direction="vertical" size={2}>
                      <Text strong>{record.attendanceDate}</Text>
                      <Text type="secondary">{record.statusDescription || record.status}</Text>
                      {record.checkInAt && <Text type="secondary">到园 {dayjs(record.checkInAt).format('HH:mm')}</Text>}
                      {record.checkOutAt && <Text type="secondary">离园 {dayjs(record.checkOutAt).format('HH:mm')}</Text>}
                    </Space>
                  ),
                }))}
              />
            ) : <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="暂无考勤记录" />}
          </Card>

          <Card title={<><SafetyCertificateOutlined /> 接送信息</>}>
            <List
              dataSource={[...pickupPersons, ...pickupDelegations]}
              locale={{ emptyText: <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="暂无接送授权或委托" /> }}
              renderItem={(item) => (
                <List.Item>
                  {'pickupDate' in item ? (
                    <List.Item.Meta title={item.pickupPersonName} description={`临时委托 · ${item.pickupDate} · ${item.pickupPhone || '无电话'}`} />
                  ) : (
                    <List.Item.Meta title={item.name} description={`${item.relationship || '接送人'} · ${item.phone || '无电话'}`} />
                  )}
                  <Tag color={item.status === 'ACTIVE' || item.status === 'APPROVED' ? 'green' : 'orange'}>{item.statusDescription || item.status}</Tag>
                </List.Item>
              )}
            />
          </Card>

          <Card title={<><MedicineBoxOutlined /> 用药委托</>}>
            <List
              dataSource={medications}
              locale={{ emptyText: <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="暂无用药委托" /> }}
              renderItem={(item) => (
                <List.Item>
                  <List.Item.Meta title={item.medicineName} description={`${item.startDate} 至 ${item.endDate} · ${item.dosage || '未填剂量'}`} />
                  <Tag color={item.status === 'APPROVED' ? 'green' : 'orange'}>{item.statusDescription || item.status}</Tag>
                </List.Item>
              )}
            />
          </Card>
        </Space>
      </div>
    </div>
  )
}

export default ParentReports
