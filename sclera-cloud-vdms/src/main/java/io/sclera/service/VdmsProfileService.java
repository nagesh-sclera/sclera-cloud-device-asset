package io.sclera.service;

import com.fasterxml.uuid.Generators;
import io.sclera.dto.VdmsDTO;
import io.sclera.repository.VdmsProfileRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@Slf4j
public class VdmsProfileService {

    @Autowired
    private VdmsProfileRepository vdmsprofileRepository;

    @Autowired
    private ProfileService profileService;



    public void tagProfileToVdmsByCustomerOrganisationId(String organisation_id, String email, VdmsDTO vdmsdto, HttpServletRequest httpServletRequest) {
        log.info("Payload:Org_Id:{},Email:{},VdmsDTO:{}", organisation_id, email, vdmsdto);
        String id = Generators.timeBasedGenerator().generate().toString();
        log.info("Tag Profile To Vdms By Customer_Org_id:{},EndPoint:{}", organisation_id, httpServletRequest.getRequestURI());
        vdmsprofileRepository.tagProfileToVdmsByOrganisationId(id, vdmsdto.getVdms_id(), organisation_id, null, vdmsdto.getProfile_id());
    }


    public void tagProfileToVdmsByVendorOrganisationId(String organisation_id, String email, VdmsDTO vdmsdto, HttpServletRequest httpServletRequest) {
        log.info("Payload:Org_Id:{},Email:{},VdmsDTO:{}", organisation_id, email, vdmsdto);
        String id = Generators.timeBasedGenerator().generate().toString();
        log.info("Tag Profile To Vdms By Vendor_Org_id:{},EndPoint:{}", organisation_id, httpServletRequest.getRequestURI());
        vdmsprofileRepository.tagProfileToVdmsByOrganisationId(id, vdmsdto.getVdms_id(), null, organisation_id, vdmsdto.getProfile_id());
    }

    public void deleteVdmsProfileByVdmsId(String vdms_id, HttpServletRequest httpServletRequest) {
        log.info("Delete Vdms Profile By Vdms_Id:{},EndPoint:{}", vdms_id, httpServletRequest.getRequestURI());
        vdmsprofileRepository.deleteVdmsProfileByVdmsId(vdms_id);
    }

    public void deleteVdmsProfileByProfileId(String profile_id, HttpServletRequest httpServletRequest) {
        log.info("Delete Vdms Profile By Profile_Id:{},EndPoint:{}", profile_id, httpServletRequest.getRequestURI());
        vdmsprofileRepository.deleteVdmsProfileByProfileId(profile_id);
    }

    public void tagPrimaryProfileToVdms(String vdms_id, String role, String organisation_id, HttpServletRequest httpServletRequest) {
        log.info("Payload:Org_Id:{},vdms_id:{},role:{}", organisation_id, vdms_id, role);
        String profile_id = null;
        int isPrimaryProfileTagged = 0;
        if (role.equalsIgnoreCase("master-user") || role.equalsIgnoreCase("org-admin")) {
            profile_id = profileService.checkFavouriteByCustomerOrganisationId(organisation_id, httpServletRequest);
            isPrimaryProfileTagged = vdmsprofileRepository.checkIfPrimaryProfileIsTaggedToVdmsByCustomerOrgId(vdms_id, organisation_id);
        }
        if (role.equalsIgnoreCase("master-vendor")) {
            profile_id = profileService.checkFavouriteByVendorOrganisationId(organisation_id, httpServletRequest);
            isPrimaryProfileTagged = vdmsprofileRepository.checkIfPrimaryProfileIsTaggedToVdmsByVendorOrgId(vdms_id, organisation_id);
        }

        if (isPrimaryProfileTagged == 0 && profile_id != null) {
            VdmsDTO vdmsdto = new VdmsDTO();
            vdmsdto.setVdms_id(vdms_id);
            vdmsdto.setProfile_id(profile_id);

            if (role.equalsIgnoreCase("master-user") || role.equalsIgnoreCase("org-admin")) {
                tagProfileToVdmsByCustomerOrganisationId(organisation_id, null, vdmsdto, httpServletRequest);
            } else if (role.equalsIgnoreCase("master-vendor")) {
                tagProfileToVdmsByVendorOrganisationId(organisation_id, null, vdmsdto, httpServletRequest);
            }
        }
    }

    public String getProfileIdByVendorOrganisationIdAndVdmsId(String vdms_id, String vendor_org_id, HttpServletRequest httpServletRequest) {
        log.info("Fetching Profile_Id By Vendor_Organisation_Id:{} And Vdms_Id:{},EndPoint:{}", vendor_org_id, vdms_id, httpServletRequest.getRequestURI());
        return vdmsprofileRepository.getProfileIdByVendorOrganisationIdAndVdmsId(vdms_id, vendor_org_id);
    }

    public void deleteVdmsProfileByVdmsIdAndCustomerOrganisationId(String vdms_id, String customer_org_id, HttpServletRequest httpServletRequest) {
        log.info("Delete Vdms Profile By Vdms_Id:{},And Customer_Org_Id:{},EndPoint:{}", vdms_id, customer_org_id, httpServletRequest.getRequestURI());
        vdmsprofileRepository.deleteVdmsProfileByVdmsIdAndCustomerOrganisationId(vdms_id, customer_org_id);
    }
}
