# Work Orders UI Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a full Work Orders (tickets) UI to `sclera-ui` — a dedicated page with CRUD, filters, search, pagination, and a detail+history drawer — plus real per-device tickets in the asset panel, wired through the gateway to the rebuilt `sclera-workorders` service, running in the existing Docker stack with Dapr sidecars in sync.

**Architecture:** Pure frontend feature in the existing Vite+React `sclera-ui` (sidebar + `view`-switch pattern, central `services/api.js`, ToastContext). The backend ticket API already exists in source but the running container is the old stub, so Task 1 rebuilds that image. The UI talks to the gateway route `/workorders/**` → `sclera-workorders:8094` (StripPrefix=1).

**Tech Stack:** React 18 + Vite, plain `fetch` via `services/api.js`, CSS classes from `styles/global.css`, Docker Compose, Dapr.

## Global Constraints

- Workorders endpoints require query params **`loggedInUser`** and **`vdms_id`** (400 if missing) — NOT the device API's `username`/`vdmsid`.
- Demo scope (from `config.js` `DEMO`): user `admin`, vdmsId `VDMS760`, docker `right_wing`.
- Gateway base: `http://localhost:8080`; workorders path prefix: `/workorders/api/v1/workorder-service`.
- Canonical ticket statuses: `new`, `open`, `on_hold`, `closed` (count map also has `all`).
- `TicketDTO.type` is service-computed — never send it from a form.
- The browser origin must stay `http://localhost:3000` (only origin in the gateway CORS allow-list).
- After any rebuild/recreate of an app container, force-recreate its `*-dapr` sidecar (`network_mode: service:<app>`).
- **Commits are the user's responsibility** (standing "never commit" preference) — do not run `git commit`/`git push`. Each task ends with a verification checkpoint instead.

---

### Task 1: Rebuild `sclera-workorders` and bring the real ticket API live

**Files:**
- None modified (image rebuild only). Run from repo root.

**Interfaces:**
- Produces: live endpoints under `http://localhost:8080/workorders/api/v1/workorder-service/ticket/*`, consumed by Task 2.

- [ ] **Step 1: Confirm the deployed container is the old stub (test-first)**

Run:
```bash
curl -s -o /dev/null -w "%{http_code}" -X POST "http://localhost:8080/workorders/api/v1/workorder-service/ticket/getticketcount?loggedInUser=admin&vdms_id=VDMS760" -H "Content-Type: application/json" -d "{}"
```
Expected: `404` (old stub has no context-path / no ticket mappings).

- [ ] **Step 2: Rebuild the workorders image**

Run:
```bash
docker compose build sclera-workorders
```
Expected: build completes (Maven build inside the Dockerfile succeeds).

- [ ] **Step 3: Recreate the app container and its Dapr sidecar (in sync)**

Run:
```bash
docker compose up -d sclera-workorders
docker compose up -d --force-recreate sclera-workorders-dapr
```
Expected: both `sclera-workorders` and `sclera-cloud-device-asset-sclera-workorders-dapr-1` report `Started`/`Running`.

- [ ] **Step 4: Verify the real API answers through the gateway**

Run:
```bash
curl -s -X POST "http://localhost:8080/workorders/api/v1/workorder-service/ticket/getticketcount?loggedInUser=admin&vdms_id=VDMS760" -H "Content-Type: application/json" -d "{}"
```
Expected: `200` with a JSON object containing keys `all`, `new`, `open`, `on_hold`, `closed` (e.g. `{"all":0,"new":0,"open":0,"on_hold":0,"closed":0}`).

- [ ] **Step 5: Verify list endpoint and Dapr sidecar health**

Run:
```bash
curl -s -X POST "http://localhost:8080/workorders/api/v1/workorder-service/ticket/getalltickets?loggedInUser=admin&vdms_id=VDMS760&pageno=1&pagesize=10" -H "Content-Type: application/json" -d "{}"
docker logs sclera-workorders --since 60s 2>&1 | grep -c "127.0.0.1:350" || true
```
Expected: list returns `200` (a JSON array, possibly empty `[]`); the grep count is `0` (sidecar reachable). If the count is non-zero, re-run Step 3's sidecar recreate.

- [ ] **Step 6: Checkpoint**

Confirm: `getticketcount` and `getalltickets` both return `200` via the gateway, and `docker ps` shows every `*-dapr` container `Up`. Do not commit (image rebuild only).

---

### Task 2: Add workorders methods to the API client

**Files:**
- Modify: `sclera-ui/src/services/api.js`

**Interfaces:**
- Consumes: existing `request`, `qs`, `BASE_URL`, `DEMO` in the file.
- Produces (used by Tasks 3–7):
  - `api.ticketCounts(filter = {})` → `Promise<{all,new,open,on_hold,closed,...}>`
  - `api.listTickets(filter = {}, { searchkey='null', pageno=1, pagesize=10 } = {})` → `Promise<TicketDTO[]>`
  - `api.getTicket(id)` → `Promise<TicketDTO>`
  - `api.upsertTicket(dto)` → `Promise<null>`
  - `api.deleteTicket(id)` → `Promise<null>`
  - `api.ticketHistory(id)` → `Promise<TicketHistoryDTO[]>`
  - `api.ticketsByDevice(deviceId, filter = {}, { pageno=1, pagesize=50 } = {})` → `Promise<TicketDTO[]>`
  - where `TicketDTO` = `{ id, number, name, status, type, category, request_type, description, user_message, assignee_user_email, device_id, docker_name, created_by, created_at, updated_at, closed_at }` and `TicketHistoryDTO` = `{ id, message, status, action_message, ticket_number, created_by, ticket_id, timestamp }`.

- [ ] **Step 1: Add the URL builder and scope helper**

In `sclera-ui/src/services/api.js`, just after the existing `const integrations = (p) => ...` line (around line 60), add:
```js
// Workorders service (gateway /workorders/** route, StripPrefix=1). NOTE: this service
// uses loggedInUser + vdms_id query params, NOT the device API's username/vdmsid.
const workorder = (p) => `${BASE_URL}/workorders/api/v1/workorder-service${p}`
function woScope({ user = DEMO.user, vdmsId = DEMO.vdmsId } = {}) {
  return { loggedInUser: user, vdms_id: vdmsId }
}
```

- [ ] **Step 2: Add the ticket methods to the `api` object**

Inside the exported `api = { ... }` object, before the closing `}` (after the `addLocation` entry), add:
```js
  // ---- Work Orders (tickets) — sclera-workorders service ----
  ticketCounts: (filter = {}, ctx = {}) =>
    request(workorder(`/ticket/getticketcount${qs(woScope(ctx))}`), { method: 'POST', body: filter }),

  listTickets: (filter = {}, { searchkey = 'null', pageno = 1, pagesize = 10, ...ctx } = {}) =>
    request(workorder(`/ticket/getalltickets${qs({ ...woScope(ctx), searchkey, pageno, pagesize })}`), { method: 'POST', body: filter }),

  getTicket: (id, ctx = {}) =>
    request(workorder(`/ticket/${encodeURIComponent(id)}/getticketdetailsbyid${qs(woScope(ctx))}`)),

  upsertTicket: (dto, ctx = {}) =>
    request(workorder(`/ticket/upsertticket${qs(woScope(ctx))}`), { method: 'POST', body: dto }),

  deleteTicket: (id, ctx = {}) =>
    request(workorder(`/ticket/${encodeURIComponent(id)}/deleteticket${qs(woScope(ctx))}`), { method: 'DELETE' }),

  ticketHistory: (id, ctx = {}) =>
    request(workorder(`/ticket/${encodeURIComponent(id)}/gettickethistory${qs(woScope(ctx))}`)),

  ticketsByDevice: (deviceId, filter = {}, { pageno = 1, pagesize = 50, ...ctx } = {}) =>
    request(workorder(`/ticket/device/${encodeURIComponent(deviceId)}/getallticketsbydeviceid${qs({ ...woScope(ctx), pageno, pagesize })}`), { method: 'POST', body: filter }),
```

- [ ] **Step 3: Verify the module parses and methods exist**

Run:
```bash
cd "sclera-ui" && node -e "import('./src/services/api.js').then(m=>console.log(['ticketCounts','listTickets','getTicket','upsertTicket','deleteTicket','ticketHistory','ticketsByDevice'].map(k=>k+':'+typeof m.default[k]).join(' ')))"
```
Expected: each prints `:function` (e.g. `ticketCounts:function listTickets:function ...`). If Node can't resolve the `import.meta.env` in `config.js`, instead verify with `npx vite build` in Step (Task 8) — but the simplest check is `node --input-type=module -e "..."`; if it errors on env, skip and rely on the Task 8 build.

- [ ] **Step 4: Checkpoint**

Confirm the seven methods are present in `api.js` and the file has no syntax error (editor/`npx eslint src/services/api.js` if available). Do not commit.

---

### Task 3: Add nav + the Work Orders page shell (counts + list)

**Files:**
- Modify: `sclera-ui/src/components/Icon.jsx` (add `clipboard` glyph)
- Modify: `sclera-ui/src/config.js` (add nav item + status constants)
- Modify: `sclera-ui/src/components/Sidebar.jsx` (route the nav key)
- Modify: `sclera-ui/src/App.jsx` (render the view)
- Create: `sclera-ui/src/pages/WorkOrdersPage.jsx`

**Interfaces:**
- Consumes: `api.ticketCounts`, `api.listTickets` (Task 2); `useApp`, `useToast`, `Icon`, `MetricCard`.
- Produces: a `WorkOrdersPage` default export rendering when `view === 'workorders'`; module-level constants `TICKET_STATUSES` and `statusTone` exported from `config.js` (used by Tasks 4–7).

- [ ] **Step 1: Add a `clipboard` icon**

In `sclera-ui/src/components/Icon.jsx`, add to the `paths` object (after the `'arrow-right'` entry):
```js
  clipboard: <><path d="M9 2h6a1 1 0 0 1 1 1v1h2a2 2 0 0 1 2 2v13a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2h2V3a1 1 0 0 1 1-1z" {...P} /><path d="M9 4h6" {...P} /><path d="M8 11h8M8 15h5" {...P} /></>,
```

- [ ] **Step 2: Add the nav item and shared status constants to `config.js`**

In `sclera-ui/src/config.js`, add `Work Orders` to `NAV_ITEMS` (after the `onboarding` entry):
```js
  { key: 'workorders', label: 'Work Orders', icon: 'clipboard' },
```
Then add at the end of the file:
```js
// Ticket status vocabulary (matches the workorders service count-map keys).
export const TICKET_STATUSES = ['new', 'open', 'on_hold', 'closed']
export const statusLabel = (s) => ({ new: 'New', open: 'Open', on_hold: 'On Hold', closed: 'Closed' }[s] || s || '—')
export const statusTone = (s) => ({ new: 'default', open: 'online', on_hold: 'default', closed: 'muted' }[s] || 'default')
```

- [ ] **Step 3: Route the nav key in the sidebar**

In `sclera-ui/src/components/Sidebar.jsx`, inside `go(key)`, add after the `onboarding` line:
```js
    if (key === 'workorders') setView('workorders')
```

- [ ] **Step 4: Create the page shell**

Create `sclera-ui/src/pages/WorkOrdersPage.jsx`:
```jsx
import { useCallback, useEffect, useState } from 'react'
import Icon from '../components/Icon.jsx'
import MetricCard from '../components/MetricCard.jsx'
import { Skeleton } from '../components/Skeleton.jsx'
import { useApp } from '../context/AppContext.jsx'
import { useToast } from '../context/ToastContext.jsx'
import { statusLabel, statusTone } from '../config.js'
import api from '../services/api.js'

const fmtDate = (ts) => {
  const n = Number(ts); if (!n) return '—'
  return new Date(n).toLocaleString(undefined, { day: '2-digit', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit' })
}

export default function WorkOrdersPage() {
  const { ctx } = useApp()
  const toast = useToast()
  const [counts, setCounts] = useState(null)
  const [tickets, setTickets] = useState(null)

  const load = useCallback(async () => {
    setTickets(null)
    try {
      const [c, list] = await Promise.all([
        api.ticketCounts({}, ctx),
        api.listTickets({}, { ...ctx, pageno: 1, pagesize: 50 }),
      ])
      setCounts(c || {})
      setTickets(Array.isArray(list) ? list : [])
    } catch (e) {
      toast.error(`Load work orders failed: ${e.message}`)
      setCounts({}); setTickets([])
    }
  }, [ctx, toast])

  useEffect(() => { load() }, [load])

  const metricItems = [
    { label: 'All', value: counts?.all, tone: 'default' },
    { label: 'New', value: counts?.new, tone: 'default' },
    { label: 'Open', value: counts?.open, tone: 'online' },
    { label: 'On Hold', value: counts?.on_hold, tone: 'default' },
    { label: 'Closed', value: counts?.closed, tone: 'muted' },
  ]

  return (
    <div className="wo-page">
      <div className="page-head">
        <h1>Work Orders</h1>
        <button className="btn btn-ghost sm" onClick={load} title="Refresh"><Icon name="refresh" size={14} /> Refresh</button>
      </div>

      <div className="metric-grid">
        <MetricCard title="Tickets" items={metricItems} accent="blue" />
      </div>

      <div className="card">
        {tickets == null ? (
          <div className="log-list"><Skeleton w="100%" h={44} /><Skeleton w="100%" h={44} /><Skeleton w="100%" h={44} /></div>
        ) : tickets.length === 0 ? (
          <div className="empty-state"><Icon name="clipboard" size={22} /><p>No work orders yet</p></div>
        ) : (
          <table className="data-table">
            <thead><tr><th>Number</th><th>Name</th><th>Status</th><th>Category</th><th>Device</th><th>Assignee</th><th>Created</th></tr></thead>
            <tbody>
              {tickets.map((t) => (
                <tr key={t.id}>
                  <td>{t.number || '—'}</td>
                  <td>{t.name || '—'}</td>
                  <td><span className={`badge tone-${statusTone(t.status)}`}>{statusLabel(t.status)}</span></td>
                  <td>{t.category || '—'}</td>
                  <td>{t.device_id || '—'}</td>
                  <td>{t.assignee_user_email || '—'}</td>
                  <td>{fmtDate(t.created_at)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  )
}
```

- [ ] **Step 5: Render the page in `App.jsx`**

In `sclera-ui/src/App.jsx`, add the import after the `AssetOnboardingPage` import:
```jsx
import WorkOrdersPage from './pages/WorkOrdersPage.jsx'
```
And add the view branch inside `<ErrorBoundary>`, after the `onboarding` line:
```jsx
            {view === 'workorders' && <WorkOrdersPage />}
```

- [ ] **Step 6: Verify in the browser (dev server)**

Run:
```bash
cd "sclera-ui" && npm install && npm run dev
```
Open `http://localhost:5173` (Vite dev). Set the API base if needed via `.env` (`VITE_API_BASE=http://localhost:8080`). Click "Work Orders" in the sidebar.
Expected: the page renders the "Tickets" metric card (All/New/Open/On Hold/Closed counts from the live service) and either an empty state or a table of tickets. No console errors.

> Note: against the dev server the browser origin is `:5173`, which is not in the gateway CORS allow-list. For a CORS-clean check, rely on the containerized verification in Task 8 (origin `:3000`). The dev-server run here is for layout/console-error verification; if CORS blocks the data, confirm the layout renders and defer data verification to Task 8.

- [ ] **Step 7: Checkpoint**

Confirm the nav item appears, clicking it shows the Work Orders page with the metric card and table/empty-state, and no render errors. Do not commit.

---

### Task 4: Create/edit ticket modal

**Files:**
- Create: `sclera-ui/src/components/TicketModal.jsx`
- Modify: `sclera-ui/src/pages/WorkOrdersPage.jsx` (add "New Work Order" button + modal state)

**Interfaces:**
- Consumes: `api.upsertTicket` (Task 2); `TICKET_STATUSES`/`statusLabel` (Task 3); `Icon`, `useToast`.
- Produces: `TicketModal` default export with props `{ open, ticket, deviceId, dockerName, onClose, onSaved }` — `onSaved` is called after a successful upsert; used by Tasks 4 (page), 5 (drawer), 7 (device panel).

- [ ] **Step 1: Create the modal**

Create `sclera-ui/src/components/TicketModal.jsx`:
```jsx
import { useEffect, useState } from 'react'
import Icon from './Icon.jsx'
import { Spinner } from './Skeleton.jsx'
import { useToast } from '../context/ToastContext.jsx'
import { useApp } from '../context/AppContext.jsx'
import { TICKET_STATUSES, statusLabel } from '../config.js'
import api from '../services/api.js'

const BLANK = { name: '', status: 'new', category: '', request_type: '', assignee_user_email: '', user_message: '', description: '', device_id: '' }

export default function TicketModal({ open, ticket, deviceId, dockerName, onClose, onSaved }) {
  const { ctx } = useApp()
  const toast = useToast()
  const [form, setForm] = useState(BLANK)
  const [busy, setBusy] = useState(false)

  useEffect(() => {
    if (!open) return
    setForm({ ...BLANK, ...(ticket || {}), device_id: (ticket?.device_id ?? deviceId) || '' })
  }, [open, ticket, deviceId])

  if (!open) return null
  const set = (k) => (e) => setForm((f) => ({ ...f, [k]: e.target.value }))
  const deviceLocked = !!deviceId && !ticket?.id

  const save = async () => {
    if (!form.name.trim()) { toast.error('Name is required'); return }
    setBusy(true)
    try {
      const dto = { ...ticket, ...form }
      if (dockerName && !dto.docker_name) dto.docker_name = dockerName
      // type is service-computed — never send it
      delete dto.type
      Object.keys(dto).forEach((k) => { if (dto[k] === '') delete dto[k] })
      await api.upsertTicket(dto, ctx)
      toast.success(ticket?.id ? 'Work order updated' : 'Work order created')
      onSaved?.()
      onClose?.()
    } catch (e) {
      toast.error(`Save failed: ${e.message}`)
    } finally { setBusy(false) }
  }

  return (
    <div className="modal-backdrop" onClick={onClose}>
      <div className="modal" onClick={(e) => e.stopPropagation()}>
        <div className="modal-head">
          <h3>{ticket?.id ? 'Edit Work Order' : 'New Work Order'}</h3>
          <button className="icon-btn" onClick={onClose}><Icon name="x" size={16} /></button>
        </div>
        <div className="modal-body">
          <label className="fld"><span>Name *</span><input value={form.name} onChange={set('name')} /></label>
          <label className="fld"><span>Status</span>
            <select value={form.status} onChange={set('status')}>
              {TICKET_STATUSES.map((s) => <option key={s} value={s}>{statusLabel(s)}</option>)}
            </select>
          </label>
          <label className="fld"><span>Category</span><input value={form.category} onChange={set('category')} /></label>
          <label className="fld"><span>Request Type</span><input value={form.request_type} onChange={set('request_type')} /></label>
          <label className="fld"><span>Assignee email</span><input type="email" value={form.assignee_user_email} onChange={set('assignee_user_email')} /></label>
          <label className="fld"><span>Device ID</span><input value={form.device_id} onChange={set('device_id')} disabled={deviceLocked} /></label>
          <label className="fld"><span>User message</span><textarea rows={2} value={form.user_message} onChange={set('user_message')} /></label>
          <label className="fld"><span>Description</span><textarea rows={3} value={form.description} onChange={set('description')} /></label>
        </div>
        <div className="modal-foot">
          <button className="btn btn-ghost" onClick={onClose} disabled={busy}>Cancel</button>
          <button className="btn btn-primary" onClick={save} disabled={busy}>{busy ? <Spinner size={13} /> : null} Save</button>
        </div>
      </div>
    </div>
  )
}
```

- [ ] **Step 2: Wire "New Work Order" into the page**

In `sclera-ui/src/pages/WorkOrdersPage.jsx`, add the import:
```jsx
import TicketModal from '../components/TicketModal.jsx'
```
Add modal state inside the component (near the other `useState`):
```jsx
  const [modal, setModal] = useState(null) // null | { ticket } for create/edit
```
Add a "New Work Order" button in `.page-head` (before the Refresh button):
```jsx
        <button className="btn btn-primary sm" onClick={() => setModal({})}><Icon name="plus" size={14} /> New Work Order</button>
```
Render the modal at the end of the returned JSX (before the final closing `</div>`):
```jsx
      <TicketModal
        open={!!modal}
        ticket={modal?.ticket}
        onClose={() => setModal(null)}
        onSaved={load}
      />
```

- [ ] **Step 3: Verify create round-trip (Task 8 stack, or dev if CORS allows)**

After Task 8's container build (or now if testing against `:3000`): open Work Orders, click "New Work Order", fill Name = `Test WO 1`, Status = `Open`, Category = `HVAC`, Save.
Expected: success toast, modal closes, the list refreshes and shows `Test WO 1` with an `Open` badge; the "All"/"Open" counts increment.

- [ ] **Step 4: Checkpoint**

Confirm a ticket can be created and appears in the list. Do not commit.

---

### Task 5: Ticket detail + history drawer (with edit/delete)

**Files:**
- Create: `sclera-ui/src/components/TicketDetailDrawer.jsx`
- Modify: `sclera-ui/src/pages/WorkOrdersPage.jsx` (row click opens the drawer; wire edit→modal, delete→refresh)

**Interfaces:**
- Consumes: `api.getTicket`, `api.ticketHistory`, `api.deleteTicket` (Task 2); `TicketModal` (Task 4); `statusLabel`/`statusTone`.
- Produces: `TicketDetailDrawer` default export with props `{ ticketId, onClose, onChanged, onEdit }` — `onChanged` fires after delete; `onEdit(ticket)` asks the parent to open the edit modal.

- [ ] **Step 1: Create the drawer**

Create `sclera-ui/src/components/TicketDetailDrawer.jsx`:
```jsx
import { useEffect, useState } from 'react'
import Icon from './Icon.jsx'
import { Skeleton } from './Skeleton.jsx'
import { useApp } from '../context/AppContext.jsx'
import { useToast } from '../context/ToastContext.jsx'
import { statusLabel, statusTone } from '../config.js'
import api from '../services/api.js'

const fmt = (ts) => { const n = Number(ts); return n ? new Date(n).toLocaleString() : '—' }

export default function TicketDetailDrawer({ ticketId, onClose, onChanged, onEdit }) {
  const { ctx } = useApp()
  const toast = useToast()
  const [ticket, setTicket] = useState(null)
  const [history, setHistory] = useState(null)
  const [busy, setBusy] = useState(false)

  useEffect(() => {
    if (!ticketId) return
    setTicket(null); setHistory(null)
    api.getTicket(ticketId, ctx).then(setTicket).catch((e) => { toast.error(e.message); onClose?.() })
    api.ticketHistory(ticketId, ctx).then((h) => setHistory(Array.isArray(h) ? h : [])).catch(() => setHistory([]))
  }, [ticketId]) // eslint-disable-line react-hooks/exhaustive-deps

  if (!ticketId) return null

  const remove = async () => {
    if (!window.confirm('Delete this work order?')) return
    setBusy(true)
    try { await api.deleteTicket(ticketId, ctx); toast.success('Work order deleted'); onChanged?.(); onClose?.() }
    catch (e) { toast.error(`Delete failed: ${e.message}`) }
    finally { setBusy(false) }
  }

  return (
    <div className="drawer-backdrop" onClick={onClose}>
      <aside className="drawer" onClick={(e) => e.stopPropagation()}>
        <div className="drawer-head">
          <div>
            <div className="drawer-title">{ticket?.name || ticket?.number || 'Work Order'}</div>
            {ticket?.status && <span className={`badge tone-${statusTone(ticket.status)}`}>{statusLabel(ticket.status)}</span>}
          </div>
          <div className="drawer-actions">
            <button className="icon-btn" disabled={!ticket} onClick={() => onEdit?.(ticket)} title="Edit"><Icon name="edit" size={15} /></button>
            <button className="icon-btn danger" disabled={busy} onClick={remove} title="Delete"><Icon name="trash" size={15} /></button>
            <button className="icon-btn" onClick={onClose} title="Close"><Icon name="x" size={16} /></button>
          </div>
        </div>

        {ticket == null ? <div className="dp-fields"><Skeleton w="100%" h={40} /><Skeleton w="100%" h={40} /></div> : (
          <div className="dp-fields">
            <Row k="Number" v={ticket.number} />
            <Row k="Category" v={ticket.category} />
            <Row k="Request Type" v={ticket.request_type} />
            <Row k="Device" v={ticket.device_id} />
            <Row k="Assignee" v={ticket.assignee_user_email} />
            <Row k="Created" v={`${fmt(ticket.created_at)}${ticket.created_by ? ' · ' + ticket.created_by : ''}`} />
            <Row k="Message" v={ticket.user_message} />
            <Row k="Description" v={ticket.description} />
          </div>
        )}

        <div className="dp-section">
          <div className="dp-section-title">History</div>
          {history == null ? <div className="log-list"><Skeleton w="100%" h={36} /></div>
            : history.length === 0 ? <div className="empty-state"><Icon name="list" size={20} /><p>No history</p></div>
            : <div className="log-list">{history.map((h) => (
                <div className="log-item" key={h.id}>
                  <span className={`badge tone-${statusTone(h.status)}`}>{statusLabel(h.status)}</span>
                  <div className="log-main"><div className="log-msg">{h.message || h.action_message}</div>
                    <div className="log-meta">{h.created_by || '—'} · {fmt(h.timestamp)}</div></div>
                </div>
              ))}</div>}
        </div>
      </aside>
    </div>
  )
}
function Row({ k, v }) { return <div className="dp-row"><span className="dp-k">{k}</span><span className="dp-v">{v || '—'}</span></div> }
```

- [ ] **Step 2: Wire row click + edit-from-drawer in the page**

In `sclera-ui/src/pages/WorkOrdersPage.jsx`, add the import:
```jsx
import TicketDetailDrawer from '../components/TicketDetailDrawer.jsx'
```
Add drawer state:
```jsx
  const [openId, setOpenId] = useState(null)
```
Make table rows clickable — change the `<tr key={t.id}>` to:
```jsx
                <tr key={t.id} className="clickable" onClick={() => setOpenId(t.id)}>
```
Render the drawer just before the `<TicketModal ... />` element:
```jsx
      <TicketDetailDrawer
        ticketId={openId}
        onClose={() => setOpenId(null)}
        onChanged={load}
        onEdit={(t) => { setOpenId(null); setModal({ ticket: t }) }}
      />
```

- [ ] **Step 3: Verify detail/edit/delete (Task 8 stack)**

Click a ticket row → drawer opens with detail + History section. Click Edit → modal opens pre-filled; change Status to `On Hold`, Save → list + drawer reflect it (re-open to confirm). Click Delete → confirm → toast, drawer closes, row gone, counts drop.
Expected: all three round-trip with success toasts and the history shows entries for create/status-change/close transitions.

- [ ] **Step 4: Checkpoint**

Confirm detail drawer, history, edit, and delete all work. Do not commit.

---

### Task 6: Filters, search, and pagination

**Files:**
- Modify: `sclera-ui/src/pages/WorkOrdersPage.jsx`

**Interfaces:**
- Consumes: `api.listTickets`/`api.ticketCounts` with filter body + paging (Task 2); `TICKET_STATUSES`/`statusLabel` (Task 3).
- Produces: no new exports; richer `load` that sends `TicketFilterDTO` `{ status?: string[], category?, assignee_user_email?, device_id? }` + `searchkey` + `pageno`/`pagesize`.

- [ ] **Step 1: Add filter/search/page state**

In `sclera-ui/src/pages/WorkOrdersPage.jsx`, add state:
```jsx
  const [filter, setFilter] = useState({ status: [], category: '', assignee_user_email: '', device_id: '' })
  const [search, setSearch] = useState('')
  const [page, setPage] = useState(1)
  const PAGE_SIZE = 10
```

- [ ] **Step 2: Make `load` honor filters, search, and page**

Replace the `load` callback body's API calls with:
```jsx
  const load = useCallback(async () => {
    setTickets(null)
    const body = {}
    if (filter.status.length) body.status = filter.status
    if (filter.category.trim()) body.category = filter.category.trim()
    if (filter.assignee_user_email.trim()) body.assignee_user_email = filter.assignee_user_email.trim()
    if (filter.device_id.trim()) body.device_id = filter.device_id.trim()
    try {
      const [c, list] = await Promise.all([
        api.ticketCounts(body, ctx),
        api.listTickets(body, { ...ctx, searchkey: search.trim() || 'null', pageno: page, pagesize: PAGE_SIZE }),
      ])
      setCounts(c || {})
      setTickets(Array.isArray(list) ? list : [])
    } catch (e) {
      toast.error(`Load work orders failed: ${e.message}`)
      setCounts({}); setTickets([])
    }
  }, [ctx, toast, filter, search, page])
```

- [ ] **Step 3: Reset to page 1 when filters/search change**

Add an effect:
```jsx
  useEffect(() => { setPage(1) }, [filter, search])
```

- [ ] **Step 4: Render the filter bar + pagination**

Add a filter bar above the table `card` (after the `metric-grid` div):
```jsx
      <div className="card wo-filters">
        <div className="status-chips">
          {['new', 'open', 'on_hold', 'closed'].map((s) => (
            <button key={s} className={`chip ${filter.status.includes(s) ? 'active' : ''}`}
              onClick={() => setFilter((f) => ({ ...f, status: f.status.includes(s) ? f.status.filter((x) => x !== s) : [...f.status, s] }))}>
              {statusLabel(s)}
            </button>
          ))}
        </div>
        <input className="wo-search" placeholder="Search…" value={search} onChange={(e) => setSearch(e.target.value)} />
        <input className="wo-search" placeholder="Category" value={filter.category} onChange={(e) => setFilter((f) => ({ ...f, category: e.target.value }))} />
        <input className="wo-search" placeholder="Assignee email" value={filter.assignee_user_email} onChange={(e) => setFilter((f) => ({ ...f, assignee_user_email: e.target.value }))} />
        <input className="wo-search" placeholder="Device ID" value={filter.device_id} onChange={(e) => setFilter((f) => ({ ...f, device_id: e.target.value }))} />
      </div>
```
Add pagination controls below the table `card` (after the closing `</div>` of the table card):
```jsx
      <div className="pager">
        <button className="btn btn-ghost sm" disabled={page <= 1} onClick={() => setPage((p) => Math.max(1, p - 1))}>Prev</button>
        <span>Page {page} of {Math.max(1, Math.ceil((counts?.all || 0) / PAGE_SIZE))}</span>
        <button className="btn btn-ghost sm" disabled={page >= Math.ceil((counts?.all || 0) / PAGE_SIZE)} onClick={() => setPage((p) => p + 1)}>Next</button>
      </div>
```

- [ ] **Step 5: Add minimal styles**

Append to `sclera-ui/src/styles/global.css`:
```css
.wo-page .page-head { display: flex; align-items: center; gap: 12px; margin-bottom: 16px; }
.wo-page .page-head h1 { flex: 1; }
.wo-filters { display: flex; flex-wrap: wrap; gap: 8px; align-items: center; margin-bottom: 12px; }
.wo-filters .status-chips { display: flex; gap: 6px; }
.wo-filters .chip { padding: 4px 10px; border-radius: 999px; border: 1px solid var(--border, #2a3344); background: transparent; color: inherit; cursor: pointer; font-size: 12px; }
.wo-filters .chip.active { background: var(--accent, #3b82f6); border-color: var(--accent, #3b82f6); color: #fff; }
.wo-filters .wo-search { flex: 1; min-width: 120px; padding: 6px 10px; border-radius: 6px; border: 1px solid var(--border, #2a3344); background: transparent; color: inherit; }
.data-table { width: 100%; border-collapse: collapse; }
.data-table th, .data-table td { text-align: left; padding: 10px 12px; border-bottom: 1px solid var(--border, #2a3344); font-size: 13px; }
.data-table tr.clickable { cursor: pointer; }
.data-table tr.clickable:hover { background: rgba(255,255,255,0.03); }
.pager { display: flex; align-items: center; gap: 12px; justify-content: flex-end; margin-top: 12px; font-size: 13px; }
.drawer-backdrop { position: fixed; inset: 0; background: rgba(0,0,0,0.45); z-index: 60; display: flex; justify-content: flex-end; }
.drawer { width: 420px; max-width: 92vw; height: 100%; overflow-y: auto; background: var(--panel, #131a26); padding: 18px; box-shadow: -8px 0 24px rgba(0,0,0,0.4); }
.drawer-head { display: flex; align-items: flex-start; justify-content: space-between; gap: 12px; margin-bottom: 14px; }
.drawer-title { font-weight: 600; margin-bottom: 6px; }
.drawer-actions { display: flex; gap: 6px; }
.fld { display: flex; flex-direction: column; gap: 4px; margin-bottom: 10px; font-size: 13px; }
.fld > span { opacity: 0.75; }
.fld input, .fld select, .fld textarea { padding: 7px 10px; border-radius: 6px; border: 1px solid var(--border, #2a3344); background: transparent; color: inherit; font: inherit; }
```

> If `styles/global.css` already defines `.modal-backdrop`/`.modal`/`.badge`/`.btn`/`.card`/`.empty-state` (the existing modals use them), reuse those; only add rules above that are missing. Check with `grep -n "modal-backdrop\|\.badge\|\.btn-primary\|\.empty-state" sclera-ui/src/styles/global.css` before appending duplicates.

- [ ] **Step 6: Verify filters/search/pagination (Task 8 stack)**

Create ≥12 tickets across statuses (or use existing). Toggle a status chip → list + counts filter; type a category → narrows; type in Search → free-text filters; use Prev/Next → page changes and "Page X of N" updates.
Expected: each control re-queries and the table updates; page resets to 1 when a filter/search changes.

- [ ] **Step 7: Checkpoint**

Confirm filtering, search, and pagination work. Do not commit.

---

### Task 7: Real tickets in the device detail panel + "Raise ticket"

**Files:**
- Modify: `sclera-ui/src/components/DeviceDetailPanel.jsx`

**Interfaces:**
- Consumes: `api.ticketsByDevice` (Task 2); `TicketModal` (Task 4); `TicketDetailDrawer` (Task 5); `statusLabel`/`statusTone` (Task 3).
- Produces: no new exports; the "Work Orders" subtab now shows live device tickets.

- [ ] **Step 1: Replace the mock import with real state**

In `sclera-ui/src/components/DeviceDetailPanel.jsx`:
- Change the mock import (line 11) from:
  ```js
  import { inspectionsFor, workOrdersFor, inventoryFor, alertsFor } from '../services/mock.js'
  ```
  to (drop `workOrdersFor`):
  ```js
  import { inspectionsFor, inventoryFor, alertsFor } from '../services/mock.js'
  ```
- Add imports near the other component imports:
  ```js
  import TicketModal from './TicketModal.jsx'
  import TicketDetailDrawer from './TicketDetailDrawer.jsx'
  import { statusLabel, statusTone } from '../config.js'
  ```

- [ ] **Step 2: Replace the `workorders` memo with fetched state**

Remove the line:
```js
  const workorders = useMemo(() => (device ? workOrdersFor(device) : []), [device])
```
Add, with the other `useState` hooks:
```js
  const [workorders, setWorkorders] = useState(null)
  const [woModal, setWoModal] = useState(false)
  const [woOpenId, setWoOpenId] = useState(null)
  const loadWorkorders = () => api.ticketsByDevice(deviceId, {}, ctx)
    .then((t) => setWorkorders(Array.isArray(t) ? t : [])).catch(() => setWorkorders([]))
```
Add a lazy-load effect next to the Notes/Documents effects (~line 149):
```js
  useEffect(() => { if (sub === 'Work Orders' && workorders == null) loadWorkorders() }, [sub]) // eslint-disable-line react-hooks/exhaustive-deps
```
In the panel's reset effect (the one that does `setSensors(null); ... setDocuments(null)` ~line 100), also reset:
```js
    setWorkorders(null)
```

- [ ] **Step 3: Fix the badge count for the Work Orders tab**

The tab count array (~line 322) references `workorders.length`. Guard for the null initial state — change `['Work Orders', workorders.length]` to:
```js
    ['Work Orders', workorders?.length ?? 0],
```

- [ ] **Step 4: Replace the `WorkOrdersView` component body**

Replace the existing `function WorkOrdersView({ items }) { ... }` (lines ~694–703) with a live version that takes the device context:
```jsx
function WorkOrdersView({ items, onAdd, onOpen }) {
  return (
    <>
      <div className="svc-head">
        <span className="svc-note">Tickets from the workorders service for this asset</span>
        <button className="btn btn-primary sm" onClick={onAdd}><Icon name="plus" size={13} /> Raise ticket</button>
      </div>
      {items == null ? (
        <div className="log-list"><Skeleton w="100%" h={44} /><Skeleton w="100%" h={44} /></div>
      ) : items.length === 0 ? (
        <Empty icon="clipboard" label="No work orders for this asset" />
      ) : (
        <div className="log-list">
          {items.map((w) => (
            <div className="svc-row clickable" key={w.id} onClick={() => onOpen(w.id)}>
              <div className="svc-row-main">
                <div className="svc-row-title">{w.number ? `${w.number} · ` : ''}{w.name || 'Work order'}</div>
                <div className="svc-row-meta">Assignee {w.assignee_user_email || '—'} · {w.category || '—'}</div>
              </div>
              <div className="svc-row-side"><span className={`badge tone-${statusTone(w.status)}`}>{statusLabel(w.status)}</span></div>
            </div>
          ))}
        </div>
      )}
    </>
  )
}
```

- [ ] **Step 5: Pass props where `WorkOrdersView` is rendered**

Change the render line (~line 380) from:
```jsx
            : sub === 'Work Orders' ? <WorkOrdersView items={workorders} />
```
to:
```jsx
            : sub === 'Work Orders' ? <WorkOrdersView items={workorders} onAdd={() => setWoModal(true)} onOpen={setWoOpenId} />
```

- [ ] **Step 6: Render the modal + drawer inside the panel**

Near the end of the panel's returned JSX (before its outermost closing tag), add:
```jsx
      <TicketModal
        open={woModal}
        deviceId={deviceId}
        dockerName={device?.docker_name || ctx.docker}
        onClose={() => setWoModal(false)}
        onSaved={loadWorkorders}
      />
      <TicketDetailDrawer
        ticketId={woOpenId}
        onClose={() => setWoOpenId(null)}
        onChanged={loadWorkorders}
        onEdit={(t) => { setWoOpenId(null); /* edit reuses create modal via woModal is single-shot; open detail edit by reloading */ }}
      />
```

> The device-panel drawer's Edit reopens via the standalone page; for the panel, Edit is optional. If you want in-panel edit, store the selected ticket and pass it to `TicketModal`'s `ticket` prop — out of scope for the minimum deliverable; the comment marks where it would go.

- [ ] **Step 7: Verify device-panel integration (Task 8 stack)**

Open any asset → its detail panel → "Work Orders" tab.
Expected: the tab badge shows the device's ticket count; the list shows that device's real tickets (or empty state); "Raise ticket" opens the modal with Device ID pre-filled and locked; saving adds a ticket and refreshes the list; clicking a row opens the detail drawer with history.

- [ ] **Step 8: Checkpoint**

Confirm the device panel shows real tickets and "Raise ticket" works against the device. Do not commit.

---

### Task 8: Rebuild the UI container and full-stack verification

**Files:**
- None modified (container rebuild + verification). Run from repo root.

**Interfaces:**
- Consumes: all prior tasks.
- Produces: the live, CORS-correct UI on `http://localhost:3000`.

- [ ] **Step 1: Rebuild and restart the UI container**

Run:
```bash
docker compose -f sclera-ui/docker-compose.yml up -d --build
```
Expected: `sclera-asset-ui` rebuilds (Vite production build succeeds) and starts on `:3000`.

- [ ] **Step 2: Verify the build had no errors**

Run:
```bash
docker logs sclera-asset-ui --since 2m 2>&1 | tail -20
```
Expected: nginx serving; no Vite build error in the build output (if the build failed, the `up` step would have errored — fix the reported file and rebuild).

- [ ] **Step 3: Browser end-to-end on the CORS-correct origin**

Open `http://localhost:3000`. Verify the full flow:
- Sidebar → "Work Orders" → page loads counts + table (no CORS/console errors).
- Create a ticket → appears in list, counts update.
- Open it → detail drawer + history; Edit changes status; Delete removes it.
- Status chips / search / category / device filters narrow the list; Prev/Next paginate.
- Open an asset → "Work Orders" tab → device tickets + "Raise ticket" creates one for that device.

Expected: every action round-trips with success toasts and no console errors.

- [ ] **Step 4: Verify all Dapr sidecars are in sync**

Run:
```bash
docker ps --format "{{.Names}}\t{{.Status}}" | grep -E "dapr|sclera-workorders|sclera-cloud-device-asset"
docker logs sclera-workorders --since 3m 2>&1 | grep -c "127.0.0.1:350" || true
docker logs sclera-cloud-device-asset --since 3m 2>&1 | grep -c "127.0.0.1:3500" || true
```
Expected: every `*-dapr` container is `Up`; both grep counts are `0` (no sidecar-connection floods). If a count is non-zero, run `docker compose up -d --force-recreate <app>-dapr` for the affected app.

- [ ] **Step 5: Final checkpoint**

Confirm: UI live on `:3000`, full Work Orders CRUD + filters + device integration verified in-browser, all Dapr sidecars Up and clean. Report results to the user. Do not commit (leave staging/commit to the user).

---

## Self-Review

**Spec coverage:**
- Backend rebuild + sidecar sync → Task 1, Task 8 Step 4. ✓
- API layer (7 methods) → Task 2. ✓
- Standalone page (cards, filters, table, pagination) → Tasks 3 + 6. ✓
- TicketModal (create/edit, no `type`) → Task 4. ✓
- TicketDetailDrawer (detail + history + edit/delete) → Task 5. ✓
- Nav wiring (config/Sidebar/App) → Task 3. ✓
- Device-panel integration (real tickets + Raise ticket) → Task 7. ✓
- Run & verify on `:3000`, sidecar check → Task 8. ✓
- Error handling via ToastContext, empty states → built into Tasks 3–7. ✓

**Placeholder scan:** No TBD/TODO left as work items. The one inline comment in Task 7 Step 6 marks an explicitly out-of-scope optional (in-panel edit), with code present for the minimum deliverable.

**Type consistency:** `ticketCounts/listTickets/getTicket/upsertTicket/deleteTicket/ticketHistory/ticketsByDevice` names and signatures are used identically across Tasks 2–7. `statusLabel`/`statusTone`/`TICKET_STATUSES` defined in Task 3, consumed consistently after. `TicketModal` props `{open,ticket,deviceId,dockerName,onClose,onSaved}` and `TicketDetailDrawer` props `{ticketId,onClose,onChanged,onEdit}` match every call site.
