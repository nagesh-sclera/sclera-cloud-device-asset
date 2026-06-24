package io.sclera.service;

import com.amazonaws.HttpMethod;
import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.sclera.dto.ResponseDTO;
import io.sclera.dto.VersionDTO;
import io.sclera.exception.ClientException;
import io.sclera.repository.VersionRepository;
import io.sclera.util.ScleraUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import static org.apache.http.HttpHeaders.AUTHORIZATION;


@Service
@Slf4j
public class VersionService {
    @Autowired
    private UserActionLogService userActionLogService;
    @Autowired
    VersionRepository versionRepository;

    @Autowired
    private AwsService awsService;

    public VersionDTO getVdmsVersion(HttpServletRequest httpServletRequest) {
        log.info("Fetching Vdms Version.EndPoint:{}", httpServletRequest.getRequestURI());
        return versionRepository.getVdmsVersion();
    }

    public ResponseEntity<InputStreamResource> downloadScleraApp(HttpServletRequest httpServletRequest) throws FileNotFoundException {

        HttpHeaders headers = new HttpHeaders();
        String name = "sclera_windows.exe";

        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + name);
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");

        File file = new File("/home/admin01/app/sclera_windows.exe");

        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));

        ResponseEntity<InputStreamResource> file_resource = ResponseEntity.ok()
                .headers(headers)
                .contentLength(file.length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
        log.info("Downloaded Sclera APP.EndPoint:{}", httpServletRequest.getRequestURI());
        return file_resource;
    }


    public ResponseEntity<?> updateVdmsVersion(String version, HttpServletRequest httpServletRequest) {
        versionRepository.updateVdmsVersion(version);
        ResponseDTO responseDTO = ScleraUtils.generatePayload(null, 200, true);
        userActionLogService.addUserActionLog(null, "Version", "UPDATE", "A VDMS Version: " + version + " Is Updated", "success");
        log.info("Updating Vdms Version:{}.EndPoint:{}", version, httpServletRequest.getRequestURI());
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    public ResponseEntity<?> getScleraResourceFile(String vdmsId, HttpServletRequest httpServletRequest) {
        log.info("Payload:vdmsId:{}", vdmsId);
        if (vdmsId != null) {

            String tokenVdmsId = this.extractVdmsIdFromToken(httpServletRequest);
            if (vdmsId.equals(tokenVdmsId)) {

                VersionDTO versionDTO = versionRepository.getVdmsVersion();
                String link = versionDTO.getLink();
                String[] parts = link.split("/", 4);


                if (parts.length >= 4) {
                    String result = parts[3];
                    String preSignedUrl = awsService.getPreSignedUrlForFileUpload(result, 30, HttpMethod.GET);
                    versionDTO.setLink(preSignedUrl);
                }
                return new ResponseEntity<>(versionDTO, HttpStatus.OK);

            } else {
                log.error("Role not authorized.EndPoint:{}", httpServletRequest.getRequestURI());
                throw new ClientException("Role not Authorised ", 702, httpServletRequest.getRequestURI());
            }
        } else {
            log.error("Invalid client param : EndPoint {}", httpServletRequest.getRequestURI());
            userActionLogService.addUserActionLog(vdmsId, "VDMS", "GET", "Invalid Client Parameters", "failed");
            throw new ClientException("Invalid client param", 700, httpServletRequest.getRequestURI());
        }
    }

    public String extractVdmsIdFromToken(HttpServletRequest httpServletRequest) {
        String authorizationHeader = httpServletRequest.getHeader(AUTHORIZATION);
        String token = authorizationHeader.substring("Bearer ".length());
        DecodedJWT decodedJWT = JWT.decode(token);
        return decodedJWT.getClaim("vdmsId").asString();

    }
}
