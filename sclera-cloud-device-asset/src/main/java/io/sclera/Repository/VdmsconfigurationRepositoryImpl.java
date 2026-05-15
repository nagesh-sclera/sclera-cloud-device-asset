package io.sclera.Repository;

import io.sclera.dto.touchscreen.settings.VdmsConfigurationDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

@Repository
@Primary
public class VdmsconfigurationRepositoryImpl implements VdmsconfigurationRepository {

    private static final Logger log = LoggerFactory.getLogger(VdmsconfigurationRepositoryImpl.class);

    @Override
    public VdmsConfigurationDTO getConfiguration() {
        log.warn("VdmsconfigurationRepositoryImpl.getConfiguration called (stub)");
        return null;
    }
}
