import React, { useEffect, useState } from 'react'
import {
  Badge,
  Button,
  Card,
  Form,
  Input,
  InputNumber,
  Modal,
  Popconfirm,
  Select,
  Space,
  Table,
  Tabs,
  Tag,
  Typography,
  message,
} from 'antd'
import {
  DeleteOutlined,
  EditOutlined,
  KeyOutlined,
  PlusOutlined,
  SettingOutlined,
} from '@ant-design/icons'
import adminApi from '../../services/adminApi'
import dayjs from 'dayjs'
import './SystemManagement.css'

const { Title } = Typography
const { TextArea } = Input

// ============================================================
// 角色管理子页面
// ============================================================
const RoleTab: React.FC = () => {
  const [roles, setRoles] = useState<any[]>([])
  const [loading, setLoading] = useState(false)
  const [modalOpen, setModalOpen] = useState(false)
  const [editingRole, setEditingRole] = useState<any>(null)
  const [form] = Form.useForm()

  const loadRoles = async () => {
    setLoading(true)
    try {
      const res = await adminApi.listRoles()
      setRoles(res?.data || [])
    } catch (e: any) {
      message.error('加载角色列表失败')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { loadRoles() }, [])

  const handleSubmit = async () => {
    const values = await form.validateFields()
    try {
      if (editingRole) {
        await adminApi.updateRole(editingRole.id, values)
        message.success('角色更新成功')
      } else {
        await adminApi.createRole(values)
        message.success('角色创建成功')
      }
      setModalOpen(false)
      form.resetFields()
      setEditingRole(null)
      loadRoles()
    } catch (e: any) {
      message.error(e?.response?.data?.message || '操作失败')
    }
  }

  const handleDelete = async (id: number) => {
    try {
      await adminApi.deleteRole(id)
      message.success('角色已删除')
      loadRoles()
    } catch (e: any) {
      message.error(e?.response?.data?.message || '删除失败')
    }
  }

  const openEdit = (role: any) => {
    setEditingRole(role)
    form.setFieldsValue(role)
    setModalOpen(true)
  }

  const columns = [
    { title: '角色名称', dataIndex: 'name', key: 'name' },
    { title: '角色编码', dataIndex: 'code', key: 'code' },
    { title: '描述', dataIndex: 'description', key: 'description', ellipsis: true },
    {
      title: '类型',
      dataIndex: 'type',
      key: 'type',
      render: (v: string) => <Tag>{v === 'SYSTEM' ? '系统' : '自定义'}</Tag>,
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (v: string) => (
        <Badge status={v === 'ACTIVE' ? 'success' : 'default'} text={v === 'ACTIVE' ? '启用' : '禁用'} />
      ),
    },
    {
      title: '用户数',
      dataIndex: 'userCount',
      key: 'userCount',
    },
    {
      title: '操作',
      key: 'actions',
      render: (_: any, record: any) => (
        <Space>
          <Button type="link" size="small" icon={<EditOutlined />} onClick={() => openEdit(record)}>
            编辑
          </Button>
          {!record.system && (
            <Popconfirm title="确认删除？" onConfirm={() => handleDelete(record.id)}>
              <Button type="link" size="small" danger icon={<DeleteOutlined />}>
                删除
              </Button>
            </Popconfirm>
          )}
        </Space>
      ),
    },
  ]

  return (
    <Card
      title="角色管理"
      extra={<Button type="primary" icon={<PlusOutlined />} onClick={() => { setEditingRole(null); form.resetFields(); setModalOpen(true) }}>新增角色</Button>}
    >
      <Table dataSource={roles} columns={columns} rowKey="id" loading={loading} />
      <Modal
        title={editingRole ? '编辑角色' : '新增角色'}
        open={modalOpen}
        onOk={handleSubmit}
        onCancel={() => { setModalOpen(false); form.resetFields(); setEditingRole(null) }}
      >
        <Form form={form} layout="vertical">
          <Form.Item name="name" label="角色名称" rules={[{ required: true, message: '请输入角色名称' }]}>
            <Input />
          </Form.Item>
          <Form.Item name="code" label="角色编码" rules={[{ required: true, message: '请输入角色编码' }]}>
            <Input disabled={editingRole?.system} placeholder="大写字母和下划线，如 TEACHER" />
          </Form.Item>
          <Form.Item name="description" label="描述">
            <TextArea rows={2} />
          </Form.Item>
        </Form>
      </Modal>
    </Card>
  )
}

// ============================================================
// 权限管理子页面
// ============================================================
const PermissionTab: React.FC = () => {
  const [permissions, setPermissions] = useState<any[]>([])
  const [loading, setLoading] = useState(false)
  const [modalOpen, setModalOpen] = useState(false)
  const [form] = Form.useForm()

  const load = async () => {
    setLoading(true)
    try {
      const res = await adminApi.listPermissions()
      setPermissions(res?.data || [])
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { load() }, [])

  const handleSubmit = async () => {
    const values = await form.validateFields()
    try {
      await adminApi.createPermission(values)
      message.success('权限创建成功')
      setModalOpen(false)
      form.resetFields()
      load()
    } catch (e: any) {
      message.error(e?.response?.data?.message || '创建失败')
    }
  }

  const handleDelete = async (id: number) => {
    try {
      await adminApi.deletePermission(id)
      message.success('权限已删除')
      load()
    } catch (e: any) {
      message.error('删除失败')
    }
  }

  const columns = [
    { title: '权限名称', dataIndex: 'name', key: 'name' },
    { title: '权限编码', dataIndex: 'code', key: 'code' },
    { title: '资源类型', dataIndex: 'resourceType', key: 'resourceType' },
    { title: '方法', dataIndex: 'method', key: 'method' },
    { title: 'URL', dataIndex: 'urlPattern', key: 'urlPattern', ellipsis: true },
    {
      title: '操作',
      key: 'actions',
      render: (_: any, record: any) => (
        <Popconfirm title="确认删除？" onConfirm={() => handleDelete(record.id)}>
          <Button type="link" size="small" danger icon={<DeleteOutlined />}>删除</Button>
        </Popconfirm>
      ),
    },
  ]

  return (
    <Card
      title="权限管理"
      extra={<Button type="primary" icon={<PlusOutlined />} onClick={() => { form.resetFields(); setModalOpen(true) }}>新增权限</Button>}
    >
      <Table dataSource={permissions} columns={columns} rowKey="id" loading={loading} />
      <Modal title="新增权限" open={modalOpen} onOk={handleSubmit} onCancel={() => { setModalOpen(false); form.resetFields() }}>
        <Form form={form} layout="vertical">
          <Form.Item name="name" label="权限名称" rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item name="code" label="权限编码" rules={[{ required: true }]}><Input placeholder="如 USER_CREATE" /></Form.Item>
          <Form.Item name="description" label="描述"><TextArea rows={2} /></Form.Item>
          <Form.Item name="resourceType" label="资源类型" rules={[{ required: true }]}>
            <Select options={[
              { label: 'API', value: 'API' },
              { label: '菜单', value: 'MENU' },
              { label: '按钮', value: 'BUTTON' },
              { label: '字段', value: 'FIELD' },
            ]} />
          </Form.Item>
          <Form.Item name="method" label="HTTP 方法">
            <Select allowClear options={[
              { label: 'GET', value: 'GET' },
              { label: 'POST', value: 'POST' },
              { label: 'PUT', value: 'PUT' },
              { label: 'DELETE', value: 'DELETE' },
            ]} />
          </Form.Item>
          <Form.Item name="urlPattern" label="URL 模式"><Input placeholder="/api/**" /></Form.Item>
        </Form>
      </Modal>
    </Card>
  )
}

// ============================================================
// 菜单管理子页面
// ============================================================
const MenuTab: React.FC = () => {
  const [menus, setMenus] = useState<any[]>([])
  const [loading, setLoading] = useState(false)
  const [modalOpen, setModalOpen] = useState(false)
  const [form] = Form.useForm()

  const load = async () => {
    setLoading(true)
    try {
      const res = await adminApi.listMenus()
      setMenus(res?.data || [])
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { load() }, [])

  const handleSubmit = async () => {
    const values = await form.validateFields()
    try {
      await adminApi.createMenu(values)
      message.success('菜单创建成功')
      setModalOpen(false)
      form.resetFields()
      load()
    } catch (e: any) {
      message.error(e?.response?.data?.message || '创建失败')
    }
  }

  const handleDelete = async (id: number) => {
    try {
      await adminApi.deleteMenu(id)
      message.success('菜单已删除')
      load()
    } catch (e: any) {
      message.error(e?.response?.data?.message || '删除失败')
    }
  }

  const renderTree = (items: any[]): any[] =>
    items.map((item: any) => ({
      ...item,
      key: item.id,
      children: item.children?.length ? renderTree(item.children) : undefined,
    }))

  const columns = [
    { title: '菜单名称', dataIndex: 'name', key: 'name' },
    { title: '路由', dataIndex: 'route', key: 'route' },
    { title: '图标', dataIndex: 'icon', key: 'icon' },
    { title: '类型', dataIndex: 'menuType', key: 'menuType' },
    {
      title: '可见',
      dataIndex: 'visible',
      key: 'visible',
      render: (v: boolean) => (v ? <Tag color="green">是</Tag> : <Tag>否</Tag>),
    },
    { title: '排序', dataIndex: 'sortOrder', key: 'sortOrder' },
    {
      title: '操作',
      key: 'actions',
      render: (_: any, record: any) => (
        <Popconfirm title="确认删除？" onConfirm={() => handleDelete(record.id)}>
          <Button type="link" size="small" danger icon={<DeleteOutlined />}>删除</Button>
        </Popconfirm>
      ),
    },
  ]

  return (
    <Card
      title="菜单管理"
      extra={<Button type="primary" icon={<PlusOutlined />} onClick={() => { form.resetFields(); setModalOpen(true) }}>新增菜单</Button>}
    >
      <Table dataSource={renderTree(menus)} columns={columns} rowKey="id" loading={loading} defaultExpandAllRows pagination={false} />
      <Modal title="新增菜单" open={modalOpen} onOk={handleSubmit} onCancel={() => { setModalOpen(false); form.resetFields() }} width={500}>
        <Form form={form} layout="vertical">
          <Form.Item name="name" label="菜单名称" rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item name="route" label="路由"><Input placeholder="/system" /></Form.Item>
          <Form.Item name="icon" label="图标名"><Input placeholder="SettingOutlined" /></Form.Item>
          <Form.Item name="parentId" label="父级菜单 ID"><InputNumber style={{ width: '100%' }} /></Form.Item>
          <Form.Item name="sortOrder" label="排序"><InputNumber style={{ width: '100%' }} /></Form.Item>
          <Form.Item name="menuType" label="菜单类型">
            <Select options={[
              { label: '目录', value: 'DIR' },
              { label: '菜单', value: 'MENU' },
              { label: '按钮', value: 'BUTTON' },
            ]} />
          </Form.Item>
          <Form.Item name="visible" label="可见" valuePropName="checked">
            <Select options={[{ label: '是', value: true }, { label: '否', value: false }]} />
          </Form.Item>
          <Form.Item name="permissionCode" label="权限编码"><Input /></Form.Item>
        </Form>
      </Modal>
    </Card>
  )
}

// ============================================================
// 用户管理（系统管理用）子页面
// ============================================================
const UserTab: React.FC = () => {
  const [users, setUsers] = useState<any[]>([])
  const [roles, setRoles] = useState<any[]>([])
  const [loading, setLoading] = useState(false)
  const [roleModalOpen, setRoleModalOpen] = useState(false)
  const [selectedUser, setSelectedUser] = useState<any>(null)
  const [userRoleIds, setUserRoleIds] = useState<number[]>([])

  const loadUsers = async () => {
    setLoading(true)
    try {
      const res = await adminApi.listUsers()
      setUsers(res?.data || [])
    } finally {
      setLoading(false)
    }
  }

  const loadRoles = async () => {
    try {
      const res = await adminApi.listRoles()
      setRoles(res?.data || [])
    } catch (_) {}
  }

  useEffect(() => { loadUsers(); loadRoles() }, [])

  const openRoleAssign = async (user: any) => {
    setSelectedUser(user)
    try {
      const res = await adminApi.getUserRoles(user.id)
      setUserRoleIds((res?.data || []).map((r: any) => r.id))
    } catch (_) {
      setUserRoleIds([])
    }
    setRoleModalOpen(true)
  }

  const handleAssignRoles = async () => {
    if (!selectedUser) return
    try {
      await adminApi.assignUserRoles({ userId: selectedUser.id, roleIds: userRoleIds })
      message.success('角色分配成功')
      setRoleModalOpen(false)
    } catch (e: any) {
      message.error(e?.response?.data?.message || '分配失败')
    }
  }

  const columns = [
    { title: '用户名', dataIndex: 'username', key: 'username' },
    { title: '昵称', dataIndex: 'nickname', key: 'nickname' },
    { title: '邮箱', dataIndex: 'email', key: 'email' },
    { title: '手机', dataIndex: 'phone', key: 'phone' },
    {
      title: '状态',
      dataIndex: 'enabled',
      key: 'enabled',
      render: (v: boolean) => <Badge status={v ? 'success' : 'default'} text={v ? '启用' : '禁用'} />,
    },
    {
      title: '操作',
      key: 'actions',
      render: (_: any, record: any) => (
        <Button type="link" size="small" icon={<KeyOutlined />} onClick={() => openRoleAssign(record)}>
          分配角色
        </Button>
      ),
    },
  ]

  return (
    <Card title="用户管理">
      <Table dataSource={users} columns={columns} rowKey="id" loading={loading} />
      <Modal
        title={`分配角色 - ${selectedUser?.username || ''}`}
        open={roleModalOpen}
        onOk={handleAssignRoles}
        onCancel={() => setRoleModalOpen(false)}
      >
        <Select
          mode="multiple"
          style={{ width: '100%' }}
          value={userRoleIds}
          onChange={setUserRoleIds}
          options={roles.map((r: any) => ({ label: r.name, value: r.id }))}
          placeholder="选择角色"
        />
      </Modal>
    </Card>
  )
}

// ============================================================
// 审计日志子页面
// ============================================================
const AuditLogTab: React.FC = () => {
  const [logs, setLogs] = useState<any[]>([])
  const [loading, setLoading] = useState(false)
  const [total, setTotal] = useState(0)
  const [page, setPage] = useState(0)
  const [filters, setFilters] = useState<any>({})

  const loadLogs = async (p: number, f: any) => {
    setLoading(true)
    try {
      const res = await adminApi.queryAuditLogs({ ...f, page: p, size: 20 })
      setLogs(res?.data?.content || [])
      setTotal(res?.data?.totalElements || 0)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { loadLogs(page, filters) }, [page])

  const handleSearch = () => {
    setPage(0)
    loadLogs(0, filters)
  }

  const columns = [
    { title: '用户名', dataIndex: 'username', key: 'username' },
    { title: '操作', dataIndex: 'actionName', key: 'actionName' },
    { title: '目标类型', dataIndex: 'targetType', key: 'targetType' },
    { title: '目标 ID', dataIndex: 'targetId', key: 'targetId' },
    { title: 'IP 地址', dataIndex: 'ipAddress', key: 'ipAddress' },
    {
      title: '结果',
      dataIndex: 'result',
      key: 'result',
      render: (v: string) => (
        <Tag color={v === 'SUCCESS' ? 'green' : v === 'FAILURE' ? 'red' : 'orange'}>{v}</Tag>
      ),
    },
    {
      title: '时间',
      dataIndex: 'createdAt',
      key: 'createdAt',
      render: (v: string) => dayjs(v).format('YYYY-MM-DD HH:mm:ss'),
    },
  ]

  return (
    <Card title="审计日志">
      <Space style={{ marginBottom: 16 }}>
        <Input placeholder="用户名" value={filters.username} onChange={e => setFilters({ ...filters, username: e.target.value })} style={{ width: 150 }} />
        <Input placeholder="操作" value={filters.action} onChange={e => setFilters({ ...filters, action: e.target.value })} style={{ width: 150 }} />
        <Button type="primary" onClick={handleSearch}>查询</Button>
      </Space>
      <Table
        dataSource={logs}
        columns={columns}
        rowKey="id"
        loading={loading}
        pagination={{
          current: page + 1,
          total,
          pageSize: 20,
          onChange: (p) => setPage(p - 1),
        }}
      />
    </Card>
  )
}

// ============================================================
// 数据字典子页面
// ============================================================
const DataDictTab: React.FC = () => {
  const [dictTypes, setDictTypes] = useState<string[]>([])
  const [selectedType, setSelectedType] = useState<string>('')
  const [items, setItems] = useState<any[]>([])
  const [loading, setLoading] = useState(false)
  const [modalOpen, setModalOpen] = useState(false)
  const [editingItem, setEditingItem] = useState<any>(null)
  const [form] = Form.useForm()

  const loadTypes = async () => {
    try {
      const res = await adminApi.listDictTypes()
      setDictTypes(res?.data || [])
      if (res?.data?.length) setSelectedType(res.data[0])
    } catch (_) {}
  }

  const loadItems = async (type: string) => {
    if (!type) return
    setLoading(true)
    try {
      const res = await adminApi.listDictByType(type)
      setItems(res?.data || [])
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { loadTypes() }, [])
  useEffect(() => { loadItems(selectedType) }, [selectedType])

  const handleSubmit = async () => {
    const values = await form.validateFields()
    try {
      if (editingItem) {
        await adminApi.updateDict(editingItem.id, values)
        message.success('更新成功')
      } else {
        await adminApi.createDict({ ...values, dictType: selectedType })
        message.success('创建成功')
      }
      setModalOpen(false)
      form.resetFields()
      setEditingItem(null)
      loadItems(selectedType)
    } catch (e: any) {
      message.error(e?.response?.data?.message || '操作失败')
    }
  }

  const handleDelete = async (id: number) => {
    try {
      await adminApi.deleteDict(id)
      message.success('已删除')
      loadItems(selectedType)
    } catch (_) { message.error('删除失败') }
  }

  const columns = [
    { title: '项编码', dataIndex: 'itemCode', key: 'itemCode' },
    { title: '项值', dataIndex: 'itemValue', key: 'itemValue' },
    { title: '项名称', dataIndex: 'dictName', key: 'dictName' },
    { title: '排序', dataIndex: 'sortOrder', key: 'sortOrder' },
    {
      title: '操作',
      key: 'actions',
      render: (_: any, record: any) => (
        <Space>
          <Button type="link" size="small" icon={<EditOutlined />} onClick={() => { setEditingItem(record); form.setFieldsValue(record); setModalOpen(true) }}>编辑</Button>
          <Popconfirm title="确认删除？" onConfirm={() => handleDelete(record.id)}>
            <Button type="link" size="small" danger icon={<DeleteOutlined />}>删除</Button>
          </Popconfirm>
        </Space>
      ),
    },
  ]

  return (
    <Card
      title="数据字典"
      extra={
        <Space>
          <Select value={selectedType} onChange={setSelectedType} style={{ width: 200 }} options={dictTypes.map(t => ({ label: t, value: t }))} placeholder="选择字典类型" />
          <Button type="primary" icon={<PlusOutlined />} onClick={() => { setEditingItem(null); form.resetFields(); setModalOpen(true) }}>新增</Button>
        </Space>
      }
    >
      <Table dataSource={items} columns={columns} rowKey="id" loading={loading} />
      <Modal title={editingItem ? '编辑字典项' : '新增字典项'} open={modalOpen} onOk={handleSubmit} onCancel={() => { setModalOpen(false); form.resetFields(); setEditingItem(null) }}>
        <Form form={form} layout="vertical" initialValues={{ dictType: selectedType }}>
          <Form.Item name="dictType" label="字典类型"><Input disabled={!!editingItem} /></Form.Item>
          <Form.Item name="itemCode" label="项编码" rules={[{ required: true }]}><Input disabled={!!editingItem} /></Form.Item>
          <Form.Item name="itemValue" label="项值" rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item name="dictName" label="项名称"><Input /></Form.Item>
          <Form.Item name="sortOrder" label="排序"><InputNumber style={{ width: '100%' }} /></Form.Item>
          <Form.Item name="remark" label="备注"><TextArea rows={2} /></Form.Item>
        </Form>
      </Modal>
    </Card>
  )
}

// ============================================================
// 系统配置子页面
// ============================================================
const ConfigTab: React.FC = () => {
  const [configs, setConfigs] = useState<any[]>([])
  const [loading, setLoading] = useState(false)
  const [editingKey, setEditingKey] = useState<string | null>(null)
  const [editValue, setEditValue] = useState('')

  const load = async () => {
    setLoading(true)
    try {
      const res = await adminApi.listConfigs()
      setConfigs(res?.data || [])
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { load() }, [])

  const handleSave = async (configKey: string) => {
    try {
      await adminApi.updateConfig(configKey, { configValue: editValue })
      message.success('配置已更新')
      setEditingKey(null)
      load()
    } catch (e: any) {
      message.error(e?.response?.data?.message || '更新失败')
    }
  }

  const columns = [
    { title: '配置键', dataIndex: 'configKey', key: 'configKey' },
    { title: '配置名', dataIndex: 'configName', key: 'configName' },
    {
      title: '配置值',
      dataIndex: 'configValue',
      key: 'configValue',
      render: (_: any, record: any) =>
        editingKey === record.configKey ? (
          <Space>
            <Input value={editValue} onChange={e => setEditValue(e.target.value)} style={{ width: 300 }} />
            <Button type="primary" size="small" onClick={() => handleSave(record.configKey)}>保存</Button>
            <Button size="small" onClick={() => setEditingKey(null)}>取消</Button>
          </Space>
        ) : (
          <span>{record.configValue}</span>
        ),
    },
    { title: '分组', dataIndex: 'configGroup', key: 'configGroup' },
    { title: '备注', dataIndex: 'remark', key: 'remark' },
    {
      title: '操作',
      key: 'actions',
      render: (_: any, record: any) => (
        <Button type="link" size="small" icon={<EditOutlined />} onClick={() => { setEditingKey(record.configKey); setEditValue(record.configValue) }}>
          编辑
        </Button>
      ),
    },
  ]

  return (
    <Card title="系统配置">
      <Table dataSource={configs} columns={columns} rowKey="id" loading={loading} />
    </Card>
  )
}

// ============================================================
// 主页面
// ============================================================
const SystemManagement: React.FC = () => {
  return (
    <div className="system-management-page">
      <Title level={4} style={{ marginBottom: 16 }}>
        <SettingOutlined /> 系统管理
      </Title>
      <Tabs defaultActiveKey="roles" items={[
        { key: 'roles', label: '角色管理', children: <RoleTab /> },
        { key: 'permissions', label: '权限管理', children: <PermissionTab /> },
        { key: 'menus', label: '菜单管理', children: <MenuTab /> },
        { key: 'users', label: '用户管理', children: <UserTab /> },
        { key: 'audit-log', label: '审计日志', children: <AuditLogTab /> },
        { key: 'data-dict', label: '数据字典', children: <DataDictTab /> },
        { key: 'config', label: '系统配置', children: <ConfigTab /> },
      ]} />
    </div>
  )
}

export default SystemManagement
