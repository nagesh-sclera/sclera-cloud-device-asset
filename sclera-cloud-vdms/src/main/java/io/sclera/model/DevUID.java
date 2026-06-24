package io.sclera.model;

import io.sclera.dto.DevUIDDTO;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;
import java.math.BigInteger;

@Entity
@Getter
@Setter

@SqlResultSetMapping(
        name = "devuidmapping",
        classes = {
                @ConstructorResult(
                        targetClass = DevUIDDTO.class,
                        columns = {
                                @ColumnResult(name = "vdms_id" , type = String.class),
                                @ColumnResult(name = "property_name" , type = String.class),
                                @ColumnResult(name = "devuid" , type = String.class),
                                @ColumnResult(name = "last_seen" , type = BigInteger.class)
                        }
                )
        }
)


@NamedNativeQuery(
        name = "DevUID.getAllDevUIDs",
        query = "SELECT v.id AS vdms_id, v.property_name, d.devuid, d.last_seen " +
                "FROM devuid d " +
                "LEFT JOIN vdms v ON v.devuid = d.devuid " +
                "WHERE " +
                "(?1 = 'all' OR CONCAT_WS('', v.id, v.property_name, d.devuid) LIKE CONCAT('%', ?1, '%')) " +
                "ORDER BY " +
                "CASE " +
                "WHEN ?2 = 'last_seen' THEN d.last_seen " +
                "WHEN ?2 = 'last_seen,property_name' THEN " +
                "CASE " +
                "WHEN v.property_name IS NULL THEN 0 " +
                "ELSE 1 " +
                "END " +
                "WHEN ?2 = 'last_seen,dev_uid' THEN " +
                "CASE " +
                "WHEN v.property_name IS NOT NULL THEN 0 " +
                "ELSE 1 " +
                "END "+
                "ELSE NULL " +
                "END DESC, -d.last_seen " +
                "LIMIT ?3 OFFSET ?4",
        resultSetMapping = "devuidmapping"
)

@NamedNativeQuery(
        name = "DevUID.getVisibleDevUIDs",
        query = "SELECT v.id AS vdms_id, v.property_name, vv.devuid, d.last_seen " +
                "FROM devuid d " +
                "JOIN vdms_access_visibility vv ON vv.devuid = d.devuid " +
                "LEFT JOIN vdms v ON v.devuid = vv.devuid " +
                "WHERE " +
                "(vv.email = ?1 AND (?2 = 'all' OR CONCAT_WS('', v.id, v.property_name, d.devuid) LIKE CONCAT('%', ?2, '%'))) " +
                "ORDER BY " +
                "CASE " +
                "WHEN ?3 = 'last_seen' THEN d.last_seen " +
                "WHEN ?3 = 'last_seen,property_name' THEN " +
                "CASE " +
                "WHEN v.property_name IS NULL THEN 0 " +
                "ELSE 1 " +
                "END " +
                "WHEN ?3 = 'last_seen,dev_uid' THEN " +
                "CASE " +
                "WHEN v.property_name IS NOT NULL THEN 0 " +
                "ELSE 1 " +
                "END "+
                "ELSE NULL " +
                "END DESC, -d.last_seen " +
                "LIMIT ?4 OFFSET ?5",
        resultSetMapping = "devuidmapping"
)


public class DevUID {

    @Id
    private String devuid;

    @Column(columnDefinition = "bigint default 0")
    private BigInteger last_seen;

}
