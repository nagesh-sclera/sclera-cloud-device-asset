package io.sclera.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigInteger;
import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DevicedataDTO {

	private String device_id;
	private String display_name;
	private BigInteger last_seen;
	private String model;
	private String vendor;
	private String user_data_name;
	private String user_data_model;
	private String user_data_vendor;
	private String network_name;
	private String vdms;
	private Integer virtual_device_type;
	private String warranty;
	private String type;

	private String asset_group;
	private Set<HistoryDTO> history;
	
	public String getDevice_id() {
		return device_id;
	}
	public void setDevice_id(String device_id) {
		this.device_id = device_id;
	}
	public String getDisplay_name() {
		return display_name;
	}
	public void setDisplay_name(String display_name) {
		this.display_name = display_name;
	}
	public BigInteger getLast_seen() {
		return last_seen;
	}
	public void setLast_seen(BigInteger last_seen) {
		this.last_seen = last_seen;
	}
	public String getModel() {
		return model;
	}
	public void setModel(String model) {
		this.model = model;
	}
	public String getVendor() {
		return vendor;
	}
	public void setVendor(String vendor) {
		this.vendor = vendor;
	}
	public String getUser_data_name() {
		return user_data_name;
	}
	public void setUser_data_name(String user_data_name) {
		this.user_data_name = user_data_name;
	}
	public String getUser_data_model() {
		return user_data_model;
	}
	public void setUser_data_model(String user_data_model) {
		this.user_data_model = user_data_model;
	}
	public String getUser_data_vendor() {
		return user_data_vendor;
	}
	public void setUser_data_vendor(String user_data_vendor) {
		this.user_data_vendor = user_data_vendor;
	}
	public String getNetwork_name() {
		return network_name;
	}
	public void setNetwork_name(String network_name) {
		this.network_name = network_name;
	}
	public String getVdms() {
		return vdms;
	}
	public void setVdms(String vdms) {
		this.vdms = vdms;
	}
	public Integer getVirtual_device_type() {
		return virtual_device_type;
	}
	public void setVirtual_device_type(Integer virtual_device_type) {
		this.virtual_device_type = virtual_device_type;
	}
	public String getWarranty() {
		return warranty;
	}
	public void setWarranty(String warranty) {
		this.warranty = warranty;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public Set<HistoryDTO> getHistory() {
		return history;
	}
	public void setHistory(Set<HistoryDTO> history) {
		this.history = history;
	}

	public String getAsset_group() {
		return asset_group;
	}

	public void setAsset_group(String asset_group) {
		this.asset_group = asset_group;
	}
	
	public DevicedataDTO(String device_id, String display_name, BigInteger last_seen, String model, String vendor,
			String user_data_name, String user_data_model, String user_data_vendor, String network_name, String vdms,
			Integer virtual_device_type, String warranty, String type,String asset_group) {
		super();
		this.device_id = device_id;
		this.display_name = display_name;
		this.last_seen = last_seen;
		this.model = model;
		this.vendor = vendor;
		this.user_data_name = user_data_name;
		this.user_data_model = user_data_model;
		this.user_data_vendor = user_data_vendor;
		this.network_name = network_name;
		this.vdms = vdms;
		this.virtual_device_type = virtual_device_type;
		this.warranty = warranty;
		this.type = type;
		this.asset_group = asset_group;
	}
	@Override
	public String toString() {
		return "DevicedataDTO [device_id=" + device_id + ", display_name=" + display_name + ", last_seen=" + last_seen
				+ ", model=" + model + ", vendor=" + vendor + ", user_data_name=" + user_data_name
				+ ", user_data_model=" + user_data_model + ", user_data_vendor=" + user_data_vendor + ", network_name="
				+ network_name + ", vdms=" + vdms + ", virtual_device_type=" + virtual_device_type + ", warranty="
				+ warranty + ", type=" + type + ", history=" + history + "]";
	}
	
	
	
	
	
	
	
	
	
}
