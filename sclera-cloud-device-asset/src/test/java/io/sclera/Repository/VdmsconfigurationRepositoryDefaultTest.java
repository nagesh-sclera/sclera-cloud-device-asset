package io.sclera.Repository;

import io.sclera.dto.touchscreen.settings.VdmsConfigurationDTO;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VdmsconfigurationRepositoryDefaultTest {

    @Test
    void getConfigurationById_delegatesToNoArg() {
        VdmsConfigurationDTO stub = new VdmsConfigurationDTO();
        stub.setId("cfg-1");
        // Anonymous implementor overriding only the no-arg method; the id-scoped
        // default must delegate to it.
        VdmsconfigurationRepository repo = () -> stub;

        assertThat(repo.getConfiguration("VDMS760")).isSameAs(stub);
        assertThat(repo.getConfiguration("VDMS760").getId()).isEqualTo("cfg-1");
    }
}
