package io.sclera.stubs;

import io.sclera.Repository.VdmsconfigurationRepository;
import io.sclera.dto.touchscreen.settings.VdmsConfigurationDTO;
import org.springframework.stereotype.Component;

@Component
public class VdmsconfigurationRepositoryStub implements VdmsconfigurationRepository {

    @Override
    public VdmsConfigurationDTO getConfiguration() {
        return null;
    }
}
