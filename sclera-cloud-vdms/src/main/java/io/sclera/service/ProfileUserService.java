package io.sclera.service;

import io.sclera.dto.ProfileUserDTO;
import io.sclera.repository.ProfileUserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@Slf4j
public class ProfileUserService {

    @Autowired
    private ProfileUserRepository profileuserRepository;

    public void deleteProfileUsersByProfileId(String profile_id, HttpServletRequest httpServletRequest) {
        log.info("Payload: ProfileId: {}", profile_id);
        profileuserRepository.deleteProfileUsersByProfileId(profile_id);
        log.info("Deleted Profile Users By Profile_Id:{},EndPoint:{}", profile_id, httpServletRequest.getRequestURI());
    }

    public Set<ProfileUserDTO> getProfileUsersByProfileId(String organisation_id, String email, String profile_id, HttpServletRequest httpServletRequest) {
        log.info("Payload:Org_Id:{},Email:{},profile_id:{}", organisation_id, email, profile_id);
        log.info("Fetching Profile User By Profile_Id:{},EndPoint:{}", profile_id, httpServletRequest.getRequestURI());
        return profileuserRepository.getProfileUsersByProfileId(profile_id);
    }

}
