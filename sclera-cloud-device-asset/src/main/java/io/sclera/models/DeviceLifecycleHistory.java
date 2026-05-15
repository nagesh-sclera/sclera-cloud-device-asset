package io.sclera.models;

import io.sclera.dto.DeviceLifecycleHistoryDTO;

import javax.persistence.*;
import java.math.BigInteger;

@SqlResultSetMapping(
        name = "deviceLifeCycleHistoryMapping",
        classes = {
                @ConstructorResult(
                        targetClass = DeviceLifecycleHistoryDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = String.class),
                                @ColumnResult(name = "operational_status", type = String.class),
                                @ColumnResult(name = "usage_status", type = String.class),
                                @ColumnResult(name = "assigned_user_id", type = String.class),
                                @ColumnResult(name = "assignment_count", type = Integer.class),
                                @ColumnResult(name = "created_timestamp", type = BigInteger.class),
                                @ColumnResult(name = "assigned_timestamp", type = BigInteger.class),
                                @ColumnResult(name = "device_id", type = String.class),
                                @ColumnResult(name = "description", type = String.class),
                                @ColumnResult(name = "assigned_by_user_id", type = String.class)
                        })
        })

@NamedNativeQuery(
        name = "DeviceLifeCycleHistory.getDeviceLifeCycleHistory",
        query = "SELECT dlc.id, dlc.operational_status, dlc.usage_status, dlc.assigned_user_id, " +
                "dlc.assignment_count, dlc.created_timestamp,dlc.assigned_timestamp, dlc.device_id, dlc.description, dlc.assigned_by_user_id " +
                "FROM device_lifecycle_history dlc " +
                "WHERE dlc.device_id = ?1 " +
                "ORDER BY dlc.created_timestamp DESC, dlc.assignment_count DESC " +
                "LIMIT ?2 OFFSET ?3",
        resultSetMapping = "deviceLifeCycleHistoryMapping"
)

@Entity
@Table(name = "device_lifecycle_history")
public class DeviceLifecycleHistory {

    @Id
    private String id;

    @Column(length = 32)
    private String operational_status;

    @Column(length = 32)
    private String usage_status;

    @Column(length = 128)
    private String assigned_user_id;

    private Integer assignment_count;

    private BigInteger created_timestamp;

    private BigInteger assigned_timestamp;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 255)
    private String assigned_by_user_id;

    @ManyToOne
    @JoinColumn(name = "device_id", referencedColumnName = "id")
    private Device device;

    // Getters and Setters
    public String getId() { return id; }

    public void setId(String id) { this.id = id; }

    public String getOperational_status() { return operational_status; }

    public void setOperational_status(String operational_status) { this.operational_status = operational_status; }

    public String getUsage_status() { return usage_status; }

    public void setUsage_status(String usage_status) { this.usage_status = usage_status; }

    public String getAssigned_user_id() { return assigned_user_id; }

    public void setAssigned_user_id(String assigned_user_id) { this.assigned_user_id = assigned_user_id; }

    public Integer getAssignment_count() { return assignment_count; }

    public void setAssignment_count(Integer assignment_count) { this.assignment_count = assignment_count; }

    public BigInteger getCreated_timestamp() { return created_timestamp; }

    public void setCreated_timestamp(BigInteger created_timestamp) { this.created_timestamp = created_timestamp; }

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }

    public String getAssigned_by_user_id() { return assigned_by_user_id; }

    public void setAssigned_by_user_id(String assigned_by_user_id) { this.assigned_by_user_id = assigned_by_user_id; }

    public Device getDevice() { return device; }

    public void setDevice(Device device) { this.device = device; }

    public BigInteger getAssigned_timestamp() {
        return assigned_timestamp;
    }

    public void setAssigned_timestamp(BigInteger assigned_timestamp) {
        this.assigned_timestamp = assigned_timestamp;
    }
}