# JavaDoc Implementation — sclera-cloud-device-asset

**Date:** 2026-06-04
**Status:** Approved — pilot in progress

## Goal

Add JavaDoc across the public API and data layers of `sclera-cloud-device-asset`,
following the convention already established in the `io.sclera.client` package, and
wire up the Maven Javadoc plugin so browsable HTML can be generated on demand.

## Scope

In scope (packages under `io.sclera`):

| Package | Total | Already documented | To document | Depth |
|---|---|---|---|---|
| `controller` | 19 | 1 | 18 | class + every public method |
| `service` | 49 | 19 | 30 | class + every public method |
| `Repository` | 47 | 13 | 34 | class + query methods |
| `queryrepository` | 12 | 0 | 12 | class + query methods |
| `dto` | 159 | 42 | 117 | class-level summary only |
| `models` | 70 | 17 | 53 | class-level summary only |
| **Total** | | | **~264** | |

Out of scope: `auth`, `config`, `client` (already done), `enums`, `exception`,
`integration`, `utils`, `proxy`, `sockets`, `stubs`, `websocket`, and the application
entrypoint. Existing JavaDoc anywhere is left untouched. No source logic is changed.

## Depth / house style

Follow the existing convention in `io.sclera.client` (e.g. `AlertClient.java`):
concise, factual, present-tense.

- **Logic class** (controller/service/repo): class-level block describing purpose,
  key collaborators, and notable caveats.
- **Data class** (DTO/model): one–two line class-level summary — what it represents and
  where it is used. No per-field, getter, or setter docs.
- **Logic method:** one-line summary + `@param` for each argument, `@return` when
  non-void, `@throws` for checked or documented runtime exceptions.
- **Repository query method:** summary of what it fetches/matches + `@param`/`@return`.

## Maven Javadoc plugin

- Add `maven-javadoc-plugin` to `pom.xml`, configured for **on-demand** generation via
  `mvnw javadoc:javadoc`. **Not** bound to the main build lifecycle.
- **`<doclint>none</doclint>`** — required so the build does not fail on the ~245 files
  still undocumented after the pilot, or on any malformed tag. Keeps `compile` and CI green.
- Pin the plugin version to whatever the current build resolves to. Verify
  `mvnw javadoc:javadoc` actually runs on this machine.

> Note: CLAUDE.md states Java 11 / Boot 2.6.5; project memory records an in-flight
> migration to Java 21 / Boot 4.0.6. Use the version the active build resolves; verify
> before claiming done.

## Execution — phased

### Phase 1 — Pilot (this session)

Fully document the `controller` package (18 undocumented of 19 files) as the reference
sample, and wire up the Maven Javadoc plugin.

Definition of done:

1. All 18 controller files documented to the templates above.
2. `mvnw -q compile` still passes.
3. `mvnw -q javadoc:javadoc` generates HTML for the controllers without failing.
4. Existing docs elsewhere untouched; no source logic changed.

### Phase 2 — Scale (deferred)

After the pilot style is approved, document the remaining ~245 files in
`service`/`dto`/`models`/`Repository`/`queryrepository`. Choose execution at that point:

- **Multi-agent workflow** (~3–5M tokens, ~30–60 min mostly unattended): parallel
  batches by package + a verify pass. Fastest wall-clock.
- **Package-by-package manual**: one package per batch, reviewed before continuing.

Estimated total for the full job: ~3–5M tokens (combined input+output). The 30 `service`
files dominate the cost; the 170 `dto`/`models` files dominate the count but are cheap.

## Constraints

- Do not modernize source or change logic.
- Source repo `sclera-vdms-edge-server` is read-only.
- Many `service`/`models` files are delombok-generated; document the visible public
  surface, do not reformat generated code.
