import { NAV_ITEMS, NAV_FOOTER, DEMO } from '../config.js'
import { useApp } from '../context/AppContext.jsx'
import Icon from './Icon.jsx'

export default function Sidebar({ collapsed, onToggle }) {
  const { nav, setNav, setView } = useApp()

  const go = (key) => {
    setNav(key)
    if (key === 'properties') setView('properties')
    if (key === 'workorders') setView('workorders')
    if (key === 'qrcodes') setView('qrcodes')
  }

  return (
    <aside className={`sidebar ${collapsed ? 'collapsed' : ''}`}>
      <div className="sidebar-logo" onClick={onToggle} title="Toggle menu">
        <span className="logo-mark">◎</span>
        {!collapsed && <span className="logo-text">Optima</span>}
      </div>

      <nav className="sidebar-nav">
        {NAV_ITEMS.map((item) => (
          <button
            key={item.key}
            className={`nav-item ${nav === item.key ? 'active' : ''}`}
            onClick={() => go(item.key)}
            title={item.label}
          >
            <Icon name={item.icon} />
            {!collapsed && <span>{item.label}</span>}
          </button>
        ))}
      </nav>

      <div className="sidebar-footer">
        {NAV_FOOTER.map((item) => (
          <button key={item.key} className="nav-item subtle" title={item.label}>
            <Icon name={item.icon} />
            {!collapsed && <span>{item.label}</span>}
          </button>
        ))}

        <div className="nav-user">
          <div className="avatar">MU</div>
          {!collapsed && (
            <div className="nav-user-meta">
              <span className="nav-user-name">{DEMO.displayName}</span>
              <span className="nav-user-role">{DEMO.user}</span>
            </div>
          )}
        </div>

        <button className="nav-item subtle" title="Settings">
          <Icon name="settings" />
          {!collapsed && <span>Settings</span>}
        </button>
      </div>
    </aside>
  )
}
