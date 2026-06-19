package io.sclera.workorder.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Two HTTP clients, one for inbound/outbound concerns:
 *
 *  1. {@code daprRestClient} — talks to the local Dapr sidecar
 *     ({@code http://localhost:{daprPort}/v1.0}). Used by {@link io.sclera.workorder.client.VdmsClient}
 *     and {@link io.sclera.workorder.client.UserActionLogClient} for cross-service invocation.
 *
 *  2. {@code maximoHttpClient} — talks DIRECTLY to the configured Maximo server
 *     (the {@code serverUrl} from {@link io.sclera.workorder.entity.MaximoConfiguration}).
 *     This is outbound traffic to an external system — it does NOT go through
 *     Dapr. Used by {@link io.sclera.workorder.client.MaximoApiClient}.
 */
@Configuration
@EnableConfigurationProperties({DaprProperties.class, JobsProperties.class})
public class DaprConfig {

    @Bean
    public RestClient daprRestClient(DaprProperties props) {
        return RestClient.builder()
                .baseUrl(props.baseUrl())
                // Propagate the correlation id (MDC requestId) on cross-service Dapr calls.
                .requestInterceptor(new CorrelationIdPropagationInterceptor())
                .build();
    }

    @Bean
    public RestClient maximoHttpClient() {
        // No base URL — MaximoApiClient passes the full URL per call because
        // it's read from the per-VDMS configuration row.
        return RestClient.builder().build();
    }
}
