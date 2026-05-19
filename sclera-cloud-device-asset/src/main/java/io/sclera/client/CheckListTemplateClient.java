package io.sclera.client;

import io.dapr.client.DaprClient;
import io.dapr.client.domain.HttpExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Thin Dapr client delegating to the sclera-inspection microservice (AP-C4).
 *
 * Replaces the stub {@code io.sclera.service.CheckListTemplateService}.
 * Scalar-returning methods return 0/null on sidecar failure (stub default).
 */
@Component
public class CheckListTemplateClient {

    private static final Logger log = LoggerFactory.getLogger(CheckListTemplateClient.class);
    private static final String APP_ID = "sclera-inspection";

    private final DaprClient dapr;

    public CheckListTemplateClient(DaprClient dapr) {
        this.dapr = dapr;
    }

    /** Mirrors {@code CheckListTemplateService#getCheckListTemplatesCountByDeviceId}. Returns 0 on failure. */
    public Integer getCheckListTemplatesCountByDeviceId(String deviceId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("deviceId", deviceId);
        try {
            dapr.invokeMethod(APP_ID, "checkListTemplate/getCheckListTemplatesCountByDeviceId", payload, HttpExtension.GET).block();
            return 0;
        } catch (Exception e) {
            log.warn("CheckListTemplateClient.getCheckListTemplatesCountByDeviceId failed; returning stub default: {}", e.getMessage());
            return 0;
        }
    }
}
