package io.sclera.service;

import io.sclera.config.ResourceUrlConfig;
import io.sclera.dto.ResponseDTO;
import io.sclera.dto.ScleraFXDTO;
import io.sclera.exception.ClientException;
import io.sclera.exception.ServerException;
import io.sclera.repository.ScleraFXRepository;
import io.sclera.util.ScleraRoleCheckUtils;
import io.sclera.util.ScleraUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@Slf4j
public class ScleraFXService {

    @Autowired
    private WebClientService webClientService;
    @Autowired
    private UserActionLogService userActionLogService;
    @Autowired
    private ScleraFXRepository scleraFXRepository;

    @Autowired
    private ResourceUrlConfig resourceUrlConfig;

    @Autowired
    private AwsService awsService;

    @Autowired
    private ScleraRoleCheckUtils scleraRoleCheckUtils;


    public ResponseEntity<?> getAllScleraFXVersions(String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload: loggedInUser: {}", loggedInUser);
        if (loggedInUser != null) {
            boolean access = scleraRoleCheckUtils.checkRole(loggedInUser, httpServletRequest, "super-admin", "admin", "master-user", "org-admin", "user", "master-vendor", "vendor", "property-admin");
            if (access) {
                Map<String, String> data = new HashMap<>();
                List<ScleraFXDTO> scleraFXDTOS = scleraFXRepository.getAllScleraFXVersions();
                log.info("ScleraFXDTO: {}", scleraFXDTOS);
                if (scleraFXDTOS != null && scleraFXDTOS.size() > 0) {
                    for (ScleraFXDTO scleraFXDTO : scleraFXDTOS) {
                        data.put(scleraFXDTO.getOs(), scleraFXDTO.getVersion());
                    }
                }
                ResponseDTO responseDTO = ScleraUtils.generatePayload(data, 200, true);
                log.info("Fetching All Sclera FX Versions. EndPoint:{}", httpServletRequest.getRequestURI());
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            } else {
                log.error("Role not authorized.EndPoint:{}", httpServletRequest.getRequestURI());
                throw new ClientException("Role not Authorised ", 702, httpServletRequest.getRequestURI());
            }
        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            throw new ClientException("Invalid client exception", 700, httpServletRequest.getRequestURI());
        }
    }


    public ResponseEntity<?> uploadScleraToCloudByOS(MultipartFile sclera_app, String os, String loggedInUser, HttpServletRequest httpServletRequest) throws IOException {
        log.info("Payload: os: {}, loggedInUser: {}", os, loggedInUser);
        if (sclera_app != null && os != null && loggedInUser != null) {
            boolean access = scleraRoleCheckUtils.checkRole(loggedInUser, httpServletRequest, "super-admin", "admin");
            if (access) {
                String db_version = scleraFXRepository.getScleraFXVersionByOS(os);
                log.info("db_version: {}", db_version);
                String version = Objects.requireNonNull(sclera_app.getOriginalFilename()).substring(sclera_app.getOriginalFilename().indexOf("-") + 1, sclera_app.getOriginalFilename().lastIndexOf("-"));
                log.info("version: {}", version);
                if (os.equalsIgnoreCase("windows")) {
                    log.info("Uploading Sclera To Cloud By OS:{},EndPoint:{}", os, httpServletRequest.getRequestURI());
                    userActionLogService.addUserActionLog(loggedInUser, "ScleraFX", "ADD", "A Sclera APP Windows Version Is Downloaded", "success");
                    awsService.removeFileFromAWSS3(resourceUrlConfig.getScleraFXAppDirectory(), "sclera-" + db_version + "-win.exe", httpServletRequest);
                    awsService.addFileToAWSS3(sclera_app.getBytes(), resourceUrlConfig.getScleraFXAppDirectory(), resourceUrlConfig.getScleraFXAppUrl(), "exe", "sclera-" + version + "-win", httpServletRequest);
                }
                if (os.equalsIgnoreCase("ubuntu")) {
                    log.info("Uploading Sclera To Cloud By OS:{},EndPoint:{}", os, httpServletRequest.getRequestURI());
                    userActionLogService.addUserActionLog(loggedInUser, "ScleraFX", "ADD", "A Sclera APP ubuntu Version Is Downloaded", "success");
                    awsService.removeFileFromAWSS3(resourceUrlConfig.getScleraFXAppDirectory(), "sclera-" + db_version + "-amd64.deb", httpServletRequest);
                    awsService.addFileToAWSS3(sclera_app.getBytes(), resourceUrlConfig.getScleraFXAppDirectory(), resourceUrlConfig.getScleraFXAppUrl(), "deb", "sclera-" + version + "-amd64", httpServletRequest);
                }
                if (os.equalsIgnoreCase("mac")) {
                    log.info("Uploading Sclera To Cloud By OS:{},EndPoint:{}", os, httpServletRequest.getRequestURI());
                    userActionLogService.addUserActionLog(loggedInUser, "ScleraFX", "ADD", "A Sclera APP mac Version Is Downloaded", "success");
                    awsService.removeFileFromAWSS3(resourceUrlConfig.getScleraFXAppDirectory(), "sclera-" + db_version + "-mac.dmg", httpServletRequest);
                    awsService.addFileToAWSS3(sclera_app.getBytes(), resourceUrlConfig.getScleraFXAppDirectory(), resourceUrlConfig.getScleraFXAppUrl(), "dmg", "sclera-" + version + "-mac", httpServletRequest);
                }
                scleraFXRepository.updateScleraFXVersionByOs(version, os);
                ResponseDTO responseDTO = ScleraUtils.generatePayload(null, 200, true);
                log.info("Update Sclera_FX Version:{} to cloud By OS:{},EndPoint:{}", version, os, httpServletRequest.getRequestURI());
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            } else {
                log.error("Role not authorized.EndPoint:{}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(loggedInUser, "ScleraFX", "ADD", "Role not Authorised", "failed");
                throw new ClientException("Role not Authorised ", 702, httpServletRequest.getRequestURI());
            }
        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            userActionLogService.addUserActionLog(loggedInUser, "QR_Code", "ADD", "Invalid Client Parameters", "failed");
            throw new ClientException("Client param exception", 700, httpServletRequest.getRequestURI());
        }
    }

    public ResponseEntity<?> getScleraFXByOS(String email, String os, String loggedInUser, HttpServletRequest httpServletRequest) throws IOException {
        log.info("Payload: Email: {}, OS: {}, loggedInUser: {}", email, os, loggedInUser);
        if (email != null && os != null && loggedInUser != null) {
            boolean access = scleraRoleCheckUtils.checkRole(loggedInUser, httpServletRequest, "super-admin", "admin", "master-user", "org-admin", "user", "master-vendor", "vendor", "property-admin");
            if (access) {
                String db_version = scleraFXRepository.getScleraFXVersionByOS(os);
                log.info("db_version: {}", db_version);
                Boolean isExists;
                if (os.equalsIgnoreCase("windows")) {
                    log.info("Fetching Sclera_FX By OS:{},EndPoint:{}", os, httpServletRequest.getRequestURI());
                    isExists = awsService.checkFileExist(resourceUrlConfig.getScleraFXAppDirectory() + "sclera-" + db_version + "-win.exe", httpServletRequest);
                } else if (os.equalsIgnoreCase("ubuntu")) {
                    log.info("Fetching Sclera_FX By OS:{},EndPoint:{}", os, httpServletRequest.getRequestURI());
                    isExists = awsService.checkFileExist(resourceUrlConfig.getScleraFXAppDirectory() + "sclera-" + db_version + "-amd64.deb", httpServletRequest);
                } else if (os.equalsIgnoreCase("mac")) {
                    log.info("Fetching Sclera_FX By OS:{},EndPoint:{}", os, httpServletRequest.getRequestURI());
                    isExists = awsService.checkFileExist(resourceUrlConfig.getScleraFXAppDirectory() + "sclera-" + db_version + "-mac.dmg", httpServletRequest);
                } else {
                    log.error("OS Not Supported.Endpoint:{}", httpServletRequest.getRequestURI());
                    throw new ClientException("OS not supported", 743, httpServletRequest.getRequestURI());
                }
                if (isExists) {
                    webClientService.updateScleraFXVersionByEmailAndOS(email, os, db_version, loggedInUser, httpServletRequest);
                    log.info("Updated Sclera_FX in Login");
                    ResponseDTO responseDTO = ScleraUtils.generatePayload(null, 200, true);
                    log.info("Fetching Sclera FX by OS {} and email {}.EndPoint:{}", os, email, httpServletRequest.getRequestURI());
                    return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                } else {
                    log.error("App Does Not Exist.EndPoint:{}", httpServletRequest.getRequestURI());
                    throw new ServerException("App does not exist", 811, httpServletRequest.getRequestURI());
                }
            } else {
                log.error("Role not authorized.EndPoint:{}", httpServletRequest.getRequestURI());
                throw new ClientException("Role not Authorised ", 702, httpServletRequest.getRequestURI());
            }
        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            throw new ClientException("Invalid client exception", 700, httpServletRequest.getRequestURI());
        }
    }

}
