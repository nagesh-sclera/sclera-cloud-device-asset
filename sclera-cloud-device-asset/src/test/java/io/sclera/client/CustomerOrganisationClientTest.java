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

class CustomerOrganisationClientTest {

    @Test
    void clientConstructsCleanly() {
        DaprClient dapr = mock(DaprClient.class);
        CustomerOrganisationClient client = new CustomerOrganisationClient(dapr);
        assertThat(client).isNotNull();
    }

    // ── happy-path: exact path and verb assertion ─────────────────────────────

    @Test
    void upsertCustomerByOrganisationIdSync_usesCorrectPathAndVerb() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(
                eq("sclera-identity"),
                eq("customerOrganisation/upsertCustomerByOrganisationIdSync"),
                any(),
                any(HttpExtension.class)))
            .thenReturn(Mono.empty());

        CustomerOrganisationClient client = new CustomerOrganisationClient(dapr);
        client.upsertCustomerByOrganisationIdSync("org-456");

        ArgumentCaptor<HttpExtension> extCaptor = ArgumentCaptor.forClass(HttpExtension.class);
        verify(dapr).invokeMethod(
                eq("sclera-identity"),
                eq("customerOrganisation/upsertCustomerByOrganisationIdSync"),
                any(),
                extCaptor.capture());
        assertThat(extCaptor.getValue().getMethod())
                .isEqualTo(DaprHttp.HttpMethods.POST);
    }

    @Test
    void deleteCustomerOrgById_usesCorrectPathAndVerb() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(
                eq("sclera-identity"),
                eq("customerOrganisation/deleteCustomerOrgById"),
                any(),
                any(HttpExtension.class)))
            .thenReturn(Mono.empty());

        CustomerOrganisationClient client = new CustomerOrganisationClient(dapr);
        client.deleteCustomerOrgById("org-456");

        ArgumentCaptor<HttpExtension> extCaptor = ArgumentCaptor.forClass(HttpExtension.class);
        verify(dapr).invokeMethod(
                eq("sclera-identity"),
                eq("customerOrganisation/deleteCustomerOrgById"),
                any(),
                extCaptor.capture());
        assertThat(extCaptor.getValue().getMethod())
                .isEqualTo(DaprHttp.HttpMethods.POST);
    }

    // ── resilience: exception swallowing ─────────────────────────────────────

    @Test
    void clientReturnsDocumentedDefaultOnDaprException() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(any(String.class), any(String.class), any(), any(HttpExtension.class)))
            .thenReturn(Mono.error(new RuntimeException("sidecar down")));

        CustomerOrganisationClient client = new CustomerOrganisationClient(dapr);
        // both void methods must swallow exceptions silently
        client.upsertCustomerByOrganisationIdSync("org-456");
        client.deleteCustomerOrgById("org-456");
    }
}
