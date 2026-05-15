# CLAUDE.md — sclera-cloud-device-asset

## Project identity
This is a verbatim-extracted seed microservice. Package root is `io.sclera.*` (not `com.sclera.<app>`). Spring Boot 2.6.5, Java 11. Do not modernize without an explicit Phase-2 plan.

## Rules
- Source repo `sclera-vdms-edge-server` is read-only. Do not edit anything there.
- All Bucket-C call sites use stubs in `io.sclera.stubs/`. Stubs must not throw; they return safe defaults and emit one WARN log per invocation.
- `migration-notes/` is the audit trail — update it as you go.

## See also
- Original design: `../sclera-vdms-edge-server/docs/superpowers/specs/2026-05-12-sclera-cloud-device-asset-design.md`
- Original plan: `../sclera-vdms-edge-server/docs/superpowers/plans/2026-05-12-sclera-cloud-device-asset.md`