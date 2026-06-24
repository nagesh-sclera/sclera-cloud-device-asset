package io.sclera.model;

import io.sclera.dto.FeatureDTO;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@SqlResultSetMapping(
        name = "featureListMapping",
        classes = {
                @ConstructorResult(
                        targetClass = FeatureDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = String.class),
                                @ColumnResult(name = "name", type = String.class),
                                @ColumnResult(name = "image_url", type = String.class),
                                @ColumnResult(name = "deep_link_url", type = String.class)
                        }
                )
        }
)

@NamedNativeQuery(
        name = "Feature.getFeatureList",
        query = "SELECT id, name, image_url, deep_link_url " +
                "FROM feature",
        resultSetMapping = "featureListMapping"
)

@NamedNativeQuery(
        name = "Feature.getFeatureDetailsById",
        query = "SELECT id, name, image_url, deep_link_url " +
                "FROM feature WHERE id = ?1",
        resultSetMapping = "featureListMapping"
)

@NamedNativeQuery(
        name = "Feature.getVdmsFeatureByVdmsId",
        query = "SELECT f.id, f.name, f.image_url, f.deep_link_url " +
                "FROM feature f " +
                "LEFT JOIN vdms_feature vf ON vf.feature_id = f.id " +
                "LEFT JOIN vdms v ON v.id = vf.vdms_id " +
                "WHERE v.id = ?1",
        resultSetMapping = "featureListMapping"
)

@NamedNativeQuery(
        name = "Feature.getVdmsFeatureByOrgId",
        query = "SELECT DISTINCT f.id, f.name, f.image_url, f.deep_link_url " +
                "FROM feature f " +
                "LEFT JOIN vdms_feature vf ON vf.feature_id = f.id " +
                "LEFT JOIN vdms v ON v.id = vf.vdms_id " +
                "LEFT JOIN customer_organisation co ON co.id = v.customer_org_id " +
                "WHERE co.id = ?1",
        resultSetMapping = "featureListMapping"
)


@Entity
@Getter
@Setter
public class Feature {

    @Id
    @Column(length = 64)
    private String id;
    @Column(length = 64)
    private String name;
    @Column(length = 256)
    private String image_url;
    @Column(length = 256)
    private String deep_link_url;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "feature")
    private Set<VdmsFeature> vdmsFeatures;
}
