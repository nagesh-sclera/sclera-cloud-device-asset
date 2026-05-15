package io.sclera.service.touchscreen;

import io.sclera.dto.touchscreen.DeviceHistoryDTO;
import io.sclera.dto.touchscreen.DeviceMonitorDTO;
import io.sclera.utils.StubLog;
import org.springframework.stereotype.Service;

import java.util.List;

/** STUB: edge-only touchscreen monitor */
@Service
public class MonitorService {
    public void deviceUpsertbyId(String dockerName, List<DeviceMonitorDTO> deviceMonitors, String type) {
        StubLog.warn("MonitorService", "deviceUpsertbyId", "edge-local");
    }
    public void insertDevicesHistory(String dockerName, List<DeviceHistoryDTO> devicesHistory) {
        StubLog.warn("MonitorService", "insertDevicesHistory", "edge-local");
    }
}
