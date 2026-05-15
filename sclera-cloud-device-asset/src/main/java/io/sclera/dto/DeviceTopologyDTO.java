package io.sclera.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeviceTopologyDTO {
	
	private String id;
	private String display_name;
	private String last_seen_on;
	private String mac_address;
	private String vendor;
	private String model;
	private String ip_address;
	private String location;
	private String user_data_vendor;
	private String user_data_name;
	private String parent;
	private String snmp_parent;
	private String docker_name;
	private String building;
	private String floor;
	private Integer status;
	private Integer virtual_device_type;
	private String type;
	private List<InterfaceDTO> ports;
	private List<String> parent_ids;
	private List<String> snmp_parent_ids;
	private List<String> ids;
	
	private String connection_type;
	private String user_connection_type;
	
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
	public String getLast_seen_on() {
		return last_seen_on;
	}
	public void setLast_seen_on(String last_seen_on) {
		this.last_seen_on = last_seen_on;
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
	public String getModel() {
		return model;
	}
	public void setModel(String model) {
		this.model = model;
	}
	public String getIp_address() {
		return ip_address;
	}
	public void setIp_address(String ip_address) {
		this.ip_address = ip_address;
	}
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}

	public String getUser_data_name() {
		return user_data_name;
	}
	public void setUser_data_name(String user_data_name) {
		this.user_data_name = user_data_name;
	}
	public String getUser_data_vendor() {
		return user_data_vendor;
	}
	public void setUser_data_vendor(String user_data_vendor) {
		this.user_data_vendor = user_data_vendor;
	}
	public String getParent() {
		return parent;
	}
	public void setParent(String parent) {
		this.parent = parent;
	}
	public String getSnmp_parent() {
		return snmp_parent;
	}
	public void setSnmp_parent(String snmp_parent) {
		this.snmp_parent = snmp_parent;
	}
	public String getDocker_name() {
		return docker_name;
	}
	public void setDocker_name(String docker_name) {
		this.docker_name = docker_name;
	}
	public String getBuilding() {
		return building;
	}
	public void setBuilding(String building) {
		this.building = building;
	}
	public String getFloor() {
		return floor;
	}
	public void setFloor(String floor) {
		this.floor = floor;
	}
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	public Integer getVirtual_device_type() {
		return virtual_device_type;
	}
	public void setVirtual_device_type(Integer virtual_device_type) {
		this.virtual_device_type = virtual_device_type;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public List<String> getParent_ids() {
		return parent_ids;
	}
	public void setParent_ids(List<String> parent_ids) {
		this.parent_ids = parent_ids;
	}
	public List<String> getSnmp_parent_ids() {
		return snmp_parent_ids;
	}
	public void setSnmp_parent_ids(List<String> snmp_parent_ids) {
		this.snmp_parent_ids = snmp_parent_ids;
	}
	public List<String> getIds() {
		return ids;
	}
	public void setIds(List<String> ids) {
		this.ids = ids;
	}
	
	public List<InterfaceDTO> getPorts() {
		return ports;
	}
	public void setPorts(List<InterfaceDTO> ports) {
		this.ports = ports;
	}
	
	public String getUser_connection_type() {
		return user_connection_type;
	}
	public void setUser_connection_type(String user_connection_type) {
		this.user_connection_type = user_connection_type;
	}
	public String getConnection_type() {
		return connection_type;
	}
	public void setConnection_type(String connection_type) {
		this.connection_type = connection_type;
	}
	
	// listTopologyDevices
	public DeviceTopologyDTO(String id, String display_name, String last_seen_on, String mac_address, String vendor,
			String model, String ip_address, String location, String user_data_vendor, String user_data_name,
			String parent, String snmp_parent, String docker_name, String building, String floor, Integer status,
			Integer virtual_device_type, String type, String connection_type, String user_connection_type) {
		super();
		this.id = id;
		this.display_name = display_name;
		this.last_seen_on = last_seen_on;
		this.mac_address = mac_address;
		this.vendor = vendor;
		this.model = model;
		this.ip_address = ip_address;
		this.location = location;
		this.user_data_vendor = user_data_vendor;
		this.user_data_name = user_data_name;
		this.parent = parent;
		this.snmp_parent = snmp_parent;
		this.docker_name = docker_name;
		this.building = building;
		this.floor = floor;
		this.status = status;
		this.virtual_device_type = virtual_device_type;
		this.type = type;
		this.connection_type = connection_type;
		this.user_connection_type = user_connection_type;
	}
	public DeviceTopologyDTO() {
		super();
		// TODO Auto-generated constructor stub
	}
	public DeviceTopologyDTO(String id, String display_name, String last_seen_on, String mac_address, String vendor,
			String model, String ip_address, String location, String user_data_vendor, String user_data_name,
			String parent, String snmp_parent, String docker_name, String building, String floor, Integer status,
			Integer virtual_device_type, String type, List<InterfaceDTO> ports, List<String> parent_ids,
			List<String> snmp_parent_ids, List<String> ids) {
		super();
		this.id = id;
		this.display_name = display_name;
		this.last_seen_on = last_seen_on;
		this.mac_address = mac_address;
		this.vendor = vendor;
		this.model = model;
		this.ip_address = ip_address;
		this.location = location;
		this.user_data_vendor = user_data_vendor;
		this.user_data_name = user_data_name;
		this.parent = parent;
		this.snmp_parent = snmp_parent;
		this.docker_name = docker_name;
		this.building = building;
		this.floor = floor;
		this.status = status;
		this.virtual_device_type = virtual_device_type;
		this.type = type;
		this.ports = ports;
		this.parent_ids = parent_ids;
		this.snmp_parent_ids = snmp_parent_ids;
		this.ids = ids;
	}

	
	
	
	
	
	
}
