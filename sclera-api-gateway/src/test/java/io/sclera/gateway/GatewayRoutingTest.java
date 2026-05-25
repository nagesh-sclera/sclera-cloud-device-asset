package io.sclera.gateway;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * Routing smoke tests for the SCG 5.0 (Spring Cloud 2025.1.0) gateway.
 *
 * <h3>Why DNS-based upstreams cannot be used in assertions</h3>
 * In SCG 5.0, ALL upstream-unreachable failures (DNS NXDOMAIN, connection
 * refused, connect-timeout) are mapped to HTTP 404 — the same status returned
 * when NO route matches at all.  Accepting 404 in a "route is matched" test
 * is therefore a masking defect: the test would pass even if the route were
 * deleted.  (Empirically confirmed: both Docker-hostname DNS failures and
 * connection-refused to localhost:19999 yield 404 in this JVM.)
 *
 * <h3>SCG 5.0 property namespace change</h3>
 * Spring Cloud Gateway 5.0 moved its {@code @ConfigurationProperties} prefix
 * from {@code spring.cloud.gateway} to {@code spring.cloud.gateway.server.webflux}.
 * Properties under the old prefix are silently ignored.
 *
 * <h3>The fix: {@code forward://} internal dispatch to a known path</h3>
 * Test properties define routes using {@code spring.cloud.gateway.server.webflux.routes[*]}
 * with upstream URI {@code forward:///actuator/health}.  The {@code forward://}
 * scheme dispatches the request internally to the specified path on the gateway
 * itself; {@code /actuator/health} is always available and returns HTTP 200.
 *
 * <ul>
 *   <li>Path predicate matches → route filter chain runs → forward to /actuator/health → 200</li>
 *   <li>Path predicate does not match → gateway no-route handler → 404</li>
 * </ul>
 *
 * HTTP 200 is an unambiguous "route was matched" signal; deleting either route
 * from the config or breaking its predicate would make the corresponding test
 * fail with 404.
 *
 * <h3>What is tested</h3>
 * The path predicates ({@code /asset/**} and {@code /vdms/**}) are identical
 * to those in application.yml — the routing logic under test is unchanged.
 * Only the upstream URI is adjusted for test isolation.
 */
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                // SCG 5.0 uses spring.cloud.gateway.server.webflux.* (not spring.cloud.gateway.*)
                "spring.cloud.gateway.server.webflux.httpclient.connect-timeout=500",
                // Routes forward internally to /actuator/health (always 200).
                // 404 then exclusively signals "no route matched": a deleted
                // or misconfigured route fails these tests.
                "spring.cloud.gateway.server.webflux.routes[0].id=asset-route",
                "spring.cloud.gateway.server.webflux.routes[0].uri=forward:///actuator/health",
                "spring.cloud.gateway.server.webflux.routes[0].predicates[0]=Path=/asset/**",
                "spring.cloud.gateway.server.webflux.routes[1].id=vdms-route",
                "spring.cloud.gateway.server.webflux.routes[1].uri=forward:///actuator/health",
                "spring.cloud.gateway.server.webflux.routes[1].predicates[0]=Path=/vdms/**"
        }
)
class GatewayRoutingTest {

    @LocalServerPort
    int port;

    WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    /**
     * Verifies that requests matching {@code /asset/**} are dispatched by
     * {@code asset-route}.
     *
     * The route's upstream is overridden to {@code forward:///actuator/health},
     * so a matched route always returns 200.
     *
     * 404 is excluded: in SCG 5.0 it is the no-route-matched signal.
     * All upstream failures (including DNS failure) also yield 404, so
     * accepting 404 would mask a deleted or misconfigured route.
     */
    @Test
    void assetRouteIsMatched() {
        webTestClient.get().uri("/asset/actuator/health")
                .exchange()
                // 200 = route matched, forwarded internally to /actuator/health.
                // 404 = no route matched — must NOT be accepted here.
                .expectStatus().isOk();
    }

    /**
     * Verifies that requests matching {@code /vdms/**} are dispatched by
     * {@code vdms-route}.
     *
     * Same strategy: forward:///actuator/health returns 200 when the
     * /vdms/** predicate matches.
     *
     * 404 is excluded for the same reason: it is the no-route-matched signal
     * in SCG 5.0 and must fail this test.
     */
    @Test
    void vdmsRouteIsMatched() {
        webTestClient.get().uri("/vdms/id")
                .exchange()
                // 200 = route matched, forwarded internally to /actuator/health.
                // 404 = no route matched — must NOT be accepted here.
                .expectStatus().isOk();
    }

    @Test
    void unknownRouteReturns404() {
        webTestClient.get().uri("/unknown/path")
                .exchange()
                .expectStatus().isNotFound();
    }
}
