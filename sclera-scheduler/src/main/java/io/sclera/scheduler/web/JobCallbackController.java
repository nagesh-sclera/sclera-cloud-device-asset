package io.sclera.scheduler.web;

import io.sclera.dapr.DaprEventPublisher;
import io.sclera.dapr.PublishResult;
import io.sclera.dapr.events.SchedulerTriggerEvent;
import io.sclera.scheduler.domain.JobEntity;
import io.sclera.scheduler.domain.JobRepository;
import io.sclera.scheduler.service.RunRecorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Dapr Scheduler invokes POST /job/{name} when a registered job fires. We record the run
 * and publish a scheduler.trigger event for the owning service to execute.
 *
 * <p>If publish fails, 500 is returned so Dapr retries the fire (scheduled triggers must
 * not be silently dropped). A retry creates a new runId; any orphaned FIRED row from the
 * failed attempt is reclaimed by the housekeeping reaper. This is the expected
 * at-least-once behavior, not a defect.
 */
@RestController
public class JobCallbackController {

    private static final Logger log = LoggerFactory.getLogger(JobCallbackController.class);

    private final RunRecorder recorder;
    private final DaprEventPublisher publisher;
    private final JobRepository jobs;

    @Value("${scheduler.pubsub-name}") private String pubsubName;
    @Value("${scheduler.trigger-topic}") private String triggerTopic;

    public JobCallbackController(RunRecorder recorder, DaprEventPublisher publisher, JobRepository jobs) {
        this.recorder = recorder;
        this.publisher = publisher;
        this.jobs = jobs;
    }

    void setPubsubName(String v) { this.pubsubName = v; }
    void setTriggerTopic(String v) { this.triggerTopic = v; }

    @PostMapping("/job/{name}")
    public ResponseEntity<Void> onJobFired(@PathVariable("name") String name) {
        // Dapr job names: "{jobName}" (global), "{jobName}::{vdmsId}" (per-VDMS),
        // "{jobName}::{vdmsId}::once-{id}" (per-VDMS one-shot), or
        // "{jobName}::once-{id}" (global one-shot). The one-shot marker is always the
        // last segment, so detect it from the tail rather than a fixed index.
        String[] parts = name.split("::");
        String jobName = parts[0];
        boolean oneShot = parts.length >= 2 && parts[parts.length - 1].startsWith("once-");
        String vdmsId = oneShot
            ? (parts.length >= 3 ? parts[1] : null)   // {job}::{vdms}::once-{id} vs {job}::once-{id}
            : (parts.length >= 2 ? parts[1] : null);   // {job}::{vdms}

        // owner drives subscriber routing; resolve it from the catalog (null if unknown).
        String owner = jobs.findById(jobName).map(JobEntity::getOwner).orElse(null);

        UUID runId = UUID.randomUUID();
        recorder.recordFired(jobName, runId, oneShot, vdmsId);
        PublishResult result = publisher.publish(pubsubName, triggerTopic,
            new SchedulerTriggerEvent(jobName, runId.toString(), vdmsId, owner, System.currentTimeMillis()));
        if (!result.success()) {
            log.error("Trigger publish failed job={} vdmsId={} runId={} error={}",
                jobName, vdmsId, runId, result.error());
            return ResponseEntity.internalServerError().build();
        }
        return ResponseEntity.ok().build();
    }
}
