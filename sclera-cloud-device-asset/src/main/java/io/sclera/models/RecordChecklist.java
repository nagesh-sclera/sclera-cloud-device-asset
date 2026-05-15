package io.sclera.models;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "record_checklist")
public class RecordChecklist {

    @Id
    private String id;
    private String record_type;

    // TODO: replace with Dapr call when checklist module is ready
    @javax.persistence.Transient
    private InspectionRecord inspection_record;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getRecord_type() { return record_type; }
    public void setRecord_type(String record_type) { this.record_type = record_type; }

    public InspectionRecord getInspection_record() { return inspection_record; }
    public void setInspection_record(InspectionRecord inspection_record) { this.inspection_record = inspection_record; }
}
