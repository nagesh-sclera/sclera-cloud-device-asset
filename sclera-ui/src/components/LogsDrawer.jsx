import { useMemo, useState } from 'react'
import Icon from './Icon.jsx'
import { Skeleton } from './Skeleton.jsx'
import { useApi, useDebounce } from '../hooks/useApi.js'
import { useApp } from '../context/AppContext.jsx'
import api from '../services/api.js'

const ACTIONS = ['ALL', 'ADD', 'UPDATE', 'DELETE']

// Activity/audit log for the whole property (GET /vdms/audit-log).
// variant: 'drawer' (slide-in, default) | 'modal' (centered popup).
export default function LogsDrawer({ onClose, variant = 'drawer' }) {
  const { ctx } = useApp()
  const [action, setAction] = useState('ALL')
  const [search, setSearch] = useState('')
  const q = useDebounce(search, 300)
  const { data, loading, error, refetch } = useApi(() => api.auditLog({ vdmsId: ctx.vdmsId, page: 0, size: 200 }), [])

  const rows = useMemo(() => {
    let list = Array.isArray(data) ? data : []
    if (action !== 'ALL') list = list.filter((l) => (l.action || '').toUpperCase() === action)
    if (q) {
      const s = q.toLowerCase()
      list = list.filter((l) => (l.message || '').toLowerCase().includes(s) || (l.userEmail || '').toLowerCase().includes(s))
    }
    return list
  }, [data, action, q])

  const body = (
    <>
        <div className="dp-head">
          <div className="dp-title"><Icon name="list" size={18} /><span className="dp-name">Activity Logs</span></div>
          <div className="dp-head-actions">
            <button className="icon-btn" onClick={() => refetch()} title="Refresh"><Icon name="refresh" size={15} /></button>
            <button className="icon-btn" onClick={onClose} aria-label="Close"><Icon name="x" size={16} /></button>
          </div>
        </div>

        <div className="logs-controls">
          <div className="header-search sm">
            <Icon name="search" size={14} />
            <input value={search} onChange={(e) => setSearch(e.target.value)} placeholder="Search logs..." />
          </div>
          <div className="filter-tabs sm">
            {ACTIONS.map((a) => <button key={a} className={`tab ${action === a ? 'active' : ''}`} onClick={() => setAction(a)}>{a}</button>)}
          </div>
        </div>

        <div className="dp-body">
          {loading ? (
            Array.from({ length: 8 }).map((_, i) => <div className="log-item" key={i}><Skeleton w={60} h={20} /><Skeleton w="70%" h={12} /></div>)
          ) : error ? (
            <div className="empty-state"><Icon name="info" size={22} /><p>Couldn't load logs</p><span className="muted">{error.message}</span><button className="btn btn-primary sm" onClick={() => refetch()}>Retry</button></div>
          ) : rows.length === 0 ? (
            <div className="empty-state"><Icon name="list" size={22} /><p>No log entries</p></div>
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
    </>
  )

  if (variant === 'modal') {
    return (
      <div className="modal-overlay" onMouseDown={(e) => { if (e.target === e.currentTarget) onClose() }}>
        <div className="modal logs-modal" role="dialog" aria-modal="true" aria-label="Activity logs">{body}</div>
      </div>
    )
  }
  return (
    <>
      <div className="panel-scrim" onClick={onClose} />
      <aside className="logs-drawer" role="dialog" aria-label="Activity logs">{body}</aside>
    </>
  )
}
