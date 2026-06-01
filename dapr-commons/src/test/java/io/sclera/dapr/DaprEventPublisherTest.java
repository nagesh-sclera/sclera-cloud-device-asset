package io.sclera.dapr;

import io.dapr.client.DaprClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DaprEventPublisherTest {

    @Mock
    private DaprClient daprClient;

    private DaprEventPublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new DaprEventPublisher(daprClient);
    }

    @Test
    void successfulPublish_returnsSuccessResultWithEventId() {
        when(daprClient.publishEvent(eq("pubsub"), eq("test-topic"), any()))
            .thenReturn(Mono.empty());

        PublishResult result = publisher.publish("pubsub", "test-topic", Map.of("key", "val"));

        assertThat(result.success()).isTrue();
        assertThat(result.eventId()).isNotBlank();
        assertThat(result.error()).isNull();
    }

    @Test
    void failedPublish_returnsFailureResultWithErrorMessage() {
        when(daprClient.publishEvent(eq("pubsub"), eq("test-topic"), any()))
            .thenReturn(Mono.error(new RuntimeException("sidecar unavailable")));

        PublishResult result = publisher.publish("pubsub", "test-topic", Map.of("key", "val"));

        assertThat(result.success()).isFalse();
        assertThat(result.eventId()).isNotBlank();
        assertThat(result.error()).contains("sidecar unavailable");
    }
}
