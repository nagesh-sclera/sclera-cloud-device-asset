package io.sclera.scheduler.web;

import io.sclera.scheduler.domain.JobRunEntity;
import io.sclera.scheduler.domain.JobRunRepository;
import io.sclera.scheduler.web.dto.FeedRunView;
import org.springframework.data.domain.Limit;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

/**
 * Global run feed across all jobs (newest first). Powers the dashboard's per-job
 * timelines and the Live Feed panel from a single request, so the page does not have
 * to fan out one /runs call per job.
 */
@RestController
public class RunFeedController {

    private final JobRunRepository runs;

    public RunFeedController(JobRunRepository runs) {
        this.runs = runs;
    }

    @GetMapping("/api/runs")
    public List<FeedRunView> feed(@RequestParam(defaultValue = "500") int limit) {
        if (limit <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "limit must be > 0");
        }
        return runs.findByOrderByFiredAtDesc(Limit.of(limit))
            .stream().map(RunFeedController::toView).toList();
    }

    private static FeedRunView toView(JobRunEntity r) {
        return new FeedRunView(
            r.getRunId().toString(), r.getJobName(), r.getStatus().name(), r.isManual(),
            iso(r.getFiredAt()), iso(r.getFinishedAt()), r.getDurationMs(), r.getError());
    }

    private static String iso(Instant t) { return t == null ? null : t.toString(); }
}
