package io.sclera.models;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Vendor_Organisation {
    @Id
    private String id;
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
}