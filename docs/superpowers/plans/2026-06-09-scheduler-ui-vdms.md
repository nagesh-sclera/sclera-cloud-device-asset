# VDMS-aware Scheduler Dashboard UI Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Surface the VDMS-aware backend in the scheduler dashboard — PER_VDMS jobs drill down into their per-VDMS instances with state-contextual actions, snooze/run-at delay controls, search/filter, SNOOZED styling, and `vdms_id` in run tooltips/feed — without regressing the global-job view.

**Architecture:** Three small read-side backend additions (`JobView` gains `scope`/`instanceCount`/`attentionCount` via one grouped instance-count query; `FeedRunView` gains `vdmsId`) feed a single-file vanilla-JS dashboard (`static/scheduler.html`). PER_VDMS job rows expand inline (third accordion level) and lazy-load `/api/jobs/{name}/instances`; per-instance actions POST to endpoints that already exist.

**Tech Stack:** Java 21 (Corretto 21.0.8), Spring Boot 4.0.6, Spring Data JPA, PostgreSQL + Flyway, JUnit 5 + Mockito + MockMvc + Testcontainers (backend). Frontend is a single static HTML file with inline vanilla JS/CSS — **no Node/bundler on this machine**, so UI changes are verified manually (Task 9), not by an automated JS harness.

---

## Build & test conventions (read once)

No global `mvn`; build from inside `sclera-scheduler` via its `.\mvnw.cmd` wrapper with `JAVA_HOME` set in the SAME command (env does not persist across tool calls). `@SpringBootTest` tests start a Testcontainers Postgres (Docker Desktop must be running).

```powershell
$env:JAVA_HOME = "C:\Users\DhanushVasanth\.jdks\corretto-21.0.8"
cd sclera-scheduler; .\mvnw.cmd -q test "-Dtest=SchedulerApiControllerTest"
```

Repo root (paths below are relative to it):
`C:\Users\DhanushVasanth\Desktop\AssetManagement POD\Microservice123\sclera-cloud-device-asset`

The dashboard is served by the running app at `http://localhost:8098/scheduler.html` (port from `application.yml`). Running it for manual UI checks is covered in Task 9.

---

## File Structure (decomposition locked here)

**Backend (`sclera-scheduler`):**
- Create `src/main/java/io/sclera/scheduler/domain/JobInstanceStateCount.java` — Spring Data projection (jobName, state, cnt).
- Modify `domain/JobInstanceRepository.java` — add `countByJobNameAndState()`.
- Modify `web/dto/JobView.java` — add `scope`, `instanceCount`, `attentionCount`.
- Modify `web/SchedulerApiController.java` — `list()` builds a counts map (one query) and `toView` populates the new fields.
- Modify `web/dto/FeedRunView.java` — add `vdmsId`.
- Modify `web/RunFeedController.java` — map `vdmsId`.
- Tests: `test/.../domain/JobInstanceRepositoryTest.java`, `test/.../web/SchedulerApiControllerTest.java`, `test/.../web/RunFeedControllerTest.java` (create).

**Frontend (single file):**
- Modify `src/main/resources/static/scheduler.html` — all UI increments (Tasks 4–8). New JS lives in dedicated functions (`renderInstances`, `openDelayPopover`, instance-action handler) kept separate from the existing `render()`/feed path.

---

## Phase 1 — Backend read surface (TDD, automated)

### Task 1: Grouped instance-count query

**Files:**
- Create: `sclera-scheduler/src/main/java/io/sclera/scheduler/domain/JobInstanceStateCount.java`
- Modify: `sclera-scheduler/src/main/java/io/sclera/scheduler/domain/JobInstanceRepository.java`
- Test: `sclera-scheduler/src/test/java/io/sclera/scheduler/domain/JobInstanceRepositoryTest.java`

- [ ] **Step 1: Add a failing repo test** — append to `JobInstanceRepositoryTest` (it already has `jobs`, `registry`, `instances` autowired and a `seed()` helper that creates the `vdmsSystemHealth` PER_VDMS job + `vdms-1`):

```java
    @Test
    void countsInstancesByJobNameAndState() {
        seed();
        registry.save(new VdmsRegistryEntity("vdms-2", "UTC", true));
        JobInstanceEntity a = new JobInstanceEntity("vdmsSystemHealth", "vdms-1", "vdmsSystemHealth::vdms-1");
        JobInstanceEntity b = new JobInstanceEntity("vdmsSystemHealth", "vdms-2", "vdmsSystemHealth::vdms-2");
        b.setState(JobInstanceState.SNOOZED);
        instances.save(a);
        instances.save(b);

        var counts = instances.countByJobNameAndState();
        long enabled = counts.stream().filter(c -> c.getJobName().equals("vdmsSystemHealth")
                && c.getState() == JobInstanceState.ENABLED).mapToLong(JobInstanceStateCount::getCnt).sum();
        long snoozed = counts.stream().filter(c -> c.getJobName().equals("vdmsSystemHealth")
                && c.getState() == JobInstanceState.SNOOZED).mapToLong(JobInstanceStateCount::getCnt).sum();
        assertThat(enabled).isEqualTo(1);
        assertThat(snoozed).isEqualTo(1);
    }
```

- [ ] **Step 2: Run test to verify it fails**

Run: `$env:JAVA_HOME="C:\Users\DhanushVasanth\.jdks\corretto-21.0.8"; cd sclera-scheduler; .\mvnw.cmd -q test "-Dtest=JobInstanceRepositoryTest"`
Expected: COMPILE FAILURE — `JobInstanceStateCount` and `countByJobNameAndState` do not exist.

- [ ] **Step 3a: Create the projection** `JobInstanceStateCount.java`:

```java
package io.sclera.scheduler.domain;

/** Spring Data projection: instance count per (jobName, state). */
public interface JobInstanceStateCount {
    String getJobName();
    JobInstanceState getState();
    long getCnt();
}
```

- [ ] **Step 3b: Add the query** to `JobInstanceRepository` (add imports `org.springframework.data.jpa.repository.Query` if absent):

```java
    @org.springframework.data.jpa.repository.Query(
        "select i.jobName as jobName, i.state as state, count(i) as cnt "
      + "from JobInstanceEntity i group by i.jobName, i.state")
    java.util.List<JobInstanceStateCount> countByJobNameAndState();
```

- [ ] **Step 4: Run test to verify it passes**

Run: `$env:JAVA_HOME="C:\Users\DhanushVasanth\.jdks\corretto-21.0.8"; cd sclera-scheduler; .\mvnw.cmd -q test "-Dtest=JobInstanceRepositoryTest"`
Expected: PASS (existing + new test).

- [ ] **Step 5: Commit**

```powershell
cd ..
git add sclera-scheduler/src/main/java/io/sclera/scheduler/domain/JobInstanceStateCount.java sclera-scheduler/src/main/java/io/sclera/scheduler/domain/JobInstanceRepository.java sclera-scheduler/src/test/java/io/sclera/scheduler/domain/JobInstanceRepositoryTest.java
git commit -m "feat(scheduler): grouped instance count-by-state query"
```

### Task 2: `JobView` scope + counts, wired in `list()`

**Files:**
- Modify: `sclera-scheduler/src/main/java/io/sclera/scheduler/web/dto/JobView.java`
- Modify: `sclera-scheduler/src/main/java/io/sclera/scheduler/web/SchedulerApiController.java`
- Test: `sclera-scheduler/src/test/java/io/sclera/scheduler/web/SchedulerApiControllerTest.java`

- [ ] **Step 1: Add a failing MockMvc test** — append to `SchedulerApiControllerTest` (it has `jobs`, `runs` autowired, a `mvc()` helper, and uses `jsonPath`). This test saves a GLOBAL job and a PER_VDMS job with 2 instances (1 SNOOZED) and asserts the new fields. It needs `instances` persisted via the real repo, so autowire it:

Add near the other `@Autowired` fields:
```java
    @Autowired JobInstanceRepository jobInstancesRepo;
    @Autowired VdmsRegistryRepository registryRepo;
```
Add the test:
```java
    @Test
    void listExposesScopeAndInstanceCounts() throws Exception {
        // GLOBAL job
        jobs.save(new JobEntity("snmpSync", "0 0 */3 * * *", "integrations",
                "scheduler.trigger", JobState.ENABLED));
        // PER_VDMS job with 2 instances, one SNOOZED
        JobEntity pv = new JobEntity("vdmsSystemHealth", "0 0 0 * * *", "device-asset",
                "scheduler.trigger", JobState.ENABLED);
        pv.setScope(JobScope.PER_VDMS);
        jobs.save(pv);
        registryRepo.save(new VdmsRegistryEntity("vdms-1", "UTC", true));
        registryRepo.save(new VdmsRegistryEntity("vdms-2", "UTC", true));
        jobInstancesRepo.save(new JobInstanceEntity("vdmsSystemHealth", "vdms-1", "vdmsSystemHealth::vdms-1"));
        JobInstanceEntity snoozed = new JobInstanceEntity("vdmsSystemHealth", "vdms-2", "vdmsSystemHealth::vdms-2");
        snoozed.setState(JobInstanceState.SNOOZED);
        jobInstancesRepo.save(snoozed);

        mvc().perform(get("/api/jobs"))
            .andExpect(status().isOk())
            // global job
            .andExpect(jsonPath("$[?(@.name=='snmpSync')].scope").value(org.hamcrest.Matchers.hasItem("GLOBAL")))
            .andExpect(jsonPath("$[?(@.name=='snmpSync')].instanceCount").value(org.hamcrest.Matchers.hasItem(0)))
            // per-vdms job: 2 instances, 1 not-ENABLED
            .andExpect(jsonPath("$[?(@.name=='vdmsSystemHealth')].scope").value(org.hamcrest.Matchers.hasItem("PER_VDMS")))
            .andExpect(jsonPath("$[?(@.name=='vdmsSystemHealth')].instanceCount").value(org.hamcrest.Matchers.hasItem(2)))
            .andExpect(jsonPath("$[?(@.name=='vdmsSystemHealth')].attentionCount").value(org.hamcrest.Matchers.hasItem(1)));
    }
```

- [ ] **Step 2: Run test to verify it fails**

Run: `$env:JAVA_HOME="C:\Users\DhanushVasanth\.jdks\corretto-21.0.8"; cd sclera-scheduler; .\mvnw.cmd -q test "-Dtest=SchedulerApiControllerTest"`
Expected: COMPILE FAILURE — `JobView` has no `scope`/`instanceCount`/`attentionCount` (8-arg record).

- [ ] **Step 3a: Extend `JobView`** (append the three fields):

```java
package io.sclera.scheduler.web.dto;

public record JobView(
    String name,
    String schedule,
    String owner,
    String state,
    String lastStatus,     // null if never run
    Long lastDurationMs,
    String lastFiredAt,     // ISO-8601, null if never run
    String nextFireAt,      // ISO-8601, null if unknown
    String scope,           // GLOBAL | PER_VDMS
    int instanceCount,      // 0 for GLOBAL
    int attentionCount) {}  // instances not in ENABLED state; 0 for GLOBAL
```

- [ ] **Step 3b: Wire the counts in `SchedulerApiController`.** Replace `list()` and `toView` (and add a small helper). The map is built ONCE per list call:

```java
    @GetMapping
    public List<JobView> list() {
        Map<String, int[]> counts = instanceCounts(); // jobName -> [total, attention]
        return jobs.findAll().stream().map(j -> toView(j, counts)).toList();
    }
```
Change `get(...)` to pass an on-demand count for the single job:
```java
    @GetMapping("/{name}")
    public ResponseEntity<JobView> get(@PathVariable String name) {
        Map<String, int[]> counts = instanceCounts();
        return jobs.findById(name).map(j -> ResponseEntity.ok(toView(j, counts)))
            .orElse(ResponseEntity.notFound().build());
    }
```
Add the helper and update `toView` (replace the existing `toView`):
```java
    // jobName -> [totalInstances, attentionInstances(not ENABLED)] in ONE query (no N+1).
    private Map<String, int[]> instanceCounts() {
        Map<String, int[]> m = new java.util.HashMap<>();
        for (JobInstanceStateCount c : jobInstances.countByJobNameAndState()) {
            int[] ta = m.computeIfAbsent(c.getJobName(), k -> new int[2]);
            ta[0] += (int) c.getCnt();
            if (c.getState() != JobInstanceState.ENABLED) ta[1] += (int) c.getCnt();
        }
        return m;
    }

    private JobView toView(JobEntity j, Map<String, int[]> counts) {
        Optional<JobRunEntity> last = j.getLastRunId() == null
            ? Optional.empty() : runs.findById(j.getLastRunId());
        int[] ta = counts.getOrDefault(j.getName(), new int[2]);
        boolean perVdms = j.getScope() == JobScope.PER_VDMS;
        return new JobView(
            j.getName(), j.getSchedule(), j.getOwner(), j.getState().name(),
            last.map(r -> r.getStatus().name()).orElse(null),
            last.map(JobRunEntity::getDurationMs).orElse(null),
            last.map(r -> iso(r.getFiredAt())).orElse(null),
            iso(j.getNextFireAt()),
            j.getScope().name(),
            perVdms ? ta[0] : 0,
            perVdms ? ta[1] : 0);
    }
```
Add `import java.util.Map;` (and `java.util.HashMap` is referenced fully-qualified above). `JobInstanceStateCount`, `JobScope`, `JobInstanceState` are in `io.sclera.scheduler.domain` and already covered by the existing `import io.sclera.scheduler.domain.*;`.

> Note: this changes the `toView` signature, so the existing `get(...)` call site is updated above. No other caller exists.

- [ ] **Step 4: Run test to verify it passes**

Run: `$env:JAVA_HOME="C:\Users\DhanushVasanth\.jdks\corretto-21.0.8"; cd sclera-scheduler; .\mvnw.cmd -q test "-Dtest=SchedulerApiControllerTest"`
Expected: PASS (all existing + new `listExposesScopeAndInstanceCounts`).

- [ ] **Step 5: Commit**

```powershell
cd ..
git add sclera-scheduler/src/main/java/io/sclera/scheduler/web/dto/JobView.java sclera-scheduler/src/main/java/io/sclera/scheduler/web/SchedulerApiController.java sclera-scheduler/src/test/java/io/sclera/scheduler/web/SchedulerApiControllerTest.java
git commit -m "feat(scheduler): expose scope + instance counts on JobView"
```

### Task 3: `vdmsId` on the run feed

**Files:**
- Modify: `sclera-scheduler/src/main/java/io/sclera/scheduler/web/dto/FeedRunView.java`
- Modify: `sclera-scheduler/src/main/java/io/sclera/scheduler/web/RunFeedController.java`
- Test: `sclera-scheduler/src/test/java/io/sclera/scheduler/web/RunFeedControllerTest.java` (create)

- [ ] **Step 1: Write the failing test** — a MockMvc slice mirroring `SchedulerApiControllerTest`'s setup style:

```java
package io.sclera.scheduler.web;

import io.sclera.scheduler.AbstractPostgresTest;
import io.sclera.scheduler.domain.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.Instant;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
class RunFeedControllerTest extends AbstractPostgresTest {

    @Autowired WebApplicationContext ctx;
    @Autowired JobRepository jobs;
    @Autowired JobRunRepository runs;

    @Test
    void feedExposesVdmsId() throws Exception {
        jobs.save(new JobEntity("vdmsSystemHealth", "0 0 0 * * *", "device-asset",
                "scheduler.trigger", JobState.ENABLED));
        runs.save(new JobRunEntity(UUID.randomUUID(), "vdmsSystemHealth", RunStatus.SUCCESS,
                false, Instant.parse("2026-06-09T00:00:00Z"), "vdms-7"));

        MockMvc mvc = MockMvcBuilders.webAppContextSetup(ctx).build();
        mvc.perform(get("/api/runs"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].jobName").value("vdmsSystemHealth"))
            .andExpect(jsonPath("$[0].vdmsId").value("vdms-7"));
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `$env:JAVA_HOME="C:\Users\DhanushVasanth\.jdks\corretto-21.0.8"; cd sclera-scheduler; .\mvnw.cmd -q test "-Dtest=RunFeedControllerTest"`
Expected: FAIL — `vdmsId` is not in the JSON (no such field on `FeedRunView`).

- [ ] **Step 3a: Add `vdmsId` to `FeedRunView`** (append the field):

```java
package io.sclera.scheduler.web.dto;

/** One run in the global activity feed (carries the job name, unlike per-job {@link RunView}). */
public record FeedRunView(
    String runId,
    String jobName,
    String status,
    boolean manual,
    String firedAt,
    String finishedAt,
    Long durationMs,
    String error,
    String vdmsId) {}   // null for GLOBAL-job runs
```

- [ ] **Step 3b: Map it in `RunFeedController.toView`:**

```java
    private static FeedRunView toView(JobRunEntity r) {
        return new FeedRunView(
            r.getRunId().toString(), r.getJobName(), r.getStatus().name(), r.isManual(),
            iso(r.getFiredAt()), iso(r.getFinishedAt()), r.getDurationMs(), r.getError(),
            r.getVdmsId());
    }
```

- [ ] **Step 4: Run test to verify it passes**

Run: `$env:JAVA_HOME="C:\Users\DhanushVasanth\.jdks\corretto-21.0.8"; cd sclera-scheduler; .\mvnw.cmd -q test "-Dtest=RunFeedControllerTest"`
Expected: PASS.

- [ ] **Step 5: Commit**

```powershell
cd ..
git add sclera-scheduler/src/main/java/io/sclera/scheduler/web/dto/FeedRunView.java sclera-scheduler/src/main/java/io/sclera/scheduler/web/RunFeedController.java sclera-scheduler/src/test/java/io/sclera/scheduler/web/RunFeedControllerTest.java
git commit -m "feat(scheduler): expose vdmsId on the run feed"
```

### Task 4 (backend gate): full scheduler suite

- [ ] **Step 1: Run the whole suite** to confirm the read-side changes didn't break anything:

Run: `$env:JAVA_HOME="C:\Users\DhanushVasanth\.jdks\corretto-21.0.8"; cd sclera-scheduler; .\mvnw.cmd test`
Expected: BUILD SUCCESS. If a test constructs `JobView`/`FeedRunView` positionally elsewhere, update it to the new arity. (At plan time the only constructors are in the two controllers, both updated above.)

- [ ] **Step 2: Commit any fixes** (only if needed)

```powershell
cd ..
git add sclera-scheduler
git commit -m "test(scheduler): align with JobView/FeedRunView additions"
```

---

## Phase 2 — Dashboard UI (`static/scheduler.html`, manual verification)

> All Phase-2 tasks edit the single file `sclera-scheduler/src/main/resources/static/scheduler.html`. There is no JS test runner (no Node), so each task's verification is a structural self-check of the diff; the full click-through happens once in Task 9. Keep commits small. Reuse the existing helpers `esc()`, `hhmm()`, `ago()`, `cls()`.

### Task 5: Scope badge, per-row summary, SNOOZED styling

**Files:**
- Modify: `sclera-scheduler/src/main/resources/static/scheduler.html`

- [ ] **Step 1: Add CSS** — in the `<style>` block, next to the existing `.st-PAUSED`/`.st-DISABLED` rules, add:

```css
  .st-SNOOZED{background:rgba(245,158,11,.15);color:var(--amber)}
  .scope-badge{font-size:.58rem;letter-spacing:.3px;padding:.05rem .35rem;border-radius:4px;
               margin-left:.4rem;background:rgba(59,130,246,.15);color:var(--blue);vertical-align:middle}
  .inst-summary{color:var(--muted);font-size:.72rem}
  .inst-summary .att{color:var(--amber)}
  .jcaret{display:inline-block;width:.9em;color:var(--muted);transition:transform .15s;cursor:pointer}
  .job.expanded .jcaret{transform:rotate(90deg)}
```

- [ ] **Step 2: Add a SNOOZED legend swatch** — in the `.legend` div (after the Pending entry), add:

```html
      <span class="lg"><span class="sw" style="background:var(--amber)"></span> Snoozed</span>
```

- [ ] **Step 3: Track per-job expansion** — near the existing `const collapsed = new Set();` add:

```javascript
const jobExpanded = new Set();   // job names whose instance list is open
const instCache = {};            // jobName -> JobInstanceView[]  (lazy)
const instLoading = {};          // jobName -> bool
const instFilter = {};           // jobName -> {q:'', state:'all'}
```

- [ ] **Step 4: Branch the job row on scope** — in `render()`, replace the per-job row construction (the block that builds `rows += \`<div class="job">…\``) so PER_VDMS jobs render a chevron + scope badge + summary instead of the timeline track. Replace the existing row-build with:

```javascript
    for(const job of list){
      const jr = byJob[job.name]||[];
      const ls = job.lastStatus;
      const sCls = ls? cls(ls):'none';
      const stateBadge = job.state!=='ENABLED' ? `<span class="badge-state st-${job.state}">${job.state}</span>` : '';

      if(job.scope==='PER_VDMS'){
        const open = jobExpanded.has(job.name);
        const att = job.attentionCount>0 ? `<span class="att">· ${job.attentionCount} need attention</span>` : '';
        rows += `
          <div class="job ${open?'expanded':''}" data-pervdms="${esc(job.name)}">
            <div>
              <div class="jname"><span class="jcaret">▸</span>${esc(job.name)}<span class="scope-badge">PER-VDMS</span></div>
              <div class="jsched">${esc(job.schedule)}</div>
            </div>
            <div class="inst-summary">${job.instanceCount} instance${job.instanceCount===1?'':'s'} ${att}</div>
            <div class="jstatus"><div class="s ${sCls}">${ls||'—'}</div></div>
          </div>
          <div class="instances" data-instances="${esc(job.name)}">${open?renderInstances(job.name):''}</div>`;
        continue;
      }

      // GLOBAL job — unchanged timeline rendering
      let dots='';
      for(const r of jr){
        const x=((new Date(r.firedAt)-start)/SPAN)*100;
        if(x<0||x>100) continue;
        const dur = r.durationMs!=null ? r.durationMs+' ms' : 'pending';
        dots += `<div class="pt ${cls(r.status)}" style="left:${x}%" title="${esc(job.name)} · ${r.status} · ${hhmm(r.firedAt)} · ${dur}${r.error?' · '+esc(r.error):''}"></div>`;
      }
      const failCount = jr.filter(r=>r.status==='FAILED').length;
      rows += `
        <div class="job">
          <div>
            <div class="jname">${esc(job.name)}${stateBadge}</div>
            <div class="jsched">${esc(job.schedule)}</div>
          </div>
          <div class="track">${gl}<div class="nowline" style="left:${nowPct}%"></div>${dots}</div>
          <div class="jstatus">
            <div class="s ${sCls}">${ls||'—'}</div>
            ${failCount?`<div class="jfail">${failCount} failure${failCount>1?'s':''}</div>`:''}
            <div class="jactions" data-job="${esc(job.name)}">
              <button class="ab run" data-act="run" title="Run now">Run</button>
              <button class="ab" data-act="pause" title="Pause">Pause</button>
              <button class="ab" data-act="resume" title="Resume">Resume</button>
              <button class="ab danger" data-act="disable" title="Disable">Disable</button>
            </div>
          </div>
        </div>`;
    }
```

- [ ] **Step 5: Add a stub `renderInstances`** so the page is valid now (filled in Task 6). Add near the other functions:

```javascript
function renderInstances(jobName){
  if(instLoading[jobName]) return `<div class="inst-msg">loading instances…</div>`;
  const data = instCache[jobName];
  if(!data) return `<div class="inst-msg">loading instances…</div>`;
  if(!data.length) return `<div class="inst-msg">no instances yet</div>`;
  return data.map(i=>`<div class="inst-row">${esc(i.vdmsId)} · ${esc(i.state)}</div>`).join('');
}
```

- [ ] **Step 6: Structural self-check + commit.** Re-read the edited regions: confirm the `render()` loop has exactly one `continue` for the PER_VDMS branch, the GLOBAL branch is byte-for-byte the prior behavior, and no template literal is left unterminated. Then:

```powershell
git add sclera-scheduler/src/main/resources/static/scheduler.html
git commit -m "feat(ui): scope badge, per-row instance summary, SNOOZED styling"
```

### Task 6: Drill-down — lazy fetch, instance rows, state-contextual actions

**Files:**
- Modify: `sclera-scheduler/src/main/resources/static/scheduler.html`

- [ ] **Step 1: Add instance CSS** — in `<style>`:

```css
  .instances{padding:0 .85rem .4rem 1.6rem;border-bottom:1px solid #16203a;background:#0d1424}
  .instances:empty{display:none}
  .inst-msg{color:var(--muted2);font-size:.74rem;padding:.45rem 0}
  .inst-row{display:grid;grid-template-columns:150px 96px 1fr auto;align-items:center;
            gap:.5rem;padding:.32rem 0;border-bottom:1px solid #121a2c}
  .inst-row:last-child{border-bottom:none}
  .inst-vdms{font-weight:600;white-space:nowrap;overflow:hidden;text-overflow:ellipsis}
  .inst-when{color:var(--muted2);font-size:.7rem}
  .inst-actions{display:flex;gap:.25rem;justify-content:flex-end}
```

- [ ] **Step 2: Implement the real `renderInstances`** (replace the Task-5 stub). It applies the per-job search/filter (filter state defaults are read defensively so this works before Task 7 adds the controls), shows status+timing, and emits state-contextual action buttons:

```javascript
function lastStatusFor(jobName, vdmsId){
  // derive from the runs cache (lastData.runs), newest-first already
  for(const r of lastData.runs){ if(r.jobName===jobName && r.vdmsId===vdmsId) return r.status; }
  return null;
}
function instActions(state){
  switch(state){
    case 'ENABLED':  return [['snooze','Snooze'],['run-at','Run at'],['pause','Pause'],['disable','Disable']];
    case 'PAUSED':   return [['resume','Resume'],['run-at','Run at'],['disable','Disable']];
    case 'SNOOZED':  return [['resume','Resume'],['run-at','Run at']];
    case 'DISABLED': return [['resume','Resume']];
    default:         return [];
  }
}
function renderInstances(jobName){
  if(instLoading[jobName] || !instCache[jobName]) return `<div class="inst-msg">loading instances…</div>`;
  let data = instCache[jobName];
  if(!data.length) return `<div class="inst-msg">no instances yet</div>`;
  const f = instFilter[jobName] || {q:'',state:'all'};
  let rows = data;
  if(f.state!=='all') rows = rows.filter(i=>i.state===f.state);
  if(f.q) rows = rows.filter(i=>i.vdmsId.toLowerCase().includes(f.q.toLowerCase()));
  const controls = instControls(jobName, f); // defined in Task 7; returns '' until then
  if(!rows.length) return controls + `<div class="inst-msg">no instances match</div>`;
  const body = rows.map(i=>{
    const ls = lastStatusFor(jobName, i.vdmsId);
    const sCls = ls? cls(ls):'none';
    const when = i.state==='SNOOZED' && i.snoozeUntil ? `until ${hhmm(i.snoozeUntil)}`
               : i.nextFireAt ? `next ${hhmm(i.nextFireAt)}` : '';
    const acts = instActions(i.state).map(([act,lbl])=>
      `<button class="ab${act==='disable'?' danger':''}" data-iact="${act}" data-job="${esc(jobName)}" data-vdms="${esc(i.vdmsId)}">${lbl}</button>`).join('');
    return `<div class="inst-row">
      <div class="inst-vdms" title="${esc(i.vdmsId)}">${esc(i.vdmsId)}</div>
      <div><span class="badge-state st-${i.state}">${i.state}</span></div>
      <div class="inst-when">${when}${ls?` · <span class="s ${sCls}" style="font-size:.7rem">${ls}</span>`:''}</div>
      <div class="inst-actions">${acts}</div>
    </div>`;
  }).join('');
  return controls + body;
}
function instControls(jobName, f){ return ''; } // replaced in Task 7
```

- [ ] **Step 3: Lazy-fetch on expand + refresh open jobs on poll.** Add a fetch helper and call it from the click handler and the poll. Add:

```javascript
async function loadInstances(jobName){
  instLoading[jobName] = true;
  try{ instCache[jobName] = await fetch(`/api/jobs/${encodeURIComponent(jobName)}/instances`).then(x=>x.json()); }
  catch(_){ instCache[jobName] = instCache[jobName] || []; }
  finally{ instLoading[jobName] = false; }
}
function refreshOpenInstances(){
  return Promise.all([...jobExpanded].map(loadInstances));
}
```

In the existing `#groups` click handler, add a branch for the PER_VDMS row (place it BEFORE the existing `.ab` job-action branch, AFTER the `[data-toggle]` owner branch):

```javascript
  const pv = e.target.closest('[data-pervdms]');
  if(pv && !e.target.closest('.inst-actions')){
    const name = pv.dataset.pervdms;
    if(jobExpanded.has(name)){ jobExpanded.delete(name); render(); return; }
    jobExpanded.add(name); render();                 // shows "loading…"
    if(!instCache[name]){ await loadInstances(name); render(); }
    return;
  }
  const iab = e.target.closest('[data-iact]');
  if(iab){
    const {iact, job, vdms} = iab.dataset;
    if(iact==='snooze' || iact==='run-at'){ openDelayPopover(iact, job, vdms, iab); return; } // Task 7
    iab.disabled=true;
    try{ await fetch(`/api/jobs/${encodeURIComponent(job)}/instances/${encodeURIComponent(vdms)}/${iact}`,{method:'POST'}); }
    catch(_){}
    await loadInstances(job); render();
    return;
  }
```

In `fetchData()`, after `lastData = {jobs:j, runs:r};` and before `render();`, refresh any open instance lists so states stay live:

```javascript
  await refreshOpenInstances();
```

- [ ] **Step 4: Add a no-op `openDelayPopover`** stub so snooze/run-at clicks don't error before Task 7:

```javascript
function openDelayPopover(kind, job, vdms, anchor){ /* implemented in Task 7 */ }
```

- [ ] **Step 5: Structural self-check + commit.** Confirm: the click handler still handles owner toggle and GLOBAL `.ab` actions; the PER_VDMS branch ignores clicks inside `.inst-actions`; `fetchData` awaits `refreshOpenInstances`. Then:

```powershell
git add sclera-scheduler/src/main/resources/static/scheduler.html
git commit -m "feat(ui): drill-down per-VDMS instances with state-contextual actions"
```

### Task 7: Search + state-filter chips + delay popover

**Files:**
- Modify: `sclera-scheduler/src/main/resources/static/scheduler.html`

- [ ] **Step 1: Add CSS** — in `<style>`:

```css
  .inst-controls{display:flex;align-items:center;gap:.5rem;padding:.4rem 0 .5rem;flex-wrap:wrap}
  .inst-search{background:var(--panel);border:1px solid var(--line);border-radius:6px;color:var(--txt);
               padding:.25rem .5rem;font-size:.74rem;min-width:150px}
  .chip{font-size:.66rem;padding:.12rem .5rem;border-radius:999px;cursor:pointer;
        border:1px solid var(--line);color:var(--muted);background:var(--panel)}
  .chip.active{color:var(--txt);border-color:#2b3a5e;background:#16203a}
  .popover{position:absolute;z-index:50;background:var(--panel2);border:1px solid #2b3a5e;border-radius:9px;
           padding:.6rem;box-shadow:0 8px 24px rgba(0,0,0,.5);width:230px}
  .popover h5{margin:0 0 .4rem;font-size:.72rem;color:var(--muted)}
  .popover .preset{display:block;width:100%;text-align:left;margin-bottom:.25rem}
  .popover .row2{display:flex;gap:.35rem;margin-top:.4rem}
  .popover input[type=datetime-local]{flex:1;background:var(--panel);border:1px solid var(--line);
        border-radius:6px;color:var(--txt);font-size:.72rem;padding:.2rem .35rem}
```

- [ ] **Step 2: Implement `instControls`** (replace the Task-6 stub) — renders the search box + state chips:

```javascript
function instControls(jobName, f){
  const chip = (val,lbl)=>`<span class="chip ${f.state===val?'active':''}" data-chip="${val}" data-job="${esc(jobName)}">${lbl}</span>`;
  return `<div class="inst-controls">
    <input class="inst-search" data-isearch="${esc(jobName)}" placeholder="search vdms…" value="${esc(f.q)}"/>
    ${chip('all','All')}${chip('ENABLED','Enabled')}${chip('SNOOZED','Snoozed')}${chip('PAUSED','Paused')}${chip('DISABLED','Disabled')}
  </div>`;
}
```

- [ ] **Step 3: Wire chip + search events.** In the `#groups` click handler add (before the PER_VDMS branch so chips inside the open section are caught):

```javascript
  const chip = e.target.closest('[data-chip]');
  if(chip){
    const name = chip.dataset.job;
    instFilter[name] = {...(instFilter[name]||{q:'',state:'all'}), state: chip.dataset.chip};
    render(); return;
  }
```
Add an `input` listener (the existing one is on `#search`; add a separate delegated listener on `#groups`):

```javascript
document.getElementById('groups').addEventListener('input', e=>{
  const s = e.target.closest('[data-isearch]');
  if(s){
    const name = s.dataset.isearch;
    instFilter[name] = {...(instFilter[name]||{q:'',state:'all'}), q: s.value};
    // re-render only this job's instance block to preserve input focus
    const block = document.querySelector(`[data-instances="${CSS.escape(name)}"]`);
    if(block){ block.innerHTML = renderInstances(name);
      const inp = block.querySelector('[data-isearch]'); if(inp){ inp.focus(); inp.setSelectionRange(inp.value.length, inp.value.length);} }
  }
});
```

- [ ] **Step 4: Implement `openDelayPopover`** (replace the Task-6 stub) — presets + custom datetime-local, POSTs ISO-8601:

```javascript
function fmtLocal(d){ // -> value for datetime-local (local time, no seconds)
  const p=n=>String(n).padStart(2,'0');
  return `${d.getFullYear()}-${p(d.getMonth()+1)}-${p(d.getDate())}T${p(d.getHours())}:${p(d.getMinutes())}`;
}
function tomorrowAt(h,m){ const d=new Date(); d.setDate(d.getDate()+1); d.setHours(h,m,0,0); return d; }
function todayAt(h,m){ const d=new Date(); d.setHours(h,m,0,0); if(d<=new Date()) d.setDate(d.getDate()+1); return d; }

function closePopover(){ const p=document.getElementById('delay-pop'); if(p) p.remove(); }
function openDelayPopover(kind, job, vdms, anchor){
  closePopover();
  const isSnooze = kind==='snooze';
  const presets = isSnooze
    ? [['+1 hour', new Date(Date.now()+3600e3)], ['+4 hours', new Date(Date.now()+4*3600e3)], ['Tomorrow 09:00', tomorrowAt(9,0)]]
    : [['In 1 hour', new Date(Date.now()+3600e3)], ['Tonight 20:00', todayAt(20,0)]];
  const pop = document.createElement('div');
  pop.className='popover'; pop.id='delay-pop';
  pop.innerHTML = `<h5>${isSnooze?'Snooze until':'Run at'} — ${esc(vdms)}</h5>
    ${presets.map((p,idx)=>`<button class="ab preset" data-iso="${p[1].toISOString()}">${p[0]}</button>`).join('')}
    <div class="row2"><input type="datetime-local" min="${fmtLocal(new Date())}" value="${fmtLocal(new Date(Date.now()+3600e3))}"/>
      <button class="ab run" data-custom="1">Set</button></div>`;
  document.body.appendChild(pop);
  const r = anchor.getBoundingClientRect();
  pop.style.top = (window.scrollY + r.bottom + 4)+'px';
  pop.style.left = (window.scrollX + Math.max(8, r.right - 230))+'px';

  async function send(iso){
    closePopover();
    const url = `/api/jobs/${encodeURIComponent(job)}/instances/${encodeURIComponent(vdms)}/${isSnooze?'snooze?until=':'run-at?at='}${encodeURIComponent(iso)}`;
    try{ await fetch(url,{method:'POST'}); }catch(_){}
    await loadInstances(job); render();
  }
  pop.querySelectorAll('[data-iso]').forEach(b=> b.addEventListener('click', ()=> send(b.dataset.iso)));
  pop.querySelector('[data-custom]').addEventListener('click', ()=>{
    const v = pop.querySelector('input[type=datetime-local]').value;
    if(!v) return;
    const d = new Date(v);
    if(d.getTime() <= Date.now()){ pop.querySelector('h5').textContent='Pick a future time'; return; }
    send(d.toISOString());
  });
}
// dismiss on outside click
document.addEventListener('click', e=>{
  const p=document.getElementById('delay-pop');
  if(p && !p.contains(e.target) && !e.target.closest('[data-iact]')) closePopover();
});
```

- [ ] **Step 5: Structural self-check + commit.** Confirm chips/search update `instFilter` and re-render; the search re-render preserves focus; the popover sends ISO-8601 and dismisses on outside click. Then:

```powershell
git add sclera-scheduler/src/main/resources/static/scheduler.html
git commit -m "feat(ui): instance search + state filters + snooze/run-at popover"
```

### Task 8: vdms_id in run-dot tooltips + live feed

**Files:**
- Modify: `sclera-scheduler/src/main/resources/static/scheduler.html`

- [ ] **Step 1: GLOBAL dot tooltip** — in the GLOBAL job dot loop (Task 5 step 4), append the vdms when present. Replace the `dots +=` line with:

```javascript
        const vd = r.vdmsId ? ' · '+esc(r.vdmsId) : '';
        dots += `<div class="pt ${cls(r.status)}" style="left:${x}%" title="${esc(job.name)} · ${r.status} · ${hhmm(r.firedAt)} · ${dur}${r.error?' · '+esc(r.error):''}${vd}"></div>`;
```
(GLOBAL jobs normally have null `vdmsId`; this is harmless and future-proofs mixed data.)

- [ ] **Step 2: Live feed row** — in the feed builder (`runs.slice(0,90).map(...)`), add the vdms to the meta line. Replace the `.meta` div with:

```javascript
      <div class="meta">${hhmm(fin)} · ${dur}${r.manual?' · manual':''}${r.vdmsId?' · '+esc(r.vdmsId):''} · ${ago(fin)}</div>
```

- [ ] **Step 3: Structural self-check + commit.**

```powershell
git add sclera-scheduler/src/main/resources/static/scheduler.html
git commit -m "feat(ui): show vdms_id in run tooltips and live feed"
```

### Task 9: Manual verification pass

**Files:** none (verification only).

There is no automated JS test harness. Verify the UI against a running app with seeded data. The dashboard's **read** paths and **client behavior** can be fully checked without a Dapr sidecar; the **action POSTs** (pause/resume/disable/snooze/run-at) call `SchedulerClient`/`JobService` → the Dapr sidecar, so without Dapr they will return 500 — verify those by confirming the correct request is sent (browser DevTools → Network) even if the backend errors, OR run with a Dapr sidecar for true end-to-end.

- [ ] **Step 1: Start the app against host Postgres** (Docker not required for the app itself; Postgres on localhost:5432 per `application.yml`):

```powershell
$env:JAVA_HOME="C:\Users\DhanushVasanth\.jdks\corretto-21.0.8"; cd sclera-scheduler; .\mvnw.cmd spring-boot:run
```
Wait for "Started Application". Leave it running.

- [ ] **Step 2: Seed a PER_VDMS job + instances** (new shell) so the drill-down has data. Connect to the `sclera_scheduler` schema and run:

```sql
INSERT INTO sclera_scheduler.job(name,schedule,owner,trigger_topic,state,scope,created_at,updated_at)
VALUES ('vdmsSystemHealth','0 0 0 * * *','device-asset','scheduler.trigger','ENABLED','PER_VDMS',now(),now())
ON CONFLICT (name) DO UPDATE SET scope='PER_VDMS';
INSERT INTO sclera_scheduler.vdms_registry(vdms_id,timezone,active,updated_at) VALUES
 ('vdms-101','America/New_York',true,now()),('vdms-102','Europe/London',true,now()),('vdms-103','UTC',true,now())
ON CONFLICT (vdms_id) DO NOTHING;
INSERT INTO sclera_scheduler.job_instance(job_name,vdms_id,state,dapr_job_name,next_fire_at,created_at,updated_at) VALUES
 ('vdmsSystemHealth','vdms-101','ENABLED','vdmsSystemHealth::vdms-101', now()+interval '6 hours', now(),now()),
 ('vdmsSystemHealth','vdms-102','SNOOZED','vdmsSystemHealth::vdms-102', now()+interval '1 day', now(),now()),
 ('vdmsSystemHealth','vdms-103','DISABLED','vdmsSystemHealth::vdms-103', null, now(),now())
ON CONFLICT (job_name,vdms_id) DO NOTHING;
UPDATE sclera_scheduler.job_instance SET snooze_until = now()+interval '4 hours'
 WHERE job_name='vdmsSystemHealth' AND vdms_id='vdms-102';
INSERT INTO sclera_scheduler.job_run(run_id,job_name,status,manual,fired_at,finished_at,duration_ms,vdms_id)
VALUES (gen_random_uuid(),'vdmsSystemHealth','SUCCESS',false, now()-interval '2 hours', now()-interval '2 hours', 250, 'vdms-101');
```

- [ ] **Step 3: Open `http://localhost:8098/scheduler.html` and walk the checklist:**
  - `vdmsSystemHealth` shows a `PER-VDMS` badge and `3 instances · 2 need attention`, with a chevron and **no** timeline track. GLOBAL jobs look exactly as before.
  - Click it → it expands and lazy-loads three instance rows: `vdms-101 ENABLED next …`, `vdms-102 SNOOZED until …` (amber badge), `vdms-103 DISABLED`.
  - The instance row for `vdms-101` shows last status `SUCCESS`.
  - State filter chips narrow the list (e.g. "Snoozed" → only vdms-102); the vdms search box filters by id and keeps focus while typing.
  - Action sets are state-correct: ENABLED shows Snooze/Run at/Pause/Disable; SNOOZED shows Resume/Run at; DISABLED shows Resume.
  - Click Snooze → popover with presets + datetime-local; picking a past time shows "Pick a future time"; a valid pick issues `POST …/snooze?until=<ISO>` (check Network tab). Same for Run at → `…/run-at?at=<ISO>`.
  - The live-feed row and the GLOBAL-dot tooltip show `· vdms-101` for the seeded run.
  - Collapsing/expanding and the 5 s poll keep instance state current.

- [ ] **Step 4: Record the result.** Note any deviations and fix in the relevant Phase-2 task (re-commit). When the checklist passes, this task is done. Stop the app (`Ctrl+C`).

> No commit for Task 9 unless fixes were needed.

---

## Self-Review

**Spec coverage:**
- `JobView` scope/instanceCount/attentionCount via one grouped query → Tasks 1, 2. ✓
- Inline drill-down (3rd level), lazy-load on expand, refresh while open → Task 6. ✓
- Instance row = status + timing (no per-instance timeline) → Task 6. ✓
- State-contextual actions → Task 6 (`instActions`). ✓
- Search + state-filter chips → Task 7. ✓
- Snooze/run-at popover, presets + custom, ISO-8601, past-time blocked → Task 7. ✓
- SNOOZED badge + legend → Task 5. ✓
- vdms_id in tooltips + feed (needs `FeedRunView.vdmsId`) → Tasks 3, 8. ✓
- GLOBAL jobs unchanged → Task 5 (GLOBAL branch preserved verbatim). ✓
- No global VDMS filter / no bulk actions / no Node → honored (not built). ✓
- Manual verification (honest about no JS harness) → Task 9. ✓

**Type/consistency checks:** `JobView` arity (11 fields) is used consistently by both `toView` call sites (Task 2 updates `get()` too). `FeedRunView` arity (9) updated with its sole constructor (Task 3). Client field names match DTO JSON: `job.scope`, `job.instanceCount`, `job.attentionCount`, `i.vdmsId`, `i.state`, `i.snoozeUntil`, `i.nextFireAt`, `r.vdmsId` — all match the records. The instance endpoint already returns `JobInstanceView(jobName, vdmsId, state, snoozeUntil, nextFireAt)`. Function names are stable across tasks: `renderInstances` (stub in Task 5 → real in Task 6), `instControls` (stub in Task 6 → real in Task 7), `openDelayPopover` (stub in Task 6 → real in Task 7), `loadInstances`/`refreshOpenInstances` (Task 6).

**Placeholder scan:** No TBD/TODO. The Task-5/6 stubs for `renderInstances`/`instControls`/`openDelayPopover` are explicit, intentional staged implementations (each replaced in a named later task), not vague placeholders — every step ships compiling, valid code.
