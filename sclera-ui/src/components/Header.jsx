import { useApp } from '../context/AppContext.jsx'
import Icon from './Icon.jsx'

export default function Header({ search, onSearch, searchPlaceholder = 'Search...' }) {
  const { view, property, setView, setNav } = useApp()

  const crumbs = []
  crumbs.push({ label: 'Property List', onClick: () => { setView('properties'); setNav('properties') } })
  if (view !== 'properties' && property) {
    crumbs.push({ label: property.name, onClick: () => setView('dashboard') })
  }
  if (view === 'assets') crumbs.push({ label: 'Managed Assets' })
  if (view === 'location') crumbs.push({ label: 'Locations Info' })
  if (view === 'activity') crumbs.push({ label: 'User Activity' })
  if (view === 'networks') crumbs.push({ label: 'My Networks' })

  return (
    <header className="header">
      <div className="breadcrumbs">
        {crumbs.map((c, i) => (
          <span key={i} className="crumb">
            {i > 0 && <span className="crumb-sep">›</span>}
            <button className={`crumb-link ${i === crumbs.length - 1 ? 'current' : ''}`} onClick={c.onClick} disabled={!c.onClick}>
              {c.label}
            </button>
          </span>
        ))}
      </div>

      <div className="header-search">
        <Icon name="search" size={16} />
        <input
          value={search}
          onChange={(e) => onSearch?.(e.target.value)}
          placeholder={searchPlaceholder}
        />
        {search ? <button className="search-clear" onClick={() => onSearch('')}><Icon name="x" size={14} /></button> : null}
      </div>

      <div className="header-right">
        <span className="conn-badge"><span className="dot online" /> Connected to gateway</span>
        <div className="avatar">MU</div>
      </div>
    </header>
  )
}
