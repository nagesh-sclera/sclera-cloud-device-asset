package io.sclera.scheduler;

import io.dapr.Topic;
import io.dapr.client.DaprClient;
import io.dapr.client.domain.CloudEvent;
import io.sclera.dapr.DaprEventPublisher;
import io.sclera.dapr.DaprEventSubscriber;
import io.sclera.dapr.events.SchedulerResultEvent;
import io.sclera.dapr.events.SchedulerTriggerEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Set;

/**
 * Dapr event subscriber that consumes scheduler trigger / dispatch events.
 *
 * <p>Subscribes to the {@code scheduler.trigger} pub/sub topic. Each event names a
 * scheduler job ({@code jobName}), an idempotency {@code runId} and an optional
 * {@code vdmsId} for per-VDMS jobs. When the named job is one this service owns
 * (see {@link #OWNED}), the corresponding {@link DeviceAssetJobHandlers} method is
 * invoked and a {@link SchedulerResultEvent} carrying {@code SUCCESS} or
 * {@code FAILED} (plus elapsed time and any error message) is published to the
 * configured result topic.
 *
 * <p>Jobs owned by other services are silently ignored (a no-op, not a dead-letter
 * case): no handler is invoked and no result is published. Handler exceptions are
 * caught and reported as a {@code FAILED} result rather than being propagated, so a
 * single failing job does not poison the subscription.
 */
@RestController
public class TriggerDispatchSubscriber extends DaprEventSubscriber<SchedulerTriggerEvent> {

    private static final Logger log = LoggerFactory.getLogger(TriggerDispatchSubscriber.class);

    private static final Set<String> OWNED = Set.of(
        "historyRecord", "unlinkVendorOrganisation", "internetBandwidthCheck",
        "vdmsSystemHealth", "connectedStatusForIOC", "qrcodeNfcBarcodeSync",
        "syncAssetCountToCloud", "userActionLog", "deviceDndEnable", "offlineDeviceCheck");

    private final DaprEventPublisher publisher;
    private final DeviceAssetJobHandlers handlers;

    @Value("${scheduler.pubsub-name:pubsub}") private String pubsubName;
    @Value("${scheduler.result-topic:scheduler.result}") private String resultTopic;

    public TriggerDispatchSubscriber(DaprClient dapr, DaprEventPublisher publisher,
                                     DeviceAssetJobHandlers handlers) {
        super(dapr, "scheduler.trigger");
        this.publisher = publisher;
        this.handlers = handlers;
    }

    void setPubsubName(String v) { this.pubsubName = v; }
    void setResultTopic(String v) { this.resultTopic = v; }

    /**
     * Dapr subscription endpoint for the {@code scheduler.trigger} topic; forwards the
     * incoming cloud event to the base subscriber's event-handling pipeline.
     *
     * @param event the Dapr cloud event wrapping the {@link SchedulerTriggerEvent} payload
     * @return the base subscriber's acknowledgement response for the event
     */
    @Topic(name = "scheduler.trigger", pubsubName = "pubsub",
           deadLetterTopic = "scheduler.trigger.dlq")
    @PostMapping("/internal/scheduler-trigger")
    public ResponseEntity<Map<String, String>> onTrigger(
            @RequestBody CloudEvent<SchedulerTriggerEvent> event) {
        log.info("onTrigger eventId={}", event != null ? event.getId() : null);
        return onEvent(event);
    }

    /**
     * Dispatches a single scheduler trigger event to its device-asset job handler.
     *
     * <p>Ignores jobs owned by other services. For owned jobs the matching handler is
     * invoked and a {@link SchedulerResultEvent} ({@code SUCCESS} or, on exception,
     * {@code FAILED}) is published. Handler exceptions are caught and reported, never
     * propagated.
     *
     * @param data the scheduler trigger payload (job name, run id, optional VDMS id)
     */
    @Override
    protected void handleEvent(SchedulerTriggerEvent data) {
        if (!OWNED.contains(data.jobName())) {
            log.info("handleEvent ignored job={} runId={} vdmsId={} (not owned)",
                data.jobName(), data.runId(), data.vdmsId());
            return; // another service owns this job
        }
        log.info("handleEvent job={} runId={} vdmsId={}",
            data.jobName(), data.runId(), data.vdmsId());
        long t0 = System.currentTimeMillis();
        try {
            run(data.jobName(), data.vdmsId());
            publish(data, "SUCCESS", t0, null);
        } catch (Exception e) {
            log.error("handleEvent job={} runId={} failed: {}",
                data.jobName(), data.runId(), e.getMessage(), e);
            publish(data, "FAILED", t0, e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
        }
    }

    /**
     * Invokes the {@link DeviceAssetJobHandlers} method matching the given owned job name.
     *
     * @param job    the owned scheduler job name (guaranteed present in {@link #OWNED})
     * @param vdmsId the target VDMS id for per-VDMS jobs; may be {@code null} for global jobs
     */
    private void run(String job, String vdmsId) {
        switch (job) {
            case "historyRecord" -> handlers.historyRecord();
            case "unlinkVendorOrganisation" -> handlers.unlinkVendorOrganisation();
            case "internetBandwidthCheck" -> handlers.internetBandwidthCheck();
            case "vdmsSystemHealth" -> handlers.vdmsSystemHealth(vdmsId);
            case "connectedStatusForIOC" -> handlers.connectedStatusForIOC();
            case "qrcodeNfcBarcodeSync" -> handlers.qrcodeNfcBarcodeSync();
            case "syncAssetCountToCloud" -> handlers.syncAssetCountToCloud();
            case "userActionLog" -> handlers.userActionLog();
            case "deviceDndEnable" -> handlers.deviceDndEnable();
            case "offlineDeviceCheck" -> handlers.offlineDeviceCheck();
            default -> { /* unreachable: guarded by OWNED */ }
        }
    }

    /**
     * Publishes a {@link SchedulerResultEvent} summarising the outcome of a job run.
     *
     * @param in     the originating trigger event (supplies job name and run id)
     * @param status the outcome status, e.g. {@code SUCCESS} or {@code FAILED}
     * @param t0     the run start time in epoch millis, used to compute elapsed duration
     * @param error  a short error description for failures, or {@code null} on success
     */
    private void publish(SchedulerTriggerEvent in, String status, long t0, String error) {
        publisher.publish(pubsubName, resultTopic,
            new SchedulerResultEvent(in.jobName(), in.runId(), status,
                System.currentTimeMillis() - t0, error));
    }
}
