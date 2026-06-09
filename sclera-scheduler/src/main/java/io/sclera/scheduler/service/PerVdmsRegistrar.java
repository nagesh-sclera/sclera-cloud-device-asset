package io.sclera.scheduler.service;

import io.sclera.scheduler.client.JobSchedule;
import io.sclera.scheduler.client.SchedulerClient;
import io.sclera.scheduler.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Owns per-VDMS job registration. For each PER_VDMS job × each active VDMS it ensures a
 * job_instance row and registers a timezone-aware Dapr job named "{jobName}::{vdmsId}".
 * All operations are idempotent. Per-registration failures are isolated (one bad VDMS must
 * not abort the batch), mirroring JobService.registerAll().
 */
@Service
public class PerVdmsRegistrar {

    private static final Logger log = LoggerFactory.getLogger(PerVdmsRegistrar.class);

    private final JobRepository jobs;
    private final VdmsRegistryRepository registry;
    private final JobInstanceRepository instances;
    private final SchedulerClient scheduler;

    @Value("${scheduler.max-instances}") private int maxInstances;

    public PerVdmsRegistrar(JobRepository jobs, VdmsRegistryRepository registry,
                            JobInstanceRepository instances, SchedulerClient scheduler) {
        this.jobs = jobs;
        this.registry = registry;
        this.instances = instances;
        this.scheduler = scheduler;
    }

    void setMaxInstances(int v) { this.maxInstances = v; }

    static String daprName(String jobName, String vdmsId) {
        return jobName + "::" + vdmsId;
    }

    /** Reconcile every PER_VDMS job against every active VDMS. Startup + self-heal path. */
    @Transactional
    public void reconcileAll() {
        List<JobEntity> perVdmsJobs = jobs.findByScope(JobScope.PER_VDMS);
        List<VdmsRegistryEntity> active = registry.findByActiveTrue();
        int registered = 0;
        for (JobEntity job : perVdmsJobs) {
            for (VdmsRegistryEntity vdms : active) {
                if (registered >= maxInstances) {
                    log.warn("max-instances={} reached — skipping remaining per-VDMS registrations", maxInstances);
                    return;
                }
                if (registerInstance(job, vdms.getVdmsId(), vdms.getTimezone())) registered++;
            }
        }
        log.info("Per-VDMS reconcile complete: registered={} (jobs={} activeVdms={})",
                registered, perVdmsJobs.size(), active.size());
    }

    /** A VDMS became active: upsert the registry row, then register all PER_VDMS jobs for it. */
    @Transactional
    public void onVdmsActivated(String vdmsId, String timezone) {
        VdmsRegistryEntity v = registry.findById(vdmsId).orElseGet(
                () -> new VdmsRegistryEntity(vdmsId, timezone, true));
        v.setTimezone(timezone);
        v.setActive(true);
        registry.save(v);
        for (JobEntity job : jobs.findByScope(JobScope.PER_VDMS)) {
            registerInstance(job, vdmsId, timezone);
        }
        log.info("Activated VDMS {} — per-VDMS jobs registered", vdmsId);
    }

    /** A VDMS was deactivated: delete its Dapr jobs, DISABLE its instances, flag registry. */
    @Transactional
    public void onVdmsDeactivated(String vdmsId) {
        for (JobInstanceEntity inst : instances.findByVdmsId(vdmsId)) {
            try {
                scheduler.delete(inst.getDaprJobName());
            } catch (Exception e) {
                log.error("Failed to delete Dapr job {} on deactivate: {}", inst.getDaprJobName(), e.getMessage());
            }
            inst.setState(JobInstanceState.DISABLED);
        }
        registry.findById(vdmsId).ifPresent(v -> { v.setActive(false); registry.save(v); });
        log.info("Deactivated VDMS {} — per-VDMS jobs torn down", vdmsId);
    }

    // Idempotent: upsert the instance row, then (re-)register with Dapr. Isolated per call.
    private boolean registerInstance(JobEntity job, String vdmsId, String timezone) {
        String daprName = daprName(job.getName(), vdmsId);
        try {
            instances.findById(new JobInstanceId(job.getName(), vdmsId)).orElseGet(
                    () -> instances.save(new JobInstanceEntity(job.getName(), vdmsId, daprName)));
            scheduler.schedule(new JobSchedule(daprName, job.getSchedule(), timezone));
            return true;
        } catch (Exception e) {
            log.error("Failed to register per-VDMS job {} vdms={} error={}",
                    job.getName(), vdmsId, e.getMessage());
            return false;
        }
    }
}
