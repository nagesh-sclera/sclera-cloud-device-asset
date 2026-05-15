package io.sclera.models;

import javax.persistence.Entity;
import javax.persistence.Id;

/** STUB: non-AP-C1 entity (no @Entity to keep out of schema) */
@Entity
public class GlobalInspectionRelation {
    @Id
    private String id;
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
}
