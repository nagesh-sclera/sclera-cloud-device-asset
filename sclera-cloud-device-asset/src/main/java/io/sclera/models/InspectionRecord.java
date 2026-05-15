package io.sclera.models;

/** STUB: non-AP-C1 entity */
public class InspectionRecord {

    private String id;
    private Boolean is_removed;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Boolean getIs_removed() { return is_removed != null ? is_removed : Boolean.FALSE; }
    public void setIs_removed(Boolean is_removed) { this.is_removed = is_removed; }
}
