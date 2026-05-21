# Dapr app-id registry

| App-id | Service | Port | Subscribes | Publishes | Owner |
|---|---|---|---|---|---|
| `sclera-api-gateway` | sclera-api-gateway | 8080 | — | — | platform (sidecar wired) |
| `sclera-cloud-device-asset` | sclera-cloud-device-asset | 8085 | — | device.audit-recorded, vdms.* | platform |
| `vdms-service` | sclera-vdms-service | 8089 | device.audit, vdms.* | — | platform |
| `sclera-audit` | sclera-audit (skeleton) | 8090 | device.audit-recorded | — | platform |
| `sclera-identity` | sclera-identity (skeleton) | 8091 | — | identity.org-renamed, identity.user-deactivated | platform |
| `sclera-alerts` | sclera-alerts (skeleton) | 8092 | device.alert-condition-fired (future) | alerts.notification-dispatched (future) | platform |
| `sclera-inventory` | sclera-inventory (skeleton) | 8093 | — | — | platform |
| `sclera-workorders` | sclera-workorders (skeleton) | 8094 | — | — | platform |
| `sclera-inspection` | sclera-inspection (skeleton) | 8095 | — | — | platform |
| `sclera-integrations` | sclera-integrations (skeleton) | 8096 | — | — | platform |
| `sclera-edge` | sclera-edge (skeleton) | 8097 | — | — | platform |

## Port range
- 8080–8089: existing services
- 8090–8099: skeleton services (this plan)
- 8100+: reserved for future split (e.g. AP-C2a/b/c)

## Convention
App-id = kebab-case prefixed `sclera-`. Matches `spring.application.name`.
Topics = `<domain>.<event-past-tense>`.

## Sidecar flags (standardized Phase 1)
All sidecars run with: `--config /dapr/config.yaml --resources-path /dapr/components/local`.
Resources path loads:
- `pubsub.yaml` (state.redis dev pub/sub; swap to `dapr/components/k8s/pubsub.yaml` for RabbitMQ in prod)
- `secretstore.yaml` (secretstores.local.file)
- `resiliency.yaml` (timeouts, retries, circuit-breakers)
- `statestore-idempotency.yaml` (subscriber dedupe, 24h TTL)
- `statestore-vdmscache.yaml` (VDMS read-through cache, 5min TTL)
- `binding-corrigo.yaml`, `binding-daintree.yaml`, `binding-smtp.yaml` (output bindings)

## Topics
- `device.audit-recorded` — published by cloud-device-asset; consumed by sclera-audit (idempotency-aware)
- `device.event-recorded` — published by cloud-device-asset RabbitmqClient (RabbitMQ replacement)
- `device.sensor-reading` — published by cloud-device-asset RabbitmqClient (sensor measurements)
- `vdms.*` — VDMS-related events between cloud-device-asset and vdms-service

## Observability
- OTel Collector: `otel-collector:4317` (OTLP gRPC), `:4318` (OTLP HTTP), `:9464` (Prometheus scrape)
- Jaeger UI: `http://localhost:16686`
- Tracing samplingRate: `"1"` (100%) — tune for prod via `dapr/config.yaml`
