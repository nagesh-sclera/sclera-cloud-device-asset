package io.sclera.workorder.client;

import io.sclera.workorder.config.DaprProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class DeviceAssetClientTest {

    private MockRestServiceServer mockServer;
    private DeviceAssetClient client;
    private DaprProperties props;

    @BeforeEach
    void setUp() {
        props = new DaprProperties();
        RestClient.Builder builder = RestClient.builder().baseUrl(props.baseUrl());
        mockServer = MockRestServiceServer.bindTo(builder).build();
        client = new DeviceAssetClient(builder.build(), props);
    }

    @Test
    void syncTicketStats_sendsPutToCorrectEndpoint() {
        mockServer.expect(requestTo(containsString(
                        "/invoke/sclera-cloud-device-asset/method/api/v1/device-asset-service/internal/device/dev-1/ticket-sync")))
                .andExpect(method(HttpMethod.PUT))
                .andRespond(withSuccess());

        client.syncTicketStats("dev-1");

        mockServer.verify();
    }

    @Test
    void syncTicketStats_doesNotThrowOnServerError() {
        mockServer.expect(requestTo(containsString("/device/dev-2/ticket-sync")))
                .andRespond(withServerError());

        client.syncTicketStats("dev-2"); // fire-and-forget: catch block must swallow the 500
    }
}
