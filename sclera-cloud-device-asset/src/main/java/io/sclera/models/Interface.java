package io.sclera.models;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ColumnResult;
import javax.persistence.ConstructorResult;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedNativeQuery;
import javax.persistence.SqlResultSetMapping;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import io.sclera.dto.InterfaceDTO;

@Entity
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class , property = "id" , scope = Interface.class)


@SqlResultSetMapping(
		name = "interfacemapping",
		classes = {
				@ConstructorResult(
						targetClass = InterfaceDTO.class,
						columns = {
								@ColumnResult(name = "id",type = String.class),
								@ColumnResult(name = "name",type = String.class),
								@ColumnResult(name = "download",type = String.class),
								@ColumnResult(name = "upload",type = String.class),
								@ColumnResult(name = "status",type = String.class),
								@ColumnResult(name = "mac_address",type = String.class),
								@ColumnResult(name = "category",type = String.class),
								@ColumnResult(name = "type",type = String.class),
								@ColumnResult(name = "ifindex",type = String.class),
								@ColumnResult(name = "connected_devices_json",type = String.class),
								@ColumnResult(name = "speed",type = String.class),
								@ColumnResult(name = "uplink",type = Integer.class),
								@ColumnResult(name = "unmanaged_switch_name",type = String.class),
								@ColumnResult(name = "unmanaged_switch_type",type = String.class)
						})
		})



@NamedNativeQuery(
		name = "Interface.listDeviceInterfaceByDeviceId",
		query = "SELECT i.id, i.name, i.download, i.upload, i.status, i.mac_address, i.category, i.type, i.ifindex, i.connected_devices as connected_devices_json, i.speed, i.uplink,"
				+ " i.unmanaged_switch_name, i.unmanaged_switch_type "
				+ "FROM interface i "
				+ "LEFT JOIN device d ON d.id = i.device_id "
				+ "WHERE i.device_id = ?1",
				resultSetMapping = "interfacemapping"
		)


@SqlResultSetMapping(
		name = "interfacemacmapping",
		classes = {
				@ConstructorResult(
						targetClass = InterfaceDTO.class,
						columns = {
								@ColumnResult(name = "ifindex",type = String.class),
								@ColumnResult(name = "connected_mac_address",type = String.class),
								@ColumnResult(name = "device_id",type = String.class),
								@ColumnResult(name = "device_mac_address",type = String.class)
						})
		})


@NamedNativeQuery(
		name = "Interface.listAllNetworkNonNativeSnmpInterfaces",
		query = "SELECT i.ifindex, i.connected_mac_address, i.device_id, d.mac_address as device_mac_address FROM interface i JOIN device d ON i.device_id = d.id "
				+ "WHERE d.docker_name != ?1 AND i.connected_mac_address IS NOT NULL ",
		resultSetMapping = "interfacemacmapping"
		)


@SqlResultSetMapping(
		name = "interfacelistmapping",
		classes = {
				@ConstructorResult(
						targetClass = InterfaceDTO.class,
						columns = {
								@ColumnResult(name = "name",type = String.class),
								@ColumnResult(name = "download",type = String.class),
								@ColumnResult(name = "upload",type = String.class),
								@ColumnResult(name = "status",type = String.class),
								@ColumnResult(name = "mac_address",type = String.class),
								@ColumnResult(name = "category",type = String.class),
								@ColumnResult(name = "type",type = String.class),
								@ColumnResult(name = "ifindex",type = String.class),
								@ColumnResult(name = "device_id",type = String.class),
								@ColumnResult(name = "device_display_name",type = String.class),
								@ColumnResult(name = "device_user_data_name",type = String.class),
								@ColumnResult(name = "device_ip_address",type = String.class),
								@ColumnResult(name = "device_mac_address",type = String.class)


						})
		})
 


@NamedNativeQuery(
		name = "Interface.listDeviceSnmpInterfaceByDeviceId",
		query = "SELECT i.name, i.download, i.upload, i.status, i.mac_address, i.category, i.type, i.ifindex, "
				+ "d.id as device_id, d.display_name as device_display_name, d.user_data_name as device_user_data_name, d.ip_address as device_ip_address, d.mac_address as device_mac_address "
				+ "FROM (Select DISTINCT i1.ifindex, i1.name, i1.download, i1.upload, i1.status, i1.mac_address, i1.category, i1.type, i1.device_id from interface i1 where i1.device_id = ?1) i "
				+ "LEFT JOIN device d ON i.device_id = d.snmp_parent AND i.ifindex = d.snmp_parent_index",
				resultSetMapping = "interfacelistmapping"
		)

public class Interface {

	@Id
	private String id;

	@Column(length = 256)
	private String name;

	@Column(length = 32)
	private String download;

	@Column(length = 32)
	private String upload;

	@Column(length = 16)
	private String status;

	@Column(length = 32)
	private String mac_address;

	@Column(length = 64)
	private String category;

	@Column(length = 10)
	private String type;

	@Column(length = 16)
	private String ifindex;

	@Column(length = 32)
	private String connected_mac_address;
	
	@Column(length = 16)
	private String speed;

	@Column(columnDefinition = "TEXT")
	private String connected_devices;

	@Column(columnDefinition = "TEXT")
	private String user_connected_devices;

	@Column(columnDefinition = "integer default 0", length = 1)
	private Integer uplink;
	
	@Column(length = 128)
	private String unmanaged_switch_name;
	
	@Column(length = 128)
	private String unmanaged_switch_type;

	@ManyToOne
	private Device device;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Device getDevice() {
		return device;
	}

	public void setDevice(Device device) {
		this.device = device;
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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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

	public String getSpeed() {
		return speed;
	}

	public void setSpeed(String speed) {
		this.speed = speed;
	}

	public String getConnected_devices() {
		return connected_devices;
	}

	public void setConnected_devices(String connected_devices) {
		this.connected_devices = connected_devices;
	}

	public String getUser_connected_devices() {
		return user_connected_devices;
	}

	public void setUser_connected_devices(String user_connected_devices) {
		this.user_connected_devices = user_connected_devices;
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







}
