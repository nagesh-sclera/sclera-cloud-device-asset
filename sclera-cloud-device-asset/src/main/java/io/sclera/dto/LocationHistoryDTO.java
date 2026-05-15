package io.sclera.dto;

import java.math.BigInteger;

public class LocationHistoryDTO {
    private String id;
    private String type;
    private String status;
    private String description;
    private BigInteger updated_timestamp;
    private String updated_email;
    private String location_id;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigInteger getUpdated_timestamp() { return updated_timestamp; }
    public void setUpdated_timestamp(BigInteger updated_timestamp) { this.updated_timestamp = updated_timestamp; }
    public String getUpdated_email() { return updated_email; }
    public void setUpdated_email(String updated_email) { this.updated_email = updated_email; }
    public String getLocation_id() { return location_id; }
    public void setLocation_id(String location_id) { this.location_id = location_id; }
}
