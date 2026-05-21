package io.sclera.client;

import io.dapr.client.DaprClient;
import io.dapr.client.DaprHttp;
import io.dapr.client.domain.HttpExtension;
import io.sclera.dto.CallFlowRuleConditionDTO;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CallFlowRuleConditionClientTest {

    @Test
    void clientConstructsCleanly() {
        DaprClient dapr = mock(DaprClient.class);
        CallFlowRuleConditionClient client = new CallFlowRuleConditionClient(dapr);
        assertThat(client).isNotNull();
    }

    // ── happy-path: path and verb assertions ─────────────────────────────────

    @Test
    void upsertCallFlowRuleCondition_usesCorrectPathAndVerb() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(
                eq("sclera-alerts"),
                eq("callFlowRuleCondition/upsertCallFlowRuleCondition"),
                any(),
                any(HttpExtension.class)))
            .thenReturn(Mono.empty());

        CallFlowRuleConditionClient client = new CallFlowRuleConditionClient(dapr);
        client.upsertCallFlowRuleCondition("id", "criteria", "actionType", "actionValue", "message", "ruleId");

        ArgumentCaptor<HttpExtension> extCaptor = ArgumentCaptor.forClass(HttpExtension.class);
        verify(dapr).invokeMethod(
                eq("sclera-alerts"),
                eq("callFlowRuleCondition/upsertCallFlowRuleCondition"),
                any(),
                extCaptor.capture());
        assertThat(extCaptor.getValue().getMethod())
                .isEqualTo(DaprHttp.HttpMethods.POST);
    }

    @Test
    void getCallFlowRuleConditionsByCallFlowRuleId_usesCorrectPathAndVerb() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(
                eq("sclera-alerts"),
                eq("callFlowRuleCondition/getCallFlowRuleConditionsByCallFlowRuleId"),
                any(),
                any(HttpExtension.class)))
            .thenReturn(Mono.empty());

        CallFlowRuleConditionClient client = new CallFlowRuleConditionClient(dapr);
        List<CallFlowRuleConditionDTO> result = client.getCallFlowRuleConditionsByCallFlowRuleId("rule-001");

        ArgumentCaptor<HttpExtension> extCaptor = ArgumentCaptor.forClass(HttpExtension.class);
        verify(dapr).invokeMethod(
                eq("sclera-alerts"),
                eq("callFlowRuleCondition/getCallFlowRuleConditionsByCallFlowRuleId"),
                any(),
                extCaptor.capture());
        assertThat(extCaptor.getValue().getMethod())
                .isEqualTo(DaprHttp.HttpMethods.GET);
        assertThat(result).isEmpty();
    }

    @Test
    void getCallFlowRuleConditionByRuleIdAndCriteria_usesCorrectPathAndVerb() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(
                eq("sclera-alerts"),
                eq("callFlowRuleCondition/getCallFlowRuleConditionByRuleIdAndCriteria"),
                any(),
                any(HttpExtension.class)))
            .thenReturn(Mono.empty());

        CallFlowRuleConditionClient client = new CallFlowRuleConditionClient(dapr);
        List<CallFlowRuleConditionDTO> result = client.getCallFlowRuleConditionByRuleIdAndCriteria("rule-001", "criteria-a");

        ArgumentCaptor<HttpExtension> extCaptor = ArgumentCaptor.forClass(HttpExtension.class);
        verify(dapr).invokeMethod(
                eq("sclera-alerts"),
                eq("callFlowRuleCondition/getCallFlowRuleConditionByRuleIdAndCriteria"),
                any(),
                extCaptor.capture());
        assertThat(extCaptor.getValue().getMethod())
                .isEqualTo(DaprHttp.HttpMethods.GET);
        assertThat(result).isEmpty();
    }

    @Test
    void deleteCallFlowRuleConditionById_usesCorrectPathAndVerb() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(
                eq("sclera-alerts"),
                eq("callFlowRuleCondition/deleteCallFlowRuleConditionById"),
                any(),
                any(HttpExtension.class)))
            .thenReturn(Mono.empty());

        CallFlowRuleConditionClient client = new CallFlowRuleConditionClient(dapr);
        client.deleteCallFlowRuleConditionById(Arrays.asList("cond-001", "cond-002"));

        ArgumentCaptor<HttpExtension> extCaptor = ArgumentCaptor.forClass(HttpExtension.class);
        verify(dapr).invokeMethod(
                eq("sclera-alerts"),
                eq("callFlowRuleCondition/deleteCallFlowRuleConditionById"),
                any(),
                extCaptor.capture());
        assertThat(extCaptor.getValue().getMethod())
                .isEqualTo(DaprHttp.HttpMethods.POST);
    }

    // ── resilience: exception swallowing ─────────────────────────────────────

    @Test
    void clientReturnsDocumentedDefaultsOnDaprException() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(any(String.class), any(String.class), any(), any(HttpExtension.class)))
            .thenReturn(Mono.error(new RuntimeException("sidecar down")));

        CallFlowRuleConditionClient client = new CallFlowRuleConditionClient(dapr);
        client.upsertCallFlowRuleCondition("id", "criteria", "actionType", "actionValue", "message", "ruleId");
        List<CallFlowRuleConditionDTO> byRule = client.getCallFlowRuleConditionsByCallFlowRuleId("rule-001");
        List<CallFlowRuleConditionDTO> byCriteria = client.getCallFlowRuleConditionByRuleIdAndCriteria("rule-001", "criteria-a");
        client.deleteCallFlowRuleConditionById(Arrays.asList("cond-001"));

        assertThat(byRule).isEmpty();
        assertThat(byCriteria).isEmpty();
    }
}
