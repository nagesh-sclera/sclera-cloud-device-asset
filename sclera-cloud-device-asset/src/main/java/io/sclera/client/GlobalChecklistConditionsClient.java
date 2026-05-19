package io.sclera.client;

import io.dapr.client.DaprClient;
import io.dapr.client.domain.HttpExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Thin Dapr client delegating to the sclera-inspection microservice (AP-C4).
 *
 * Replaces the stub {@code io.sclera.service.GlobalChecklistConditionsService}.
 * Void methods swallow exceptions with a WARN log.
 *
 * NOTE: methods passing Set bodies use GET routing — bodies are
 * silently dropped. Needs POST upgrade when scaffold supports verbs.
 */
@Component
public class GlobalChecklistConditionsClient {

    private static final Logger log = LoggerFactory.getLogger(GlobalChecklistConditionsClient.class);
    private static final String APP_ID = "sclera-inspection";

    private final DaprClient dapr;

    public GlobalChecklistConditionsClient(DaprClient dapr) {
        this.dapr = dapr;
    }

    /**
     * Mirrors {@code GlobalChecklistConditionsService#updateGlobalChecklistConditionsDeviceAndIsRemoved}.
     * NOTE: Set body is lost under GET-only skeleton routing (needs POST upgrade).
     */
    public void updateGlobalChecklistConditionsDeviceAndIsRemoved(Set<String> ids) {
        try {
            dapr.invokeMethod(APP_ID, "globalChecklistConditions/updateGlobalChecklistConditionsDeviceAndIsRemoved", null, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("GlobalChecklistConditionsClient.updateGlobalChecklistConditionsDeviceAndIsRemoved failed; swallowing: {}", e.getMessage());
        }
    }

    /**
     * Mirrors {@code GlobalChecklistConditionsService#updateGlobalChecklistConditionsLocationAndIsRemoved}.
     * NOTE: Set body is lost under GET-only skeleton routing (needs POST upgrade).
     */
    public void updateGlobalChecklistConditionsLocationAndIsRemoved(Set<String> locationIds) {
        try {
            dapr.invokeMethod(APP_ID, "globalChecklistConditions/updateGlobalChecklistConditionsLocationAndIsRemoved", null, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("GlobalChecklistConditionsClient.updateGlobalChecklistConditionsLocationAndIsRemoved failed; swallowing: {}", e.getMessage());
        }
    }
}
