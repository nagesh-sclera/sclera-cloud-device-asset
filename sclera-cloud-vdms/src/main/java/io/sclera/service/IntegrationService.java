package io.sclera.service;

import com.fasterxml.uuid.Generators;
import io.sclera.config.ResourceUrlConfig;
import io.sclera.dto.IntegrationDTO;
import io.sclera.dto.ResponseDTO;
import io.sclera.exception.ClientException;
import io.sclera.repository.IntegrationRepository;
import io.sclera.util.ScleraRoleCheckUtils;
import io.sclera.util.ScleraUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.util.Base64;
import java.util.Set;

@Service
@Slf4j
public class IntegrationService {

    @Autowired
    private IntegrationRepository integrationRepository;
    @Autowired
    private UserActionLogService userActionLogService;
    @Autowired
    private AwsService awsService;

    @Autowired
    private ResourceUrlConfig resourceUrlConfig;

    @Autowired
    private ScleraRoleCheckUtils scleraRoleCheckUtils;


    public ResponseEntity<?> getDistinctIntegrations(String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload: LoggedInUser: {}", loggedInUser);

        if (loggedInUser != null) {
            boolean access = scleraRoleCheckUtils.checkRole(loggedInUser, httpServletRequest, "super-admin", "admin", "master-user", "org-admin", "user", "master-vendor", "vendor", "property-admin");
            if (access) {
                Set<IntegrationDTO> data = integrationRepository.getDistinctIntegrations();
                ResponseDTO responseDTO = ScleraUtils.generatePayload(data, 200, true);
                log.info("Get Distinct Integrations. EndPoint:{}", httpServletRequest.getRequestURI());
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            } else {
                log.error("Role not authorized.EndPoint:{}", httpServletRequest.getRequestURI());
                throw new ClientException("Role not Authorised ", 702, httpServletRequest.getRequestURI());
            }
        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            throw new ClientException("Client param exception", 700, httpServletRequest.getRequestURI());
        }

    }

    public ResponseEntity<?> getIntegrationNames(String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload: LoggedInUser: {}", loggedInUser);

        if (loggedInUser != null) {
            boolean access = scleraRoleCheckUtils.checkRole(loggedInUser, httpServletRequest, "super-admin", "admin", "master-user", "org-admin", "user", "master-vendor", "vendor", "property-admin");
            if (access) {
                Set<IntegrationDTO> data = integrationRepository.getIntegrationNames();
                ResponseDTO responseDTO = ScleraUtils.generatePayload(data, 200, true);
                log.info("Get Integration Names. EndPoint:{}", httpServletRequest.getRequestURI());
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            } else {
                log.error("Role not authorized.EndPoint:{}", httpServletRequest.getRequestURI());
                throw new ClientException("Role not Authorised ", 702, httpServletRequest.getRequestURI());
            }
        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            throw new ClientException("Client param exception", 700, httpServletRequest.getRequestURI());
        }

    }


    public ResponseEntity<?> getIntegrationDataByIntegrationId(String id, String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload: Integration Id: {}, LoggedInUser: {}", id, loggedInUser);
        if (id != null) {
            boolean access = scleraRoleCheckUtils.checkRole(loggedInUser, httpServletRequest, "super-admin", "admin", "master-user", "org-admin", "user", "master-vendor", "vendor", "property-admin");
            if (access) {
                IntegrationDTO data = integrationRepository.getIntegrationDataByIntegrationId(id);
                ResponseDTO responseDTO = ScleraUtils.generatePayload(data, 200, true);
                log.info("Get Integration Data By Integration Id:{}. EndPoint:{}", id, httpServletRequest.getRequestURI());
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            } else {
                log.error("Role not authorized.EndPoint:{}", httpServletRequest.getRequestURI());
                throw new ClientException("Role not Authorised ", 702, httpServletRequest.getRequestURI());
            }
        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            throw new ClientException("Client param exception", 700, httpServletRequest.getRequestURI());
        }

    }

    public void upsertIntegration(IntegrationDTO integrationDTO, String loggedInUser, HttpServletRequest httpServletRequest) throws IOException {
        log.info("Payload:IntegrationDTO:{},loggedInUser{}", integrationDTO, loggedInUser);
        if (integrationDTO != null && loggedInUser != null) {
            boolean access = scleraRoleCheckUtils.checkRole(loggedInUser, httpServletRequest, "super-admin", "admin", "master-user", "org-admin", "user", "master-vendor", "vendor", "property-admin");
            if (access) {
                if (integrationDTO.getIntegration_id() == null) {
                    String id = Generators.timeBasedGenerator().generate().toString();
                    integrationDTO.setIntegration_id(id);
                }
                if (integrationDTO.getBase64image() != null) {
                    byte[] image = Base64.getDecoder().decode(integrationDTO.getBase64image());
                    log.info("Adding Image to AWS S3.EndPoint:{}", httpServletRequest.getRequestURI());
                    String image_url = awsService.addFileToAWSS3(image, resourceUrlConfig.getIntegrationImageDirectory(), resourceUrlConfig.getIntegrationImageUrl(),
                            integrationDTO.getExtension(), integrationDTO.getIntegration_id(), httpServletRequest);
                    integrationDTO.setImage_url(image_url);
                }
                integrationRepository.upsertIntegration(integrationDTO.getIntegration_id(), integrationDTO.getName(), integrationDTO.getIntegration_name(),
                        integrationDTO.getSubscriptions(), integrationDTO.getProtocols(), integrationDTO.getTag_list(),
                        integrationDTO.getAuthentications(), integrationDTO.getTemplate(), integrationDTO.getImage_url(),
                        integrationDTO.getDescription(), integrationDTO.getCategory());
                userActionLogService.addUserActionLog(loggedInUser, "Integration", "ADD", "A Integration Is Added For Name:" + integrationDTO.getName(), "success");
                log.info("Successfully Upserted Integration.EndPoint:{}", httpServletRequest.getRequestURI());
            } else {
                log.error("Role not authorized.EndPoint:{}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(loggedInUser, "Integration", "ADD", "Role Not Authorised", "failed");
                throw new ClientException("Role not Authorised ", 702, httpServletRequest.getRequestURI());
            }
        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            userActionLogService.addUserActionLog(loggedInUser, "Integration", "ADD", "Invalid Client Parameters", "failed");
            throw new ClientException("Client param exception", 700, httpServletRequest.getRequestURI());
        }

    }

    public ResponseEntity<?> getIntegrationsByCategory(Set<String> categories, String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload:Categories:{},loggedInUser{}", categories, loggedInUser);
        if (categories != null && loggedInUser != null) {
            boolean access = scleraRoleCheckUtils.checkRole(loggedInUser, httpServletRequest, "super-admin", "admin", "master-user", "org-admin", "user", "master-vendor", "vendor", "property-admin");
            if (access) {
                Set<IntegrationDTO> data = integrationRepository.getIntegrationsByCategory(categories);
                ResponseDTO responseDTO = ScleraUtils.generatePayload(data, 200, true);
                log.info("Get Integrations By Category.EndPoint:{}", httpServletRequest.getRequestURI());
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            } else {
                log.error("Role not authorized.EndPoint:{}", httpServletRequest.getRequestURI());
                throw new ClientException("Role not Authorised ", 702, httpServletRequest.getRequestURI());
            }
        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            throw new ClientException("Client param exception", 700, httpServletRequest.getRequestURI());
        }

    }
}
