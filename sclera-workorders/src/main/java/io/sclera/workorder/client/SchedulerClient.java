package io.sclera.workorder.client;

import io.sclera.workorder.config.DaprProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.Map;

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

    /** Ask the scheduler to fire a one-time job named {@code name} once at {@code dueAt}. */
    public void scheduleOneTime(String name, String owner, Instant dueAt) {
        daprRestClient.post()
            .uri("/invoke/{appId}/method/api/jobs/onetime", props.getSchedulerAppId())
            .contentType(MediaType.APPLICATION_JSON)
            .body(Map.of("name", name, "owner", owner, "dueAt", dueAt.toString()))
            .retrieve()
            .toBodilessEntity();
        log.info("Requested one-time job name={} owner={} dueAt={}", name, owner, dueAt);
    }
}
