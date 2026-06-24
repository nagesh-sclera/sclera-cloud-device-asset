package io.sclera.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TierCountSummaryDTO {

    private Integer tierOneCount;

    private Integer tierTwoCount;

    private Integer tierThreeCount;

    private Integer tierTypeCount;

    private Integer totalOnboardedAssets;

    private Integer totalLicensedAssets;

    private Integer totalBalanceAssets;

    private Integer selectedVdmsCount;

}
