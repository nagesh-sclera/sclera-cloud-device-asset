# Module-wide MapStruct Read-Path Migration (Mapping-first, two-pass)

**Date:** 2026-06-18
**Module:** `sclera-cloud-device-asset` (only)
**Status:** Approved design — pending implementation plan
**Builds on:** `2026-06-17-getdevicebyid-jpql-mapstruct-design.md` (proved the pattern on one path)

## Problem

The module populates response DTOs via Hibernate `@SqlResultSetMapping` / `@ConstructorResult`
over hand-written SQL. Current footprint in `src/main/java`:

- ~120 `@SqlResultSetMapping` declarations across 26 files
- ~280 inline `@Query(nativeQuery = true)` across 32 files
- ~334 `@NamedNativeQuery` across 34 files (134 in `DeviceRepository`, 81 in `Device.java`)

This mapping style is invisible to the compiler — column/constructor mismatches fail only at
runtime — and couples every read to Postgres-specific SQL.

## Goals

1. **Compile-time safety** — replace runtime `@SqlResultSetMapping` mapping with compile-checked
   MapStruct mapping.
2. **Consistency** — one uniform read pattern (DAO impl + projection + MapStruct) across the module.
3. **Portability** — reduce Postgres-specific native SQL — but as a *later* pass, not at the
   expense of parity.

## Hard constraints

- **Response contract is frozen — byte-identical.** Every response key, type, and value must
  remain identical (modulo JSON key order). Any field gained, lost, or changed is a defect.
- **Parity always wins.** Where a portable query cannot reproduce the exact bytes on Postgres, the
  query stays native.
- **Demo-safety (today).** The user has a same-day demo on `sclera2.0/demo`. The migration must be
  isolatable and revertible in seconds without risking the working app. End of day the user may
  keep, shelve, or fully revert the MapStruct work.
- **Never commit on the user's behalf** — work is left in the tree; the user commits manually.

## Strategy — Approach C: mapping-first, two passes

### Why C (given "parity always wins")

In **Pass 1 the SQL text is unchanged**, so the database returns byte-identical result sets *by
construction*. The only thing that can alter a response is the new Java mapping — small,
compile-checked, test-locked. All parity *risk* (SQL rewrites) is deferred to Pass 2 and handled
one path at a time against an already-MapStruct'd baseline.

### Pass 1 (this effort) — mapping migration, queries stay native

Each read path is converted from `@SqlResultSetMapping`/`@ConstructorResult` to
**projection → MapStruct → DTO**, with the native SQL copied **verbatim**. Wiring lands in a DAO
custom-impl (`XxxRepositoryCustom` + `XxxRepositoryImpl`):

- The impl `@Autowired`s `EntityManager` + the MapStruct mapper directly. This **retires the
  `DeviceRepositoryMapperHolder` static-field hack** (a workaround needed only because Spring Data
  default methods aren't injectable) as each path migrates.
- The native SQL string is copied **character-for-character** from the old
  `@NamedNativeQuery`/`@Query` into `em.createNativeQuery(...)` — guaranteeing identical result
  sets.
- The result is read as the managed entity (single-table reads) or `jakarta.persistence.Tuple`
  (multi-join reads), assembled into a projection record, and mapped by MapStruct.
- The repository interface method **keeps its exact signature** — no caller or endpoint observes a
  change.

### Pass 2 (later, separate spec) — portability

For each already-converted path the only change is editing the SQL string inside its DAO impl
method from native → JPQL/Criteria, then re-running the same parity harness. Bucket ⑤ paths never
advance and stay native behind the uniform interface.

## Read-path catalogue (drives Pass-1 ordering and mapper shape)

All ~700 read sites are classified so each path's mapper shape is known up front and work is
ordered easy→hard:

| Bucket | Shape | Pass-1 source type | Projection record? | Pass-2 eligible? |
|---|---|---|---|---|
| ① clean single-entity | `SELECT` from one table | managed **entity** | no (entity→DTO) | yes |
| ② simple projection | few columns, 1–2 tables | `Tuple` | small record | yes |
| ③ computed / `COALESCE` | fallback logic, casts | `Tuple` | record | maybe (parity-dependent) |
| ④ dynamic-filter / paginated | runtime predicates, `Page<T>` | `Tuple` | record | yes (→ Criteria) |
| ⑤ irreducibly-Postgres | JSON ops, PG-only funcs | `Tuple`/entity | record | **no — stays native** |

## Per-path conversion recipe (Pass 1)

1. Add/locate the projection record (skip for bucket ①). Component order matches the SELECT.
2. Write the MapStruct mapper: `@BeanMapping(ignoreByDefault = true)` + one explicit `@Mapping`
   per field the old SELECT populated — the machine-checkable mirror of the old mapping. Reuse the
   existing `bigIntToStr` / `intToStr` qualifiers for cast columns.
3. Move the method into `XxxRepositoryImpl`: `em.createNativeQuery(<verbatim SQL>)`, assemble the
   row(s) into the projection, map with MapStruct.
4. **Deferred until after the demo / parity confirmation:** delete the now-dead
   `@SqlResultSetMapping`/`@ConstructorResult`/`@NamedNativeQuery` declarations. In Pass 1 they
   stay so revert is trivial.
5. Keep the repository interface method signature identical.

## Demo-safety & revertibility (top priority today)

1. **Isolate in a git worktree.** The demo runs off untouched `sclera2.0/demo`; the migration
   happens in a separate worktree/branch. If MapStruct misbehaves near demo time, switch back to
   the pristine branch — zero code surgery.
2. **Additive, non-destructive.** Recipe step 4 (deletion) is deferred; old mapping declarations
   stay alongside the new path. Revert = drop new files + one-line call-site flip.
3. **Per-path kill switch.** Each migrated method flips between old and new via a single boolean
   config property (e.g. `mapstruct.read.<path>=on|off`, default **off**). At demo time everything
   `off` → 100% original behavior, new code dormant. No revert needed — just don't enable it.
4. **Stop point.** Today targets only 1–2 low-risk paths end-to-end; the catalogue + rails are
   scaffolding. Nothing forces a wide change before the demo.

Three safe end-of-day outcomes: **keep** (flags on, parity verified), **shelve** (flags off, code
dormant), or **revert** (discard the worktree branch). The working app is never at risk.

## Verification (parity harness)

1. **Before/after JSON diff.** Reusing the existing `backups/parity/getdevice-<id>-before.json` /
   `-after.json` convention: hit the endpoint on the old path → save `before`; flip the flag → save
   `after`; byte-diff modulo key order. Any field gained/lost/changed = defect.
2. **Integration test per path.** Follow `DeviceRepositoryIT` / Testcontainers: seed rows, assert
   the mapped DTO field-by-field. Locks parity in CI so a future Pass-2 SQL rewrite can't drift.
3. **Build gate.** `mvn -q -pl sclera-cloud-device-asset compile` must pass (MapStruct generates
   impls; a wrong property name fails the build, not runtime).

## Sequencing & decomposition

- Whole-module scope is too big for one plan. This spec defines strategy + conventions +
  catalogue; implementation proceeds in **per-path slices**, batched by repository, each its own
  small plan.
- **First concrete target (today, demo-safe):** build the rails (DAO custom-impl convention,
  parity step, flag mechanism) and convert **1–2 bucket-① clean single-entity reads** in
  `DeviceRepository` end-to-end behind a flag.
- **Then (after demo):** batch the rest of `DeviceRepository`'s reads by bucket, easy→hard, then
  sibling repositories.
- **Pass 2** is a separate future spec, per path, parity-verified again.

## Risks & mitigations

- **Migration endangers the demo** → worktree isolation + flags default-off + deferred deletion;
  the demo branch is never mutated.
- **Over-mapping changes the response** → `@BeanMapping(ignoreByDefault = true)` + explicit
  per-field `@Mapping`; parity diff + field-by-field IT.
- **Positional `Tuple` assembly drift** → projection record component order documented against the
  SELECT; IT asserts field-by-field.
- **Annotation-processor ordering (Lombok vs MapStruct)** → existing `annotationProcessorPaths`
  config (lombok → lombok-mapstruct-binding → mapstruct-processor) already in `pom.xml`.
- **Scope creep across ~700 sites** → strict per-path slices; nothing batched before the demo.

## Notes

- Builds directly on the proven `getDeviceByDeviceId` slice and its 5 existing mappers
  (`DeviceDtoMapper`, `DeviceOnboardDtoMapper`, `DeviceInfoDtoMapper`, `DeviceByInstrumentDtoMapper`,
  `DeviceImagesDtoMapper`).
- `sclera-vdms-edge-server` (source/reference repo) is read-only and untouched.
