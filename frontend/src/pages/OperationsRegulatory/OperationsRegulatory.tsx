import React, { useEffect, useState } from 'react'
import { Alert, Card, Empty, List, Select, Skeleton, Space, Tabs, Tag, Typography } from 'antd'
import { AuditOutlined, DollarOutlined, ReadOutlined, UserAddOutlined } from '@ant-design/icons'
import dayjs from 'dayjs'
import {
  admissionLeadAPI,
  billingAPI,
  mealPlanAPI,
  organizationAPI,
  regulatoryReportAPI,
} from '../../services/api'
import type {
  AdmissionLead,
  BillingStatement,
  MealPlan,
  Organization,
  RegulatoryExportRow,
  RegulatoryReport,
} from '../../types'
import './OperationsRegulatory.css'

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
    ])

    setLeads(results[0].status === 'fulfilled' ? unwrap<AdmissionLead[]>(results[0].value) : [])
    setBills(results[1].status === 'fulfilled' ? unwrap<BillingStatement[]>(results[1].value) : [])
    setMeals(results[2].status === 'fulfilled' ? unwrap<MealPlan[]>(results[2].value) : [])
    setReport(results[3].status === 'fulfilled' ? unwrap<RegulatoryReport>(results[3].value) : null)
    setExportRows(results[4].status === 'fulfilled' ? unwrap<RegulatoryExportRow[]>(results[4].value) : [])
    setError(results.some((item) => item.status === 'rejected') ? '部分运营监管数据加载失败' : null)
    setLoading(false)
  }

  useEffect(() => {
    loadOrganizations()
  }, [])

  useEffect(() => {
    if (organizationId) loadData(organizationId)
  }, [organizationId])

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
                <Card title={<><UserAddOutlined /> 招生线索</>}>
                  <List dataSource={leads} locale={{ emptyText: <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="暂无招生线索" /> }}
                    renderItem={(item) => (
                      <List.Item>
                        <List.Item.Meta title={`${item.childName} · ${item.guardianName}`} description={`${item.guardianPhone} · ${item.sourceDescription || item.source}`} />
                        <Tag color={item.intentionLevel === 'HIGH' ? 'red' : 'blue'}>{item.statusDescription || item.status}</Tag>
                      </List.Item>
                    )} />
                </Card>
              ),
            },
            {
              key: 'billing',
              label: '收费',
              children: (
                <Card title={<><DollarOutlined /> 收费账单</>}>
                  <List dataSource={bills} locale={{ emptyText: <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="暂无账单" /> }}
                    renderItem={(item) => (
                      <List.Item>
                        <List.Item.Meta title={`${item.babyName} · ${item.title}`} description={`${item.amount} 元 · 截止 ${item.dueDate || '未设置'}`} />
                        <Tag color={item.status === 'PAID' ? 'green' : 'orange'}>{item.statusDescription || item.status}</Tag>
                      </List.Item>
                    )} />
                </Card>
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
