package io.sclera.models;

import io.sclera.dto.AiCallLogDTO;
import io.sclera.dto.CallStatusDTO;

import javax.persistence.*;
import java.math.BigInteger;
import java.util.Set;


@SqlResultSetMapping(
        name = "aiCallStatusMapping",
        classes = {
                @ConstructorResult(
                        targetClass = AiCallLogDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = String.class),
                                @ColumnResult(name = "createdAt", type = BigInteger.class),
                                @ColumnResult(name = "assignedAt", type = BigInteger.class),
                                @ColumnResult(name = "issueType", type = String.class),
                                @ColumnResult(name = "description", type = String.class),
                                @ColumnResult(name = "priority", type = String.class),
                                @ColumnResult(name = "status", type = String.class),
                                @ColumnResult(name = "isCompleted", type = Boolean.class),
                                @ColumnResult(name = "deviceId", type = String.class),
                                @ColumnResult(name = "technicianId", type = String.class),
                                @ColumnResult(name = "technicianName", type = String.class)


                        })
        })


@NamedNativeQuery(
        name = "AiCallLog.getAllAiCallLog",
        query = "SELECT aicl.id, aicl.created_at as createdAt ,aicl.assigned_at as assignedAt ,aicl.description, aicl.issue_type as issueType, aicl.priority,aicl.status,aicl.is_completed as isCompleted,aicl.device_id as deviceId,aicl.technician_id as technicianId, t.name as technicianName " +
                "FROM ai_call_log aicl " +
                "JOIN device d ON d.id = aicl.device_id " +
                "LEFT JOIN technician t ON t.id = aicl.technician_id " +
                "WHERE (?3 = 'null' OR CONCAT_WS('', d.user_data_name, COALESCE(t.name, ''), aicl.description, aicl.issue_type) LIKE CONCAT('%', ?3, '%')) AND aicl.is_completed = ?4 ORDER BY aicl.created_at DESC " +
                "LIMIT ?1 OFFSET ?2 ",
        resultSetMapping = "aiCallStatusMapping"
)

@SqlResultSetMapping(
        name = "callstatusmapping",
        classes = {
                @ConstructorResult(
                        targetClass = CallStatusDTO.class,
                        columns = {
                                @ColumnResult(name = "deviceName", type = String.class),
                                @ColumnResult(name = "propertyId", type = String.class),
                                @ColumnResult(name = "propertyName", type = String.class),
                                @ColumnResult(name = "buildingId", type = String.class),
                                @ColumnResult(name = "buildingName", type = String.class),
                                @ColumnResult(name = "floorId", type = String.class),
                                @ColumnResult(name = "floorName", type = String.class)
                        }
                )
        }
)

//TODO Technician name shd be updated in the query
@NamedNativeQuery(
        name = "AiCallLog.getStatusInformation",
        query = "SELECT user_data_name AS deviceName, " +
                "v.id AS propertyId, " +
                "v.property_name AS propertyName, " +
                "b.id AS buildingId, " +
                "b.name AS buildingName, " +
                "f.id AS floorId, " +
                "f.name AS floorName " +
                "FROM device d " +
                "LEFT JOIN location loc ON d.location_id = loc.id " +
                "LEFT JOIN floor f ON f.id = loc.floor_id " +
                "LEFT JOIN building b ON b.id = f.building_id " +
                "LEFT JOIN vdms v ON v.id = b.vdms_id " +
                "WHERE d.id = ?1 ",
        resultSetMapping = "callstatusmapping"
)



@Entity
public class AiCallLog {
    @Id
    private String id;
    private BigInteger createdAt;
    private BigInteger assignedAt;
    private String issueType;
    private String description;
    private String priority;
    private String status;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "aiCallLog")
    private Set<AiCallLogHistory> aiCallLogHistories;

    @ManyToOne
    private Device device;

    @ManyToOne
    @JoinColumn(name = "technician_id")
    private Technician technician;

    private String isCompleted;


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

    public String getIssueType() {
        return issueType;
    }

    public void setIssueType(String issueType) {
        this.issueType = issueType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public Set<AiCallLogHistory> getAiCallLogHistories() {
        return aiCallLogHistories;
    }

    public void setAiCallLogHistories(Set<AiCallLogHistory> aiCallLogHistories) {
        this.aiCallLogHistories = aiCallLogHistories;
    }


    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    public String getIsCompleted() {
        return isCompleted;
    }

    public void setIsCompleted(String isCompleted) {
        this.isCompleted = isCompleted;
    }

    public Technician getTechnician() {
        return technician;
    }

    public void setTechnician(Technician technician) {
        this.technician = technician;
    }
}
