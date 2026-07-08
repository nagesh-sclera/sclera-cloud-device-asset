import { useEffect, useRef, useState } from 'react'
import { Html5Qrcode, Html5QrcodeSupportedFormats } from 'html5-qrcode'
import Icon from './Icon.jsx'
import { Spinner } from './Skeleton.jsx'
import { pdfToImageFiles } from '../services/pdfRender.js'

// Self-contained bar code scanner. Knows nothing about assets or the API — its only
// job is to decode a bar code (via live camera or an uploaded image/PDF) and hand the
// raw text back through onResult(text). Camera needs a secure context (https or
// localhost); when it's blocked, the Upload tab still works over plain http.
const REGION_ID = 'barcode-scan-region'

// Common 1D bar code formats html5-qrcode should look for (plus QR, which also
// encodes bar-code-style ids). Kept resilient: if the enum is unavailable for some
// reason, we simply omit the hint and let the library scan with its defaults.
const BARCODE_FORMATS = (() => {
  try {
    return [
      Html5QrcodeSupportedFormats.CODE_128,
      Html5QrcodeSupportedFormats.EAN_13,
      Html5QrcodeSupportedFormats.CODE_39,
      Html5QrcodeSupportedFormats.UPC_A,
      Html5QrcodeSupportedFormats.QR_CODE,
    ].filter((f) => f !== undefined)
  } catch { return undefined }
})()
const scanConfig = () => (BARCODE_FORMATS && BARCODE_FORMATS.length
  ? { verbose: false, formatsToSupport: BARCODE_FORMATS }
  : { verbose: false })

// html5-qrcode only decodes the area INSIDE qrbox, not the whole frame. A 1D bar code
// held to a webcam is WIDE — if the box is a small centred square (driven by the shorter
// video dimension) the code's ends and quiet zones fall outside it and never decode, even
// though a compact QR would fit. So size the box as a wide, short strip based on the
// viewport WIDTH: nearly full width so the whole bar code + quiet zones sit inside it.
const qrboxSize = (vw, vh) => {
  const w = Math.max(200, Math.floor(vw * 0.92))
  const h = Math.max(90, Math.floor(vh * 0.45))
  return { width: w, height: h }
}

export default function BarcodeScanModal({ onResult, onClose, title = 'Scan barcode' }) {
  const [tab, setTab] = useState('camera') // 'camera' | 'upload'
  const [status, setStatus] = useState('starting') // starting | scanning | error
  const [error, setError] = useState('')
  const [diag, setDiag] = useState('') // which decoder is active (diagnostic)
  const scannerRef = useRef(null)

  // DIAGNOSTIC: report whether the browser exposes a native BarcodeDetector (great at 1D)
  // or html5-qrcode must fall back to its JS decoder (weak at 1D over a webcam).
  useEffect(() => {
    const BD = typeof window !== 'undefined' ? window.BarcodeDetector : undefined
    if (!BD) { setDiag('Decoder: JS fallback (no native BarcodeDetector in this browser)'); return }
    if (typeof BD.getSupportedFormats === 'function') {
      BD.getSupportedFormats()
        .then((fmts) => setDiag('Decoder: native BarcodeDetector — formats: ' + ((fmts && fmts.length) ? fmts.join(', ') : 'none reported')))
        .catch(() => setDiag('Decoder: native BarcodeDetector present (format list unavailable)'))
    } else {
      setDiag('Decoder: native BarcodeDetector present')
    }
  }, [])

  // --- live camera ---
  useEffect(() => {
    if (tab !== 'camera') return
    let cancelled = false
    setStatus('starting'); setError('')
    const scanner = new Html5Qrcode(REGION_ID, scanConfig())
    scannerRef.current = scanner

    // start() is async (getUserMedia). Keep the promise so cleanup can wait for it.
    const startP = scanner
      .start(
        { facingMode: 'environment' }, // rear camera on phones
        { fps: 10, qrbox: qrboxSize },
        (decodedText) => {
          if (cancelled) return
          stopCamera().finally(() => onResult(decodedText))
        },
        () => {} // per-frame "not found" — ignore, it fires constantly
      )
      .then(() => { if (!cancelled) setStatus('scanning') })
      .catch((err) => {
        if (cancelled) return
        setStatus('error')
        setError(cameraErrorText(err))
      })

    return () => {
      cancelled = true
      // Stop THIS scanner, and only AFTER start() settles. React StrictMode mounts→
      // unmounts→remounts in dev; stopping before the async start resolves would leak
      // its camera stream and the remount would open a SECOND camera (double-camera bug).
      // Also stop the captured `scanner`, not scannerRef.current — the remount may have
      // already reassigned the ref to the new instance by the time this deferred stop runs.
      if (scannerRef.current === scanner) scannerRef.current = null
      startP.finally(() => stopScanner(scanner))
    }
    // eslint-disable-line react-hooks/exhaustive-deps
  }, [tab]) // eslint-disable-line react-hooks/exhaustive-deps

  // Stop + tear down a specific scanner instance. stop() rejects if it isn't running
  // (e.g. start() failed) — swallow that; clear() removes the injected DOM either way.
  const stopScanner = async (s) => {
    if (!s) return
    try { await s.stop() } catch { /* not running */ }
    try { await s.clear() } catch { /* ignore */ }
  }

  const stopCamera = async () => {
    const s = scannerRef.current
    scannerRef.current = null
    await stopScanner(s)
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
      // A PDF (e.g. a generated bar code label bundle) is rendered to page images first;
      // an image file is decoded as-is. We scan pages in order and stop at the first hit.
      let images = [file]
      if (isPdf) {
        images = await pdfToImageFiles(file)
        if (!images.length) throw new Error('the PDF has no pages')
      }
      let text = null
      for (const img of images) {
        const s = new Html5Qrcode(REGION_ID, scanConfig())
        try { text = await s.scanFile(img, false) } catch { /* no code on this page — try next */ }
        finally { try { await s.clear() } catch { /* ignore */ } }
        if (text) break
      }
      if (text) onResult(text)
      else setError(isPdf
        ? 'No bar code found in that PDF. If a page holds many codes at once, crop to a single code and upload it as an image.'
        : 'Could not read a bar code from that image. Try a clearer, well-lit photo.')
    } catch (err) {
      setError(isPdf
        ? `Could not read the PDF: ${err.message}.`
        : 'Could not read a bar code from that image. Try a clearer, well-lit photo.')
    } finally {
      setFileBusy(false)
    }
  }

  return (
    <div className="modal-overlay" onMouseDown={(e) => { if (e.target === e.currentTarget) close() }}>
      <div className="modal" role="dialog" aria-modal="true" style={{ maxWidth: 440, width: '100%' }}>
        <div className="modal-head">
          <div className="dp-avatar" style={{ background: 'var(--accent, #4a9eff)', color: '#fff' }}><Icon name="barcode" size={16} /></div>
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
              {diag && <div className="note" style={{ marginBottom: 6, fontSize: 11, wordBreak: 'break-word' }}>{diag}</div>}
              {status === 'starting' && <span className="muted"><Spinner size={13} /> Starting camera…</span>}
              {status === 'scanning' && <span className="muted">Point the rear camera at a bar code.</span>}
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
                Choose a photo of the bar code, or a PDF (e.g. a generated bar code label bundle).
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
