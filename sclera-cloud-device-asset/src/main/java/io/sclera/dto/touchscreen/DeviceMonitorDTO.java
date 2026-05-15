package io.sclera.dto.touchscreen;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigInteger;
import java.util.List;


@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeviceMonitorDTO {

	private String id;
	private String ip_address;
	private String mac_address;
	private Integer status;
	private String vdms_id;
	private String docker_name;
	private Integer alarm;
	private BigInteger last_seen_on;
	private String snmp_parent;
	
	private List<DeviceIPAddressDTO> ip_addresses;
	
	public String getId() {
		return id;
	}
	public String getIp_address() {
		return ip_address;
	}
	public String getMac_address() {
		return mac_address;
	}
	public Integer getStatus() {
		return status;
	}
	public String getVdms_id() {
		return vdms_id;
	}
	public String getDocker_name() {
		return docker_name;
	}
	public Integer getAlarm() {
		return alarm;
	}
	public BigInteger getLast_seen_on() {
		return last_seen_on;
	}
	public void setId(String id) {
		this.id = id;
	}
	public void setIp_address(String ip_address) {
		this.ip_address = ip_address;
	}
	public void setMac_address(String mac_address) {
		this.mac_address = mac_address;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	public void setVdms_id(String vdms_id) {
		this.vdms_id = vdms_id;
	}
	public void setDocker_name(String docker_name) {
		this.docker_name = docker_name;
	}
	public void setAlarm(Integer alarm) {
		this.alarm = alarm;
	}
	public void setLast_seen_on(BigInteger last_seen_on) {
		this.last_seen_on = last_seen_on;
	}
	public String getSnmp_parent() {
		return snmp_parent;
	}
	public void setSnmp_parent(String snmp_parent) {
		this.snmp_parent = snmp_parent;
	}
	public List<DeviceIPAddressDTO> getIp_addresses() {
		return ip_addresses;
	}
	public void setIp_addresses(List<DeviceIPAddressDTO> ip_addresses) {
		this.ip_addresses = ip_addresses;
	}
	public DeviceMonitorDTO() {
		super();
		// TODO Auto-generated constructor stub
	}
	public DeviceMonitorDTO(String id, String ip_address, String mac_address, Integer status, String vdms_id,
			String docker_name, Integer alarm, BigInteger last_seen_on) {
		super();
		this.id = id;
		this.ip_address = ip_address;
		this.mac_address = mac_address;
		this.status = status;
		this.vdms_id = vdms_id;
		this.docker_name = docker_name;
		this.alarm = alarm;
		this.last_seen_on = last_seen_on;
	}
	
	
	
	@Override
	public String toString() {
		return "DeviceMonitorDTO [id=" + id + ", ip_address=" + ip_address + ", mac_address=" + mac_address
				+ ", status=" + status + ", vdms_id=" + vdms_id + ", docker_name=" + docker_name + ", alarm=" + alarm
				+ ", last_seen_on=" + last_seen_on + ", snmp_parent=" + snmp_parent + ", ip_addresses=" + ip_addresses
				+ "]";
	}
	
	public DeviceMonitorDTO(String ip_address, String mac_address, Integer status) {
		super();
		this.ip_address = ip_address;
		this.mac_address = mac_address;
		this.status = status;
	}
	
	public DeviceMonitorDTO(String id, String ip_address, String mac_address, String snmp_parent) {
		super();
		this.id = id;
		this.ip_address = ip_address;
		this.mac_address = mac_address;
		this.snmp_parent = snmp_parent;
	}
}
