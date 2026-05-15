package io.sclera.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigInteger;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AiCallLogHistoryDTO {

    private String id;
    private BigInteger createdAt;
    private String description;
    private String technicianId;
    private String technicianName;
    private String state;
    private String aiCallLogId;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getTechnicianId() {
        return technicianId;
    }

    public void setTechnicianId(String technicianId) {
        this.technicianId = technicianId;
    }

    public String getTechnicianName() {
        return technicianName;
    }

    public void setTechnicianName(String technicianName) {
        this.technicianName = technicianName;
    }

    public String getAiCallLogId() {
        return aiCallLogId;
    }

    public void setAiCallLogId(String aiCallLogId) {
        this.aiCallLogId = aiCallLogId;
    }

    public AiCallLogHistoryDTO(String id, BigInteger created_at, String description, String technician_id, String state, String ai_call_log_id) {
        this.id = id;
        this.createdAt = created_at;
        this.description = description;
        this.technicianId = technician_id;
        this.state = state;
        this.aiCallLogId = ai_call_log_id;
    }

    public AiCallLogHistoryDTO() {
    }

    @Override
    public String toString() {
        return "AiCallLogHistoryDTO{" +
                "id='" + id + '\'' +
                ", createdAt=" + createdAt +
                ", description='" + description + '\'' +
                ", technicianId='" + technicianId + '\'' +
                ", technicianName='" + technicianName + '\'' +
                ", state='" + state + '\'' +
                ", aiCallLogId='" + aiCallLogId + '\'' +
                '}';
    }
}
