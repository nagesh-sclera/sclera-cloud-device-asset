package io.sclera.scheduler.service;

import io.sclera.dapr.DaprEventPublisher;
import io.sclera.dapr.PublishResult;
import io.sclera.scheduler.client.JobSchedule;
import io.sclera.scheduler.client.SchedulerClient;
import io.sclera.dapr.events.SchedulerTriggerEvent;
import io.sclera.scheduler.domain.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobServiceTest {

    @Mock JobRepository jobs;
    @Mock SchedulerClient scheduler;
    @Mock DaprEventPublisher publisher;
    @Mock RunRecorder recorder;

    JobService service() {
        JobService s = new JobService(jobs, scheduler, publisher, recorder);
        s.setPubsubName("pubsub");
        s.setTriggerTopic("scheduler.trigger");
        return s;
    }

    @Test
    void registerAllSchedulesOnlyEnabledJobs() {
        when(jobs.findByState(JobState.ENABLED)).thenReturn(List.of(
            new JobEntity("a", "@every 1m", "x", "scheduler.trigger", JobState.ENABLED)));

        service().registerAll();

        verify(scheduler).schedule(new JobSchedule("a", "@every 1m"));
        verifyNoMoreInteractions(scheduler);
    }

    @Test
    void pauseDeletesFromSchedulerAndSetsState() {
        JobEntity job = new JobEntity("a", "@every 1m", "x", "scheduler.trigger", JobState.ENABLED);
        when(jobs.findById("a")).thenReturn(Optional.of(job));

        service().pause("a");

        verify(scheduler).delete("a");
        assertThat(job.getState()).isEqualTo(JobState.PAUSED);
    }

    @Test
    void disableDeletesFromSchedulerAndSetsState() {
        JobEntity job = new JobEntity("a", "@every 1m", "x", "scheduler.trigger", JobState.ENABLED);
        when(jobs.findById("a")).thenReturn(Optional.of(job));

        service().disable("a");

        verify(scheduler).delete("a");
        assertThat(job.getState()).isEqualTo(JobState.DISABLED);
    }

    @Test
    void resumeReregistersAndEnables() {
        JobEntity job = new JobEntity("a", "@every 1m", "x", "scheduler.trigger", JobState.PAUSED);
        when(jobs.findById("a")).thenReturn(Optional.of(job));

        service().resume("a");

        verify(scheduler).schedule(new JobSchedule("a", "@every 1m"));
        assertThat(job.getState()).isEqualTo(JobState.ENABLED);
    }

    @Test
    void runNowPublishesTriggerAndRecordsManualRunWithSameRunId() {
        JobEntity job = new JobEntity("a", "@every 1m", "x", "scheduler.trigger", JobState.ENABLED);
        when(jobs.findById("a")).thenReturn(Optional.of(job));
        when(publisher.publish(anyString(), anyString(), any()))
            .thenReturn(new PublishResult(true, "evt", null));

        service().runNow("a");

        // The recorded run and the published trigger MUST share one runId so the
        // result subscriber can correlate them.
        ArgumentCaptor<UUID> recordedRunId = ArgumentCaptor.forClass(UUID.class);
        verify(recorder).recordFired(eq("a"), recordedRunId.capture(), eq(true));
        verify(publisher).publish(eq("pubsub"), eq("scheduler.trigger"), argThat(payload ->
            payload instanceof SchedulerTriggerEvent e
                && e.runId().equals(recordedRunId.getValue().toString())
                && e.jobName().equals("a")));
    }

    @Test
    void unknownJobThrows() {
        when(jobs.findById("nope")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service().pause("nope"))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
