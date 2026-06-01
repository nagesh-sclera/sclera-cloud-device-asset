# CLAUDE.md — sclera-integrations

## Project identity
Data-owning microservice for integration entities (Bacnet, Daintree, Disruptive, Ecobee,
Lorawan, Modbus, Monnit, MyDevices, Pelican). Spring Boot 4.0.6, Java 21.

Previously a walking-skeleton stub returning Defaults; expanded into a real PG-backed
service when the entities were moved out of `sclera-cloud-device-asset`.

## Architecture
- Persistence: PostgreSQL, shared `vdms` database, schema `integrations_svc`.
- Entities live under `io.sclera.integrations.model`; composite-key class under
  `io.sclera.integrations.model.compositeclass`.
- Repositories under `io.sclera.integrations.repository`.
- Cross-module entity references use scalar FK columns (e.g. `device_id`, `docker_name`,
  `vdms_id`) — no @ManyToOne to entities living in other modules (loose-coupling rule).
- IDs are `String` (UUIDs) across all entities.

## Controller wiring status
- `BacnetController`, `MyDevicesController`: repo-injected, trivial methods wired,
  business-logic-heavy methods marked `// TODO`.
- `EcobeeController`, `DisruptiveController`, `MonnitController`, `ModbusController`,
  `PelicanController`, `DaintreeController`, `LorawanController`: repo-injected; method
  bodies still return Defaults pending business-logic port.
- `KNXController`, `MqttController`, `PolyLensController`, `SiemensController`,
  `SnmpController`, `AssetMapperController`, `IntegrationController`: no entities in
  scope; unchanged.

## Rules
- Pub/sub subscriptions are no-op handlers that log and return 200 (unchanged).
- New entities follow the loose-coupling rule: scalar FK for any reference into
  cloud-device-asset / vdms-service / other modules.

## See also
- Spec: `docs/superpowers/specs/2026-05-19-walking-skeleton-extraction-design.md`
- Plan: `docs/superpowers/plans/2026-05-19-walking-skeleton-poc.md`
