import React from 'react'
import { Button, Card, List, Space, Tag, Typography } from 'antd'
import {
  BellOutlined,
  CheckCircleOutlined,
  CommentOutlined,
  PlusOutlined,
  TeamOutlined,
} from '@ant-design/icons'

const { Title, Paragraph, Text } = Typography

const notices = [
  { title: '本周户外活动安排', scope: '全园', status: '已发布' },
  { title: '苗苗班家长会提醒', scope: '班级', status: '待确认' },
  { title: '春季传染病预防提示', scope: '全园', status: '草稿' },
]

const tasks = [
  { title: '确认乐乐用药授权', owner: '保健老师', priority: '高' },
  { title: '补充午睡观察记录', owner: '芽芽一班', priority: '中' },
  { title: '整理今日活动照片', owner: '苗苗班', priority: '中' },
]

const FamilyCollaboration: React.FC = () => {
  return (
    <div>
      <Space direction="vertical" size={4} style={{ marginBottom: 20 }}>
        <Title level={2} style={{ margin: 0 }}>
          家园协作
        </Title>
        <Paragraph type="secondary" style={{ margin: 0 }}>
          面向园所、老师和家长的通知、任务和沟通协作模块。
        </Paragraph>
      </Space>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))', gap: 16 }}>
        <Card
          title="通知公告"
          extra={
            <Button type="primary" icon={<PlusOutlined />}>
              发布通知
            </Button>
          }
        >
          <List
            dataSource={notices}
            renderItem={(item) => (
              <List.Item>
                <List.Item.Meta
                  avatar={<BellOutlined style={{ color: 'var(--primary-color)', fontSize: 18 }} />}
                  title={item.title}
                  description={item.scope}
                />
                <Tag color={item.status === '已发布' ? 'green' : item.status === '草稿' ? 'default' : 'orange'}>
                  {item.status}
                </Tag>
              </List.Item>
            )}
          />
        </Card>

        <Card title="协作任务">
          <List
            dataSource={tasks}
            renderItem={(item) => (
              <List.Item>
                <List.Item.Meta
                  avatar={<CheckCircleOutlined style={{ color: 'var(--success-color)', fontSize: 18 }} />}
                  title={item.title}
                  description={item.owner}
                />
                <Tag color={item.priority === '高' ? 'red' : 'orange'}>{item.priority}优先级</Tag>
              </List.Item>
            )}
          />
        </Card>

        <Card title="沟通入口">
          <Space direction="vertical" size={14}>
            <Text>
              <TeamOutlined /> 班级家长群、老师沟通和园所通知将统一沉淀。
            </Text>
            <Text>
              <CommentOutlined /> 后续支持围绕日报、异常事件和请假单发起沟通。
            </Text>
          </Space>
        </Card>
      </div>
    </div>
  )
}

export default FamilyCollaboration
