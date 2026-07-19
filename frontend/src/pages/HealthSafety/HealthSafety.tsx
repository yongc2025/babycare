import React, { useEffect, useState } from 'react'
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
  Skeleton,
  Space,
  Statistic,
  Switch,
  Tabs,
  Tag,
  Typography,
  message,
} from 'antd'
import { AlertOutlined, BugOutlined, MedicineBoxOutlined, PlusOutlined, SafetyCertificateOutlined, WarningOutlined } from '@ant-design/icons'
import dayjs from 'dayjs'
import {
  classroomAPI,
  enrollmentAPI,
  healthObservationAPI,
  incidentReportAPI,
  infectiousDiseaseAPI,
  mealPlanAPI,
  medicationCareAPI,
  organizationAPI,
  safetyLedgerAPI,
} from '../../services/api'
import type { AllergyTag, Classroom, Enrollment, HealthObservation, InfectiousDisease, IncidentReport, MealPlan, MedicationRequest, Organization, SafetyLedger } from '../../types'
import NutritionAnalysis from './NutritionAnalysis'
import SafetyLedgerTab from './SafetyLedgerTab'
import './HealthSafety.css'
const { Title, Paragraph } = Typography

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

const HealthSafety: React.FC = () => {
  const [organizations, setOrganizations] = useState<Organization[]>([])
  const [organizationId, setOrganizationId] = useState<string>()
  const [classrooms, setClassrooms] = useState<Classroom[]>([])
  const [classroomId, setClassroomId] = useState<string>()
  const [enrollments, setEnrollments] = useState<Enrollment[]>([])
  const [healthRecords, setHealthRecords] = useState<HealthObservation[]>([])
  const [medications, setMedications] = useState<MedicationRequest[]>([])
  const [incidents, setIncidents] = useState<IncidentReport[]>([])
  const [ledgers, setLedgers] = useState<SafetyLedger[]>([])
  const [allergies, setAllergies] = useState<AllergyTag[]>([])
  const [mealPlans, setMealPlans] = useState<MealPlan[]>([])
  const [infectiousDiseases, setInfectiousDiseases] = useState<InfectiousDisease[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [modal, setModal] = useState<'health' | 'medication' | 'incident' | 'ledger' | 'allergy' | 'infectious' | null>(null)
  const [healthForm] = Form.useForm()
  const [medicationForm] = Form.useForm()
  const [incidentForm] = Form.useForm()
  const [ledgerForm] = Form.useForm()
  const [allergyForm] = Form.useForm()
  const [infectiousForm] = Form.useForm()
  const today = dayjs().format('YYYY-MM-DD')

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

  const loadData = async () => {
    if (!organizationId || !classroomId) return
    setLoading(true)
    setError(null)
    const results = await Promise.allSettled([
      enrollmentAPI.getClassroomEnrollments(classroomId),
      healthObservationAPI.getClassroomObservations(classroomId, today),
      medicationCareAPI.getClassroomMedications(classroomId),
      incidentReportAPI.getClassroomReports(classroomId),
      safetyLedgerAPI.getOrganizationLedgers(organizationId, { startDate: today, endDate: today }),
      mealPlanAPI.getOrganizationMeals(organizationId, { date: today }),
      infectiousDiseaseAPI.getClassroomRecords(classroomId),
    ])
    const enrolls = results[0].status === 'fulfilled' ? unwrap<Enrollment[]>(results[0].value) : []
    setEnrollments(enrolls)
    setHealthRecords(results[1].status === 'fulfilled' ? unwrap<HealthObservation[]>(results[1].value) : [])
    setMedications(results[2].status === 'fulfilled' ? unwrap<MedicationRequest[]>(results[2].value) : [])
    setIncidents(results[3].status === 'fulfilled' ? unwrap<IncidentReport[]>(results[3].value) : [])
    setLedgers(results[4].status === 'fulfilled' ? unwrap<SafetyLedger[]>(results[4].value) : [])
    setMealPlans(results[5].status === 'fulfilled' ? unwrap<MealPlan[]>(results[5].value) : [])
    setInfectiousDiseases(results[6].status === 'fulfilled' ? unwrap<InfectiousDisease[]>(results[6].value) : [])

    // 并行获取各班宝宝的过敏标签
    if (enrolls.length > 0) {
      const allergyResults = await Promise.allSettled(
        enrolls.map((enr) => medicationCareAPI.getBabyAllergies(enr.babyId)),
      )
      const allAllergies: AllergyTag[] = []
      allergyResults.forEach((r) => {
        if (r.status === 'fulfilled') {
          const list = unwrap<AllergyTag[]>(r.value)
          allAllergies.push(...list)
        }
      })
      setAllergies(allAllergies)
    }
    setError(results.some((item) => item.status === 'rejected') ? '部分健康安全数据加载失败' : null)
    setLoading(false)
  }

  useEffect(() => {
    loadOrganizations()
  }, [])

  useEffect(() => {
    if (organizationId) loadClassrooms(organizationId)
  }, [organizationId])

  useEffect(() => {
    loadData()
  }, [organizationId, classroomId])

  const closeIncident = async (id: string) => {
    await incidentReportAPI.closeReport(id)
    message.success('事故记录已关闭')
    loadData()
  }

  const handleCreateHealth = async () => {
    const values = await healthForm.validateFields()
    await healthObservationAPI.createObservation({
      ...values,
      observationDate: today,
      observationTime: dayjs().format('YYYY-MM-DDTHH:mm:ss'),
      source: 'HEALTH_SAFETY',
    })
    message.success('健康观察已新增')
    healthForm.resetFields()
    setModal(null)
    loadData()
  }

  const handleCreateMedication = async () => {
    const values = await medicationForm.validateFields()
    await medicationCareAPI.createMedicationRequest({
      ...values,
      startDate: values.startDate?.format('YYYY-MM-DD'),
      endDate: values.endDate?.format('YYYY-MM-DD'),
    })
    message.success('用药委托已新增')
    medicationForm.resetFields()
    setModal(null)
    loadData()
  }

  const handleCreateIncident = async () => {
    const values = await incidentForm.validateFields()
    await incidentReportAPI.createReport({
      ...values,
      occurredAt: values.occurredAt?.format('YYYY-MM-DDTHH:mm:ss'),
    })
    message.success('事故记录已新增')
    incidentForm.resetFields()
    setModal(null)
    loadData()
  }

  const handleCreateLedger = async () => {
    const values = await ledgerForm.validateFields()
    await safetyLedgerAPI.createLedger({
      ...values,
      organizationId,
      ledgerDate: values.ledgerDate?.format('YYYY-MM-DD'),
      dueAt: values.dueAt?.format('YYYY-MM-DDTHH:mm:ss'),
    })
    message.success('安全台账已新增')
    ledgerForm.resetFields()
    setModal(null)
    loadData()
  }

  const handleCreateAllergy = async () => {
    const values = await allergyForm.validateFields()
    await medicationCareAPI.createAllergyTag(values)
    message.success('过敏标签已新增')
    allergyForm.resetFields()
    setModal(null)
    loadData()
  }

  const handleCreateInfectious = async () => {
    const values = await infectiousForm.validateFields()
    await infectiousDiseaseAPI.createRecord({
      ...values,
      onsetDate: values.onsetDate?.format('YYYY-MM-DD'),
      organizationId,
      classroomId,
    })
    message.success('传染病记录已新增')
    infectiousForm.resetFields()
    setModal(null)
    loadData()
  }

  const enrollmentOptions = enrollments.map((item) => ({ label: item.babyName, value: item.id }))

  return (
    <div className="health-safety-page">
      <Space direction="vertical" size={4} className="page-title">
        <Title level={2}>保健工作台</Title>
        <Paragraph type="secondary">聚合晨午检、用药、过敏、食谱、台账和异常事件。</Paragraph>
      </Space>
      {error && <Alert type="warning" showIcon banner message={error} />}
      <div className="health-summary-cards">
        <Card className="health-summary-card" size="small">
          <Statistic title="今日异常观察" value={healthRecords.filter((r) => r.abnormal).length} valueStyle={{ color: '#cf1322' }} prefix={<WarningOutlined />} />
        </Card>
        <Card className="health-summary-card" size="small">
          <Statistic title="待处理用药" value={medications.filter((r) => r.status === 'PENDING').length} valueStyle={{ color: '#faad14' }} />
        </Card>
        <Card className="health-summary-card" size="small">
          <Statistic title="未关闭事故" value={incidents.filter((r) => r.status !== 'CLOSED').length} valueStyle={{ color: '#cf1322' }} />
        </Card>
        <Card className="health-summary-card" size="small">
          <Statistic title="未关闭台账" value={ledgers.filter((r) => r.status !== 'CLOSED').length} valueStyle={{ color: '#1890ff' }} />
        </Card>
        <Card className="health-summary-card" size="small">
          <Statistic title="今日餐次" value={mealPlans.length} valueStyle={{ color: '#52c41a' }} />
        </Card>
      </div>

      <Card>
        <Space wrap>
          <Select className="health-select" value={organizationId} placeholder="选择机构"
            options={organizations.map((item) => ({ label: item.name, value: item.id }))} onChange={setOrganizationId} />
          <Select className="health-select" value={classroomId} placeholder="选择班级"
            options={classrooms.map((item) => ({ label: item.name, value: item.id }))} onChange={setClassroomId} />
          <Button disabled={!classroomId || enrollments.length === 0} icon={<PlusOutlined />} onClick={() => setModal('health')}>新增晨午检</Button>
          <Button disabled={!classroomId || enrollments.length === 0} icon={<PlusOutlined />} onClick={() => setModal('medication')}>新增用药</Button>
          <Button disabled={!classroomId || enrollments.length === 0} icon={<PlusOutlined />} onClick={() => setModal('allergy')}>新增过敏</Button>
          <Button disabled={!classroomId || enrollments.length === 0} icon={<PlusOutlined />} onClick={() => setModal('incident')}>新增事故</Button>
          <Button disabled={!classroomId || enrollments.length === 0} icon={<PlusOutlined />} onClick={() => setModal('infectious')}>新增传染病</Button>
          <Button disabled={!organizationId} icon={<PlusOutlined />} onClick={() => setModal('ledger')}>新增台账</Button>
          <Button onClick={loadData}>刷新</Button>
        </Space>
      </Card>

      {loading ? <Card><Skeleton active paragraph={{ rows: 8 }} /></Card> : (
        <Tabs
          items={[
            {
              key: 'health',
              label: '晨午检',
              children: (
                <Card title={<><MedicineBoxOutlined /> 今日健康观察</>}>
                  <List dataSource={healthRecords} locale={{ emptyText: <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="暂无健康观察记录" /> }}
                    renderItem={(item) => (
                      <List.Item>
                        <List.Item.Meta title={item.babyName} description={`${item.typeDescription || item.type} · 体温 ${item.temperature ?? '-'} · ${item.symptoms || '无症状备注'}`} />
                        <Tag color={item.abnormal ? 'red' : 'green'}>{item.abnormal ? '异常' : '正常'}</Tag>
                      </List.Item>
                    )} />
                </Card>
              ),
            },
            {
              key: 'medication',
              label: '用药',
              children: (
                <Card title={<><MedicineBoxOutlined /> 用药委托</>}>
                  <List dataSource={medications} locale={{ emptyText: <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="暂无用药委托" /> }}
                    renderItem={(item) => (
                      <List.Item>
                        <List.Item.Meta title={`${item.babyName} · ${item.medicineName}`} description={`${item.startDate} 至 ${item.endDate} · ${item.dosage || '未填剂量'}`} />
                        <Tag color={item.status === 'APPROVED' ? 'green' : 'orange'}>{item.statusDescription || item.status}</Tag>
                      </List.Item>
                    )} />
                </Card>
              ),
            },
            {
              key: 'allergy',
              label: '过敏',
              children: (
                <Card title={<><WarningOutlined /> 过敏标签</>}>
                  <List dataSource={allergies} locale={{ emptyText: <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="暂无过敏标签" /> }}
                    renderItem={(item) => (
                      <List.Item>
                        <List.Item.Meta title={`${item.babyName}`} description={`${item.allergen} · ${item.reaction || '未描述反应'}`} />
                        <Space>
                          <Tag color={item.severity === 'SEVERE' ? 'red' : item.severity === 'MODERATE' ? 'orange' : 'blue'}>{item.severityDescription || item.severity}</Tag>
                          <Tag color={item.status === 'ACTIVE' ? 'red' : 'default'}>{item.statusDescription || item.status}</Tag>
                        </Space>
                      </List.Item>
                    )} />
                </Card>
              ),
            },
            {
              key: 'meal',
              label: '食谱',
              children: (
                <Card title={<><SafetyCertificateOutlined /> 今日食谱</>}>
                  <List dataSource={mealPlans} locale={{ emptyText: <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="暂无今日食谱" /> }}
                    renderItem={(item) => (
                      <List.Item>
                        <List.Item.Meta title={`${item.mealTypeDescription || item.mealType} · ${item.title}`}
                          description={`${item.foodItems || '未填写餐品'} ${item.allergenNotes ? `⚠️ ${item.allergenNotes}` : ''}`} />
                        <Tag color={item.status === 'PUBLISHED' ? 'green' : 'default'}>{item.statusDescription || item.status}</Tag>
                      </List.Item>
                    )} />
                </Card>
              ),
            },
            {
              key: 'nutrition',
              label: '营养分析',
              children: <NutritionAnalysis organizationId={organizationId} />,
            },
            {
              key: 'infectious',
              label: '传染病',
              children: (
                <Card title={<><BugOutlined /> 传染病防控记录</>}>
                  <List dataSource={infectiousDiseases} locale={{ emptyText: <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="暂无传染病记录" /> }}
                    renderItem={(item) => (
                      <List.Item>
                        <List.Item.Meta title={`${item.babyName} · ${item.diseaseName}`} description={item.symptoms || ''} />
                        <Tag color={item.severity === 'SEVERE' ? 'red' : item.severity === 'MODERATE' ? 'orange' : 'blue'}>{item.severityDescription || item.severity}</Tag>
                        <Tag color={item.status === 'RETURNED' ? 'green' : item.status === 'CONFIRMED' || item.status === 'ISOLATED' ? 'red' : 'orange'}>{item.statusDescription || item.status}</Tag>
                      </List.Item>
                    )} />
                </Card>
              ),
            },
            {
              key: 'incident',
              label: '事故',
              children: (
                <Card title={<><AlertOutlined /> 异常/事故</>}>
                  <List dataSource={incidents} locale={{ emptyText: <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="暂无事故记录" /> }}
                    renderItem={(item) => (
                      <List.Item actions={[item.status !== 'CLOSED' && <Button type="link" onClick={() => closeIncident(item.id)}>关闭</Button>]}>
                        <List.Item.Meta title={`${item.babyName} · ${item.title}`} description={`${item.severityDescription || item.severity} · ${item.occurredAt ? dayjs(item.occurredAt).format('YYYY-MM-DD HH:mm') : ''}`} />
                        <Tag color={item.status === 'CLOSED' ? 'green' : 'red'}>{item.statusDescription || item.status}</Tag>
                      </List.Item>
                    )} />
                </Card>
              ),
            },
            {
              key: 'ledger',
              label: '台账',
              children: <SafetyLedgerTab organizationId={organizationId} />,
            },
          ]}
        />
      )}

      <Modal title="新增晨午检" open={modal === 'health'} onOk={handleCreateHealth} onCancel={() => setModal(null)} destroyOnClose>
        <Form form={healthForm} layout="vertical" initialValues={{ type: 'MORNING_CHECK', abnormal: false, followUpRequired: false }}>
          <Form.Item name="enrollmentId" label="宝宝" rules={[{ required: true, message: '请选择宝宝' }]}><Select options={enrollmentOptions} /></Form.Item>
          <Form.Item name="type" label="观察类型" rules={[{ required: true, message: '请选择观察类型' }]}>
            <Select options={[{ label: '晨检', value: 'MORNING_CHECK' }, { label: '午检', value: 'NOON_CHECK' }, { label: '全日观察', value: 'FULL_DAY_OBSERVATION' }]} />
          </Form.Item>
          <Form.Item name="temperature" label="体温"><InputNumber className="full-width" min={34} max={42} step={0.1} /></Form.Item>
          <Form.Item name="symptoms" label="症状描述"><Input.TextArea rows={3} /></Form.Item>
          <Form.Item name="actionTaken" label="处理建议"><Input.TextArea rows={2} /></Form.Item>
          <Form.Item name="abnormal" label="是否异常" valuePropName="checked"><Switch /></Form.Item>
          <Form.Item name="followUpRequired" label="需要跟进" valuePropName="checked"><Switch /></Form.Item>
        </Form>
      </Modal>
      <Modal title="新增用药委托" open={modal === 'medication'} onOk={handleCreateMedication} onCancel={() => setModal(null)} destroyOnClose>
        <Form form={medicationForm} layout="vertical">
          <Form.Item name="enrollmentId" label="宝宝" rules={[{ required: true, message: '请选择宝宝' }]}><Select options={enrollmentOptions} /></Form.Item>
          <Form.Item name="medicineName" label="药品名称" rules={[{ required: true, message: '请输入药品名称' }]}><Input /></Form.Item>
          <Form.Item name="dosage" label="剂量"><Input /></Form.Item>
          <Form.Item name="frequency" label="频次"><Input placeholder="例如：每日2次" /></Form.Item>
          <Form.Item name="startDate" label="开始日期" rules={[{ required: true, message: '请选择开始日期' }]}><DatePicker className="full-width" /></Form.Item>
          <Form.Item name="endDate" label="结束日期" rules={[{ required: true, message: '请选择结束日期' }]}><DatePicker className="full-width" /></Form.Item>
          <Form.Item name="instructions" label="用药说明"><Input.TextArea rows={3} /></Form.Item>
        </Form>
      </Modal>
      <Modal title="新增事故记录" open={modal === 'incident'} onOk={handleCreateIncident} onCancel={() => setModal(null)} destroyOnClose>
        <Form form={incidentForm} layout="vertical" initialValues={{ type: 'HEALTH_ABNORMAL', severity: 'LOW', occurredAt: dayjs() }}>
          <Form.Item name="enrollmentId" label="宝宝" rules={[{ required: true, message: '请选择宝宝' }]}><Select options={enrollmentOptions} /></Form.Item>
          <Form.Item name="type" label="事件类型" rules={[{ required: true, message: '请选择事件类型' }]}>
            <Select options={[{ label: '健康异常', value: 'HEALTH_ABNORMAL' }, { label: '伤害', value: 'INJURY' }, { label: '安全事件', value: 'SAFETY_EVENT' }, { label: '行为事件', value: 'BEHAVIOR' }, { label: '其他', value: 'OTHER' }]} />
          </Form.Item>
          <Form.Item name="severity" label="严重程度"><Select options={[{ label: '低', value: 'LOW' }, { label: '中', value: 'MEDIUM' }, { label: '高', value: 'HIGH' }, { label: '紧急', value: 'CRITICAL' }]} /></Form.Item>
          <Form.Item name="occurredAt" label="发生时间"><DatePicker showTime className="full-width" /></Form.Item>
          <Form.Item name="title" label="标题" rules={[{ required: true, message: '请输入标题' }]}><Input /></Form.Item>
          <Form.Item name="location" label="地点"><Input /></Form.Item>
          <Form.Item name="description" label="事件描述"><Input.TextArea rows={3} /></Form.Item>
          <Form.Item name="handlingProcess" label="处理过程"><Input.TextArea rows={3} /></Form.Item>
        </Form>
      </Modal>
      <Modal title="新增安全卫生台账" open={modal === 'ledger'} onOk={handleCreateLedger} onCancel={() => setModal(null)} destroyOnClose>
        <Form form={ledgerForm} layout="vertical" initialValues={{ ledgerDate: dayjs(), ledgerType: 'FACILITY_INSPECTION', status: 'OPEN' }}>
          <Form.Item name="ledgerDate" label="台账日期" rules={[{ required: true, message: '请选择台账日期' }]}><DatePicker className="full-width" /></Form.Item>
          <Form.Item name="ledgerType" label="台账类型" rules={[{ required: true, message: '请选择台账类型' }]}>
            <Select options={[{ label: '消毒', value: 'DISINFECTION' }, { label: '食品留样', value: 'FOOD_SAMPLE' }, { label: '设施巡检', value: 'FACILITY_INSPECTION' }, { label: '消防安全', value: 'FIRE_SAFETY' }, { label: '安全教育', value: 'SAFETY_EDUCATION' }, { label: '事故跟进', value: 'INCIDENT_FOLLOWUP' }, { label: '其他', value: 'OTHER' }]} />
          </Form.Item>
          <Form.Item name="title" label="标题" rules={[{ required: true, message: '请输入标题' }]}><Input /></Form.Item>
          <Form.Item name="location" label="位置"><Input /></Form.Item>
          <Form.Item name="responsiblePerson" label="责任人"><Input /></Form.Item>
          <Form.Item name="dueAt" label="截止时间"><DatePicker showTime className="full-width" /></Form.Item>
          <Form.Item name="content" label="内容"><Input.TextArea rows={3} /></Form.Item>
        </Form>
      </Modal>
      <Modal title="新增过敏标签" open={modal === 'allergy'} onOk={handleCreateAllergy} onCancel={() => { setModal(null); allergyForm.resetFields(); }} destroyOnClose>
        <Form form={allergyForm} layout="vertical" initialValues={{ severity: 'MILD', status: 'ACTIVE' }}>
          <Form.Item name="enrollmentId" label="宝宝" rules={[{ required: true, message: '请选择宝宝' }]}><Select options={enrollmentOptions} /></Form.Item>
          <Form.Item name="allergen" label="过敏源" rules={[{ required: true, message: '请输入过敏源' }]}><Input /></Form.Item>
          <Form.Item name="reaction" label="过敏反应"><Input /></Form.Item>
          <Form.Item name="severity" label="严重程度"><Select options={[{ label: '轻微', value: 'MILD' }, { label: '中等', value: 'MODERATE' }, { label: '严重', value: 'SEVERE' }]} /></Form.Item>
        </Form>
      </Modal>
      <Modal title="新增传染病记录" open={modal === 'infectious'} onOk={handleCreateInfectious} onCancel={() => { setModal(null); infectiousForm.resetFields(); }} destroyOnClose>
        <Form form={infectiousForm} layout="vertical" initialValues={{ status: 'SUSPECTED', severity: 'MILD', parentNotified: false, classroomAlertSent: false }}>
          <Form.Item name="enrollmentId" label="宝宝" rules={[{ required: true, message: '请选择宝宝' }]}><Select options={enrollmentOptions} /></Form.Item>
          <Form.Item name="diseaseName" label="疾病名称" rules={[{ required: true, message: '请输入疾病名称' }]}><Input placeholder="例如：手足口病、流感" /></Form.Item>
          <Form.Item name="symptoms" label="症状描述"><Input.TextArea rows={2} /></Form.Item>
          <Form.Item name="onsetDate" label="发病日期" rules={[{ required: true, message: '请选择发病日期' }]}><DatePicker className="full-width" /></Form.Item>
          <Form.Item name="status" label="当前状态"><Select options={[
            { label: '疑似', value: 'SUSPECTED' }, { label: '确诊', value: 'CONFIRMED' }, { label: '隔离', value: 'ISOLATED' },
            { label: '康复', value: 'RECOVERED' }, { label: '复园', value: 'RETURNED' },
          ]} /></Form.Item>
          <Form.Item name="severity" label="严重程度"><Select options={[{ label: '轻', value: 'MILD' }, { label: '中', value: 'MODERATE' }, { label: '重', value: 'SEVERE' }]} /></Form.Item>
          <Form.Item name="treatmentNotes" label="处理记录"><Input.TextArea rows={2} /></Form.Item>
          <Form.Item name="closeContacts" label="密切接触者"><Input.TextArea rows={2} placeholder="记录密切接触的宝宝或员工" /></Form.Item>
          <Form.Item name="parentNotified" label="已通知家长" valuePropName="checked"><Switch /></Form.Item>
          <Form.Item name="classroomAlertSent" label="已发送班级提醒" valuePropName="checked"><Switch /></Form.Item>
        </Form>
      </Modal>
    </div>
  )
}

export default HealthSafety
