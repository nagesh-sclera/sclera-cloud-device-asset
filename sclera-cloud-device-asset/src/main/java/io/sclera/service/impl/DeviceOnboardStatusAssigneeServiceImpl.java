package io.sclera.service.impl;
import io.sclera.service.*;

import com.fasterxml.uuid.Generators;
import io.sclera.Repository.DeviceOnboardStatusAssigneeRepository;
import io.sclera.dto.DeviceOnboardStatusAssigneeDTO;
import io.sclera.dto.DeviceOnboardStatusDTO;
import io.sclera.service.DeviceOnboardStatusAssigneeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * Manages the assignees associated with a device onboard status.
 *
 * <p>Persists, retrieves, and removes assignee records for a given onboard status via
 * {@link DeviceOnboardStatusAssigneeRepository}, and reconciles the assignee set when an
 * onboard status is updated.
 */
@Service
public class DeviceOnboardStatusAssigneeServiceImpl implements DeviceOnboardStatusAssigneeService {

    @Autowired
    DeviceOnboardStatusAssigneeRepository deviceOnboardStatusAssigneeRepository;

    /**
     * Persists a set of assignees for the given onboard status, generating a time-based
     * identifier for each and assigning them the "secondary" role.
     *
     * @param device_onboard_status_id the identifier of the onboard status to attach the assignees to
     * @param deviceOnboardStatusAssigneeDTOS the assignees to persist
     */
    public void addDeviceOnboardStatusAssignees(String device_onboard_status_id, Set<DeviceOnboardStatusAssigneeDTO> deviceOnboardStatusAssigneeDTOS) {
        for (DeviceOnboardStatusAssigneeDTO deviceOnboardStatusAssigneeDTO : deviceOnboardStatusAssigneeDTOS) {
            deviceOnboardStatusAssigneeDTO.setId(Generators.timeBasedGenerator().generate().toString());
            deviceOnboardStatusAssigneeDTO.setDevice_onboard_status_id(device_onboard_status_id);
            deviceOnboardStatusAssigneeRepository.addDeviceOnboardStatusAssignees(deviceOnboardStatusAssigneeDTO.getId(), deviceOnboardStatusAssigneeDTO.getEmail(), "secondary", deviceOnboardStatusAssigneeDTO.getDevice_onboard_status_id());

        }
    }

    /**
     * Removes all assignees associated with the given onboard status.
     *
     * @param device_onboard_status_id the identifier of the onboard status whose assignees are removed
     */
    public void deleteDeviceOnboardStatusAssigneesByDeviceOnboardStatusId(String device_onboard_status_id) {
        deviceOnboardStatusAssigneeRepository.deleteDeviceOnboardStatusAssigneesByDeviceOnboardStatusId(device_onboard_status_id);
    }

    /**
     * Returns the assignees associated with the given onboard status.
     *
     * @param deviceOnboardStatusId the identifier of the onboard status
     * @return the assignees for the onboard status
     */
    public Set<DeviceOnboardStatusAssigneeDTO> getDeviceOnboardStatusAssignees(String deviceOnboardStatusId) {
        return deviceOnboardStatusAssigneeRepository.getDeviceOnboardStatusAssignees(deviceOnboardStatusId);
    }

    /**
     * Returns the email addresses of all onboard status assignees.
     *
     * @return the assignee email addresses
     */
    public Set<String> getDeviceOnboardStatusAssigneesEmail() {
        return deviceOnboardStatusAssigneeRepository.getDeviceOnboardStatusAssigneesEmail();
    }

    /**
     * Reconciles the assignees of an onboard status by clearing the existing assignees and,
     * when the supplied set is non-empty, re-adding them.
     *
     * @param deviceOnboardStatusDTO the onboard status carrying the desired assignee set
     */
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
