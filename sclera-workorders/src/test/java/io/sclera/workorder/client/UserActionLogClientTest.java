package io.sclera.workorder.client;

import io.sclera.workorder.config.DaprProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withNoContent;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

/**
 * Tests {@link UserActionLogClient} pub/sub publish behavior.
 * Stubs the Dapr sidecar's publish endpoint via MockRestServiceServer.
 */
class UserActionLogClientTest {

    private MockRestServiceServer mockServer;
    private UserActionLogClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://localhost:3501/v1.0");
        mockServer = MockRestServiceServer.bindTo(builder).build();

        DaprProperties props = new DaprProperties();
        props.setHttpPort(3501);
        props.setPubsubName("pubsub");
        props.setTopicName("user-action-log-events");

        RestClient restClient = builder.build();
        client = new UserActionLogClient(restClient, props);
    }

    @Test
    void addUserAction_publishesToPubSubTopic() throws Exception {
        mockServer.expect(requestTo("http://localhost:3501/v1.0/publish/pubsub/user-action-log-events"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("{\"email\":\"user@test.com\",\"type\":\"maximo\",\"action\":\"ADD\"," +
                        "\"message\":\"config added\",\"status\":\"success\",\"subType\":\"maximo_configuration\"," +
                        "\"primaryId\":\"cfg-001\",\"vdmsId\":\"vdms-001\"}", false))
                .andRespond(withNoContent());

        client.addUserAction("user@test.com", "maximo", "ADD", "config added",
                "success", "maximo_configuration", "cfg-001", "vdms-001");

        mockServer.verify();
    }

    @Test
    void addUserAction_swallowsExceptionOnPublishFailure() {
        mockServer.expect(requestTo("http://localhost:3501/v1.0/publish/pubsub/user-action-log-events"))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThatNoException().isThrownBy(() ->
                client.addUserAction("user@test.com", "maximo", "ADD", "config added",
                        "success", "maximo_configuration", "cfg-001", "vdms-001"));
    }
}
