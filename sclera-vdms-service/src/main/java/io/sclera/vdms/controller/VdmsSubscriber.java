package io.sclera.vdms.controller;

import io.sclera.vdms.repository.VdmsJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class VdmsSubscriber {

    private static final Logger log = LoggerFactory.getLogger(VdmsSubscriber.class);
    private final VdmsJpaRepository repo;

    public VdmsSubscriber(VdmsJpaRepository repo) {
        this.repo = repo;
    }

    @PostMapping("/vdms/update-property-details")
    public void updatePropertyDetails(@RequestBody Map<String, Object> cloudEvent) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) cloudEvent.get("data");
            if (data == null) return;
            String vdmsId = (String) data.get("id");
            String address = (String) data.get("address");
            if (vdmsId != null && address != null) {
                repo.updateAddress(vdmsId, address);
                log.info("Updated address for vdms {}", vdmsId);
            }
        } catch (Exception e) {
            log.error("Failed to handle update-property-details event", e);
        }
    }

    @PostMapping("/vdms/update-customer-org-id")
    public void updateCustomerOrgId(@RequestBody Map<String, Object> cloudEvent) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) cloudEvent.get("data");
            if (data == null) return;
            String vdmsId = (String) data.get("vdmsId");
            String customerOrgId = (String) data.get("customerOrgId");
            if (vdmsId != null && customerOrgId != null) {
                repo.updateCustomerOrgId(vdmsId, customerOrgId);
                log.info("Updated customerOrgId for vdms {}", vdmsId);
            }
        } catch (Exception e) {
            log.error("Failed to handle update-customer-org-id event", e);
        }
    }

    @PostMapping("/vdms/set-agent-permission")
    public void setAgentPermission(@RequestBody Map<String, Object> cloudEvent) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) cloudEvent.get("data");
            log.info("Received set-agent-permission event, data={}", data);
        } catch (Exception e) {
            log.error("Failed to handle set-agent-permission event", e);
        }
    }
}
