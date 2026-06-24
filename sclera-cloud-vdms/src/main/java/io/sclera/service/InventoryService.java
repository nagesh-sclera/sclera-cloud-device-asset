package io.sclera.service;

import com.alibaba.fastjson2.JSONObject;
import io.sclera.dto.ResponseDTO;
import io.sclera.dto.VdmsDTO;
import io.sclera.repository.VdmsRepository;
import io.sclera.util.ScleraUtils;
import io.sclera.util.SocketUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class InventoryService {

    @Autowired
    private VdmsRepository vdmsRepository;

    @Autowired
    private SocketUtils socketUtils;

    @Autowired
    private WebClientService webClientService;

    public ResponseEntity<ResponseDTO> getVdmsListByOrganisationId(String orgId) {
        log.info("Fetching VDMS IDs and Names for Org ID: {}", orgId);
        Set<VdmsDTO> vdmsList =vdmsRepository.getVdmsListByOrganisationId(orgId);
        Set<VdmsDTO> onlineVdmsList = new HashSet<>();

        for (VdmsDTO vdms : vdmsList) {
            boolean isOnline = TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis()) - TimeUnit.MILLISECONDS.toMinutes(vdms.getLast_seen().longValueExact()) <= 10;
            if (isOnline) {
                vdms.setVdms_status("1");
                vdms.setTrialStatus(null);
                vdms.setTrialStartDate(null);
                vdms.setTrialEndDate(null);
                vdms.setTrialDaysRemaining(null);
                onlineVdmsList.add(vdms);
            }
        }
        ResponseDTO responseDTO= ScleraUtils.generatePayload(onlineVdmsList,200,true);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    public void socket(String vdmsId, JSONObject jsonObject, HttpServletRequest httpServletRequest) {
        log.info("Invoking WebSocket for VDMS ID : {} with payload :{}", vdmsId, jsonObject);
        int isMultiTenant = vdmsRepository.getMultiTenantCheck(vdmsId);
        if (isMultiTenant == 1) {
            String awsRegion=vdmsRepository.getAwsRegionByVdmsId(vdmsId);
            webClientService.multiTenantSyncApiCall(vdmsId, jsonObject,awsRegion,httpServletRequest);
        } else {
            socketUtils.invokeWebSocketEndpoint("/topic/vdms/" + vdmsId + "/sync/data", jsonObject);
        }
    }
}
