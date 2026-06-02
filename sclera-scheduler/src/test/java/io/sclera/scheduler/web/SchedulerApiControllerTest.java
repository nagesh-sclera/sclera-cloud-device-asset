package io.sclera.scheduler.web;

import io.sclera.scheduler.AbstractPostgresTest;
import io.sclera.scheduler.catalog.CatalogStartupRunner;
import io.sclera.scheduler.client.SchedulerClient;
import io.sclera.scheduler.domain.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class SchedulerApiControllerTest extends AbstractPostgresTest {

    @Autowired WebApplicationContext ctx;
    @Autowired JobRepository jobs;
    @Autowired JobRunRepository runs;

    @MockitoBean SchedulerClient schedulerClient;          // pause delegates here
    @MockitoBean CatalogStartupRunner catalogStartupRunner; // no-op: keep DB clean

    MockMvc mvc() { return MockMvcBuilders.webAppContextSetup(ctx).build(); }

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
    void unknownJobReturns404() throws Exception {
        mvc().perform(post("/api/jobs/nope/pause")).andExpect(status().isNotFound());
    }
}
