package io.sclera.scheduler;

import io.dapr.Topic;
import io.dapr.client.DaprClient;
import io.dapr.client.domain.CloudEvent;
import io.sclera.dapr.DaprEventPublisher;
import io.sclera.dapr.DaprEventSubscriber;
import io.sclera.dapr.events.SchedulerResultEvent;
import io.sclera.dapr.events.SchedulerTriggerEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Set;

/** Executes the device-asset-owned scheduler jobs and reports a result. Jobs owned by
 *  other services are ignored (a no-op, not a DLQ case). */
@RestController
public class TriggerDispatchSubscriber extends DaprEventSubscriber<SchedulerTriggerEvent> {

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
     */
    @Topic(name = "scheduler.trigger", pubsubName = "pubsub",
           deadLetterTopic = "scheduler.trigger.dlq")
    @PostMapping("/internal/scheduler-trigger")
    public ResponseEntity<Map<String, String>> onTrigger(
            @RequestBody CloudEvent<SchedulerTriggerEvent> event) {
        return onEvent(event);
    }

    @Override
    protected void handleEvent(SchedulerTriggerEvent data) {
        if (!OWNED.contains(data.jobName())) {
            return; // another service owns this job
        }
        long t0 = System.currentTimeMillis();
        try {
            run(data.jobName());
            publish(data, "SUCCESS", t0, null);
        } catch (Exception e) {
            publish(data, "FAILED", t0, e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
        }
    }

    private void run(String job) {
        switch (job) {
            case "historyRecord" -> handlers.historyRecord();
            case "unlinkVendorOrganisation" -> handlers.unlinkVendorOrganisation();
            case "internetBandwidthCheck" -> handlers.internetBandwidthCheck();
            case "vdmsSystemHealth" -> handlers.vdmsSystemHealth();
            case "connectedStatusForIOC" -> handlers.connectedStatusForIOC();
            case "qrcodeNfcBarcodeSync" -> handlers.qrcodeNfcBarcodeSync();
            case "syncAssetCountToCloud" -> handlers.syncAssetCountToCloud();
            case "userActionLog" -> handlers.userActionLog();
            case "deviceDndEnable" -> handlers.deviceDndEnable();
            case "offlineDeviceCheck" -> handlers.offlineDeviceCheck();
            default -> { /* unreachable: guarded by OWNED */ }
        }
    }

    private void publish(SchedulerTriggerEvent in, String status, long t0, String error) {
        publisher.publish(pubsubName, resultTopic,
            new SchedulerResultEvent(in.jobName(), in.runId(), status,
                System.currentTimeMillis() - t0, error));
    }
}
