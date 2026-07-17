import React, { useEffect, useMemo, useState } from 'react'
import { Alert, Avatar, Button, Card, Empty, List, Progress, Select, Skeleton, Space, Tag, Typography } from 'antd'
import {
  AlertOutlined,
  BankOutlined,
  CalendarOutlined,
  CheckCircleOutlined,
  FileTextOutlined,
  MedicineBoxOutlined,
  RightOutlined,
  SafetyCertificateOutlined,
  TeamOutlined,
} from '@ant-design/icons'
import dayjs from 'dayjs'
import { useNavigate } from 'react-router-dom'
import { classroomAPI, directorDashboardAPI, organizationAPI } from '../../services/api'
import type { Classroom, DirectorDashboard, Organization } from '../../types'
import './Dashboard.css'

const { Title, Text } = Typography

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

const formatMoney = (value?: number) => {
  if (!value) return '0'
  return value.toLocaleString('zh-CN', { minimumFractionDigits: 0, maximumFractionDigits: 2 })
}

const Dashboard: React.FC = () => {
  const navigate = useNavigate()
  const [organizations, setOrganizations] = useState<Organization[]>([])
  const [selectedOrganizationId, setSelectedOrganizationId] = useState<string>()
  const [overview, setOverview] = useState<DirectorDashboard | null>(null)
  const [classrooms, setClassrooms] = useState<Classroom[]>([])
  const [isLoadingOrganizations, setIsLoadingOrganizations] = useState(false)
  const [isLoadingOverview, setIsLoadingOverview] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const today = dayjs().format('YYYY-MM-DD')
  const todayText = dayjs().format('YYYY年MM月DD日')

  useEffect(() => {
    const loadOrganizations = async () => {
      setIsLoadingOrganizations(true)
      setError(null)

      try {
        const data = unwrap<Organization[]>(await organizationAPI.getMyOrganizations())
        setOrganizations(data)
        setSelectedOrganizationId((current) => current || data[0]?.id)
      } catch (err) {
        setError(err instanceof Error ? err.message : '机构信息加载失败')
      } finally {
        setIsLoadingOrganizations(false)
      }
    }

    loadOrganizations()
  }, [])

  useEffect(() => {
    if (!selectedOrganizationId) {
      setOverview(null)
      setClassrooms([])
      return
    }

    const loadWorkspaceData = async () => {
      setIsLoadingOverview(true)
      setError(null)

      const [overviewResult, classroomResult] = await Promise.allSettled([
        directorDashboardAPI.getOrganizationOverview(selectedOrganizationId, today),
        classroomAPI.getOrganizationClassrooms(selectedOrganizationId),
      ])

      if (overviewResult.status === 'fulfilled') {
        setOverview(unwrap<DirectorDashboard>(overviewResult.value))
      } else {
        setOverview(null)
      }

      if (classroomResult.status === 'fulfilled') {
        setClassrooms(unwrap<Classroom[]>(classroomResult.value))
      } else {
        setClassrooms([])
      }

      const failed = [overviewResult, classroomResult].some((result) => result.status === 'rejected')
      setError(failed ? '部分工作台数据暂时无法加载，请稍后重试' : null)
      setIsLoadingOverview(false)
    }

    loadWorkspaceData()
  }, [selectedOrganizationId, today])

  const selectedOrganization = useMemo(
    () => organizations.find((item) => item.id === selectedOrganizationId),
    [organizations, selectedOrganizationId],
  )

  const expectedAttendanceCount = overview?.expectedAttendanceCount || 0
  const checkedInCount = overview?.checkedInCount || 0
  const leaveCount = overview?.leaveCount || 0
  const notArrivedCount = Math.max(expectedAttendanceCount - checkedInCount - leaveCount, 0)
  const totalCapacity = classrooms.reduce((sum, classroom) => sum + (classroom.capacity || 0), 0)
  const capacityRate = totalCapacity > 0 && overview
    ? Math.round((overview.activeEnrollmentCount / totalCapacity) * 100)
    : 0

  const dashboardStats = [
    {
      label: '在托幼儿',
      value: overview?.activeEnrollmentCount ?? 0,
      unit: '人',
      trend: `班级 ${overview?.classroomCount ?? classrooms.length} 个`,
      icon: <TeamOutlined />,
      tone: 'teal',
    },
    {
      label: '今日已到园',
      value: checkedInCount,
      unit: '人',
      trend: `出勤率 ${overview?.attendanceRate ?? 0}%`,
      icon: <CheckCircleOutlined />,
      tone: 'blue',
    },
    {
      label: '请假幼儿',
      value: leaveCount,
      unit: '人',
      trend: `未到园 ${notArrivedCount} 人`,
      icon: <CalendarOutlined />,
      tone: 'amber',
    },
    {
      label: '待处理事项',
      value: (overview?.openIncidentCount || 0) + (overview?.unpaidBillCount || 0),
      unit: '项',
      trend: `异常 ${overview?.openIncidentCount || 0} · 欠费 ${overview?.unpaidBillCount || 0}`,
      icon: <AlertOutlined />,
      tone: 'red',
    },
  ]

  const careFlows = [
    {
      name: '入园考勤',
      progress: overview?.attendanceRate || 0,
      desc: `${checkedInCount}/${expectedAttendanceCount} 已到园`,
    },
    {
      name: '请假确认',
      progress: expectedAttendanceCount > 0 ? Math.round((leaveCount / expectedAttendanceCount) * 100) : 0,
      desc: `${leaveCount} 名幼儿请假`,
    },
    {
      name: '托位使用',
      progress: capacityRate,
      desc: totalCapacity > 0 ? `${overview?.activeEnrollmentCount || 0}/${totalCapacity} 托位` : '暂无容量数据',
    },
    {
      name: '家园通知',
      progress: overview?.publishedAnnouncementCount ? 100 : 0,
      desc: `已发布 ${overview?.publishedAnnouncementCount || 0} 条通知`,
    },
  ]

  const operationAlerts = [
    overview?.openIncidentCount
      ? {
          title: '存在未关闭异常/事故',
          desc: `${overview.openIncidentCount} 条待处理`,
          color: 'red',
        }
      : null,
    overview?.unpaidBillCount
      ? {
          title: '存在未支付账单',
          desc: `${overview.unpaidBillCount} 笔，合计 ${formatMoney(overview.unpaidBillAmount)} 元`,
          color: 'orange',
        }
      : null,
    notArrivedCount
      ? {
          title: '仍有幼儿未到园',
          desc: `${notArrivedCount} 人未签到或未标记请假`,
          color: 'gold',
        }
      : null,
  ].filter(Boolean) as Array<{ title: string; desc: string; color: string }>

  const operationLinks = [
    {
      title: '机构运营',
      desc: '机构、班级、员工、入托档案',
      path: '/organization-management',
      icon: <BankOutlined />,
    },
    {
      title: '教师工作台',
      desc: '考勤、晨午检、一日照护',
      path: '/teacher-workbench',
      icon: <TeamOutlined />,
    },
    {
      title: '健康安全',
      desc: '用药、事故、安全台账',
      path: '/health-safety',
      icon: <SafetyCertificateOutlined />,
    },
    {
      title: '运营监管',
      desc: '招生、收费、食谱、监管导出',
      path: '/operations-regulatory',
      icon: <FileTextOutlined />,
    },
  ]

  if (isLoadingOrganizations) {
    return (
      <Card>
        <Skeleton active paragraph={{ rows: 8 }} />
      </Card>
    )
  }

  if (!isLoadingOrganizations && organizations.length === 0) {
    return (
      <Card>
        <Empty
          image={Empty.PRESENTED_IMAGE_SIMPLE}
          description="当前账号还没有可管理的托育机构"
        >
          <Text type="secondary">请先创建机构，再继续配置班级、员工和入托档案。</Text>
        </Empty>
      </Card>
    )
  }

  return (
    <div className="dashboard">
      <section className="dashboard-hero">
        <div>
          <Text className="dashboard-kicker">{todayText}</Text>
          <Title level={1}>园长运营工作台</Title>
          <Text className="dashboard-desc">
            面向托育机构日常经营，集中查看今日出勤、托位容量、照护进度、异常安全和收费风险。
          </Text>
        </div>
        <Space className="dashboard-actions" wrap>
          <Select className="organization-select" value={selectedOrganizationId} onChange={setSelectedOrganizationId}
            options={organizations.map((item) => ({ label: item.name, value: item.id }))} />
          <Button type="primary" icon={<FileTextOutlined />} onClick={() => navigate('/parent-reports')}>
            查看家长日报
          </Button>
        </Space>
      </section>

      {error && <Alert type="warning" showIcon message={error} />}

      <Card className="workspace-card">
        <div className="workspace-card-inner">
          <Space direction="vertical" size={4}>
            <Text type="secondary">当前机构</Text>
            <Space wrap>
              <Tag color="cyan" icon={<BankOutlined />}>
                {selectedOrganization?.name || '未选择机构'}
              </Tag>
              {selectedOrganization?.contactPhone && <Tag>{selectedOrganization.contactPhone}</Tag>}
              {selectedOrganization?.address && <Tag>{selectedOrganization.address}</Tag>}
            </Space>
          </Space>
          <Space wrap>
            {operationLinks.map((item) => (
              <Button key={item.path} onClick={() => navigate(item.path)}>
                {item.title}
              </Button>
            ))}
          </Space>
        </div>
      </Card>

      <section className="operation-link-grid">
        {operationLinks.map((item) => (
          <button className="operation-link" key={item.path} type="button" onClick={() => navigate(item.path)}>
            <span className="operation-link-icon">{item.icon}</span>
            <span>
              <strong>{item.title}</strong>
              <em>{item.desc}</em>
            </span>
            <RightOutlined />
          </button>
        ))}
      </section>

      {isLoadingOverview ? (
        <Card>
          <Skeleton active paragraph={{ rows: 10 }} />
        </Card>
      ) : (
        <>
          <section className="stat-grid">
            {dashboardStats.map((item) => (
              <Card className={`stat-card stat-card-${item.tone}`} key={item.label}>
                <div className="stat-card-icon">{item.icon}</div>
                <Text className="stat-label">{item.label}</Text>
                <div className="stat-value">
                  {item.value}
                  <span>{item.unit}</span>
                </div>
                <Text className="stat-trend">{item.trend}</Text>
              </Card>
            ))}
          </section>

          <section className="dashboard-grid">
            <Card title="今日运营流程">
              <Space direction="vertical" size={18} className="full-width">
                {careFlows.map((item) => (
                  <div className="care-flow" key={item.name}>
                    <div className="care-flow-header">
                      <span>{item.name}</span>
                      <Text type="secondary">{item.desc}</Text>
                    </div>
                    <Progress percent={item.progress} showInfo={false} />
                  </div>
                ))}
              </Space>
            </Card>

            <Card
              title="班级概览"
              extra={
                <Button type="link" icon={<RightOutlined />} onClick={() => navigate('/organization-management')}>
                  进入管理
                </Button>
              }
            >
              <List
                className="class-list"
                dataSource={classrooms}
                locale={{
                  emptyText: <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="暂无班级数据" />,
                }}
                renderItem={(item) => (
                  <List.Item>
                    <List.Item.Meta
                      avatar={<Avatar className="class-avatar">{item.name[0]}</Avatar>}
                      title={item.name}
                      description={[
                        item.ageRangeMinMonths !== undefined && item.ageRangeMaxMonths !== undefined
                          ? `${item.ageRangeMinMonths}-${item.ageRangeMaxMonths} 月龄`
                          : '未配置月龄段',
                        item.capacity ? `容量 ${item.capacity} 人` : '未配置容量',
                      ].join(' · ')}
                    />
                    <Tag color={item.status === 'ACTIVE' ? 'green' : 'default'}>
                      {item.statusDescription || item.status}
                    </Tag>
                  </List.Item>
                )}
              />
            </Card>
          </section>

          <section className="dashboard-grid">
            <Card title={<><AlertOutlined /> 运营提醒</>}>
              <List
                dataSource={operationAlerts}
                locale={{
                  emptyText: <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="暂无待处理提醒" />,
                }}
                renderItem={(item) => (
                  <List.Item>
                    <List.Item.Meta
                      avatar={<AlertOutlined className="task-icon" />}
                      title={item.title}
                      description={item.desc}
                    />
                    <Tag color={item.color}>待处理</Tag>
                  </List.Item>
                )}
              />
            </Card>

            <Card title={<><MedicineBoxOutlined /> 数据完整度</>}>
              <Space direction="vertical" size={14} className="full-width">
                <Alert
                  type={overview ? 'success' : 'info'}
                  showIcon
                  message={overview ? '园长驾驶舱数据已接入' : '暂无园长驾驶舱数据'}
                  description="当前首页只展示真实接口返回的数据；未接入的录入页面会标记为待接入。"
                />
                <div className="data-health-row">
                  <span>机构</span>
                  <Tag color="green">{organizations.length} 个</Tag>
                </div>
                <div className="data-health-row">
                  <span>班级</span>
                  <Tag color={classrooms.length > 0 ? 'green' : 'orange'}>{classrooms.length} 个</Tag>
                </div>
              </Space>
            </Card>
          </section>
        </>
      )}
    </div>
  )
}

export default Dashboard
