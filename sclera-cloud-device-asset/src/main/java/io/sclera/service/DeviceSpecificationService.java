package io.sclera.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.sclera.Repository.*;
import io.sclera.dto.DeviceDTO;
import io.sclera.dto.DeviceSpecificationDTO;
import io.sclera.dto.RemoteAgentServerDetailsDTO;
import io.sclera.models.*;
import io.sclera.utils.Utils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.*;
//import io.sclera.utils.DockerUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DeviceSpecificationService {
    private static final Logger log = LoggerFactory.getLogger(DeviceSpecificationService.class);

    private final DeviceSpecificationRepository deviceSpecificationRepository;

    private final DeviceInstalledAppsRepository deviceInstalledAppsRepository;

    private final DeviceRepository deviceRepository;

    private final DeviceService deviceService;
  
    private final RemoteAgentServerDetailsRepository remoteAgentServerDetailsRepository;

    private final APICallService apiCallService;

    private final DeviceNetworkSpecificationRepository deviceNetworkSpecificationRepository;

    private final Utils utils;

    private final ManagedSoftwareService managedSoftwareService;

    private final UserRepository userRepository;

    private final RemoteDesktopSessionRepository remoteDesktopSessionRepository;

    public DeviceSpecificationService(DeviceSpecificationRepository deviceSpecificationRepository, DeviceInstalledAppsRepository deviceInstalledAppsRepository,
                                      DeviceRepository deviceRepository, DeviceService deviceService, RemoteAgentServerDetailsRepository remoteAgentServerDetailsRepository, APICallService apiCallService, DeviceNetworkSpecificationRepository deviceNetworkSpecificationRepository, Utils utils, ManagedSoftwareService managedSoftwareService, UserRepository userRepository, RemoteDesktopSessionRepository remoteDesktopSessionRepository) {

        this.deviceSpecificationRepository = deviceSpecificationRepository;
        this.deviceInstalledAppsRepository = deviceInstalledAppsRepository;
        this.deviceRepository = deviceRepository;
        this.deviceService = deviceService;
        this.remoteAgentServerDetailsRepository = remoteAgentServerDetailsRepository;
        this.apiCallService = apiCallService;
        this.deviceNetworkSpecificationRepository = deviceNetworkSpecificationRepository;
        this.utils = utils;
        this.managedSoftwareService = managedSoftwareService;
        this.userRepository = userRepository;
        this.remoteDesktopSessionRepository = remoteDesktopSessionRepository;
    }

    private String getObjectJson(JSONObject parent, String key) {
        return (parent.containsKey(key) && parent.getJSONObject(key) != null) ? parent.getJSONObject(key).toJSONString() : "{}";
    }

    private String getArrayJson(JSONObject parent, String key) {
        return (parent.containsKey(key) && parent.getJSONArray(key) != null) ? parent.getJSONArray(key).toJSONString() : "[]";
    }

    private boolean isValidObject(JSONObject parent, String key) {
        return parent.containsKey(key) && parent.getJSONObject(key) != null;
    }

    private boolean isValidArray(JSONObject parent, String key) {
        return parent.containsKey(key) && parent.getJSONArray(key) != null;
    }
    private JSONObject getJsonObjectJson(JSONObject parent, String key) {
        if (parent.containsKey(key) && parent.getJSONObject(key) != null) {
            return parent.getJSONObject(key);
        }
        return new JSONObject();
    }

    private String safeGetString(JSONObject obj, String key) {
        return obj != null && obj.containsKey(key) ? obj.getString(key) : null;
    }

    @Transactional
    public String saveFullJson(JSONObject json, HttpServletRequest httpServletRequest, String assignee) {

        if (json == null || !json.containsKey("systemInfo")) {
            log.warn("Received null or invalid JSON payload: {}", json);
            return null;
        }
        try {
            String id = safeGetString(json, "id"); // serial number
            String ipAddress = safeGetString(json, "deviceIp");
            String deviceName = safeGetString(json, "deviceName");
            String vdmsId = safeGetString(json, "vdmsId");
            String macAddress = safeGetString(json, "macAddress");
            Long createdAt = System.currentTimeMillis();
            String osType = safeGetString(json, "osType");
            String username = safeGetString(json, "username");
            String userUUID = safeGetString(json, "userUUID");
            String email = safeGetString(json, "email");
            String accountType = safeGetString(json, "accountType");

            JSONObject systemInfo = json.getJSONObject("systemInfo");
            String model = safeGetString(systemInfo, "model");
            log.info("Processing system info for device with MAC address and serial number : {}, {}", macAddress, id);

            JSONObject biosJson = getJsonObjectJson(systemInfo, "bios");
            String manufacturer = "";
            if(biosJson != null && biosJson.containsKey("manufacturer")){
                manufacturer = biosJson.getString("manufacturer");
            }

            Device device = deviceRepository.findDeviceIdBySerialNumber(id);
            String deviceId = (device != null) ? device.getId() : null;

            String masterUserEmail = userRepository.getMasterUserEmail();

            if (deviceId == null) {
                log.info("Device not found by Serial number. Sending data to inventory.");
                // Send data to inventory
                JSONObject object = new JSONObject();
                object.put("source","agent");
                object.put("name", deviceName);
//                JSONObject biosJson = getJsonObjectJson(systemInfo, "bios");
                if(biosJson != null && biosJson.containsKey("serialNumber")){
                    object.put("serial_number",biosJson.getString("serialNumber"));
                }
                object.put("mac_address", macAddress);
                object.put("ip_address", ipAddress);
                object.put("assignee",masterUserEmail);
                object.put("vdms_id", vdmsId);
                object.put("model",model);

                apiCallService.sendAgentDataToInventory(object);

                log.info("Agent Data sent to inventory");

            }

            // Updating device info (IP, Model, MAC, Device Name) on network change
            // Also updating status to online if offline
            if (device != null) {
                boolean updated = false;

                // Update IP if changed
                String existingIp = device.getIp_address();
                if (existingIp == null || !existingIp.equals(ipAddress)) {
                    device.setIp_address(ipAddress);
                    updated = true;
                    log.info("Updated IP address for device (Serial Number: {}): {} → {}", id, existingIp, ipAddress);
                }

                // Update Model if changed
                String existingModel = device.getUser_data_model();
                if (model != null && !model.equals(existingModel)) {
                    device.setUser_data_model(model);
                    updated = true;
                    log.info("Updated Model for device (Serial Number: {}): {} → {}", id, existingModel, model);
                }

                // Update MAC Address if changed
                String existingMac = device.getMac_address();
                if (macAddress != null && !macAddress.equals(existingMac)) {
                    device.setMac_address(macAddress);
                    updated = true;
                    log.info("Updated MAC Address for device (Serial Number: {}): {} → {}", id, existingMac, macAddress);
                }

                // Update Device Name if changed
                String existingName = device.getUser_data_name();
                if (deviceName != null && !deviceName.equals(existingName)) {
                    device.setUser_data_name(deviceName);
                    updated = true;
                    log.info("Updated Device Name for device (Serial Number: {}): {} → {}", id, existingName, deviceName);
                }

                // Update Vendor if changed
                String existingVendor = device.getUser_data_vendor();
                if (manufacturer != null && !manufacturer.equals(existingVendor)) {
                    device.setUser_data_vendor(manufacturer);
                    updated = true;
                    log.info("Updated Vendor for device (Serial Number: {}): {} → {}", id, existingVendor, manufacturer);
                }


                // If any updates happened, set timestamp + save
                if (updated) {
                    device.setUpdated_timestamp(BigInteger.valueOf(System.currentTimeMillis()));
                    deviceRepository.save(device);
                }

                // Always set device online if it was offline
                this.updateDeviceStatusToOnline(deviceId);
            }

            DeviceSpecification existingSpec = deviceSpecificationRepository.findById(id).orElse(null);

            DeviceSpecification spec = DeviceSpecification.builder()
                    .id(id)
                    .createdAt(existingSpec != null ? existingSpec.getCreatedAt() : System.currentTimeMillis())
                    .updatedAt(System.currentTimeMillis())
                    .username(username)
                    .email(email)
                    .accountType(accountType)
                    .userUUID(userUUID)
                    .deviceName(deviceName)
                    .model(model)
                    .osType(osType)
                    .locationInfo(getObjectJson(systemInfo, "location"))
                    .osInfo(getObjectJson(systemInfo, "osInfo"))
                    .cpuInfo(getObjectJson(systemInfo, "cpuInfo"))
                    .diskDrives(getArrayJson(systemInfo, "diskDrives"))
                    .physicalDisks(getArrayJson(systemInfo, "physicalDisks"))
                    .bios(getObjectJson(systemInfo, "bios"))
                    .ramInfo(getArrayJson(systemInfo, "ramInfo"))
                    .videoCards(getArrayJson(systemInfo, "videoCards"))
                    .soundDevices(getArrayJson(systemInfo, "soundDevices"))
                    .batteryInfo(getObjectJson(systemInfo, "batteryInfo"))
                    .processes(getArrayJson(systemInfo, "processes"))
                    .systemUpdates(getArrayJson(systemInfo, "systemUpdates"))
                    .childDevices(getObjectJson(systemInfo,"childDevices"))
                    .build();

            deviceSpecificationRepository.save(spec);
            log.info("DeviceSpecification saved for serial number: {}", id);

            if (deviceId != null && !deviceId.trim().isEmpty()) {
                // update child devices
                updateChildDevices(deviceId, vdmsId, username);
            }

            DeviceNetworkSpecification networkSpec = DeviceNetworkSpecification.builder()
                    .id(id) // Use the same ID (e.g. Serial Number)
                    .networkInterfaces(getArrayJson(systemInfo, "networkInterfaces"))
                    .networkSettings(getArrayJson(systemInfo, "networkSettings"))
                    .networkPorts(getArrayJson(systemInfo, "networkPorts"))
                    .networkProcesses(getArrayJson(systemInfo,"networkProcesses"))
                    .build(); // device field is null for now

            deviceNetworkSpecificationRepository.save(networkSpec);
            log.info("DeviceNetworkSpecification saved for Serial Number: {}", id);

            // Save installed apps in bulk
            JSONArray apps = systemInfo.getJSONArray("installedApps");
            List<DeviceInstalledApps> installedAppList = new ArrayList<>();
            if (apps != null) {
                for (int i = 0; i < apps.size(); i++) {
                    JSONObject app = apps.getJSONObject(i);
                    String name = app.getString("name");
                    String publisher = app.getString("publisher");
                    String version = app.getString("version");

                    // check if already exists for this device
                    boolean exists = deviceInstalledAppsRepository.existsByDeviceIdAndName(deviceId, name);
                    if (exists) {
                        log.debug("Skipping app '{}' for deviceId {} (already exists)", name, deviceId);
                        continue;
                    }

                    // call managed software service first
                    String managedSoftwareId = managedSoftwareService.insertManagedSoftware(name, publisher);

                    DeviceInstalledApps entity = DeviceInstalledApps.builder()
                            .id(UUID.randomUUID().toString())
                            .createdAt(createdAt)
                            .name(name)
                            .publisher(publisher)
                            .version(version)
                            .deviceId(deviceId)
                            .deviceSpecificationId(id)
                            .managedSoftwareId(managedSoftwareId)
                            .build();

                    installedAppList.add(entity);
                }
            }

            deviceInstalledAppsRepository.saveAll(installedAppList);
            log.info("Installed apps saved (count: {}) for deviceId: {} and serial number: {}", installedAppList.size(), deviceId, id);

            return deviceId;
        } catch (Exception e) {
            log.error("Error while processing and saving full JSON data", e);
            return null;
        }

    }

    @Transactional
    public String upsertDeltaJson(JSONObject json) {
        if (json == null || !json.containsKey("systemInfo")) {
            return null;
        }
        String vdmsId = json.getString("vdmsId");
        String id = json.getString("id"); // Serial Number
        String eventType = json.getString("eventType");
        JSONObject systemInfo = json.getJSONObject("systemInfo");
        log.info("Processing Delta system info for device with serial number: {}", id);
        log.info("Processing Delta system info {}", systemInfo);
        Device device = deviceRepository.findDeviceIdBySerialNumber(id);
        String deviceId = (device != null) ? device.getId() : null;

        if (deviceId == null) {
            log.info("Device is null for delta changes");
            return null;
        }

        //update device status if offline
        this.updateDeviceStatusToOnline(deviceId);

        DeviceSpecification spec = deviceSpecificationRepository.findById(id).orElse(null);
        if (spec == null) {
            return null;
        }

        // Update fields only if present and not null
        if (isValidArray(systemInfo, "processes")) {
            spec.setProcesses(systemInfo.getJSONArray("processes").toJSONString());
        }

        if (isValidObject(systemInfo, "childDevices")) {
            spec.setChildDevices(systemInfo.getJSONObject("childDevices").toJSONString());
        }
        DeviceNetworkSpecification networkSpec = deviceNetworkSpecificationRepository.findById(id).orElse(null);

        if (!"HighCPU".equalsIgnoreCase(eventType) && networkSpec != null) {
            if (isValidArray(systemInfo, "networkInterfaces")) {
                networkSpec.setNetworkInterfaces(systemInfo.getJSONArray("networkInterfaces").toJSONString());
            }
            if (isValidArray(systemInfo, "networkSettings")) {
                networkSpec.setNetworkSettings(systemInfo.getJSONArray("networkSettings").toJSONString());
            }
            if (isValidObject(systemInfo, "batteryInfo")) {
                spec.setBatteryInfo(systemInfo.getJSONObject("batteryInfo").toJSONString());
            }
            if (isValidArray(systemInfo, "networkPorts")) {
                networkSpec.setNetworkPorts(systemInfo.getJSONArray("networkPorts").toJSONString());
            }
            if (isValidArray(systemInfo, "networkProcesses")) {
                networkSpec.setNetworkProcesses(systemInfo.getJSONArray("networkProcesses").toJSONString());
            }
            log.info("Delta changes to update: {}", networkSpec);
            deviceNetworkSpecificationRepository.save(networkSpec);
        }
        spec.setUpdatedAt(System.currentTimeMillis());
        deviceSpecificationRepository.save(spec);
        if (!deviceId.trim().isEmpty()) {
            // update child devices
            String username = spec.getUsername();
            updateChildDevices(deviceId, vdmsId,username);
        }

//        RemoteAgentServerDetails remoteAgentServerDetails = remoteAgentServerDetailsRepository.findAll().stream().findFirst().orElse(null);
//        if (remoteAgentServerDetails == null) return null;

//        return mapAgentDetailsToDTO(remoteAgentServerDetails, deviceId);
        return "success";
    }

//    private RemoteAgentServerDetailsDTO mapAgentDetailsToDTO(RemoteAgentServerDetails server, String deviceId) {
//
//        List<String> remoteSupportUrls = remoteDesktopSessionRepository.findAllUrlsByDeviceId(deviceId);
//
//        List<String> customDomains = remoteSupportUrls.stream()
//                .map(url -> url.replaceFirst("https?://", "").split("[:/]")[0])
//                .collect(Collectors.toList());
//
//        boolean enabled = !customDomains.isEmpty();
//
//        String password = enabled ? remoteDesktopSessionRepository.getPasswordByDeviceId(deviceId) : null;
//
//        return RemoteAgentServerDetailsDTO.builder()
//                .id(server.getId())
//                .name(server.getName())
//                .exposePort(server.getExposePort())
//                .gatewayPort(server.getGatewayPort())
//                .tunnelTcpPort(server.getTunnelTcpPort())
//                .tunnelKcpPort(server.getTunnelKcpPort())
//                .tunnelQuicPort(server.getTunnelQuicPort())
//                .vhttpPort(server.getVhttpPort())
//                .webserverPort(server.getWebserverPort())
//                .publicIp(server.getPublicIp())
//                .privateIp(server.getPrivateIp())
//                .publicDns(server.getPublicDns())
//                .privateDns(server.getPrivateDns())
//                .location(server.getLocation())
//                .latitude(server.getLatitude())
//                .longitude(server.getLongitude())
//                .timezone(server.getTimezone())
//                .serverType(server.getServerType())
//                .token(server.getToken())
//                .password(password)
//                .customDomain(enabled ? customDomains.toString() : null)
//                .build();
//    }


    public DeviceSpecificationDTO getSpecDtoByDeviceId(String deviceId) {
        DeviceSpecification spec = deviceSpecificationRepository.findByDeviceId(deviceId);

        if (spec == null) return null;

        DeviceNetworkSpecification networkSpec = deviceNetworkSpecificationRepository.findByDeviceId(deviceId);

        String username = parseObjectSafe(spec.getOsInfo()).getString("username");
        String accountType = parseObjectSafe(spec.getOsInfo()).getString("accountType");
        String emailId = parseObjectSafe(spec.getOsInfo()).getString("emailId");

        String deviceName = deviceRepository.getDeviceNameById(deviceId);
        String model = deviceRepository.getModelById(deviceId);

        return DeviceSpecificationDTO.builder()
                .userName(username)
                .accountType(accountType)
                .emailId(emailId)
                .deviceName(deviceName)
                .model(model)
                .deviceId(deviceId)
                .cpuInfo(parseObjectSafe(spec.getCpuInfo()))
                .ramInfo(parseArraySafe(spec.getRamInfo()))
                .osInfo(parseObjectSafe(spec.getOsInfo()))
                .locationInfo(parseObjectSafe(spec.getLocationInfo()))
                .batteryInfo(parseObjectSafe(spec.getBatteryInfo()))
                .videoCards(parseArraySafe(spec.getVideoCards()))
                .soundDevices(parseArraySafe(spec.getSoundDevices()))
                .bios(parseObjectSafe(spec.getBios()))
                .diskDrives(parseArraySafe(spec.getDiskDrives()))
                // Fetch network details from networkSpec, if it exists
                .networkPorts(parseArraySafe(networkSpec != null ? networkSpec.getNetworkPorts() : null))
                .networkSettings(parseArraySafe(networkSpec != null ? networkSpec.getNetworkSettings() : null))
                .networkInterfaces(parseArraySafe(networkSpec != null ? networkSpec.getNetworkInterfaces() : null))
                .networkProcesses(parseArraySafe(networkSpec != null ? networkSpec.getNetworkProcesses() : null))
                .processes(parseArraySafe(spec.getProcesses()))
                .build();
    }


    private JSONObject parseObjectSafe(String json) {
        try {
            return JSON.parseObject(json);
        } catch (Exception e) {
            return new JSONObject();
        }
    }

    private JSONArray parseArraySafe(String json) {
        try {
            return JSON.parseArray(json);
        } catch (Exception e) {
            return new JSONArray();
        }
    }

    public JSONArray getSystemUpdatesArrayByDeviceId(String deviceId) {
        DeviceSpecification spec = deviceSpecificationRepository.findByDeviceId(deviceId);
        if (spec != null && spec.getSystemUpdates() != null) {
            return JSON.parseArray(spec.getSystemUpdates());
        }
        return new JSONArray();
    }

    public void updateDeviceIdBySerialNumber(String serialNumber, String deviceId) {
        deviceSpecificationRepository.updateDeviceIdBySerialNumber(serialNumber, deviceId);
        deviceNetworkSpecificationRepository.updateDeviceIdBySerialNumber(serialNumber, deviceId);
    }

    public void updateDeviceStatusToOnline(String deviceId) {
        Device device = deviceRepository.findById(deviceId).orElse(null);
        if (device != null && device.getStatus() == 0) {
            device.setStatus(1);
            device.setLast_seen_on(BigInteger.valueOf(System.currentTimeMillis()));
            deviceRepository.save(device);
        }
    }

    public void updateDeviceStatusToOffline() {
        List<Device> onlineDevices = deviceRepository.findByStatus(1); // status == 1 → online

        long now = System.currentTimeMillis();

        for (Device device : onlineDevices) {
            String deviceId = device.getId();
            DeviceSpecification spec = deviceSpecificationRepository.findByDeviceId(deviceId);

            if (spec != null && spec.getUpdatedAt() != null) {
                long lastUpdated = spec.getUpdatedAt();
                long timeDiffSeconds = (now - lastUpdated) / 1000;

                if (timeDiffSeconds > 90) {
                    device.setStatus(0);
                    device.setLast_seen_on(BigInteger.valueOf(lastUpdated));
                    deviceRepository.save(device);

                    log.info("Device {} set to OFFLINE (inactive for {} seconds)", deviceId, timeDiffSeconds);
                }
            }
        }
    }

    public void updateChildDevices(String deviceId, String vdmsid, String username) {

        String childDevices = deviceSpecificationRepository.getChildDeviceByDeviceId(deviceId);
        if (childDevices == null) {
            log.info("childDevices spec is null");
            return;
        }

        JSONObject childDevicesJson = parseObjectSafe(childDevices);
        log.info(String.valueOf(childDevicesJson));
        Set<String> createdChildIds = new HashSet<>();
        for (String category : childDevicesJson.keySet()) {
            JSONArray devicesArray = childDevicesJson.getJSONArray(category);
            if (devicesArray == null) continue;

            for (int i = 0; i < devicesArray.size(); i++) {
                JSONObject deviceObj = devicesArray.getJSONObject(i);
                String name = null;
                if (deviceObj.containsKey("displayName")) {
                    log.info("Checking display name");
                    name = deviceObj.getString("displayName");

                } else if (deviceObj.containsKey("deviceName")) {
                    log.info("Checking device name");
                    name = deviceObj.getString("deviceName");
                }

                log.info("Name of the child device {} is {}", i, name);
                if (name != null && !name.trim().isEmpty()) {
                    Optional<Device> existingDevice = deviceRepository.findByDisplayNameAndSubsystemParentId(name, deviceId);

                    if (existingDevice.isPresent()) {
                        log.info("Device present in as sub system");
                    } else {
                        log.info("Adding child device {}", name);
                        DeviceDTO virtualDevice = new DeviceDTO();
                        virtualDevice.setUser_data_name(name);
                        virtualDevice.setSubsystem_parent_id(deviceId);
                        virtualDevice.setCreated_timestamp(BigInteger.valueOf(System.currentTimeMillis()));
                        virtualDevice.setDocker_name("agent_onboarded_assets");
                        virtualDevice.setAsset_group("it_asset");
                        virtualDevice.setVirtual_device_type(2);

                        String device_id = vdmsid + "_" + virtualDevice.getDocker_name() + "_" + name + "_" + System.currentTimeMillis();
                        String final_device_id = utils.replaceSpecialCharactersWithUnderscore(device_id);

                        deviceRepository.addVirtualDeviceFromInventory(final_device_id,
                                virtualDevice.getUser_data_name(), virtualDevice.getMonitor(), virtualDevice.getDocker_name(), vdmsid,
                                virtualDevice.getWarranty(), virtualDevice.getVirtual_device_type(),
                                virtualDevice.getSerial_number(),
                                virtualDevice.getAsset_match_status(), virtualDevice.getCreated_timestamp(),
                                "agent", virtualDevice.getAsset_group(), virtualDevice.getAssigned_user_email(), virtualDevice.getMac_address(),
                                virtualDevice.getIp_address(), virtualDevice.getStatus(), virtualDevice.getLast_seen_on(),
                                virtualDevice.getUser_data_model(), virtualDevice.getSubsystem_parent_id());
                        createdChildIds.add(final_device_id);
                        deviceService.updateParentSubsystemCount(deviceId);
                    }
                }
            }
        }
        if (!createdChildIds.isEmpty()) {
            Set<DeviceDTO> childDtos = this.getDevicesByIdList(createdChildIds);
            deviceService.updateVirtualDeviceOnboardStatus(childDtos, username);
        }
    }

    public Set<DeviceDTO> getDevicesByIdList(Set<String> deviceIds) {
        if (deviceIds == null || deviceIds.isEmpty()) {
            return new HashSet<>();
        }
        return deviceRepository.getDevicesByIdList(deviceIds);
    }

}