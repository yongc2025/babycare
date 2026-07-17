import React, { useEffect, useMemo, useState } from 'react'
import {
  Alert,
  Button,
  Card,
  DatePicker,
  Empty,
  Form,
  Input,
  InputNumber,
  List,
  Modal,
  Select,
  Space,
  Tabs,
  Tag,
  Typography,
  message,
} from 'antd'
import { BankOutlined, PlusOutlined, TeamOutlined, UserAddOutlined, UserOutlined } from '@ant-design/icons'
import {
  classroomAPI,
  enrollmentAPI,
  organizationAPI,
  staffAPI,
} from '../../services/api'
import type { Classroom, Enrollment, Organization, Staff } from '../../types'
import './OrganizationManagement.css'

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

const roleOptions = [
  { label: '园长', value: 'DIRECTOR' },
  { label: '教师', value: 'TEACHER' },
  { label: '保育员', value: 'CAREGIVER' },
  { label: '财务', value: 'FINANCE' },
]

const OrganizationManagement: React.FC = () => {
  const [organizations, setOrganizations] = useState<Organization[]>([])
  const [selectedOrganizationId, setSelectedOrganizationId] = useState<string>()
  const [classrooms, setClassrooms] = useState<Classroom[]>([])
  const [selectedClassroomId, setSelectedClassroomId] = useState<string>()
  const [staffList, setStaffList] = useState<Staff[]>([])
  const [enrollments, setEnrollments] = useState<Enrollment[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [modal, setModal] = useState<'organization' | 'classroom' | 'staff' | 'enrollment' | null>(null)
  const [organizationForm] = Form.useForm()
  const [classroomForm] = Form.useForm()
  const [staffForm] = Form.useForm()
  const [enrollmentForm] = Form.useForm()

  const selectedOrganization = useMemo(
    () => organizations.find((item) => item.id === selectedOrganizationId),
    [organizations, selectedOrganizationId],
  )

  const loadOrganizations = async () => {
    setLoading(true)
    setError(null)

    try {
      const data = unwrap<Organization[]>(await organizationAPI.getMyOrganizations())
      setOrganizations(data)
      setSelectedOrganizationId((current) => current || data[0]?.id)
    } catch (err) {
      setError(err instanceof Error ? err.message : '机构列表加载失败')
    } finally {
      setLoading(false)
    }
  }

  const loadOrganizationData = async (organizationId: string) => {
    setLoading(true)
    setError(null)

    const [classroomResult, staffResult] = await Promise.allSettled([
      classroomAPI.getOrganizationClassrooms(organizationId),
      staffAPI.getOrganizationStaff(organizationId),
    ])

    if (classroomResult.status === 'fulfilled') {
      const data = unwrap<Classroom[]>(classroomResult.value)
      setClassrooms(data)
      setSelectedClassroomId((current) => current || data[0]?.id)
    } else {
      setClassrooms([])
    }

    if (staffResult.status === 'fulfilled') {
      setStaffList(unwrap<Staff[]>(staffResult.value))
    } else {
      setStaffList([])
    }

    const failed = [classroomResult, staffResult].some((result) => result.status === 'rejected')
    setError(failed ? '部分园所运营数据加载失败，请稍后重试' : null)
    setLoading(false)
  }

  const loadEnrollments = async (classroomId: string) => {
    try {
      setEnrollments(unwrap<Enrollment[]>(await enrollmentAPI.getClassroomEnrollments(classroomId)))
    } catch (err) {
      setEnrollments([])
      setError(err instanceof Error ? err.message : '入托档案加载失败')
    }
  }

  useEffect(() => {
    loadOrganizations()
  }, [])

  useEffect(() => {
    if (selectedOrganizationId) {
      loadOrganizationData(selectedOrganizationId)
    }
  }, [selectedOrganizationId])

  useEffect(() => {
    if (selectedClassroomId) {
      loadEnrollments(selectedClassroomId)
    } else {
      setEnrollments([])
    }
  }, [selectedClassroomId])

  const closeModal = () => setModal(null)

  const handleCreateOrganization = async () => {
    const values = await organizationForm.validateFields()
    await organizationAPI.createOrganization(values)
    message.success('机构已创建')
    organizationForm.resetFields()
    closeModal()
    loadOrganizations()
  }

  const handleCreateClassroom = async () => {
    const values = await classroomForm.validateFields()
    await classroomAPI.createClassroom({ ...values, organizationId: selectedOrganizationId })
    message.success('班级已创建')
    classroomForm.resetFields()
    closeModal()
    selectedOrganizationId && loadOrganizationData(selectedOrganizationId)
  }

  const handleCreateStaff = async () => {
    const values = await staffForm.validateFields()
    await staffAPI.createStaff({ ...values, organizationId: selectedOrganizationId })
    message.success('员工已添加')
    staffForm.resetFields()
    closeModal()
    selectedOrganizationId && loadOrganizationData(selectedOrganizationId)
  }

  const handleCreateEnrollment = async () => {
    const values = await enrollmentForm.validateFields()
    await enrollmentAPI.createEnrollment({
      ...values,
      organizationId: selectedOrganizationId,
      enrolledAt: values.enrolledAt?.format('YYYY-MM-DD'),
    })
    message.success('入托档案已创建')
    enrollmentForm.resetFields()
    closeModal()
    values.classroomId && loadEnrollments(values.classroomId)
  }

  return (
    <div className="organization-page">
      <Space direction="vertical" size={4} className="page-title">
        <Title level={2}>园所运营</Title>
        <Paragraph type="secondary">
          管理托育机构、班级、员工与入托档案，当前页面全部使用真实后端 API。
        </Paragraph>
      </Space>

      {error && <Alert type="warning" showIcon message={error} />}

      <Card>
        <Space wrap className="toolbar">
          <Select
            className="organization-selector"
            value={selectedOrganizationId}
            placeholder="选择机构"
            loading={loading}
            options={organizations.map((item) => ({ label: item.name, value: item.id }))}
            onChange={setSelectedOrganizationId}
          />
          <Button type="primary" icon={<PlusOutlined />} onClick={() => setModal('organization')}>
            创建机构
          </Button>
        </Space>
      </Card>

      {!selectedOrganization ? (
        <Card>
          <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="暂无机构，请先创建机构" />
        </Card>
      ) : (
        <Tabs
          items={[
            {
              key: 'organization',
              label: '机构信息',
              children: (
                <Card title={selectedOrganization.name}>
                  <div className="info-grid">
                    <Text>电话：{selectedOrganization.contactPhone || '未填写'}</Text>
                    <Text>地址：{selectedOrganization.address || '未填写'}</Text>
                    <Text>备案编号：{selectedOrganization.registrationNo || '未填写'}</Text>
                    <Text>主管部门：{selectedOrganization.supervisorDepartment || '未填写'}</Text>
                    <Text>机构等级：{selectedOrganization.organizationLevel || '未填写'}</Text>
                    <Text>运营类型：{selectedOrganization.operationType || '未填写'}</Text>
                  </div>
                </Card>
              ),
            },
            {
              key: 'classrooms',
              label: '班级',
              children: (
                <Card
                  title="班级列表"
                  extra={<Button icon={<PlusOutlined />} onClick={() => setModal('classroom')}>新增班级</Button>}
                >
                  <List
                    loading={loading}
                    dataSource={classrooms}
                    locale={{ emptyText: <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="暂无班级" /> }}
                    renderItem={(item) => (
                      <List.Item actions={[<Button type="link" onClick={() => setSelectedClassroomId(item.id)}>查看入托</Button>]}>
                        <List.Item.Meta
                          avatar={<TeamOutlined className="list-icon" />}
                          title={<Space>{item.name}{selectedClassroomId === item.id && <Tag color="green">当前</Tag>}</Space>}
                          description={`${item.ageRangeMinMonths ?? '-'}-${item.ageRangeMaxMonths ?? '-'} 月龄 · 容量 ${item.capacity ?? '-'} 人`}
                        />
                        <Tag color={item.status === 'ACTIVE' ? 'green' : 'default'}>{item.statusDescription || item.status}</Tag>
                      </List.Item>
                    )}
                  />
                </Card>
              ),
            },
            {
              key: 'staff',
              label: '员工',
              children: (
                <Card title="员工列表" extra={<Button icon={<UserAddOutlined />} onClick={() => setModal('staff')}>添加员工</Button>}>
                  <List
                    loading={loading}
                    dataSource={staffList}
                    locale={{ emptyText: <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="暂无员工" /> }}
                    renderItem={(item) => (
                      <List.Item>
                        <List.Item.Meta
                          avatar={<UserOutlined className="list-icon" />}
                          title={item.nickname || item.username}
                          description={`${item.roleDescription || item.role} · ${item.phone || item.email || '无联系方式'}`}
                        />
                        <Tag color={item.status === 'ACTIVE' ? 'green' : 'default'}>{item.statusDescription || item.status}</Tag>
                      </List.Item>
                    )}
                  />
                </Card>
              ),
            },
            {
              key: 'enrollments',
              label: '入托档案',
              children: (
                <Card
                  title="入托档案"
                  extra={<Button icon={<PlusOutlined />} onClick={() => setModal('enrollment')}>新增入托</Button>}
                >
                  <Select
                    className="classroom-selector"
                    value={selectedClassroomId}
                    placeholder="选择班级"
                    options={classrooms.map((item) => ({ label: item.name, value: item.id }))}
                    onChange={setSelectedClassroomId}
                  />
                  <List
                    className="enrollment-list"
                    dataSource={enrollments}
                    locale={{ emptyText: <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="当前班级暂无入托档案" /> }}
                    renderItem={(item) => (
                      <List.Item>
                        <List.Item.Meta
                          avatar={<BankOutlined className="list-icon" />}
                          title={item.babyName}
                          description={`${item.classroomName || '未分班'} · 入园日期 ${item.enrolledAt || '未填写'} · 紧急联系人 ${item.emergencyContactName || '未填写'}`}
                        />
                        <Tag color={item.status === 'ACTIVE' ? 'green' : 'orange'}>{item.statusDescription || item.status}</Tag>
                      </List.Item>
                    )}
                  />
                </Card>
              ),
            },
          ]}
        />
      )}

      <Modal title="创建机构" open={modal === 'organization'} onOk={handleCreateOrganization} onCancel={closeModal} destroyOnClose>
        <Form form={organizationForm} layout="vertical">
          <Form.Item name="name" label="机构名称" rules={[{ required: true, message: '请输入机构名称' }]}><Input /></Form.Item>
          <Form.Item name="contactPhone" label="联系电话"><Input /></Form.Item>
          <Form.Item name="address" label="机构地址"><Input /></Form.Item>
          <Form.Item name="description" label="机构简介"><Input.TextArea rows={3} /></Form.Item>
        </Form>
      </Modal>

      <Modal title="新增班级" open={modal === 'classroom'} onOk={handleCreateClassroom} onCancel={closeModal} destroyOnClose>
        <Form form={classroomForm} layout="vertical">
          <Form.Item name="name" label="班级名称" rules={[{ required: true, message: '请输入班级名称' }]}><Input /></Form.Item>
          <Form.Item name="ageRangeMinMonths" label="最小月龄"><InputNumber min={0} className="full-width" /></Form.Item>
          <Form.Item name="ageRangeMaxMonths" label="最大月龄"><InputNumber min={0} className="full-width" /></Form.Item>
          <Form.Item name="capacity" label="托位容量"><InputNumber min={0} className="full-width" /></Form.Item>
        </Form>
      </Modal>

      <Modal title="添加员工" open={modal === 'staff'} onOk={handleCreateStaff} onCancel={closeModal} destroyOnClose>
        <Form form={staffForm} layout="vertical">
          <Form.Item name="userId" label="用户 ID" rules={[{ required: true, message: '请输入用户 ID' }]}><Input /></Form.Item>
          <Form.Item name="role" label="岗位角色" rules={[{ required: true, message: '请选择岗位角色' }]}><Select options={roleOptions} /></Form.Item>
        </Form>
      </Modal>

      <Modal title="新增入托档案" open={modal === 'enrollment'} onOk={handleCreateEnrollment} onCancel={closeModal} destroyOnClose>
        <Form form={enrollmentForm} layout="vertical" initialValues={{ classroomId: selectedClassroomId }}>
          <Form.Item name="babyId" label="宝宝 ID" rules={[{ required: true, message: '请输入宝宝 ID' }]}><Input /></Form.Item>
          <Form.Item name="classroomId" label="班级" rules={[{ required: true, message: '请选择班级' }]}><Select options={classrooms.map((item) => ({ label: item.name, value: item.id }))} /></Form.Item>
          <Form.Item name="enrolledAt" label="入园日期"><DatePicker className="full-width" /></Form.Item>
          <Form.Item name="emergencyContactName" label="紧急联系人"><Input /></Form.Item>
          <Form.Item name="emergencyContactPhone" label="紧急联系电话"><Input /></Form.Item>
          <Form.Item name="allergyNotes" label="过敏备注"><Input.TextArea rows={2} /></Form.Item>
          <Form.Item name="medicalNotes" label="健康备注"><Input.TextArea rows={2} /></Form.Item>
          <Form.Item name="specialCareNotes" label="特殊照护"><Input.TextArea rows={2} /></Form.Item>
        </Form>
      </Modal>
    </div>
  )
}

export default OrganizationManagement
