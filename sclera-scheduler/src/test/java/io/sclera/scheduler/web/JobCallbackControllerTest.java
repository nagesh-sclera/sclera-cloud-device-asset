package io.sclera.scheduler.web;

import io.sclera.dapr.DaprEventPublisher;
import io.sclera.dapr.PublishResult;
import io.sclera.dapr.events.SchedulerTriggerEvent;
import io.sclera.scheduler.domain.JobEntity;
import io.sclera.scheduler.domain.JobRepository;
import io.sclera.scheduler.domain.JobState;
import io.sclera.scheduler.service.RunRecorder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobCallbackControllerTest {

    @Mock RunRecorder recorder;
    @Mock DaprEventPublisher publisher;
    @Mock JobRepository jobs;

    @BeforeEach
    void defaultOwnerLookup() {
        // Callback resolves owner via jobs.findById; default to "unknown job" so tests
        // that don't care about owner don't NPE. Owner-specific tests override below.
        lenient().when(jobs.findById(anyString())).thenReturn(Optional.empty());
    }

    private void stubOwner(String jobName, String owner) {
        when(jobs.findById(jobName)).thenReturn(Optional.of(
            new JobEntity(jobName, "0 0 */3 * * *", owner, "scheduler.trigger", JobState.ENABLED)));
    }

    JobCallbackController controller() {
        JobCallbackController c = new JobCallbackController(recorder, publisher, jobs);
        c.setPubsubName("pubsub");
        c.setTriggerTopic("scheduler.trigger");
        return c;
    }

    @Test
    void onFireRecordsRunAndPublishesTriggerWithSameRunIdAndOwner() {
        when(publisher.publish(anyString(), anyString(), any()))
            .thenReturn(new PublishResult(true, "evt", null));
        stubOwner("snmpSync", "integrations");

        ResponseEntity<Void> response = controller().onJobFired("snmpSync");

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        // The recorded run and the published trigger MUST share one runId so the
        // result subscriber can correlate them; the trigger carries the catalog owner.
        ArgumentCaptor<UUID> recordedRunId = ArgumentCaptor.forClass(UUID.class);
        verify(recorder).recordFired(eq("snmpSync"), recordedRunId.capture(), eq(false), isNull());
        verify(publisher).publish(eq("pubsub"), eq("scheduler.trigger"), argThat(payload ->
            payload instanceof SchedulerTriggerEvent e
                && e.jobName().equals("snmpSync")
                && e.runId().equals(recordedRunId.getValue().toString())
                && "integrations".equals(e.owner())));
    }

    @Test
    void onFireReturns500WhenPublishFails() {
        when(publisher.publish(anyString(), anyString(), any()))
            .thenReturn(new PublishResult(false, null, "broker-unavailable"));

        ResponseEntity<Void> response = controller().onJobFired("snmpSync");

        // 500 signals the Dapr Scheduler to retry the fire.
        assertThat(response.getStatusCode().value()).isEqualTo(500);
        // The run was still recorded as FIRED (reaper reclaims it if the retry mints a new one).
        verify(recorder).recordFired(eq("snmpSync"), any(), eq(false), isNull());
    }

    @Test
    void perVdmsFireRecordsAndPublishesVdmsId() {
        when(publisher.publish(any(), any(), any())).thenReturn(new PublishResult(true, "e", null));

        controller().onJobFired("vdmsSystemHealth::vdms-7");

        verify(recorder).recordFired(eq("vdmsSystemHealth"), any(), eq(false), eq("vdms-7"));
        verify(publisher).publish(eq("pubsub"), eq("scheduler.trigger"), argThat(p ->
                p instanceof SchedulerTriggerEvent e
                        && e.jobName().equals("vdmsSystemHealth")
                        && "vdms-7".equals(e.vdmsId())));
    }

    @Test
    void oneShotFireIsRecordedAsManual() {
        when(publisher.publish(any(), any(), any())).thenReturn(new PublishResult(true, "e", null));

        controller().onJobFired("vdmsSystemHealth::vdms-7::once-abc");

        verify(recorder).recordFired(eq("vdmsSystemHealth"), any(), eq(true), eq("vdms-7"));
    }

    @Test
    void globalFireHasNullVdmsId() {
        when(publisher.publish(any(), any(), any())).thenReturn(new PublishResult(true, "e", null));

        controller().onJobFired("snmpSync");

        verify(recorder).recordFired(eq("snmpSync"), any(), eq(false), isNull());
    }

    @Test
    void globalOneShotFireParsesJobNameOwnerAndNullVdms() {
        when(publisher.publish(any(), any(), any())).thenReturn(new PublishResult(true, "e", null));
        stubOwner("workorderTicketSync", "workorder");

        // {job}::once-{id} is a GLOBAL one-shot — jobName=workorderTicketSync, vdmsId=null, manual=true.
        controller().onJobFired("workorderTicketSync::once-abc");

        verify(recorder).recordFired(eq("workorderTicketSync"), any(), eq(true), isNull());
        verify(publisher).publish(eq("pubsub"), eq("scheduler.trigger"), argThat(p ->
            p instanceof SchedulerTriggerEvent e
                && e.jobName().equals("workorderTicketSync")
                && e.vdmsId() == null
                && "workorder".equals(e.owner())));
    }
}
