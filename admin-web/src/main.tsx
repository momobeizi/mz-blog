import React from 'react'
import ReactDOM from 'react-dom/client'
import { ConfigProvider, App as AntApp } from 'antd'
import zhCN from 'antd/locale/zh_CN'
import dayjs from 'dayjs'
import 'dayjs/locale/zh-cn'
import { App } from './app/App'
import './styles/global.css'

dayjs.locale('zh-cn')

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <ConfigProvider
      locale={zhCN}
      theme={{
        token: {
          colorPrimary: '#3f7353',
          colorInfo: '#315f88',
          colorSuccess: '#3f7353',
          colorWarning: '#865b1d',
          colorError: '#9f4a45',
          borderRadius: 4,
          colorBgLayout: '#f7f3e9',
          colorBgContainer: '#fffdf6',
          colorBgElevated: '#fffdf6',
          colorText: '#4a5751',
          colorTextHeading: '#2c3531',
          colorTextSecondary: '#6f6a5c',
          colorBorder: '#ddd0b5',
          colorBorderSecondary: '#ece3d0',
          colorLink: '#3f7353',
          fontFamily: '"Noto Sans SC", "PingFang SC", "Microsoft YaHei", sans-serif',
        },
        components: {
          Button: {
            controlHeight: 34,
            fontWeight: 600,
            defaultBg: '#fffdf6',
            defaultBorderColor: '#ddd0b5',
            primaryShadow: '0 2px 0 rgba(44, 53, 49, .16)',
          },
          Table: {
            headerBg: '#f8f2e2',
            headerColor: '#6f6a5c',
            borderColor: '#ece3d0',
            rowHoverBg: '#f8f2e2',
          },
          Card: { boxShadowTertiary: '0 2px 6px rgba(82, 64, 38, .14)' },
          Input: { controlHeight: 34, activeShadow: '0 0 0 3px #d8e8d2', activeBorderColor: '#6fa67c' },
          Select: { controlHeight: 34 },
          Menu: { itemSelectedBg: '#d8e8d2', itemSelectedColor: '#315d43', itemBorderRadius: 3 },
          Layout: { bodyBg: '#f7f3e9', headerBg: '#fffdf6', siderBg: '#f8f2e2' },
        },
      }}
    >
      <AntApp>
        <App />
      </AntApp>
    </ConfigProvider>
  </React.StrictMode>,
)
