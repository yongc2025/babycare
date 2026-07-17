import React, { useEffect } from 'react'
import { Form, Input, Button, Card, Typography, message, Checkbox } from 'antd'
import {
  UserOutlined,
  LockOutlined,
} from '@ant-design/icons'
import { Link, useNavigate } from 'react-router-dom'
import { useAuthStore } from '../../stores/authStore'
import { RegisterForm } from '../../types'
import './Auth.css'

const { Title, Text } = Typography

// 本地存储的键名
const STORAGE_KEY = 'babycare_register_memory'

interface RegisterMemory {
  username?: string
  lastUsed?: string
}

const Register: React.FC = () => {
  const navigate = useNavigate()
  const { register, isLoading } = useAuthStore()
  const [form] = Form.useForm()

  // 加载记忆的数据
  useEffect(() => {
    const savedData = localStorage.getItem(STORAGE_KEY)
    if (savedData) {
      try {
        const memory: RegisterMemory = JSON.parse(savedData)
        if (memory.username) {
          form.setFieldsValue({ username: memory.username })
        }
      } catch (error) {
        console.error('Failed to load registration memory:', error)
      }
    }
  }, [])

  // 保存到记忆
  const saveToMemory = (username: string) => {
    const memory: RegisterMemory = {
      username,
      lastUsed: new Date().toISOString()
    }
    localStorage.setItem(STORAGE_KEY, JSON.stringify(memory))
  }

  const handleSubmit = async (values: RegisterForm) => {
    try {
      // 保存用户名到记忆
      saveToMemory(values.username)
      
      await register({
        username: values.username,
        password: values.password,
      })
      
      // 注册成功后显示提示并跳转
      message.success('注册成功！欢迎加入好芽儿！')
      setTimeout(() => {
        navigate('/dashboard')
      }, 1500)
    } catch (error: any) {
      // 错误已在store中处理
      console.error('注册失败:', error)
    }
  }

  return (
    <div className="auth-container">
      <div className="auth-content">
        <div className="auth-header">
          <img src="/brand-lockup.svg" alt="好芽儿托育保育机构管理系统" className="register-brand-lockup" />
          <Title level={2} className="auth-title">
            创建机构账号
          </Title>
          <Text type="secondary" className="auth-subtitle">
            开启托育保育数字化工作台
          </Text>
        </div>

        <Card className="auth-card">
          <Form
            form={form}
            name="register"
            layout="vertical"
            onFinish={handleSubmit}
            size="large"
            autoComplete="off"
          >
            <Form.Item
              name="username"
              label="用户名"
              rules={[
                { required: true, message: '请输入用户名' },
                { min: 3, message: '用户名至少3位字符' },
                { max: 20, message: '用户名最多20位字符' },
                { pattern: /^[a-zA-Z0-9_]+$/, message: '用户名只能包含字母、数字和下划线' },
              ]}
            >
              <Input
                prefix={<UserOutlined />}
                placeholder="请输入用户名"
                autoComplete="username"
              />
            </Form.Item>

            <Form.Item
              name="password"
              label="密码"
              rules={[
                { required: true, message: '请输入密码' },
                { min: 6, message: '密码至少6位字符' },
              ]}
            >
              <Input.Password
                prefix={<LockOutlined />}
                placeholder="请输入密码"
                autoComplete="new-password"
              />
            </Form.Item>

            <Form.Item
              name="confirmPassword"
              label="确认密码"
              dependencies={['password']}
              rules={[
                { required: true, message: '请确认密码' },
                ({ getFieldValue }) => ({
                  validator(_, value) {
                    if (!value || getFieldValue('password') === value) {
                      return Promise.resolve()
                    }
                    return Promise.reject(new Error('两次密码输入不一致'))
                  },
                }),
              ]}
            >
              <Input.Password
                prefix={<LockOutlined />}
                placeholder="请再次输入密码"
                autoComplete="new-password"
              />
            </Form.Item>

            <Form.Item
              name="agreement"
              valuePropName="checked"
              rules={[
                { validator: (_, value) => 
                  value ? Promise.resolve() : Promise.reject(new Error('请同意服务条款')) 
                },
              ]}
            >
              <Checkbox>
                我已阅读并同意 
                <Link to="/terms" target="_blank">《服务条款》</Link>
                和 
                <Link to="/privacy" target="_blank">《隐私政策》</Link>
              </Checkbox>
            </Form.Item>

            <Form.Item>
              <Button
                type="primary"
                htmlType="submit"
                loading={isLoading}
                block
                size="large"
                className="submit-btn"
              >
                立即注册
              </Button>
            </Form.Item>
          </Form>

          <div className="login-link">
            已有账号？
            <Link to="/login"> 立即登录</Link>
          </div>
        </Card>

        <div className="auth-footer">
          <Text type="secondary" className="footer-text">
            © 2026 好芽儿. All rights reserved.
          </Text>
        </div>
      </div>
    </div>
  )
}

export default Register
