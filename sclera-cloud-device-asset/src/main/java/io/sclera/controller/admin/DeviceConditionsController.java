package io.sclera.controller.admin;

import io.sclera.dto.DeviceConditionsDTO;
import io.sclera.dto.ShareConditionsDTO;
import io.sclera.service.DeviceConditionsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

/**
 * REST endpoints for managing device conditions, including sharing and resetting them.
 * Delegates all persistence and business logic to {@link DeviceConditionsService}.
 */
@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/api/v1/sclera-cloud-device-asset-service")
public class DeviceConditionsController {

    @Autowired
    DeviceConditionsService deviceConditionsService;

    //upsert conditions
    /**
     * Creates or updates the given device conditions for a docker.
     *
     * @param username           owning user
     * @param vdmsid             owning VDMS id
     * @param dockername         docker the conditions belong to
     * @param device_conditions  set of device conditions to upsert
     */
    @RequestMapping(method = RequestMethod.POST, value = "/docker/{dockername}/upsertdeviceconditions")
    public void upsertDeviceConditions(@RequestParam String username, @RequestParam String vdmsid, @PathVariable String dockername, @RequestBody Set<DeviceConditionsDTO> device_conditions) {
        deviceConditionsService.upsertDeviceConditions(username, vdmsid, dockername, device_conditions);
    }
    //getcondtions
    /**
     * Returns the device conditions for a specific device under a docker.
     *
     * @param username    owning user
     * @param vdmsid      owning VDMS id
     * @param dockername  docker the conditions belong to
     * @param device_id   device whose conditions are requested
     * @return set of device conditions for the device
     */
    @RequestMapping(method = RequestMethod.GET, value = "/docker/{dockername}/device/{device_id}/getdeviceconditions")
    public Set<DeviceConditionsDTO> getDeviceConditions(@RequestParam String username, @RequestParam String vdmsid, @PathVariable String dockername, @PathVariable String device_id) {
        return deviceConditionsService.getDeviceConditions(username, vdmsid, dockername, device_id);
    }
    // delete all conditions
    /**
     * Deletes all conditions for the given device.
     *
     * @param username   owning user
     * @param vdmsid     owning VDMS id
     * @param device_id  device whose conditions are deleted
     */
    @RequestMapping(method = RequestMethod.DELETE, value = "/device/{device_id}/deletealldeviceconditions")
    public void deleteAllDeviceConditions(@RequestParam String username, @RequestParam String vdmsid, @PathVariable String device_id ) {
        deviceConditionsService.deleteAllDeviceConditions(username, vdmsid, device_id);
    }

    // get condition by id
    /**
     * Returns a single device condition by its id.
     *
     * @param username      owning user
     * @param vdmsid        owning VDMS id
     * @param condition_id  id of the condition to retrieve
     * @return the matching device condition
     */
    @RequestMapping(method = RequestMethod.GET, value = "/condition/{condition_id}/getdeviceconditionsbyid")
    public DeviceConditionsDTO getDeviceConditionsById(@RequestParam String username, @RequestParam String vdmsid, @PathVariable String condition_id) {
        return deviceConditionsService.getDeviceConditionsById(username, vdmsid, condition_id);
    }
    //delete condition
    /**
     * Deletes the given device conditions.
     *
     * @param username           owning user
     * @param vdmsid             owning VDMS id
     * @param device_conditions  set of device conditions to delete
     */
    @RequestMapping(method = RequestMethod.DELETE, value = "/deletedeviceconditions")
    public void deleteDeviceConditions(@RequestParam String username, @RequestParam String vdmsid,  @RequestBody Set<DeviceConditionsDTO> device_conditions) {
        deviceConditionsService.deleteDeviceConditions(username, vdmsid, device_conditions);
    }

    /**
     * Shares device conditions with the targets described in the payload.
     *
     * @param username         owning user
     * @param vdmsid           owning VDMS id
     * @param dockername       docker the conditions belong to
     * @param shareConditions  payload describing the conditions and share targets
     */
    @RequestMapping(method = RequestMethod.POST, value = "/docker/{dockername}/sharedeviceconditions")
    public void shareDeviceConditions(@RequestParam String username, @RequestParam String vdmsid, @PathVariable String dockername, @RequestBody ShareConditionsDTO shareConditions) {
        deviceConditionsService.shareDeviceConditions(username, vdmsid, dockername, shareConditions);
    }

    /**
     * Resets the given device conditions to their default state for a docker.
     *
     * @param username           owning user
     * @param vdmsid             owning VDMS id
     * @param dockername         docker the conditions belong to
     * @param device_conditions  set of device conditions to reset
     */
    @RequestMapping(method = RequestMethod.POST, value = "/docker/{dockername}/resetdeviceconditions")
    public void resetDeviceConditions(@RequestParam String username, @RequestParam String vdmsid, @PathVariable String dockername, @RequestBody Set<DeviceConditionsDTO> device_conditions) {
        deviceConditionsService.resetDeviceConditions(username, vdmsid, dockername,  device_conditions);
    }
}
