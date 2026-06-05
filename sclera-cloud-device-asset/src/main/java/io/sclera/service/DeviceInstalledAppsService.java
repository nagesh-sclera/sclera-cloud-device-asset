package io.sclera.service;

import io.sclera.Repository.DeviceInstalledAppsRepository;
import io.sclera.dto.DeviceInstalledAppsDTO;
import io.sclera.models.DeviceInstalledApps;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Provides read and maintenance operations for applications installed on devices.
 *
 * <p>Delegates persistence to {@link DeviceInstalledAppsRepository} and exposes
 * installed-app data both as {@link DeviceInstalledApps} entities and as projected
 * {@link DeviceInstalledAppsDTO} views.
 */
@Service
public class DeviceInstalledAppsService {

    private final DeviceInstalledAppsRepository deviceInstalledAppsRepository;

    public DeviceInstalledAppsService(DeviceInstalledAppsRepository deviceInstalledAppsRepository) {
        this.deviceInstalledAppsRepository = deviceInstalledAppsRepository;
    }

    /**
     * Returns the installed-application entities associated with the given device.
     *
     * @param deviceId the identifier of the device whose installed apps are retrieved
     * @return the list of {@link DeviceInstalledApps} for the device
     */
    public List<DeviceInstalledApps> findByDeviceId(String deviceId) {
        return deviceInstalledAppsRepository.findByDeviceId(deviceId);
    }

    /**
     * Returns the installed applications for the given device as DTO projections.
     *
     * @param deviceId the identifier of the device whose installed apps are retrieved
     * @return the list of {@link DeviceInstalledAppsDTO} carrying name, version, and vendor
     */
    public List<DeviceInstalledAppsDTO> getInstalledAppDTOs(String deviceId) {
        return deviceInstalledAppsRepository.findByDeviceId(deviceId).stream()
                .map(app -> DeviceInstalledAppsDTO.builder()
                        .name(app.getName())
                        .version(app.getVersion())
                        .vendor(app.getPublisher()) // assuming publisher is vendor
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Reassigns installed-app records matching the given serial number to the specified device.
     *
     * @param serialNumber the serial number of the records to update
     * @param deviceId the device identifier to assign to the matching records
     */
    public void updateDeviceIdBySerialNumber(String serialNumber, String deviceId) {
        deviceInstalledAppsRepository.updateDeviceIdBySerialNumber(serialNumber, deviceId);
    }
/*
    public List<DeviceInstalledAppsDTO> getAllUniqueInstalledAppDTOs() {
        return new ArrayList<>(deviceInstalledAppsRepository.findAll().stream()
                .map(app -> DeviceInstalledAppsDTO.builder()
                        .name(app.getName())
                        .version(app.getVersion())
                        .vendor(app.getPublisher()) // assuming publisher is vendor
                        .build())
                .collect(Collectors.toMap(
                        DeviceInstalledAppsDTO::getName, // key = name
                        dto -> dto,                     // value = dto itself
                        (existing, replacement) -> existing // keep first
                ))
                .values());
    }

 */

}