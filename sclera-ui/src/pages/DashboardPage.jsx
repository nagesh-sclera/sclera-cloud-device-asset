import { useState } from 'react'
import MetricCard from '../components/MetricCard.jsx'
import FeatureTile from '../components/FeatureTile.jsx'
import VdmsInfoModal from '../components/VdmsInfoModal.jsx'
import { CardSkeleton } from '../components/Skeleton.jsx'
import { useApi } from '../hooks/useApi.js'
import { useApp } from '../context/AppContext.jsx'
import api from '../services/api.js'

const n = (m, k) => (m && (m[k] ?? m[k?.toLowerCase?.()])) || 0
// Backend getdevicecount keys, e.g. all_device_count, online_device_count.
const dc = (m, k) => n(m, `${k}_device_count`)

const FEATURES = [
  { label: 'VDMS Info', icon: 'info' }, { label: 'Task Dashboard', icon: 'list' },
  { label: 'Location', icon: 'device' }, { label: 'User Activity', icon: 'users' },
  { label: 'Networks', icon: 'network' },
  { label: 'ITAM Inventory', icon: 'layers' }, { label: 'Inventory', icon: 'box' },
  { label: 'Asset Data Collection AI', icon: 'database' }, { label: 'Asset Onboarding AI', icon: 'device' },
  { label: 'Asset Onboarding', icon: 'upload' }, { label: 'Skill Profile', icon: 'users' },
  { label: 'Resources', icon: 'grid' }, { label: 'Phonebook', icon: 'portal' },
]

export default function DashboardPage() {
  const { property, setView, ctx } = useApp()
  // count across all networks (matches the Managed Assets list default)
  const { data: counts, loading } = useApi(() => api.deviceCount({ ...ctx, docker: 'all' }), [])
  const [showVdms, setShowVdms] = useState(false)

  const m = counts || {}
  const assets = [
    { label: 'All', value: dc(m, 'all'), tone: 'default' },
    { label: 'Unmonitored', value: dc(m, 'unmonitor'), tone: 'muted' },
    { label: 'Online', value: dc(m, 'online'), tone: 'online' },
    { label: 'Offline', value: dc(m, 'offline'), tone: 'offline' },
  ]

  return (
    <div className="page">
      <div className="page-head">
        <div className="page-title"><h1>{property?.name || 'Property Dashboard'}</h1></div>
      </div>

      {loading ? <CardSkeleton count={5} /> : (
        <div className="metric-grid">
          <MetricCard title="Managed Assets" accent="blue" items={assets} onClick={() => setView('assets')} />
          <MetricCard title="Managed Softwares" accent="green" items={[
            { label: 'All', value: n(m, 'software_all') }, { label: 'Expired', value: n(m, 'expired'), tone: 'offline' },
            { label: 'Monthly', value: n(m, 'monthly') }, { label: 'Yearly', value: n(m, 'yearly') },
          ]} />
          <MetricCard title="Work Orders" accent="amber" items={[
            { label: 'New', value: n(m, 'wo_new') }, { label: 'Open', value: n(m, 'wo_open') },
            { label: 'Flagged', value: n(m, 'flagged'), tone: 'offline' }, { label: 'On Hold', value: n(m, 'wo_hold') },
          ]} />
          <MetricCard title="Tickets" accent="blue" items={[
            { label: 'All', value: n(m, 'ticket_all') }, { label: 'Open', value: n(m, 'ticket_open'), tone: 'online' },
            { label: 'On hold', value: n(m, 'ticket_hold') }, { label: 'Closed', value: n(m, 'ticket_closed') },
          ]} />
          <MetricCard title="Tasks" accent="green" items={[
            { label: 'All', value: n(m, 'task_all') }, { label: 'Active', value: n(m, 'active'), tone: 'online' },
            { label: 'Assigned', value: n(m, 'assigned') }, { label: 'Complete', value: n(m, 'complete') },
          ]} />
        </div>
      )}

      <h2 className="section-title">VDMS</h2>
      <div className="feature-grid">
        {FEATURES.map((f) => (
          <FeatureTile key={f.label} label={f.label} icon={f.icon}
            onClick={() => {
              if (f.label === 'VDMS Info') setShowVdms(true)
              else if (f.label === 'Location') setView('location')
              else if (f.label === 'User Activity') setView('activity')
              else if (f.label === 'Networks') setView('networks')
              else if (f.label.startsWith('ITAM') || f.label === 'Asset Onboarding') setView('assets')
            }} />
        ))}
      </div>

      {showVdms && <VdmsInfoModal onClose={() => setShowVdms(false)} />}
    </div>
  )
}
