import React from 'react'
import { Button, Card, List, Progress, Space, Tag, Typography } from 'antd'
import {
  BookOutlined,
  CalendarOutlined,
  CheckCircleOutlined,
  PlusOutlined,
  ReadOutlined,
} from '@ant-design/icons'

const { Title, Paragraph, Text } = Typography

const plans = [
  {
    title: '语言表达活动',
    desc: '通过绘本、儿歌和情景对话提升表达意愿',
    progress: 68,
    tag: '语言',
  },
  {
    title: '大运动发展',
    desc: '围绕平衡、走跑、攀爬建立分龄活动计划',
    progress: 54,
    tag: '运动',
  },
  {
    title: '生活自理练习',
    desc: '训练洗手、收纳、独立进餐等日常能力',
    progress: 42,
    tag: '自理',
  },
]

const EducationPlanning: React.FC = () => {
  return (
    <div>
      <Space direction="vertical" size={4} style={{ marginBottom: 20 }}>
        <Title level={2} style={{ margin: 0 }}>
          教育规划
        </Title>
        <Paragraph type="secondary" style={{ margin: 0 }}>
          面向托育班级的分龄活动计划。后续会与班级、宝宝月龄、保育观察和成长评估联动。
        </Paragraph>
      </Space>

      <div
        style={{
          display: 'grid',
          gridTemplateColumns: 'minmax(0, 1.4fr) minmax(280px, 0.6fr)',
          gap: 16,
        }}
      >
        <Card
          title="计划列表"
          extra={
            <Button type="primary" icon={<PlusOutlined />}>
              新建计划
            </Button>
          }
        >
          <List
            dataSource={plans}
            renderItem={(item) => (
              <List.Item>
                <List.Item.Meta
                  avatar={<BookOutlined style={{ color: 'var(--primary-color)', fontSize: 20 }} />}
                  title={
                    <Space>
                      {item.title}
                      <Tag>{item.tag}</Tag>
                    </Space>
                  }
                  description={item.desc}
                />
                <div style={{ width: 160 }}>
                  <Progress percent={item.progress} size="small" />
                </div>
              </List.Item>
            )}
          />
        </Card>

        <Card title="本周重点">
          <Space direction="vertical" size={16}>
            <Text>
              <CalendarOutlined /> 按班级配置活动日程
            </Text>
            <Text>
              <ReadOutlined /> 输出活动目标和材料清单
            </Text>
            <Text>
              <CheckCircleOutlined /> 记录完成情况和老师观察
            </Text>
          </Space>
        </Card>
      </div>
    </div>
  )
}

export default EducationPlanning
