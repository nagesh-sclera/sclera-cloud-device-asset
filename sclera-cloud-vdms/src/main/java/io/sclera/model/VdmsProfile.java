package io.sclera.model;

import io.sclera.dto.VdmsDTO;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity

@SqlResultSetMapping(
        name = "vdmsProfileMapping",
        classes = {
                @ConstructorResult(
                        targetClass = VdmsDTO.class,
                        columns = {
                                @ColumnResult(name = "vdms_id", type = String.class),
                                @ColumnResult(name = "property_name", type = String.class),
                                @ColumnResult(name = "customer_org_id", type = String.class),
                                @ColumnResult(name = "vendor_org_id", type = String.class),
                                @ColumnResult(name = "profile_id", type = String.class),
                                @ColumnResult(name = "vdms_profile_id", type = String.class),
                                @ColumnResult(name = "profile_name", type = String.class)
                        }
                )
        }
)

@NamedNativeQuery(
        name = "VdmsProfile.getVdmsProfilesByCustomerOrganisationId",
        query = "SELECT v.id AS vdms_id ,v.property_name ,vp.customer_org_id ,vp.vendor_org_id ,p.id AS profile_id ,vp.id AS vdms_profile_id ,p.name AS profile_name "
                + "FROM vdms v "
                + "LEFT JOIN vdms_profile vp on v.customer_org_id = vp.customer_org_id AND v.id = vp.vdms_id "
                + "LEFT JOIN profile p on p.id = vp.profile_id "
                + "WHERE v.customer_org_id = ?1",
        resultSetMapping = "vdmsProfileMapping"
)


@NamedNativeQuery(
        name = "VdmsProfile.getVdmsProfilesByCustomerOrganisationIdAndEmail",
        query = "SELECT v.id AS vdms_id ,v.property_name ,vp.customer_org_id ,vp.vendor_org_id ,p.id AS profile_id ,vp.id AS vdms_profile_id ,p.name AS profile_name "
                + "FROM vdms v "
                + "LEFT JOIN vdms_profile vp ON v.customer_org_id = vp.customer_org_id AND v.id = vp.vdms_id "
                + "LEFT JOIN profile p ON p.id = vp.profile_id "
                + "LEFT JOIN vdms_visibility vv ON v.id = vv.vdms_id "
                + "WHERE v.customer_org_id = ?1 AND vv.email = ?2",
        resultSetMapping = "vdmsProfileMapping"
)


@NamedNativeQuery(
        name = "VdmsProfile.getVdmsProfilesByVendorOrganisationId",
        query = "SELECT DISTINCT d.vdms_id AS vdms_id ,v.property_name ,vp.customer_org_id ,d.vendor_org_id  , p.id AS profile_id ,vp.id AS vdms_profile_id ,p.name AS profile_name "
                + "FROM docker d "
                + "LEFT JOIN vdms_profile vp on vp.vendor_org_id = d.vendor_org_id	AND vp.vdms_id = d.vdms_id "
                + "LEFT JOIN vdms v on v.id = d.vdms_id "
                + "LEFT JOIN profile p on p.id = vp.profile_id "
                + "WHERE d.vendor_org_id = ?1",
        resultSetMapping = "vdmsProfileMapping"
)


@NamedNativeQuery(
        name = "VdmsProfile.getVdmsProfilesByVendorOrganisationIdAndEmail",
        query = "SELECT DISTINCT d.vdms_id AS vdms_id ,v.property_name ,vp.customer_org_id ,d.vendor_org_id  , p.id AS profile_id ,vp.id AS vdms_profile_id ,p.name AS profile_name "
                + "FROM docker d "
                + "LEFT JOIN vdms_profile vp on vp.vendor_org_id = d.vendor_org_id	AND vp.vdms_id = d.vdms_id "
                + "LEFT JOIN vdms v on v.id = d.vdms_id "
                + "LEFT JOIN profile p on p.id = vp.profile_id "
                + "LEFT JOIN vdms_visibility vv ON v.id = vv.vdms_id "
                + "WHERE d.vendor_org_id = ?1 AND vv.email = ?2",
        resultSetMapping = "vdmsProfileMapping"
)


@Getter
@Setter
public class VdmsProfile {

    @Id
    @Column(length = 64)
    private String id;

    @Column(length = 64)
    private String vdms_id;

    @Column(length = 64)
    private String profile_id;

    @Column(length = 64)
    private String customer_org_id;

    @Column(length = 64)
    private String vendor_org_id;


}
