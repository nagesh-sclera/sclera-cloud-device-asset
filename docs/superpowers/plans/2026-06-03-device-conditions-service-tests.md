# DeviceConditionsService Test Coverage Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development or superpowers:executing-plans. Steps use checkbox (`- [ ]`) syntax. This plan is intentionally ONE task (a single test file) to minimize compile/run/token overhead — inline execution is recommended.

**Goal:** Add pure-Mockito unit test coverage for the main methods of `io.sclera.service.DeviceConditionsService`, establishing a reusable service-test pattern.

**Architecture:** One JUnit 5 + Mockito test class. The service's four collaborators are mocked (`@Mock`) and injected (`@InjectMocks`); no Spring context, no database. Behavior is verified via return values and Mockito interaction verification. Tests cover each public method's main path plus the key branches of the branch-heavy methods.

**Tech Stack:** JUnit 5 (Jupiter), Mockito (`MockitoExtension`, strict stubbing), AssertJ — all already on the classpath via `spring-boot-starter-test`.

---

## Context for the implementer

- Repo root: `C:\Users\DhanushVasanth\Desktop\AssetManagement POD\Microservice123\sclera-cloud-device-asset`. The module is the **nested** `sclera-cloud-device-asset/` directory (Java 21, Maven, `./mvnw`).
- Run tests on Windows via PowerShell: `$env:JAVA_HOME = "C:\Users\DhanushVasanth\.jdks\corretto-21.0.8"; Set-Location "sclera-cloud-device-asset\sclera-cloud-device-asset"; .\mvnw.cmd -Dtest=DeviceConditionsServiceTest test`. Judge success by the Maven `Tests run:` / `BUILD SUCCESS` lines, NOT PowerShell `$?` (native-command stderr — e.g. the Mockito self-attach notice — flips `$?` to false even on success). The Bash tool's `./mvnw` also works with JAVA_HOME set.
- The unit under test: `src/main/java/io/sclera/service/DeviceConditionsService.java`. It uses field injection (`@Autowired` on package-private fields) of: `DeviceConditionsRepository deviceConditionsRepository`, `AlertProfileClient alertProfileClient`, `DeviceService deviceService`, `JobSchedulerService jobSchedulerService`. `@InjectMocks` injects mocks into these fields by type via reflection.
- This is a test for EXISTING code (not TDD red-green): write the tests, run them, and they should pass against the current implementation. If a test fails, first confirm the test's expectation matches the actual code behavior before changing anything.

### Signatures the tests rely on (already verified)
- Repo: `int deviceConditionById(String)`, `DeviceConditionsDTO getDeviceConditionsById(String)`, `Set<DeviceConditionsDTO> getDeviceConditions(String deviceId)`, `void addDeviceConditions(String id, String alert_condition, String device_id, String alert_profile_id, Integer trigger_time, String priority, String start_time, String end_time, Integer max_alert_count, Integer alert_count_enabled, Integer schedule, String schedule_conditions, Integer alert_count_time, Boolean last_alerted, String alert_message)` (15 args), `void updateDeviceConditions(String id, String alert_condition, String device_id, String alert_profile_id, Integer trigger_time, String priority, String start_time, String end_time, Integer alert_count, Integer max_alert_count, Integer alert_count_enabled, Integer schedule, String schedule_conditions, Integer alert_count_time, Boolean last_alerted, String alert_message)` (16 args), `void deleteById(String)`, `void updateLastAlertedDetails(String, BigInteger, Boolean, Integer)`, `void updateLastAlertedTimestamp(String)`, `void updateAlertProfileId(String)`, `void resetDeviceConditions(String, Integer, Boolean)`, `Integer getAlertCount(String)`, `String getDeviceConditionIdByDeviceId(String)`, `Set<DeviceConditionsDTO> getDeviceConditionsForAiCall(String)`, `void updateAlertCountByConditionId(String, int)`, `DeviceConditionsDTO getDeviceConditionsByIdForAiCall(String)`.
- `AlertProfileClient`: `AlertProfileDTO getAlertProfileDetailsById(String, String, String)`.
- `DeviceService`: `DeviceDTO getDeviceDetails(String)`, `void getDeviceConditionStatus(String, Integer)`, `void deleteDeviceAlertJob(String)`, `void getAiCallDeviceOfflineConditionStatus(String, Integer)`.
- `JobSchedulerService`: `ScheduledJobDTO getScheduledJobByConditionId(String)`.
- `DeviceConditionsDTO`: no-arg constructor + setters/getters (`setId/getId`, `setDevice_id`, `setAlert_condition`, `setAlert_profile_id`, `setAlert_profile`, `setStart_time`, `setEnd_time`, `setSchedule`, `setMax_alert_count`, `setAlert_count_enabled`, `setTrigger_time`, `setAlert_count`, `setLast_alerted`, `getAlert_profile`). Also a 14-arg constructor used internally by `shareDeviceConditions`.
- `DeviceDTO`: `getId()`, `Integer getStatus()` (use a Mockito mock — its no-arg constructor is not relied upon).
- `ShareConditionsDTO`: no-arg ctor + `setDevices(List<DeviceDTO>)`, `setCondition_method(String)`, `setDeviceConditions(List<DeviceConditionsDTO>)`.

---

## Task 1: DeviceConditionsServiceTest

**Files:**
- Create: `sclera-cloud-device-asset/src/test/java/io/sclera/service/DeviceConditionsServiceTest.java`

> Path is relative to the nested module dir `sclera-cloud-device-asset/sclera-cloud-device-asset/`.

- [ ] **Step 1: Write the test file**

```java
package io.sclera.service;

import io.sclera.Repository.DeviceConditionsRepository;
import io.sclera.client.AlertProfileClient;
import io.sclera.dto.AlertProfileDTO;
import io.sclera.dto.DeviceConditionsDTO;
import io.sclera.dto.DeviceDTO;
import io.sclera.dto.ScheduledJobDTO;
import io.sclera.dto.ShareConditionsDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeviceConditionsServiceTest {

    @Mock DeviceConditionsRepository deviceConditionsRepository;
    @Mock AlertProfileClient alertProfileClient;
    @Mock DeviceService deviceService;
    @Mock JobSchedulerService jobSchedulerService;

    @InjectMocks DeviceConditionsService service;

    // ---- helpers ----------------------------------------------------------

    private DeviceConditionsDTO cond(String id, String deviceId) {
        DeviceConditionsDTO c = new DeviceConditionsDTO();
        c.setId(id);
        c.setDevice_id(deviceId);
        return c;
    }

    /** A condition with the fields the upsert change-detection block dereferences set,
     *  so the .equals() comparisons don't NPE. */
    private DeviceConditionsDTO fullCond(String id, String deviceId, String alertCondition) {
        DeviceConditionsDTO c = cond(id, deviceId);
        c.setAlert_condition(alertCondition);
        c.setStart_time("08:00");
        c.setEnd_time("18:00");
        c.setSchedule(1);
        c.setMax_alert_count(5);
        c.setAlert_count_enabled(1);
        return c;
    }

    private DeviceDTO mockDevice(String id) {
        DeviceDTO d = mock(DeviceDTO.class);
        when(d.getId()).thenReturn(id);
        return d;
    }

    // ---- pass-through methods --------------------------------------------

    @Test
    void getAlertCount_delegatesToRepository() {
        when(deviceConditionsRepository.getAlertCount("dev1")).thenReturn(5);
        assertThat(service.getAlertCount("dev1")).isEqualTo(5);
    }

    @Test
    void updateAlertProfileId_delegatesToRepository() {
        service.updateAlertProfileId("ap1");
        verify(deviceConditionsRepository).updateAlertProfileId("ap1");
    }

    @Test
    void updateLastAlertedDetails_delegatesWithArgs() {
        BigInteger ts = BigInteger.valueOf(123L);
        service.updateLastAlertedDetails("c1", ts, true, 2);
        verify(deviceConditionsRepository).updateLastAlertedDetails("c1", ts, true, 2);
    }

    @Test
    void getDeviceConditionsForAiCall_returnsRepoResult() {
        Set<DeviceConditionsDTO> set = new HashSet<>(Set.of(cond("c1", "d1")));
        when(deviceConditionsRepository.getDeviceConditionsForAiCall("d1")).thenReturn(set);
        assertThat(service.getDeviceConditionsForAiCall("u", "v", "dock", "d1")).isSameAs(set);
    }

    @Test
    void getDeviceConditionsByIdForAiCall_returnsRepoResult() {
        DeviceConditionsDTO c = cond("c1", "d1");
        when(deviceConditionsRepository.getDeviceConditionsByIdForAiCall("c1")).thenReturn(c);
        assertThat(service.getDeviceConditionsByIdForAiCall("u", "v", "c1")).isSameAs(c);
    }

    // ---- guard branch -----------------------------------------------------

    @Test
    void updateAlertCountByConditionId_valid_updates() {
        service.updateAlertCountByConditionId("c1", 3);
        verify(deviceConditionsRepository).updateAlertCountByConditionId("c1", 3);
    }

    @Test
    void updateAlertCountByConditionId_invalid_doesNotUpdate() {
        service.updateAlertCountByConditionId(null, 3);
        service.updateAlertCountByConditionId("c1", 0);
        verify(deviceConditionsRepository, never()).updateAlertCountByConditionId(anyString(), anyInt());
    }

    // ---- enrichment branch ------------------------------------------------

    @Test
    void getDeviceConditions_enrichesWithAlertProfileWhenProfileIdSet() {
        DeviceConditionsDTO c = cond("c1", "d1");
        c.setAlert_profile_id("ap1");
        when(deviceConditionsRepository.getDeviceConditions("d1")).thenReturn(new HashSet<>(Set.of(c)));
        AlertProfileDTO profile = mock(AlertProfileDTO.class);
        when(alertProfileClient.getAlertProfileDetailsById(null, null, "ap1")).thenReturn(profile);

        service.getDeviceConditions("u", "v", "dock", "d1");

        verify(alertProfileClient).getAlertProfileDetailsById(null, null, "ap1");
        assertThat(c.getAlert_profile()).isSameAs(profile);
    }

    @Test
    void getDeviceConditions_skipsEnrichmentWhenProfileIdNull() {
        DeviceConditionsDTO c = cond("c1", "d1"); // alert_profile_id is null
        when(deviceConditionsRepository.getDeviceConditions("d1")).thenReturn(new HashSet<>(Set.of(c)));

        service.getDeviceConditions("u", "v", "dock", "d1");

        verify(alertProfileClient, never()).getAlertProfileDetailsById(any(), any(), any());
    }

    @Test
    void getDeviceConditionsById_enrichesWhenProfileIdSet() {
        DeviceConditionsDTO c = cond("c1", "d1");
        c.setAlert_profile_id("ap1");
        when(deviceConditionsRepository.getDeviceConditionsById("c1")).thenReturn(c);
        when(alertProfileClient.getAlertProfileDetailsById(null, null, "ap1")).thenReturn(mock(AlertProfileDTO.class));

        service.getDeviceConditionsById("u", "v", "c1");

        verify(alertProfileClient).getAlertProfileDetailsById(null, null, "ap1");
    }

    // ---- delete methods ---------------------------------------------------

    @Test
    void deleteAllDeviceConditions_deletesEachByid() {
        Set<DeviceConditionsDTO> set = new LinkedHashSet<>(List.of(cond("c1", "d1"), cond("c2", "d1")));
        when(deviceConditionsRepository.getDeviceConditions("d1")).thenReturn(set);

        service.deleteAllDeviceConditions("u", "v", "d1");

        verify(deviceConditionsRepository).deleteById("c1");
        verify(deviceConditionsRepository).deleteById("c2");
    }

    @Test
    void deleteDeviceConditions_deletesEachByid() {
        Set<DeviceConditionsDTO> set = new LinkedHashSet<>(List.of(cond("c1", "d1"), cond("c2", "d1")));

        service.deleteDeviceConditions("u", "v", set);

        verify(deviceConditionsRepository).deleteById("c1");
        verify(deviceConditionsRepository).deleteById("c2");
    }

    // ---- resetDeviceConditions -------------------------------------------

    @Test
    void resetDeviceConditions_resetsAndUpdatesStatusWhenDevicePresent() {
        DeviceConditionsDTO c = cond("c1", "d1");
        DeviceDTO dev = mockDevice("d1");
        when(dev.getStatus()).thenReturn(2);
        when(deviceService.getDeviceDetails("d1")).thenReturn(dev);

        service.resetDeviceConditions("u", "v", "dock", new HashSet<>(Set.of(c)));

        verify(deviceConditionsRepository).resetDeviceConditions("c1", 0, false);
        verify(deviceService).getDeviceConditionStatus("d1", 2);
    }

    @Test
    void resetDeviceConditions_skipsStatusWhenDeviceNull() {
        DeviceConditionsDTO c = cond("c1", "d1");
        when(deviceService.getDeviceDetails("d1")).thenReturn(null);

        service.resetDeviceConditions("u", "v", "dock", new HashSet<>(Set.of(c)));

        verify(deviceConditionsRepository).resetDeviceConditions("c1", 0, false);
        verify(deviceService, never()).getDeviceConditionStatus(anyString(), any());
    }

    // ---- upsertDeviceConditions (branchy) --------------------------------

    @Test
    void upsertDeviceConditions_newConditionInserts() {
        DeviceConditionsDTO incoming = fullCond(null, "d1", "temp>5"); // id null -> insert
        when(deviceService.getDeviceDetails("d1")).thenReturn(null);

        service.upsertDeviceConditions("u", "v", "dock", new HashSet<>(Set.of(incoming)));

        // new id is generated; device_id and last_alerted=false are fixed
        verify(deviceConditionsRepository).addDeviceConditions(
                anyString(), eq("temp>5"), eq("d1"), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), eq(false), any());
    }

    @Test
    void upsertDeviceConditions_existingChange_resetsCountAndUpdates() {
        DeviceConditionsDTO incoming = fullCond("c1", "d1", "temp>10"); // differs from existing
        DeviceConditionsDTO existing = fullCond("c1", "d1", "temp>5");
        existing.setAlert_count(7);
        existing.setLast_alerted(true);
        when(deviceConditionsRepository.deviceConditionById("c1")).thenReturn(1);
        when(deviceConditionsRepository.getDeviceConditionsById("c1")).thenReturn(existing);
        when(deviceService.getDeviceDetails("d1")).thenReturn(null);

        service.upsertDeviceConditions("u", "v", "dock", new HashSet<>(Set.of(incoming)));

        // alert_condition changed -> alert_count reset to 0, last_alerted false
        verify(deviceConditionsRepository).updateDeviceConditions(
                eq("c1"), eq("temp>10"), eq("d1"), any(), any(), any(), eq("08:00"), eq("18:00"),
                eq(0), eq(5), eq(1), eq(1), any(), any(), eq(false), any());
    }

    @Test
    void upsertDeviceConditions_triggerTimeChanged_deletesAlertJob() {
        DeviceConditionsDTO incoming = fullCond("c1", "d1", "temp>5");
        incoming.setTrigger_time(20);
        DeviceConditionsDTO existing = fullCond("c1", "d1", "temp>5");
        existing.setTrigger_time(10); // differs -> triggers job deletion path
        when(deviceConditionsRepository.deviceConditionById("c1")).thenReturn(1);
        when(deviceConditionsRepository.getDeviceConditionsById("c1")).thenReturn(existing);
        when(jobSchedulerService.getScheduledJobByConditionId("c1")).thenReturn(mock(ScheduledJobDTO.class));
        when(deviceService.getDeviceDetails("d1")).thenReturn(null);

        service.upsertDeviceConditions("u", "v", "dock", new HashSet<>(Set.of(incoming)));

        verify(deviceService).deleteDeviceAlertJob("c1");
    }

    // ---- upsertDeviceConditionsForAiCall ---------------------------------

    @Test
    void upsertDeviceConditionsForAiCall_newCondition_inserts() {
        DeviceConditionsDTO incoming = fullCond(null, "d1", "temp>5");
        when(deviceService.getDeviceDetails("d1")).thenReturn(null);
        when(deviceConditionsRepository.getDeviceConditionIdByDeviceId("d1")).thenReturn(null);

        service.upsertDeviceConditionsForAiCall("u", "v", "dock", new HashSet<>(Set.of(incoming)));

        verify(deviceConditionsRepository).addDeviceConditions(
                anyString(), eq("temp>5"), eq("d1"), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), eq(false), any());
    }

    @Test
    void upsertDeviceConditionsForAiCall_alertCountAtThreshold_deletesThenReAdds() {
        DeviceConditionsDTO incoming = fullCond("c1", "d1", "temp>5");
        when(deviceService.getDeviceDetails("d1")).thenReturn(null);
        when(deviceConditionsRepository.getDeviceConditionIdByDeviceId("d1")).thenReturn("existingId");
        when(deviceConditionsRepository.getAlertCount("d1")).thenReturn(3);
        when(deviceConditionsRepository.getDeviceConditionsForAiCall("d1"))
                .thenReturn(new HashSet<>(Set.of(cond("old1", "d1"))));

        service.upsertDeviceConditionsForAiCall("u", "v", "dock", new HashSet<>(Set.of(incoming)));

        verify(deviceConditionsRepository).deleteById("old1");
        verify(deviceConditionsRepository).addDeviceConditions(
                anyString(), eq("temp>5"), eq("d1"), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), eq(false), any());
    }

    // ---- shareDeviceConditions -------------------------------------------

    @Test
    void shareDeviceConditions_add_upsertsMappedConditionOntoTargetDevice() {
        DeviceConditionsDTO template = fullCond(null, "ignored", "temp>5");
        ShareConditionsDTO share = new ShareConditionsDTO();
        share.setDevices(List.of(mockDevice("dev1")));
        share.setCondition_method("add");
        share.setDeviceConditions(List.of(template));
        when(deviceService.getDeviceDetails("dev1")).thenReturn(null);

        service.shareDeviceConditions("u", "v", "dock", share);

        // mapped condition has device_id=dev1, id null -> insert via upsert
        verify(deviceConditionsRepository).addDeviceConditions(
                anyString(), eq("temp>5"), eq("dev1"), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), eq(false), any());
    }

    @Test
    void shareDeviceConditions_replace_deletesExistingThenUpserts() {
        DeviceConditionsDTO existingOnDevice = cond("oldC", "dev1");
        when(deviceConditionsRepository.getDeviceConditions("dev1"))
                .thenReturn(new HashSet<>(Set.of(existingOnDevice)));
        DeviceConditionsDTO template = fullCond(null, "ignored", "x>1");
        ShareConditionsDTO share = new ShareConditionsDTO();
        share.setDevices(List.of(mockDevice("dev1")));
        share.setCondition_method("replace");
        share.setDeviceConditions(List.of(template));
        when(deviceService.getDeviceDetails("dev1")).thenReturn(null);

        service.shareDeviceConditions("u", "v", "dock", share);

        verify(deviceConditionsRepository).deleteById("oldC");
        verify(deviceConditionsRepository).addDeviceConditions(
                anyString(), eq("x>1"), eq("dev1"), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), eq(false), any());
    }

    @Test
    void shareDeviceConditions_swallowsDownstreamException() {
        ShareConditionsDTO share = new ShareConditionsDTO();
        share.setDevices(List.of(mockDevice("dev1")));
        share.setCondition_method("replace");
        share.setDeviceConditions(List.of());
        when(deviceConditionsRepository.getDeviceConditions("dev1")).thenThrow(new RuntimeException("boom"));

        assertThatCode(() -> service.shareDeviceConditions("u", "v", "dock", share))
                .doesNotThrowAnyException();
    }
}
```

- [ ] **Step 2: Run the test**

Run (PowerShell): `$env:JAVA_HOME = "C:\Users\DhanushVasanth\.jdks\corretto-21.0.8"; Set-Location "C:\Users\DhanushVasanth\Desktop\AssetManagement POD\Microservice123\sclera-cloud-device-asset\sclera-cloud-device-asset"; .\mvnw.cmd -Dtest=DeviceConditionsServiceTest test`

Expected: `Tests run: 22, Failures: 0, Errors: 0` and `BUILD SUCCESS`.

- [ ] **Step 3: Triage any failure (existing code is the source of truth)**

If a test fails or errors, diagnose against the actual `DeviceConditionsService` behavior, in this likely order:
- **`UnnecessaryStubbingException`** (Mockito strict): a `when(...)` was set but never called on that path. Remove or correct the stub so it matches the code path actually exercised.
- **`NullPointerException` inside `upsertDeviceConditions`**: the change-detection block dereferences `getStart_time()/getEnd_time()/getAlert_condition()` — ensure both `existing` and `incoming` use `fullCond(...)` (which sets those). Do NOT weaken the assertion to hide a real NPE.
- **Argument-matcher mismatch on `addDeviceConditions`/`updateDeviceConditions`**: re-count positions against the signatures in the Context section (add = 15 args, update = 16 args) and adjust `eq(...)`/`any()` positions. All arguments must be matchers (Mockito rule) — keep every position a matcher.
- A genuine assertion mismatch means the test's expectation is wrong about the code — fix the test to match what the code does (this is characterization testing of existing behavior).

Re-run Step 2 until green. Keep the test count at ~22 (don't delete cases to pass).

- [ ] **Step 4: Commit**

```bash
git add "sclera-cloud-device-asset/src/test/java/io/sclera/service/DeviceConditionsServiceTest.java"
git commit -m "test: add unit coverage for DeviceConditionsService main methods"
```
Add the trailer `Co-Authored-By: Claude Opus 4.8 (1M context) <noreply@anthropic.com>`. From PowerShell keep the message plain ASCII and use multiple `-m` flags rather than a here-string.

---

## Self-Review

**Spec coverage:** every method row in the spec table maps to a test above — pass-throughs (getAlertCount, updateAlertProfileId, getDeviceConditionsForAiCall, getDeviceConditionsByIdForAiCall, updateLastAlertedDetails), the `updateAlertCountByConditionId` guard (valid + invalid), enrichment (`getDeviceConditions` set/null + `getDeviceConditionsById`), deletes (all + selective), `resetDeviceConditions` (device present/null), `upsertDeviceConditions` (insert / existing-change-reset / trigger-time-delete), `upsertDeviceConditionsForAiCall` (new / threshold), `shareDeviceConditions` (add / replace / exception-swallow). The spec's "error handling" case = `shareDeviceConditions_swallowsDownstreamException`. ✓

**Placeholder scan:** no TBD/TODO; every step has concrete code/commands. ✓

**Type/signature consistency:** matcher counts match the verified repo signatures (`addDeviceConditions` 15, `updateDeviceConditions` 16); field names match the service (`deviceConditionsRepository`, `alertProfileClient`, `deviceService`, `jobSchedulerService`); DTO/DeviceDTO accessors match the verified signatures. `DeviceDTO` is mocked (no constructor dependency). ✓

**Scope:** one focused test file — appropriate for a single plan and a single compile/run. ✓
