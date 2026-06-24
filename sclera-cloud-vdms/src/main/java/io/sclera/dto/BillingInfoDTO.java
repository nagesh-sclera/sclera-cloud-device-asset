package io.sclera.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.math.BigInteger;


@RequiredArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BillingInfoDTO {

    private String billingId;

    private String orgId;

    private String orgName;

    private String iconUrl;

    private Integer totalLicensedAssets;

    private Integer totalOnboardedAssets;

    private Integer totalBalanceAssets;

    private String invoiceNumber;

    private String billingContact;

    private BigInteger billingStartDate;

    private BigInteger billingEndDate;

    private Integer saasTerm;

    private String status;

    private String createdBy;

    private String updatedBy;

    private BigInteger creationTime;

    private String selectedVdms;

    private Integer poTracking;

    private String currencyCode;

    private Integer trial;

    private String currencyId;

    public BillingInfoDTO(String billingId, String orgId, String orgName, String iconUrl, Integer totalLicensedAssets, Integer totalOnboardedAssets,
                          Integer totalBalanceAssets, String invoiceNumber, String billingContact, BigInteger billingStartDate, BigInteger billingEndDate,
                          Integer saasTerm, String status, String createdBy, String updatedBy, BigInteger creationTime, String selectedVdms,
                          Integer poTracking, String currencyCode, Integer trial, String currencyId) {
        this.billingId = billingId;
        this.orgId = orgId;
        this.orgName = orgName;
        this.iconUrl = iconUrl;
        this.totalLicensedAssets = totalLicensedAssets;
        this.totalOnboardedAssets = totalOnboardedAssets;
        this.totalBalanceAssets = totalBalanceAssets;
        this.invoiceNumber = invoiceNumber;
        this.billingContact = billingContact;
        this.billingStartDate = billingStartDate;
        this.billingEndDate = billingEndDate;
        this.saasTerm = saasTerm;
        this.status = status;
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
        this.creationTime = creationTime;
        this.selectedVdms = selectedVdms;
        this.poTracking = poTracking;
        this.currencyCode = currencyCode;
        this.trial = trial;
        this.currencyId = currencyId;
    }

    public BillingInfoDTO(String billingId, String orgId, String orgName, String iconUrl, Integer totalLicensedAssets, Integer totalOnboardedAssets,
                          Integer totalBalanceAssets, String invoiceNumber, String billingContact, BigInteger billingStartDate, BigInteger billingEndDate,
                          Integer saasTerm,  String createdBy, String updatedBy, BigInteger creationTime, String selectedVdms,
                          Integer poTracking, String currencyCode, Integer trial, String currencyId) {
        this.billingId = billingId;
        this.orgId = orgId;
        this.orgName = orgName;
        this.iconUrl = iconUrl;
        this.totalLicensedAssets = totalLicensedAssets;
        this.totalOnboardedAssets = totalOnboardedAssets;
        this.totalBalanceAssets = totalBalanceAssets;
        this.invoiceNumber = invoiceNumber;
        this.billingContact = billingContact;
        this.billingStartDate = billingStartDate;
        this.billingEndDate = billingEndDate;
        this.saasTerm = saasTerm;
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
        this.creationTime = creationTime;
        this.selectedVdms = selectedVdms;
        this.poTracking = poTracking;
        this.currencyCode = currencyCode;
        this.trial = trial;
        this.currencyId = currencyId;
    }

}
