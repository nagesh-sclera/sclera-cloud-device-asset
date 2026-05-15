package io.sclera.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigInteger;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class VlanDTO {
	private String id;
	private String interface_name;
	private Boolean is_tagged;
	private String vlan_id;
	private String ip_address;
	private String gateway;
	private String subnet;
	private BigInteger timestamp;
	private String cidr;
	
	@JsonIgnore
	private String pid;
	private Boolean is_running;
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getInterface_name() {
		return interface_name;
	}

	public void setInterface_name(String interface_name) {
		this.interface_name = interface_name;
	}

	public Boolean getIs_tagged() {
		return is_tagged;
	}

	public void setIs_tagged(Boolean is_tagged) {
		this.is_tagged = is_tagged;
	}

	public String getVlan_id() {
		return vlan_id;
	}

	public void setVlan_id(String vlan_id) {
		this.vlan_id = vlan_id;
	}

	public String getIp_address() {
		return ip_address;
	}

	public void setIp_address(String ip_address) {
		this.ip_address = ip_address;
	}

	public String getGateway() {
		return gateway;
	}

	public void setGateway(String gateway) {
		this.gateway = gateway;
	}

	public String getSubnet() {
		return subnet;
	}

	public void setSubnet(String subnet) {
		this.subnet = subnet;
	}
	
	public BigInteger getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(BigInteger timestamp) {
		this.timestamp = timestamp;
	}
	
	public String getCidr() {
		return cidr;
	}

	public void setCidr(String cidr) {
		this.cidr = cidr;
	}

	public String getPid() {
		return pid;
	}

	public void setPid(String pid) {
		this.pid = pid;
	}
	
	public Boolean getIs_running() {
		return is_running;
	}

	public void setIs_running(Boolean is_running) {
		this.is_running = is_running;
	}
	public VlanDTO() {
		super();
		// TODO Auto-generated constructor stub
	}
	

	//	listDiscoveredDevicesbyInterfaceName
	public VlanDTO(String id, String interface_name, Boolean is_tagged, String vlan_id, String ip_address, BigInteger timestamp) {
		super();
		this.id = id;
		this.interface_name = interface_name;
		this.is_tagged = is_tagged;
		this.vlan_id = vlan_id;
		this.ip_address = ip_address;
		this.timestamp = timestamp;
	}

	public VlanDTO(String pid, BigInteger timestamp) {
		super();
		this.timestamp = timestamp;
		this.pid = pid;
	}
	@Override
	public String toString() {
		return "VlanDTO [id=" + id + ", interface_name=" + interface_name + ", is_tagged=" + is_tagged + ", vlan_id="
				+ vlan_id + ", ip_address=" + ip_address + ", gateway=" + gateway + ", subnet=" + subnet
				+ ", timestamp=" + timestamp + ", cidr=" + cidr + "]";
	}



















}
