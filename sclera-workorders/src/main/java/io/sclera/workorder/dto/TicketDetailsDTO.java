package io.sclera.workorder.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigInteger;

/** Detailed read projection of a native ticket (full field set returned by the detail lookup). */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TicketDetailsDTO {
	
	private String id;
	private String number;
	private String status;
	private BigInteger timestamp;
	private String user_message;
	private String vendor_message;
	private String type;
	private String response;
	private String vendor_email;
	
	//Device Info
	private String device_id;
	private String device_name;
	private String device_image_url_1;
	private Integer device_monitor;
	private Integer device_popup_notification;
	private Integer device_email_alert;
	private Integer device_sms_alert;
	private String device_product_id;
	private String docker_name;
	private String docker_system_type;
	private String vdms_id;
	private String vendor_org_id;
	private String customer_org_id;
	
	private String device_model;
	private String device_vendor;
	private String device_type;
	private Integer device_virtual_device_type;
	private String device_warranty;
	private Integer device_status;
	private BigInteger device_last_seen_on;
	private Boolean device_sensor_alert;
	private Integer device_local_vendor_email_alert;
	private Integer device_local_vendor_sms_alert;
	private String device_global_image_url_1;
	private String location;
	private String location_id;
	private String floor;
	private String floor_id;
	private String building;
	private String building_id;
	
	private String device_local_vendor_name;
	private String device_local_vendor_email;
	private String device_local_vendor_extension;
	private String device_local_vendor_phone;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getNumber() {
		return number;
	}
	public void setNumber(String number) {
		this.number = number;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public BigInteger getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(BigInteger timestamp) {
		this.timestamp = timestamp;
	}
	public String getUser_message() {
		return user_message;
	}
	public void setUser_message(String user_message) {
		this.user_message = user_message;
	}
	public String getVendor_message() {
		return vendor_message;
	}
	public void setVendor_message(String vendor_message) {
		this.vendor_message = vendor_message;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getResponse() {
		return response;
	}
	public void setResponse(String response) {
		this.response = response;
	}
	public String getVendor_email() {
		return vendor_email;
	}
	public void setVendor_email(String vendor_email) {
		this.vendor_email = vendor_email;
	}
	public String getDevice_id() {
		return device_id;
	}
	public void setDevice_id(String device_id) {
		this.device_id = device_id;
	}
	public String getDevice_name() {
		return device_name;
	}
	public void setDevice_name(String device_name) {
		this.device_name = device_name;
	}
	public String getDevice_image_url_1() {
		return device_image_url_1;
	}
	public void setDevice_image_url_1(String device_image_url_1) {
		this.device_image_url_1 = device_image_url_1;
	}
	public Integer getDevice_monitor() {
		return device_monitor;
	}
	public void setDevice_monitor(Integer device_monitor) {
		this.device_monitor = device_monitor;
	}
	public Integer getDevice_popup_notification() {
		return device_popup_notification;
	}
	public void setDevice_popup_notification(Integer device_popup_notification) {
		this.device_popup_notification = device_popup_notification;
	}
	public Integer getDevice_email_alert() {
		return device_email_alert;
	}
	public void setDevice_email_alert(Integer device_email_alert) {
		this.device_email_alert = device_email_alert;
	}
	public Integer getDevice_sms_alert() {
		return device_sms_alert;
	}
	public void setDevice_sms_alert(Integer device_sms_alert) {
		this.device_sms_alert = device_sms_alert;
	}
	public String getDevice_product_id() {
		return device_product_id;
	}
	public void setDevice_product_id(String device_product_id) {
		this.device_product_id = device_product_id;
	}
	public String getDocker_name() {
		return docker_name;
	}
	public void setDocker_name(String docker_name) {
		this.docker_name = docker_name;
	}
	public String getDocker_system_type() {
		return docker_system_type;
	}
	public void setDocker_system_type(String docker_system_type) {
		this.docker_system_type = docker_system_type;
	}
	public String getVdms_id() {
		return vdms_id;
	}
	public void setVdms_id(String vdms_id) {
		this.vdms_id = vdms_id;
	}
	public String getVendor_org_id() {
		return vendor_org_id;
	}
	public void setVendor_org_id(String vendor_org_id) {
		this.vendor_org_id = vendor_org_id;
	}
	public String getCustomer_org_id() {
		return customer_org_id;
	}
	public void setCustomer_org_id(String customer_org_id) {
		this.customer_org_id = customer_org_id;
	}
	public String getDevice_model() {
		return device_model;
	}
	public void setDevice_model(String device_model) {
		this.device_model = device_model;
	}
	public String getDevice_vendor() {
		return device_vendor;
	}
	public void setDevice_vendor(String device_vendor) {
		this.device_vendor = device_vendor;
	}
	public String getDevice_type() {
		return device_type;
	}
	public void setDevice_type(String device_type) {
		this.device_type = device_type;
	}
	public Integer getDevice_virtual_device_type() {
		return device_virtual_device_type;
	}
	public void setDevice_virtual_device_type(Integer device_virtual_device_type) {
		this.device_virtual_device_type = device_virtual_device_type;
	}
	public String getDevice_warranty() {
		return device_warranty;
	}
	public void setDevice_warranty(String device_warranty) {
		this.device_warranty = device_warranty;
	}
	public Integer getDevice_status() {
		return device_status;
	}
	public void setDevice_status(Integer device_status) {
		this.device_status = device_status;
	}
	public BigInteger getDevice_last_seen_on() {
		return device_last_seen_on;
	}
	public void setDevice_last_seen_on(BigInteger device_last_seen_on) {
		this.device_last_seen_on = device_last_seen_on;
	}
	public Boolean getDevice_sensor_alert() {
		return device_sensor_alert;
	}
	public void setDevice_sensor_alert(Boolean device_sensor_alert) {
		this.device_sensor_alert = device_sensor_alert;
	}
	public Integer getDevice_local_vendor_email_alert() {
		return device_local_vendor_email_alert;
	}
	public void setDevice_local_vendor_email_alert(Integer device_local_vendor_email_alert) {
		this.device_local_vendor_email_alert = device_local_vendor_email_alert;
	}
	public Integer getDevice_local_vendor_sms_alert() {
		return device_local_vendor_sms_alert;
	}
	public void setDevice_local_vendor_sms_alert(Integer device_local_vendor_sms_alert) {
		this.device_local_vendor_sms_alert = device_local_vendor_sms_alert;
	}
	public String getDevice_global_image_url_1() {
		return device_global_image_url_1;
	}
	public void setDevice_global_image_url_1(String device_global_image_url_1) {
		this.device_global_image_url_1 = device_global_image_url_1;
	}
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public String getLocation_id() {
		return location_id;
	}
	public void setLocation_id(String location_id) {
		this.location_id = location_id;
	}
	public String getFloor() {
		return floor;
	}
	public void setFloor(String floor) {
		this.floor = floor;
	}
	public String getFloor_id() {
		return floor_id;
	}
	public void setFloor_id(String floor_id) {
		this.floor_id = floor_id;
	}
	public String getBuilding() {
		return building;
	}
	public void setBuilding(String building) {
		this.building = building;
	}
	public String getBuilding_id() {
		return building_id;
	}
	public void setBuilding_id(String building_id) {
		this.building_id = building_id;
	}
	public String getDevice_local_vendor_name() {
		return device_local_vendor_name;
	}
	public void setDevice_local_vendor_name(String device_local_vendor_name) {
		this.device_local_vendor_name = device_local_vendor_name;
	}
	public String getDevice_local_vendor_email() {
		return device_local_vendor_email;
	}
	public void setDevice_local_vendor_email(String device_local_vendor_email) {
		this.device_local_vendor_email = device_local_vendor_email;
	}
	public String getDevice_local_vendor_extension() {
		return device_local_vendor_extension;
	}
	public void setDevice_local_vendor_extension(String device_local_vendor_extension) {
		this.device_local_vendor_extension = device_local_vendor_extension;
	}
	public String getDevice_local_vendor_phone() {
		return device_local_vendor_phone;
	}
	public void setDevice_local_vendor_phone(String device_local_vendor_phone) {
		this.device_local_vendor_phone = device_local_vendor_phone;
	}
	public TicketDetailsDTO() {
		super();
	}
	public TicketDetailsDTO(String id, String number, String status, BigInteger timestamp, String user_message,
                            String vendor_message, String type, String response, String device_id, String device_name,
                            String device_image_url_1, Integer device_monitor, Integer device_popup_notification,
                            Integer device_email_alert, Integer device_sms_alert, String device_product_id, String docker_name,
                            String docker_system_type, String vdms_id, String vendor_org_id, String customer_org_id,
                            String device_model, String device_vendor, String device_type, Integer device_virtual_device_type,
                            String device_warranty, Integer device_status, BigInteger device_last_seen_on, Boolean device_sensor_alert,
                            Integer device_local_vendor_email_alert, Integer device_local_vendor_sms_alert,
                            String device_global_image_url_1, String location, String location_id, String floor, String floor_id,
                            String building, String building_id, String device_local_vendor_name, String device_local_vendor_email,
                            String device_local_vendor_extension, String device_local_vendor_phone) {
		super();
		this.id = id;
		this.number = number;
		this.status = status;
		this.timestamp = timestamp;
		this.user_message = user_message;
		this.vendor_message = vendor_message;
		this.type = type;
		this.response = response;
		this.device_id = device_id;
		this.device_name = device_name;
		this.device_image_url_1 = device_image_url_1;
		this.device_monitor = device_monitor;
		this.device_popup_notification = device_popup_notification;
		this.device_email_alert = device_email_alert;
		this.device_sms_alert = device_sms_alert;
		this.device_product_id = device_product_id;
		this.docker_name = docker_name;
		this.docker_system_type = docker_system_type;
		this.vdms_id = vdms_id;
		this.vendor_org_id = vendor_org_id;
		this.customer_org_id = customer_org_id;
		this.device_model = device_model;
		this.device_vendor = device_vendor;
		this.device_type = device_type;
		this.device_virtual_device_type = device_virtual_device_type;
		this.device_warranty = device_warranty;
		this.device_status = device_status;
		this.device_last_seen_on = device_last_seen_on;
		this.device_sensor_alert = device_sensor_alert;
		this.device_local_vendor_email_alert = device_local_vendor_email_alert;
		this.device_local_vendor_sms_alert = device_local_vendor_sms_alert;
		this.device_global_image_url_1 = device_global_image_url_1;
		this.location = location;
		this.location_id = location_id;
		this.floor = floor;
		this.floor_id = floor_id;
		this.building = building;
		this.building_id = building_id;
		this.device_local_vendor_name = device_local_vendor_name;
		this.device_local_vendor_email = device_local_vendor_email;
		this.device_local_vendor_extension = device_local_vendor_extension;
		this.device_local_vendor_phone = device_local_vendor_phone;
	}
	
	
}
