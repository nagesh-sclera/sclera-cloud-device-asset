package io.sclera.dto.touchscreen;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigInteger;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SensorDTO {
	
	private String primary_id;
	private String secondary_id;
	private String name;
	private Boolean alert;
	private String category;
	private String protocol;
	private  String value;
	List<SensorValueDTO> sensor_values;

	private String location_id;
	private String location_name;
	private String sensor_name;
	private String device_id;
	private String device_name;
	private String unit;

	private BigInteger last_seen;
	
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
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
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

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public List<SensorValueDTO> getSensor_values() {
		return sensor_values;
	}
	public void setSensor_values(List<SensorValueDTO> sensor_values) {
		this.sensor_values = sensor_values;
	}

	public String getLocation_id() {
		return location_id;
	}

	public void setLocation_id(String location_id) {
		this.location_id = location_id;
	}

	public String getLocation_name() {
		return location_name;
	}

	public void setLocation_name(String location_name) {
		this.location_name = location_name;
	}

	public String getSensor_name() {
		return sensor_name;
	}

	public void setSensor_name(String sensor_name) {
		this.sensor_name = sensor_name;
	}

	public String getDevice_id() {
		return device_id;
	}

	public void setDevice_id(String device_id) {
		this.device_id = device_id;
	}

	public String getDevice_name() {
		return device_name;
	}

	public void setDevice_name(String device_name) {
		this.device_name = device_name;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public BigInteger getLast_seen() {
		return last_seen;
	}

	public void setLast_seen(BigInteger last_seen) {
		this.last_seen = last_seen;
	}

	public SensorDTO() {
		super();
	}

	//Constructor for Bacnet Sensor
	public SensorDTO(String primary_id, String secondary_id, String name, Boolean alert, String category) {
		super();
		this.primary_id = primary_id;
		this.secondary_id = secondary_id;
		this.name = name;
		this.alert = alert;
		this.category = category;
	}
	
	//Constructor for other Sensors
	public SensorDTO(String primary_id, String name, Boolean alert, String category) {
		super();
		this.primary_id = primary_id;
		this.name = name;
		this.alert = alert;
		this.category = category;
	}


	public SensorDTO(String primary_id, String secondary_id, String name, Boolean alert, String category, String protocol,String value) {
		this.primary_id = primary_id;
		this.secondary_id = secondary_id;
		this.name = name;
		this.alert = alert;
		this.category = category;
		this.protocol = protocol;
		this.value = value;
	}

//	//Constructor for Lorawan Sensors
//	public SensorDTO(String primary_id, String name, String category) {
//		super();
//		this.primary_id = primary_id;
//		this.name = name;
//		this.category = category;
//	}

	public SensorDTO(String primary_id, String secondary_id, String name, Boolean alert, String category, String protocol, String value, String location_id, String location_name, String sensor_name, String device_id, String device_name, String unit, BigInteger last_seen) {
		this.primary_id = primary_id;
		this.secondary_id = secondary_id;
		this.name = name;
		this.alert = alert;
		this.category = category;
		this.protocol = protocol;
		this.value = value;
		this.location_id = location_id;
		this.location_name = location_name;
		this.sensor_name = sensor_name;
		this.device_id = device_id;
		this.device_name = device_name;
		this.unit = unit;
		this.last_seen = last_seen;
	}

	public SensorDTO(String name, String value, Boolean alert, String category, String primary_id,
					 String secondary_id, String protocol, String location_id,BigInteger last_seen) {
		this.name = name;
		this.value = value;
		this.alert = alert;
		this.category = category;
		this.primary_id = primary_id;
		this.secondary_id = secondary_id;
		this.protocol = protocol;
		this.location_id = location_id;
		this.last_seen = last_seen;
	}

	// Daintree share conditions constructor
	public SensorDTO(String primary_id, String secondary_id, String name) {
		this.primary_id = primary_id;
		this.secondary_id = secondary_id;
		this.name = name;
	}


	public SensorDTO(String name, String value, Boolean alert, String category, String primary_id,
					 String secondary_id, String protocol, String location_id,BigInteger last_seen, String unit) {
		this.name = name;
		this.value = value;
		this.alert = alert;
		this.category = category;
		this.primary_id = primary_id;
		this.secondary_id = secondary_id;
		this.protocol = protocol;
		this.location_id = location_id;
		this.last_seen = last_seen;
		this.unit = unit;
	}

}
