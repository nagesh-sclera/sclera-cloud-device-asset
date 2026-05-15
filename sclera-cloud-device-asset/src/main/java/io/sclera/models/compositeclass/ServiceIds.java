package io.sclera.models.compositeclass;

import java.io.Serializable;

import io.sclera.models.Device;

public class ServiceIds implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String id;
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
	
	public ServiceIds() {}
	
	public ServiceIds(String id, Device device) {
		super();
		this.id = id;
		this.device = device;
	}

}
