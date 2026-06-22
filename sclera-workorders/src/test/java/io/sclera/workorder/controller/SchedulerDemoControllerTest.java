package io.sclera.workorder.controller;

import io.sclera.workorder.client.SchedulerClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SchedulerDemoController.class)
class SchedulerDemoControllerTest {

    @Autowired MockMvc mvc;
    @MockitoBean SchedulerClient scheduler;

    @Test
    void scheduleOneTime_returnsJobNameAndInvokesClient() throws Exception {
        mvc.perform(post("/demo/schedule-onetime").param("seconds", "20"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.scheduledJob").value("workorderTicketSync"));
        verify(scheduler).scheduleOnce(eq("workorderTicketSync"), any());
    }
}
