package io.sclera.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

/**
 * JPA entity representing a vendor organisation within the asset-management domain,
 * identified by its unique organisation id.
 */
@Entity
public class Vendor_Organisation {
    @Id
    private String id;
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
}