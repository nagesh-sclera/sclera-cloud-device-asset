package io.sclera.service;

import java.math.BigInteger;

/** STUB: UserActionLogDTO for compile compatibility */
public class UserActionLogDTO {

    private String type;
    private String sub_type;
    private String status;
    private String primary_id;
    private String email;
    private String secondary_id;
    private String table_name;
    private BigInteger created_timestamp;
    private String message;
    private String action;

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getSub_type() { return sub_type; }
    public void setSub_type(String sub_type) { this.sub_type = sub_type; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPrimary_id() { return primary_id; }
    public void setPrimary_id(String primary_id) { this.primary_id = primary_id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getSecondary_id() { return secondary_id; }
    public void setSecondary_id(String secondary_id) { this.secondary_id = secondary_id; }
    public String getTable_name() { return table_name; }
    public void setTable_name(String table_name) { this.table_name = table_name; }
    public BigInteger getCreated_timestamp() { return created_timestamp; }
    public void setCreated_timestamp(BigInteger created_timestamp) { this.created_timestamp = created_timestamp; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
}
