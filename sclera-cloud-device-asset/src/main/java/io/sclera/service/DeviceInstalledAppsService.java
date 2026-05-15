package io.sclera.service;

import io.sclera.Repository.DeviceInstalledAppsRepository;
import io.sclera.dto.DeviceInstalledAppsDTO;
import io.sclera.models.DeviceInstalledApps;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DeviceInstalledAppsService {

    private final DeviceInstalledAppsRepository deviceInstalledAppsRepository;

    public DeviceInstalledAppsService(DeviceInstalledAppsRepository deviceInstalledAppsRepository) {
        this.deviceInstalledAppsRepository = deviceInstalledAppsRepository;
    }

    public List<DeviceInstalledApps> findByDeviceId(String deviceId) {
        return deviceInstalledAppsRepository.findByDeviceId(deviceId);
    }

    public List<DeviceInstalledAppsDTO> getInstalledAppDTOs(String deviceId) {
        return deviceInstalledAppsRepository.findByDeviceId(deviceId).stream()
                .map(app -> DeviceInstalledAppsDTO.builder()
                        .name(app.getName())
                        .version(app.getVersion())
                        .vendor(app.getPublisher()) // assuming publisher is vendor
                        .build())
                .collect(Collectors.toList());
    }

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