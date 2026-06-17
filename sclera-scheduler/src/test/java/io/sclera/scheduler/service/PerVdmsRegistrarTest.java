package io.sclera.scheduler.service;

import io.sclera.scheduler.client.JobSchedule;
import io.sclera.scheduler.client.SchedulerClient;
import io.sclera.scheduler.domain.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PerVdmsRegistrarTest {

    @Mock JobRepository jobs;
    @Mock VdmsRegistryRepository registry;
    @Mock JobInstanceRepository instances;
    @Mock SchedulerClient scheduler;

    PerVdmsRegistrar registrar() {
        PerVdmsRegistrar r = new PerVdmsRegistrar(jobs, registry, instances, scheduler);
        r.setMaxInstances(5000);
        return r;
    }

    private JobEntity perVdmsJob() {
        JobEntity j = new JobEntity("vdmsSystemHealth", "0 0 0 * * *", "device-asset",
                "scheduler.trigger", JobState.ENABLED);
        j.setScope(JobScope.PER_VDMS);
        return j;
    }

    @Test
    void reconcileAllRegistersEachPerVdmsJobForEachActiveVdms() {
        when(jobs.findByScope(JobScope.PER_VDMS)).thenReturn(List.of(perVdmsJob()));
        when(registry.findByActiveTrue()).thenReturn(List.of(
                new VdmsRegistryEntity("vdms-1", "UTC", true),
                new VdmsRegistryEntity("vdms-2", "America/New_York", true)));
        when(instances.findById(any())).thenReturn(Optional.empty());

        registrar().reconcileAll();

        verify(scheduler).schedule(new JobSchedule("vdmsSystemHealth::vdms-1", "0 0 0 * * *", "UTC"));
        verify(scheduler).schedule(new JobSchedule("vdmsSystemHealth::vdms-2", "0 0 0 * * *", "America/New_York"));
        verify(instances, times(2)).save(any(JobInstanceEntity.class));
    }

    @Test
    void onVdmsActivatedUpsertsRegistryAndRegistersAllPerVdmsJobs() {
        when(registry.findById("vdms-3")).thenReturn(Optional.empty());
        when(jobs.findByScope(JobScope.PER_VDMS)).thenReturn(List.of(perVdmsJob()));
        when(instances.findById(any())).thenReturn(Optional.empty());

        registrar().onVdmsActivated("vdms-3", "Europe/London");

        verify(registry).save(argThat(v -> v.getVdmsId().equals("vdms-3") && v.isActive()));
        verify(scheduler).schedule(new JobSchedule("vdmsSystemHealth::vdms-3", "0 0 0 * * *", "Europe/London"));
    }

    @Test
    void onVdmsDeactivatedDeletesDaprJobsAndDisablesInstances() {
        JobInstanceEntity inst = new JobInstanceEntity("vdmsSystemHealth", "vdms-1", "vdmsSystemHealth::vdms-1");
        when(instances.findByVdmsId("vdms-1")).thenReturn(List.of(inst));
        when(registry.findById("vdms-1")).thenReturn(
                Optional.of(new VdmsRegistryEntity("vdms-1", "UTC", true)));

        registrar().onVdmsDeactivated("vdms-1");

        verify(scheduler).delete("vdmsSystemHealth::vdms-1");
        org.assertj.core.api.Assertions.assertThat(inst.getState()).isEqualTo(JobInstanceState.DISABLED);
        verify(registry).save(argThat(v -> !v.isActive()));
    }

    @Test
    void reconcileAllStopsAtMaxInstancesGuardrail() {
        PerVdmsRegistrar r = new PerVdmsRegistrar(jobs, registry, instances, scheduler);
        r.setMaxInstances(1);
        when(jobs.findByScope(JobScope.PER_VDMS)).thenReturn(List.of(perVdmsJob()));
        when(registry.findByActiveTrue()).thenReturn(List.of(
                new VdmsRegistryEntity("vdms-1", "UTC", true),
                new VdmsRegistryEntity("vdms-2", "UTC", true)));
        when(instances.findById(any())).thenReturn(Optional.empty());

        r.reconcileAll();

        // Only the first instance registered; guardrail skipped the rest.
        verify(scheduler, times(1)).schedule(any(JobSchedule.class));
    }

    @Test
    void reregisterUpdatesTimezoneAndOnlyReschedulesEnabledInstances() {
        JobEntity job = new JobEntity("vdmsSystemHealth", "0 0 0 * * *", "device-asset",
                "scheduler.trigger", JobState.ENABLED);
        job.setScope(JobScope.PER_VDMS);
        when(jobs.findById("vdmsSystemHealth")).thenReturn(Optional.of(job));

        VdmsRegistryEntity vdmsReg = new VdmsRegistryEntity("vdms-1", "UTC", true);
        when(registry.findById("vdms-1")).thenReturn(Optional.of(vdmsReg));

        JobInstanceEntity enabled = new JobInstanceEntity("vdmsSystemHealth", "vdms-1", "vdmsSystemHealth::vdms-1");
        // default state is ENABLED
        JobInstanceEntity paused = new JobInstanceEntity("vdmsSystemHealth", "vdms-1", "vdmsSystemHealth::vdms-1-x");
        paused.setState(JobInstanceState.PAUSED);
        when(instances.findByVdmsId("vdms-1")).thenReturn(List.of(enabled, paused));

        registrar().reregister("vdms-1", "America/New_York");

        assertEquals("America/New_York", vdmsReg.getTimezone());
        org.mockito.ArgumentCaptor<JobSchedule> cap = org.mockito.ArgumentCaptor.forClass(JobSchedule.class);
        verify(scheduler).schedule(cap.capture());   // ONLY the ENABLED instance → exactly one schedule() call
        assertEquals("vdmsSystemHealth::vdms-1", cap.getValue().name());
        assertEquals("America/New_York", cap.getValue().timezone());
    }
}
