package io.sclera.model;

import io.sclera.dto.SubCategoryDTO;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigInteger;

@SqlResultSetMapping(
        name = "sensorSubCategoryMapping",
        classes = {
                @ConstructorResult(
                        targetClass = SubCategoryDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = String.class),
                                @ColumnResult(name = "name", type = String.class),
                                @ColumnResult(name = "iconUrl", type = String.class),
                                @ColumnResult(name = "displayName", type = String.class),
                                @ColumnResult(name = "creationTimestamp", type = BigInteger.class),
                        }
                )
        }
)

@NamedNativeQuery(
        name = "SensorSubcategory.getSensorSubCategoryBySensorCategoryId",
        query = "SELECT sc.id, sc.name, sc.icon_url AS iconUrl, sc.display_name AS displayName, sc.creation_timestamp AS creationTimestamp " +
                "FROM sensor_subcategory sc " +
                "LEFT JOIN sensor_category c ON c.id = sc.sensor_category_id " +
                "WHERE ((sc.sensor_category_id = ?1) " +
                "AND (?2 = 'all' OR CONCAT_WS('',sc.display_name) LIKE CONCAT('%',?2,'%'))) " +
                "ORDER BY " +
                "CASE " +
                "   WHEN ?3 = 'creation_timestamp' THEN c.creation_timestamp " +
                "   ELSE NULL " +
                "END DESC, " +
                "CASE " +
                "   WHEN ?3 = 'name' THEN c.name " +
                "   ELSE NULL " +
                "END ASC",
        resultSetMapping = "sensorSubCategoryMapping"
)

@NamedNativeQuery(
        name = "SensorSubcategory.getSensorSubCategoryBySensorCategoryName",
        query = "SELECT sc.id, sc.name, sc.icon_url AS iconUrl, sc.display_name AS displayName, sc.creation_timestamp AS creationTimestamp " +
                "FROM sensor_subcategory sc " +
                "LEFT JOIN sensor_category c ON c.id = sc.sensor_category_id " +
                "WHERE ((c.name = ?1) " +
                "AND (?2 = 'all' OR CONCAT_WS('',sc.display_name) LIKE CONCAT('%',?2,'%'))) " +
                "ORDER BY " +
                "CASE " +
                "   WHEN ?3 = 'creation_timestamp' THEN c.creation_timestamp " +
                "   ELSE NULL " +
                "END DESC, " +
                "CASE " +
                "   WHEN ?3 = 'name' THEN c.name " +
                "   ELSE NULL " +
                "END ASC",
        resultSetMapping = "sensorSubCategoryMapping"
)

@Entity
@Getter
@Setter
public class SensorSubcategory {

    @Id
    private String id;
    @Column(length = 128)
    private String name;
    @Column(columnDefinition = "TEXT")
    private String iconUrl;
    @Column(length = 128)
    private String display_name;
    private BigInteger creation_timestamp;
    @ManyToOne
    @JoinColumn(referencedColumnName = "id", name = "sensor_category_id")
    private SensorCategory sensorCategory;
}
