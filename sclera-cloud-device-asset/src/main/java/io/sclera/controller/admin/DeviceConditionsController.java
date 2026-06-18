package io.sclera.controller.admin;

import io.sclera.dto.DeviceConditionsDTO;
import io.sclera.dto.ShareConditionsDTO;
import io.sclera.service.DeviceConditionsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(DeviceConditionsController.class);

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
        log.info("upsertDeviceConditions username={} vdmsid={} dockername={}", username, vdmsid, dockername);
        try {
            deviceConditionsService.upsertDeviceConditions(username, vdmsid, dockername, device_conditions);
        } catch (Exception e) {
            log.error("upsertDeviceConditions failed username={} vdmsid={} dockername={}: {}", username, vdmsid, dockername, e.getMessage(), e);
            throw e;
        }
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
        log.info("getDeviceConditions username={} vdmsid={} dockername={} device_id={}", username, vdmsid, dockername, device_id);
        try {
            return deviceConditionsService.getDeviceConditions(username, vdmsid, dockername, device_id);
        } catch (Exception e) {
            log.error("getDeviceConditions failed username={} vdmsid={} dockername={} device_id={}: {}", username, vdmsid, dockername, device_id, e.getMessage(), e);
            throw e;
        }
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
        log.info("deleteAllDeviceConditions username={} vdmsid={} device_id={}", username, vdmsid, device_id);
        try {
            deviceConditionsService.deleteAllDeviceConditions(username, vdmsid, device_id);
        } catch (Exception e) {
            log.error("deleteAllDeviceConditions failed username={} vdmsid={} device_id={}: {}", username, vdmsid, device_id, e.getMessage(), e);
            throw e;
        }
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
        log.info("getDeviceConditionsById username={} vdmsid={} condition_id={}", username, vdmsid, condition_id);
        try {
            return deviceConditionsService.getDeviceConditionsById(username, vdmsid, condition_id);
        } catch (Exception e) {
            log.error("getDeviceConditionsById failed username={} vdmsid={} condition_id={}: {}", username, vdmsid, condition_id, e.getMessage(), e);
            throw e;
        }
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
        log.info("deleteDeviceConditions username={} vdmsid={}", username, vdmsid);
        try {
            deviceConditionsService.deleteDeviceConditions(username, vdmsid, device_conditions);
        } catch (Exception e) {
            log.error("deleteDeviceConditions failed username={} vdmsid={}: {}", username, vdmsid, e.getMessage(), e);
            throw e;
        }
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
        log.info("shareDeviceConditions username={} vdmsid={} dockername={}", username, vdmsid, dockername);
        try {
            deviceConditionsService.shareDeviceConditions(username, vdmsid, dockername, shareConditions);
        } catch (Exception e) {
            log.error("shareDeviceConditions failed username={} vdmsid={} dockername={}: {}", username, vdmsid, dockername, e.getMessage(), e);
            throw e;
        }
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
        log.info("resetDeviceConditions username={} vdmsid={} dockername={}", username, vdmsid, dockername);
        try {
            deviceConditionsService.resetDeviceConditions(username, vdmsid, dockername,  device_conditions);
        } catch (Exception e) {
            log.error("resetDeviceConditions failed username={} vdmsid={} dockername={}: {}", username, vdmsid, dockername, e.getMessage(), e);
            throw e;
        }
    }
}
