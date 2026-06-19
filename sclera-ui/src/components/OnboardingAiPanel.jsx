import { useEffect, useState } from 'react'
import Icon from './Icon.jsx'
import { Skeleton, Spinner } from './Skeleton.jsx'
import { useApp } from '../context/AppContext.jsx'
import { useToast } from '../context/ToastContext.jsx'
import api from '../services/api.js'

// Onboarding sub-steps tracked on DeviceOnboardStatus (1 = collected/done).
const STEPS = [
  { key: 'image_status', label: 'Image' },
  { key: 'geolocation_status', label: 'Geolocation' },
  { key: 'tag_status', label: 'Tag' },
  { key: 'field_status', label: 'Field Data' },
]

/**
 * Right-side "Data Collection AI" panel for an onboarding asset. Loads the device, offers a
 * (demo) "Collect with AI" action that marks the onboarding steps complete via the real
 * updateassetonboarddata write, and a "Move to Managed" action (onboard status = 3).
 */
export default function OnboardingAiPanel({ deviceId, onClose, onMoved }) {
  const { ctx } = useApp()
  const toast = useToast()
  const [device, setDevice] = useState(null)
  const [loading, setLoading] = useState(true)
  const [collecting, setCollecting] = useState(false)
  const [moving, setMoving] = useState(false)
  const [collected, setCollected] = useState(false)

  const load = () => {
    setLoading(true)
    api.getDevice(deviceId, ctx).then((d) => setDevice(d)).catch(() => setDevice(null)).finally(() => setLoading(false))
  }
  useEffect(() => { load() }, [deviceId]) // eslint-disable-line react-hooks/exhaustive-deps

  const name = device?.user_data_name || device?.display_name || device?.name || deviceId
  const stepsDone = collected ? STEPS.length : (device ? STEPS.filter((s) => device[s.key] === 1).length : 0)
  const allDone = stepsDone === STEPS.length
  const alreadyManaged = device?.onboard_status === 3

  const collect = async () => {
    // Demo simulation ONLY — intentionally does not persist or move the asset. Marking all
    // onboarding steps via the backend would auto-promote a draft to managed; the asset must move
    // ONLY when the user clicks "Move to Managed" below.
    setCollecting(true)
    await new Promise((r) => setTimeout(r, 900))
    setCollected(true)
    setCollecting(false)
    toast.success('AI data collection complete')
  }

  const moveToManaged = async () => {
    setMoving(true)
    try {
      await api.onboard(deviceId, { ...ctx, status: 3 })
      toast.success('Asset moved to Managed')
      onMoved?.()
    } catch (e) {
      toast.error(`Move failed: ${e.message}`)
    } finally {
      setMoving(false)
    }
  }

  return (
    <div className="detail-panel">
      <div className="dp-head">
        <span style={{ display: 'flex', alignItems: 'center', gap: 8, fontSize: 14, fontWeight: 500 }}>
          <Icon name="database" size={16} /> Data Collection AI
        </span>
        <button className="icon-btn" onClick={onClose} title="Close"><Icon name="x" size={16} /></button>
      </div>

      <div className="dp-body">
        {loading ? (
          <div className="dp-fields"><Skeleton w="100%" h={40} /><Skeleton w="100%" h={120} /></div>
        ) : !device ? (
          <div className="empty-state"><Icon name="info" size={24} /><p>Couldn't load asset</p></div>
        ) : (
          <>
            <div className="dp-row"><span className="dp-k">Asset</span><span className="dp-v">{name}</span></div>
            <div className="dp-row"><span className="dp-k">Type</span><span className="dp-v">{device.type || 'Generic'}</span></div>

            <div className="dp-section">
              <div style={{ display: 'flex', alignItems: 'center', gap: 6, fontSize: 13, fontWeight: 500, marginBottom: 8 }}>
                <Icon name="database" size={14} /> Data Collection AI
              </div>
              <p style={{ fontSize: 12, color: 'var(--muted)', margin: '0 0 10px' }}>
                Let AI detect and fill this asset's onboarding data — image, geolocation, tag and field details.
              </p>
              <button className="btn btn-primary sm" disabled={collecting || allDone} onClick={collect}>
                {collecting ? <Spinner size={13} /> : <span>✦</span>} {allDone ? 'Data Collected' : 'Collect with AI'}
              </button>
            </div>

            <div className="dp-section">
              <div style={{ fontSize: 13, fontWeight: 500, marginBottom: 8 }}>Onboarding Steps ({stepsDone}/{STEPS.length})</div>
              {STEPS.map((s) => {
                const done = collected || device[s.key] === 1
                return (
                  <div className="dp-row" key={s.key}>
                    <span className="dp-k">{s.label}</span>
                    <span className="dp-v" style={{ color: done ? 'var(--online)' : 'var(--muted)' }}>{done ? '✓ Collected' : '○ Pending'}</span>
                  </div>
                )
              })}
            </div>

            {(collected || allDone) && (
              <div className="dp-section">
                <div style={{ fontSize: 13, fontWeight: 500, marginBottom: 8 }}>AI-collected summary</div>
                <div className="dp-row"><span className="dp-k">Vendor</span><span className="dp-v">{device.user_data_vendor || device.vendor || '—'}</span></div>
                <div className="dp-row"><span className="dp-k">Model</span><span className="dp-v">{device.user_data_model || device.model || '—'}</span></div>
                <div className="dp-row"><span className="dp-k">Location</span><span className="dp-v">{device.location || '—'}</span></div>
              </div>
            )}
          </>
        )}
      </div>

      <div className="dp-foot">
        <button className="btn btn-ghost sm" onClick={onClose}>Close</button>
        <button className="btn btn-primary sm" disabled={moving || loading || !device || alreadyManaged} onClick={moveToManaged}>
          {moving ? <Spinner size={14} /> : <Icon name="arrow-right" size={14} />} {alreadyManaged ? 'Already Managed' : 'Move to Managed'}
        </button>
      </div>
    </div>
  )
}
