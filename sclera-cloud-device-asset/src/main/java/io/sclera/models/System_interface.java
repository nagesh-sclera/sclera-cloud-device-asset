package io.sclera.models;

import java.math.BigInteger;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;


/**
 * Represents a system network interface tracked for discovery, holding its name, status and the PID of any running discovery process.
 */
@Entity
public class System_interface {

	@Id
	private String interface_name;
	private String status;
	private String pid;
	private BigInteger timestamp;


	public String getInterface_name() {
		return interface_name;
	}
	public void setInterface_name(String interface_name) {
		this.interface_name = interface_name;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getPid() {
		return pid;
	}
	public void setPid(String pid) {
		this.pid = pid;
	}
	public BigInteger getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(BigInteger timestamp) {
		this.timestamp = timestamp;
	}




}
