package io.sclera.dto.touchscreen;

import java.math.BigInteger;

public class DeviceHistoryDTO {
	
	private String id;
	private String ip_address;
	private String mac_address;
	private String vdms_id;
	private String docker_name;
	private Integer alarm;
	private BigInteger timestamp;
	private String old_ip_address;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
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
	public Integer getAlarm() {
		return alarm;
	}
	public void setAlarm(Integer alarm) {
		this.alarm = alarm;
	}
	public BigInteger getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(BigInteger timestamp) {
		this.timestamp = timestamp;
	}
	public String getOld_ip_address() {
		return old_ip_address;
	}
	public void setOld_ip_address(String old_ip_address) {
		this.old_ip_address = old_ip_address;
	}
	
	@Override
	public String toString() {
		return "DeviceHistoryDTO [id=" + id + ", ip_address=" + ip_address + ", mac_address=" + mac_address
				+ ", vdms_id=" + vdms_id + ", docker_name=" + docker_name + ", alarm=" + alarm + ", timestamp="
				+ timestamp + ", old_ip_address=" + old_ip_address + "]";
	}
}
