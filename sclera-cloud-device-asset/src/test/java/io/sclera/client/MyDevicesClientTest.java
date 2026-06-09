package io.sclera.client;

import com.alibaba.fastjson.JSONObject;
import io.dapr.client.DaprClient;
import io.dapr.client.DaprHttp;
import io.dapr.client.domain.HttpExtension;
import io.sclera.dto.ConditionsDTO;
import io.sclera.dto.MyDevicesCompanyDTO;
import io.sclera.dto.MyDevicesSensorAttributesDTO;
import io.sclera.dto.MyDevicesSensorDTO;
import io.sclera.dto.touchscreen.SensorDTO;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MyDevicesClientTest {

    @Test
    void clientConstructsCleanly() {
        DaprClient dapr = mock(DaprClient.class);
        MyDevicesClient client = new MyDevicesClient(dapr);
        assertThat(client).isNotNull();
    }

    // ── happy-path: exact path and verb assertions ────────────────────────────

    @Test
    void getDeviceIdByMyDevicesSensorId_usesCorrectPathAndVerb() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(
                eq("sclera-integrations"),
                eq("myDevices/getDeviceIdByMyDevicesSensorId"),
                any(),
                any(HttpExtension.class)))
            .thenReturn(Mono.empty());

        MyDevicesClient client = new MyDevicesClient(dapr);
        String result = client.getDeviceIdByMyDevicesSensorId("sensor-001");

        ArgumentCaptor<HttpExtension> extCaptor = ArgumentCaptor.forClass(HttpExtension.class);
        verify(dapr).invokeMethod(
                eq("sclera-integrations"),
                eq("myDevices/getDeviceIdByMyDevicesSensorId"),
                any(),
                extCaptor.capture());
        assertThat(extCaptor.getValue().getMethod()).isEqualTo(DaprHttp.HttpMethods.GET);
        assertThat(result).isNull();
    }

    @Test
    void getMyDevicesSensorCountByDeviceId_usesCorrectPathAndVerb() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(
                eq("sclera-integrations"),
                eq("myDevices/getMyDevicesSensorCountByDeviceId"),
                any(),
                any(HttpExtension.class)))
            .thenReturn(Mono.empty());

        MyDevicesClient client = new MyDevicesClient(dapr);
        Integer result = client.getMyDevicesSensorCountByDeviceId("device-001");

        ArgumentCaptor<HttpExtension> extCaptor = ArgumentCaptor.forClass(HttpExtension.class);
        verify(dapr).invokeMethod(
                eq("sclera-integrations"),
                eq("myDevices/getMyDevicesSensorCountByDeviceId"),
                any(),
                extCaptor.capture());
        assertThat(extCaptor.getValue().getMethod()).isEqualTo(DaprHttp.HttpMethods.GET);
        assertThat(result).isEqualTo(0);
    }

    @Test
    void getMyDevicesSensorAlertStatusByDeviceId_usesCorrectPathAndVerb() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(
                eq("sclera-integrations"),
                eq("myDevices/getMyDevicesSensorAlertStatusByDeviceId"),
                any(),
                any(HttpExtension.class)))
            .thenReturn(Mono.empty());

        MyDevicesClient client = new MyDevicesClient(dapr);
        Boolean result = client.getMyDevicesSensorAlertStatusByDeviceId("device-001");

        ArgumentCaptor<HttpExtension> extCaptor = ArgumentCaptor.forClass(HttpExtension.class);
        verify(dapr).invokeMethod(
                eq("sclera-integrations"),
                eq("myDevices/getMyDevicesSensorAlertStatusByDeviceId"),
                any(),
                extCaptor.capture());
        assertThat(extCaptor.getValue().getMethod()).isEqualTo(DaprHttp.HttpMethods.GET);
        assertThat(result).isNull();
    }

    @Test
    void startMyDevicesService_usesCorrectPathAndVerb() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(
                eq("sclera-integrations"),
                eq("myDevices/startMyDevicesService"),
                any(),
                any(HttpExtension.class)))
            .thenReturn(Mono.empty());

        MyDevicesClient client = new MyDevicesClient(dapr);
        client.startMyDevicesService();

        ArgumentCaptor<HttpExtension> extCaptor = ArgumentCaptor.forClass(HttpExtension.class);
        verify(dapr).invokeMethod(
                eq("sclera-integrations"),
                eq("myDevices/startMyDevicesService"),
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

        MyDevicesClient client = new MyDevicesClient(dapr);
        assertThat(client.getDeviceIdByMyDevicesSensorId("s")).isNull();
        assertThat(client.getMyDevicesSensorCountByDeviceId("d")).isEqualTo(0);
        assertThat(client.getMyDevicesSensorAlertStatusByDeviceId("d")).isNull();
        assertThat(client.getDeviceMyDevicesSensors("v", "c", "d")).isEmpty();
        assertThat(client.getMydevicesSensorsByDeviceId("d")).isEmpty();
        assertThat(client.listmydevicesDeviceAlertMessagesByDeviceIds(Collections.singletonList("d"))).isEmpty();
        assertThat(client.getMyDevicesCompanies("v", 0, 10)).isEmpty();
        assertThat(client.getMyDevicesSensors("v", "c", 0, 10)).isEmpty();
        assertThat(client.getAllMyDevicesCompanies("u", "v")).isEmpty();
        assertThat(client.getMyDevicesCompaniesPagination("u", "v", "k", 0, 10)).isEmpty();
        assertThat(client.getMyDevicesSensor("u", "v", "s")).isNull();
        assertThat(client.getAllMyDevicesSensors("u", "v")).isEmpty();
        assertThat(client.getAllMyDevicesSensorsByPagination("u", "v", "k", 0, 10)).isEmpty();
        assertThat(client.getMyDevicesSensorsByPagination("u", "v", "c", "k", 0, 10)).isEmpty();
        // void methods must not throw
        client.startMyDevicesService();
        client.upsertMyDevicesCompany("u", "v", new MyDevicesCompanyDTO());
        client.updateMyDevicesEventData(new JSONObject());
        client.deleteMyDevicesCompany("u", "v", "id");
        client.deleteMyDevicesSensor("id");
        client.updateMyDevicesSensorDeviceId("old", "new", Collections.emptySet());
        client.updateMyDevicesSensors("u", "v", Collections.emptyList());
        client.deleteMyDevicesSensors("u", "v", Collections.emptyList());
        client.updateDeviceMyDevicesSensors("u", "v", Collections.emptyList());
        client.deleteDeviceMyDevicesSensors("u", "v", Collections.emptyList());
        client.updateMyDevicesSensorAttributes("u", "v", Collections.emptyList());
    }
}
