package io.sclera.workorder.client;

import io.sclera.workorder.config.DaprProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Instant;

/**
 * Calls the central sclera-scheduler to create a one-time job, through the local Dapr sidecar's
 * service-invocation API (same daprRestClient used by {@link VdmsClient}).
 */
@Component
public class SchedulerClient {

    private static final Logger log = LoggerFactory.getLogger(SchedulerClient.class);

    private final RestClient daprRestClient;
    private final DaprProperties props;

    public SchedulerClient(RestClient daprRestClient, DaprProperties props) {
        this.daprRestClient = daprRestClient;
        this.props = props;
    }

    /**
     * Ask the central scheduler to fire an existing catalog job exactly once at {@code dueAt}.
     * Targets the scheduler's global run-at endpoint
     * ({@code POST /api/jobs/{jobName}/run-at?at=...}); the job's owner is resolved by the
     * scheduler from its catalog, so the resulting trigger reaches this service's
     * {@code SchedulerTriggerSubscriber} when {@code jobName} is workorder-owned.
     */
    public void scheduleOnce(String jobName, Instant dueAt) {
        daprRestClient.post()
            .uri(uriBuilder -> uriBuilder
                .path("/invoke/{appId}/method/api/jobs/{jobName}/run-at")
                .queryParam("at", dueAt.toString())
                .build(props.getSchedulerAppId(), jobName))
            .retrieve()
            .toBodilessEntity();
        log.info("Requested one-time fire of job={} dueAt={}", jobName, dueAt);
    }
}
