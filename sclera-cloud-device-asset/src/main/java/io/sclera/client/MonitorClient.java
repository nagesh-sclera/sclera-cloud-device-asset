package io.sclera.client;

import io.dapr.client.DaprClient;
import io.dapr.client.domain.HttpExtension;
import io.sclera.dto.touchscreen.DeviceHistoryDTO;
import io.sclera.dto.touchscreen.DeviceMonitorDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Thin Dapr client delegating to sclera-edge.
 * Replaces {@code io.sclera.service.touchscreen.MonitorService}.
 */
@Component
public class MonitorClient {

    private static final Logger log = LoggerFactory.getLogger(MonitorClient.class);
    private static final String APP_ID = "sclera-edge";

    private final DaprClient dapr;

    public MonitorClient(DaprClient dapr) {
        this.dapr = dapr;
    }

    public void deviceUpsertbyId(String dockerName, List<DeviceMonitorDTO> deviceMonitors, String type) {
        try {
            dapr.invokeMethod(APP_ID, "monitor/deviceUpsertbyId", null, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("MonitorClient.deviceUpsertbyId failed; swallowing", e);
        }
    }

    public void insertDevicesHistory(String dockerName, List<DeviceHistoryDTO> devicesHistory) {
        try {
            dapr.invokeMethod(APP_ID, "monitor/insertDevicesHistory", null, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("MonitorClient.insertDevicesHistory failed; swallowing", e);
        }
    }
}
