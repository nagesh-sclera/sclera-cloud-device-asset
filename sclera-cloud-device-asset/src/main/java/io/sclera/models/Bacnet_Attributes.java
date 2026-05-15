package io.sclera.models;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class Bacnet_Attributes {
    @Id
    private String id;
    @ManyToOne
    private Bacnet_Object bacnet_object;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public Bacnet_Object getBacnet_object() { return bacnet_object; }
    public void setBacnet_object(Bacnet_Object bacnet_object) { this.bacnet_object = bacnet_object; }
}