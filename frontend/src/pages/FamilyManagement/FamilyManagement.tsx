import React, { useEffect, useState } from 'react'
import { Alert, Button, Card, DatePicker, Empty, Form, Input, List, Modal, Select, Space, Tag, Typography, message } from 'antd'
import {
  HomeOutlined,
  PlusOutlined,
  TeamOutlined,
  UserAddOutlined,
  UserOutlined,
} from '@ant-design/icons'
import { useFamilyStore } from '../../stores/familyStore'

const { Title, Paragraph, Text } = Typography

const FamilyManagement: React.FC = () => {
  const {
    families,
    currentFamily,
    currentBaby,
    isLoading,
    error,
    loadFamilies,
    createFamily,
    joinFamily,
    addBaby,
    switchFamily,
    switchBaby,
  } = useFamilyStore()
  const [familyModalOpen, setFamilyModalOpen] = useState(false)
  const [joinModalOpen, setJoinModalOpen] = useState(false)
  const [babyModalOpen, setBabyModalOpen] = useState(false)
  const [familyForm] = Form.useForm()
  const [joinForm] = Form.useForm()
  const [babyForm] = Form.useForm()

  useEffect(() => {
    loadFamilies().catch(() => {
      // Error is kept in the store and displayed below.
    })
  }, [loadFamilies])

  const handleCreateFamily = async () => {
    const values = await familyForm.validateFields()
    await createFamily(values)
    message.success('家庭创建成功')
    familyForm.resetFields()
    setFamilyModalOpen(false)
  }

  const handleJoinFamily = async () => {
    const values = await joinForm.validateFields()
    await joinFamily(values.inviteCode)
    message.success('已加入家庭')
    joinForm.resetFields()
    setJoinModalOpen(false)
  }

  const handleAddBaby = async () => {
    const values = await babyForm.validateFields()
    await addBaby({
      name: values.name,
      gender: values.gender,
      birthday: values.birthday.format('YYYY-MM-DD'),
      avatar: values.avatar,
    })
    message.success('宝宝添加成功')
    babyForm.resetFields()
    setBabyModalOpen(false)
  }

  return (
    <div>
      <Space direction="vertical" size={4} style={{ marginBottom: 20 }}>
        <Title level={2} style={{ margin: 0 }}>
          家庭与宝宝管理
        </Title>
        <Paragraph type="secondary" style={{ margin: 0 }}>
          当前使用真实家庭与宝宝接口；后续会升级为机构、班级、入托档案和家长关系管理。
        </Paragraph>
      </Space>

      {error && <Alert type="error" showIcon message={error} style={{ marginBottom: 16 }} />}

      <div style={{ display: 'grid', gridTemplateColumns: 'minmax(0, 1.3fr) minmax(280px, 0.7fr)', gap: 16 }}>
        <Card
          title="家庭列表"
          extra={
            <Space>
              <Button icon={<UserAddOutlined />} onClick={() => setJoinModalOpen(true)}>
                加入家庭
              </Button>
              <Button type="primary" icon={<PlusOutlined />} onClick={() => setFamilyModalOpen(true)}>
                创建家庭
              </Button>
            </Space>
          }
        >
          <List
            loading={isLoading}
            dataSource={families}
            locale={{
              emptyText: <Empty description="暂无家庭，请先创建或通过邀请码加入家庭" />,
            }}
            renderItem={(family) => (
              <List.Item
                actions={[
                  <Button key="switch" type="link" onClick={() => switchFamily(family.id)}>
                    设为当前
                  </Button>,
                ]}
              >
                <List.Item.Meta
                  avatar={<HomeOutlined style={{ color: 'var(--primary-color)', fontSize: 20 }} />}
                  title={
                    <Space>
                      {family.name}
                      {currentFamily?.id === family.id && <Tag color="green">当前</Tag>}
                    </Space>
                  }
                  description={`邀请码：${family.inviteCode || '暂无'} · 宝宝 ${family.babies.length} 名 · 成员 ${family.members.length} 名`}
                />
              </List.Item>
            )}
          />
        </Card>

        <Card
          title="当前宝宝"
          extra={
            <Button type="primary" icon={<PlusOutlined />} disabled={!currentFamily} onClick={() => setBabyModalOpen(true)}>
              添加宝宝
            </Button>
          }
        >
          {!currentFamily && <Empty description="请先选择家庭" />}

          {currentFamily && (
            <List
              dataSource={currentFamily.babies}
              locale={{
                emptyText: <Empty description="当前家庭暂无宝宝" />,
              }}
              renderItem={(baby) => (
                <List.Item
                  actions={[
                    <Button key="switchBaby" type="link" onClick={() => switchBaby(baby.id)}>
                      设为当前
                    </Button>,
                  ]}
                >
                  <List.Item.Meta
                    avatar={<UserOutlined style={{ color: 'var(--primary-color)', fontSize: 20 }} />}
                    title={
                      <Space>
                        {baby.name}
                        {currentBaby?.id === baby.id && <Tag color="green">当前</Tag>}
                      </Space>
                    }
                    description={`${baby.gender === 'MALE' ? '男宝宝' : '女宝宝'} · ${baby.birthday}`}
                  />
                </List.Item>
              )}
            />
          )}
        </Card>
      </div>

      <Card title="下一阶段边界" style={{ marginTop: 16 }}>
        <Space direction="vertical" size={12}>
          <Text>
            <TeamOutlined /> 家庭关系会继续保留，用于家长和监护人关系。
          </Text>
          <Text>
            <HomeOutlined /> 托育产品主链会新增机构、班级、员工和入托档案，不直接用家庭替代机构。
          </Text>
        </Space>
      </Card>

      <Modal
        title="创建家庭"
        open={familyModalOpen}
        onOk={handleCreateFamily}
        onCancel={() => setFamilyModalOpen(false)}
        confirmLoading={isLoading}
        destroyOnClose
      >
        <Form form={familyForm} layout="vertical">
          <Form.Item name="name" label="家庭名称" rules={[{ required: true, message: '请输入家庭名称' }]}>
            <Input placeholder="例如：小树家庭" />
          </Form.Item>
          <Form.Item name="description" label="家庭描述">
            <Input.TextArea placeholder="可选" rows={3} />
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title="加入家庭"
        open={joinModalOpen}
        onOk={handleJoinFamily}
        onCancel={() => setJoinModalOpen(false)}
        confirmLoading={isLoading}
        destroyOnClose
      >
        <Form form={joinForm} layout="vertical">
          <Form.Item name="inviteCode" label="邀请码" rules={[{ required: true, message: '请输入邀请码' }]}>
            <Input placeholder="请输入 6 位家庭邀请码" />
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title="添加宝宝"
        open={babyModalOpen}
        onOk={handleAddBaby}
        onCancel={() => setBabyModalOpen(false)}
        confirmLoading={isLoading}
        destroyOnClose
      >
        <Form form={babyForm} layout="vertical">
          <Form.Item name="name" label="宝宝姓名" rules={[{ required: true, message: '请输入宝宝姓名' }]}>
            <Input placeholder="请输入宝宝姓名" />
          </Form.Item>
          <Form.Item name="gender" label="性别" rules={[{ required: true, message: '请选择性别' }]}>
            <Select
              options={[
                { label: '男宝宝', value: 'MALE' },
                { label: '女宝宝', value: 'FEMALE' },
              ]}
            />
          </Form.Item>
          <Form.Item name="birthday" label="生日" rules={[{ required: true, message: '请选择生日' }]}>
            <DatePicker style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="avatar" label="头像地址">
            <Input placeholder="可选，后续会接入文件上传" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
}

export default FamilyManagement
