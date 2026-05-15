package io.sclera.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeviceOnboardStatusDTO {
    private String id;
    private String device_id;
    private String assignee_email;
    private Integer image_status;
    private Integer geolocation_status;
    private Integer tag_status;
    private Integer field_status;
    private Integer onboard_status;
    private String image_comment;
    private String geolocation_comment;
    private String tag_comment;
    private String field_comment;
    private Set<DeviceOnboardStatusAssigneeDTO> device_onboard_status_assignees;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDevice_id() {
        return device_id;
    }

    public void setDevice_id(String device_id) {
        this.device_id = device_id;
    }

    public String getAssignee_email() {
        return assignee_email;
    }

    public void setAssignee_email(String assignee_email) {
        this.assignee_email = assignee_email;
    }

    public Integer getImage_status() {
        return image_status;
    }

    public void setImage_status(Integer image_status) {
        this.image_status = image_status;
    }

    public Integer getGeolocation_status() {
        return geolocation_status;
    }

    public void setGeolocation_status(Integer geolocation_status) {
        this.geolocation_status = geolocation_status;
    }

    public Integer getTag_status() {
        return tag_status;
    }

    public void setTag_status(Integer tag_status) {
        this.tag_status = tag_status;
    }

    public Integer getField_status() {
        return field_status;
    }

    public void setField_status(Integer field_status) {
        this.field_status = field_status;
    }

    public Integer getOnboard_status() {
        return onboard_status;
    }

    public void setOnboard_status(Integer onboard_status) {
        this.onboard_status = onboard_status;
    }

    public String getImage_comment() {
        return image_comment;
    }

    public void setImage_comment(String image_comment) {
        this.image_comment = image_comment;
    }

    public String getGeolocation_comment() {
        return geolocation_comment;
    }

    public void setGeolocation_comment(String geolocation_comment) {
        this.geolocation_comment = geolocation_comment;
    }

    public String getTag_comment() {
        return tag_comment;
    }

    public void setTag_comment(String tag_comment) {
        this.tag_comment = tag_comment;
    }

    public String getField_comment() {
        return field_comment;
    }

    public void setField_comment(String field_comment) {
        this.field_comment = field_comment;
    }

    public Set<DeviceOnboardStatusAssigneeDTO> getDevice_onboard_status_assignees() {
        return device_onboard_status_assignees;
    }

    public void setDevice_onboard_status_assignees(Set<DeviceOnboardStatusAssigneeDTO> device_onboard_status_assignees) {
        this.device_onboard_status_assignees = device_onboard_status_assignees;
    }

    public DeviceOnboardStatusDTO() {
    }

    public DeviceOnboardStatusDTO(String assignee_email, Integer image_status, Integer geolocation_status, Integer tag_status, Integer field_status) {
        this.assignee_email = assignee_email;
        this.image_status = image_status;
        this.geolocation_status = geolocation_status;
        this.tag_status = tag_status;
        this.field_status = field_status;
    }
    public DeviceOnboardStatusDTO(String assignee_email, Integer image_status, Integer geolocation_status, Integer tag_status, Integer field_status,
                                  Set<DeviceOnboardStatusAssigneeDTO> device_onboard_status_assignees) {
        this.assignee_email = assignee_email;
        this.image_status = image_status;
        this.geolocation_status = geolocation_status;
        this.tag_status = tag_status;
        this.field_status = field_status;
        this.device_onboard_status_assignees = device_onboard_status_assignees;
    }

    public DeviceOnboardStatusDTO(String assignee_email, Integer image_status, Integer geolocation_status, Integer tag_status, Integer field_status, Integer onboard_status) {
        this.assignee_email = assignee_email;
        this.image_status = image_status;
        this.geolocation_status = geolocation_status;
        this.tag_status = tag_status;
        this.field_status = field_status;
        this.onboard_status = onboard_status;
    }

    public DeviceOnboardStatusDTO(String device_id, String assignee_email, Integer image_status, Integer geolocation_status, Integer tag_status, Integer field_status) {
        this.device_id = device_id;
        this.assignee_email = assignee_email;
        this.image_status = image_status;
        this.geolocation_status = geolocation_status;
        this.tag_status = tag_status;
        this.field_status = field_status;
    }

    public DeviceOnboardStatusDTO(String assignee_email, Integer image_status, Integer geolocation_status, Integer tag_status, Integer field_status, String id) {
        this.assignee_email = assignee_email;
        this.image_status = image_status;
        this.geolocation_status = geolocation_status;
        this.tag_status = tag_status;
        this.field_status = field_status;
        this.id = id;
    }

    @Override
    public String toString() {
        return "DeviceOnboardStatusDTO{" +
                "id='" + id + '\'' +
                ", device_id='" + device_id + '\'' +
                ", assignee_email='" + assignee_email + '\'' +
                ", image_status=" + image_status +
                ", geolocation_status=" + geolocation_status +
                ", tag_status=" + tag_status +
                ", field_status=" + field_status +
                '}';
    }
}
