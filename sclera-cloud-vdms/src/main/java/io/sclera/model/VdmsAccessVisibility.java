package io.sclera.model;

import io.sclera.dto.VdmsAccessVisibilityDTO;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@SqlResultSetMapping(
        name = "vdmsAccessVisibilityMapping",
        classes = {
                @ConstructorResult(
                        targetClass = VdmsAccessVisibilityDTO.class,
                        columns = {
                                @ColumnResult(name = "devUId" , type = String.class),
                                @ColumnResult(name = "vdmsId" , type = String.class),
                                @ColumnResult(name = "propertyName" , type = String.class)
                        }
                )
        }
)

@NamedNativeQuery(
        name = "VdmsAccessVisibility.getVdmsAccessVisibilityByEmail",
        query = "SELECT v.id AS vdmsId, v.property_name AS propertyName, vv.devuid AS devUId FROM vdms_access_visibility vv " +
                "LEFT JOIN vdms v ON v.devuid = vv.devuid WHERE vv.email = ?1",
        resultSetMapping = "vdmsAccessVisibilityMapping"
)

@Entity
@Getter
@Setter
public class VdmsAccessVisibility {

    @Id
    private String id;

    @Column(length = 64)
    private String email;

    @Column(length = 64)
    private String devuid;

    @Column(columnDefinition = "varchar(8) DEFAULT '0'")
    private Boolean full_access;

}
