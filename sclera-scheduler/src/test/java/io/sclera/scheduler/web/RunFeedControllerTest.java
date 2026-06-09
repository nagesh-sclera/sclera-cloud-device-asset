package io.sclera.scheduler.web;

import io.sclera.scheduler.AbstractPostgresTest;
import io.sclera.scheduler.domain.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.Instant;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
class RunFeedControllerTest extends AbstractPostgresTest {

    @Autowired WebApplicationContext ctx;
    @Autowired JobRepository jobs;
    @Autowired JobRunRepository runs;

    @Test
    void feedExposesVdmsId() throws Exception {
        jobs.save(new JobEntity("vdmsSystemHealth", "0 0 0 * * *", "device-asset",
                "scheduler.trigger", JobState.ENABLED));
        runs.save(new JobRunEntity(UUID.randomUUID(), "vdmsSystemHealth", RunStatus.SUCCESS,
                false, Instant.parse("2026-06-09T00:00:00Z"), "vdms-7"));

        MockMvc mvc = MockMvcBuilders.webAppContextSetup(ctx).build();
        mvc.perform(get("/api/runs"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].jobName").value("vdmsSystemHealth"))
            .andExpect(jsonPath("$[0].vdmsId").value("vdms-7"));
    }
}
