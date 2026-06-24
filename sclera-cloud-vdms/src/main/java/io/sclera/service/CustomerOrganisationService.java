package io.sclera.service;

import io.sclera.dto.CustomerOrganisationDto;
import io.sclera.dto.FeatureDTO;
import io.sclera.dto.ResponseDTO;
import io.sclera.repository.CustomerOrganisationRepository;
import io.sclera.util.ScleraUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

@Service
@Slf4j
public class CustomerOrganisationService {

    @Autowired
    private CustomerOrganisationRepository customerorganisationRepository;

    @Autowired
    private ProxyProfileService proxyProfileService;

    @Autowired
    private FeatureService featureService;

    public void addCustomerOrganisationById(String customer_org_id, String company_name, Integer is_enterprise, HttpServletRequest httpServletRequest) {
        log.info("Payload: Customer_Org_ID: {}, Company_Name:{}, is_enterprise {}", customer_org_id, company_name, is_enterprise);
        customerorganisationRepository.addCustomerOrganisationById(customer_org_id, company_name, is_enterprise);
        log.info("Added Customer Organisation By Id:{}. EndPoint:{}", customer_org_id, httpServletRequest.getRequestURI());
    }

    public void deleteCustomerOrganisationById(String customer_org_id, HttpServletRequest httpServletRequest) {
        log.info("Payload: CustomerOrgId: {}", customer_org_id);
        proxyProfileService.deleteProxyProfileByCustomerOrganisationId(customer_org_id, httpServletRequest);
        customerorganisationRepository.deleteCustomerOrganisationById(customer_org_id);
        log.info("Deleted Customer Organisation By Id:{},EndPoint:{}", customer_org_id, httpServletRequest.getRequestURI());
    }

    public List<String> getAllOrganisationIds(HttpServletRequest httpServletRequest) {
        log.info("Fetching All Organisation_Ids:EndPoint:{}", httpServletRequest.getRequestURI());
        return customerorganisationRepository.getAllOrganisationIds();
    }

    public void updateCompanyNameByOrganisationId(String company_name, String organisation_id, HttpServletRequest httpServletRequest) {
        log.info("Payload: company_name: {}, organisation_id: {}", company_name, organisation_id);
        customerorganisationRepository.updateCompanyNameByOrganisationId(company_name, organisation_id);
        log.info("Updated Company Name By Organisation_Id:{},EndPoint:{}", organisation_id, httpServletRequest.getRequestURI());
    }

    public Integer getEnterpriseInfoById(String customer_org_id) {
        log.info("Payload: customer_org_id: {}", customer_org_id);
        log.info("Fetching enterprise info by Id: {}", customer_org_id);
        return customerorganisationRepository.getEnterpriseInfoById(customer_org_id);
    }

    public List<CustomerOrganisationDto> getAllOrgIdAndCompanyName(HttpServletRequest httpServletRequest) {
        log.info("Fetching all the organisation Id and company name. Endpoint: {}", httpServletRequest.getRequestURI());
        return customerorganisationRepository.getAllOrgIdAndCompanyName();
    }

    public List<CustomerOrganisationDto> getAllOrganisationDetails(HttpServletRequest httpServletRequest) {
        return customerorganisationRepository.getAllOrgIdAndCompanyName();
    }

    public ResponseEntity<ResponseDTO> getVdmsFeaturesByOrgId(String orgId, String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload: OrgId: {}, LoggedInUser: {}", orgId, loggedInUser);
        List<FeatureDTO> featureDTOList = featureService.getVdmsFeatureByOrgId(orgId);
        ResponseDTO responseDTO = ScleraUtils.generatePayload(featureDTOList, 200, true);
        log.info("Fetching Vdms Features By OrgId: {}. EndPoint:{}", orgId, httpServletRequest.getRequestURI());
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    public String getOrgNameByOrgId(String orgId, HttpServletRequest httpServletRequest) {
        return customerorganisationRepository.getOrgNameByOrgId(orgId);
    }
}
