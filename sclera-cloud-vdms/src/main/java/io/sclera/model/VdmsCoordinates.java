package io.sclera.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Geometry;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VdmsCoordinates {

    @Id
    private String id;

    @OneToOne(mappedBy = "vdmsCoordinates")
    private Vdms vdms;

    @Column(columnDefinition = "geometry")
    private Geometry coordinates;
}
