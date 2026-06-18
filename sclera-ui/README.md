# Sclera Asset Management UI (POC)

A React dashboard (Optima-style dark theme) wired to the **real** Sclera API gateway.
Pages: Property List, Property Dashboard (metrics + feature tiles), Managed Assets
(list, filter tabs, search, pagination, context menu), and an Add/Edit Asset modal —
all backed by live CRUD against the backend.

## Architecture

```
Browser (localhost:3000)
  └─ fetch → API gateway (localhost:8080)
                ├─ /vdms/**   → vdms-service        (Property List)
                └─ /asset/**  → cloud-device-asset  (Assets CRUD, counts, dropdowns)
```

The gateway runs the `docker` Spring profile → **no auth required** and CORS allows
`http://localhost:3000`. That's why the UI must be served on **port 3000**.

## Quick start (demo)

From the **repo root**:

```powershell
# 1. Bring up the backend (gateway, services, Postgres, Redis...)
docker compose up -d

# 2. Wait until the gateway is healthy:
#    curl http://localhost:8080/actuator/health   -> {"status":"UP"}

# 3. (Optional) seed extra demo assets via the real API
./sclera-ui/seed/seed-demo.ps1

# 4. Build & run the UI (standalone)
docker compose -f sclera-ui/docker-compose.yml up -d --build
```

Open **http://localhost:3000** → Property List shows **VDMS760 "Sclera HQ — Demo
Building"** → click it → Dashboard → **Managed Assets**.

> Notes:
> - This UI serves on port 3000 (the only origin the gateway CORS allow-list permits).
>   The old `test-ui` harness also uses 3000 — if it's running, stop it: `docker compose stop ui`.
> - A foreign `postgres` container may already hold host port 5432; the demo override
>   (`docker-compose.override.yml`) remaps this stack's Postgres to host **5433**.

## Local dev (hot reload, no Docker)

```powershell
cd sclera-ui
npm install
npm run dev        # http://localhost:3000
```

## Configuration

All via env / build args (see `.env.example`):

| Var               | Default                 | Purpose                                   |
|-------------------|-------------------------|-------------------------------------------|
| `VITE_API_BASE`   | `http://localhost:8080` | Gateway base URL                          |
| `VITE_DEMO_USER`  | `admin`                 | `username` query param                    |
| `VITE_DEMO_DISPLAY`| `Master User`          | Name shown in the sidebar                 |
| `VITE_DEMO_VDMS`  | `VDMS760`               | `vdmsid` scope (live property)            |
| `VITE_DEMO_DOCKER`| `right_wing`            | `dockername` (avoid SQL reserved words!)  |

To point at QA instead of local:
`VITE_API_BASE=https://qa-app.sclera.com docker compose -f sclera-ui/docker-compose.yml up -d --build`
(QA may enforce auth/CORS — local docker profile is the reliable demo path.)

## Endpoints used

| Action            | Method | Path |
|-------------------|--------|------|
| Property card     | GET    | `/vdms/details`, `/vdms/id` |
| Asset list (browse) | GET  | `/asset/api/v1/sclera-cloud-device-asset-service/docker/{docker}/getsubsystemparentdevicesbypagination` |
| Asset list (search) | GET  | `.../docker/{docker}/getfilterdevice` (supports `searchKey`) |
| Filter (multi-keyword) | POST | `.../docker/{docker}/searchsortfilterdevices` |
| Asset counts      | GET    | `.../docker/{docker}/getdevicecount` |
| Get one asset (detail panel) | GET | `.../docker/{docker}/device/{id}/getdevice` |
| Create asset      | POST   | `.../docker/{docker}/devicesupsert` |
| Edit asset        | POST   | `.../docker/{docker}/device/{id}/edit` |
| Delete / Archive  | DELETE / POST | `.../deletedevices`, `.../archivedevices` |
| Activity / audit logs | GET | `/vdms/audit-log?vdmsId=&page=&size=` |
| Asset fields / dropdowns | GET | `.../asset-fields`, `.../getbuildingsbyvdmsid`, `.../getlocations` |

`condition` (filter tabs) valid values: `all, unmonitored, online, offline, other, matched, unmatched, verified, archived, assigned, unassigned`.

## Notes

- Even with no seed data, the UI renders clean empty states, and **Add Asset** persists
  live through the gateway — reload shows the new asset. That alone demonstrates real
  backend integration.
- Error handling: toasts, retry-on-failure in the fetch layer, loading skeletons,
  React error boundary.
- `src/config.js` holds `API_CONFIG` + demo context; `src/services/api.js` is the API layer.
