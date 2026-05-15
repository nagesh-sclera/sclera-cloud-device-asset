package io.sclera.dto;

import java.math.BigInteger;

public class Product_PortsDTO {
	private String id;
	private String port;
	private Integer status;
	private BigInteger timestamp;
	private String product_id;
	private String device_id;
	private Integer is_global;
	
	private String ip_address;
	
	public Integer getIs_global() {
		return is_global;
	}
	public void setIs_global(Integer is_global) {
		this.is_global = is_global;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getPort() {
		return port;
	}
	public void setPort(String port) {
		this.port = port;
	}
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	public BigInteger getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(BigInteger timestamp) {
		this.timestamp = timestamp;
	}
	public String getProduct_id() {
		return product_id;
	}
	public void setProduct_id(String product_id) {
		this.product_id = product_id;
	}
	
	public String getDevice_id() {
		return device_id;
	}
	public void setDevice_id(String device_id) {
		this.device_id = device_id;
	}
	
	
	
	
	
	public String getIp_address() {
		return ip_address;
	}
	public void setIp_address(String ip_address) {
		this.ip_address = ip_address;
	}
	public Product_PortsDTO() {}

	public Product_PortsDTO(String id, String port, Integer status, BigInteger timestamp, String device_id,
			Integer is_global) {
		super();
		this.id = id;
		this.port = port;
		this.status = status;
		this.timestamp = timestamp;
		this.device_id = device_id;
		this.is_global = is_global;
	}
	@Override
	public String toString() {
		return "Product_PortsDTO [id=" + id + ", port=" + port + ", status=" + status + ", timestamp=" + timestamp
				+ ", product_id=" + product_id + ", device_id=" + device_id + ", is_global=" + is_global + "]";
	}

	
	
	
	

	//Service Monitor
	public Product_PortsDTO(String id, String port, String ip_address, String device_id) {
		super();
		this.id = id;
		this.port = port;
		this.ip_address = ip_address;
		this.device_id = device_id;
	}
	
	
	
	
	
	
	
	
	
}
