package io.sclera.Repository;

import io.sclera.dto.touchscreen.settings.VdmsConfigurationDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

/**
 * Stub implementation of {@link VdmsconfigurationRepository} for querying VDMS configuration data.
 */
@Repository
public class VdmsconfigurationRepositoryImpl implements VdmsconfigurationRepository {

    private static final Logger log = LoggerFactory.getLogger(VdmsconfigurationRepositoryImpl.class);

    /**
     * Returns the VDMS configuration; this stub logs a warning and returns {@code null}.
     *
     * @return the VDMS configuration, or {@code null}
     */
    @Override
    public VdmsConfigurationDTO getConfiguration() {
        log.warn("VdmsconfigurationRepositoryImpl.getConfiguration called (stub)");
        return null;
    }
}
