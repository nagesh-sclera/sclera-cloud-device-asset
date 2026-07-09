package io.sclera.service;

import io.sclera.dto.DeviceLifecycleHistoryDTO;

import java.util.Set;

/** Service contract for {@link io.sclera.service.DeviceLifecycleHistoryService}. */
public interface DeviceLifecycleHistoryService {

    void addDeviceHistory(String username, String vdmsid, DeviceLifecycleHistoryDTO deviceLifecycleHistoryDTO, String retireStatus);

    void updateOperationalStatus(String device_id, String operational_status, String retireStatus, String username, String vdmsid, String description);

    Set<DeviceLifecycleHistoryDTO> getDeviceHistory(String username, String vdmsid, String deviceId, Integer pageno, Integer pagesize);
}
