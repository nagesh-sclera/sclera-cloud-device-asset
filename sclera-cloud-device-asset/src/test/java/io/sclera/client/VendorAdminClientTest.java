package io.sclera.client;

import io.dapr.client.DaprClient;
import io.dapr.client.DaprHttp;
import io.dapr.client.domain.HttpExtension;
import io.sclera.dto.VendorDTO;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class VendorAdminClientTest {

    @Test
    void clientConstructsCleanly() {
        DaprClient dapr = mock(DaprClient.class);
        VendorAdminClient client = new VendorAdminClient(dapr);
        assertThat(client).isNotNull();
    }

    // ── happy-path: exact path and verb assertion ─────────────────────────────

    @Test
    void deleteVendorsByOrganisationId_usesCorrectPathAndVerb() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(
                eq("sclera-identity"),
                eq("vendorAdmin/deleteVendorsByOrganisationId"),
                any(),
                any(HttpExtension.class)))
            .thenReturn(Mono.empty());

        VendorAdminClient client = new VendorAdminClient(dapr);
        client.deleteVendorsByOrganisationId("org-123");

        ArgumentCaptor<HttpExtension> extCaptor = ArgumentCaptor.forClass(HttpExtension.class);
        verify(dapr).invokeMethod(
                eq("sclera-identity"),
                eq("vendorAdmin/deleteVendorsByOrganisationId"),
                any(),
                extCaptor.capture());
        assertThat(extCaptor.getValue().getMethod())
                .isEqualTo(DaprHttp.HttpMethods.POST);
    }

    @Test
    void insertVendors_usesCorrectPathAndVerb() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(
                eq("sclera-identity"),
                eq("vendorAdmin/insertVendors"),
                any(),
                any(HttpExtension.class)))
            .thenReturn(Mono.empty());

        VendorAdminClient client = new VendorAdminClient(dapr);
        client.insertVendors(new VendorDTO());

        ArgumentCaptor<HttpExtension> extCaptor = ArgumentCaptor.forClass(HttpExtension.class);
        verify(dapr).invokeMethod(
                eq("sclera-identity"),
                eq("vendorAdmin/insertVendors"),
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

        VendorAdminClient client = new VendorAdminClient(dapr);
        // both void methods must swallow exceptions silently
        client.deleteVendorsByOrganisationId("org-123");
        client.insertVendors(new VendorDTO());
    }
}
