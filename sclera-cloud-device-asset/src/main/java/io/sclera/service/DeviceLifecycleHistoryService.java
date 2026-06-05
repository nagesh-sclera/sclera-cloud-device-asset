package io.sclera.service;

import com.fasterxml.uuid.Generators;
import io.sclera.Repository.DeviceRepository;
import io.sclera.client.InventoryDeviceClient;
import io.sclera.dto.DeviceDTO;
import io.sclera.dto.DeviceLifecycleHistoryDTO;
import io.sclera.Repository.DeviceLifeCycleHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.Collections;
import java.util.Set;

/**
 * Records and queries device lifecycle history, tracking operational status,
 * usage status, and user assignments over a device's lifetime.
 *
 * <p>Persists history entries through {@link DeviceLifeCycleHistoryRepository},
 * synchronises the owning device's operational status and assigned-user fields
 * via {@link DeviceRepository}, and delegates retirement handling to
 * {@link DeviceService} and {@link InventoryDeviceClient}.
 */
@Service
public class DeviceLifecycleHistoryService {

    @Autowired
    private DeviceLifeCycleHistoryRepository deviceLifeCycleHistoryRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    DeviceService deviceService;

    @Autowired
    InventoryDeviceClient inventoryDeviceClient;

    /**
     * Creates a new device lifecycle history entry, deriving usage status and
     * assignment count, updating the device's operational status, and persisting the record.
     *
     * @param username the user performing the action
     * @param vdmsid the VDMS identifier for the request context
     * @param deviceLifecycleHistoryDTO the history details to record
     * @param retireStatus retirement flag ("true"/"false") controlling assigned-user reset and retirement
     */
    public void addDeviceHistory(String username, String vdmsid, DeviceLifecycleHistoryDTO deviceLifecycleHistoryDTO, String retireStatus) {

        if (deviceLifecycleHistoryDTO.getId() == null) {
            deviceLifecycleHistoryDTO.setId(Generators.timeBasedGenerator().generate().toString());
        }
        int assignmentCount = 0;
        Integer latestCount = deviceLifeCycleHistoryRepository.getLatestAssignedCount(deviceLifecycleHistoryDTO.getDevice_id());
        if (latestCount != null) {
            assignmentCount = latestCount;
        }

        if ((deviceLifecycleHistoryDTO.getAssigned_user_id() == null && assignmentCount == 0) ||
                (assignmentCount == 0 && deviceLifecycleHistoryDTO.getAssigned_user_id() != null)) {
            deviceLifecycleHistoryDTO.setUsage_status("new");
        } else {
            deviceLifecycleHistoryDTO.setUsage_status("used");
        }
        if (deviceLifecycleHistoryDTO.getAssigned_user_id() != null) {
            assignmentCount += 1;
            deviceLifecycleHistoryDTO.setId(Generators.timeBasedGenerator().generate().toString());
            deviceLifecycleHistoryDTO.setAssigned_user_id(deviceLifecycleHistoryDTO.getAssigned_user_id());
            deviceLifecycleHistoryDTO.setCreated_timestamp(BigInteger.valueOf(System.currentTimeMillis()));
            deviceLifecycleHistoryDTO.setAssigned_timestamp(BigInteger.valueOf(System.currentTimeMillis()));
            deviceLifecycleHistoryDTO.setOperational_status(deviceLifecycleHistoryDTO.getOperational_status());
            deviceLifecycleHistoryDTO.setAssignment_count(assignmentCount);
            deviceLifecycleHistoryDTO.setDevice_id(deviceLifecycleHistoryDTO.getDevice_id());
        } else {
            deviceLifecycleHistoryDTO.setId(Generators.timeBasedGenerator().generate().toString());
            deviceLifecycleHistoryDTO.setAssigned_user_id(null);
            deviceLifecycleHistoryDTO.setCreated_timestamp(BigInteger.valueOf(System.currentTimeMillis()));
            deviceLifecycleHistoryDTO.setAssigned_timestamp(null);
            deviceLifecycleHistoryDTO.setOperational_status(deviceLifecycleHistoryDTO.getOperational_status());
            deviceLifecycleHistoryDTO.setAssignment_count(assignmentCount);
            deviceLifecycleHistoryDTO.setDevice_id(deviceLifecycleHistoryDTO.getDevice_id());
        }

        // if ("Not Working".equalsIgnoreCase(deviceLifecycleHistoryDTO.getOperational_status()) &&
        //     (deviceLifecycleHistoryDTO.getDescription() == null || deviceLifecycleHistoryDTO.getDescription().trim().isEmpty())) {
        //     throw new IllegalArgumentException("Description is required when device condition is 'Not Working'");
        // }
        this.updateOperationalStatus(deviceLifecycleHistoryDTO.getDevice_id(), deviceLifecycleHistoryDTO.getOperational_status(),
                retireStatus, username, vdmsid, deviceLifecycleHistoryDTO.getDescription());
//        this.updateAssignedUserEmail(deviceLifecycleHistoryDTO.getDevice_id(), deviceLifecycleHistoryDTO.getAssigned_user_id());


        deviceLifeCycleHistoryRepository.addDeviceLifeCycleHistory(
                deviceLifecycleHistoryDTO.getId(),
                deviceLifecycleHistoryDTO.getOperational_status(),
                deviceLifecycleHistoryDTO.getUsage_status(),
                deviceLifecycleHistoryDTO.getAssigned_user_id(),
                deviceLifecycleHistoryDTO.getAssignment_count(),
                deviceLifecycleHistoryDTO.getCreated_timestamp(),
                deviceLifecycleHistoryDTO.getAssigned_timestamp(),
                deviceLifecycleHistoryDTO.getDevice_id(),
                deviceLifecycleHistoryDTO.getDescription(),
                deviceLifecycleHistoryDTO.getAssigned_by_user_id()
        );
    }



    /**
     * Updates the device's operational status when it differs from the latest recorded status,
     * clears the assigned-user email on retirement, and archives/retires the device in inventory when retired.
     *
     * @param device_id the device identifier
     * @param operational_status the new operational status to apply
     * @param retireStatus retirement flag ("true"/"false") controlling assigned-user reset and retirement
     * @param username the user performing the action
     * @param vdmsid the VDMS identifier for the request context
     * @param description the reason or note associated with the change
     */
    public void updateOperationalStatus(String device_id , String operational_status, String retireStatus, String username, String vdmsid, String description){
        String latestStatus = deviceLifeCycleHistoryRepository.getLatestOperationalStatusFromHistory(device_id);
        if (operational_status != null && !operational_status.equalsIgnoreCase(latestStatus)) {
            deviceRepository.updateDeviceOperationalStatus(device_id, operational_status);
        }

        // Set assigned_user_email to null if status is not_working or disposed
        if ("true".equalsIgnoreCase(retireStatus) || "false".equalsIgnoreCase(retireStatus)) {
            deviceRepository.updateAssignedUserEmail(device_id, null);
        }

        if ("true".equalsIgnoreCase(retireStatus)){
            DeviceDTO deviceDTO = deviceService.getDeviceDetailsByDeviceId(device_id);
            if(deviceDTO.getInventory_tracking_id() != null){
                System.out.println("Please make device archive based on Inventory_tracking_id");
                deviceService.archiveDevicesForInventoryDevice(username, vdmsid, 1, Collections.singleton(device_id));
                inventoryDeviceClient.retireInventoryDevice(vdmsid, device_id, username, description, deviceDTO.getInventory_tracking_id());

            }
        }

    }

//    public void updateAssignedUserEmail(String deviceId, String assignedUserEmail) {
//        String latestEmail = deviceLifeCycleHistoryRepository.getLatestAssignedUserEmailFromHistory(deviceId);
//
//        if (assignedUserEmail == null && !assignedUserEmail.equalsIgnoreCase(latestEmail)) {
//            deviceRepository.updateAssignedUserEmail(deviceId, assignedUserEmail);
//        }



    /**
     * Retrieves a paginated set of lifecycle history entries for a device.
     *
     * @param username the user requesting the history
     * @param vdmsid the VDMS identifier for the request context
     * @param deviceId the device identifier
     * @param pageno the one-based page number
     * @param pagesize the number of entries per page
     * @return the lifecycle history entries for the requested page
     */
    public Set<DeviceLifecycleHistoryDTO> getDeviceHistory(String username, String vdmsid, String deviceId, Integer pageno, Integer pagesize) {
        Integer offset = pagesize * (pageno - 1);
        return deviceLifeCycleHistoryRepository.getDeviceLifeCycleHistory(deviceId, pagesize, offset);
    }


}