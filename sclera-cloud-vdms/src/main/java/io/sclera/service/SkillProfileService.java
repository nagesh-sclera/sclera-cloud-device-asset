package io.sclera.service;


import io.sclera.dto.ResponseDTO;
import io.sclera.dto.VdmsSyncDTO;
import io.sclera.util.ScleraUtils;
import io.sclera.util.SocketUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class SkillProfileService {

    @Autowired
    private SocketUtils socketUtils;
    @Autowired
    private VdmsService vdmsService;
    @Autowired
    private WebClientService webClientService;

    public ResponseEntity<ResponseDTO> syncSkillProfileByVdmsId(String vdmsId, HttpServletRequest httpServletRequest) {
        VdmsSyncDTO vdmsSyncDTO = VdmsSyncDTO.builder()
                .skill_profiles_sync(1)
                .build();
        int isMultiTenant = vdmsService.getMultiTenantCheck(vdmsId);
        if (isMultiTenant == 1) {
            String awsRegion = vdmsService.getAwsRegionByVdmsId(vdmsId);
            webClientService.multiTenantSyncApiCall(vdmsId, vdmsSyncDTO,awsRegion,httpServletRequest);
        } else {
            socketUtils.invokeWebSocketEndpoint("/topic/vdms/" + vdmsId + "/sync/data", vdmsSyncDTO);
        }
        ResponseDTO responseDTO = ScleraUtils.generatePayload("SuccessFully Invoked Skill Profile Sync", 200, true);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }
}
