package io.sclera.scheduler.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "dapr")
public record DaprProperties(int httpPort) {
    public DaprProperties {
        if (httpPort == 0) httpPort = 3500;
    }
}
