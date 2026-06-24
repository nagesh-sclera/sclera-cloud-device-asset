import { useCallback, useEffect, useState } from 'react'
import Icon from '../components/Icon.jsx'
import MetricCard from '../components/MetricCard.jsx'
import { Skeleton } from '../components/Skeleton.jsx'
import { useApp } from '../context/AppContext.jsx'
import { useToast } from '../context/ToastContext.jsx'
import { statusLabel, statusTone, TICKET_STATUSES, TICKET_CATEGORIES, categoryLabel } from '../config.js'
import api from '../services/api.js'
import TicketModal from '../components/TicketModal.jsx'
import TicketDetailDrawer from '../components/TicketDetailDrawer.jsx'

const fmtDate = (ts) => {
  const n = Number(ts); if (!n) return '—'
  return new Date(n).toLocaleString(undefined, { day: '2-digit', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit' })
}

const PAGE_SIZE = 10

export default function WorkOrdersPage() {
  const { ctx } = useApp()
  const toast = useToast()
  const [counts, setCounts] = useState(null)
  const [tickets, setTickets] = useState(null)
  const [modal, setModal] = useState(null) // null | { ticket } for create/edit
  const [openId, setOpenId] = useState(null)
  const [filter, setFilter] = useState({ status: [], category: '', assignee_user_email: '', device_id: '' })
  const [search, setSearch] = useState('')
  const [page, setPage] = useState(1)

  const load = useCallback(async () => {
    setTickets(null)
    const body = {}
    if (filter.status.length) body.status = filter.status
    if (filter.category.trim()) body.category = filter.category.trim()
    if (filter.assignee_user_email.trim()) body.assignee_user_email = filter.assignee_user_email.trim()
    if (filter.device_id.trim()) body.device_id = filter.device_id.trim()
    try {
      const [c, list] = await Promise.all([
        api.ticketCounts(body, ctx),
        api.listTickets(body, { ...ctx, searchkey: search.trim() || 'null', pageno: page, pagesize: PAGE_SIZE }),
      ])
      setCounts(c || {})
      setTickets(Array.isArray(list) ? list : [])
    } catch (e) {
      toast.error(`Load work orders failed: ${e.message}`)
      setCounts({}); setTickets([])
    }
  }, [ctx, toast, filter, search, page])

  useEffect(() => { load() }, [load])

  useEffect(() => { setPage(1) }, [filter, search])

  const metricItems = [
    { label: 'All', value: counts?.all, tone: 'default' },
    { label: 'New', value: counts?.new, tone: 'default' },
    { label: 'Open', value: counts?.open, tone: 'online' },
    { label: 'On Hold', value: counts?.on_hold, tone: 'default' },
    { label: 'Closed', value: counts?.closed, tone: 'muted' },
  ]

  return (
    <div className="wo-page">
      <div className="page-head">
        <h1>Work Orders</h1>
        <button className="btn btn-primary sm" onClick={() => setModal({})}><Icon name="plus" size={14} /> New Work Order</button>
        <button className="btn btn-ghost sm" onClick={load} title="Refresh"><Icon name="refresh" size={14} /> Refresh</button>
      </div>

      <div className="metric-grid">
        <MetricCard title="Tickets" items={metricItems} accent="blue" />
      </div>

      <div className="card wo-filters">
        <div className="status-chips">
          {TICKET_STATUSES.map((s) => (
            <button key={s} className={`chip ${filter.status.includes(s) ? 'active' : ''}`}
              onClick={() => setFilter((f) => ({ ...f, status: f.status.includes(s) ? f.status.filter((x) => x !== s) : [...f.status, s] }))}>
              {statusLabel(s)}
            </button>
          ))}
        </div>
        <input className="wo-search" placeholder="Search…" value={search} onChange={(e) => setSearch(e.target.value)} />
        <select className="wo-search" value={filter.category} onChange={(e) => setFilter((f) => ({ ...f, category: e.target.value }))}>
          <option value="">All categories</option>
          {TICKET_CATEGORIES.map((c) => <option key={c} value={c}>{categoryLabel(c)}</option>)}
        </select>
        <input className="wo-search" placeholder="Assignee email" value={filter.assignee_user_email} onChange={(e) => setFilter((f) => ({ ...f, assignee_user_email: e.target.value }))} />
        <input className="wo-search" placeholder="Device ID" value={filter.device_id} onChange={(e) => setFilter((f) => ({ ...f, device_id: e.target.value }))} />
      </div>

      <div className="card">
        {tickets == null ? (
          <div className="log-list"><Skeleton w="100%" h={44} /><Skeleton w="100%" h={44} /><Skeleton w="100%" h={44} /></div>
        ) : tickets.length === 0 ? (
          <div className="empty-state"><Icon name="clipboard" size={22} /><p>No work orders yet</p></div>
        ) : (
          <table className="data-table">
            <thead><tr><th>Number</th><th>Name</th><th>Status</th><th>Category</th><th>Device</th><th>Assignee</th><th>Created</th></tr></thead>
            <tbody>
              {tickets.map((t) => (
                <tr key={t.id} className="clickable" onClick={() => setOpenId(t.id)}>
                  <td>{t.number || '—'}</td>
                  <td>{t.name || '—'}</td>
                  <td><span className={`badge tone-${statusTone(t.status)}`}>{statusLabel(t.status)}</span></td>
                  <td>{t.category ? categoryLabel(t.category) : '—'}</td>
                  <td>{t.device_id || '—'}</td>
                  <td>{t.assignee_user_email || '—'}</td>
                  <td>{fmtDate(t.created_at)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
      <div className="pager">
        <button className="btn btn-ghost sm" disabled={page <= 1} onClick={() => setPage((p) => Math.max(1, p - 1))}>Prev</button>
        <span>Page {page} of {Math.max(1, Math.ceil((counts?.all || 0) / PAGE_SIZE))}</span>
        <button className="btn btn-ghost sm" disabled={page >= Math.ceil((counts?.all || 0) / PAGE_SIZE)} onClick={() => setPage((p) => p + 1)}>Next</button>
      </div>
      <TicketDetailDrawer
        ticketId={openId}
        onClose={() => setOpenId(null)}
        onChanged={load}
        onEdit={(t) => { setOpenId(null); setModal({ ticket: t }) }}
      />
      <TicketModal
        open={!!modal}
        ticket={modal?.ticket}
        onClose={() => setModal(null)}
        onSaved={load}
      />
    </div>
  )
}
