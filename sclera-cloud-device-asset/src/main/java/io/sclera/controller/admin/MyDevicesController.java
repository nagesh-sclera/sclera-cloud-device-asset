package io.sclera.controller.admin;

import java.util.List;
import java.util.Set;

import io.sclera.dto.MyDevicesSensorAttributesDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.sclera.dto.LorawanSensorDTO;
import io.sclera.dto.MyDevicesCompanyDTO;
import io.sclera.dto.MyDevicesSensorDTO;
import io.sclera.models.MyDevicesCompany;
import io.sclera.service.MyDevicesService;

import com.alibaba.fastjson.JSONObject;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")

public class MyDevicesController {
	
	@Autowired
	MyDevicesService myDevicesService;
	
	@RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdmsid}/upsertmydevicescompany")
	public void upsertMyDevicesCompany(@PathVariable String username, @PathVariable String vdmsid, @RequestBody MyDevicesCompanyDTO myDevicesCompany) {
		
		myDevicesService.upsertMyDevicesCompany(username, vdmsid, myDevicesCompany);
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/mydevicesevent")
	public void updateMyDevicesEventData(@RequestBody JSONObject myDevicesEventData) {
		
		myDevicesService.updateMyDevicesEventData(myDevicesEventData);
	}
	
	//to be removed after pagination api works
	@RequestMapping(method = RequestMethod.GET, value = "/user/{username}/vdms/{vdmsid}/getallmydevicescompanies")
	public List<MyDevicesCompanyDTO> getAllMyDevicesCompanies(@PathVariable String username, @PathVariable String vdmsid) {
		
		return myDevicesService.getAllMyDevicesCompanies(username, vdmsid);
	}

	//Added pagination for getAllMyDevicesCompanies
	@RequestMapping(method = RequestMethod.GET, value = "/user/{username}/vdms/{vdmsid}/getmydevicescompaniesbypagination")
	public Set<MyDevicesCompanyDTO> getMyDevicesCompaniesPagination(@PathVariable String username, @PathVariable String vdmsid,
			@RequestParam(defaultValue = "null") String searchkey, @RequestParam(defaultValue = "1") Integer pageno,
			@RequestParam(defaultValue = "10") Integer pagesize) {
		return myDevicesService.getMyDevicesCompaniesPagination(username, vdmsid, searchkey, pageno, pagesize);
	}
	
	@RequestMapping(method = RequestMethod.DELETE, value = "/user/{username}/vdms/{vdmsid}/mydevicescompany/{my_devices_company_id}/deletemydevicescompany")
	public void deleteMyDevicesCompany(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String my_devices_company_id)
	{
		myDevicesService.deleteMyDevicesCompany(username, vdmsid, my_devices_company_id);
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/user/{username}/vdms/{vdmsid}/mydevicessensor/{my_devices_sensor_id}/getmydevicessensor")
	public MyDevicesSensorDTO getMyDevicesSensor(@PathVariable String username, @PathVariable String vdmsid, 
			@PathVariable String my_devices_sensor_id) {
		return myDevicesService.getMyDevicesSensor(username, vdmsid, my_devices_sensor_id);
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdmsid}//updatemydevicessensors")
	public void updateMyDevicesSensors(@PathVariable String username, @PathVariable String vdmsid, 
			@RequestBody List<MyDevicesSensorDTO> myDevicesSensors) {
		 myDevicesService.updateMyDevicesSensors(username, vdmsid, myDevicesSensors);
	}
	
	@RequestMapping(method = RequestMethod.DELETE, value = "/user/{username}/vdms/{vdmsid}//deletemydevicessensors")
	public void deleteMyDevicesSensors(@PathVariable String username, @PathVariable String vdmsid, 
			@RequestBody List<MyDevicesSensorDTO> myDevicesSensors) {
		 myDevicesService.deleteMyDevicesSensors(username, vdmsid, myDevicesSensors);
	}
	
	//to get sensor info tagged to a device
	@RequestMapping(method = RequestMethod.GET, value = "/user/{username}/vdms/{vdmsid}/device/{device_id}/getdevicemydevicessensors")
	public Set<MyDevicesSensorDTO> getDeviceMyDevicesSensors(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String device_id) {
		return myDevicesService.getDeviceMyDevicesSensors(username, vdmsid, device_id);
	}

	//update sensor tagged to a device
	@RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdmsid}/updatedevicemydevicessensors")
	public void updateDeviceMyDevicesSensors(@PathVariable String username, @PathVariable String vdmsid, 
			@RequestBody List<MyDevicesSensorDTO> myDevicesSensors) {
		myDevicesService.updateDeviceMyDevicesSensors(username, vdmsid, myDevicesSensors);
	}

	//delete sensor tagged to a device
	@RequestMapping(method = RequestMethod.DELETE, value = "/user/{username}/vdms/{vdmsid}/deletedevicemydevicessensors")
	public void deleteDeviceMyDevicesSensors(@PathVariable String username, @PathVariable String vdmsid, 
			@RequestBody List<MyDevicesSensorDTO> myDevicesSensors) {
		myDevicesService.deleteDeviceMyDevicesSensors(username, vdmsid, myDevicesSensors);
	}

	//to be removed after pagination api works
	//get list of all sensors in the vdms
	@RequestMapping(method = RequestMethod.GET, value = "/user/{username}/vdms/{vdmsid}/getallmydevicessensors")
	public List<MyDevicesSensorDTO> getAllMyDevicesSensors(@PathVariable String username, @PathVariable String vdmsid) {
		return myDevicesService.getAllMyDevicesSensors(username, vdmsid);
	}
	
	//Added pagination for getAllMyDevicesSensors
	@RequestMapping(method = RequestMethod.GET, value = "/user/{username}/vdms/{vdmsid}/getallmydevicessensorsbypagination")
	public List<MyDevicesSensorDTO> getAllMyDevicesSensorsByPagination(@PathVariable String username, @PathVariable String vdmsid,
			@RequestParam(defaultValue = "null") String searchkey,@RequestParam(defaultValue = "1") Integer pageno, 
			@RequestParam(defaultValue = "10") Integer pagesize) {
		return myDevicesService.getAllMyDevicesSensorsByPagination(username, vdmsid, searchkey, pageno, pagesize);
	}
	
	//Get Mydevices sensor by configuration id pagination
	@RequestMapping(method = RequestMethod.GET, value = "/user/{username}/vdms/{vdmsid}/company/{company_id}/getmydevicessesnorsbypagination")
	public Set<MyDevicesSensorDTO> getMyDevicesSensorsByPagination(@PathVariable String username, @PathVariable String vdmsid, 
			@PathVariable String company_id, @RequestParam(defaultValue = "null") String searchkey,@RequestParam(defaultValue = "1") Integer pageno, 
			@RequestParam(defaultValue = "10") Integer pagesize) {
		return myDevicesService.getMyDevicesSensorsByPagination(username, vdmsid, company_id, searchkey, pageno, pagesize);
	}

	@RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdmsid}/updatemydevicessensorattributes")
	public void updateMyDevicesSensorAttributes(@PathVariable String username, @PathVariable String vdmsid,
												@RequestBody List<MyDevicesSensorAttributesDTO> myDevicesSensorAttributes) {
		myDevicesService.updateMyDevicesSensorAttributes(username, vdmsid, myDevicesSensorAttributes);
	}

}
