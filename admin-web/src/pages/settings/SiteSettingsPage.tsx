import { useEffect, useState } from 'react'
import { Button, Form, Input, InputNumber, Spin, Switch, Tabs, message } from 'antd'
import { SaveOutlined } from '@ant-design/icons'
import { configApi } from '@/api'
import { errorMessage } from '@/api/client'
import { PageTitle } from '@/components/PageTitle'
import type { SiteConfig } from '@/types'

const groupLabels: Record<string, string> = {
  basic: '基础信息', hero: '首屏', footer: '页脚', seo: 'SEO', social: '社交链接', feature: '功能开关', upload: '上传设置',
}

export function SiteSettingsPage() {
  const [groups, setGroups] = useState<Record<string, SiteConfig[]>>({})
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [form] = Form.useForm()

  useEffect(() => {
    configApi.list().then(data => {
      setGroups(data)
      const values: Record<string, unknown> = {}
      Object.values(data).flat().forEach(item => {
        values[item.configKey] = item.valueType === 'BOOLEAN'
          ? item.configValue === 'true'
          : item.valueType === 'NUMBER' ? Number(item.configValue || 0) : item.configValue
      })
      form.setFieldsValue(values)
    }).catch(error => message.error(errorMessage(error))).finally(() => setLoading(false))
  }, [form])

  const save = async () => {
    try {
      const values = form.getFieldsValue(true)
      setSaving(true)
      const flat = Object.values(groups).flat()
      await configApi.save(flat.map(item => ({
        configKey: item.configKey,
        configValue: values[item.configKey] === undefined
          ? item.configValue ?? ''
          : item.valueType === 'BOOLEAN' ? String(!!values[item.configKey]) : String(values[item.configKey] ?? ''),
      })))
      message.success('站点配置已保存')
    } catch (error) { message.error(errorMessage(error)) } finally { setSaving(false) }
  }

  if (loading) return <div style={{ padding: 100, textAlign: 'center' }}><Spin size="large" /></div>

  return (
    <>
      <PageTitle title="网站配置" eyebrow="Settings" description="调整站点身份、首屏文案、SEO 与功能开关。" extra={<Button type="primary" icon={<SaveOutlined />} loading={saving} onClick={save}>保存配置</Button>} />
      <div className="panel"><div className="panel-body">
        <Form form={form} layout="vertical">
          <Tabs
            tabPosition="left"
            items={Object.entries(groups).map(([key, configs]) => ({
              key,
              label: groupLabels[key] || key,
              children: (
                <div style={{ maxWidth: 720, padding: '4px 10px 20px' }}>
                  {configs.map(config => (
                    <div className="config-item" key={config.configKey}>
                      <label>{config.label}<span className="config-key" style={{ marginLeft: 8 }}>{config.configKey}</span></label>
                      <Form.Item name={config.configKey} noStyle>
                        {config.valueType === 'BOOLEAN' ? <Switch checkedChildren="开启" unCheckedChildren="关闭" /> :
                          config.valueType === 'NUMBER' ? <InputNumber min={0} style={{ width: '100%' }} /> :
                          config.valueType === 'TEXT' ? <Input.TextArea autoSize={{ minRows: 2, maxRows: 5 }} /> : <Input />}
                      </Form.Item>
                      {config.description && <small>{config.description}</small>}
                    </div>
                  ))}
                </div>
              ),
            }))}
          />
        </Form>
      </div></div>
    </>
  )
}
