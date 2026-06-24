import { useEffect, useState } from 'react'
import Icon from './Icon.jsx'
import { Spinner } from './Skeleton.jsx'
import { useToast } from '../context/ToastContext.jsx'
import { useApp } from '../context/AppContext.jsx'
import { TICKET_STATUSES, statusLabel, TICKET_CATEGORIES, categoryLabel } from '../config.js'
import api from '../services/api.js'

const BLANK = { name: '', status: 'new', category: '', request_type: '', assignee_user_email: '', user_message: '', description: '', device_id: '' }

export default function TicketModal({ open, ticket, deviceId, dockerName, onClose, onSaved }) {
  const { ctx } = useApp()
  const toast = useToast()
  const [form, setForm] = useState(BLANK)
  const [busy, setBusy] = useState(false)

  useEffect(() => {
    if (!open) return
    setForm({ ...BLANK, ...(ticket || {}), device_id: (ticket?.device_id ?? deviceId) || '' })
  }, [open, ticket, deviceId])

  if (!open) return null
  const set = (k) => (e) => setForm((f) => ({ ...f, [k]: e.target.value }))
  const deviceLocked = !!deviceId && !ticket?.id

  const save = async () => {
    if (!form.category) { toast.error('Category is required'); return }
    setBusy(true)
    try {
      const dto = { ...ticket, ...form }
      if (dockerName && !dto.docker_name) dto.docker_name = dockerName
      // type is service-computed — never send it
      delete dto.type
      Object.keys(dto).forEach((k) => { if (dto[k] === '') delete dto[k] })
      await api.upsertTicket(dto, ctx)
      toast.success(ticket?.id ? 'Work order updated' : 'Work order created')
      onSaved?.()
      onClose?.()
    } catch (e) {
      toast.error(`Save failed: ${e.message}`)
    } finally { setBusy(false) }
  }

  return (
    <div className="modal-backdrop" onClick={onClose}>
      <div className="modal" onClick={(e) => e.stopPropagation()}>
        <div className="modal-head">
          <h3>{ticket?.id ? 'Edit Work Order' : 'New Work Order'}</h3>
          <button className="icon-btn" onClick={onClose}><Icon name="x" size={16} /></button>
        </div>
        <div className="modal-body">
          <label className="fld"><span>Category *</span>
            <select value={form.category} onChange={set('category')}>
              <option value="">Select…</option>
              {TICKET_CATEGORIES.map((c) => <option key={c} value={c}>{categoryLabel(c)}</option>)}
            </select>
          </label>
          <label className="fld"><span>Status</span>
            <select value={form.status} onChange={set('status')}>
              {TICKET_STATUSES.map((s) => <option key={s} value={s}>{statusLabel(s)}</option>)}
            </select>
          </label>
          <label className="fld"><span>Request Type</span><input value={form.request_type} onChange={set('request_type')} placeholder="Used as the name for Service Requests" /></label>
          <label className="fld"><span>Assignee email</span><input type="email" value={form.assignee_user_email} onChange={set('assignee_user_email')} /></label>
          <label className="fld"><span>Device ID</span><input value={form.device_id} onChange={set('device_id')} disabled={deviceLocked} /></label>
          <label className="fld"><span>User message</span><textarea rows={2} value={form.user_message} onChange={set('user_message')} /></label>
          <label className="fld"><span>Description</span><textarea rows={3} value={form.description} onChange={set('description')} /></label>
        </div>
        <div className="modal-foot">
          <button className="btn btn-ghost" onClick={onClose} disabled={busy}>Cancel</button>
          <button className="btn btn-primary" onClick={save} disabled={busy}>{busy ? <Spinner size={13} /> : null} Save</button>
        </div>
      </div>
    </div>
  )
}
