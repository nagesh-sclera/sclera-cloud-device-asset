package io.sclera.scheduler.domain;

import io.sclera.scheduler.AbstractPostgresTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Limit;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class JobRepositoryTest extends AbstractPostgresTest {

    @Autowired JobRepository jobs;
    @Autowired JobRunRepository runs;

    @Test
    void savesJobAndFindsByState() {
        jobs.save(new JobEntity("snmpSync", "0 0 */3 * * *", "integrations",
                "scheduler.trigger", JobState.ENABLED));
        jobs.save(new JobEntity("paused", "@every 1m", "device-asset",
                "scheduler.trigger", JobState.PAUSED));

        List<JobEntity> enabled = jobs.findByState(JobState.ENABLED);
        assertThat(enabled).extracting(JobEntity::getName).containsExactly("snmpSync");
    }

    @Test
    void runHistoryReturnsNewestFirst() {
        jobs.save(new JobEntity("j", "@every 1m", "x", "scheduler.trigger", JobState.ENABLED));
        runs.save(new JobRunEntity(UUID.randomUUID(), "j", RunStatus.SUCCESS, false,
                Instant.parse("2026-06-01T00:00:00Z")));
        runs.save(new JobRunEntity(UUID.randomUUID(), "j", RunStatus.FIRED, false,
                Instant.parse("2026-06-02T00:00:00Z")));

        List<JobRunEntity> history = runs.findByJobNameOrderByFiredAtDesc("j", Limit.of(50));
        assertThat(history).hasSize(2);
        assertThat(history.get(0).getStatus()).isEqualTo(RunStatus.FIRED);
    }

    @Test
    void findsJobsByScope() {
        JobEntity perVdms = new JobEntity("vdmsSystemHealth", "0 0 0 * * *", "device-asset",
                "scheduler.trigger", JobState.ENABLED);
        perVdms.setScope(JobScope.PER_VDMS);
        jobs.save(perVdms);
        jobs.save(new JobEntity("snmpSync", "0 0 */3 * * *", "integrations",
                "scheduler.trigger", JobState.ENABLED)); // defaults to GLOBAL

        assertThat(jobs.findByScope(JobScope.PER_VDMS))
                .extracting(JobEntity::getName).containsExactly("vdmsSystemHealth");
        assertThat(jobs.findByScope(JobScope.GLOBAL))
                .extracting(JobEntity::getName).containsExactly("snmpSync");
    }
}
