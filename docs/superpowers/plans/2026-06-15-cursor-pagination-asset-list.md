# Cursor (keyset) Pagination for Asset-List Calls — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.
>
> **Project convention (overrides the skill's commit steps):** the user commits manually — NEVER run `git commit`/`git push`. Each task's final step is **"Stage changes"** (`git add`) only; stop there and let the user commit.

**Goal:** Add industry-standard keyset (seek) cursor pagination to the three asset GET calls the Sclera UI demo uses, backward-compatibly, and switch the demo list to infinite scroll.

**Architecture:** An opaque base64url cursor encodes the last row's sort key `(timestamp,id)`. Endpoints gain an optional `cursor` param: absent → existing offset path (unchanged); present → keyset path returning a `CursorPage<T>` envelope. The two named-query endpoints get true DB keyset; `searchsortfilterdevices` (in-memory fuzzy sort) gets an opaque-offset cursor wrapper for a uniform client contract. The demo `AssetPage` accumulates rows and appends via a "Load more" button that also auto-triggers on scroll.

**Tech Stack:** Spring Boot 2.6.5 / Java 11, Hibernate `@NamedNativeQuery` (Postgres), JUnit 5 + AssertJ + Mockito, React 18 + Vite.

**Reference spec:** `docs/superpowers/specs/2026-06-15-cursor-pagination-asset-list-design.md`

---

## File Structure

**Backend (`sclera-cloud-device-asset/src/main/java/io/sclera/`):**
- Create `dto/CursorPage.java` — generic cursor response envelope.
- Create `utils/CursorCodec.java` — encode/decode the opaque cursor.
- Modify `models/Device.java` — add two keyset `@NamedNativeQuery` variants.
- Modify `Repository/DeviceRepository.java` — add two keyset repo methods (return `List<DeviceDTO>`).
- Modify `service/DeviceService.java` — add keyset service methods for the two list endpoints.
- Modify `controller/admin/DeviceController.java` — add optional `cursor` param to the 3 endpoints.

**Backend tests (`sclera-cloud-device-asset/src/test/java/io/sclera/`):**
- Create `utils/CursorCodecTest.java`, `dto/CursorPageTest.java`.
- Create `controller/admin/DeviceControllerCursorTest.java` (controller routing: cursor → keyset service path).

**Frontend (`sclera-ui/src/`):**
- Modify `services/api.js` — cursor-aware variants of the 3 calls.
- Modify `pages/AssetPage.jsx` — infinite-scroll state + "Load more"/sentinel.

---

## Cursor wire format (single source of truth)

`CursorCodec` encodes a small map to base64url(JSON) and back. Three shapes share one codec:

- Keyset (timestamp): `{"v":1,"t":<long>,"i":"<id>"}`
- Keyset (custom sort — reserved, not used by live searchsortfilter): `{"v":1,"s":<val>,"i":"<id>","d":"asc|desc"}`
- Offset fallback: `{"v":1,"o":<long>}`

Decoding a malformed/blank cursor throws `IllegalArgumentException`; controllers translate that to HTTP 400.

---

## Task 1: `CursorCodec` utility

**Files:**
- Create: `sclera-cloud-device-asset/src/main/java/io/sclera/utils/CursorCodec.java`
- Test: `sclera-cloud-device-asset/src/test/java/io/sclera/utils/CursorCodecTest.java`

- [ ] **Step 1: Write the failing test**

```java
package io.sclera.utils;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CursorCodecTest {

    @Test
    void timestampCursor_roundTrips() {
        String c = CursorCodec.encodeTimestamp(1718000000000L, "dev-42");
        CursorCodec.Cursor d = CursorCodec.decode(c);
        assertThat(d.version).isEqualTo(1);
        assertThat(d.timestamp).isEqualTo(1718000000000L);
        assertThat(d.id).isEqualTo("dev-42");
        assertThat(d.offset).isNull();
    }

    @Test
    void offsetCursor_roundTrips() {
        String c = CursorCodec.encodeOffset(120L);
        CursorCodec.Cursor d = CursorCodec.decode(c);
        assertThat(d.offset).isEqualTo(120L);
        assertThat(d.timestamp).isNull();
    }

    @Test
    void encoded_isOpaque_noRawIdVisible() {
        String c = CursorCodec.encodeTimestamp(1L, "secret-id");
        assertThat(c).doesNotContain("secret-id");
    }

    @Test
    void decode_blankOrMalformed_throws() {
        assertThatThrownBy(() -> CursorCodec.decode("")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> CursorCodec.decode("!!!not-base64!!!")).isInstanceOf(IllegalArgumentException.class);
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run (Docker, from repo root):
```bash
docker run --rm -v "$PWD:/build" -v sclera-m2:/root/.m2 -w /build maven:3.9-eclipse-temurin-21 \
  mvn -B -ntp -pl sclera-cloud-device-asset -am test -Dtest=CursorCodecTest -Dsurefire.failIfNoSpecifiedTests=false
```
Expected: FAIL — `CursorCodec` does not exist (compilation error).

- [ ] **Step 3: Write minimal implementation**

```java
package io.sclera.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Opaque, stateless keyset cursor codec. Encodes the last row's sort key as base64url(JSON) so the
 * client treats it as opaque and only echoes it back. See
 * docs/superpowers/specs/2026-06-15-cursor-pagination-asset-list-design.md.
 */
public final class CursorCodec {

    private static final int VERSION = 1;

    private CursorCodec() {
    }

    /** Decoded cursor. Exactly one of {timestamp+id} or {offset} is populated. */
    public static final class Cursor {
        public final int version;
        public final Long timestamp; // effective ordering timestamp of the last row
        public final String id;      // last row id (unique tiebreaker)
        public final Long offset;    // offset-fallback mode

        Cursor(int version, Long timestamp, String id, Long offset) {
            this.version = version;
            this.timestamp = timestamp;
            this.id = id;
            this.offset = offset;
        }
    }

    public static String encodeTimestamp(long timestamp, String id) {
        JSONObject o = new JSONObject();
        o.put("v", VERSION);
        o.put("t", timestamp);
        o.put("i", id);
        return b64(o.toJSONString());
    }

    public static String encodeOffset(long offset) {
        JSONObject o = new JSONObject();
        o.put("v", VERSION);
        o.put("o", offset);
        return b64(o.toJSONString());
    }

    public static Cursor decode(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            throw new IllegalArgumentException("Invalid cursor: blank");
        }
        try {
            byte[] raw = Base64.getUrlDecoder().decode(cursor);
            JSONObject o = JSON.parseObject(new String(raw, StandardCharsets.UTF_8));
            int v = o.getIntValue("v");
            Long t = o.containsKey("t") ? o.getLong("t") : null;
            String i = o.getString("i");
            Long off = o.containsKey("o") ? o.getLong("o") : null;
            return new Cursor(v, t, i, off);
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("Invalid cursor", e);
        }
    }

    private static String b64(String json) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(json.getBytes(StandardCharsets.UTF_8));
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run the same command as Step 2. Expected: PASS (4 tests green).

- [ ] **Step 5: Stage changes**

```bash
git add sclera-cloud-device-asset/src/main/java/io/sclera/utils/CursorCodec.java \
        sclera-cloud-device-asset/src/test/java/io/sclera/utils/CursorCodecTest.java
```

---

## Task 2: `CursorPage<T>` envelope

**Files:**
- Create: `sclera-cloud-device-asset/src/main/java/io/sclera/dto/CursorPage.java`
- Test: `sclera-cloud-device-asset/src/test/java/io/sclera/dto/CursorPageTest.java`

- [ ] **Step 1: Write the failing test**

```java
package io.sclera.dto;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

class CursorPageTest {

    @Test
    void holdsContentAndCursorMetadata() {
        CursorPage<String> p = new CursorPage<>(List.of("a", "b"), "next-cursor", true, 2);
        assertThat(p.getContent()).containsExactly("a", "b");
        assertThat(p.getNextCursor()).isEqualTo("next-cursor");
        assertThat(p.isHasMore()).isTrue();
        assertThat(p.getPageSize()).isEqualTo(2);
    }

    @Test
    void lastPage_hasNoCursor() {
        CursorPage<String> p = new CursorPage<>(List.of("a"), null, false, 2);
        assertThat(p.getNextCursor()).isNull();
        assertThat(p.isHasMore()).isFalse();
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run:
```bash
docker run --rm -v "$PWD:/build" -v sclera-m2:/root/.m2 -w /build maven:3.9-eclipse-temurin-21 \
  mvn -B -ntp -pl sclera-cloud-device-asset -am test -Dtest=CursorPageTest -Dsurefire.failIfNoSpecifiedTests=false
```
Expected: FAIL — `CursorPage` does not exist.

- [ ] **Step 3: Write minimal implementation**

```java
package io.sclera.dto;

import java.util.List;

/**
 * Cursor pagination response envelope. Unlike Spring's Page/Slice (offset-oriented), this carries an
 * opaque forward cursor and no grand total. {@code content} is the page slice; {@code nextCursor} is
 * null when {@code hasMore} is false.
 */
public class CursorPage<T> {

    private List<T> content;
    private String nextCursor;
    private boolean hasMore;
    private int pageSize;

    public CursorPage() {
    }

    public CursorPage(List<T> content, String nextCursor, boolean hasMore, int pageSize) {
        this.content = content;
        this.nextCursor = nextCursor;
        this.hasMore = hasMore;
        this.pageSize = pageSize;
    }

    public List<T> getContent() { return content; }
    public void setContent(List<T> content) { this.content = content; }

    public String getNextCursor() { return nextCursor; }
    public void setNextCursor(String nextCursor) { this.nextCursor = nextCursor; }

    public boolean isHasMore() { return hasMore; }
    public void setHasMore(boolean hasMore) { this.hasMore = hasMore; }

    public int getPageSize() { return pageSize; }
    public void setPageSize(int pageSize) { this.pageSize = pageSize; }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run the Step 2 command. Expected: PASS (2 tests green).

- [ ] **Step 5: Stage changes**

```bash
git add sclera-cloud-device-asset/src/main/java/io/sclera/dto/CursorPage.java \
        sclera-cloud-device-asset/src/test/java/io/sclera/dto/CursorPageTest.java
```

---

## Task 3: Keyset query + repo method for `getfilterdevice`

This endpoint's named query (`Device.getfilterdevices`, `Device.java:414-447`) currently has **no
`ORDER BY`** (non-deterministic paging). The keyset variant imposes `ORDER BY d.created_timestamp
DESC, d.id` and a keyset predicate.

**Files:**
- Modify: `sclera-cloud-device-asset/src/main/java/io/sclera/models/Device.java` (add a `@NamedNativeQuery` after line 447)
- Modify: `sclera-cloud-device-asset/src/main/java/io/sclera/Repository/DeviceRepository.java` (add method near line 57)

- [ ] **Step 1: Add the keyset named query**

In `Device.java`, immediately after the existing `Device.getfilterdevices` `@NamedNativeQuery` block
(ends at line 447), add a new block. **Copy the SELECT … FROM … LEFT JOIN … WHERE prefix verbatim
from lines 416-444** (do NOT retype it — copy to avoid transcription errors), then use this name,
keyset tail, and result mapping. The full block:

```java
// Keyset (seek) variant of getfilterdevices. Same projection/filters; deterministic ORDER BY +
// keyset predicate. First page passes cursorTs = NULL. Params:
//  ?1 vdmsid ?2 dockername ?3 searchKey ?4 virtual_device_type ?5 status ?6 monitor
//  ?7 asset_match_status ?8 assigned_status ?9 limit ?10 cursorTs(nullable) ?11 cursorId(nullable)
@NamedNativeQuery(
        name = "Device.getfilterdevicesKeyset",
        query = "<<COPY the string-concatenated SELECT...FROM...LEFT JOIN...WHERE body from "
                // Device.java lines 416 (\"SELECT d.id,...\") through 444 (the asset_match_status AND clause) VERBATIM,
                // but renumber params: keep ?1..?7 as-is, change the assigned_status param from ?10 to ?8,
                // and DROP the trailing LIMIT/OFFSET (old line 445). Then append the keyset tail below. >>
                + " AND (?10 IS NULL OR (d.created_timestamp, d.id) < (CAST(?10 AS bigint), CAST(?11 AS text)))"
                + " ORDER BY d.created_timestamp DESC, d.id DESC"
                + " LIMIT ?9",
        resultSetMapping = "devicemapping"
)
```

> Note: the old query's assigned_status clause (line 443) uses `?10`; in the keyset variant it must be
> renumbered to `?8` because the keyset cursor params take `?10`/`?11`. The search filter (line 440)
> keeps `?3`; status/monitor keep `?5`/`?6`; asset_match_status keeps `?7`.

- [ ] **Step 2: Add the repository method**

In `DeviceRepository.java`, after the existing `getfilterdevices` declaration (line 57-58), add:

```java
    /**
     * Keyset (seek) page of filtered devices ordered by (created_timestamp DESC, id DESC).
     * Pass cursorTs/cursorId = null for the first page; otherwise the last row's created_timestamp
     * and id. Returns an ordered List so the caller can read the true last row for the next cursor.
     */
    @Query(nativeQuery = true)
    List<DeviceDTO> getfilterdevicesKeyset(String vdmsid, String dockername, String searchKey,
                                           Integer virtual_device_type, Integer status, Integer monitor,
                                           Integer asset_match_status, Integer assigned_status,
                                           Integer limit, Long cursorTs, String cursorId);
```

Ensure `java.util.List` is imported in `DeviceRepository.java` (it already is — used elsewhere).

- [ ] **Step 3: Compile to verify the named query binds**

Run:
```bash
docker run --rm -v "$PWD:/build" -v sclera-m2:/root/.m2 -w /build maven:3.9-eclipse-temurin-21 \
  mvn -B -ntp -pl sclera-cloud-device-asset -am test-compile
```
Expected: BUILD SUCCESS (named query name matches the repo method; mapping `devicemapping` exists).

- [ ] **Step 4: Stage changes**

```bash
git add sclera-cloud-device-asset/src/main/java/io/sclera/models/Device.java \
        sclera-cloud-device-asset/src/main/java/io/sclera/Repository/DeviceRepository.java
```

---

## Task 4: `getfilterdevice` keyset service method + controller wiring

**Files:**
- Modify: `sclera-cloud-device-asset/src/main/java/io/sclera/service/DeviceService.java` (add method after `getfilterdevices`, line 533)
- Modify: `sclera-cloud-device-asset/src/main/java/io/sclera/controller/admin/DeviceController.java` (the `getfilterdevice` endpoint)
- Test: `sclera-cloud-device-asset/src/test/java/io/sclera/controller/admin/DeviceControllerCursorTest.java`

- [ ] **Step 1: Add the keyset service method**

In `DeviceService.java`, after the `getfilterdevices(...)` method (ends line 534), add. This replicates
the existing `condition`→params mapping (lines 482-519), then fetches `pageSize + 1`, trims, enriches,
and builds the `CursorPage`:

```java
    /**
     * Keyset (seek) variant of {@link #getfilterdevices}. Returns a {@link CursorPage}: fetches
     * pageSize+1 rows to detect hasMore, trims the extra, and derives nextCursor from the last kept
     * row's (created_timestamp, id). cursor == null/blank starts from the first page.
     */
    public io.sclera.dto.CursorPage<DeviceDTO> getfilterdevicesKeyset(String username, String vdmsid, String dockername,
                                                                      String condition, String searchKey,
                                                                      Integer pageSize, String cursor) {
        Integer virtual_device_type = null;
        Integer status = null;
        Integer monitor = 123;
        Integer asset_match_status = null;
        Integer assigned_status = null;

        if (condition.equals("all")) {
        } else if (condition.equals("unmonitored")) {
            monitor = 0;
        } else if (condition.equals("online")) {
            monitor = 1;
            status = 1;
        } else if (condition.equals("offline")) {
            monitor = 1;
            status = 0;
        } else if (condition.equals("other")) {
            virtual_device_type = 123;
        } else if (condition.equals("matched")) {
            asset_match_status = 1;
        } else if (condition.equals("unmatched")) {
            asset_match_status = 0;
        } else if (condition.equals("verified")) {
            asset_match_status = 2;
        } else if (condition.equals("archived")) {
            asset_match_status = 3;
        } else if (condition.equals("assigned")) {
            assigned_status = 1;
        } else if (condition.equals("unassigned")) {
            assigned_status = 0;
        }

        Long cursorTs = null;
        String cursorId = null;
        if (cursor != null && !cursor.isBlank()) {
            io.sclera.utils.CursorCodec.Cursor c = io.sclera.utils.CursorCodec.decode(cursor);
            cursorTs = c.timestamp;
            cursorId = c.id;
        }

        java.util.List<DeviceDTO> rows = deviceRepository.getfilterdevicesKeyset(
                vdmsid, dockername, searchKey, virtual_device_type, status, monitor,
                asset_match_status, assigned_status, pageSize + 1, cursorTs, cursorId);

        boolean hasMore = rows.size() > pageSize;
        if (hasMore) {
            rows = new java.util.ArrayList<>(rows.subList(0, pageSize));
        }
        for (DeviceDTO device : rows) {
            try {
                device.setIp_addresses(this.getDeviceIPAddressByDeviceId(device.getId()));
                device.setSubsystems(new HashSet<>());
            } catch (Exception e) {
                log.error("getfilterdevicesKeyset enrich failed for device={}", device.getId(), e);
            }
        }

        String nextCursor = null;
        if (hasMore && !rows.isEmpty()) {
            DeviceDTO last = rows.get(rows.size() - 1);
            nextCursor = io.sclera.utils.CursorCodec.encodeTimestamp(last.getCreated_timestamp(), last.getId());
        }
        return new io.sclera.dto.CursorPage<>(rows, nextCursor, hasMore, pageSize);
    }
```

> If `DeviceDTO.getCreated_timestamp()` returns a boxed type other than `long` (e.g. `BigInteger`),
> adapt the `encodeTimestamp` arg with `.longValue()`. Verify the getter's type in `dto/DeviceDTO.java`
> before running; the test in Step 4 will catch a mismatch.

- [ ] **Step 2: Wire the controller `cursor` param**

Find the `getfilterdevice` endpoint in `DeviceController.java` (search for `value = "/docker/{dockername}/getfilterdevice"`). It currently returns `Page<DeviceDTO>` via `PageUtils.toPage(...)`. Change its return type to `Object` and branch on `cursor`:

```java
    @RequestMapping(method = RequestMethod.GET, value = "/docker/{dockername}/getfilterdevice")
    public Object getFilterDevice(@RequestParam String username, @RequestParam String vdmsid, @PathVariable String dockername,
                                  @RequestParam(defaultValue = "all") String condition, @RequestParam(defaultValue = "null") String searchKey,
                                  @RequestParam(defaultValue = "1") Integer pageno, @RequestParam(defaultValue = "10") Integer pagesize,
                                  @RequestParam(required = false) String cursor) {
        log.info("getFilterDevice username={} vdmsid={} dockername={} cursor={}", username, vdmsid, dockername, cursor != null);
        try {
            if (cursor != null) {
                return deviceService.getfilterdevicesKeyset(username, vdmsid, dockername, condition, searchKey, pagesize, cursor);
            }
            return PageUtils.toPage(deviceService.getfilterdevices(username, vdmsid, dockername, condition, searchKey, pageno, pagesize), pageno, pagesize);
        } catch (IllegalArgumentException e) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            log.error("getFilterDevice failed username={} vdmsid={} dockername={}: {}", username, vdmsid, dockername, e.getMessage(), e);
            throw e;
        }
    }
```

> Match the existing method's exact parameter list/order when you edit (the snippet above mirrors the
> current signature plus `cursor`). Keep the existing body for the non-cursor branch verbatim — only
> add the `cursor` param, the `if (cursor != null)` branch, and the `IllegalArgumentException` catch.

- [ ] **Step 3: Write the controller routing test**

```java
package io.sclera.controller.admin;

import io.sclera.dto.CursorPage;
import io.sclera.dto.DeviceDTO;
import io.sclera.service.DeviceService;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class DeviceControllerCursorTest {

    @Test
    void getFilterDevice_withCursor_usesKeysetServicePath() {
        DeviceService svc = mock(DeviceService.class);
        DeviceController controller = new DeviceController();
        org.springframework.test.util.ReflectionTestUtils.setField(controller, "deviceService", svc);

        CursorPage<DeviceDTO> page = new CursorPage<>(List.of(new DeviceDTO()), "next", true, 10);
        when(svc.getfilterdevicesKeyset(any(), any(), any(), any(), any(), eq(10), eq("cur-1"))).thenReturn(page);

        Object res = controller.getFilterDevice("u", "v", "dk", "all", "null", 1, 10, "cur-1");

        assertThat(res).isInstanceOf(CursorPage.class);
        verify(svc).getfilterdevicesKeyset("u", "v", "dk", "all", "null", 10, "cur-1");
        verify(svc, never()).getfilterdevices(any(), any(), any(), any(), any(), any(), any());
    }
}
```

> If `DeviceController`'s `deviceService` field is not field-injected with that exact name, open the
> controller, read the field name/injection style, and adjust the `setField` name accordingly.

- [ ] **Step 4: Run the test**

Run:
```bash
docker run --rm -v "$PWD:/build" -v sclera-m2:/root/.m2 -w /build maven:3.9-eclipse-temurin-21 \
  mvn -B -ntp -pl sclera-cloud-device-asset -am test -Dtest=DeviceControllerCursorTest -Dsurefire.failIfNoSpecifiedTests=false
```
Expected: PASS.

- [ ] **Step 5: Stage changes**

```bash
git add sclera-cloud-device-asset/src/main/java/io/sclera/service/DeviceService.java \
        sclera-cloud-device-asset/src/main/java/io/sclera/controller/admin/DeviceController.java \
        sclera-cloud-device-asset/src/test/java/io/sclera/controller/admin/DeviceControllerCursorTest.java
```

---

## Task 5: Keyset query + repo method + service for `getsubsystemparentdevicesbypagination`

Backed by `Device.getSubsystemParentDevicesByPagination` (`Device.java:158-193`), already ordered by
`(CASE WHEN ?6=3 THEN updated_timestamp ELSE created_timestamp END) DESC, id`. The keyset predicate
must use the **same** effective-timestamp expression.

**Files:**
- Modify: `models/Device.java` (add `@NamedNativeQuery` after line 194)
- Modify: `Repository/DeviceRepository.java` (add method near line 772)
- Modify: `service/DeviceService.java` (add keyset service method near `getAllSubsystemDevicesByPagination`, line 4109)

- [ ] **Step 1: Add the keyset named query**

In `Device.java`, after the existing `Device.getSubsystemParentDevicesByPagination` block (ends line
194). **Copy the SELECT…FROM…JOIN…WHERE body verbatim from lines 160-189**, keep params `?1..?6,
?9,?10,?11` as in the original, then append the keyset tail. The effective-timestamp expression
reuses `?6` (asset_match_status). Cursor params are `?7` (limit), `?8` (cursorTs, nullable), `?12`
(cursorId, nullable):

```java
// Keyset (seek) variant of getSubsystemParentDevicesByPagination. Same projection/filters and the
// same effective-timestamp ordering; keyset predicate replaces LIMIT/OFFSET. Params:
//  ?1 vdmsid ?2 dockername ?3 virtual_device_type ?4 status ?5 monitor ?6 asset_match_status
//  ?7 limit ?8 cursorTs(nullable) ?9 onboard_status ?10 assigned_status ?11 assignee ?12 cursorId(nullable)
@NamedNativeQuery(
        name = "Device.getSubsystemParentDevicesByPaginationKeyset",
        query = "<<COPY the SELECT...FROM...LEFT JOIN...WHERE body from Device.java lines 160-189 VERBATIM "
                // (keep ALL existing param numbers ?1..?6, ?9, ?10, ?11 exactly as written; DROP the old
                // ORDER BY (line 191) and LIMIT/OFFSET (line 192)). Then append the keyset tail below. >>
                + " AND (?8 IS NULL OR ((CASE WHEN ?6 = 3 THEN d.updated_timestamp ELSE d.created_timestamp END), d.id) < (CAST(?8 AS bigint), CAST(?12 AS text)))"
                + " ORDER BY (CASE WHEN ?6 = 3 THEN d.updated_timestamp ELSE d.created_timestamp END) DESC, d.id DESC"
                + " LIMIT ?7",
        resultSetMapping = "devicedtomapping"
)
```

> The original `ORDER BY` writes `CASE ?6 WHEN 3 ...`; the keyset tail uses the equivalent `CASE WHEN
> ?6 = 3 ...` form so the same expression appears in both the predicate and ORDER BY. Keep them
> identical to each other.

- [ ] **Step 2: Add the repository method**

In `DeviceRepository.java`, after the existing `getSubsystemParentDevicesByPagination` declaration
(line 772-773):

```java
    /**
     * Keyset (seek) page of subsystem parent devices, ordered by the effective timestamp
     * (updated_timestamp when asset_match_status=3, else created_timestamp) DESC, id DESC.
     * cursorTs/cursorId = null for the first page. Returns an ordered List.
     */
    @Query(nativeQuery = true)
    List<DeviceDTO> getSubsystemParentDevicesByPaginationKeyset(String vdmsid, String dockername,
            Integer virtual_device_type, Integer status, Integer monitor, Integer asset_match_status,
            Integer limit, Long cursorTs, Integer onboard_status, Integer assigned_status, String assignee,
            String cursorId);
```

- [ ] **Step 3: Add the keyset service method**

In `DeviceService.java`, after `getAllSubsystemDevicesByPagination(...)` returns (find its closing
brace after line ~4170). This mirrors the existing `condition`→params mapping (lines 4122-4154) for
the parent path (no `device_id`):

```java
    /**
     * Keyset (seek) variant of getSubsystemParentDevicesByPagination. Returns a {@link CursorPage}.
     * Mirrors getAllSubsystemDevicesByPagination's condition mapping for the parent path, fetches
     * pageSize+1, trims, enriches IP addresses + onboard data, and derives nextCursor from the last
     * kept row's effective timestamp (updated when archived/condition resolves to asset_match_status=3,
     * else created).
     */
    public io.sclera.dto.CursorPage<DeviceDTO> getSubsystemParentDevicesByPaginationKeyset(String username, String vdmsid,
            String dockername, String condition, Integer pageSize, String cursor) {
        Integer virtual_device_type = null;
        Integer status = null;
        Integer monitor = 123;
        Integer asset_match_status = null;
        Integer onboard_status = 123;
        Integer assigned_status = null;
        String assignee = "all";

        if (condition.equals("all")) {
        } else if (condition.equals("unmonitored")) {
            monitor = 0;
        } else if (condition.equals("online")) {
            monitor = 1;
            status = 1;
        } else if (condition.equals("offline")) {
            monitor = 1;
            status = 0;
        } else if (condition.equals("other")) {
            virtual_device_type = 123;
        } else if (condition.equals("matched")) {
            asset_match_status = 1;
        } else if (condition.equals("unmatched")) {
            asset_match_status = 0;
        } else if (condition.equals("verified")) {
            asset_match_status = 2;
        } else if (condition.equals("archived")) {
            asset_match_status = 3;
        } else if (condition.equals("onboarded")) {
            onboard_status = 3;
        } else if (condition.equals("notonboarded")) {
            onboard_status = 210;
        } else if (condition.equals("assigned")) {
            assigned_status = 1;
        } else if (condition.equals("unassigned")) {
            assigned_status = 0;
        }

        Long cursorTs = null;
        String cursorId = null;
        if (cursor != null && !cursor.isBlank()) {
            io.sclera.utils.CursorCodec.Cursor c = io.sclera.utils.CursorCodec.decode(cursor);
            cursorTs = c.timestamp;
            cursorId = c.id;
        }

        java.util.List<DeviceDTO> rows = deviceRepository.getSubsystemParentDevicesByPaginationKeyset(
                vdmsid, dockername, virtual_device_type, status, monitor, asset_match_status,
                pageSize + 1, cursorTs, onboard_status, assigned_status, assignee, cursorId);

        boolean hasMore = rows.size() > pageSize;
        if (hasMore) {
            rows = new java.util.ArrayList<>(rows.subList(0, pageSize));
        }
        boolean archived = asset_match_status != null && asset_match_status == 3;
        for (DeviceDTO device : rows) {
            try {
                device.setIp_addresses(this.getDeviceIPAddressByDeviceId(device.getId()));
                device.setOnboard_data(new DeviceOnboardStatusDTO(device.getAssignee_email(), device.getImage_status(),
                        device.getGeolocation_status(), device.getTag_status(), device.getField_status(),
                        deviceOnboardStatusAssigneeService.getDeviceOnboardStatusAssignees(device.getDevice_onboard_status_id())));
                device.setSubsystems(new HashSet<>());
            } catch (Exception e) {
                log.error("getSubsystemParentDevicesByPaginationKeyset enrich failed for device={}", device.getId(), e);
            }
        }

        String nextCursor = null;
        if (hasMore && !rows.isEmpty()) {
            DeviceDTO last = rows.get(rows.size() - 1);
            long ts = archived ? last.getUpdated_timestamp() : last.getCreated_timestamp();
            nextCursor = io.sclera.utils.CursorCodec.encodeTimestamp(ts, last.getId());
        }
        return new io.sclera.dto.CursorPage<>(rows, nextCursor, hasMore, pageSize);
    }
```

> Verify `DeviceDTO` exposes `getUpdated_timestamp()` and `getCreated_timestamp()` (and the onboard
> getters used above — they are already used at lines 4165-4167 for the offset path, so they exist).
> Adjust `.longValue()` if those getters are boxed `BigInteger`/`Long`.

- [ ] **Step 4: Compile**

Run:
```bash
docker run --rm -v "$PWD:/build" -v sclera-m2:/root/.m2 -w /build maven:3.9-eclipse-temurin-21 \
  mvn -B -ntp -pl sclera-cloud-device-asset -am test-compile
```
Expected: BUILD SUCCESS.

- [ ] **Step 5: Stage changes**

```bash
git add sclera-cloud-device-asset/src/main/java/io/sclera/models/Device.java \
        sclera-cloud-device-asset/src/main/java/io/sclera/Repository/DeviceRepository.java \
        sclera-cloud-device-asset/src/main/java/io/sclera/service/DeviceService.java
```

---

## Task 6: Wire the subsystem-parent controller `cursor` param

**Files:**
- Modify: `controller/admin/DeviceController.java` (`getSubsystemParentDevicesByPagination`, lines 124-134)
- Test: append to `DeviceControllerCursorTest.java`

- [ ] **Step 1: Add the `cursor` branch**

Replace the body of `getSubsystemParentDevicesByPagination` (lines 124-134) with:

```java
    @RequestMapping(method = RequestMethod.GET, value = "/docker/{dockername}/getsubsystemparentdevicesbypagination")
    public Object getSubsystemParentDevicesByPagination(@RequestParam String username, @RequestParam String vdmsid, @PathVariable String dockername, @RequestParam(defaultValue = "all") String condition,
                                                        @RequestParam(defaultValue = "1") Integer pageno, @RequestParam(defaultValue = "10") Integer pagesize, @RequestParam(defaultValue = "all") String assignee,
                                                        @RequestParam(required = false) String cursor) {
        log.info("getSubsystemParentDevicesByPagination username={} vdmsid={} dockername={} cursor={}", username, vdmsid, dockername, cursor != null);
        try {
            if (cursor != null) {
                return deviceService.getSubsystemParentDevicesByPaginationKeyset(username, vdmsid, dockername, condition, pagesize, cursor);
            }
            return PageUtils.toPage(deviceService.getSubsystemParentDevicesByPagination(username, vdmsid, dockername, condition, pageno, pagesize, assignee), pageno, pagesize);
        } catch (IllegalArgumentException e) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            log.error("getSubsystemParentDevicesByPagination failed username={} vdmsid={} dockername={}: {}", username, vdmsid, dockername, e.getMessage(), e);
            throw e;
        }
    }
```

(Return type changed from `Page<DeviceDTO>` to `Object`.)

- [ ] **Step 2: Add the routing test**

Append this method to `DeviceControllerCursorTest`:

```java
    @Test
    void getSubsystemParentDevices_withCursor_usesKeysetServicePath() {
        DeviceService svc = mock(DeviceService.class);
        DeviceController controller = new DeviceController();
        org.springframework.test.util.ReflectionTestUtils.setField(controller, "deviceService", svc);

        CursorPage<DeviceDTO> page = new CursorPage<>(List.of(new DeviceDTO()), "next", true, 10);
        when(svc.getSubsystemParentDevicesByPaginationKeyset(any(), any(), any(), any(), eq(10), eq("cur-9"))).thenReturn(page);

        Object res = controller.getSubsystemParentDevicesByPagination("u", "v", "dk", "all", 1, 10, "all", "cur-9");

        assertThat(res).isInstanceOf(CursorPage.class);
        verify(svc).getSubsystemParentDevicesByPaginationKeyset("u", "v", "dk", "all", 10, "cur-9");
    }
```

- [ ] **Step 3: Run the test**

Run:
```bash
docker run --rm -v "$PWD:/build" -v sclera-m2:/root/.m2 -w /build maven:3.9-eclipse-temurin-21 \
  mvn -B -ntp -pl sclera-cloud-device-asset -am test -Dtest=DeviceControllerCursorTest -Dsurefire.failIfNoSpecifiedTests=false
```
Expected: PASS (both tests).

- [ ] **Step 4: Stage changes**

```bash
git add sclera-cloud-device-asset/src/main/java/io/sclera/controller/admin/DeviceController.java \
        sclera-cloud-device-asset/src/test/java/io/sclera/controller/admin/DeviceControllerCursorTest.java
```

---

## Task 7: `searchsortfilterdevices` opaque-offset cursor wrapper

Its live path sorts in memory (fuzzy), so DB keyset is impossible. Wrap the existing offset result in
a `CursorPage` whose cursor encodes the next offset (`{"v":1,"o":<n>}`) — uniform client contract,
same underlying query.

**Files:**
- Modify: `controller/admin/DeviceController.java` (`multipleKeywordSearchSortFilterDevices`, lines 899-913)
- Test: append to `DeviceControllerCursorTest.java`

- [ ] **Step 1: Add the `cursor` branch**

Replace lines 899-913 with:

```java
    @RequestMapping(method = RequestMethod.POST, value = "/docker/{dockername}/searchsortfilterdevices")
    public Object multipleKeywordSearchSortFilterDevices(@RequestParam String username, @RequestParam String vdmsid,
                                                         @PathVariable String dockername, @RequestParam(defaultValue = "all") String condition,
                                                         @RequestParam(defaultValue = "1") Integer pageno,
                                                         @RequestParam(defaultValue = "10") Integer pagesize,
                                                         @RequestParam(defaultValue = "123") Integer onboard_status,
                                                         @RequestParam(required = false) String cursor,
                                                         @RequestBody com.alibaba.fastjson.JSONObject search_sort_filter_details) {
        log.info("multipleKeywordSearchSortFilterDevices username={} vdmsid={} dockername={} cursor={}", username, vdmsid, dockername, cursor != null);
        try {
            if (cursor != null) {
                long offset = 0;
                if (!cursor.isBlank()) {
                    io.sclera.utils.CursorCodec.Cursor c = io.sclera.utils.CursorCodec.decode(cursor);
                    offset = c.offset != null ? c.offset : 0;
                }
                int effectivePageNo = (int) (offset / pagesize) + 1;
                java.util.List<DeviceDTO> rows = new java.util.ArrayList<>(deviceSearchService.multipleKeywordSearchSortFilterDevices(
                        username, vdmsid, dockername, condition, effectivePageNo, pagesize + 1, search_sort_filter_details, onboard_status));
                boolean hasMore = rows.size() > pagesize;
                if (hasMore) {
                    rows = new java.util.ArrayList<>(rows.subList(0, pagesize));
                }
                String nextCursor = hasMore ? io.sclera.utils.CursorCodec.encodeOffset(offset + pagesize) : null;
                return new io.sclera.dto.CursorPage<>(rows, nextCursor, hasMore, pagesize);
            }
            return PageUtils.toPage(deviceSearchService.multipleKeywordSearchSortFilterDevices(username, vdmsid, dockername, condition, pageno, pagesize, search_sort_filter_details, onboard_status), pageno, pagesize);
        } catch (IllegalArgumentException e) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            log.error("multipleKeywordSearchSortFilterDevices failed username={} vdmsid={} dockername={}: {}", username, vdmsid, dockername, e.getMessage(), e);
            throw e;
        }
    }
```

> `multipleKeywordSearchSortFilterDevices(...)` returns a `Set<DeviceDTO>`; wrapping in `new
> ArrayList<>(...)` preserves the service's `LinkedHashSet` fuzzy order. The `pagesize + 1` probe gives
> `hasMore`. Offset advances by `pagesize` each page. This is offset under the hood by design (fuzzy
> sort can't be keyset) but presents the identical `CursorPage` contract to the UI.

- [ ] **Step 2: Add the routing test**

Append to `DeviceControllerCursorTest` (note `deviceSearchService` field):

```java
    @Test
    void searchSortFilter_withCursor_returnsCursorPage_offsetMode() {
        io.sclera.service.DeviceSearchService search = mock(io.sclera.service.DeviceSearchService.class);
        DeviceController controller = new DeviceController();
        org.springframework.test.util.ReflectionTestUtils.setField(controller, "deviceSearchService", search);

        java.util.LinkedHashSet<DeviceDTO> set = new java.util.LinkedHashSet<>();
        set.add(new DeviceDTO());
        when(search.multipleKeywordSearchSortFilterDevices(any(), any(), any(), any(), anyInt(), anyInt(), any(), anyInt()))
                .thenReturn(set);

        Object res = controller.multipleKeywordSearchSortFilterDevices("u", "v", "dk", "all", 1, 10, 123, "",
                new com.alibaba.fastjson.JSONObject());

        assertThat(res).isInstanceOf(CursorPage.class);
        assertThat(((CursorPage<?>) res).isHasMore()).isFalse(); // 1 row < pagesize+1 ⇒ no more
    }
```

> Confirm the controller's search-service field is named `deviceSearchService` (it is, per
> `DeviceController.java:908`/934). Adjust the `setField` name if different.

- [ ] **Step 3: Run the test**

Run:
```bash
docker run --rm -v "$PWD:/build" -v sclera-m2:/root/.m2 -w /build maven:3.9-eclipse-temurin-21 \
  mvn -B -ntp -pl sclera-cloud-device-asset -am test -Dtest=DeviceControllerCursorTest -Dsurefire.failIfNoSpecifiedTests=false
```
Expected: PASS (3 tests).

- [ ] **Step 4: Full module test run (regression)**

Run:
```bash
docker run --rm -v "$PWD:/build" -v sclera-m2:/root/.m2 -w /build maven:3.9-eclipse-temurin-21 \
  mvn -B -ntp -pl sclera-cloud-device-asset -am test -Dsurefire.failIfNoSpecifiedTests=false
```
Expected: BUILD SUCCESS — existing offset tests still green (backward compatibility intact).

- [ ] **Step 5: Stage changes**

```bash
git add sclera-cloud-device-asset/src/main/java/io/sclera/controller/admin/DeviceController.java \
        sclera-cloud-device-asset/src/test/java/io/sclera/controller/admin/DeviceControllerCursorTest.java
```

---

## Task 8: `api.js` cursor-aware asset calls

**Files:**
- Modify: `sclera-ui/src/services/api.js` (the `listParentDevices`, `listDevices`, `searchSortFilter` entries, lines 74-88)

- [ ] **Step 1: Add cursor support to the three calls**

`request`/`qs`/`scope`/`asset` already exist. `qs` skips null/undefined, so passing `cursor: undefined`
omits it (offset mode preserved). In cursor mode the backend returns `{content, nextCursor, hasMore}`;
do NOT run those through `unwrapPage` (which strips to a bare array). Replace lines 74-88 with:

```javascript
  // Browse: parent/subsystem devices. Pass `cursor` (opaque) to use keyset pagination; the response is
  // {content, nextCursor, hasMore}. Omit `cursor` for legacy offset (pageno/pagesize) mode.
  listParentDevices: ({ docker = DEMO.docker, condition = 'all', pageno = 1, pagesize = 12, cursor, ...ctx } = {}) =>
    request(asset(`/docker/${encodeURIComponent(docker)}/getsubsystemparentdevicesbypagination${qs({ ...scope(ctx), condition, pageno, pagesize, assignee: 'all', cursor })}`))
      .then((r) => (cursor !== undefined ? normalizeCursorPage(r) : { rows: unwrap(r), nextCursor: null, hasMore: (unwrap(r) || []).length >= pagesize })),

  listDevices: ({ docker = DEMO.docker, condition = 'all', searchKey = 'null', pageno = 1, pagesize = 12, cursor, ...ctx } = {}) =>
    request(asset(`/docker/${encodeURIComponent(docker)}/getfilterdevice${qs({ ...scope(ctx), condition, searchKey, pageno, pagesize, cursor })}`))
      .then((r) => (cursor !== undefined ? normalizeCursorPage(r) : { rows: unwrap(r), nextCursor: null, hasMore: (unwrap(r) || []).length >= pagesize })),

  searchSortFilter: (criteria = {}, { docker = DEMO.docker, condition = 'all', pageno = 1, pagesize = 50, onboard_status = 123, cursor, ...ctx } = {}) =>
    request(asset(`/docker/${encodeURIComponent(docker)}/searchsortfilterdevices${qs({ ...scope(ctx), condition, pageno, pagesize, onboard_status, cursor })}`), {
      method: 'POST', body: criteria,
    }).then((r) => (cursor !== undefined ? normalizeCursorPage(r) : { rows: unwrap(r), nextCursor: null, hasMore: (unwrap(r) || []).length >= pagesize })),
```

- [ ] **Step 2: Add the helpers**

Just above `export const api = {` (line 67), add:

```javascript
// Unwrap either a Page<T> ({content:[...]}) or a bare array to a plain list.
const unwrap = (r) => (r && !Array.isArray(r) && Array.isArray(r.content)) ? r.content : (Array.isArray(r) ? r : [])
// Normalize a CursorPage<T> response to a uniform { rows, nextCursor, hasMore }.
const normalizeCursorPage = (r) => ({
  rows: unwrap(r),
  nextCursor: (r && r.nextCursor) || null,
  hasMore: Boolean(r && r.hasMore),
})
```

> The existing `unwrapPage` (line 55) stays for other callers. These three now return the uniform
> object shape in BOTH modes, so `AssetPage` reads `res.rows`/`res.nextCursor`/`res.hasMore`.

- [ ] **Step 3: Build to verify no syntax errors**

Run:
```bash
cd sclera-ui && npm run build
```
Expected: `✓ built` with no errors. (`cd` back to repo root after.)

- [ ] **Step 4: Stage changes**

```bash
git add sclera-ui/src/services/api.js
```

---

## Task 9: `AssetPage.jsx` infinite scroll

**Files:**
- Modify: `sclera-ui/src/pages/AssetPage.jsx` (data-load effect lines 54-143; list-foot Prev/Next lines 338-340)

- [ ] **Step 1: Switch state from page-number to cursor + accumulated rows**

In `AssetPage`, replace `const [page, setPage] = useState(1)` (line 58) with:

```javascript
  const [rows, setRows] = useState([])
  const [cursor, setCursor] = useState(null)     // opaque; '' means "first page, cursor mode"
  const [hasMore, setHasMore] = useState(false)
  const [loadingMore, setLoadingMore] = useState(false)
```

- [ ] **Step 2: Replace the data-load effect**

Replace the existing fetch effect (the block spanning lines ~110-138 that sets `res` from
`searchSortFilter`/`listDevices`/`listParentDevices` and the `useEffect` deps on line 138) with a
cursor-aware loader. It appends when a cursor is present, resets when not:

```javascript
  const load = useCallback(async (nextCursor) => {
    const append = nextCursor !== '' && nextCursor != null
    if (append) setLoadingMore(true); else setLoading(true)
    try {
      const opts = { ...scope, condition: filter, cursor: nextCursor ?? '' }
      let res
      if (adv?.criteria) {
        res = await api.searchSortFilter(adv.criteria, { ...opts, onboard_status: adv.onboard_status, pagesize: 50 })
        api.searchSortFilterCount(adv.criteria, { ...scope, condition: filter, onboard_status: adv.onboard_status }).catch(() => {})
      } else if (debounced) {
        res = await api.listDevices({ ...opts, searchKey: debounced, pagesize: PAGE_SIZE })
      } else {
        res = await api.listParentDevices({ ...opts, pagesize: PAGE_SIZE })
      }
      setRows((prev) => (append ? [...prev, ...res.rows] : res.rows))
      setCursor(res.nextCursor)
      setHasMore(res.hasMore)
    } catch (e) {
      if (!append) setRows([])
      setHasMore(false)
    } finally {
      setLoading(false); setLoadingMore(false)
    }
  }, [ctx, filter, debounced, adv, network]) // eslint-disable-line react-hooks/exhaustive-deps

  // Reset + first page whenever the query changes.
  useEffect(() => { setRows([]); setCursor(null); setHasMore(false); load('') }, [filter, debounced, adv, network]) // eslint-disable-line react-hooks/exhaustive-deps
```

> Add `useCallback` to the React import at the top of the file. `scope`/`filter`/`debounced`/`adv`/
> `network`/`PAGE_SIZE`/`setLoading` already exist in this component. Remove the now-unused
> `useEffect(() => { setPage(1) }, ...)` (line 141) and any remaining `page`/`setPage` references.
> Wherever the table previously rendered from the old results variable, render from `rows`.

- [ ] **Step 3: Replace Prev/Next with Load-more + auto-trigger sentinel**

Replace the pager (lines 338-340) with:

```javascript
          {hasMore && (
            <div ref={sentinelRef} className="load-more-row">
              <button className="btn btn-ghost sm" disabled={loadingMore} onClick={() => load(cursor)}>
                {loadingMore ? 'Loading…' : 'Load more'}
              </button>
            </div>
          )}
```

Add the auto-trigger near the other hooks in the component body:

```javascript
  const sentinelRef = useRef(null)
  useEffect(() => {
    if (!hasMore || loadingMore) return
    const el = sentinelRef.current
    if (!el) return
    const io = new IntersectionObserver((entries) => {
      if (entries[0].isIntersecting) load(cursor)
    }, { rootMargin: '200px' })
    io.observe(el)
    return () => io.disconnect()
  }, [hasMore, loadingMore, cursor, load])
```

> Add `useRef` to the React import. The `200px` rootMargin pre-fetches before the user hits the
> bottom (smooth infinite scroll); the button is the manual/fallback trigger.

- [ ] **Step 4: Add minimal styling**

In `sclera-ui/src/styles/global.css`, append:

```css
.load-more-row { display: flex; justify-content: center; padding: 16px 0; }
```

- [ ] **Step 5: Build to verify**

Run:
```bash
cd sclera-ui && npm run build
```
Expected: `✓ built` with no errors.

- [ ] **Step 6: Rebuild the Docker UI and smoke-test**

Run:
```bash
docker compose -f sclera-ui/docker-compose.yml up -d --build
```
Then open http://localhost:3000, hard-refresh, and verify: the asset list loads, scrolling to the
bottom appends a disjoint next page (no dupes), "Load more" appears only while `hasMore`, changing the
search/filter resets the list to the top, and the end of data hides the control.

- [ ] **Step 7: Stage changes**

```bash
git add sclera-ui/src/pages/AssetPage.jsx sclera-ui/src/styles/global.css
```

---

## Self-Review (completed by plan author)

**Spec coverage:**
- Cursor model (opaque base64url, `{v,t,i}`/`{v,o}`) → Task 1. ✓
- `CursorPage<T>` envelope → Task 2. ✓
- Keyset on `getfilterdevice` (+ fixes missing ORDER BY) → Tasks 3-4. ✓
- Keyset on `getsubsystemparentdevicesbypagination` (effective-timestamp expr) → Tasks 5-6. ✓
- `searchsortfilterdevices` opaque-offset fallback (in-memory fuzzy sort) → Task 7. ✓
- Backward compat (no `cursor` → unchanged offset path) → Tasks 4/6/7 non-cursor branch + Task 7 Step 4 regression run. ✓
- Invalid cursor → HTTP 400 → `IllegalArgumentException` catch in each controller. ✓
- UI infinite scroll (Load-more + sentinel, reset on query change) → Tasks 8-9. ✓
- `hasMore` via `pageSize + 1` probe → Tasks 4/5/7. ✓

**Type consistency:** `CursorCodec.Cursor` fields (`timestamp`,`id`,`offset`), `CursorPage` getters
(`getContent`/`getNextCursor`/`isHasMore`/`getPageSize`), service methods
(`getfilterdevicesKeyset`/`getSubsystemParentDevicesByPaginationKeyset`), repo methods, and the
`{rows,nextCursor,hasMore}` FE shape are used consistently across tasks. ✓

**Open verification points flagged inline for the implementer (not placeholders — explicit checks):**
- `DeviceDTO` timestamp getter return type (`long` vs boxed) — Tasks 4/5.
- `DeviceController` injected field names (`deviceService`, `deviceSearchService`) for `ReflectionTestUtils` — Tasks 4/7.
- Verbatim copy of the large SELECT bodies in the two keyset named queries — Tasks 3/5.
