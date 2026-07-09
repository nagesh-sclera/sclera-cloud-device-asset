package io.sclera.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.sclera.dto.DeviceDTO;
import io.sclera.dto.DeviceSpecificationDTO;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Set;

/** Service contract for the matching service class. */
public interface DeviceSpecificationService {

    String saveFullJson(JSONObject json, HttpServletRequest httpServletRequest, String assignee);

    String upsertDeltaJson(JSONObject json);

    DeviceSpecificationDTO getSpecDtoByDeviceId(String deviceId);

    JSONArray getSystemUpdatesArrayByDeviceId(String deviceId);

    void updateDeviceIdBySerialNumber(String serialNumber, String deviceId);

    void updateDeviceStatusToOnline(String deviceId);

    void updateDeviceStatusToOffline();

    void updateChildDevices(String deviceId, String vdmsid, String username);

    Set<DeviceDTO> getDevicesByIdList(Set<String> deviceIds);
}
