package io.sclera.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

/** STUB: non-AP-C1 entity (no @Entity to keep out of schema) */
@Entity
public class CheckListTemplate {
    @Id
    private Long id;
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
}
