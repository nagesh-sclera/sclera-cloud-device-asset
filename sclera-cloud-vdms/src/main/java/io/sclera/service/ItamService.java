package io.sclera.service;


import com.alibaba.fastjson2.JSONObject;
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
public class ItamService {

    @Autowired
    private SocketUtils socketUtils;

    @Autowired
    private WebClientAlertService webClientAlertService;

    @Autowired
    private WebClientService webClientService;

    @Autowired
    private VdmsService vdmsService;

    public ResponseEntity<ResponseDTO> syncManagedSoftwaresByVdmsId(String vdmsId, JSONObject jsonObject,HttpServletRequest httpServletRequest) {
        log.info("Payload:VdmsId:{},JsonObject:{}", vdmsId, jsonObject);
        VdmsSyncDTO vdmsSyncDTO = null;
        if (jsonObject.get("type").equals("application")) {
            vdmsSyncDTO = VdmsSyncDTO.builder()
                    .managed_software_sync(1)
                    .build();
        } else if (jsonObject.get("type").equals("user")) {
            vdmsSyncDTO = VdmsSyncDTO.builder()
                    .managed_software_user_sync(1)
                    .build();
        }
        int isMultiTenant = vdmsService.getMultiTenantCheck(vdmsId);
        if (isMultiTenant == 1) {
            String awsRegion=vdmsService.getAwsRegionByVdmsId(vdmsId);
            webClientService.multiTenantSyncApiCall(vdmsId, vdmsSyncDTO,awsRegion,httpServletRequest);
        } else {
            socketUtils.invokeWebSocketEndpoint("/topic/vdms/" + vdmsId + "/sync/data", vdmsSyncDTO);
        }
        ResponseDTO responseDTO = ScleraUtils.generatePayload("Successfully invoked Managed Software Sync", 200, true);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    public void statusFailedAlert(JSONObject jsonObject) {
        log.info("ITAM Status Failed Alert Payload:{}", jsonObject);
        if (jsonObject.getString("alert_type").equalsIgnoreCase("email")) {
            webClientAlertService.statusFailedEmailAlert(jsonObject);
        } else if (jsonObject.getString("alert_type").equalsIgnoreCase("sms")) {
            webClientAlertService.statusFailedSmsAlert(jsonObject);
        } else {
            log.error("Invalid alert type: {}", jsonObject.getString("alert_type"));
        }
    }
}
