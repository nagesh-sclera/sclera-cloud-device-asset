package io.sclera.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class InterfaceDTO {

	private String id;

	private String name;

	private String download;

	private String upload;

	private String status;

	private String mac_address;

	private String category;

	private String type;

	private String ifindex;

	private String connected_mac_address;
	
	private String device_id;
	
	private String device_display_name;
	
	private String device_user_data_name;
	
	private String device_ip_address;
	
	private String device_mac_address;
	
	private List<DeviceDTO> devices;
	
	private  List<String> user_connected_devices;

	private List<String> connected_devices;

	@JsonIgnore
	private String connected_devices_json;
	
	private String user_connected_devices_json;
	
	private String speed;
	
	private Integer uplink;
	
	private String unmanaged_switch_name;
	
	private String unmanaged_switch_type;
	
	private List<String> connected_mac_addresses;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDownload() {
		return download;
	}

	public void setDownload(String download) {
		this.download = download;
	}

	public String getUpload() {
		return upload;
	}

	public void setUpload(String upload) {
		this.upload = upload;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getMac_address() {
		return mac_address;
	}

	public void setMac_address(String mac_address) {
		this.mac_address = mac_address;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	public String getIfindex() {
		return ifindex;
	}

	public void setIfindex(String ifindex) {
		this.ifindex = ifindex;
	}

	public String getConnected_mac_address() {
		return connected_mac_address;
	}

	public void setConnected_mac_address(String connected_mac_address) {
		this.connected_mac_address = connected_mac_address;
	}

	public String getDevice_id() {
		return device_id;
	}

	public void setDevice_id(String device_id) {
		this.device_id = device_id;
	}
	
	public String getDevice_mac_address() {
		return device_mac_address;
	}

	public void setDevice_mac_address(String device_mac_address) {
		this.device_mac_address = device_mac_address;
	}

	public String getDevice_display_name() {
		return device_display_name;
	}

	public void setDevice_display_name(String device_display_name) {
		this.device_display_name = device_display_name;
	}

	public String getDevice_user_data_name() {
		return device_user_data_name;
	}

	public void setDevice_user_data_name(String device_user_data_name) {
		this.device_user_data_name = device_user_data_name;
	}

	public String getDevice_ip_address() {
		return device_ip_address;
	}

	public void setDevice_ip_address(String device_ip_address) {
		this.device_ip_address = device_ip_address;
	}

	public List<DeviceDTO> getDevices() {
		return devices;
	}

	public void setDevices(List<DeviceDTO> devices) {
		this.devices = devices;
	}
	
	public List<String> getUser_connected_devices() {
		return user_connected_devices;
	}

	public void setUser_connected_devices(List<String> user_connected_devices) {
		this.user_connected_devices = user_connected_devices;
	}

	public List<String> getConnected_devices() {
		return connected_devices;
	}

	public void setConnected_devices(List<String> connected_devices) {
		this.connected_devices = connected_devices;
	}

	public String getConnected_devices_json() {
		return connected_devices_json;
	}

	public void setConnected_devices_json(String connected_devices_json) {
		this.connected_devices_json = connected_devices_json;
	}

	public String getSpeed() {
		return speed;
	}

	public void setSpeed(String speed) {
		this.speed = speed;
	}
	
	public String getUser_connected_devices_json() {
		return user_connected_devices_json;
	}

	public void setUser_connected_devices_json(String user_connected_devices_json) {
		this.user_connected_devices_json = user_connected_devices_json;
	}

	public Integer getUplink() {
		return uplink;
	}

	public void setUplink(Integer uplink) {
		this.uplink = uplink;
	}

	public String getUnmanaged_switch_name() {
		return unmanaged_switch_name;
	}

	public void setUnmanaged_switch_name(String unmanaged_switch_name) {
		this.unmanaged_switch_name = unmanaged_switch_name;
	}
	
	public String getUnmanaged_switch_type() {
		return unmanaged_switch_type;
	}

	public void setUnmanaged_switch_type(String unmanaged_switch_type) {
		this.unmanaged_switch_type = unmanaged_switch_type;
	}
	
	public List<String> getConnected_mac_addresses() {
		return connected_mac_addresses;
	}

	public void setConnected_mac_addresses(List<String> connected_mac_addresses) {
		this.connected_mac_addresses = connected_mac_addresses;
	}

	public InterfaceDTO() {
		super();
	}
	public InterfaceDTO(String id, String name, String download, String upload, String status, String mac_address,
			String category, String type, String ifindex, String connected_mac_address) {
		super();
		this.id = id;
		this.name = name;
		this.download = download;
		this.upload = upload;
		this.status = status;
		this.mac_address = mac_address;
		this.category = category;
		this.type = type;
		this.ifindex = ifindex;
		this.connected_mac_address = connected_mac_address;
	}
	
	public InterfaceDTO(String ifindex, String connected_mac_address, String device_id, String device_mac_address) {
		super();
		this.ifindex = ifindex;
		this.connected_mac_address = connected_mac_address;
		this.device_id = device_id;
		this.device_mac_address = device_mac_address;
	}
	
	

	
	//listDeviceSnmpInterfaceByDeviceId
	public InterfaceDTO(String name, String download, String upload, String status, String mac_address, String category,
			String type, String ifindex, String device_id, String device_display_name, String device_user_data_name,
			String device_ip_address, String device_mac_address) {
		super();
		this.name = name;
		this.download = download;
		this.upload = upload;
		this.status = status;
		this.mac_address = mac_address;
		this.category = category;
		this.type = type;
		this.ifindex = ifindex;
		this.device_id = device_id;
		this.device_display_name = device_display_name;
		this.device_user_data_name = device_user_data_name;
		this.device_ip_address = device_ip_address;
		this.device_mac_address = device_mac_address;
	}






	public InterfaceDTO(String id, String name, String download, String upload, String status, String mac_address,
			String category, String type, String ifindex, String connected_devices_json, String speed, Integer uplink, String unmanaged_switch_name, String unmanaged_switch_type) {
		super();
		this.id = id;
		this.name = name;
		this.download = download;
		this.upload = upload;
		this.status = status;
		this.mac_address = mac_address;
		this.category = category;
		this.type = type;
		this.ifindex = ifindex;
		this.connected_devices_json = connected_devices_json;
		this.speed = speed;
		this.uplink = uplink;
		this.unmanaged_switch_name = unmanaged_switch_name;
		this.unmanaged_switch_type = unmanaged_switch_type;
	}

	@Override
	public String toString() {
		return "InterfaceDTO [id=" + id + ", name=" + name + ", download=" + download + ", upload=" + upload
				+ ", status=" + status + ", mac_address=" + mac_address + ", category=" + category + ", type=" + type
				+ ", ifindex=" + ifindex + ", connected_mac_address=" + connected_mac_address + ", device_id="
				+ device_id + ", device_display_name=" + device_display_name + ", device_user_data_name="
				+ device_user_data_name + ", device_ip_address=" + device_ip_address + ", device_mac_address="
				+ device_mac_address + ", devices=" + devices + ", user_connected_devices=" + user_connected_devices
				+ ", connected_devices=" + connected_devices + ", connected_devices_json=" + connected_devices_json
				+ ", user_connected_devices_json=" + user_connected_devices_json + ", speed=" + speed + ", uplink="
				+ uplink + "]";
	}





	


	
	
}
