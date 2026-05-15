package io.sclera.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigInteger;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeviceLifecycleHistoryDTO {

    private String id;
    private String operational_status;
    private String usage_status;
    private String assigned_user_id;
    private Integer assignment_count;
    private BigInteger created_timestamp;
    private BigInteger assigned_timestamp;
    private String device_id;
    private String description;
    private String assigned_by_user_id;

    public DeviceLifecycleHistoryDTO() {
    }

    // Constructor with all fields
    public DeviceLifecycleHistoryDTO(String id, String operational_status, String usage_status,
                                     String assigned_user_id, Integer assignment_count,
                                     BigInteger created_timestamp, BigInteger assigned_timestamp, String device_id,
                                     String description, String assigned_by_user_id) {
        this.id = id;
        this.operational_status = operational_status;
        this.usage_status = usage_status;
        this.assigned_user_id = assigned_user_id;
        this.assignment_count = assignment_count;
        this.created_timestamp = created_timestamp;
        this.assigned_timestamp = assigned_timestamp;
        this.device_id = device_id;
        this.description = description;
        this.assigned_by_user_id = assigned_by_user_id;
    }

    // Constructor without ID
    public DeviceLifecycleHistoryDTO(String operational_status, String usage_status,
                                     String assigned_user_id, Integer assignment_count,
                                     BigInteger created_timestamp, BigInteger assigned_timestamp, String device_id,
                                     String description, String assigned_by_user_id) {
        this.operational_status = operational_status;
        this.usage_status = usage_status;
        this.assigned_user_id = assigned_user_id;
        this.assignment_count = assignment_count;
        this.created_timestamp = created_timestamp;
        this.assigned_timestamp = assigned_timestamp;
        this.device_id = device_id;
        this.description = description;
        this.assigned_by_user_id = assigned_by_user_id;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOperational_status() {
        return operational_status;
    }

    public void setOperational_status(String operational_status) {
        this.operational_status = operational_status;
    }

    public String getUsage_status() {
        return usage_status;
    }

    public void setUsage_status(String usage_status) {
        this.usage_status = usage_status;
    }

    public String getAssigned_user_id() {
        return assigned_user_id;
    }

    public void setAssigned_user_id(String assigned_user_id) {
        this.assigned_user_id = assigned_user_id;
    }

    public Integer getAssignment_count() {
        return assignment_count;
    }

    public void setAssignment_count(Integer assignment_count) {
        this.assignment_count = assignment_count;
    }

    public BigInteger getCreated_timestamp() {
        return created_timestamp;
    }

    public void setCreated_timestamp(BigInteger created_timestamp) {
        this.created_timestamp = created_timestamp;
    }

    public String getDevice_id() {
        return device_id;
    }

    public void setDevice_id(String device_id) {
        this.device_id = device_id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAssigned_by_user_id() {
        return assigned_by_user_id;
    }

    public void setAssigned_by_user_id(String assigned_by_user_id) {
        this.assigned_by_user_id = assigned_by_user_id;
    }

    public BigInteger getAssigned_timestamp() {
        return assigned_timestamp;
    }

    public void setAssigned_timestamp(BigInteger assigned_timestamp) {
        this.assigned_timestamp = assigned_timestamp;
    }
}