package io.sclera.client;

import io.dapr.client.DaprClient;
import io.dapr.client.DaprHttp;
import io.dapr.client.domain.HttpExtension;
import io.sclera.dto.CorrigoConfigurationDTO;
import io.sclera.dto.DeviceDTO;
import org.json.JSONArray;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CorrigoClientTest {

    @Test
    void clientConstructsCleanly() {
        DaprClient dapr = mock(DaprClient.class);
        CorrigoClient client = new CorrigoClient(dapr);
        assertThat(client).isNotNull();
    }

    // ── happy-path: exact path and verb assertions ────────────────────────────

    @Test
    void getCorrigoConfigurationDetails_usesCorrectPathAndVerb() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(
                eq("sclera-workorders"),
                eq("corrigo/getCorrigoConfigurationDetails"),
                any(),
                any(HttpExtension.class)))
            .thenReturn(Mono.empty());

        CorrigoClient client = new CorrigoClient(dapr);
        CorrigoConfigurationDTO result = client.getCorrigoConfigurationDetails();

        ArgumentCaptor<HttpExtension> extCaptor = ArgumentCaptor.forClass(HttpExtension.class);
        verify(dapr).invokeMethod(
                eq("sclera-workorders"),
                eq("corrigo/getCorrigoConfigurationDetails"),
                any(),
                extCaptor.capture());
        assertThat(extCaptor.getValue().getMethod()).isEqualTo(DaprHttp.HttpMethods.GET);
        assertThat(result).isNull();
    }

    @Test
    void updateCorrigoAssets_usesCorrectPathAndVerb() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(
                eq("sclera-workorders"),
                eq("corrigo/updateCorrigoAssets"),
                any(),
                any(HttpExtension.class)))
            .thenReturn(Mono.empty());

        CorrigoClient client = new CorrigoClient(dapr);
        client.updateCorrigoAssets("user", "vdms-001", 0, 10, "key", new CorrigoConfigurationDTO());

        ArgumentCaptor<HttpExtension> extCaptor = ArgumentCaptor.forClass(HttpExtension.class);
        verify(dapr).invokeMethod(
                eq("sclera-workorders"),
                eq("corrigo/updateCorrigoAssets"),
                any(),
                extCaptor.capture());
        assertThat(extCaptor.getValue().getMethod()).isEqualTo(DaprHttp.HttpMethods.GET);
    }

    @Test
    void getWorkordersByAssetIdForBot_usesCorrectPathAndVerb() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(
                eq("sclera-workorders"),
                eq("corrigo/getWorkordersByAssetIdForBot"),
                any(),
                any(HttpExtension.class)))
            .thenReturn(Mono.empty());

        CorrigoClient client = new CorrigoClient(dapr);
        JSONArray result = client.getWorkordersByAssetIdForBot(new DeviceDTO());

        ArgumentCaptor<HttpExtension> extCaptor = ArgumentCaptor.forClass(HttpExtension.class);
        verify(dapr).invokeMethod(
                eq("sclera-workorders"),
                eq("corrigo/getWorkordersByAssetIdForBot"),
                any(),
                extCaptor.capture());
        assertThat(extCaptor.getValue().getMethod()).isEqualTo(DaprHttp.HttpMethods.GET);
        assertThat(result).isNull();
    }

    @Test
    void corrigoUrlSync_usesCorrectPathAndVerb() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(
                eq("sclera-workorders"),
                eq("corrigo/corrigoUrlSync"),
                any(),
                any(HttpExtension.class)))
            .thenReturn(Mono.empty());

        CorrigoClient client = new CorrigoClient(dapr);
        client.corrigoUrlSync(new Object(), "vdms-001");

        ArgumentCaptor<HttpExtension> extCaptor = ArgumentCaptor.forClass(HttpExtension.class);
        verify(dapr).invokeMethod(
                eq("sclera-workorders"),
                eq("corrigo/corrigoUrlSync"),
                any(),
                extCaptor.capture());
        assertThat(extCaptor.getValue().getMethod()).isEqualTo(DaprHttp.HttpMethods.GET);
    }

    @Test
    void updateCorrigoCredentialsFromCloud_usesCorrectPathAndVerb() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(
                eq("sclera-workorders"),
                eq("corrigo/updateCorrigoCredentialsFromCloud"),
                any(),
                any(HttpExtension.class)))
            .thenReturn(Mono.empty());

        CorrigoClient client = new CorrigoClient(dapr);
        client.updateCorrigoCredentialsFromCloud("vdms-001");

        ArgumentCaptor<HttpExtension> extCaptor = ArgumentCaptor.forClass(HttpExtension.class);
        verify(dapr).invokeMethod(
                eq("sclera-workorders"),
                eq("corrigo/updateCorrigoCredentialsFromCloud"),
                any(),
                extCaptor.capture());
        assertThat(extCaptor.getValue().getMethod()).isEqualTo(DaprHttp.HttpMethods.GET);
    }

    @Test
    void updateCorrigoCredentialsMigration_usesCorrectPathAndVerb() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(
                eq("sclera-workorders"),
                eq("corrigo/updateCorrigoCredentialsMigration"),
                any(),
                any(HttpExtension.class)))
            .thenReturn(Mono.empty());

        CorrigoClient client = new CorrigoClient(dapr);
        client.updateCorrigoCredentialsMigration("vdms-001");

        ArgumentCaptor<HttpExtension> extCaptor = ArgumentCaptor.forClass(HttpExtension.class);
        verify(dapr).invokeMethod(
                eq("sclera-workorders"),
                eq("corrigo/updateCorrigoCredentialsMigration"),
                any(),
                extCaptor.capture());
        assertThat(extCaptor.getValue().getMethod()).isEqualTo(DaprHttp.HttpMethods.GET);
    }

    // ── resilience: exception swallowing ─────────────────────────────────────

    @Test
    void clientReturnsDocumentedDefaultOnDaprException() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(any(String.class), any(String.class), any(), any(HttpExtension.class)))
            .thenReturn(Mono.error(new RuntimeException("sidecar down")));

        CorrigoClient client = new CorrigoClient(dapr);
        assertThat(client.getCorrigoConfigurationDetails()).isNull();
        assertThat(client.getWorkordersByAssetIdForBot(new DeviceDTO())).isNull();
        client.updateCorrigoAssets("u", "v", 0, 10, "k", new CorrigoConfigurationDTO()); // must not throw
        client.corrigoUrlSync(new Object(), "v"); // must not throw
        client.updateCorrigoCredentialsFromCloud("v"); // must not throw
        client.updateCorrigoCredentialsMigration("v"); // must not throw
    }
}
