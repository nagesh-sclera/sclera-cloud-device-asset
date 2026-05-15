package io.sclera.service.touchscreen;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** STUB: edge-only touchscreen */
@Service
public class CustomerOrganisationService {
    private static final Logger log = LoggerFactory.getLogger(CustomerOrganisationService.class);

    public void upsertCustomerByOrganisationIdSync(String orgId) {
        log.warn("STUB: upsertCustomerByOrganisationIdSync called");
    }

    public void deleteCustomerOrgById(String orgId) {
        log.warn("STUB: deleteCustomerOrgById called");
    }
}
