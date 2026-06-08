# Scheduler End-to-End — Run Guide

How to build and run the Dapr scheduler so jobs execute end-to-end (FIRED → real work → SUCCESS),
including the device-asset dispatcher and the dev-only simulator.

Implements `docs/superpowers/plans/2026-06-08-scheduler-execution-and-cleanup.md`.

---

## What runs where

| Piece | Lives in | Role |
|-------|----------|------|
| Dapr Scheduler control plane | `dapr-scheduler` container | Fires jobs on their cron/`@every` schedule |
| `sclera-scheduler` service | `sclera-scheduler` container (port **8098**) | Registers the job catalog, receives fires (`POST /job/{name}`), records runs, serves the dashboard/API, records results |
| **Device-asset dispatcher** | `app` container (`sclera-cloud-device-asset`, port **8085**) | `TriggerDispatchSubscriber` runs the **10 device-asset jobs** for real and publishes `scheduler.result` |
| **Dev simulator** | `sclera-scheduler` container (only under `dev`/`docker` profile) | `TriggerSimulatorSubscriber` publishes a simulated SUCCESS for **non-device-asset** jobs (their owning services aren't running here) |

Event flow: Scheduler → `POST /job/{name}` → publish `scheduler.trigger` → (device-asset dispatcher **or** simulator) → publish `scheduler.result` → scheduler `ResultSubscriber` updates the run.

Only the **2 wired** device-asset jobs do real work today: `offlineDeviceCheck`
(→ `DeviceSpecificationService.updateDeviceStatusToOffline()`) and `deviceDndEnable`
(→ `DeviceService.dndCheckAndUpdate()`). The other 8 device-asset jobs are stub-with-WARN
(succeed, log a WARN, do no work) — see `sclera-cloud-device-asset/migration-notes/scheduler-handler-wiring.md`.

---

## Prerequisites (Windows)

- **Docker Desktop** running.
- **JDK 21 = Amazon Corretto 21.0.8** at `C:\Users\DhanushVasanth\.jdks\corretto-21.0.8`
  (no global `mvn`; each module uses its own `.\mvnw.cmd`). Set `JAVA_HOME` per shell — it does
  not persist across calls.
- **`dapr-commons` installed to the local Maven repo** (the `app`/device-asset host build depends on
  it; the `sclera-scheduler` image builds it internally so it needs nothing pre-installed):
  ```powershell
  Set-Location "C:\Users\DhanushVasanth\Desktop\AssetManagement POD\Microservice123\sclera-cloud-device-asset\dapr-commons"
  $env:JAVA_HOME="C:\Users\DhanushVasanth\.jdks\corretto-21.0.8"
  .\mvnw.cmd -DskipTests install
  ```

---

## Build

Run all commands from the repo root unless noted:
`C:\Users\DhanushVasanth\Desktop\AssetManagement POD\Microservice123\sclera-cloud-device-asset`

### 1. Build the device-asset jar (required before the `app` image)

The `app` image is runtime-only (`COPY target/sclera.jar`), so it needs a host jar build first.
Rebuild this whenever device-asset code changes (e.g. the dispatcher/handlers):

```powershell
Set-Location "C:\Users\DhanushVasanth\Desktop\AssetManagement POD\Microservice123\sclera-cloud-device-asset\sclera-cloud-device-asset"
$env:JAVA_HOME="C:\Users\DhanushVasanth\.jdks\corretto-21.0.8"
.\mvnw.cmd -DskipTests clean package      # -> target/sclera.jar
```

> ⚠️ Run this in **PowerShell** with `JAVA_HOME` set. The bundled wrapper needs JDK 21; git-bash
> has no `JAVA_HOME` and will fail with "JAVA_HOME not found". Confirm the jar is fresh
> (`Get-Item .\target\sclera.jar | Select LastWriteTime`) — a stale jar means the image won't
> contain your latest code.

### 2. Build the images

The `sclera-scheduler` image is now **self-contained** (multi-stage: it builds `dapr-commons` then
itself from source — no prior host `mvnw package` needed). The `app` image copies the jar from step 1.

```powershell
docker compose build sclera-scheduler app
```

---

## Run

`docker-compose.override.yml` is auto-merged and fixes two local-stack issues:
1. `infra/otel/opentelemetry-javaagent.jar` is a 1-byte placeholder → it blanks `JAVA_TOOL_OPTIONS`
   for `app` and `sclera-scheduler` so the JVM starts.
2. `dapr-scheduler` runs non-root and can't create its data dir → it runs that container as root.

```powershell
docker compose up -d
docker compose ps          # all app + dapr sidecars Up
```

The simulator loads because `sclera-scheduler` runs with `SPRING_PROFILES_ACTIVE: docker`.

---

## Verify end-to-end

### Scheduler API / dashboard (port 8098)
```powershell
# list jobs + latest run status
curl.exe http://localhost:8098/api/jobs
# run-history for a job
curl.exe "http://localhost:8098/api/jobs/offlineDeviceCheck/runs?limit=10"
```
The dashboard page is served by the scheduler (open `http://localhost:8098/` — see the
`SchedulerApiController` mapping). Swagger: `http://localhost:8085/swagger-ui/index.html`.
Test UI: `http://localhost:3000`.

### 1. A real device-asset job (no manual result needed)
`offlineDeviceCheck` fires every 90s (`@every 90s`, owner `device-asset`). Watch a run go
**FIRED → SUCCESS** with a real `durationMs` — executed by the device-asset dispatcher calling
`updateDeviceStatusToOffline()`:
```powershell
# wait ~90s, then:
curl.exe "http://localhost:8098/api/jobs/offlineDeviceCheck/runs?limit=3"
```
You can also confirm in the `app` logs:
```powershell
docker compose logs --tail=50 app | Select-String "offlineDeviceCheck"
```

### 2. A simulated (non-device-asset) job
Trigger one immediately and watch the simulator close the loop:
```powershell
curl.exe -X POST http://localhost:8098/api/jobs/snmpSync/run
curl.exe "http://localhost:8098/api/jobs/snmpSync/runs?limit=3"   # -> SUCCESS via simulator
```
`snmpSync` (owner `integrations`) has no real owning service here, so `TriggerSimulatorSubscriber`
publishes a simulated SUCCESS. The simulator **skips** device-asset-owned jobs (the real dispatcher
owns those), so there are no double results.

---

## Gotchas

- **Rebuild after code changes:** device-asset changes → redo Build steps 1 **and** 2 (`app`), then
  `docker compose up -d app`. Scheduler changes → just `docker compose build sclera-scheduler` (it
  builds from source) then `up -d sclera-scheduler`.
- **The in-container Dapr port 3500 is not host-exposed.** To call a sidecar's Dapr API directly:
  `docker run --rm --network container:sclera-scheduler curlimages/curl <url>`.
- **Don't send raw JSON via Windows PowerShell 5.1 `curl.exe`** — PS 5.1 strips the double quotes and
  Dapr returns 400. Drive JSON bodies from **git-bash** (or use `Invoke-RestMethod -Body (… | ConvertTo-Json)`).
- **Simulator is dev/docker only** (`@Profile({"dev","docker"})`) — it never loads in prod.
- **Stub-with-WARN jobs** report SUCCESS but do nothing; that's expected until they're wired (see the
  migration note). The real monolith dispatcher lives in the read-only `sclera-vdms-edge-server` repo
  and is out of scope here.
