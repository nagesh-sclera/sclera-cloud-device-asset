import { useCallback, useEffect, useMemo, useState } from 'react'
import Icon from '../components/Icon.jsx'
import { Skeleton, Spinner } from '../components/Skeleton.jsx'
import { useApp } from '../context/AppContext.jsx'
import { useToast } from '../context/ToastContext.jsx'
import api from '../services/api.js'

const bId = (b) => b.building_id || b.id
const fId = (f) => f.floor_id || f.id
const lId = (l) => l.location_id || l.id

// Full-page Locations Info (img_11): browse locations by building/floor and
// create buildings / floors / locations against the real APIs.
export default function LocationPage() {
  const { ctx, setView } = useApp()
  const toast = useToast()
  const [buildings, setBuildings] = useState([])
  const [floors, setFloors] = useState([])
  const [locations, setLocations] = useState(null)
  const [bldg, setBldg] = useState('all')
  const [floor, setFloor] = useState('all')
  const [search, setSearch] = useState('')
  const [nb, setNb] = useState('')
  const [nf, setNf] = useState('')
  const [nl, setNl] = useState('')
  const [busy, setBusy] = useState('')

  const loadBuildings = useCallback(() => api.getBuildings(ctx).then((b) => setBuildings(Array.isArray(b) ? b : [])).catch(() => setBuildings([])), [ctx])
  const loadFloors = useCallback((buildingId) => {
    if (!buildingId || buildingId === 'all') { setFloors([]); return Promise.resolve() }
    return api.getFloorsByBuilding(buildingId, ctx).then((f) => setFloors(Array.isArray(f) ? f : [])).catch(() => setFloors([]))
  }, [ctx])
  const loadLocations = useCallback(async () => {
    setLocations(null)
    try {
      let list
      if (floor !== 'all') {
        // a specific floor
        list = await api.getLocationsByFloor(floor, ctx)
      } else if (bldg !== 'all') {
        // a building, all floors → union of each floor's locations (no building leak)
        const fl = await api.getFloorsByBuilding(bldg, ctx)
        const perFloor = await Promise.all((Array.isArray(fl) ? fl : []).map((f) => api.getLocationsByFloor(fId(f), ctx).catch(() => [])))
        list = perFloor.flat()
      } else {
        // everything
        list = await api.getLocations(ctx)
      }
      setLocations(Array.isArray(list) ? list : [])
    } catch { setLocations([]) }
  }, [ctx, floor, bldg])

  useEffect(() => { loadBuildings() }, []) // eslint-disable-line react-hooks/exhaustive-deps
  useEffect(() => { loadFloors(bldg); setFloor('all') }, [bldg]) // eslint-disable-line react-hooks/exhaustive-deps
  useEffect(() => { loadLocations() }, [loadLocations])

  const addBuilding = async () => {
    if (!nb.trim()) return
    setBusy('b')
    try {
      const res = await api.addBuilding({ name: nb.trim() }, ctx)
      toast.success('Building created'); setNb('')
      await loadBuildings()
      const created = Array.isArray(res) ? res.find((x) => x.name === nb.trim()) : null
      if (created) setBldg(bId(created))
    } catch (e) { toast.error(`Create building failed: ${e.message}`) }
    finally { setBusy('') }
  }
  const addFloor = async () => {
    if (!nf.trim() || bldg === 'all') return
    setBusy('f')
    try {
      const res = await api.addFloor(bldg, nf.trim(), ctx)
      toast.success('Floor created'); setNf('')
      await loadFloors(bldg)
      const created = Array.isArray(res) ? res.find((x) => x.name === nf.trim()) : null
      if (created) setFloor(fId(created))
    } catch (e) { toast.error(`Create floor failed: ${e.message}`) }
    finally { setBusy('') }
  }
  const addLocation = async () => {
    if (!nl.trim() || floor === 'all') return
    setBusy('l')
    try {
      await api.addLocation(floor, nl.trim(), ctx)
      toast.success('Location created'); setNl('')
      await loadLocations()
    } catch (e) { toast.error(`Create location failed: ${e.message}`) }
    finally { setBusy('') }
  }

  const shown = useMemo(() => {
    const list = locations || []
    if (!search) return list
    const q = search.toLowerCase()
    return list.filter((l) => (l.name || '').toLowerCase().includes(q))
  }, [locations, search])

  return (
    <div className="page">
      <div className="page-head">
        <div className="page-title">
          <button className="icon-btn" onClick={() => setView('dashboard')} title="Back"><Icon name="portal" size={16} /></button>
          <h1>Locations Info</h1>
        </div>
        <button className="icon-btn" onClick={loadLocations} title="Refresh"><Icon name="refresh" size={16} /></button>
      </div>

      <div className="card loc-create-card">
        <div className="loc-create">
          <div className="loc-create-item">
            <label>New Building</label>
            <div className="loc-add">
              <input value={nb} onChange={(e) => setNb(e.target.value)} placeholder="Building name" />
              <button className="btn btn-primary sm" disabled={busy === 'b' || !nb.trim()} onClick={addBuilding}>{busy === 'b' ? <Spinner size={13} /> : <Icon name="plus" size={13} />} Add</button>
            </div>
          </div>
          <div className="loc-create-item">
            <label>New Floor {bldg === 'all' && <span className="muted">(select a building)</span>}</label>
            <div className="loc-add">
              <input value={nf} onChange={(e) => setNf(e.target.value)} placeholder="Floor name" disabled={bldg === 'all'} />
              <button className="btn btn-primary sm" disabled={busy === 'f' || !nf.trim() || bldg === 'all'} onClick={addFloor}>{busy === 'f' ? <Spinner size={13} /> : <Icon name="plus" size={13} />} Add</button>
            </div>
          </div>
          <div className="loc-create-item">
            <label>New Location {floor === 'all' && <span className="muted">(select a floor)</span>}</label>
            <div className="loc-add">
              <input value={nl} onChange={(e) => setNl(e.target.value)} placeholder="Location name" disabled={floor === 'all'} />
              <button className="btn btn-primary sm" disabled={busy === 'l' || !nl.trim() || floor === 'all'} onClick={addLocation}>{busy === 'l' ? <Spinner size={13} /> : <Icon name="plus" size={13} />} Add</button>
            </div>
          </div>
        </div>
      </div>

      <div className="loc-filters">
        <div className="filter-group"><label className="filter-label">Building</label>
          <select value={bldg} onChange={(e) => setBldg(e.target.value)}>
            <option value="all">All</option>
            {buildings.map((b) => <option key={bId(b)} value={bId(b)}>{b.name}</option>)}
          </select>
        </div>
        <div className="filter-group"><label className="filter-label">Floor</label>
          <select value={floor} onChange={(e) => setFloor(e.target.value)} disabled={bldg === 'all'}>
            <option value="all">All</option>
            {floors.map((f) => <option key={fId(f)} value={fId(f)}>{f.name}</option>)}
          </select>
        </div>
        <div className="header-search" style={{ flex: 1, minWidth: 220 }}>
          <Icon name="search" size={16} />
          <input value={search} onChange={(e) => setSearch(e.target.value)} placeholder="Search locations..." />
        </div>
      </div>

      <div className="loc-count">{shown.length} location(s) found</div>

      {locations == null ? (
        <div className="loc-grid">{Array.from({ length: 10 }).map((_, i) => <div className="loc-card" key={i}><Skeleton w={56} h={56} r={28} /><Skeleton w="60%" h={12} /></div>)}</div>
      ) : shown.length === 0 ? (
        <div className="empty-state"><Icon name="device" size={28} /><p>No locations</p><span className="muted">Create a building → floor → location above.</span></div>
      ) : (
        <div className="loc-grid">
          {shown.map((l) => (
            <div className="loc-card" key={lId(l)}>
              <div className="loc-pin"><Icon name="device" size={26} /></div>
              <div className="loc-name">{l.name || 'Unnamed'}</div>
              {(l.floor_name || l.building_name) && <div className="loc-sub">{[l.building_name, l.floor_name].filter(Boolean).join(' · ')}</div>}
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
