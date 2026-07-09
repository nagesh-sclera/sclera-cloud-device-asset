# Removal of `sclera-cloud-vdms` (2026-07-08)

The legacy cloud service `sclera-cloud-vdms` has been deleted from the repo. By the time
of removal it was **already fully decoupled** — not in the Maven reactor (`pom.xml`), not in
`docker-compose*.yml`, not a Dapr service-invocation target, and referenced by no URL/config
property. Its QR/NFC/Barcode/tagging feature set had already been ported into
`sclera-cloud-device-asset` as self-contained local code, and VDMS reads had been re-pointed
to the separate live `sclera-vdms-service` (Dapr app-id `vdms-service`). This change therefore
**migrated nothing new**; it removed the orphaned directory and the inert cloud-sync remnants
left behind by the earlier migrations.

> Note on naming: `sclera-cloud-vdms` (removed) ≠ `sclera-vdms-service` (kept, live, in the
> reactor + compose). The `sclera-edge` / `sclera-integrations` / `sclera-alerts` / etc. Dapr
> app-ids that `device-asset` still calls are **other live platform services** (trimmed only
> from the local dev compose) and were intentionally left untouched.

## Removed

1. **Directory** `sclera-cloud-vdms/` (366 tracked files) — `git rm -r`.
2. **Dead barcode cloud-sync** (target was `sclera-edge`, returned empty, all call sites already
   commented out):
   - `service/ClientBarCodeService`: `syncAllClientBarCode`, `syncClientBarCode`, the private
     helpers `updateIsDeletedForAllClientBarCode` / `deleteOldClientBarCode`, and the
     `APICallClient apiCallService` field + import. Class Javadoc updated (local-only, no cloud).
   - `interfaces/ClientBarCodeServiceInterface`: `syncAllClientBarCode` / `syncClientBarCode` decls.
   - `client/APICallClient`: `getAllClientBarCodeByVdmsId`, `getSyncedClientBarCodeByVdmsId`.
     (`APICallClient` itself is kept — it serves live `sclera-edge` calls.)
3. **VDMS access-token no-op chain** (was a `// no-op`):
   - `client/APICallClient.getVdmsAccessToken`
   - `service/touchscreen/VdmsService.getVdmsAccessToken` + its call in `startVdmsService`
     + the now-unused `APICallClient apiCallService` field/import.
   - `interfaces/VdmsServiceInterface.getVdmsAccessToken`
4. **Cloud-sync scheduler jobs** (WARN-only stubs) removed from the scheduler contract:
   - `scheduler/DeviceAssetJobHandlers`: `vdmsSystemHealth`, `qrcodeNfcBarcodeSync`,
     `syncAssetCountToCloud`.
   - `scheduler/TriggerDispatchSubscriber`: removed from the `OWNED` set and the dispatch switch.
   - `test/.../TriggerDispatchSubscriberTest`: repointed the throw-test to `deviceDndEnable`;
     removed `perVdmsJob_passesVdmsIdToHandler` (its only target `vdmsSystemHealth` is gone and no
     owned per-VDMS job remains — the per-VDMS dispatch plumbing in `run()` is retained for future use).
5. **Provenance comments** — "Ported from sclera-cloud-vdms …" reworded to "the legacy VDMS
   service's …" in the ported QR files (controllers/admin, `models/QrCodeTemplate`,
   `dto/QrCodeTemplateDTO`, `Repository/QrCodeTemplateRepository`, `service/QrCodeTemplateService`).

## Deliberately NOT changed

- **Historical design docs/plans** under `docs/superpowers/**` and `migration-notes/qr-self-contained.md`
  still name `sclera-cloud-vdms` — they are an accurate audit trail of past work.
- **`EssentialService`** large commented-out legacy region (mixed QR/NFC/barcode/skill/interface/
  model-script code) — inert comment text, heterogeneous, out of scope for "cloud-sync remnants";
  left as-is.
- All live inter-service clients (`sclera-edge`, `sclera-integrations`, `sclera-alerts`,
  `sclera-inspection`, `sclera-audit`, `sclera-inventory`, `sclera-identity`, `sclera-workorders`)
  and the `vdms-service` clients — kept.

## Validation

- `mvnw test-compile` (whole reactor): **BUILD SUCCESS** (dapr-commons, device-asset,
  vdms-service, workorders, api-gateway, aggregator).
- Unit tests: `TriggerDispatchSubscriberTest` (3), `ClientBarCodeServiceTest` (5),
  `VdmsServiceTest` (15) — all pass. Testcontainers ITs remain blocked on this machine (Docker 29);
  run in CI.

## Cross-service follow-up (manual)

The central scheduler should stop emitting the three removed job names
(`vdmsSystemHealth`, `qrcodeNfcBarcodeSync`, `syncAssetCountToCloud`). After this change,
triggers for those names are silently ignored by device-asset (no handler, no `SUCCESS`
result published) instead of returning a stub success.
