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
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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
    @RequestMapping(method = RequestMethod.GET, value = "/docker/{dockername}/devices")
    public Set<DeviceDTO> listAllDevicebyVdmsidAndDockerName(@RequestParam String username, @RequestParam String vdmsid, @PathVariable String dockername) {
        log.info("listAllDevicebyVdmsidAndDockerName username={} vdmsid={} dockername={}", username, vdmsid, dockername);
        try {
            return deviceService.listAllDevicebyVdmsidAndDockerName(username, vdmsid, dockername);

        } catch (Exception e) {
            log.error("listAllDevicebyVdmsidAndDockerName failed username={} vdmsid={} dockername={}: {}", username, vdmsid, dockername, e.getMessage(), e);
            throw e;
        }
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
    @RequestMapping(method = RequestMethod.GET, value = "/docker/{dockername}/getfilterdevice")
    public Set<DeviceDTO> getfilterdevice(@RequestParam String username, @RequestParam String vdmsid, @PathVariable String dockername, @RequestParam(defaultValue = "all") String condition, @RequestParam(defaultValue = "null") String searchKey,
                                          @RequestParam(defaultValue = "1") Integer pageno, @RequestParam(defaultValue = "10") Integer pagesize) {
        log.info("getfilterdevice username={} vdmsid={} dockername={}", username, vdmsid, dockername);
        try {
            return deviceService.getfilterdevices(username, vdmsid, dockername, condition, searchKey, pageno, pagesize);
        } catch (Exception e) {
            log.error("getfilterdevice failed username={} vdmsid={} dockername={}: {}", username, vdmsid, dockername, e.getMessage(), e);
            throw e;
        }
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
    @RequestMapping(method = RequestMethod.GET, value = "/docker/{dockername}/getsubsystemparentdevicesbypagination")
    public Set<DeviceDTO> getSubsystemParentDevicesByPagination(@RequestParam String username, @RequestParam String vdmsid, @PathVariable String dockername, @RequestParam(defaultValue = "all") String condition,
                                                                @RequestParam(defaultValue = "1") Integer pageno, @RequestParam(defaultValue = "10") Integer pagesize, @RequestParam(defaultValue = "all") String assignee) {
        log.info("getSubsystemParentDevicesByPagination username={} vdmsid={} dockername={}", username, vdmsid, dockername);
        try {
            return deviceService.getSubsystemParentDevicesByPagination(username, vdmsid, dockername, condition, pageno, pagesize, assignee);
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
    @RequestMapping(method = RequestMethod.GET, value = "/docker/{dockername}/device/{device_id}/getsubsystemdevicesbypagination")
    public Set<DeviceDTO> getSubsystemDevicesByPagination(@RequestParam String username, @RequestParam String vdmsid, @PathVariable String dockername, @PathVariable String device_id, @RequestParam(defaultValue = "all") String condition,
                                                          @RequestParam(defaultValue = "1") Integer pageno, @RequestParam(defaultValue = "10") Integer pagesize, @RequestParam(defaultValue = "all") String assignee) {
        log.info("getSubsystemDevicesByPagination username={} vdmsid={} dockername={} device_id={}", username, vdmsid, dockername, device_id);
        try {
            return deviceService.getSubsystemDevicesByPagination(username, vdmsid, dockername, device_id, condition, pageno, pagesize, assignee);
        } catch (Exception e) {
            log.error("getSubsystemDevicesByPagination failed username={} vdmsid={} dockername={} device_id={}: {}", username, vdmsid, dockername, device_id, e.getMessage(), e);
            throw e;
        }
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
    @RequestMapping(method = RequestMethod.POST, value = "/docker/{dockername}/devicesupsert")
    public void upsertDeviceListByVdmsIdAndDockerName(@RequestBody List<DeviceDTO> devicesDto, @RequestParam String username, @RequestParam String vdmsid, @PathVariable String dockername, @RequestParam(defaultValue = "all") String assignee) {
        log.info("upsertDeviceListByVdmsIdAndDockerName username={} vdmsid={} dockername={}", username, vdmsid, dockername);
        try {
            deviceService.upsertDeviceListByVdmsIdAndDockerName(devicesDto, username, vdmsid, dockername, assignee);
        } catch (Exception e) {
            log.error("upsertDeviceListByVdmsIdAndDockerName failed username={} vdmsid={} dockername={}: {}", username, vdmsid, dockername, e.getMessage(), e);
            throw e;
        }
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
    @RequestMapping(method = RequestMethod.POST, value = "/docker/{dockername}/device/{device_id}/edit")
    public DeviceDTO editDeviceByDeviceId(@RequestParam String username, @RequestParam String vdmsid, @PathVariable String dockername,
                                          @PathVariable String device_id, @RequestBody DeviceDTO devicedto, HttpServletRequest httpServletRequest, @RequestParam(defaultValue = "all") String assignee) throws JSONException, IOException {
        log.info("editDeviceByDeviceId username={} vdmsid={} dockername={} device_id={}", username, vdmsid, dockername, device_id);
        try {
            return deviceService.editDeviceByDeviceID(username, vdmsid, dockername, device_id, devicedto, httpServletRequest, assignee);
        } catch (Exception e) {
            log.error("editDeviceByDeviceId failed username={} vdmsid={} dockername={} device_id={}: {}", username, vdmsid, dockername, device_id, e.getMessage(), e);
            throw e;
        }
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
    @RequestMapping(method = RequestMethod.PUT, value = "/docker/{dockername}/phoneaccount/{phoneaccount}/device/{device_id}/{vendor_type}")
    public void unlinkVendorByVendorIdAndDeviceId(@RequestParam String username, @PathVariable String dockername, @PathVariable String phoneaccount,
                                                  @PathVariable String device_id, @PathVariable String vendor_type, HttpServletRequest httpServletRequest) {
        log.info("unlinkVendorByVendorIdAndDeviceId username={} dockername={} phoneaccount={} device_id={} vendor_type={}", username, dockername, phoneaccount, device_id, vendor_type);
        try {
            deviceService.unlinkVendorByVendorIdAndDeviceId(username, dockername, phoneaccount, device_id, vendor_type, httpServletRequest);
        } catch (Exception e) {
            log.error("unlinkVendorByVendorIdAndDeviceId failed username={} dockername={} phoneaccount={} device_id={} vendor_type={}: {}", username, dockername, phoneaccount, device_id, vendor_type, e.getMessage(), e);
            throw e;
        }
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
    @RequestMapping(method = RequestMethod.POST, value = "/docker/{dockername}/phoneaccount/device/{device_id}/{vendor_type}/link")
    public String linkVendorByVendorIdAndDeviceId(@RequestParam String username, @RequestParam String vdmsid, @PathVariable String dockername,
                                                  @PathVariable String device_id, @PathVariable String vendor_type,
                                                  @RequestBody PhonebookAddressDto phonebookaddressdto, HttpServletRequest httpServletRequest) {
        log.info("linkVendorByVendorIdAndDeviceId username={} vdmsid={} dockername={} device_id={} vendor_type={}", username, vdmsid, dockername, device_id, vendor_type);
        try {
            return deviceService.linkVendorByVendorIdAndDeviceId(username, vdmsid, dockername, device_id, phonebookaddressdto, vendor_type, httpServletRequest);
        } catch (Exception e) {
            log.error("linkVendorByVendorIdAndDeviceId failed username={} vdmsid={} dockername={} device_id={} vendor_type={}: {}", username, vdmsid, dockername, device_id, vendor_type, e.getMessage(), e);
            throw e;
        }
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
    @RequestMapping(method = RequestMethod.PUT, value = "/docker/{dockername}/devices")
    public void multiDeviceUpdate(@RequestParam String username, @RequestParam String vdmsid, @PathVariable String dockername,
                                  @RequestBody Set<MultiDeviceDTO> multidevicedtos, HttpServletRequest httpServletRequest, @RequestParam(defaultValue = "all") String assignee) throws JSONException, IOException {
        log.info("multiDeviceUpdate username={} vdmsid={} dockername={}", username, vdmsid, dockername);
        try {
            System.out.println("***************************************************************");
            System.out.println(multidevicedtos);
            System.out.println("***************************************************************");

            deviceService.multiDeviceUpdate(username, vdmsid, dockername, multidevicedtos, httpServletRequest, assignee);
        } catch (Exception e) {
            log.error("multiDeviceUpdate failed username={} vdmsid={} dockername={}: {}", username, vdmsid, dockername, e.getMessage(), e);
            throw e;
        }
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
    @RequestMapping(method = RequestMethod.POST, value = "/docker/{dockername}/devices/quickupdate")
    public Set<DeviceDTO> quickUpdate(@RequestParam String username, @RequestParam String vdmsid, @PathVariable String dockername,
                                      @RequestBody TagDeviceOrLocationDTO tagDeviceOrLocationDTO, HttpServletRequest httpServletRequest, @RequestParam(defaultValue = "all") String assignee) throws IOException {
        log.info("quickUpdate username={} vdmsid={} dockername={}", username, vdmsid, dockername);
        try {
            return deviceService.quickUpdate(username, vdmsid, dockername, tagDeviceOrLocationDTO, httpServletRequest, assignee);
        } catch (Exception e) {
            log.error("quickUpdate failed username={} vdmsid={} dockername={}: {}", username, vdmsid, dockername, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Returns the device names for the given VDMS and docker.
     *
     * @param username   owning user
     * @param vdmsid     owning VDMS id
     * @param dockername docker (gateway) name to scope devices to
     * @return set of devices carrying their names
     */
    @RequestMapping(method = RequestMethod.GET, value = "/docker/{dockername}/device/names")
    public Set<DeviceDTO> getDeviceNamesByVdmsIdAndDockerName(@RequestParam String username, @RequestParam String vdmsid, @PathVariable String dockername) {
        log.info("getDeviceNamesByVdmsIdAndDockerName username={} vdmsid={} dockername={}", username, vdmsid, dockername);
        try {
            return deviceService.getDeviceNamesByVdmsIdAndDockerName(username, vdmsid, dockername);
        } catch (Exception e) {
            log.error("getDeviceNamesByVdmsIdAndDockerName failed username={} vdmsid={} dockername={}: {}", username, vdmsid, dockername, e.getMessage(), e);
            throw e;
        }
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
    @RequestMapping(method = RequestMethod.POST, value = "/docker/{dockername}/addvirtualdevice")
    public void addVirtualDeviceByVdmsIdAndDockerName(@RequestParam String username, @RequestParam String vdmsid, @PathVariable String dockername,
                                                      @RequestParam(value = "images", required = false) List<MultipartFile> asset_images,
                                                      @RequestParam(value = "virtual_devices") String virtualDevicesDTO, HttpServletRequest httpServletRequest, @RequestParam(defaultValue = "all") String assignee) {
        log.info("addVirtualDeviceByVdmsIdAndDockerName username={} vdmsid={} dockername={}", username, vdmsid, dockername);
        try {
            deviceService.addVirtualDeviceByVdmsIdAndDockerName(username, vdmsid, dockername, virtualDevicesDTO, asset_images, httpServletRequest, assignee);
        } catch (Exception e) {
            log.error("addVirtualDeviceByVdmsIdAndDockerName failed username={} vdmsid={} dockername={}: {}", username, vdmsid, dockername, e.getMessage(), e);
            throw e;
        }
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
    @RequestMapping(method = RequestMethod.POST, value = "/docker/{dockername}/updatevirtualdevice")
    public void editVirtualDeviceByVirtualDeviceId(@RequestParam String username, @RequestParam String vdmsid, @PathVariable String dockername,
                                                   @RequestBody Set<DeviceDTO> virtualDevices, HttpServletRequest httpServletRequest) throws IOException {
        log.info("editVirtualDeviceByVirtualDeviceId username={} vdmsid={} dockername={}", username, vdmsid, dockername);
        try {
            deviceService.editVirtualDeviceByVirtualDeviceId(username, vdmsid, dockername, virtualDevices, httpServletRequest);
        } catch (Exception e) {
            log.error("editVirtualDeviceByVirtualDeviceId failed username={} vdmsid={} dockername={}: {}", username, vdmsid, dockername, e.getMessage(), e);
            throw e;
        }
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
    @RequestMapping(method = RequestMethod.DELETE, value = "/docker/{dockername}/virtual-device/{virtual_device_id}")
    public void deleteVirtualDeviceByVirtualDeviceId(@RequestParam String username, @RequestParam String vdmsid, @PathVariable String dockername,
                                                     @PathVariable String virtual_device_id, @RequestParam(defaultValue = "all") String assignee) {
        log.info("deleteVirtualDeviceByVirtualDeviceId username={} vdmsid={} dockername={} virtual_device_id={}", username, vdmsid, dockername, virtual_device_id);
        try {
            deviceService.deleteVirtualDeviceByVirtualDeviceId(username, vdmsid, dockername, virtual_device_id, assignee);
        } catch (Exception e) {
            log.error("deleteVirtualDeviceByVirtualDeviceId failed username={} vdmsid={} dockername={} virtual_device_id={}: {}", username, vdmsid, dockername, virtual_device_id, e.getMessage(), e);
            throw e;
        }
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
    @RequestMapping(method = RequestMethod.DELETE, value = "/docker/{dockername}/deletedevices")
    public void deleteDevicesById(@RequestParam String username, @RequestParam String vdmsid, @PathVariable String dockername, @RequestBody Set<String> deviceIds, HttpServletRequest httpServletRequest, @RequestParam(defaultValue = "all") String assignee) {
        log.info("deleteDevicesById username={} vdmsid={} dockername={}", username, vdmsid, dockername);
        try {
//        deviceService.deleteDevicesById(username, vdmsid, dockername, deviceIds, httpServletRequest);
            deviceService.softDeleteDevicesById(username, vdmsid, dockername, deviceIds, httpServletRequest, assignee);
        } catch (Exception e) {
            log.error("deleteDevicesById failed username={} vdmsid={} dockername={}: {}", username, vdmsid, dockername, e.getMessage(), e);
            throw e;
        }
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
    @RequestMapping(method = RequestMethod.GET, value = "/docker/{dockername}/device/{device_id}/getdevice")
    public DeviceDTO getDeviceByDeviceId(@RequestParam String username, @RequestParam String vdmsid, @PathVariable String dockername, @PathVariable String device_id) {
        log.info("getDeviceByDeviceId username={} vdmsid={} dockername={} device_id={}", username, vdmsid, dockername, device_id);
        try {
            return deviceService.getDeviceByDeviceId(username, vdmsid, dockername, device_id);
        } catch (Exception e) {
            log.error("getDeviceByDeviceId failed username={} vdmsid={} dockername={} device_id={}: {}", username, vdmsid, dockername, device_id, e.getMessage(), e);
            throw e;
        }
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
    @RequestMapping(method = RequestMethod.PUT, value = "/docker/{dockername}/virtual-device/{virtual_device_id}/syncstatus")
    public DeviceDTO updateVirtualDeviceStatusByVirtualDeviceId(@RequestParam String username, @RequestParam String vdmsid, @PathVariable String dockername, @PathVariable String virtual_device_id, @RequestBody DeviceDTO virtualdevicedto) throws IOException {
        log.info("updateVirtualDeviceStatusByVirtualDeviceId username={} vdmsid={} dockername={} virtual_device_id={}", username, vdmsid, dockername, virtual_device_id);
        try {
            return deviceService.updateVirtualDeviceStatusByVirtualDeviceId(username, vdmsid, dockername, virtual_device_id, virtualdevicedto);
        } catch (Exception e) {
            log.error("updateVirtualDeviceStatusByVirtualDeviceId failed username={} vdmsid={} dockername={} virtual_device_id={}: {}", username, vdmsid, dockername, virtual_device_id, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Returns product details for a hard-coded sample product id (test endpoint).
     *
     * @return the product details for the sample product
     */
    @GetMapping(value = "/test/product")
    public ProductDTO test() {
        log.info("test called");
        try {
            String product_id = "6aeeae74-2855-4b67-943e-49d979a45abf";
            return apicallService.getProductDetailsByProductId(product_id);
        } catch (Exception e) {
            log.error("test failed: {}", e.getMessage(), e);
            throw e;
        }
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
    @RequestMapping(method = RequestMethod.GET, value = "/docker/{dockername}/getdevicecount")
    public Map<String, Integer> getDeviceCount(@RequestParam String username, @RequestParam String vdmsid, @PathVariable String dockername, @RequestParam(defaultValue = "all") String assignee) {
        log.info("getDeviceCount username={} vdmsid={} dockername={}", username, vdmsid, dockername);
        try {


            return deviceService.getDeviceCount(username, vdmsid, dockername, assignee);

        } catch (Exception e) {
            log.error("getDeviceCount failed username={} vdmsid={} dockername={}: {}", username, vdmsid, dockername, e.getMessage(), e);
            throw e;
        }
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
    @RequestMapping(method = RequestMethod.GET, value = "/docker/{dockername}/devicetopology")
    public List<DeviceTopologyDTO> listTopologyDevicesByDockerName(@RequestParam String username, @RequestParam String vdmsid, @PathVariable String dockername) {
        log.info("listTopologyDevicesByDockerName username={} vdmsid={} dockername={}", username, vdmsid, dockername);
        try {
            return deviceService.listTopologyDevicesByDockerName(username, vdmsid, dockername);
        } catch (Exception e) {
            log.error("listTopologyDevicesByDockerName failed username={} vdmsid={} dockername={}: {}", username, vdmsid, dockername, e.getMessage(), e);
            throw e;
        }
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
    @RequestMapping(method = RequestMethod.POST, value = "/docker/{dockername}/updatedeviceposition")
    public void updateDevicePosition(@RequestParam String username, @RequestParam String vdmsid, @PathVariable String dockername,
                                     @RequestBody List<DeviceDTO> devicePositions, HttpServletRequest httpServletRequest) {
        log.info("updateDevicePosition username={} vdmsid={} dockername={}", username, vdmsid, dockername);
        try {

            deviceService.updateDevicePosition(devicePositions, vdmsid, username, httpServletRequest);
        } catch (Exception e) {
            log.error("updateDevicePosition failed username={} vdmsid={} dockername={}: {}", username, vdmsid, dockername, e.getMessage(), e);
            throw e;
        }
    }

    // Device list by docker name for integration
    /**
     * Returns the device list for the given docker, intended for integration consumers.
     *
     * @param dockername docker (gateway) name whose devices are listed
     * @return list of devices for the docker
     */
    @RequestMapping(method = RequestMethod.GET, value = "/docker/{dockername}/devicelistintegration")
    public List<DeviceDTO> listDevicebyDockerIntegration(@PathVariable String dockername) {
        log.info("listDevicebyDockerIntegration dockername={}", dockername);
        try {
            return deviceService.listDevicebyDockerIntegration(dockername);
        } catch (Exception e) {
            log.error("listDevicebyDockerIntegration failed dockername={}: {}", dockername, e.getMessage(), e);
            throw e;
        }
    }

    //Get Gateway ID
    /**
     * Returns the gateway id for the given docker.
     *
     * @param dockername docker (gateway) name to resolve
     * @return the gateway id
     */
    @RequestMapping(method = RequestMethod.GET, value = "/docker/{dockername}/getgatewayid")
    public String getGatewayId(@PathVariable String dockername) {
        log.info("getGatewayId dockername={}", dockername);
        try {
            return deviceService.getGatewayId(dockername);
        } catch (Exception e) {
            log.error("getGatewayId failed dockername={}: {}", dockername, e.getMessage(), e);
            throw e;
        }
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
    @RequestMapping(method = RequestMethod.POST, value = "/docker/{dockername}/updatetopology")
    public void updateTopology(@RequestParam String username, @RequestParam String vdmsid, @PathVariable String dockername,
                               @RequestBody List<DeviceTopologyDTO> devices, HttpServletRequest httpServletRequest) {
        log.info("updateTopology username={} vdmsid={} dockername={}", username, vdmsid, dockername);
        try {
            deviceService.updateTopology(username, vdmsid, dockername, devices, httpServletRequest);
        } catch (Exception e) {
            log.error("updateTopology failed username={} vdmsid={} dockername={}: {}", username, vdmsid, dockername, e.getMessage(), e);
            throw e;
        }
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
    @RequestMapping(method = RequestMethod.POST, value = "/docker/{dockername}/resettopology")
    public void resetTopology(@RequestParam String username, @RequestParam String vdmsid, @PathVariable String dockername, HttpServletRequest httpServletRequest) {
        log.info("resetTopology username={} vdmsid={} dockername={}", username, vdmsid, dockername);
        try {
            deviceService.resetTopologyByDockername(username, vdmsid, dockername, httpServletRequest);
        } catch (Exception e) {
            log.error("resetTopology failed username={} vdmsid={} dockername={}: {}", username, vdmsid, dockername, e.getMessage(), e);
            throw e;
        }
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
    @RequestMapping(method = RequestMethod.GET, value = "/docker/{dockername}/device/{device_id}/getalldevicesensors")
    public AllSensorsDTO getDeviceSensors(@RequestParam String username, @RequestParam String vdmsid, @PathVariable String dockername,
                                          @PathVariable String device_id) {
        log.info("getDeviceSensors username={} vdmsid={} dockername={} device_id={}", username, vdmsid, dockername, device_id);
        try {
            return deviceService.getDeviceSensors(username, vdmsid, dockername, device_id);
        } catch (Exception e) {
            log.error("getDeviceSensors failed username={} vdmsid={} dockername={} device_id={}: {}", username, vdmsid, dockername, device_id, e.getMessage(), e);
            throw e;
        }
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
    @RequestMapping(method = RequestMethod.POST, value = "/getparentdevicebypagination")
    public Set<DeviceDTO> getParentDeviceByPagination(@RequestParam String username, @RequestParam String vdmsid,
                                                      @RequestParam(defaultValue = "null") String searchKey, @RequestParam(defaultValue = "1") Integer pageno,
                                                      @RequestParam(defaultValue = "10") Integer pagesize, @RequestParam(defaultValue = "all") Set<String> dockernames, @RequestParam(defaultValue = "all") Set<String> types, @RequestParam(defaultValue = "all") Set<String> virtual_device_types) {
        log.info("getParentDeviceByPagination username={} vdmsid={}", username, vdmsid);
        try {
            return deviceService.getParentDeviceByPagination(username, vdmsid, searchKey, pageno, pagesize, dockernames, types, virtual_device_types);
        } catch (Exception e) {
            log.error("getParentDeviceByPagination failed username={} vdmsid={}: {}", username, vdmsid, e.getMessage(), e);
            throw e;
        }
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
    @RequestMapping(method = RequestMethod.POST, value = "/docker/{dockername}/getparentdevice")
    public Set<DeviceDTO> getParentDeviceById(@RequestParam String username, @RequestParam String vdmsid,
                                              @PathVariable String dockername, @RequestBody Set<DeviceDTO> parent_devices, HttpServletRequest httpServletRequest) {
        log.info("getParentDeviceById username={} vdmsid={} dockername={}", username, vdmsid, dockername);
        try {
            return deviceService.getParentDeviceById(username, vdmsid, dockername, parent_devices, httpServletRequest);
        } catch (Exception e) {
            log.error("getParentDeviceById failed username={} vdmsid={} dockername={}: {}", username, vdmsid, dockername, e.getMessage(), e);
            throw e;
        }
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
    @RequestMapping(method = RequestMethod.GET, value = "/docker/{dockername}/device/{device_id}/parent/{parent_id}/getsubsystemparentdeviceinfo")
    public DeviceDTO getSubsystemParentDeviceInfo(@RequestParam String username, @RequestParam String vdmsid,
                                                  @PathVariable String dockername, @PathVariable String device_id, @PathVariable String parent_id) {
        log.info("getSubsystemParentDeviceInfo username={} vdmsid={} dockername={} device_id={} parent_id={}", username, vdmsid, dockername, device_id, parent_id);
        try {
            return deviceService.getSubsystemParentDeviceInfo(username, vdmsid, dockername, device_id, parent_id);
        } catch (Exception e) {
            log.error("getSubsystemParentDeviceInfo failed username={} vdmsid={} dockername={} device_id={} parent_id={}: {}", username, vdmsid, dockername, device_id, parent_id, e.getMessage(), e);
            throw e;
        }
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
    @RequestMapping(method = RequestMethod.POST, value = "/docker/{dockername}/updatematcheddeviceproduct")
    public void updateMatchedDeviceProduct(@RequestParam String username, @RequestParam String vdmsid,
                                           @PathVariable String dockername, @RequestBody DeviceDTO device, HttpServletRequest httpServletRequest) {
        log.info("updateMatchedDeviceProduct username={} vdmsid={} dockername={}", username, vdmsid, dockername);
        try {
            deviceService.updateMatchedDeviceProduct(username, vdmsid, dockername, device, httpServletRequest);
        } catch (Exception e) {
            log.error("updateMatchedDeviceProduct failed username={} vdmsid={} dockername={}: {}", username, vdmsid, dockername, e.getMessage(), e);
            throw e;
        }
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
    @RequestMapping(method = RequestMethod.POST, value = "/docker/{dockername}/searchdevices")
    public Set<DeviceDTO> searchDevices(@RequestParam String username, @RequestParam String vdmsid,
                                        @PathVariable String dockername, @RequestParam(defaultValue = "all") String condition,
                                        @RequestParam(defaultValue = "1") Integer pageno, @RequestParam(defaultValue = "10") Integer pagesize,
                                        @RequestBody Map<String, Object> search_details) {
        log.info("searchDevices username={} vdmsid={} dockername={}", username, vdmsid, dockername);
        try {
            return deviceSearchService.searchDevices(username, vdmsid, dockername, condition, pageno, pagesize, search_details);
        } catch (Exception e) {
            log.error("searchDevices failed username={} vdmsid={} dockername={}: {}", username, vdmsid, dockername, e.getMessage(), e);
            throw e;
        }
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
    @RequestMapping(method = RequestMethod.POST, value = "/docker/{dockername}/sortdevices")
    public Set<DeviceDTO> sortDevices(@RequestParam String username, @RequestParam String vdmsid,
                                      @PathVariable String dockername, @RequestParam(defaultValue = "all") String condition,
                                      @RequestParam(defaultValue = "1") Integer pageno, @RequestParam(defaultValue = "10") Integer pagesize,
                                      @RequestBody Map<String, Object> sort_details) {
        log.info("sortDevices username={} vdmsid={} dockername={}", username, vdmsid, dockername);
        try {
            return deviceSearchService.sortDevices(username, vdmsid, dockername, condition, pageno, pagesize, sort_details);
        } catch (Exception e) {
            log.error("sortDevices failed username={} vdmsid={} dockername={}: {}", username, vdmsid, dockername, e.getMessage(), e);
            throw e;
        }
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
    @RequestMapping(method = RequestMethod.POST, value = "/docker/{dockername}/filterdevices")
    public Set<DeviceDTO> filterDevices(@RequestParam String username, @RequestParam String vdmsid,
                                        @PathVariable String dockername, @RequestParam(defaultValue = "all") String condition,
                                        @RequestParam(defaultValue = "1") Integer pageno,
                                        @RequestParam(defaultValue = "10") Integer pagesize, @RequestBody List<Map<String, Object>> filter_details) {
        log.info("filterDevices username={} vdmsid={} dockername={}", username, vdmsid, dockername);
        try {
            return deviceSearchService.filterDevices(username, vdmsid, dockername, condition, pageno, pagesize, filter_details);
        } catch (Exception e) {
            log.error("filterDevices failed username={} vdmsid={} dockername={}: {}", username, vdmsid, dockername, e.getMessage(), e);
            throw e;
        }
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
    @RequestMapping(method = RequestMethod.POST, value = "/docker/{dockername}/archivedevices")
    public void archiveDevices(@RequestParam String username, @RequestParam String vdmsid, @PathVariable String dockername,
                               @RequestParam(defaultValue = "1") Integer archive, @RequestBody Set<String> deviceIds, HttpServletRequest httpServletRequest, @RequestParam(defaultValue = "all") String assignee) {
        log.info("archiveDevices username={} vdmsid={} dockername={}", username, vdmsid, dockername);
        try {
            deviceService.archiveDevices(username, vdmsid, dockername, archive, deviceIds, httpServletRequest, assignee);
        } catch (Exception e) {
            log.error("archiveDevices failed username={} vdmsid={} dockername={}: {}", username, vdmsid, dockername, e.getMessage(), e);
            throw e;
        }
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
    @RequestMapping(method = RequestMethod.POST, value = "/docker/{dockername}/getdeviceinfobycustomfields")
    public List<DeviceDTO> getDeviceInfoByCustomFields(@RequestParam String username, @RequestParam String vdmsid, @PathVariable String dockername,
                                                       @RequestBody com.alibaba.fastjson.JSONObject custom_fields) {
        log.info("getDeviceInfoByCustomFields username={} vdmsid={} dockername={}", username, vdmsid, dockername);
        try {
            return deviceSearchService.getDeviceInfoByCustomFields(username, vdmsid, dockername, custom_fields);
        } catch (Exception e) {
            log.error("getDeviceInfoByCustomFields failed username={} vdmsid={} dockername={}: {}", username, vdmsid, dockername, e.getMessage(), e);
            throw e;
        }
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
    @RequestMapping(method = RequestMethod.POST, value = "/docker/{dockername}/searchsortfilterdevices")
    public Set<DeviceDTO> multipleKeywordSearchSortFilterDevices(@RequestParam String username, @RequestParam String vdmsid,
                                                                 @PathVariable String dockername, @RequestParam(defaultValue = "all") String condition,
                                                                 @RequestParam(defaultValue = "1") Integer pageno,
                                                                 @RequestParam(defaultValue = "10") Integer pagesize,
                                                                 @RequestParam(defaultValue = "123") Integer onboard_status,
                                                                 @RequestBody com.alibaba.fastjson.JSONObject search_sort_filter_details) {
        log.info("multipleKeywordSearchSortFilterDevices username={} vdmsid={} dockername={}", username, vdmsid, dockername);
        try {
            return deviceSearchService.multipleKeywordSearchSortFilterDevices(username, vdmsid, dockername, condition, pageno, pagesize, search_sort_filter_details, onboard_status);
        } catch (Exception e) {
            log.error("multipleKeywordSearchSortFilterDevices failed username={} vdmsid={} dockername={}: {}", username, vdmsid, dockername, e.getMessage(), e);
            throw e;
        }
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
    @RequestMapping(method = RequestMethod.POST, value = "/docker/{dockername}/searchsortfilterdevicescount")
    public String multipleKeywordSearchSortFilterDevicesCount(@RequestParam String username, @RequestParam String vdmsid,
                                                              @PathVariable String dockername, @RequestParam(defaultValue = "all") String condition,
                                                              @RequestParam(defaultValue = "123") Integer onboard_status,
                                                              @RequestBody com.alibaba.fastjson.JSONObject search_sort_filter_details) {
        log.info("multipleKeywordSearchSortFilterDevicesCount username={} vdmsid={} dockername={}", username, vdmsid, dockername);
        try {
            return deviceSearchService.multipleKeywordSearchSortFilterDevicesCount(username, vdmsid, dockername, condition,
                    search_sort_filter_details, onboard_status);
        } catch (Exception e) {
            log.error("multipleKeywordSearchSortFilterDevicesCount failed username={} vdmsid={} dockername={}: {}", username, vdmsid, dockername, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Returns the distinct assigned-user emails for devices on the given network.
     *
     * @param vdms_id      owning VDMS id
     * @param network_name network whose assigned users are requested
     * @return list of unique assigned-user email addresses
     */
    @RequestMapping(method = RequestMethod.GET, value = "/device/network/{network_name}/getassignedemail")
    public List<String> getUniqueAssignedUser(@RequestParam String vdms_id, @PathVariable String network_name) {
        log.info("getUniqueAssignedUser vdms_id={} network_name={}", vdms_id, network_name);
        try {
            return deviceMonitorService.getUniqueAssignedUserEmail(vdms_id, network_name);
        } catch (Exception e) {
            log.error("getUniqueAssignedUser failed vdms_id={} network_name={}: {}", vdms_id, network_name, e.getMessage(), e);
            throw e;
        }
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
    @RequestMapping(method = RequestMethod.GET, value = "/docker/{dockername}/getalertmessages")
    public List<ConditionsDTO> getDeviceAlertMessages(@RequestParam String username, @RequestParam String vdmsid, @PathVariable String dockername) {
        log.info("getDeviceAlertMessages username={} vdmsid={} dockername={}", username, vdmsid, dockername);
        try {
            return deviceService.getDeviceAlertMessages(username, vdmsid, dockername);
        } catch (Exception e) {
            log.error("getDeviceAlertMessages failed username={} vdmsid={} dockername={}: {}", username, vdmsid, dockername, e.getMessage(), e);
            throw e;
        }
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
    @RequestMapping(method = RequestMethod.POST, value = "/upsertassetimages")
    public void upsertAssetImages(@RequestParam String username, @RequestParam String vdms_id,
                                  @RequestParam List<String> device_ids, @RequestParam(value = "images", required = false) List<MultipartFile> asset_images, HttpServletRequest httpServletRequest) {
        log.info("upsertAssetImages username={} vdms_id={}", username, vdms_id);
        try {
            deviceService.upsertAssetImages(username, vdms_id, device_ids, asset_images, httpServletRequest);
        } catch (Exception e) {
            log.error("upsertAssetImages failed username={} vdms_id={}: {}", username, vdms_id, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Deletes asset images for the given devices.
     *
     * @param username           owning user
     * @param vdms_id            owning VDMS id
     * @param deviceDTOS         devices whose asset images should be deleted
     * @param httpServletRequest current request, used to resolve tenant/VDMS context
     */
    @RequestMapping(method = RequestMethod.DELETE, value = "/deleteassetimages")
    public void deleteAssetImages(@RequestParam String username, @RequestParam String vdms_id, @RequestBody List<DeviceDTO> deviceDTOS, HttpServletRequest httpServletRequest) {
        log.info("deleteAssetImages username={} vdms_id={}", username, vdms_id);
        try {
            System.out.println("heere");
            deviceService.deleteAssetImages(username, vdms_id, deviceDTOS, httpServletRequest);
        } catch (Exception e) {
            log.error("deleteAssetImages failed username={} vdms_id={}: {}", username, vdms_id, e.getMessage(), e);
            throw e;
        }
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
    @RequestMapping(method = RequestMethod.DELETE, value = "/deletedeviceimages")
    public void deleteDeviceImages(@RequestParam String username, @RequestParam String vdms_id, @RequestBody List<DeviceDTO> deviceDTOS, @RequestParam String category, HttpServletRequest httpServletRequest) {
        log.info("deleteDeviceImages username={} vdms_id={} category={}", username, vdms_id, category);
        try {
            deviceService.deleteDeviceImages(username, vdms_id, deviceDTOS, category,httpServletRequest);
        } catch (Exception e) {
            log.error("deleteDeviceImages failed username={} vdms_id={} category={}: {}", username, vdms_id, category, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Returns the asset image URLs for the given device.
     *
     * @param username  owning user
     * @param vdms_id   owning VDMS id
     * @param device_id device whose asset image URLs are requested
     * @return serialized asset image URLs
     */
    @RequestMapping(method = RequestMethod.GET, value = "/device/{device_id}/getassetimages")
    public String getAssetImageUrls(@RequestParam String username, @RequestParam String vdms_id, @PathVariable String device_id) {
        log.info("getAssetImageUrls username={} vdms_id={} device_id={}", username, vdms_id, device_id);
        try {
            return deviceService.getAssetImageUrls(username, vdms_id, device_id);
        } catch (Exception e) {
            log.error("getAssetImageUrls failed username={} vdms_id={} device_id={}: {}", username, vdms_id, device_id, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Returns the asset image URLs for the given device grouped by category.
     *
     * @param username  owning user
     * @param vdms_id   owning VDMS id
     * @param device_id device whose categorized asset image URLs are requested
     * @return serialized asset image URLs by category
     */
    @RequestMapping(method = RequestMethod.GET, value = "/device/{device_id}/getallassetimages")
    public String getAssetImageUrlsByCategory(@RequestParam String username, @RequestParam String vdms_id, @PathVariable String device_id) {
        log.info("getAssetImageUrlsByCategory username={} vdms_id={} device_id={}", username, vdms_id, device_id);
        try {
            return deviceService.getAssetImageUrlsCategory(username, vdms_id, device_id);
        } catch (Exception e) {
            log.error("getAssetImageUrlsByCategory failed username={} vdms_id={} device_id={}: {}", username, vdms_id, device_id, e.getMessage(), e);
            throw e;
        }
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
    @RequestMapping(method = RequestMethod.POST, value = "/group/{group}/getalldevicespagination")
    public Set<DeviceDTO> getAllDevicesPagination(@RequestParam String username, @RequestParam String vdmsid,
                                                  @PathVariable String group, @RequestParam(defaultValue = "null") String searchkey,
                                                  @RequestParam(defaultValue = "1") Integer pageno, @RequestParam(defaultValue = "10") Integer pagesize,
                                                  @RequestBody JSONObject filterObject) {
        log.info("getAllDevicesPagination username={} vdmsid={} group={}", username, vdmsid, group);
        try {
            return deviceService.getAllDevicesPagination(username, vdmsid, group, searchkey, pageno, pagesize, filterObject);
        } catch (Exception e) {
            log.error("getAllDevicesPagination failed username={} vdmsid={} group={}: {}", username, vdmsid, group, e.getMessage(), e);
            throw e;
        }
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
    @RequestMapping(method = RequestMethod.GET, value = "/getfiltervirtualdevicesbypagination")
    public Set<DeviceDTO> getFilterVirtualDevicesByPagination(@RequestParam String username, @RequestParam String vdmsid, @RequestParam(defaultValue = "null") String searchKey,
                                                              @RequestParam(defaultValue = "1") Integer pageno, @RequestParam(defaultValue = "10") Integer pagesize, @RequestParam(defaultValue = "all") Set<String> dockernames, @RequestParam(defaultValue = "all") Set<String> types, @RequestParam(defaultValue = "all") Set<String> virtual_device_types) {
        log.info("getFilterVirtualDevicesByPagination username={} vdmsid={}", username, vdmsid);
        try {
            return deviceService.getFilterVirtualDevicesByPagination(username, vdmsid, searchKey, pageno, pagesize, dockernames, types, virtual_device_types);
        } catch (Exception e) {
            log.error("getFilterVirtualDevicesByPagination failed username={} vdmsid={}: {}", username, vdmsid, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Returns the number of power-source topology connections for the given VDMS.
     *
     * @param username owning user
     * @param vdmsid   owning VDMS id
     * @return count of power-source topology connections
     */
    @RequestMapping(method = RequestMethod.GET, value = "/getpowersourcetopologyconnectionscount")
    public Integer getPowerSourceTopologyConnectionsCount(@RequestParam String username, @RequestParam String vdmsid) {
        log.info("getPowerSourceTopologyConnectionsCount username={} vdmsid={}", username, vdmsid);
        try {
            return deviceService.getPowerSourceTopologyConnectionsCount(username, vdmsid);
        } catch (Exception e) {
            log.error("getPowerSourceTopologyConnectionsCount failed username={} vdmsid={}: {}", username, vdmsid, e.getMessage(), e);
            throw e;
        }
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
    @RequestMapping(method = RequestMethod.GET, value = "/getpowersourcetopologybypagination")
    public PowerSourceTopologyDTO getPowerSourceTopologyByPagination(@RequestParam String username, @RequestParam String vdmsid,
                                                                     @RequestParam(defaultValue = "1") Integer pageno, @RequestParam(defaultValue = "10") Integer pagesize) {
        log.info("getPowerSourceTopologyByPagination username={} vdmsid={}", username, vdmsid);
        try {
            return deviceService.getPowerSourceTopologyByPagination(username, vdmsid, pageno, pagesize);
        } catch (Exception e) {
            log.error("getPowerSourceTopologyByPagination failed username={} vdmsid={}: {}", username, vdmsid, e.getMessage(), e);
            throw e;
        }
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
    @RequestMapping(method = RequestMethod.GET, value = "/location/{location_id}/getdevicesbylocationid")
    public Set<DeviceDTO> getAssetsByLocationId(@RequestParam String username, @RequestParam String vdmsid, @PathVariable String location_id,
                                                @RequestParam(defaultValue = "1") Integer pageno, @RequestParam(defaultValue = "10") Integer pagesize) {
        log.info("getAssetsByLocationId username={} vdmsid={} location_id={}", username, vdmsid, location_id);
        try {
            return deviceService.getAssetsByLocationId(username, vdmsid, location_id, pageno, pagesize);
        } catch (Exception e) {
            log.error("getAssetsByLocationId failed username={} vdmsid={} location_id={}: {}", username, vdmsid, location_id, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Returns the reboot status of the given device.
     *
     * @param username owning user
     * @param vdmsid   owning VDMS id
     * @param deviceid device whose reboot status is requested
     * @return the device reboot status
     */
    @RequestMapping(method = RequestMethod.GET, value = "/device/{deviceid}/getdevicerebootstatus")
    public String getDeviceRebootStatus(@RequestParam String username, @RequestParam String vdmsid, @PathVariable String deviceid) {
        log.info("getDeviceRebootStatus username={} vdmsid={} deviceid={}", username, vdmsid, deviceid);
        try {

            return deviceService.getDeviceRebootStatus(username, vdmsid, deviceid);

        } catch (Exception e) {
            log.error("getDeviceRebootStatus failed username={} vdmsid={} deviceid={}: {}", username, vdmsid, deviceid, e.getMessage(), e);
            throw e;
        }
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
    @RequestMapping(method = RequestMethod.POST, value = "/upsertassetocrimages")
    public void upsertAssetOcrImages(@RequestParam String username, @RequestParam String vdms_id,
                                     @RequestParam List<String> device_ids, @RequestParam(value = "images", required = false) List<MultipartFile> asset_ocr_images, HttpServletRequest httpServletRequest) {
        log.info("upsertAssetOcrImages username={} vdms_id={}", username, vdms_id);
        try {
            deviceService.upsertAssetOcrImages(username, vdms_id, device_ids, asset_ocr_images, httpServletRequest);
        } catch (Exception e) {
            log.error("upsertAssetOcrImages failed username={} vdms_id={}: {}", username, vdms_id, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Deletes asset OCR images for the given devices.
     *
     * @param username           owning user
     * @param vdms_id            owning VDMS id
     * @param deviceDTOS         devices whose asset OCR images should be deleted
     * @param httpServletRequest current request, used to resolve tenant/VDMS context
     */
    @RequestMapping(method = RequestMethod.DELETE, value = "/deleteassetocrimages")
    public void deleteAssetOcrImages(@RequestParam String username, @RequestParam String vdms_id, @RequestBody List<DeviceDTO> deviceDTOS, HttpServletRequest httpServletRequest) {
        log.info("deleteAssetOcrImages username={} vdms_id={}", username, vdms_id);
        try {
            System.out.println("heere");
            deviceService.deleteAssetOcrImages(username, vdms_id, deviceDTOS, httpServletRequest);
        } catch (Exception e) {
            log.error("deleteAssetOcrImages failed username={} vdms_id={}: {}", username, vdms_id, e.getMessage(), e);
            throw e;
        }
    }


    /**
     * Returns the asset OCR image URLs for the given device.
     *
     * @param username  owning user
     * @param vdms_id   owning VDMS id
     * @param device_id device whose asset OCR image URLs are requested
     * @return serialized asset OCR image URLs
     */
    @RequestMapping(method = RequestMethod.GET, value = "/device/{device_id}/getassetocrimages")
    public String getAssetOcrImageUrls(@RequestParam String username, @RequestParam String vdms_id, @PathVariable String device_id) {
        log.info("getAssetOcrImageUrls username={} vdms_id={} device_id={}", username, vdms_id, device_id);
        try {
            return deviceService.getAssetOcrImageUrls(username, vdms_id, device_id);
        } catch (Exception e) {
            log.error("getAssetOcrImageUrls failed username={} vdms_id={} device_id={}: {}", username, vdms_id, device_id, e.getMessage(), e);
            throw e;
        }
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
    @RequestMapping(method = RequestMethod.POST, value = "/docker/{dockername}/adddevice")
    public DeviceDTO addDevice(@RequestParam String username, @RequestParam String vdmsid, @PathVariable String dockername,
                               @RequestBody DeviceDTO deviceDto, HttpServletRequest httpServletRequest) {
        log.info("addDevice username={} vdmsid={} dockername={}", username, vdmsid, dockername);
        try {
            return deviceService.addDevice(username, vdmsid, dockername, deviceDto, httpServletRequest);
        } catch (Exception e) {
            log.error("addDevice failed username={} vdmsid={} dockername={}: {}", username, vdmsid, dockername, e.getMessage(), e);
            throw e;
        }
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
    @RequestMapping(method = RequestMethod.POST, value = "/docker/{dockername}/exportfiltereddevices")
    public void exportFilteredDevices(HttpServletResponse response, @RequestParam String username, @RequestParam String vdmsid, @PathVariable String dockername,
                                      @RequestParam(defaultValue = "all") String condition, @RequestParam(defaultValue = "123") Integer onboard_status,
                                      @RequestParam(defaultValue = "simple_report") String template_name, @RequestParam(defaultValue = "excel") String file_type,
                                      @RequestBody com.alibaba.fastjson.JSONObject search_sort_filter_details, @RequestParam(defaultValue = "") String email, HttpServletRequest httpServletRequest) throws IOException {
        log.info("exportFilteredDevices username={} vdmsid={} dockername={}", username, vdmsid, dockername);
        try {
            deviceService.exportFilteredDevices(response, username, vdmsid, dockername, condition, search_sort_filter_details, onboard_status,
                    template_name, email, httpServletRequest, file_type);

        } catch (Exception e) {
            log.error("exportFilteredDevices failed username={} vdmsid={} dockername={}: {}", username, vdmsid, dockername, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Syncs the onboard status of all devices in the given VDMS.
     *
     * @param vdmsid owning VDMS id
     */
    @RequestMapping(method = RequestMethod.GET, value = "/syncdeviceonboardstatus")
    public void syncDeviceOnboardStatus(@RequestParam String vdmsid) {
        log.info("syncDeviceOnboardStatus vdmsid={}", vdmsid);
        try {
            deviceService.syncDeviceOnboardStatus(vdmsid);
        } catch (Exception e) {
            log.error("syncDeviceOnboardStatus failed vdmsid={}: {}", vdmsid, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Syncs the onboard status of a single device in the given VDMS.
     *
     * @param vdmsid    owning VDMS id
     * @param device_id device whose onboard status is synced
     */
    @RequestMapping(method = RequestMethod.GET, value = "/device/{device_id}/syncsingledeviceonboardstatus")
    public void syncSingleDeviceOnboardStatus(@RequestParam String vdmsid, @PathVariable String device_id) {
        log.info("syncSingleDeviceOnboardStatus vdmsid={} device_id={}", vdmsid, device_id);
        try {
            deviceService.syncSingleDeviceOnboardStatus(vdmsid, device_id);
        } catch (Exception e) {
            log.error("syncSingleDeviceOnboardStatus failed vdmsid={} device_id={}: {}", vdmsid, device_id, e.getMessage(), e);
            throw e;
        }
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
    @RequestMapping(method = RequestMethod.POST, value = "/docker/{dockername}/updateassetmatchdetails")
    public DeviceDTO updateAssetMatchDetails(@RequestParam String username, @RequestParam String vdmsid, @PathVariable String dockername,
                                             @RequestBody JSONObject deviceObject, HttpServletRequest httpServletRequest, @RequestParam(defaultValue = "all") String assignee) {
        log.info("updateAssetMatchDetails username={} vdmsid={} dockername={}", username, vdmsid, dockername);
        try {
            return deviceService.updateAssetMatchDetails(username, vdmsid, dockername, deviceObject, httpServletRequest, assignee);
        } catch (Exception e) {
            log.error("updateAssetMatchDetails failed username={} vdmsid={} dockername={}: {}", username, vdmsid, dockername, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Upserts digital twin instruments for the given VDMS.
     *
     * @param username           owning user
     * @param vdmsid             owning VDMS id
     * @param filterObject       payload describing the digital twin instruments to upsert
     * @param httpServletRequest current request, used to resolve tenant/VDMS context
     */
    @RequestMapping(method = RequestMethod.POST, value = "/upsertdigitaltwininstruments")
    public void upsertDigitalTwin(@RequestParam String username, @RequestParam String vdmsid, @RequestBody TagDeviceOrLocationDTO filterObject, HttpServletRequest httpServletRequest) {
        log.info("upsertDigitalTwin username={} vdmsid={}", username, vdmsid);
        try {
            deviceService.upsertDigitalTwin(username, vdmsid, filterObject, httpServletRequest);
        } catch (Exception e) {
            log.error("upsertDigitalTwin failed username={} vdmsid={}: {}", username, vdmsid, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Deletes the digital twin associated with the given device.
     *
     * @param username           owning user
     * @param vdmsid             owning VDMS id
     * @param device_id          device whose digital twin should be deleted
     * @param httpServletRequest current request, used to resolve tenant/VDMS context
     */
    @RequestMapping(method = RequestMethod.DELETE, value = "/device_id/{device_id}/deletedigitaltwin")
    public void deleteDigitalTwin(@RequestParam String username, @RequestParam String vdmsid, @PathVariable String device_id, HttpServletRequest httpServletRequest) {
        log.info("deleteDigitalTwin username={} vdmsid={} device_id={}", username, vdmsid, device_id);
        try {
            deviceService.deleteDigitalTwin(username, vdmsid, device_id, httpServletRequest);
        } catch (Exception e) {
            log.error("deleteDigitalTwin failed username={} vdmsid={} device_id={}: {}", username, vdmsid, device_id, e.getMessage(), e);
            throw e;
        }
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
    @RequestMapping(method = RequestMethod.POST, value = "/multieditdigitaltwininstruments")
    public void multiEditDigitalTwin(@RequestParam String username, @RequestParam String vdmsid, @RequestParam(required = true) String data,
                                     @RequestParam(required = false) String image_url,
                                     @RequestParam(required = false) MultipartFile image, HttpServletRequest httpServletRequest) {
        log.info("multiEditDigitalTwin username={} vdmsid={}", username, vdmsid);
        try {
            deviceService.multiEditDigitalTwin(username, vdmsid, data, image_url, image, httpServletRequest);
        } catch (Exception e) {
            log.error("multiEditDigitalTwin failed username={} vdmsid={}: {}", username, vdmsid, e.getMessage(), e);
            throw e;
        }
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
    @RequestMapping(method = RequestMethod.POST, value = "/docker/{dockername}/exportfilteredmeasuringinstrument")
    public void exportFilteredMeasuringInstrument(HttpServletResponse response, @RequestParam String username, @RequestParam String vdmsid,
                                                  @PathVariable String dockername, @RequestParam(defaultValue = "all") String condition,
                                                  @RequestParam(defaultValue = "1") Integer pageno,
                                                  @RequestParam(defaultValue = "10") Integer pagesize,
                                                  @RequestParam(defaultValue = "123") Integer onboard_status,
                                                  @RequestBody com.alibaba.fastjson.JSONObject search_sort_filter_details) throws IOException {
        log.info("exportFilteredMeasuringInstrument username={} vdmsid={} dockername={}", username, vdmsid, dockername);
        try {
            deviceService.exportFilteredMeasuringInstrument(response, username, vdmsid, dockername, condition, pageno, pagesize, search_sort_filter_details, onboard_status);
        } catch (Exception e) {
            log.error("exportFilteredMeasuringInstrument failed username={} vdmsid={} dockername={}: {}", username, vdmsid, dockername, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Recomputes and updates device types, optionally scoped to a single VDMS.
     *
     * @param vdmsId optional VDMS id to scope the update to; when absent applies to all
     * @return response describing the outcome of the update
     */
    @RequestMapping(method = RequestMethod.PUT, value = "/vdms/updatedevicetype")
    public ResponseDTO updateDeviceTypes(@RequestParam(required = false) String vdmsId) {
        log.info("updateDeviceTypes vdmsId={}", vdmsId);
        try {
            return deviceService.updateDeviceTypeForAll(vdmsId);
        } catch (Exception e) {
            log.error("updateDeviceTypes failed vdmsId={}: {}", vdmsId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Toggles the do-not-disturb (DND) status of a device.
     *
     * @param device_id          device whose DND status is toggled (required)
     * @param is_dnd_enabled     whether DND should be enabled (required)
     * @param httpServletRequest current request, used to resolve tenant/VDMS context
     * @throws IOException if downstream I/O fails
     */
    @RequestMapping(method = RequestMethod.POST, value = "/togglednd")
    public void toggleDndStatus(@RequestParam(value = "device_id", required = true) String device_id,
                                @RequestParam(value = "is_dnd_enabled", required = true) Boolean is_dnd_enabled, HttpServletRequest httpServletRequest) throws IOException {
        log.info("toggleDndStatus device_id={} is_dnd_enabled={}", device_id, is_dnd_enabled);
        try {
            deviceService.toggleDndStatus(device_id, is_dnd_enabled, httpServletRequest);
        } catch (Exception e) {
            log.error("toggleDndStatus failed device_id={}: {}", device_id, e.getMessage(), e);
            throw e;
        }
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
    @RequestMapping(method = RequestMethod.POST, value = "/deviceid/{id}/timetamp/{timetamp}/updatedndstatus")
    public void updatedndstatus(@PathVariable String id, @PathVariable BigInteger timetamp) {
        log.info("updatedndstatus id={} timetamp={}", id, timetamp);
        try {
            deviceService.UpdateDeviceDndEnabledAndTimestamp(id, timetamp);
        } catch (Exception e) {
            log.error("updatedndstatus failed id={} timetamp={}: {}", id, timetamp, e.getMessage(), e);
            throw e;
        }
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
    @RequestMapping(method = RequestMethod.GET, value = "/docker/{docker_name}/getalldevicedetails")
    public List<DeviceDTO> getAllDeviceCustomDetails(@RequestParam String username, @RequestParam String vdmsid, @PathVariable String docker_name,
                                                     @RequestParam(defaultValue = "1") Integer page_no, @RequestParam(defaultValue = "10") Integer page_size,
                                                     @RequestParam(defaultValue = "null") String search_key, @RequestParam(defaultValue = "internal") String profile_type) throws IOException {
        log.info("getAllDeviceCustomDetails username={} vdmsid={} docker_name={}", username, vdmsid, docker_name);
        try {
            return deviceService.getAllDeviceCustomDetails(username, vdmsid, docker_name, page_no, page_size, search_key, profile_type);
        } catch (Exception e) {
            log.error("getAllDeviceCustomDetails failed username={} vdmsid={} docker_name={}: {}", username, vdmsid, docker_name, e.getMessage(), e);
            throw e;
        }
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
    @RequestMapping(method = RequestMethod.POST, value = "/docker/{docker_name}/getdevicedetailsbyids")
    public List<DeviceDTO> getDeviceCustomDetails(@PathVariable String docker_name,
                                                  @RequestParam(defaultValue = "1") Integer page_no, @RequestParam(defaultValue = "10") Integer page_size,
                                                  @RequestParam(defaultValue = "null") String search_key,
                                                  @RequestBody List<String> device_ids) throws IOException {
        log.info("getDeviceCustomDetails docker_name={}", docker_name);
        try {
            return deviceService.getDeviceCustomDetails(docker_name, page_no, page_size, search_key, device_ids);
        } catch (Exception e) {
            log.error("getDeviceCustomDetails failed docker_name={}: {}", docker_name, e.getMessage(), e);
            throw e;
        }
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
    @RequestMapping(method = RequestMethod.POST, value = "/docker/{docker_name}/getdevicecustomdetailsbyids")
    public List<DeviceDTO> getDeviceCustomDetailsByIds(@RequestParam String username, @RequestParam String vdmsid, @PathVariable String docker_name,
                                                       @RequestParam(defaultValue = "1") Integer page_no, @RequestParam(defaultValue = "10") Integer page_size,
                                                       @RequestParam(defaultValue = "null") String search_key, @RequestParam(defaultValue = "0") Integer has_pagination,
                                                       @RequestBody JSONObject requestBody) throws IOException {
        log.info("getDeviceCustomDetailsByIds username={} vdmsid={} docker_name={}", username, vdmsid, docker_name);
        try {
            return deviceService.getDeviceCustomDetailsByIds(username, vdmsid, docker_name, has_pagination, page_no, page_size, search_key, requestBody);
        } catch (Exception e) {
            log.error("getDeviceCustomDetailsByIds failed username={} vdmsid={} docker_name={}: {}", username, vdmsid, docker_name, e.getMessage(), e);
            throw e;
        }
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
    @RequestMapping(method = RequestMethod.GET, value = "/docker/{docker_name}/getalldeviceids")
    public List<String> getAllDeviceIds(@PathVariable String docker_name,
                                        @RequestParam(defaultValue = "1") Integer page_no, @RequestParam(defaultValue = "10") Integer page_size,
                                        @RequestParam(defaultValue = "null") String search_key,
                                        @RequestParam(defaultValue = "false") String is_select_all){
        log.info("getAllDeviceIds docker_name={}", docker_name);
        try {
            return deviceService.getAllDeviceIds(docker_name, page_no, page_size, search_key, is_select_all);
        } catch (Exception e) {
            log.error("getAllDeviceIds failed docker_name={}: {}", docker_name, e.getMessage(), e);
            throw e;
        }
    }

}


