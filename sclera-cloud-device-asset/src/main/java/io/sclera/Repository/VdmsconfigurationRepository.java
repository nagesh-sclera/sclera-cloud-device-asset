package io.sclera.Repository;

import io.sclera.dto.touchscreen.settings.VdmsConfigurationDTO;
import org.springframework.stereotype.Repository;

/** STUB Repository: real impl deferred to Phase 2 */
@Repository
public interface VdmsconfigurationRepository {

    VdmsConfigurationDTO getConfiguration();
}
