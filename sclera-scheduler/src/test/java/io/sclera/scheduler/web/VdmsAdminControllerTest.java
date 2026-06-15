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

import static org.junit.jupiter.api.Assertions.*;
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

    private void seedPerVdmsJob() {
        var j = new JobEntity("vdmsSystemHealth", "0 0 0 * * *", "device-asset",
                "scheduler.trigger", JobState.ENABLED);
        j.setScope(JobScope.PER_VDMS);
        jobs.save(j);
    }

    @Test
    void addCreatesRegistryRowAndRegistersInstances() throws Exception {
        seedPerVdmsJob();
        mvc().perform(post("/api/vdms").contentType("application/json")
                .content("{\"vdmsId\":\"vdms-1\",\"timezone\":\"America/New_York\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.vdmsId").value("vdms-1"))
            .andExpect(jsonPath("$.timezone").value("America/New_York"))
            .andExpect(jsonPath("$.active").value(true));
        assertTrue(registry.findById("vdms-1").isPresent());
        assertEquals(1, instances.countByVdmsId("vdms-1"));
    }

    @Test
    void addRejectsDuplicateActiveVdms() throws Exception {
        registry.save(new VdmsRegistryEntity("vdms-1", "UTC", true));
        mvc().perform(post("/api/vdms").contentType("application/json")
                .content("{\"vdmsId\":\"vdms-1\",\"timezone\":\"UTC\"}"))
            .andExpect(status().isConflict());
    }

    @Test
    void addRejectsInvalidTimezone() throws Exception {
        mvc().perform(post("/api/vdms").contentType("application/json")
                .content("{\"vdmsId\":\"vdms-9\",\"timezone\":\"Not/AZone\"}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void addRejectsBlankId() throws Exception {
        mvc().perform(post("/api/vdms").contentType("application/json")
                .content("{\"vdmsId\":\"  \",\"timezone\":\"UTC\"}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void editTimezoneUpdatesRegistryAndReregisters() throws Exception {
        registry.save(new VdmsRegistryEntity("vdms-1", "UTC", true));
        seedPerVdmsJob();
        instances.save(new JobInstanceEntity("vdmsSystemHealth", "vdms-1", "vdmsSystemHealth::vdms-1"));

        mvc().perform(put("/api/vdms/vdms-1/timezone").contentType("application/json")
                .content("{\"timezone\":\"Asia/Kolkata\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.timezone").value("Asia/Kolkata"));
        assertEquals("Asia/Kolkata", registry.findById("vdms-1").orElseThrow().getTimezone());
        org.mockito.ArgumentCaptor<io.sclera.scheduler.client.JobSchedule> cap =
            org.mockito.ArgumentCaptor.forClass(io.sclera.scheduler.client.JobSchedule.class);
        org.mockito.Mockito.verify(schedulerClient, org.mockito.Mockito.atLeastOnce()).schedule(cap.capture());
        assertTrue(cap.getAllValues().stream().anyMatch(s -> "Asia/Kolkata".equals(s.timezone())));
    }

    @Test
    void editTimezoneRejectsUnknownVdms() throws Exception {
        mvc().perform(put("/api/vdms/nope/timezone").contentType("application/json")
                .content("{\"timezone\":\"UTC\"}"))
            .andExpect(status().isNotFound());
    }

    @Test
    void editTimezoneRejectsInvalidZone() throws Exception {
        registry.save(new VdmsRegistryEntity("vdms-1", "UTC", true));
        mvc().perform(put("/api/vdms/vdms-1/timezone").contentType("application/json")
                .content("{\"timezone\":\"Bogus/Zone\"}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void removeDeactivatesVdmsAndDisablesInstances() throws Exception {
        registry.save(new VdmsRegistryEntity("vdms-1", "UTC", true));
        seedPerVdmsJob();
        instances.save(new JobInstanceEntity("vdmsSystemHealth", "vdms-1", "vdmsSystemHealth::vdms-1"));

        mvc().perform(delete("/api/vdms/vdms-1")).andExpect(status().isNoContent());

        assertFalse(registry.findById("vdms-1").orElseThrow().isActive());
        assertEquals(JobInstanceState.DISABLED,
            instances.findByVdmsId("vdms-1").get(0).getState());
    }

    @Test
    void removeRejectsUnknownVdms() throws Exception {
        mvc().perform(delete("/api/vdms/nope")).andExpect(status().isNotFound());
    }
}
