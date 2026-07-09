import { useEffect, useMemo, useRef, useState } from 'react'
import Icon from './Icon.jsx'
import { Skeleton, Spinner } from './Skeleton.jsx'
import { useApp } from '../context/AppContext.jsx'
import { useToast } from '../context/ToastContext.jsx'
import { statusInfo } from '../config.js'
import api from '../services/api.js'
import { getLocalImage, setLocalImage } from '../services/localImages.js'
import { firstAssetImage } from '../services/assetImage.js'
import ImagePreview from './ImagePreview.jsx'
import { inspectionsFor, inventoryFor, alertsFor } from '../services/mock.js'
import TicketModal from './TicketModal.jsx'
import TicketDetailDrawer from './TicketDetailDrawer.jsx'
import QrScanModal from './QrScanModal.jsx'
import BarcodeScanModal from './BarcodeScanModal.jsx'
import DigitalTwinModal from './DigitalTwinModal.jsx'
import { statusLabel, statusTone, categoryLabel } from '../config.js'

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

const SUBTABS = ['Info', 'Sensors', 'Notes', 'Inspections', 'Work Orders', 'QR Code', 'Barcode', 'NFC', 'Inventory', 'Documents', 'Activity']

// ALL editable text/select fields (img.png / img_2).
const EDITABLE = [
  { key: 'user_data_name', label: 'Asset Name', type: 'text' },
  // "Display Name" edits the same value as "Asset Name" (user_data_name); the discovered
  // display_name column is never written by the backend edit path.
  { key: 'user_data_name', label: 'Display Name', type: 'text' },
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
  // Building / Floor / Location are a cascade (rendered separately in EditForm) — device stores location_id.
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
  const [preview, setPreview] = useState(false)   // asset-image lightbox
  const [imgBusy, setImgBusy] = useState(false)

  // real sensors (added/listed on the integrations service; deviceId relates them)
  const [sensors, setSensors] = useState(null)
  const [sensorBusy, setSensorBusy] = useState(false)
  // sensor count fetched by cloud-device-asset via Dapr from sclera-integrations (other DB)
  const [sensorCount, setSensorCount] = useState(null)
  // real notes
  const [notes, setNotes] = useState(null)
  const [noteBusy, setNoteBusy] = useState(false)
  // real documents
  const [documents, setDocuments] = useState(null)
  const [docBusy, setDocBusy] = useState(false)
  // QR code tagging (this asset)
  const [qrTags, setQrTags] = useState(null)
  const [qrUntagged, setQrUntagged] = useState([])
  const [qrBusy, setQrBusy] = useState(false)
  // Bar code tagging (this asset)
  const [barcodeTags, setBarcodeTags] = useState(null)
  const [barcodeUntagged, setBarcodeUntagged] = useState([])
  const [barcodeBusy, setBarcodeBusy] = useState(false)
  // NFC tagging (this asset)
  const [nfcTags, setNfcTags] = useState(null)
  const [nfcUntagged, setNfcUntagged] = useState([])
  const [nfcBusy, setNfcBusy] = useState(false)
  // networks (for moving the device between gateways during edit)
  const [networks, setNetworks] = useState([])
  // Building -> Floor -> Location cascade options
  const [buildings, setBuildings] = useState([])
  const [floors, setFloors] = useState([])
  const [locations, setLocations] = useState([])
  const [workorders, setWorkorders] = useState(null)
  const [woModal, setWoModal] = useState(false)
  const [woOpenId, setWoOpenId] = useState(null)
  const [woEditTicket, setWoEditTicket] = useState(null)
  const [twinOpen, setTwinOpen] = useState(false)   // digital-twin editor/viewer
  useEffect(() => {
    api.listNetworks({ vdmsId: ctx.vdmsId }).then((l) => setNetworks((Array.isArray(l) ? l : []).map((n) => n.name).filter(Boolean))).catch(() => {})
    api.getBuildings(ctx).then((b) => setBuildings(Array.isArray(b) ? b : [])).catch(() => {})
  }, []) // eslint-disable-line react-hooks/exhaustive-deps

  useEffect(() => {
    if (!deviceId) return
    let alive = true
    setLoading(true); setError(null); setSub('Info'); setEditing(false); setSensors(null); setSensorCount(null); setNotes(null); setDocuments(null); setWorkorders(null)
    setQrTags(null); setQrUntagged([]); setBarcodeTags(null); setBarcodeUntagged([]); setNfcTags(null); setNfcUntagged([])
    setFloors([]); setLocations([])
    api.getDevice(deviceId, ctx)
      .then((d) => {
        if (!alive) return
        setDevice(d); setForm(toForm(d))
        // Pre-load the cascade so Building/Floor/Location show selected when editing.
        if (d.building_id) api.getFloorsByBuilding(d.building_id, ctx).then((f) => alive && setFloors(Array.isArray(f) ? f : [])).catch(() => {})
        if (d.floor_id) api.getLocationsByFloor(d.floor_id, ctx).then((l) => alive && setLocations(Array.isArray(l) ? l : [])).catch(() => {})
      })
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

  // Sensor list (integrations service, via gateway) + the live count fetched by cloud-device-asset
  // over Dapr from sclera-integrations (separate service + integrations_svc DB).
  const loadSensorCount = () => api.sensorCount(deviceId)
    .then((r) => setSensorCount(typeof r?.count === 'number' ? r.count : null))
    .catch(() => setSensorCount(null))
  const loadSensors = () => {
    api.listSensors(deviceId, ctx).then((s) => setSensors(Array.isArray(s) ? s : [])).catch(() => setSensors([]))
    loadSensorCount()
  }
  // Eager-load sensors on open so the Sensors count/badge is correct without first opening the tab.
  // Guarded so a slow fetch for a previous asset can't overwrite the newly-opened one's sensors.
  useEffect(() => {
    if (!deviceId) return
    let alive = true
    api.listSensors(deviceId, ctx)
      .then((s) => { if (alive) setSensors(Array.isArray(s) ? s : []) })
      .catch(() => { if (alive) setSensors([]) })
    api.sensorCount(deviceId)
      .then((r) => { if (alive) setSensorCount(typeof r?.count === 'number' ? r.count : null) })
      .catch(() => { if (alive) setSensorCount(null) })
    return () => { alive = false }
  }, [deviceId]) // eslint-disable-line react-hooks/exhaustive-deps

  const loadNotes = () => api.listNotes(deviceId, ctx).then((n) => setNotes(Array.isArray(n) ? n : [])).catch(() => setNotes([]))
  useEffect(() => { if (sub === 'Notes' && notes == null) loadNotes() }, [sub]) // eslint-disable-line react-hooks/exhaustive-deps

  const loadDocuments = () => api.listDocuments(deviceId, ctx).then((d) => setDocuments(Array.isArray(d) ? d : [])).catch(() => setDocuments([]))
  useEffect(() => { if (sub === 'Documents' && documents == null) loadDocuments() }, [sub]) // eslint-disable-line react-hooks/exhaustive-deps

  const loadWorkorders = () => api.ticketsByDevice(deviceId, {}, ctx)
    .then((t) => setWorkorders(Array.isArray(t) ? t : [])).catch(() => setWorkorders([]))
  useEffect(() => { if (sub === 'Work Orders' && workorders == null) loadWorkorders() }, [sub]) // eslint-disable-line react-hooks/exhaustive-deps

  const loadQrTags = async () => {
    try {
      const [tags, untagged, clientTags] = await Promise.all([
        api.qrCodesForDevice(deviceId, ctx),
        api.untaggedQrCodes(ctx),
        // Client QRs are a bonus — don't let their failure blank the whole panel.
        api.clientQrCodesForDevice(deviceId, ctx).catch(() => []),
      ])
      // Merge Sclera-generated (qr_code) and client (client_qr_code) codes into one list;
      // mark client rows so untag routes to the right endpoint.
      const sclera = Array.isArray(tags) ? tags : []
      const client = (Array.isArray(clientTags) ? clientTags : []).map((c) => ({
        id: c.clientQrCodeId || c.client_qr_code_id || c.id,
        _client: true,
      }))
      setQrTags([...sclera, ...client])
      setQrUntagged(Array.isArray(untagged) ? untagged : [])
    } catch (e) { setQrTags([]); toast.error(`Load QR codes failed: ${e.message}`) }
  }
  useEffect(() => { if (sub === 'QR Code' && qrTags == null) loadQrTags() }, [sub]) // eslint-disable-line react-hooks/exhaustive-deps

  const tagQr = async (qrCodeId) => {
    if (!qrCodeId) { toast.error('Select or paste a QR code id'); return }
    setQrBusy(true)
    try {
      // A Sclera-generated code exists in the qr_code table → tag it there. Anything else
      // (e.g. a QR scanned from online) isn't in the DB → tag it as a client QR instead.
      const known = await api.qrCodeExistsInDb(qrCodeId)
      if (known) await api.tagQrCodeToDevice(qrCodeId, deviceId, ctx)
      else await api.tagClientQrCode(qrCodeId, deviceId, ctx)
      toast.success(`Tagged QR ${qrCodeId}`); setQrTags(null); await loadQrTags(); onChanged?.()
    }
    catch (e) { toast.error(`Tag failed: ${e.message}`) }
    finally { setQrBusy(false) }
  }
  const untagQr = async (qrCodeId, isClient) => {
    setQrBusy(true)
    try {
      if (isClient) await api.untagClientQrCode(qrCodeId, ctx)
      else await api.untagQrCodeFromDevice(qrCodeId, ctx)
      toast.success(`Untagged QR ${qrCodeId}`); setQrTags(null); await loadQrTags(); onChanged?.()
    }
    catch (e) { toast.error(`Untag failed: ${e.message}`) }
    finally { setQrBusy(false) }
  }

  // Bar codes are all client-style: no generated pool routing — tag always inserts-if-new.
  const loadBarcodeTags = async () => {
    try {
      const [tags, untagged] = await Promise.all([
        api.barcodesForDevice(deviceId, ctx),
        api.untaggedBarcodes(ctx).catch(() => []),
      ])
      // Bar codes are all client codes: the API returns full ClientBarCode rows. Normalise
      // to the clientBarCodeId (the scannable value) so the list/untag use that — not the
      // internal row UUID — and the untagged dropdown renders strings, not objects.
      const bcId = (o) => (typeof o === 'string' ? o : (o.clientBarCodeId || o.client_bar_code_id || o.id))
      setBarcodeTags((Array.isArray(tags) ? tags : []).map((c) => ({ ...c, id: bcId(c) })))
      setBarcodeUntagged((Array.isArray(untagged) ? untagged : []).map(bcId).filter(Boolean))
    } catch (e) { setBarcodeTags([]); toast.error(`Load bar codes failed: ${e.message}`) }
  }
  useEffect(() => { if (sub === 'Barcode' && barcodeTags == null) loadBarcodeTags() }, [sub]) // eslint-disable-line react-hooks/exhaustive-deps

  const tagBarcode = async (barcodeId) => {
    if (!barcodeId) { toast.error('Select or paste a bar code id'); return }
    setBarcodeBusy(true)
    try {
      await api.tagBarcodeToDevice(barcodeId, deviceId, ctx)
      toast.success(`Tagged bar code ${barcodeId}`); setBarcodeTags(null); await loadBarcodeTags(); onChanged?.()
    }
    catch (e) { toast.error(`Tag failed: ${e.message}`) }
    finally { setBarcodeBusy(false) }
  }
  const untagBarcode = async (barcodeId) => {
    setBarcodeBusy(true)
    try {
      await api.untagBarcode(barcodeId, ctx)
      toast.success(`Untagged bar code ${barcodeId}`); setBarcodeTags(null); await loadBarcodeTags(); onChanged?.()
    }
    catch (e) { toast.error(`Untag failed: ${e.message}`) }
    finally { setBarcodeBusy(false) }
  }

  const loadNfcTags = async () => {
    try {
      const [tags, untagged, clientTags] = await Promise.all([
        api.nfcForDevice(deviceId, ctx),
        api.untaggedNfc(ctx),
        // Client NFC tags are a bonus — don't let their failure blank the whole panel.
        api.clientNfcForDevice(deviceId, ctx).catch(() => []),
      ])
      const sclera = Array.isArray(tags) ? tags : []
      const client = (Array.isArray(clientTags) ? clientTags : []).map((c) => ({
        id: c.nfcId || c.nfc_id || c.id,
        _client: true,
      }))
      setNfcTags([...sclera, ...client])
      setNfcUntagged(Array.isArray(untagged) ? untagged : [])
    } catch (e) { setNfcTags([]); toast.error(`Load NFC tags failed: ${e.message}`) }
  }
  useEffect(() => { if (sub === 'NFC' && nfcTags == null) loadNfcTags() }, [sub]) // eslint-disable-line react-hooks/exhaustive-deps

  const tagNfc = async (nfcId) => {
    if (!nfcId) { toast.error('Select or paste an NFC id'); return }
    setNfcBusy(true)
    try {
      // A Sclera-generated NFC exists in the nfc table → tag it there. Anything else
      // (e.g. an NFC read from an unknown tag) isn't in the DB → tag it as a client NFC.
      const known = await api.nfcExistsInDb(nfcId)
      if (known) await api.tagNfcToDevice(nfcId, deviceId, ctx)
      else await api.tagClientNfc(nfcId, deviceId, ctx)
      toast.success(`Tagged NFC ${nfcId}`); setNfcTags(null); await loadNfcTags(); onChanged?.()
    }
    catch (e) { toast.error(`Tag failed: ${e.message}`) }
    finally { setNfcBusy(false) }
  }
  const untagNfc = async (nfcId, isClient) => {
    setNfcBusy(true)
    try {
      if (isClient) await api.untagClientNfc(nfcId, ctx)
      else await api.untagNfcFromDevice(nfcId, ctx)
      toast.success(`Untagged NFC ${nfcId}`); setNfcTags(null); await loadNfcTags(); onChanged?.()
    }
    catch (e) { toast.error(`Untag failed: ${e.message}`) }
    finally { setNfcBusy(false) }
  }

  const name = device?.user_data_name || device?.display_name || device?.name || deviceId
  const st = device ? statusInfo(device) : null
  // Stored (backend) image vs a session-only local one added at create time.
  const storedImg = firstAssetImage(device?.asset_image_url)
  const assetImg = storedImg || getLocalImage(deviceId)

  const inspections = useMemo(() => (device ? inspectionsFor(device) : []), [device])
  const inventory = useMemo(() => (device ? inventoryFor(device) : null), [device])
  const alerts = useMemo(() => (device ? alertsFor(device) : []), [device])
  const bldOptions = useMemo(() => buildings.map((b) => ({ id: b.id || b.building_id, name: b.name })), [buildings])
  const floorOptions = useMemo(() => floors.map((f) => ({ id: f.id || f.floor_id, name: f.name || f.floor_name || f.id })), [floors])
  const locOptions = useMemo(() => locations.map((l) => ({ id: l.id || l.location_id, name: l.name || l.location_name || l.id })), [locations])

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
    f.building_id = d.building_id || ''
    f.floor_id = d.floor_id || ''
    f.location_id = d.location_id || ''
    return f
  }
  const set = (k) => (e) => setForm((f) => ({ ...f, [k]: e.target?.value ?? e }))
  const setFlag = (k) => (e) => setForm((f) => ({ ...f, [k]: e.target.checked }))

  // Building -> Floor -> Location cascade handlers.
  const onBuilding = (e) => {
    const building_id = e.target.value
    setForm((f) => ({ ...f, building_id, floor_id: '', location_id: '' }))
    setFloors([]); setLocations([])
    if (building_id) api.getFloorsByBuilding(building_id, ctx).then((fl) => setFloors(Array.isArray(fl) ? fl : [])).catch(() => {})
  }
  const onFloor = (e) => {
    const floor_id = e.target.value
    setForm((f) => ({ ...f, floor_id, location_id: '' }))
    setLocations([])
    if (floor_id) api.getLocationsByFloor(floor_id, ctx).then((l) => setLocations(Array.isArray(l) ? l : [])).catch(() => {})
  }

  const save = async () => {
    setBusy(true)
    try {
      const payload = { ...device }
      EDITABLE.forEach((e) => {
        if (e.key === 'cost') return
        const v = form[e.key]
        if (e.key === 'user_data_name') { payload.user_data_name = v; payload.display_name = v }
        else if (e.key === 'vendor') { payload.vendor = v; payload.user_data_vendor = v }
        else if (e.key === 'model') { payload.model = v; payload.user_data_model = v }
        else payload[e.key] = v === '' ? null : v
      })
      payload.cost_value = form.cost_value === '' ? null : Number(form.cost_value)
      payload.cost_unit = form.cost_unit
      payload.location_id = form.location_id || null // Building/Floor are derived from the location
      FLAGS.forEach((fl) => { payload[fl.key] = fl.bool ? Boolean(form[fl.key]) : (form[fl.key] ? 1 : 0) })

      // Edit targets the device's OWN network (backend WHERE matches docker_name).
      await api.editDevice(deviceId, payload, { ...ctx, docker: device?.docker_name || ctx.docker })
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

  // Delete the asset image: removes the file from disk + clears asset_image_url (backend),
  // and drops the session-local copy. Closes the preview and refreshes the device.
  const deleteImage = async () => {
    setImgBusy(true)
    try {
      if (storedImg) await api.deleteAssetImages(deviceId, [storedImg], ctx)
      setLocalImage(deviceId, '')
      toast.success('Image deleted')
      if (storedImg) {
        const fresh = await api.getDevice(deviceId, ctx)
        setDevice(fresh); setForm(toForm(fresh))
      }
      setPreview(false)
      onChanged?.()
    } catch (e) { toast.error(`Delete image failed: ${e.message}`) }
    finally { setImgBusy(false) }
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

  const addDocument = async ({ file, name }) => {
    setDocBusy(true)
    try { await api.uploadDocument(deviceId, { file, name }, ctx); toast.success('Document uploaded'); await loadDocuments(); onChanged?.() }
    catch (e) { toast.error(`Upload failed: ${e.message}`) }
    finally { setDocBusy(false) }
  }
  const removeDocument = async (id) => {
    setDocBusy(true)
    try { await api.deleteDocument(id, ctx); toast.success('Document deleted'); await loadDocuments(); onChanged?.() }
    catch (e) { toast.error(`Delete failed: ${e.message}`) }
    finally { setDocBusy(false) }
  }

  const counts = device ? [
    ['Sensors', sensors?.length ?? (device.snmp_count || 0)], ['Inspections', inspections.length], ['Work Orders', workorders?.length ?? 0],
    ['Alerts', alerts.length], ['Tickets', device.ticket_count || 0], ['Notes', device.notes_count || 0],
  ] : []

  return (
    <>
      <div className="panel-scrim" onClick={onClose} />
      <aside className="detail-panel" role="dialog" aria-label="Asset details">
        <div className="dp-head">
          <div className="dp-title">
            <div className="dp-avatar">{assetImg ? (
              <button type="button" className="dp-avatar-btn" onClick={() => setPreview(true)} title="Preview image">
                <img src={assetImg} alt="" className="avatar-img" />
              </button>
            ) : <Icon name="device" size={18} />}</div>
            <span className="dp-name" title={name}>{loading ? 'Loading…' : name}</span>
          </div>
          <div className="dp-head-actions">
            <button className="dp-icontext" onClick={() => setSub('Barcode')} title="Tag a bar code to this asset"><Icon name="barcode" size={16} /><span>{editing ? 'Re-assign Bar Code' : 'Tag Bar Code'}</span></button>
            <button className="dp-icontext" onClick={() => setSub('QR Code')} title="Tag a QR code to this asset"><Icon name="qrcode" size={16} /><span>{editing ? 'Re-assign' : 'Tag Asset'}</span></button>
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
          <button className="btn btn-ghost sm" disabled={!device} onClick={() => setTwinOpen(true)}><Icon name="layers" size={14} /> Digital Twin</button>
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
            editing ? <EditForm form={form} set={set} setFlag={setFlag} networks={networks} bldOptions={bldOptions} floorOptions={floorOptions} locOptions={locOptions} onBuilding={onBuilding} onFloor={onFloor} /> : <InfoView device={device} name={name} st={st} counts={counts} />
          ) : sub === 'Sensors' ? (
            <SensorsView sensors={sensors} count={sensorCount} busy={sensorBusy} onAdd={addSensor} onRemove={removeSensor} onRefresh={loadSensors} />
          ) : sub === 'Notes' ? (
            <NotesView notes={notes} busy={noteBusy} onAdd={addNote} onRemove={removeNote} onRefresh={loadNotes} />
          ) : sub === 'Inspections' ? <InspectionsView items={inspections} />
            : sub === 'Work Orders' ? <WorkOrdersView items={workorders} onAdd={() => { setWoEditTicket(null); setWoModal(true) }} onOpen={setWoOpenId} />
            : sub === 'QR Code' ? <QrCodeTagView tags={qrTags} untagged={qrUntagged} busy={qrBusy} onTag={tagQr} onUntag={untagQr} onRefresh={() => { setQrTags(null); loadQrTags() }} />
            : sub === 'Barcode' ? <BarcodeTagView tags={barcodeTags} untagged={barcodeUntagged} busy={barcodeBusy} onTag={tagBarcode} onUntag={untagBarcode} onRefresh={() => { setBarcodeTags(null); loadBarcodeTags() }} />
            : sub === 'NFC' ? <NfcTagView tags={nfcTags} untagged={nfcUntagged} busy={nfcBusy} onTag={tagNfc} onUntag={untagNfc} onRefresh={() => { setNfcTags(null); loadNfcTags() }} />
            : sub === 'Inventory' ? <InventoryView inv={inventory} />
            : sub === 'Documents' ? <DocumentsView docs={documents} busy={docBusy} onAdd={addDocument} onRemove={removeDocument} onRefresh={loadDocuments} />
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
      <ImagePreview
        open={preview}
        src={assetImg}
        alt={name}
        onClose={() => setPreview(false)}
        onDelete={assetImg ? deleteImage : undefined}
        deleting={imgBusy}
      />
      <TicketModal
        open={woModal}
        ticket={woEditTicket}
        deviceId={deviceId}
        dockerName={device?.docker_name || ctx.docker}
        onClose={() => { setWoModal(false); setWoEditTicket(null) }}
        onSaved={loadWorkorders}
      />
      <TicketDetailDrawer
        ticketId={woOpenId}
        onClose={() => setWoOpenId(null)}
        onChanged={loadWorkorders}
        onEdit={(t) => { setWoOpenId(null); setWoEditTicket(t); setWoModal(true) }}
      />
      {twinOpen && device && (
        <DigitalTwinModal device={device} sensors={sensors} onClose={() => setTwinOpen(false)} />
      )}
    </>
  )
}

// Device custom_fields is stored as a JSON array of single-key {label:value} objects
// (asset-mapper import) or a plain {label:value} object. Flatten to [label, value] pairs.
function parseCustomFields(raw) {
  if (Array.isArray(raw)) return flattenCustomFields(raw)
  if (raw && typeof raw === 'object') return Object.entries(raw)
  if (typeof raw !== 'string') return []
  const s = raw.trim()
  if (!s || s === '[]' || s === '{}' || s === 'null') return []
  try {
    const parsed = JSON.parse(s)
    if (Array.isArray(parsed)) return flattenCustomFields(parsed)
    if (parsed && typeof parsed === 'object') return Object.entries(parsed)
  } catch { /* not JSON — ignore */ }
  return []
}
function flattenCustomFields(arr) {
  const out = []
  for (const item of arr) {
    if (item && typeof item === 'object') for (const [k, v] of Object.entries(item)) out.push([k, v])
  }
  return out
}

function InfoView({ device, name, st, counts }) {
  const d = device
  const customFields = parseCustomFields(d.custom_fields)
  return (
    <>
      <div className="dp-countbar">
        {counts.map(([label, val]) => (
          <div className="dp-count" key={label}><span className="dp-count-n">{val}</span><span className="dp-count-l">{label}</span></div>
        ))}
      </div>
      <div className="dp-fields">
        <Row k="Asset Name" v={name} />
        <Row k="Display Name" v={name} />
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

      {customFields.length > 0 && (
        <Section title="Custom Fields">
          {customFields.map(([k, v]) => <Row key={k} k={k} v={dash(v)} accent />)}
        </Section>
      )}

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

function EditForm({ form, set, setFlag, networks = [], bldOptions = [], floorOptions = [], locOptions = [], onBuilding, onFloor }) {
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
      <div className="dp-editrow">
        <label>Building</label>
        <select value={form.building_id || ''} onChange={onBuilding}>
          <option value="">Select...</option>
          {bldOptions.map((b) => <option key={b.id} value={b.id}>{b.name}</option>)}
        </select>
      </div>
      <div className="dp-editrow">
        <label>Floor</label>
        <select value={form.floor_id || ''} onChange={onFloor} disabled={!form.building_id}>
          <option value="">{form.building_id ? 'Select...' : 'Select a building first'}</option>
          {floorOptions.map((f) => <option key={f.id} value={f.id}>{f.name}</option>)}
        </select>
      </div>
      <div className="dp-editrow">
        <label>Location</label>
        <select value={form.location_id || ''} onChange={set('location_id')} disabled={!form.floor_id}>
          <option value="">{form.floor_id ? 'Select...' : 'Select a floor first'}</option>
          {locOptions.map((l) => <option key={l.id} value={l.id}>{l.name}</option>)}
        </select>
      </div>
      {EDITABLE.map((f, i) => (
        <div className="dp-editrow" key={i}>
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

function SensorsView({ sensors, count, busy, onAdd, onRemove, onRefresh }) {
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
        <span className="svc-note">Sensors live in the <b>sclera-integrations</b> service (its own <b>integrations_svc</b> DB)</span>
        <div className="svc-head-actions">
          <button className="icon-btn" onClick={onRefresh} title="Refresh"><Icon name="refresh" size={14} /></button>
          <button className="btn btn-primary sm" onClick={() => setShow((s) => !s)}><Icon name="plus" size={13} /> Add Sensor</button>
        </div>
      </div>

      <div style={{
        marginBottom: 12, padding: '10px 12px', borderRadius: 8, fontSize: 12, lineHeight: 1.5,
        border: '1px solid var(--border, #334155)', borderLeft: '3px solid var(--accent, #4f46e5)',
        background: 'rgba(79,70,229,0.08)', color: 'var(--text, #e2e8f0)',
      }}>
        <b style={{ fontSize: 15 }}>{count == null ? '—' : count}</b> sensor{count === 1 ? '' : 's'} for this asset —
        counted by <b>cloud-device-asset → Dapr → sclera-integrations</b> from the{' '}
        <b>integrations_svc</b> database. Add one below and watch this count update from the other service.
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
function WorkOrdersView({ items, onAdd, onOpen }) {
  return (
    <>
      <div className="svc-head">
        <span className="svc-note">Tickets from the workorders service for this asset</span>
        <button className="btn btn-primary sm" onClick={onAdd}><Icon name="plus" size={13} /> Raise ticket</button>
      </div>
      {items == null ? (
        <div className="log-list"><Skeleton w="100%" h={44} /><Skeleton w="100%" h={44} /></div>
      ) : items.length === 0 ? (
        <Empty icon="clipboard" label="No work orders for this asset" />
      ) : (
        <div className="log-list">
          {items.map((w) => (
            <div className="svc-row clickable" key={w.id} onClick={() => onOpen(w.id)}>
              <div className="svc-row-main">
                <div className="svc-row-title">{w.number ? `${w.number} · ` : ''}{w.name || 'Work order'}</div>
                <div className="svc-row-meta">Assignee {w.assignee_user_email || '—'} · {w.category ? categoryLabel(w.category) : '—'}</div>
              </div>
              <div className="svc-row-side"><span className={`badge tone-${statusTone(w.status)}`}>{statusLabel(w.status)}</span></div>
            </div>
          ))}
        </div>
      )}
    </>
  )
}
// Sclera codes (QR / bar code / NFC) can encode "<server_url>/<id>" (see backend
// QrCodeService), but tagging uses the raw id. So if the scan/read yields a URL, take
// its last path segment; otherwise (a plain id, e.g. a client code) use the text as-is.
function codeIdFromText(text) {
  const t = (text || '').trim()
  if (!t) return ''
  if (/^https?:\/\//i.test(t)) {
    try {
      const segs = new URL(t).pathname.split('/').filter(Boolean)
      if (segs.length) return decodeURIComponent(segs[segs.length - 1])
    } catch { /* not a parseable URL — fall through */ }
  }
  return t
}
// Back-compat alias — QR code paths still call qrIdFromText.
const qrIdFromText = codeIdFromText

function QrCodeTagView({ tags, untagged, busy, onTag, onUntag, onRefresh }) {
  const [sel, setSel] = useState('')
  const [manual, setManual] = useState('')
  const [scanning, setScanning] = useState(false)
  const idToTag = (manual.trim() || sel).trim()
  // A scanned QR encodes its id — drop it into the same field the paste box uses,
  // so it flows through the existing tag logic. Works for generated or client QRs.
  const onScanned = (text) => {
    setScanning(false)
    const id = qrIdFromText(text)
    if (id) { setSel(''); setManual(id) }
  }
  return (
    <>
      {scanning && <QrScanModal onResult={onScanned} onClose={() => setScanning(false)} title="Scan QR to tag this asset" />}
      <div className="svc-head">
        <span className="svc-note">QR codes tagged to this asset</span>
        <button className="icon-btn" onClick={onRefresh} title="Refresh"><Icon name="refresh" size={14} /></button>
      </div>

      <div className="svc-row" style={{ gap: 8, flexWrap: 'wrap', alignItems: 'center' }}>
        <select value={sel} onChange={(e) => { setSel(e.target.value); setManual('') }} style={{ minWidth: 200 }}>
          <option value="">{untagged.length ? `Select untagged QR (${untagged.length})` : 'No untagged QR codes'}</option>
          {untagged.map((id) => <option key={id} value={id}>{id}</option>)}
        </select>
        <span className="muted">or</span>
        <input placeholder="paste QR code id" value={manual} onChange={(e) => { setManual(e.target.value); setSel('') }} style={{ minWidth: 180 }} />
        <button className="btn btn-ghost sm" disabled={busy} onClick={() => setScanning(true)} title="Scan a QR code with the camera or an image">
          <Icon name="camera" size={14} /> Scan
        </button>
        <button className="btn btn-primary sm" disabled={busy || !idToTag} onClick={() => onTag(idToTag)}>
          {busy ? <Spinner size={13} /> : <Icon name="qrcode" size={14} />} Tag
        </button>
      </div>

      {tags == null ? (
        <div className="log-list"><Skeleton w="100%" h={44} /><Skeleton w="100%" h={44} /></div>
      ) : tags.length === 0 ? (
        <Empty icon="qrcode" label="No QR codes tagged to this asset yet" />
      ) : (
        <div className="log-list">
          {tags.map((q) => (
            <div className="svc-row" key={q.id}>
              <div className="svc-row-main" style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                {(q.imageUrl || q.image_url)
                  ? <img src={q.imageUrl || q.image_url} alt="" style={{ width: 30, height: 30, borderRadius: 4, background: '#fff' }} />
                  : <Icon name="qrcode" size={18} />}
                <code style={{ fontSize: 11 }}>{q.id}</code>
              </div>
              <div className="svc-row-side">
                <button className="btn btn-ghost sm" disabled={busy} onClick={() => onUntag(q.id, q._client)}><Icon name="x" size={13} /> Untag</button>
              </div>
            </div>
          ))}
        </div>
      )}
    </>
  )
}

function BarcodeTagView({ tags, untagged, busy, onTag, onUntag, onRefresh }) {
  const [sel, setSel] = useState('')
  const [manual, setManual] = useState('')
  const [scanning, setScanning] = useState(false)
  const idToTag = (manual.trim() || sel).trim()
  // A scanned bar code encodes its id — drop it into the same field the paste box uses,
  // so it flows through the existing tag logic.
  const onScanned = (text) => {
    setScanning(false)
    const id = codeIdFromText(text)
    if (id) { setSel(''); setManual(id) }
  }
  return (
    <>
      {scanning && <BarcodeScanModal onResult={onScanned} onClose={() => setScanning(false)} title="Scan bar code to tag this asset" />}
      <div className="svc-head">
        <span className="svc-note">Bar codes tagged to this asset</span>
        <button className="icon-btn" onClick={onRefresh} title="Refresh"><Icon name="refresh" size={14} /></button>
      </div>

      <div className="svc-row" style={{ gap: 8, flexWrap: 'wrap', alignItems: 'center' }}>
        <select value={sel} onChange={(e) => { setSel(e.target.value); setManual('') }} style={{ minWidth: 200 }}>
          <option value="">{untagged.length ? `Select untagged bar code (${untagged.length})` : 'No untagged bar codes'}</option>
          {untagged.map((id) => <option key={id} value={id}>{id}</option>)}
        </select>
        <span className="muted">or</span>
        <input placeholder="paste bar code id" value={manual} onChange={(e) => { setManual(e.target.value); setSel('') }} style={{ minWidth: 180 }} />
        <button className="btn btn-ghost sm" disabled={busy} onClick={() => setScanning(true)} title="Scan a bar code with the camera or an image">
          <Icon name="camera" size={14} /> Scan
        </button>
        <button className="btn btn-primary sm" disabled={busy || !idToTag} onClick={() => onTag(idToTag)}>
          {busy ? <Spinner size={13} /> : <Icon name="barcode" size={14} />} Tag
        </button>
      </div>

      {tags == null ? (
        <div className="log-list"><Skeleton w="100%" h={44} /><Skeleton w="100%" h={44} /></div>
      ) : tags.length === 0 ? (
        <Empty icon="barcode" label="No bar codes tagged to this asset yet" />
      ) : (
        <div className="log-list">
          {tags.map((q) => (
            <div className="svc-row" key={q.id}>
              <div className="svc-row-main" style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                {(q.imageUrl || q.image_url)
                  ? <img src={q.imageUrl || q.image_url} alt="" style={{ width: 30, height: 30, borderRadius: 4, background: '#fff' }} />
                  : <Icon name="barcode" size={18} />}
                <code style={{ fontSize: 11 }}>{q.id}</code>
              </div>
              <div className="svc-row-side">
                <button className="btn btn-ghost sm" disabled={busy} onClick={() => onUntag(q.id)}><Icon name="x" size={13} /> Untag</button>
              </div>
            </div>
          ))}
        </div>
      )}
    </>
  )
}

function NfcTagView({ tags, untagged, busy, onTag, onUntag, onRefresh }) {
  const [sel, setSel] = useState('')
  const [manual, setManual] = useState('')
  const [reading, setReading] = useState(false)
  const idToTag = (manual.trim() || sel).trim()
  // Web NFC is only available on supported devices (Android Chrome, over https).
  const nfcSupported = typeof window !== 'undefined' && 'NDEFReader' in window
  // Read an NFC tag via Web NFC and drop the decoded id into the manual field, so it
  // flows through the same tag logic used by the select/paste box.
  const readNfc = async () => {
    if (!nfcSupported) return
    setReading(true)
    try {
      const reader = new window.NDEFReader()
      await reader.scan()
      reader.onreading = (event) => {
        let text = event?.serialNumber || ''
        for (const rec of event?.message?.records || []) {
          try {
            const dec = new TextDecoder(rec.encoding || 'utf-8')
            const val = dec.decode(rec.data)
            if (val) { text = val; break }
          } catch { /* not a decodable record — keep the serial number */ }
        }
        const id = codeIdFromText(text)
        if (id) { setSel(''); setManual(id) }
        setReading(false)
      }
      reader.onreadingerror = () => setReading(false)
    } catch { setReading(false) }
  }
  return (
    <>
      <div className="svc-head">
        <span className="svc-note">NFC tags tagged to this asset</span>
        <button className="icon-btn" onClick={onRefresh} title="Refresh"><Icon name="refresh" size={14} /></button>
      </div>

      <div className="svc-row" style={{ gap: 8, flexWrap: 'wrap', alignItems: 'center' }}>
        <select value={sel} onChange={(e) => { setSel(e.target.value); setManual('') }} style={{ minWidth: 200 }}>
          <option value="">{untagged.length ? `Select untagged NFC (${untagged.length})` : 'No untagged NFC tags'}</option>
          {untagged.map((id) => <option key={id} value={id}>{id}</option>)}
        </select>
        <span className="muted">or</span>
        <input placeholder="paste NFC id" value={manual} onChange={(e) => { setManual(e.target.value); setSel('') }} style={{ minWidth: 180 }} />
        {nfcSupported && (
          <button className="btn btn-ghost sm" disabled={busy || reading} onClick={readNfc} title="Read an NFC tag on this device">
            {reading ? <Spinner size={13} /> : <Icon name="nfc" size={14} />} {reading ? 'Tap tag…' : 'Read'}
          </button>
        )}
        {!nfcSupported && (
          <button className="btn btn-ghost sm" disabled title="NFC scanning needs a supported device">
            <Icon name="nfc" size={14} /> Read
          </button>
        )}
        <button className="btn btn-primary sm" disabled={busy || !idToTag} onClick={() => onTag(idToTag)}>
          {busy ? <Spinner size={13} /> : <Icon name="nfc" size={14} />} Tag
        </button>
      </div>

      {tags == null ? (
        <div className="log-list"><Skeleton w="100%" h={44} /><Skeleton w="100%" h={44} /></div>
      ) : tags.length === 0 ? (
        <Empty icon="nfc" label="No NFC tags tagged to this asset yet" />
      ) : (
        <div className="log-list">
          {tags.map((q) => (
            <div className="svc-row" key={q.id}>
              <div className="svc-row-main" style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                {(q.imageUrl || q.image_url)
                  ? <img src={q.imageUrl || q.image_url} alt="" style={{ width: 30, height: 30, borderRadius: 4, background: '#fff' }} />
                  : <Icon name="nfc" size={18} />}
                <code style={{ fontSize: 11 }}>{q.id}</code>
              </div>
              <div className="svc-row-side">
                <button className="btn btn-ghost sm" disabled={busy} onClick={() => onUntag(q.id, q._client)}><Icon name="x" size={13} /> Untag</button>
              </div>
            </div>
          ))}
        </div>
      )}
    </>
  )
}

function InventoryView({ inv }) {
  if (!inv) return <Empty icon="box" label="No inventory" />
  return (<><div className="svc-note">From the inventory service</div>
    <div className="dp-fields"><Row k="Tracking ID" v={inv.trackingId} accent /><Row k="Storage Location" v={inv.location} /></div>
    <Section title="Linked Parts">{inv.parts.map((p) => (
      <div className="svc-row" key={p.id}><div className="svc-row-main"><div className="svc-row-title">{p.name}</div><div className="svc-row-meta">Qty {p.qty}</div></div><span className={`badge tone-${p.status === 'In Stock' ? 'online' : 'offline'}`}>{p.status}</span></div>
    ))}</Section></>)
}
function DocumentsView({ docs, busy, onAdd, onRemove, onRefresh }) {
  const fileRef = useRef(null)
  const onPick = (e) => {
    const file = (e.target.files || [])[0]
    if (file) onAdd({ file, name: file.name })
    e.target.value = '' // allow re-selecting the same file
  }
  return (
    <>
      <div className="svc-head">
        <span className="svc-note">Documents — stored on the device-asset service</span>
        <button className="icon-btn" onClick={onRefresh} title="Refresh"><Icon name="refresh" size={14} /></button>
      </div>
      <div className="note-form-foot" style={{ marginBottom: 10 }}>
        <button className="btn btn-primary sm" disabled={busy} onClick={() => fileRef.current?.click()}>
          {busy ? <Spinner size={13} /> : <Icon name="plus" size={13} />} Upload Document
        </button>
        <input ref={fileRef} type="file" hidden onChange={onPick} />
      </div>
      {docs == null ? (
        <div className="log-list"><Skeleton w="100%" h={50} /><Skeleton w="100%" h={50} /></div>
      ) : docs.length === 0 ? (
        <Empty icon="upload" label="No documents yet — upload the first one" />
      ) : (
        <div className="log-list">{docs.map((d) => (
          <div className="svc-row" key={d.id}>
            <div className="svc-row-main">
              <div className="svc-row-title"><Icon name="upload" size={14} /> {d.name}</div>
              <div className="svc-row-meta">{d.category || 'Document'}</div>
            </div>
            <div className="svc-row-side">
              {d.link ? <a className="btn btn-ghost sm" href={d.link} target="_blank" rel="noreferrer">Open</a> : null}
              <button className="icon-btn danger" style={{ width: 24, height: 24 }} disabled={busy} onClick={() => onRemove(d.id)} title="Delete"><Icon name="trash" size={12} /></button>
            </div>
          </div>
        ))}</div>
      )}
    </>
  )
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
