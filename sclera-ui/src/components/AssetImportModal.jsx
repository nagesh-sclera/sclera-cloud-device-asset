import { useEffect, useMemo, useState } from 'react'
import Icon from './Icon.jsx'
import { Spinner } from './Skeleton.jsx'
import api from '../services/api.js'

// Import wizard mirroring the real asset-mapper flow:
//   Step 1  Asset Field Matching (img_14)  -> POST /upload (stage rows against the mapping)
//   Step 2  Assets From File   (img_16)    -> GET  /getSubSystemParentAssets (preview staged)
//   Save                       (img_17)    -> POST /saveAssets (commit staged -> devices)
// Headers are read in the browser (SheetJS) only to populate the matching screen; the original
// file is uploaded as-is and parsed server-side using the field mapping.

const IGNORE = '__ignore__'

// Fallback Sclera fields if /getAssetFields can't be reached (value = device column).
const FALLBACK_FIELDS = [
  { key: 'id', label: 'ID' },
  { key: 'subsystem_parent_id', label: 'Subsystem Parent ID' },
  { key: 'display_name', label: 'Display Name' },
  { key: 'model', label: 'Model' },
  { key: 'vendor', label: 'Vendor' },
  { key: 'type', label: 'Type' },
  { key: 'serial_number', label: 'Serial Number' },
  { key: 'mac_address', label: 'MAC Address' },
  { key: 'ip_address', label: 'IP Address' },
  { key: 'description', label: 'Description' },
]

// header (lowercased) -> device column, for the auto-guess.
const ALIASES = {
  id: 'id',
  name: 'display_name', 'display name': 'display_name', display_name: 'display_name', 'asset name': 'display_name',
  model: 'model', vendor: 'vendor', manufacturer: 'vendor',
  'serial number': 'serial_number', serial_number: 'serial_number',
  description: 'description', 'asset type': 'type', type: 'type',
  'mac address': 'mac_address', mac_address: 'mac_address',
  'ip address': 'ip_address', ip_address: 'ip_address',
  'subsystem parent id': 'subsystem_parent_id', subsystem_parent_id: 'subsystem_parent_id',
}
const guess = (h) => ALIASES[String(h || '').trim().toLowerCase()] || IGNORE

export default function AssetImportModal({ open, file, onClose, onConfirm }) {
  const [step, setStep] = useState(1)
  const [parsing, setParsing] = useState(false)
  const [error, setError] = useState(null)
  const [columns, setColumns] = useState([])   // [{ name, index }]
  const [fields, setFields] = useState(FALLBACK_FIELDS)
  const [mapping, setMapping] = useState({})    // columnName -> device column | IGNORE
  const [uploading, setUploading] = useState(false)
  const [staged, setStaged] = useState([])      // assets returned by /getSubSystemParentAssets
  const [excluded, setExcluded] = useState(() => new Set())
  const [search, setSearch] = useState('')
  const [saving, setSaving] = useState(false)

  // Read the header row (client-side) and load the Sclera field list when a file arrives.
  useEffect(() => {
    if (!open || !file) return
    let cancelled = false
    setStep(1); setError(null); setSearch(''); setStaged([]); setExcluded(new Set())
    setParsing(true)
    ;(async () => {
      try {
        const XLSX = await import('xlsx')
        const buf = await file.arrayBuffer()
        const wb = XLSX.read(buf, { type: 'array' })
        const sheet = wb.Sheets[wb.SheetNames[0]]
        if (!sheet) throw new Error('Workbook has no sheets')
        const hdr = (XLSX.utils.sheet_to_json(sheet, { header: 1 })[0] || [])
          .map((h, index) => ({ name: String(h).trim(), index }))
          .filter((c) => c.name)
        const fieldList = await api.getImportAssetFields().catch(() => null)
        if (cancelled) return
        setColumns(hdr)
        setMapping(Object.fromEntries(hdr.map((c) => [c.name, guess(c.name)])))
        if (Array.isArray(fieldList) && fieldList.length) setFields(fieldList)
      } catch (e) {
        if (!cancelled) setError(e.message || 'Could not read this file')
      } finally {
        if (!cancelled) setParsing(false)
      }
    })()
    return () => { cancelled = true }
  }, [open, file])

  const mappedCount = Object.values(mapping).filter((t) => t !== IGNORE).length

  const visible = useMemo(() => {
    const q = search.trim().toLowerCase()
    if (!q) return staged
    return staged.filter((a) => Object.values(a).some((v) => String(v).toLowerCase().includes(q)))
  }, [staged, search])

  if (!open) return null

  const setMap = (col) => (e) => setMapping((m) => ({ ...m, [col]: e.target.value }))
  const toggleExclude = (id) => setExcluded((s) => {
    const n = new Set(s); n.has(id) ? n.delete(id) : n.add(id); return n
  })

  // Step 1 -> upload: build the field mapping and stage the file server-side.
  const doUpload = async () => {
    const fieldMapping = columns
      .filter((c) => mapping[c.name] && mapping[c.name] !== IGNORE)
      .map((c) => ({ originalKey: [c.name], deviceKey: mapping[c.name], index: [c.index] }))
    setUploading(true); setError(null)
    try {
      await api.uploadImport(file, fieldMapping)
      const rows = await api.getImportParents({ importType: 'spreadsheet' })
      setStaged(Array.isArray(rows) ? rows : [])
      setExcluded(new Set())
      setStep(2)
    } catch (e) { setError(e.message || 'Upload failed') }
    finally { setUploading(false) }
  }

  // Save: commit the kept staged assets into real devices.
  const doSave = async () => {
    const ids = staged.map((a) => a.id).filter((id) => !excluded.has(id))
    if (!ids.length) { setError('Select at least one asset to import'); return }
    setSaving(true); setError(null)
    try {
      const res = await api.saveImportedAssets(ids)
      await onConfirm(res?.saved ?? ids.length)
    } catch (e) { setError(e.message || 'Save failed') }
    finally { setSaving(false) }
  }

  const keptCount = staged.length - excluded.size

  return (
    <div className="modal-overlay" onMouseDown={(e) => { if (e.target === e.currentTarget && !uploading && !saving) onClose() }}>
      <div className="modal import-wizard" role="dialog" aria-modal="true">
        <div className="modal-head">
          <h2>{step === 1 ? 'Asset Field Matching' : 'Assets From File'}</h2>
          {step === 2 && (
            <div className="import-search">
              <Icon name="search" size={15} />
              <input placeholder="Type to search asset" value={search} onChange={(e) => setSearch(e.target.value)} />
            </div>
          )}
          <button className="icon-btn" onClick={onClose} aria-label="Close" disabled={uploading || saving}><Icon name="x" /></button>
        </div>

        <div className="modal-body">
          {error && <div className="import-error"><Icon name="info" size={15} /> {error}</div>}

          {parsing ? (
            <div className="empty-state"><Spinner size={22} /><p>Reading spreadsheet…</p></div>
          ) : step === 1 ? (
            columns.length === 0 ? (
              <div className="empty-state"><Icon name="info" size={26} /><p>No columns found in this file</p></div>
            ) : (
              <div className="field-match">
                <div className="field-match-head">
                  <span>Source Asset Fields</span>
                  <span>Sclera Asset Fields</span>
                </div>
                {columns.map((c) => (
                  <div className="field-match-row" key={c.name}>
                    <div className="fm-source" title={c.name}>{c.name}</div>
                    <Icon name="arrow-right" size={16} />
                    <select value={mapping[c.name] ?? IGNORE} onChange={setMap(c.name)}>
                      <option value={IGNORE}>— Ignore —</option>
                      {fields.map((f) => <option key={f.key} value={f.key}>{f.label}</option>)}
                    </select>
                  </div>
                ))}
              </div>
            )
          ) : staged.length === 0 ? (
            <div className="empty-state"><Icon name="device" size={26} /><p>No assets staged from this file</p></div>
          ) : (
            <div className="import-asset-list">
              {visible.map((a) => {
                const excl = excluded.has(a.id)
                const name = a.display_name || a.id
                return (
                  <div className={`import-asset-row ${excl ? 'excluded' : ''}`} key={a.id}>
                    <div className="asset-avatar"><Icon name="device" size={18} /></div>
                    <div className="asset-main">
                      <div className="asset-name" title={name}>{name}</div>
                      <div className="asset-sub">
                        <span>{a.type || 'generic'}</span>
                        {a.vendor && <span>· {a.vendor}</span>}
                        {a.model && <span>· {a.model}</span>}
                      </div>
                    </div>
                    <div className="import-asset-meta">
                      <span className="muted">Serial</span>
                      <span>{a.serial_number || '—'}</span>
                    </div>
                    <button className="icon-btn danger" title={excl ? 'Include in import' : 'Exclude from import'} onClick={() => toggleExclude(a.id)}>
                      <Icon name={excl ? 'plus' : 'trash'} size={15} />
                    </button>
                  </div>
                )
              })}
            </div>
          )}
        </div>

        <div className="modal-foot">
          {step === 1 ? (
            <button className="btn btn-primary" onClick={doUpload} disabled={uploading || parsing || mappedCount === 0}>
              {uploading ? <Spinner size={14} /> : null} Next <Icon name="arrow-right" size={15} />
            </button>
          ) : (
            <>
              <button className="btn btn-ghost" onClick={() => setStep(1)} disabled={saving}>Back</button>
              <button className="btn btn-primary" onClick={doSave} disabled={saving || keptCount === 0}>
                {saving ? <Spinner size={14} /> : <Icon name="import" size={15} />} Import {keptCount} asset{keptCount === 1 ? '' : 's'}
              </button>
            </>
          )}
        </div>
      </div>
    </div>
  )
}
