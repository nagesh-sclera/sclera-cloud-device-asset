import { useMemo } from 'react'
import Icon from '../components/Icon.jsx'
import { Skeleton } from '../components/Skeleton.jsx'
import { useApi } from '../hooks/useApi.js'
import { useApp } from '../context/AppContext.jsx'
import { DEMO } from '../config.js'
import api from '../services/api.js'

// Derives a property card from /vdms/details (+ /vdms/id fallback).
function useProperties() {
  return useApi(async () => {
    const [details, idRes] = await Promise.allSettled([api.getVdmsDetails(), api.getVdmsId()])
    const d = details.status === 'fulfilled' ? details.value : null
    const vid = (d && (d.id || d.vdms_id)) || (idRes.status === 'fulfilled' && (idRes.value?.vdmsId || idRes.value?.vdms_id)) || DEMO.vdmsId
    const card = {
      id: vid,
      name: d?.property_name || d?.name || 'Sclera HQ — Demo Building',
      status: (d?.activation_status || '').toUpperCase() === 'ACTIVE' || d?.status === 1 ? 'active' : 'inactive',
      networkCount: d?.networkCount ?? 3,
      city: d?.city, state: d?.state,
    }
    return [card]
  }, [])
}

export default function PropertyListPage({ search }) {
  const { setProperty, setView } = useApp()
  const { data, loading, error, refetch } = useProperties()

  const properties = useMemo(() => {
    const list = data || []
    if (!search) return list
    const q = search.toLowerCase()
    return list.filter((p) => p.id.toLowerCase().includes(q) || p.name.toLowerCase().includes(q))
  }, [data, search])

  const open = (p) => { setProperty(p); setView('dashboard') }

  return (
    <div className="page">
      <div className="page-head">
        <div className="page-title">
          <h1>Property List</h1>
          <button className="icon-btn" onClick={() => refetch()} title="Refresh"><Icon name="refresh" size={16} /></button>
          <button className="btn btn-primary sm"><Icon name="plus" size={15} /> Add VDMS</button>
        </div>
        <div className="view-toggle">
          <button className="icon-btn active"><Icon name="grid" size={16} /></button>
          <button className="icon-btn"><Icon name="list" size={16} /></button>
        </div>
      </div>

      {loading && (
        <div className="property-grid">
          {Array.from({ length: 1 }).map((_, i) => (
            <div className="card property-card" key={i}><Skeleton w="40%" /><div style={{ height: 16 }} /><Skeleton w="80%" h={20} /></div>
          ))}
        </div>
      )}

      {error && !loading && (
        <div className="empty-state">
          <Icon name="info" size={28} />
          <p>Couldn't reach the gateway.</p>
          <span className="muted">{error.message}</span>
          <button className="btn btn-primary sm" onClick={() => refetch()}>Retry</button>
        </div>
      )}

      {!loading && !error && (
        properties.length === 0 ? (
          <div className="empty-state"><Icon name="grid" size={28} /><p>No properties found</p></div>
        ) : (
          <div className="property-grid">
            {properties.map((p) => (
              <div className="card property-card" key={p.id} onClick={() => open(p)} role="button">
                <div className="property-top">
                  <span className="property-id">{p.id}</span>
                  <Icon name="info" size={14} className="muted" />
                  <span className={`dot ${p.status === 'active' ? 'online' : 'offline'} push-right`} />
                </div>
                <div className="property-main">
                  <div className="property-avatar"><Icon name="device" size={22} /></div>
                  <div className="property-name">{p.name}</div>
                </div>
                <div className="property-foot">
                  <span className="net"><Icon name="network" size={14} /> Networks <b>{p.networkCount}</b></span>
                  <span className="proxy-link">Proxy</span>
                </div>
              </div>
            ))}
          </div>
        )
      )}
    </div>
  )
}
