package io.sclera.dto.touchscreen;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)

public class DeviceMonitorSpaceDTO {

	private String id;
	private String spacename;
	private Integer status;
	private Integer sensorstatus;
	private String position;
	private String initial_position;
	private String image_url;
	private String angle;
	private String area;
	private String record_checklist_status;
	private String record_checklist_result;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getSpacename() {
		return spacename;
	}
	public void setSpacename(String spacename) {
		this.spacename = spacename;
	}
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	public Integer getSensorstatus() {
		return sensorstatus;
	}
	public void setSensorstatus(Integer sensorstatus) {
		this.sensorstatus = sensorstatus;
	}
	public String getPosition() {
		return position;
	}
	public void setPosition(String position) {
		this.position = position;
	}
	
	public String getInitial_position() {
		return initial_position;
	}
	public void setInitial_position(String initial_position) {
		this.initial_position = initial_position;
	}
	public String getImage_url() {
		return image_url;
	}
	public void setImage_url(String image_url) {
		this.image_url = image_url;
	}
	public String getAngle() {
		return angle;
	}
	public void setAngle(String angle) {
		this.angle = angle;
	}
	public String getArea() {
		return area;
	}
	public void setArea(String area) {
		this.area = area;
	}

	public String getRecord_checklist_status() {
		return record_checklist_status;
	}

	public void setRecord_checklist_status(String record_checklist_status) {
		this.record_checklist_status = record_checklist_status;
	}

	public String getRecord_checklist_result() {
		return record_checklist_result;
	}

	public void setRecord_checklist_result(String record_checklist_result) {
		this.record_checklist_result = record_checklist_result;
	}

	public DeviceMonitorSpaceDTO() {
		super();
		// TODO Auto-generated constructor stub
	}
	public DeviceMonitorSpaceDTO(String id, String spacename, Integer status,  String position,
			String initial_position, String image_url, Integer sensorstatus , String angle, String area) {
		super();
		this.id = id;
		this.spacename = spacename;
		this.status = status;
		this.position = position;
		this.initial_position = initial_position;
		this.image_url = image_url;
		this.sensorstatus = sensorstatus;
		this.angle = angle;
		this.area = area;
	}
	
	@Override
	public String toString() {
		return "DeviceMonitorSpaceDTO [id=" + id + ", spacename=" + spacename + ", status=" + status + ", sensorstatus="
				+ sensorstatus + ", position=" + position + ", initial_position=" + initial_position + ", image_url="
				+ image_url + "]";
	}

	public DeviceMonitorSpaceDTO(String id, Integer status, Integer sensorstatus) {
		this.id = id;
		this.status = status;
		this.sensorstatus = sensorstatus;
	}
	
	
	
}

