package io.sclera.client;

import io.dapr.client.DaprClient;
import io.dapr.client.DaprHttp;
import io.dapr.client.domain.HttpExtension;
import io.sclera.dto.AlertProfileDTO;
import io.sclera.dto.DeviceAlertDTO;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import reactor.core.publisher.Mono;

import java.math.BigInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AlertClientTest {

    @Test
    void clientConstructsCleanly() {
        DaprClient dapr = mock(DaprClient.class);
        AlertClient client = new AlertClient(dapr);
        assertThat(client).isNotNull();
    }

    // ── happy-path: exact path and verb assertions ────────────────────────────

    @Test
    void sendDeviceConditionsAlertInfo_usesCorrectPathAndVerb() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(
                eq("sclera-alerts"),
                eq("alert/sendDeviceConditionsAlertInfo"),
                any(),
                any(HttpExtension.class)))
            .thenReturn(Mono.empty());

        AlertClient client = new AlertClient(dapr);
        client.sendDeviceConditionsAlertInfo(null, new AlertProfileDTO(), BigInteger.ZERO);

        ArgumentCaptor<HttpExtension> extCaptor = ArgumentCaptor.forClass(HttpExtension.class);
        verify(dapr).invokeMethod(
                eq("sclera-alerts"),
                eq("alert/sendDeviceConditionsAlertInfo"),
                any(),
                extCaptor.capture());
        assertThat(extCaptor.getValue().getMethod())
                .isEqualTo(DaprHttp.HttpMethods.GET);
    }

    @Test
    void sendSensorAlertInfo_usesCorrectPathAndVerb() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(
                eq("sclera-alerts"),
                eq("alert/sendSensorAlertInfo"),
                any(),
                any(HttpExtension.class)))
            .thenReturn(Mono.empty());

        AlertClient client = new AlertClient(dapr);
        client.sendSensorAlertInfo(new Object(), new AlertProfileDTO(), BigInteger.ZERO);

        ArgumentCaptor<HttpExtension> extCaptor = ArgumentCaptor.forClass(HttpExtension.class);
        verify(dapr).invokeMethod(
                eq("sclera-alerts"),
                eq("alert/sendSensorAlertInfo"),
                any(),
                extCaptor.capture());
        assertThat(extCaptor.getValue().getMethod())
                .isEqualTo(DaprHttp.HttpMethods.GET);
    }

    @Test
    void sendDownloadEmail_usesCorrectPathAndVerb() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(
                eq("sclera-alerts"),
                eq("alert/sendDownloadEmail"),
                any(),
                any(HttpExtension.class)))
            .thenReturn(Mono.empty());

        AlertClient client = new AlertClient(dapr);
        client.sendDownloadEmail(new com.alibaba.fastjson.JSONObject(), null, "type", "vdms-001");

        ArgumentCaptor<HttpExtension> extCaptor = ArgumentCaptor.forClass(HttpExtension.class);
        verify(dapr).invokeMethod(
                eq("sclera-alerts"),
                eq("alert/sendDownloadEmail"),
                any(),
                extCaptor.capture());
        assertThat(extCaptor.getValue().getMethod())
                .isEqualTo(DaprHttp.HttpMethods.GET);
    }

    // ── resilience: exception swallowing ─────────────────────────────────────

    @Test
    void clientSwallowsDaprException() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(any(String.class), any(String.class), any(), any(HttpExtension.class)))
            .thenReturn(Mono.error(new RuntimeException("sidecar down")));

        AlertClient client = new AlertClient(dapr);
        // all void methods must swallow exceptions silently
        client.sendDeviceConditionsAlertInfo(null, new AlertProfileDTO(), BigInteger.ZERO);
        client.sendSensorAlertInfo(new Object(), new AlertProfileDTO(), BigInteger.ZERO);
        client.sendDownloadEmail(new com.alibaba.fastjson.JSONObject(), null, "type", "vdms-001");
    }
}
