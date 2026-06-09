package io.sclera.scheduler.service;

import io.sclera.scheduler.client.JobSchedule;
import io.sclera.scheduler.client.SchedulerClient;
import io.sclera.scheduler.domain.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SnoozeReconcilerTest {

    @Mock JobInstanceRepository instances;
    @Mock JobRepository jobs;
    @Mock VdmsRegistryRepository registry;
    @Mock SchedulerClient scheduler;

    @Test
    void rearmsDueSnoozesTimezoneAwareAndEnables() {
        JobInstanceEntity inst = new JobInstanceEntity("vdmsSystemHealth", "vdms-1", "vdmsSystemHealth::vdms-1");
        inst.setState(JobInstanceState.SNOOZED);
        inst.setSnoozeUntil(Instant.parse("2020-01-01T00:00:00Z"));
        when(instances.findByStateAndSnoozeUntilLessThanEqual(eq(JobInstanceState.SNOOZED), any()))
                .thenReturn(List.of(inst));
        when(jobs.findById("vdmsSystemHealth")).thenReturn(Optional.of(
                new JobEntity("vdmsSystemHealth", "0 0 0 * * *", "device-asset",
                        "scheduler.trigger", JobState.ENABLED)));
        when(registry.findById("vdms-1")).thenReturn(Optional.of(
                new VdmsRegistryEntity("vdms-1", "America/New_York", true)));

        new SnoozeReconciler(instances, jobs, registry, scheduler).rearmExpiredSnoozes();

        verify(scheduler).schedule(new JobSchedule("vdmsSystemHealth::vdms-1", "0 0 0 * * *", "America/New_York"));
        assertThat(inst.getState()).isEqualTo(JobInstanceState.ENABLED);
        assertThat(inst.getSnoozeUntil()).isNull();
    }

    @Test
    void oneBadInstanceDoesNotAbortTheBatch() {
        JobInstanceEntity bad = new JobInstanceEntity("vdmsSystemHealth", "vdms-1", "vdmsSystemHealth::vdms-1");
        bad.setState(JobInstanceState.SNOOZED);
        JobInstanceEntity good = new JobInstanceEntity("vdmsSystemHealth", "vdms-2", "vdmsSystemHealth::vdms-2");
        good.setState(JobInstanceState.SNOOZED);
        when(instances.findByStateAndSnoozeUntilLessThanEqual(eq(JobInstanceState.SNOOZED), any()))
                .thenReturn(List.of(bad, good));
        when(jobs.findById("vdmsSystemHealth")).thenReturn(Optional.of(
                new JobEntity("vdmsSystemHealth", "0 0 0 * * *", "device-asset",
                        "scheduler.trigger", JobState.ENABLED)));
        when(registry.findById("vdms-1")).thenReturn(Optional.empty()); // bad: null tz → still works
        when(registry.findById("vdms-2")).thenReturn(Optional.of(
                new VdmsRegistryEntity("vdms-2", "UTC", true)));
        doThrow(new RuntimeException("dapr down")).when(scheduler)
                .schedule(new JobSchedule("vdmsSystemHealth::vdms-1", "0 0 0 * * *", null));

        new SnoozeReconciler(instances, jobs, registry, scheduler).rearmExpiredSnoozes();

        verify(scheduler).schedule(new JobSchedule("vdmsSystemHealth::vdms-2", "0 0 0 * * *", "UTC"));
        assertThat(good.getState()).isEqualTo(JobInstanceState.ENABLED);
    }
}
