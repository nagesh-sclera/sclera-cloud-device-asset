
package io.sclera.controller.admin;

import io.sclera.dto.DeviceDTO;
import io.sclera.dto.LoadCalculationDTO;
import io.sclera.dto.SpecificationsDTO;
import io.sclera.service.SpecificationsService;
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
        specificationsService.editDeviceSpecifications(username,vdmsid,specifications);
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
        return specificationsService.upsertDeviceSpecifications(username,vdmsid,specifications);
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
        return specificationsService.getDeviceSpecificationsByDeviceId(username,vdmsid,device_id);
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
        specificationsService.tagPowerSources(username,vdmsid,specifications);
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
        specificationsService.untagPowerSource(username,vdmsid,specifications);
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
        specificationsService.untagDevice(username,vdmsid,specifications);
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
        return specificationsService.getTaggedDevices(username,vdmsid,specificationsDTO,pageno,pagesize);
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
        return specificationsService.getTaggedPowerSourcesByDeviceId(username,vdmsid,device_id);
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
        specificationsService.deleteSpecifications(username,vdmsid,specifications);
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
        return specificationsService.getPowerBasedLoadCalculation(username,vdmsid,specifications);
    }


}
