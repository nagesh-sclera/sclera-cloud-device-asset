package io.sclera.scheduler.service;

import io.sclera.scheduler.AbstractPostgresTest;
import io.sclera.scheduler.domain.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class RunRecorderTest extends AbstractPostgresTest {

    @Autowired RunRecorder recorder;
    @Autowired JobRepository jobs;
    @Autowired JobRunRepository runs;

    @Test
    void recordFiredInsertsFiredRunAndStampsLastRunId() {
        jobs.save(new JobEntity("j", "@every 1m", "x", "scheduler.trigger", JobState.ENABLED));
        UUID runId = UUID.randomUUID();

        recorder.recordFired("j", runId, false);

        assertThat(runs.findById(runId)).get().extracting(JobRunEntity::getStatus)
            .isEqualTo(RunStatus.FIRED);
        assertThat(jobs.findById("j")).get().extracting(JobEntity::getLastRunId)
            .isEqualTo(runId);
    }

    @Test
    void recordResultUpdatesStatusDurationError() {
        jobs.save(new JobEntity("j", "@every 1m", "x", "scheduler.trigger", JobState.ENABLED));
        UUID runId = UUID.randomUUID();
        recorder.recordFired("j", runId, false);

        recorder.recordResult(runId, RunStatus.FAILED, 500L, "boom");

        JobRunEntity run = runs.findById(runId).orElseThrow();
        assertThat(run.getStatus()).isEqualTo(RunStatus.FAILED);
        assertThat(run.getDurationMs()).isEqualTo(500L);
        assertThat(run.getError()).isEqualTo("boom");
        assertThat(run.getFinishedAt()).isNotNull();
    }

    @Test
    void recordResultForUnknownRunIsIgnored() {
        recorder.recordResult(UUID.randomUUID(), RunStatus.SUCCESS, 1L, null); // no throw
    }

    @Test
    void recordFiredWithVdmsIdPersistsIt() {
        JobRepository jobsMock = org.mockito.Mockito.mock(JobRepository.class);
        JobRunRepository runsMock = org.mockito.Mockito.mock(JobRunRepository.class);
        java.util.UUID runId = java.util.UUID.randomUUID();
        new RunRecorder(jobsMock, runsMock).recordFired("vdmsSystemHealth", runId, false, "vdms-9");

        org.mockito.ArgumentCaptor<JobRunEntity> cap =
                org.mockito.ArgumentCaptor.forClass(JobRunEntity.class);
        org.mockito.Mockito.verify(runsMock).save(cap.capture());
        org.assertj.core.api.Assertions.assertThat(cap.getValue().getVdmsId()).isEqualTo("vdms-9");
    }
}
