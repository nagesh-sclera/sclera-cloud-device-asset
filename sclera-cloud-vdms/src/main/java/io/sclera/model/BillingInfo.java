package io.sclera.model;

import io.sclera.dto.BillingInfoDTO;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity

@SqlResultSetMapping(
        name = "BillingInfoDTOMapper",
        classes = {
                @ConstructorResult(
                        targetClass = BillingInfoDTO.class,
                        columns = {
                                @ColumnResult(name = "billingId", type = String.class),
                                @ColumnResult(name = "orgId", type = String.class),
                                @ColumnResult(name = "totalLicensedAssets", type = Integer.class),
                                @ColumnResult(name = "invoiceNumber", type = String.class),
                                @ColumnResult(name = "billingContact", type = String.class),
                                @ColumnResult(name = "billingStartDate", type = BigInteger.class),
                                @ColumnResult(name = "billingEndDate", type = BigInteger.class),
                                @ColumnResult(name = "saasTerm", type = Integer.class),
                                @ColumnResult(name = "createdBy", type = String.class),
                                @ColumnResult(name = "updatedBy", type = String.class),
                                @ColumnResult(name = "creationTime", type = BigInteger.class),
                                @ColumnResult(name = "status", type = String.class),
                                @ColumnResult(name = "poTracking",type = Integer.class),
                                @ColumnResult(name = "currencyCode",type = String.class),
                                @ColumnResult(name = "trial",type = Integer.class),
                                @ColumnResult(name = "currencyId",type = String.class)
                        }
                )
        }
)

@NamedNativeQuery(
        name = "BillingInfo.findAllByOrgId",
        query = """
                SELECT billingId,orgId,totalLicensedAssets,invoiceNumber,billingContact,billingStartDate,billingEndDate,saasTerm,createdBy,
                updatedBy,creationTime,status,poTracking,currencyCode,trial,currencyId
                FROM (
                    SELECT b.billing_id AS billingId, 
                           b.org_id AS orgId, 
                           b.total_licensed_assets AS totalLicensedAssets, 
                           b.invoice_number AS invoiceNumber, 
                           b.billing_contact AS billingContact, 
                           b.billing_start_date AS billingStartDate, 
                           b.billing_end_date AS billingEndDate,
                           b.saas_term AS saasTerm,
                           b.created_by AS createdBy, 
                           b.updated_by AS updatedBy, 
                           b.creation_time AS creationTime,
                           b.po_tracking AS poTracking, 
                           c.code AS currencyCode, 
                           b.trial,
                           b.currency_id AS currencyId,
                           CASE 
                               WHEN ?2 BETWEEN b.billing_start_date AND (b.billing_end_date + 24*60*60*1000-1)
                               THEN 'active' 
                               ELSE 'inactive' 
                           END AS status
                    FROM billing_info b LEFT JOIN currency c ON c.id = b.currency_id
                    WHERE (?1 = 'all' OR b.org_id = ?1)
                ) subquery
                WHERE (?3 = 'all' OR status = ?3)
                LIMIT ?4 OFFSET ?5
                """,
        resultSetMapping = "BillingInfoDTOMapper"
)


@SqlResultSetMapping(
        name = "TouchScreenBillingInfoDTOMapper",
        classes = {
                @ConstructorResult(
                        targetClass = BillingInfoDTO.class,
                        columns = {
                                @ColumnResult(name = "billingId", type = String.class),
                                @ColumnResult(name = "orgId", type = String.class),
                                @ColumnResult(name = "totalLicensedAssets", type = Integer.class),
                                @ColumnResult(name = "invoiceNumber", type = String.class),
                                @ColumnResult(name = "billingContact", type = String.class),
                                @ColumnResult(name = "billingStartDate", type = BigInteger.class),
                                @ColumnResult(name = "billingEndDate", type = BigInteger.class),
                                @ColumnResult(name = "saasTerm", type = Integer.class),
                                @ColumnResult(name = "createdBy", type = String.class),
                                @ColumnResult(name = "updatedBy", type = String.class),
                                @ColumnResult(name = "creationTime", type = BigInteger.class),
                                @ColumnResult(name = "poTracking",type = Integer.class),
                                @ColumnResult(name = "currencyCode",type = String.class),
                                @ColumnResult(name = "trial",type = Integer.class),
                                @ColumnResult(name = "currencyId",type = String.class)
                        }
                )
        }
)

@NamedNativeQuery(
        name = "BillingInfo.findBillingDataByOrgId",
        query = "SELECT b.billing_id AS billingId,b.org_id AS orgId,b.total_licensed_assets AS totalLicensedAssets, b.invoice_number AS invoiceNumber, " +
                "b.billing_contact AS billingContact,b.billing_start_date AS billingStartDate,b.billing_end_date AS billingEndDate, " +
                "b.saas_term AS saasTerm,b.created_by AS createdBy,b.updated_by AS updatedBy,b.creation_time AS creationTime, " +
                "b.po_tracking AS poTracking,c.code AS currencyCode,b.trial,b.currency_id AS currencyId FROM billing_info b LEFT JOIN currency c ON c.id = b.currency_id " +
                "WHERE b.org_id = ?1",
        resultSetMapping = "TouchScreenBillingInfoDTOMapper"
)


@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "billing_info")
public class BillingInfo {

    @Id
    private String billingId;

    private String orgId;

    private Integer totalLicensedAssets;

    private String invoiceNumber;

    private String billingContact;

    private BigInteger billingStartDate;

    private BigInteger billingEndDate;

    private Integer saasTerm;

    private BigInteger creationTime;

    private String createdBy;

    private String updatedBy;

    @Column(columnDefinition = "boolean default false")
    private Boolean poTracking;

    private String currencyId;

    @Column(columnDefinition = "boolean default false")
    private Boolean trial;

    @OneToMany(mappedBy = "billingInfo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BillingSelectedVdms> selectedVdms = new ArrayList<>();

}
