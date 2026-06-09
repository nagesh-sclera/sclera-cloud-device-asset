package io.sclera.scheduler.web;

import io.sclera.scheduler.domain.*;
import io.sclera.scheduler.service.JobService;
import io.sclera.scheduler.web.dto.JobInstanceView;
import io.sclera.scheduler.web.dto.JobView;
import io.sclera.scheduler.web.dto.RunView;
import org.springframework.data.domain.Limit;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/jobs")
public class SchedulerApiController {

    private final JobRepository jobs;
    private final JobRunRepository runs;
    private final JobInstanceRepository jobInstances;
    private final JobService jobService;

    public SchedulerApiController(JobRepository jobs, JobRunRepository runs,
                                  JobInstanceRepository jobInstances, JobService jobService) {
        this.jobs = jobs;
        this.runs = runs;
        this.jobInstances = jobInstances;
        this.jobService = jobService;
    }

    @GetMapping
    public List<JobView> list() {
        Map<String, int[]> counts = instanceCounts();
        return jobs.findAll().stream().map(j -> toView(j, counts)).toList();
    }

    @GetMapping("/{name}")
    public ResponseEntity<JobView> get(@PathVariable String name) {
        Map<String, int[]> counts = instanceCounts();
        return jobs.findById(name).map(j -> ResponseEntity.ok(toView(j, counts)))
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{name}/runs")
    public List<RunView> history(@PathVariable String name,
                                 @RequestParam(defaultValue = "50") int limit) {
        // Reject a bad limit explicitly: Limit.of(<=0) would otherwise throw
        // IllegalArgumentException and be misclassified as 404 by the handler below.
        if (limit <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "limit must be > 0");
        }
        return runs.findByJobNameOrderByFiredAtDesc(name, Limit.of(limit))
            .stream().map(this::toRunView).toList();
    }

    @PostMapping("/{name}/pause")
    public void pause(@PathVariable String name) { jobService.pause(name); }

    @PostMapping("/{name}/resume")
    public void resume(@PathVariable String name) { jobService.resume(name); }

    @PostMapping("/{name}/disable")
    public void disable(@PathVariable String name) { jobService.disable(name); }

    @PostMapping("/{name}/run")
    public void run(@PathVariable String name) { jobService.runNow(name); }

    @GetMapping("/{name}/instances")
    public List<JobInstanceView> instances(@PathVariable String name) {
        return jobInstances.findByJobName(name).stream().map(i -> new JobInstanceView(
            i.getJobName(), i.getVdmsId(), i.getState().name(),
            iso(i.getSnoozeUntil()), iso(i.getNextFireAt()))).toList();
    }

    @PostMapping("/{name}/instances/{vdmsId}/pause")
    public void pauseInstance(@PathVariable String name, @PathVariable String vdmsId) {
        jobService.pauseInstance(name, vdmsId);
    }

    @PostMapping("/{name}/instances/{vdmsId}/resume")
    public void resumeInstance(@PathVariable String name, @PathVariable String vdmsId) {
        jobService.resumeInstance(name, vdmsId);
    }

    @PostMapping("/{name}/instances/{vdmsId}/disable")
    public void disableInstance(@PathVariable String name, @PathVariable String vdmsId) {
        jobService.disableInstance(name, vdmsId);
    }

    @PostMapping("/{name}/instances/{vdmsId}/snooze")
    public void snooze(@PathVariable String name, @PathVariable String vdmsId,
                       @RequestParam("until") String until) {
        jobService.snoozeInstance(name, vdmsId, Instant.parse(until));
    }

    @PostMapping("/{name}/instances/{vdmsId}/run-at")
    public void runAt(@PathVariable String name, @PathVariable String vdmsId,
                      @RequestParam("at") String at) {
        jobService.runAtInstance(name, vdmsId, Instant.parse(at));
    }

    // Thrown by JobService.require() for an unknown job name. The bad-limit case is
    // handled separately above (400) so this only ever means "no such job" (404).
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public void notFound() {}

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

    private RunView toRunView(JobRunEntity r) {
        return new RunView(r.getRunId().toString(), r.getStatus().name(), r.isManual(),
            iso(r.getFiredAt()), iso(r.getFinishedAt()), r.getDurationMs(), r.getError());
    }

    private static String iso(Instant t) { return t == null ? null : t.toString(); }
}
