package io.sclera.interfaces;

import io.sclera.dto.DeviceOnboardStatusAssigneeDTO;
import io.sclera.dto.DeviceOnboardStatusDTO;
import java.util.Set;

/** Service contract for the matching service class. */
public interface DeviceOnboardStatusAssigneeServiceInterface {
    void addDeviceOnboardStatusAssignees(String device_onboard_status_id, Set<DeviceOnboardStatusAssigneeDTO> deviceOnboardStatusAssigneeDTOS);
    void deleteDeviceOnboardStatusAssigneesByDeviceOnboardStatusId(String device_onboard_status_id);
    Set<DeviceOnboardStatusAssigneeDTO> getDeviceOnboardStatusAssignees(String deviceOnboardStatusId);
    Set<String> getDeviceOnboardStatusAssigneesEmail();
    void updateDeviceOnboardStautsAssignee(DeviceOnboardStatusDTO deviceOnboardStatusDTO);
}
