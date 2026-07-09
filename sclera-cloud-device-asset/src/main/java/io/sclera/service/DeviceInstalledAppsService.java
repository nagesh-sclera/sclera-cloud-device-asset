package io.sclera.service;

import io.sclera.dto.DeviceInstalledAppsDTO;
import io.sclera.models.DeviceInstalledApps;

import java.util.List;

/** Service contract for {@link io.sclera.service.DeviceInstalledAppsService}. */
public interface DeviceInstalledAppsService {

    List<DeviceInstalledApps> findByDeviceId(String deviceId);

    List<DeviceInstalledAppsDTO> getInstalledAppDTOs(String deviceId);

    void updateDeviceIdBySerialNumber(String serialNumber, String deviceId);
}
