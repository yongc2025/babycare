import React from 'react'
import { Button, Card, Empty, List, Space, Tag, Typography } from 'antd'
import {
  CameraOutlined,
  FileTextOutlined,
  MedicineBoxOutlined,
  MoonOutlined,
  PlusOutlined,
  SmileOutlined,
} from '@ant-design/icons'

const { Title, Paragraph, Text } = Typography

const recordTypes = [
  { title: '喂养记录', desc: '记录饮奶、辅食、饮水和进食反馈', icon: <FileTextOutlined />, color: 'green' },
  { title: '睡眠记录', desc: '记录入睡、醒来、睡眠状态和老师观察', icon: <MoonOutlined />, color: 'blue' },
  { title: '健康观察', desc: '记录体温、晨检、情绪和异常提醒', icon: <MedicineBoxOutlined />, color: 'orange' },
  { title: '活动照片', desc: '沉淀班级活动、成长瞬间和家长可见素材', icon: <CameraOutlined />, color: 'cyan' },
]

const recentRecords = [
  { time: '09:12', title: '芽芽一班完成晨检登记', status: '已完成' },
  { time: '10:30', title: '户外活动照片待审核', status: '待处理' },
  { time: '12:15', title: '午餐摄入情况待补录', status: '待处理' },
]

const GrowthRecord: React.FC = () => {
  return (
    <div>
      <Space direction="vertical" size={4} style={{ marginBottom: 20 }}>
        <Title level={2} style={{ margin: 0 }}>
          保育记录
        </Title>
        <Paragraph type="secondary" style={{ margin: 0 }}>
          后续将从家庭成长记录升级为托育每日照护记录，覆盖喂养、睡眠、健康观察和活动素材。
        </Paragraph>
      </Space>

      <div
        style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))',
          gap: 16,
          marginBottom: 20,
        }}
      >
        {recordTypes.map((item) => (
          <Card key={item.title}>
            <Space direction="vertical" size={10}>
              <Tag color={item.color} icon={item.icon}>
                {item.title}
              </Tag>
              <Text type="secondary">{item.desc}</Text>
              <Button type="primary" icon={<PlusOutlined />}>
                新增记录
              </Button>
            </Space>
          </Card>
        ))}
      </div>

      <Card title="最近记录">
        <List
          dataSource={recentRecords}
          renderItem={(item) => (
            <List.Item>
              <List.Item.Meta
                avatar={<SmileOutlined style={{ color: 'var(--primary-color)', fontSize: 18 }} />}
                title={item.title}
                description={item.time}
              />
              <Tag color={item.status === '已完成' ? 'green' : 'orange'}>{item.status}</Tag>
            </List.Item>
          )}
          locale={{
            emptyText: <Empty description="暂无保育记录" />,
          }}
        />
      </Card>
    </div>
  )
}

export default GrowthRecord
