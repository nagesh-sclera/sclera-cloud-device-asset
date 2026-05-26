package io.sclera.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.route.RouteLocator;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Proves that the REAL routes from application.yml are loaded by the gateway.
 *
 * <p>This test intentionally does NOT override any route properties, so it
 * exercises the actual {@code spring.cloud.gateway.server.webflux.routes}
 * configuration.  If the property namespace is wrong (e.g. still using the
 * old {@code spring.cloud.gateway.routes} prefix), the route locator returns
 * zero routes and both assertions fail.
 *
 * <p>SCG requires a reactive web context (it depends on ServerProperties and
 * the Netty server infrastructure), so RANDOM_PORT is used.  No HTTP calls
 * are made — only the injected RouteLocator bean is queried.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RouteConfigTest {

    @Autowired
    RouteLocator routeLocator;

    @Test
    void realRoutesFromApplicationYmlLoad() {
        List<String> routeIds = routeLocator.getRoutes()
                .map(route -> route.getId())
                .collectList()
                .block();

        assertThat(routeIds)
                .as("Routes loaded from application.yml — namespace must be spring.cloud.gateway.server.webflux.*")
                .contains("asset-route", "vdms-route");
    }
}
