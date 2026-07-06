import { useEffect, useMemo, useRef, useState } from 'react'
import Icon from './Icon.jsx'
import { Spinner } from './Skeleton.jsx'

// Digital-twin editor/viewer for a single device. A twin is an IMAGE of the
// physical asset (digital_twin_image_url on the backend) with measuring-instrument
// markers positioned on it (digital_twin_position). Here markers are the device's
// sensors, positioned as %-based x/y so they stay put when the image is resized.
//
// State is persisted per-device in localStorage (frontend-only, demo). It maps
// 1:1 to the existing backend endpoints (upsertdigitaltwininstruments /
// deletedigitaltwin / multieditdigitaltwininstruments) so it can be wired later.
const KEY = (id) => `sclera.twin.${id}`

const loadTwin = (id) => {
  try { return JSON.parse(localStorage.getItem(KEY(id))) || null } catch { return null }
}
const saveTwin = (id, data) => {
  try {
    if (data && (data.image || data.markers?.length)) localStorage.setItem(KEY(id), JSON.stringify(data))
    else localStorage.removeItem(KEY(id))
  } catch { /* quota / private mode — ignore, it's only demo state */ }
}

const fileToDataUrl = (file) => new Promise((resolve, reject) => {
  const r = new FileReader()
  r.onload = () => resolve(r.result)
  r.onerror = reject
  r.readAsDataURL(file)
})

// clamp a value into [0,100] (percent within the stage)
const pct = (v) => Math.max(0, Math.min(100, v))
const toneOf = (s) => (s?.status === 'alert' ? 'alert' : s?.status === 'ok' ? 'ok' : 'idle')

export default function DigitalTwinModal({ device, sensors, onClose }) {
  const deviceId = device?.id
  const name = device?.user_data_name || device?.display_name || device?.name || 'Asset'

  const [image, setImage] = useState(null)          // data URL of the twin image
  const [markers, setMarkers] = useState([])        // [{ sensorId, x, y }]
  const [mode, setMode] = useState('edit')          // 'edit' | 'view'
  const [busy, setBusy] = useState(false)
  const [selected, setSelected] = useState(null)    // sensorId of open reading tooltip (view mode)
  const stageRef = useRef(null)
  const dragRef = useRef(null)                       // { sensorId } while dragging
  const fileRef = useRef(null)

  // hydrate persisted twin on open
  useEffect(() => {
    const t = loadTwin(deviceId)
    if (t) { setImage(t.image || null); setMarkers(Array.isArray(t.markers) ? t.markers : []) }
    setMode(t?.image ? 'view' : 'edit')
  }, [deviceId])

  // persist on every change
  useEffect(() => { if (deviceId) saveTwin(deviceId, { image, markers }) }, [deviceId, image, markers])

  const list = Array.isArray(sensors) ? sensors : []
  const sensorById = useMemo(() => new Map(list.map((s) => [String(s.id), s])), [list])
  const placedIds = useMemo(() => new Set(markers.map((m) => String(m.sensorId))), [markers])

  const chooseImage = async (file) => {
    if (!file) return
    setBusy(true)
    try { setImage(await fileToDataUrl(file)); setMode('edit') }
    finally { setBusy(false) }
  }

  const placeSensor = (s) => {
    const id = String(s.id)
    if (placedIds.has(id)) { // already placed — nudge focus to it
      setSelected(id); return
    }
    // drop new markers slightly offset so a stack of them stays clickable
    const n = markers.length
    setMarkers((m) => [...m, { sensorId: id, x: pct(42 + (n % 4) * 4), y: pct(42 + Math.floor(n / 4) * 6) }])
  }
  const removeMarker = (id) => setMarkers((m) => m.filter((x) => String(x.sensorId) !== String(id)))
  const clearAll = () => { setMarkers([]); setSelected(null) }

  // --- dragging (edit mode) ---
  const stagePointFromEvent = (e) => {
    const box = stageRef.current?.getBoundingClientRect()
    if (!box) return { x: 50, y: 50 }
    return { x: pct(((e.clientX - box.left) / box.width) * 100), y: pct(((e.clientY - box.top) / box.height) * 100) }
  }
  const onMarkerDown = (e, id) => {
    if (mode !== 'edit') return
    e.stopPropagation()
    dragRef.current = { sensorId: String(id) }
    window.addEventListener('pointermove', onDragMove)
    window.addEventListener('pointerup', onDragUp, { once: true })
  }
  const onDragMove = (e) => {
    const d = dragRef.current
    if (!d) return
    const p = stagePointFromEvent(e)
    setMarkers((m) => m.map((x) => (String(x.sensorId) === d.sensorId ? { ...x, x: p.x, y: p.y } : x)))
  }
  const onDragUp = () => {
    dragRef.current = null
    window.removeEventListener('pointermove', onDragMove)
  }
  useEffect(() => () => window.removeEventListener('pointermove', onDragMove), [])

  const placedMarkers = markers.filter((m) => sensorById.has(String(m.sensorId)))

  return (
    <div className="modal-overlay" onMouseDown={(e) => { if (e.target === e.currentTarget) onClose() }}>
      <style>{TWIN_CSS}</style>
      <div className="modal dt-modal" role="dialog" aria-modal="true" aria-label="Digital twin">
        <div className="modal-head">
          <div className="dp-avatar" style={{ background: 'var(--accent, #4a9eff)', color: '#fff' }}><Icon name="layers" size={16} /></div>
          <h2 style={{ marginLeft: 8 }}>Digital Twin — <span className="muted">{name}</span></h2>
          <div className="dt-head-actions">
            {image && (
              <div className="dt-modeswitch" role="tablist">
                <button className={`chip ${mode === 'view' ? 'active' : ''}`} onClick={() => setMode('view')}><Icon name="globe" size={13} /> View</button>
                <button className={`chip ${mode === 'edit' ? 'active' : ''}`} onClick={() => { setMode('edit'); setSelected(null) }}><Icon name="edit" size={13} /> Edit</button>
              </div>
            )}
            <button className="icon-btn" onClick={onClose} aria-label="Close"><Icon name="x" /></button>
          </div>
        </div>

        <div className="modal-body dt-body">
          {/* stage */}
          <div className="dt-stage-wrap">
            {!image ? (
              <div className="dt-empty">
                <Icon name="layers" size={30} />
                <p>No digital twin yet</p>
                <span className="muted">Upload a photo or schematic of <b>{name}</b>, then drop its sensors onto it.</span>
                <button className="btn btn-primary sm" disabled={busy} onClick={() => fileRef.current?.click()}>
                  {busy ? <Spinner size={13} /> : <Icon name="upload" size={14} />} Upload image
                </button>
              </div>
            ) : (
              <div
                ref={stageRef}
                className={`dt-stage ${mode}`}
                style={{ backgroundImage: `url(${image})` }}
                onClick={() => setSelected(null)}
              >
                {placedMarkers.map((m) => {
                  const s = sensorById.get(String(m.sensorId))
                  const tone = toneOf(s)
                  const open = selected === String(m.sensorId)
                  return (
                    <div
                      key={m.sensorId}
                      className={`dt-marker tone-${tone} ${open ? 'open' : ''}`}
                      style={{ left: `${m.x}%`, top: `${m.y}%` }}
                      onPointerDown={(e) => onMarkerDown(e, m.sensorId)}
                      onClick={(e) => { e.stopPropagation(); if (mode === 'view') setSelected(open ? null : String(m.sensorId)) }}
                      title={s?.name}
                    >
                      <span className="dt-dot" />
                      <div className="dt-tip">
                        <div className="dt-tip-name">{s?.name || 'Sensor'}</div>
                        <div className="dt-tip-val">{s?.value || '—'}<span className="dt-tip-unit">{s?.unit || ''}</span></div>
                        <div className="dt-tip-meta">{(s?.type || 'sensor')}{s?.protocol ? ` · ${s.protocol}` : ''}</div>
                      </div>
                      {mode === 'edit' && (
                        <button className="dt-marker-x" onClick={(e) => { e.stopPropagation(); removeMarker(m.sensorId) }} title="Remove"><Icon name="x" size={10} /></button>
                      )}
                    </div>
                  )
                })}
                {mode === 'edit' && <div className="dt-hint">Drag markers to position · add sensors from the right</div>}
              </div>
            )}
          </div>

          {/* sidebar */}
          <div className="dt-side">
            <div className="dt-side-head">
              <span>Sensors</span>
              {image && (
                <div className="dt-side-tools">
                  <button className="icon-btn" title="Change image" onClick={() => fileRef.current?.click()}><Icon name="upload" size={14} /></button>
                  {placedMarkers.length > 0 && mode === 'edit' && (
                    <button className="icon-btn danger" title="Clear all markers" onClick={clearAll}><Icon name="trash" size={14} /></button>
                  )}
                </div>
              )}
            </div>

            {list.length === 0 ? (
              <div className="dt-side-empty muted">No sensors on this asset yet. Add sensors from the Sensors tab, then place them here.</div>
            ) : (
              <div className="dt-sensor-list">
                {list.map((s) => {
                  const on = placedIds.has(String(s.id))
                  return (
                    <button
                      key={s.id}
                      className={`dt-sensor ${on ? 'placed' : ''}`}
                      disabled={mode === 'view' && !on}
                      onClick={() => (mode === 'edit' ? placeSensor(s) : setSelected(String(s.id)))}
                    >
                      <span className={`dt-sensor-dot tone-${toneOf(s)}`} />
                      <span className="dt-sensor-name">{s.name}</span>
                      <span className="dt-sensor-val">{s.value ? `${s.value}${s.unit || ''}` : ''}</span>
                      {on
                        ? <span className="dt-sensor-tag">placed</span>
                        : mode === 'edit' && <Icon name="plus" size={13} />}
                    </button>
                  )
                })}
              </div>
            )}

            <div className="dt-legend">
              <span><i className="tone-ok" /> OK</span>
              <span><i className="tone-alert" /> Alert</span>
              <span><i className="tone-idle" /> Idle</span>
            </div>
          </div>
        </div>

        <input ref={fileRef} type="file" accept="image/*" style={{ display: 'none' }} onChange={(e) => chooseImage(e.target.files?.[0])} />
      </div>
    </div>
  )
}

const TWIN_CSS = `
.dt-modal { max-width: 960px; width: 100%; }
.dt-head-actions { margin-left: auto; display: flex; align-items: center; gap: 10px; }
.dt-modeswitch { display: flex; gap: 4px; }
.dt-body { display: flex; gap: 14px; padding-top: 10px; }
.dt-stage-wrap { flex: 1 1 auto; min-width: 0; }
.dt-side { flex: 0 0 220px; display: flex; flex-direction: column; gap: 10px; }

.dt-stage {
  position: relative; width: 100%; aspect-ratio: 4 / 3; border-radius: 10px;
  background-size: contain; background-repeat: no-repeat; background-position: center;
  background-color: var(--surface-2, #11151c); border: 1px solid var(--border, #2a2f3a);
  user-select: none; touch-action: none;
}
.dt-stage.edit { cursor: crosshair; }

.dt-empty {
  display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 8px;
  text-align: center; width: 100%; aspect-ratio: 4 / 3; border-radius: 10px;
  border: 1px dashed var(--border, #2a2f3a); background: var(--surface-2, #11151c); color: var(--text, #cbd3df);
}
.dt-empty p { margin: 4px 0 0; font-weight: 600; }
.dt-empty span { max-width: 320px; font-size: 12.5px; }
.dt-empty .btn { margin-top: 6px; }

.dt-marker { position: absolute; transform: translate(-50%, -50%); z-index: 2; }
.dt-stage.edit .dt-marker { cursor: grab; }
.dt-stage.edit .dt-marker:active { cursor: grabbing; }
.dt-dot {
  display: block; width: 16px; height: 16px; border-radius: 50%;
  border: 2px solid #fff; box-shadow: 0 0 0 2px rgba(0,0,0,.35), 0 1px 4px rgba(0,0,0,.4);
}
.dt-marker.tone-ok    .dt-dot { background: #2ea043; }
.dt-marker.tone-alert .dt-dot { background: #f85149; animation: dt-pulse 1.4s ease-in-out infinite; }
.dt-marker.tone-idle  .dt-dot { background: #8b949e; }
@keyframes dt-pulse { 0%,100% { box-shadow: 0 0 0 2px rgba(0,0,0,.35), 0 0 0 0 rgba(248,81,73,.55);} 50% { box-shadow: 0 0 0 2px rgba(0,0,0,.35), 0 0 0 8px rgba(248,81,73,0);} }

.dt-tip {
  position: absolute; bottom: 22px; left: 50%; transform: translateX(-50%);
  min-width: 108px; padding: 6px 8px; border-radius: 8px; pointer-events: none;
  background: var(--surface, #1c2129); border: 1px solid var(--border, #2a2f3a);
  box-shadow: 0 6px 20px rgba(0,0,0,.35); opacity: 0; transition: opacity .12s; white-space: nowrap;
}
.dt-marker:hover .dt-tip, .dt-marker.open .dt-tip { opacity: 1; }
.dt-tip-name { font-size: 11px; color: var(--muted, #8b949e); }
.dt-tip-val { font-size: 16px; font-weight: 700; }
.dt-tip-unit { font-size: 11px; font-weight: 500; margin-left: 2px; color: var(--muted, #8b949e); }
.dt-tip-meta { font-size: 10.5px; color: var(--muted, #8b949e); margin-top: 1px; }

.dt-marker-x {
  position: absolute; top: -8px; right: -8px; width: 16px; height: 16px; border-radius: 50%;
  display: none; align-items: center; justify-content: center; padding: 0;
  background: #f85149; color: #fff; border: 1px solid #fff; cursor: pointer;
}
.dt-marker:hover .dt-marker-x { display: flex; }

.dt-hint {
  position: absolute; left: 8px; bottom: 8px; font-size: 11px; color: var(--muted, #8b949e);
  background: rgba(0,0,0,.45); padding: 3px 8px; border-radius: 6px; pointer-events: none;
}

.dt-side-head { display: flex; align-items: center; justify-content: space-between; font-weight: 600; font-size: 13px; }
.dt-side-tools { display: flex; gap: 2px; }
.dt-side-empty { font-size: 12px; line-height: 1.4; }
.dt-sensor-list { display: flex; flex-direction: column; gap: 6px; overflow-y: auto; max-height: 46vh; }
.dt-sensor {
  display: flex; align-items: center; gap: 8px; width: 100%; text-align: left;
  padding: 7px 9px; border-radius: 8px; cursor: pointer; font-size: 12.5px;
  background: var(--surface-2, #11151c); border: 1px solid var(--border, #2a2f3a); color: var(--text, #cbd3df);
}
.dt-sensor:hover:not(:disabled) { border-color: var(--accent, #4a9eff); }
.dt-sensor:disabled { opacity: .5; cursor: default; }
.dt-sensor.placed { border-color: var(--accent, #4a9eff); background: color-mix(in srgb, var(--accent, #4a9eff) 10%, transparent); }
.dt-sensor-name { flex: 1 1 auto; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.dt-sensor-val { font-size: 11px; color: var(--muted, #8b949e); }
.dt-sensor-tag { font-size: 10px; text-transform: uppercase; letter-spacing: .04em; color: var(--accent, #4a9eff); }
.dt-sensor-dot, .dt-legend i { width: 9px; height: 9px; border-radius: 50%; flex: 0 0 auto; display: inline-block; }
.tone-ok { background: #2ea043; } .tone-alert { background: #f85149; } .tone-idle { background: #8b949e; }

.dt-legend { display: flex; gap: 12px; font-size: 11px; color: var(--muted, #8b949e); margin-top: auto; padding-top: 6px; }
.dt-legend span { display: inline-flex; align-items: center; gap: 5px; }

@media (max-width: 720px) { .dt-body { flex-direction: column; } .dt-side { flex-basis: auto; } }
`
