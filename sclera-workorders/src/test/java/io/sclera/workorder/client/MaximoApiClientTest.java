package io.sclera.workorder.client;

import io.sclera.workorder.dto.MaximoConfigurationDTO;
import io.sclera.workorder.dto.MaximoDTO;
import io.sclera.workorder.util.MaximoUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class MaximoApiClientTest {

    private MockRestServiceServer mockServer;
    private MaximoApiClient client;
    private MaximoUtils mockUtils;

    private static final String AUTH_URL = "http://maximo-auth/token";
    private static final String SERVER_URL = "http://maximo-oslc/api";

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();
        mockUtils = mock(MaximoUtils.class);
        when(mockUtils.buildParams(any(), any(), anyInt(), anyInt())).thenReturn(Map.of());
        client = new MaximoApiClient(builder.build(), mockUtils);
    }

    private MaximoConfigurationDTO sampleConfig() {
        return new MaximoConfigurationDTO("id", "name", SERVER_URL, AUTH_URL, "cid", "csecret", "[]");
    }

    // ── generateMaximoAccessToken ─────────────────────────────────────────────

    @Test
    void generateMaximoAccessToken_returnsTokenOn200() {
        mockServer.expect(requestTo(AUTH_URL))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{\"access_token\":\"tok-abc\"}", MediaType.APPLICATION_JSON));

        String token = client.generateMaximoAccessToken(sampleConfig());

        assertThat(token).isEqualTo("tok-abc");
        mockServer.verify();
    }

    @Test
    void generateMaximoAccessToken_returnsNullOnServerError() {
        mockServer.expect(requestTo(AUTH_URL))
                .andRespond(withServerError());

        assertThat(client.generateMaximoAccessToken(sampleConfig())).isNull();
    }

    // ── getAllWorkorders ───────────────────────────────────────────────────────

    @Test
    void getAllWorkorders_returnsParsedBodyOn200() {
        mockServer.expect(requestTo(SERVER_URL))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"member\":[]}", MediaType.APPLICATION_JSON));

        var result = client.getAllWorkorders("token", SERVER_URL, "all", new MaximoDTO(), 1, 10);

        assertThat(result).isNotNull();
        assertThat(result.getInteger("status_code")).isEqualTo(200);
        mockServer.verify();
    }

    @Test
    void getAllWorkorders_returnsNullOn401() {
        // RestClient throws RestClientResponseException on 4xx, caught by the generic handler → null
        mockServer.expect(requestTo(SERVER_URL))
                .andRespond(withStatus(HttpStatus.UNAUTHORIZED));

        assertThat(client.getAllWorkorders("bad-token", SERVER_URL, "all", new MaximoDTO(), 1, 10)).isNull();
    }

    @Test
    void getAllWorkorders_returnsNullOnServerError() {
        mockServer.expect(requestTo(SERVER_URL))
                .andRespond(withServerError());

        assertThat(client.getAllWorkorders("token", SERVER_URL, "all", new MaximoDTO(), 1, 10)).isNull();
    }
}
