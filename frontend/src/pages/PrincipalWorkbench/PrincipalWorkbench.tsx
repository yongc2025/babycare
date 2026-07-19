import React, { useEffect, useState } from 'react'
import { Card, Col, Row, Statistic, List, Tag, Typography, Spin, Empty, Alert, Badge } from 'antd'
import {
  AlertOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
  ClusterOutlined,
  ExclamationCircleOutlined,
  FileTextOutlined,
  TeamOutlined,
  UserOutlined,
  WalletOutlined,
} from '@ant-design/icons'
import dayjs from 'dayjs'
import { directorDashboardAPI } from '../../services/api'
import type { DirectorWorkbench } from '../../types'

const { Title, Text } = Typography

const formatMoney = (value?: number) => {
  if (!value) return '0.00'
  return value.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

const PrincipalWorkbench: React.FC = () => {
  const [data, setData] = useState<DirectorWorkbench | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    // 尝试获取用户管理的第一个机构的园长工作台数据
    // 实际应用中应从用户角色/Staff 关联获取机构ID
    setLoading(true)
    setError(null)

    // 通过 /organization/my-organizations 获取用户机构列表
    import('../../services/api').then(({ organizationAPI }) => {
      organizationAPI.getMyOrganizations()
        .then((res: any) => {
          const orgs = res?.data ?? res
          if (Array.isArray(orgs) && orgs.length > 0) {
            const orgId = orgs[0].id
            return directorDashboardAPI.getWorkbench(orgId)
          }
          throw new Error('暂无关联机构')
        })
        .then((res: any) => {
          const d = res?.data ?? res
          setData(d)
        })
        .catch((err: any) => {
          setError(err?.message || '加载工作台数据失败')
        })
        .finally(() => setLoading(false))
    })
  }, [])

  if (loading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: 400 }}>
        <Spin size="large" tip="加载中..." />
      </div>
    )
  }

  if (error) {
    return (
      <div style={{ padding: 24 }}>
        <Alert message="数据加载失败" description={error} type="error" showIcon />
      </div>
    )
  }

  if (!data) {
    return (
      <div style={{ padding: 24 }}>
        <Empty description="暂无数据" />
      </div>
    )
  }

  const severityColor = (severity: string) => {
    switch (severity) {
      case 'HIGH': return 'red'
      case 'MEDIUM': return 'orange'
      case 'LOW': return 'blue'
      default: return 'default'
    }
  }

  const todoTypeIcon = (type: string) => {
    switch (type) {
      case 'LEAVE_APPROVAL': return <FileTextOutlined style={{ color: '#faad14' }} />
      case 'INCIDENT_HANDLE': return <AlertOutlined style={{ color: '#ff4d4f' }} />
      case 'BILL_REMIND': return <WalletOutlined style={{ color: '#faad14' }} />
      case 'ENROLLMENT_REVIEW': return <TeamOutlined style={{ color: '#1890ff' }} />
      default: return <ExclamationCircleOutlined />
    }
  }

  const riskIcon = (type: string) => {
    switch (type) {
      case 'LOW_ATTENDANCE': return <CloseCircleOutlined />
      case 'OPEN_INCIDENT': return <AlertOutlined />
      case 'HIGH_ABSENTEEISM': return <ExclamationCircleOutlined />
      case 'UNPAID_BILLS': return <WalletOutlined />
      default: return <ExclamationCircleOutlined />
    }
  }

  return (
    <div style={{ padding: 24 }}>
      <Title level={3}>
        <ClusterOutlined /> 园长工作台
      </Title>
      <Text type="secondary" style={{ fontSize: 14, display: 'block', marginBottom: 16 }}>
        {data.organizationName} · {dayjs(data.date).format('YYYY年MM月DD日')}
      </Text>

      {/* 概览指标卡片 */}
      <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
        <Col xs={12} sm={8} md={4}>
          <Card size="small">
            <Statistic title="班级数" value={data.classroomCount} prefix={<TeamOutlined />} />
          </Card>
        </Col>
        <Col xs={12} sm={8} md={4}>
          <Card size="small">
            <Statistic title="在托人数" value={data.activeEnrollmentCount} prefix={<UserOutlined />} />
          </Card>
        </Col>
        <Col xs={12} sm={8} md={4}>
          <Card size="small">
            <Statistic
              title="今日到园"
              value={data.checkedInCount}
              suffix={`/ ${data.activeEnrollmentCount}`}
              prefix={<CheckCircleOutlined />}
              valueStyle={{ color: '#52c41a' }}
            />
          </Card>
        </Col>
        <Col xs={12} sm={8} md={4}>
          <Card size="small">
            <Statistic
              title="今日缺勤"
              value={data.leaveCount}
              prefix={<CloseCircleOutlined />}
              valueStyle={{ color: '#faad14' }}
            />
          </Card>
        </Col>
        <Col xs={12} sm={8} md={4}>
          <Card size="small">
            <Statistic
              title="出勤率"
              value={data.attendanceRate}
              suffix="%"
              precision={1}
              prefix={<CheckCircleOutlined />}
              valueStyle={{ color: data.attendanceRate >= 70 ? '#52c41a' : '#ff4d4f' }}
            />
          </Card>
        </Col>
        <Col xs={12} sm={8} md={4}>
          <Card size="small">
            <Statistic
              title="待处理事件"
              value={data.openIncidentCount}
              prefix={<AlertOutlined />}
              valueStyle={{ color: data.openIncidentCount > 0 ? '#ff4d4f' : '#52c41a' }}
            />
          </Card>
        </Col>
        <Col xs={12} sm={8} md={6}>
          <Card size="small">
            <Statistic
              title="未缴账单"
              value={data.unpaidBillCount}
              prefix={<WalletOutlined />}
              suffix={`¥${formatMoney(data.unpaidBillAmount)}`}
              valueStyle={{ color: data.unpaidBillCount > 0 ? '#faad14' : '#52c41a' }}
            />
          </Card>
        </Col>
      </Row>

      {/* 待办和风险 */}
      <Row gutter={24}>
        {/* 待办事项 */}
        <Col xs={24} md={14}>
          <Card
            title={
              <span>
                <Badge count={data.pendingTodos?.length || 0} offset={[8, 0]}>
                  <span>待办事项</span>
                </Badge>
              </span>
            }
            style={{ marginBottom: 16 }}
          >
            {data.pendingTodos && data.pendingTodos.length > 0 ? (
              <List
                size="small"
                dataSource={data.pendingTodos}
                renderItem={(item) => (
                  <List.Item>
                    <List.Item.Meta
                      avatar={todoTypeIcon(item.type)}
                      title={
                        <span>
                          <Tag color={item.type === 'INCIDENT_HANDLE' ? 'red' : 'blue'}>{item.typeName}</Tag>
                          {item.title}
                        </span>
                      }
                      description={
                        <div>
                          <Text type="secondary" ellipsis>{item.description}</Text>
                          <br />
                          <Text type="secondary" style={{ fontSize: 12 }}>
                            {dayjs(item.createdAt).format('MM-DD HH:mm')}
                          </Text>
                        </div>
                      }
                    />
                  </List.Item>
                )}
              />
            ) : (
              <Empty description="暂无待办事项" image={Empty.PRESENTED_IMAGE_SIMPLE} />
            )}
          </Card>
        </Col>

        {/* 风险预警 */}
        <Col xs={24} md={10}>
          <Card title="风险预警" style={{ marginBottom: 16 }}>
            {data.riskAlerts && data.riskAlerts.length > 0 ? (
              <List
                size="small"
                dataSource={data.riskAlerts}
                renderItem={(item) => (
                  <List.Item>
                    <List.Item.Meta
                      avatar={riskIcon(item.type)}
                      title={
                        <span>
                          <Tag color={severityColor(item.severity)}>{item.typeName}</Tag>
                          {item.title}
                        </span>
                      }
                      description={
                        <Text type="secondary" ellipsis>{item.description}</Text>
                      }
                    />
                  </List.Item>
                )}
              />
            ) : (
              <Empty description="暂无风险预警" image={Empty.PRESENTED_IMAGE_SIMPLE} />
            )}
          </Card>
        </Col>
      </Row>
    </div>
  )
}

export default PrincipalWorkbench
