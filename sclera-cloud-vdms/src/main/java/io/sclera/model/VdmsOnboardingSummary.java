package io.sclera.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import io.sclera.dto.VdmsOnboardingSummaryDTO;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigInteger;

@Entity
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = VdmsOnboardingSummary.class)


@SqlResultSetMapping(
        name = "vdmsOnboardingSummaryMapping",
        classes = {
                @ConstructorResult(
                        targetClass = VdmsOnboardingSummaryDTO.class,
                        columns = {
                                @ColumnResult(name = "vdms_onboarding_summary_id", type = String.class),
                                @ColumnResult(name = "total_assets_onboarded", type = BigInteger.class),
                                @ColumnResult(name = "total_locations_onboarded", type = BigInteger.class),
                                @ColumnResult(name = "total_qr_codes_onboarded", type = BigInteger.class),
                                @ColumnResult(name = "vdms_id", type = String.class),
                                @ColumnResult(name = "property_name", type = String.class)
                        }
                )
        }
)


@NamedNativeQuery(
        name = "VdmsOnboardingSummary.getVdmsOnboardingSummaryByVdmsId",
        query = "SELECT vos.id AS vdms_onboarding_summary_id ,vos.total_assets_onboarded ,vos.total_locations_onboarded ," +
                "vos.total_qr_codes_onboarded ,v.id AS vdms_id ,v.property_name " +
                "FROM vdms_onboarding_summary vos LEFT JOIN vdms v ON vos.vdms_id = v.id WHERE vos.vdms_id = ?1 ",
        resultSetMapping = "vdmsOnboardingSummaryMapping"
)


@NamedNativeQuery(
        name = "VdmsOnboardingSummary.getVdmsOnboardingSummaryByOrganisationId",
        query = "SELECT vos.id AS vdms_onboarding_summary_id ,vos.total_assets_onboarded ,vos.total_locations_onboarded ," +
                "vos.total_qr_codes_onboarded ,v.id AS vdms_id ,v.property_name " +
                "FROM vdms_onboarding_summary vos LEFT JOIN vdms v ON vos.vdms_id = v.id WHERE v.customer_org_id = ?1 ",
        resultSetMapping = "vdmsOnboardingSummaryMapping"
)


@Data
public class VdmsOnboardingSummary {

    @Id
    private String id;


    @Column(columnDefinition = "bigint default 0")
    private BigInteger total_assets_onboarded;


    @Column(columnDefinition = "bigint default 0")
    private BigInteger total_locations_onboarded;


    @Column(columnDefinition = "bigint default 0")
    private BigInteger total_qr_codes_onboarded;


    @OneToOne(cascade = CascadeType.ALL)
    private Vdms vdms;

}
