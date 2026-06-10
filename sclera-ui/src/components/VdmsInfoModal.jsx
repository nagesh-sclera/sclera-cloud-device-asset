import Icon from './Icon.jsx'
import { Skeleton } from './Skeleton.jsx'
import { useApi } from '../hooks/useApi.js'
import api from '../services/api.js'

const dash = (v) => (v === 0 ? '0' : v ? String(v) : '—')
function fmtTs(ts) {
  const n = Number(ts)
  if (!n) return '—'
  return new Date(n).toLocaleString()
}

// VDMS / property info sourced from the vdms-service (GET /vdms/details).
export default function VdmsInfoModal({ onClose }) {
  const { data, loading, error, refetch } = useApi(() => api.getVdmsDetails(), [])
  const d = data || {}

  const rows = [
    ['VDMS ID', dash(d.id)],
    ['Property Name', dash(d.property_name)],
    ['Activation Status', dash(d.activation_status)],
    ['Status', d.status === 1 ? 'Active' : dash(d.status)],
    ['Deployment Type', dash(d.deployment_type)],
    ['Region', dash(d.region)],
    ['Address', dash(d.address)],
    ['City', dash(d.city)],
    ['State', dash(d.state)],
    ['Country', dash(d.country)],
    ['Zip', dash(d.zip)],
    ['Timezone', dash(d.timezone)],
    ['Latitude', dash(d.latitude)],
    ['Longitude', dash(d.longitude)],
    ['Customer Org ID', dash(d.customer_org_id)],
    ['ADC Config ID', dash(d.adc_configuration_id)],
    ['Is Master', d.is_master === 1 ? 'Yes' : 'No'],
    ['Has Secondary Device', d.has_secondary_device === 1 ? 'Yes' : 'No'],
    ['Activated', fmtTs(d.activation_timestamp)],
  ]

  return (
    <div className="modal-overlay" onMouseDown={(e) => { if (e.target === e.currentTarget) onClose() }}>
      <div className="modal vdms-modal" role="dialog" aria-modal="true">
        <div className="modal-head">
          <div className="dp-avatar" style={{ background: 'var(--accent)', color: '#fff' }}><Icon name="info" size={16} /></div>
          <h2 style={{ marginLeft: 8 }}>VDMS Info</h2>
          <span className="muted" style={{ marginLeft: 8, fontSize: 12 }}>from vdms-service</span>
          <button className="icon-btn" onClick={() => refetch()} title="Refresh" style={{ marginLeft: 'auto' }}><Icon name="refresh" size={15} /></button>
          <button className="icon-btn" onClick={onClose} aria-label="Close"><Icon name="x" /></button>
        </div>

        <div className="modal-body">
          {loading ? (
            <div className="dp-fields">{Array.from({ length: 12 }).map((_, i) => (
              <div className="dp-row" key={i}><Skeleton w="40%" h={11} /><Skeleton w="45%" h={11} /></div>
            ))}</div>
          ) : error ? (
            <div className="empty-state"><Icon name="info" size={24} /><p>Couldn't load VDMS info</p><span className="muted">{error.message}</span>
              <button className="btn btn-primary sm" onClick={() => refetch()}>Retry</button></div>
          ) : (
            <div className="dp-fields">
              {rows.map(([k, v]) => (
                <div className="dp-row" key={k}><span className="dp-k">{k}</span><span className="dp-v">{v}</span></div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
