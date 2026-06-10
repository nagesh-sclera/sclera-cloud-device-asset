
package io.sclera.controller.admin;

import io.sclera.dto.DeviceDTO;
import io.sclera.dto.LoadCalculationDTO;
import io.sclera.dto.SpecificationsDTO;
import io.sclera.service.SpecificationsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;


/**
 * REST endpoints for managing device specifications and power-source tagging,
 * including load calculations.
 * Delegates all persistence and business logic to {@link SpecificationsService}.
 */
@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/api/v1/sclera-cloud-device-asset-service")
public class SpecificationsController {
    private static final Logger log = LoggerFactory.getLogger(SpecificationsController.class);

    @Autowired
    SpecificationsService specificationsService;

    /**
     * Edits the supplied device specifications.
     *
     * @param username       owning user
     * @param vdmsid         owning VDMS id
     * @param specifications specifications to edit
     */
    // API to edit specifications
    @RequestMapping(method = RequestMethod.POST, value = "/editdevicespecifications")
    public void editDeviceSpecifications(@RequestParam String username, @RequestParam String vdmsid, @RequestBody List<SpecificationsDTO> specifications) {
        log.info("editDeviceSpecifications username={} vdmsid={}", username, vdmsid);
        try {
            specificationsService.editDeviceSpecifications(username,vdmsid,specifications);
        } catch (Exception e) {
            log.error("editDeviceSpecifications failed username={}: {}", username, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Upserts the supplied device specifications.
     *
     * @param username       owning user
     * @param vdmsid         owning VDMS id
     * @param specifications specifications to create or update
     * @return the persisted specifications
     */
    // API to add/update specifications
    @RequestMapping(method = RequestMethod.POST, value = "/adddevicespecifications")
    public List<SpecificationsDTO> upsertDeviceSpecifications(@RequestParam String username, @RequestParam String vdmsid, @RequestBody List<SpecificationsDTO> specifications) {
        log.info("upsertDeviceSpecifications username={} vdmsid={}", username, vdmsid);
        try {
            return specificationsService.upsertDeviceSpecifications(username,vdmsid,specifications);
        } catch (Exception e) {
            log.error("upsertDeviceSpecifications failed username={}: {}", username, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Returns the specifications for the given device.
     *
     * @param username  owning user
     * @param vdmsid    owning VDMS id
     * @param device_id device whose specifications are requested
     * @return the device's specifications
     */
    // API to get device specifications
    @RequestMapping(method = RequestMethod.GET, value = "/device/{device_id}/getdevicespecificationsbydeviceid")
    public List<SpecificationsDTO> getDeviceSpecificationsByDeviceId(@RequestParam String username, @RequestParam String vdmsid, @PathVariable String device_id) {
        log.info("getDeviceSpecificationsByDeviceId username={} vdmsid={} device_id={}", username, vdmsid, device_id);
        try {
            return specificationsService.getDeviceSpecificationsByDeviceId(username,vdmsid,device_id);
        } catch (Exception e) {
            log.error("getDeviceSpecificationsByDeviceId failed username={}: {}", username, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Tags multiple devices to a given power source.
     *
     * @param username       owning user
     * @param vdmsid         owning VDMS id
     * @param specifications specifications describing the devices and power source to tag
     */
    // API tagging multiple devices to a given power source
    @RequestMapping(method = RequestMethod.POST, value = "/tagpowersources")
    public void tagPowerSources(@RequestParam String username, @RequestParam String vdmsid, @RequestBody List<SpecificationsDTO> specifications) {
        log.info("tagPowerSources username={} vdmsid={}", username, vdmsid);
        try {
            specificationsService.tagPowerSources(username,vdmsid,specifications);
        } catch (Exception e) {
            log.error("tagPowerSources failed username={}: {}", username, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Untags a power source from a device.
     *
     * @param username       owning user
     * @param vdmsid         owning VDMS id
     * @param specifications specifications describing the power source to untag
     */
    // API to untag a power source from a device
    @RequestMapping(method = RequestMethod.POST, value = "/untagpowersource")
    public void untagPowerSource(@RequestParam String username, @RequestParam String vdmsid, @RequestBody List<SpecificationsDTO> specifications) {
        log.info("untagPowerSource username={} vdmsid={}", username, vdmsid);
        try {
            specificationsService.untagPowerSource(username,vdmsid,specifications);
        } catch (Exception e) {
            log.error("untagPowerSource failed username={}: {}", username, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Untags a device from its respective power source.
     *
     * @param username       owning user
     * @param vdmsid         owning VDMS id
     * @param specifications specifications describing the device to untag
     */
    // API to untag a device from its respective power source
    @RequestMapping(method = RequestMethod.POST, value = "/untagdevice")
    public void untagDevice(@RequestParam String username, @RequestParam String vdmsid, @RequestBody List<SpecificationsDTO> specifications) {
        log.info("untagDevice username={} vdmsid={}", username, vdmsid);
        try {
            specificationsService.untagDevice(username,vdmsid,specifications);
        } catch (Exception e) {
            log.error("untagDevice failed username={}: {}", username, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Returns the devices tagged to a given output port, paginated.
     *
     * @param username          owning user
     * @param vdmsid            owning VDMS id
     * @param specificationsDTO specification describing the output port to match
     * @param pageno            page number to retrieve (default 1)
     * @param pagesize          number of records per page (default 10)
     * @return the matching page of tagged devices
     */
    // API to get all tagged devices based on a given output port
    @RequestMapping(method = RequestMethod.POST, value = "/gettaggeddevices")
    public List<DeviceDTO> getTaggedDevices(@RequestParam String username, @RequestParam String vdmsid, @RequestBody SpecificationsDTO specificationsDTO,  @RequestParam(defaultValue = "1") Integer pageno, @RequestParam(defaultValue = "10") Integer pagesize) {
        log.info("getTaggedDevices username={} vdmsid={}", username, vdmsid);
        try {
            return specificationsService.getTaggedDevices(username,vdmsid,specificationsDTO,pageno,pagesize);
        } catch (Exception e) {
            log.error("getTaggedDevices failed username={}: {}", username, e.getMessage(), e);
            throw e;
        }
    }


    /**
     * Returns the power sources tagged to the given device.
     *
     * @param username  owning user
     * @param vdmsid    owning VDMS id
     * @param device_id device whose tagged power sources are requested
     * @return the list of tagged power sources
     */
    // API to get all tagged power sources of a given device
    @RequestMapping(method = RequestMethod.GET, value = "/device/{device_id}/gettaggedpowersourcesbydeviceid")
    public List<DeviceDTO> getTaggedPowerSourcesByDeviceId(@RequestParam String username, @RequestParam String vdmsid, @PathVariable String device_id) {
        log.info("getTaggedPowerSourcesByDeviceId username={} vdmsid={} device_id={}", username, vdmsid, device_id);
        try {
            return specificationsService.getTaggedPowerSourcesByDeviceId(username,vdmsid,device_id);
        } catch (Exception e) {
            log.error("getTaggedPowerSourcesByDeviceId failed username={}: {}", username, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Deletes the supplied device specifications.
     *
     * @param username       owning user
     * @param vdmsid         owning VDMS id
     * @param specifications specifications to delete
     */
    // API to delete specifications
    @RequestMapping(method = RequestMethod.DELETE, value = "/deletespecifications")
    public void deleteSpecifications(@RequestParam String username, @RequestParam String vdmsid, @RequestBody List<SpecificationsDTO> specifications) {
        log.info("deleteSpecifications username={} vdmsid={}", username, vdmsid);
        try {
            specificationsService.deleteSpecifications(username,vdmsid,specifications);
        } catch (Exception e) {
            log.error("deleteSpecifications failed username={}: {}", username, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Returns the power-based load calculation for the supplied specifications.
     *
     * @param username       owning user
     * @param vdmsid         owning VDMS id
     * @param specifications specifications to base the load calculation on
     * @return the computed load calculations
     */
    // API to get Power based load calculation
    @RequestMapping(method = RequestMethod.POST, value = "/getpowerbasedloadcalculation")
    public List<LoadCalculationDTO> getPowerBasedLoadCalculation(@RequestParam String username, @RequestParam String vdmsid, @RequestBody List<SpecificationsDTO> specifications) {
        log.info("getPowerBasedLoadCalculation username={} vdmsid={}", username, vdmsid);
        try {
            return specificationsService.getPowerBasedLoadCalculation(username,vdmsid,specifications);
        } catch (Exception e) {
            log.error("getPowerBasedLoadCalculation failed username={}: {}", username, e.getMessage(), e);
            throw e;
        }
    }


}
