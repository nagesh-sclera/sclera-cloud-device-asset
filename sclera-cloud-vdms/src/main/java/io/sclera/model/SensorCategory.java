package io.sclera.model;

import io.sclera.dto.CategoryDTO;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigInteger;
import java.util.Set;

@SqlResultSetMapping(
        name = "sensorCategoryMapping",
        classes = {
                @ConstructorResult(
                        targetClass = CategoryDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = String.class),
                                @ColumnResult(name = "name", type = String.class),
                                @ColumnResult(name = "iconUrl", type = String.class),
                                @ColumnResult(name = "displayName", type = String.class),
                                @ColumnResult(name = "creationTimestamp", type = BigInteger.class),
                                @ColumnResult(name = "subCategoryCount", type = String.class)

                        }
                )
        }
)

@NamedNativeQuery(
        name = "SensorCategory.getAllSensorCategory",
        query = "SELECT c.id, c.name, c.icon_url AS iconUrl, c.display_name AS displayName, c.creation_timestamp AS creationTimestamp, " +
                "(SELECT COUNT(*) FROM sensor_subcategory sc WHERE sc.sensor_category_id = c.id) AS subCategoryCount " +
                "FROM sensor_category c " +
                "WHERE (?1 = 'all' OR CONCAT_WS('',c.display_name) LIKE CONCAT('%',?1,'%')) " +
                "ORDER BY " +
                "CASE " +
                "   WHEN ?2 = 'creation_timestamp' THEN c.creation_timestamp " +
                "   ELSE NULL " +
                "END DESC, " +
                "CASE " +
                "   WHEN ?2 = 'name' THEN c.name " +
                "   ELSE NULL " +
                "END ASC " +
                "LIMIT ?3 OFFSET ?4",
        resultSetMapping = "sensorCategoryMapping"
)

@NamedNativeQuery(
        name = "SensorCategory.getAllSensorName",
        query = "SELECT id, name, icon_url AS iconUrl, display_name AS displayName, creation_timestamp AS creationTimestamp FROM sensor_category",
        resultSetMapping = "sensorCategoryMapping"
)


@Entity
@Getter
@Setter
public class SensorCategory {

    @Id
    private String id;
    @Column(length = 128)
    private String name;
    @Column(columnDefinition = "TEXT")
    private String iconUrl;
    @Column(length = 128)
    private String display_name;

    private BigInteger creation_timestamp;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "sensorCategory")
    private Set<SensorSubcategory> sensorSubcategories;
}
