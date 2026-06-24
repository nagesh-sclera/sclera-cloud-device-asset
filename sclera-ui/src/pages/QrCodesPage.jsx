import { useMemo, useState } from 'react'
import { API_CONFIG, DEMO } from '../config.js'
import { useApp } from '../context/AppContext.jsx'
import Icon from '../components/Icon.jsx'

// Base for all 5 QR admin controllers (they share this class-level mapping).
// Goes through the gateway: {BASE_URL}/asset/api/v1/sclera-cloud-device-asset-service/...
const QR_BASE = `${API_CONFIG.BASE_URL}${API_CONFIG.ASSET_PREFIX}`

// ---- endpoint catalog (mirrors the backend QR controllers) ----------------
// q: query params [{n,d,r,hint}]  | pd: path-var defaults | body:{type:'json'|'string',ex}
// multipart:[{n,type:'file'|'text',ex}] | download:true (GET binary) | list:[queryNamesToSplitOnComma]
const EP = [
  { g: 'QR Codes', t: 'Generate & download QR codes (≤1000)', m: 'GET', path: '/qrCode/generateQRCode', download: true,
    q: [{ n: 'orgId', r: 1 }, { n: 'email', r: 1 }, { n: 'count', d: '1', hint: '1–1000' }, { n: 'width', d: '2.75' }, { n: 'height', d: '4.0' },
        { n: 'type', d: 'pdf', r: 1, hint: 'zip | pdf | txt' }, { n: 'brand', d: 'true' }, { n: 'defaultTemplate', d: 'false' }, { n: 'templateUrl' }],
    desc: 'Streams a pdf/zip/txt bundle of generated QR codes.' },
  { g: 'QR Codes', t: 'Generate bulk QR codes (>1000, async email)', m: 'POST', path: '/qrCode/generateBulkQRCode',
    q: [{ n: 'orgId', r: 1 }, { n: 'email', r: 1 }, { n: 'count', d: '1001', hint: '1001–50000' }, { n: 'width', d: '2.75' }, { n: 'height', d: '4.0' },
        { n: 'type', d: 'pdf', r: 1, hint: 'zip | pdf | txt' }, { n: 'brand', d: 'true' }, { n: 'defaultTemplate', d: 'false' }, { n: 'templateUrl' }],
    body: { type: 'string', ex: 'recipient@example.com' },
    desc: 'Body = raw emailTo string. Generates asynchronously; link logged.' },
  { g: 'QR Codes', t: 'Update / tag a QR code', m: 'POST', path: '/qrCode/updateQrCode',
    q: [{ n: 'loggedInUser', r: 1 }],
    body: { type: 'json', ex: { id: 'QR123', deviceId: 'DEV1', locationId: 'LOC1', vdmsId: 'VDMS760' } },
    desc: 'Upserts a QR code tag assignment (QrCodeDTO).' },
  { g: 'QR Codes', t: 'Get QR code details by id', m: 'GET', path: '/qrCode/{qrCodeId}/getQrCodeDetailsByQrCodeId',
    pd: { qrCodeId: 'QR123' }, desc: 'Full QR code details for one id.' },
  { g: 'QR Codes', t: 'QR codes by vdmsId + deviceId', m: 'GET',
    path: '/vdms/{vdmsId}/deviceId/{deviceId}/getQrCodeDetailsByVdmsIdAndDeviceId', pd: { deviceId: 'DEV1' }, desc: 'List<QrCodeDTO> for a device.' },
  { g: 'QR Codes', t: 'QR codes by vdmsId + locationId', m: 'GET',
    path: '/vdms/{vdmsId}/locationId/{locationId}/getQrCodeDetailsByVdmsIdAndLocationId', pd: { locationId: 'LOC1' }, desc: 'List<QrCodeDTO> for a location.' },
  { g: 'QR Codes', t: 'List untagged QR codes (paged)', m: 'GET', path: '/vdms/{vdmsId}/getUnTaggedQrCode',
    q: [{ n: 'loggedInUser', r: 1 }, { n: 'pageNo', d: '1' }, { n: 'pageSize', d: '1000' }], desc: 'Untagged QR ids for a vdms.' },
  { g: 'QR Codes', t: 'Count QR codes since lastSyncTime', m: 'GET', path: '/vdms/{vdmsId}/getQrCodeCounts',
    q: [{ n: 'lastSyncTime', r: 1, d: '0', hint: 'epoch millis' }], desc: 'Count updated after lastSyncTime.' },
  { g: 'QR Codes', t: 'ADC / tagged check by id', m: 'GET', path: '/qrCode/{qrCodeId}/getQrCodeCheckById',
    pd: { qrCodeId: 'QR123' }, desc: 'isTagged (0/1/2) + isAdc.' },
  { g: 'QR Codes', t: 'Update details for a list of QR codes', m: 'PUT', path: '/vdms/{vdmsId}/updateQrCodeDetails',
    body: { type: 'json', ex: [{ id: 'QR123', deviceId: 'DEV1', locationId: 'LOC1' }] }, desc: 'Body = List<QrCodeDTO>.' },

  // ---- Client QR ----
  { g: 'Client QR', t: 'Tag / re-tag a client QR code', m: 'POST', path: '/clientQrCode',
    q: [{ n: 'orgId', r: 1 }, { n: 'email', r: 1 }, { n: 'loggedInUser', r: 1 }],
    body: { type: 'json', ex: { clientQrCodeId: 'CQR1', deviceId: 'DEV1', locationId: 'LOC1', vdmsId: 'VDMS760' } },
    desc: 'ClientQrCodeDTO. Re-tag dedups by clientQrCodeId.' },
  { g: 'Client QR', t: 'Client QR by clientQrCodeId', m: 'GET', path: '/clientQrCode/getClientQrCodeDetailsByClientQrCodeId',
    q: [{ n: 'clientQrCodeId', r: 1, d: 'CQR1' }], desc: 'Lookup one client QR record.' },
  { g: 'Client QR', t: 'Client QR by vdmsId + deviceId', m: 'GET',
    path: '/vdms/{vdmsId}/deviceId/{deviceId}/getClientQrCodeDetailsByVdmsIdAndDeviceId', pd: { deviceId: 'DEV1' }, desc: 'List for a device.' },
  { g: 'Client QR', t: 'Client QR by vdmsId + locationId', m: 'GET',
    path: '/vdms/{vdmsId}/locationId/{locationId}/getClientQrCodeDetailsByVdmsIdAndLocationId', pd: { locationId: 'LOC1' }, desc: 'List for a location.' },
  { g: 'Client QR', t: 'Import client QR codes (Excel)', m: 'POST', path: '/importClientQrCode',
    q: [{ n: 'orgId', r: 1 }, { n: 'email', r: 1 }, { n: 'loggedInUser', r: 1 }], multipart: [{ n: 'file', type: 'file' }],
    desc: 'XLSX/XLS headers: client_qr_code_id, device_id, location_id, vdms_id.' },
  { g: 'Client QR', t: 'List untagged client QR codes (paged)', m: 'GET', path: '/vdms/{vdmsId}/getUnTaggedClientQrCode',
    q: [{ n: 'orgId', r: 1 }, { n: 'email', r: 1 }, { n: 'loggedInUser', r: 1 }, { n: 'pageNo', d: '1' }, { n: 'pageSize', d: '1000' }],
    desc: 'Untagged client QR codes for a vdms.' },
  { g: 'Client QR', t: 'Preview / validate Excel before import', m: 'POST', path: '/vdms/{vdmsId}/clientQrCode/preview',
    q: [{ n: 'loggedInUser', r: 1 }], multipart: [{ n: 'file', type: 'file' }], desc: 'Structural validation of the upload.' },
  { g: 'Client QR', t: 'ADC check by clientQrCodeId', m: 'GET', path: '/clientQrCode/getClientQrCodeCheckById',
    q: [{ n: 'clientQrCodeId', r: 1, d: 'CQR1' }], desc: 'ADC tagging status.' },

  // ---- Global QR (note casing: username / vdmsid) ----
  { g: 'Global QR', t: 'List global QR codes by type', m: 'POST', path: '/type/{qrcode_type}/getqrcodes', pd: { qrcode_type: 'all' },
    q: [{ n: 'username', r: 1 }, { n: 'vdmsid', r: 1 }, { n: 'searchkey', d: 'null' }, { n: 'pageno', d: '1' }, { n: 'pagesize', d: '10' }],
    body: { type: 'json', ex: { device_types: [], building_id: '', floor_id: '' } },
    desc: 'qrcode_type: all | untagged | device | location. Body = filter JSON.' },
  { g: 'Global QR', t: 'Delete global QR codes', m: 'DELETE', path: '/deleteqrcodes',
    q: [{ n: 'username', r: 1 }, { n: 'vdmsid', r: 1 }], body: { type: 'json', ex: ['id1', 'id2'] }, desc: 'Body = Set<String> of qr ids.' },
  { g: 'Global QR', t: 'Export global QR codes to PDF', m: 'GET', path: '/exportqrcodes', download: true,
    q: [{ n: 'username', r: 1 }, { n: 'vdmsid', r: 1 }, { n: 'qrcodes', hint: 'comma-separated ids' }, { n: 'type', d: 'all' },
        { n: 'dockernames', hint: 'comma-separated' }, { n: 'device_types', hint: 'comma-separated' },
        { n: 'building_ids', hint: 'comma-separated' }, { n: 'floor_ids', hint: 'comma-separated' }, { n: 'width', d: '2' }, { n: 'height', d: '2' }],
    list: ['qrcodes', 'dockernames', 'device_types', 'building_ids', 'floor_ids'],
    desc: 'Streams a PDF. List params = comma separated (blank ⇒ server default).' },
  { g: 'Global QR', t: 'Upsert global QR codes', m: 'POST', path: '/upsertglobalqrcode',
    q: [{ n: 'username', r: 1 }, { n: 'vdmsid', r: 1 }], body: { type: 'json', ex: [{ device_id: 'DEV1', location_id: 'LOC1', qrcode_type: 'device' }] },
    desc: 'Set<GlobalQrcodeDTO>. Omit id to create.' },
  { g: 'Global QR', t: 'Create N untagged QR codes', m: 'GET', path: '/count/{count}/createqrcode', pd: { count: '5' },
    q: [{ n: 'username', r: 1 }, { n: 'vdmsid', r: 1 }], desc: "Generates 'count' new untagged QR codes." },
  { g: 'Global QR', t: 'Resolve scanned QR to asset detail', m: 'POST', path: '/getqrcodedetail',
    q: [{ n: 'username', r: 1 }, { n: 'vdmsid', r: 1 }], body: { type: 'json', ex: { id: 'QR1', location_id: 'LOC1', device_id: 'DEV1' } },
    desc: 'GlobalQrcodeDTO ⇒ List<GlobalQrcodeDTO>.' },

  // ---- Templates ----
  { g: 'Templates', t: 'List templates by org (paged)', m: 'GET', path: '/organisation/{orgId}/QrCodeTemplate/getAllQrCodeTemplateByOrgId',
    q: [{ n: 'loggedInUser' }, { n: 'pageNo', d: '1' }, { n: 'pageSize', d: '1000' }, { n: 'key', d: 'all' }], desc: 'Paginated, searchable template list.' },
  { g: 'Templates', t: 'Get in-use template URL', m: 'GET', path: '/organisation/{orgId}/QrCodeTemplate/getInUseUrlByOrgId', desc: 'Currently selected template.' },
  { g: 'Templates', t: 'Get default template', m: 'GET', path: '/QrCodeTemplate/getDefaultTemplate', desc: 'System default template.' },
  { g: 'Templates', t: 'Add template (multipart)', m: 'POST', path: '/organisation/{orgId}/QrCodeTemplate/addQrCodeTemplate',
    q: [{ n: 'loggedInUser' }], multipart: [{ n: 'file', type: 'file' }, { n: 'companyLogo', type: 'file' }, { n: 'body', type: 'text', ex: '{"templateName":"My Template"}' }],
    desc: 'file + companyLogo images; body = JSON metadata string (required).' },
  { g: 'Templates', t: 'Update template (multipart)', m: 'PUT', path: '/organisation/{orgId}/QrCodeTemplate/updateQrCodeTemplate',
    q: [{ n: 'templateUrl' }, { n: 'logoUrl' }, { n: 'loggedInUser' }],
    multipart: [{ n: 'file', type: 'file' }, { n: 'companyLogo', type: 'file' }, { n: 'body', type: 'text', ex: '{"id":"TPL1","templateName":"Updated"}' }],
    desc: 'body = JSON metadata string (required).' },
  { g: 'Templates', t: 'Delete templates', m: 'DELETE', path: '/organisation/{orgId}/QrCodeTemplate/deleteQrCodeTemplate',
    body: { type: 'json', ex: ['TPL1', 'TPL2'] }, desc: 'Body = List<String> of template ids.' },
  { g: 'Templates', t: 'Set in-use / default template', m: 'PUT', path: '/organisation/{orgId}/QrCodeTemplate/updateInUseByUrl',
    q: [{ n: 'templateUrl' }, { n: 'isDefault', d: 'false' }], desc: 'Select active template by URL.' },

  // ---- ADC tagging ----
  { g: 'ADC Tagging', t: 'Tag QR codes by org + vdms (touchscreen)', m: 'PUT',
    path: '/touchscreen/qrCode/organisation/{orgId}/vdms/{vdmsId}/tagQrCodeByVdmsId',
    body: { type: 'json', ex: [{ id: 'QR1', deviceId: 'DEV1', locationId: 'LOC1' }] }, desc: 'Body = List<QrCodeDTO>.' },
]

const GROUPS = [...new Set(EP.map((e) => e.g))]
const pathVars = (p) => [...p.matchAll(/\{(\w+)\}/g)].map((m) => m[1])

export default function QrCodesPage() {
  const { ctx } = useApp()
  const [scope, setScope] = useState({
    orgId: ctx?.vdmsId || DEMO.vdmsId,
    email: `${ctx?.user || DEMO.user}@sclera.com`,
    loggedInUser: ctx?.user || DEMO.user,
    vdmsId: ctx?.vdmsId || DEMO.vdmsId,
    username: ctx?.user || DEMO.user,
    deviceId: '',
    locationId: '',
  })
  const [scopeVersion, setScopeVersion] = useState(0)
  const [activeGroup, setActiveGroup] = useState(GROUPS[0])

  const scopeVal = useMemo(() => (name) => {
    if (name === 'vdmsid') return scope.vdmsId
    return scope[name] ?? ''
  }, [scope])

  const setScopeField = (k, v) => setScope((s) => ({ ...s, [k]: v }))

  return (
    <div className="page">
      <div className="page-head">
        <div>
          <h1>QR Codes <span className="muted">· test console</span></h1>
          <p className="muted" style={{ margin: '2px 0 0', fontSize: 12 }}>
            Calls go to <code>{QR_BASE}</code>
          </p>
        </div>
      </div>

      {/* Scope / common values */}
      <div className="card" style={{ padding: 14, marginBottom: 14 }}>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill,minmax(180px,1fr))', gap: 10 }}>
          {['orgId', 'email', 'loggedInUser', 'vdmsId', 'username', 'deviceId', 'locationId'].map((k) => (
            <label className="fld" key={k}>
              <span>{k}</span>
              <input value={scope[k]} onChange={(e) => setScopeField(k, e.target.value)} />
            </label>
          ))}
        </div>
        <div style={{ marginTop: 10 }}>
          <button className="btn btn-ghost sm" onClick={() => setScopeVersion((v) => v + 1)}>
            <Icon name="refresh" size={13} /> Apply scope to forms
          </button>
          <span className="muted" style={{ marginLeft: 10, fontSize: 12 }}>
            Re-fills every form's matching fields with the values above.
          </span>
        </div>
      </div>

      {/* group tabs */}
      <div className="status-chips" style={{ marginBottom: 12 }}>
        {GROUPS.map((g) => (
          <button key={g} className={`chip ${activeGroup === g ? 'active' : ''}`} onClick={() => setActiveGroup(g)}>
            {g} <span className="muted">({EP.filter((e) => e.g === g).length})</span>
          </button>
        ))}
      </div>

      {EP.map((e, i) => e.g === activeGroup && (
        <EndpointCard key={`${i}-${scopeVersion}`} ep={e} scopeVal={scopeVal} />
      ))}
    </div>
  )
}

function EndpointCard({ ep, scopeVal }) {
  const pvs = pathVars(ep.path)
  const initVals = () => {
    const v = {}
    pvs.forEach((pv) => { v[pv] = scopeVal(pv) || (ep.pd && ep.pd[pv]) || '' })
    ;(ep.q || []).forEach((q) => { v[q.n] = scopeVal(q.n) || q.d || '' })
    ;(ep.multipart || []).forEach((mp) => { if (mp.type === 'text') v[mp.n] = mp.ex || '' })
    return v
  }
  const [vals, setVals] = useState(initVals)
  const [files, setFiles] = useState({})
  const [bodyText, setBodyText] = useState(
    ep.body ? (ep.body.type === 'json' ? JSON.stringify(ep.body.ex, null, 2) : ep.body.ex || '') : ''
  )
  const [open, setOpen] = useState(false)
  const [resp, setResp] = useState(null)
  const [busy, setBusy] = useState(false)

  const setVal = (k, v) => setVals((s) => ({ ...s, [k]: v }))

  const buildUrl = () => {
    let p = ep.path
    pvs.forEach((pv) => { p = p.replace(`{${pv}}`, encodeURIComponent(vals[pv] || '')) })
    const parts = []
    ;(ep.q || []).forEach((q) => {
      const v = (vals[q.n] ?? '').trim()
      if (v === '') return
      if (ep.list && ep.list.includes(q.n)) {
        v.split(',').map((s) => s.trim()).filter(Boolean).forEach((it) => parts.push(`${encodeURIComponent(q.n)}=${encodeURIComponent(it)}`))
      } else {
        parts.push(`${encodeURIComponent(q.n)}=${encodeURIComponent(v)}`)
      }
    })
    return QR_BASE + p + (parts.length ? `?${parts.join('&')}` : '')
  }

  const send = async () => {
    const url = buildUrl()
    if (ep.download) {
      setResp({ kind: 'download', url })
      const a = document.createElement('a'); a.href = url; a.target = '_blank'; a.rel = 'noopener'
      document.body.appendChild(a); a.click(); a.remove()
      return
    }
    const opt = { method: ep.m, mode: 'cors', headers: {} }
    if (ep.multipart) {
      const fd = new FormData()
      ep.multipart.forEach((mp) => {
        if (mp.type === 'file') { if (files[mp.n]) fd.append(mp.n, files[mp.n]) }
        else fd.append(mp.n, vals[mp.n] ?? '')
      })
      opt.body = fd // browser sets multipart boundary
    } else if (ep.body?.type === 'json') {
      try { JSON.parse(bodyText) } catch (err) { setResp({ kind: 'err', text: `Invalid JSON: ${err.message}` }); return }
      opt.headers['Content-Type'] = 'application/json'; opt.body = bodyText
    } else if (ep.body?.type === 'string') {
      opt.headers['Content-Type'] = 'text/plain'; opt.body = bodyText
    }

    setBusy(true)
    const t0 = performance.now()
    try {
      const res = await fetch(url, opt)
      const ms = Math.round(performance.now() - t0)
      const ct = res.headers.get('content-type') || ''
      let text
      if (ct.includes('application/json')) { try { text = JSON.stringify(await res.json(), null, 2) } catch { text = await res.text() } }
      else { text = await res.text(); if (text === '') text = '(empty body)' }
      setResp({ kind: res.ok ? 'ok' : 'http', status: res.status, statusText: res.statusText, ms, ct, text, url })
    } catch (err) {
      setResp({ kind: 'err', text: `${err.message}\n\n(If CORS/network: the dev server must run on http://localhost:3000 — the gateway only allows that origin.)`, url })
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="card" style={{ marginBottom: 12 }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: 10, padding: '10px 14px', cursor: 'pointer' }} onClick={() => setOpen((o) => !o)}>
        <span className={`badge tone-${verbTone(ep.m)}`} style={{ minWidth: 56, textAlign: 'center' }}>{ep.m}</span>
        <code style={{ fontSize: 12.5 }}>{ep.path}</code>
        <span className="muted" style={{ marginLeft: 'auto', fontSize: 12, textAlign: 'right' }}>{ep.desc}</span>
        <span className="muted" aria-hidden style={{ fontSize: 12 }}>{open ? '▾' : '▸'}</span>
      </div>

      {open && (
        <div style={{ padding: '0 14px 14px', borderTop: '1px solid var(--border, #2d3a48)' }}>
          {(pvs.length > 0 || (ep.q && ep.q.length > 0) || ep.multipart) && (
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill,minmax(200px,1fr))', gap: 10, margin: '12px 0' }}>
              {pvs.map((pv) => (
                <label className="fld" key={`p-${pv}`}><span>{pv} <em className="muted">(path)</em></span>
                  <input value={vals[pv] || ''} onChange={(e) => setVal(pv, e.target.value)} /></label>
              ))}
              {(ep.q || []).map((q) => (
                <label className="fld" key={`q-${q.n}`}>
                  <span>{q.n}{q.r ? ' *' : ''} {q.hint ? <em className="muted">{q.hint}</em> : null}</span>
                  <input value={vals[q.n] || ''} placeholder={q.hint || ''} onChange={(e) => setVal(q.n, e.target.value)} />
                </label>
              ))}
              {(ep.multipart || []).map((mp) => mp.type === 'file' ? (
                <label className="fld" key={`f-${mp.n}`}><span>{mp.n} <em className="muted">(file)</em></span>
                  <input type="file" onChange={(e) => setFiles((s) => ({ ...s, [mp.n]: e.target.files[0] }))} /></label>
              ) : (
                <label className="fld" key={`t-${mp.n}`}><span>{mp.n} <em className="muted">(text part)</em></span>
                  <input value={vals[mp.n] || ''} onChange={(e) => setVal(mp.n, e.target.value)} /></label>
              ))}
            </div>
          )}

          {ep.body && (
            <label className="fld" style={{ display: 'block', margin: '4px 0 10px' }}>
              <span>{ep.body.type === 'json' ? 'Request body (JSON)' : 'Request body (raw string)'}</span>
              <textarea value={bodyText} onChange={(e) => setBodyText(e.target.value)} rows={6}
                style={{ width: '100%', fontFamily: 'ui-monospace, Menlo, Consolas, monospace', fontSize: 12 }} />
            </label>
          )}

          <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
            <button className="btn btn-primary sm" onClick={send} disabled={busy}>
              {busy ? 'Sending…' : ep.download ? 'Send & download' : 'Send'}
            </button>
            <button className="btn btn-ghost sm" onClick={() => navigator.clipboard.writeText(buildUrl())}>Copy URL</button>
          </div>

          {resp && (
            <div style={{ marginTop: 10, border: '1px solid var(--border, #2d3a48)', borderRadius: 8, overflow: 'hidden' }}>
              <div style={{ padding: '6px 10px', display: 'flex', gap: 12, alignItems: 'center', fontSize: 12, borderBottom: '1px solid var(--border, #2d3a48)' }}>
                {resp.kind === 'download' && <span className="badge tone-online">DOWNLOAD</span>}
                {resp.kind === 'ok' && <span className="badge tone-online">{resp.status} {resp.statusText}</span>}
                {resp.kind === 'http' && <span className="badge tone-offline">{resp.status} {resp.statusText}</span>}
                {resp.kind === 'err' && <span className="badge tone-offline">ERROR</span>}
                {resp.ms != null && <span className="muted">{resp.ms} ms · {resp.ct || 'no content-type'}</span>}
                {resp.url && <code className="muted" style={{ fontSize: 11 }}>{resp.url}</code>}
              </div>
              <pre style={{ margin: 0, padding: 10, maxHeight: 360, overflow: 'auto', fontSize: 12, whiteSpace: 'pre-wrap', wordBreak: 'break-word' }}>
                {resp.kind === 'download' ? 'Opened download in a new tab…' : resp.text}
              </pre>
            </div>
          )}
        </div>
      )}
    </div>
  )
}

function verbTone(m) {
  return { GET: 'online', POST: 'default', PUT: 'default', DELETE: 'offline' }[m] || 'default'
}
