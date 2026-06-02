package io.sclera.scheduler.web;

import io.sclera.scheduler.domain.*;
import io.sclera.scheduler.service.JobService;
import io.sclera.scheduler.web.dto.JobView;
import io.sclera.scheduler.web.dto.RunView;
import org.springframework.data.domain.Limit;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/jobs")
public class SchedulerApiController {

    private final JobRepository jobs;
    private final JobRunRepository runs;
    private final JobService jobService;

    public SchedulerApiController(JobRepository jobs, JobRunRepository runs, JobService jobService) {
        this.jobs = jobs;
        this.runs = runs;
        this.jobService = jobService;
    }

    @GetMapping
    public List<JobView> list() {
        return jobs.findAll().stream().map(this::toView).toList();
    }

    @GetMapping("/{name}")
    public ResponseEntity<JobView> get(@PathVariable String name) {
        return jobs.findById(name).map(j -> ResponseEntity.ok(toView(j)))
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

    // Thrown by JobService.require() for an unknown job name. The bad-limit case is
    // handled separately above (400) so this only ever means "no such job" (404).
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public void notFound() {}

    private JobView toView(JobEntity j) {
        Optional<JobRunEntity> last = j.getLastRunId() == null
            ? Optional.empty() : runs.findById(j.getLastRunId());
        return new JobView(
            j.getName(), j.getSchedule(), j.getOwner(), j.getState().name(),
            last.map(r -> r.getStatus().name()).orElse(null),
            last.map(JobRunEntity::getDurationMs).orElse(null),
            last.map(r -> iso(r.getFiredAt())).orElse(null),
            iso(j.getNextFireAt()));
    }

    private RunView toRunView(JobRunEntity r) {
        return new RunView(r.getRunId().toString(), r.getStatus().name(), r.isManual(),
            iso(r.getFiredAt()), iso(r.getFinishedAt()), r.getDurationMs(), r.getError());
    }

    private static String iso(Instant t) { return t == null ? null : t.toString(); }
}
