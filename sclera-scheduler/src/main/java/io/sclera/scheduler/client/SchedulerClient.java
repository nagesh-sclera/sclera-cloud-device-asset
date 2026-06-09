package io.sclera.scheduler.client;

import io.sclera.scheduler.config.DaprProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.Map;

/**
 * The ONLY touch-point for the Dapr Jobs API (alpha). Registers/deletes jobs on the
 * local Dapr sidecar. The Scheduler control plane persists them in etcd and calls back
 * the app at POST /job/{name} when each fires.
 */
@Component
public class SchedulerClient {

    private static final Logger log = LoggerFactory.getLogger(SchedulerClient.class);
    private final RestClient http;

    // Spring injects this constructor in production.
    @Autowired
    public SchedulerClient(DaprProperties props) {
        this("http://localhost:" + props.httpPort());
    }

    // Test constructor — explicit base URL.
    public SchedulerClient(String daprBaseUrl) {
        this.http = RestClient.builder().baseUrl(daprBaseUrl).build();
    }

    /** Register or replace a job. Idempotent: re-posting the same name replaces it. */
    public void schedule(JobSchedule job) {
        Map<String, Object> body = Map.of(
            "schedule", effectiveSchedule(job.schedule(), job.timezone()),
            "data", Map.of("jobName", job.name())
        );
        http.post()
            .uri("/v1.0-alpha1/jobs/{name}", job.name())
            .contentType(MediaType.APPLICATION_JSON)
            .body(body)
            .retrieve()
            .toBodilessEntity();
        log.info("Scheduled job name={} schedule={} tz={}", job.name(), job.schedule(), job.timezone());
    }

    /** Register a one-shot job that fires once at dueTime, then Dapr auto-removes it. */
    public void scheduleOnce(String name, Instant dueTime) {
        Map<String, Object> body = Map.of(
            "dueTime", dueTime.toString(),
            "repeats", 1,
            "data", Map.of("jobName", name)
        );
        http.post()
            .uri("/v1.0-alpha1/jobs/{name}", name)
            .contentType(MediaType.APPLICATION_JSON)
            .body(body)
            .retrieve()
            .toBodilessEntity();
        log.info("Scheduled one-shot job name={} dueTime={}", name, dueTime);
    }

    // CRON_TZ applies only to cron expressions; "@every <dur>" interval schedules ignore tz.
    private static String effectiveSchedule(String schedule, String timezone) {
        if (timezone == null || timezone.isBlank() || schedule.startsWith("@")) {
            return schedule;
        }
        return "CRON_TZ=" + timezone + " " + schedule;
    }

    /** Remove a job from the Scheduler. Safe to call if it does not exist (404 is ignored). */
    public void delete(String name) {
        http.delete()
            .uri("/v1.0-alpha1/jobs/{name}", name)
            .retrieve()
            // Deleting a job the Scheduler never registered returns 404 — that is a no-op
            // for us (pause/disable on an unregistered job must not surface an error).
            .onStatus(status -> status.value() == 404, (req, res) -> { })
            .toBodilessEntity();
        log.info("Deleted job name={}", name);
    }
}
