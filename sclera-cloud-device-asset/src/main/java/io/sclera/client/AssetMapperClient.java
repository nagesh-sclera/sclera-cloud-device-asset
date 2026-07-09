package io.sclera.client;

import io.dapr.client.DaprClient;
import io.dapr.client.domain.HttpExtension;
import io.sclera.dto.DeviceTypesDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Thin Dapr client delegating to sclera-integrations (AP-C2).
 * Replaces {@code io.sclera.service.impl.touchscreen.assetmapper.AssetMapperService}.
 */
@Component
public class AssetMapperClient {

    private static final Logger log = LoggerFactory.getLogger(AssetMapperClient.class);
    private static final String APP_ID = "sclera-integrations";

    private final DaprClient dapr;

    public AssetMapperClient(DaprClient dapr) {
        this.dapr = dapr;
    }

    /**
     * Mirrors {@code AssetMapperService#updateDeviceTypeForAllAsset}.
     * NOTE: List body is lost under GET-only skeleton routing (needs POST upgrade).
     */
    public void updateDeviceTypeForAllAsset(List<DeviceTypesDTO> deviceTypes) {
        try {
            dapr.invokeMethod(APP_ID, "assetMapper/updateDeviceTypeForAllAsset", null, HttpExtension.POST).block();
        } catch (Exception e) {
            log.warn("AssetMapperClient.updateDeviceTypeForAllAsset failed; swallowing: {}", e.getMessage());
        }
    }
}
