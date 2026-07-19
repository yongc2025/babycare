import React, { useEffect, useMemo, useState } from 'react'
import { Alert, Button, Card, DatePicker, Empty, Input, List, Modal, Select, Skeleton, Space, Tabs, Tag, Timeline, Typography, message } from 'antd'
import {
  BellOutlined,
  CalendarOutlined,
  CheckCircleOutlined,
  ClockCircleOutlined,
  FileTextOutlined,
  HeartOutlined,
  MedicineBoxOutlined,
  SafetyCertificateOutlined,
  WalletOutlined,
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
import { parentApplicationAPI } from '../../services/organizationApi'
import type {
  Announcement,
  AttendanceRecord,
  BillingStatement,
  CareRecord,
  DailyReport,
  Enrollment,
  EnrollmentSupplementRequest,
  EnrollmentSupplementResponse,
  MedicationRequest,
  MyEnrollment,
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
  const [bills, setBills] = useState<BillingStatement[]>([])
  const [billLoading, setBillLoading] = useState(false)
  const [billFilter, setBillFilter] = useState<'UNPAID' | undefined>(undefined)
  const [myEnrollments, setMyEnrollments] = useState<MyEnrollment[]>([])
  const [bindModalVisible, setBindModalVisible] = useState(false)
  const [bindCode, setBindCode] = useState('')
  const [bindRelationship, setBindRelationship] = useState('OTHER')
  const [submittingBind, setSubmittingBind] = useState(false)

  // T076 资料补充
  const [supplementModalVisible, setSupplementModalVisible] = useState(false)
  const [supplementEnrollment, setSupplementEnrollment] = useState<MyEnrollment | null>(null)
  const [supplementData, setSupplementData] = useState<EnrollmentSupplementResponse | null>(null)
  const [supplementForm, setSupplementForm] = useState<EnrollmentSupplementRequest>({})
  const [supplementSubmitting, setSupplementSubmitting] = useState(false)
  const [supplementActiveTab, setSupplementActiveTab] = useState('baby')

  const dateText = useMemo(() => selectedDate.format('YYYY-MM-DD'), [selectedDate])

  // T076 资料补充：打开弹窗
  const openSupplementModal = (item: MyEnrollment) => {
    setSupplementEnrollment(item)
    setSupplementForm({})
    setSupplementActiveTab('baby')
    setSupplementModalVisible(true)
    enrollmentAPI.getSupplementStatus(item.enrollmentId)
      .then((res: any) => {
        try {
          const data = unwrap<EnrollmentSupplementResponse>(res)
          setSupplementData(data)
        } catch { /* 静默 */ }
      })
      .catch(() => { /* 静默 */ })
  }

  // T076 资料补充：确认
  const handleSupplementConfirm = async (enrollmentId: string) => {
    // 先保存当前填写的内容
    if (Object.keys(supplementForm).length > 0) {
      try {
        const saveRes = await enrollmentAPI.saveSupplement(enrollmentId, supplementForm)
        unwrap<EnrollmentSupplementResponse>(saveRes)
      } catch { /* 静默 */ }
    }
    setSupplementSubmitting(true)
    try {
      const res = await enrollmentAPI.confirmSupplement(enrollmentId)
      const data = unwrap<EnrollmentSupplementResponse>(res)
      setSupplementData(data)
      message.success('资料确认成功')
      setSupplementModalVisible(false)

      // 刷新我的入托列表
      const enrollmentsRes = await enrollmentAPI.getMyEnrollments()
      try { setMyEnrollments(unwrap<MyEnrollment[]>(enrollmentsRes)) } catch { /* 静默 */ }
    } catch (err) {
      message.error(err instanceof Error ? err.message : '确认失败')
    } finally {
      setSupplementSubmitting(false)
    }
  }

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

    // 加载我的入托档案（T070）
    enrollmentAPI.getMyEnrollments()
      .then((res) => {
        try { setMyEnrollments(unwrap<MyEnrollment[]>(res)) } catch { /* 静默 */ }
      })
      .catch(() => { /* 静默 */ })

    // 加载账单（T072）
    setBillLoading(true)
    parentApplicationAPI.getMyBills()
      .then((res) => {
        try { setBills(unwrap<BillingStatement[]>(res)) } catch { /* 静默 */ }
      })
      .catch(() => { /* 静默 */ })
      .finally(() => setBillLoading(false))
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

      {/* 我的入托档案（T070 + T076 资料补充） */}
      {myEnrollments.length > 0 && (
        <Card size="small" style={{ marginBottom: 16 }} title="我的入托档案" extra={
          <Button size="small" onClick={() => setBindModalVisible(true)}>绑定邀请码</Button>
        }>
          <Space wrap size={[8, 8]}>
            {myEnrollments.map((item) => (
              <Tag key={item.enrollmentId} color="blue" style={{ padding: '4px 12px', fontSize: 14 }}>
                {item.babyName} · {item.organizationName} · {item.classroomName}
              </Tag>
            ))}
          </Space>
          {/* 资料补充入口 */}
          <div style={{ marginTop: 12, borderTop: '1px solid #f0f0f0', paddingTop: 12 }}>
            {myEnrollments.filter(e => e.status === 'PENDING' || e.status === 'HEALTH_CHECK').length > 0 ? (
              <Space wrap size={[8, 8]}>
                {myEnrollments.filter(e => e.status === 'PENDING' || e.status === 'HEALTH_CHECK').map((item) => (
                  <Space key={item.enrollmentId} size={4}>
                    <Text type="secondary" style={{ fontSize: 13 }}>
                      {item.babyName} · {item.statusDescription}
                    </Text>
                    {item.parentConfirmed ? (
                      <Tag color="green" style={{ marginRight: 0 }}>已确认</Tag>
                    ) : (
                      <Button size="small" type="primary" ghost
                        onClick={() => openSupplementModal(item)}>
                        补资料
                      </Button>
                    )}
                  </Space>
                ))}
              </Space>
            ) : (
              <Text type="secondary" style={{ fontSize: 13 }}>
                {myEnrollments.every(e => e.parentConfirmed) ? '所有入托档案资料已确认' : '暂无待补充资料的入托档案'}
              </Text>
            )}
          </div>
        </Card>
      )}
      {myEnrollments.length === 0 && (
        <Card size="small" style={{ marginBottom: 16 }}>
          <Space>
            <span>暂无入托绑定</span>
            <Button size="small" onClick={() => setBindModalVisible(true)}>绑定邀请码</Button>
          </Space>
        </Card>
      )}

      {/* ========== 资料补充弹窗（T076） ========== */}
      <Modal title={supplementEnrollment ? `补充资料 - ${supplementEnrollment.babyName}` : '补充资料'}
        open={supplementModalVisible} width={640}
        onCancel={() => { setSupplementModalVisible(false); setSupplementData(null) }}
        footer={null} destroyOnClose>
        {supplementEnrollment && supplementData && (
          <Space direction="vertical" size={16} style={{ width: '100%' }}>
            <Tabs activeKey={supplementActiveTab} onChange={setSupplementActiveTab}
              items={[
                { key: 'baby', label: '宝宝资料', children: (
                  <Space direction="vertical" size={12} style={{ width: '100%' }}>
                    <div>
                      <Text strong>{supplementData.babyName}</Text>
                      <Text type="secondary" style={{ marginLeft: 8 }}>
                        {supplementData.babyGender === 'MALE' ? '男' : '女'} · {supplementData.babyBirthday}
                      </Text>
                    </div>
                    <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 }}>
                      <div>
                        <Text type="secondary">身份证号</Text>
                        <Input placeholder="宝宝身份证号" maxLength={18}
                          value={supplementForm.babyIdCard ?? supplementData.babyIdCard ?? ''}
                          onChange={(e) => setSupplementForm(f => ({ ...f, babyIdCard: e.target.value }))} />
                      </div>
                      <div>
                        <Text type="secondary">出生证编号</Text>
                        <Input placeholder="出生证编号" maxLength={30}
                          value={supplementForm.babyBirthCertificateNo ?? supplementData.babyBirthCertificateNo ?? ''}
                          onChange={(e) => setSupplementForm(f => ({ ...f, babyBirthCertificateNo: e.target.value }))} />
                      </div>
                    </div>
                    <div style={{ textAlign: 'right' }}>
                      <Button type="primary" ghost onClick={() => setSupplementActiveTab('guardian')}>下一步</Button>
                    </div>
                  </Space>
                )},
                { key: 'guardian', label: '监护人资料', children: (
                  <Space direction="vertical" size={12} style={{ width: '100%' }}>
                    <div>
                      <Text strong>监护人关系：</Text>
                      <Text>{supplementData.guardianRelationship || '未设置'}</Text>
                    </div>
                    <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 }}>
                      <div>
                        <Text type="secondary">身份证号</Text>
                        <Input placeholder="监护人身份证号" maxLength={18}
                          value={supplementForm.guardianIdCard ?? supplementData.guardianIdCard ?? ''}
                          onChange={(e) => setSupplementForm(f => ({ ...f, guardianIdCard: e.target.value }))} />
                      </div>
                      <div>
                        <Text type="secondary">职业</Text>
                        <Input placeholder="监护人职业" maxLength={50}
                          value={supplementForm.guardianOccupation ?? supplementData.guardianOccupation ?? ''}
                          onChange={(e) => setSupplementForm(f => ({ ...f, guardianOccupation: e.target.value }))} />
                      </div>
                    </div>
                    <div>
                      <Text type="secondary">联系电话</Text>
                      <Input placeholder="监护人电话" maxLength={20}
                        value={supplementForm.guardianPhone ?? supplementData.guardianPhone ?? ''}
                        onChange={(e) => setSupplementForm(f => ({ ...f, guardianPhone: e.target.value }))} />
                    </div>
                    <div style={{ textAlign: 'right' }}>
                      <Button type="primary" ghost onClick={() => setSupplementActiveTab('health')}>下一步</Button>
                    </div>
                  </Space>
                )},
                { key: 'health', label: '健康与紧急联系', children: (
                  <Space direction="vertical" size={12} style={{ width: '100%' }}>
                    <div>
                      <Text type="secondary">过敏信息</Text>
                      <Input.TextArea rows={2} placeholder="过敏信息（如有）" maxLength={200}
                        value={supplementForm.allergyNotes ?? supplementData.allergyNotes ?? ''}
                        onChange={(e) => setSupplementForm(f => ({ ...f, allergyNotes: e.target.value }))} />
                    </div>
                    <div>
                      <Text type="secondary">健康/病史备注</Text>
                      <Input.TextArea rows={2} placeholder="健康或病史备注" maxLength={300}
                        value={supplementForm.medicalNotes ?? supplementData.medicalNotes ?? ''}
                        onChange={(e) => setSupplementForm(f => ({ ...f, medicalNotes: e.target.value }))} />
                    </div>
                    <div>
                      <Text type="secondary">特殊照护要求</Text>
                      <Input.TextArea rows={2} placeholder="特殊照护要求" maxLength={300}
                        value={supplementForm.specialCareNotes ?? supplementData.specialCareNotes ?? ''}
                        onChange={(e) => setSupplementForm(f => ({ ...f, specialCareNotes: e.target.value }))} />
                    </div>
                    <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 }}>
                      <div>
                        <Text type="secondary">紧急联系人姓名</Text>
                        <Input placeholder="联系人姓名" maxLength={30}
                          value={supplementForm.emergencyContactName ?? supplementData.emergencyContactName ?? ''}
                          onChange={(e) => setSupplementForm(f => ({ ...f, emergencyContactName: e.target.value }))} />
                      </div>
                      <div>
                        <Text type="secondary">紧急联系人电话</Text>
                        <Input placeholder="联系电话" maxLength={20}
                          value={supplementForm.emergencyContactPhone ?? supplementData.emergencyContactPhone ?? ''}
                          onChange={(e) => setSupplementForm(f => ({ ...f, emergencyContactPhone: e.target.value }))} />
                      </div>
                    </div>
                    <div style={{ textAlign: 'right' }}>
                      <Button type="primary" ghost onClick={() => setSupplementActiveTab('confirm')}>下一步</Button>
                    </div>
                  </Space>
                )},
                { key: 'confirm', label: '确认提交', children: (
                  <Space direction="vertical" size={16} style={{ width: '100%' }}>
                    <Alert type="info" showIcon message="请确认以下资料已完整填写" />
                    <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 8 }}>
                      <div style={{ padding: 12, border: '1px solid #f0f0f0', borderRadius: 6 }}>
                        <Text strong>宝宝资料</Text>
                        <div style={{ marginTop: 8 }}>
                          <Tag color={supplementData.babyInfoFilled || !!supplementForm.babyIdCard ? 'green' : 'orange'}>
                            {supplementForm.babyIdCard || supplementData.babyIdCard ? '已填' : '未填'}
                          </Tag>
                          {supplementForm.babyIdCard && <div style={{ fontSize: 12, marginTop: 4 }}>身份证: {supplementForm.babyIdCard}</div>}
                          {supplementForm.babyBirthCertificateNo && <div style={{ fontSize: 12 }}>出生证: {supplementForm.babyBirthCertificateNo}</div>}
                        </div>
                      </div>
                      <div style={{ padding: 12, border: '1px solid #f0f0f0', borderRadius: 6 }}>
                        <Text strong>监护人资料</Text>
                        <div style={{ marginTop: 8 }}>
                          <Tag color={supplementData.guardianInfoFilled || !!supplementForm.guardianIdCard ? 'green' : 'orange'}>
                            {supplementForm.guardianIdCard || supplementData.guardianIdCard ? '已填' : '未填'}
                          </Tag>
                          {supplementForm.guardianIdCard && <div style={{ fontSize: 12, marginTop: 4 }}>身份证: {supplementForm.guardianIdCard}</div>}
                          {supplementForm.guardianOccupation && <div style={{ fontSize: 12 }}>职业: {supplementForm.guardianOccupation}</div>}
                        </div>
                      </div>
                    </div>
                    <div style={{ padding: 12, border: '1px solid #f0f0f0', borderRadius: 6 }}>
                      <Text strong>健康与紧急联系</Text>
                      <div style={{ marginTop: 8 }}>
                        <Tag color={supplementData.healthInfoFilled || !!supplementForm.emergencyContactName ? 'green' : 'orange'}>
                          {supplementForm.emergencyContactName || supplementData.emergencyContactName ? '已填' : '未填'}
                        </Tag>
                        {supplementForm.emergencyContactName && <div style={{ fontSize: 12, marginTop: 4 }}>紧急联系人: {supplementForm.emergencyContactName}</div>}
                        {supplementForm.emergencyContactPhone && <div style={{ fontSize: 12 }}>电话: {supplementForm.emergencyContactPhone}</div>}
                      </div>
                    </div>
                    <Button type="primary" size="large" block
                      loading={supplementSubmitting}
                      disabled={!supplementForm.babyIdCard && !supplementForm.guardianIdCard && !supplementForm.emergencyContactName}
                      onClick={() => handleSupplementConfirm(supplementEnrollment!.enrollmentId)}>
                      {supplementData.parentConfirmed ? '已确认' : '确认资料完整'}
                    </Button>
                  </Space>
                )},
              ]} />
          </Space>
        )}
      </Modal>

      <Modal title="绑定入托档案" open={bindModalVisible}
        onCancel={() => { setBindModalVisible(false); setBindCode(''); }}
        onOk={async () => {
          if (!bindCode.trim()) { message.warning('请输入邀请码'); return }
          setSubmittingBind(true)
          try {
            await enrollmentAPI.bindByInviteCode({
              inviteCode: bindCode.trim(),
              relationship: bindRelationship,
            })
            message.success('绑定成功')
            setBindModalVisible(false)
            setBindCode('')
            // 刷新
            const res = await enrollmentAPI.getMyEnrollments()
            try { setMyEnrollments(unwrap<MyEnrollment[]>(res)) } catch { /* 静默 */ }
          } catch (err) {
            message.error(err instanceof Error ? err.message : '绑定失败')
          } finally {
            setSubmittingBind(false)
          }
        }} confirmLoading={submittingBind} destroyOnClose>
        <Space direction="vertical" className="full-width" size={12}>
          <div>
            <Text type="secondary">从机构获取邀请码后在此绑定</Text>
          </div>
          <Input placeholder="输入邀请码" value={bindCode} onChange={(e) => setBindCode(e.target.value)} />
          <Select value={bindRelationship} onChange={setBindRelationship}
            options={[
              { label: '父亲', value: 'FATHER' },
              { label: '母亲', value: 'MOTHER' },
              { label: '祖父/外祖父', value: 'GRANDFATHER' },
              { label: '祖母/外祖母', value: 'GRANDMOTHER' },
              { label: '其他亲属', value: 'OTHER' },
            ]} style={{ width: '100%' }} />
        </Space>
      </Modal>

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
          <Card title={<><WalletOutlined /> 我的账单</>} extra={
            <Button type="link" size="small" onClick={() => setBillFilter(billFilter === 'UNPAID' ? undefined : 'UNPAID')}>
              {billFilter === 'UNPAID' ? '全部' : '仅看未付'}
            </Button>
          }>
            {billLoading ? <Skeleton active paragraph={{ rows: 3 }} /> : (
              <List
                dataSource={billFilter ? bills.filter((b) => b.status === billFilter) : bills}
                locale={{ emptyText: <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description={billFilter === 'UNPAID' ? '暂无未付账单' : '暂无账单'} /> }}
                renderItem={(item) => (
                  <List.Item>
                    <List.Item.Meta
                      title={<Space>{item.title}{item.babyName && <Text type="secondary">({item.babyName})</Text>}</Space>}
                      description={<Space direction="vertical" size={2}>
                        <Text>¥{item.amount?.toFixed(2)}</Text>
                        {item.dueDate && <Text type="secondary">到期 {item.dueDate}</Text>}
                      </Space>}
                    />
                    <Space direction="vertical" size={2} style={{ textAlign: 'right' }}>
                      <Tag color={item.status === 'UNPAID' ? 'volcano' : item.status === 'PAID' ? 'green' : 'default'}>
                        {item.statusDescription || item.status}
                      </Tag>
                      {item.status === 'UNPAID' && (
                        <Button type="primary" size="small" disabled>支付（待接入）</Button>
                      )}
                    </Space>
                  </List.Item>
                )}
              />
            )}
          </Card>        </Space>
      </div>
    </div>
  )
}

export default ParentReports
