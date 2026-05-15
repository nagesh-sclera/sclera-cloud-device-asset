
package io.sclera.controller.admin;

import io.sclera.dto.DeviceDTO;
import io.sclera.dto.LoadCalculationDTO;
import io.sclera.dto.SpecificationsDTO;
import io.sclera.service.SpecificationsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;


@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class SpecificationsController {

    @Autowired
    SpecificationsService specificationsService;

    // API to edit specifications
    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdmsid}/editdevicespecifications")
    public void editDeviceSpecifications(@PathVariable String username, @PathVariable String vdmsid, @RequestBody List<SpecificationsDTO> specifications) {
        specificationsService.editDeviceSpecifications(username,vdmsid,specifications);
    }

    // API to add/update specifications
    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdmsid}/adddevicespecifications")
    public List<SpecificationsDTO> upsertDeviceSpecifications(@PathVariable String username, @PathVariable String vdmsid, @RequestBody List<SpecificationsDTO> specifications) {
        return specificationsService.upsertDeviceSpecifications(username,vdmsid,specifications);
    }

    // API to get device specifications
    @RequestMapping(method = RequestMethod.GET, value = "/user/{username}/vdms/{vdmsid}/device/{device_id}/getdevicespecificationsbydeviceid")
    public List<SpecificationsDTO> getDeviceSpecificationsByDeviceId(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String device_id) {
        return specificationsService.getDeviceSpecificationsByDeviceId(username,vdmsid,device_id);
    }

    // API tagging multiple devices to a given power source
    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdmsid}/tagpowersources")
    public void tagPowerSources(@PathVariable String username, @PathVariable String vdmsid, @RequestBody List<SpecificationsDTO> specifications) {
        specificationsService.tagPowerSources(username,vdmsid,specifications);
    }

    // API to untag a power source from a device
    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdmsid}/untagpowersource")
    public void untagPowerSource(@PathVariable String username, @PathVariable String vdmsid, @RequestBody List<SpecificationsDTO> specifications) {
        specificationsService.untagPowerSource(username,vdmsid,specifications);
    }

    // API to untag a device from its respective power source
    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdmsid}/untagdevice")
    public void untagDevice(@PathVariable String username, @PathVariable String vdmsid, @RequestBody List<SpecificationsDTO> specifications) {
        specificationsService.untagDevice(username,vdmsid,specifications);
    }

    // API to get all tagged devices based on a given output port
    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdmsid}/gettaggeddevices")
    public List<DeviceDTO> getTaggedDevices(@PathVariable String username, @PathVariable String vdmsid, @RequestBody SpecificationsDTO specificationsDTO,  @RequestParam(defaultValue = "1") Integer pageno, @RequestParam(defaultValue = "10") Integer pagesize) {
        return specificationsService.getTaggedDevices(username,vdmsid,specificationsDTO,pageno,pagesize);
    }


    // API to get all tagged power sources of a given device
    @RequestMapping(method = RequestMethod.GET, value = "/user/{username}/vdms/{vdmsid}/device/{device_id}/gettaggedpowersourcesbydeviceid")
    public List<DeviceDTO> getTaggedPowerSourcesByDeviceId(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String device_id) {
        return specificationsService.getTaggedPowerSourcesByDeviceId(username,vdmsid,device_id);
    }

    // API to delete specifications
    @RequestMapping(method = RequestMethod.DELETE, value = "/user/{username}/vdms/{vdmsid}/deletespecifications")
    public void deleteSpecifications(@PathVariable String username, @PathVariable String vdmsid, @RequestBody List<SpecificationsDTO> specifications) {
        specificationsService.deleteSpecifications(username,vdmsid,specifications);
    }

    // API to get Power based load calculation
    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdmsid}/getpowerbasedloadcalculation")
    public List<LoadCalculationDTO> getPowerBasedLoadCalculation(@PathVariable String username, @PathVariable String vdmsid, @RequestBody List<SpecificationsDTO> specifications) {
        return specificationsService.getPowerBasedLoadCalculation(username,vdmsid,specifications);
    }


}
