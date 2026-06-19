package io.sclera.workorder.client;

import io.sclera.workorder.config.DaprProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.time.Instant;

import static org.springframework.http.HttpMethod.POST;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class SchedulerClientTest {

    private MockRestServiceServer mockServer;
    private SchedulerClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://localhost:3501/v1.0");
        mockServer = MockRestServiceServer.bindTo(builder).build();
        DaprProperties props = new DaprProperties();
        props.setHttpPort(3501);
        props.setSchedulerAppId("sclera-scheduler");
        client = new SchedulerClient(builder.build(), props);
    }

    @Test
    void scheduleOneTime_invokesSchedulerOnetimeEndpoint() {
        Instant due = Instant.parse("2026-06-20T14:00:00Z");
        mockServer.expect(requestTo(
                "http://localhost:3501/v1.0/invoke/sclera-scheduler/method/api/jobs/onetime"))
            .andExpect(method(POST))
            .andExpect(jsonPath("$.name").value("demoWorkorderOnce-x"))
            .andExpect(jsonPath("$.owner").value("workorder"))
            .andExpect(jsonPath("$.dueAt").value("2026-06-20T14:00:00Z"))
            .andRespond(withSuccess());

        client.scheduleOneTime("demoWorkorderOnce-x", "workorder", due);
        mockServer.verify();
    }
}
