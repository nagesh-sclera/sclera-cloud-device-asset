package io.sclera.workorder.client;

import io.sclera.workorder.config.DaprProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Calls sclera-cloud-device-asset through the local Dapr sidecar.
 *
 * URL pattern:
 *   http://localhost:{daprPort}/v1.0/invoke/sclera-cloud-device-asset/method/{path}
 *
 * Dapr handles service discovery, retries, and mTLS.
 */
@Component
public class DeviceAssetClient {

    private static final Logger log = LoggerFactory.getLogger(DeviceAssetClient.class);

    private final RestClient daprRestClient;
    private final DaprProperties props;

    public DeviceAssetClient(RestClient daprRestClient, DaprProperties props) {
        this.daprRestClient = daprRestClient;
        this.props = props;
    }

    /**
     * Triggers ticket count and status recomputation on device-asset for the given device.
     * device-asset calls its own DeviceService.updateDeviceTicketCount / updateDeviceTicketStatus.
     * Fire-and-forget: logs a warning on failure but never throws.
     */
    public void syncTicketStats(String deviceId) {
        try {
            daprRestClient
                    .put()
                    .uri("/invoke/{appId}/method/api/v1/device-asset-service/internal/device/{deviceId}/ticket-sync",
                            props.getDeviceAssetAppId(), deviceId)
                    .retrieve()
                    .toBodilessEntity();
            log.debug("Ticket sync triggered for device={}", deviceId);
        } catch (RestClientException e) {
            log.warn("Could not sync ticket stats to device-asset for device={}: {}", deviceId, e.getMessage());
        }
    }
}
