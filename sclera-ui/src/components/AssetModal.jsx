import { useEffect, useMemo, useState } from 'react'
import Icon from './Icon.jsx'
import { Spinner } from './Skeleton.jsx'
import { useToast } from '../context/ToastContext.jsx'
import { useApp } from '../context/AppContext.jsx'
import { DEMO } from '../config.js'
import api from '../services/api.js'

const slug = (s) => (s || 'asset').toLowerCase().replace(/[^a-z0-9]+/g, '_').replace(/^_|_$/g, '').slice(0, 24)
// Unique MAC so the backend's mac-based dedup treats each new asset as an insert.
function randomMac() {
  const h = () => Math.floor(Math.random() * 256).toString(16).padStart(2, '0').toUpperCase()
  return [h(), h(), h(), h(), h(), h()].join(':')
}

const TABS = ['IP Device', 'Power Sources', 'Other Device']

const empty = {
  name: '', model: '', type: 'Generic', category: 'Generic', serial_number: '', cost_value: '',
  cost_unit: 'USD', location_id: '', floor: '', networkName: '', vendor: '', asset_group: 'Generic',
  sub_category: 'Generic', warranty: '', building: '', assignee_email: '', description: '',
}

// Maps a backend DeviceDTO into the modal's form shape.
function fromDevice(d) {
  if (!d) return { ...empty }
  return {
    ...empty,
    name: d.user_data_name || d.display_name || d.name || '',
    model: d.user_data_model || d.model || '',
    type: d.type || 'Generic',
    category: d.category || 'Generic',
    serial_number: d.serial_number || '',
    cost_value: d.cost_value ?? '',
    cost_unit: d.cost_unit || 'USD',
    location_id: d.location_id || '',
    floor: d.floor || '',
    vendor: d.user_data_vendor || d.vendor || '',
    asset_group: d.asset_group || 'Generic',
    sub_category: d.sub_category || 'Generic',
    warranty: d.warranty || '',
    building: d.building || '',
    assignee_email: d.assignee_email || d.assigned_user_email || '',
    description: d.description || '',
  }
}

export default function AssetModal({ open, onClose, onSaved, editing }) {
  const toast = useToast()
  const { ctx } = useApp()
  const [tab, setTab] = useState('Other Device')
  const [form, setForm] = useState({ ...empty })
  const [errors, setErrors] = useState({})
  const [saving, setSaving] = useState(false)
  const [images, setImages] = useState([])
  const [buildings, setBuildings] = useState([])
  const [locations, setLocations] = useState([])

  const isEdit = Boolean(editing)

  useEffect(() => {
    if (!open) return
    setForm(fromDevice(editing))
    setErrors({})
    setImages(editing?.asset_image_url ? [editing.asset_image_url] : [])
    // best-effort dropdown loads — never block the modal
    api.getBuildings(ctx).then((b) => setBuildings(Array.isArray(b) ? b : [])).catch(() => {})
    api.getLocations(ctx).then((l) => setLocations(Array.isArray(l) ? l : [])).catch(() => {})
  }, [open, editing]) // eslint-disable-line react-hooks/exhaustive-deps

  const set = (k) => (e) => setForm((f) => ({ ...f, [k]: e.target?.value ?? e }))

  const validate = () => {
    const er = {}
    if (!form.name.trim()) er.name = 'Device Name is required'
    if (form.cost_value !== '' && isNaN(Number(form.cost_value))) er.cost_value = 'Cost must be a number'
    setErrors(er)
    return Object.keys(er).length === 0
  }

  const buildPayload = () => {
    const name = form.name.trim()
    const base = {
      ...(editing || {}),
      name,
      display_name: name,
      user_data_name: name,
      user_data_model: form.model || null,
      model: form.model || null,
      type: form.type || 'Generic',
      category: form.category || 'Generic',
      sub_category: form.sub_category || 'Generic',
      asset_group: form.asset_group || 'Generic',
      serial_number: form.serial_number || null,
      cost_value: form.cost_value === '' ? null : Number(form.cost_value),
      cost_unit: form.cost_unit || 'USD',
      warranty: form.warranty || null,
      user_data_vendor: form.vendor || null,
      vendor: form.vendor || null,
      location_id: form.location_id || null,
      assignee_email: form.assignee_email || null,
      description: form.description || null,
      monitor: editing?.monitor ?? 1,
      status: editing?.status ?? 1,
    }
    if (isEdit) return base
    // New asset: the upsert service only INSERTS when virtual_device_type is 0/null
    // and dedups by mac_address, so give each new asset a unique id + mac.
    return {
      ...base,
      id: `${ctx.vdmsId}_${ctx.docker}_${slug(name)}_${Date.now()}`,
      vdms_id: ctx.vdmsId,
      docker_vdms_id: ctx.vdmsId,
      docker_name: ctx.docker,
      mac_address: randomMac(),
      virtual_device_type: 0,
      asset_match_status: 0,
      created_email: DEMO.user,
    }
  }

  const submit = async () => {
    if (!validate()) { toast.error('Please fix the highlighted fields'); return }
    setSaving(true)
    try {
      const payload = buildPayload()
      if (isEdit) {
        await api.editDevice(editing.id, payload, ctx)
        toast.success('Asset updated')
      } else {
        await api.upsertDevices([payload], ctx)
        toast.success('Asset created')
      }
      onSaved?.()
      onClose()
    } catch (e) {
      toast.error(`Save failed: ${e.message}`)
    } finally {
      setSaving(false)
    }
  }

  const onPickImages = (e) => {
    const files = Array.from(e.target.files || [])
    setImages((prev) => [...prev, ...files.map((f) => URL.createObjectURL(f))])
  }

  const locOptions = useMemo(
    () => locations.map((l) => ({ id: l.id || l.location_id, name: l.name || l.location_name || l.id })),
    [locations]
  )
  const bldOptions = useMemo(
    () => buildings.map((b) => ({ id: b.id || b.building_id, name: b.name })),
    [buildings]
  )

  if (!open) return null

  return (
    <div className="modal-overlay" onMouseDown={(e) => { if (e.target === e.currentTarget) onClose() }}>
      <div className="modal asset-modal" role="dialog" aria-modal="true">
        <div className="modal-head">
          <h2>{isEdit ? 'Edit Asset' : 'Add Asset'}</h2>
          <div className="modal-tabs">
            {TABS.map((t) => (
              <button key={t} className={`pill ${tab === t ? 'active' : ''}`} onClick={() => setTab(t)}>{t}</button>
            ))}
          </div>
          <button className="icon-btn" onClick={onClose} aria-label="Close"><Icon name="x" /></button>
        </div>

        <div className="modal-body">
          <div className="image-upload">
            <div className="image-strip">
              {images.map((src, i) => <img key={i} src={src} alt="" className="thumb" />)}
              <label className="add-images">
                <Icon name="plus" size={22} />
                <span>Add Images</span>
                <input type="file" accept="image/*" multiple hidden onChange={onPickImages} />
              </label>
            </div>
          </div>

          <div className="form-grid">
            <Field label="Device Name" required error={errors.name}>
              <input value={form.name} onChange={set('name')} placeholder="Enter device name" />
            </Field>
            <Field label="Networks">
              <select value={form.networkName} onChange={set('networkName')}>
                <option value="">Select...</option>
                <option value="default">default</option>
                <option value="proxy">Proxy</option>
              </select>
            </Field>

            <Field label="Model"><input value={form.model} onChange={set('model')} placeholder="Model" /></Field>
            <Field label="Vendor"><input value={form.vendor} onChange={set('vendor')} placeholder="Vendor" /></Field>

            <Field label="Device Type">
              <select value={form.type} onChange={set('type')}>
                {['Generic', 'IP Device', 'Power Source', 'Sensor', 'Gateway', 'Controller'].map((o) => <option key={o}>{o}</option>)}
              </select>
            </Field>
            <Field label="Asset Group">
              <select value={form.asset_group} onChange={set('asset_group')}>
                {['Generic', 'HVAC', 'Lighting', 'Security', 'Network'].map((o) => <option key={o}>{o}</option>)}
              </select>
            </Field>

            <Field label="Category">
              <select value={form.category} onChange={set('category')}>
                {['Generic', 'Hardware', 'Software', 'Appliance'].map((o) => <option key={o}>{o}</option>)}
              </select>
            </Field>
            <Field label="Sub Category">
              <select value={form.sub_category} onChange={set('sub_category')}>
                {['Generic', 'Primary', 'Secondary'].map((o) => <option key={o}>{o}</option>)}
              </select>
            </Field>

            <Field label="Serial Number"><input value={form.serial_number} onChange={set('serial_number')} placeholder="Serial Number" /></Field>
            <Field label="Warranty"><input value={form.warranty} onChange={set('warranty')} placeholder="e.g. 2026-12-31" /></Field>

            <Field label="Location">
              <select value={form.location_id} onChange={set('location_id')}>
                <option value="">Select...</option>
                {locOptions.map((l) => <option key={l.id} value={l.id}>{l.name}</option>)}
              </select>
            </Field>
            <Field label="Building">
              <select value={form.building} onChange={set('building')}>
                <option value="">Select...</option>
                {bldOptions.map((b) => <option key={b.id} value={b.name}>{b.name}</option>)}
              </select>
            </Field>

            <Field label="Floor"><input value={form.floor} onChange={set('floor')} placeholder="Floor" /></Field>
            <Field label="Assignee">
              <input value={form.assignee_email} onChange={set('assignee_email')} placeholder="Select User" />
            </Field>

            <Field label="Cost" error={errors.cost_value}>
              <div className="cost-row">
                <input value={form.cost_value} onChange={set('cost_value')} placeholder="0.00" />
                <select value={form.cost_unit} onChange={set('cost_unit')}>
                  {['USD', 'EUR', 'GBP', 'INR'].map((c) => <option key={c}>{c}</option>)}
                </select>
              </div>
            </Field>
            <div />

            <Field label="Description" full>
              <textarea value={form.description} onChange={set('description')} rows={3} placeholder="Description" />
            </Field>
          </div>
        </div>

        <div className="modal-foot">
          <button className="btn btn-ghost" onClick={onClose} disabled={saving}>Cancel</button>
          <button className="btn btn-primary" onClick={submit} disabled={saving}>
            {saving ? <Spinner /> : <Icon name="upload" size={15} />}
            {isEdit ? 'Save Changes' : 'Save'}
          </button>
        </div>
      </div>
    </div>
  )
}

function Field({ label, required, error, full, children }) {
  return (
    <div className={`field ${full ? 'full' : ''} ${error ? 'has-error' : ''}`}>
      <label>{label}{required && <span className="req">*</span>}</label>
      {children}
      {error && <span className="field-error">{error}</span>}
    </div>
  )
}
