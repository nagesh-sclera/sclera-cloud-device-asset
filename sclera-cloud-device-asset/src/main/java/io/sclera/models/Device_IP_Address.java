package io.sclera.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

/**
 * Represents an IP address associated with a device, including its IP-conflict status. Used to track
 * device network addressing and to detect IP address conflicts across devices.
 */
@Entity
public class Device_IP_Address {

	@Id
	private String id;
	
	@Column(length = 64)
	private String ip_address;
	
	@Column(length = 8, columnDefinition = "integer default 0")
	private Integer ip_conflict_status;
	
	@ManyToOne
	private Device device;

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

	public Device getDevice() {
		return device;
	}

	public void setDevice(Device device) {
		this.device = device;
	}
}
