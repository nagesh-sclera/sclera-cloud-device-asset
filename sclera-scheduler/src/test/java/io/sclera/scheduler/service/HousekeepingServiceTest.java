package io.sclera.scheduler.service;

import io.sclera.scheduler.AbstractPostgresTest;
import io.sclera.scheduler.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
    "scheduler.orphan-timeout-seconds=900",
    "scheduler.history-retention-days=30"
})
class HousekeepingServiceTest extends AbstractPostgresTest {

    @Autowired HousekeepingService housekeeping;
    @Autowired JobRepository jobs;
    @Autowired JobRunRepository runs;

    @BeforeEach
    void clean() { runs.deleteAll(); jobs.deleteAll(); }

    @Test
    void sweepsStuckFiredRunsToFailed() {
        jobs.save(new JobEntity("j", "@every 1m", "x", "scheduler.trigger", JobState.ENABLED));
        UUID old = UUID.randomUUID();
        JobRunEntity stuck = new JobRunEntity(old, "j", RunStatus.FIRED, false,
                Instant.now().minus(2, ChronoUnit.HOURS));
        runs.save(stuck);

        housekeeping.reapOrphans();

        assertThat(runs.findById(old)).get().extracting(JobRunEntity::getStatus)
            .isEqualTo(RunStatus.FAILED);
        assertThat(runs.findById(old)).get().extracting(JobRunEntity::getError)
            .isEqualTo("timed out: no result received");
    }

    @Test
    void prunesOldHistory() {
        jobs.save(new JobEntity("j", "@every 1m", "x", "scheduler.trigger", JobState.ENABLED));
        runs.save(new JobRunEntity(UUID.randomUUID(), "j", RunStatus.SUCCESS, false,
                Instant.now().minus(40, ChronoUnit.DAYS)));
        runs.save(new JobRunEntity(UUID.randomUUID(), "j", RunStatus.SUCCESS, false,
                Instant.now().minus(1, ChronoUnit.DAYS)));

        housekeeping.pruneHistory();

        assertThat(runs.count()).isEqualTo(1);
    }
}
