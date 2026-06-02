package io.sclera.scheduler.catalog;

import io.sclera.scheduler.service.JobService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/** On boot: reconcile the catalog into the DB, then register enabled jobs with Dapr. */
@Component
public class CatalogStartupRunner implements ApplicationRunner {

    private final JobCatalogProperties catalog;
    private final JobCatalogReconciler reconciler;
    private final JobService jobService;

    public CatalogStartupRunner(JobCatalogProperties catalog,
                                JobCatalogReconciler reconciler,
                                JobService jobService) {
        this.catalog = catalog;
        this.reconciler = reconciler;
        this.jobService = jobService;
    }

    @Override
    public void run(ApplicationArguments args) {
        reconciler.reconcile(catalog.jobs());
        jobService.registerAll();
    }
}
