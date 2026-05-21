package io.sclera.client;

import io.dapr.client.DaprClient;
import io.dapr.client.DaprHttp;
import io.dapr.client.domain.HttpExtension;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class IntegrationClientTest {

    @Test
    void updateCustomerOrgByIntegrationId_usesCorrectAppIdPathAndVerb() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(
                eq("sclera-integrations"),
                eq("integration/updateCustomerOrgByIntegrationId"),
                any(),
                any(HttpExtension.class)))
            .thenReturn(Mono.empty());

        IntegrationClient client = new IntegrationClient(dapr);
        client.updateCustomerOrgByIntegrationId("org-001");

        ArgumentCaptor<HttpExtension> extCaptor = ArgumentCaptor.forClass(HttpExtension.class);
        verify(dapr).invokeMethod(
                eq("sclera-integrations"),
                eq("integration/updateCustomerOrgByIntegrationId"),
                any(),
                extCaptor.capture());
        assertThat(extCaptor.getValue().getMethod()).isEqualTo(DaprHttp.HttpMethods.POST);
    }

    @Test
    void updateCustomerOrgByIntegrationId_swallowsException() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(any(String.class), any(String.class), any(), any(HttpExtension.class)))
            .thenReturn(Mono.error(new RuntimeException("sidecar down")));

        IntegrationClient client = new IntegrationClient(dapr);
        // must not throw
        client.updateCustomerOrgByIntegrationId("org-001");
    }
}
