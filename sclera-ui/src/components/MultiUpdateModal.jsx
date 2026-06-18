import { useState } from 'react'
import Icon from './Icon.jsx'
import { Spinner } from './Skeleton.jsx'

// Bulk-edit modal: only the fields the user fills are sent, so blanks leave that
// column untouched on the selected assets. Field keys are backend device columns.
const empty = { type: '', vendor: '', model: '', asset_group: '', category: '', monitor: '', status: '', description: '' }

export default function MultiUpdateModal({ open, count, busy, onClose, onApply }) {
  const [form, setForm] = useState({ ...empty })
  if (!open) return null

  const set = (k) => (e) => setForm((f) => ({ ...f, [k]: e.target.value }))

  // Collect only the filled fields; coerce the toggles to integers.
  const buildChanges = () => {
    const c = {}
    Object.entries(form).forEach(([k, v]) => {
      if (v === '' || v == null) return
      c[k] = k === 'monitor' || k === 'status' ? Number(v) : v
    })
    return c
  }

  const changes = buildChanges()
  const nChanges = Object.keys(changes).length

  return (
    <div className="modal-overlay" onMouseDown={(e) => { if (e.target === e.currentTarget && !busy) onClose() }}>
      <div className="modal" role="dialog" aria-modal="true" style={{ maxWidth: 560 }}>
        <div className="modal-head">
          <h3>Multi Update · {count} selected</h3>
          <button className="icon-btn" onClick={onClose} aria-label="Close" disabled={busy}><Icon name="x" /></button>
        </div>

        <div className="modal-body">
          <p className="muted" style={{ marginTop: 0 }}>
            Only the fields you fill are applied to the {count} selected asset{count === 1 ? '' : 's'}. Leave a field blank to keep it unchanged.
          </p>
          <div className="form-grid">
            <Field label="Device Type">
              <select value={form.type} onChange={set('type')}>
                <option value="">— keep —</option>
                {['Generic', 'IP Device', 'Power Source', 'Sensor', 'Gateway', 'Controller'].map((o) => <option key={o}>{o}</option>)}
              </select>
            </Field>
            <Field label="Asset Group">
              <select value={form.asset_group} onChange={set('asset_group')}>
                <option value="">— keep —</option>
                {['Generic', 'HVAC', 'Lighting', 'Security', 'Network'].map((o) => <option key={o}>{o}</option>)}
              </select>
            </Field>

            <Field label="Vendor"><input value={form.vendor} onChange={set('vendor')} placeholder="Keep unchanged" /></Field>
            <Field label="Model"><input value={form.model} onChange={set('model')} placeholder="Keep unchanged" /></Field>

            <Field label="Category"><input value={form.category} onChange={set('category')} placeholder="Keep unchanged" /></Field>
            <Field label="Monitoring">
              <select value={form.monitor} onChange={set('monitor')}>
                <option value="">— keep —</option>
                <option value="1">Monitored</option>
                <option value="0">Unmonitored</option>
              </select>
            </Field>

            <Field label="Status">
              <select value={form.status} onChange={set('status')}>
                <option value="">— keep —</option>
                <option value="1">Online</option>
                <option value="0">Offline</option>
              </select>
            </Field>
            <Field label="Description"><input value={form.description} onChange={set('description')} placeholder="Keep unchanged" /></Field>
          </div>
        </div>

        <div className="modal-foot">
          <button className="btn btn-ghost" onClick={onClose} disabled={busy}>Cancel</button>
          <button className="btn btn-primary" onClick={() => onApply(changes)} disabled={busy || nChanges === 0}>
            {busy ? <Spinner size={14} /> : <Icon name="sliders" size={15} />} Apply to {count}
          </button>
        </div>
      </div>
    </div>
  )
}

function Field({ label, children }) {
  return (
    <div className="field">
      <label>{label}</label>
      {children}
    </div>
  )
}
