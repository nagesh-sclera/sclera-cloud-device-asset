package io.sclera.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrganisationOnboardingSummaryDTO {

    private String organisation_onboarding_summary_id;
    private String status;
    private String organisation_image_url;
    private BigInteger total_licensed_assets;
    private BigInteger total_onboarded_assets;
    private BigInteger total_balance_assets;
    private String customer_organisation_id;
    private String organisation_name;
    private String organisation_id;
    private List<VdmsOnboardingSummaryDTO> vdmsOnboardingSummaryDTOS;


    public OrganisationOnboardingSummaryDTO(String organisation_onboarding_summary_id, String organisation_image_url,
                                            BigInteger total_licensed_assets, BigInteger total_onboarded_assets,
                                            BigInteger total_balance_assets, String customer_organisation_id,
                                            String organisation_name, String organisation_id) {
        this.organisation_onboarding_summary_id = organisation_onboarding_summary_id;
        this.organisation_image_url = organisation_image_url;
        this.total_licensed_assets = total_licensed_assets;
        this.total_onboarded_assets = total_onboarded_assets;
        this.total_balance_assets = total_balance_assets;
        this.customer_organisation_id = customer_organisation_id;
        this.organisation_name = organisation_name;
        this.organisation_id = organisation_id;
    }
}
