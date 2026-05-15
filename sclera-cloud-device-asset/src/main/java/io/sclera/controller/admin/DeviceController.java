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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import io.sclera.service.APICallService;
import io.sclera.service.DeviceSearchService;
import io.sclera.service.DeviceService;
import io.sclera.utils.APIRequest;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class DeviceController {

    @Autowired
    DeviceService deviceService;

    @Autowired
    APIRequest apiRequest;

    @Autowired
    APICallService apicallService;

    @Autowired
    DeviceSearchService deviceSearchService;

    @Autowired
    DeviceMonitorService deviceMonitorService;

    @RequestMapping(method = RequestMethod.GET, value = "/user/{username}/vdms/{vdmsid}/docker/{dockername}/devices")
    public Set<DeviceDTO> listAllDevicebyVdmsidAndDockerName(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String dockername) {
        return deviceService.listAllDevicebyVdmsidAndDockerName(username, vdmsid, dockername);

    }

    @RequestMapping(method = RequestMethod.GET, value = "/user/{username}/vdms/{vdmsid}/docker/{dockername}/getfilterdevice")
    public Set<DeviceDTO> getfilterdevice(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String dockername, @RequestParam(defaultValue = "all") String condition, @RequestParam(defaultValue = "null") String searchKey,
                                          @RequestParam(defaultValue = "1") Integer pageno, @RequestParam(defaultValue = "10") Integer pagesize) {
        return deviceService.getfilterdevices(username, vdmsid, dockername, condition, searchKey, pageno, pagesize);
    }

    //new get method with subsystem parent device get initial
    @RequestMapping(method = RequestMethod.GET, value = "/user/{username}/vdms/{vdmsid}/docker/{dockername}/getsubsystemparentdevicesbypagination")
    public Set<DeviceDTO> getSubsystemParentDevicesByPagination(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String dockername, @RequestParam(defaultValue = "all") String condition,
                                                                @RequestParam(defaultValue = "1") Integer pageno, @RequestParam(defaultValue = "10") Integer pagesize, @RequestParam(defaultValue = "all") String assignee) {
        return deviceService.getSubsystemParentDevicesByPagination(username, vdmsid, dockername, condition, pageno, pagesize, assignee);
    }

    //new get method with subsystem devices get
    @RequestMapping(method = RequestMethod.GET, value = "/user/{username}/vdms/{vdmsid}/docker/{dockername}/device/{device_id}/getsubsystemdevicesbypagination")
    public Set<DeviceDTO> getSubsystemDevicesByPagination(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String dockername, @PathVariable String device_id, @RequestParam(defaultValue = "all") String condition,
                                                          @RequestParam(defaultValue = "1") Integer pageno, @RequestParam(defaultValue = "10") Integer pagesize, @RequestParam(defaultValue = "all") String assignee) {
        return deviceService.getSubsystemDevicesByPagination(username, vdmsid, dockername, device_id, condition, pageno, pagesize, assignee);
    }


    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdmsid}/docker/{dockername}/devicesupsert")
    public void upsertDeviceListByVdmsIdAndDockerName(@RequestBody List<DeviceDTO> devicesDto, @PathVariable String username, @PathVariable String vdmsid, @PathVariable String dockername, @RequestParam(defaultValue = "all") String assignee) {
        deviceService.upsertDeviceListByVdmsIdAndDockerName(devicesDto, username, vdmsid, dockername, assignee);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdmsid}/docker/{dockername}/device/{device_id}/edit")
    public DeviceDTO editDeviceByDeviceId(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String dockername,
                                          @PathVariable String device_id, @RequestBody DeviceDTO devicedto, HttpServletRequest httpServletRequest, @RequestParam(defaultValue = "all") String assignee) throws JSONException, IOException {
        return deviceService.editDeviceByDeviceID(username, vdmsid, dockername, device_id, devicedto, httpServletRequest, assignee);
    }


    @RequestMapping(method = RequestMethod.PUT, value = "/user/{username}/vdms/{vdmsid}/docker/{dockername}/phoneaccount/{phoneaccount}/device/{device_id}/{vendor_type}")
    public void unlinkVendorByVendorIdAndDeviceId(@PathVariable String username, @PathVariable String dockername, @PathVariable String phoneaccount,
                                                  @PathVariable String device_id, @PathVariable String vendor_type, HttpServletRequest httpServletRequest) {
        deviceService.unlinkVendorByVendorIdAndDeviceId(username, dockername, phoneaccount, device_id, vendor_type, httpServletRequest);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdmsid}/docker/{dockername}/phoneaccount/device/{device_id}/{vendor_type}/link")
    public String linkVendorByVendorIdAndDeviceId(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String dockername,
                                                  @PathVariable String device_id, @PathVariable String vendor_type,
                                                  @RequestBody PhonebookAddressDto phonebookaddressdto, HttpServletRequest httpServletRequest) {
        return deviceService.linkVendorByVendorIdAndDeviceId(username, vdmsid, dockername, device_id, phonebookaddressdto, vendor_type, httpServletRequest);
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/user/{username}/vdms/{vdmsid}/docker/{dockername}/devices")
    public void multiDeviceUpdate(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String dockername,
                                  @RequestBody Set<MultiDeviceDTO> multidevicedtos, HttpServletRequest httpServletRequest, @RequestParam(defaultValue = "all") String assignee) throws JSONException, IOException {
        System.out.println("***************************************************************");
        System.out.println(multidevicedtos);
        System.out.println("***************************************************************");

        deviceService.multiDeviceUpdate(username, vdmsid, dockername, multidevicedtos, httpServletRequest, assignee);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdmsid}/docker/{dockername}/devices/quickupdate")
    public Set<DeviceDTO> quickUpdate(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String dockername,
                                      @RequestBody TagDeviceOrLocationDTO tagDeviceOrLocationDTO, HttpServletRequest httpServletRequest, @RequestParam(defaultValue = "all") String assignee) throws IOException {
        return deviceService.quickUpdate(username, vdmsid, dockername, tagDeviceOrLocationDTO, httpServletRequest, assignee);
    }

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
    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdmsid}/docker/{dockername}/updatevirtualdevice")
    public void editVirtualDeviceByVirtualDeviceId(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String dockername,
                                                   @RequestBody Set<DeviceDTO> virtualDevices, HttpServletRequest httpServletRequest) throws IOException {
        deviceService.editVirtualDeviceByVirtualDeviceId(username, vdmsid, dockername, virtualDevices, httpServletRequest);
    }


    @RequestMapping(method = RequestMethod.DELETE, value = "/user/{username}/vdms/{vdmsid}/docker/{dockername}/virtual-device/{virtual_device_id}")
    public void deleteVirtualDeviceByVirtualDeviceId(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String dockername,
                                                     @PathVariable String virtual_device_id, @RequestParam(defaultValue = "all") String assignee) {
        deviceService.deleteVirtualDeviceByVirtualDeviceId(username, vdmsid, dockername, virtual_device_id, assignee);
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/user/{username}/vdms/{vdmsid}/docker/{dockername}/deletedevices")
    public void deleteDevicesById(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String dockername, @RequestBody Set<String> deviceIds, HttpServletRequest httpServletRequest, @RequestParam(defaultValue = "all") String assignee) {
//        deviceService.deleteDevicesById(username, vdmsid, dockername, deviceIds, httpServletRequest);
        deviceService.softDeleteDevicesById(username, vdmsid, dockername, deviceIds, httpServletRequest, assignee);
    }

    //Get Single device information
    @RequestMapping(method = RequestMethod.GET, value = "/user/{username}/vdms/{vdmsid}/docker/{dockername}/device/{device_id}/getdevice")
    public DeviceDTO getDeviceByDeviceId(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String dockername, @PathVariable String device_id) {
        return deviceService.getDeviceByDeviceId(username, vdmsid, dockername, device_id);
    }

    //Sync Virtual Device Status
    @RequestMapping(method = RequestMethod.PUT, value = "/user/{username}/vdms/{vdmsid}/docker/{dockername}/virtual-device/{virtual_device_id}/syncstatus")
    public DeviceDTO updateVirtualDeviceStatusByVirtualDeviceId(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String dockername, @PathVariable String virtual_device_id, @RequestBody DeviceDTO virtualdevicedto) throws IOException {
        return deviceService.updateVirtualDeviceStatusByVirtualDeviceId(username, vdmsid, dockername, virtual_device_id, virtualdevicedto);
    }

    @GetMapping(value = "/test/product")
    public ProductDTO test() throws JsonMappingException, JsonProcessingException {
        String product_id = "6aeeae74-2855-4b67-943e-49d979a45abf";
        return apicallService.getProductDetailsByProductId(product_id);
    }


    @RequestMapping(method = RequestMethod.GET, value = "/user/{username}/vdms/{vdmsid}/docker/{dockername}/getdevicecount")
    public Map<String, Integer> getDeviceCount(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String dockername, @RequestParam(defaultValue = "all") String assignee) {


        return deviceService.getDeviceCount(username, vdmsid, dockername, assignee);

    }

    //listing all devices for snmp topology
    @RequestMapping(method = RequestMethod.GET, value = "/user/{username}/vdms/{vdmsid}/docker/{dockername}/devicetopology")
    public List<DeviceTopologyDTO> listTopologyDevicesByDockerName(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String dockername) {
        return deviceService.listTopologyDevicesByDockerName(username, vdmsid, dockername);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdmsid}/docker/{dockername}/updatedeviceposition")
    public void updateDevicePosition(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String dockername,
                                     @RequestBody List<DeviceDTO> devicePositions, HttpServletRequest httpServletRequest) {

        deviceService.updateDevicePosition(devicePositions, vdmsid, username, httpServletRequest);
    }

    // Device list by docker name for integration
    @RequestMapping(method = RequestMethod.GET, value = "/user/{username}/vdms/{vdmsid}/docker/{dockername}/devicelistintegration")
    public List<DeviceDTO> listDevicebyDockerIntegration(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String dockername) {
        return deviceService.listDevicebyDockerIntegration(dockername);
    }

    //Get Gateway ID
    @RequestMapping(method = RequestMethod.GET, value = "/docker/{dockername}/getgatewayid")
    public String getGatewayId(@PathVariable String dockername) {
        return deviceService.getGatewayId(dockername);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdmsid}/docker/{dockername}/updatetopology")
    public void updateTopology(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String dockername,
                               @RequestBody List<DeviceTopologyDTO> devices, HttpServletRequest httpServletRequest) {
        deviceService.updateTopology(username, vdmsid, dockername, devices, httpServletRequest);
    }

    //reset topology
    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdmsid}/docker/{dockername}/resettopology")
    public void resetTopology(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String dockername, HttpServletRequest httpServletRequest) {
        deviceService.resetTopologyByDockername(username, vdmsid, dockername, httpServletRequest);
    }

    //Get All sensors tagged to a device
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

    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdmsid}/getparentdevicebypagination")
    public Set<DeviceDTO> getParentDeviceByPagination(@PathVariable String username, @PathVariable String vdmsid,
                                                      @RequestParam(defaultValue = "null") String searchKey, @RequestParam(defaultValue = "1") Integer pageno,
                                                      @RequestParam(defaultValue = "10") Integer pagesize, @RequestParam(defaultValue = "all") Set<String> dockernames, @RequestParam(defaultValue = "all") Set<String> types, @RequestParam(defaultValue = "all") Set<String> virtual_device_types) {
        return deviceService.getParentDeviceByPagination(username, vdmsid, searchKey, pageno, pagesize, dockernames, types, virtual_device_types);
    }

    //Get Parent Device by Id
    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdmsid}/docker/{dockername}/getparentdevice")
    public Set<DeviceDTO> getParentDeviceById(@PathVariable String username, @PathVariable String vdmsid,
                                              @PathVariable String dockername, @RequestBody Set<DeviceDTO> parent_devices, HttpServletRequest httpServletRequest) {
        return deviceService.getParentDeviceById(username, vdmsid, dockername, parent_devices, httpServletRequest);
    }

    //get subsystem parent device info
    @RequestMapping(method = RequestMethod.GET, value = "/user/{username}/vdms/{vdmsid}/docker/{dockername}/device/{device_id}/parent/{parent_id}/getsubsystemparentdeviceinfo")
    public DeviceDTO getSubsystemParentDeviceInfo(@PathVariable String username, @PathVariable String vdmsid,
                                                  @PathVariable String dockername, @PathVariable String device_id, @PathVariable String parent_id) {
        return deviceService.getSubsystemParentDeviceInfo(username, vdmsid, dockername, device_id, parent_id);
    }

    //update device matched product info
    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdmsid}/docker/{dockername}/updatematcheddeviceproduct")
    public void updateMatchedDeviceProduct(@PathVariable String username, @PathVariable String vdmsid,
                                           @PathVariable String dockername, @RequestBody DeviceDTO device, HttpServletRequest httpServletRequest) {
        deviceService.updateMatchedDeviceProduct(username, vdmsid, dockername, device, httpServletRequest);
    }

    //search device by specific column or all columns
    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdmsid}/docker/{dockername}/searchdevices")
    public Set<DeviceDTO> searchDevices(@PathVariable String username, @PathVariable String vdmsid,
                                        @PathVariable String dockername, @RequestParam(defaultValue = "all") String condition,
                                        @RequestParam(defaultValue = "1") Integer pageno, @RequestParam(defaultValue = "10") Integer pagesize,
                                        @RequestBody Map<String, Object> search_details) {
        return deviceSearchService.searchDevices(username, vdmsid, dockername, condition, pageno, pagesize, search_details);
    }

    //sort device by specific column
    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdmsid}/docker/{dockername}/sortdevices")
    public Set<DeviceDTO> sortDevices(@PathVariable String username, @PathVariable String vdmsid,
                                      @PathVariable String dockername, @RequestParam(defaultValue = "all") String condition,
                                      @RequestParam(defaultValue = "1") Integer pageno, @RequestParam(defaultValue = "10") Integer pagesize,
                                      @RequestBody Map<String, Object> sort_details) {
        return deviceSearchService.sortDevices(username, vdmsid, dockername, condition, pageno, pagesize, sort_details);
    }

    //filter devices by specific or multiple columns
    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdmsid}/docker/{dockername}/filterdevices")
    public Set<DeviceDTO> filterDevices(@PathVariable String username, @PathVariable String vdmsid,
                                        @PathVariable String dockername, @RequestParam(defaultValue = "all") String condition,
                                        @RequestParam(defaultValue = "1") Integer pageno,
                                        @RequestParam(defaultValue = "10") Integer pagesize, @RequestBody List<Map<String, Object>> filter_details) {
        return deviceSearchService.filterDevices(username, vdmsid, dockername, condition, pageno, pagesize, filter_details);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdmsid}/docker/{dockername}/archivedevices")
    public void archiveDevices(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String dockername,
                               @RequestParam(defaultValue = "1") Integer archive, @RequestBody Set<String> deviceIds, HttpServletRequest httpServletRequest, @RequestParam(defaultValue = "all") String assignee) {
        deviceService.archiveDevices(username, vdmsid, dockername, archive, deviceIds, httpServletRequest, assignee);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdmsid}/docker/{dockername}/getdeviceinfobycustomfields")
    public List<DeviceDTO> getDeviceInfoByCustomFields(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String dockername,
                                                       @RequestBody com.alibaba.fastjson.JSONObject custom_fields) {
        return deviceSearchService.getDeviceInfoByCustomFields(username, vdmsid, dockername, custom_fields);
    }

    //multiple keyword search sort filter
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
    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdmsid}/docker/{dockername}/searchsortfilterdevicescount")
    public String multipleKeywordSearchSortFilterDevicesCount(@PathVariable String username, @PathVariable String vdmsid,
                                                              @PathVariable String dockername, @RequestParam(defaultValue = "all") String condition,
                                                              @RequestParam(defaultValue = "123") Integer onboard_status,
                                                              @RequestBody com.alibaba.fastjson.JSONObject search_sort_filter_details) {
        return deviceSearchService.multipleKeywordSearchSortFilterDevicesCount(username, vdmsid, dockername, condition,
                search_sort_filter_details, onboard_status);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/vdms/{vdms_id}/device/network/{network_name}/getassignedemail")
    public List<String> getUniqueAssignedUser(@PathVariable String vdms_id, @PathVariable String network_name) {
        return deviceMonitorService.getUniqueAssignedUserEmail(vdms_id, network_name);
    }

    //Device Alert Message
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

    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdms_id}/upsertassetimages")
    public void upsertAssetImages(@PathVariable String username, @PathVariable String vdms_id,
                                  @RequestParam List<String> device_ids, @RequestParam(value = "images", required = false) List<MultipartFile> asset_images, HttpServletRequest httpServletRequest) {
        deviceService.upsertAssetImages(username, vdms_id, device_ids, asset_images, httpServletRequest);
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/user/{username}/vdms/{vdms_id}/deleteassetimages")
    public void deleteAssetImages(@PathVariable String username, @PathVariable String vdms_id, @RequestBody List<DeviceDTO> deviceDTOS, HttpServletRequest httpServletRequest) {
        System.out.println("heere");
        deviceService.deleteAssetImages(username, vdms_id, deviceDTOS, httpServletRequest);
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/user/{username}/vdms/{vdms_id}/deletedeviceimages")
    public void deleteDeviceImages(@PathVariable String username, @PathVariable String vdms_id, @RequestBody List<DeviceDTO> deviceDTOS, @RequestParam String category, HttpServletRequest httpServletRequest) {
        deviceService.deleteDeviceImages(username, vdms_id, deviceDTOS, category,httpServletRequest);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/user/{username}/vdms/{vdms_id}/device/{device_id}/getassetimages")
    public String getAssetImageUrls(@PathVariable String username, @PathVariable String vdms_id, @PathVariable String device_id) {
        return deviceService.getAssetImageUrls(username, vdms_id, device_id);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/user/{username}/vdms/{vdms_id}/device/{device_id}/getallassetimages")
    public String getAssetImageUrlsByCategory(@PathVariable String username, @PathVariable String vdms_id, @PathVariable String device_id) {
        return deviceService.getAssetImageUrlsCategory(username, vdms_id, device_id);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdmsid}/group/{group}/getalldevicespagination")
    public Set<DeviceDTO> getAllDevicesPagination(@PathVariable String username, @PathVariable String vdmsid,
                                                  @PathVariable String group, @RequestParam(defaultValue = "null") String searchkey,
                                                  @RequestParam(defaultValue = "1") Integer pageno, @RequestParam(defaultValue = "10") Integer pagesize,
                                                  @RequestBody JSONObject filterObject) {
        return deviceService.getAllDevicesPagination(username, vdmsid, group, searchkey, pageno, pagesize, filterObject);
    }


    @RequestMapping(method = RequestMethod.GET, value = "/user/{username}/vdms/{vdmsid}/getfiltervirtualdevicesbypagination")
    public Set<DeviceDTO> getFilterVirtualDevicesByPagination(@PathVariable String username, @PathVariable String vdmsid, @RequestParam(defaultValue = "null") String searchKey,
                                                              @RequestParam(defaultValue = "1") Integer pageno, @RequestParam(defaultValue = "10") Integer pagesize, @RequestParam(defaultValue = "all") Set<String> dockernames, @RequestParam(defaultValue = "all") Set<String> types, @RequestParam(defaultValue = "all") Set<String> virtual_device_types) {
        return deviceService.getFilterVirtualDevicesByPagination(username, vdmsid, searchKey, pageno, pagesize, dockernames, types, virtual_device_types);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/user/{username}/vdms/{vdmsid}/getpowersourcetopologyconnectionscount")
    public Integer getPowerSourceTopologyConnectionsCount(@PathVariable String username, @PathVariable String vdmsid) {
        return deviceService.getPowerSourceTopologyConnectionsCount(username, vdmsid);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/user/{username}/vdms/{vdmsid}/getpowersourcetopologybypagination")
    public PowerSourceTopologyDTO getPowerSourceTopologyByPagination(@PathVariable String username, @PathVariable String vdmsid,
                                                                     @RequestParam(defaultValue = "1") Integer pageno, @RequestParam(defaultValue = "10") Integer pagesize) {
        return deviceService.getPowerSourceTopologyByPagination(username, vdmsid, pageno, pagesize);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/user/{username}/vdms/{vdmsid}/location/{location_id}/getdevicesbylocationid")
    public Set<DeviceDTO> getAssetsByLocationId(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String location_id,
                                                @RequestParam(defaultValue = "1") Integer pageno, @RequestParam(defaultValue = "10") Integer pagesize) {
        return deviceService.getAssetsByLocationId(username, vdmsid, location_id, pageno, pagesize);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/user/{username}/vdms/{vdmsid}/device/{deviceid}/getdevicerebootstatus")
    public String getDeviceRebootStatus(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String deviceid) {

        return deviceService.getDeviceRebootStatus(username, vdmsid, deviceid);

    }

    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdms_id}/upsertassetocrimages")
    public void upsertAssetOcrImages(@PathVariable String username, @PathVariable String vdms_id,
                                     @RequestParam List<String> device_ids, @RequestParam(value = "images", required = false) List<MultipartFile> asset_ocr_images, HttpServletRequest httpServletRequest) {
        deviceService.upsertAssetOcrImages(username, vdms_id, device_ids, asset_ocr_images, httpServletRequest);
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/user/{username}/vdms/{vdms_id}/deleteassetocrimages")
    public void deleteAssetOcrImages(@PathVariable String username, @PathVariable String vdms_id, @RequestBody List<DeviceDTO> deviceDTOS, HttpServletRequest httpServletRequest) {
        System.out.println("heere");
        deviceService.deleteAssetOcrImages(username, vdms_id, deviceDTOS, httpServletRequest);
    }


    @RequestMapping(method = RequestMethod.GET, value = "/user/{username}/vdms/{vdms_id}/device/{device_id}/getassetocrimages")
    public String getAssetOcrImageUrls(@PathVariable String username, @PathVariable String vdms_id, @PathVariable String device_id) {
        return deviceService.getAssetOcrImageUrls(username, vdms_id, device_id);
    }


    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdmsid}/docker/{dockername}/adddevice")
    public DeviceDTO addDevice(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String dockername,
                               @RequestBody DeviceDTO deviceDto, HttpServletRequest httpServletRequest) {
        return deviceService.addDevice(username, vdmsid, dockername, deviceDto, httpServletRequest);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdmsid}/docker/{dockername}/exportfiltereddevices")
    public void exportFilteredDevices(HttpServletResponse response, @PathVariable String username, @PathVariable String vdmsid, @PathVariable String dockername,
                                      @RequestParam(defaultValue = "all") String condition, @RequestParam(defaultValue = "123") Integer onboard_status,
                                      @RequestParam(defaultValue = "simple_report") String template_name, @RequestParam(defaultValue = "excel") String file_type,
                                      @RequestBody com.alibaba.fastjson.JSONObject search_sort_filter_details, @RequestParam(defaultValue = "") String email, HttpServletRequest httpServletRequest) throws IOException {
        deviceService.exportFilteredDevices(response, username, vdmsid, dockername, condition, search_sort_filter_details, onboard_status,
                template_name, email, httpServletRequest, file_type);

    }

    @RequestMapping(method = RequestMethod.GET, value = "/vdms/{vdmsid}/syncdeviceonboardstatus")
    public void syncDeviceOnboardStatus(@PathVariable String vdmsid) {
        deviceService.syncDeviceOnboardStatus(vdmsid);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/vdms/{vdmsid}/device/{device_id}/syncsingledeviceonboardstatus")
    public void syncSingleDeviceOnboardStatus(@PathVariable String vdmsid, @PathVariable String device_id) {
        deviceService.syncSingleDeviceOnboardStatus(vdmsid, device_id);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdmsid}/docker/{dockername}/updateassetmatchdetails")
    public DeviceDTO updateAssetMatchDetails(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String dockername,
                                             @RequestBody JSONObject deviceObject, HttpServletRequest httpServletRequest, @RequestParam(defaultValue = "all") String assignee) throws JsonProcessingException {
        return deviceService.updateAssetMatchDetails(username, vdmsid, dockername, deviceObject, httpServletRequest, assignee);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdmsid}/upsertdigitaltwininstruments")
    public void upsertDigitalTwin(@PathVariable String username, @PathVariable String vdmsid, @RequestBody TagDeviceOrLocationDTO filterObject, HttpServletRequest httpServletRequest) {
        deviceService.upsertDigitalTwin(username, vdmsid, filterObject, httpServletRequest);
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/user/{username}/vdms/{vdmsid}/device_id/{device_id}/deletedigitaltwin")
    public void deleteDigitalTwin(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String device_id, HttpServletRequest httpServletRequest) {
        deviceService.deleteDigitalTwin(username, vdmsid, device_id, httpServletRequest);
    }


    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdmsid}/multieditdigitaltwininstruments")
    public void multiEditDigitalTwin(@PathVariable String username, @PathVariable String vdmsid, @RequestParam(required = true) String data,
                                     @RequestParam(required = false) String image_url,
                                     @RequestParam(required = false) MultipartFile image, HttpServletRequest httpServletRequest) throws JsonProcessingException {
        deviceService.multiEditDigitalTwin(username, vdmsid, data, image_url, image, httpServletRequest);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdmsid}/docker/{dockername}/exportfilteredmeasuringinstrument")
    public void exportFilteredMeasuringInstrument(HttpServletResponse response, @PathVariable String username, @PathVariable String vdmsid,
                                                  @PathVariable String dockername, @RequestParam(defaultValue = "all") String condition,
                                                  @RequestParam(defaultValue = "1") Integer pageno,
                                                  @RequestParam(defaultValue = "10") Integer pagesize,
                                                  @RequestParam(defaultValue = "123") Integer onboard_status,
                                                  @RequestBody com.alibaba.fastjson.JSONObject search_sort_filter_details) throws IOException {
        deviceService.exportFilteredMeasuringInstrument(response, username, vdmsid, dockername, condition, pageno, pagesize, search_sort_filter_details, onboard_status);
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/vdms/updatedevicetype")
    public ResponseDTO updateDeviceTypes(@RequestParam(required = false) String vdmsId) {
        return deviceService.updateDeviceTypeForAll(vdmsId);
    }

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

    @RequestMapping(method = RequestMethod.POST, value = "/deviceid/{id}/timetamp/{timetamp}/updatedndstatus")
    public void updatedndstatus(@PathVariable String id, @PathVariable BigInteger timetamp) {
        deviceService.UpdateDeviceDndEnabledAndTimestamp(id, timetamp);
    }


    @RequestMapping(method = RequestMethod.GET, value = "/user/{username}/vdms/{vdmsid}/docker/{docker_name}/getalldevicedetails")
    public List<DeviceDTO> getAllDeviceCustomDetails(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String docker_name,
                                                     @RequestParam(defaultValue = "1") Integer page_no, @RequestParam(defaultValue = "10") Integer page_size,
                                                     @RequestParam(defaultValue = "null") String search_key, @RequestParam(defaultValue = "internal") String profile_type) throws IOException {
        return deviceService.getAllDeviceCustomDetails(username, vdmsid, docker_name, page_no, page_size, search_key, profile_type);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/docker/{docker_name}/getdevicedetailsbyids")
    public List<DeviceDTO> getDeviceCustomDetails(@PathVariable String docker_name,
                                                  @RequestParam(defaultValue = "1") Integer page_no, @RequestParam(defaultValue = "10") Integer page_size,
                                                  @RequestParam(defaultValue = "null") String search_key,
                                                  @RequestBody List<String> device_ids) throws IOException {
        return deviceService.getDeviceCustomDetails(docker_name, page_no, page_size, search_key, device_ids);
    }

    // This is not currently being used, was written when there was a filter page with asset_category and model in the edit profile section
    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdmsid}/docker/{docker_name}/getdevicecustomdetailsbyids")
    public List<DeviceDTO> getDeviceCustomDetailsByIds(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String docker_name,
                                                       @RequestParam(defaultValue = "1") Integer page_no, @RequestParam(defaultValue = "10") Integer page_size,
                                                       @RequestParam(defaultValue = "null") String search_key, @RequestParam(defaultValue = "0") Integer has_pagination,
                                                       @RequestBody JSONObject requestBody) throws IOException {
        return deviceService.getDeviceCustomDetailsByIds(username, vdmsid, docker_name, has_pagination, page_no, page_size, search_key, requestBody);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/docker/{docker_name}/getalldeviceids")
    public List<String> getAllDeviceIds(@PathVariable String docker_name,
                                        @RequestParam(defaultValue = "1") Integer page_no, @RequestParam(defaultValue = "10") Integer page_size,
                                        @RequestParam(defaultValue = "null") String search_key,
                                        @RequestParam(defaultValue = "false") String is_select_all){
        return deviceService.getAllDeviceIds(docker_name, page_no, page_size, search_key, is_select_all);
    }

}


