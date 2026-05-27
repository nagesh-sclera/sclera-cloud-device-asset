package io.sclera.models;

// Minimal compatibility stub for the extracted service — only columns referenced by native queries (loose coupling, scalar FKs).

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "customer_organisation")
public class CustomerOrganisation {

    @Id
    private String id;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
}
