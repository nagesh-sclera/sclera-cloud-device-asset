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
import static org.mockito.Mockito.*;

class SpecificationsClientTest {

    @Test
    void clientConstructsCleanly() {
        DaprClient dapr = mock(DaprClient.class);
        SpecificationsClient client = new SpecificationsClient(dapr);
        assertThat(client).isNotNull();
    }

    @Test
    void getDeviceSpecificationsBasedOnDeviceId_usesCorrectPathAndVerb() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(eq("sclera-inventory"), eq("specifications/getDeviceSpecificationsBasedOnDeviceId"), any(), any(HttpExtension.class)))
                .thenReturn(Mono.empty());

        SpecificationsClient client = new SpecificationsClient(dapr);
        var result = client.getDeviceSpecificationsBasedOnDeviceId("device-001");

        ArgumentCaptor<HttpExtension> extCaptor = ArgumentCaptor.forClass(HttpExtension.class);
        verify(dapr).invokeMethod(eq("sclera-inventory"), eq("specifications/getDeviceSpecificationsBasedOnDeviceId"), any(), extCaptor.capture());
        assertThat(extCaptor.getValue().getMethod()).isEqualTo(DaprHttp.HttpMethods.GET);
        assertThat(result).isEmpty();
    }

    @Test
    void checkSpecificationByDeviceId_usesCorrectPath() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(eq("sclera-inventory"), eq("specifications/checkSpecificationByDeviceId"), any(), any(HttpExtension.class)))
                .thenReturn(Mono.empty());

        SpecificationsClient client = new SpecificationsClient(dapr);
        Integer result = client.checkSpecificationByDeviceId("device-001", "Power");
        assertThat(result).isEqualTo(0);
    }

    @Test
    void returnsDocumentedDefaultOnDaprException() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(any(String.class), any(String.class), any(), any(HttpExtension.class)))
                .thenReturn(Mono.error(new RuntimeException("sidecar down")));

        SpecificationsClient client = new SpecificationsClient(dapr);
        assertThat(client.checkSpecificationByDeviceId("device-001", "Power")).isEqualTo(0);
        assertThat(client.getDeviceSpecificationsBasedOnDeviceId("device-001")).isEmpty();
        assertThat(client.getDeviceSpecificationsBasedOnDeviceIdAndKeyName("device-001", "Power")).isNull();
        assertThat(client.getPower("device-001", "Output Power")).isNull();
        // void methods swallow
        client.editDeviceSpecifications("id", "val", "unit", "name");
        client.upsertDeviceSpecification("id", "name", "val", "unit", "device-001");
        client.deleteById("id-001");
    }
}
