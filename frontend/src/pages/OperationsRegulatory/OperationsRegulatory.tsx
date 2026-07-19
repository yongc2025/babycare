import React, { useEffect, useState } from 'react'
import { Alert, Button, Card, Col, DatePicker, Empty, Form, Input, List, message, Modal, Row, Select, Skeleton, Space, Statistic, Tabs, Tag, Typography } from 'antd'
import { AuditOutlined, DollarOutlined, HistoryOutlined, PlusOutlined, ReadOutlined, UserAddOutlined } from '@ant-design/icons'
import dayjs from 'dayjs'
import {
  admissionLeadAPI,
  billingAPI,
  classroomAPI,
  mealPlanAPI,
  organizationAPI,
  regulatoryReportAPI,
} from '../../services/api'
import type {
  AdmissionLead,
  BillingStatement,
  FeeItem,
  FeeItemForm,
  FinanceWorkbench,
  FollowUpRecord,
  MealPlan,
  Organization,
  RegulatoryExportRow,
  RegulatoryReport,
} from '../../types'
import './OperationsRegulatory.css'

const { Title, Paragraph, Text } = Typography

interface ApiResponse<T> {
  success: boolean
  data?: T
  message?: string
}

const unwrap = <T,>(response: ApiResponse<T> | T): T => {
  if (response && typeof response === 'object' && 'success' in response) {
    const apiResponse = response as ApiResponse<T>
    if (!apiResponse.success || apiResponse.data === undefined) throw new Error(apiResponse.message || '请求失败')
    return apiResponse.data
  }
  return response as T
}

const LEAD_STATUS_META: Record<string, { label: string; color: string }> = {
  NEW: { label: '新线索', color: 'blue' },
  FOLLOWING: { label: '跟进中', color: 'cyan' },
  APPLIED: { label: '已报名', color: 'geekblue' },
  APPROVED: { label: '已审核', color: 'purple' },
  REJECTED: { label: '已拒绝', color: 'red' },
  TRIALING: { label: '试托中', color: 'orange' },
  TRIAL_COMPLETED: { label: '试托结束', color: 'gold' },
  ENROLLED: { label: '已入托', color: 'green' },
  LOST: { label: '已流失', color: 'default' },
}

const OperationsRegulatory: React.FC = () => {
  const [organizations, setOrganizations] = useState<Organization[]>([])
  const [organizationId, setOrganizationId] = useState<string>()
  const [leads, setLeads] = useState<AdmissionLead[]>([])
  const [bills, setBills] = useState<BillingStatement[]>([])
  const [meals, setMeals] = useState<MealPlan[]>([])
  const [report, setReport] = useState<RegulatoryReport | null>(null)
  const [exportRows, setExportRows] = useState<RegulatoryExportRow[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const today = dayjs().format('YYYY-MM-DD')

  // T068+T069 新状态
  const [financeWorkbench, setFinanceWorkbench] = useState<FinanceWorkbench | null>(null)
  const [funnelStats, setFunnelStats] = useState<Record<string, number> | null>(null)
  const [feeItems, setFeeItems] = useState<FeeItem[]>([])

  // 跟进弹窗
  const [followUpVisible, setFollowUpVisible] = useState(false)
  const [selectedLead, setSelectedLead] = useState<AdmissionLead | null>(null)
  const [followUpContent, setFollowUpContent] = useState('')
  const [followUpNextDate, setFollowUpNextDate] = useState<string>()
  const [followUps, setFollowUps] = useState<FollowUpRecord[]>([])
  const [submittingFollowUp, setSubmittingFollowUp] = useState(false)

  // 线索转入托弹窗（T075）
  const [convertModalVisible, setConvertModalVisible] = useState(false)
  const [convertLead, setConvertLead] = useState<AdmissionLead | null>(null)
  const [convertForm] = Form.useForm()
  const [classroomOptions, setClassroomOptions] = useState<{ label: string; value: string }[]>([])

  // 收费项目弹窗
  const [feeItemVisible, setFeeItemVisible] = useState(false)
  const [editingFeeItem, setEditingFeeItem] = useState<FeeItem | null>(null)
  const [feeItemForm] = Form.useForm()
  const [submittingFeeItem, setSubmittingFeeItem] = useState(false)

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

  const loadData = async (id: string) => {
    setLoading(true)
    setError(null)

    const results = await Promise.allSettled([
      admissionLeadAPI.getOrganizationLeads(id),
      billingAPI.getOrganizationBills(id),
      mealPlanAPI.getOrganizationMeals(id, { date: today }),
      regulatoryReportAPI.getOrganizationReport(id),
      regulatoryReportAPI.getOrganizationExportRows(id),
      billingAPI.getFinanceWorkbench(id).catch(() => null),
      admissionLeadAPI.getFunnelStats(id).catch(() => null),
      billingAPI.getOrganizationFeeItems(id).catch(() => []),
      classroomAPI.getOrganizationClassrooms(id).catch(() => []),
    ])

    setLeads(results[0].status === 'fulfilled' ? unwrap<AdmissionLead[]>(results[0].value) : [])
    setBills(results[1].status === 'fulfilled' ? unwrap<BillingStatement[]>(results[1].value) : [])
    setMeals(results[2].status === 'fulfilled' ? unwrap<MealPlan[]>(results[2].value) : [])
    setReport(results[3].status === 'fulfilled' ? unwrap<RegulatoryReport>(results[3].value) : null)
    setExportRows(results[4].status === 'fulfilled' ? unwrap<RegulatoryExportRow[]>(results[4].value) : [])
    setFinanceWorkbench(results[5].status === 'fulfilled' ? unwrap<FinanceWorkbench>(results[5].value) : null)
    setFunnelStats(results[6].status === 'fulfilled' ? unwrap<Record<string, number>>(results[6].value) : null)
    setFeeItems(results[7].status === 'fulfilled' ? unwrap<FeeItem[]>(results[7].value) : [])
    setClassroomOptions(results[8].status === 'fulfilled'
      ? unwrap<{ id: string; name: string }[]>(results[8].value).map((c) => ({ label: c.name, value: c.id }))
      : [])
    setError(results.some((item) => item.status === 'rejected') ? '部分运营监管数据加载失败' : null)
    setLoading(false)
  }

  useEffect(() => {
    loadOrganizations()
  }, [])

  useEffect(() => {
    if (organizationId) loadData(organizationId)
  }, [organizationId])

  // --- 跟进操作 ---
  const openFollowUp = async (lead: AdmissionLead) => {
    setSelectedLead(lead)
    setFollowUpContent('')
    setFollowUpNextDate(undefined)
    setFollowUpVisible(true)
    try {
      const data = unwrap<FollowUpRecord[]>(await admissionLeadAPI.getFollowUps(lead.id))
      setFollowUps(data)
    } catch {
      setFollowUps([])
    }
  }

  const submitFollowUp = async () => {
    if (!selectedLead || !followUpContent.trim()) return
    setSubmittingFollowUp(true)
    try {
      await admissionLeadAPI.addFollowUp(selectedLead.id, {
        content: followUpContent.trim(),
        nextFollowUpAt: followUpNextDate || undefined,
      })
      // 刷新跟进记录
      const data = unwrap<FollowUpRecord[]>(await admissionLeadAPI.getFollowUps(selectedLead.id))
      setFollowUps(data)
      setFollowUpContent('')
      setFollowUpNextDate(undefined)
    } catch {
      // 静默处理
    } finally {
      setSubmittingFollowUp(false)
    }
  }

  // 线索转入托（T075）
  const openConvertModal = (lead: AdmissionLead) => {
    setConvertLead(lead)
    setConvertModalVisible(true)
    convertForm.resetFields()
  }

  const handleConvert = async () => {
    if (!convertLead) return
    try {
      const values = await convertForm.validateFields()
      await admissionLeadAPI.convertToEnrollment(convertLead.id, values)
      message.success('已转为入托档案')
      setConvertModalVisible(false)
      setConvertLead(null)
      // 刷新线索列表
      if (organizationId) {
        const data = unwrap<AdmissionLead[]>(await admissionLeadAPI.getOrganizationLeads(organizationId))
        setLeads(data)
      }
    } catch (err) {
      if (err instanceof Error) message.error(err.message)
    }
  }

  // --- 收费项目操作 ---
  const openFeeItemModal = (feeItem?: FeeItem) => {
    setEditingFeeItem(feeItem || null)
    feeItemForm.resetFields()
    if (feeItem) {
      feeItemForm.setFieldsValue(feeItem)
    }
    setFeeItemVisible(true)
  }

  const submitFeeItem = async () => {
    try {
      const values = await feeItemForm.validateFields()
      setSubmittingFeeItem(true)
      const data: FeeItemForm = {
        organizationId: organizationId!,
        name: values.name,
        description: values.description,
        amount: values.amount,
        status: 'ACTIVE',
      }
      if (editingFeeItem) {
        await billingAPI.updateFeeItem(editingFeeItem.id, data)
      } else {
        await billingAPI.createFeeItem(data)
      }
      setFeeItemVisible(false)
      // 刷新收费项目
      const items = unwrap<FeeItem[]>(await billingAPI.getOrganizationFeeItems(organizationId!))
      setFeeItems(items)
    } catch {
      // 静默处理
    } finally {
      setSubmittingFeeItem(false)
    }
  }

  const funnelCards = funnelStats ? [
    { label: '新线索', value: funnelStats.NEW || 0, color: 'blue' },
    { label: '跟进中', value: funnelStats.FOLLOWING || 0, color: 'cyan' },
    { label: '已报名', value: funnelStats.APPLIED || 0, color: 'geekblue' },
    { label: '试托中', value: funnelStats.TRIALING || 0, color: 'orange' },
    { label: '已入托', value: funnelStats.ENROLLED || 0, color: 'green' },
    { label: '已流失', value: funnelStats.LOST || 0, color: 'default' },
  ] : []

  const workbench = financeWorkbench

  return (
    <div className="operations-page">
      <Space direction="vertical" size={4} className="page-title">
        <Title level={2}>运营监管</Title>
        <Paragraph type="secondary">聚合招生、收费、食谱和监管导出数据。</Paragraph>
      </Space>

      {error && <Alert type="warning" showIcon message={error} />}

      <Card>
        <Select className="operations-select" value={organizationId} placeholder="选择机构"
          options={organizations.map((item) => ({ label: item.name, value: item.id }))} onChange={setOrganizationId} />
      </Card>

      {loading ? <Card><Skeleton active paragraph={{ rows: 8 }} /></Card> : (
        <Tabs
          items={[
            {
              key: 'admission',
              label: '招生',
              children: (
                <>
                  {/* 漏斗统计 */}
                  {funnelCards.length > 0 && (
                    <Row gutter={[12, 12]} style={{ marginBottom: 16 }}>
                      {funnelCards.map((item) => (
                        <Col key={item.label} xs={12} sm={8} md={4}>
                          <Card size="small">
                            <Statistic title={item.label} value={item.value}
                              valueStyle={{ color: item.color === 'default' ? undefined : item.color }} />
                          </Card>
                        </Col>
                      ))}
                    </Row>
                  )}
                  {/* 线索列表 */}
                  <Card title={<><UserAddOutlined /> 招生线索</>} extra={
                    <Text type="secondary">共 {leads.length} 条线索</Text>
                  }>
                    <List dataSource={leads} locale={{ emptyText: <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="暂无招生线索" /> }}
                      renderItem={(item) => {
                        const statusMeta = LEAD_STATUS_META[item.status] || {}
                        return (
                          <List.Item actions={[
                            <Button key="follow" type="link" size="small" icon={<HistoryOutlined />}
                              onClick={() => openFollowUp(item)}>跟进</Button>,
                            (item.status === 'APPROVED' || item.status === 'TRIAL_COMPLETED') && (
                              <Button key="convert" type="link" size="small" style={{ color: 'green' }}
                                onClick={() => openConvertModal(item)}>转为入托</Button>
                            ),
                          ].filter(Boolean)}>
                            <List.Item.Meta
                              title={<>{item.childName} · {item.guardianName} <Tag color={statusMeta.color}>{statusMeta.label}</Tag></>}
                              description={<>{item.guardianPhone} · {item.sourceDescription || item.source}</>}
                            />
                          </List.Item>
                        )
                      }} />
                  </Card>

                  {/* 跟进弹窗 */}
                  <Modal title={`跟进 - ${selectedLead?.childName || ''}`} open={followUpVisible}
                    onCancel={() => setFollowUpVisible(false)} footer={null} width={520}>
                    <Space direction="vertical" className="full-width" size={12}>
                      <Input.TextArea rows={3} placeholder="填写跟进内容" value={followUpContent}
                        onChange={(e) => setFollowUpContent(e.target.value)} />
                      <DatePicker style={{ width: '100%' }} placeholder="下次跟进时间（可选）"
                        value={followUpNextDate ? dayjs(followUpNextDate) : null}
                        onChange={(date) => setFollowUpNextDate(date?.format('YYYY-MM-DD HH:mm:ss'))} />
                      <Button type="primary" loading={submittingFollowUp} disabled={!followUpContent.trim()}
                        onClick={submitFollowUp}>提交跟进</Button>
                    </Space>
                    {followUps.length > 0 && (
                      <List header="历史跟进" dataSource={followUps} style={{ marginTop: 16 }}
                        renderItem={(fu) => (
                          <List.Item>
                            <List.Item.Meta
                              title={<Text type="secondary">{dayjs(fu.createdAt).format('MM-DD HH:mm')} · {fu.handledByName || fu.handledBy}</Text>}
                              description={<>{fu.content}{fu.nextFollowUpAt && <Tag style={{ marginLeft: 8 }}>下次: {dayjs(fu.nextFollowUpAt).format('MM-DD HH:mm')}</Tag>}</>}
                            />
                          </List.Item>
                        )} />
                    )}
                  </Modal>

                  {/* 线索转入托弹窗（T075） */}
                  <Modal title={`转为入托档案 - ${convertLead?.childName || ''}`} open={convertModalVisible}
                    onOk={handleConvert} onCancel={() => { setConvertModalVisible(false); setConvertLead(null); }} destroyOnClose>
                    <Form form={convertForm} layout="vertical">
                      <Form.Item name="classroomId" label="目标班级" rules={[{ required: true, message: '请选择目标班级' }]}>
                        <Select placeholder="选择班级" options={classroomOptions} />
                      </Form.Item>
                      <Form.Item name="enrolledAt" label="入托日期">
                        <DatePicker className="full-width" />
                      </Form.Item>
                      <Form.Item name="emergencyContactName" label="紧急联系人">
                        <Input placeholder={convertLead?.guardianName || '家长姓名'} />
                      </Form.Item>
                      <Form.Item name="emergencyContactPhone" label="紧急联系电话">
                        <Input placeholder={convertLead?.guardianPhone || '家长电话'} />
                      </Form.Item>
                      <Form.Item name="allergyNotes" label="过敏备注">
                        <Input.TextArea rows={2} />
                      </Form.Item>
                      <Form.Item name="medicalNotes" label="健康备注">
                        <Input.TextArea rows={2} />
                      </Form.Item>
                      <Form.Item name="specialCareNotes" label="特殊照护">
                        <Input.TextArea rows={2} />
                      </Form.Item>
                    </Form>
                  </Modal>
                </>
              ),
            },
            {
              key: 'billing',
              label: '收费',
              children: (
                <>
                  {/* 财务统计 */}
                  {workbench?.billingStats && (
                    <Row gutter={[12, 12]} style={{ marginBottom: 16 }}>
                      <Col xs={12} sm={8} md={4}>
                        <Card size="small"><Statistic title="总收入" value={workbench.billingStats.totalRevenue} prefix="¥" precision={2} /></Card>
                      </Col>
                      <Col xs={12} sm={8} md={4}>
                        <Card size="small"><Statistic title="待收金额" value={workbench.billingStats.unpaidAmount} prefix="¥" precision={2} valueStyle={{ color: workbench.billingStats.unpaidAmount > 0 ? '#cf1322' : undefined }} /></Card>
                      </Col>
                      <Col xs={12} sm={8} md={4}>
                        <Card size="small"><Statistic title="账单总数" value={workbench.billingStats.totalBillCount} /></Card>
                      </Col>
                      <Col xs={12} sm={8} md={4}>
                        <Card size="small"><Statistic title="已支付" value={workbench.billingStats.paidBillCount} valueStyle={{ color: '#3f8600' }} /></Card>
                      </Col>
                      <Col xs={12} sm={8} md={4}>
                        <Card size="small"><Statistic title="未支付" value={workbench.billingStats.unpaidBillCount} valueStyle={{ color: '#cf1322' }} /></Card>
                      </Col>
                      <Col xs={12} sm={8} md={4}>
                        <Card size="small"><Statistic title="收费项目" value={workbench.activeFeeItemCount ?? 0} suffix="个" /></Card>
                      </Col>
                    </Row>
                  )}
                  {/* 收费项目 */}
                  <Card title={<><DollarOutlined /> 收费项目</>} style={{ marginBottom: 16 }}
                    extra={<Button type="primary" size="small" icon={<PlusOutlined />} onClick={() => openFeeItemModal()}>新增项目</Button>}>
                    <List dataSource={feeItems} locale={{ emptyText: <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="暂无收费项目" /> }}
                      renderItem={(item) => (
                        <List.Item actions={[
                          <Button key="edit" type="link" size="small" onClick={() => openFeeItemModal(item)}>编辑</Button>,
                        ]}>
                          <List.Item.Meta title={`${item.name} · ¥${item.amount}`} description={item.description || '无描述'} />
                          <Tag color={item.status === 'ACTIVE' ? 'green' : 'default'}>{item.statusDescription || item.status}</Tag>
                        </List.Item>
                      )} />
                  </Card>
                  {/* 收费账单 */}
                  <Card title={<><DollarOutlined /> 收费账单</>}>
                    <List dataSource={bills} locale={{ emptyText: <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="暂无账单" /> }}
                      renderItem={(item) => (
                        <List.Item>
                          <List.Item.Meta title={`${item.babyName} · ${item.title}`} description={`${item.amount} 元 · 截止 ${item.dueDate || '未设置'}`} />
                          <Tag color={item.status === 'PAID' ? 'green' : 'orange'}>{item.statusDescription || item.status}</Tag>
                        </List.Item>
                      )} />
                  </Card>

                  {/* 收费项目弹窗 */}
                  <Modal title={editingFeeItem ? '编辑收费项目' : '新增收费项目'} open={feeItemVisible}
                    onCancel={() => setFeeItemVisible(false)} onOk={submitFeeItem} confirmLoading={submittingFeeItem}>
                    <Form form={feeItemForm} layout="vertical">
                      <Form.Item name="name" label="项目名称" rules={[{ required: true, message: '请输入项目名称' }]}>
                        <Input placeholder="例如：保育费、餐费" />
                      </Form.Item>
                      <Form.Item name="amount" label="金额" rules={[{ required: true, message: '请输入金额' }]}>
                        <Input type="number" prefix="¥" placeholder="0.00" />
                      </Form.Item>
                      <Form.Item name="description" label="描述">
                        <Input.TextArea rows={2} placeholder="可选描述" />
                      </Form.Item>
                    </Form>
                  </Modal>
                </>
              ),
            },
            {
              key: 'meal',
              label: '食谱',
              children: (
                <Card title={<><ReadOutlined /> 今日食谱</>}>
                  <List dataSource={meals} locale={{ emptyText: <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="暂无今日食谱" /> }}
                    renderItem={(item) => (
                      <List.Item>
                        <List.Item.Meta title={`${item.mealTypeDescription || item.mealType} · ${item.title}`} description={item.foodItems || '未填写食材'} />
                        <Tag color={item.status === 'PUBLISHED' ? 'green' : 'blue'}>{item.statusDescription || item.status}</Tag>
                      </List.Item>
                    )} />
                </Card>
              ),
            },
            {
              key: 'regulatory',
              label: '监管',
              children: (
                <Space direction="vertical" size={16} className="full-width">
                  <Card title={<><AuditOutlined /> 监管概览</>}>
                    {report ? (
                      <div className="regulatory-grid">
                        <span>班级数：{report.classroomCount}</span>
                        <span>托位：{report.totalCapacity}</span>
                        <span>在托：{report.activeEnrollmentCount}</span>
                        <span>员工：{report.staffCount}</span>
                        <span>健康观察：{report.healthObservationCount}</span>
                        <span>安全台账：{report.safetyLedgerCount}</span>
                      </div>
                    ) : <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="暂无监管概览" />}
                  </Card>
                  <Card title="导出字段">
                    <List dataSource={exportRows} locale={{ emptyText: <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="暂无导出字段" /> }}
                      renderItem={(item) => (
                        <List.Item>
                          <List.Item.Meta title={`${item.category} · ${item.fieldName}`} description={item.value || '未填写'} />
                          <Tag color={item.status === 'READY' ? 'green' : 'orange'}>{item.status}</Tag>
                        </List.Item>
                      )} />
                  </Card>
                </Space>
              ),
            },
          ]}
        />
      )}
    </div>
  )
}

export default OperationsRegulatory
