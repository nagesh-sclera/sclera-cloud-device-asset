package io.sclera.model;


import io.sclera.dto.CategoryDTO;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigInteger;


@SqlResultSetMapping(
        name = "assetTypeMapping",
        classes = {
                @ConstructorResult(
                        targetClass = CategoryDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = String.class),
                                @ColumnResult(name = "name", type = String.class),
                                @ColumnResult(name = "iconUrl", type = String.class),
                                @ColumnResult(name = "displayName", type = String.class),
                                @ColumnResult(name = "creationTimestamp", type = BigInteger.class)
                        }
                )
        }
)

@SqlResultSetMapping(
        name = "assetTypeGroupMapping",
        classes = {
                @ConstructorResult(
                        targetClass = CategoryDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = String.class),
                                @ColumnResult(name = "name", type = String.class),
                                @ColumnResult(name = "iconUrl", type = String.class),
                                @ColumnResult(name = "displayName", type = String.class),
                                @ColumnResult(name = "creationTimestamp", type = BigInteger.class),
                                @ColumnResult(name = "assetTypeGroupName", type = String.class),
                                @ColumnResult(name = "assetGroupId", type = String.class)


                        }
                )
        }
)

@SqlResultSetMapping(
        name = "assetTypeUrlMapping",
        classes = {
                @ConstructorResult(
                        targetClass = CategoryDTO.class,
                        columns = {
                                @ColumnResult(name = "iconUrl", type = String.class),
                                @ColumnResult(name = "assetTypeGroupName", type = String.class)

                        }
                )
        }
)

@NamedNativeQuery(
        name = "AssetType.getAssetType",
        query = "SELECT sc.id, sc.name, sc.icon_url AS iconUrl,sc.display_name AS displayName, sc.creation_timestamp AS creationTimestamp, " +
                "sc.asset_type_group_name AS assetTypeGroupName, sc.id AS assetGroupId " +
                "FROM asset_type sc " +
                "LEFT JOIN asset_type_group c ON c.name = sc.asset_type_group_name " +
                "WHERE ((?1 = 'all' OR sc.asset_type_group_name = ?1) " +
                "AND (?2 = 'all' OR CONCAT_WS('',c.name,sc.name,sc.display_name) LIKE CONCAT('%',?2,'%'))) " +
                "ORDER BY " +
                "CASE " +
                "   WHEN ?3 = 'creation_timestamp' THEN sc.creation_timestamp " +
                "   ELSE NULL " +
                "END DESC, " +
                "CASE " +
                "   WHEN ?3 = 'name' THEN sc.name " +
                "   ELSE NULL " +
                "END ASC " +
                "LIMIT ?4 OFFSET ?5",
        resultSetMapping = "assetTypeGroupMapping"
)

@NamedNativeQuery(
        name = "AssetType.getAssetTypeByIds",
        query = "SELECT at.icon_url AS iconUrl, at.asset_type_group_name AS assetTypeGroupName " +
                "FROM asset_type at " +
                "WHERE at.id IN ?1",
        resultSetMapping = "assetTypeUrlMapping"
)


@NamedNativeQuery(
        name = "AssetType.getAllAssetTypes",
        query = "SELECT at.id, at.name, at.icon_url AS iconUrl, at.display_name AS displayName, at.creation_timestamp AS creationTimestamp " +
                "FROM asset_type at " +
                "WHERE (?1 = 'all' OR CONCAT_WS('',at.name, at.display_name) LIKE CONCAT('%',?1,'%')) " +
                "ORDER BY " +
                "CASE " +
                "   WHEN ?2 = 'creation_timestamp' THEN at.creation_timestamp " +
                "   ELSE NULL " +
                "END DESC, " +
                "CASE " +
                "   WHEN ?2 = 'name' THEN at.name " +
                "   ELSE NULL " +
                "END ASC",
        resultSetMapping = "assetTypeMapping"
)

@SqlResultSetMapping(
        name = "updatedAssetTypeGroupMapping",
        classes = {
                @ConstructorResult(
                        targetClass = CategoryDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = String.class),
                                @ColumnResult(name = "name", type = String.class),
                                @ColumnResult(name = "iconUrl", type = String.class),
                                @ColumnResult(name = "displayName", type = String.class),
                                @ColumnResult(name = "creationTimestamp", type = BigInteger.class),
                                @ColumnResult(name = "assetTypeGroupName", type = String.class),
                                @ColumnResult(name = "assetGroupId", type = String.class),
                                @ColumnResult(name = "updatedTimestamp", type = BigInteger.class)
                        }
                )
        }
)

@NamedNativeQuery(
        name = "AssetType.getUpdatedAssetType",
        query = "SELECT sc.id, sc.name, sc.icon_url AS iconUrl,sc.display_name AS displayName, sc.creation_timestamp AS creationTimestamp, " +
                "sc.asset_type_group_name AS assetTypeGroupName, sc.id AS assetGroupId,sc.updated_timestamp AS updatedTimestamp " +
                "FROM asset_type sc " +
                "LEFT JOIN asset_type_group c ON c.name = sc.asset_type_group_name " +
                "WHERE ((?1 = 'all' OR sc.asset_type_group_name = ?1) " +
                "AND updated_timestamp >= ?6 " +
                "AND (?2 = 'all' OR CONCAT_WS('',c.name,sc.name,sc.display_name) LIKE CONCAT('%',?2,'%'))) " +
                "ORDER BY " +
                "CASE " +
                "   WHEN ?3 = 'creation_timestamp' THEN sc.creation_timestamp " +
                "   ELSE NULL " +
                "END DESC, " +
                "CASE " +
                "   WHEN ?3 = 'name' THEN sc.name " +
                "   ELSE NULL " +
                "END ASC " +
                "LIMIT ?4 OFFSET ?5",
        resultSetMapping = "updatedAssetTypeGroupMapping"
)


@Entity
@Getter
@Setter
public class AssetType {

    @Id
    private String id;
    @Column(length = 128)
    private String name;
    @Column(columnDefinition = "TEXT")
    private String iconUrl;
    @Column(length = 128)
    private String display_name;
    private BigInteger creation_timestamp;
    private BigInteger updated_timestamp;

    @ManyToOne
    @JoinColumn(referencedColumnName = "name", name = "asset_type_group_name")
    private AssetTypeGroup assetTypeGroup;

}
