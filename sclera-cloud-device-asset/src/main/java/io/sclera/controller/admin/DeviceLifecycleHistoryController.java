package io.sclera.controller.admin;

import io.sclera.dto.DeviceLifecycleHistoryDTO;
import io.sclera.service.DeviceLifecycleHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

/**
 * REST endpoints recording and retrieving the lifecycle history of a device
 * (status changes, retirement, etc.). Delegates to {@link DeviceLifecycleHistoryService}.
 */
@RestController
@CrossOrigin(allowedHeaders = "*", origins = "*")
@RequestMapping("/api/v1/sclera-cloud-device-asset-service")
public class DeviceLifecycleHistoryController {

    @Autowired
    DeviceLifecycleHistoryService deviceLifeCycleHistoryService;

    /**
     * Appends a lifecycle history entry for a device.
     *
     * @param username     owning user
     * @param vdmsid       owning VDMS id
     * @param dto          lifecycle history entry to record
     * @param retireStatus retirement status applied alongside the entry
     */
    @RequestMapping(method = RequestMethod.POST, value = "/adddevicehistory")
    public void addDeviceHistory(@RequestParam String username,
                                 @RequestParam String vdmsid,
                                 @RequestBody DeviceLifecycleHistoryDTO dto,
                                 @RequestParam String retireStatus) {
        deviceLifeCycleHistoryService.addDeviceHistory(username, vdmsid, dto, retireStatus);
    }

    /**
     * Returns a paginated lifecycle history for the given device.
     *
     * @param username  owning user
     * @param vdmsid    owning VDMS id
     * @param device_id device whose history is fetched
     * @param pageno    1-based page number (default 1)
     * @param pagesize  page size (default 5)
     * @return set of lifecycle history entries for the requested page
     */
    @RequestMapping(method = RequestMethod.GET, value = "/device/{device_id}/getdevicehistory")
    public Set<DeviceLifecycleHistoryDTO> getDeviceHistory(@RequestParam String username,
                                                           @RequestParam String vdmsid,
                                                           @PathVariable String device_id,
                                                           @RequestParam(defaultValue = "1") Integer pageno, @RequestParam(defaultValue = "5") Integer pagesize) {
        return deviceLifeCycleHistoryService.getDeviceHistory(username, vdmsid, device_id, pageno, pagesize);
    }
}