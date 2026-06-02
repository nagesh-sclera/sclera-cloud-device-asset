package io.sclera.scheduler.subscriber;

import io.dapr.Topic;
import io.dapr.client.DaprClient;
import io.dapr.client.domain.CloudEvent;
import io.sclera.dapr.DaprEventSubscriber;
import io.sclera.dapr.events.SchedulerResultEvent;
import io.sclera.scheduler.domain.RunStatus;
import io.sclera.scheduler.service.RunRecorder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
public class ResultSubscriber extends DaprEventSubscriber<SchedulerResultEvent> {

    private final RunRecorder recorder;

    public ResultSubscriber(DaprClient dapr, RunRecorder recorder) {
        super(dapr, "scheduler.result");
        this.recorder = recorder;
    }

    @Topic(name = "scheduler.result", pubsubName = "pubsub",
           deadLetterTopic = "scheduler.result.dlq")
    @PostMapping("/internal/scheduler-result")
    public ResponseEntity<Map<String, String>> onResult(
            @RequestBody CloudEvent<SchedulerResultEvent> event) {
        return onEvent(event);
    }

    @Override
    protected void handleEvent(SchedulerResultEvent data) {
        RunStatus status = RunStatus.valueOf(data.status());
        recorder.recordResult(UUID.fromString(data.runId()), status, data.durationMs(), data.error());
    }
}
