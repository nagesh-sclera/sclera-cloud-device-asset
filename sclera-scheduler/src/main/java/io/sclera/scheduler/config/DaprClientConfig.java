package io.sclera.scheduler.config;

import io.dapr.client.DaprClient;
import io.dapr.client.DaprClientBuilder;
import io.sclera.dapr.DaprEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DaprClientConfig {
    @Bean(destroyMethod = "close")
    public DaprClient daprClient() {
        return new DaprClientBuilder().build();
    }

    /**
     * Explicit bean for the dapr-commons publisher. It lives in package io.sclera.dapr
     * (outside this app's io.sclera.scheduler base package), so we declare it here rather
     * than widening component scanning to the whole io.sclera tree.
     */
    @Bean
    public DaprEventPublisher daprEventPublisher(DaprClient daprClient) {
        return new DaprEventPublisher(daprClient);
    }
}
