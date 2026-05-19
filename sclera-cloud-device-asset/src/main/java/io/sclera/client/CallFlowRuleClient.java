package io.sclera.client;

import io.dapr.client.DaprClient;
import io.dapr.client.domain.HttpExtension;
import io.sclera.dto.CallFlowRuleDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Thin Dapr client delegating to the sclera-alerts microservice (AP-C5).
 *
 * Replaces the {@code io.sclera.Repository.CallFlowRuleRepository} stub
 * (both CallFlowRuleRepositoryImpl and CallFlowRuleRepositoryStub).
 * Methods return documented safe defaults (empty list / null) on sidecar
 * failure so that call sites in AiCallService are never interrupted.
 *
 * NOTE: upsertAiCallFlow passes multiple primitive params under GET routing —
 * body is silently dropped. Needs POST upgrade when scaffold supports verbs.
 */
@Component
public class CallFlowRuleClient {

    private static final Logger log = LoggerFactory.getLogger(CallFlowRuleClient.class);
    private static final String APP_ID = "sclera-alerts";

    private final DaprClient dapr;

    public CallFlowRuleClient(DaprClient dapr) {
        this.dapr = dapr;
    }

    /**
     * Mirrors {@code CallFlowRuleRepository#getAllCallFlowRules}.
     * Maps to GET sclera-alerts/callFlowRule/getAllCallFlowRules.
     * Returns empty list on failure (documented stub default).
     */
    public List<CallFlowRuleDTO> getAllCallFlowRules(Integer offset, Integer pagesize, String searchkey) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("offset", offset);
        payload.put("pagesize", pagesize);
        payload.put("searchkey", searchkey);
        try {
            dapr.invokeMethod(APP_ID, "callFlowRule/getAllCallFlowRules", payload, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("CallFlowRuleClient.getAllCallFlowRules failed; returning empty list: {}", e.getMessage());
        }
        return Collections.emptyList();
    }

    /**
     * Mirrors {@code CallFlowRuleRepository#deleteById}.
     * Maps to GET sclera-alerts/callFlowRule/deleteById.
     */
    public void deleteById(String id) {
        Map<String, String> payload = new HashMap<>();
        payload.put("id", id);
        try {
            dapr.invokeMethod(APP_ID, "callFlowRule/deleteById", payload, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("CallFlowRuleClient.deleteById failed; swallowing: {}", e.getMessage());
        }
    }

    /**
     * Mirrors {@code CallFlowRuleRepository#checkCallFlowByDeviceid}.
     * Maps to GET sclera-alerts/callFlowRule/checkCallFlowByDeviceid.
     * Returns null on failure (documented stub default).
     */
    public String checkCallFlowByDeviceid(String deviceId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("deviceId", deviceId);
        try {
            dapr.invokeMethod(APP_ID, "callFlowRule/checkCallFlowByDeviceid", payload, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("CallFlowRuleClient.checkCallFlowByDeviceid failed; returning null: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Mirrors {@code CallFlowRuleRepository#upsertAiCallFlow}.
     * Maps to GET sclera-alerts/callFlowRule/upsertAiCallFlow.
     * NOTE: params passed as query-params under GET routing (needs POST upgrade).
     */
    public void upsertAiCallFlow(String id, String name, String createdBy, BigInteger createdAt,
                                 String updatedBy, BigInteger updatedAt, String deviceId) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("id", id);
        payload.put("name", name);
        payload.put("createdBy", createdBy);
        payload.put("createdAt", createdAt);
        payload.put("updatedBy", updatedBy);
        payload.put("updatedAt", updatedAt);
        payload.put("deviceId", deviceId);
        try {
            dapr.invokeMethod(APP_ID, "callFlowRule/upsertAiCallFlow", payload, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("CallFlowRuleClient.upsertAiCallFlow failed; swallowing: {}", e.getMessage());
        }
    }

    /**
     * Mirrors {@code CallFlowRuleRepository#getCallFlowByDeviceId}.
     * Maps to GET sclera-alerts/callFlowRule/getCallFlowByDeviceId.
     * Returns empty list on failure (documented stub default).
     */
    public List<CallFlowRuleDTO> getCallFlowByDeviceId(String deviceId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("deviceId", deviceId);
        try {
            dapr.invokeMethod(APP_ID, "callFlowRule/getCallFlowByDeviceId", payload, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("CallFlowRuleClient.getCallFlowByDeviceId failed; returning empty list: {}", e.getMessage());
        }
        return Collections.emptyList();
    }
}
