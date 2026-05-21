package io.sclera.client;

import io.dapr.client.DaprClient;
import io.dapr.client.DaprHttp;
import io.dapr.client.domain.HttpExtension;
import io.sclera.dto.CallFlowRuleDTO;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import reactor.core.publisher.Mono;

import java.math.BigInteger;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CallFlowRuleClientTest {

    @Test
    void clientConstructsCleanly() {
        DaprClient dapr = mock(DaprClient.class);
        CallFlowRuleClient client = new CallFlowRuleClient(dapr);
        assertThat(client).isNotNull();
    }

    // ── happy-path: path and verb assertions ─────────────────────────────────

    @Test
    void getAllCallFlowRules_usesCorrectPathAndVerb() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(
                eq("sclera-alerts"),
                eq("callFlowRule/getAllCallFlowRules"),
                any(),
                any(HttpExtension.class)))
            .thenReturn(Mono.empty());

        CallFlowRuleClient client = new CallFlowRuleClient(dapr);
        List<CallFlowRuleDTO> result = client.getAllCallFlowRules(0, 10, "key");

        ArgumentCaptor<HttpExtension> extCaptor = ArgumentCaptor.forClass(HttpExtension.class);
        verify(dapr).invokeMethod(
                eq("sclera-alerts"),
                eq("callFlowRule/getAllCallFlowRules"),
                any(),
                extCaptor.capture());
        assertThat(extCaptor.getValue().getMethod())
                .isEqualTo(DaprHttp.HttpMethods.GET);
        assertThat(result).isEmpty();
    }

    @Test
    void deleteById_usesCorrectPathAndVerb() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(
                eq("sclera-alerts"),
                eq("callFlowRule/deleteById"),
                any(),
                any(HttpExtension.class)))
            .thenReturn(Mono.empty());

        CallFlowRuleClient client = new CallFlowRuleClient(dapr);
        client.deleteById("rule-001");

        ArgumentCaptor<HttpExtension> extCaptor = ArgumentCaptor.forClass(HttpExtension.class);
        verify(dapr).invokeMethod(
                eq("sclera-alerts"),
                eq("callFlowRule/deleteById"),
                any(),
                extCaptor.capture());
        assertThat(extCaptor.getValue().getMethod())
                .isEqualTo(DaprHttp.HttpMethods.POST);
    }

    @Test
    void checkCallFlowByDeviceid_usesCorrectPathAndVerb() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(
                eq("sclera-alerts"),
                eq("callFlowRule/checkCallFlowByDeviceid"),
                any(),
                any(HttpExtension.class)))
            .thenReturn(Mono.empty());

        CallFlowRuleClient client = new CallFlowRuleClient(dapr);
        String result = client.checkCallFlowByDeviceid("device-001");

        ArgumentCaptor<HttpExtension> extCaptor = ArgumentCaptor.forClass(HttpExtension.class);
        verify(dapr).invokeMethod(
                eq("sclera-alerts"),
                eq("callFlowRule/checkCallFlowByDeviceid"),
                any(),
                extCaptor.capture());
        assertThat(extCaptor.getValue().getMethod())
                .isEqualTo(DaprHttp.HttpMethods.GET);
        assertThat(result).isNull();
    }

    @Test
    void upsertAiCallFlow_usesCorrectPathAndVerb() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(
                eq("sclera-alerts"),
                eq("callFlowRule/upsertAiCallFlow"),
                any(),
                any(HttpExtension.class)))
            .thenReturn(Mono.empty());

        CallFlowRuleClient client = new CallFlowRuleClient(dapr);
        client.upsertAiCallFlow("id", "name", "creator", BigInteger.ZERO, "updater", BigInteger.ONE, "device-001");

        ArgumentCaptor<HttpExtension> extCaptor = ArgumentCaptor.forClass(HttpExtension.class);
        verify(dapr).invokeMethod(
                eq("sclera-alerts"),
                eq("callFlowRule/upsertAiCallFlow"),
                any(),
                extCaptor.capture());
        assertThat(extCaptor.getValue().getMethod())
                .isEqualTo(DaprHttp.HttpMethods.POST);
    }

    @Test
    void getCallFlowByDeviceId_usesCorrectPathAndVerb() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(
                eq("sclera-alerts"),
                eq("callFlowRule/getCallFlowByDeviceId"),
                any(),
                any(HttpExtension.class)))
            .thenReturn(Mono.empty());

        CallFlowRuleClient client = new CallFlowRuleClient(dapr);
        List<CallFlowRuleDTO> result = client.getCallFlowByDeviceId("device-001");

        ArgumentCaptor<HttpExtension> extCaptor = ArgumentCaptor.forClass(HttpExtension.class);
        verify(dapr).invokeMethod(
                eq("sclera-alerts"),
                eq("callFlowRule/getCallFlowByDeviceId"),
                any(),
                extCaptor.capture());
        assertThat(extCaptor.getValue().getMethod())
                .isEqualTo(DaprHttp.HttpMethods.GET);
        assertThat(result).isEmpty();
    }

    // ── resilience: exception swallowing ─────────────────────────────────────

    @Test
    void clientReturnsDocumentedDefaultsOnDaprException() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(any(String.class), any(String.class), any(), any(HttpExtension.class)))
            .thenReturn(Mono.error(new RuntimeException("sidecar down")));

        CallFlowRuleClient client = new CallFlowRuleClient(dapr);
        List<CallFlowRuleDTO> all = client.getAllCallFlowRules(0, 10, "key");
        client.deleteById("rule-001");
        String check = client.checkCallFlowByDeviceid("device-001");
        client.upsertAiCallFlow("id", "name", "creator", BigInteger.ZERO, "updater", BigInteger.ONE, "device-001");
        List<CallFlowRuleDTO> byDevice = client.getCallFlowByDeviceId("device-001");

        assertThat(all).isEmpty();
        assertThat(check).isNull();
        assertThat(byDevice).isEmpty();
    }
}
