package io.sclera.integration.model;

import io.sclera.integration.dto.VdmsAccessDTO;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;

@SqlResultSetMapping(
        name = "vdmsAccessMapping",
        classes = {
                @ConstructorResult(
                        targetClass = VdmsAccessDTO.class,
                        columns = {
                                @ColumnResult(name = "vdmsId", type = String.class),
                                @ColumnResult(name = "propertyName", type = String.class)
                        }
                )
        }
)


@NamedNativeQuery(
        name = "VdmsAccess.getVdmsAccessByUsername",
        query = "SELECT va.vdms_id AS vdmsId , va.property_name AS propertyName FROM vdms_access va " +
                "LEFT JOIN vdms v ON v.id = va.vdms_id WHERE (va.username = ?1 AND v.customer_org_id = ?2 " +
                "AND (?3 = 'all' " +
                "OR CONCAT_WS('', va.vdms_id, va.property_name) LIKE CONCAT ('%', ?3, '%'))) " +
                "LIMIT ?4 OFFSET ?5",
        resultSetMapping = "vdmsAccessMapping"
)
@Entity
@Getter
@Setter
public class VdmsAccess {

    @Id
    private String id;

    @Column(length = 64)
    private String vdms_id;

    @Column(length = 64)
    private String property_name;

    @Column(length = 64)
    private String username;

}
