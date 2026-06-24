package io.sclera.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import io.sclera.dto.OrganisationOnboardingSummaryDTO;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigInteger;

@Entity
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = OrganisationOnboardingSummary.class)




@SqlResultSetMapping(
        name = "organisationOnboardingSummaryMapping",
        classes = {
                @ConstructorResult(
                        targetClass = OrganisationOnboardingSummaryDTO.class,
                        columns = {
                                @ColumnResult(name = "organisation_onboarding_summary_id", type = String.class),
                                @ColumnResult(name = "organisation_image_url", type = String.class),
                                @ColumnResult(name = "total_licensed_assets", type = BigInteger.class),
                                @ColumnResult(name = "total_onboarded_assets", type = BigInteger.class),
                                @ColumnResult(name = "total_balance_assets", type = BigInteger.class),
                                @ColumnResult(name = "customer_organisation_id", type = String.class),
                                @ColumnResult(name = "organisation_name", type = String.class),
                                @ColumnResult(name = "organisation_id", type = String.class)
                        }
                )
        }
)


@NamedNativeQuery(
        name = "OrganisationOnboardingSummary.getOrganisationOnboardingSummaryByOrganisationId",
        query = "SELECT oos.id AS organisation_onboarding_summary_id ,oos.organisation_image_url ,oos.total_licensed_assets ," +
                "oos.total_onboarded_assets ,oos.total_balance_assets ,oos.customer_organisation_id ,co.id AS organisation_id ,co.company_name AS organisation_name  " +
                "FROM organisation_onboarding_summary oos LEFT JOIN customer_organisation co ON co.id = oos.customer_organisation_id WHERE oos.customer_organisation_id = ?1 ",
        resultSetMapping = "organisationOnboardingSummaryMapping"
)






@Data
public class OrganisationOnboardingSummary {

    @Id
    private String id;


    @Column(columnDefinition = "TEXT")
    private String organisation_image_url;


    @Column(columnDefinition = "bigint default 0")
    private BigInteger total_licensed_assets;


    @Column(columnDefinition = "bigint default 0")
    private BigInteger total_onboarded_assets;


    @Column(columnDefinition = "bigint default 0")
    private BigInteger total_balance_assets;


    @OneToOne(cascade = CascadeType.ALL)
    private Customer_Organisation customerOrganisation;


}
