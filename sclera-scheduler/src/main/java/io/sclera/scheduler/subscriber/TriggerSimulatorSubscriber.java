package io.sclera.scheduler.subscriber;

import io.dapr.Topic;
import io.dapr.client.DaprClient;
import io.dapr.client.domain.CloudEvent;
import io.sclera.dapr.DaprEventPublisher;
import io.sclera.dapr.DaprEventSubscriber;
import io.sclera.dapr.events.SchedulerResultEvent;
import io.sclera.dapr.events.SchedulerTriggerEvent;
import io.sclera.scheduler.domain.JobRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * DEV/DOCKER ONLY. Stand-in for owning services not running in this workspace: for any job
 * whose catalog owner != "device-asset", publishes a simulated SUCCESS so the dashboard
 * shows the full FIRED→SUCCESS lifecycle. NOT a real dispatcher; never loads in prod.
 *
 * <p>Jobs owned by "device-asset" are skipped — the real TriggerDispatchSubscriber in
 * sclera-cloud-device-asset handles those and publishes its own result.</p>
 */
@Profile({"dev", "docker"})
@RestController
public class TriggerSimulatorSubscriber extends DaprEventSubscriber<SchedulerTriggerEvent> {

    private static final long SIMULATED_DURATION_MS = 5L;

    private final DaprEventPublisher publisher;
    private final JobRepository jobRepository;

    @Value("${scheduler.pubsub-name:pubsub}") private String pubsubName;
    @Value("${scheduler.result-topic:scheduler.result}") private String resultTopic;

    public TriggerSimulatorSubscriber(DaprClient dapr, DaprEventPublisher publisher,
                                      JobRepository jobRepository) {
        super(dapr, "scheduler.trigger");
        this.publisher = publisher;
        this.jobRepository = jobRepository;
    }

    void setPubsubName(String v) { this.pubsubName = v; }
    void setResultTopic(String v) { this.resultTopic = v; }

    @Topic(name = "scheduler.trigger", pubsubName = "pubsub",
           deadLetterTopic = "scheduler.trigger.dlq")
    @PostMapping("/internal/scheduler-trigger-sim")
    public ResponseEntity<Map<String, String>> onTrigger(
            @RequestBody CloudEvent<SchedulerTriggerEvent> event) {
        return onEvent(event);
    }

    @Override
    protected void handleEvent(SchedulerTriggerEvent data) {
        String owner = jobRepository.findById(data.jobName())
            .map(j -> j.getOwner())
            .orElse(null);
        if ("device-asset".equals(owner)) {
            return; // real device-asset dispatcher owns this — avoid double results
        }
        publisher.publish(pubsubName, resultTopic,
            new SchedulerResultEvent(data.jobName(), data.runId(), "SUCCESS",
                SIMULATED_DURATION_MS, null));
    }
}
