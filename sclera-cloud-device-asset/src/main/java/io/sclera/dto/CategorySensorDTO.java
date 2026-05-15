package io.sclera.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CategorySensorDTO {

	private String name;
	private String value;
	private Boolean alert;
	private String category;
	private String primary_id;
	private String secondary_id;
	private String protocol;
	private String location_id;
	private String last_seen;

	private String device_id;


	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public Boolean getAlert() {
		return alert;
	}
	public void setAlert(Boolean alert) {
		this.alert = alert;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public String getPrimary_id() {
		return primary_id;
	}
	public void setPrimary_id(String primary_id) {
		this.primary_id = primary_id;
	}
	public String getSecondary_id() {
		return secondary_id;
	}
	public void setSecondary_id(String secondary_id) {
		this.secondary_id = secondary_id;
	}
	public String getProtocol() {
		return protocol;
	}
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}
	public String getLocation_id() {
		return location_id;
	}
	public void setLocation_id(String location_id) {
		this.location_id = location_id;
	}
	public String getLast_seen() {
		return last_seen;
	}

	public void setLast_seen(String last_seen) {
		this.last_seen = last_seen;
	}

	public String getDevice_id() {
		return device_id;
	}

	public void setDevice_id(String device_id) {
		this.device_id = device_id;
	}

	public CategorySensorDTO() {
		super();
		// TODO Auto-generated constructor stub
	}

	public CategorySensorDTO(String name, String value, Boolean alert, String category, String primary_id, String secondary_id, String protocol, String location_id, String last_seen, String device_id) {
		this.name = name;
		this.value = value;
		this.alert = alert;
		this.category = category;
		this.primary_id = primary_id;
		this.secondary_id = secondary_id;
		this.protocol = protocol;
		this.location_id = location_id;
		this.last_seen = last_seen;
		this.device_id = device_id;
	}
	
	
	
	
	

}
