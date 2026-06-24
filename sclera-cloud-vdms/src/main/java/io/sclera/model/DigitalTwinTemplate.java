package io.sclera.model;

import io.sclera.dto.DigitalTwinTemplateDTO;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@SqlResultSetMapping(
        name = "digitalTwinTemplateListMapping",
        classes = {
                @ConstructorResult(
                        targetClass = DigitalTwinTemplateDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = String.class),
                                @ColumnResult(name = "name", type = String.class),
                                @ColumnResult(name = "description", type = String.class),
                                @ColumnResult(name = "imageUrl", type = String.class),
                                @ColumnResult(name = "vdmsId", type = String.class),
                                @ColumnResult(name = "categoryId", type = String.class),
                                @ColumnResult(name = "categoryName", type = String.class),
                                @ColumnResult(name = "subCategoryId", type = String.class),
                                @ColumnResult(name = "subCategoryName", type = String.class)
                        }
                )
        }
)

@NamedNativeQuery(
        name = "DigitalTwinTemplate.getDigitalTwinTemplateListByVdmsId",
        query = "SELECT dtt.id, dtt.name, dtt.description, dtt.image_url AS imageUrl, dtt.vdms_id AS vdmsId, " +
                "c.id AS categoryId, c.name AS categoryName, sc.id AS subCategoryId, sc.name AS subCategoryName " +
                "FROM digital_twin_template dtt " +
                "JOIN asset_sub_category sc ON sc.id = dtt.asset_sub_category_id " +
                "JOIN asset_category c ON c.id = sc.asset_category_id " +
                "WHERE ((dtt.vdms_id = ?1) " +
                "AND (?2 = 'all' OR CONCAT_WS('',dtt.name) LIKE CONCAT('%',?2,'%')) " +
                "AND (?3 = 'all' OR c.id = ?3) " +
                "AND (?4 = 'all' OR sc.id = ?4)) ORDER BY dtt.name " +
                "LIMIT ?5 OFFSET ?6",
        resultSetMapping = "digitalTwinTemplateListMapping"
)

@NamedNativeQuery(
        name = "DigitalTwinTemplate.getDigitalTwinTemplateDetailsByVdmsId",
        query = "SELECT dtt.id, dtt.name, dtt.description, dtt.image_url AS imageUrl, dtt.vdms_id AS vdmsId, " +
                "c.id AS categoryId, c.name AS categoryName, sc.id AS subCategoryId, sc.name AS subCategoryName " +
                "FROM digital_twin_template dtt " +
                "JOIN asset_sub_category sc ON sc.id = dtt.asset_sub_category_id " +
                "JOIN asset_category c ON c.id = sc.asset_category_id " +
                "WHERE dtt.vdms_id = ?1 AND dtt.id = ?2",
        resultSetMapping = "digitalTwinTemplateListMapping"
)

@Entity
@Getter
@Setter
public class DigitalTwinTemplate {

    @Id
    private String id;
    @Column(length = 128)
    private String name;
    @Column(columnDefinition = "TEXT")
    private String description;
    @Column(columnDefinition = "TEXT")
    private String imageUrl;
    @OneToOne(cascade = CascadeType.ALL)
    private Vdms vdms;
    @OneToOne(cascade = CascadeType.ALL)
    private AssetSubCategory assetSubCategory;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "digitalTwinTemplate")
    private Set<DigitalTwinMeasuringInstrument> digitalTwinMeasuringInstruments;
}
