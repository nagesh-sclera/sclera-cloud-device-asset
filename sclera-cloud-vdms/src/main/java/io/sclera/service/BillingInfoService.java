package io.sclera.service;

import io.sclera.dto.*;
import io.sclera.exception.ClientException;
import io.sclera.repository.BillingInfoRepository;
import io.sclera.util.ScleraUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.*;


@Service
@RequiredArgsConstructor
@Slf4j
public class BillingInfoService {

    private final BillingInfoRepository billingInfoRepository;

    private final VdmsService vdmsService;

    private final BillingSelectedVdmsService billingSelectedVdmsService;

    private final WebClientService webClientService;

    public ResponseEntity<BillingInfoDTO> addBillingInfoByOrgId(String loggedInUser, String email, String orgId, BillingInfoDTO billingInfoDTO, HttpServletRequest httpServletRequest)
    {
        if(loggedInUser==null || email==null || orgId==null)
        {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
        }

        int isDateValid=billingInfoDTO.getBillingStartDate().compareTo(billingInfoDTO.getBillingEndDate());

        if(isDateValid > 0)
        {
            throw new ClientException("End date cannot be less than start date",700,httpServletRequest.getRequestURI());
        }

        if(billingInfoDTO.getSelectedVdms() == null)
        {
            throw new ClientException("Selected vdms is mandatory",700,httpServletRequest.getRequestURI());
        }

        if(billingInfoDTO.getBillingStartDate() == null || billingInfoDTO.getBillingEndDate() == null)
        {
            throw new ClientException("Billing dates cannot be null",700,httpServletRequest.getRequestURI());
        }

        String billingId = UUID.randomUUID().toString();
        BigInteger creationTime = BigInteger.valueOf(System.currentTimeMillis());
        billingInfoRepository.addBillingInfo(billingId,
                orgId,
                billingInfoDTO.getTotalLicensedAssets(),
                billingInfoDTO.getInvoiceNumber(),
                billingInfoDTO.getBillingContact(),
                billingInfoDTO.getBillingStartDate(),
                billingInfoDTO.getBillingEndDate(),
                billingInfoDTO.getSaasTerm(),
                loggedInUser,
                loggedInUser,
                creationTime, billingInfoDTO.getPoTracking(), billingInfoDTO.getCurrencyId(), billingInfoDTO.getTrial());

        String selectedVdms = billingInfoDTO.getSelectedVdms();
        List<String> vdmsIds = Arrays.asList(selectedVdms.split(","));

        for (String vdmsId : vdmsIds) {
            String billingSelectedVdmsId = UUID.randomUUID().toString();
            billingSelectedVdmsService.addBillingSelectedVdms(billingSelectedVdmsId, billingId, vdmsId);
        }

        return ResponseEntity.ok(billingInfoDTO);
    }

    public ResponseEntity<List<BillingInfoDTO>> getAllBillingInfoByOrgId(int pageNo, int pageSize, String orgId,String status,BigInteger currentDate,String loggedInUser, String email, HttpServletRequest httpServletRequest) {
        if (loggedInUser == null || email == null || orgId == null || currentDate == null) {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
        }

        int offset = ScleraUtils.calculateOffset(pageNo, pageSize);

        List<BillingInfoDTO> billingInfos = billingInfoRepository.findAllByOrgId(orgId,currentDate,status,pageSize, offset);

        for(BillingInfoDTO billingInfoDTO:billingInfos)
        {
            String selectedVdms=billingSelectedVdmsService.getSelectedVdms(billingInfoDTO.getBillingId());
            billingInfoDTO.setSelectedVdms(selectedVdms);
            List<String> selectedVdmsList = getSelectedVdmsList(selectedVdms,billingInfoDTO.getOrgId());
            Integer totalOnboardedAssets=vdmsService.getTotalOnboardedAssets(selectedVdmsList,billingInfoDTO.getOrgId());
            billingInfoDTO.setTotalOnboardedAssets(totalOnboardedAssets);
            if(billingInfoDTO.getTotalLicensedAssets() == null)
            {
                billingInfoDTO.setTotalBalanceAssets(null);
            }
            else
            {
                billingInfoDTO.setTotalBalanceAssets(billingInfoDTO.getTotalLicensedAssets() - billingInfoDTO.getTotalOnboardedAssets());
            }
        }

        List<String> orgIds = billingInfos.stream()
                                          .map(BillingInfoDTO::getOrgId)
                                          .toList();

        List<OrganisationDTO> orgNameAndIcons=webClientService.getOrganisationNameAndIconUrlByOrgIds(orgIds,loggedInUser,httpServletRequest);

        //add icon url and org name
        billingInfos.forEach(billingInfo ->
                orgNameAndIcons.stream()
                        .filter(org -> org.getId().equals(billingInfo.getOrgId())) // Find matching orgId
                        .findFirst() // Get the first match
                        .ifPresent(org -> {
                            billingInfo.setOrgName(org.getName());
                            billingInfo.setIconUrl(org.getOrganisation_image_url());
                        })
        );

        return ResponseEntity.ok(billingInfos);
    }

    public ResponseEntity<BillingInfoDTO> updateBillingInfoByBillingId(String loggedInUser, String email, String billingId, BillingInfoDTO billingInfoDTO, HttpServletRequest httpServletRequest) {
        if (loggedInUser == null || email == null || billingId == null) {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
        }

        billingInfoRepository.updateBillingInfoByBillingId(billingId, billingInfoDTO.getTotalLicensedAssets(),
                billingInfoDTO.getInvoiceNumber(),
                billingInfoDTO.getBillingContact(),
                billingInfoDTO.getBillingStartDate(),
                billingInfoDTO.getBillingEndDate(),
                billingInfoDTO.getSaasTerm(),
                loggedInUser, billingInfoDTO.getPoTracking(), billingInfoDTO.getCurrencyId(), billingInfoDTO.getTrial());

        billingSelectedVdmsService.updatedSelectedVdms(billingId,billingInfoDTO.getSelectedVdms());

        return ResponseEntity.ok(billingInfoDTO);

    }

    public ResponseEntity<ResponseDTO> deleteBillingInfoByBillingIds(String loggedInUser, String email,List<String> billingIds, HttpServletRequest httpServletRequest) {
        if (loggedInUser == null || email == null || billingIds == null) {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
        }
        billingSelectedVdmsService.deleteSelectedVdms(billingIds);
        billingInfoRepository.deleteBillingInfoByBillingIds(billingIds);
        ResponseDTO responseDTO = ScleraUtils.generatePayload("BillingInfo Deleted successfully", 200, true);
        return ResponseEntity.ok(responseDTO);
    }


    public ResponseEntity<List<PropertySummaryDTO>> getBillingInfoPropertySummaryByOrgId(String loggedInUser, String email, String orgId,String billingId,int pageno,int pagesize,HttpServletRequest httpServletRequest)
    {
        if(loggedInUser==null || email==null || orgId==null || billingId==null)
        {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
        }
        int offset = ScleraUtils.calculateOffset(pageno, pagesize);

        String selectedVdms=billingSelectedVdmsService.getSelectedVdms(billingId);

        List<String> selectedVdmsList=getSelectedVdmsList(selectedVdms,orgId);

        List<PropertySummaryDTO> propertySummary=vdmsService.getBillingInfoPropertySummaryByOrgId(orgId,selectedVdmsList,pagesize,offset);

        return ResponseEntity.ok(propertySummary);
    }

    public List<String> getSelectedVdmsList(String selectedVdms,String orgId)
    {
        List<String> selectedVdmsList = new ArrayList<>();
        if (selectedVdms.equalsIgnoreCase("all"))
        {
            selectedVdmsList=vdmsService.getVdmsIdListByOrgId(orgId);
        }
        else
        {
            selectedVdmsList = selectedVdms != null ? Arrays.asList(selectedVdms.split(",")) : Collections.emptyList();
        }

        return selectedVdmsList;
    }


    public ResponseEntity<TierCountSummaryDTO> getPropertyTierSummaryByOrgId(String loggedInUser, String email, String orgId,String billingId,HttpServletRequest httpServletRequest)
    {
        if(loggedInUser==null || email==null || orgId==null || billingId == null)
        {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
        }

        String selectedVdms=billingSelectedVdmsService.getSelectedVdms(billingId);
        List<String> selectedVdmsList = getSelectedVdmsList(selectedVdms,orgId);
        int selectedVdmsCount=selectedVdmsList.size();

        if(selectedVdmsCount == 0)
        {
            Integer totalLicensedAssets= billingInfoRepository.getTotalLicensedAssets(billingId);
            TierCountSummaryDTO tierCountSummaryDTO=new TierCountSummaryDTO();
            tierCountSummaryDTO.setTotalLicensedAssets(totalLicensedAssets);
            tierCountSummaryDTO.setTotalOnboardedAssets(0);
            tierCountSummaryDTO.setTotalBalanceAssets(totalLicensedAssets);
            return ResponseEntity.ok(tierCountSummaryDTO);
        }

        TierCountSummaryDTO tierCountSummaryDTO=vdmsService.getPropertyTierSummaryByOrgId(orgId,selectedVdmsList,billingId);

        if(tierCountSummaryDTO.getTotalLicensedAssets() == null)
        {
            tierCountSummaryDTO.setTotalBalanceAssets(null);
        }
        else
        {
            tierCountSummaryDTO.setTotalBalanceAssets(tierCountSummaryDTO.getTotalLicensedAssets() - tierCountSummaryDTO.getTotalOnboardedAssets());
        }
        tierCountSummaryDTO.setSelectedVdmsCount(selectedVdmsCount);
        return ResponseEntity.ok(tierCountSummaryDTO);
    }

    public BillingInfoDTO findBillingDataByOrgId(String orgId) {
        return billingInfoRepository.findBillingDataByOrgId(orgId);
    }
}
