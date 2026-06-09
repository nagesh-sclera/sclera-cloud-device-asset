package io.sclera.client;

import io.dapr.client.DaprClient;
import io.dapr.client.DaprClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration that exposes a singleton Dapr client used by the client-package
 * components to invoke remote microservices through the Dapr sidecar.
 */
@Configuration
public class DaprClientConfig {
    /**
     * Builds and registers the application-wide {@link DaprClient} bean, closed on shutdown.
     */
    @Bean(destroyMethod = "close")
    public DaprClient daprClient() {
        return new DaprClientBuilder().build();
    }
}
