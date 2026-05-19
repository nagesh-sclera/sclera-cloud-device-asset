# CLAUDE.md — sclera-identity

## Project identity
Walking-skeleton microservice scaffolded by `tools/skeleton-template/scaffold-skeleton.sh`. Spring Boot 2.6.5, Java 17. Hosts the future real `sclera-identity` service's Dapr surface.

## Rules
- No database, no Flyway, no JPA. All endpoints return hardcoded defaults from `defaults/Defaults.java`.
- Endpoints generated from `sclera-cloud-device-asset/migration-notes/stub-inventory.md` — do not edit by hand. Re-run the scaffold script to regenerate.
- Pub/sub subscriptions are no-op handlers that log the event and return 200.

## See also
- Spec: `docs/superpowers/specs/2026-05-19-walking-skeleton-extraction-design.md`
- Plan: `docs/superpowers/plans/2026-05-19-walking-skeleton-poc.md`
