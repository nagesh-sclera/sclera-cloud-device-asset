package io.sclera.workorder.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Consumer of the scheduler's {@code scheduler.trigger} topic for workorder. Wired by the declarative
 * subscription {@code components-docker/subscription-scheduler-demo-workorder.yaml}, which routes the
 * event to {@code /api/v1/workorder-service/internal/scheduler-demo}.
 *
 * <p>Every subscribing app receives every trigger, so this handler routes by the event's {@code owner}
 * field: it prints for any job (recurring or one-time) owned by {@code workorder}, whatever its name,
 * and ignores the rest. It only prints — the scheduler's TriggerSimulatorSubscriber records the
 * SUCCESS for non-device-asset jobs in dev/docker.
 */
@RestController
public class SchedulerTriggerSubscriber {

    private static final Logger log = LoggerFactory.getLogger(SchedulerTriggerSubscriber.class);
    private static final String OWNER = "workorder";

    @PostMapping("/internal/scheduler-demo")
    @SuppressWarnings("unchecked")
    public ResponseEntity<Void> onTrigger(@RequestBody Map<String, Object> cloudEvent) {
        Object raw = cloudEvent.get("data");
        Map<String, Object> data = raw instanceof Map ? (Map<String, Object>) raw : null;
        String owner = data != null ? (String) data.get("owner") : null;
        String jobName = data != null ? (String) data.get("jobName") : null;
        if (OWNER.equals(owner)) {
            log.info("[scheduler-demo] workorder job fired name={} — hello from sclera-cloud-workorder", jobName);
        }
        return ResponseEntity.ok().build(); // not a workorder job — ignore
    }
}
