import { useEffect, useMemo, useState } from 'react'
import Icon from './Icon.jsx'
import { Skeleton, Spinner } from './Skeleton.jsx'
import { useApp } from '../context/AppContext.jsx'
import { useToast } from '../context/ToastContext.jsx'
import { statusInfo } from '../config.js'
import api from '../services/api.js'
import { inspectionsFor, workOrdersFor, inventoryFor, alertsFor, documentsFor } from '../services/mock.js'

const dash = (v) => (v === 0 ? '0' : v ? String(v) : '—')
function fmtDate(ts) {
  if (!ts) return '—'
  const n = Number(ts); if (!n) return '—'
  // full date + time (e.g. "10 Jun 2026, 2:30:05 PM")
  return new Date(n).toLocaleString(undefined, {
    day: '2-digit', month: 'short', year: 'numeric',
    hour: '2-digit', minute: '2-digit', second: '2-digit',
  })
}
const money = (d) => (d?.cost_value == null || d.cost_value === '' ? '—' : `${d.cost_value} ${d.cost_unit || ''}`.trim())

const SUBTABS = ['Info', 'Sensors', 'Notes', 'Inspections', 'Work Orders', 'Inventory', 'Documents', 'Activity']

// ALL editable text/select fields (img.png / img_2).
const EDITABLE = [
  { key: 'user_data_name', label: 'Asset Name', type: 'text' },
  { key: 'display_name', label: 'Display Name', type: 'text' },
  { key: 'model', label: 'Model', type: 'text' },
  { key: 'assignee_email', label: 'Assignee', type: 'text', placeholder: 'Select User' },
  { key: 'operational_status', label: 'Operation Status', type: 'select', options: ['Working', 'Faulty', 'Under Repair', 'Decommissioned'] },
  { key: 'cost', label: 'Cost', type: 'cost' },
  { key: 'vendor', label: 'Manufacturer', type: 'text' },
  { key: 'type', label: 'Asset Type', type: 'select', options: ['Generic', 'IP Device', 'Power Source', 'Sensor', 'Gateway', 'Controller'] },
  { key: 'asset_group', label: 'Asset Group', type: 'select', options: ['Generic', 'HVAC', 'Lighting', 'Security', 'Network'] },
  { key: 'category', label: 'Category', type: 'select', options: ['Generic', 'Hardware', 'Software', 'Appliance'] },
  { key: 'sub_category', label: 'Sub Category', type: 'select', options: ['Generic', 'Primary', 'Secondary'] },
  { key: 'serial_number', label: 'Serial Number', type: 'text' },
  { key: 'warranty', label: 'Warranty', type: 'text' },
  { key: 'ip_address', label: 'IP Address', type: 'text' },
  { key: 'mac_address', label: 'MAC Address', type: 'text' },
  { key: 'network_layer', label: 'Network Layer', type: 'text' },
  { key: 'location', label: 'Location', type: 'text' },
  { key: 'floor', label: 'Floor', type: 'text' },
  { key: 'building', label: 'Building', type: 'text' },
  { key: 'latitude', label: 'Latitude', type: 'text' },
  { key: 'longitude', label: 'Longitude', type: 'text' },
  { key: 'description', label: 'Description', type: 'textarea' },
]
// Editable flags (int 0/1, except ai_call / is_dnd_enabled which are boolean).
const FLAGS = [
  { key: 'monitor', label: 'monitor', bool: false },
  { key: 'ai_call', label: 'AI Call', bool: true },
  { key: 'popup_notification', label: 'touchscreen Popup', bool: false },
  { key: 'email_alert', label: 'Email Alert', bool: false },
  { key: 'sms_alert', label: 'SMS Alert', bool: false },
  { key: 'remote_access', label: 'Remote Access', bool: false },
  { key: 'is_dnd_enabled', label: 'Do Not Disturb', bool: true },
]

export default function DeviceDetailPanel({ deviceId, onClose, onChanged }) {
  const { ctx } = useApp()
  const toast = useToast()
  const [device, setDevice] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [sub, setSub] = useState('Info')
  const [logs, setLogs] = useState(null)
  const [busy, setBusy] = useState(false)
  const [editing, setEditing] = useState(false)
  const [form, setForm] = useState({})

  // real sensors (called directly on the main service via the gateway)
  const [sensors, setSensors] = useState(null)
  const [sensorBusy, setSensorBusy] = useState(false)
  // real notes
  const [notes, setNotes] = useState(null)
  const [noteBusy, setNoteBusy] = useState(false)
  // networks (for moving the device between gateways during edit)
  const [networks, setNetworks] = useState([])
  useEffect(() => {
    api.listNetworks({ vdmsId: ctx.vdmsId }).then((l) => setNetworks((Array.isArray(l) ? l : []).map((n) => n.name).filter(Boolean))).catch(() => {})
  }, []) // eslint-disable-line react-hooks/exhaustive-deps

  useEffect(() => {
    if (!deviceId) return
    let alive = true
    setLoading(true); setError(null); setSub('Info'); setEditing(false); setSensors(null); setNotes(null)
    api.getDevice(deviceId, ctx)
      .then((d) => { if (alive) { setDevice(d); setForm(toForm(d)) } })
      .catch((e) => { if (alive) setError(e) })
      .finally(() => { if (alive) setLoading(false) })
    return () => { alive = false }
  }, [deviceId]) // eslint-disable-line react-hooks/exhaustive-deps

  useEffect(() => {
    if (sub !== 'Activity' || logs) return
    api.auditLog({ vdmsId: ctx.vdmsId, page: 0, size: 100 })
      .then((all) => setLogs((Array.isArray(all) ? all : []).filter((l) => l.affectedRecordId === deviceId)))
      .catch(() => setLogs([]))
  }, [sub]) // eslint-disable-line react-hooks/exhaustive-deps

  const loadSensors = () => api.listSensors(deviceId, ctx).then((s) => setSensors(Array.isArray(s) ? s : [])).catch(() => setSensors([]))
  useEffect(() => { if (sub === 'Sensors' && sensors == null) loadSensors() }, [sub]) // eslint-disable-line react-hooks/exhaustive-deps

  const loadNotes = () => api.listNotes(deviceId, ctx).then((n) => setNotes(Array.isArray(n) ? n : [])).catch(() => setNotes([]))
  useEffect(() => { if (sub === 'Notes' && notes == null) loadNotes() }, [sub]) // eslint-disable-line react-hooks/exhaustive-deps

  const name = device?.user_data_name || device?.display_name || device?.name || deviceId
  const st = device ? statusInfo(device) : null
  const onboarded = device?.onboard_status === 3

  const inspections = useMemo(() => (device ? inspectionsFor(device) : []), [device])
  const workorders = useMemo(() => (device ? workOrdersFor(device) : []), [device])
  const inventory = useMemo(() => (device ? inventoryFor(device) : null), [device])
  const alerts = useMemo(() => (device ? alertsFor(device) : []), [device])
  const documents = useMemo(() => (device ? documentsFor(device) : []), [device])

  function toForm(d) {
    const f = {
      cost_value: d.cost_value ?? '', cost_unit: d.cost_unit || 'USD',
    }
    EDITABLE.forEach((e) => {
      if (e.key === 'cost') return
      if (e.key === 'user_data_name') f[e.key] = d.user_data_name || d.display_name || ''
      else if (e.key === 'vendor') f[e.key] = d.user_data_vendor || d.vendor || ''
      else if (e.key === 'model') f[e.key] = d.user_data_model || d.model || ''
      else if (e.key === 'operational_status') f[e.key] = d.operational_status || 'Working'
      else if (e.key === 'assignee_email') f[e.key] = d.assignee_email || d.assigned_user_email || ''
      else if (['type', 'asset_group', 'category', 'sub_category'].includes(e.key)) f[e.key] = d[e.key] || 'Generic'
      else f[e.key] = d[e.key] ?? ''
    })
    FLAGS.forEach((fl) => { f[fl.key] = fl.bool ? Boolean(d[fl.key]) : d[fl.key] === 1 })
    f.docker_name = d.docker_name || ''
    return f
  }
  const set = (k) => (e) => setForm((f) => ({ ...f, [k]: e.target?.value ?? e }))
  const setFlag = (k) => (e) => setForm((f) => ({ ...f, [k]: e.target.checked }))

  const save = async () => {
    setBusy(true)
    try {
      const payload = { ...device }
      EDITABLE.forEach((e) => {
        if (e.key === 'cost') return
        const v = form[e.key]
        if (e.key === 'user_data_name') { payload.user_data_name = v; payload.display_name = form.display_name || v }
        else if (e.key === 'vendor') { payload.vendor = v; payload.user_data_vendor = v }
        else if (e.key === 'model') { payload.model = v; payload.user_data_model = v }
        else payload[e.key] = v === '' ? null : v
      })
      payload.cost_value = form.cost_value === '' ? null : Number(form.cost_value)
      payload.cost_unit = form.cost_unit
      FLAGS.forEach((fl) => { payload[fl.key] = fl.bool ? Boolean(form[fl.key]) : (form[fl.key] ? 1 : 0) })

      await api.editDevice(deviceId, payload, ctx)
      // network change is a separate call (edit doesn't move the device)
      if (form.docker_name && form.docker_name !== device.docker_name) {
        await api.moveDeviceNetwork(deviceId, form.docker_name, { vdmsId: ctx.vdmsId, user: ctx.user })
      }
      // editDevice doesn't stamp updated_timestamp/email — do it so "Updated Date" refreshes
      await api.touchDevice(deviceId, { vdmsId: ctx.vdmsId, user: ctx.user }).catch(() => {})
      toast.success('Asset updated')
      const fresh = await api.getDevice(deviceId, ctx)
      setDevice(fresh); setForm(toForm(fresh)); setEditing(false)
      onChanged?.()
    } catch (e) { toast.error(`Update failed: ${e.message}`) }
    finally { setBusy(false) }
  }

  const doOnboard = async () => {
    setBusy(true)
    try {
      await api.onboard(deviceId, { ...ctx, status: onboarded ? 0 : 3 })
      toast.success(onboarded ? 'Asset set to not onboarded' : 'Asset onboarded')
      const fresh = await api.getDevice(deviceId, ctx)
      setDevice(fresh); setForm(toForm(fresh))
      onChanged?.()
    } catch (e) { toast.error(`Onboard failed: ${e.message}`) }
    finally { setBusy(false) }
  }

  const act = async (kind) => {
    const verb = kind === 'archive' ? 'Archive' : 'Delete'
    if (!confirm(`${verb} asset "${name}"?`)) return
    setBusy(true)
    try {
      if (kind === 'archive') await api.archiveDevices([deviceId], ctx)
      else await api.deleteDevices([deviceId], ctx)
      toast.success(`Asset ${verb.toLowerCase()}d`); onChanged?.(); onClose()
    } catch (e) { toast.error(`${verb} failed: ${e.message}`) }
    finally { setBusy(false) }
  }

  const addSensor = async (s) => {
    setSensorBusy(true)
    try {
      await api.addSensor(deviceId, s, ctx)
      toast.success('Sensor added')
      await loadSensors()
    } catch (e) { toast.error(`Add sensor failed: ${e.message}`) }
    finally { setSensorBusy(false) }
  }
  const removeSensor = async (id) => {
    setSensorBusy(true)
    try { await api.deleteSensor(deviceId, id, ctx); await loadSensors() }
    catch (e) { toast.error(`Delete failed: ${e.message}`) }
    finally { setSensorBusy(false) }
  }

  const addNote = async (n) => {
    setNoteBusy(true)
    try { await api.addNote(deviceId, n, ctx); toast.success('Note added'); await loadNotes() }
    catch (e) { toast.error(`Add note failed: ${e.message}`) }
    finally { setNoteBusy(false) }
  }
  const removeNote = async (id) => {
    setNoteBusy(true)
    try { await api.deleteNote(deviceId, id, ctx); await loadNotes() }
    catch (e) { toast.error(`Delete failed: ${e.message}`) }
    finally { setNoteBusy(false) }
  }

  const counts = device ? [
    ['Sensors', sensors?.length ?? (device.snmp_count || 0)], ['Inspections', inspections.length], ['Work Orders', workorders.length],
    ['Alerts', alerts.length], ['Tickets', device.ticket_count || 0], ['Notes', device.notes_count || 0],
  ] : []

  return (
    <>
      <div className="panel-scrim" onClick={onClose} />
      <aside className="detail-panel" role="dialog" aria-label="Asset details">
        <div className="dp-head">
          <div className="dp-title">
            <div className="dp-avatar"><Icon name="device" size={18} /></div>
            <span className="dp-name" title={name}>{loading ? 'Loading…' : name}</span>
          </div>
          <div className="dp-head-actions">
            <button className="dp-icontext"><Icon name="barcode" size={16} /><span>{editing ? 'Re-assign Bar Code' : 'Tag Bar Code'}</span></button>
            <button className="dp-icontext"><Icon name="qrcode" size={16} /><span>{editing ? 'Re-assign' : 'Tag Asset'}</span></button>
            <button className="icon-btn" onClick={onClose} aria-label="Close"><Icon name="x" size={16} /></button>
          </div>
        </div>

        <div className="dp-subtabs">
          {SUBTABS.map((s) => (
            <button key={s} className={`dp-subtab ${sub === s ? 'active' : ''}`} onClick={() => setSub(s)}>
              {s}{s === 'Sensors' && sensors?.length ? <span className="subtab-badge">{sensors.length}</span> : null}
            </button>
          ))}
        </div>

        <div className="dp-toolbar">
          <button className={`btn sm ${onboarded ? 'btn-ghost' : 'btn-primary'}`} disabled={busy} onClick={doOnboard} title={onboarded ? 'Click to un-onboard' : 'Onboard this asset'}>
            <Icon name="upload" size={14} /> {onboarded ? 'Onboarded ✓' : 'Onboard'}
          </button>
          <button className="btn btn-ghost sm"><Icon name="plus" size={14} /> Add Digital Twin</button>
          <span className="push-right" />
          {sub === 'Info' && !editing && device && (
            <button className="icon-btn" onClick={() => setEditing(true)} title="Edit"><Icon name="edit" size={15} /></button>
          )}
          <button className="icon-btn" title="More"><Icon name="more" size={15} /></button>
        </div>

        <div className="dp-body">
          {loading ? (
            <div className="dp-fields">{Array.from({ length: 12 }).map((_, i) => (
              <div className="dp-row" key={i}><Skeleton w="40%" h={11} /><Skeleton w="45%" h={11} /></div>
            ))}</div>
          ) : error ? (
            <div className="empty-state"><Icon name="info" size={24} /><p>Couldn't load asset</p><span className="muted">{error.message}</span></div>
          ) : sub === 'Info' ? (
            editing ? <EditForm form={form} set={set} setFlag={setFlag} networks={networks} /> : <InfoView device={device} name={name} st={st} onboarded={onboarded} counts={counts} />
          ) : sub === 'Sensors' ? (
            <SensorsView sensors={sensors} busy={sensorBusy} onAdd={addSensor} onRemove={removeSensor} onRefresh={loadSensors} />
          ) : sub === 'Notes' ? (
            <NotesView notes={notes} busy={noteBusy} onAdd={addNote} onRemove={removeNote} onRefresh={loadNotes} />
          ) : sub === 'Inspections' ? <InspectionsView items={inspections} />
            : sub === 'Work Orders' ? <WorkOrdersView items={workorders} />
            : sub === 'Inventory' ? <InventoryView inv={inventory} />
            : sub === 'Documents' ? <DocumentsView docs={documents} />
            : <ActivityList logs={logs} />}
        </div>

        <div className="dp-foot">
          {editing ? (
            <>
              <button className="btn btn-ghost sm" disabled={busy} onClick={() => { setForm(toForm(device)); setEditing(false) }}>Cancel</button>
              <button className="btn btn-primary sm" disabled={busy} onClick={save}>{busy ? <Spinner size={14} /> : <Icon name="upload" size={14} />} Update</button>
            </>
          ) : (
            <>
              <button className="btn btn-danger sm" disabled={busy} onClick={() => act('delete')}>{busy ? <Spinner size={14} /> : <Icon name="trash" size={14} />} Delete</button>
              <button className="btn btn-warn sm" disabled={busy} onClick={() => act('archive')}><Icon name="box" size={14} /> Archive</button>
            </>
          )}
        </div>
      </aside>
    </>
  )
}

function InfoView({ device, name, st, onboarded, counts }) {
  const d = device
  return (
    <>
      <div className="dp-countbar">
        {counts.map(([label, val]) => (
          <div className="dp-count" key={label}><span className="dp-count-n">{val}</span><span className="dp-count-l">{label}</span></div>
        ))}
      </div>
      <div className="dp-fields">
        <Row k="Onboard status" v={onboarded ? 'Onboarded' : 'Not Onboarded'} accent />
        <Row k="Asset Name" v={name} />
        <Row k="Display Name" v={dash(d.display_name)} />
        <Row k="Model" v={dash(d.user_data_model || d.model)} />
        <Row k="Status" v={st?.label} tone={st?.tone} />
        <Row k="Operation Status" v={dash(d.operational_status) === '—' ? 'Working' : d.operational_status} accent />
        <Row k="Assignee" v={dash(d.assignee_email || d.assigned_user_email)} />
        <Row k="Cost" v={money(d)} />
        <Row k="Manufacturer / Vendor" v={dash(d.user_data_vendor || d.vendor)} />
        <Row k="Asset Type" v={dash(d.type)} accent />
        <Row k="Asset Group" v={dash(d.asset_group)} accent />
        <Row k="Category" v={dash(d.category)} accent />
        <Row k="Sub Category" v={dash(d.sub_category)} accent />
        <Row k="Serial Number" v={dash(d.serial_number)} />
        <Row k="Warranty" v={dash(d.warranty)} />
        <Row k="Description" v={dash(d.description)} />
      </div>

      <Section title="Identity & Network">
        <Row k="Device ID" v={dash(d.id)} />
        <Row k="Network" v={dash(d.docker_name)} accent />
        <Row k="VDMS" v={dash(d.vdms_id || d.docker_vdms_id)} />
        <Row k="IP Address" v={dash(d.ip_address)} />
        <Row k="MAC Address" v={dash(d.mac_address)} />
        <Row k="Network Layer" v={dash(d.network_layer)} />
        <Row k="SNMP Parent" v={dash(d.snmp_parent)} />
        <Row k="Subsystem Parent" v={dash(d.subsystem_parent_name || d.subsystem_parent_id)} />
        <Row k="Product ID" v={dash(d.product_id)} />
        <Row k="Inventory Tracking" v={dash(d.inventory_tracking_id)} />
      </Section>

      <Section title="Audit">
        <Row k="Created Date" v={fmtDate(d.created_timestamp)} accent />
        <Row k="Created Email" v={dash(d.created_email)} accent />
        <Row k="Updated Date" v={fmtDate(d.updated_timestamp)} />
        <Row k="Updated Email" v={dash(d.updated_email)} />
      </Section>

      <Section title="Location Details">
        <Row k="Location" v={dash(d.location)} />
        <Row k="Latitude" v={dash(d.latitude)} />
        <Row k="Longitude" v={dash(d.longitude)} />
        <Row k="Floor" v={dash(d.floor)} />
        <Row k="Building" v={dash(d.building)} />
      </Section>

      <Section title="Flags">
        <div className="dp-flags">
          {FLAGS.map((fl) => (
            <label key={fl.key}><input type="checkbox" readOnly checked={fl.bool ? Boolean(d[fl.key]) : d[fl.key] === 1} /> {fl.label}</label>
          ))}
        </div>
      </Section>
    </>
  )
}

function EditForm({ form, set, setFlag, networks = [] }) {
  const netOptions = [...new Set([form.docker_name, ...networks].filter(Boolean))]
  return (
    <div className="dp-editform">
      <div className="dp-editrow">
        <label>Network (Gateway)</label>
        <select value={form.docker_name || ''} onChange={set('docker_name')}>
          {netOptions.length === 0 && <option value="">—</option>}
          {netOptions.map((n) => <option key={n} value={n}>{n}</option>)}
        </select>
      </div>
      {EDITABLE.map((f) => (
        <div className="dp-editrow" key={f.key}>
          <label>{f.label}</label>
          {f.type === 'text' && <input value={form[f.key] || ''} onChange={set(f.key)} placeholder={f.placeholder || ''} />}
          {f.type === 'textarea' && <textarea rows={3} value={form[f.key] || ''} onChange={set(f.key)} />}
          {f.type === 'select' && <select value={form[f.key] || ''} onChange={set(f.key)}>{f.options.map((o) => <option key={o}>{o}</option>)}</select>}
          {f.type === 'cost' && (
            <div className="cost-row">
              <input value={form.cost_value} onChange={set('cost_value')} placeholder="0.00" />
              <select value={form.cost_unit} onChange={set('cost_unit')}>{['USD', 'EUR', 'GBP', 'INR'].map((c) => <option key={c}>{c}</option>)}</select>
            </div>
          )}
        </div>
      ))}
      <div className="dp-section" style={{ marginTop: 4 }}>
        <div className="dp-section-title">Flags</div>
        <div className="dp-flags">
          {FLAGS.map((fl) => (
            <label key={fl.key}><input type="checkbox" checked={Boolean(form[fl.key])} onChange={setFlag(fl.key)} /> {fl.label}</label>
          ))}
        </div>
      </div>
    </div>
  )
}

function SensorsView({ sensors, busy, onAdd, onRemove, onRefresh }) {
  const [show, setShow] = useState(false)
  const [f, setF] = useState({ name: '', type: 'Temperature', value: '', unit: '°C', status: 'ok', protocol: 'BACnet' })
  const sf = (k) => (e) => setF((s) => ({ ...s, [k]: e.target.value }))
  const submit = () => {
    if (!f.name.trim()) return
    onAdd({ ...f, name: f.name.trim() })
    setF({ name: '', type: 'Temperature', value: '', unit: '°C', status: 'ok', protocol: 'BACnet' })
    setShow(false)
  }
  return (
    <>
      <div className="svc-head">
        <span className="svc-note">Real sensors — fetched &amp; added on the device-asset service</span>
        <div className="svc-head-actions">
          <button className="icon-btn" onClick={onRefresh} title="Refresh"><Icon name="refresh" size={14} /></button>
          <button className="btn btn-primary sm" onClick={() => setShow((s) => !s)}><Icon name="plus" size={13} /> Add Sensor</button>
        </div>
      </div>

      {show && (
        <div className="sensor-form">
          <div className="sensor-form-grid">
            <input placeholder="Sensor name *" value={f.name} onChange={sf('name')} />
            <select value={f.type} onChange={sf('type')}>{['Temperature', 'Humidity', 'Power', 'CPU Load', 'Signal', 'Door'].map((o) => <option key={o}>{o}</option>)}</select>
            <input placeholder="Value" value={f.value} onChange={sf('value')} />
            <input placeholder="Unit" value={f.unit} onChange={sf('unit')} />
            <select value={f.protocol} onChange={sf('protocol')}>{['BACnet', 'SNMP', 'LoRaWAN', 'Modbus', 'MQTT'].map((o) => <option key={o}>{o}</option>)}</select>
            <select value={f.status} onChange={sf('status')}>{['ok', 'warn', 'alert'].map((o) => <option key={o}>{o}</option>)}</select>
          </div>
          <div className="sensor-form-foot">
            <button className="btn btn-ghost sm" onClick={() => setShow(false)}>Cancel</button>
            <button className="btn btn-primary sm" disabled={busy || !f.name.trim()} onClick={submit}>{busy ? <Spinner size={13} /> : null} Save</button>
          </div>
        </div>
      )}

      {sensors == null ? (
        <div className="sensor-list"><Skeleton w="100%" h={80} /><Skeleton w="100%" h={80} /></div>
      ) : sensors.length === 0 ? (
        <Empty icon="device" label="No sensors yet — add one (real call to the service)" />
      ) : (
        <div className="sensor-list">
          {sensors.map((s) => (
            <div className="sensor-card" key={s.id}>
              <div className="sensor-top">
                <span className="sensor-name">{s.name}</span>
                <button className="icon-btn danger" style={{ width: 24, height: 24 }} onClick={() => onRemove(s.id)} title="Delete"><Icon name="trash" size={12} /></button>
              </div>
              <div className="sensor-reading">{s.value || '—'}<span className="sensor-unit">{s.unit}</span></div>
              <div className="sensor-meta">
                <span className={`badge tone-${s.status === 'ok' ? 'online' : s.status === 'alert' ? 'offline' : 'default'}`}>{(s.status || '').toUpperCase()}</span>
                {' '}{s.type} · {s.protocol}
              </div>
            </div>
          ))}
        </div>
      )}
    </>
  )
}

function NotesView({ notes, busy, onAdd, onRemove, onRefresh }) {
  const [title, setTitle] = useState('')
  const [body, setBody] = useState('')
  const submit = () => {
    if (!title.trim() && !body.trim()) return
    onAdd({ title: title.trim(), body: body.trim() })
    setTitle(''); setBody('')
  }
  return (
    <>
      <div className="svc-head">
        <span className="svc-note">Notes — stored on the device-asset service</span>
        <button className="icon-btn" onClick={onRefresh} title="Refresh"><Icon name="refresh" size={14} /></button>
      </div>
      <div className="note-form">
        <input placeholder="Title" value={title} onChange={(e) => setTitle(e.target.value)} />
        <textarea placeholder="Write a note..." rows={3} value={body} onChange={(e) => setBody(e.target.value)} />
        <div className="note-form-foot">
          <button className="btn btn-primary sm" disabled={busy || (!title.trim() && !body.trim())} onClick={submit}>
            {busy ? <Spinner size={13} /> : <Icon name="plus" size={13} />} Add Note
          </button>
        </div>
      </div>
      {notes == null ? (
        <div className="log-list"><Skeleton w="100%" h={50} /><Skeleton w="100%" h={50} /></div>
      ) : notes.length === 0 ? (
        <Empty icon="list" label="No notes yet — add the first one" />
      ) : (
        <div className="note-list">
          {notes.map((n) => (
            <div className="note-card" key={n.id}>
              <div className="note-top">
                <span className="note-title">{n.title || 'Untitled'}</span>
                <button className="icon-btn danger" style={{ width: 24, height: 24 }} onClick={() => onRemove(n.id)} title="Delete"><Icon name="trash" size={12} /></button>
              </div>
              {n.body ? <div className="note-body">{n.body}</div> : null}
              {n.is_global === 1 ? <span className="badge tone-default">Global</span> : null}
            </div>
          ))}
        </div>
      )}
    </>
  )
}

function InspectionsView({ items }) {
  if (!items.length) return <Empty icon="list" label="No inspections" />
  return (<><div className="svc-note">From the inspection service</div><div className="log-list">
    {items.map((it) => (
      <div className="svc-row" key={it.id}>
        <div className="svc-row-main"><div className="svc-row-title">{it.title}</div><div className="svc-row-meta">{it.inspector} · {it.date}</div></div>
        <div className="svc-row-side"><span className={`badge tone-${it.status === 'Passed' ? 'online' : it.status === 'Failed' ? 'offline' : 'default'}`}>{it.status}</span><span className="svc-score">{it.score}%</span></div>
      </div>
    ))}</div></>)
}
function WorkOrdersView({ items }) {
  if (!items.length) return <Empty icon="list" label="No work orders" />
  return (<><div className="svc-note">From the workorders service</div><div className="log-list">
    {items.map((w) => (
      <div className="svc-row" key={w.id}>
        <div className="svc-row-main"><div className="svc-row-title">{w.id} · {w.title}</div><div className="svc-row-meta">Assignee {w.assignee} · due {w.due}</div></div>
        <div className="svc-row-side"><span className={`badge tone-${w.priority === 'Critical' || w.priority === 'High' ? 'offline' : 'default'}`}>{w.priority}</span><span className="badge">{w.status}</span></div>
      </div>
    ))}</div></>)
}
function InventoryView({ inv }) {
  if (!inv) return <Empty icon="box" label="No inventory" />
  return (<><div className="svc-note">From the inventory service</div>
    <div className="dp-fields"><Row k="Tracking ID" v={inv.trackingId} accent /><Row k="Storage Location" v={inv.location} /></div>
    <Section title="Linked Parts">{inv.parts.map((p) => (
      <div className="svc-row" key={p.id}><div className="svc-row-main"><div className="svc-row-title">{p.name}</div><div className="svc-row-meta">Qty {p.qty}</div></div><span className={`badge tone-${p.status === 'In Stock' ? 'online' : 'offline'}`}>{p.status}</span></div>
    ))}</Section></>)
}
function DocumentsView({ docs }) {
  if (!docs.length) return <Empty icon="upload" label="No documents" />
  return (<div className="log-list">{docs.map((d) => (
    <div className="svc-row" key={d.id}><div className="svc-row-main"><div className="svc-row-title"><Icon name="upload" size={14} /> {d.name}</div><div className="svc-row-meta">{d.kind} · {d.size}</div></div><button className="btn btn-ghost sm">Open</button></div>
  ))}</div>)
}
function ActivityList({ logs }) {
  if (logs == null) return <div className="dp-fields"><Skeleton w="100%" h={40} /><Skeleton w="100%" h={40} /></div>
  if (!logs.length) return <Empty icon="list" label="No activity for this asset yet" />
  return (<div className="log-list">{logs.map((l) => (
    <div className="log-item" key={l.id}><span className={`log-badge ${l.action?.toLowerCase()}`}>{l.action}</span><div className="log-main"><div className="log-msg">{l.message}</div><div className="log-meta">{l.userEmail} · {new Date(l.createdAt).toLocaleString()}</div></div></div>
  ))}</div>)
}

function Row({ k, v, accent, tone }) {
  return <div className="dp-row"><span className="dp-k">{k}</span><span className={`dp-v ${accent ? 'accent' : ''} ${tone ? `tone-${tone}` : ''}`}>{v}</span></div>
}
function Section({ title, children }) {
  return <div className="dp-section"><div className="dp-section-title">{title}</div><div className="dp-fields">{children}</div></div>
}
function Empty({ icon, label }) {
  return <div className="empty-state"><Icon name={icon} size={22} /><p>{label}</p></div>
}
