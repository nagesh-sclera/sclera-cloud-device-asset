package io.sclera.models;

import io.sclera.dto.AssetFieldDTO;
import org.hibernate.annotations.ColumnDefault;

import javax.persistence.*;
import java.math.BigInteger;

@SqlResultSetMapping(
        name = "assetFieldMapping",
        classes = @ConstructorResult(
                targetClass = AssetFieldDTO.class,
                columns = {
                        @ColumnResult(name = "id", type = String.class),
                        @ColumnResult(name = "name", type = String.class)
                }
        )
)
@NamedNativeQuery(
        name = "AssetField.getGlobalAssetFields",
        query = "SELECT id, name " +
                "FROM asset_field WHERE name IN ?1",
        resultSetMapping = "assetFieldMapping"
)

@SqlResultSetMapping(
        name = "assetFieldMappings",
        classes = @ConstructorResult(
                targetClass = AssetFieldDTO.class,
                columns = {
                        @ColumnResult(name = "id", type = String.class),
                        @ColumnResult(name = "name", type = String.class),
                        @ColumnResult(name = "type", type = String.class),
                        @ColumnResult(name = "toolTip", type = String.class),
                        @ColumnResult(name = "defaultValue", type = String.class),
                        @ColumnResult(name = "isActive", type = Boolean.class),
                        @ColumnResult(name = "options", type = String.class),
                        @ColumnResult(name = "isDeleted", type = Boolean.class),
                        @ColumnResult(name = "showInSection", type = Integer.class),
                        @ColumnResult(name = "createdAt", type = BigInteger.class)

                }
        )
)
@NamedNativeQuery(
        name = "AssetField.getAllAssetFields",
        query = " SELECT id, name, type, tool_tip AS toolTip , default_value AS defaultValue, is_active AS isActive, options, is_deleted AS isDeleted, show_in_section AS showInSection, created_at AS createdAt " +
                "FROM asset_field WHERE is_deleted = false ",
        resultSetMapping = "assetFieldMappings"
)
@Entity
public class AssetField {
    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "name")
    private String name;

    @Column(name = "type")
    private String type;

    @Column(name = "tool_tip")
    private String toolTip;

    @Column(name = "default_value")
    private String defaultValue;

    @Column(name = "is_active")
    @ColumnDefault("true")
    private Boolean isActive;

    @Column(name = "options", columnDefinition = "TEXT")
    private String options;

    @Column(name = "is_deleted")
    @ColumnDefault("false")
    private Boolean isDeleted;

    @Column(name = "show_in_section", columnDefinition = "INT DEFAULT 0")
    private Integer showInSection;

    @Column(name = "created_at")
    private BigInteger createdAt;

}
