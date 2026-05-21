package io.sclera.client;

import io.dapr.client.DaprClient;
import io.dapr.client.DaprHttp;
import io.dapr.client.domain.HttpExtension;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import reactor.core.publisher.Mono;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GlobalChecklistConditionsClientTest {

    @Test
    void clientConstructsCleanly() {
        DaprClient dapr = mock(DaprClient.class);
        GlobalChecklistConditionsClient client = new GlobalChecklistConditionsClient(dapr);
        assertThat(client).isNotNull();
    }

    @Test
    void updateGlobalChecklistConditionsDeviceAndIsRemoved_usesCorrectPathAndVerb() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(
                eq("sclera-inspection"),
                eq("globalChecklistConditions/updateGlobalChecklistConditionsDeviceAndIsRemoved"),
                any(),
                any(HttpExtension.class)))
            .thenReturn(Mono.empty());

        GlobalChecklistConditionsClient client = new GlobalChecklistConditionsClient(dapr);
        client.updateGlobalChecklistConditionsDeviceAndIsRemoved(Collections.singleton("device-001"));

        ArgumentCaptor<HttpExtension> extCaptor = ArgumentCaptor.forClass(HttpExtension.class);
        verify(dapr).invokeMethod(
                eq("sclera-inspection"),
                eq("globalChecklistConditions/updateGlobalChecklistConditionsDeviceAndIsRemoved"),
                any(),
                extCaptor.capture());
        assertThat(extCaptor.getValue().getMethod()).isEqualTo(DaprHttp.HttpMethods.POST);
    }

    @Test
    void updateGlobalChecklistConditionsLocationAndIsRemoved_usesCorrectPathAndVerb() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(
                eq("sclera-inspection"),
                eq("globalChecklistConditions/updateGlobalChecklistConditionsLocationAndIsRemoved"),
                any(),
                any(HttpExtension.class)))
            .thenReturn(Mono.empty());

        GlobalChecklistConditionsClient client = new GlobalChecklistConditionsClient(dapr);
        client.updateGlobalChecklistConditionsLocationAndIsRemoved(Collections.singleton("loc-001"));

        ArgumentCaptor<HttpExtension> extCaptor = ArgumentCaptor.forClass(HttpExtension.class);
        verify(dapr).invokeMethod(
                eq("sclera-inspection"),
                eq("globalChecklistConditions/updateGlobalChecklistConditionsLocationAndIsRemoved"),
                any(),
                extCaptor.capture());
        assertThat(extCaptor.getValue().getMethod()).isEqualTo(DaprHttp.HttpMethods.POST);
    }

    @Test
    void clientReturnsDocumentedDefaultOnDaprException() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(any(String.class), any(String.class), any(), any(HttpExtension.class)))
            .thenReturn(Mono.error(new RuntimeException("sidecar down")));

        GlobalChecklistConditionsClient client = new GlobalChecklistConditionsClient(dapr);
        // void methods must not throw
        client.updateGlobalChecklistConditionsDeviceAndIsRemoved(Collections.emptySet());
        client.updateGlobalChecklistConditionsLocationAndIsRemoved(Collections.emptySet());
    }
}
