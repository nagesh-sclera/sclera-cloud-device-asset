package io.sclera.scheduler.client;

import io.sclera.scheduler.config.DaprProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

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
        // Jobs API body: { "schedule": "<cron|@every>", "data": { "jobName": "<name>" } }
        // data is echoed back to POST /job/{name}; we key everything on the path name.
        Map<String, Object> body = Map.of(
            "schedule", job.schedule(),
            "data", Map.of("jobName", job.name())
        );
        http.post()
            .uri("/v1.0-alpha1/jobs/{name}", job.name())
            .body(body)
            .retrieve()
            .toBodilessEntity();
        log.info("Scheduled job name={} schedule={}", job.name(), job.schedule());
    }

    /** Remove a job from the Scheduler. Safe to call if it does not exist. */
    public void delete(String name) {
        http.delete()
            .uri("/v1.0-alpha1/jobs/{name}", name)
            .retrieve()
            .toBodilessEntity();
        log.info("Deleted job name={}", name);
    }
}
