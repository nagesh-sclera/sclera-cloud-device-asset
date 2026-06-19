package io.sclera.workorder.controller;

import io.sclera.workorder.jobs.JobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Trigger endpoint for cron-driven background jobs.
 *
 * <p>The Dapr cron input-binding (see {@code components-docker/scheduler.yaml}) POSTs here on
 * each tick. The binding's {@code route} must target the full context-path, e.g.
 * {@code /api/v1/workorder-service/internal/jobs/status-refresh}. Not routed by the gateway —
 * reachable only by the local Dapr sidecar.
 */
@RestController
@RequestMapping("/internal/jobs")
@Tag(name = "Internal Jobs", description = "Cron-triggered background jobs (Dapr binding only)")
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    /**
     * Handles a cron tick for a named job. Always returns 200 so the Dapr binding is acked;
     * success/failure (and dead-lettering) are handled inside {@link JobService}.
     *
     * @param jobName the job to run (from the cron binding's route)
     * @return {@code {"job":..,"status":"SUCCESS|FAILED"}}
     */
    @Operation(summary = "Run a cron-triggered job (invoked by the Dapr cron binding)")
    @PostMapping("/{jobName}")
    public ResponseEntity<Map<String, String>> trigger(@PathVariable String jobName) {
        boolean ok = jobService.run(jobName);
        return ResponseEntity.ok(Map.of("job", jobName, "status", ok ? "SUCCESS" : "FAILED"));
    }
}
