package io.sclera.models;

import io.sclera.dto.LocationHistoryDTO;

import jakarta.persistence.*;
import java.math.BigInteger;

/**
 * JPA entity recording a status-change event for a location, used to build the audit
 * trail of location history within the asset-management domain.
 */
@Entity
public class LocationHistory {
    @Id
    private String id;

    @Column(length = 128)
    private String status;
    private String type;
    private String description;
    private BigInteger updated_timestamp;
    private String updated_email;
    @ManyToOne
    private Location location;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigInteger getUpdated_timestamp() {
        return updated_timestamp;
    }

    public void setUpdated_timestamp(BigInteger updated_timestamp) {
        this.updated_timestamp = updated_timestamp;
    }

    public String getUpdated_email() {
        return updated_email;
    }

    public void setUpdated_email(String updated_email) {
        this.updated_email = updated_email;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
}
