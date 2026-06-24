package io.sclera.service;

import io.sclera.config.ResourceUrlConfig;
import io.sclera.dto.ResponseDTO;
import io.sclera.util.ScleraUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AssetDataCollectionService {

    @Autowired
    private VdmsService vdmsService;

    public ResponseEntity<ResponseDTO> getIsMultiTenantCheckByVdmsId(String vdmsId, HttpServletRequest httpServletRequest) {
        log.info("Get Is Multi Tenant Check By VdmsId. Endpoint: {}, vdmsId: {}", httpServletRequest.getRequestURI(), vdmsId);
        Integer isMultiTenant = vdmsService.getMultiTenantCheck(vdmsId);
        ResponseDTO responseDTO = ScleraUtils.generatePayload(isMultiTenant, 200, true);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

}
