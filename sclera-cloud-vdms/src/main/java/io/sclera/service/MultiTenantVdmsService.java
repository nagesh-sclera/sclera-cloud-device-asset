package io.sclera.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import io.sclera.dto.ResponseDTO;
import io.sclera.dto.VdmsDTO;
import io.sclera.util.ScleraUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MultiTenantVdmsService {

    @Autowired
    private VdmsService vdmsService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserActionLogService userActionLogService;

    public ResponseEntity<?> updateVdmsActivationStatus(String vdmsId, HttpServletRequest httpServletRequest) {
        log.info("payload: VdmsId:{}", vdmsId);
        vdmsService.updateVdmsActivationStatus(vdmsId);
        String email = userService.getMasterUserEmailByVdmsId(vdmsId, httpServletRequest);
        vdmsService.sendActivationEmail(email, vdmsId, httpServletRequest);
        ResponseDTO responseDTO = ScleraUtils.generatePayload(null, 200, true);
        log.info("successfully Updated vdms activation status:EndPoint:{}", httpServletRequest.getRequestURI());
        userActionLogService.addUserActionLog(email, "MultiTenancy", "UPDATE",
                "Vdms Activated:" + vdmsId, "success");
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    public ResponseEntity<ResponseDTO> getMultiTenantCheck(String vdmsId, HttpServletRequest httpServletRequest) {
        log.info("Fetching multi-tenant check for vdmsId: {}", vdmsId);
        int isMultiTenant = vdmsService.getMultiTenantCheck(vdmsId);
        ResponseDTO responseDTO = ScleraUtils.generatePayload(isMultiTenant, 200, true);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    public ResponseEntity<?> updateVdmsDeploymentType(String vdmsId, HttpServletRequest httpServletRequest) {
        log.info("payload: VdmsId:{}", vdmsId);
        String email = userService.getMasterUserEmailByVdmsId(vdmsId, httpServletRequest);
        VdmsDTO vdmsDTO = vdmsService.getVdmsInfoByVdmsId(vdmsId, httpServletRequest);
        if (vdmsDTO.getDeployment_type().equals("on_premises")) {
            vdmsService.updateVdmsDeploymentType(vdmsId, httpServletRequest);
        }
        ResponseDTO responseDTO = ScleraUtils.generatePayload(null, 200, true);
        log.info("successfully Updated vdms Deployment Type:EndPoint:{}", httpServletRequest.getRequestURI());
        userActionLogService.addUserActionLog(email, "MultiTenancy", "UPDATE", "Vdms Updated Deployment Type:" + vdmsId, "success");
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    public ResponseEntity<ResponseDTO> multiTenantVdmsInfo(String vdmsId, HttpServletRequest httpServletRequest) {
        log.info("payload: VdmsId:{}", vdmsId);
        VdmsDTO vdmsDTO = vdmsService.getVdmsInfoByVdmsId(vdmsId, httpServletRequest);
        JSONObject jsonObject = (JSONObject) JSON.toJSON(vdmsDTO);
        ResponseDTO responseDTO = ScleraUtils.generatePayload(jsonObject, 200, true);
        log.info("Fetching multi-tenant Info for vdmsId: {}", vdmsId);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }
}