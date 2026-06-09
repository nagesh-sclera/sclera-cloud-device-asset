package io.sclera.scheduler.catalog;

import io.sclera.scheduler.service.JobService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CatalogStartupRunnerTest {

    @Mock JobCatalogReconciler reconciler;
    @Mock JobService jobService;

    @Test
    void reconcilesThenRegistersInOrder() throws Exception {
        var props = new JobCatalogProperties(List.of(
            new JobCatalogProperties.Entry("a", "@every 1m", "x", "scheduler.trigger", "GLOBAL")));
        var runner = new CatalogStartupRunner(props, reconciler, jobService);

        runner.run(null);

        InOrder order = inOrder(reconciler, jobService);
        order.verify(reconciler).reconcile(props.jobs());
        order.verify(jobService).registerAll();
    }
}
