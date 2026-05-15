package io.sclera.dto.touchscreen;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeviceIPAddressDTO {
	
	private String id;
	private String ip_address;
	private Integer ip_conflict_status;
	private String device_id;
	
	
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
	public Integer getIp_conflict_status() {
		return ip_conflict_status;
	}
	public void setIp_conflict_status(Integer ip_conflict_status) {
		this.ip_conflict_status = ip_conflict_status;
	}
	public String getDevice_id() {
		return device_id;
	}
	public void setDevice_id(String device_id) {
		this.device_id = device_id;
	}
	
	public DeviceIPAddressDTO() {
		super();
	}
	
	public DeviceIPAddressDTO(String ip_address, Integer ip_conflict_status) {
		super();
		this.ip_address = ip_address;
		this.ip_conflict_status = ip_conflict_status;
	}
	
	@Override
	public String toString() {
		return "DeviceIPAddressDTO [id=" + id + ", ip_address=" + ip_address + ", ip_conflict_status="
				+ ip_conflict_status + ", device_id=" + device_id + "]";
	}
	
	
}
