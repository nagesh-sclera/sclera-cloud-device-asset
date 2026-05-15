package io.sclera.models;

import javax.persistence.Entity;
import javax.persistence.Id;

/** STUB: non-AP-C1 entity (no @Entity to keep out of schema) */
@Entity
public class Snmp_Configuration {
    @Id
    private Long id;
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
}
