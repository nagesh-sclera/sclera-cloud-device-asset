import { useCallback, useEffect, useRef, useState } from 'react'
import Icon from '../components/Icon.jsx'
import { RowSkeleton, Spinner } from '../components/Skeleton.jsx'
import ContextMenu from '../components/ContextMenu.jsx'
import AssetModal from '../components/AssetModal.jsx'
import DeviceDetailPanel from '../components/DeviceDetailPanel.jsx'
import LogsDrawer from '../components/LogsDrawer.jsx'
import FilterModal from '../components/FilterModal.jsx'
import { useDebounce } from '../hooks/useApi.js'
import { useApp } from '../context/AppContext.jsx'
import { useToast } from '../context/ToastContext.jsx'
import { statusInfo } from '../config.js'
import api from '../services/api.js'

// Client-side refinement for facets the backend filter API doesn't cover
// (features / onboarded-detail flags / source) — applied on top of the real
// searchsortfilterdevices result.
function refineRows(rows, meta) {
  if (!meta) return rows
  let out = rows
  const FEAT = {
    'Asset Images': (d) => Boolean(d.asset_image_url),
    Documents: (d) => (d.document_count || 0) > 0,
    Sensor: (d) => (d.snmp_count || 0) + (d.bacnet_count || 0) + (d.lorawan_count || 0) + (d.modbus_count || 0) + (d.monnit_count || 0) > 0,
    'Sensor Alerts': (d) => Boolean(d.alarm) || d.email_alert === 1,
    'QR Code': (d) => (d.qrcode_count || 0) > 0,
    'Bar Code': (d) => (d.barcode_count || 0) > 0,
    NFC: (d) => (d.nfc_count || 0) > 0,
    Procedures: (d) => (d.checklist_template_count || 0) > 0,
  }
  ;(meta.features || []).forEach((f) => { if (FEAT[f]) out = out.filter(FEAT[f]) })
  const OB = { 'Image Status': 'image_status', 'Tag Status': 'tag_status', 'Field Status': 'field_status', 'Geolocation Status': 'geolocation_status' }
  ;(meta.onboardDetails || []).forEach((o) => { const k = OB[o]; if (k) out = out.filter((d) => d[k] === 1) })
  if (meta.source) out = out.filter((d) => (d.source_type || '').toLowerCase().includes('adc') || (d.source_type || '').toLowerCase().includes('collection'))
  return out
}

// keys map to real backend `condition` values
const TABS = [
  { key: 'all', label: 'All', countKey: 'all_device_count' },
  { key: 'unmonitored', label: 'Unmonitored', countKey: 'unmonitor_device_count' },
  { key: 'online', label: 'Online', countKey: 'online_device_count' },
  { key: 'offline', label: 'Offline', countKey: 'offline_device_count' },
]
const PAGE_SIZE = 12

export default function AssetPage({ search, onSearch }) {
  const { ctx } = useApp()
  const toast = useToast()
  const [filter, setFilter] = useState('all')
  const [page, setPage] = useState(1)
  const [rows, setRows] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [ctxMenu, setCtxMenu] = useState(null)
  const [rowMenu, setRowMenu] = useState(null)
  const [modal, setModal] = useState({ open: false, editing: null })
  const [busyId, setBusyId] = useState(null)
  const [detailId, setDetailId] = useState(null)
  const [showLogs, setShowLogs] = useState(false)
  const [showFilter, setShowFilter] = useState(false)
  const [adv, setAdv] = useState(null) // applied advanced filter {criteria, onboard_status, meta}
  const [filterCount, setFilterCount] = useState(null)
  const [network, setNetwork] = useState('all') // selected gateway/network (docker)
  const [networks, setNetworks] = useState(['all'])
  const [counts, setCounts] = useState({})
  const debounced = useDebounce(search, 400)
  const reqId = useRef(0)

  // Tab counts (scoped to the selected network).
  const loadCounts = useCallback(() => {
    api.deviceCount({ ...ctx, docker: network })
      .then((c) => setCounts(c && typeof c === 'object' ? c : {}))
      .catch(() => setCounts({}))
  }, [ctx, network])

  // Populate the Network Name (gateway) dropdown from the real networks endpoint.
  const loadNetworks = useCallback(() => {
    api.listNetworks(ctx)
      .then((list) => setNetworks(['all', ...(Array.isArray(list) ? list : []).map((n) => n.name).filter(Boolean)]))
      .catch(() => {})
  }, [ctx])
  useEffect(() => { loadNetworks() }, [loadNetworks])

  const handleAddNetwork = async () => {
    const name = window.prompt('New network name (e.g. left_wing):')
    if (!name || !name.trim()) return
    try {
      await api.addNetwork({ name: name.trim(), gateway: '' }, ctx)
      toast.success(`Network "${name.trim()}" added`)
      loadNetworks()
      setNetwork(name.trim())
    } catch (e) { toast.error(`Add network failed: ${e.message}`) }
  }

  const load = useCallback(async () => {
    const id = ++reqId.current
    setLoading(true); setError(null)
    loadCounts() // refresh tab counts alongside the list
    try {
      // filter-modal network takes precedence over the header selector when set
      const activeNetwork = (adv?.meta?.network && adv.meta.network !== 'all') ? adv.meta.network : network
      const scope = { ...ctx, docker: activeNetwork } // 'all' scopes across gateways
      let res
      if (adv) {
        // Advanced filter → the real searchsortfilterdevices (+ count). img_9
        res = await api.searchSortFilter(adv.criteria, { ...scope, condition: filter, onboard_status: adv.onboard_status, pageno: page, pagesize: 50 })
        // fire the real count call too (img_9 fidelity), then refine client-side
        api.searchSortFilterCount(adv.criteria, { ...scope, condition: filter, onboard_status: adv.onboard_status }).catch(() => {})
        res = refineRows(Array.isArray(res) ? res : [], adv.meta)
        if (id === reqId.current) setFilterCount(res.length)
      } else if (debounced) {
        res = await api.listDevices({ ...scope, condition: filter, searchKey: debounced, pageno: page, pagesize: PAGE_SIZE })
      } else {
        res = await api.listParentDevices({ ...scope, condition: filter, pageno: page, pagesize: PAGE_SIZE })
      }
      if (id !== reqId.current) return
      setRows(Array.isArray(res) ? res : [])
    } catch (e) {
      if (id !== reqId.current) return
      setError(e); setRows([])
    } finally {
      if (id === reqId.current) setLoading(false)
    }
  }, [ctx, filter, debounced, page, adv, network, loadCounts])

  useEffect(() => { load() }, [load])
  useEffect(() => { setPage(1) }, [filter, debounced, adv, network])

  const remove = async (device) => {
    if (!confirm(`Delete asset "${device.user_data_name || device.display_name || device.id}"?`)) return
    setBusyId(device.id)
    try {
      await api.deleteDevices([device.id], ctx)
      toast.success('Asset deleted')
      load()
    } catch (e) { toast.error(`Delete failed: ${e.message}`) }
    finally { setBusyId(null) }
  }

  const pageMenu = [
    { label: 'Settings', icon: 'settings' },
    { label: 'Add Asset', icon: 'plus', onClick: () => setModal({ open: true, editing: null }) },
    { label: 'Multi Update', icon: 'sliders' },
    { divider: true },
    { label: 'Import', icon: 'import' },
    { label: 'Export', icon: 'upload' },
  ]

  return (
    <div className="page">
      <div className="page-head">
        <div className="network-select">
          <label>Network Name</label>
          <div className="network-row">
            <select value={network} onChange={(e) => setNetwork(e.target.value)}>
              {networks.map((n) => <option key={n} value={n}>{n === 'all' ? 'All' : n}</option>)}
            </select>
            <button className="icon-btn" onClick={handleAddNetwork} title="Add network"><Icon name="plus" size={15} /></button>
          </div>
        </div>
        <div className="filter-tabs">
          {TABS.map((t) => {
            const c = counts[t.countKey]
            return (
              <button key={t.key} className={`tab ${filter === t.key ? 'active' : ''}`} onClick={() => setFilter(t.key)}>
                {t.label}{c != null && <span className="tab-count">{c}</span>}
              </button>
            )
          })}
        </div>
        <div className="asset-actions">
          <button className="btn btn-primary sm" onClick={() => setModal({ open: true, editing: null })}><Icon name="plus" size={15} /> Add Asset</button>
          <button className="btn btn-ghost sm" onClick={() => setShowLogs(true)} title="Activity logs"><Icon name="list" size={15} /> Logs</button>
          <button className="icon-btn" onClick={() => load()} title="Refresh"><Icon name="refresh" size={16} /></button>
          <button className={`icon-btn ${adv ? 'active' : ''}`} onClick={() => setShowFilter(true)} title="Filter"><Icon name="filter" size={16} /></button>
          <button className="icon-btn" onClick={(e) => setCtxMenu({ x: e.clientX, y: e.clientY })} title="More"><Icon name="more" size={16} /></button>
        </div>
      </div>

      {adv && (
        <div className="filter-chipbar">
          <Icon name="filter" size={14} />
          <span>Filtered{filterCount != null ? ` · ${filterCount} match${filterCount === 1 ? '' : 'es'}` : ''}</span>
          {adv.meta?.assetTypes?.map((t) => <span className="chip" key={`t${t}`}>type: {t}</span>)}
          {adv.meta?.assetGroups?.map((g) => <span className="chip" key={`g${g}`}>group: {g}</span>)}
          {adv.meta?.category && <span className="chip">category: {adv.meta.category}</span>}
          {adv.meta?.assignee && <span className="chip">assignee: {adv.meta.assignee}</span>}
          {adv.meta?.sortBy && adv.meta.sortBy !== 'none' && <span className="chip">sorted</span>}
          {adv.meta?.onboard && adv.meta.onboard !== 'any' && <span className="chip">{adv.meta.onboard}</span>}
          <button className="chip-clear" onClick={() => { setAdv(null); setFilterCount(null) }}>Clear ✕</button>
        </div>
      )}

      {loading ? <RowSkeleton count={6} /> : error ? (
        <div className="empty-state">
          <Icon name="info" size={28} /><p>Couldn't load assets.</p>
          <span className="muted">{error.message}</span>
          <button className="btn btn-primary sm" onClick={() => load()}>Retry</button>
        </div>
      ) : rows.length === 0 ? (
        <div className="empty-state">
          <Icon name="device" size={28} /><p>No assets yet</p>
          <span className="muted">Create one to see it persist through the real gateway.</span>
          <button className="btn btn-primary sm" onClick={() => setModal({ open: true, editing: null })}><Icon name="plus" size={15} /> Add Asset</button>
        </div>
      ) : (
        <div className="asset-list">
          {rows.map((d) => {
            const st = statusInfo(d)
            const name = d.user_data_name || d.display_name || d.name || d.id
            return (
              <div className={`asset-row clickable ${detailId === d.id ? 'selected' : ''}`} key={d.id} onClick={() => setDetailId(d.id)}>
                <div className="asset-avatar"><Icon name="device" size={20} /></div>
                <div className="asset-main">
                  <div className="asset-name" title={name}>{name}</div>
                  <div className="asset-sub">
                    <span>{d.type || 'Generic'}</span>
                    {d.user_data_vendor || d.vendor ? <span>· {d.user_data_vendor || d.vendor}</span> : null}
                    {d.building ? <span>· {d.building}</span> : null}
                  </div>
                </div>
                <div className="asset-badges">
                  <span className={`badge tone-${st.tone}`}><span className={`dot ${st.tone}`} /> {st.label}</span>
                  {d.onboard_status === 3 ? <span className="badge tone-online">Onboarded</span> : null}
                </div>
                <div className="asset-row-actions" onClick={(e) => e.stopPropagation()}>
                  <button className="icon-btn" title="Edit" onClick={() => setModal({ open: true, editing: d })}><Icon name="edit" size={15} /></button>
                  <button className="icon-btn danger" title="Delete" disabled={busyId === d.id} onClick={() => remove(d)}>
                    {busyId === d.id ? <Spinner size={14} /> : <Icon name="trash" size={15} />}
                  </button>
                  <button className="icon-btn" title="More" onClick={(e) => { e.stopPropagation(); setRowMenu({ pos: { x: e.clientX, y: e.clientY }, device: d }) }}><Icon name="more" size={15} /></button>
                </div>
              </div>
            )
          })}
        </div>
      )}

      {!loading && !error && (
        <div className="pagination">
          <button className="icon-btn" disabled={page <= 1} onClick={() => setPage((p) => Math.max(1, p - 1))}>‹ Prev</button>
          <span className="page-ind">Page {page}</span>
          <button className="icon-btn" disabled={rows.length < PAGE_SIZE} onClick={() => setPage((p) => p + 1)}>Next ›</button>
        </div>
      )}

      {ctxMenu && <ContextMenu pos={ctxMenu} items={pageMenu} onClose={() => setCtxMenu(null)} />}
      {rowMenu && (
        <ContextMenu
          pos={rowMenu.pos}
          items={[
            { label: 'Edit', icon: 'edit', onClick: () => setModal({ open: true, editing: rowMenu.device }) },
            { label: 'Delete', icon: 'trash', danger: true, onClick: () => remove(rowMenu.device) },
          ]}
          onClose={() => setRowMenu(null)}
        />
      )}

      <AssetModal
        open={modal.open}
        editing={modal.editing}
        onClose={() => setModal({ open: false, editing: null })}
        onSaved={() => load()}
      />

      {detailId && (
        <DeviceDetailPanel
          deviceId={detailId}
          onClose={() => setDetailId(null)}
          onEdit={(d) => { setDetailId(null); setModal({ open: true, editing: d }) }}
          onChanged={() => load()}
        />
      )}

      {showLogs && <LogsDrawer onClose={() => setShowLogs(false)} />}

      {showFilter && (
        <FilterModal
          initial={adv?.meta}
          onClose={() => setShowFilter(false)}
          onApply={(f) => { setAdv(f); setShowFilter(false) }}
        />
      )}
    </div>
  )
}
