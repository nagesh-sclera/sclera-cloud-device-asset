package io.sclera.vdms.subscriber;

import io.dapr.Topic;
import io.dapr.client.DaprClient;
import io.dapr.client.domain.CloudEvent;
import io.sclera.dapr.DaprEventSubscriber;
import io.sclera.dapr.events.VdmsAgentPermissionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class VdmsAgentPermissionSubscriber extends DaprEventSubscriber<VdmsAgentPermissionEvent> {

    private static final Logger log = LoggerFactory.getLogger(VdmsAgentPermissionSubscriber.class);

    public VdmsAgentPermissionSubscriber(DaprClient dapr) {
        super(dapr, "vdms.set-agent-permission");
    }

    @Topic(name = "vdms.set-agent-permission", pubsubName = "pubsub",
           deadLetterTopic = "vdms.set-agent-permission.dlq")
    @PostMapping("/vdms/set-agent-permission")
    public ResponseEntity<Map<String, String>> onAgentPermission(
            @RequestBody CloudEvent<VdmsAgentPermissionEvent> event) {
        return onEvent(event);
    }

    @Override
    protected void handleEvent(VdmsAgentPermissionEvent data) {
        log.info("Received set-agent-permission vdmsId={} agentId={} permission={}",
            data.vdmsId(), data.agentId(), data.permission());
    }
}
