package io.sclera.client;

import io.dapr.client.DaprClient;
import io.dapr.client.domain.HttpExtension;
import io.sclera.Repository.VdmsconfigurationRepository;
import io.sclera.dto.touchscreen.settings.VdmsConfigurationDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * Thin Dapr client delegating to vdms-service.
 * Replaces {@code io.sclera.stubs.VdmsconfigurationRepositoryStub}.
 */
@Component
@Primary
public class VdmsconfigurationRepoClient implements VdmsconfigurationRepository {

    private static final Logger log = LoggerFactory.getLogger(VdmsconfigurationRepoClient.class);
    private static final String APP_ID = "vdms-service";

    private final DaprClient dapr;

    public VdmsconfigurationRepoClient(DaprClient dapr) {
        this.dapr = dapr;
    }

    /** Retrieves the VDMS configuration from vdms-service. Returns null on sidecar failure. */
    @Override
    public VdmsConfigurationDTO getConfiguration() {
        try {
            return dapr.invokeMethod(APP_ID, "vdmsconfiguration/getConfiguration", null, HttpExtension.GET, VdmsConfigurationDTO.class).block();
        } catch (Exception e) {
            log.warn("VdmsconfigurationRepoClient.getConfiguration failed; returning null", e);
        }
        return null;
    }
}
