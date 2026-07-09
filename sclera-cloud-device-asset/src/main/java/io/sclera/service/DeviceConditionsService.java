package io.sclera.service;

import io.sclera.dto.DeviceConditionsDTO;
import io.sclera.dto.ShareConditionsDTO;

import java.math.BigInteger;
import java.util.Set;

/** Service contract for {@link io.sclera.service.DeviceConditionsService}. */
public interface DeviceConditionsService {

    void upsertDeviceConditions(String username, String vdmsid, String dockername, Set<DeviceConditionsDTO> device_conditions);

    Set<DeviceConditionsDTO> getDeviceConditions(String username, String vdmsid, String dockername, String device_id);

    void deleteAllDeviceConditions(String username, String vdmsid, String device_id);

    DeviceConditionsDTO getDeviceConditionsById(String username, String vdmsid, String device_condition_id);

    void deleteDeviceConditions(String username, String vdmsid, Set<DeviceConditionsDTO> device_conditions);

    void updateLastAlertedDetails(String id, BigInteger current_timestamp, Boolean last_alerted, Integer alert_count);

    void shareDeviceConditions(String username, String vdmsid, String dockername, ShareConditionsDTO shareConditions);

    void updateAlertProfileId(String alert_profile_id);

    void resetDeviceConditions(String username, String vdmsid, String dockername, Set<DeviceConditionsDTO> deviceConditions);

    void upsertDeviceConditionsForAiCall(String username, String vdmsid, String dockername, Set<DeviceConditionsDTO> device_conditions);

    Integer getAlertCount(String deviceId);

    Set<DeviceConditionsDTO> getDeviceConditionsForAiCall(String username, String vdmsid, String dockername, String device_id);

    DeviceConditionsDTO getDeviceConditionsByIdForAiCall(String username, String vdmsid, String device_condition_id);

    void updateAlertCountByConditionId(String id, int alertCount);
}
