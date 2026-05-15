package io.sclera.controller.admin;

import io.sclera.dto.DeviceConditionsDTO;
import io.sclera.dto.ShareConditionsDTO;
import io.sclera.service.DeviceConditionsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class DeviceConditionsController {

    @Autowired
    DeviceConditionsService deviceConditionsService;

    //upsert conditions
    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdmsid}/docker/{dockername}/upsertdeviceconditions")
    public void upsertDeviceConditions(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String dockername, @RequestBody Set<DeviceConditionsDTO> device_conditions) {
        deviceConditionsService.upsertDeviceConditions(username, vdmsid, dockername, device_conditions);
    }
    //getcondtions
    @RequestMapping(method = RequestMethod.GET, value = "/user/{username}/vdms/{vdmsid}/docker/{dockername}/device/{device_id}/getdeviceconditions")
    public Set<DeviceConditionsDTO> getDeviceConditions(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String dockername, @PathVariable String device_id) {
        return deviceConditionsService.getDeviceConditions(username, vdmsid, dockername, device_id);
    }
    // delete all conditions
    @RequestMapping(method = RequestMethod.DELETE, value = "/user/{username}/vdms/{vdmsid}/device/{device_id}/deletealldeviceconditions")
    public void deleteAllDeviceConditions(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String device_id ) {
        deviceConditionsService.deleteAllDeviceConditions(username, vdmsid, device_id);
    }

    // get condition by id
    @RequestMapping(method = RequestMethod.GET, value = "/user/{username}/vdms/{vdmsid}/condition/{condition_id}/getdeviceconditionsbyid")
    public DeviceConditionsDTO getDeviceConditionsById(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String condition_id) {
        return deviceConditionsService.getDeviceConditionsById(username, vdmsid, condition_id);
    }
    //delete condition
    @RequestMapping(method = RequestMethod.DELETE, value = "/user/{username}/vdms/{vdmsid}/deletedeviceconditions")
    public void deleteDeviceConditions(@PathVariable String username, @PathVariable String vdmsid,  @RequestBody Set<DeviceConditionsDTO> device_conditions) {
        deviceConditionsService.deleteDeviceConditions(username, vdmsid, device_conditions);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdmsid}/docker/{dockername}/sharedeviceconditions")
    public void shareDeviceConditions(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String dockername, @RequestBody ShareConditionsDTO shareConditions) {
        deviceConditionsService.shareDeviceConditions(username, vdmsid, dockername, shareConditions);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdmsid}/docker/{dockername}/resetdeviceconditions")
    public void resetDeviceConditions(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String dockername, @RequestBody Set<DeviceConditionsDTO> device_conditions) {
        deviceConditionsService.resetDeviceConditions(username, vdmsid, dockername,  device_conditions);
    }
}
