import React from 'react'
import ReactDOM from 'react-dom/client'
import { ConfigProvider } from 'antd'
import zhCN from 'antd/locale/zh_CN'
import dayjs from 'dayjs'
import 'dayjs/locale/zh-cn'
import App from './App.tsx'
import './index.css'

dayjs.locale('zh-cn')

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <ConfigProvider
      locale={zhCN}
      theme={{
        token: {
          colorPrimary: '#2f6f63',
          colorSuccess: '#2f8a57',
          colorWarning: '#d18a22',
          colorError: '#c94848',
          colorInfo: '#3c63b8',
          colorText: '#1e2b28',
          colorTextSecondary: '#687772',
          colorBorder: '#dde5df',
          colorBgLayout: '#f5f7f4',
          borderRadius: 8,
          fontFamily:
            "-apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Microsoft YaHei', Arial, sans-serif",
          wireframe: false,
        },
        components: {
          Button: {
            borderRadius: 6,
            controlHeightLG: 44,
          },
          Card: {
            borderRadiusLG: 8,
            headerBg: '#ffffff',
          },
          Menu: {
            itemBorderRadius: 6,
          },
          Input: {
            borderRadius: 6,
          },
        },
      }}
    >
      <App />
    </ConfigProvider>
  </React.StrictMode>,
)
