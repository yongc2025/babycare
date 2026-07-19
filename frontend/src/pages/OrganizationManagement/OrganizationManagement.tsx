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
  Switch,
  Tabs,
  Tag,
  Typography,
  message,
} from 'antd'
import { BankOutlined, PlusOutlined, SettingOutlined, TeamOutlined, UserAddOutlined, UserOutlined } from '@ant-design/icons'
import {
  classroomAPI,
  enrollmentAPI,
  organizationAPI,
  staffAPI,
} from '../../services/api'
import type { Classroom, Enrollment, EnrollmentGuardian, Organization, Staff, StaffClassroomAssignment } from '../../types'
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
  { label: '保健员', value: 'HEALTH_WORKER' },
  { label: '保健医', value: 'HEALTH_DOCTOR' },
  { label: '财务', value: 'FINANCE' },
  { label: '安全员', value: 'SAFETY_OFFICER' },
  { label: '后勤人员', value: 'LOGISTICS_STAFF' },
  { label: '运营人员', value: 'OPERATIONS_STAFF' },
  { label: '招生人员', value: 'ADMISSIONS_OFFICER' },
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
  const [modal, setModal] = useState<'organization' | 'classroom' | 'staff' | 'enrollment' | 'assignClassroom' | 'reviewEnrollment' | 'healthCheckEnrollment' | 'transferEnrollment' | 'withdrawEnrollment' | 'suspendEnrollment' | 'reactivateEnrollment' | null>(null)
  const [assignStaffId, setAssignStaffId] = useState<string | null>(null)
  const [staffAssignments, setStaffAssignments] = useState<Record<string, StaffClassroomAssignment[]>>({})
  const [selectedEnrollmentId, setSelectedEnrollmentId] = useState<string | null>(null)
  const [guardians, setGuardians] = useState<EnrollmentGuardian[]>([])
  const [guardianModalVisible, setGuardianModalVisible] = useState(false)
  const [guardianEnrollmentId, setGuardianEnrollmentId] = useState<string | null>(null)
  const [addGuardianModalVisible, setAddGuardianModalVisible] = useState(false)
  const [inviteCode, setInviteCode] = useState<string | null>(null)
  const [guardianForm] = Form.useForm()
  const [organizationForm] = Form.useForm()
  const [classroomForm] = Form.useForm()
  const [staffForm] = Form.useForm()
  const [enrollmentForm] = Form.useForm()
  const [assignForm] = Form.useForm()
  const [reviewForm] = Form.useForm()
  const [transferForm] = Form.useForm()

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

  const loadStaffAssignments = async (staffId: string) => {
    try {
      const res = await staffAPI.getStaffAssignments(staffId)
      const data = unwrap<StaffClassroomAssignment[]>(res)
      setStaffAssignments((prev) => ({ ...prev, [staffId]: data }))
    } catch {
      setStaffAssignments((prev) => ({ ...prev, [staffId]: [] }))
    }
  }

  const handleAssignToClassroom = async () => {
    const values = await assignForm.validateFields()
    await staffAPI.assignToClassroom({
      staffId: assignStaffId!,
      classroomId: values.classroomId,
      assignmentType: values.assignmentType,
    })
    message.success('分配成功')
    assignForm.resetFields()
    setAssignStaffId(null)
    closeModal()
    // 重新加载该员工的班级分配
    if (assignStaffId) loadStaffAssignments(assignStaffId)
  }

  const handleRemoveFromClassroom = async (staffId: string, classroomId: string) => {
    await staffAPI.removeFromClassroom(staffId, classroomId)
    message.success('已移除')
    loadStaffAssignments(staffId)
  }

  // 入托审核流程
  const handleReviewEnrollment = async () => {
    const values = await reviewForm.validateFields()
    await enrollmentAPI.reviewEnrollment(selectedEnrollmentId!, values)
    message.success(values.action === 'APPROVE' ? '入托申请已通过' : '入托申请已驳回')
    reviewForm.resetFields()
    setSelectedEnrollmentId(null)
    closeModal()
    selectedClassroomId && loadEnrollments(selectedClassroomId)
  }

  const handleHealthCheckEnrollment = async () => {
    const values = await reviewForm.validateFields()
    await enrollmentAPI.healthCheckEnrollment(selectedEnrollmentId!, values)
    message.success(values.passed ? '保健审核通过，正式入托' : '保健审核驳回')
    reviewForm.resetFields()
    setSelectedEnrollmentId(null)
    closeModal()
    selectedClassroomId && loadEnrollments(selectedClassroomId)
  }

  const handleTransferEnrollment = async () => {
    const values = await transferForm.validateFields()
    await enrollmentAPI.transferClassroom(selectedEnrollmentId!, values)
    message.success('转班成功')
    transferForm.resetFields()
    setSelectedEnrollmentId(null)
    closeModal()
    selectedClassroomId && loadEnrollments(selectedClassroomId)
  }

  const handleWithdrawEnrollment = async () => {
    const values = await reviewForm.validateFields() // 复用 reviewForm，包含 reason 字段
    await enrollmentAPI.withdrawEnrollment(selectedEnrollmentId!, { reason: values.reason })
    message.success('退托成功')
    reviewForm.resetFields()
    setSelectedEnrollmentId(null)
    closeModal()
    selectedClassroomId && loadEnrollments(selectedClassroomId)
  }

  // ========== 暂停与复托（T077） ==========

  const handleSuspendEnrollment = async () => {
    const values = await reviewForm.validateFields() // 复用 reviewForm
    await enrollmentAPI.suspendEnrollment(selectedEnrollmentId!, { reason: values.reason })
    message.success('入托已暂停')
    reviewForm.resetFields()
    setSelectedEnrollmentId(null)
    closeModal()
    selectedClassroomId && loadEnrollments(selectedClassroomId)
  }

  const handleReactivateEnrollment = async () => {
    try {
      await enrollmentAPI.reactivateEnrollment(selectedEnrollmentId!)
      message.success('复托成功')
      setSelectedEnrollmentId(null)
      closeModal()
      selectedClassroomId && loadEnrollments(selectedClassroomId)
    } catch (err: any) {
      message.error(err?.response?.data?.message || err.message || '复托失败')
    }
  }

  // 监护人管理
  const openGuardianModal = async (enrollmentId: string) => {
    setGuardianEnrollmentId(enrollmentId)
    setGuardianModalVisible(true)
    setInviteCode(null)
    try {
      const data = unwrap<EnrollmentGuardian[]>(await enrollmentAPI.getEnrollmentGuardians(enrollmentId))
      setGuardians(data)
    } catch {
      setGuardians([])
    }
  }

  const handleAddGuardian = async () => {
    const values = await guardianForm.validateFields()
    if (!guardianEnrollmentId) return
    try {
      await enrollmentAPI.addGuardian(guardianEnrollmentId, values)
      message.success('监护人已添加')
      guardianForm.resetFields()
      setAddGuardianModalVisible(false)
      // 刷新
      const data = unwrap<EnrollmentGuardian[]>(await enrollmentAPI.getEnrollmentGuardians(guardianEnrollmentId))
      setGuardians(data)
    } catch (err) {
      message.error(err instanceof Error ? err.message : '添加失败')
    }
  }

  const handleRemoveGuardian = async (guardianId: string) => {
    if (!guardianEnrollmentId) return
    try {
      await enrollmentAPI.removeGuardian(guardianEnrollmentId, guardianId)
      message.success('监护人已移除')
      const data = unwrap<EnrollmentGuardian[]>(await enrollmentAPI.getEnrollmentGuardians(guardianEnrollmentId))
      setGuardians(data)
    } catch (err) {
      message.error(err instanceof Error ? err.message : '移除失败')
    }
  }

  const handleGenerateInviteCode = async () => {
    if (!guardianEnrollmentId) return
    try {
      const res = await enrollmentAPI.generateInviteCode(guardianEnrollmentId)
      const code = unwrap<string>(res)
      setInviteCode(code)
    } catch (err) {
      message.error(err instanceof Error ? err.message : '生成失败')
    }
  }

  const handleToggleApproval = async (checked: boolean) => {
    if (!selectedOrganizationId) return
    try {
      await organizationAPI.updateOrganization(selectedOrganizationId, { dailyReportApprovalRequired: checked })
      message.success(`日报审核模式已${checked ? '开启' : '关闭'}`)
      // 刷新机构信息
      const data = unwrap<Organization>(await organizationAPI.getOrganizationDetail(selectedOrganizationId))
      setOrganizations((prev) => prev.map((o) => o.id === data.id ? data : o))
    } catch {
      message.error('设置失败')
    }
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
                  <div style={{ marginTop: 24, paddingTop: 16, borderTop: '1px solid #f0f0f0' }}>
                    <Space align="center">
                      <SettingOutlined />
                      <Text strong>日报审核模式</Text>
                      <Switch
                        checked={!!selectedOrganization.dailyReportApprovalRequired}
                        onChange={handleToggleApproval}
                        checkedChildren="开启"
                        unCheckedChildren="关闭"
                      />
                      <Text type="secondary">
                        {selectedOrganization.dailyReportApprovalRequired ? '教师提交日报后需园长审核方可发布' : '教师可直接发布日报'}
                      </Text>
                    </Space>
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
                      <List.Item
                        actions={[
                          <Button
                            type="link"
                            size="small"
                            onClick={() => {
                              setAssignStaffId(item.id)
                              setModal('assignClassroom')
                              loadStaffAssignments(item.id)
                            }}
                          >
                            分配班级
                          </Button>,
                        ]}
                      >
                        <List.Item.Meta
                          avatar={<UserOutlined className="list-icon" />}
                          title={item.nickname || item.username}
                          description={
                            <div>
                              <span>{`${item.roleDescription || item.role} · ${item.phone || item.email || '无联系方式'}`}</span>
                              {staffAssignments[item.id] && staffAssignments[item.id].length > 0 && (
                                <div style={{ marginTop: 4 }}>
                                  {staffAssignments[item.id].map((a) => (
                                    <Tag
                                      key={a.id}
                                      color="blue"
                                      closable
                                      onClose={() => handleRemoveFromClassroom(item.id, a.classroomId)}
                                    >
                                      {a.classroomName} ({a.assignmentTypeDescription})
                                    </Tag>
                                  ))}
                                </div>
                              )}
                            </div>
                          }
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
                      <List.Item
                        actions={[
                          <Button key="guardian" type="link" size="small" onClick={() => openGuardianModal(item.id)}>
                            监护人
                          </Button>,
                          item.status === 'PENDING' && (
                            <Button key="review" type="link" size="small" onClick={() => { setSelectedEnrollmentId(item.id); setModal('reviewEnrollment'); }}>
                              审核
                            </Button>
                          ),
                          item.status === 'HEALTH_CHECK' && (
                            <Button key="healthCheck" type="link" size="small" style={{ color: 'cyan' }} onClick={() => { setSelectedEnrollmentId(item.id); setModal('healthCheckEnrollment'); }}>
                              保健审核
                            </Button>
                          ),
                          item.status === 'ACTIVE' && (
                            <>
                              <Button key="transfer" type="link" size="small" onClick={() => { setSelectedEnrollmentId(item.id); setModal('transferEnrollment'); }}>
                                转班
                              </Button>
                              <Button key="suspend" type="link" size="small" onClick={() => { setSelectedEnrollmentId(item.id); setModal('suspendEnrollment'); }}>
                                暂停
                              </Button>
                              <Button key="withdraw" type="link" size="small" danger onClick={() => { setSelectedEnrollmentId(item.id); setModal('withdrawEnrollment'); }}>
                                退托
                              </Button>
                            </>
                          ),
                          item.status === 'SUSPENDED' && (
                            <Button key="reactivate" type="link" size="small" style={{ color: 'green' }} onClick={() => { setSelectedEnrollmentId(item.id); setModal('reactivateEnrollment'); }}>
                              复托
                            </Button>
                          ),
                        ].filter(Boolean)}
                      >
                        <List.Item.Meta
                          avatar={<BankOutlined className="list-icon" />}
                          title={item.babyName}
                          description={`${item.classroomName || '未分班'} · 入园日期 ${item.enrolledAt || '未填写'} · 紧急联系人 ${item.emergencyContactName || '未填写'}`}
                        />
                        <Tag color={item.status === 'ACTIVE' ? 'green' : item.status === 'HEALTH_CHECK' ? 'cyan' : item.status === 'PENDING' ? 'blue' : item.status === 'SUSPENDED' ? 'gold' : item.status === 'REJECTED' ? 'red' : 'orange'}>
                          {item.statusDescription || item.status}
                        </Tag>
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

      <Modal title="分配班级" open={modal === 'assignClassroom'} onOk={handleAssignToClassroom} onCancel={() => { closeModal(); setAssignStaffId(null); assignForm.resetFields(); }} destroyOnClose>
        <Form form={assignForm} layout="vertical">
          <Form.Item name="classroomId" label="选择班级" rules={[{ required: true, message: '请选择班级' }]}>
            <Select options={classrooms.map((c) => ({ label: c.name, value: c.id }))} />
          </Form.Item>
          <Form.Item name="assignmentType" label="分配类型" rules={[{ required: true, message: '请选择分配类型' }]}>
            <Select options={[
              { label: '教师', value: 'TEACHER' },
              { label: '保育员', value: 'CAREGIVER' },
              { label: '助教', value: 'ASSISTANT' },
            ]} />
          </Form.Item>
        </Form>
      </Modal>

      {/* 入托审核 */}
      <Modal title="审核入托申请" open={modal === 'reviewEnrollment'} onOk={handleReviewEnrollment} onCancel={() => { closeModal(); setSelectedEnrollmentId(null); reviewForm.resetFields(); }} destroyOnClose>
        <Form form={reviewForm} layout="vertical">
          <Form.Item name="action" label="审核结果" rules={[{ required: true, message: '请选择审核结果' }]}>
            <Select options={[
              { label: '通过', value: 'APPROVE' },
              { label: '驳回', value: 'REJECT' },
            ]} />
          </Form.Item>
          <Form.Item name="reason" label="原因（驳回必填）">
            <Input.TextArea rows={3} placeholder="驳回时请输入原因" />
          </Form.Item>
        </Form>
      </Modal>

      {/* 入托保健审核 */}
      <Modal title="入托保健审核" open={modal === 'healthCheckEnrollment'} onOk={handleHealthCheckEnrollment} onCancel={() => { closeModal(); setSelectedEnrollmentId(null); reviewForm.resetFields(); }} destroyOnClose>
        <Form form={reviewForm} layout="vertical">
          <Form.Item name="passed" label="保健审核结果" rules={[{ required: true, message: '请选择审核结果' }]}>
            <Select options={[
              { label: '通过（正式入托）', value: true },
              { label: '驳回', value: false },
            ]} />
          </Form.Item>
          <Form.Item name="remark" label="备注（驳回必填）">
            <Input.TextArea rows={3} placeholder="驳回时请输入原因" />
          </Form.Item>
        </Form>
      </Modal>

      {/* 转班 */}
      <Modal title="转班" open={modal === 'transferEnrollment'} onOk={handleTransferEnrollment} onCancel={() => { closeModal(); setSelectedEnrollmentId(null); transferForm.resetFields(); }} destroyOnClose>
        <Form form={transferForm} layout="vertical">
          <Form.Item name="newClassroomId" label="目标班级" rules={[{ required: true, message: '请选择目标班级' }]}>
            <Select options={classrooms.map((c) => ({ label: c.name, value: c.id }))} />
          </Form.Item>
          <Form.Item name="reason" label="转班原因">
            <Input.TextArea rows={3} />
          </Form.Item>
        </Form>
      </Modal>

      {/* 退托 */}
      <Modal title="确认退托" open={modal === 'withdrawEnrollment'} onOk={handleWithdrawEnrollment} onCancel={() => { closeModal(); setSelectedEnrollmentId(null); reviewForm.resetFields(); }} destroyOnClose>
        <Form form={reviewForm} layout="vertical">
          <Form.Item name="reason" label="退托原因">
            <Input.TextArea rows={3} />
          </Form.Item>
        </Form>
      </Modal>

      {/* 暂停（T077） */}
      <Modal title="暂停入托" open={modal === 'suspendEnrollment'} onOk={handleSuspendEnrollment} onCancel={() => { closeModal(); setSelectedEnrollmentId(null); reviewForm.resetFields(); }} destroyOnClose>
        <p style={{ marginBottom: 16 }}>暂停后该幼儿的入托档案将标记为"暂停"状态，可随时恢复。</p>
        <Form form={reviewForm} layout="vertical">
          <Form.Item name="reason" label="暂停原因">
            <Input.TextArea rows={3} placeholder="选填" />
          </Form.Item>
        </Form>
      </Modal>

      {/* 复托（T077） */}
      <Modal title="确认复托" open={modal === 'reactivateEnrollment'} onOk={handleReactivateEnrollment} onCancel={() => { closeModal(); setSelectedEnrollmentId(null); }} destroyOnClose>
        <p>复托后该幼儿的入托档案将恢复为"在托"状态。</p>
      </Modal>

      {/* 监护人管理弹窗 */}
      <Modal title="监护人管理" open={guardianModalVisible}
        onCancel={() => { setGuardianModalVisible(false); setGuardianEnrollmentId(null); setInviteCode(null); }}
        footer={null} width={520} destroyOnClose>
        <Space direction="vertical" className="full-width" size={12}>
          <Button type="primary" size="small" icon={<PlusOutlined />} onClick={() => { guardianForm.resetFields(); setAddGuardianModalVisible(true); }}>
            添加监护人
          </Button>
          <Button size="small" onClick={handleGenerateInviteCode} loading={false}>
            生成邀请码
          </Button>
          {inviteCode && (
            <Alert type="success" showIcon message={
              <Space>
                <Text copyable={{ text: inviteCode }}>邀请码：{inviteCode}</Text>
                <Text type="secondary">家长注册后使用此码绑定</Text>
              </Space>
            } />
          )}
        </Space>
        <List dataSource={guardians} locale={{ emptyText: <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="暂无监护人" /> }}
          renderItem={(g) => (
            <List.Item actions={[
              <Button key="remove" type="link" size="small" danger onClick={() => handleRemoveGuardian(g.id)}>移除</Button>,
            ]}>
              <List.Item.Meta
                title={<>{g.userNickname || g.userName} <Tag>{g.relationshipDescription}</Tag> {g.isPrimary && <Tag color="red">主要</Tag>}</>}
                description={`电话：${g.guardianPhone || g.userPhone || '未填写'} · 绑定方式：${g.bindTypeDescription}`}
              />
            </List.Item>
          )} />
      </Modal>

      {/* 添加监护人弹窗 */}
      <Modal title="添加监护人" open={addGuardianModalVisible}
        onOk={handleAddGuardian} onCancel={() => { setAddGuardianModalVisible(false); guardianForm.resetFields(); }} destroyOnClose>
        <Form form={guardianForm} layout="vertical">
          <Form.Item name="userId" label="用户 ID" rules={[{ required: true, message: '请输入用户 ID' }]}>
            <Input placeholder="输入家长用户 ID" />
          </Form.Item>
          <Form.Item name="relationship" label="关系">
            <Select options={[
              { label: '父亲', value: 'FATHER' },
              { label: '母亲', value: 'MOTHER' },
              { label: '祖父/外祖父', value: 'GRANDFATHER' },
              { label: '祖母/外祖母', value: 'GRANDMOTHER' },
              { label: '其他亲属', value: 'OTHER' },
            ]} />
          </Form.Item>
          <Form.Item name="isPrimary" label="是否主要联系人" valuePropName="checked">
            <Switch />
          </Form.Item>
          <Form.Item name="guardianPhone" label="监护人电话">
            <Input placeholder="可选" />
          </Form.Item>
          <Form.Item name="remark" label="备注">
            <Input.TextArea rows={2} placeholder="可选" />
          </Form.Item>
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
