package io.sclera.scheduler.service;

import io.sclera.scheduler.client.JobSchedule;
import io.sclera.scheduler.client.SchedulerClient;
import io.sclera.scheduler.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Re-arms snoozed per-VDMS instances whose snooze window has elapsed. This is an in-process
 * Spring {@code @Scheduled} (NOT a Dapr job) — same pattern as HousekeepingService — so snooze
 * state lives only in the DB. On expiry it re-registers the real job through the Dapr control
 * plane. Idempotent and per-item isolated: one failing re-arm must not block the rest.
 */
@Service
public class SnoozeReconciler {

    private static final Logger log = LoggerFactory.getLogger(SnoozeReconciler.class);

    private final JobInstanceRepository instances;
    private final JobRepository jobs;
    private final VdmsRegistryRepository registry;
    private final SchedulerClient scheduler;

    public SnoozeReconciler(JobInstanceRepository instances, JobRepository jobs,
                            VdmsRegistryRepository registry, SchedulerClient scheduler) {
        this.instances = instances;
        this.jobs = jobs;
        this.registry = registry;
        this.scheduler = scheduler;
    }

    @Scheduled(fixedDelayString = "${scheduler.snooze-scan-ms}")
    @Transactional
    public void rearmExpiredSnoozes() {
        List<JobInstanceEntity> due = instances.findByStateAndSnoozeUntilLessThanEqual(
                JobInstanceState.SNOOZED, Instant.now());
        int rearmed = 0;
        for (JobInstanceEntity inst : due) {
            try {
                String schedule = jobs.findById(inst.getJobName())
                        .map(JobEntity::getSchedule).orElse(null);
                if (schedule == null) {
                    log.warn("Snoozed instance {} references unknown job — skipping", inst.getDaprJobName());
                    continue;
                }
                String tz = registry.findById(inst.getVdmsId())
                        .map(VdmsRegistryEntity::getTimezone).orElse(null);
                scheduler.schedule(new JobSchedule(inst.getDaprJobName(), schedule, tz));
                inst.setState(JobInstanceState.ENABLED);
                inst.setSnoozeUntil(null);
                rearmed++;
            } catch (Exception e) {
                log.error("Failed to re-arm snoozed instance {}: {}", inst.getDaprJobName(), e.getMessage());
            }
        }
        if (rearmed > 0) log.info("Re-armed {} snoozed instances", rearmed);
    }
}
