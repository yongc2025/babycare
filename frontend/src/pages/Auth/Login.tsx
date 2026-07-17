import React, { useEffect } from 'react'
import {
  Button,
  Card,
  Checkbox,
  Divider,
  Form,
  Input,
  Space,
  Typography,
  message,
} from 'antd'
import {
  CheckCircleOutlined,
  LockOutlined,
  SafetyCertificateOutlined,
  TeamOutlined,
  UserOutlined,
  WechatOutlined,
} from '@ant-design/icons'
import { Link, useNavigate } from 'react-router-dom'
import { useAuthStore } from '../../stores/authStore'
import { LoginForm } from '../../types'
import './Auth.css'

const { Title, Text } = Typography
const STORAGE_KEY = 'babycare_login_memory'

interface LoginMemory {
  emailOrUsername?: string
  lastUsed?: string
}

const Login: React.FC = () => {
  const navigate = useNavigate()
  const { login, isLoading } = useAuthStore()
  const [form] = Form.useForm<LoginForm>()

  useEffect(() => {
    const savedData = localStorage.getItem(STORAGE_KEY)
    if (!savedData) return

    try {
      const memory: LoginMemory = JSON.parse(savedData)
      if (memory.emailOrUsername) {
        form.setFieldsValue({ emailOrUsername: memory.emailOrUsername })
      }
    } catch (error) {
      console.error('Failed to load login memory:', error)
    }
  }, [form])

  const saveToMemory = (emailOrUsername: string) => {
    const memory: LoginMemory = {
      emailOrUsername,
      lastUsed: new Date().toISOString(),
    }
    localStorage.setItem(STORAGE_KEY, JSON.stringify(memory))
  }

  const handleSubmit = async (values: LoginForm) => {
    try {
      if (values.remember) {
        saveToMemory(values.emailOrUsername)
      }

      await login(values.emailOrUsername, values.password)
      message.success('登录成功')
      navigate('/dashboard')
    } catch (error) {
      // The store already displays the request error.
    }
  }

  const handleWeChatLogin = () => {
    message.info('微信登录正在规划接入')
  }

  return (
    <div className="auth-container">
      <section className="auth-brand-panel">
        <div className="auth-brand">
          <img src="/brand-lockup-light.svg" alt="好芽儿托育保育机构管理系统" className="auth-brand-lockup" />
        </div>

        <div className="auth-copy">
          <Text className="auth-eyebrow">托育 + 保育机构工作台</Text>
          <Title level={1}>让园所每日照护更清晰，家长同步更安心</Title>
          <Text>
            面向托育中心、保育老师和家长的协同系统，围绕入托、考勤、保育记录和家园沟通沉淀真实运营数据。
          </Text>
        </div>

        <div className="auth-proof-grid">
          <div className="auth-proof-item">
            <TeamOutlined />
            <span>班级与宝宝档案</span>
          </div>
          <div className="auth-proof-item">
            <CheckCircleOutlined />
            <span>考勤与照护闭环</span>
          </div>
          <div className="auth-proof-item">
            <SafetyCertificateOutlined />
            <span>安全合规留痕</span>
          </div>
        </div>
      </section>

      <section className="auth-form-panel">
        <Card className="auth-card">
          <div className="auth-card-header">
            <Title level={2}>登录工作台</Title>
            <Text type="secondary">使用账号进入好芽儿机构管理系统</Text>
          </div>

          <Form
            form={form}
            name="login"
            layout="vertical"
            onFinish={handleSubmit}
            size="large"
            autoComplete="off"
            initialValues={{ remember: true }}
          >
            <Form.Item
              name="emailOrUsername"
              label="用户名或邮箱"
              rules={[{ required: true, message: '请输入用户名或邮箱' }]}
            >
              <Input
                prefix={<UserOutlined />}
                placeholder="请输入用户名或邮箱"
                autoComplete="username"
              />
            </Form.Item>

            <Form.Item
              name="password"
              label="密码"
              rules={[
                { required: true, message: '请输入密码' },
                { min: 6, message: '密码至少 6 位' },
              ]}
            >
              <Input.Password
                prefix={<LockOutlined />}
                placeholder="请输入密码"
                autoComplete="current-password"
              />
            </Form.Item>

            <div className="form-actions">
              <Form.Item name="remember" valuePropName="checked" noStyle>
                <Checkbox>记住账号</Checkbox>
              </Form.Item>
              <Link to="/forgot-password" className="forgot-link">
                忘记密码？
              </Link>
            </div>

            <Button
              type="primary"
              htmlType="submit"
              loading={isLoading}
              block
              size="large"
              className="submit-btn"
            >
              登录
            </Button>
          </Form>

          <Divider>或</Divider>

          <Space direction="vertical" style={{ width: '100%' }} size="middle">
            <Button
              icon={<WechatOutlined />}
              size="large"
              block
              onClick={handleWeChatLogin}
              className="wechat-btn"
            >
              微信登录
            </Button>

            <div className="register-link">
              还没有账号？
              <Link to="/register"> 立即注册</Link>
            </div>
          </Space>
        </Card>

        <Text className="footer-text">© 2026 好芽儿. All rights reserved.</Text>
      </section>
    </div>
  )
}

export default Login
