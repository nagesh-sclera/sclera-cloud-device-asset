package io.sclera.dto;

import java.util.List;

/** STUB: belongs to a future microservice */
public class CallFlowRuleDTO {

    private String id;
    private String name;
    private String deviceId;
    private String deviceName;
    private Integer isAdded;
    private List<CallFlowRuleConditionDTO> callFlowRuleConditions;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public String getDeviceName() { return deviceName; }
    public void setDeviceName(String deviceName) { this.deviceName = deviceName; }

    public Integer getIsAdded() { return isAdded; }
    public void setIsAdded(Integer isAdded) { this.isAdded = isAdded; }

    public List<CallFlowRuleConditionDTO> getCallFlowRuleConditions() { return callFlowRuleConditions; }
    public void setCallFlowRuleConditions(List<CallFlowRuleConditionDTO> callFlowRuleConditions) {
        this.callFlowRuleConditions = callFlowRuleConditions;
    }
}
