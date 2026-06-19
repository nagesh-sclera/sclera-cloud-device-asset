package io.sclera.workorder.client;

import io.sclera.workorder.config.DaprProperties;
import io.sclera.workorder.dto.VdmsDetailsDTO;
import io.sclera.workorder.exception.VdmsUnavailableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Stubs the Dapr sidecar's HTTP invoke endpoint via MockRestServiceServer.
 * Uses {@code MockRestServiceServer.bindTo(RestClient.Builder)} — the recommended
 * Spring 6.1+ pattern for testing {@link RestClient}.
 */
class VdmsClientTest {

    private MockRestServiceServer mockServer;
    private VdmsClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://localhost:3501/v1.0");
        mockServer = MockRestServiceServer.bindTo(builder).build();
        RestClient restClient = builder.build();

        DaprProperties props = new DaprProperties();
        props.setHttpPort(3501);
        props.setVdmsAppId("vdms-service");

        client = new VdmsClient(restClient, props);
    }

    // ── getVdmsId ─────────────────────────────────────────────────────────────

    @Test
    void getVdmsId_returnsIdFromJsonResponse() {
        mockServer.expect(requestTo("http://localhost:3501/v1.0/invoke/vdms-service/method/vdms/id"))
                .andExpect(method(GET))
                .andRespond(withSuccess("{\"vdmsId\":\"v-123\"}", MediaType.APPLICATION_JSON));

        assertThat(client.getVdmsId()).isEqualTo("v-123");
    }

    @Test
    void getVdmsId_returnsNullOnServerError() {
        mockServer.expect(requestTo("http://localhost:3501/v1.0/invoke/vdms-service/method/vdms/id"))
                .andRespond(withServerError());

        assertThat(client.getVdmsId()).isNull();
    }

    // ── getVdmsDetails ────────────────────────────────────────────────────────

    @Test
    void getVdmsDetails_returnsDtoOn200() {
        mockServer.expect(requestTo("http://localhost:3501/v1.0/invoke/vdms-service/method/vdms/details"))
                .andExpect(method(org.springframework.http.HttpMethod.GET))
                .andRespond(withSuccess(
                        "{\"id\":\"v1\",\"name\":\"Site A\",\"status\":\"ACTIVE\"}",
                        MediaType.APPLICATION_JSON));

        Optional<VdmsDetailsDTO> result = client.getVdmsDetails("v1");

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo("v1");
        assertThat(result.get().getName()).isEqualTo("Site A");
        mockServer.verify();
    }

    @Test
    void getVdmsDetails_returnsEmptyOn404() {
        mockServer.expect(requestTo("http://localhost:3501/v1.0/invoke/vdms-service/method/vdms/details"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        Optional<VdmsDetailsDTO> result = client.getVdmsDetails("missing");

        assertThat(result).isEmpty();
    }

    @Test
    void getVdmsDetails_throwsUnavailableOn500() {
        mockServer.expect(requestTo("http://localhost:3501/v1.0/invoke/vdms-service/method/vdms/details"))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThatThrownBy(() -> client.getVdmsDetails("v1"))
                .isInstanceOf(VdmsUnavailableException.class);
    }
}
