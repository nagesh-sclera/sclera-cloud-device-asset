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

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.startsWith;

@ExtendWith(MockitoExtension.class)
class JobServiceTest {

    @Mock JobRepository jobs;
    @Mock JobInstanceRepository instances;
    @Mock VdmsRegistryRepository registry;
    @Mock SchedulerClient scheduler;
    @Mock DaprEventPublisher publisher;
    @Mock RunRecorder recorder;

    JobService service() {
        JobService s = new JobService(jobs, instances, registry, scheduler, publisher, recorder);
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

    private JobInstanceEntity inst() {
        return new JobInstanceEntity("vdmsSystemHealth", "vdms-1", "vdmsSystemHealth::vdms-1");
    }
    private void stubInstanceLookup(JobInstanceEntity inst) {
        when(instances.findById(new JobInstanceId("vdmsSystemHealth", "vdms-1")))
                .thenReturn(Optional.of(inst));
        when(jobs.findById("vdmsSystemHealth")).thenReturn(Optional.of(
                new JobEntity("vdmsSystemHealth", "0 0 0 * * *", "device-asset",
                        "scheduler.trigger", JobState.ENABLED)));
        when(registry.findById("vdms-1")).thenReturn(Optional.of(
                new VdmsRegistryEntity("vdms-1", "UTC", true)));
    }

    @Test
    void pauseInstanceDeletesDaprAndSetsPaused() {
        JobInstanceEntity i = inst();
        when(instances.findById(new JobInstanceId("vdmsSystemHealth", "vdms-1")))
                .thenReturn(Optional.of(i));
        service().pauseInstance("vdmsSystemHealth", "vdms-1");
        verify(scheduler).delete("vdmsSystemHealth::vdms-1");
        assertThat(i.getState()).isEqualTo(JobInstanceState.PAUSED);
    }

    @Test
    void snoozeInstanceDeletesDaprSetsSnoozedAndStoresUntil() {
        JobInstanceEntity i = inst();
        when(instances.findById(new JobInstanceId("vdmsSystemHealth", "vdms-1")))
                .thenReturn(Optional.of(i));
        Instant until = Instant.parse("2026-06-10T00:00:00Z");
        service().snoozeInstance("vdmsSystemHealth", "vdms-1", until);
        verify(scheduler).delete("vdmsSystemHealth::vdms-1");
        assertThat(i.getState()).isEqualTo(JobInstanceState.SNOOZED);
        assertThat(i.getSnoozeUntil()).isEqualTo(until);
    }

    @Test
    void resumeInstanceReregistersTimezoneAwareAndEnables() {
        JobInstanceEntity i = inst();
        i.setState(JobInstanceState.SNOOZED);
        i.setSnoozeUntil(Instant.parse("2026-06-10T00:00:00Z"));
        stubInstanceLookup(i);
        service().resumeInstance("vdmsSystemHealth", "vdms-1");
        verify(scheduler).schedule(new JobSchedule("vdmsSystemHealth::vdms-1", "0 0 0 * * *", "UTC"));
        assertThat(i.getState()).isEqualTo(JobInstanceState.ENABLED);
        assertThat(i.getSnoozeUntil()).isNull();
    }

    @Test
    void runAtInstanceRegistersOneShot() {
        JobInstanceEntity i = inst();
        when(instances.findById(new JobInstanceId("vdmsSystemHealth", "vdms-1")))
                .thenReturn(Optional.of(i));
        Instant at = Instant.now().plusSeconds(3600);
        service().runAtInstance("vdmsSystemHealth", "vdms-1", at);
        verify(scheduler).scheduleOnce(startsWith("vdmsSystemHealth::vdms-1::once-"), eq(at));
    }

    @Test
    void runAtInPastIsRejected() {
        JobInstanceEntity i = inst();
        lenient().when(instances.findById(new JobInstanceId("vdmsSystemHealth", "vdms-1")))
                .thenReturn(Optional.of(i));
        assertThatThrownBy(() -> service().runAtInstance(
                "vdmsSystemHealth", "vdms-1", Instant.parse("2000-01-01T00:00:00Z")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void unknownInstanceThrows() {
        when(instances.findById(new JobInstanceId("vdmsSystemHealth", "nope")))
                .thenReturn(Optional.empty());
        assertThatThrownBy(() -> service().pauseInstance("vdmsSystemHealth", "nope"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
