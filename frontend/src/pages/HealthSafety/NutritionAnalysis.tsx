import React, { useState } from 'react'
import { Button, Card, Col, DatePicker, Row, Space, Statistic, Table, message } from 'antd'
import { BarChartOutlined } from '@ant-design/icons'
import dayjs from 'dayjs'
import { mealPlanAPI } from '../../services/api'
import type { MealNutritionAnalysisResponse } from '../../types'

interface Props {
  organizationId?: string
}

const NutritionAnalysis: React.FC<Props> = ({ organizationId }) => {
  const [range, setRange] = useState<[dayjs.Dayjs, dayjs.Dayjs]>([dayjs().subtract(7, 'day'), dayjs()])
  const [data, setData] = useState<MealNutritionAnalysisResponse | null>(null)

  const loadAnalysis = async () => {
    if (!organizationId) { message.warning('请先选择机构'); return }
    try {
      const res = await mealPlanAPI.getNutritionAnalysis(organizationId, {
        startDate: range[0].format('YYYY-MM-DD'),
        endDate: range[1].format('YYYY-MM-DD'),
      })
      setData((res?.data ?? res) as MealNutritionAnalysisResponse)
    } catch { message.error('营养分析加载失败') }
  }

  const avgRate = data?.mealStats?.length
    ? Math.round(data.mealStats.reduce((s, m) => s + m.avgIntakeRate, 0) / data.mealStats.length * 10) / 10
    : 0

  const columns = [
    { title: '日期', dataIndex: 'mealDate', key: 'mealDate', width: 80, render: (v: string) => dayjs(v).format('MM-DD') },
    { title: '餐次', dataIndex: 'mealTypeDescription', key: 'mealType', width: 100 },
    { title: '标题', dataIndex: 'title', key: 'title' },
    { title: '全部', dataIndex: 'allCount', key: 'allCount', width: 60 },
    { title: '大部', dataIndex: 'mostCount', key: 'mostCount', width: 60 },
    { title: '一半', dataIndex: 'halfCount', key: 'halfCount', width: 60 },
    { title: '少量', dataIndex: 'lessCount', key: 'lessCount', width: 60 },
    { title: '拒食', dataIndex: 'noneCount', key: 'noneCount', width: 60 },
    { title: '过敏', dataIndex: 'allergyCount', key: 'allergyCount', width: 60 },
    { title: '进食率', dataIndex: 'avgIntakeRate', key: 'avgIntakeRate', width: 80, render: (v: number) => `${v}%` },
  ]

  return (
    <Card>
      <Space direction="vertical" style={{ width: '100%' }}>
        <Space wrap>
          <DatePicker.RangePicker value={range} onChange={(v) => { if (v?.[0] && v?.[1]) setRange([v[0], v[1]]) }} />
          <Button type="primary" icon={<BarChartOutlined />} onClick={loadAnalysis}>分析</Button>
        </Space>
        {data && (
          <>
            <Row gutter={12}>
              <Col span={6}><Card size="small"><Statistic title="食谱总数" value={data.totalMeals} suffix="餐" /></Card></Col>
              <Col span={6}><Card size="small"><Statistic title="进食宝宝" value={data.totalBabies} suffix="人" /></Card></Col>
              <Col span={6}><Card size="small"><Statistic title="平均进食率" value={avgRate} suffix="%" precision={1} /></Card></Col>
              <Col span={6}><Card size="small"><Statistic title="过敏事件" value={data.allergyEventCount} suffix="次" valueStyle={data.allergyEventCount > 0 ? { color: 'red' } : undefined} /></Card></Col>
            </Row>
            <Table rowKey="mealPlanId" dataSource={data.mealStats} columns={columns} size="small" pagination={false} scroll={{ x: 800 }} />
          </>
        )}
      </Space>
    </Card>
  )
}

export default NutritionAnalysis
