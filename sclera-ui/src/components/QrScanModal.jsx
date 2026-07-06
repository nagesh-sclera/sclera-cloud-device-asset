import { useEffect, useRef, useState } from 'react'
import { Html5Qrcode } from 'html5-qrcode'
import Icon from './Icon.jsx'
import { Spinner } from './Skeleton.jsx'
import { pdfToImageFiles } from '../services/pdfRender.js'

// Self-contained QR scanner. Knows nothing about assets or the API — its only
// job is to decode a QR (via live camera or an uploaded image) and hand the raw
// text back through onResult(text). Camera needs a secure context (https or
// localhost); when it's blocked, the Upload tab still works over plain http.
const REGION_ID = 'qr-scan-region'

// html5-qrcode only decodes the area INSIDE qrbox, not the whole frame. A fixed
// box (e.g. 240x240) is often smaller than a QR held to a webcam, so the code
// never lands fully inside it and nothing scans. Size the box to the actual
// viewfinder (a large centered square) so a normally-presented QR fits.
const qrboxSize = (vw, vh) => {
  const size = Math.max(160, Math.floor(Math.min(vw, vh) * 0.8))
  return { width: size, height: size }
}

export default function QrScanModal({ onResult, onClose, title = 'Scan QR code' }) {
  const [tab, setTab] = useState('camera') // 'camera' | 'upload'
  const [status, setStatus] = useState('starting') // starting | scanning | error
  const [error, setError] = useState('')
  const scannerRef = useRef(null)
  const startedRef = useRef(false)

  // --- live camera ---
  useEffect(() => {
    if (tab !== 'camera') return
    let cancelled = false
    setStatus('starting'); setError('')
    const scanner = new Html5Qrcode(REGION_ID, { verbose: false })
    scannerRef.current = scanner
    startedRef.current = false

    scanner
      .start(
        { facingMode: 'environment' }, // rear camera on phones
        { fps: 10, qrbox: qrboxSize },
        (decodedText) => {
          if (cancelled) return
          stopCamera().finally(() => onResult(decodedText))
        },
        () => {} // per-frame "not found" — ignore, it fires constantly
      )
      .then(() => { if (!cancelled) { startedRef.current = true; setStatus('scanning') } })
      .catch((err) => {
        if (cancelled) return
        setStatus('error')
        setError(cameraErrorText(err))
      })

    return () => { cancelled = true; stopCamera() }
    // eslint-disable-line react-hooks/exhaustive-deps
  }, [tab]) // eslint-disable-line react-hooks/exhaustive-deps

  const stopCamera = async () => {
    const s = scannerRef.current
    scannerRef.current = null
    if (!s) return
    try { if (startedRef.current) await s.stop() } catch { /* already stopped */ }
    try { await s.clear() } catch { /* ignore */ }
    startedRef.current = false
  }

  const close = () => { stopCamera().finally(onClose) }

  // --- image upload ---
  const fileRef = useRef(null)
  const [fileBusy, setFileBusy] = useState(false)
  const decodeFile = async (file) => {
    if (!file) return
    setFileBusy(true); setError('')
    const isPdf = file.type === 'application/pdf' || /\.pdf$/i.test(file.name)
    try {
      // A PDF (e.g. a generated QR label bundle) is rendered to page images first;
      // an image file is decoded as-is. We scan pages in order and stop at the first hit.
      let images = [file]
      if (isPdf) {
        images = await pdfToImageFiles(file)
        if (!images.length) throw new Error('the PDF has no pages')
      }
      let text = null
      for (const img of images) {
        const s = new Html5Qrcode(REGION_ID, { verbose: false })
        try { text = await s.scanFile(img, false) } catch { /* no QR on this page — try next */ }
        finally { try { await s.clear() } catch { /* ignore */ } }
        if (text) break
      }
      if (text) onResult(text)
      else setError(isPdf
        ? 'No QR code found in that PDF. If a page holds many codes at once, crop to a single code and upload it as an image.'
        : 'Could not read a QR code from that image. Try a clearer, well-lit photo.')
    } catch (err) {
      setError(isPdf
        ? `Could not read the PDF: ${err.message}.`
        : 'Could not read a QR code from that image. Try a clearer, well-lit photo.')
    } finally {
      setFileBusy(false)
    }
  }

  return (
    <div className="modal-overlay" onMouseDown={(e) => { if (e.target === e.currentTarget) close() }}>
      <div className="modal" role="dialog" aria-modal="true" style={{ maxWidth: 440, width: '100%' }}>
        <div className="modal-head">
          <div className="dp-avatar" style={{ background: 'var(--accent, #4a9eff)', color: '#fff' }}><Icon name="qrcode" size={16} /></div>
          <h2 style={{ marginLeft: 8 }}>{title}</h2>
          <button className="icon-btn" onClick={close} aria-label="Close" style={{ marginLeft: 'auto' }}><Icon name="x" /></button>
        </div>

        <div className="modal-body">
          {/* tabs */}
          <div className="status-chips" style={{ marginBottom: 12 }}>
            <button className={`chip ${tab === 'camera' ? 'active' : ''}`} onClick={() => setTab('camera')}>
              <Icon name="camera" size={13} /> Camera
            </button>
            <button className={`chip ${tab === 'upload' ? 'active' : ''}`} onClick={() => { stopCamera(); setTab('upload') }}>
              <Icon name="upload" size={13} /> Upload image / PDF
            </button>
          </div>

          {/* the html5-qrcode library renders the camera stream / decodes files into this element */}
          <div
            id={REGION_ID}
            style={{
              width: '100%', minHeight: tab === 'camera' ? 260 : 0, borderRadius: 8,
              overflow: 'hidden', background: '#000',
              display: tab === 'camera' ? 'block' : 'none',
            }}
          />

          {tab === 'camera' && (
            <div style={{ marginTop: 10, fontSize: 12.5 }}>
              {status === 'starting' && <span className="muted"><Spinner size={13} /> Starting camera…</span>}
              {status === 'scanning' && <span className="muted">Point the rear camera at a QR code.</span>}
              {status === 'error' && (
                <div className="note" style={{ color: 'var(--warning, #d29922)' }}>
                  {error}
                  <div style={{ marginTop: 6 }}>
                    <button className="btn btn-ghost sm" onClick={() => { stopCamera(); setTab('upload') }}>Use image upload instead</button>
                  </div>
                </div>
              )}
            </div>
          )}

          {tab === 'upload' && (
            <div style={{ padding: '8px 0' }}>
              <p className="muted" style={{ marginTop: 0, fontSize: 12.5 }}>
                Choose a photo of the QR code, or a PDF (e.g. a generated QR label bundle).
                Works even where the camera is blocked.
              </p>
              <input
                ref={fileRef}
                type="file"
                accept="image/*,application/pdf"
                disabled={fileBusy}
                onChange={(e) => decodeFile(e.target.files?.[0])}
              />
              {fileBusy && <div style={{ marginTop: 10 }}><Spinner size={13} /> <span className="muted">Reading file…</span></div>}
              {error && <div className="note" style={{ marginTop: 10, color: 'var(--danger, #f85149)' }}>{error}</div>}
            </div>
          )}
        </div>
      </div>
    </div>
  )
}

function cameraErrorText(err) {
  const name = err?.name || ''
  const msg = String(err?.message || err || '')
  if (name === 'NotAllowedError' || /permission/i.test(msg)) {
    return 'Camera permission denied. Allow camera access, or use image upload.'
  }
  if (name === 'NotFoundError' || /no camera|not found/i.test(msg)) {
    return 'No camera found on this device. Use image upload instead.'
  }
  if (/secure context|https|getUserMedia/i.test(msg) || (typeof window !== 'undefined' && !window.isSecureContext)) {
    return 'Camera needs a secure page (https or localhost). Over plain http on a phone the camera is blocked — use image upload.'
  }
  return `Could not start the camera: ${msg}. Use image upload instead.`
}
