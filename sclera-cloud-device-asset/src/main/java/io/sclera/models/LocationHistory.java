package io.sclera.models;

import io.sclera.dto.LocationHistoryDTO;

import javax.persistence.*;
import java.math.BigInteger;

@SqlResultSetMapping(
        name = "locationhistorymapping",
        classes = {
                @ConstructorResult(
                        targetClass = LocationHistoryDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = String.class),
                                @ColumnResult(name = "status", type = String.class),
                                @ColumnResult(name = "type", type = String.class),
                                @ColumnResult(name = "description", type = String.class),
                                @ColumnResult(name = "updated_timestamp", type = BigInteger.class),
                                @ColumnResult(name = "updated_email", type = String.class),
                                @ColumnResult(name = "location_id", type = String.class)


                        })
        })

@NamedNativeQuery(
        name = "LocationHistory.getLocationHistory",
        query = "SELECT lh.id , lh.status,lh.type, lh.description, lh.updated_timestamp, lh.updated_email, lh.location_id "
                + " FROM location_history lh "
                + " WHERE lh.location_id = ?1 "
                + " ORDER BY  lh.updated_timestamp DESC, lh.id ",
        resultSetMapping = "locationhistorymapping"
)
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
