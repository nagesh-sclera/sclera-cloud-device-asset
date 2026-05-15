# Phase 1 Handoff — sclera-cloud-device-asset Extraction

**Date:** 2026-05-12
**Stopping commit:** `572bd57 chore(compile-loop): pass 2 - bulk stub generation`
**Branch:** `main` of `Microservice/sclera-cloud-device-asset/`
**Source repo state:** `sclera-vdms-edge-server` is **untouched** — all extraction work is additive in this new repo.

## What's complete

| Plan task | Status | Commit(s) |
|---|---|---|
| T1 — skeleton dirs | done | `0a3cfec` |
| T2 — pom.xml | done | `0a3cfec` |
| T3 — application.yml + application-test.yml | done | `0a3cfec` |
| T4 — Dockerfile + docker-compose.yml | done | `0a3cfec` |
| T5 — README, CLAUDE.md, .gitignore, migration-notes | done | `0a3cfec` |
| T6 — initial git commit | done | `0a3cfec` |
| T7 — ScleraCloudDeviceAssetApplication.java | done | `3473d2c`, `e96e163` (BOM fix) |
| T8 — AP-C1 controllers (20 files) | done | `baa639b` |
| T9 — AP-C1 services (23 files) | done | `1d2bff9` |
| T10 — AP-C1 repositories (25 files) | done | `55c7c9b` |
| T11 — AP-C1 entity models (24 files) | done | `466bdc4` |
| T12 — AP-C1 DTOs (21 files) | done | `8016a1e` |
| T13 — Shared infra (config, exception, enums, utils + ScleraTools.jar) | done | `1186aec` |
| T14 — First compile + error landscape | done | `cc18490` |
| T15 — Compile loop | **in progress** | `05d63bc`, `572bd57` (2 waves of ~5–7 estimated) |
| T16–T22 | not started | — |

## Compile state after wave 2

```
mvn clean compile → 371 errors, ~40 unique missing symbols
```

The pattern stabilized — each pass surfaces a smaller wave. The remaining work is the same shape as wave 1+2: copy AP-C1 files the original whitelist missed, generate empty stubs for Bucket-C/D, repeat.

## Outstanding missing symbols (after wave 2)

Categorized for the next operator:

### AP-C1 files missed by the original whitelist — copy from source

| Symbol | Source path | Notes |
|---|---|---|
| `ClientBarCodeService` | `service/ClientBarCodeService.java` | Paired with `ClientBarCode` model (already in AP-C1) |
| `ConditionsService` | `service/ConditionsService.java` | Used by `DeviceConditionsService` — likely AP-C1 |
| `Technician`, `TechnicianService`, `TechnicianDTO` | `models/Technician.java`, etc. | Used by `DeviceTechnicianAISuggestionService` (AP-C1). Bring as AP-C1. |
| `MultiDeviceDTO`, `PowerSourceTopologyDTO`, `TagDeviceOrLocationDTO`, `AllSensorsDTO`, `AlertProfileDTO` | `dto/` | Used by AP-C1 controllers; check each and copy if AP-C1-domain |
| `DeviceQueryRepository` reference | `queryrepository/DeviceQueryRepository.java` | **Already copied** in wave 2 — likely an import path mismatch; check `service/DeviceService.java` import line |

### Bucket-C — write empty stubs (mirror FQN, `@Service`, no methods)

| Symbol | Target microservice |
|---|---|
| `ArchivedRecordService`, `HistoryRepository`, `SyslogService` | AP-C6 analytics |
| `InspectionRecordService`, `GlobalChecklistConditionsService` | AP-C4 inspection |
| `InventoryDeviceRepository` | AP-C8 inventory |
| `AssetMapperService` | AP-C2 / edge mapper |
| `CallFlowRule*` (DTO, Repository, ConditionDTO, ConditionRepository) | AP-C5 alerts call-flow routing |
| `QrCodeRepository`, `ClientQrCodeRepository` | AP-C1edge or stub |

### Bucket-C/D DTOs — copy or stub

`BacnetAdvanceExportExcelDTO`, `ConditionsAdvanceExportExcelDto`, `SiemensAdvanceExportExcelDTO`, `SiemensBmsExportDTO`, `Product_SnmpDTO`, `ProductDTO`, `CallStatusDTO`, `ResponseDTO` — these are excel-export DTOs for sensor integration. Either copy from source as Bucket-B helpers or stub as empty classes. Quick check: where are they referenced? If only in Bucket-C call paths that were trimmed in wave 1, they can be stubbed empty.

### `VdmsService`

Listed both as Bucket-D-stubbed (wave 2) and still missing. Likely a stale call-site reference in a file the agent didn't reach in wave 1. Grep `VdmsService` to find the remaining call site and either re-stub the call or remove the line.

## Pattern that worked (replicate for next waves)

For each compile pass:

```powershell
# 1. Set env
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"
$env:Path = "C:\Users\DhanushVasanth\.tools\apache-maven-3.9.15\bin;$env:JAVA_HOME\bin;$env:Path"

# 2. Compile, capture log
cd "C:\...\sclera-cloud-device-asset"
mvn -B -q clean compile 2>&1 | Tee-Object -FilePath "C:\tmp\mvn-pass-N.log"

# 3. Extract unique missing symbols
Select-String -Path "C:\tmp\mvn-pass-N.log" -Pattern "cannot find symbol" -Context 0,1 |
  ForEach-Object { $_.Context.PostContext } | Select-String "symbol:" |
  ForEach-Object { $_.Line.Trim() } | Sort-Object -Unique
```

For each missing symbol, decide:

- **Bucket B (AP-C1 file missed by whitelist):** copy from source using `Copy-Item "src\main\java\<fqn-path>.java" "..\sclera-cloud-device-asset\src\main\java\<fqn-path>.java"`. Log in `compile-fixes.md`.
- **Bucket C (other microservice):** create empty `@Service` stub at the original FQN path. Use the template at `migration-notes/phase-1-handoff.md` (this file, below). Log in `stubbed-services.md`.
- **Bucket D (edge-only):** find the call site, comment out the `@Autowired` field and any method invocation, replace return values with safe defaults. Log in `compile-fixes.md`.

After each wave, commit:

```
git add -A
git commit -m "chore(compile-loop): pass N - <one-line summary>"
```

## Stub class template (Bucket C, empty)

```java
package io.sclera.service;   // mirror the original FQN package

import org.springframework.stereotype.Service;

/** STUB: replace with remote call to AP-Cx <microservice-name> */
@Service
public class FooService {
    // Add methods on demand as the compile loop surfaces them.
    // When you add a method, emit a WARN log using io.sclera.utils.StubLog.warn().
}
```

When a call site needs a method, add it with safe-default body:

```java
public java.util.List<Object> findByDeviceId(Long id) {
    io.sclera.utils.StubLog.warn(FooService.class.getName(), "findByDeviceId", "AP-Cx");
    return java.util.Collections.emptyList();
}
```

`StubLog` is at `src/main/java/io/sclera/utils/StubLog.java` (committed in `572bd57`).

## After T15 is green

Continue to T16 (DB create + smoke boot), T17 (manual smoke), T18–T22 (test pack) per the plan at `../sclera-vdms-edge-server/docs/superpowers/plans/2026-05-12-sclera-cloud-device-asset.md`.

## Known gotchas

1. **UTF-8 BOM on Set-Content** — PowerShell 5.1's `Set-Content -Encoding utf8` writes a BOM that breaks `javac` on `.java` files. For Java sources, use `[System.IO.File]::WriteAllText($path, $content, [System.Text.UTF8Encoding]::new($false))` or PowerShell 7+'s `-Encoding utf8NoBOM`. The bug bit `ScleraCloudDeviceAssetApplication.java` (T7) and was fixed in `e96e163`. The bulk-stub script in wave 2 used Bash heredoc which is BOM-free, so wave-2 stubs are clean.
2. **JAVA_HOME** — not set globally on this machine. Set per shell.
3. **Maven** — not on PATH globally. Located at `C:\Users\DhanushVasanth\.tools\apache-maven-3.9.15\bin\`.
4. **Plan's "around 35" dependency estimate** for `pom.xml` — actual is 41 (h2 + spring-security-test added on top of source's 39 retained). Not a defect.
5. **`spring-boot-starter-oauth2-resource-server:3.0.6`** on Spring Boot 2.6.5 — inherited verbatim from source. If runtime context-load fails, this is the first place to look.
6. **`@Valid` validation starter** — `spring-boot-starter-validation` not in pom. If any controller uses `@Valid`, add it during the next compile pass.
7. **`application-test.yml` JWT issuer-uri** — `http://localhost/test-issuer` is unreachable. The T18 context-load smoke test will fail unless a `TestSecurityConfig` stubs `JwtDecoder` in test scope. Address during T18.

## Source repo invariant

`sclera-vdms-edge-server` is **read-only** throughout this extraction. Verify with `git status` inside it; only the pre-existing untracked files (`.claude/`, `CLAUDE.md`, `PLUGINS.md`, `Sclera_decomposition.xlsx`) plus the spec/plan/handoff temp logs at the source root (`target-tmp-compile-pass-*.log`, `target-tmp-missing-symbols-*.txt`) should appear. No tracked file should be modified.

## Memories saved for future sessions

- `feedback_copy-not-move.md` — source repo strictly read-only during extraction; words like "drop"/"delete" target the new repo only.
- `feedback_powershell-utf8-bom.md` — PS 5.1 `Set-Content -Encoding utf8` writes a BOM; use BOM-free write for `.java` files only.
