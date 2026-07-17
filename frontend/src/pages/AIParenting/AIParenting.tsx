import React from 'react'
import { Alert, Button, Card, Empty, Input, Space, Tabs, Typography } from 'antd'
import { MessageOutlined, RobotOutlined, SendOutlined } from '@ant-design/icons'
import type { TabsProps } from 'antd'

const { Title, Paragraph, Text } = Typography
const { TextArea } = Input

const AIParenting: React.FC = () => {
  const tabItems: TabsProps['items'] = [
    {
      key: 'chat',
      label: (
        <span>
          <MessageOutlined />
          智能咨询
        </span>
      ),
      children: (
        <Card>
          <Space direction="vertical" size={18} style={{ width: '100%' }}>
            <Alert
              type="info"
              showIcon
              message="AI 会话接口待接入"
              description="当前页面不再使用前端模拟回复。后续会接入后端 AIParentingController 的会话、消息和反馈接口。"
            />

            <Empty
              image={Empty.PRESENTED_IMAGE_SIMPLE}
              description="暂无真实 AI 会话数据"
            />

            <TextArea
              disabled
              placeholder="接入真实 AI 会话接口后，可在这里输入育儿或保育问题"
              autoSize={{ minRows: 3, maxRows: 5 }}
            />

            <Button type="primary" icon={<SendOutlined />} disabled>
              发送
            </Button>
          </Space>
        </Card>
      ),
    },
  ]

  return (
    <div>
      <Title level={2}>AI 育儿</Title>
      <Paragraph type="secondary">
        面向家长和老师的智能辅助模块，后续将基于真实保育记录、日报和发展评估生成建议。
      </Paragraph>
      <Tabs items={tabItems} />
      <Text type="secondary">
        <RobotOutlined /> 当前模块已移除本地模拟回复，等待真实接口接入。
      </Text>
    </div>
  )
}

export default AIParenting
