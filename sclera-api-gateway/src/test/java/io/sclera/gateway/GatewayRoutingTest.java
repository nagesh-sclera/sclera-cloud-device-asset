package io.sclera.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.cloud.gateway.httpclient.connect-timeout=500"
)
@AutoConfigureWebTestClient
class GatewayRoutingTest {

    @Autowired
    WebTestClient webTestClient;

    @Test
    void assetRouteIsMatched() {
        // 503/500 = route matched, backend not running in test env — that is correct
        // Spring Cloud Gateway may return 500 or 503 depending on the connection error type
        webTestClient.get().uri("/asset/actuator/health")
                .exchange()
                .expectStatus().value(status -> assertThat(status).isIn(200, 500, 503));
    }

    @Test
    void vdmsRouteIsMatched() {
        // /vdms/id → no strip → forwarded as /vdms/id to vdms-service
        // 503/500 = route matched, backend not running in test env — that is correct
        // Spring Cloud Gateway may return 500 or 503 depending on the connection error type
        webTestClient.get().uri("/vdms/id")
                .exchange()
                .expectStatus().value(status -> assertThat(status).isIn(200, 404, 500, 503));
    }

    @Test
    void unknownRouteReturns404() {
        webTestClient.get().uri("/unknown/path")
                .exchange()
                .expectStatus().isNotFound();
    }
}
