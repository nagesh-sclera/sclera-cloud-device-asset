package io.sclera.Repository;

import io.sclera.dto.touchscreen.settings.VdmsConfigurationDTO;
import org.springframework.stereotype.Repository;

/** STUB Repository: real impl deferred to Phase 2 */
@Repository
public interface VdmsconfigurationRepository {

    /**
     * Returns the VDMS configuration.
     *
     * @return the VDMS configuration
     */
    VdmsConfigurationDTO getConfiguration();

    /**
     * Returns the VDMS network configuration for the given VDMS id.
     *
     * <p>The {@code vdms_configuration} table is not part of this service's schema
     * (it was a Bucket-D entity in the edge server), so this remains a safe stub that
     * delegates to the no-arg {@link #getConfiguration()}. The id-scoped signature
     * lets callers thread a VDMS id without relying on process-global state.
     *
     * @param vdmsId the VDMS id
     * @return the configuration, or {@code null}
     */
    default VdmsConfigurationDTO getConfiguration(String vdmsId) {
        return getConfiguration();
    }
}
