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
public class DeviceController {

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
    @RequestMapping(method = RequestMethod.GET, value = "/user/{username}/vdms/{vdmsid}/docker/{dockername}/devices")
    public Set<DeviceDTO> listAllDevicebyVdmsidAndDockerName(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String dockername) {
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
    @RequestMapping(method = RequestMethod.GET, value = "/user/{username}/vdms/{vdmsid}/docker/{dockername}/getfilterdevice")
    public Set<DeviceDTO> getfilterdevice(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String dockername, @RequestParam(defaultValue = "all") String condition, @RequestParam(defaultValue = "null") String searchKey,
                                          @RequestParam(defaultValue = "1") Integer pageno, @RequestParam(defaultValue = "10") Integer pagesize) {
        return deviceService.getfilterdevices(username, vdmsid, dockername, condition, searchKey, pageno, pagesize);
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
    @RequestMapping(method = RequestMethod.GET, value = "/user/{username}/vdms/{vdmsid}/docker/{dockername}/getsubsystemparentdevicesbypagination")
    public Set<DeviceDTO> getSubsystemParentDevicesByPagination(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String dockername, @RequestParam(defaultValue = "all") String condition,
                                                                @RequestParam(defaultValue = "1") Integer pageno, @RequestParam(defaultValue = "10") Integer pagesize, @RequestParam(defaultValue = "all") String assignee) {
        return deviceService.getSubsystemParentDevicesByPagination(username, vdmsid, dockername, condition, pageno, pagesize, assignee);
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
    @RequestMapping(method = RequestMethod.GET, value = "/user/{username}/vdms/{vdmsid}/docker/{dockername}/device/{device_id}/getsubsystemdevicesbypagination")
    public Set<DeviceDTO> getSubsystemDevicesByPagination(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String dockername, @PathVariable String device_id, @RequestParam(defaultValue = "all") String condition,
                                                          @RequestParam(defaultValue = "1") Integer pageno, @RequestParam(defaultValue = "10") Integer pagesize, @RequestParam(defaultValue = "all") String assignee) {
        return deviceService.getSubsystemDevicesByPagination(username, vdmsid, dockername, device_id, condition, pageno, pagesize, assignee);
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
    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdmsid}/docker/{dockername}/devicesupsert")
    public void upsertDeviceListByVdmsIdAndDockerName(@RequestBody List<DeviceDTO> devicesDto, @PathVariable String username, @PathVariable String vdmsid, @PathVariable String dockername, @RequestParam(defaultValue = "all") String assignee) {
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
    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdmsid}/docker/{dockername}/device/{device_id}/edit")
    public DeviceDTO editDeviceByDeviceId(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String dockername,
                                          @PathVariable String device_id, @RequestBody DeviceDTO devicedto, HttpServletRequest httpServletRequest, @RequestParam(defaultValue = "all") String assignee) throws JSONException, IOException {
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
    @RequestMapping(method = RequestMethod.PUT, value = "/user/{username}/vdms/{vdmsid}/docker/{dockername}/phoneaccount/{phoneaccount}/device/{device_id}/{vendor_type}")
    public void unlinkVendorByVendorIdAndDeviceId(@PathVariable String username, @PathVariable String dockername, @PathVariable String phoneaccount,
                                                  @PathVariable String device_id, @PathVariable String vendor_type, HttpServletRequest httpServletRequest) {
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
    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdmsid}/docker/{dockername}/phoneaccount/device/{device_id}/{vendor_type}/link")
    public String linkVendorByVendorIdAndDeviceId(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String dockername,
                                                  @PathVariable String device_id, @PathVariable String vendor_type,
                                                  @RequestBody PhonebookAddressDto phonebookaddressdto, HttpServletRequest httpServletRequest) {
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
    @RequestMapping(method = RequestMethod.PUT, value = "/user/{username}/vdms/{vdmsid}/docker/{dockername}/devices")
    public void multiDeviceUpdate(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String dockername,
                                  @RequestBody Set<MultiDeviceDTO> multidevicedtos, HttpServletRequest httpServletRequest, @RequestParam(defaultValue = "all") String assignee) throws JSONException, IOException {
        System.out.println("***************************************************************");
        System.out.println(multidevicedtos);
        System.out.println("***************************************************************");

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
    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdmsid}/docker/{dockername}/devices/quickupdate")
    public Set<DeviceDTO> quickUpdate(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String dockername,
                                      @RequestBody TagDeviceOrLocationDTO tagDeviceOrLocationDTO, HttpServletRequest httpServletRequest, @RequestParam(defaultValue = "all") String assignee) throws IOException {
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
    @RequestMapping(method = RequestMethod.GET, value = "/user/{username}/vdms/{vdmsid}/docker/{dockername}/device/names")
    public Set<DeviceDTO> getDeviceNamesByVdmsIdAndDockerName(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String dockername) {
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
    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdmsid}/docker/{dockername}/addvirtualdevice")
    public void addVirtualDeviceByVdmsIdAndDockerName(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String dockername,
                                                      @RequestParam(value = "images", required = false) List<MultipartFile> asset_images,
                                                      @RequestParam(value = "virtual_devices") String virtualDevicesDTO, HttpServletRequest httpServletRequest, @RequestParam(defaultValue = "all") String assignee) {
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
    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdmsid}/docker/{dockername}/updatevirtualdevice")
    public void editVirtualDeviceByVirtualDeviceId(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String dockername,
                                                   @RequestBody Set<DeviceDTO> virtualDevices, HttpServletRequest httpServletRequest) throws IOException {
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
    @RequestMapping(method = RequestMethod.DELETE, value = "/user/{username}/vdms/{vdmsid}/docker/{dockername}/virtual-device/{virtual_device_id}")
    public void deleteVirtualDeviceByVirtualDeviceId(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String dockername,
                                                     @PathVariable String virtual_device_id, @RequestParam(defaultValue = "all") String assignee) {
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
    @RequestMapping(method = RequestMethod.DELETE, value = "/user/{username}/vdms/{vdmsid}/docker/{dockername}/deletedevices")
    public void deleteDevicesById(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String dockername, @RequestBody Set<String> deviceIds, HttpServletRequest httpServletRequest, @RequestParam(defaultValue = "all") String assignee) {
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
    @RequestMapping(method = RequestMethod.GET, value = "/user/{username}/vdms/{vdmsid}/docker/{dockername}/device/{device_id}/getdevice")
    public DeviceDTO getDeviceByDeviceId(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String dockername, @PathVariable String device_id) {
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
    @RequestMapping(method = RequestMethod.PUT, value = "/user/{username}/vdms/{vdmsid}/docker/{dockername}/virtual-device/{virtual_device_id}/syncstatus")
    public DeviceDTO updateVirtualDeviceStatusByVirtualDeviceId(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String dockername, @PathVariable String virtual_device_id, @RequestBody DeviceDTO virtualdevicedto) throws IOException {
        return deviceService.updateVirtualDeviceStatusByVirtualDeviceId(username, vdmsid, dockername, virtual_device_id, virtualdevicedto);
    }

    /**
     * Returns product details for a hard-coded sample product id (test endpoint).
     *
     * @return the product details for the sample product
     */
    @GetMapping(value = "/test/product")
    public ProductDTO test() {
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
    @RequestMapping(method = RequestMethod.GET, value = "/user/{username}/vdms/{vdmsid}/docker/{dockername}/getdevicecount")
    public Map<String, Integer> getDeviceCount(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String dockername, @RequestParam(defaultValue = "all") String assignee) {


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
    @RequestMapping(method = RequestMethod.GET, value = "/user/{username}/vdms/{vdmsid}/docker/{dockername}/devicetopology")
    public List<DeviceTopologyDTO> listTopologyDevicesByDockerName(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String dockername) {
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
    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdmsid}/docker/{dockername}/updatedeviceposition")
    public void updateDevicePosition(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String dockername,
                                     @RequestBody List<DeviceDTO> devicePositions, HttpServletRequest httpServletRequest) {

        deviceService.updateDevicePosition(devicePositions, vdmsid, username, httpServletRequest);
    }

    // Device list by docker name for integration
    /**
     * Returns the device list for the given docker, intended for integration consumers.
     *
     * @param username   owning user
     * @param vdmsid     owning VDMS id
     * @param dockername docker (gateway) name whose devices are listed
     * @return list of devices for the docker
     */
    @RequestMapping(method = RequestMethod.GET, value = "/user/{username}/vdms/{vdmsid}/docker/{dockername}/devicelistintegration")
    public List<DeviceDTO> listDevicebyDockerIntegration(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String dockername) {
        return deviceService.listDevicebyDockerIntegration(dockername);
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
    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdmsid}/docker/{dockername}/updatetopology")
    public void updateTopology(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String dockername,
                               @RequestBody List<DeviceTopologyDTO> devices, HttpServletRequest httpServletRequest) {
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
    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdmsid}/docker/{dockername}/resettopology")
    public void resetTopology(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String dockername, HttpServletRequest httpServletRequest) {
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
    @RequestMapping(method = RequestMethod.GET, value = "/user/{username}/vdms/{vdmsid}/docker/{dockername}/device/{device_id}/getalldevicesensors")
    public AllSensorsDTO getDeviceSensors(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String dockername,
                                          @PathVariable String device_id) {
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
    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdmsid}/getparentdevicebypagination")
    public Set<DeviceDTO> getParentDeviceByPagination(@PathVariable String username, @PathVariable String vdmsid,
                                                      @RequestParam(defaultValue = "null") String searchKey, @RequestParam(defaultValue = "1") Integer pageno,
                                                      @RequestParam(defaultValue = "10") Integer pagesize, @RequestParam(defaultValue = "all") Set<String> dockernames, @RequestParam(defaultValue = "all") Set<String> types, @RequestParam(defaultValue = "all") Set<String> virtual_device_types) {
        return deviceService.getParentDeviceByPagination(username, vdmsid, searchKey, pageno, pagesize, dockernames, types, virtual_device_types);
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
    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdmsid}/docker/{dockername}/getparentdevice")
    public Set<DeviceDTO> getParentDeviceById(@PathVariable String username, @PathVariable String vdmsid,
                                              @PathVariable String dockername, @RequestBody Set<DeviceDTO> parent_devices, HttpServletRequest httpServletRequest) {
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
    @RequestMapping(method = RequestMethod.GET, value = "/user/{username}/vdms/{vdmsid}/docker/{dockername}/device/{device_id}/parent/{parent_id}/getsubsystemparentdeviceinfo")
    public DeviceDTO getSubsystemParentDeviceInfo(@PathVariable String username, @PathVariable String vdmsid,
                                                  @PathVariable String dockername, @PathVariable String device_id, @PathVariable String parent_id) {
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
    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdmsid}/docker/{dockername}/updatematcheddeviceproduct")
    public void updateMatchedDeviceProduct(@PathVariable String username, @PathVariable String vdmsid,
                                           @PathVariable String dockername, @RequestBody DeviceDTO device, HttpServletRequest httpServletRequest) {
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
    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdmsid}/docker/{dockername}/searchdevices")
    public Set<DeviceDTO> searchDevices(@PathVariable String username, @PathVariable String vdmsid,
                                        @PathVariable String dockername, @RequestParam(defaultValue = "all") String condition,
                                        @RequestParam(defaultValue = "1") Integer pageno, @RequestParam(defaultValue = "10") Integer pagesize,
                                        @RequestBody Map<String, Object> search_details) {
        return deviceSearchService.searchDevices(username, vdmsid, dockername, condition, pageno, pagesize, search_details);
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
    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdmsid}/docker/{dockername}/sortdevices")
    public Set<DeviceDTO> sortDevices(@PathVariable String username, @PathVariable String vdmsid,
                                      @PathVariable String dockername, @RequestParam(defaultValue = "all") String condition,
                                      @RequestParam(defaultValue = "1") Integer pageno, @RequestParam(defaultValue = "10") Integer pagesize,
                                      @RequestBody Map<String, Object> sort_details) {
        return deviceSearchService.sortDevices(username, vdmsid, dockername, condition, pageno, pagesize, sort_details);
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
    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdmsid}/docker/{dockername}/filterdevices")
    public Set<DeviceDTO> filterDevices(@PathVariable String username, @PathVariable String vdmsid,
                                        @PathVariable String dockername, @RequestParam(defaultValue = "all") String condition,
                                        @RequestParam(defaultValue = "1") Integer pageno,
                                        @RequestParam(defaultValue = "10") Integer pagesize, @RequestBody List<Map<String, Object>> filter_details) {
        return deviceSearchService.filterDevices(username, vdmsid, dockername, condition, pageno, pagesize, filter_details);
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
    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdmsid}/docker/{dockername}/archivedevices")
    public void archiveDevices(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String dockername,
                               @RequestParam(defaultValue = "1") Integer archive, @RequestBody Set<String> deviceIds, HttpServletRequest httpServletRequest, @RequestParam(defaultValue = "all") String assignee) {
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
    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdmsid}/docker/{dockername}/getdeviceinfobycustomfields")
    public List<DeviceDTO> getDeviceInfoByCustomFields(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String dockername,
                                                       @RequestBody com.alibaba.fastjson.JSONObject custom_fields) {
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
    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdmsid}/docker/{dockername}/searchsortfilterdevices")
    public Set<DeviceDTO> multipleKeywordSearchSortFilterDevices(@PathVariable String username, @PathVariable String vdmsid,
                                                                 @PathVariable String dockername, @RequestParam(defaultValue = "all") String condition,
                                                                 @RequestParam(defaultValue = "1") Integer pageno,
                                                                 @RequestParam(defaultValue = "10") Integer pagesize,
                                                                 @RequestParam(defaultValue = "123") Integer onboard_status,
                                                                 @RequestBody com.alibaba.fastjson.JSONObject search_sort_filter_details) {
        return deviceSearchService.multipleKeywordSearchSortFilterDevices(username, vdmsid, dockername, condition, pageno, pagesize, search_sort_filter_details, onboard_status);
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
    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdmsid}/docker/{dockername}/searchsortfilterdevicescount")
    public String multipleKeywordSearchSortFilterDevicesCount(@PathVariable String username, @PathVariable String vdmsid,
                                                              @PathVariable String dockername, @RequestParam(defaultValue = "all") String condition,
                                                              @RequestParam(defaultValue = "123") Integer onboard_status,
                                                              @RequestBody com.alibaba.fastjson.JSONObject search_sort_filter_details) {
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
    @RequestMapping(method = RequestMethod.GET, value = "/vdms/{vdms_id}/device/network/{network_name}/getassignedemail")
    public List<String> getUniqueAssignedUser(@PathVariable String vdms_id, @PathVariable String network_name) {
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
    @RequestMapping(method = RequestMethod.GET, value = "/user/{username}/vdms/{vdmsid}/docker/{dockername}/getalertmessages")
    public List<ConditionsDTO> getDeviceAlertMessages(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String dockername) {
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
    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdms_id}/upsertassetimages")
    public void upsertAssetImages(@PathVariable String username, @PathVariable String vdms_id,
                                  @RequestParam List<String> device_ids, @RequestParam(value = "images", required = false) List<MultipartFile> asset_images, HttpServletRequest httpServletRequest) {
        deviceService.upsertAssetImages(username, vdms_id, device_ids, asset_images, httpServletRequest);
    }

    /**
     * Deletes asset images for the given devices.
     *
     * @param username           owning user
     * @param vdms_id            owning VDMS id
     * @param deviceDTOS         devices whose asset images should be deleted
     * @param httpServletRequest current request, used to resolve tenant/VDMS context
     */
    @RequestMapping(method = RequestMethod.DELETE, value = "/user/{username}/vdms/{vdms_id}/deleteassetimages")
    public void deleteAssetImages(@PathVariable String username, @PathVariable String vdms_id, @RequestBody List<DeviceDTO> deviceDTOS, HttpServletRequest httpServletRequest) {
        System.out.println("heere");
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
    @RequestMapping(method = RequestMethod.DELETE, value = "/user/{username}/vdms/{vdms_id}/deletedeviceimages")
    public void deleteDeviceImages(@PathVariable String username, @PathVariable String vdms_id, @RequestBody List<DeviceDTO> deviceDTOS, @RequestParam String category, HttpServletRequest httpServletRequest) {
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
    @RequestMapping(method = RequestMethod.GET, value = "/user/{username}/vdms/{vdms_id}/device/{device_id}/getassetimages")
    public String getAssetImageUrls(@PathVariable String username, @PathVariable String vdms_id, @PathVariable String device_id) {
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
    @RequestMapping(method = RequestMethod.GET, value = "/user/{username}/vdms/{vdms_id}/device/{device_id}/getallassetimages")
    public String getAssetImageUrlsByCategory(@PathVariable String username, @PathVariable String vdms_id, @PathVariable String device_id) {
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
    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdmsid}/group/{group}/getalldevicespagination")
    public Set<DeviceDTO> getAllDevicesPagination(@PathVariable String username, @PathVariable String vdmsid,
                                                  @PathVariable String group, @RequestParam(defaultValue = "null") String searchkey,
                                                  @RequestParam(defaultValue = "1") Integer pageno, @RequestParam(defaultValue = "10") Integer pagesize,
                                                  @RequestBody JSONObject filterObject) {
        return deviceService.getAllDevicesPagination(username, vdmsid, group, searchkey, pageno, pagesize, filterObject);
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
    @RequestMapping(method = RequestMethod.GET, value = "/user/{username}/vdms/{vdmsid}/getfiltervirtualdevicesbypagination")
    public Set<DeviceDTO> getFilterVirtualDevicesByPagination(@PathVariable String username, @PathVariable String vdmsid, @RequestParam(defaultValue = "null") String searchKey,
                                                              @RequestParam(defaultValue = "1") Integer pageno, @RequestParam(defaultValue = "10") Integer pagesize, @RequestParam(defaultValue = "all") Set<String> dockernames, @RequestParam(defaultValue = "all") Set<String> types, @RequestParam(defaultValue = "all") Set<String> virtual_device_types) {
        return deviceService.getFilterVirtualDevicesByPagination(username, vdmsid, searchKey, pageno, pagesize, dockernames, types, virtual_device_types);
    }

    /**
     * Returns the number of power-source topology connections for the given VDMS.
     *
     * @param username owning user
     * @param vdmsid   owning VDMS id
     * @return count of power-source topology connections
     */
    @RequestMapping(method = RequestMethod.GET, value = "/user/{username}/vdms/{vdmsid}/getpowersourcetopologyconnectionscount")
    public Integer getPowerSourceTopologyConnectionsCount(@PathVariable String username, @PathVariable String vdmsid) {
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
    @RequestMapping(method = RequestMethod.GET, value = "/user/{username}/vdms/{vdmsid}/getpowersourcetopologybypagination")
    public PowerSourceTopologyDTO getPowerSourceTopologyByPagination(@PathVariable String username, @PathVariable String vdmsid,
                                                                     @RequestParam(defaultValue = "1") Integer pageno, @RequestParam(defaultValue = "10") Integer pagesize) {
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
    @RequestMapping(method = RequestMethod.GET, value = "/user/{username}/vdms/{vdmsid}/location/{location_id}/getdevicesbylocationid")
    public Set<DeviceDTO> getAssetsByLocationId(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String location_id,
                                                @RequestParam(defaultValue = "1") Integer pageno, @RequestParam(defaultValue = "10") Integer pagesize) {
        return deviceService.getAssetsByLocationId(username, vdmsid, location_id, pageno, pagesize);
    }

    /**
     * Returns the reboot status of the given device.
     *
     * @param username owning user
     * @param vdmsid   owning VDMS id
     * @param deviceid device whose reboot status is requested
     * @return the device reboot status
     */
    @RequestMapping(method = RequestMethod.GET, value = "/user/{username}/vdms/{vdmsid}/device/{deviceid}/getdevicerebootstatus")
    public String getDeviceRebootStatus(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String deviceid) {

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
    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdms_id}/upsertassetocrimages")
    public void upsertAssetOcrImages(@PathVariable String username, @PathVariable String vdms_id,
                                     @RequestParam List<String> device_ids, @RequestParam(value = "images", required = false) List<MultipartFile> asset_ocr_images, HttpServletRequest httpServletRequest) {
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
    @RequestMapping(method = RequestMethod.DELETE, value = "/user/{username}/vdms/{vdms_id}/deleteassetocrimages")
    public void deleteAssetOcrImages(@PathVariable String username, @PathVariable String vdms_id, @RequestBody List<DeviceDTO> deviceDTOS, HttpServletRequest httpServletRequest) {
        System.out.println("heere");
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
    @RequestMapping(method = RequestMethod.GET, value = "/user/{username}/vdms/{vdms_id}/device/{device_id}/getassetocrimages")
    public String getAssetOcrImageUrls(@PathVariable String username, @PathVariable String vdms_id, @PathVariable String device_id) {
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
    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdmsid}/docker/{dockername}/adddevice")
    public DeviceDTO addDevice(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String dockername,
                               @RequestBody DeviceDTO deviceDto, HttpServletRequest httpServletRequest) {
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
    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdmsid}/docker/{dockername}/exportfiltereddevices")
    public void exportFilteredDevices(HttpServletResponse response, @PathVariable String username, @PathVariable String vdmsid, @PathVariable String dockername,
                                      @RequestParam(defaultValue = "all") String condition, @RequestParam(defaultValue = "123") Integer onboard_status,
                                      @RequestParam(defaultValue = "simple_report") String template_name, @RequestParam(defaultValue = "excel") String file_type,
                                      @RequestBody com.alibaba.fastjson.JSONObject search_sort_filter_details, @RequestParam(defaultValue = "") String email, HttpServletRequest httpServletRequest) throws IOException {
        deviceService.exportFilteredDevices(response, username, vdmsid, dockername, condition, search_sort_filter_details, onboard_status,
                template_name, email, httpServletRequest, file_type);

    }

    /**
     * Syncs the onboard status of all devices in the given VDMS.
     *
     * @param vdmsid owning VDMS id
     */
    @RequestMapping(method = RequestMethod.GET, value = "/vdms/{vdmsid}/syncdeviceonboardstatus")
    public void syncDeviceOnboardStatus(@PathVariable String vdmsid) {
        deviceService.syncDeviceOnboardStatus(vdmsid);
    }

    /**
     * Syncs the onboard status of a single device in the given VDMS.
     *
     * @param vdmsid    owning VDMS id
     * @param device_id device whose onboard status is synced
     */
    @RequestMapping(method = RequestMethod.GET, value = "/vdms/{vdmsid}/device/{device_id}/syncsingledeviceonboardstatus")
    public void syncSingleDeviceOnboardStatus(@PathVariable String vdmsid, @PathVariable String device_id) {
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
    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdmsid}/docker/{dockername}/updateassetmatchdetails")
    public DeviceDTO updateAssetMatchDetails(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String dockername,
                                             @RequestBody JSONObject deviceObject, HttpServletRequest httpServletRequest, @RequestParam(defaultValue = "all") String assignee) {
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
    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdmsid}/upsertdigitaltwininstruments")
    public void upsertDigitalTwin(@PathVariable String username, @PathVariable String vdmsid, @RequestBody TagDeviceOrLocationDTO filterObject, HttpServletRequest httpServletRequest) {
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
    @RequestMapping(method = RequestMethod.DELETE, value = "/user/{username}/vdms/{vdmsid}/device_id/{device_id}/deletedigitaltwin")
    public void deleteDigitalTwin(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String device_id, HttpServletRequest httpServletRequest) {
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
    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdmsid}/multieditdigitaltwininstruments")
    public void multiEditDigitalTwin(@PathVariable String username, @PathVariable String vdmsid, @RequestParam(required = true) String data,
                                     @RequestParam(required = false) String image_url,
                                     @RequestParam(required = false) MultipartFile image, HttpServletRequest httpServletRequest) {
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
    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdmsid}/docker/{dockername}/exportfilteredmeasuringinstrument")
    public void exportFilteredMeasuringInstrument(HttpServletResponse response, @PathVariable String username, @PathVariable String vdmsid,
                                                  @PathVariable String dockername, @RequestParam(defaultValue = "all") String condition,
                                                  @RequestParam(defaultValue = "1") Integer pageno,
                                                  @RequestParam(defaultValue = "10") Integer pagesize,
                                                  @RequestParam(defaultValue = "123") Integer onboard_status,
                                                  @RequestBody com.alibaba.fastjson.JSONObject search_sort_filter_details) throws IOException {
        deviceService.exportFilteredMeasuringInstrument(response, username, vdmsid, dockername, condition, pageno, pagesize, search_sort_filter_details, onboard_status);
    }

    /**
     * Recomputes and updates device types, optionally scoped to a single VDMS.
     *
     * @param vdmsId optional VDMS id to scope the update to; when absent applies to all
     * @return response describing the outcome of the update
     */
    @RequestMapping(method = RequestMethod.PUT, value = "/vdms/updatedevicetype")
    public ResponseDTO updateDeviceTypes(@RequestParam(required = false) String vdmsId) {
        return deviceService.updateDeviceTypeForAll(vdmsId);
    }

    /**
     * Toggles the do-not-disturb (DND) status of a device.
     *
     * @param username           owning user
     * @param vdmsid             owning VDMS id
     * @param device_id          device whose DND status is toggled (required)
     * @param is_dnd_enabled     whether DND should be enabled (required)
     * @param httpServletRequest current request, used to resolve tenant/VDMS context
     * @throws IOException if downstream I/O fails
     */
    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdmsid}/togglednd")
    public void toggleDndStatus(@PathVariable String username, @PathVariable String vdmsid, @RequestParam(value = "device_id", required = true) String device_id,
                                @RequestParam(value = "is_dnd_enabled", required = true) Boolean is_dnd_enabled, HttpServletRequest httpServletRequest) throws IOException {
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
    @RequestMapping(method = RequestMethod.POST, value = "/deviceid/{id}/timetamp/{timetamp}/updatedndstatus")
    public void updatedndstatus(@PathVariable String id, @PathVariable BigInteger timetamp) {
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
    @RequestMapping(method = RequestMethod.GET, value = "/user/{username}/vdms/{vdmsid}/docker/{docker_name}/getalldevicedetails")
    public List<DeviceDTO> getAllDeviceCustomDetails(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String docker_name,
                                                     @RequestParam(defaultValue = "1") Integer page_no, @RequestParam(defaultValue = "10") Integer page_size,
                                                     @RequestParam(defaultValue = "null") String search_key, @RequestParam(defaultValue = "internal") String profile_type) throws IOException {
        return deviceService.getAllDeviceCustomDetails(username, vdmsid, docker_name, page_no, page_size, search_key, profile_type);
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
        return deviceService.getDeviceCustomDetails(docker_name, page_no, page_size, search_key, device_ids);
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
    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdmsid}/docker/{docker_name}/getdevicecustomdetailsbyids")
    public List<DeviceDTO> getDeviceCustomDetailsByIds(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String docker_name,
                                                       @RequestParam(defaultValue = "1") Integer page_no, @RequestParam(defaultValue = "10") Integer page_size,
                                                       @RequestParam(defaultValue = "null") String search_key, @RequestParam(defaultValue = "0") Integer has_pagination,
                                                       @RequestBody JSONObject requestBody) throws IOException {
        return deviceService.getDeviceCustomDetailsByIds(username, vdmsid, docker_name, has_pagination, page_no, page_size, search_key, requestBody);
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
        return deviceService.getAllDeviceIds(docker_name, page_no, page_size, search_key, is_select_all);
    }

}


