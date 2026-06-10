import { useMemo, useState } from 'react'
import Icon from '../components/Icon.jsx'
import { Skeleton } from '../components/Skeleton.jsx'
import { useApi, useDebounce } from '../hooks/useApi.js'
import { useApp } from '../context/AppContext.jsx'
import api from '../services/api.js'

const ACTIONS = ['ALL', 'ADD', 'UPDATE', 'DELETE']

// Full-page activity / audit log for the property (GET /vdms/audit-log).
export default function UserActivityPage() {
  const { ctx, setView } = useApp()
  const [action, setAction] = useState('ALL')
  const [search, setSearch] = useState('')
  const q = useDebounce(search, 300)
  const { data, loading, error, refetch } = useApi(() => api.auditLog({ vdmsId: ctx.vdmsId, page: 0, size: 300 }), [])

  const rows = useMemo(() => {
    let list = Array.isArray(data) ? data : []
    if (action !== 'ALL') list = list.filter((l) => (l.action || '').toUpperCase() === action)
    if (q) {
      const s = q.toLowerCase()
      list = list.filter((l) => (l.message || '').toLowerCase().includes(s) || (l.userEmail || '').toLowerCase().includes(s))
    }
    return list
  }, [data, action, q])

  return (
    <div className="page">
      <div className="page-head">
        <div className="page-title">
          <button className="icon-btn" onClick={() => setView('dashboard')} title="Back"><Icon name="portal" size={16} /></button>
          <h1>User Activity</h1>
        </div>
        <button className="icon-btn" onClick={() => refetch()} title="Refresh"><Icon name="refresh" size={16} /></button>
      </div>

      <div className="loc-filters" style={{ marginBottom: 14 }}>
        <div className="filter-tabs">
          {ACTIONS.map((a) => <button key={a} className={`tab ${action === a ? 'active' : ''}`} onClick={() => setAction(a)}>{a}</button>)}
        </div>
        <div className="header-search" style={{ flex: 1, minWidth: 220 }}>
          <Icon name="search" size={16} />
          <input value={search} onChange={(e) => setSearch(e.target.value)} placeholder="Search activity..." />
        </div>
      </div>

      <div className="loc-count">{rows.length} activit{rows.length === 1 ? 'y' : 'ies'}</div>

      {loading ? (
        <div className="log-list">{Array.from({ length: 10 }).map((_, i) => <div className="log-item" key={i}><Skeleton w={64} h={22} /><Skeleton w="70%" h={12} /></div>)}</div>
      ) : error ? (
        <div className="empty-state"><Icon name="info" size={26} /><p>Couldn't load activity</p><span className="muted">{error.message}</span><button className="btn btn-primary sm" onClick={() => refetch()}>Retry</button></div>
      ) : rows.length === 0 ? (
        <div className="empty-state"><Icon name="list" size={26} /><p>No activity</p></div>
      ) : (
        <div className="log-list">
          {rows.map((l) => (
            <div className="log-item" key={l.id}>
              <span className={`log-badge ${(l.action || '').toLowerCase()}`}>{l.action}</span>
              <div className="log-main">
                <div className="log-msg">{l.message}</div>
                <div className="log-meta">
                  <span className={`log-status ${l.status}`}>{l.status}</span> · {l.userEmail} · {l.createdAt ? new Date(l.createdAt).toLocaleString() : ''}
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
