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

class SiemensClientTest {

    @Test
    void getSiemensDeviceIdForAdvanceExcelExport_usesCorrectAppIdPathAndVerb() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(
                eq("sclera-integrations"),
                eq("siemens/getSiemensDeviceIdForAdvanceExcelExport"),
                any(),
                any(HttpExtension.class)))
            .thenReturn(Mono.empty());

        SiemensClient client = new SiemensClient(dapr);
        var result = client.getSiemensDeviceIdForAdvanceExcelExport("user", "vdms-001", "dev-001");

        ArgumentCaptor<HttpExtension> extCaptor = ArgumentCaptor.forClass(HttpExtension.class);
        verify(dapr).invokeMethod(
                eq("sclera-integrations"),
                eq("siemens/getSiemensDeviceIdForAdvanceExcelExport"),
                any(),
                extCaptor.capture());
        assertThat(extCaptor.getValue().getMethod()).isEqualTo(DaprHttp.HttpMethods.GET);
        assertThat(result).isEqualTo(Collections.emptyList());
    }

    @Test
    void getSiemensBmsData_returnsEmptyListOnException() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(any(String.class), any(String.class), any(), any(HttpExtension.class)))
            .thenReturn(Mono.error(new RuntimeException("sidecar down")));

        SiemensClient client = new SiemensClient(dapr);
        assertThat(client.getSiemensBmsData("user", "vdms-001", "dev-001")).isEqualTo(Collections.emptyList());
    }

    @Test
    void updateSiemensDeviceId_swallowsException() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(any(String.class), any(String.class), any(), any(HttpExtension.class)))
            .thenReturn(Mono.error(new RuntimeException("sidecar down")));

        SiemensClient client = new SiemensClient(dapr);
        client.updateSiemensDeviceId("old", "new");
    }
}
