package io.sclera.controller.frontend;

import io.sclera.dto.BillingInfoDTO;
import io.sclera.dto.PropertySummaryDTO;
import io.sclera.dto.ResponseDTO;
import io.sclera.dto.TierCountSummaryDTO;
import io.sclera.service.BillingInfoService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/billingInfo")
public class BillingInfoController {

    private final BillingInfoService billingInfoService;

    @PostMapping("/user/{email}/addBillingInfoByOrgId/{orgId}")
    public ResponseEntity<BillingInfoDTO> addBillingInfoByOrgId(@RequestParam String loggedInUser,
                                                                   @PathVariable String email,
                                                                   @PathVariable String orgId,
                                                                   @RequestBody BillingInfoDTO billingInfoDTO,
                                                                   HttpServletRequest httpServletRequest)
    {
        return billingInfoService.addBillingInfoByOrgId(loggedInUser,email,orgId,billingInfoDTO,httpServletRequest);
    }

    @GetMapping("/user/{email}/getAllBillingInfoByOrgId")
    public ResponseEntity<List<BillingInfoDTO>> getAllBillingInfoByOrgId(@RequestParam String loggedInUser,
                                                                         @RequestParam(required = false,defaultValue = "all") String orgId,
                                                                         @RequestParam(required = false,defaultValue = "all") String status,
                                                                         @RequestParam BigInteger currentDate,
                                                                         @RequestParam(defaultValue = "1") @Min(1) @Max(1000) int pageno,
                                                                         @RequestParam(defaultValue = "100") @Min(1) @Max(1000) int pagesize,
                                                                         @PathVariable String email,
                                                                         HttpServletRequest httpServletRequest)
    {
        return billingInfoService.getAllBillingInfoByOrgId(pageno,pagesize,orgId,status,currentDate,loggedInUser,email,httpServletRequest);
    }

    @GetMapping("/user/{email}/getBillingInfoPropertySummaryByOrgId/{orgId}/billingId/{billingId}")
    public ResponseEntity<List<PropertySummaryDTO>> getBillingInfoPropertySummaryByOrgId(@RequestParam String loggedInUser,
                                                                            @PathVariable String email,
                                                                            @PathVariable String orgId,
                                                                            @PathVariable String billingId,
                                                                            @RequestParam(defaultValue = "1") @Min(1) @Max(1000) int pageno,
                                                                            @RequestParam(defaultValue = "100") @Min(1) @Max(1000) int pagesize,
                                                                            HttpServletRequest httpServletRequest)
    {
        return billingInfoService.getBillingInfoPropertySummaryByOrgId(loggedInUser,email,orgId,billingId,pageno,pagesize,httpServletRequest);
    }

    @GetMapping("/user/{email}/getPropertyTierSummaryByOrgId/{orgId}/billingId/{billingId}")
    public ResponseEntity<TierCountSummaryDTO> getPropertyTierSummaryByOrgId(@RequestParam String loggedInUser,
                                                                             @PathVariable String email,
                                                                             @PathVariable String orgId,
                                                                             @PathVariable String billingId,
                                                                             HttpServletRequest httpServletRequest)
    {
        return billingInfoService.getPropertyTierSummaryByOrgId(loggedInUser,email,orgId,billingId,httpServletRequest);
    }

    @PutMapping("/user/{email}/updateBillingInfoByBillingId/{billingId}")
    public ResponseEntity<BillingInfoDTO> updateBillingInfoByBillingId(@RequestParam String loggedInUser,
                                                                          @PathVariable String email,
                                                                          @PathVariable String billingId,
                                                                          @RequestBody BillingInfoDTO billingInfoDTO,
                                                                          HttpServletRequest httpServletRequest)
    {
        return billingInfoService.updateBillingInfoByBillingId(loggedInUser,email,billingId,billingInfoDTO,httpServletRequest);
    }

    @DeleteMapping("/user/{email}/deleteBillingInfoByBillingIds")
    public ResponseEntity<ResponseDTO> deleteBillingInfoByBillingIds(@RequestParam String loggedInUser,
                                                                    @RequestBody List<String> billingIds,
                                                                    @PathVariable String email,
                                                                    HttpServletRequest httpServletRequest)
    {
        return billingInfoService.deleteBillingInfoByBillingIds(loggedInUser,email,billingIds,httpServletRequest);
    }

}
