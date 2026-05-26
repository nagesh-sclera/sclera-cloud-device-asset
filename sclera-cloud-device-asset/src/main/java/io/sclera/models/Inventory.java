package io.sclera.models;

import lombok.Data;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
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
