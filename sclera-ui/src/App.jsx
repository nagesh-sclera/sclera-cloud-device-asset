import { useState } from 'react'
import Sidebar from './components/Sidebar.jsx'
import Header from './components/Header.jsx'
import ErrorBoundary from './components/ErrorBoundary.jsx'
import PropertyListPage from './pages/PropertyListPage.jsx'
import DashboardPage from './pages/DashboardPage.jsx'
import AssetPage from './pages/AssetPage.jsx'
import LocationPage from './pages/LocationPage.jsx'
import UserActivityPage from './pages/UserActivityPage.jsx'
import NetworkPage from './pages/NetworkPage.jsx'
import WorkOrdersPage from './pages/WorkOrdersPage.jsx'
import QrCodesPage from './pages/QrCodesPage.jsx'
import { useApp } from './context/AppContext.jsx'

export default function App() {
  const { view } = useApp()
  const [collapsed, setCollapsed] = useState(false)
  const [search, setSearch] = useState('')

  const placeholder =
    view === 'assets' ? 'Search assets...' : view === 'dashboard' ? 'Search...' : 'Search properties (e.g. 181)...'

  return (
    <div className={`app ${collapsed ? 'nav-collapsed' : ''}`}>
      <Sidebar collapsed={collapsed} onToggle={() => setCollapsed((c) => !c)} />
      <div className="main">
        <Header search={search} onSearch={setSearch} searchPlaceholder={placeholder} />
        <main className="content">
          <ErrorBoundary>
            {view === 'properties' && <PropertyListPage search={search} />}
            {view === 'dashboard' && <DashboardPage />}
            {view === 'assets' && <AssetPage search={search} onSearch={setSearch} />}
            {view === 'location' && <LocationPage />}
            {view === 'activity' && <UserActivityPage />}
            {view === 'networks' && <NetworkPage />}
            {view === 'workorders' && <WorkOrdersPage />}
            {view === 'qrcodes' && <QrCodesPage />}
          </ErrorBoundary>
        </main>
      </div>
    </div>
  )
}
