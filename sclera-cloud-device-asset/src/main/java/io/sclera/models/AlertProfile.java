package io.sclera.models;

// Minimal compatibility stub for the extracted service — only columns referenced by native queries (loose coupling, scalar FKs).

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Minimal JPA entity representing an alert profile, exposing only the columns referenced by native
 * queries in the extracted service. Used as a loosely coupled compatibility stub.
 */
@Entity
@Table(name = "alert_profile")
public class AlertProfile {

    @Id
    private String id;

    @Column(length = 128)
    private String name;

    @Column(columnDefinition = "integer default 0")
    private Integer ioc;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getIoc() { return ioc; }
    public void setIoc(Integer ioc) { this.ioc = ioc; }
}
