package io.sclera.workorder.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

/**
 * VDMS details returned by vdms-service via Dapr service invocation.
 *
 * Used by the NEW sample method {@code MaximoService.getVdmsDetailsForMaximoConfig}.
 * Field set matches {@code io.sclera.vdms.dto.VdmsDTO} on the vdms-service side
 * (the wire contract between the two services).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VdmsDetailsDTO {

    private String id;
    private String name;
    private String description;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;

    public VdmsDetailsDTO() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
