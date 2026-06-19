import { useCallback, useEffect, useState } from 'react'
import Icon from '../components/Icon.jsx'
import { Skeleton } from '../components/Skeleton.jsx'
import AssetModal from '../components/AssetModal.jsx'
import OnboardingAiPanel from '../components/OnboardingAiPanel.jsx'
import { useApp } from '../context/AppContext.jsx'
import { useToast } from '../context/ToastContext.jsx'
import api from '../services/api.js'

/**
 * Asset Onboarding page: assets not yet managed (onboard_status != 3). Add an asset, click a row to
 * collect data with AI, then move it to managed. Once moved, the asset leaves this page and lives on
 * the main Asset list page (All / Monitored / Onboarded filters) — we navigate there on move.
 * Additive — does not affect existing pages.
 */
export default function AssetOnboardingPage() {
  const { ctx } = useApp()
  const toast = useToast()
  const [loading, setLoading] = useState(true)
  const [rows, setRows] = useState([])
  const [error, setError] = useState(null)
  const [modalOpen, setModalOpen] = useState(false)
  const [detailId, setDetailId] = useState(null)

  const load = useCallback(async () => {
    setLoading(true); setError(null)
    try {
      const res = await api.listOnboardingAssets(ctx)
      setRows(Array.isArray(res) ? res : [])
    } catch (e) { setError(e); setRows([]) }
    finally { setLoading(false) }
  }, [ctx])
  useEffect(() => { load() }, [load])

  const onboardLabel = (d) => d.onboard_status === 1 ? 'In progress' : d.onboard_status === 2 ? 'In review' : 'Not onboarded'

  // After moving to managed, just remove that one asset from the onboarding list (reload). The
  // asset is now managed and viewable on the main Asset list page under the "Onboarded" filter.
  const handleMoved = () => {
    setDetailId(null)
    toast.success('Asset moved to Managed')
    load()
  }

  return (
    <div className="page">
      <div className="page-head">
        <div>
          <h2 style={{ margin: 0, fontSize: 20 }}>Asset Onboarding</h2>
          <p style={{ color: 'var(--muted)', fontSize: 13, margin: '4px 0 0' }}>Add assets, collect data with AI, then move them to managed.</p>
        </div>
        <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
          <button className="icon-btn" onClick={load} title="Refresh"><Icon name="refresh" size={15} /></button>
          <button className="btn btn-primary sm" onClick={() => setModalOpen(true)}><Icon name="plus" size={15} /> Add Asset</button>
        </div>
      </div>

      {loading ? (
        <div className="asset-list">{Array.from({ length: 5 }).map((_, i) => <Skeleton key={i} w="100%" h={64} />)}</div>
      ) : error ? (
        <div className="empty-state"><Icon name="info" size={28} /><p>Couldn't load assets</p><span style={{ color: 'var(--muted)' }}>{error.message}</span></div>
      ) : rows.length === 0 ? (
        <div className="empty-state"><Icon name="upload" size={28} /><p>No assets to onboard</p>
          <button className="btn btn-primary sm" onClick={() => setModalOpen(true)}><Icon name="plus" size={15} /> Add Asset</button>
        </div>
      ) : (
        <div className="asset-list">
          {rows.map((d) => {
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
                  <span className="badge">{onboardLabel(d)}</span>
                </div>
                <div className="asset-row-actions" onClick={(e) => e.stopPropagation()}>
                  <button className="btn btn-ghost sm" onClick={() => setDetailId(d.id)}><Icon name="database" size={14} /> Onboard</button>
                </div>
              </div>
            )
          })}
        </div>
      )}

      <AssetModal open={modalOpen} editing={null} onClose={() => setModalOpen(false)} onSaved={() => { toast.success('Asset added — ready to onboard'); load() }} />
      {detailId && (
        <OnboardingAiPanel deviceId={detailId} onClose={() => setDetailId(null)} onMoved={handleMoved} />
      )}
    </div>
  )
}
