package io.sclera.client;

import io.dapr.client.DaprClient;
import io.dapr.client.domain.HttpExtension;
import io.sclera.dto.VendorDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Thin Dapr client delegating to the sclera-identity microservice (CP-2).
 *
 * Replaces the no-op {@code io.sclera.service.VendorAdminService} stub.
 * All methods are void: they invoke the remote endpoint and swallow any
 * exception (sidecar-down, network error) with a WARN log so that
 * call sites are never interrupted.
 *
 * NOTE: insertVendors passes a DTO body under GET routing — the body is
 * silently dropped. Needs POST upgrade when scaffold supports verbs.
 */
@Component
public class VendorAdminClient {

    private static final Logger log = LoggerFactory.getLogger(VendorAdminClient.class);
    private static final String APP_ID = "sclera-identity";

    private final DaprClient dapr;

    public VendorAdminClient(DaprClient dapr) {
        this.dapr = dapr;
    }

    /**
     * Mirrors {@code VendorAdminService#deleteVendorsByOrganisationId}.
     * Maps to GET sclera-identity/vendorAdmin/deleteVendorsByOrganisationId.
     */
    public void deleteVendorsByOrganisationId(String vendorOrgId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("vendorOrgId", vendorOrgId);
        try {
            dapr.invokeMethod(APP_ID, "vendorAdmin/deleteVendorsByOrganisationId", payload, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("VendorAdminClient.deleteVendorsByOrganisationId failed; swallowing: {}", e.getMessage());
        }
    }

    /**
     * Mirrors {@code VendorOrganisationRepository#addVendor}.
     * Maps to GET sclera-identity/vendorAdmin/addVendor.
     */
    public void addVendor(String vendorOrgId, String vdmsId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("vendorOrgId", vendorOrgId);
        payload.put("vdmsId", vdmsId);
        try {
            dapr.invokeMethod(APP_ID, "vendorAdmin/addVendor", payload, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("VendorAdminClient.addVendor failed; swallowing: {}", e.getMessage());
        }
    }

    /**
     * Mirrors {@code VendorAdminService#insertVendors}.
     * Maps to GET sclera-identity/vendorAdmin/insertVendors.
     * NOTE: DTO body is lost under GET-only skeleton routing (needs POST upgrade).
     */
    public void insertVendors(VendorDTO vendor) {
        try {
            dapr.invokeMethod(APP_ID, "vendorAdmin/insertVendors", vendor, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("VendorAdminClient.insertVendors failed; swallowing: {}", e.getMessage());
        }
    }
}
