package io.sclera.models;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "history")
public class History {
    @Id
    private Long id;

    @ManyToOne
    private Bacnet_Object bacnet_object;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Bacnet_Object getBacnet_object() { return bacnet_object; }
    public void setBacnet_object(Bacnet_Object bacnet_object) { this.bacnet_object = bacnet_object; }
}
