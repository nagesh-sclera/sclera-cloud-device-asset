package io.sclera.service;

import io.sclera.dto.VendorDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** STUB: non-AP-C1 */
@Service
public class VendorAdminService {
    private static final Logger log = LoggerFactory.getLogger(VendorAdminService.class);

    public void deleteVendorsByOrganisationId(String vendorOrgId) {
        log.warn("STUB: deleteVendorsByOrganisationId called");
    }

    public void insertVendors(VendorDTO vendor) {
        log.warn("STUB: insertVendors called");
    }
}
