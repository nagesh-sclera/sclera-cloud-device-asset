package io.sclera.sockets;

import io.sclera.dto.DeviceDTO;
import io.sclera.dto.InterfaceDTO;
import io.sclera.dto.touchscreen.DeviceMonitorDTO;
import io.sclera.utils.StubLog;
import org.springframework.stereotype.Service;
import java.util.Set;

/** STUB: edge-only websocket broadcast */
@Service
public class SocketService {

    public void socketDeviceCount() {}
    public void sockerDeviceCountByDocker(String dockername, String assignee) {}
    public void socketAiCallLogHistoryUpdate(String id) { StubLog.warn("SocketService", "socketAiCallLogHistoryUpdate", "edge-local"); }
    public void socketAiCallLogOngoingHistoryUpdate(String id) { StubLog.warn("SocketService", "socketAiCallLogOngoingHistoryUpdate", "edge-local"); }
    public void socketDeviceStatus(DeviceMonitorDTO dto) { StubLog.warn("SocketService", "socketDeviceStatus", "edge-local"); }
    public void socketOnlineDevice(String deviceId) { StubLog.warn("SocketService", "socketOnlineDevice", "edge-local"); }
    public void socketOfflineDevice(String deviceId) { StubLog.warn("SocketService", "socketOfflineDevice", "edge-local"); }
    public void socketDeviceUpdate(Set<DeviceDTO> devices) {}
    public void updateDeviceInterfaceStatus(InterfaceDTO dto, String a, String b) {}

    public void socketMeasuringInstrumentSensorValueUpdate(String deviceId) {
        StubLog.warn("SocketService", "socketMeasuringInstrumentSensorValueUpdate", "edge-local");
    }
    public void socketDockerInterfaceStatus(String interfaceName, String interfaceStatus, Integer networkOrigin) {
        StubLog.warn("SocketService", "socketDockerInterfaceStatus", "edge-local");
    }
}
