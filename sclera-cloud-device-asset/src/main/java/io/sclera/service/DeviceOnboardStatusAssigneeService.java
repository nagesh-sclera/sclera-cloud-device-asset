package io.sclera.service;

import com.fasterxml.uuid.Generators;
import io.sclera.Repository.DeviceOnboardStatusAssigneeRepository;
import io.sclera.dto.DeviceOnboardStatusAssigneeDTO;
import io.sclera.dto.DeviceOnboardStatusDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class DeviceOnboardStatusAssigneeService {

    @Autowired
    DeviceOnboardStatusAssigneeRepository deviceOnboardStatusAssigneeRepository;

    public void addDeviceOnboardStatusAssignees(String device_onboard_status_id, Set<DeviceOnboardStatusAssigneeDTO> deviceOnboardStatusAssigneeDTOS) {
        for (DeviceOnboardStatusAssigneeDTO deviceOnboardStatusAssigneeDTO : deviceOnboardStatusAssigneeDTOS) {
            deviceOnboardStatusAssigneeDTO.setId(Generators.timeBasedGenerator().generate().toString());
            deviceOnboardStatusAssigneeDTO.setDevice_onboard_status_id(device_onboard_status_id);
            deviceOnboardStatusAssigneeRepository.addDeviceOnboardStatusAssignees(deviceOnboardStatusAssigneeDTO.getId(), deviceOnboardStatusAssigneeDTO.getEmail(), "secondary", deviceOnboardStatusAssigneeDTO.getDevice_onboard_status_id());

        }
    }

    public void deleteDeviceOnboardStatusAssigneesByDeviceOnboardStatusId(String device_onboard_status_id) {
        deviceOnboardStatusAssigneeRepository.deleteDeviceOnboardStatusAssigneesByDeviceOnboardStatusId(device_onboard_status_id);
    }

    public Set<DeviceOnboardStatusAssigneeDTO> getDeviceOnboardStatusAssignees(String deviceOnboardStatusId) {
        return deviceOnboardStatusAssigneeRepository.getDeviceOnboardStatusAssignees(deviceOnboardStatusId);
    }

    public Set<String> getDeviceOnboardStatusAssigneesEmail() {
        return deviceOnboardStatusAssigneeRepository.getDeviceOnboardStatusAssigneesEmail();
    }

    public void updateDeviceOnboardStautsAssignee(DeviceOnboardStatusDTO deviceOnboardStatusDTO) {
        if (deviceOnboardStatusDTO.getDevice_onboard_status_assignees() != null) {
            if (deviceOnboardStatusDTO.getDevice_onboard_status_assignees().size() == 0) {
                this.deleteDeviceOnboardStatusAssigneesByDeviceOnboardStatusId(deviceOnboardStatusDTO.getId());
            } else if (deviceOnboardStatusDTO.getDevice_onboard_status_assignees().size() > 0) {
                this.deleteDeviceOnboardStatusAssigneesByDeviceOnboardStatusId(deviceOnboardStatusDTO.getId());
                this.addDeviceOnboardStatusAssignees(deviceOnboardStatusDTO.getId(), deviceOnboardStatusDTO.getDevice_onboard_status_assignees());
            }
        }
    }
}
