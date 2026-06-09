package io.sclera.scheduler.catalog;

import io.sclera.scheduler.AbstractPostgresTest;
import io.sclera.scheduler.domain.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class JobCatalogReconcilerTest extends AbstractPostgresTest {

    @Autowired JobCatalogReconciler reconciler;
    @Autowired JobRepository jobs;

    @Test
    void insertsNewCatalogEntriesButPreservesRuntimeState() {
        jobs.save(new JobEntity("snmpSync", "0 0 */3 * * *", "integrations",
                "scheduler.trigger", JobState.PAUSED));

        reconciler.reconcile(List.of(
            new JobCatalogProperties.Entry("snmpSync", "0 0 */3 * * *", "integrations", "scheduler.trigger", "GLOBAL"),
            new JobCatalogProperties.Entry("modbusSync", "0 */5 * * * *", "integrations", "scheduler.trigger", "GLOBAL")
        ));

        assertThat(jobs.findById("modbusSync")).get()
            .extracting(JobEntity::getState).isEqualTo(JobState.ENABLED);
        assertThat(jobs.findById("snmpSync")).get()
            .extracting(JobEntity::getState).isEqualTo(JobState.PAUSED);
    }

    @Test
    void updatesScheduleWhenCatalogChanges() {
        jobs.save(new JobEntity("modbusSync", "0 */5 * * * *", "integrations",
                "scheduler.trigger", JobState.ENABLED));

        reconciler.reconcile(List.of(
            new JobCatalogProperties.Entry("modbusSync", "0 */10 * * * *", "integrations", "scheduler.trigger", "GLOBAL")
        ));

        assertThat(jobs.findById("modbusSync")).get()
            .extracting(JobEntity::getSchedule).isEqualTo("0 */10 * * * *");
    }

    @Test
    void reconcileWritesScopeFromCatalog() {
        var entry = new JobCatalogProperties.Entry(
                "vdmsSystemHealth", "0 0 0 * * *", "device-asset", "scheduler.trigger", "PER_VDMS");
        reconciler.reconcile(java.util.List.of(entry));

        JobEntity saved = jobs.findById("vdmsSystemHealth").orElseThrow();
        assertThat(saved.getScope()).isEqualTo(JobScope.PER_VDMS);
    }
}
