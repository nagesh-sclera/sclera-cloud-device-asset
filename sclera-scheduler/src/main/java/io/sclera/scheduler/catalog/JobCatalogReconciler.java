package io.sclera.scheduler.catalog;

import io.sclera.scheduler.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Reconciles the declarative catalog into the job table. Never clobbers runtime state. */
@Service
public class JobCatalogReconciler {

    private static final Logger log = LoggerFactory.getLogger(JobCatalogReconciler.class);
    private final JobRepository jobs;

    public JobCatalogReconciler(JobRepository jobs) {
        this.jobs = jobs;
    }

    @Transactional
    public void reconcile(List<JobCatalogProperties.Entry> entries) {
        for (JobCatalogProperties.Entry e : entries) {
            jobs.findById(e.name()).ifPresentOrElse(existing -> {
                existing.setSchedule(e.schedule());
                existing.setOwner(e.owner());
                existing.setTriggerTopic(e.triggerTopic());
                // state, lastRunId, nextFireAt are runtime-owned — left untouched
            }, () -> {
                jobs.save(new JobEntity(e.name(), e.schedule(), e.owner(),
                        e.triggerTopic(), JobState.ENABLED));
                log.info("Catalog: inserted new job name={}", e.name());
            });
        }
    }
}
