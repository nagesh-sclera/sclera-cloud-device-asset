package io.sclera.dto.touchscreen;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)

public class DeviceListDTO {

	private String id;
	private String display_name;
	private String user_data_name;
	private String location;
	private String floor;
	private String building;
	private Integer status;
	private String model;
	private String last_seen_on;
	private String user_data_model;
	private String alarm;
	private String image_url_1;
	private String ip_address;
	private String mac_address;
	private String vendor;
	private String user_data_vendor;
	private String docker_name;
	private String account_number;
	private String company_name;
	private String email;
	private String phone;
	private String phone_type;
	private String value;
	private String vendor_name;
	
	private Integer virtual_device_type;
	private String snmp_status;
	private String bacnet_status;
	private String lorawan_status;
	private String disruptive_status;
	private String my_devices_status;
	private String monnit_status;
	private String pelican_status;
	private String knx_status;
	private String snmp_object_status;
	private String measuring_instrument_status;
	private String record_checklist_status;
	private String daintree_status;
	private String ecobee_status;
	private String modbus_status;
	private String record_checklist_result;
	private String type;
	private String sub_category;
	private String category;

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getDisplay_name() {
		return display_name;
	}
	public void setDisplay_name(String display_name) {
		this.display_name = display_name;
	}
	public String getUser_data_name() {
		return user_data_name;
	}
	public void setUser_data_name(String user_data_name) {
		this.user_data_name = user_data_name;
	}
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public String getFloor() {
		return floor;
	}
	public void setFloor(String floor) {
		this.floor = floor;
	}
	public String getBuilding() {
		return building;
	}
	public void setBuilding(String building) {
		this.building = building;
	}
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	
	public String getModel() {
		return model;
	}
	public void setModel(String model) {
		this.model = model;
	}
	public String getLast_seen_on() {
		return last_seen_on;
	}
	public void setLast_seen_on(String last_seen_on) {
		this.last_seen_on = last_seen_on;
	}
	public String getImage_url_1() {
		return image_url_1;
	}
	public void setImage_url_1(String image_url_1) {
		this.image_url_1 = image_url_1;
	}
	public String getAccount_number() {
		return account_number;
	}
	public void setAccount_number(String account_number) {
		this.account_number = account_number;
	}
	public String getCompany_name() {
		return company_name;
	}
	public void setCompany_name(String company_name) {
		this.company_name = company_name;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public String getPhone_type() {
		return phone_type;
	}
	public void setPhone_type(String phone_type) {
		this.phone_type = phone_type;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public String getVendor_name() {
		return vendor_name;
	}
	public void setVendor_name(String vendor_name) {
		this.vendor_name = vendor_name;
	}
	public String getAlarm() {
		return alarm;
	}
	public void setAlarm(String alarm) {
		this.alarm = alarm;
	}
	public String getUser_data_model() {
		return user_data_model;
	}
	public void setUser_data_model(String user_data_model) {
		this.user_data_model = user_data_model;
	}
	
	public String getIp_address() {
		return ip_address;
	}
	public void setIp_address(String ip_address) {
		this.ip_address = ip_address;
	}
	public String getMac_address() {
		return mac_address;
	}
	public void setMac_address(String mac_address) {
		this.mac_address = mac_address;
	}
	public String getVendor() {
		return vendor;
	}
	public void setVendor(String vendor) {
		this.vendor = vendor;
	}
	public String getUser_data_vendor() {
		return user_data_vendor;
	}
	public void setUser_data_vendor(String user_data_vendor) {
		this.user_data_vendor = user_data_vendor;
	}
	public String getDocker_name() {
		return docker_name;
	}
	public void setDocker_name(String docker_name) {
		this.docker_name = docker_name;
	}
	public Integer getVirtual_device_type() {
		return virtual_device_type;
	}
	public void setVirtual_device_type(Integer virtual_device_type) {
		this.virtual_device_type = virtual_device_type;
	}
	public String getSnmp_status() {
		return snmp_status;
	}
	public void setSnmp_status(String snmp_status) {
		this.snmp_status = snmp_status;
	}
	public String getBacnet_status() {
		return bacnet_status;
	}
	public void setBacnet_status(String bacnet_status) {
		this.bacnet_status = bacnet_status;
	}
	public String getLorawan_status() {
		return lorawan_status;
	}
	public void setLorawan_status(String lorawan_status) {
		this.lorawan_status = lorawan_status;
	}
	public String getDisruptive_status() {
		return disruptive_status;
	}
	public void setDisruptive_status(String disruptive_status) {
		this.disruptive_status = disruptive_status;
	}
	public String getMy_devices_status() {
		return my_devices_status;
	}
	public void setMy_devices_status(String my_devices_status) {
		this.my_devices_status = my_devices_status;
	}
	public String getMonnit_status() {
		return monnit_status;
	}
	public void setMonnit_status(String monnit_status) {
		this.monnit_status = monnit_status;
	}
	public String getPelican_status() {
		return pelican_status;
	}
	public void setPelican_status(String pelican_status) {
		this.pelican_status = pelican_status;
	}
	public String getKnx_status() {
		return knx_status;
	}
	public void setKnx_status(String knx_status) {
		this.knx_status = knx_status;
	}
	public String getSnmp_object_status() {
		return snmp_object_status;
	}

	public String getMeasuring_instrument_status() {
		return measuring_instrument_status;
	}

	public void setMeasuring_instrument_status(String measuring_instrument_status) {
		this.measuring_instrument_status = measuring_instrument_status;
	}

	public void setSnmp_object_status(String snmp_object_status) {
		this.snmp_object_status = snmp_object_status;
	}

	public String getRecord_checklist_status() {
		return record_checklist_status;
	}

	public void setRecord_checklist_status(String record_checklist_status) {
		this.record_checklist_status = record_checklist_status;
	}

	public String getDaintree_status() {
		return daintree_status;
	}

	public void setDaintree_status(String daintree_status) {
		this.daintree_status = daintree_status;
	}
	public String getEcobee_status() {
		return ecobee_status;
	}

	public void setEcobee_status(String ecobee_status) {
		this.ecobee_status = ecobee_status;
	}


	public String getModbus_status() {
		return modbus_status;
	}

	public void setModbus_status(String modbus_status) {
		this.modbus_status = modbus_status;
	}

	public String getRecord_checklist_result() {
		return record_checklist_result;
	}

	public void setRecord_checklist_result(String record_checklist_result) {
		this.record_checklist_result = record_checklist_result;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getSub_category() {
		return sub_category;
	}

	public void setSub_category(String sub_category) {
		this.sub_category = sub_category;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public DeviceListDTO() {
		super();
		// TODO Auto-generated constructor stub
	}

	public DeviceListDTO(String id, String display_name, String user_data_name, String location, String floor,
						 String building, Integer status, String model, String last_seen_on , String image_url_1, Integer virtual_device_type,
						 String snmp_status, String bacnet_status, String lorawan_status, String disruptive_status, String my_devices_status,
						 String monnit_status, String pelican_status,String knx_status, String snmp_object_status, String measuring_instrument_status,
						 String record_checklist_status, String daintree_status, String ecobee_status, String modbus_status,
						 String type, String sub_category, String category) {
		super();
		this.id = id;
		this.display_name = display_name;
		this.user_data_name = user_data_name;
		this.location = location;
		this.floor = floor;
		this.building = building;
		this.status = status;
		this.model = model;
		this.last_seen_on = last_seen_on;
		this.image_url_1 = image_url_1;
		this.virtual_device_type = virtual_device_type;
		this.snmp_status = snmp_status;
		this.bacnet_status = bacnet_status;
		this.lorawan_status = lorawan_status;
		this.disruptive_status = disruptive_status;
		this.my_devices_status = my_devices_status;
		this.monnit_status = monnit_status;
		this.pelican_status = pelican_status;
		this.knx_status = knx_status;
		this.snmp_object_status = snmp_object_status;
		this.measuring_instrument_status = measuring_instrument_status;
		this.record_checklist_status = record_checklist_status;
		this.daintree_status = daintree_status;
		this.ecobee_status = ecobee_status;
		this.modbus_status =  modbus_status;
		this.type = type;
		this.sub_category = sub_category;
		this.category = category;
	}

	public DeviceListDTO(String id, String display_name, String user_data_name, String location, String floor,
			String building, Integer status, String model, String last_seen_on, String user_data_model, String alarm,
			String image_url_1, String ip_address, String mac_address, String vendor, String user_data_vendor, String docker_name,
			String account_number, String company_name, String email, String phone, String phone_type, String value,
			String vendor_name) {
		super();
		this.id = id;
		this.display_name = display_name;
		this.user_data_name = user_data_name;
		this.location = location;
		this.floor = floor;
		this.building = building;
		this.status = status;
		this.model = model;
		this.last_seen_on = last_seen_on;
		this.user_data_model = user_data_model;
		this.alarm = alarm;
		this.image_url_1 = image_url_1;
		this.ip_address = ip_address;
		this.mac_address = mac_address;
		this.vendor = vendor;
		this.user_data_vendor = user_data_vendor;
		this.docker_name = docker_name;
		this.account_number = account_number;
		this.company_name = company_name;
		this.email = email;
		this.phone = phone;
		this.phone_type = phone_type;
		this.value = value;
		this.vendor_name = vendor_name;
	}
	
	
	
	
	
}
