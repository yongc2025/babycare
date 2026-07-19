import React, { useCallback, useEffect, useMemo, useState } from 'react'
import { Alert, Button, DatePicker, Empty, List, message, Skeleton, Space, Switch, Tag, Typography } from 'antd'
import {
  CalendarOutlined,
  CheckCircleOutlined,
  CheckOutlined,
  ClockCircleOutlined,
  FileTextOutlined,
  PhoneOutlined,
  SafetyCertificateOutlined,
  SoundOutlined,
  TeamOutlined,
} from '@ant-design/icons'
import dayjs, { Dayjs } from 'dayjs'
import { attendanceAPI, dailyReportAPI, pickupAPI } from '../../services/api'
import { useFamilyStore } from '../../stores/familyStore'
import type { AttendanceRecord, DailyReport, PickupDelegation, PickupPerson } from '../../types'
import './ElderMode.css'

const { Title, Text, Paragraph } = Typography

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

const unwrapOr = <T,>(response: PromiseSettledResult<ApiResponse<T> | T>, fallback: T): T => {
  if (response.status !== 'fulfilled') return fallback

  try {
    return unwrap<T>(response.value)
  } catch {
    return fallback
  }
}

const formatTime = (value?: string) => (value ? dayjs(value).format('HH:mm') : '--')

const getTodayAttendance = (records: AttendanceRecord[], dateText: string) =>
  records.find((record) => record.attendanceDate === dateText) || records[0]

const getTodayDelegations = (delegations: PickupDelegation[], dateText: string) =>
  delegations.filter((delegation) => delegation.pickupDate === dateText)

const ElderMode: React.FC = () => {
  const { currentBaby, loadFamilies } = useFamilyStore()
  const [selectedDate, setSelectedDate] = useState<Dayjs>(dayjs())
  const [largeText, setLargeText] = useState(true)
  const [highContrast, setHighContrast] = useState(true)
  const [dailyReport, setDailyReport] = useState<DailyReport | null>(null)
  const [attendanceRecords, setAttendanceRecords] = useState<AttendanceRecord[]>([])
  const [pickupPersons, setPickupPersons] = useState<PickupPerson[]>([])
  const [pickupDelegations, setPickupDelegations] = useState<PickupDelegation[]>([])
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const [confirmingDelegationId, setConfirmingDelegationId] = useState<string | null>(null)

  const dateText = useMemo(() => selectedDate.format('YYYY-MM-DD'), [selectedDate])
  const todayAttendance = useMemo(
    () => getTodayAttendance(attendanceRecords, dateText),
    [attendanceRecords, dateText],
  )
  const todayDelegations = useMemo(
    () => getTodayDelegations(pickupDelegations, dateText),
    [pickupDelegations, dateText],
  )

  const handleConfirmDelegation = useCallback(async (delegationId: string) => {
    setConfirmingDelegationId(delegationId)
    try {
      await pickupAPI.elderConfirmDelegation(delegationId)
      message.success('已确认该接送委托')
      // 刷新列表
      const updated = await pickupAPI.getBabyDelegations(currentBaby!.id)
      setPickupDelegations(Array.isArray(updated) ? updated : updated?.data || [])
    } catch (err: any) {
      message.error(err?.response?.data?.message || err.message || '确认失败')
    } finally {
      setConfirmingDelegationId(null)
    }
  }, [currentBaby])

  useEffect(() => {
    loadFamilies().catch(() => {
      setError('家庭和宝宝信息加载失败，请稍后再试')
    })
  }, [loadFamilies])

  useEffect(() => {
    if (!currentBaby) return

    const loadElderData = async () => {
      setIsLoading(true)
      setError(null)

      const startDate = selectedDate.subtract(6, 'day').format('YYYY-MM-DD')
      const results = await Promise.allSettled([
        dailyReportAPI.getBabyReport(currentBaby.id, dateText),
        attendanceAPI.getBabyAttendance(currentBaby.id, startDate, dateText),
        pickupAPI.getBabyPickupPersons(currentBaby.id),
        pickupAPI.getBabyDelegations(currentBaby.id),
      ])

      setDailyReport(unwrapOr<DailyReport | null>(results[0], null))
      setAttendanceRecords(unwrapOr<AttendanceRecord[]>(results[1], []))
      setPickupPersons(unwrapOr<PickupPerson[]>(results[2], []))
      setPickupDelegations(unwrapOr<PickupDelegation[]>(results[3], []))

      const failedCount = results.filter((result) => result.status === 'rejected').length
      setError(failedCount > 1 ? '部分信息暂时无法加载，请稍后刷新' : null)
      setIsLoading(false)
    }

    loadElderData()
  }, [currentBaby, dateText, selectedDate])

  const pageClassName = [
    'elder-mode-page',
    largeText ? 'elder-mode-large' : '',
    highContrast ? 'elder-mode-contrast' : '',
  ]
    .filter(Boolean)
    .join(' ')

  if (!currentBaby) {
    return (
      <div className={pageClassName}>
        <div className="elder-empty-panel">
          <Empty
            image={Empty.PRESENTED_IMAGE_SIMPLE}
            description="请先在家庭管理中添加或选择宝宝"
          />
        </div>
      </div>
    )
  }

  return (
    <div className={pageClassName}>
      <section className="elder-hero">
        <div>
          <Text className="elder-kicker">长辈模式</Text>
          <Title level={1}>{currentBaby.name} 今天在园情况</Title>
          <Paragraph>
            把日报、考勤、接送人放在一个清楚的大字页面里，方便祖辈快速查看。
          </Paragraph>
        </div>

        <div className="elder-controls">
          <DatePicker value={selectedDate} onChange={(value) => value && setSelectedDate(value)} />
          <Space>
            <Text>大字</Text>
            <Switch checked={largeText} onChange={setLargeText} />
          </Space>
          <Space>
            <Text>高对比</Text>
            <Switch checked={highContrast} onChange={setHighContrast} />
          </Space>
        </div>
      </section>

      {error && <Alert className="elder-alert" type="warning" showIcon message={error} />}

      <section className="elder-status-grid">
        <div className="elder-status-card">
          <CalendarOutlined />
          <span>考勤状态</span>
          <strong>{todayAttendance?.statusDescription || todayAttendance?.status || '暂无记录'}</strong>
        </div>
        <div className="elder-status-card">
          <ClockCircleOutlined />
          <span>到园时间</span>
          <strong>{formatTime(todayAttendance?.checkInAt)}</strong>
        </div>
        <div className="elder-status-card">
          <SafetyCertificateOutlined />
          <span>离园时间</span>
          <strong>{formatTime(todayAttendance?.checkOutAt)}</strong>
        </div>
      </section>

      <section className="elder-main-grid">
        <div className="elder-section elder-report-section">
          <div className="elder-section-title">
            <FileTextOutlined />
            <h2>今天日报</h2>
            {dailyReport?.statusDescription && <Tag color="green">{dailyReport.statusDescription}</Tag>}
          </div>

          {isLoading ? (
            <Skeleton active paragraph={{ rows: 5 }} />
          ) : dailyReport ? (
            <div className="elder-report-body">
              <h3>{dailyReport.summary || `${dailyReport.reportDate} 日报`}</h3>
              <p>{dailyReport.attendanceSummary || '暂无考勤摘要'}</p>
              <p>{dailyReport.careSummary || '暂无照护摘要'}</p>
              <p>{dailyReport.healthSummary || '暂无健康摘要'}</p>
              <p>{dailyReport.activitySummary || '暂无活动摘要'}</p>
              {dailyReport.teacherComment && (
                <div className="elder-teacher-note">
                  <strong>老师评语</strong>
                  <span>{dailyReport.teacherComment}</span>
                </div>
              )}
            </div>
          ) : (
            <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="当天暂无已发布日报" />
          )}
        </div>

        <div className="elder-section">
          <div className="elder-section-title">
            <TeamOutlined />
            <h2>接送信息</h2>
          </div>

          {isLoading ? (
            <Skeleton active paragraph={{ rows: 4 }} />
          ) : (
            <Space direction="vertical" size={16} className="elder-full-width">
              <div>
                <h3 className="elder-subtitle">今日委托</h3>
                <List
                  dataSource={todayDelegations}
                  locale={{
                    emptyText: <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="今天暂无委托接送" />,
                  }}
                  renderItem={(item) => (
                    <List.Item className="elder-list-item">
                      <List.Item.Meta
                        title={`${item.pickupPersonName} ${item.pickupRelationship || ''}`}
                        description={
                          <Space wrap>
                            <Tag color={item.status === 'APPROVED' ? 'green' : 'orange'}>
                              {item.statusDescription || item.status}
                            </Tag>
                            {item.pickupCode && <Text>接送码：{item.pickupCode}</Text>}
                          </Space>
                        }
                      />
                      {(item.status === 'PENDING' || item.status === 'APPROVED') && (
                        <Button
                          type="primary"
                          size="small"
                          icon={confirmingDelegationId === item.id ? undefined : <CheckOutlined />}
                          loading={confirmingDelegationId === item.id}
                          onClick={() => handleConfirmDelegation(item.id)}
                        >
                          确认接送
                        </Button>
                      )}
                    </List.Item>
                  )}
                />
              </div>

              <div>
                <h3 className="elder-subtitle">常用接送人</h3>
                <List
                  dataSource={pickupPersons.slice(0, 4)}
                  locale={{
                    emptyText: <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="暂无常用接送人" />,
                  }}
                  renderItem={(person) => (
                    <List.Item className="elder-list-item">
                      <List.Item.Meta
                        title={`${person.name} ${person.relationship || ''}`}
                        description={
                          <Space wrap>
                            {person.phone && (
                              <Text>
                                <PhoneOutlined /> {person.phone}
                              </Text>
                            )}
                            <Tag>{person.statusDescription || person.status}</Tag>
                          </Space>
                        }
                      />
                    </List.Item>
                  )}
                />
              </div>
            </Space>
          )}
        </div>
      </section>

      <section className="elder-actions">
        <Button size="large" icon={<CheckCircleOutlined />} onClick={() => window.scrollTo({ top: 0, behavior: 'smooth' })}>
          回到顶部
        </Button>
        <Button size="large" icon={<SoundOutlined />} disabled>
          语音播报预留
        </Button>
      </section>
    </div>
  )
}

export default ElderMode
