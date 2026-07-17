import React, { useEffect, useState } from 'react'
import { Avatar, Badge, Button, Drawer, Dropdown, Layout, Menu, Tag, Typography } from 'antd'
import type { MenuProps } from 'antd'
import {
  BellOutlined,
  BookOutlined,
  CalendarOutlined,
  CameraOutlined,
  AuditOutlined,
  FileTextOutlined,
  HeartOutlined,
  BankOutlined,
  HomeOutlined,
  LogoutOutlined,
  MedicineBoxOutlined,
  MenuOutlined,
  RobotOutlined,
  SettingOutlined,
  TeamOutlined,
  UserOutlined,
  UsergroupAddOutlined,
} from '@ant-design/icons'
import { useLocation, useNavigate } from 'react-router-dom'
import { useAuthStore } from '../../stores/authStore'
import { useFamilyStore } from '../../stores/familyStore'
import './MainLayout.css'

const { Header, Sider, Content } = Layout
const { Text } = Typography

interface MainLayoutProps {
  children: React.ReactNode
}

type UserRole = 'PARENT' | 'ADMIN'

interface AppMenuItem {
  key: string
  icon: React.ReactNode
  label: string
  roles: UserRole[]
}

const appMenuItems: AppMenuItem[] = [
  {
    key: '/dashboard',
    icon: <HomeOutlined />,
    label: '今日工作台',
    roles: ['ADMIN', 'PARENT'],
  },
  {
    key: '/growth-record',
    icon: <CameraOutlined />,
    label: '成长记录',
    roles: ['PARENT'],
  },
  {
    key: '/organization-management',
    icon: <BankOutlined />,
    label: '园所运营',
    roles: ['ADMIN'],
  },
  {
    key: '/teacher-workbench',
    icon: <CalendarOutlined />,
    label: '班级照护',
    roles: ['ADMIN'],
  },
  {
    key: '/daily-report-management',
    icon: <FileTextOutlined />,
    label: '日报管理',
    roles: ['ADMIN'],
  },
  {
    key: '/health-safety',
    icon: <MedicineBoxOutlined />,
    label: '健康安全',
    roles: ['ADMIN'],
  },
  {
    key: '/operations-regulatory',
    icon: <AuditOutlined />,
    label: '运营监管',
    roles: ['ADMIN'],
  },
  {
    key: '/ai-parenting',
    icon: <RobotOutlined />,
    label: 'AI 育儿',
    roles: ['PARENT'],
  },
  {
    key: '/education-planning',
    icon: <BookOutlined />,
    label: '教育规划',
    roles: ['PARENT', 'ADMIN'],
  },
  {
    key: '/family-collaboration',
    icon: <TeamOutlined />,
    label: '家园协作',
    roles: ['PARENT', 'ADMIN'],
  },
  {
    key: '/parent-reports',
    icon: <FileTextOutlined />,
    label: '家长日报',
    roles: ['PARENT', 'ADMIN'],
  },
  {
    key: '/elder-mode',
    icon: <HeartOutlined />,
    label: '长辈模式',
    roles: ['PARENT'],
  },
  {
    key: '/family-management',
    icon: <UsergroupAddOutlined />,
    label: '家庭管理',
    roles: ['PARENT'],
  },
  {
    key: '/system-management',
    icon: <SettingOutlined />,
    label: '系统管理',
    roles: ['ADMIN'],
  },
]

const MainLayout: React.FC<MainLayoutProps> = ({ children }) => {
  const navigate = useNavigate()
  const location = useLocation()
  const { user, logout } = useAuthStore()
  const { currentBaby, currentFamily, families, loadFamilies } = useFamilyStore()
  const [collapsed, setCollapsed] = useState(false)
  const [mobileMenuVisible, setMobileMenuVisible] = useState(false)
  const currentRole: UserRole = user?.role === 'ADMIN' ? 'ADMIN' : 'PARENT'
  const roleLabel = currentRole === 'ADMIN' ? '园所工作台' : '家长工作台'
  const visibleMenuItems: MenuProps['items'] = appMenuItems
    .filter((item) => item.roles.includes(currentRole))
    .map(({ roles: _roles, ...item }) => item)

  useEffect(() => {
    if (families.length > 0) return

    loadFamilies().catch(() => {
      // The store keeps the error state; pages render empty context when loading fails.
    })
  }, [families.length, loadFamilies])

  const userMenuItems: MenuProps['items'] = [
    {
      key: 'profile',
      icon: <UserOutlined />,
      label: '个人资料',
      onClick: () => navigate('/profile'),
    },
    {
      key: 'settings',
      icon: <SettingOutlined />,
      label: '系统设置',
    },
    {
      type: 'divider',
    },
    {
      key: 'logout',
      icon: <LogoutOutlined />,
      label: '退出登录',
      onClick: () => {
        logout()
        navigate('/login')
      },
    },
  ]

  const handleMenuClick = ({ key }: { key: string }) => {
    navigate(key)
    setMobileMenuVisible(false)
  }

  const renderMenu = (className: string) => (
    <Menu
      mode="inline"
      selectedKeys={[location.pathname]}
      items={visibleMenuItems}
      onClick={handleMenuClick}
      className={className}
    />
  )

  return (
    <Layout className="main-layout">
      <Sider
        trigger={null}
        collapsible
        collapsed={collapsed}
        className="layout-sider desktop-only"
        theme="light"
        width={248}
      >
        <div className="layout-logo">
          <img
            src={collapsed ? '/logo.svg' : '/brand-compact-light.svg'}
            alt="好芽儿托育保育机构管理系统"
            className={collapsed ? 'layout-icon-logo' : 'layout-brand-lockup'}
          />
        </div>

        {renderMenu('nav-menu')}
      </Sider>

      <Layout className="layout-shell">
        <Header className="layout-header">
          <div className="header-left">
            <Button
              type="text"
              icon={<MenuOutlined />}
              onClick={() => setCollapsed(!collapsed)}
              className="desktop-only icon-btn"
            />
            <Button
              type="text"
              icon={<MenuOutlined />}
              onClick={() => setMobileMenuVisible(true)}
              className="mobile-only icon-btn"
            />

            <div className="workspace-meta">
              <Text className="workspace-title">
                {currentRole === 'ADMIN' ? '好芽儿托育中心' : currentFamily?.name || '家庭照护空间'}
              </Text>
              <Text className="workspace-subtitle">
                <CalendarOutlined /> {roleLabel}
              </Text>
            </div>
          </div>

          <div className="header-right">
            {currentBaby && (
              <div className="current-baby desktop-only">
                <Avatar src={currentBaby.avatar} size="small">
                  {currentBaby.name[0]}
                </Avatar>
                <span>{currentBaby.name}</span>
              </div>
            )}

            <Tag className="role-tag">{roleLabel}</Tag>

            <Badge count={5} size="small">
              <Button type="text" icon={<BellOutlined />} className="icon-btn" />
            </Badge>

            <Dropdown menu={{ items: userMenuItems }} placement="bottomRight">
              <button className="user-info" type="button">
                <Avatar src={user?.avatar} size="small">
                  {user?.nickname?.[0] || user?.username?.[0] || '用'}
                </Avatar>
                <span className="user-name desktop-only">{user?.nickname || user?.username}</span>
              </button>
            </Dropdown>
          </div>
        </Header>

        <Content className="layout-content">{children}</Content>
      </Layout>

      <Drawer
        title="好芽儿"
        placement="left"
        onClose={() => setMobileMenuVisible(false)}
        open={mobileMenuVisible}
        className="mobile-menu"
        width={286}
      >
        <div className="mobile-logo">
          <img src="/brand-compact.svg" alt="好芽儿托育保育机构管理系统" className="mobile-brand-lockup" />
        </div>
        {renderMenu('mobile-nav-menu')}
      </Drawer>
    </Layout>
  )
}

export default MainLayout
