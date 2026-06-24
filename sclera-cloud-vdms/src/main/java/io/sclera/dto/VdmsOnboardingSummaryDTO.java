package io.sclera.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VdmsOnboardingSummaryDTO {

    private String vdms_onboarding_summary_id;
    private BigInteger total_assets_onboarded;
    private BigInteger total_locations_onboarded;
    private BigInteger total_qr_codes_onboarded;
    private String vdms_id;
    private String property_name;

}
