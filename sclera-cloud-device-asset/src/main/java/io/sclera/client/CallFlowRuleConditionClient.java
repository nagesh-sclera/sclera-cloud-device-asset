package io.sclera.client;

import io.dapr.client.DaprClient;
import io.dapr.client.domain.HttpExtension;
import io.sclera.dto.CallFlowRuleConditionDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Thin Dapr client delegating to the sclera-alerts microservice (AP-C5).
 *
 * Replaces the {@code io.sclera.Repository.CallFlowRuleConditionRepository} stub
 * (both CallFlowRuleConditionRepositoryImpl and CallFlowRuleConditionRepositoryStub).
 * Methods return documented safe defaults (empty list) on sidecar failure so
 * that call sites in AiCallService are never interrupted.
 *
 * NOTE: upsertCallFlowRuleCondition and deleteCallFlowRuleConditionById pass
 * data under GET routing — dropped silently. Needs POST upgrade.
 */
@Component
public class CallFlowRuleConditionClient {

    private static final Logger log = LoggerFactory.getLogger(CallFlowRuleConditionClient.class);
    private static final String APP_ID = "sclera-alerts";

    private final DaprClient dapr;

    public CallFlowRuleConditionClient(DaprClient dapr) {
        this.dapr = dapr;
    }

    /**
     * Mirrors {@code CallFlowRuleConditionRepository#upsertCallFlowRuleCondition}.
     * Maps to GET sclera-alerts/callFlowRuleCondition/upsertCallFlowRuleCondition.
     * NOTE: params dropped under GET routing (needs POST upgrade).
     */
    public void upsertCallFlowRuleCondition(String id, String criteria, String actionType,
                                            String actionValue, String actionMessage, String callFlowRuleId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("id", id);
        payload.put("criteria", criteria);
        payload.put("actionType", actionType);
        payload.put("actionValue", actionValue);
        payload.put("actionMessage", actionMessage);
        payload.put("callFlowRuleId", callFlowRuleId);
        try {
            dapr.invokeMethod(APP_ID, "callFlowRuleCondition/upsertCallFlowRuleCondition", payload, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("CallFlowRuleConditionClient.upsertCallFlowRuleCondition failed; swallowing: {}", e.getMessage());
        }
    }

    /**
     * Mirrors {@code CallFlowRuleConditionRepository#getCallFlowRuleConditionsByCallFlowRuleId}.
     * Maps to GET sclera-alerts/callFlowRuleCondition/getCallFlowRuleConditionsByCallFlowRuleId.
     * Returns empty list on failure (documented stub default).
     */
    public List<CallFlowRuleConditionDTO> getCallFlowRuleConditionsByCallFlowRuleId(String callFlowRuleId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("callFlowRuleId", callFlowRuleId);
        try {
            dapr.invokeMethod(APP_ID, "callFlowRuleCondition/getCallFlowRuleConditionsByCallFlowRuleId", payload, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("CallFlowRuleConditionClient.getCallFlowRuleConditionsByCallFlowRuleId failed; returning empty list: {}", e.getMessage());
        }
        return Collections.emptyList();
    }

    /**
     * Mirrors {@code CallFlowRuleConditionRepository#getCallFlowRuleConditionByRuleIdAndCriteria}.
     * Maps to GET sclera-alerts/callFlowRuleCondition/getCallFlowRuleConditionByRuleIdAndCriteria.
     * Returns empty list on failure (documented stub default).
     */
    public List<CallFlowRuleConditionDTO> getCallFlowRuleConditionByRuleIdAndCriteria(String ruleId, String criteria) {
        Map<String, String> payload = new HashMap<>();
        payload.put("ruleId", ruleId);
        payload.put("criteria", criteria);
        try {
            dapr.invokeMethod(APP_ID, "callFlowRuleCondition/getCallFlowRuleConditionByRuleIdAndCriteria", payload, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("CallFlowRuleConditionClient.getCallFlowRuleConditionByRuleIdAndCriteria failed; returning empty list: {}", e.getMessage());
        }
        return Collections.emptyList();
    }

    /**
     * Mirrors {@code CallFlowRuleConditionRepository#deleteCallFlowRuleConditionById}.
     * Maps to GET sclera-alerts/callFlowRuleCondition/deleteCallFlowRuleConditionById.
     * NOTE: List body dropped under GET routing (needs POST upgrade).
     */
    public void deleteCallFlowRuleConditionById(List<String> ids) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("ids", ids);
        try {
            dapr.invokeMethod(APP_ID, "callFlowRuleCondition/deleteCallFlowRuleConditionById", payload, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("CallFlowRuleConditionClient.deleteCallFlowRuleConditionById failed; swallowing: {}", e.getMessage());
        }
    }
}
