# Work Orders UI — Design

**Date:** 2026-06-19
**Topic:** Add a Work Orders (tickets) UI to `sclera-ui`, wired through the API gateway to
the real `sclera-workorders` microservice, integrated with the device/asset view, and run
in the existing Docker Desktop stack with all Dapr sidecars in sync.

## Background / current state

- **`sclera-workorders`** (Java 21 / Spring Boot, Dapr app-id `sclera-workorders`, port 8094,
  context-path `/api/v1/workorder-service`) already exposes a real ticket API:
  - `POST /ticket/getticketcount` → `Map<status,Integer>` with keys `all`, `new`, `open`,
    `on_hold`, `closed`.
  - `POST /ticket/getalltickets?searchkey=&pageno=&pagesize=` (body `TicketFilterDTO`) →
    `Set<TicketDTO>` (paging metadata in `X-Total-Count`/`X-Page`/`X-Page-Size` headers).
  - `GET /ticket/{id}/getticketdetailsbyid`
  - `POST /ticket/upsertticket` (body `TicketDTO`, upsert — new when no `id`)
  - `DELETE /ticket/{id}/deleteticket` (soft delete)
  - `GET /ticket/{id}/gettickethistory`
  - `POST /ticket/device/{deviceId}/getallticketsbydeviceid`
  - `GET /ticket/device/{deviceId}/gettickethistory`
  - All require query params **`loggedInUser`** and **`vdms_id`** (400 if missing) — note these
    differ from the device API's `username`/`vdmsid`.
- **Gateway** already routes `/workorders/**` → `http://sclera-workorders:8094` with
  `StripPrefix=1`, and CORS already allows `http://localhost:3000`.
- **`sclera-ui`** (Vite + React, "Optima" dark theme) runs on `:3000` (container
  `sclera-asset-ui`). It uses a sidebar + `view`-switch pattern (`AppContext`), a central
  `services/api.js`, and toast/error-boundary infrastructure. It has no Work Orders UI yet.
- The `DeviceDetailPanel` **already has a "Work Orders" subtab**, currently fed by mock data
  (`workOrdersFor(device)` from `services/mock.js`).

### Key constraint discovered

The **running `sclera-workorders` container is the old walking-skeleton stub** — it answers
bare `/actuator/health` (UP) but 404s on every `/api/v1/workorder-service/**` path, so the real
ticket service is committed in source but was never rebuilt into the image. The UI cannot
function until that image is rebuilt and its Dapr sidecar recreated.

### Domain notes

- Canonical statuses: `new`, `open`, `on_hold`, `closed`.
- `TicketDTO.type` is a **service-computed transition enum** (`TicketType`: generated, status
  updated, closed, …) — it is NOT a user-facing "incident vs service" field and must NOT be a
  form input.
- `TicketDTO` form-relevant fields: `name`, `status`, `category`, `request_type`,
  `description`, `user_message`, `assignee_user_email`, `device_id`, `docker_name`.

## Goals

1. A dedicated **Work Orders page** with full CRUD, filters, search, pagination, and a
   detail+history drawer.
2. **Device-panel integration**: the existing "Work Orders" subtab shows a device's real
   tickets, with a "Raise ticket" action — demonstrating workorders↔device-microservice
   integration.
3. Everything runs in the existing Docker Desktop stack with all Dapr sidecars in sync.

Out of scope (YAGNI): Maximo config screen, scheduler-demo/jobs endpoints, vendor-side flows.

## Components & data flow

### 1. Backend rebuild (no code change)
- `docker compose build sclera-workorders` → `docker compose up -d sclera-workorders`
- `docker compose up -d --force-recreate sclera-workorders-dapr` — the sidecar uses
  `network_mode: "service:sclera-workorders"`; recreating the app orphans the sidecar's net
  namespace, so it must be recreated too.
- Verify `POST /workorders/api/v1/workorder-service/ticket/getticketcount?loggedInUser=admin&vdms_id=VDMS760`
  returns 200 through the gateway, and all `*-dapr` containers stay Up with no
  `127.0.0.1:3500` errors in logs.

### 2. API layer — `src/services/api.js`
Add a `workorder(p)` URL builder (`${BASE_URL}/workorders/api/v1/workorder-service${p}`) and a
workorders scope helper (`loggedInUser` + `vdms_id`). New methods:
- `ticketCounts(filter)` — POST `/ticket/getticketcount`; returns the status→count map.
- `listTickets(filter, { searchkey='null', pageno=1, pagesize=10 })` — POST
  `/ticket/getalltickets`; returns the ticket array.
- `getTicket(id)` — GET `/ticket/{id}/getticketdetailsbyid`.
- `upsertTicket(dto)` — POST `/ticket/upsertticket`.
- `deleteTicket(id)` — DELETE `/ticket/{id}/deleteticket`.
- `ticketHistory(id)` — GET `/ticket/{id}/gettickethistory`.
- `ticketsByDevice(deviceId, filter, { pageno, pagesize })` — POST
  `/ticket/device/{id}/getallticketsbydeviceid`.

Pagination total is taken from `ticketCounts().all` (the list endpoint returns a bare `Set`
and the existing `request()` helper does not expose response headers). `request()` already
tolerates a JSON-array body, so no helper change is needed.

### 3. Page — `src/pages/WorkOrdersPage.jsx`
- **Summary cards** (reuse `MetricCard` styling) from `ticketCounts`: All / New / Open /
  On Hold / Closed. Clicking a card sets the status filter.
- **Filter bar**: status (multi-select via the count keys), category (text), assignee email,
  device id, free-text search. Drives `listTickets` + `ticketCounts`.
- **Table**: number, name, status badge, category, device, assignee, created-at. Row click →
  detail drawer. "New Work Order" button → `TicketModal` (create).
- **Pagination** (pageno/pagesize) using `ticketCounts().all` as total.

### 4. `src/components/TicketModal.jsx`
Create/edit form. Fields: `name`, `status` (new/open/on_hold/closed), `category`,
`request_type`, `assignee_user_email`, `user_message`, `description`, `device_id` (optional;
pre-filled and read-only when raised from a device). Submits `upsertTicket`; on edit, includes
the existing `id`. Does NOT expose `type` (service-computed). Validation mirrors the DTO
(`@Email` on assignee, length caps) and surfaces backend 400s via toast.

### 5. `src/components/TicketDetailDrawer.jsx`
Right-side drawer (follows `DeviceDetailPanel`/`LogsDrawer` pattern). Shows full ticket detail
(`getTicket`) and a **history timeline** (`ticketHistory`: message, status, action, created_by,
timestamp). Actions: Edit (opens `TicketModal` pre-filled), Delete (`deleteTicket` + confirm).

### 6. Navigation wiring
- `config.js` `NAV_ITEMS`: add `{ key: 'workorders', label: 'Work Orders', icon: 'clipboard' }`
  (add a `clipboard`/ticket glyph to `Icon.jsx` if absent).
- `Sidebar.jsx` `go()`: `if (key === 'workorders') setView('workorders')`.
- `App.jsx`: render `<WorkOrdersPage />` when `view === 'workorders'`.

### 7. Device-panel integration — `DeviceDetailPanel.jsx`
Replace the mock-fed "Work Orders" subtab with real data via `api.ticketsByDevice(deviceId)`:
list the device's tickets (number, status, assignee, created), each row opening the detail
drawer; add a "Raise ticket" button that opens `TicketModal` pre-filled with the device's
`device_id` + `docker_name`. Refresh the list on save. Lazy-load on first tab open (same
pattern as the Notes/Sensors tabs).

### 8. Run & verify
- Rebuild UI: `docker compose -f sclera-ui/docker-compose.yml up -d --build`.
- Verify in-browser: Work Orders page loads counts + list; create/edit/delete round-trips;
  filters + search + pagination work; a device's panel shows its tickets and "Raise ticket"
  creates one against that device.
- Confirm every `*-dapr` container is Up and in sync (no `127.0.0.1:3500` flood), and the
  scheduler→workorders pub/sub subscription still loads.

## Error handling
- API errors surface via the existing `ToastContext`; the `request()` helper already retries
  once on network/5xx and throws a descriptive `HTTP <status>` error otherwise.
- Empty states (no tickets, no history) render explicit "No work orders" / "No history"
  messaging, matching the Notes/Sensors tabs.
- Backend validation 400s (bad email, missing scope) are shown verbatim in the toast.

## Testing
- Manual end-to-end via the browser against the live stack (the project's established
  verification approach for this UI). No automated frontend test harness exists in `sclera-ui`.
- Backend ticket logic is already covered by the service's existing unit tests; this change
  rebuilds, it does not modify, that service.

## Risks / notes
- **Dapr sidecar sync** is the main operational risk — mitigated by force-recreating
  `sclera-workorders-dapr` after the app rebuild and verifying logs.
- The `getalltickets` `X-Total-Count` ignores the free-text `searchkey` (documented backend
  limitation); using `ticketCounts().all` for the total is consistent with that.
