import React, { useEffect, useState } from 'react'
import { Button, Card, DatePicker, Empty, Form, Input, List, Modal, Select, Space, Tag, Tabs, Typography, message } from 'antd'
import {
  CloseCircleOutlined,
  ClockCircleOutlined,
  MedicineBoxOutlined,
  SafetyCertificateOutlined,
} from '@ant-design/icons'
import dayjs from 'dayjs'
import { enrollmentAPI, parentApplicationAPI } from '../../services/organizationApi'
import { useFamilyStore } from '../../stores/familyStore'
import type { MyEnrollment } from '../../types'

const { Title, Paragraph, Text } = Typography

interface ParentApplication {
  id: number
  applicationType: 'LEAVE' | 'MEDICATION' | 'PICKUP'
  applicationTypeDescription: string
  babyId: number
  babyName: string
  enrollmentId: number
  classroomName?: string
  organizationName?: string
  status: 'PENDING' | 'APPROVED' | 'REJECTED' | 'CANCELLED'
  statusDescription?: string
  reason?: string
  requestedByName?: string
  reviewedByName?: string
  reviewRemark?: string
  reviewedAt?: string
  createdAt: string
  // 请假
  leaveStartDate?: string
  leaveEndDate?: string
  leaveType?: string
  leaveTypeDescription?: string
  // 用药
  medicineName?: string
  dosage?: string
  frequency?: string
  medicationStartDate?: string
  medicationEndDate?: string
  instructions?: string
  // 接送
  pickupDate?: string
  pickupPersonName?: string
  pickupRelationship?: string
  pickupPhone?: string
  pickupCode?: string
}

const statusColorMap: Record<string, string> = {
  PENDING: 'orange',
  APPROVED: 'green',
  REJECTED: 'red',
  CANCELLED: 'default',
}

const ParentApplications: React.FC = () => {
  const { loadFamilies } = useFamilyStore()
  const [applications, setApplications] = useState<ParentApplication[]>([])
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [myEnrollments, setMyEnrollments] = useState<MyEnrollment[]>([])
  const [leaveModalVisible, setLeaveModalVisible] = useState(false)
  const [medicationModalVisible, setMedicationModalVisible] = useState(false)
  const [pickupModalVisible, setPickupModalVisible] = useState(false)
  const [submitting, setSubmitting] = useState(false)
  const [leaveForm] = Form.useForm()
  const [medicationForm] = Form.useForm()
  const [pickupForm] = Form.useForm()

  useEffect(() => {
    loadFamilies().catch(() => setError('家庭与宝宝信息加载失败'))
  }, [loadFamilies])

  useEffect(() => {
    loadApplications()
    enrollmentAPI.getMyEnrollments()
      .then((res: any) => {
        try {
          const data = res && typeof res === 'object' && 'data' in res ? (res as any).data : res
          setMyEnrollments(Array.isArray(data) ? data : [])
        } catch { /* 静默 */ }
      })
      .catch(() => { /* 静默 */ })
  }, [])

  const loadApplications = async () => {
    setIsLoading(true)
    setError(null)
    try {
      const res = await parentApplicationAPI.getMyApplications()
      const data = res && typeof res === 'object' && 'data' in res ? (res as any).data : res
      setApplications(Array.isArray(data) ? data : [])
    } catch (err) {
      setError('申请列表加载失败')
    } finally {
      setIsLoading(false)
    }
  }

  const handleCancel = async (app: ParentApplication) => {
    try {
      await parentApplicationAPI.cancelApplication({
        applicationType: app.applicationType,
        applicationId: app.id,
      })
      message.success('申请已取消')
      loadApplications()
    } catch (err) {
      message.error('取消失败')
    }
  }

  const handleCreateLeave = async (values: any) => {
    setSubmitting(true)
    try {
      await parentApplicationAPI.createLeaveRequest({
        enrollmentId: values.enrollmentId,
        startDate: values.dateRange[0].format('YYYY-MM-DD'),
        endDate: values.dateRange[1].format('YYYY-MM-DD'),
        type: values.type,
        reason: values.reason,
      })
      message.success('请假申请提交成功')
      setLeaveModalVisible(false)
      leaveForm.resetFields()
      loadApplications()
    } catch (err) {
      message.error('提交失败')
    } finally {
      setSubmitting(false)
    }
  }

  const handleCreateMedication = async (values: any) => {
    setSubmitting(true)
    try {
      await parentApplicationAPI.createMedicationRequest({
        enrollmentId: values.enrollmentId,
        medicineName: values.medicineName,
        dosage: values.dosage,
        frequency: values.frequency,
        startDate: values.startDate.format('YYYY-MM-DD'),
        endDate: values.endDate.format('YYYY-MM-DD'),
        instructions: values.instructions,
      })
      message.success('用药委托提交成功')
      setMedicationModalVisible(false)
      medicationForm.resetFields()
      loadApplications()
    } catch (err) {
      message.error('提交失败')
    } finally {
      setSubmitting(false)
    }
  }

  const handleCreatePickup = async (values: any) => {
    setSubmitting(true)
    try {
      await parentApplicationAPI.createPickupDelegation({
        enrollmentId: values.enrollmentId,
        pickupDate: values.pickupDate.format('YYYY-MM-DD'),
        pickupPersonName: values.pickupPersonName,
        pickupRelationship: values.pickupRelationship,
        pickupPhone: values.pickupPhone,
        reason: values.reason,
      })
      message.success('接送委托提交成功')
      setPickupModalVisible(false)
      pickupForm.resetFields()
      loadApplications()
    } catch (err) {
      message.error('提交失败')
    } finally {
      setSubmitting(false)
    }
  }

  const renderApplicationItem = (app: ParentApplication) => {
    const descriptions: string[] = []
    if (app.applicationType === 'LEAVE') {
      descriptions.push(`${app.leaveStartDate} ~ ${app.leaveEndDate}`)
      if (app.leaveTypeDescription) descriptions.push(app.leaveTypeDescription)
      if (app.reason) descriptions.push(`原因: ${app.reason}`)
    } else if (app.applicationType === 'MEDICATION') {
      descriptions.push(app.medicineName || '')
      if (app.dosage) descriptions.push(`剂量: ${app.dosage}`)
      if (app.medicationStartDate) descriptions.push(`${app.medicationStartDate} ~ ${app.medicationEndDate}`)
    } else if (app.applicationType === 'PICKUP') {
      descriptions.push(app.pickupPersonName || '')
      if (app.pickupDate) descriptions.push(app.pickupDate)
      if (app.pickupPhone) descriptions.push(app.pickupPhone)
      if (app.reason) descriptions.push(`原因: ${app.reason}`)
    }

    return (
      <List.Item
        actions={
          app.status === 'PENDING'
            ? [<Button key="cancel" type="link" danger icon={<CloseCircleOutlined />} onClick={() => handleCancel(app)}>取消</Button>]
            : []
        }
      >
        <List.Item.Meta
          title={
            <Space>
              <Tag color={statusColorMap[app.status]}>{app.statusDescription || app.status}</Tag>
              <Text strong>{app.applicationTypeDescription}</Text>
              <Text type="secondary">{app.babyName}</Text>
            </Space>
          }
          description={
            <Space direction="vertical" size={2}>
              <Text type="secondary">{descriptions.filter(Boolean).join(' · ')}</Text>
              {app.reviewRemark && <Text type="warning">审核意见: {app.reviewRemark}</Text>}
              <Text type="secondary" style={{ fontSize: 12 }}>{dayjs(app.createdAt).format('YYYY-MM-DD HH:mm')} 提交</Text>
            </Space>
          }
        />
      </List.Item>
    )
  }

  const enrollmentOptions = myEnrollments.map((e) => ({
    label: `${e.babyName} · ${e.organizationName} · ${e.classroomName}`,
    value: e.enrollmentId,
  }))

  return (
    <div>
      <Space direction="vertical" size={4} style={{ marginBottom: 20 }}>
        <Title level={2} style={{ margin: 0 }}>我的申请</Title>
        <Paragraph type="secondary" style={{ margin: 0 }}>
          统一管理请假、用药委托和接送委托申请。
        </Paragraph>
      </Space>

      <Card style={{ marginBottom: 16 }}>
        <Space wrap size={12}>
          <Button type="primary" icon={<ClockCircleOutlined />} onClick={() => setLeaveModalVisible(true)}>
            请假申请
          </Button>
          <Button type="primary" icon={<MedicineBoxOutlined />} onClick={() => setMedicationModalVisible(true)}>
            用药委托
          </Button>
          <Button type="primary" icon={<SafetyCertificateOutlined />} onClick={() => setPickupModalVisible(true)}>
            接送委托
          </Button>
        </Space>
      </Card>

      {error && <div style={{ color: 'red', marginBottom: 16 }}>{error}</div>}

      <Card>
        <Tabs items={[
          { key: 'all', label: '全部' },
          { key: 'PENDING', label: '待审核' },
          { key: 'APPROVED', label: '已通过' },
          { key: 'REJECTED', label: '已拒绝' },
          { key: 'CANCELLED', label: '已取消' },
        ]} />

        <List
          loading={isLoading}
          dataSource={applications}
          locale={{ emptyText: <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="暂无申请记录" /> }}
          renderItem={renderApplicationItem}
        />
      </Card>

      {/* 请假申请弹窗 */}
      <Modal title="提交请假申请" open={leaveModalVisible}
        onCancel={() => { setLeaveModalVisible(false); leaveForm.resetFields() }}
        onOk={() => leaveForm.submit()} confirmLoading={submitting} destroyOnClose>
        <Form form={leaveForm} layout="vertical" onFinish={handleCreateLeave}>
          <Form.Item name="enrollmentId" label="宝宝" rules={[{ required: true, message: '请选择宝宝' }]}>
            <Select options={enrollmentOptions} placeholder="请选择宝宝" />
          </Form.Item>
          <Form.Item name="dateRange" label="请假日期" rules={[{ required: true, message: '请选择请假日期' }]}>
            <DatePicker.RangePicker style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="type" label="请假类型" rules={[{ required: true, message: '请选择请假类型' }]}>
            <Select options={[
              { label: '病假', value: 'SICK' },
              { label: '事假', value: 'PERSONAL' },
              { label: '其他', value: 'OTHER' },
            ]} />
          </Form.Item>
          <Form.Item name="reason" label="请假原因">
            <Input.TextArea rows={3} placeholder="请输入请假原因（选填）" />
          </Form.Item>
        </Form>
      </Modal>

      {/* 用药委托弹窗 */}
      <Modal title="提交用药委托" open={medicationModalVisible}
        onCancel={() => { setMedicationModalVisible(false); medicationForm.resetFields() }}
        onOk={() => medicationForm.submit()} confirmLoading={submitting} destroyOnClose>
        <Form form={medicationForm} layout="vertical" onFinish={handleCreateMedication}>
          <Form.Item name="enrollmentId" label="宝宝" rules={[{ required: true, message: '请选择宝宝' }]}>
            <Select options={enrollmentOptions} placeholder="请选择宝宝" />
          </Form.Item>
          <Form.Item name="medicineName" label="药品名称" rules={[{ required: true, message: '请输入药品名称' }]}>
            <Input placeholder="请输入药品名称" />
          </Form.Item>
          <Form.Item name="dosage" label="剂量">
            <Input placeholder="如：5ml/次" />
          </Form.Item>
          <Form.Item name="frequency" label="用药频率">
            <Input placeholder="如：每日3次" />
          </Form.Item>
          <Form.Item name="startDate" label="开始日期" rules={[{ required: true, message: '请选择开始日期' }]}>
            <DatePicker style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="endDate" label="结束日期" rules={[{ required: true, message: '请选择结束日期' }]}>
            <DatePicker style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="instructions" label="用药说明">
            <Input.TextArea rows={3} placeholder="其他用药说明（选填）" />
          </Form.Item>
        </Form>
      </Modal>

      {/* 接送委托弹窗 */}
      <Modal title="提交接送委托" open={pickupModalVisible}
        onCancel={() => { setPickupModalVisible(false); pickupForm.resetFields() }}
        onOk={() => pickupForm.submit()} confirmLoading={submitting} destroyOnClose>
        <Form form={pickupForm} layout="vertical" onFinish={handleCreatePickup}>
          <Form.Item name="enrollmentId" label="宝宝" rules={[{ required: true, message: '请选择宝宝' }]}>
            <Select options={enrollmentOptions} placeholder="请选择宝宝" />
          </Form.Item>
          <Form.Item name="pickupDate" label="接送日期" rules={[{ required: true, message: '请选择接送日期' }]}>
            <DatePicker style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="pickupPersonName" label="接送人姓名" rules={[{ required: true, message: '请输入接送人姓名' }]}>
            <Input placeholder="请输入接送人姓名" />
          </Form.Item>
          <Form.Item name="pickupRelationship" label="与宝宝关系">
            <Select allowClear placeholder="请选择关系" options={[
              { label: '父亲', value: 'FATHER' },
              { label: '母亲', value: 'MOTHER' },
              { label: '祖父/外祖父', value: 'GRANDFATHER' },
              { label: '祖母/外祖母', value: 'GRANDMOTHER' },
              { label: '其他', value: 'OTHER' },
            ]} />
          </Form.Item>
          <Form.Item name="pickupPhone" label="联系电话">
            <Input placeholder="请输入联系电话" />
          </Form.Item>
          <Form.Item name="reason" label="委托原因">
            <Input.TextArea rows={3} placeholder="请输入委托原因（选填）" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
}

export default ParentApplications
