package io.sclera.models;

// Minimal compatibility stub for the extracted service — only columns referenced by native queries (loose coupling, scalar FKs).

import java.io.Serializable;
import java.util.Objects;

/**
 * Composite-key holder identifying the association between a location and a global
 * checklist, used as the identity for location-checklist join records.
 */
public class LocationGlobalChecklistId implements Serializable {

    private static final long serialVersionUID = 1L;

    private String location_id;
    private String global_checklist_id;

    public LocationGlobalChecklistId() {}

    public LocationGlobalChecklistId(String location_id, String global_checklist_id) {
        this.location_id = location_id;
        this.global_checklist_id = global_checklist_id;
    }

    public String getLocation_id() { return location_id; }
    public void setLocation_id(String location_id) { this.location_id = location_id; }

    public String getGlobal_checklist_id() { return global_checklist_id; }
    public void setGlobal_checklist_id(String global_checklist_id) { this.global_checklist_id = global_checklist_id; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LocationGlobalChecklistId)) return false;
        LocationGlobalChecklistId that = (LocationGlobalChecklistId) o;
        return Objects.equals(location_id, that.location_id) &&
               Objects.equals(global_checklist_id, that.global_checklist_id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(location_id, global_checklist_id);
    }
}
