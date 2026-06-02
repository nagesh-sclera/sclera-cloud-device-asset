package io.sclera.scheduler.service;

import io.sclera.dapr.DaprEventPublisher;
import io.sclera.dapr.events.SchedulerTriggerEvent;
import io.sclera.scheduler.client.JobSchedule;
import io.sclera.scheduler.client.SchedulerClient;
import io.sclera.scheduler.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class JobService {

    private static final Logger log = LoggerFactory.getLogger(JobService.class);

    private final JobRepository jobs;
    private final SchedulerClient scheduler;
    private final DaprEventPublisher publisher;
    private final RunRecorder recorder;

    @Value("${scheduler.pubsub-name}") private String pubsubName;
    @Value("${scheduler.trigger-topic}") private String triggerTopic;

    public JobService(JobRepository jobs, SchedulerClient scheduler,
                      DaprEventPublisher publisher, RunRecorder recorder) {
        this.jobs = jobs;
        this.scheduler = scheduler;
        this.publisher = publisher;
        this.recorder = recorder;
    }

    void setPubsubName(String v) { this.pubsubName = v; }
    void setTriggerTopic(String v) { this.triggerTopic = v; }

    /** Called once at startup (Task 8 wires the runner). Registers all ENABLED jobs. */
    public void registerAll() {
        int ok = 0, failed = 0;
        for (JobEntity job : jobs.findByState(JobState.ENABLED)) {
            // Isolate each registration: one bad job (e.g. a schedule Dapr rejects) must
            // not prevent the remaining jobs from registering at startup.
            try {
                scheduler.schedule(new JobSchedule(job.getName(), job.getSchedule()));
                ok++;
            } catch (Exception e) {
                failed++;
                log.error("Failed to register job={} schedule={} error={}",
                    job.getName(), job.getSchedule(), e.getMessage());
            }
        }
        log.info("Registered enabled jobs with Dapr Scheduler: ok={} failed={}", ok, failed);
    }

    @Transactional
    public void pause(String name) {
        JobEntity job = require(name);
        scheduler.delete(name);
        job.setState(JobState.PAUSED);
    }

    /**
     * Re-arms a job from either PAUSED or DISABLED (there is no separate enable op).
     * schedule() runs before the state flip: on a tx rollback Dapr would hold the job
     * while the DB still shows the prior state — registerAll() on restart reconciles
     * ENABLED jobs, and a repeat resume is idempotent on the Dapr side.
     */
    @Transactional
    public void resume(String name) {
        JobEntity job = require(name);
        scheduler.schedule(new JobSchedule(name, job.getSchedule()));
        job.setState(JobState.ENABLED);
    }

    @Transactional
    public void disable(String name) {
        JobEntity job = require(name);
        scheduler.delete(name);
        job.setState(JobState.DISABLED);
    }

    /**
     * Fire immediately, bypassing the schedule. Records a manual run.
     * Best-effort: the FIRED run is recorded before publish. If publish fails we log
     * (not throw) — the run stays FIRED and the housekeeping reaper sweeps it to FAILED,
     * so a transient broker hiccup does not surface as an error to the operator.
     */
    public void runNow(String name) {
        require(name);
        UUID runId = UUID.randomUUID();
        recorder.recordFired(name, runId, true);
        var result = publisher.publish(pubsubName, triggerTopic,
            new SchedulerTriggerEvent(name, runId.toString(), System.currentTimeMillis()));
        if (!result.success()) {
            log.error("run-now publish failed job={} error={}", name, result.error());
        }
    }

    private JobEntity require(String name) {
        return jobs.findById(name)
            .orElseThrow(() -> new IllegalArgumentException("Unknown job: " + name));
    }
}
