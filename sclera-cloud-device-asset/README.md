# sclera-cloud-device-asset

AP-C1 cloud device/asset microservice. Verbatim extraction from `sclera-vdms-edge-server`.

## Status
Phase 1 — seed repo. Stubs in place of cross-microservice calls. Source repo is read-only.

## Stack
Spring Boot 2.6.5, Java 11, package root `io.sclera.*`, MySQL 8, springdoc-openapi.

## Run locally
```
mvn spring-boot:run
```
Swagger UI: http://localhost:8085/swagger-ui.html

## References
- Design: `../sclera-vdms-edge-server/docs/superpowers/specs/2026-05-12-sclera-cloud-device-asset-design.md`
- Plan: `../sclera-vdms-edge-server/docs/superpowers/plans/2026-05-12-sclera-cloud-device-asset.md`
- Audit trail: see `migration-notes/`