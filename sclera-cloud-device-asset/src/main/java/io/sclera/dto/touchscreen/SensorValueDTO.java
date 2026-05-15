package io.sclera.dto.touchscreen;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SensorValueDTO {

	private String name;
	private String value;
	
	private Boolean alert;
	
	private String category;
	
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
	public SensorValueDTO() {
		super();
	}
	public SensorValueDTO(String name, String value, Boolean alert) {
		super();
		this.name = name;
		this.value = value;
		this.alert = alert;
	}
	
	public SensorValueDTO(String name, String value, Boolean alert, String category) {
		super();
		this.name = name;
		this.value = value;
		this.alert = alert;
		this.category = category;
	}
}
