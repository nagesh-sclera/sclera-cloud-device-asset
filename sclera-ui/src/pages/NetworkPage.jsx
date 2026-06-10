import { useCallback, useEffect, useMemo, useState } from 'react'
import Icon from '../components/Icon.jsx'
import { Skeleton, Spinner } from '../components/Skeleton.jsx'
import { useApp } from '../context/AppContext.jsx'
import { useToast } from '../context/ToastContext.jsx'
import api from '../services/api.js'

const internet = (v) => {
  const s = (v || '').toLowerCase()
  if (s === 'connected' || s === 'online') return { label: 'Connected', tone: 'online', icon: 'network' }
  if (s === 'disconnected' || s === 'offline') return { label: 'Disconnected', tone: 'offline', icon: 'network' }
  return { label: 'Unknown', tone: 'muted', icon: 'info' }
}
const configured = (v) => (v || '').toLowerCase() === 'configured'

// Full-page "My Networks" (img_13): list networks (gateways) + add network.
export default function NetworkPage() {
  const { ctx, setView } = useApp()
  const toast = useToast()
  const [networks, setNetworks] = useState(null)
  const [search, setSearch] = useState('')
  const [showAdd, setShowAdd] = useState(false)
  const [form, setForm] = useState({ name: '', system_type: 'generic', gateway: '' })
  const [busy, setBusy] = useState(false)

  const load = useCallback(() => {
    setNetworks(null)
    api.listNetworks(ctx).then((l) => setNetworks(Array.isArray(l) ? l : [])).catch(() => setNetworks([]))
  }, [ctx])
  useEffect(() => { load() }, [load])

  const add = async () => {
    if (!form.name.trim()) return
    setBusy(true)
    try {
      await api.addNetwork({ name: form.name.trim(), system_type: form.system_type, gateway: form.gateway }, ctx)
      toast.success(`Network "${form.name.trim()}" added`)
      setForm({ name: '', system_type: 'generic', gateway: '' }); setShowAdd(false)
      load()
    } catch (e) { toast.error(`Add network failed: ${e.message}`) }
    finally { setBusy(false) }
  }

  const rows = useMemo(() => {
    const list = networks || []
    if (!search) return list
    const q = search.toLowerCase()
    return list.filter((n) => (n.name || '').toLowerCase().includes(q) || (n.system_type || '').toLowerCase().includes(q))
  }, [networks, search])

  return (
    <div className="page">
      <div className="page-head">
        <div className="page-title">
          <button className="icon-btn" onClick={() => setView('dashboard')} title="Back"><Icon name="portal" size={16} /></button>
          <h1>My Networks</h1>
          <button className="btn btn-primary sm" onClick={() => setShowAdd((s) => !s)}><Icon name="plus" size={15} /> Add network</button>
        </div>
        <div className="header-search" style={{ maxWidth: 320 }}>
          <Icon name="search" size={16} />
          <input value={search} onChange={(e) => setSearch(e.target.value)} placeholder="Search networks..." />
        </div>
      </div>

      {showAdd && (
        <div className="card net-add">
          <div className="net-add-grid">
            <div className="field"><label>Network Name <span className="req">*</span></label><input value={form.name} onChange={(e) => setForm((f) => ({ ...f, name: e.target.value }))} placeholder="e.g. left_wing" /></div>
            <div className="field"><label>System Type</label>
              <select value={form.system_type} onChange={(e) => setForm((f) => ({ ...f, system_type: e.target.value }))}>
                {['generic', 'back_office', 'isp', 'digital_signage', 'host', 'proxy'].map((o) => <option key={o}>{o}</option>)}
              </select>
            </div>
            <div className="field"><label>Gateway IP</label><input value={form.gateway} onChange={(e) => setForm((f) => ({ ...f, gateway: e.target.value }))} placeholder="e.g. 192.168.1.1" /></div>
          </div>
          <div className="net-add-foot">
            <button className="btn btn-ghost sm" onClick={() => setShowAdd(false)}>Cancel</button>
            <button className="btn btn-primary sm" disabled={busy || !form.name.trim()} onClick={add}>{busy ? <Spinner size={13} /> : <Icon name="plus" size={13} />} Add</button>
          </div>
        </div>
      )}

      <div className="loc-count">{rows.length} network(s)</div>

      {networks == null ? (
        <div className="net-list">{Array.from({ length: 3 }).map((_, i) => <div className="net-row" key={i}><Skeleton w="100%" h={28} /></div>)}</div>
      ) : rows.length === 0 ? (
        <div className="empty-state"><Icon name="network" size={28} /><p>No networks</p><span className="muted">Add one with “+ Add network”.</span></div>
      ) : (
        <div className="net-list">
          {rows.map((n) => {
            const inet = internet(n.internet_status)
            const isConf = configured(n.configuration_status)
            return (
              <div className="net-row" key={n.name}>
                <div className="net-col"><span className="net-label">Network Name</span><span className="net-val">{n.name}</span></div>
                <div className="net-col"><span className="net-label">System type</span><span className="net-val">{n.system_type || '—'}</span></div>
                <div className="net-col"><span className="net-label">Internet Status</span><span className={`net-val tone-${inet.tone}`}><Icon name={inet.icon} size={14} /> {inet.label}</span></div>
                <div className="net-col net-master">{n.master ? <span className="badge tone-default">Master</span> : null}</div>
                <div className="net-col net-conf"><span className={isConf ? 'conf-ok' : 'conf-no'}>{isConf ? 'Configured' : 'Not Configured'}</span></div>
                <button className="icon-btn" title="More"><Icon name="more" size={16} /></button>
              </div>
            )
          })}
        </div>
      )}
    </div>
  )
}
