package io.sclera.controller.admin;

import io.sclera.dto.DeviceLifecycleHistoryDTO;
import io.sclera.service.DeviceLifecycleHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@CrossOrigin(allowedHeaders = "*", origins = "*")
public class DeviceLifecycleHistoryController {

    @Autowired
    DeviceLifecycleHistoryService deviceLifeCycleHistoryService;

    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdmsid}/adddevicehistory")
    public void addDeviceHistory(@PathVariable String username,
                                 @PathVariable String vdmsid,
                                 @RequestBody DeviceLifecycleHistoryDTO dto,
                                 @RequestParam String retireStatus) {
        deviceLifeCycleHistoryService.addDeviceHistory(username, vdmsid, dto, retireStatus);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/user/{username}/vdms/{vdmsid}/device/{device_id}/getdevicehistory")
    public Set<DeviceLifecycleHistoryDTO> getDeviceHistory(@PathVariable String username,
                                                           @PathVariable String vdmsid,
                                                           @PathVariable String device_id,
                                                           @RequestParam(defaultValue = "1") Integer pageno, @RequestParam(defaultValue = "5") Integer pagesize) {
        return deviceLifeCycleHistoryService.getDeviceHistory(username, vdmsid, device_id, pageno, pagesize);
    }
}