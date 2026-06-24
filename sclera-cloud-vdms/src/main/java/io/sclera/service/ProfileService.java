package io.sclera.service;

import io.sclera.dto.ResponseDTO;
import io.sclera.exception.ClientException;
import io.sclera.repository.ProfileRepository;
import io.sclera.util.ScleraRoleCheckUtils;
import io.sclera.util.ScleraUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@Slf4j
public class ProfileService {

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private ProfileUserService profileuserService;

    @Autowired
    private VdmsProfileService vdmsprofileService;

    @Autowired
    private ScleraRoleCheckUtils scleraRoleCheckUtils;


    public String checkFavouriteByCustomerOrganisationId(String organisation_id, HttpServletRequest httpServletRequest) {
        log.info("Payload: OrgId: {}", organisation_id);
        log.info("Checking Favourite By Customer Organisation_Id:{},EndPoint:{}", organisation_id, httpServletRequest.getRequestURI());
        return profileRepository.checkFavouriteByCustomerOrganisationId(organisation_id);
    }

    public String checkFavouriteByVendorOrganisationId(String organisation_id, HttpServletRequest httpServletRequest) {
        log.info("Payload: OrgId: {}", organisation_id);
        log.info("Checking Favourite By Vendor Organisation_Id:{},EndPoint:{}", organisation_id, httpServletRequest.getRequestURI());
        return profileRepository.checkFavouriteByVendorOrgansiationId(organisation_id);
    }


    public void deleteProfileByProfileId(String organisation_id, String email, String profile_id, HttpServletRequest httpServletRequest) {
        log.info("Payload:Org_Id:{},Email:{},profile_id:{}", organisation_id, email, profile_id);
        profileuserService.deleteProfileUsersByProfileId(profile_id, httpServletRequest);
        vdmsprofileService.deleteVdmsProfileByProfileId(profile_id, httpServletRequest);
        profileRepository.deleteProfileByProfileId(profile_id, httpServletRequest);
        log.info("Delete Profile By profile_Id:{},EndPoint:{}", profile_id, httpServletRequest.getRequestURI());
    }


    public ResponseEntity<?> deleteProfileByCustomerOrganisationId(String organisation_id, String email, String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload:Org_Id:{},Email:{},loggedInUser:{}", organisation_id, email, loggedInUser);
        if (organisation_id != null && email != null && loggedInUser != null) {
            boolean access = scleraRoleCheckUtils.checkRole(loggedInUser, httpServletRequest, "super-admin", "admin", "master-user", "org-admin", "user", "master-vendor", "vendor", "property-admin");
            if (access) {
                Set<String> profile_ids = profileRepository.getAllProfilesByCustomerOrganisationId(organisation_id);
                if (profile_ids != null && profile_ids.size() > 0) {
                    for (String profile_id : profile_ids) {
                        deleteProfileByProfileId(organisation_id, email, profile_id, httpServletRequest);
                    }
                }
                ResponseDTO responseDTO = ScleraUtils.generatePayload(null, 200, true);
                log.info("Deleting Profile By Customer Organisation_id:{},EndPoint:{}", organisation_id, httpServletRequest.getRequestURI());
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            } else {
                log.error("Role not authorized.EndPoint:{}", httpServletRequest.getRequestURI());
                throw new ClientException("Role not Authorised ", 702, httpServletRequest.getRequestURI());
            }
        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            throw new ClientException("Client param exception", 700, httpServletRequest.getRequestURI());
        }
    }

}