package io.sclera.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigInteger;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AiCallLogDTO {
    private String id;
    private BigInteger createdAt;
    private BigInteger assignedAt;
    private String issueType;
    private String description;
    private String priority;
    private String status;
    @JsonProperty("isCompleted")
    private Boolean isCompleted;
    private String deviceId;
    private String technicianId;
    private String technicianName;


    private List<AiCallLogHistoryDTO> aiCallLogHistory;
    private CallStatusDTO callStatus;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public BigInteger getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(BigInteger createdAt) {
        this.createdAt = createdAt;
    }

    public BigInteger getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(BigInteger assignedAt) {
        this.assignedAt = assignedAt;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIssueType() {
        return issueType;
    }

    public void setIssueType(String issueType) {
        this.issueType = issueType;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @JsonProperty("isCompleted")
    public Boolean getCompleted() {
        return isCompleted;
    }

    @JsonProperty("isCompleted")
    public void setCompleted(Boolean isCompleted) {
        this.isCompleted = isCompleted;
    }


    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getTechnicianId() {
        return technicianId;
    }

    public void setTechnicianId(String technicianId) {
        this.technicianId = technicianId;
    }

    public List<AiCallLogHistoryDTO> getAiCallLogHistory() {
        return aiCallLogHistory;
    }

    public void setAiCallLogHistory(List<AiCallLogHistoryDTO> aiCallLogHistory) {
        this.aiCallLogHistory = aiCallLogHistory;
    }

    public CallStatusDTO getCallStatus() {
        return callStatus;
    }

    public void setCallStatus(CallStatusDTO callStatus) {
        this.callStatus = callStatus;
    }

    public String getTechnicianName() {
        return technicianName;
    }

    public void setTechnicianName(String technicianName) {
        this.technicianName = technicianName;
    }

    public AiCallLogDTO(String id, BigInteger createdAt, BigInteger assignedAt, String issueType, String description, String priority, String status, Boolean isCompleted, String deviceId, String technicianId, List<AiCallLogHistoryDTO> aiCallLogHistory, CallStatusDTO callStatus) {
        this.id = id;
        this.createdAt = createdAt;
        this.assignedAt = assignedAt;
        this.issueType = issueType;
        this.description = description;
        this.priority = priority;
        this.status = status;
        this.isCompleted = isCompleted;
        this.deviceId = deviceId;
        this.technicianId = technicianId;
        this.aiCallLogHistory = aiCallLogHistory;
        this.callStatus = callStatus;
    }

    public AiCallLogDTO(String id, BigInteger created_at, BigInteger assigned_at, String description, String issueType, String priority, String status, Boolean is_completed, String device_id, String technician_id, String technician_name) {
        this.id = id;
        this.createdAt = created_at;
        this.assignedAt = assigned_at;
        this.issueType = issueType;
        this.description = description;
        this.priority = priority;
        this.status = status;
        this.isCompleted = is_completed;
        this.deviceId = device_id;
        this.technicianId = technician_id;
        this.technicianName = technician_name;
    }

    public AiCallLogDTO() {
    }

    @Override
    public String toString() {
        return "AiCallLogDTO{" +
                "id='" + id + '\'' +
                ", createdAt=" + createdAt +
                ", assignedAt=" + assignedAt +
                ", issueType='" + issueType + '\'' +
                ", description='" + description + '\'' +
                ", priority='" + priority + '\'' +
                ", status='" + status + '\'' +
                ", isCompleted=" + isCompleted +
                ", deviceId='" + deviceId + '\'' +
                ", technicianId='" + technicianId + '\'' +
                ", aiCallLogHistory=" + aiCallLogHistory +
                ", callStatus=" + callStatus +
                '}';
    }
}
