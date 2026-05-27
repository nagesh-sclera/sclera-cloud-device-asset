package io.sclera.models;

// Minimal compatibility stub for the extracted service — only columns referenced by native queries (loose coupling, scalar FKs).
// This entity replaces the @ManyToMany @JoinTable that existed on Location in the monolith.
// The is_removed column is used only via native queries (WHERE lgc.is_removed = 0); Hibernate manages DDL only.

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

@Entity
@Table(name = "location_global_checklist")
@IdClass(LocationGlobalChecklistId.class)
public class LocationGlobalChecklist {

    @Id
    @Column(name = "location_id", length = 255)
    private String location_id;

    @Id
    @Column(name = "global_checklist_id", length = 255)
    private String global_checklist_id;

    /** Soft-delete flag referenced in native queries as lgc.is_removed = 0. */
    @Column(name = "is_removed")
    private Integer is_removed;

    public String getLocation_id() { return location_id; }
    public void setLocation_id(String location_id) { this.location_id = location_id; }

    public String getGlobal_checklist_id() { return global_checklist_id; }
    public void setGlobal_checklist_id(String global_checklist_id) { this.global_checklist_id = global_checklist_id; }

    public Integer getIs_removed() { return is_removed; }
    public void setIs_removed(Integer is_removed) { this.is_removed = is_removed; }
}
