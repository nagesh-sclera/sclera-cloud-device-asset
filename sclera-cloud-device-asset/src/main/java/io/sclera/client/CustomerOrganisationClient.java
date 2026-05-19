package io.sclera.client;

import io.dapr.client.DaprClient;
import io.dapr.client.domain.HttpExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Thin Dapr client delegating to the sclera-identity microservice (CP-2).
 *
 * Replaces the no-op {@code io.sclera.service.touchscreen.CustomerOrganisationService} stub.
 * All methods are void: they invoke the remote endpoint and swallow any
 * exception (sidecar-down, network error) with a WARN log so that
 * call sites are never interrupted.
 *
 * Source stub was under {@code service/touchscreen/} — only one CustomerOrganisationService
 * exists in the repo (no top-level variant found).
 */
@Component
public class CustomerOrganisationClient {

    private static final Logger log = LoggerFactory.getLogger(CustomerOrganisationClient.class);
    private static final String APP_ID = "sclera-identity";

    private final DaprClient dapr;

    public CustomerOrganisationClient(DaprClient dapr) {
        this.dapr = dapr;
    }

    /**
     * Mirrors {@code CustomerOrganisationService#upsertCustomerByOrganisationIdSync}.
     * Maps to GET sclera-identity/customerOrganisation/upsertCustomerByOrganisationIdSync.
     */
    public void upsertCustomerByOrganisationIdSync(String orgId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("orgId", orgId);
        try {
            dapr.invokeMethod(APP_ID, "customerOrganisation/upsertCustomerByOrganisationIdSync", payload, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("CustomerOrganisationClient.upsertCustomerByOrganisationIdSync failed; swallowing: {}", e.getMessage());
        }
    }

    /**
     * Mirrors {@code CustomerOrganisationService#deleteCustomerOrgById}.
     * Maps to GET sclera-identity/customerOrganisation/deleteCustomerOrgById.
     */
    public void deleteCustomerOrgById(String orgId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("orgId", orgId);
        try {
            dapr.invokeMethod(APP_ID, "customerOrganisation/deleteCustomerOrgById", payload, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("CustomerOrganisationClient.deleteCustomerOrgById failed; swallowing: {}", e.getMessage());
        }
    }
}
