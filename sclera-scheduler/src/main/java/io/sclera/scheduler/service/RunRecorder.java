package io.sclera.scheduler.service;

import io.sclera.scheduler.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class RunRecorder {

    private static final Logger log = LoggerFactory.getLogger(RunRecorder.class);
    private final JobRepository jobs;
    private final JobRunRepository runs;

    public RunRecorder(JobRepository jobs, JobRunRepository runs) {
        this.jobs = jobs;
        this.runs = runs;
    }

    @Transactional
    public void recordFired(String jobName, UUID runId, boolean manual) {
        runs.save(new JobRunEntity(runId, jobName, RunStatus.FIRED, manual, Instant.now()));
        jobs.findById(jobName).ifPresent(j -> j.setLastRunId(runId));
        log.info("Run fired job={} runId={} manual={}", jobName, runId, manual);
    }

    @Transactional
    public void recordResult(UUID runId, RunStatus status, long durationMs, String error) {
        runs.findById(runId).ifPresentOrElse(run -> {
            run.setStatus(status);
            run.setFinishedAt(Instant.now());
            run.setDurationMs(durationMs);
            run.setError(error);
            log.info("Run result runId={} status={} durationMs={}", runId, status, durationMs);
        }, () -> log.warn("Result for unknown runId={} ignored", runId));
    }
}
