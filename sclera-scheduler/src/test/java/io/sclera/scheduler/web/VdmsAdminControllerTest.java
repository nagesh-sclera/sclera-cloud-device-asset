package io.sclera.scheduler.web;

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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
class VdmsAdminControllerTest extends AbstractPostgresTest {

    @Autowired WebApplicationContext ctx;
    @Autowired VdmsRegistryRepository registry;
    @Autowired JobInstanceRepository instances;
    @Autowired JobRepository jobs;
    @MockitoBean SchedulerClient schedulerClient;

    MockMvc mvc() { return MockMvcBuilders.webAppContextSetup(ctx).build(); }

    @Test
    void listReturnsRegistryRowsWithJobCount() throws Exception {
        jobs.save(new JobEntity("vdmsSystemHealth", "0 0 0 * * *", "device-asset",
                "scheduler.trigger", JobState.ENABLED));
        registry.save(new VdmsRegistryEntity("vdms-1", "UTC", true));
        instances.save(new JobInstanceEntity("vdmsSystemHealth", "vdms-1", "vdmsSystemHealth::vdms-1"));

        mvc().perform(get("/api/vdms"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].vdmsId").value("vdms-1"))
            .andExpect(jsonPath("$[0].timezone").value("UTC"))
            .andExpect(jsonPath("$[0].active").value(true))
            .andExpect(jsonPath("$[0].jobCount").value(1));
    }

    @Test
    void timezonesReturnsNonEmptySortedList() throws Exception {
        mvc().perform(get("/api/vdms/timezones"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0]").exists())
            .andExpect(jsonPath("$", org.hamcrest.Matchers.hasItem("UTC")));
    }
}
