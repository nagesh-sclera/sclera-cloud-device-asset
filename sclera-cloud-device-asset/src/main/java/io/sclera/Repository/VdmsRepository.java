package io.sclera.Repository;

import io.sclera.dto.touchscreen.settings.VdmsDTO;
import org.springframework.stereotype.Repository;

/** STUB Repository: real impl deferred to Phase 2 */
@Repository
public interface VdmsRepository {
    /**
     * Returns the customer organisation identifier associated with the given VDMS.
     *
     * @param vdms_id the VDMS identifier
     * @return the customer organisation identifier
     */
    String getCustomerOrgIdByVdmsId(String vdms_id);

    /**
     * Returns the synchronization details used by the ADC.
     *
     * @return the VDMS sync details
     */
    VdmsDTO getSyncDetailsForADC();

    /**
     * Returns the VDMS identifier.
     *
     * @return the VDMS identifier
     */
    String getVDMSId();

    /**
     * Returns the VDMS details.
     *
     * @return the VDMS details
     */
    VdmsDTO getVdmsDetails();

    /**
     * Updates the customer organisation identifier of the given VDMS.
     *
     * @param vdmsId the VDMS identifier
     * @param customerOrgId the new customer organisation identifier
     */
    void updateCustomerOrgIdByVdmsId(String vdmsId, String customerOrgId);

    /**
     * Returns the VDMS password.
     *
     * @return the VDMS password
     */
    String getVDMSPassword();

    /**
     * Returns whether this VDMS is the master.
     *
     * @return a non-zero value if this VDMS is the master, otherwise zero
     */
    Integer getIsMaster();

    /**
     * Returns the single VDMS id iff exactly one VDMS row exists (backward-compat
     * fallback), otherwise {@code null}.
     */
    default String findSingleVdmsId() {
        return getVDMSId();
    }

    /** Returns the VDMS details for the given id. */
    default io.sclera.dto.touchscreen.settings.VdmsDTO getVdmsDetails(String vdmsId) {
        return getVdmsDetails();
    }

    /** Returns the VDMS password for the given id. */
    default String getVDMSPassword(String vdmsId) {
        return getVDMSPassword();
    }

    /** Returns the master flag for the given id. */
    default Integer getIsMaster(String vdmsId) {
        return getIsMaster();
    }
}
