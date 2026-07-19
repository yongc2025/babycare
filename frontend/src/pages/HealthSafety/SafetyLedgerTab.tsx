import React, { useEffect, useState } from 'react'
import { Button, Card, DatePicker, Empty, Form, Input, InputNumber, List, Modal, Select, Space, Switch, Tag, message } from 'antd'
import { PlusOutlined, SafetyCertificateOutlined } from '@ant-design/icons'
import dayjs from 'dayjs'
import { safetyLedgerAPI } from '../../services/api'
import type { SafetyLedger, SafetyLedgerTemplate } from '../../types'

interface ApiResponse<T> {
  success: boolean
  data?: T
  message?: string
}

const unwrap = <T,>(response: ApiResponse<T> | T): T => {
  if (response && typeof response === 'object' && 'success' in response) {
    const apiResponse = response as ApiResponse<T>
    if (!apiResponse.success || apiResponse.data === undefined) throw new Error(apiResponse.message || '请求失败')
    return apiResponse.data
  }
  return response as T
}

interface SafetyLedgerTabProps {
  organizationId?: string
}

const SafetyLedgerTab: React.FC<SafetyLedgerTabProps> = ({ organizationId }) => {
  const [ledgers, setLedgers] = useState<SafetyLedger[]>([])
  const [templates, setTemplates] = useState<SafetyLedgerTemplate[]>([])
  const [templateModalOpen, setTemplateModalOpen] = useState(false)
  const [editingTemplate, setEditingTemplate] = useState<SafetyLedgerTemplate | null>(null)
  const [templateForm] = Form.useForm()
  const today = dayjs().format('YYYY-MM-DD')

  const loadData = async () => {
    if (!organizationId) return
    try {
      const [ledgerData, templateData] = await Promise.all([
        safetyLedgerAPI.getOrganizationLedgers(organizationId, { startDate: today, endDate: today }),
        safetyLedgerAPI.getOrganizationTemplates(organizationId),
      ])
      setLedgers(unwrap<SafetyLedger[]>(ledgerData) || [])
      setTemplates(unwrap<SafetyLedgerTemplate[]>(templateData) || [])
    } catch { /* ignore */ }
  }

  useEffect(() => {
    loadData()
  }, [organizationId])

  const processLedger = async (id: string) => {
    await safetyLedgerAPI.markProcessing(id)
    message.success('台账已标记处理中')
    loadData()
  }

  const closeLedger = async (id: string) => {
    await safetyLedgerAPI.closeLedger(id)
    message.success('台账已关闭')
    loadData()
  }

  const handleGenerateTasks = async () => {
    if (!organizationId) return
    const r = await safetyLedgerAPI.generateTasks(organizationId)
    message.success(unwrap<string>(r))
    loadData()
  }

  const handleCheckOverdue = async () => {
    if (!organizationId) return
    const r = await safetyLedgerAPI.checkOverdue(organizationId)
    message.success(unwrap<string>(r))
    loadData()
  }

  const openTemplateModal = (tpl?: SafetyLedgerTemplate) => {
    setEditingTemplate(tpl || null)
    if (tpl) {
      templateForm.setFieldsValue({ ...tpl, nextGenerateDate: tpl.nextGenerateDate ? dayjs(tpl.nextGenerateDate) : undefined })
    } else {
      templateForm.resetFields()
    }
    setTemplateModalOpen(true)
  }

  const handleSaveTemplate = async () => {
    const values = await templateForm.validateFields()
    const formData = {
      ...values,
      organizationId,
      nextGenerateDate: values.nextGenerateDate?.format('YYYY-MM-DD'),
    }
    if (editingTemplate) {
      await safetyLedgerAPI.updateTemplate(editingTemplate.id, formData)
      message.success('模板已更新')
    } else {
      await safetyLedgerAPI.createTemplate(formData)
      message.success('模板已创建')
    }
    templateForm.resetFields()
    setEditingTemplate(null)
    setTemplateModalOpen(false)
    loadData()
  }

  const handleDeleteTemplate = async (id: string) => {
    await safetyLedgerAPI.deleteTemplate(id)
    message.success('模板已删除')
    loadData()
  }

  return (
    <>
      <Card title={<><SafetyCertificateOutlined /> 安全卫生台账</>}
        extra={<Space>
          <Button onClick={handleGenerateTasks}>生成任务</Button>
          <Button onClick={handleCheckOverdue}>检查逾期</Button>
          <Button icon={<PlusOutlined />} onClick={() => openTemplateModal()}>模板管理</Button>
        </Space>}>
        <List dataSource={ledgers} locale={{ emptyText: <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="暂无安全卫生台账" /> }}
          renderItem={(item) => (
            <List.Item actions={[
              item.status === 'OPEN' && <Button type="link" onClick={() => processLedger(item.id)}>处理中</Button>,
              item.status !== 'CLOSED' && <Button type="link" onClick={() => closeLedger(item.id)}>关闭</Button>,
            ]}>
              <List.Item.Meta title={item.title}
                description={`${item.ledgerTypeDescription || item.ledgerType} · ${item.responsiblePerson || '未填责任人'}${item.dueAt ? ` · 截止:${dayjs(item.dueAt).format('MM-DD')}` : ''}`} />
              <Tag color={item.status === 'CLOSED' ? 'green' : item.status === 'OVERDUE' ? 'red' : 'orange'}>{item.statusDescription || item.status}</Tag>
            </List.Item>
          )} />
      </Card>
      <Modal title={editingTemplate ? '编辑台账模板' : '台账模板管理'} open={templateModalOpen}
        onOk={handleSaveTemplate} onCancel={() => { setTemplateModalOpen(false); setEditingTemplate(null); templateForm.resetFields(); }}
        destroyOnClose width={640}>
        <Space direction="vertical" style={{ width: '100%' }}>
          {!editingTemplate && templates.length > 0 && (
            <List size="small" dataSource={templates} locale={{ emptyText: '暂无模板' }}
              renderItem={(item) => (
                <List.Item actions={[
                  <Button type="link" onClick={() => openTemplateModal(item)}>编辑</Button>,
                  <Button type="link" danger onClick={() => handleDeleteTemplate(item.id)}>删除</Button>,
                ]}>
                  <List.Item.Meta title={`${item.title} (${item.frequencyDescription || item.frequency})`}
                    description={`${item.ledgerTypeDescription || item.ledgerType} · ${item.responsiblePerson || '未填责任人'} · 下次:${item.nextGenerateDate ? dayjs(item.nextGenerateDate).format('MM-DD') : '-'}`} />
                  <Tag color={item.isActive ? 'green' : 'default'}>{item.isActive ? '启用' : '停用'}</Tag>
                </List.Item>
              )} />
          )}
          <div style={{ fontWeight: 500, marginTop: 8 }}>{editingTemplate ? '编辑模板' : '新建模板'}</div>
          <Form form={templateForm} layout="vertical" initialValues={{ ledgerType: 'DISINFECTION', frequency: 'DAILY', isActive: true }}>
            <Form.Item name="ledgerType" label="台账类型" rules={[{ required: true }]}><Select options={[
              { label: '消毒', value: 'DISINFECTION' }, { label: '食品留样', value: 'FOOD_SAMPLE' },
              { label: '设施巡检', value: 'FACILITY_INSPECTION' }, { label: '消防安全', value: 'FIRE_SAFETY' },
              { label: '安全教育', value: 'SAFETY_EDUCATION' }, { label: '事故跟进', value: 'INCIDENT_FOLLOWUP' },
              { label: '其他', value: 'OTHER' },
            ]} /></Form.Item>
            <Form.Item name="frequency" label="频率" rules={[{ required: true }]}><Select options={[
              { label: '每天', value: 'DAILY' }, { label: '每周', value: 'WEEKLY' },
              { label: '每两周', value: 'BIWEEKLY' }, { label: '每月', value: 'MONTHLY' },
            ]} /></Form.Item>
            <Form.Item noStyle shouldUpdate={(prev, cur) => prev.frequency !== cur.frequency}>
              {({ getFieldValue }) => {
                const freq = getFieldValue('frequency')
                return freq === 'WEEKLY' || freq === 'BIWEEKLY' ? (
                  <Form.Item name="dayOfWeek" label="星期" rules={[{ required: true }]}><Select options={[
                    { label: '周一', value: 1 }, { label: '周二', value: 2 }, { label: '周三', value: 3 },
                    { label: '周四', value: 4 }, { label: '周五', value: 5 }, { label: '周六', value: 6 }, { label: '周日', value: 7 },
                  ]} /></Form.Item>
                ) : freq === 'MONTHLY' ? (
                  <Form.Item name="dayOfMonth" label="每月几号" rules={[{ required: true }]}><InputNumber min={1} max={28} className="full-width" /></Form.Item>
                ) : null
              }}
            </Form.Item>
            <Form.Item name="title" label="标题" rules={[{ required: true }]}><Input /></Form.Item>
            <Space style={{ width: '100%' }} size={12}>
              <Form.Item name="responsiblePerson" label="责任人" style={{ width: '50%' }}><Input /></Form.Item>
              <Form.Item name="location" label="位置" style={{ width: '50%' }}><Input /></Form.Item>
            </Space>
            <Form.Item name="content" label="模板内容"><Input.TextArea rows={2} /></Form.Item>
            <Form.Item name="nextGenerateDate" label="首次生成日期"><DatePicker className="full-width" /></Form.Item>
            <Form.Item name="isActive" label="启用" valuePropName="checked"><Switch /></Form.Item>
          </Form>
        </Space>
      </Modal>
    </>
  )
}

export default SafetyLedgerTab
