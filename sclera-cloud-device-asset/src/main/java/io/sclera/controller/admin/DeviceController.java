package io.sclera.controller.admin;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.alibaba.fastjson.JSONObject;
import io.sclera.dto.*;
import io.sclera.service.touchscreen.DeviceMonitorService;
import io.sclera.integration.dto.ResponseDTO;
import io.sclera.utils.PageUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import io.sclera.client.APICallClient;
import io.sclera.service.DeviceSearchService;
import io.sclera.service.DeviceService;
import io.sclera.utils.APIRequest;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * REST endpoints for managing devices, virtual devices, topology, asset images
 * and related search/export operations within a tenant's VDMS/docker context.
 * Delegates persistence and business logic to {@link DeviceService},
 * {@link DeviceSearchService} and {@link DeviceMonitorService}, and product
 * lookups to {@link APICallClient}.
 */
@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/api/v1/sclera-cloud-device-asset-service")
@Tag(name = "Devices", description = "Manage devices, virtual devices, topology, asset images and device search/export for a VDMS.")
public class DeviceController {

    private static final Logger log = LoggerFactory.getLogger(DeviceController.class);

    @Autowired
    DeviceService deviceService;

    @Autowired
    APIRequest apiRequest;

    @Autowired
    APICallClient apicallService;

    @Autowired
    DeviceSearchService deviceSearchService;

    @Autowired
    DeviceMonitorService deviceMonitorService;

    /**
     * Returns all devices for the given VDMS and docker.
     *
     * @param username   owning user
     * @param vdmsid     owning VDMS id
     * @param dockername docker (gateway) name to scope devices to
     * @return set of devices for the VDMS/docker
     */
    @Operation(summary = "List devices for a VDMS/docker",
            description = "Returns all devices for the given VDMS and docker.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Devices returned"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @GetMapping("/docker/{dockername}/devices")
    public Set<DeviceDTO> listAllDevicebyVdmsidAndDockerName(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @Parameter(description = "Docker (gateway) name to scope devices to") @PathVariable String dockername) {
        log.info("listAllDevicebyVdmsidAndDockerName username={} vdmsid={} dockername={}", username, vdmsid, dockername);
        return deviceService.listAllDevicebyVdmsidAndDockerName(username, vdmsid, dockername);
    }

    /**
     * Returns a paginated, filtered slice of devices for the given VDMS and docker.
     *
     * @param username   owning user
     * @param vdmsid     owning VDMS id
     * @param dockername docker (gateway) name to scope devices to
     * @param condition  filter condition to apply (default "all")
     * @param searchKey  search keyword to match against (default "null")
     * @param pageno     page number to return (default 1)
     * @param pagesize   number of devices per page (default 10)
     * @return matching page of devices
     */
    @Operation(summary = "Get a filtered page of devices",
            description = "Returns a paginated, filtered slice of devices for the given VDMS and docker.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Devices returned"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @GetMapping("/docker/{dockername}/getfilterdevice")
    public Page<DeviceDTO> getfilterdevice(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @Parameter(description = "Docker (gateway) name to scope devices to") @PathVariable String dockername,
            @Parameter(description = "Filter condition to apply") @RequestParam(defaultValue = "all") String condition,
            @Parameter(description = "Search keyword to match against") @RequestParam(defaultValue = "null") String searchKey,
            @Parameter(description = "Page number to return") @RequestParam(defaultValue = "1") Integer pageno,
            @Parameter(description = "Number of devices per page") @RequestParam(defaultValue = "10") Integer pagesize) {
        log.info("getfilterdevice username={} vdmsid={} dockername={}", username, vdmsid, dockername);
        return PageUtils.toPage(deviceService.getfilterdevices(username, vdmsid, dockername, condition, searchKey, pageno, pagesize), pageno, pagesize);
    }

    //new get method with subsystem parent device get initial
    /**
     * Returns a paginated set of subsystem parent devices for the given VDMS and docker.
     *
     * @param username   owning user
     * @param vdmsid     owning VDMS id
     * @param dockername docker (gateway) name to scope devices to
     * @param condition  filter condition to apply (default "all")
     * @param pageno     page number to return (default 1)
     * @param pagesize   number of devices per page (default 10)
     * @param assignee   assignee to filter by (default "all")
     * @return matching page of subsystem parent devices
     */
    @Operation(summary = "Get subsystem parent devices by pagination",
            description = "Returns a paginated set of subsystem parent devices for the given VDMS and docker.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Devices returned"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @GetMapping("/docker/{dockername}/getsubsystemparentdevicesbypagination")
    public Page<DeviceDTO> getSubsystemParentDevicesByPagination(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @Parameter(description = "Docker (gateway) name to scope devices to") @PathVariable String dockername,
            @Parameter(description = "Filter condition to apply") @RequestParam(defaultValue = "all") String condition,
            @Parameter(description = "Page number to return") @RequestParam(defaultValue = "1") Integer pageno,
            @Parameter(description = "Number of devices per page") @RequestParam(defaultValue = "10") Integer pagesize,
            @Parameter(description = "Assignee to filter by") @RequestParam(defaultValue = "all") String assignee) {
        log.info("getSubsystemParentDevicesByPagination username={} vdmsid={} dockername={}", username, vdmsid, dockername);
        try {
            var slice = deviceService.getSubsystemParentDevicesByPagination(username, vdmsid, dockername, condition, pageno, pagesize, assignee);
            long total = deviceService.getSubsystemParentDevicesCount(username, vdmsid, dockername, condition, assignee);
            return PageUtils.toPage(slice, pageno, pagesize, total);
        } catch (Exception e) {
            log.error("getSubsystemParentDevicesByPagination failed username={} vdmsid={} dockername={}: {}", username, vdmsid, dockername, e.getMessage(), e);
            throw e;
        }

    }

    //new get method with subsystem devices get
    /**
     * Returns a paginated set of subsystem devices under the given parent device.
     *
     * @param username   owning user
     * @param vdmsid     owning VDMS id
     * @param dockername docker (gateway) name to scope devices to
     * @param device_id  parent device whose subsystem devices are requested
     * @param condition  filter condition to apply (default "all")
     * @param pageno     page number to return (default 1)
     * @param pagesize   number of devices per page (default 10)
     * @param assignee   assignee to filter by (default "all")
     * @return matching page of subsystem devices
     */
    @Operation(summary = "Get subsystem devices by pagination",
            description = "Returns a paginated set of subsystem devices under the given parent device.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Devices returned"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @GetMapping("/docker/{dockername}/device/{device_id}/getsubsystemdevicesbypagination")
    public Page<DeviceDTO> getSubsystemDevicesByPagination(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @Parameter(description = "Docker (gateway) name to scope devices to") @PathVariable String dockername,
            @Parameter(description = "Parent device whose subsystem devices are requested") @PathVariable String device_id,
            @Parameter(description = "Filter condition to apply") @RequestParam(defaultValue = "all") String condition,
            @Parameter(description = "Page number to return") @RequestParam(defaultValue = "1") Integer pageno,
            @Parameter(description = "Number of devices per page") @RequestParam(defaultValue = "10") Integer pagesize,
            @Parameter(description = "Assignee to filter by") @RequestParam(defaultValue = "all") String assignee) {
        log.info("getSubsystemDevicesByPagination username={} vdmsid={} dockername={} device_id={}", username, vdmsid, dockername, device_id);
        return PageUtils.toPage(deviceService.getSubsystemDevicesByPagination(username, vdmsid, dockername, device_id, condition, pageno, pagesize, assignee), pageno, pagesize);
    }


    /**
     * Upserts a list of devices for the given VDMS and docker.
     *
     * @param devicesDto list of device payloads to create or update
     * @param username   owning user
     * @param vdmsid     owning VDMS id
     * @param dockername docker (gateway) name to scope devices to
     * @param assignee   assignee to associate with the devices (default "all")
     */
    @Operation(summary = "Upsert devices",
            description = "Creates or updates the given list of devices for the VDMS and docker.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Devices upserted"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PostMapping("/docker/{dockername}/devicesupsert")
    public void upsertDeviceListByVdmsIdAndDockerName(
            @RequestBody List<DeviceDTO> devicesDto,
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @Parameter(description = "Docker (gateway) name to scope devices to") @PathVariable String dockername,
            @Parameter(description = "Assignee to associate with the devices") @RequestParam(defaultValue = "all") String assignee) {
        log.info("upsertDeviceListByVdmsIdAndDockerName username={} vdmsid={} dockername={}", username, vdmsid, dockername);
        deviceService.upsertDeviceListByVdmsIdAndDockerName(devicesDto, username, vdmsid, dockername, assignee);
    }

    /**
     * Edits a single device identified by its id.
     *
     * @param username           owning user
     * @param vdmsid             owning VDMS id
     * @param dockername         docker (gateway) name to scope the device to
     * @param device_id          device to edit
     * @param devicedto          device payload with updated values
     * @param httpServletRequest current request, used to resolve tenant/VDMS context
     * @param assignee           assignee to associate with the device (default "all")
     * @return the updated device
     * @throws JSONException if the request payload cannot be parsed as JSON
     * @throws IOException   if reading the request or downstream I/O fails
     */
    @Operation(summary = "Edit a device by id",
            description = "Updates a single device identified by its id.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Device updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "404", description = "Device not found"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PostMapping("/docker/{dockername}/device/{device_id}/edit")
    public DeviceDTO editDeviceByDeviceId(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @Parameter(description = "Docker (gateway) name to scope the device to") @PathVariable String dockername,
            @Parameter(description = "Device to edit") @PathVariable String device_id,
            @RequestBody DeviceDTO devicedto, HttpServletRequest httpServletRequest,
            @Parameter(description = "Assignee to associate with the device") @RequestParam(defaultValue = "all") String assignee) throws JSONException, IOException {
        log.info("editDeviceByDeviceId username={} vdmsid={} dockername={} device_id={}", username, vdmsid, dockername, device_id);
        return deviceService.editDeviceByDeviceID(username, vdmsid, dockername, device_id, devicedto, httpServletRequest, assignee);
    }


    /**
     * Unlinks a vendor of the given type from a device.
     *
     * @param username           owning user
     * @param dockername         docker (gateway) name to scope the device to
     * @param phoneaccount       phone account associated with the vendor link
     * @param device_id          device to unlink the vendor from
     * @param vendor_type        type of vendor to unlink
     * @param httpServletRequest current request, used to resolve tenant/VDMS context
     */
    @Operation(summary = "Unlink a vendor from a device",
            description = "Unlinks a vendor of the given type from a device.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Vendor unlinked"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PutMapping("/docker/{dockername}/phoneaccount/{phoneaccount}/device/{device_id}/{vendor_type}")
    public void unlinkVendorByVendorIdAndDeviceId(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Docker (gateway) name to scope the device to") @PathVariable String dockername,
            @Parameter(description = "Phone account associated with the vendor link") @PathVariable String phoneaccount,
            @Parameter(description = "Device to unlink the vendor from") @PathVariable String device_id,
            @Parameter(description = "Type of vendor to unlink") @PathVariable String vendor_type, HttpServletRequest httpServletRequest) {
        log.info("unlinkVendorByVendorIdAndDeviceId username={} dockername={} phoneaccount={} device_id={} vendor_type={}", username, dockername, phoneaccount, device_id, vendor_type);
        deviceService.unlinkVendorByVendorIdAndDeviceId(username, dockername, phoneaccount, device_id, vendor_type, httpServletRequest);
    }

    /**
     * Links a vendor of the given type to a device using the supplied phonebook address.
     *
     * @param username           owning user
     * @param vdmsid             owning VDMS id
     * @param dockername         docker (gateway) name to scope the device to
     * @param device_id          device to link the vendor to
     * @param vendor_type        type of vendor to link
     * @param phonebookaddressdto phonebook address payload describing the vendor link
     * @param httpServletRequest current request, used to resolve tenant/VDMS context
     * @return identifier or status of the created vendor link
     */
    @Operation(summary = "Link a vendor to a device",
            description = "Links a vendor of the given type to a device using the supplied phonebook address.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Vendor linked"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PostMapping("/docker/{dockername}/phoneaccount/device/{device_id}/{vendor_type}/link")
    public String linkVendorByVendorIdAndDeviceId(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @Parameter(description = "Docker (gateway) name to scope the device to") @PathVariable String dockername,
            @Parameter(description = "Device to link the vendor to") @PathVariable String device_id,
            @Parameter(description = "Type of vendor to link") @PathVariable String vendor_type,
            @RequestBody PhonebookAddressDto phonebookaddressdto, HttpServletRequest httpServletRequest) {
        log.info("linkVendorByVendorIdAndDeviceId username={} vdmsid={} dockername={} device_id={} vendor_type={}", username, vdmsid, dockername, device_id, vendor_type);
        return deviceService.linkVendorByVendorIdAndDeviceId(username, vdmsid, dockername, device_id, phonebookaddressdto, vendor_type, httpServletRequest);
    }

    /**
     * Updates multiple devices in a single request.
     *
     * @param username           owning user
     * @param vdmsid             owning VDMS id
     * @param dockername         docker (gateway) name to scope devices to
     * @param multidevicedtos    set of multi-device update payloads
     * @param httpServletRequest current request, used to resolve tenant/VDMS context
     * @param assignee           assignee to associate with the devices (default "all")
     * @throws JSONException if a request payload cannot be parsed as JSON
     * @throws IOException   if reading the request or downstream I/O fails
     */
    @Operation(summary = "Update multiple devices",
            description = "Updates multiple devices in a single request.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Devices updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PutMapping("/docker/{dockername}/devices")
    public void multiDeviceUpdate(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @Parameter(description = "Docker (gateway) name to scope devices to") @PathVariable String dockername,
            @RequestBody Set<MultiDeviceDTO> multidevicedtos, HttpServletRequest httpServletRequest,
            @Parameter(description = "Assignee to associate with the devices") @RequestParam(defaultValue = "all") String assignee) throws JSONException, IOException {
        log.info("multiDeviceUpdate username={} vdmsid={} dockername={} count={}", username, vdmsid, dockername, multidevicedtos == null ? 0 : multidevicedtos.size());
        deviceService.multiDeviceUpdate(username, vdmsid, dockername, multidevicedtos, httpServletRequest, assignee);
    }

    /**
     * Applies a quick update (tag device or location) to matching devices.
     *
     * @param username              owning user
     * @param vdmsid                owning VDMS id
     * @param dockername            docker (gateway) name to scope devices to
     * @param tagDeviceOrLocationDTO payload describing the device/location tagging to apply
     * @param httpServletRequest    current request, used to resolve tenant/VDMS context
     * @param assignee              assignee to associate with the devices (default "all")
     * @return set of devices affected by the quick update
     * @throws IOException if downstream I/O fails
     */
    @Operation(summary = "Quick-update devices",
            description = "Applies a quick update (tag device or location) to matching devices.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Devices updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PostMapping("/docker/{dockername}/devices/quickupdate")
    public Set<DeviceDTO> quickUpdate(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @Parameter(description = "Docker (gateway) name to scope devices to") @PathVariable String dockername,
            @RequestBody TagDeviceOrLocationDTO tagDeviceOrLocationDTO, HttpServletRequest httpServletRequest,
            @Parameter(description = "Assignee to associate with the devices") @RequestParam(defaultValue = "all") String assignee) throws IOException {
        log.info("quickUpdate username={} vdmsid={} dockername={}", username, vdmsid, dockername);
        return deviceService.quickUpdate(username, vdmsid, dockername, tagDeviceOrLocationDTO, httpServletRequest, assignee);
    }

    /**
     * Returns the device names for the given VDMS and docker.
     *
     * @param username   owning user
     * @param vdmsid     owning VDMS id
     * @param dockername docker (gateway) name to scope devices to
     * @return set of devices carrying their names
     */
    @Operation(summary = "Get device names",
            description = "Returns the device names for the given VDMS and docker.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Device names returned"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @GetMapping("/docker/{dockername}/device/names")
    public Set<DeviceDTO> getDeviceNamesByVdmsIdAndDockerName(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @Parameter(description = "Docker (gateway) name to scope devices to") @PathVariable String dockername) {
        log.info("getDeviceNamesByVdmsIdAndDockerName username={} vdmsid={} dockername={}", username, vdmsid, dockername);
        return deviceService.getDeviceNamesByVdmsIdAndDockerName(username, vdmsid, dockername);
    }

//	@RequestMapping(method = RequestMethod.POST , value = "/user/{username}/vdms/{vdmsid}/docker/{dockername}/virtual-device")
//	public String addVirtualDeviceByVdmsIdAndDockerName(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String dockername ,@RequestBody DeviceDTO virtualdevicedto)
//	{
//		return deviceService.addVirtualDeviceByVdmsIdAndDockerName(username ,vdmsid, dockername ,virtualdevicedto);
//	}

    //************New Code Changes**********************************************8
    /**
     * Adds one or more virtual devices, optionally with attached asset images.
     *
     * @param username           owning user
     * @param vdmsid             owning VDMS id
     * @param dockername         docker (gateway) name to scope devices to
     * @param asset_images       optional asset image files to attach
     * @param virtualDevicesDTO  serialized virtual device definitions to add
     * @param httpServletRequest current request, used to resolve tenant/VDMS context
     * @param assignee           assignee to associate with the devices (default "all")
     */
    @Operation(summary = "Add virtual devices",
            description = "Adds one or more virtual devices, optionally with attached asset images.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Virtual devices added"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PostMapping("/docker/{dockername}/addvirtualdevice")
    public void addVirtualDeviceByVdmsIdAndDockerName(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @Parameter(description = "Docker (gateway) name to scope devices to") @PathVariable String dockername,
            @Parameter(description = "Optional asset image files to attach") @RequestParam(value = "images", required = false) List<MultipartFile> asset_images,
            @Parameter(description = "Serialized virtual device definitions to add") @RequestParam(value = "virtual_devices") String virtualDevicesDTO, HttpServletRequest httpServletRequest,
            @Parameter(description = "Assignee to associate with the devices") @RequestParam(defaultValue = "all") String assignee) {
        log.info("addVirtualDeviceByVdmsIdAndDockerName username={} vdmsid={} dockername={}", username, vdmsid, dockername);
        deviceService.addVirtualDeviceByVdmsIdAndDockerName(username, vdmsid, dockername, virtualDevicesDTO, asset_images, httpServletRequest, assignee);
    }

    //	@RequestMapping(method = RequestMethod.POST , value = "/user/{username}/vdms/{vdmsid}/docker/{dockername}/virtual-device/{virtual_device_id}")
//	public DeviceDTO editVirtualDeviceByVirtualDeviceId(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String dockername ,@PathVariable String virtual_device_id ,@RequestBody DeviceDTO virtualdevicedto) throws IOException
//	{
//		return deviceService.editVirtualDeviceByVirtualDeviceId(username ,vdmsid ,dockername ,virtual_device_id ,virtualdevicedto);
//	}
    //************New Code Changes**********************************************8
    /**
     * Edits a set of existing virtual devices.
     *
     * @param username           owning user
     * @param vdmsid             owning VDMS id
     * @param dockername         docker (gateway) name to scope devices to
     * @param virtualDevices     set of virtual device payloads with updated values
     * @param httpServletRequest current request, used to resolve tenant/VDMS context
     * @throws IOException if downstream I/O fails
     */
    @Operation(summary = "Edit virtual devices",
            description = "Edits a set of existing virtual devices.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Virtual devices updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PostMapping("/docker/{dockername}/updatevirtualdevice")
    public void editVirtualDeviceByVirtualDeviceId(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @Parameter(description = "Docker (gateway) name to scope devices to") @PathVariable String dockername,
            @RequestBody Set<DeviceDTO> virtualDevices, HttpServletRequest httpServletRequest) throws IOException {
        log.info("editVirtualDeviceByVirtualDeviceId username={} vdmsid={} dockername={}", username, vdmsid, dockername);
        deviceService.editVirtualDeviceByVirtualDeviceId(username, vdmsid, dockername, virtualDevices, httpServletRequest);
    }


    /**
     * Deletes a virtual device by its id.
     *
     * @param username          owning user
     * @param vdmsid            owning VDMS id
     * @param dockername        docker (gateway) name to scope the device to
     * @param virtual_device_id virtual device to delete
     * @param assignee          assignee scope for the deletion (default "all")
     */
    @Operation(summary = "Delete a virtual device",
            description = "Deletes a virtual device by its id.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Virtual device deleted"),
            @ApiResponse(responseCode = "404", description = "Virtual device not found"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @DeleteMapping("/docker/{dockername}/virtual-device/{virtual_device_id}")
    public void deleteVirtualDeviceByVirtualDeviceId(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @Parameter(description = "Docker (gateway) name to scope the device to") @PathVariable String dockername,
            @Parameter(description = "Virtual device to delete") @PathVariable String virtual_device_id,
            @Parameter(description = "Assignee scope for the deletion") @RequestParam(defaultValue = "all") String assignee) {
        log.info("deleteVirtualDeviceByVirtualDeviceId username={} vdmsid={} dockername={} virtual_device_id={}", username, vdmsid, dockername, virtual_device_id);
        deviceService.deleteVirtualDeviceByVirtualDeviceId(username, vdmsid, dockername, virtual_device_id, assignee);
    }

    /**
     * Soft-deletes the given devices by their ids.
     *
     * @param username           owning user
     * @param vdmsid             owning VDMS id
     * @param dockername         docker (gateway) name to scope devices to
     * @param deviceIds          ids of the devices to delete
     * @param httpServletRequest current request, used to resolve tenant/VDMS context
     * @param assignee           assignee scope for the deletion (default "all")
     */
    @Operation(summary = "Delete devices by ids",
            description = "Soft-deletes the given devices by their ids so they can be reactivated later.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Devices deleted"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @DeleteMapping("/docker/{dockername}/deletedevices")
    public void deleteDevicesById(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @Parameter(description = "Docker (gateway) name to scope devices to") @PathVariable String dockername,
            @RequestBody Set<String> deviceIds, HttpServletRequest httpServletRequest,
            @Parameter(description = "Assignee scope for the deletion") @RequestParam(defaultValue = "all") String assignee) {
        log.info("deleteDevicesById username={} vdmsid={} dockername={}", username, vdmsid, dockername);
//        deviceService.deleteDevicesById(username, vdmsid, dockername, deviceIds, httpServletRequest);
        deviceService.softDeleteDevicesById(username, vdmsid, dockername, deviceIds, httpServletRequest, assignee);
    }

    //Get Single device information
    /**
     * Returns the details of a single device by its id.
     *
     * @param username   owning user
     * @param vdmsid     owning VDMS id
     * @param dockername docker (gateway) name to scope the device to
     * @param device_id  device to retrieve
     * @return the requested device
     */
    @Operation(summary = "Get a device by id",
            description = "Returns the details of a single device by its id.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Device found"),
            @ApiResponse(responseCode = "404", description = "Device not found"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @GetMapping("/docker/{dockername}/device/{device_id}/getdevice")
    public DeviceDTO getDeviceByDeviceId(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @Parameter(description = "Docker (gateway) name to scope the device to") @PathVariable String dockername,
            @Parameter(description = "Device to retrieve") @PathVariable String device_id) {
        log.info("getDeviceByDeviceId username={} vdmsid={} dockername={} device_id={}", username, vdmsid, dockername, device_id);
        return deviceService.getDeviceByDeviceId(username, vdmsid, dockername, device_id);
    }

    //Sync Virtual Device Status
    /**
     * Syncs and updates the status of a virtual device by its id.
     *
     * @param username          owning user
     * @param vdmsid            owning VDMS id
     * @param dockername        docker (gateway) name to scope the device to
     * @param virtual_device_id virtual device whose status is synced
     * @param virtualdevicedto  payload carrying the new status values
     * @return the updated virtual device
     * @throws IOException if downstream I/O fails
     */
    @Operation(summary = "Sync virtual device status",
            description = "Syncs and updates the status of a virtual device by its id.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status synced"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "404", description = "Virtual device not found"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PutMapping("/docker/{dockername}/virtual-device/{virtual_device_id}/syncstatus")
    public DeviceDTO updateVirtualDeviceStatusByVirtualDeviceId(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @Parameter(description = "Docker (gateway) name to scope the device to") @PathVariable String dockername,
            @Parameter(description = "Virtual device whose status is synced") @PathVariable String virtual_device_id,
            @RequestBody DeviceDTO virtualdevicedto) throws IOException {
        log.info("updateVirtualDeviceStatusByVirtualDeviceId username={} vdmsid={} dockername={} virtual_device_id={}", username, vdmsid, dockername, virtual_device_id);
        return deviceService.updateVirtualDeviceStatusByVirtualDeviceId(username, vdmsid, dockername, virtual_device_id, virtualdevicedto);
    }

    /**
     * Returns product details for a hard-coded sample product id (test endpoint).
     *
     * @return the product details for the sample product
     */
    @Operation(summary = "Get sample product details",
            description = "Returns product details for a hard-coded sample product id. Test endpoint.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product details returned"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @GetMapping(value = "/test/product")
    public ProductDTO test() {
        log.info("test called");
        String product_id = "6aeeae74-2855-4b67-943e-49d979a45abf";
        return apicallService.getProductDetailsByProductId(product_id);
    }


    /**
     * Returns device counts (e.g. by status) for the given VDMS and docker.
     *
     * @param username   owning user
     * @param vdmsid     owning VDMS id
     * @param dockername docker (gateway) name to scope devices to
     * @param assignee   assignee to filter the counts by (default "all")
     * @return map of category to device count
     */
    @Operation(summary = "Get device counts",
            description = "Returns device counts (e.g. by status) for the given VDMS and docker.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Counts returned"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @GetMapping("/docker/{dockername}/getdevicecount")
    public Map<String, Integer> getDeviceCount(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @Parameter(description = "Docker (gateway) name to scope devices to") @PathVariable String dockername,
            @Parameter(description = "Assignee to filter the counts by") @RequestParam(defaultValue = "all") String assignee) {
        log.info("getDeviceCount username={} vdmsid={} dockername={}", username, vdmsid, dockername);
        return deviceService.getDeviceCount(username, vdmsid, dockername, assignee);
    }

    //listing all devices for snmp topology
    /**
     * Returns all devices arranged for the SNMP topology view.
     *
     * @param username   owning user
     * @param vdmsid     owning VDMS id
     * @param dockername docker (gateway) name to scope devices to
     * @return list of topology device entries
     */
    @Operation(summary = "List topology devices",
            description = "Returns all devices arranged for the SNMP topology view.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Topology devices returned"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @GetMapping("/docker/{dockername}/devicetopology")
    public List<DeviceTopologyDTO> listTopologyDevicesByDockerName(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @Parameter(description = "Docker (gateway) name to scope devices to") @PathVariable String dockername) {
        log.info("listTopologyDevicesByDockerName username={} vdmsid={} dockername={}", username, vdmsid, dockername);
        return deviceService.listTopologyDevicesByDockerName(username, vdmsid, dockername);
    }

    /**
     * Updates the topology positions of the given devices.
     *
     * @param username           owning user
     * @param vdmsid             owning VDMS id
     * @param dockername         docker (gateway) name to scope devices to
     * @param devicePositions    devices with their updated position coordinates
     * @param httpServletRequest current request, used to resolve tenant/VDMS context
     */
    @Operation(summary = "Update device positions",
            description = "Updates the topology positions of the given devices.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Positions updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PostMapping("/docker/{dockername}/updatedeviceposition")
    public void updateDevicePosition(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @Parameter(description = "Docker (gateway) name to scope devices to") @PathVariable String dockername,
            @RequestBody List<DeviceDTO> devicePositions, HttpServletRequest httpServletRequest) {
        log.info("updateDevicePosition username={} vdmsid={} dockername={}", username, vdmsid, dockername);
        deviceService.updateDevicePosition(devicePositions, vdmsid, username, httpServletRequest);
    }

    // Device list by docker name for integration
    /**
     * Returns the device list for the given docker, intended for integration consumers.
     *
     * @param dockername docker (gateway) name whose devices are listed
     * @return list of devices for the docker
     */
    @Operation(summary = "List devices for integration",
            description = "Returns the device list for the given docker, intended for integration consumers.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Devices returned"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @GetMapping("/docker/{dockername}/devicelistintegration")
    public List<DeviceDTO> listDevicebyDockerIntegration(
            @Parameter(description = "Docker (gateway) name whose devices are listed") @PathVariable String dockername) {
        log.info("listDevicebyDockerIntegration dockername={}", dockername);
        return deviceService.listDevicebyDockerIntegration(dockername);
    }

    //Get Gateway ID
    /**
     * Returns the gateway id for the given docker.
     *
     * @param dockername docker (gateway) name to resolve
     * @return the gateway id
     */
    @Operation(summary = "Get gateway id",
            description = "Returns the gateway id for the given docker.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Gateway id returned"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @GetMapping("/docker/{dockername}/getgatewayid")
    public String getGatewayId(
            @Parameter(description = "Docker (gateway) name to resolve") @PathVariable String dockername) {
        log.info("getGatewayId dockername={}", dockername);
        return deviceService.getGatewayId(dockername);
    }

    /**
     * Updates the device topology for the given VDMS and docker.
     *
     * @param username           owning user
     * @param vdmsid             owning VDMS id
     * @param dockername         docker (gateway) name to scope the topology to
     * @param devices            topology device entries to persist
     * @param httpServletRequest current request, used to resolve tenant/VDMS context
     */
    @Operation(summary = "Update device topology",
            description = "Updates the device topology for the given VDMS and docker.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Topology updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PostMapping("/docker/{dockername}/updatetopology")
    public void updateTopology(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @Parameter(description = "Docker (gateway) name to scope the topology to") @PathVariable String dockername,
            @RequestBody List<DeviceTopologyDTO> devices, HttpServletRequest httpServletRequest) {
        log.info("updateTopology username={} vdmsid={} dockername={}", username, vdmsid, dockername);
        deviceService.updateTopology(username, vdmsid, dockername, devices, httpServletRequest);
    }

    //reset topology
    /**
     * Resets the device topology for the given docker.
     *
     * @param username           owning user
     * @param vdmsid             owning VDMS id
     * @param dockername         docker (gateway) name whose topology is reset
     * @param httpServletRequest current request, used to resolve tenant/VDMS context
     */
    @Operation(summary = "Reset device topology",
            description = "Resets the device topology for the given docker.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Topology reset"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PostMapping("/docker/{dockername}/resettopology")
    public void resetTopology(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @Parameter(description = "Docker (gateway) name whose topology is reset") @PathVariable String dockername, HttpServletRequest httpServletRequest) {
        log.info("resetTopology username={} vdmsid={} dockername={}", username, vdmsid, dockername);
        deviceService.resetTopologyByDockername(username, vdmsid, dockername, httpServletRequest);
    }

    //Get All sensors tagged to a device
    /**
     * Returns all sensors tagged to the given device.
     *
     * @param username   owning user
     * @param vdmsid     owning VDMS id
     * @param dockername docker (gateway) name to scope the device to
     * @param device_id  device whose sensors are requested
     * @return the sensors associated with the device
     */
    @Operation(summary = "Get device sensors",
            description = "Returns all sensors tagged to the given device.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sensors returned"),
            @ApiResponse(responseCode = "404", description = "Device not found"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @GetMapping("/docker/{dockername}/device/{device_id}/getalldevicesensors")
    public AllSensorsDTO getDeviceSensors(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @Parameter(description = "Docker (gateway) name to scope the device to") @PathVariable String dockername,
            @Parameter(description = "Device whose sensors are requested") @PathVariable String device_id) {
        log.info("getDeviceSensors username={} vdmsid={} dockername={} device_id={}", username, vdmsid, dockername, device_id);
        return deviceService.getDeviceSensors(username, vdmsid, dockername, device_id);
    }

    //Get Parent Device by Pagination
//	@RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdmsid}/getparentdevicebypagination")
//	public Set<DeviceDTO> getParentDeviceByPagination(@PathVariable String username, @PathVariable String vdmsid,
//			@RequestParam(defaultValue = "null") String searchKey, @RequestParam(defaultValue = "1") Integer pageno,
//			@RequestParam(defaultValue = "10") Integer pagesize, @RequestBody Set<String> dockernames)
//	{
//		return deviceService.getParentDeviceByPagination(username, vdmsid, searchKey, pageno, pagesize, dockernames);
//	}

    /**
     * Returns a paginated set of parent devices for the given VDMS, optionally filtered.
     *
     * @param username             owning user
     * @param vdmsid               owning VDMS id
     * @param searchKey            search keyword to match against (default "null")
     * @param pageno               page number to return (default 1)
     * @param pagesize             number of devices per page (default 10)
     * @param dockernames          docker names to filter by (default "all")
     * @param types                device types to filter by (default "all")
     * @param virtual_device_types virtual device types to filter by (default "all")
     * @return matching page of parent devices
     */
    @Operation(summary = "Get parent devices by pagination",
            description = "Returns a paginated set of parent devices for the given VDMS, optionally filtered by docker, type and virtual device type.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Parent devices returned"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PostMapping("/getparentdevicebypagination")
    public Page<DeviceDTO> getParentDeviceByPagination(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @Parameter(description = "Search keyword to match against") @RequestParam(defaultValue = "null") String searchKey,
            @Parameter(description = "Page number to return") @RequestParam(defaultValue = "1") Integer pageno,
            @Parameter(description = "Number of devices per page") @RequestParam(defaultValue = "10") Integer pagesize,
            @Parameter(description = "Docker names to filter by") @RequestParam(defaultValue = "all") Set<String> dockernames,
            @Parameter(description = "Device types to filter by") @RequestParam(defaultValue = "all") Set<String> types,
            @Parameter(description = "Virtual device types to filter by") @RequestParam(defaultValue = "all") Set<String> virtual_device_types) {
        log.info("getParentDeviceByPagination username={} vdmsid={}", username, vdmsid);
        return PageUtils.toPage(deviceService.getParentDeviceByPagination(username, vdmsid, searchKey, pageno, pagesize, dockernames, types, virtual_device_types), pageno, pagesize);
    }

    //Get Parent Device by Id
    /**
     * Returns parent device details for the supplied parent devices.
     *
     * @param username           owning user
     * @param vdmsid             owning VDMS id
     * @param dockername         docker (gateway) name to scope devices to
     * @param parent_devices     parent devices to resolve details for
     * @param httpServletRequest current request, used to resolve tenant/VDMS context
     * @return set of resolved parent devices
     */
    @Operation(summary = "Get parent devices by id",
            description = "Returns parent device details for the supplied parent devices.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Parent devices returned"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PostMapping("/docker/{dockername}/getparentdevice")
    public Set<DeviceDTO> getParentDeviceById(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @Parameter(description = "Docker (gateway) name to scope devices to") @PathVariable String dockername,
            @RequestBody Set<DeviceDTO> parent_devices, HttpServletRequest httpServletRequest) {
        log.info("getParentDeviceById username={} vdmsid={} dockername={}", username, vdmsid, dockername);
        return deviceService.getParentDeviceById(username, vdmsid, dockername, parent_devices, httpServletRequest);
    }

    //get subsystem parent device info
    /**
     * Returns information about the subsystem parent device for the given device.
     *
     * @param username   owning user
     * @param vdmsid     owning VDMS id
     * @param dockername docker (gateway) name to scope the device to
     * @param device_id  device whose subsystem parent is requested
     * @param parent_id  id of the subsystem parent device
     * @return the subsystem parent device details
     */
    @Operation(summary = "Get subsystem parent device info",
            description = "Returns information about the subsystem parent device for the given device.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Parent device info returned"),
            @ApiResponse(responseCode = "404", description = "Device not found"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @GetMapping("/docker/{dockername}/device/{device_id}/parent/{parent_id}/getsubsystemparentdeviceinfo")
    public DeviceDTO getSubsystemParentDeviceInfo(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @Parameter(description = "Docker (gateway) name to scope the device to") @PathVariable String dockername,
            @Parameter(description = "Device whose subsystem parent is requested") @PathVariable String device_id,
            @Parameter(description = "Id of the subsystem parent device") @PathVariable String parent_id) {
        log.info("getSubsystemParentDeviceInfo username={} vdmsid={} dockername={} device_id={} parent_id={}", username, vdmsid, dockername, device_id, parent_id);
        return deviceService.getSubsystemParentDeviceInfo(username, vdmsid, dockername, device_id, parent_id);
    }

    //update device matched product info
    /**
     * Updates the matched product information for a device.
     *
     * @param username           owning user
     * @param vdmsid             owning VDMS id
     * @param dockername         docker (gateway) name to scope the device to
     * @param device             device payload carrying the matched product info
     * @param httpServletRequest current request, used to resolve tenant/VDMS context
     */
    @Operation(summary = "Update matched device product",
            description = "Updates the matched product information for a device.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Matched product updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PostMapping("/docker/{dockername}/updatematcheddeviceproduct")
    public void updateMatchedDeviceProduct(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @Parameter(description = "Docker (gateway) name to scope the device to") @PathVariable String dockername,
            @RequestBody DeviceDTO device, HttpServletRequest httpServletRequest) {
        log.info("updateMatchedDeviceProduct username={} vdmsid={} dockername={}", username, vdmsid, dockername);
        deviceService.updateMatchedDeviceProduct(username, vdmsid, dockername, device, httpServletRequest);
    }

    //search device by specific column or all columns
    /**
     * Searches devices by a specific column or across all columns.
     *
     * @param username       owning user
     * @param vdmsid         owning VDMS id
     * @param dockername     docker (gateway) name to scope devices to
     * @param condition      search condition to apply (default "all")
     * @param pageno         page number to return (default 1)
     * @param pagesize       number of devices per page (default 10)
     * @param search_details map describing the search criteria
     * @return matching page of devices
     */
    @Operation(summary = "Search devices",
            description = "Searches devices by a specific column or across all columns.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Devices returned"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PostMapping("/docker/{dockername}/searchdevices")
    public Page<DeviceDTO> searchDevices(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @Parameter(description = "Docker (gateway) name to scope devices to") @PathVariable String dockername,
            @Parameter(description = "Search condition to apply") @RequestParam(defaultValue = "all") String condition,
            @Parameter(description = "Page number to return") @RequestParam(defaultValue = "1") Integer pageno,
            @Parameter(description = "Number of devices per page") @RequestParam(defaultValue = "10") Integer pagesize,
            @RequestBody Map<String, Object> search_details) {
        log.info("searchDevices username={} vdmsid={} dockername={}", username, vdmsid, dockername);
        return PageUtils.toPage(deviceSearchService.searchDevices(username, vdmsid, dockername, condition, pageno, pagesize, search_details), pageno, pagesize);
    }

    //sort device by specific column
    /**
     * Sorts devices by a specific column.
     *
     * @param username     owning user
     * @param vdmsid       owning VDMS id
     * @param dockername   docker (gateway) name to scope devices to
     * @param condition    sort condition to apply (default "all")
     * @param pageno       page number to return (default 1)
     * @param pagesize     number of devices per page (default 10)
     * @param sort_details map describing the sort criteria
     * @return matching page of sorted devices
     */
    @Operation(summary = "Sort devices",
            description = "Sorts devices by a specific column.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Devices returned"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PostMapping("/docker/{dockername}/sortdevices")
    public Page<DeviceDTO> sortDevices(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @Parameter(description = "Docker (gateway) name to scope devices to") @PathVariable String dockername,
            @Parameter(description = "Sort condition to apply") @RequestParam(defaultValue = "all") String condition,
            @Parameter(description = "Page number to return") @RequestParam(defaultValue = "1") Integer pageno,
            @Parameter(description = "Number of devices per page") @RequestParam(defaultValue = "10") Integer pagesize,
            @RequestBody Map<String, Object> sort_details) {
        log.info("sortDevices username={} vdmsid={} dockername={}", username, vdmsid, dockername);
        return PageUtils.toPage(deviceSearchService.sortDevices(username, vdmsid, dockername, condition, pageno, pagesize, sort_details), pageno, pagesize);
    }

    //filter devices by specific or multiple columns
    /**
     * Filters devices by one or multiple columns.
     *
     * @param username       owning user
     * @param vdmsid         owning VDMS id
     * @param dockername     docker (gateway) name to scope devices to
     * @param condition      filter condition to apply (default "all")
     * @param pageno         page number to return (default 1)
     * @param pagesize       number of devices per page (default 10)
     * @param filter_details list of maps describing the filter criteria
     * @return matching page of filtered devices
     */
    @Operation(summary = "Filter devices",
            description = "Filters devices by one or multiple columns.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Devices returned"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PostMapping("/docker/{dockername}/filterdevices")
    public Page<DeviceDTO> filterDevices(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @Parameter(description = "Docker (gateway) name to scope devices to") @PathVariable String dockername,
            @Parameter(description = "Filter condition to apply") @RequestParam(defaultValue = "all") String condition,
            @Parameter(description = "Page number to return") @RequestParam(defaultValue = "1") Integer pageno,
            @Parameter(description = "Number of devices per page") @RequestParam(defaultValue = "10") Integer pagesize,
            @RequestBody List<Map<String, Object>> filter_details) {
        log.info("filterDevices username={} vdmsid={} dockername={}", username, vdmsid, dockername);
        return PageUtils.toPage(deviceSearchService.filterDevices(username, vdmsid, dockername, condition, pageno, pagesize, filter_details), pageno, pagesize);
    }

    /**
     * Archives or unarchives the given devices.
     *
     * @param username           owning user
     * @param vdmsid             owning VDMS id
     * @param dockername         docker (gateway) name to scope devices to
     * @param archive            archive flag (default 1 to archive)
     * @param deviceIds          ids of the devices to archive/unarchive
     * @param httpServletRequest current request, used to resolve tenant/VDMS context
     * @param assignee           assignee scope for the operation (default "all")
     */
    @Operation(summary = "Archive or unarchive devices",
            description = "Archives or unarchives the given devices, depending on the archive flag.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Devices archived"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PostMapping("/docker/{dockername}/archivedevices")
    public void archiveDevices(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @Parameter(description = "Docker (gateway) name to scope devices to") @PathVariable String dockername,
            @Parameter(description = "Archive flag (1 to archive)") @RequestParam(defaultValue = "1") Integer archive,
            @RequestBody Set<String> deviceIds, HttpServletRequest httpServletRequest,
            @Parameter(description = "Assignee scope for the operation") @RequestParam(defaultValue = "all") String assignee) {
        log.info("archiveDevices username={} vdmsid={} dockername={} archive={}", username, vdmsid, dockername, archive);
        deviceService.archiveDevices(username, vdmsid, dockername, archive, deviceIds, httpServletRequest, assignee);
    }

    /**
     * Returns device information matching the supplied custom field values.
     *
     * @param username      owning user
     * @param vdmsid        owning VDMS id
     * @param dockername    docker (gateway) name to scope devices to
     * @param custom_fields JSON object of custom field values to match
     * @return list of matching devices
     */
    @Operation(summary = "Get devices by custom fields",
            description = "Returns device information matching the supplied custom field values.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Devices returned"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PostMapping("/docker/{dockername}/getdeviceinfobycustomfields")
    public List<DeviceDTO> getDeviceInfoByCustomFields(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @Parameter(description = "Docker (gateway) name to scope devices to") @PathVariable String dockername,
            @RequestBody com.alibaba.fastjson.JSONObject custom_fields) {
        log.info("getDeviceInfoByCustomFields username={} vdmsid={} dockername={}", username, vdmsid, dockername);
        return deviceSearchService.getDeviceInfoByCustomFields(username, vdmsid, dockername, custom_fields);
    }

    //multiple keyword search sort filter
    /**
     * Returns devices matching a combined multi-keyword search, sort and filter request.
     *
     * @param username                   owning user
     * @param vdmsid                     owning VDMS id
     * @param dockername                 docker (gateway) name to scope devices to
     * @param condition                  condition to apply (default "all")
     * @param pageno                     page number to return (default 1)
     * @param pagesize                   number of devices per page (default 10)
     * @param onboard_status             onboard status to filter by (default 123)
     * @param search_sort_filter_details JSON object describing the search/sort/filter criteria
     * @return matching page of devices
     */
    @Operation(summary = "Search, sort and filter devices",
            description = "Returns devices matching a combined multi-keyword search, sort and filter request.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Devices returned"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PostMapping("/docker/{dockername}/searchsortfilterdevices")
    public Page<DeviceDTO> multipleKeywordSearchSortFilterDevices(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @Parameter(description = "Docker (gateway) name to scope devices to") @PathVariable String dockername,
            @Parameter(description = "Condition to apply") @RequestParam(defaultValue = "all") String condition,
            @Parameter(description = "Page number to return") @RequestParam(defaultValue = "1") Integer pageno,
            @Parameter(description = "Number of devices per page") @RequestParam(defaultValue = "10") Integer pagesize,
            @Parameter(description = "Onboard status to filter by") @RequestParam(defaultValue = "123") Integer onboard_status,
            @RequestBody com.alibaba.fastjson.JSONObject search_sort_filter_details) {
        log.info("multipleKeywordSearchSortFilterDevices username={} vdmsid={} dockername={}", username, vdmsid, dockername);
        return PageUtils.toPage(deviceSearchService.multipleKeywordSearchSortFilterDevices(username, vdmsid, dockername, condition, pageno, pagesize, search_sort_filter_details, onboard_status), pageno, pagesize);
    }

    //return count of search sort filter result
    /**
     * Returns the count of devices matching a multi-keyword search, sort and filter request.
     *
     * @param username                   owning user
     * @param vdmsid                     owning VDMS id
     * @param dockername                 docker (gateway) name to scope devices to
     * @param condition                  condition to apply (default "all")
     * @param onboard_status             onboard status to filter by (default 123)
     * @param search_sort_filter_details JSON object describing the search/sort/filter criteria
     * @return count of matching devices
     */
    @Operation(summary = "Count search/sort/filter devices",
            description = "Returns the count of devices matching a multi-keyword search, sort and filter request.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Count returned"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PostMapping("/docker/{dockername}/searchsortfilterdevicescount")
    public String multipleKeywordSearchSortFilterDevicesCount(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @Parameter(description = "Docker (gateway) name to scope devices to") @PathVariable String dockername,
            @Parameter(description = "Condition to apply") @RequestParam(defaultValue = "all") String condition,
            @Parameter(description = "Onboard status to filter by") @RequestParam(defaultValue = "123") Integer onboard_status,
            @RequestBody com.alibaba.fastjson.JSONObject search_sort_filter_details) {
        log.info("multipleKeywordSearchSortFilterDevicesCount username={} vdmsid={} dockername={}", username, vdmsid, dockername);
        return deviceSearchService.multipleKeywordSearchSortFilterDevicesCount(username, vdmsid, dockername, condition,
                search_sort_filter_details, onboard_status);
    }

    /**
     * Returns the distinct assigned-user emails for devices on the given network.
     *
     * @param vdms_id      owning VDMS id
     * @param network_name network whose assigned users are requested
     * @return list of unique assigned-user email addresses
     */
    @Operation(summary = "Get assigned-user emails by network",
            description = "Returns the distinct assigned-user emails for devices on the given network.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Emails returned"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @GetMapping("/device/network/{network_name}/getassignedemail")
    public List<String> getUniqueAssignedUser(
            @Parameter(description = "Owning VDMS id") @RequestParam String vdms_id,
            @Parameter(description = "Network whose assigned users are requested") @PathVariable String network_name) {
        log.info("getUniqueAssignedUser vdms_id={} network_name={}", vdms_id, network_name);
        return deviceMonitorService.getUniqueAssignedUserEmail(vdms_id, network_name);
    }

    //Device Alert Message
    /**
     * Returns the device alert messages (conditions) for the given VDMS and docker.
     *
     * @param username   owning user
     * @param vdmsid     owning VDMS id
     * @param dockername docker (gateway) name to scope devices to
     * @return list of alert condition messages
     */
    @Operation(summary = "Get device alert messages",
            description = "Returns the device alert messages (conditions) for the given VDMS and docker.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Alert messages returned"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @GetMapping("/docker/{dockername}/getalertmessages")
    public List<ConditionsDTO> getDeviceAlertMessages(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @Parameter(description = "Docker (gateway) name to scope devices to") @PathVariable String dockername) {
        log.info("getDeviceAlertMessages username={} vdmsid={} dockername={}", username, vdmsid, dockername);
        return deviceService.getDeviceAlertMessages(username, vdmsid, dockername);
    }

    //search Parent devices by specific column or all columns
//	@RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdmsid}/docker/{dockername}/searchparentdevices")
//	public Set<DeviceDTO> searchParentDevices(@PathVariable String username, @PathVariable String vdmsid,
//	@RequestParam(defaultValue = "null") String searchKey, @RequestParam(defaultValue = "1") Integer pageno,
//	@RequestParam(defaultValue = "10") Integer pagesize, @RequestParam(defaultValue = "all") Set<String> dockernames,
//	@RequestParam(defaultValue = "all") Set<String> types, @RequestBody Map<String, Object> search_details)
//	{
//		return deviceSearchService.searchParentDevices(username, vdmsid, searchKey, pageno, pagesize, dockernames, types, search_details);
//	}

    /**
     * Upserts asset images for the given devices.
     *
     * @param username           owning user
     * @param vdms_id            owning VDMS id
     * @param device_ids         ids of the devices the images belong to
     * @param asset_images       optional asset image files to upsert
     * @param httpServletRequest current request, used to resolve tenant/VDMS context
     */
    @Operation(summary = "Upsert asset images",
            description = "Creates or updates asset images for the given devices.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Asset images upserted"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PostMapping("/upsertassetimages")
    public void upsertAssetImages(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdms_id,
            @Parameter(description = "Ids of the devices the images belong to") @RequestParam List<String> device_ids,
            @Parameter(description = "Optional asset image files to upsert") @RequestParam(value = "images", required = false) List<MultipartFile> asset_images, HttpServletRequest httpServletRequest) {
        log.info("upsertAssetImages username={} vdms_id={}", username, vdms_id);
        deviceService.upsertAssetImages(username, vdms_id, device_ids, asset_images, httpServletRequest);
    }

    /**
     * Sets a device's asset image to the given value (data URL or hosted URL), persisting it so it
     * survives a page reload.
     *
     * @param device_id device whose asset image is set
     * @param body      request body carrying the image value under the "image" key
     */
    @Operation(summary = "Set a device asset image",
            description = "Sets a device's asset image to the given value (data URL or hosted URL) so it survives a page reload.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Asset image set"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PostMapping("/device/{device_id}/setassetimage")
    public void setAssetImage(
            @Parameter(description = "Device whose asset image is set") @PathVariable String device_id,
            @RequestBody java.util.Map<String, String> body) {
        log.info("setAssetImage device_id={}", device_id);
        deviceService.setAssetImage(device_id, body != null ? body.get("image") : null);
    }

    /**
     * Deletes asset images for the given devices.
     *
     * @param username           owning user
     * @param vdms_id            owning VDMS id
     * @param deviceDTOS         devices whose asset images should be deleted
     * @param httpServletRequest current request, used to resolve tenant/VDMS context
     */
    @Operation(summary = "Delete asset images",
            description = "Deletes asset images for the given devices.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Asset images deleted"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @DeleteMapping("/deleteassetimages")
    public void deleteAssetImages(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdms_id,
            @RequestBody List<DeviceDTO> deviceDTOS, HttpServletRequest httpServletRequest) {
        log.info("deleteAssetImages username={} vdms_id={}", username, vdms_id);
        deviceService.deleteAssetImages(username, vdms_id, deviceDTOS, httpServletRequest);
    }

    /**
     * Deletes device images of a given category for the supplied devices.
     *
     * @param username           owning user
     * @param vdms_id            owning VDMS id
     * @param deviceDTOS         devices whose images should be deleted
     * @param category           image category to delete
     * @param httpServletRequest current request, used to resolve tenant/VDMS context
     */
    @Operation(summary = "Delete device images by category",
            description = "Deletes device images of a given category for the supplied devices.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Device images deleted"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @DeleteMapping("/deletedeviceimages")
    public void deleteDeviceImages(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdms_id,
            @RequestBody List<DeviceDTO> deviceDTOS,
            @Parameter(description = "Image category to delete") @RequestParam String category, HttpServletRequest httpServletRequest) {
        log.info("deleteDeviceImages username={} vdms_id={} category={}", username, vdms_id, category);
        deviceService.deleteDeviceImages(username, vdms_id, deviceDTOS, category,httpServletRequest);
    }

    /**
     * Returns the asset image URLs for the given device.
     *
     * @param username  owning user
     * @param vdms_id   owning VDMS id
     * @param device_id device whose asset image URLs are requested
     * @return serialized asset image URLs
     */
    @Operation(summary = "Get asset image URLs",
            description = "Returns the asset image URLs for the given device.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Asset image URLs returned"),
            @ApiResponse(responseCode = "404", description = "Device not found"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @GetMapping("/device/{device_id}/getassetimages")
    public String getAssetImageUrls(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdms_id,
            @Parameter(description = "Device whose asset image URLs are requested") @PathVariable String device_id) {
        log.info("getAssetImageUrls username={} vdms_id={} device_id={}", username, vdms_id, device_id);
        return deviceService.getAssetImageUrls(username, vdms_id, device_id);
    }

    /**
     * Returns the asset image URLs for the given device grouped by category.
     *
     * @param username  owning user
     * @param vdms_id   owning VDMS id
     * @param device_id device whose categorized asset image URLs are requested
     * @return serialized asset image URLs by category
     */
    @Operation(summary = "Get asset image URLs by category",
            description = "Returns the asset image URLs for the given device grouped by category.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Asset image URLs returned"),
            @ApiResponse(responseCode = "404", description = "Device not found"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @GetMapping("/device/{device_id}/getallassetimages")
    public String getAssetImageUrlsByCategory(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdms_id,
            @Parameter(description = "Device whose categorized asset image URLs are requested") @PathVariable String device_id) {
        log.info("getAssetImageUrlsByCategory username={} vdms_id={} device_id={}", username, vdms_id, device_id);
        return deviceService.getAssetImageUrlsCategory(username, vdms_id, device_id);
    }

    /**
     * Returns a paginated set of all devices in a group, optionally filtered.
     *
     * @param username     owning user
     * @param vdmsid       owning VDMS id
     * @param group        group whose devices are listed
     * @param searchkey    search keyword to match against (default "null")
     * @param pageno       page number to return (default 1)
     * @param pagesize     number of devices per page (default 10)
     * @param filterObject JSON object describing additional filters
     * @return matching page of devices
     */
    @Operation(summary = "Get all devices in a group",
            description = "Returns a paginated set of all devices in a group, optionally filtered.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Devices returned"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PostMapping("/group/{group}/getalldevicespagination")
    public Page<DeviceDTO> getAllDevicesPagination(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @Parameter(description = "Group whose devices are listed") @PathVariable String group,
            @Parameter(description = "Search keyword to match against") @RequestParam(defaultValue = "null") String searchkey,
            @Parameter(description = "Page number to return") @RequestParam(defaultValue = "1") Integer pageno,
            @Parameter(description = "Number of devices per page") @RequestParam(defaultValue = "10") Integer pagesize,
            @RequestBody JSONObject filterObject) {
        log.info("getAllDevicesPagination username={} vdmsid={} group={}", username, vdmsid, group);
        return PageUtils.toPage(deviceService.getAllDevicesPagination(username, vdmsid, group, searchkey, pageno, pagesize, filterObject), pageno, pagesize);
    }


    /**
     * Returns a paginated, filtered set of virtual devices for the given VDMS.
     *
     * @param username             owning user
     * @param vdmsid               owning VDMS id
     * @param searchKey            search keyword to match against (default "null")
     * @param pageno               page number to return (default 1)
     * @param pagesize             number of devices per page (default 10)
     * @param dockernames          docker names to filter by (default "all")
     * @param types                device types to filter by (default "all")
     * @param virtual_device_types virtual device types to filter by (default "all")
     * @return matching page of virtual devices
     */
    @Operation(summary = "Get filtered virtual devices by pagination",
            description = "Returns a paginated, filtered set of virtual devices for the given VDMS.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Virtual devices returned"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @GetMapping("/getfiltervirtualdevicesbypagination")
    public Page<DeviceDTO> getFilterVirtualDevicesByPagination(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @Parameter(description = "Search keyword to match against") @RequestParam(defaultValue = "null") String searchKey,
            @Parameter(description = "Page number to return") @RequestParam(defaultValue = "1") Integer pageno,
            @Parameter(description = "Number of devices per page") @RequestParam(defaultValue = "10") Integer pagesize,
            @Parameter(description = "Docker names to filter by") @RequestParam(defaultValue = "all") Set<String> dockernames,
            @Parameter(description = "Device types to filter by") @RequestParam(defaultValue = "all") Set<String> types,
            @Parameter(description = "Virtual device types to filter by") @RequestParam(defaultValue = "all") Set<String> virtual_device_types) {
        log.info("getFilterVirtualDevicesByPagination username={} vdmsid={}", username, vdmsid);
        return PageUtils.toPage(deviceService.getFilterVirtualDevicesByPagination(username, vdmsid, searchKey, pageno, pagesize, dockernames, types, virtual_device_types), pageno, pagesize);
    }

    /**
     * Returns the number of power-source topology connections for the given VDMS.
     *
     * @param username owning user
     * @param vdmsid   owning VDMS id
     * @return count of power-source topology connections
     */
    @Operation(summary = "Count power-source topology connections",
            description = "Returns the number of power-source topology connections for the given VDMS.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Count returned"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @GetMapping("/getpowersourcetopologyconnectionscount")
    public Integer getPowerSourceTopologyConnectionsCount(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid) {
        log.info("getPowerSourceTopologyConnectionsCount username={} vdmsid={}", username, vdmsid);
        return deviceService.getPowerSourceTopologyConnectionsCount(username, vdmsid);
    }

    /**
     * Returns a paginated view of the power-source topology for the given VDMS.
     *
     * @param username owning user
     * @param vdmsid   owning VDMS id
     * @param pageno   page number to return (default 1)
     * @param pagesize number of entries per page (default 10)
     * @return the requested page of the power-source topology
     */
    @Operation(summary = "Get power-source topology by pagination",
            description = "Returns a paginated view of the power-source topology for the given VDMS.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Topology page returned"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @GetMapping("/getpowersourcetopologybypagination")
    public PowerSourceTopologyDTO getPowerSourceTopologyByPagination(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @Parameter(description = "Page number to return") @RequestParam(defaultValue = "1") Integer pageno,
            @Parameter(description = "Number of entries per page") @RequestParam(defaultValue = "10") Integer pagesize) {
        log.info("getPowerSourceTopologyByPagination username={} vdmsid={}", username, vdmsid);
        return deviceService.getPowerSourceTopologyByPagination(username, vdmsid, pageno, pagesize);
    }

    /**
     * Returns a paginated set of assets (devices) at the given location.
     *
     * @param username    owning user
     * @param vdmsid      owning VDMS id
     * @param location_id location whose devices are requested
     * @param pageno      page number to return (default 1)
     * @param pagesize    number of devices per page (default 10)
     * @return matching page of devices at the location
     */
    @Operation(summary = "Get assets by location",
            description = "Returns a paginated set of assets (devices) at the given location.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Devices returned"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @GetMapping("/location/{location_id}/getdevicesbylocationid")
    public Page<DeviceDTO> getAssetsByLocationId(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @Parameter(description = "Location whose devices are requested") @PathVariable String location_id,
            @Parameter(description = "Page number to return") @RequestParam(defaultValue = "1") Integer pageno,
            @Parameter(description = "Number of devices per page") @RequestParam(defaultValue = "10") Integer pagesize) {
        log.info("getAssetsByLocationId username={} vdmsid={} location_id={}", username, vdmsid, location_id);
        return PageUtils.toPage(deviceService.getAssetsByLocationId(username, vdmsid, location_id, pageno, pagesize), pageno, pagesize);
    }

    /**
     * Returns the reboot status of the given device.
     *
     * @param username owning user
     * @param vdmsid   owning VDMS id
     * @param deviceid device whose reboot status is requested
     * @return the device reboot status
     */
    @Operation(summary = "Get device reboot status",
            description = "Returns the reboot status of the given device.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reboot status returned"),
            @ApiResponse(responseCode = "404", description = "Device not found"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @GetMapping("/device/{deviceid}/getdevicerebootstatus")
    public String getDeviceRebootStatus(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @Parameter(description = "Device whose reboot status is requested") @PathVariable String deviceid) {
        log.info("getDeviceRebootStatus username={} vdmsid={} deviceid={}", username, vdmsid, deviceid);
        return deviceService.getDeviceRebootStatus(username, vdmsid, deviceid);
    }

    /**
     * Upserts asset OCR images for the given devices.
     *
     * @param username           owning user
     * @param vdms_id            owning VDMS id
     * @param device_ids         ids of the devices the OCR images belong to
     * @param asset_ocr_images   optional asset OCR image files to upsert
     * @param httpServletRequest current request, used to resolve tenant/VDMS context
     */
    @Operation(summary = "Upsert asset OCR images",
            description = "Creates or updates asset OCR images for the given devices.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Asset OCR images upserted"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PostMapping("/upsertassetocrimages")
    public void upsertAssetOcrImages(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdms_id,
            @Parameter(description = "Ids of the devices the OCR images belong to") @RequestParam List<String> device_ids,
            @Parameter(description = "Optional asset OCR image files to upsert") @RequestParam(value = "images", required = false) List<MultipartFile> asset_ocr_images, HttpServletRequest httpServletRequest) {
        log.info("upsertAssetOcrImages username={} vdms_id={}", username, vdms_id);
        deviceService.upsertAssetOcrImages(username, vdms_id, device_ids, asset_ocr_images, httpServletRequest);
    }

    /**
     * Deletes asset OCR images for the given devices.
     *
     * @param username           owning user
     * @param vdms_id            owning VDMS id
     * @param deviceDTOS         devices whose asset OCR images should be deleted
     * @param httpServletRequest current request, used to resolve tenant/VDMS context
     */
    @Operation(summary = "Delete asset OCR images",
            description = "Deletes asset OCR images for the given devices.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Asset OCR images deleted"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @DeleteMapping("/deleteassetocrimages")
    public void deleteAssetOcrImages(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdms_id,
            @RequestBody List<DeviceDTO> deviceDTOS, HttpServletRequest httpServletRequest) {
        log.info("deleteAssetOcrImages username={} vdms_id={}", username, vdms_id);
        deviceService.deleteAssetOcrImages(username, vdms_id, deviceDTOS, httpServletRequest);
    }


    /**
     * Returns the asset OCR image URLs for the given device.
     *
     * @param username  owning user
     * @param vdms_id   owning VDMS id
     * @param device_id device whose asset OCR image URLs are requested
     * @return serialized asset OCR image URLs
     */
    @Operation(summary = "Get asset OCR image URLs",
            description = "Returns the asset OCR image URLs for the given device.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Asset OCR image URLs returned"),
            @ApiResponse(responseCode = "404", description = "Device not found"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @GetMapping("/device/{device_id}/getassetocrimages")
    public String getAssetOcrImageUrls(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdms_id,
            @Parameter(description = "Device whose asset OCR image URLs are requested") @PathVariable String device_id) {
        log.info("getAssetOcrImageUrls username={} vdms_id={} device_id={}", username, vdms_id, device_id);
        return deviceService.getAssetOcrImageUrls(username, vdms_id, device_id);
    }


    /**
     * Adds a single device to the given VDMS and docker.
     *
     * @param username           owning user
     * @param vdmsid             owning VDMS id
     * @param dockername         docker (gateway) name to scope the device to
     * @param deviceDto          device payload to create
     * @param httpServletRequest current request, used to resolve tenant/VDMS context
     * @return the created device
     */
    @Operation(summary = "Add a device",
            description = "Adds a single device to the given VDMS and docker.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Device created"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PostMapping("/docker/{dockername}/adddevice")
    public DeviceDTO addDevice(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @Parameter(description = "Docker (gateway) name to scope the device to") @PathVariable String dockername,
            @RequestBody DeviceDTO deviceDto, HttpServletRequest httpServletRequest) {
        log.info("addDevice username={} vdmsid={} dockername={}", username, vdmsid, dockername);
        return deviceService.addDevice(username, vdmsid, dockername, deviceDto, httpServletRequest);
    }

    /**
     * Exports the filtered devices as a report file (e.g. Excel) to the response stream.
     *
     * @param response                   response to which the exported file is written
     * @param username                   owning user
     * @param vdmsid                     owning VDMS id
     * @param dockername                 docker (gateway) name to scope devices to
     * @param condition                  condition to apply (default "all")
     * @param onboard_status             onboard status to filter by (default 123)
     * @param template_name              report template to use (default "simple_report")
     * @param file_type                  output file type (default "excel")
     * @param search_sort_filter_details JSON object describing the search/sort/filter criteria
     * @param email                      optional email to send the export to (default empty)
     * @param httpServletRequest         current request, used to resolve tenant/VDMS context
     * @throws IOException if writing the export or downstream I/O fails
     */
    @Operation(summary = "Export filtered devices",
            description = "Exports the filtered devices as a report file (e.g. Excel) to the response stream, optionally emailing the result.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Export written"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PostMapping("/docker/{dockername}/exportfiltereddevices")
    public void exportFilteredDevices(HttpServletResponse response,
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @Parameter(description = "Docker (gateway) name to scope devices to") @PathVariable String dockername,
            @Parameter(description = "Condition to apply") @RequestParam(defaultValue = "all") String condition,
            @Parameter(description = "Onboard status to filter by") @RequestParam(defaultValue = "123") Integer onboard_status,
            @Parameter(description = "Report template to use") @RequestParam(defaultValue = "simple_report") String template_name,
            @Parameter(description = "Output file type") @RequestParam(defaultValue = "excel") String file_type,
            @RequestBody com.alibaba.fastjson.JSONObject search_sort_filter_details,
            @Parameter(description = "Optional email to send the export to") @RequestParam(defaultValue = "") String email, HttpServletRequest httpServletRequest) throws IOException {
        log.info("exportFilteredDevices username={} vdmsid={} dockername={}", username, vdmsid, dockername);
        deviceService.exportFilteredDevices(response, username, vdmsid, dockername, condition, search_sort_filter_details, onboard_status,
                template_name, email, httpServletRequest, file_type);
    }

    /**
     * Imports assets from an uploaded .xlsx (the same column layout produced by exportfiltereddevices),
     * upserting by id. Returns {created, updated, failed, errors}.
     *
     * @param username   owning user
     * @param vdmsid     owning VDMS id
     * @param dockername docker (gateway) name to scope devices to
     * @param file       uploaded .xlsx file to import
     * @return map describing the import result (created, updated, failed, errors)
     * @throws java.io.IOException if reading the uploaded file fails
     */
    @Operation(summary = "Import assets from Excel",
            description = "Imports assets from an uploaded .xlsx (the column layout produced by exportfiltereddevices), upserting by id and returning created/updated/failed/errors.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Assets imported"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PostMapping("/docker/{dockername}/importassets")
    public java.util.Map<String, Object> importAssets(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @Parameter(description = "Docker (gateway) name to scope devices to") @PathVariable String dockername,
            @Parameter(description = "Uploaded .xlsx file to import") @RequestParam("file") org.springframework.web.multipart.MultipartFile file) throws java.io.IOException {
        log.info("importAssets username={} vdmsid={} dockername={} filename={}", username, vdmsid, dockername,
                file != null ? file.getOriginalFilename() : null);
        return deviceService.importAssetsFromExcel(file, vdmsid, dockername);
    }

    /**
     * Applies one set of field changes to many assets at once.
     *
     * @param username   owning user
     * @param vdmsid     owning VDMS id
     * @param dockername docker (gateway) name to scope devices to
     * @param body       request body carrying the ids and changes (e.g. {ids:[...], changes:{...}})
     * @return map describing the result of the bulk update
     */
    @Operation(summary = "Multi-update assets",
            description = "Applies one set of field changes to many assets at once.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Assets updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PostMapping("/docker/{dockername}/multiupdateassets")
    public java.util.Map<String, Object> multiUpdateAssets(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @Parameter(description = "Docker (gateway) name to scope devices to") @PathVariable String dockername,
            @RequestBody java.util.Map<String, Object> body) {
        log.info("multiUpdateAssets username={} vdmsid={} dockername={}", username, vdmsid, dockername);
        @SuppressWarnings("unchecked")
        java.util.List<String> ids = (java.util.List<String>) body.get("ids");
        @SuppressWarnings("unchecked")
        java.util.Map<String, Object> changes = (java.util.Map<String, Object>) body.get("changes");
        return deviceService.multiUpdateAssets(ids, changes);
    }

    /**
     * Syncs the onboard status of all devices in the given VDMS.
     *
     * @param vdmsid owning VDMS id
     */
    @Operation(summary = "Sync onboard status for all devices",
            description = "Syncs the onboard status of all devices in the given VDMS.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Onboard status synced"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @GetMapping("/syncdeviceonboardstatus")
    public void syncDeviceOnboardStatus(
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid) {
        log.info("syncDeviceOnboardStatus vdmsid={}", vdmsid);
        deviceService.syncDeviceOnboardStatus(vdmsid);
    }

    /**
     * Syncs the onboard status of a single device in the given VDMS.
     *
     * @param vdmsid    owning VDMS id
     * @param device_id device whose onboard status is synced
     */
    @Operation(summary = "Sync onboard status for a device",
            description = "Syncs the onboard status of a single device in the given VDMS.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Onboard status synced"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @GetMapping("/device/{device_id}/syncsingledeviceonboardstatus")
    public void syncSingleDeviceOnboardStatus(
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @Parameter(description = "Device whose onboard status is synced") @PathVariable String device_id) {
        log.info("syncSingleDeviceOnboardStatus vdmsid={} device_id={}", vdmsid, device_id);
        deviceService.syncSingleDeviceOnboardStatus(vdmsid, device_id);
    }

    /**
     * Updates the asset match details for a device.
     *
     * @param username           owning user
     * @param vdmsid             owning VDMS id
     * @param dockername         docker (gateway) name to scope the device to
     * @param deviceObject       JSON object carrying the asset match details
     * @param httpServletRequest current request, used to resolve tenant/VDMS context
     * @param assignee           assignee to associate with the device (default "all")
     * @return the updated device
     */
    @Operation(summary = "Update asset match details",
            description = "Updates the asset match details for a device.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Asset match details updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PostMapping("/docker/{dockername}/updateassetmatchdetails")
    public DeviceDTO updateAssetMatchDetails(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @Parameter(description = "Docker (gateway) name to scope the device to") @PathVariable String dockername,
            @RequestBody JSONObject deviceObject, HttpServletRequest httpServletRequest,
            @Parameter(description = "Assignee to associate with the device") @RequestParam(defaultValue = "all") String assignee) {
        log.info("updateAssetMatchDetails username={} vdmsid={} dockername={}", username, vdmsid, dockername);
        return deviceService.updateAssetMatchDetails(username, vdmsid, dockername, deviceObject, httpServletRequest, assignee);
    }

    /**
     * Upserts digital twin instruments for the given VDMS.
     *
     * @param username           owning user
     * @param vdmsid             owning VDMS id
     * @param filterObject       payload describing the digital twin instruments to upsert
     * @param httpServletRequest current request, used to resolve tenant/VDMS context
     */
    @Operation(summary = "Upsert digital twin instruments",
            description = "Creates or updates digital twin instruments for the given VDMS.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Digital twin instruments upserted"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PostMapping("/upsertdigitaltwininstruments")
    public void upsertDigitalTwin(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @RequestBody TagDeviceOrLocationDTO filterObject, HttpServletRequest httpServletRequest) {
        log.info("upsertDigitalTwin username={} vdmsid={}", username, vdmsid);
        deviceService.upsertDigitalTwin(username, vdmsid, filterObject, httpServletRequest);
    }

    /**
     * Deletes the digital twin associated with the given device.
     *
     * @param username           owning user
     * @param vdmsid             owning VDMS id
     * @param device_id          device whose digital twin should be deleted
     * @param httpServletRequest current request, used to resolve tenant/VDMS context
     */
    @Operation(summary = "Delete a digital twin",
            description = "Deletes the digital twin associated with the given device.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Digital twin deleted"),
            @ApiResponse(responseCode = "404", description = "Device not found"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @DeleteMapping("/device_id/{device_id}/deletedigitaltwin")
    public void deleteDigitalTwin(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @Parameter(description = "Device whose digital twin should be deleted") @PathVariable String device_id, HttpServletRequest httpServletRequest) {
        log.info("deleteDigitalTwin username={} vdmsid={} device_id={}", username, vdmsid, device_id);
        deviceService.deleteDigitalTwin(username, vdmsid, device_id, httpServletRequest);
    }


    /**
     * Applies a bulk edit to digital twin instruments, optionally with an image.
     *
     * @param username           owning user
     * @param vdmsid             owning VDMS id
     * @param data               serialized digital twin edit data (required)
     * @param image_url          optional image URL to associate
     * @param image              optional image file to associate
     * @param httpServletRequest current request, used to resolve tenant/VDMS context
     */
    @Operation(summary = "Multi-edit digital twin instruments",
            description = "Applies a bulk edit to digital twin instruments, optionally with an image.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Digital twin instruments updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PostMapping("/multieditdigitaltwininstruments")
    public void multiEditDigitalTwin(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @Parameter(description = "Serialized digital twin edit data") @RequestParam(required = true) String data,
            @Parameter(description = "Optional image URL to associate") @RequestParam(required = false) String image_url,
            @Parameter(description = "Optional image file to associate") @RequestParam(required = false) MultipartFile image, HttpServletRequest httpServletRequest) {
        log.info("multiEditDigitalTwin username={} vdmsid={}", username, vdmsid);
        deviceService.multiEditDigitalTwin(username, vdmsid, data, image_url, image, httpServletRequest);
    }

    /**
     * Exports filtered measuring instruments to the response stream.
     *
     * @param response                   response to which the exported file is written
     * @param username                   owning user
     * @param vdmsid                     owning VDMS id
     * @param dockername                 docker (gateway) name to scope devices to
     * @param condition                  condition to apply (default "all")
     * @param pageno                     page number to return (default 1)
     * @param pagesize                   number of devices per page (default 10)
     * @param onboard_status             onboard status to filter by (default 123)
     * @param search_sort_filter_details JSON object describing the search/sort/filter criteria
     * @throws IOException if writing the export or downstream I/O fails
     */
    @Operation(summary = "Export filtered measuring instruments",
            description = "Exports filtered measuring instruments to the response stream.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Export written"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PostMapping("/docker/{dockername}/exportfilteredmeasuringinstrument")
    public void exportFilteredMeasuringInstrument(HttpServletResponse response,
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @Parameter(description = "Docker (gateway) name to scope devices to") @PathVariable String dockername,
            @Parameter(description = "Condition to apply") @RequestParam(defaultValue = "all") String condition,
            @Parameter(description = "Page number to return") @RequestParam(defaultValue = "1") Integer pageno,
            @Parameter(description = "Number of devices per page") @RequestParam(defaultValue = "10") Integer pagesize,
            @Parameter(description = "Onboard status to filter by") @RequestParam(defaultValue = "123") Integer onboard_status,
            @RequestBody com.alibaba.fastjson.JSONObject search_sort_filter_details) throws IOException {
        log.info("exportFilteredMeasuringInstrument username={} vdmsid={} dockername={}", username, vdmsid, dockername);
        deviceService.exportFilteredMeasuringInstrument(response, username, vdmsid, dockername, condition, pageno, pagesize, search_sort_filter_details, onboard_status);
    }

    /**
     * Recomputes and updates device types, optionally scoped to a single VDMS.
     *
     * @param vdmsId optional VDMS id to scope the update to; when absent applies to all
     * @return response describing the outcome of the update
     */
    @Operation(summary = "Update device types",
            description = "Recomputes and updates device types, optionally scoped to a single VDMS; when no VDMS is supplied it applies to all.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Device types updated"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PutMapping("/vdms/updatedevicetype")
    public ResponseDTO updateDeviceTypes(
            @Parameter(description = "Optional VDMS id to scope the update to") @RequestParam(required = false) String vdmsId) {
        log.info("updateDeviceTypes vdmsId={}", vdmsId);
        return deviceService.updateDeviceTypeForAll(vdmsId);
    }

    /**
     * Toggles the do-not-disturb (DND) status of a device.
     *
     * @param device_id          device whose DND status is toggled (required)
     * @param is_dnd_enabled     whether DND should be enabled (required)
     * @param httpServletRequest current request, used to resolve tenant/VDMS context
     * @throws IOException if downstream I/O fails
     */
    @Operation(summary = "Toggle device DND status",
            description = "Toggles the do-not-disturb (DND) status of a device.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "DND status toggled"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PostMapping("/togglednd")
    public void toggleDndStatus(
            @Parameter(description = "Device whose DND status is toggled") @RequestParam(value = "device_id", required = true) String device_id,
            @Parameter(description = "Whether DND should be enabled") @RequestParam(value = "is_dnd_enabled", required = true) Boolean is_dnd_enabled, HttpServletRequest httpServletRequest) throws IOException {
        log.info("toggleDndStatus device_id={} is_dnd_enabled={}", device_id, is_dnd_enabled);
        deviceService.toggleDndStatus(device_id, is_dnd_enabled, httpServletRequest);
    }

//    // HAM Assets import changes ///
//    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdmsid}/docker/{dockername}/addvirtualdevicefromservicenow")
//    public void addVirtualDeviceByVdmsIdAndDockerNameFromITAM(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String dockername,
//                                                      @RequestBody Set<DeviceDTO> virtualDevicesDTO, HttpServletRequest httpServletRequest, @RequestParam(defaultValue = "all") String assignee) {
//        deviceService.addVirtualDeviceByVdmsIdAndDockerNameFromITAM(username, vdmsid, dockername, virtualDevicesDTO, httpServletRequest, assignee);
//    }

    /**
     * Updates a device's DND enabled flag and the associated timestamp.
     *
     * @param id       device id whose DND state is updated
     * @param timetamp timestamp to record for the DND change
     */
    @Operation(summary = "Update device DND status and timestamp",
            description = "Updates a device's DND enabled flag and the associated timestamp.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "DND status updated"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PostMapping("/deviceid/{id}/timetamp/{timetamp}/updatedndstatus")
    public void updatedndstatus(
            @Parameter(description = "Device id whose DND state is updated") @PathVariable String id,
            @Parameter(description = "Timestamp to record for the DND change") @PathVariable BigInteger timetamp) {
        log.info("updatedndstatus id={} timetamp={}", id, timetamp);
        deviceService.UpdateDeviceDndEnabledAndTimestamp(id, timetamp);
    }


    /**
     * Returns paginated custom device details for the given VDMS and docker.
     *
     * @param username     owning user
     * @param vdmsid       owning VDMS id
     * @param docker_name  docker (gateway) name to scope devices to
     * @param page_no      page number to return (default 1)
     * @param page_size    number of devices per page (default 10)
     * @param search_key   search keyword to match against (default "null")
     * @param profile_type profile type to use (default "internal")
     * @return list of devices with their custom details
     * @throws IOException if downstream I/O fails
     */
    @Operation(summary = "Get all device custom details",
            description = "Returns paginated custom device details for the given VDMS and docker.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Device details returned"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @GetMapping("/docker/{docker_name}/getalldevicedetails")
    public Page<DeviceDTO> getAllDeviceCustomDetails(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @Parameter(description = "Docker (gateway) name to scope devices to") @PathVariable String docker_name,
            @Parameter(description = "Page number to return") @RequestParam(defaultValue = "1") Integer page_no,
            @Parameter(description = "Number of devices per page") @RequestParam(defaultValue = "10") Integer page_size,
            @Parameter(description = "Search keyword to match against") @RequestParam(defaultValue = "null") String search_key,
            @Parameter(description = "Profile type to use") @RequestParam(defaultValue = "internal") String profile_type) throws IOException {
        log.info("getAllDeviceCustomDetails username={} vdmsid={} docker_name={}", username, vdmsid, docker_name);
        return PageUtils.toPage(deviceService.getAllDeviceCustomDetails(username, vdmsid, docker_name, page_no, page_size, search_key, profile_type), page_no, page_size);
    }

    /**
     * Returns paginated custom device details for the supplied device ids.
     *
     * @param docker_name docker (gateway) name to scope devices to
     * @param page_no     page number to return (default 1)
     * @param page_size   number of devices per page (default 10)
     * @param search_key  search keyword to match against (default "null")
     * @param device_ids  ids of the devices to return details for
     * @return list of devices with their custom details
     * @throws IOException if downstream I/O fails
     */
    @Operation(summary = "Get device custom details by ids",
            description = "Returns paginated custom device details for the supplied device ids.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Device details returned"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PostMapping("/docker/{docker_name}/getdevicedetailsbyids")
    public Page<DeviceDTO> getDeviceCustomDetails(
            @Parameter(description = "Docker (gateway) name to scope devices to") @PathVariable String docker_name,
            @Parameter(description = "Page number to return") @RequestParam(defaultValue = "1") Integer page_no,
            @Parameter(description = "Number of devices per page") @RequestParam(defaultValue = "10") Integer page_size,
            @Parameter(description = "Search keyword to match against") @RequestParam(defaultValue = "null") String search_key,
            @RequestBody List<String> device_ids) throws IOException {
        log.info("getDeviceCustomDetails docker_name={}", docker_name);
        return PageUtils.toPage(deviceService.getDeviceCustomDetails(docker_name, page_no, page_size, search_key, device_ids), page_no, page_size);
    }

    // This is not currently being used, was written when there was a filter page with asset_category and model in the edit profile section
    /**
     * Returns custom device details for the ids in the request body, optionally paginated.
     *
     * @param username      owning user
     * @param vdmsid        owning VDMS id
     * @param docker_name   docker (gateway) name to scope devices to
     * @param page_no       page number to return (default 1)
     * @param page_size     number of devices per page (default 10)
     * @param search_key    search keyword to match against (default "null")
     * @param has_pagination whether pagination is applied (default 0)
     * @param requestBody   JSON object carrying the device ids and filter criteria
     * @return list of devices with their custom details
     * @throws IOException if downstream I/O fails
     */
    @Operation(summary = "Get device custom details by ids (body)",
            description = "Returns custom device details for the ids in the request body, optionally paginated.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Device details returned"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PostMapping("/docker/{docker_name}/getdevicecustomdetailsbyids")
    public Page<DeviceDTO> getDeviceCustomDetailsByIds(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @Parameter(description = "Docker (gateway) name to scope devices to") @PathVariable String docker_name,
            @Parameter(description = "Page number to return") @RequestParam(defaultValue = "1") Integer page_no,
            @Parameter(description = "Number of devices per page") @RequestParam(defaultValue = "10") Integer page_size,
            @Parameter(description = "Search keyword to match against") @RequestParam(defaultValue = "null") String search_key,
            @Parameter(description = "Whether pagination is applied") @RequestParam(defaultValue = "0") Integer has_pagination,
            @RequestBody JSONObject requestBody) throws IOException {
        log.info("getDeviceCustomDetailsByIds username={} vdmsid={} docker_name={}", username, vdmsid, docker_name);
        return PageUtils.toPage(deviceService.getDeviceCustomDetailsByIds(username, vdmsid, docker_name, has_pagination, page_no, page_size, search_key, requestBody), page_no, page_size);
    }

    /**
     * Returns the device ids for the given docker, with pagination and optional select-all.
     *
     * @param docker_name   docker (gateway) name to scope devices to
     * @param page_no       page number to return (default 1)
     * @param page_size     number of devices per page (default 10)
     * @param search_key    search keyword to match against (default "null")
     * @param is_select_all whether all matching ids should be returned (default "false")
     * @return list of matching device ids
     */
    @Operation(summary = "Get all device ids",
            description = "Returns the device ids for the given docker, with pagination and optional select-all.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Device ids returned"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @GetMapping("/docker/{docker_name}/getalldeviceids")
    public List<String> getAllDeviceIds(
            @Parameter(description = "Docker (gateway) name to scope devices to") @PathVariable String docker_name,
            @Parameter(description = "Page number to return") @RequestParam(defaultValue = "1") Integer page_no,
            @Parameter(description = "Number of devices per page") @RequestParam(defaultValue = "10") Integer page_size,
            @Parameter(description = "Search keyword to match against") @RequestParam(defaultValue = "null") String search_key,
            @Parameter(description = "Whether all matching ids should be returned") @RequestParam(defaultValue = "false") String is_select_all){
        log.info("getAllDeviceIds docker_name={}", docker_name);
        return deviceService.getAllDeviceIds(docker_name, page_no, page_size, search_key, is_select_all);
    }

}

