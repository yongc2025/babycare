import React, { useEffect, useState } from 'react'
import { Card, Col, Row, Statistic, Table, Tag, Typography, Spin, Empty, Alert } from 'antd'
import {
  BankOutlined,
  TeamOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
  AlertOutlined,
  WalletOutlined,
  UserOutlined,
} from '@ant-design/icons'
import { bossDashboardAPI } from '../../services/api'
import type { BossDashboard as BossDashboardData } from '../../types'

const { Title } = Typography

const formatMoney = (value?: number) => {
  if (!value) return '0.00'
  return value.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

const BossDashboard: React.FC = () => {
  const [data, setData] = useState<BossDashboardData | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    setLoading(true)
    setError(null)
    bossDashboardAPI.getOverview()
      .then((res: any) => {
        const d = res?.data ?? res
        setData(d)
      })
      .catch((err: any) => {
        setError(err?.message || '加载驾驶舱数据失败')
      })
      .finally(() => setLoading(false))
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

  const columns = [
    {
      title: '机构名称',
      dataIndex: 'organizationName',
      key: 'organizationName',
      render: (name: string) => <strong>{name}</strong>,
    },
    {
      title: '园长',
      dataIndex: 'directorName',
      key: 'directorName',
      render: (name: string) => name || <Tag color="default">未任命</Tag>,
    },
    {
      title: '班级数',
      dataIndex: 'classroomCount',
      key: 'classroomCount',
      sorter: (a: any, b: any) => a.classroomCount - b.classroomCount,
    },
    {
      title: '在托人数',
      dataIndex: 'activeEnrollmentCount',
      key: 'activeEnrollmentCount',
      sorter: (a: any, b: any) => a.activeEnrollmentCount - b.activeEnrollmentCount,
    },
    {
      title: '今日到园',
      dataIndex: 'checkedInCount',
      key: 'checkedInCount',
      render: (count: number, record: any) => (
        <span>
          {count}
          <span style={{ color: '#999', marginLeft: 8 }}>
            ({record.attendanceRate?.toFixed(1)}%)
          </span>
        </span>
      ),
    },
    {
      title: '今日缺勤',
      dataIndex: 'leaveCount',
      key: 'leaveCount',
    },
    {
      title: '异常事件',
      dataIndex: 'openIncidentCount',
      key: 'openIncidentCount',
      render: (count: number) => (
        <Tag color={count > 0 ? 'red' : 'green'}>
          {count > 0 ? `${count} 起待处理` : '无'}
        </Tag>
      ),
    },
  ]

  return (
    <div style={{ padding: 24 }}>
      <Title level={3}>
        <BankOutlined /> 多园区驾驶舱
      </Title>

      {/* 全局概览指标卡片 */}
      <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
        <Col xs={12} sm={8} md={4}>
          <Card>
            <Statistic
              title="机构总数"
              value={data.totalOrganizations}
              prefix={<BankOutlined />}
              valueStyle={{ color: '#1890ff' }}
            />
          </Card>
        </Col>
        <Col xs={12} sm={8} md={4}>
          <Card>
            <Statistic
              title="总班级数"
              value={data.totalClassrooms}
              prefix={<TeamOutlined />}
              valueStyle={{ color: '#52c41a' }}
            />
          </Card>
        </Col>
        <Col xs={12} sm={8} md={4}>
          <Card>
            <Statistic
              title="在托总数"
              value={data.totalEnrollments}
              prefix={<UserOutlined />}
              valueStyle={{ color: '#1890ff' }}
            />
          </Card>
        </Col>
        <Col xs={12} sm={8} md={4}>
          <Card>
            <Statistic
              title="今日到园"
              value={data.totalCheckedInToday}
              suffix={`/ ${data.totalEnrollments}`}
              prefix={<CheckCircleOutlined />}
              valueStyle={{ color: '#52c41a' }}
            />
          </Card>
        </Col>
        <Col xs={12} sm={8} md={4}>
          <Card>
            <Statistic
              title="今日缺勤"
              value={data.totalLeaveToday}
              prefix={<CloseCircleOutlined />}
              valueStyle={{ color: '#faad14' }}
            />
          </Card>
        </Col>
        <Col xs={12} sm={8} md={4}>
          <Card>
            <Statistic
              title="出勤率"
              value={data.overallAttendanceRate}
              suffix="%"
              precision={1}
              prefix={<CheckCircleOutlined />}
              valueStyle={{ color: '#1890ff' }}
            />
          </Card>
        </Col>
        <Col xs={12} sm={8} md={4}>
          <Card>
            <Statistic
              title="待处理事件"
              value={data.totalOpenIncidents}
              prefix={<AlertOutlined />}
              valueStyle={{ color: data.totalOpenIncidents > 0 ? '#ff4d4f' : '#52c41a' }}
            />
          </Card>
        </Col>
        <Col xs={12} sm={8} md={4}>
          <Card>
            <Statistic
              title="未缴账单"
              value={data.totalUnpaidBills}
              prefix={<WalletOutlined />}
              suffix={`¥${formatMoney(data.totalUnpaidAmount)}`}
              valueStyle={{ color: data.totalUnpaidBills > 0 ? '#faad14' : '#52c41a' }}
            />
          </Card>
        </Col>
      </Row>

      {/* 各机构明细表格 */}
      <Card title="各机构详情">
        <Table
          dataSource={data.orgSummaries}
          columns={columns}
          rowKey="organizationId"
          pagination={false}
          locale={{ emptyText: '暂无机构数据' }}
        />
      </Card>
    </div>
  )
}

export default BossDashboard
