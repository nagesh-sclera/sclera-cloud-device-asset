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
        for (JobEntity job : jobs.findByState(JobState.ENABLED)) {
            scheduler.schedule(new JobSchedule(job.getName(), job.getSchedule()));
        }
        log.info("Registered all enabled jobs with Dapr Scheduler");
    }

    @Transactional
    public void pause(String name) {
        JobEntity job = require(name);
        scheduler.delete(name);
        job.setState(JobState.PAUSED);
    }

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

    /** Fire immediately, bypassing the schedule. Records a manual run. */
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
