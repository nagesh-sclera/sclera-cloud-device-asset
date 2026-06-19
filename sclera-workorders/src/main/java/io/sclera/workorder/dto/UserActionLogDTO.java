package io.sclera.workorder.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Outbound DTO for audit-log entries that sclera-cloud-workorder publishes to vdms-service
 * via Dapr pub/sub (vdms-service owns the user_action_log table).
 *
 * Field set mirrors the monolith's UserActionLogService.addUserAction parameters:
 *   addUserAction(email, type, action, message, status, sub_type, primary_id)
 *
 * Plus vdms_id (vdms-service partitions audit data per VDMS).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserActionLogDTO {

    private String email;
    private String type;        // "maximo"
    private String action;      // "ADD" | "UPDATE" | "DELETE"
    private String message;
    private String status;      // "success" | "failed"
    private String subType;     // "maximo_configuration"
    private String primaryId;   // configuration id
    private String vdmsId;
    private String requestId;   // correlation id (MDC) so async audit logs trace back to the request

    public UserActionLogDTO() {
    }

    public UserActionLogDTO(String email, String type, String action, String message,
                            String status, String subType, String primaryId, String vdmsId) {
        this.email = email;
        this.type = type;
        this.action = action;
        this.message = message;
        this.status = status;
        this.subType = subType;
        this.primaryId = primaryId;
        this.vdmsId = vdmsId;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getSubType() { return subType; }
    public void setSubType(String subType) { this.subType = subType; }

    public String getPrimaryId() { return primaryId; }
    public void setPrimaryId(String primaryId) { this.primaryId = primaryId; }

    public String getVdmsId() { return vdmsId; }
    public void setVdmsId(String vdmsId) { this.vdmsId = vdmsId; }

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
}
