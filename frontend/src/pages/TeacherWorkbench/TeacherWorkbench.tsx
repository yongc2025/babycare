import React, { useEffect, useMemo, useState } from 'react'
import {
  Alert,
  Button,
  Card,
  Empty,
  Form,
  Input,
  InputNumber,
  List,
  Modal,
  Select,
  Skeleton,
  Space,
  Switch,
  Tabs,
  Tag,
  Typography,
  message,
} from 'antd'
import {
  CalendarOutlined,
  CheckCircleOutlined,
  FileTextOutlined,
  HeartOutlined,
  MedicineBoxOutlined,
  TeamOutlined,
} from '@ant-design/icons'
import dayjs from 'dayjs'
import { useNavigate } from 'react-router-dom'
import {
  attendanceAPI,
  careRecordAPI,
  classroomAPI,
  enrollmentAPI,
  healthObservationAPI,
  organizationAPI,
} from '../../services/api'
import type { AttendanceRecord, CareRecord, Classroom, Enrollment, HealthObservation, Organization } from '../../types'
import './TeacherWorkbench.css'

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

const TeacherWorkbench: React.FC = () => {
  const navigate = useNavigate()
  const [organizations, setOrganizations] = useState<Organization[]>([])
  const [organizationId, setOrganizationId] = useState<string>()
  const [classrooms, setClassrooms] = useState<Classroom[]>([])
  const [classroomId, setClassroomId] = useState<string>()
  const [enrollments, setEnrollments] = useState<Enrollment[]>([])
  const [attendanceRecords, setAttendanceRecords] = useState<AttendanceRecord[]>([])
  const [careRecords, setCareRecords] = useState<CareRecord[]>([])
  const [healthRecords, setHealthRecords] = useState<HealthObservation[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [modal, setModal] = useState<'care' | 'health' | null>(null)
  const [careForm] = Form.useForm()
  const [healthForm] = Form.useForm()
  const today = dayjs().format('YYYY-MM-DD')

  const attendanceByEnrollment = useMemo(() => {
    return new Map(attendanceRecords.map((item) => [item.enrollmentId, item]))
  }, [attendanceRecords])

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

  const loadClassroomToday = async (id: string) => {
    setLoading(true)
    setError(null)

    const results = await Promise.allSettled([
      enrollmentAPI.getClassroomEnrollments(id),
      attendanceAPI.getClassroomAttendance(id, today),
      careRecordAPI.getClassroomRecords(id, today),
      healthObservationAPI.getClassroomObservations(id, today),
    ])

    setEnrollments(results[0].status === 'fulfilled' ? unwrap<Enrollment[]>(results[0].value) : [])
    setAttendanceRecords(results[1].status === 'fulfilled' ? unwrap<AttendanceRecord[]>(results[1].value) : [])
    setCareRecords(results[2].status === 'fulfilled' ? unwrap<CareRecord[]>(results[2].value) : [])
    setHealthRecords(results[3].status === 'fulfilled' ? unwrap<HealthObservation[]>(results[3].value) : [])

    const failed = results.some((item) => item.status === 'rejected')
    setError(failed ? '部分班级数据暂时无法加载，请稍后重试' : null)
    setLoading(false)
  }

  useEffect(() => {
    loadOrganizations()
  }, [])

  useEffect(() => {
    if (organizationId) {
      loadClassrooms(organizationId)
    }
  }, [organizationId])

  useEffect(() => {
    if (classroomId) {
      loadClassroomToday(classroomId)
    }
  }, [classroomId])

  const handleCheckIn = async (enrollmentId: string) => {
    await attendanceAPI.checkIn({ enrollmentId, attendanceDate: today })
    message.success('已记录到园')
    classroomId && loadClassroomToday(classroomId)
  }

  const handleMarkAbsent = async (enrollmentId: string) => {
    await attendanceAPI.markAbsent({ enrollmentId, attendanceDate: today })
    message.success('已标记缺勤')
    classroomId && loadClassroomToday(classroomId)
  }

  const handleCreateCare = async () => {
    const values = await careForm.validateFields()
    await careRecordAPI.createRecord({
      ...values,
      recordDate: today,
      recordTime: dayjs().format('YYYY-MM-DDTHH:mm:ss'),
      source: 'TEACHER_WORKBENCH',
    })
    message.success('照护记录已新增')
    careForm.resetFields()
    setModal(null)
    classroomId && loadClassroomToday(classroomId)
  }

  const handleCreateHealth = async () => {
    const values = await healthForm.validateFields()
    await healthObservationAPI.createObservation({
      ...values,
      observationDate: today,
      observationTime: dayjs().format('YYYY-MM-DDTHH:mm:ss'),
      source: 'TEACHER_WORKBENCH',
    })
    message.success('健康观察已新增')
    healthForm.resetFields()
    setModal(null)
    classroomId && loadClassroomToday(classroomId)
  }

  const checkedInCount = attendanceRecords.filter((item) => item.status === 'CHECKED_IN' || item.status === 'CHECKED_OUT').length
  const leaveCount = attendanceRecords.filter((item) => item.status === 'LEAVE').length
  const abnormalHealthCount = healthRecords.filter((item) => item.abnormal).length

  return (
    <div className="teacher-page">
      <Space direction="vertical" size={4} className="page-title">
        <Title level={2}>班级照护</Title>
        <Paragraph type="secondary">老师/保育员今日工作台，聚合入托幼儿、考勤、晨午检和照护记录。</Paragraph>
      </Space>

      {error && <Alert type="warning" showIcon message={error} />}

      <Card>
        <Space wrap>
          <Select className="teacher-select" value={organizationId} placeholder="选择机构"
            options={organizations.map((item) => ({ label: item.name, value: item.id }))} onChange={setOrganizationId} />
          <Select className="teacher-select" value={classroomId} placeholder="选择班级"
            options={classrooms.map((item) => ({ label: item.name, value: item.id }))} onChange={setClassroomId} />
          <Button icon={<FileTextOutlined />} onClick={() => navigate('/daily-report-management')}>
            日报管理
          </Button>
          <Button disabled={!classroomId || enrollments.length === 0} icon={<HeartOutlined />} onClick={() => setModal('care')}>
            新增照护
          </Button>
          <Button disabled={!classroomId || enrollments.length === 0} icon={<MedicineBoxOutlined />} onClick={() => setModal('health')}>
            新增健康观察
          </Button>
        </Space>
      </Card>

      {loading ? (
        <Card><Skeleton active paragraph={{ rows: 8 }} /></Card>
      ) : !classroomId ? (
        <Card><Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="请先选择班级" /></Card>
      ) : (
        <>
          <section className="teacher-stat-grid">
            <Card><Text type="secondary">在班幼儿</Text><strong>{enrollments.length}</strong><span>人</span></Card>
            <Card><Text type="secondary">已到园</Text><strong>{checkedInCount}</strong><span>人</span></Card>
            <Card><Text type="secondary">请假</Text><strong>{leaveCount}</strong><span>人</span></Card>
            <Card><Text type="secondary">健康异常</Text><strong>{abnormalHealthCount}</strong><span>条</span></Card>
          </section>

          <Tabs
            items={[
              {
                key: 'children',
                label: '今日幼儿',
                children: (
                  <Card title={<><TeamOutlined /> 今日幼儿</>}>
                    <List
                      dataSource={enrollments}
                      locale={{ emptyText: <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="当前班级暂无入托幼儿" /> }}
                      renderItem={(item) => {
                        const attendance = attendanceByEnrollment.get(item.id)
                        return (
                          <List.Item
                            actions={[
                              <Button type="link" onClick={() => handleCheckIn(item.id)} disabled={!!attendance}>到园</Button>,
                              <Button type="link" danger onClick={() => handleMarkAbsent(item.id)} disabled={!!attendance}>缺勤</Button>,
                            ]}
                          >
                            <List.Item.Meta
                              title={item.babyName}
                              description={`${item.classroomName || '当前班级'} · ${item.emergencyContactName || '未填紧急联系人'}`}
                            />
                            <Tag color={attendance ? 'green' : 'orange'}>{attendance?.statusDescription || '未签到'}</Tag>
                          </List.Item>
                        )
                      }}
                    />
                  </Card>
                ),
              },
              {
                key: 'attendance',
                label: '考勤',
                children: (
                  <Card title={<><CalendarOutlined /> 今日考勤</>}>
                    <List
                      dataSource={attendanceRecords}
                      locale={{ emptyText: <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="暂无考勤记录" /> }}
                      renderItem={(item) => (
                        <List.Item>
                          <List.Item.Meta title={item.babyName} description={`到园 ${item.checkInAt ? dayjs(item.checkInAt).format('HH:mm') : '-'} · 离园 ${item.checkOutAt ? dayjs(item.checkOutAt).format('HH:mm') : '-'}`} />
                          <Tag color="blue">{item.statusDescription || item.status}</Tag>
                        </List.Item>
                      )}
                    />
                  </Card>
                ),
              },
              {
                key: 'health',
                label: '晨午检',
                children: (
                  <Card title={<><MedicineBoxOutlined /> 今日健康观察</>}>
                    <List
                      dataSource={healthRecords}
                      locale={{ emptyText: <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="暂无晨午检或全日观察记录" /> }}
                      renderItem={(item) => (
                        <List.Item>
                          <List.Item.Meta title={item.babyName} description={`${item.typeDescription || item.type} · 体温 ${item.temperature ?? '-'} · ${item.symptoms || '无症状备注'}`} />
                          <Tag color={item.abnormal ? 'red' : 'green'}>{item.abnormal ? '异常' : '正常'}</Tag>
                        </List.Item>
                      )}
                    />
                  </Card>
                ),
              },
              {
                key: 'care',
                label: '照护',
                children: (
                  <Card title={<><HeartOutlined /> 今日照护记录</>}>
                    <List
                      dataSource={careRecords}
                      locale={{ emptyText: <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="暂无照护记录" /> }}
                      renderItem={(item) => (
                        <List.Item>
                          <List.Item.Meta title={item.babyName} description={`${item.typeDescription || item.type} · ${item.valueText || item.amount || '-'}${item.unit || ''}`} />
                          <Tag icon={<CheckCircleOutlined />} color="green">{dayjs(item.recordTime).format('HH:mm')}</Tag>
                        </List.Item>
                      )}
                    />
                  </Card>
                ),
              },
            ]}
          />
        </>
      )}

      <Modal title="新增照护记录" open={modal === 'care'} onOk={handleCreateCare} onCancel={() => setModal(null)} destroyOnClose>
        <Form form={careForm} layout="vertical" initialValues={{ type: 'FEEDING', unit: 'ml' }}>
          <Form.Item name="enrollmentId" label="宝宝" rules={[{ required: true, message: '请选择宝宝' }]}>
            <Select options={enrollments.map((item) => ({ label: item.babyName, value: item.id }))} />
          </Form.Item>
          <Form.Item name="type" label="照护类型" rules={[{ required: true, message: '请选择照护类型' }]}>
            <Select
              options={[
                { label: '喂养', value: 'FEEDING' },
                { label: '饮水', value: 'WATER' },
                { label: '睡眠', value: 'SLEEP' },
                { label: '如厕', value: 'TOILET' },
                { label: '体温', value: 'TEMPERATURE' },
                { label: '情绪', value: 'MOOD' },
                { label: '活动', value: 'ACTIVITY' },
              ]}
            />
          </Form.Item>
          <Form.Item name="valueText" label="记录内容"><Input placeholder="例如：午餐吃完、睡眠平稳" /></Form.Item>
          <Form.Item name="amount" label="数值"><InputNumber className="full-width" min={0} /></Form.Item>
          <Form.Item name="unit" label="单位"><Input placeholder="ml、分钟、℃" /></Form.Item>
          <Form.Item name="remark" label="备注"><Input.TextArea rows={3} /></Form.Item>
        </Form>
      </Modal>

      <Modal title="新增健康观察" open={modal === 'health'} onOk={handleCreateHealth} onCancel={() => setModal(null)} destroyOnClose>
        <Form form={healthForm} layout="vertical" initialValues={{ type: 'MORNING_CHECK', abnormal: false, followUpRequired: false }}>
          <Form.Item name="enrollmentId" label="宝宝" rules={[{ required: true, message: '请选择宝宝' }]}>
            <Select options={enrollments.map((item) => ({ label: item.babyName, value: item.id }))} />
          </Form.Item>
          <Form.Item name="type" label="观察类型" rules={[{ required: true, message: '请选择观察类型' }]}>
            <Select
              options={[
                { label: '晨检', value: 'MORNING_CHECK' },
                { label: '午检', value: 'NOON_CHECK' },
                { label: '全日观察', value: 'FULL_DAY_OBSERVATION' },
              ]}
            />
          </Form.Item>
          <Form.Item name="temperature" label="体温"><InputNumber className="full-width" min={34} max={42} step={0.1} /></Form.Item>
          <Form.Item name="symptoms" label="症状描述"><Input.TextArea rows={3} /></Form.Item>
          <Form.Item name="actionTaken" label="处理建议"><Input.TextArea rows={2} /></Form.Item>
          <Form.Item name="abnormal" label="是否异常" valuePropName="checked"><Switch /></Form.Item>
          <Form.Item name="followUpRequired" label="需要跟进" valuePropName="checked"><Switch /></Form.Item>
        </Form>
      </Modal>
    </div>
  )
}

export default TeacherWorkbench
