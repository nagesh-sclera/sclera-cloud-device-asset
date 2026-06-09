package io.sclera.scheduler.catalog;

import io.sclera.scheduler.domain.VdmsRegistryRepository;
import io.sclera.scheduler.service.JobService;
import io.sclera.scheduler.service.PerVdmsRegistrar;
import io.sclera.scheduler.service.VdmsActiveDto;
import io.sclera.scheduler.service.VdmsDirectoryClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CatalogStartupRunnerTest {

    @Mock JobCatalogReconciler reconciler;
    @Mock JobService jobService;
    @Mock VdmsDirectoryClient vdmsDirectory;
    @Mock VdmsRegistryRepository registry;
    @Mock PerVdmsRegistrar registrar;

    @Test
    void reconcilesThenRegistersInOrder() throws Exception {
        var props = new JobCatalogProperties(List.of(
            new JobCatalogProperties.Entry("a", "@every 1m", "x", "scheduler.trigger", "GLOBAL")));
        var runner = new CatalogStartupRunner(props, reconciler, jobService, vdmsDirectory, registry, registrar);

        when(vdmsDirectory.fetchActiveVdms()).thenReturn(List.of());

        runner.run(null);

        InOrder order = inOrder(reconciler, jobService);
        order.verify(reconciler).reconcile(props.jobs());
        order.verify(jobService).registerAll();
    }

    @Test
    void runReconcilesThenRegistersGlobalThenSyncsVdmsThenReconcilesPerVdms() {
        var props = new JobCatalogProperties(List.of());
        var runner = new CatalogStartupRunner(props, reconciler, jobService, vdmsDirectory, registry, registrar);

        when(vdmsDirectory.fetchActiveVdms()).thenReturn(List.of(
                new VdmsActiveDto("vdms-1", "UTC")));
        when(registry.findById("vdms-1")).thenReturn(Optional.empty());

        runner.run(null);

        InOrder order = inOrder(reconciler, jobService, vdmsDirectory, registry, registrar);
        order.verify(reconciler).reconcile(any());
        order.verify(jobService).registerAll();
        order.verify(vdmsDirectory).fetchActiveVdms();
        order.verify(registry).save(argThat(v -> v.getVdmsId().equals("vdms-1") && v.isActive()));
        order.verify(registrar).reconcileAll();
    }
}
