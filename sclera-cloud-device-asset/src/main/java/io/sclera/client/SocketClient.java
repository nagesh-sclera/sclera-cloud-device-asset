package io.sclera.client;

import io.dapr.client.DaprClient;
import io.dapr.client.domain.HttpExtension;
import io.sclera.dto.DeviceDTO;
import io.sclera.dto.InterfaceDTO;
import io.sclera.dto.touchscreen.DeviceMonitorDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Thin Dapr client delegating to sclera-edge.
 * Replaces {@code io.sclera.sockets.SocketService}.
 */
@Component
public class SocketClient {

    private static final Logger log = LoggerFactory.getLogger(SocketClient.class);
    private static final String APP_ID = "sclera-edge";

    private final DaprClient dapr;

    public SocketClient(DaprClient dapr) {
        this.dapr = dapr;
    }

    /** Pushes a device-count socket update via sclera-edge. Swallows exceptions with a WARN log. */
    public void socketDeviceCount() {
        try {
            dapr.invokeMethod(APP_ID, "socket/socketDeviceCount", null, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("SocketClient.socketDeviceCount failed; swallowing", e);
        }
    }

    /** Pushes a per-docker device-count socket update via sclera-edge. Swallows exceptions with a WARN log. */
    public void sockerDeviceCountByDocker(String dockername, String assignee) {
        try {
            dapr.invokeMethod(APP_ID, "socket/sockerDeviceCountByDocker", null, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("SocketClient.sockerDeviceCountByDocker failed; swallowing", e);
        }
    }

    /** Pushes an AI call-log history socket update for the given id via sclera-edge. Swallows exceptions with a WARN log. */
    public void socketAiCallLogHistoryUpdate(String id) {
        try {
            Map<String, String> p = new HashMap<>();
            p.put("id", id);
            dapr.invokeMethod(APP_ID, "socket/socketAiCallLogHistoryUpdate", p, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("SocketClient.socketAiCallLogHistoryUpdate failed; swallowing", e);
        }
    }

    /** Pushes an ongoing AI call-log history socket update for the given id via sclera-edge. Swallows exceptions with a WARN log. */
    public void socketAiCallLogOngoingHistoryUpdate(String id) {
        try {
            Map<String, String> p = new HashMap<>();
            p.put("id", id);
            dapr.invokeMethod(APP_ID, "socket/socketAiCallLogOngoingHistoryUpdate", p, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("SocketClient.socketAiCallLogOngoingHistoryUpdate failed; swallowing", e);
        }
    }

    /** Pushes a device-status socket update via sclera-edge. Swallows exceptions with a WARN log. */
    public void socketDeviceStatus(DeviceMonitorDTO dto) {
        try {
            dapr.invokeMethod(APP_ID, "socket/socketDeviceStatus", null, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("SocketClient.socketDeviceStatus failed; swallowing", e);
        }
    }

    /** Pushes an online-device socket notification for the given device via sclera-edge. Swallows exceptions with a WARN log. */
    public void socketOnlineDevice(String deviceId) {
        try {
            Map<String, String> p = new HashMap<>();
            p.put("deviceId", deviceId);
            dapr.invokeMethod(APP_ID, "socket/socketOnlineDevice", p, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("SocketClient.socketOnlineDevice failed; swallowing", e);
        }
    }

    /** Pushes an offline-device socket notification for the given device via sclera-edge. Swallows exceptions with a WARN log. */
    public void socketOfflineDevice(String deviceId) {
        try {
            Map<String, String> p = new HashMap<>();
            p.put("deviceId", deviceId);
            dapr.invokeMethod(APP_ID, "socket/socketOfflineDevice", p, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("SocketClient.socketOfflineDevice failed; swallowing", e);
        }
    }

    /** Pushes a device-update socket message via sclera-edge. Swallows exceptions with a WARN log. */
    public void socketDeviceUpdate(Set<DeviceDTO> devices) {
        try {
            dapr.invokeMethod(APP_ID, "socket/socketDeviceUpdate", null, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("SocketClient.socketDeviceUpdate failed; swallowing", e);
        }
    }

    /** Pushes a device interface-status socket update via sclera-edge. Swallows exceptions with a WARN log. */
    public void updateDeviceInterfaceStatus(InterfaceDTO dto, String a, String b) {
        try {
            dapr.invokeMethod(APP_ID, "socket/updateDeviceInterfaceStatus", null, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("SocketClient.updateDeviceInterfaceStatus failed; swallowing", e);
        }
    }

    /** Pushes a measuring-instrument sensor-value socket update for the given device via sclera-edge. Swallows exceptions with a WARN log. */
    public void socketMeasuringInstrumentSensorValueUpdate(String deviceId) {
        try {
            Map<String, String> p = new HashMap<>();
            p.put("deviceId", deviceId);
            dapr.invokeMethod(APP_ID, "socket/socketMeasuringInstrumentSensorValueUpdate", p, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("SocketClient.socketMeasuringInstrumentSensorValueUpdate failed; swallowing", e);
        }
    }

    /** Pushes a docker interface-status socket update via sclera-edge. Swallows exceptions with a WARN log. */
    public void socketDockerInterfaceStatus(String interfaceName, String interfaceStatus, Integer networkOrigin) {
        try {
            dapr.invokeMethod(APP_ID, "socket/socketDockerInterfaceStatus", null, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("SocketClient.socketDockerInterfaceStatus failed; swallowing", e);
        }
    }
}
