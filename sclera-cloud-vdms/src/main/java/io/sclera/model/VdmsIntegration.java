package io.sclera.model;

import io.sclera.dto.VdmsIntegrationDTO;
import io.sclera.model.compositeclass.VdmsIntegrationIds;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@IdClass(VdmsIntegrationIds.class)

@SqlResultSetMapping(
        name = "vdmsIntegrationMapping",
        classes = {
                @ConstructorResult(
                        targetClass = VdmsIntegrationDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = String.class),
                                @ColumnResult(name = "vdms_id", type = String.class),
                                @ColumnResult(name = "active", type = Integer.class),
                                @ColumnResult(name = "helper_id", type = String.class)
                        }
                )
        }
)


@NamedNativeQuery(
        name = "VdmsIntegration.getAllVdmsIntegrationsByVdmsIdAndHelperId",
        query = "SELECT id,vdms_id,active,helper_id FROM vdms_integration WHERE helper_id IN ?1 AND vdms_id =?2 ORDER By helper_id",
        resultSetMapping = "vdmsIntegrationMapping"
)

@Getter
@Setter
public class VdmsIntegration {

    @Id
    private String id;

    @MapsId
    @ManyToOne
    private Vdms vdms;

    @Column(columnDefinition = "integer default 0")
    private Integer active;

    @Column
    private String helper_id;

}
