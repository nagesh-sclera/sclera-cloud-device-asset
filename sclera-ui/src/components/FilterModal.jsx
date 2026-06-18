import { useEffect, useMemo, useState } from 'react'
import Icon from './Icon.jsx'
import { useApp } from '../context/AppContext.jsx'
import api from '../services/api.js'

const SORT_OPTIONS = [
  { key: 'none', label: 'None' },
  { key: 'name_asc', label: 'Name (A → Z)', column: 'display_name', order: true },
  { key: 'name_desc', label: 'Name (Z → A)', column: 'display_name', order: false },
  { key: 'created_desc', label: 'Newest first', column: 'created_timestamp', order: false },
  { key: 'cost_desc', label: 'Cost (high → low)', column: 'cost_value', order: false },
]

const FEATURES = ['QR Code', 'NFC', 'Asset Images', 'Sensor Alerts', 'Bar Code', 'Documents', 'Sensor', 'Procedures']
const ONBOARD_DETAILS = ['Image Status', 'Tag Status', 'Field Status', 'Geolocation Status']
const FIELDS = ['ID', 'Display Name', 'Manufacturer', 'Model', 'IP Address', 'Mac Address', 'Location', 'Floor', 'Building', 'Latitude', 'Longitude', 'Serial Number', 'Warranty', 'Created Timestamp', 'Updated Email', 'Updated Timestamp', 'Description', 'Assigned User Email', 'Cost Value', 'Pc Location', 'Isp Location', 'Username']

export default function FilterModal({ initial, onClose, onApply }) {
  const { ctx } = useApp()
  const [searchBy, setSearchBy] = useState(initial?.searchBy || 'All')
  const [sortBy, setSortBy] = useState(initial?.sortBy || 'none')
  const [assignee, setAssignee] = useState(initial?.assignee || '')
  const [alertMessage, setAlertMessage] = useState(initial?.alertMessage || '')
  const [onboardState, setOnboardState] = useState(initial?.onboard || 'any') // any | onboarded | notonboarded
  const [assetTypes, setAssetTypes] = useState(new Set(initial?.assetTypes || []))
  const [assetGroups, setAssetGroups] = useState(new Set(initial?.assetGroups || []))
  const [category, setCategory] = useState(initial?.category || '')
  const [typesContains, setTypesContains] = useState(initial?.typesContains ?? true)
  const [features, setFeatures] = useState(new Set(initial?.features || []))
  const [onboardDetails, setOnboardDetails] = useState(new Set(initial?.onboardDetails || []))
  const [fields, setFields] = useState(new Set())
  const [osType, setOsType] = useState(initial?.osType || '')
  const [source, setSource] = useState(initial?.source || false)
  const [network, setNetwork] = useState(initial?.network || 'all')
  const [typeSearch, setTypeSearch] = useState('')

  // option sources
  const [opts, setOpts] = useState({ types: [], groups: [], categories: [], assignees: [], alerts: [], networks: ['all'] })

  useEffect(() => {
    let alive = true
    // Filter option calls (img_10): the five real lookups + a device fetch used only
    // as a fallback if a lookup returns nothing.
    Promise.allSettled([
      api.alertMessages(ctx),
      api.assignedEmails({ network: ctx.docker, vdmsId: ctx.vdmsId }),
      api.uniqueDeviceTypes({ vdmsId: ctx.vdmsId, network: 'all', user: ctx.user }),
      api.uniqueAssetGroups({ vdmsId: ctx.vdmsId, network: 'all', user: ctx.user }),
      api.uniqueCategory({ vdmsId: ctx.vdmsId, network: 'all', user: ctx.user }),
      api.listParentDevices({ ...ctx, condition: 'all', pageno: 1, pagesize: 200 }),
      api.listNetworks({ vdmsId: ctx.vdmsId }),
    ]).then(([alertsR, assigneesR, typesR, groupsR, catsR, devicesR, netsR]) => {
      if (!alive) return
      const arr = (r) => (r.status === 'fulfilled' && Array.isArray(r.value) ? r.value.filter(Boolean) : [])
      const devices = arr(devicesR)
      const uniq = (sel) => [...new Set(devices.map(sel).filter(Boolean))].sort()
      const alerts = (alertsR.status === 'fulfilled' && Array.isArray(alertsR.value)
        ? alertsR.value.map((a) => a.message || a.name || a) : []).filter(Boolean)
      const orFallback = (real, sel) => (real.length ? real : uniq(sel))
      const netNames = arr(netsR).map((n) => n.name).filter(Boolean)
      setOpts({
        types: orFallback(arr(typesR), (d) => d.type),
        groups: orFallback(arr(groupsR), (d) => d.asset_group),
        categories: orFallback(arr(catsR), (d) => d.category),
        assignees: arr(assigneesR),
        alerts,
        networks: ['all', ...(netNames.length ? netNames : uniq((d) => d.docker_name))],
      })
    })
    return () => { alive = false }
  }, []) // eslint-disable-line react-hooks/exhaustive-deps

  const toggle = (setter) => (val) => setter((s) => { const n = new Set(s); n.has(val) ? n.delete(val) : n.add(val); return n })

  const filteredTypes = useMemo(
    () => opts.types.filter((t) => t.toLowerCase().includes(typeSearch.toLowerCase())),
    [opts.types, typeSearch]
  )

  const activeCount =
    assetTypes.size + assetGroups.size + (category ? 1 : 0) + (assignee ? 1 : 0) +
    (sortBy !== 'none' ? 1 : 0) + (onboardState !== 'any' ? 1 : 0) + features.size

  const buildAndApply = () => {
    const column_details = []
    if (assetTypes.size) column_details.push({ custom: false, condition: typesContains ? 'is_present' : 'is_not_present', column: 'type', value: [...assetTypes] })
    if (assetGroups.size) column_details.push({ custom: false, condition: 'is_present', column: 'asset_group', value: [...assetGroups] })
    // backend expects category value as { categoryName: [subcategories...] }
    if (category) column_details.push({ custom: false, condition: 'is_present', column: 'category', value: { [category]: [] } })
    if (assignee) column_details.push({ custom: false, condition: 'is_present', column: 'assignee_email', value: assignee })

    const criteria = { filter_details: { column_details } }
    const sort = SORT_OPTIONS.find((s) => s.key === sortBy)
    if (sort && sort.column) criteria.sort_details = { column: sort.column, custom: false, order: sort.order }

    const onboard_status = onboardState === 'onboarded' ? 3 : onboardState === 'notonboarded' ? 210 : 123

    onApply({
      criteria, onboard_status,
      // echo selections so we can re-open with state + show chips + client-refine
      meta: {
        searchBy, sortBy, assignee, alertMessage, onboard: onboardState, network,
        assetTypes: [...assetTypes], assetGroups: [...assetGroups], category, typesContains,
        features: [...features], onboardDetails: [...onboardDetails], osType, source,
      },
    })
  }

  const clearAll = () => {
    setAssetTypes(new Set()); setAssetGroups(new Set()); setCategory(''); setAssignee('')
    setSortBy('none'); setOnboardState('any'); setFeatures(new Set()); setAlertMessage('')
    setOnboardDetails(new Set()); setFields(new Set()); setOsType(''); setSource(false); setNetwork('all')
  }

  return (
    <div className="modal-overlay" onMouseDown={(e) => { if (e.target === e.currentTarget) onClose() }}>
      <div className="modal filter-modal" role="dialog" aria-modal="true">
        <div className="modal-head">
          <h2>Filter</h2>
          {activeCount > 0 && <span className="filter-active-badge">{activeCount} active</span>}
          <button className="icon-btn" onClick={onClose} aria-label="Close" style={{ marginLeft: 'auto' }}><Icon name="x" /></button>
        </div>

        <div className="modal-body filter-body">
          <div className="filter-grid2">
            <Group label="Network Name (Gateway)">
              <select value={network} onChange={(e) => setNetwork(e.target.value)}>
                {opts.networks.map((n) => <option key={n} value={n}>{n === 'all' ? 'All' : n}</option>)}
              </select>
            </Group>
            <Group label="Search By">
              <select value={searchBy} onChange={(e) => setSearchBy(e.target.value)}>
                {['All', 'Display Name', 'Manufacturer', 'Model', 'Serial Number', 'IP Address'].map((o) => <option key={o}>{o}</option>)}
              </select>
            </Group>
            <Group label="Sort By">
              <select value={sortBy} onChange={(e) => setSortBy(e.target.value)}>
                {SORT_OPTIONS.map((s) => <option key={s.key} value={s.key}>{s.label}</option>)}
              </select>
            </Group>
            <Group label="Alert Message">
              <select value={alertMessage} onChange={(e) => setAlertMessage(e.target.value)}>
                <option value="">Select...</option>
                {opts.alerts.map((a, i) => <option key={i} value={a}>{a}</option>)}
              </select>
            </Group>
            <Group label="Assignee">
              <select value={assignee} onChange={(e) => setAssignee(e.target.value)}>
                <option value="">Select assignee</option>
                {opts.assignees.map((a, i) => <option key={i} value={a}>{a}</option>)}
              </select>
            </Group>
          </div>

          <Section title="Onboarded Details">
            <div className="chk-grid">
              {ONBOARD_DETAILS.map((o) => (
                <label key={o} className="chk"><input type="checkbox" checked={onboardDetails.has(o)} onChange={() => toggle(setOnboardDetails)(o)} /> {o}</label>
              ))}
            </div>
            <div className="radio-row" style={{ marginTop: 10 }}>
              {[['any', 'Any'], ['onboarded', 'Onboarded'], ['notonboarded', 'Not Onboarded']].map(([v, l]) => (
                <label key={v} className="radio"><input type="radio" name="onb" checked={onboardState === v} onChange={() => setOnboardState(v)} /> {l}</label>
              ))}
            </div>
          </Section>

          <Section title="Specification Filter">
            <Group label="OS type">
              <select value={osType} onChange={(e) => setOsType(e.target.value)}>
                <option value="">-Select-</option>
                {['Windows', 'Linux', 'macOS', 'Android', 'iOS', 'Embedded'].map((o) => <option key={o}>{o}</option>)}
              </select>
            </Group>
          </Section>

          <Section title="Features">
            <div className="chk-grid">
              {FEATURES.map((f) => (
                <label key={f} className="chk"><input type="checkbox" checked={features.has(f)} onChange={() => toggle(setFeatures)(f)} /> {f}</label>
              ))}
            </div>
          </Section>

          <Section title="Fields">
            <div className="chk-grid five">
              {FIELDS.map((f) => (
                <label key={f} className="chk"><input type="checkbox" checked={fields.has(f)} onChange={() => toggle(setFields)(f)} /> {f}</label>
              ))}
            </div>
          </Section>

          <Section title="Asset Types">
            <div className="radio-row">
              <label className="radio"><input type="radio" name="atc" checked={typesContains} onChange={() => setTypesContains(true)} /> Contains</label>
              <label className="radio"><input type="radio" name="atc" checked={!typesContains} onChange={() => setTypesContains(false)} /> Not Contains</label>
            </div>
            <div className="header-search sm" style={{ margin: '10px 0' }}>
              <Icon name="search" size={14} /><input value={typeSearch} onChange={(e) => setTypeSearch(e.target.value)} placeholder="Search" />
            </div>
            <div className="chk-grid">
              {filteredTypes.length === 0 ? <span className="muted">No types found</span> :
                filteredTypes.map((t) => (
                  <label key={t} className="chk"><input type="checkbox" checked={assetTypes.has(t)} onChange={() => toggle(setAssetTypes)(t)} /> {t}</label>
                ))}
            </div>
          </Section>

          <Section title="Asset Group">
            <div className="chk-grid">
              {opts.groups.length === 0 ? <span className="muted">No groups</span> :
                opts.groups.map((g) => (
                  <label key={g} className="chk"><input type="checkbox" checked={assetGroups.has(g)} onChange={() => toggle(setAssetGroups)(g)} /> {g}</label>
                ))}
            </div>
          </Section>

          <Section title="Categories">
            <Group label="Select Category">
              <select value={category} onChange={(e) => setCategory(e.target.value)}>
                <option value="">Select...</option>
                {opts.categories.map((c) => <option key={c} value={c}>{c}</option>)}
              </select>
            </Group>
          </Section>

          <Section title="Source">
            <label className="chk"><input type="checkbox" checked={source} onChange={(e) => setSource(e.target.checked)} /> Asset data collection</label>
          </Section>
        </div>

        <div className="modal-foot filter-foot">
          <button className="btn btn-ghost" onClick={clearAll}>Clear</button>
          <button className="btn btn-primary" onClick={buildAndApply}>Apply</button>
        </div>
      </div>
    </div>
  )
}

function Group({ label, children }) {
  return <div className="filter-group"><label className="filter-label">{label}</label>{children}</div>
}
function Section({ title, children }) {
  return (
    <div className="filter-section">
      <div className="filter-section-head"><span>{title}</span><Icon name="more" size={14} /></div>
      {children}
    </div>
  )
}
