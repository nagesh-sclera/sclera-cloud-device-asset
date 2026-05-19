package io.sclera.client;

import io.dapr.client.DaprClient;
import io.dapr.client.domain.HttpExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Thin Dapr client delegating to sclera-integrations (AP-C2).
 * Replaces {@code io.sclera.service.IntegrationService}.
 */
@Component
public class IntegrationClient {

    private static final Logger log = LoggerFactory.getLogger(IntegrationClient.class);
    private static final String APP_ID = "sclera-integrations";

    private final DaprClient dapr;

    public IntegrationClient(DaprClient dapr) {
        this.dapr = dapr;
    }

    /** Mirrors {@code IntegrationService#updateCustomerOrgByIntegrationId}. */
    public void updateCustomerOrgByIntegrationId(String customerOrgId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("customerOrgId", customerOrgId);
        try {
            dapr.invokeMethod(APP_ID, "integration/updateCustomerOrgByIntegrationId", payload, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("IntegrationClient.updateCustomerOrgByIntegrationId failed; swallowing: {}", e.getMessage());
        }
    }
}
