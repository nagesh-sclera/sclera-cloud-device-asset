package io.sclera.scheduler.domain;

import io.sclera.scheduler.AbstractPostgresTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class JobInstanceRepositoryTest extends AbstractPostgresTest {

    @Autowired JobRepository jobs;
    @Autowired VdmsRegistryRepository registry;
    @Autowired JobInstanceRepository instances;

    private void seed() {
        JobEntity j = new JobEntity("vdmsSystemHealth", "0 0 0 * * *", "device-asset",
                "scheduler.trigger", JobState.ENABLED);
        j.setScope(JobScope.PER_VDMS);
        jobs.save(j);
        registry.save(new VdmsRegistryEntity("vdms-1", "UTC", true));
    }

    @Test
    void savesByCompositeKeyAndFindsByJobName() {
        seed();
        instances.save(new JobInstanceEntity("vdmsSystemHealth", "vdms-1",
                "vdmsSystemHealth::vdms-1"));

        assertThat(instances.findByJobName("vdmsSystemHealth"))
                .extracting(JobInstanceEntity::getVdmsId).containsExactly("vdms-1");
        assertThat(instances.findById(new JobInstanceId("vdmsSystemHealth", "vdms-1"))).isPresent();
    }

    @Test
    void findsSnoozedDueForRearm() {
        seed();
        JobInstanceEntity inst = new JobInstanceEntity("vdmsSystemHealth", "vdms-1",
                "vdmsSystemHealth::vdms-1");
        inst.setState(JobInstanceState.SNOOZED);
        inst.setSnoozeUntil(Instant.parse("2020-01-01T00:00:00Z")); // in the past → due
        instances.save(inst);

        assertThat(instances.findByStateAndSnoozeUntilLessThanEqual(
                JobInstanceState.SNOOZED, Instant.now()))
                .hasSize(1);
    }
}
