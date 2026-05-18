package io.sclera.models;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import java.util.List;
import java.util.Set;

/** STUB: non-AP-C1 entity (no @Entity to keep out of schema) */
@Entity
@Data
public class Inventory {
    @Id
    private Long id;
    @ManyToMany(mappedBy = "inventory")
    private Set<Device> device;
}
