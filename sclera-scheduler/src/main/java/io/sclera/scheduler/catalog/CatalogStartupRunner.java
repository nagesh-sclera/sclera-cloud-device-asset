package io.sclera.scheduler.catalog;

import io.sclera.scheduler.domain.VdmsRegistryEntity;
import io.sclera.scheduler.domain.VdmsRegistryRepository;
import io.sclera.scheduler.service.JobService;
import io.sclera.scheduler.service.PerVdmsRegistrar;
import io.sclera.scheduler.service.VdmsActiveDto;
import io.sclera.scheduler.service.VdmsDirectoryClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * On boot: reconcile the catalog, register GLOBAL jobs, then sync the active-VDMS list from
 * vdms-service into vdms_registry and reconcile per-VDMS jobs. The VDMS sync is the self-heal
 * backstop for any lifecycle event missed while the scheduler was down. A vdms-service that is
 * unreachable at boot must not crash the scheduler — sync failures are logged, not fatal
 * (the next lifecycle event or restart reconciles).
 */
@Component
public class CatalogStartupRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(CatalogStartupRunner.class);

    private final JobCatalogProperties catalog;
    private final JobCatalogReconciler reconciler;
    private final JobService jobService;
    private final VdmsDirectoryClient vdmsDirectory;
    private final VdmsRegistryRepository registry;
    private final PerVdmsRegistrar registrar;

    public CatalogStartupRunner(JobCatalogProperties catalog,
                                JobCatalogReconciler reconciler,
                                JobService jobService,
                                VdmsDirectoryClient vdmsDirectory,
                                VdmsRegistryRepository registry,
                                PerVdmsRegistrar registrar) {
        this.catalog = catalog;
        this.reconciler = reconciler;
        this.jobService = jobService;
        this.vdmsDirectory = vdmsDirectory;
        this.registry = registry;
        this.registrar = registrar;
    }

    @Override
    public void run(ApplicationArguments args) {
        reconciler.reconcile(catalog.jobs());
        jobService.registerAll();
        try {
            for (VdmsActiveDto v : vdmsDirectory.fetchActiveVdms()) {
                VdmsRegistryEntity row = registry.findById(v.vdmsId())
                        .orElseGet(() -> new VdmsRegistryEntity(v.vdmsId(), v.timezone(), true));
                row.setTimezone(v.timezone());
                row.setActive(true);
                registry.save(row);
            }
            registrar.reconcileAll();
        } catch (Exception e) {
            log.error("VDMS startup-sync failed (will reconcile on next event/restart): {}", e.getMessage());
        }
    }
}
