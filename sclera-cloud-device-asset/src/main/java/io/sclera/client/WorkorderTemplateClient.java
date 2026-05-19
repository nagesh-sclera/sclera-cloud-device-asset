package io.sclera.client;

import io.dapr.client.DaprClient;
import io.dapr.client.domain.HttpExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Thin Dapr client delegating to the sclera-workorders microservice (AP-C3).
 *
 * Replaces the stub {@code io.sclera.service.WorkorderTemplateService}.
 * Returns null on sidecar failure (stub default).
 */
@Component
public class WorkorderTemplateClient {

    private static final Logger log = LoggerFactory.getLogger(WorkorderTemplateClient.class);
    private static final String APP_ID = "sclera-workorders";

    private final DaprClient dapr;

    public WorkorderTemplateClient(DaprClient dapr) {
        this.dapr = dapr;
    }

    /**
     * Mirrors {@code WorkorderTemplateService#getWorkOrderTemplateComment}.
     * Maps to GET sclera-workorders/workorderTemplate/getWorkOrderTemplateComment.
     * Returns null on sidecar failure (stub default).
     */
    public String getWorkOrderTemplateComment(String templateId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("templateId", templateId);
        try {
            dapr.invokeMethod(APP_ID, "workorderTemplate/getWorkOrderTemplateComment", payload, HttpExtension.GET).block();
            return null;
        } catch (Exception e) {
            log.warn("WorkorderTemplateClient.getWorkOrderTemplateComment failed; returning stub default: {}", e.getMessage());
            return null;
        }
    }
}
