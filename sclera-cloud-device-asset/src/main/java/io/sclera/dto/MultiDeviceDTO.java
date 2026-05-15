package io.sclera.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class MultiDeviceDTO {

	private String id;
	private String user_data_name;
	private String user_data_model;
	private String user_data_vendor;
	private String type;
	private String network_layer;
	private String location_id;
	private BuildingDTO building_object;		
	private String parent;
	private PhonebookAddressDto global_vendor;		
	private PhonebookAddressDto local_vendor;		
	private PhonebookAddressDto other_vendor_1;		
	private PhonebookAddressDto other_vendor_2;		
	private PhonebookAddressDto other_vendor_3;	
	public String global_vendor_id;		
	public String local_vendor_id;		
	public String other_vendor_1_id;		
	public String other_vendor_2_id;		
	public String other_vendor_3_id;	
	private String warranty;
	private Integer monitor;
	private Integer remote_access;
	private String product_id;		
	private String vdms_id;
	private String docker_name;
	
	private Integer email_alert;
	private Integer sms_alert;
	private Integer popup_notification;
	private String serial_number;
	private Integer local_vendor_email_alert;
	private Integer local_vendor_sms_alert;
	
	private String subsystem_parent_id;
	private String previous_subsystem_parent_id;
	private String description;
	private Integer asset_match_status;
	
	private String custom_fields;

	private String asset_group;

	private String category;

	private String sub_category;

	private DeviceOnboardStatusDTO onboard_data;

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getUser_data_name() {
		return user_data_name;
	}
	public void setUser_data_name(String user_data_name) {
		this.user_data_name = user_data_name;
	}
	public String getUser_data_model() {
		return user_data_model;
	}
	public void setUser_data_model(String user_data_model) {
		this.user_data_model = user_data_model;
	}
	public String getUser_data_vendor() {
		return user_data_vendor;
	}
	public void setUser_data_vendor(String user_data_vendor) {
		this.user_data_vendor = user_data_vendor;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getNetwork_layer() {
		return network_layer;
	}
	public void setNetwork_layer(String network_layer) {
		this.network_layer = network_layer;
	}
	public String getLocation_id() {
		return location_id;
	}
	public void setLocation_id(String location_id) {
		this.location_id = location_id;
	}
	public BuildingDTO getBuilding_object() {
		return building_object;
	}
	public void setBuilding_object(BuildingDTO building_object) {
		this.building_object = building_object;
	}
	public String getParent() {
		return parent;
	}
	public void setParent(String parent) {
		this.parent = parent;
	}
	public PhonebookAddressDto getGlobal_vendor() {
		return global_vendor;
	}
	public void setGlobal_vendor(PhonebookAddressDto global_vendor) {
		this.global_vendor = global_vendor;
	}
	public PhonebookAddressDto getLocal_vendor() {
		return local_vendor;
	}
	public void setLocal_vendor(PhonebookAddressDto local_vendor) {
		this.local_vendor = local_vendor;
	}
	public PhonebookAddressDto getOther_vendor_1() {
		return other_vendor_1;
	}
	public void setOther_vendor_1(PhonebookAddressDto other_vendor_1) {
		this.other_vendor_1 = other_vendor_1;
	}
	public PhonebookAddressDto getOther_vendor_2() {
		return other_vendor_2;
	}
	public void setOther_vendor_2(PhonebookAddressDto other_vendor_2) {
		this.other_vendor_2 = other_vendor_2;
	}
	public PhonebookAddressDto getOther_vendor_3() {
		return other_vendor_3;
	}
	public void setOther_vendor_3(PhonebookAddressDto other_vendor_3) {
		this.other_vendor_3 = other_vendor_3;
	}
	public String getGlobal_vendor_id() {
		return global_vendor_id;
	}
	public void setGlobal_vendor_id(String global_vendor_id) {
		this.global_vendor_id = global_vendor_id;
	}
	public String getLocal_vendor_id() {
		return local_vendor_id;
	}
	public void setLocal_vendor_id(String local_vendor_id) {
		this.local_vendor_id = local_vendor_id;
	}
	public String getOther_vendor_1_id() {
		return other_vendor_1_id;
	}
	public void setOther_vendor_1_id(String other_vendor_1_id) {
		this.other_vendor_1_id = other_vendor_1_id;
	}
	public String getOther_vendor_2_id() {
		return other_vendor_2_id;
	}
	public void setOther_vendor_2_id(String other_vendor_2_id) {
		this.other_vendor_2_id = other_vendor_2_id;
	}
	public String getOther_vendor_3_id() {
		return other_vendor_3_id;
	}
	public void setOther_vendor_3_id(String other_vendor_3_id) {
		this.other_vendor_3_id = other_vendor_3_id;
	}
	public String getWarranty() {
		return warranty;
	}
	public void setWarranty(String warranty) {
		this.warranty = warranty;
	}
	public Integer getMonitor() {
		return monitor;
	}
	public void setMonitor(Integer monitor) {
		this.monitor = monitor;
	}
	public Integer getRemote_access() {
		return remote_access;
	}
	public void setRemote_access(Integer remote_access) {
		this.remote_access = remote_access;
	}
	public String getProduct_id() {
		return product_id;
	}
	public void setProduct_id(String product_id) {
		this.product_id = product_id;
	}
	public String getVdms_id() {
		return vdms_id;
	}
	public void setVdms_id(String vdms_id) {
		this.vdms_id = vdms_id;
	}
	public String getDocker_name() {
		return docker_name;
	}
	public void setDocker_name(String docker_name) {
		this.docker_name = docker_name;
	}
	
	public Integer getEmail_alert() {
		return email_alert;
	}
	public void setEmail_alert(Integer email_alert) {
		this.email_alert = email_alert;
	}
	public Integer getSms_alert() {
		return sms_alert;
	}
	public void setSms_alert(Integer sms_alert) {
		this.sms_alert = sms_alert;
	}
	
	public Integer getPopup_notification() {
		return popup_notification;
	}
	public void setPopup_notification(Integer popup_notification) {
		this.popup_notification = popup_notification;
	}
	
	public String getSerial_number() {
		return serial_number;
	}
	public void setSerial_number(String serial_number) {
		this.serial_number = serial_number;
	}
	
	public Integer getLocal_vendor_email_alert() {
		return local_vendor_email_alert;
	}
	public void setLocal_vendor_email_alert(Integer local_vendor_email_alert) {
		this.local_vendor_email_alert = local_vendor_email_alert;
	}
	public Integer getLocal_vendor_sms_alert() {
		return local_vendor_sms_alert;
	}
	public void setLocal_vendor_sms_alert(Integer local_vendor_sms_alert) {
		this.local_vendor_sms_alert = local_vendor_sms_alert;
	}

	public String getSubsystem_parent_id() {
		return subsystem_parent_id;
	}
	public void setSubsystem_parent_id(String subsystem_parent_id) {
		this.subsystem_parent_id = subsystem_parent_id;
	}
	public String getPrevious_subsystem_parent_id() {
		return previous_subsystem_parent_id;
	}
	public void setPrevious_subsystem_parent_id(String previous_subsystem_parent_id) {
		this.previous_subsystem_parent_id = previous_subsystem_parent_id;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Integer getAsset_match_status() {
		return asset_match_status;
	}
	public void setAsset_match_status(Integer asset_match_status) {
		this.asset_match_status = asset_match_status;
	}
	public String getCustom_fields() {
		return custom_fields;
	}
	public void setCustom_fields(String custom_fields) {
		this.custom_fields = custom_fields;
	}

	public String getAsset_group() {
		return asset_group;
	}

	public void setAsset_group(String asset_group) {
		this.asset_group = asset_group;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getSub_category() {
		return sub_category;
	}

	public void setSub_category(String sub_category) {
		this.sub_category = sub_category;
	}

	public DeviceOnboardStatusDTO getOnboard_data() {
		return onboard_data;
	}

	public void setOnboard_data(DeviceOnboardStatusDTO onboard_data) {
		this.onboard_data = onboard_data;
	}

	@Override
	public String toString() {
		return "MultiDeviceDTO [id=" + id + ", user_data_name=" + user_data_name + ", user_data_model="
				+ user_data_model + ", user_data_vendor=" + user_data_vendor + ", type=" + type + ", network_layer="
				+ network_layer + ", location_id=" + location_id + ", building_object=" + building_object + ", parent="
				+ parent + ", global_vendor=" + global_vendor + ", local_vendor=" + local_vendor + ", other_vendor_1="
				+ other_vendor_1 + ", other_vendor_2=" + other_vendor_2 + ", other_vendor_3=" + other_vendor_3
				+ ", global_vendor_id=" + global_vendor_id + ", local_vendor_id=" + local_vendor_id
				+ ", other_vendor_1_id=" + other_vendor_1_id + ", other_vendor_2_id=" + other_vendor_2_id
				+ ", other_vendor_3_id=" + other_vendor_3_id + ", warranty=" + warranty + ", monitor=" + monitor
				+ ", remote_access=" + remote_access + ", product_id=" + product_id + ", vdms_id=" + vdms_id
				+ ", docker_name=" + docker_name + ", email_alert=" + email_alert + ", sms_alert=" + sms_alert
				+ ", popup_notification=" + popup_notification + ", serial_number=" + serial_number
				+ ", local_vendor_email_alert=" + local_vendor_email_alert + ", local_vendor_sms_alert="
				+ local_vendor_sms_alert + ", subsystem_parent_id=" + subsystem_parent_id
				+ ", previous_subsystem_parent_id=" + previous_subsystem_parent_id + ", description=" + description
				+ ", asset_match_status=" + asset_match_status + ", custom_fields=" + custom_fields + "]";
	}
	
}
