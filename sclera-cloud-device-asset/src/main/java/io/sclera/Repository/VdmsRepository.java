package io.sclera.Repository;

import io.sclera.dto.touchscreen.settings.VdmsDTO;
import org.springframework.stereotype.Repository;

/** STUB Repository: real impl deferred to Phase 2 */
@Repository
public interface VdmsRepository {
    String getCustomerOrgIdByVdmsId(String vdms_id);
    VdmsDTO getSyncDetailsForADC();
    String getVDMSId();
    VdmsDTO getVdmsDetails();

    void updateCustomerOrgIdByVdmsId(String vdmsId, String customerOrgId);

    String getVDMSPassword();

    Integer getIsMaster();
}
