import React, { useEffect, useState } from 'react'
import { Form, Input, Button, Card, Typography, message, Checkbox, Tabs, Space } from 'antd'
import {
  UserOutlined,
  LockOutlined,
  PhoneOutlined,
  MessageOutlined,
} from '@ant-design/icons'
import { Link, useNavigate } from 'react-router-dom'
import { useAuthStore } from '../../stores/authStore'
import { authAPI } from '../../services/authService'
import { RegisterForm } from '../../types'
import './Auth.css'

const { Title, Text } = Typography

const Register: React.FC = () => {
  const navigate = useNavigate()
  const { register, isLoading } = useAuthStore()
  const [form] = Form.useForm()
  const [registerMode, setRegisterMode] = useState<'phone' | 'account'>('phone')
  const [countdown, setCountdown] = useState(0)
  const [sendingCode, setSendingCode] = useState(false)

  // 倒计时
  useEffect(() => {
    if (countdown <= 0) return
    const timer = setTimeout(() => setCountdown(countdown - 1), 1000)
    return () => clearTimeout(timer)
  }, [countdown])

  const handleSendCode = async () => {
    const phone = form.getFieldValue('phone')
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

  const handleSubmit = async (values: RegisterForm) => {
    try {
      if (registerMode === 'phone') {
        await register({
          phone: values.phone,
          password: values.password,
          nickname: values.nickname,
          phoneVerified: !!values.phoneCode,
        })
      } else {
        await register({
          username: values.username,
          password: values.password,
          nickname: values.nickname,
        })
      }

      message.success('注册成功！欢迎加入好芽儿！')
      setTimeout(() => navigate('/dashboard'), 1500)
    } catch (error: any) {
      console.error('注册失败:', error)
    }
  }

  return (
    <div className="auth-container">
      <div className="auth-content">
        <div className="auth-header">
          <img src="/brand-lockup.svg" alt="好芽儿托育保育机构管理系统" className="register-brand-lockup" />
          <Title level={2} className="auth-title">创建账号</Title>
          <Text type="secondary" className="auth-subtitle">开启托育保育数字化工作台</Text>
        </div>

        <Card className="auth-card">
          <Tabs activeKey={registerMode} onChange={(key) => setRegisterMode(key as 'phone' | 'account')}
            centered items={[
              { key: 'phone', label: '手机号注册' },
              { key: 'account', label: '账号注册' },
            ]} />

          <Form form={form} name="register" layout="vertical" onFinish={handleSubmit} size="large" autoComplete="off">

            {registerMode === 'phone' ? (
              <>
                <Form.Item name="phone" label="手机号"
                  rules={[
                    { required: true, message: '请输入手机号' },
                    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确' },
                  ]}>
                  <Input prefix={<PhoneOutlined />} placeholder="请输入手机号" maxLength={11} />
                </Form.Item>

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

                <Form.Item name="nickname" label="昵称（可选）">
                  <Input placeholder="请输入昵称" />
                </Form.Item>
              </>
            ) : (
              <>
                <Form.Item name="username" label="用户名"
                  rules={[
                    { required: true, message: '请输入用户名' },
                    { min: 3, message: '用户名至少3位字符' },
                    { max: 20, message: '用户名最多20位字符' },
                    { pattern: /^[a-zA-Z0-9_]+$/, message: '用户名只能包含字母、数字和下划线' },
                  ]}>
                  <Input prefix={<UserOutlined />} placeholder="请输入用户名" autoComplete="username" />
                </Form.Item>

                <Form.Item name="nickname" label="昵称（可选）">
                  <Input placeholder="请输入昵称" />
                </Form.Item>
              </>
            )}

            <Form.Item name="password" label="密码"
              rules={[
                { required: true, message: '请输入密码' },
                { min: 6, message: '密码至少6位字符' },
              ]}>
              <Input.Password prefix={<LockOutlined />} placeholder="请输入密码" autoComplete="new-password" />
            </Form.Item>

            <Form.Item name="agreement" valuePropName="checked"
              rules={[{ validator: (_, value) => value ? Promise.resolve() : Promise.reject(new Error('请同意服务条款')) }]}>
              <Checkbox>
                我已阅读并同意
                <Link to="/terms" target="_blank">《服务条款》</Link>和
                <Link to="/privacy" target="_blank">《隐私政策》</Link>
              </Checkbox>
            </Form.Item>

            <Form.Item>
              <Button type="primary" htmlType="submit" loading={isLoading} block size="large" className="submit-btn">
                立即注册
              </Button>
            </Form.Item>
          </Form>

          <div className="login-link">
            已有账号？<Link to="/login">立即登录</Link>
          </div>
        </Card>

        <div className="auth-footer">
          <Text type="secondary" className="footer-text">© 2026 好芽儿. All rights reserved.</Text>
        </div>
      </div>
    </div>
  )
}

export default Register
