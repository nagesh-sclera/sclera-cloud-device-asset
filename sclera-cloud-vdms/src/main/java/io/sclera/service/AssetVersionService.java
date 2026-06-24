package io.sclera.service;

import io.sclera.dto.ResponseDTO;
import io.sclera.dto.VersionDTO;
import io.sclera.repository.AssetVersionRepository;
import io.sclera.util.ScleraUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AssetVersionService {

    @Autowired
    private AssetVersionRepository assetVersionRepository;

    @Autowired
    private UserActionLogService userActionLogService;

    public VersionDTO getAssetVersion(HttpServletRequest httpServletRequest) {
        log.info("Fetching Asset Version and Download Link. Endpoint: {}", httpServletRequest.getRequestURI());
        return assetVersionRepository.getAssetVersion();
    }

    public ResponseEntity<ResponseDTO> updateAssetVersion(String assetVersion, HttpServletRequest httpServletRequest) {
        log.info("Payload: AssetVersion: {}", assetVersion);
        assetVersionRepository.updateAssetVersion(assetVersion);
        ResponseDTO responseDTO = ScleraUtils.generatePayload("Asset Version Updated Successfully", 200, true);
        userActionLogService.addUserActionLog(null, "AssetVersion", "UPDATE", "A Asset Version:" + assetVersion + " Is Updated", "success");
        log.info("Updating Asset Version:{}. EndPoint:{}", assetVersion, httpServletRequest.getRequestURI());
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }
}
