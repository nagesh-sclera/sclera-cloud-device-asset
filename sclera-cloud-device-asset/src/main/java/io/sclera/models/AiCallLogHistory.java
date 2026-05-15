package io.sclera.models;

import io.sclera.dto.AiCallLogHistoryDTO;

import javax.persistence.*;
import java.math.BigInteger;

@SqlResultSetMapping(
        name = "aicallloghistorymapping",
        classes = {
                @ConstructorResult(
                        targetClass = AiCallLogHistoryDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = String.class),
                                @ColumnResult(name = "createdAt", type = BigInteger.class),
                                @ColumnResult(name = "description", type = String.class),
                                @ColumnResult(name = "technicianId", type = String.class),
                                @ColumnResult(name = "state", type = String.class),
                                @ColumnResult(name = "aiCallLogId", type = String.class)
                        })
        })


@NamedNativeQuery(
        name = "AiCallLogHistory.getAiCallLogHistoryByAiCallLogId",
        query = "SELECT ach.id, ach.created_at as createdAt, ach.description, ach.ai_call_log_id as aiCallLogId, ach.technician_id as technicianId, ach.state " +
                "FROM ai_call_log_history ach WHERE ai_call_log_id = ?1 " +
                "ORDER BY ach.created_at ASC",
        resultSetMapping = "aicallloghistorymapping"
)

@NamedNativeQuery(
        name = "AiCallLogHistory.getAiCallLogHistoryById",
        query = "SELECT ach.id, ach.created_at as createdAt, ach.description, ach.ai_call_log_id as aiCallLogId , ach.technician_id as technicianId , ach.state " +
                "FROM ai_call_log_history ach WHERE id=?1",
        resultSetMapping = "aicallloghistorymapping"
)

@Entity
public class AiCallLogHistory {

    @Id
    private String id;
    private BigInteger createdAt;
    private String description;
    private String state;
    @ManyToOne
    private AiCallLog aiCallLog;

    @ManyToOne
    @JoinColumn(name = "technician_id")
    private Technician technician;

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

    public AiCallLog getAiCallLog() {
        return aiCallLog;
    }

    public void setAiCallLog(AiCallLog aiCallLog) {
        this.aiCallLog = aiCallLog;
    }

    public Technician getTechnician() {
        return technician;
    }

    public void setTechnician(Technician technician) {
        this.technician = technician;
    }
}

