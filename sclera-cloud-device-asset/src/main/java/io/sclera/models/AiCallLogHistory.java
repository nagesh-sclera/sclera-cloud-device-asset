package io.sclera.models;

import jakarta.persistence.*;
import java.math.BigInteger;

// @NamedNativeQuery "AiCallLogHistory.getAiCallLogHistoryByAiCallLogId" and
// "AiCallLogHistory.getAiCallLogHistoryById" and their @SqlResultSetMapping
// "aicallloghistorymapping" have been removed — both queries are now expressed as
// JPQL constructor expressions in AiCallLogHistoryRepository (grep confirmed no other
// class referenced these names).

/**
 * JPA entity recording a single state-change entry in the lifecycle history of an {@link AiCallLog},
 * capturing the timestamp, description, state, and acting technician for each update.
 */
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

