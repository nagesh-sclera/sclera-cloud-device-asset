# sclera-cloud-workorder

Workorder microservice — part of the Sclera 2.0 platform. Manages service/incident
tickets and Maximo integration, with cross-service calls and audit eventing over Dapr.

## Stack

- Java 21, Spring Boot 4.x
- PostgreSQL (`workorder_db` database), Spring Data JPA / Hibernate
- Dapr sidecar (service invocation + pub/sub)
- springdoc-openapi (Swagger UI)
- Package root: `io.sclera.workorder.*`

## Ports

| Port | Purpose |
|---|---|
| 8092 | Application HTTP (`APP_PORT`) |
| 3501 | Dapr sidecar HTTP (`DAPR_HTTP_PORT`) |
| 5432 | PostgreSQL |

All HTTP endpoints are served under the context-path **`/api/v1/workorder-service`**.

## Endpoints

Base prefix omitted below (full path = `/api/v1/workorder-service` + the path shown).

### Maximo — `/maximo`
| Method | Path | Purpose |
|---|---|---|
| POST | `/maximo/upsertmaximoconfiguration` | Create/update Maximo config |
| GET | `/maximo/getmaximoconfiguration` | Fetch Maximo config |
| DELETE | `/maximo/configuration/{configurationId}/deletemaximoconfiguration` | Delete config |
| POST | `/maximo/workorder/{workOrderId}/getmaximoworkorders` | List Maximo work orders |
| POST | `/maximo/workorder/{workOrderId}/getmaximoworkorderid` | Resolve Maximo work-order id |
| POST | `/maximo/checkmaximoconfiguration` | Validate Maximo connectivity/config |
| GET | `/maximo/getmaximosites` | List Maximo sites |
| GET | `/maximo/getvdmsdetails` | VDMS details (via Dapr to vdms-service) |

### Tickets — `/ticket`
| Method | Path | Purpose |
|---|---|---|
| POST | `/ticket/upsertticket` | Create/update a ticket |
| POST | `/ticket/getalltickets` | Query tickets |
| POST | `/ticket/device/{device_id}/getallticketsbydeviceid` | Tickets for a device |
| DELETE | `/ticket/{ticket_id}/deleteticket` | Delete a ticket |
| POST | `/ticket/getticketcount` | Ticket counts |
| GET | `/ticket/{ticket_id}/getticketdetailsbyid` | Ticket detail |
| GET | `/ticket/{ticket_id}/gettickethistory` | History for a ticket |
| GET | `/ticket/device/{device_id}/gettickethistory` | History for a device |

> Endpoints take **mandatory** `loggedInUser` / `vdms_id` query params (replacing the
> old `/user/{u}/vdms/{v}/...` path prefix from the monolith). A missing one returns HTTP 400.

## Dapr integration

| Setting | Default | Purpose |
|---|---|---|
| `dapr.vdms-app-id` | `vdms-service` | service invocation → vdms-service |
| `dapr.device-asset-app-id` | `sclera-cloud-device-asset` | service invocation → device-asset |
| `dapr.pubsub-name` | `pubsub` | pub/sub component name |
| `dapr.topic-name` | `audit-events` | audit events published here (consumed by vdms-service) |

User actions are published as audit events to the `audit-events` topic; vdms-service
subscribes and persists them.

**Scheduler subscriber:** the service also consumes the central scheduler's
`scheduler.trigger` topic via `io.sclera.workorder.scheduler.SchedulerDemoSubscriber`
(route `/internal/scheduler-demo`), wired by the declarative subscription
`components-docker/subscription-scheduler-demo-workorder.yaml`. It filters to the demo jobs
workorder owns — `demoWorkorder1/2/3` — which are currently dummy print handlers.

## Configuration

`application.yml` (overridable via environment variables):

| Property | Env var | Default |
|---|---|---|
| datasource url | `DB_URL` | `jdbc:postgresql://localhost:5432/workorder_db` |
| datasource user | `DB_USER` | `root` |
| datasource password | `DB_PASSWORD` | `mypass123` |
| server port | `APP_PORT` | `8092` |
| Dapr HTTP port | `DAPR_HTTP_PORT` | `3501` |
| active profile | `SPRING_PROFILES_ACTIVE` | `development` (`docker compose up`) / `docker` (base only) |

Hibernate `ddl-auto=update` creates tables on first boot (switch to `validate` + Flyway
for production). HikariCP pool: max 10 / min idle 2.

## Build & run

### Docker (recommended)

From the project root:

```bash
docker compose up -d sclera-cloud-workorder workorder-dapr postgres redis placement
```

The service builds via a multi-stage Maven Dockerfile and starts once PostgreSQL is healthy.

### Local (Maven)

```bash
mvn clean package
java -jar target/*.jar
# or: mvn spring-boot:run
```

Requirements: JDK 21, Maven 3.9+, a reachable PostgreSQL with `workorder_db`. For local
Dapr-dependent calls, run the Dapr sidecar alongside.

## API docs

- Swagger UI (direct): `http://localhost:8092/api/v1/workorder-service/swagger-ui.html`
- Via gateway: `http://localhost:8080/maximo/swagger-ui.html`
- OpenAPI JSON: `.../v3/api-docs`

## Health

Actuator: `/actuator/health`, `/actuator/info`, `/actuator/metrics`
(health details shown only when authorized).

## Logging

SLF4J + Logback. Config lives in `src/main/resources/logback/` and is selected per
Spring profile via `logging.config`:

| Profile | File | `LOG_PATH` |
|---|---|---|
| default / docker | `logback.xml` | `/home/sclera/logs/sclera_backend/services` |
| development | `logback-development.xml` | `./logs` |
| uat / qa | `logback-uat.xml` / `logback-qa.xml` | `/home/sclera/logs/sclera_backend/services` |

Appenders: **Console** (all logs) + a rolling **Debug** file (INFO and above) + a rolling
**Error** file (ERROR only) **per service class**. Rolling policy: 10MB/file, 30-day history,
30MB cap. `application.yml` `logging.level` (e.g. `org.hibernate.SQL: WARN`) still applies on top.

Files are written per service class (`MaximoService`, `TicketService`, `TicketHistoryService`,
`UserActionLogService`):
```
${LOG_PATH}/TicketService/Debug/TicketServiceDebug.log
${LOG_PATH}/TicketService/Error/TicketServiceError.log
```

In Docker the image creates `/home/sclera/logs` and chowns it to the non-root `spring`
user (see `Dockerfile`). With `docker compose up` (development profile) the logs are
bind-mounted to `./logs/sclera-cloud-workorder` on the host. Running from the IDE on Windows
with the default profile, `LOG_PATH` resolves to `C:\home\sclera\logs\...`; the `development`
profile uses `./logs` for a project-local path.

## Correlation IDs

`CorrelationIdFilter` (`OncePerRequestFilter`, highest precedence) reads the
`X-Request-Id` header (set by the gateway; generated if absent for direct calls), puts it
in the SLF4J **MDC** under `requestId`, and echoes it on the response. The logback patterns
include `[%X{requestId:-}]`, so **every** log line — console and per-class files — carries
the id:
```
[a1b2c3d4] INFO i.s.workorder.service.TicketService - Successfully added ticket ID: CID-1
```
Grep one id across services to trace a request end-to-end.

## Project layout

```
src/main/java/io/sclera/workorder/
├── controller/   # MaximoController, TicketController, TicketHistoryController
├── service/      # TicketService, MaximoUtils, UserActionLogService, ...
├── client/       # Dapr clients: VdmsClient, DeviceAssetClient, MaximoApiClient, UserActionLogClient
└── ...
```
