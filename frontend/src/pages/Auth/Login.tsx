import React, { useEffect, useState } from 'react'
import {
  Button,
  Card,
  Checkbox,
  Divider,
  Form,
  Input,
  Space,
  Tabs,
  Typography,
  message,
} from 'antd'
import {
  CheckCircleOutlined,
  LockOutlined,
  MessageOutlined,
  PhoneOutlined,
  SafetyCertificateOutlined,
  TeamOutlined,
  UserOutlined,
  WechatOutlined,
} from '@ant-design/icons'
import { Link, useNavigate } from 'react-router-dom'
import { useAuthStore } from '../../stores/authStore'
import { authAPI } from '../../services/authService'
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
  const { login, phoneLogin, isLoading } = useAuthStore()
  const [form] = Form.useForm<LoginForm>()
  const [loginMode, setLoginMode] = useState<'account' | 'phone'>('account')
  const [phoneLoginMode] = useState<'password' | 'code'>('code')
  const [countdown, setCountdown] = useState(0)
  const [sendingCode, setSendingCode] = useState(false)
  const [phoneInput, setPhoneInput] = useState('')

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

  // 倒计时
  useEffect(() => {
    if (countdown <= 0) return
    const timer = setTimeout(() => setCountdown(countdown - 1), 1000)
    return () => clearTimeout(timer)
  }, [countdown])

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

  const handleWeChatLogin = async () => {
    try {
      const resp: any = await authAPI.weChatLogin?.({ code: 'placeholder' })
      const data = resp?.data ?? resp
      if (data?.success) {
        message.success('微信登录成功')
      } else {
        message.info(data?.message || '微信登录功能待接入')
      }
    } catch {
      message.info('微信登录功能待接入')
    }
  }

  const handlePhoneLogin = async (values: any) => {
    try {
      if (phoneLoginMode === 'code') {
        await phoneLogin(values.phone, undefined, values.phoneCode)
      } else {
        await phoneLogin(values.phone, values.password)
      }
      message.success('登录成功')
      navigate('/dashboard')
    } catch (error) {
      // The store already displays the request error.
    }
  }

  const handleSendCode = async () => {
    const phone = phoneInput
    if (!phone || !/^1[3-9]\d{9}$/.test(phone)) {
      message.warning('请输入正确的手机号')
      return
    }
    setSendingCode(true)
    try {
      await authAPI.sendVerificationCode(phone)
      message.success('验证码已发送（模拟码：123456）')
      setCountdown(60)
    } catch (err) {
      message.error('发送失败')
    } finally {
      setSendingCode(false)
    }
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

          <Tabs activeKey={loginMode} onChange={(key) => setLoginMode(key as 'account' | 'phone')}
            centered items={[
              { key: 'account', label: '账号登录' },
              { key: 'phone', label: '手机号登录' },
            ]} />

          {loginMode === 'account' ? (
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
          ) : (
            <Form
              form={form}
              name="phoneLogin"
              layout="vertical"
              onFinish={handlePhoneLogin}
              size="large"
              autoComplete="off"
            >
              <Form.Item
                name="phone"
                label="手机号"
                rules={[
                  { required: true, message: '请输入手机号' },
                  { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确' },
                ]}
              >
                <Input prefix={<PhoneOutlined />} placeholder="请输入手机号" maxLength={11}
                onChange={(e) => setPhoneInput(e.target.value)} />
              </Form.Item>

              {phoneLoginMode === 'code' ? (
                <>
                  <Space.Compact style={{ width: '100%' }} size="large">
                    <Form.Item name="phoneCode" label="验证码" noStyle
                      rules={[{ required: true, message: '请输入验证码' }]}>
                      <Input prefix={<MessageOutlined />} placeholder="验证码" maxLength={6} style={{ width: '65%' }} />
                    </Form.Item>
                    <Button disabled={countdown > 0 || sendingCode} onClick={handleSendCode}
                      style={{ width: '35%', height: 40 }}>
                      {countdown > 0 ? `${countdown}s` : '获取验证码'}
                    </Button>
                  </Space.Compact>
                </>
              ) : (
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
              )}

              <Button
                type="primary"
                htmlType="submit"
                loading={isLoading}
                block
                size="large"
                className="submit-btn"
              >
                {phoneLoginMode === 'code' ? '验证码登录' : '手机号登录'}
              </Button>
            </Form>
          )}

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
