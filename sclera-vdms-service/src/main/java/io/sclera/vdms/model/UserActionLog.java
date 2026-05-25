package io.sclera.vdms.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_action_log")
public class UserActionLog {

    @Id
    private String id;

    @Column(name = "user_email")
    private String userEmail;

    @Column(name = "vdms_id")
    private String vdmsId;

    private String type;            // e.g. "device"

    private String action;          // ADD | UPDATE | DELETE

    private String status;          // success | failure

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(name = "affected_record_id")
    private String affectedRecordId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    public String getVdmsId() { return vdmsId; }
    public void setVdmsId(String vdmsId) { this.vdmsId = vdmsId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getAffectedRecordId() { return affectedRecordId; }
    public void setAffectedRecordId(String affectedRecordId) { this.affectedRecordId = affectedRecordId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
