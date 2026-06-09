package io.sclera.scheduler.subscriber;

import io.dapr.Topic;
import io.dapr.client.DaprClient;
import io.dapr.client.domain.CloudEvent;
import io.sclera.dapr.DaprEventSubscriber;
import io.sclera.dapr.events.VdmsLifecycleEvent;
import io.sclera.scheduler.service.PerVdmsRegistrar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** Registers/tears down a VDMS's per-VDMS jobs when the VDMS-owner publishes a lifecycle event. */
@RestController
public class VdmsLifecycleSubscriber extends DaprEventSubscriber<VdmsLifecycleEvent> {

    private static final Logger log = LoggerFactory.getLogger(VdmsLifecycleSubscriber.class);
    private final PerVdmsRegistrar registrar;

    public VdmsLifecycleSubscriber(DaprClient dapr, PerVdmsRegistrar registrar) {
        super(dapr, "vdms.lifecycle");
        this.registrar = registrar;
    }

    @Topic(name = "vdms.lifecycle", pubsubName = "pubsub",
           deadLetterTopic = "vdms.lifecycle.dlq")
    @PostMapping("/internal/vdms-lifecycle")
    public ResponseEntity<Map<String, String>> onLifecycle(
            @RequestBody CloudEvent<VdmsLifecycleEvent> event) {
        return onEvent(event);
    }

    @Override
    protected void handleEvent(VdmsLifecycleEvent data) {
        if (data.vdmsId() == null || data.vdmsId().isBlank()) {
            throw new IllegalArgumentException("vdmsId is required");
        }
        switch (data.status()) {
            case "ACTIVATED" -> registrar.onVdmsActivated(data.vdmsId(), data.timezone());
            case "DEACTIVATED" -> registrar.onVdmsDeactivated(data.vdmsId());
            default -> throw new IllegalArgumentException("Unknown VDMS lifecycle status: " + data.status());
        }
        log.info("Handled VDMS lifecycle vdmsId={} status={}", data.vdmsId(), data.status());
    }
}
