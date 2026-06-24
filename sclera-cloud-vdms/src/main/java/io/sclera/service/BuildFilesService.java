package io.sclera.service;

import com.amazonaws.HttpMethod;
import io.sclera.dto.BuildFilesDTO;
import io.sclera.dto.ResponseDTO;
import io.sclera.repository.BuildFilesRepository;
import io.sclera.util.ScleraUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class BuildFilesService {

    @Autowired
    private BuildFilesRepository buildFilesRepository;

    @Autowired
    private AwsService awsService;

    @Autowired
    private UserActionLogService userActionLogService;

    public ResponseEntity<ResponseDTO> getBuildFiles(String type, HttpServletRequest httpServletRequest) {
        log.info("Fetching Build Files Details by Type:{}. Endpoint:{}", type, httpServletRequest.getRequestURI());
        BuildFilesDTO buildFilesDTO = buildFilesRepository.getBuildFiles(type);

        String link = buildFilesDTO.getLink();
        String[] parts = link.split("/", 4);

        if (parts.length >= 4) {
            String result = parts[3];
            String preSignedUrl = awsService.getPreSignedUrlForFileUpload(result, 30, HttpMethod.GET);
            buildFilesDTO.setLink(preSignedUrl);
        }

        ResponseDTO responseDTO = ScleraUtils.generatePayload(buildFilesDTO, 200, true);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);

    }

    public ResponseEntity<ResponseDTO> updateBuildFiles(String type, String version, HttpServletRequest httpServletRequest) {
        log.info("Payload: Type:{}, Version: {}", type, version);
        buildFilesRepository.updateVersionByType(version, type);
        ResponseDTO responseDTO = ScleraUtils.generatePayload("Version Updated Successfully", 200, true);
        userActionLogService.addUserActionLog(null, "BuildFiles", "UPDATE", "A Version is updated to : " + version + " for type : " + type, "success");
        log.info("Updating Version:{} for type:{} . EndPoint:{}", version, type, httpServletRequest.getRequestURI());
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }
}
