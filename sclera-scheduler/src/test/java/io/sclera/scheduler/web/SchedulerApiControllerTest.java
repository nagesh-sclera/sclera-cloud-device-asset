package io.sclera.scheduler.web;

import io.sclera.dapr.DaprEventPublisher;
import io.sclera.dapr.PublishResult;
import io.sclera.scheduler.AbstractPostgresTest;
import io.sclera.scheduler.client.SchedulerClient;
import io.sclera.scheduler.domain.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional   // each test rolls back so run/job rows do not bleed across tests
class SchedulerApiControllerTest extends AbstractPostgresTest {

    @Autowired WebApplicationContext ctx;
    @Autowired JobRepository jobs;
    @Autowired JobRunRepository runs;

    @MockitoBean SchedulerClient schedulerClient;          // pause/resume/disable delegate here
    @MockitoBean DaprEventPublisher publisher;              // run-now publishes here
    // CatalogStartupRunner is mocked in AbstractPostgresTest (keeps the job table clean).

    MockMvc mvc() { return MockMvcBuilders.webAppContextSetup(ctx).build(); }

    private JobEntity enabledJob(String name) {
        return new JobEntity(name, "0 0 */3 * * *", "integrations", "scheduler.trigger", JobState.ENABLED);
    }

    @Test
    void listJobsReturnsCatalogWithLatestRun() throws Exception {
        UUID runId = UUID.randomUUID();
        JobEntity job = new JobEntity("snmpSync", "0 0 */3 * * *", "integrations",
                "scheduler.trigger", JobState.ENABLED);
        job.setLastRunId(runId);             // wire the "latest run" pointer
        jobs.save(job);
        JobRunEntity run = new JobRunEntity(runId, "snmpSync", RunStatus.SUCCESS, false,
                Instant.parse("2026-06-02T00:00:00Z"));
        run.setDurationMs(123L);
        runs.save(run);

        mvc().perform(get("/api/jobs"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("snmpSync"))
            .andExpect(jsonPath("$[0].lastStatus").value("SUCCESS"))
            .andExpect(jsonPath("$[0].lastDurationMs").value(123));
    }

    @Test
    void pauseEndpointDelegatesToScheduler() throws Exception {
        jobs.save(new JobEntity("snmpSync", "0 0 */3 * * *", "integrations",
                "scheduler.trigger", JobState.ENABLED));

        mvc().perform(post("/api/jobs/snmpSync/pause")).andExpect(status().isOk());

        verify(schedulerClient).delete("snmpSync");
    }

    @Test
    void resumeEndpointReschedulesAndEnables() throws Exception {
        JobEntity job = new JobEntity("snmpSync", "0 0 */3 * * *", "integrations",
                "scheduler.trigger", JobState.PAUSED);
        jobs.save(job);

        mvc().perform(post("/api/jobs/snmpSync/resume")).andExpect(status().isOk());

        verify(schedulerClient).schedule(any());
        org.assertj.core.api.Assertions.assertThat(jobs.findById("snmpSync"))
            .get().extracting(JobEntity::getState).isEqualTo(JobState.ENABLED);
    }

    @Test
    void disableEndpointDeletesAndDisables() throws Exception {
        jobs.save(enabledJob("snmpSync"));

        mvc().perform(post("/api/jobs/snmpSync/disable")).andExpect(status().isOk());

        verify(schedulerClient).delete("snmpSync");
        org.assertj.core.api.Assertions.assertThat(jobs.findById("snmpSync"))
            .get().extracting(JobEntity::getState).isEqualTo(JobState.DISABLED);
    }

    @Test
    void runEndpointPublishesTriggerAndRecordsManualRun() throws Exception {
        when(publisher.publish(anyString(), anyString(), any()))
            .thenReturn(new PublishResult(true, "evt", null));
        jobs.save(enabledJob("snmpSync"));

        mvc().perform(post("/api/jobs/snmpSync/run")).andExpect(status().isOk());

        verify(publisher).publish(anyString(), anyString(), any());
        org.assertj.core.api.Assertions.assertThat(runs.findByJobNameOrderByFiredAtDesc(
                "snmpSync", org.springframework.data.domain.Limit.of(10)))
            .anySatisfy(r -> org.assertj.core.api.Assertions.assertThat(r.isManual()).isTrue());
    }

    @Test
    void historyEndpointReturnsRunsNewestFirst() throws Exception {
        jobs.save(enabledJob("snmpSync"));
        JobRunEntity older = new JobRunEntity(UUID.randomUUID(), "snmpSync", RunStatus.SUCCESS, false,
                Instant.parse("2026-06-01T00:00:00Z"));
        older.setFinishedAt(Instant.parse("2026-06-01T00:00:01Z"));
        older.setDurationMs(1000L);
        JobRunEntity newer = new JobRunEntity(UUID.randomUUID(), "snmpSync", RunStatus.FIRED, true,
                Instant.parse("2026-06-02T00:00:00Z")); // finishedAt null -> must serialize as null
        runs.save(older);
        runs.save(newer);

        mvc().perform(get("/api/jobs/snmpSync/runs"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].status").value("FIRED"))
            .andExpect(jsonPath("$[0].manual").value(true))
            .andExpect(jsonPath("$[0].finishedAt").doesNotExist())
            .andExpect(jsonPath("$[1].status").value("SUCCESS"))
            .andExpect(jsonPath("$[1].durationMs").value(1000));
    }

    @Test
    void historyRejectsNonPositiveLimit() throws Exception {
        mvc().perform(get("/api/jobs/snmpSync/runs").param("limit", "0"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void unknownJobReturns404() throws Exception {
        mvc().perform(post("/api/jobs/nope/pause")).andExpect(status().isNotFound());
    }
}
