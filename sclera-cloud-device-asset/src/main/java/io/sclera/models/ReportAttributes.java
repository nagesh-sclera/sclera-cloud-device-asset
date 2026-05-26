package io.sclera.models;

// Minimal compatibility stub for the extracted service — only columns referenced by native queries (loose coupling, scalar FKs).

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "report_attributes")
public class ReportAttributes {

    @Id
    private String id;

    private String primary_id;

    private String secondary_id;

    private String protocol;

    /** Scalar FK — replaces @ManyToOne ReportTemplate (loose coupling). */
    @Column(name = "report_template_id")
    private String report_template_id;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPrimary_id() { return primary_id; }
    public void setPrimary_id(String primary_id) { this.primary_id = primary_id; }

    public String getSecondary_id() { return secondary_id; }
    public void setSecondary_id(String secondary_id) { this.secondary_id = secondary_id; }

    public String getProtocol() { return protocol; }
    public void setProtocol(String protocol) { this.protocol = protocol; }

    public String getReport_template_id() { return report_template_id; }
    public void setReport_template_id(String report_template_id) { this.report_template_id = report_template_id; }
}
