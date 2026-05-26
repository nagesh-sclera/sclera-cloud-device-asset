package io.sclera.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "record_checklist")
public class RecordChecklist {
    @Id
    private String id;
    private String record_type;

    @ManyToOne
    private Device device;

    @jakarta.persistence.Transient
    private InspectionRecord inspection_record;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getRecord_type() { return record_type; }
    public void setRecord_type(String record_type) { this.record_type = record_type; }
    public Device getDevice() { return device; }
    public void setDevice(Device device) { this.device = device; }
    public InspectionRecord getInspection_record() { return inspection_record; }
    public void setInspection_record(InspectionRecord r) { this.inspection_record = r; }
}