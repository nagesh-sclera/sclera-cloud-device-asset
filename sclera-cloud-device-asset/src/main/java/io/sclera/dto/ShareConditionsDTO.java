package io.sclera.dto;

import java.util.Collections;
import java.util.List;

/**
 * Carries the criteria used when sharing devices, pairing a list of devices and
 * per-device conditions with the matching method that determines how they apply.
 */
public class ShareConditionsDTO {
    private List<DeviceDTO> devices;
    private String condition_method;
    private List<DeviceConditionsDTO> deviceConditions;

    public List<DeviceDTO> getDevices() { return devices != null ? devices : Collections.emptyList(); }
    public void setDevices(List<DeviceDTO> v) { this.devices = v; }
    public String getCondition_method() { return condition_method; }
    public void setCondition_method(String v) { this.condition_method = v; }
    public List<DeviceConditionsDTO> getDeviceConditions() { return deviceConditions != null ? deviceConditions : Collections.emptyList(); }
    public void setDeviceConditions(List<DeviceConditionsDTO> v) { this.deviceConditions = v; }
}
