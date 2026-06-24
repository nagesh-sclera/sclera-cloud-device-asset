import { useEffect, useState } from 'react'
import Icon from './Icon.jsx'
import { Skeleton } from './Skeleton.jsx'
import { useApp } from '../context/AppContext.jsx'
import { useToast } from '../context/ToastContext.jsx'
import { statusLabel, statusTone, categoryLabel } from '../config.js'
import api from '../services/api.js'

const fmt = (ts) => { const n = Number(ts); return n ? new Date(n).toLocaleString() : '—' }

export default function TicketDetailDrawer({ ticketId, onClose, onChanged, onEdit }) {
  const { ctx } = useApp()
  const toast = useToast()
  const [ticket, setTicket] = useState(null)
  const [history, setHistory] = useState(null)
  const [busy, setBusy] = useState(false)

  useEffect(() => {
    if (!ticketId) return
    setTicket(null); setHistory(null)
    api.getTicket(ticketId, ctx).then(setTicket).catch((e) => { toast.error(e.message); onClose?.() })
    api.ticketHistory(ticketId, ctx).then((h) => setHistory(Array.isArray(h) ? h : [])).catch(() => setHistory([]))
  }, [ticketId]) // eslint-disable-line react-hooks/exhaustive-deps

  if (!ticketId) return null

  const remove = async () => {
    if (!window.confirm('Delete this work order?')) return
    setBusy(true)
    try { await api.deleteTicket(ticketId, ctx); toast.success('Work order deleted'); onChanged?.(); onClose?.() }
    catch (e) { toast.error(`Delete failed: ${e.message}`) }
    finally { setBusy(false) }
  }

  return (
    <div className="drawer-backdrop" onClick={onClose}>
      <aside className="drawer" onClick={(e) => e.stopPropagation()}>
        <div className="drawer-head">
          <div>
            <div className="drawer-title">{ticket?.name || ticket?.number || 'Work Order'}</div>
            {ticket?.status && <span className={`badge tone-${statusTone(ticket.status)}`}>{statusLabel(ticket.status)}</span>}
          </div>
          <div className="drawer-actions">
            <button className="icon-btn" disabled={!ticket} onClick={() => onEdit?.(ticket)} title="Edit"><Icon name="edit" size={15} /></button>
            <button className="icon-btn danger" disabled={busy} onClick={remove} title="Delete"><Icon name="trash" size={15} /></button>
            <button className="icon-btn" onClick={onClose} title="Close"><Icon name="x" size={16} /></button>
          </div>
        </div>

        {ticket == null ? <div className="dp-fields"><Skeleton w="100%" h={40} /><Skeleton w="100%" h={40} /></div> : (
          <div className="dp-fields">
            <Row k="Number" v={ticket.number} />
            <Row k="Category" v={ticket.category ? categoryLabel(ticket.category) : ''} />
            <Row k="Request Type" v={ticket.request_type} />
            <Row k="Device" v={ticket.device_id} />
            <Row k="Assignee" v={ticket.assignee_user_email} />
            <Row k="Created" v={`${fmt(ticket.created_at)}${ticket.created_by ? ' · ' + ticket.created_by : ''}`} />
            <Row k="Message" v={ticket.user_message} />
            <Row k="Description" v={ticket.description} />
          </div>
        )}

        <div className="dp-section">
          <div className="dp-section-title">History</div>
          {history == null ? <div className="log-list"><Skeleton w="100%" h={36} /></div>
            : history.length === 0 ? <div className="empty-state"><Icon name="list" size={20} /><p>No history</p></div>
            : <div className="log-list">{history.map((h) => (
                <div className="log-item" key={h.id}>
                  <span className={`badge tone-${statusTone(h.status)}`}>{statusLabel(h.status)}</span>
                  <div className="log-main"><div className="log-msg">{h.message || h.action_message}</div>
                    <div className="log-meta">{h.created_by || '—'} · {fmt(h.timestamp)}</div></div>
                </div>
              ))}</div>}
        </div>
      </aside>
    </div>
  )
}
function Row({ k, v }) { return <div className="dp-row"><span className="dp-k">{k}</span><span className="dp-v">{v || '—'}</span></div> }
