package io.sclera.model;

import io.sclera.dto.CategoryDTO;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigInteger;
import java.util.Set;

@Entity

@SqlResultSetMapping(
        name = "categoryMapping",
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
        name = "AssetCategory.getAllCategory",
        query = "SELECT c.id, c.name, c.icon_url AS iconUrl, c.display_name AS displayName, c.creation_timestamp AS creationTimestamp, " +
                "(SELECT COUNT(*) FROM asset_sub_category sc WHERE sc.asset_category_id = c.id) AS subCategoryCount " +
                "FROM asset_category c " +
                "WHERE (?1 = 'all' OR CONCAT_WS('',c.name,c.display_name) LIKE CONCAT('%',?1,'%')) " +
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
        resultSetMapping = "categoryMapping"
)


@Getter
@Setter
public class AssetCategory {

    @Id
    private String id;
    @Column(length = 128)
    private String name;
    @Column(columnDefinition = "TEXT")
    private String iconUrl;
    @Column(length = 128)
    private String display_name;
    private BigInteger creation_timestamp;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "assetCategory")
    private Set<AssetSubCategory> subCategories;

}
