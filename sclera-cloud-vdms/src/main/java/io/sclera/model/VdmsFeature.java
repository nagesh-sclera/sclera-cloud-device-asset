package io.sclera.model;

import io.sclera.dto.VdmsFeatureDTO;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@SqlResultSetMapping(
        name = "vdmsFeatureDetailsMapping",
        classes = {
                @ConstructorResult(
                        targetClass = VdmsFeatureDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = String.class),
                                @ColumnResult(name = "feature_id", type = String.class),
                                @ColumnResult(name = "vdms_id", type = String.class)
                        }
                )
        }
)

@NamedNativeQuery(
        name = "VdmsFeature.getVdmsFeatureDetailsByVdmsId",
        query = "SELECT id, feature_id, vdms_id " +
                "FROM vdms_feature WHERE vdms_id = ?1",
        resultSetMapping = "vdmsFeatureDetailsMapping"
)

@Entity
@Getter
@Setter
public class VdmsFeature {

    @Id
    @Column(length = 64)
    private String id;

    @ManyToOne
    private Vdms vdms;

    @ManyToOne
    private Feature feature;
}
