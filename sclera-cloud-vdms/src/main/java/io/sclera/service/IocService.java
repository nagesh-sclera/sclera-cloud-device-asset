package io.sclera.service;

import com.alibaba.fastjson2.JSON;
import com.fasterxml.uuid.Generators;
import io.sclera.dto.CustomerOrganisationDto;
import io.sclera.dto.IocDto;
import io.sclera.dto.ResponseDTO;
import io.sclera.exception.ClientException;
import io.sclera.repository.IocRepository;
import io.sclera.util.ScleraRoleCheckUtils;
import io.sclera.util.ScleraUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;

@Service
@Slf4j
public class IocService {

    @Autowired
    public IocRepository iocRepository;

    @Autowired
    public WebClientService webClientService;


    @Autowired
    public ScleraRoleCheckUtils scleraRoleCheckUtils;

    @Autowired
    public CustomerOrganisationService customerOrganisationService;
    @Autowired
    private UserActionLogService userActionLogService;

    public ResponseEntity<?> getAllIocDetails(String email, String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload: Email: {}, LoggedInUser: {}", email, loggedInUser);
        if (email != null) {
            List<IocDto> iocData = iocRepository.getAllIocDetails();
            log.info("IocDTO: {}", iocData);
            if (iocData != null) {
                List<IocDto> loginIocData = webClientService.getAllIocDetails(email, loggedInUser, httpServletRequest);
                log.info("Login IocData Response: {}", loginIocData);
                for (IocDto data : iocData) {
                    for (IocDto loginData : loginIocData) {
                        if (data.getId().equals(loginData.getId())) {
                            data.setUsername(loginData.getUsername());
                        }
                    }
                }
            }
            ResponseDTO responseDTO = ScleraUtils.generatePayload(iocData, 200, true);
            log.info("Fetching All Ioc Data. Endpoint: {}", httpServletRequest.getRequestURI());
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } else {
            log.error("Error! Invalid client parameters:EndPoint:{}", httpServletRequest.getRequestURI());
            throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
        }

    }

    public ResponseEntity<?> getIocDetailsByIocId(String email, String iocId, String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload: Email: {}, IocId: {}, LoggedInUser: {}", email, iocId, loggedInUser);
        if (email != null && iocId != null) {
//                boolean authorized = scleraRoleCheckUtils.checkRole(loggedInUser, httpServletRequest, "super-admin", "admin", "org-admin", "user", "master-vendor", "vendor", "master-user","property-admin");
//                if (authorized) {
            IocDto iocData = iocRepository.getIocDetailsByIocId(iocId);
            log.info("IocDTO: {}", iocData);
            if (iocData != null) {
                IocDto loginIocData = webClientService.getIocDetailsByIocId(email, iocId, loggedInUser, httpServletRequest);
                iocData.setUsername(loginIocData.getUsername());
                ResponseDTO responseDTO = ScleraUtils.generatePayload(iocData, 200, true);
                log.info("Fetching Ioc Data by  Ioc Id : {}. Endpoint: {}", iocId, httpServletRequest.getRequestURI());
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            } else {
                ResponseDTO responseDTO = ScleraUtils.generatePayload(null, 200, true);
                log.info("Ioc Data Not Found by Ioc Id : {}. Endpoint: {}", iocId, httpServletRequest.getRequestURI());
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            }

//                } else {
//                    log.error("Role not authorized.EndPoint:{}", httpServletRequest.getRequestURI());
//                    throw new ClientException("Role not Authorised ", 702, httpServletRequest.getRequestURI());
//                }
        } else {
            log.error("Error! Invalid client parameters:EndPoint:{}", httpServletRequest.getRequestURI());
            throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
        }

    }


    public ResponseEntity<?> getAllIocDetailsByOrgId(String email, String orgId, String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload: Email: {}, orgId: {}, LoggedInUser: {}", email, orgId, loggedInUser);
        if (email != null) {
            List<IocDto> iocData = iocRepository.getAllIocDetailsByOrgId(orgId);
            if (iocData != null) {
                List<IocDto> loginIocData = webClientService.getAllIocDetailsByOrgId(email, orgId, loggedInUser, httpServletRequest);
                log.info("IocDTO: {}", iocData);
                for (IocDto data : iocData) {
                    for (IocDto loginData : loginIocData) {
                        if (data.getId().equals(loginData.getId())) {
                            data.setUsername(loginData.getUsername());
                        }
                    }
                }
            }
            ResponseDTO responseDTO = ScleraUtils.generatePayload(iocData, 200, true);
            log.info("Fetching All Ioc Data By orgId {}. Endpoint: {}", orgId, httpServletRequest.getRequestURI());
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } else {
            log.error("Error! Invalid client parameters:EndPoint:{}", httpServletRequest.getRequestURI());
            throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
        }
    }

    public ResponseEntity<?> getIocDetailsByIocIdAndOrgId(String email, String orgId, String iocId, String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload: Email: {}, orgId: {},iocId:{} LoggedInUser: {}", email, orgId, iocId, loggedInUser);
        if (email != null && iocId != null) {
            boolean authorized = scleraRoleCheckUtils.checkRole(loggedInUser, httpServletRequest, "super-admin", "admin", "org-admin", "user", "master-vendor", "vendor", "master-user", "property-admin");
            if (authorized) {
                IocDto iocData = iocRepository.getIocDetailsByIocIdAndOrgId(iocId, orgId);
                log.info("IocDTO: {}", iocData);
                if (iocData != null) {
                    IocDto loginIocData = webClientService.getIocDetailsByIocIdAndOrgId(email, iocId, orgId, loggedInUser, httpServletRequest);
                    iocData.setUsername(loginIocData.getUsername());
                    ResponseDTO responseDTO = ScleraUtils.generatePayload(iocData, 200, true);
                    log.info("Fetching Ioc Data by  Ioc Id : {}. Endpoint: {}", iocId, httpServletRequest.getRequestURI());
                    return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                } else {
                    ResponseDTO responseDTO = ScleraUtils.generatePayload(null, 200, true);
                    log.info("Ioc Data Not Found by Ioc Id : {}. Endpoint: {}", iocId, httpServletRequest.getRequestURI());
                    return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                }

            } else {
                log.error("Role not authorized.EndPoint:{}", httpServletRequest.getRequestURI());
                throw new ClientException("Role not Authorised ", 702, httpServletRequest.getRequestURI());
            }
        } else {
            log.error("Error! Invalid client parameters:EndPoint:{}", httpServletRequest.getRequestURI());
            throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
        }

    }

    public ResponseEntity<?> addIocData(String orgId, String email, IocDto iocData, String
            loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload: OrgId: {}, Email: {}, IocDto: {}, LoggedInUser: {}", orgId, email, iocData, loggedInUser);
        if (orgId != null && email != null && iocData != null) {
//            boolean authorized = scleraRoleCheckUtils.checkRole(loggedInUser, httpServletRequest, "super-admin", "admin", "org-admin", "user", "master-vendor", "vendor", "master-user", "property-admin");
//            if (authorized) {
            String id = Generators.timeBasedGenerator().generate().toString();
            iocData.setId(id);
            iocData.setUsername(generateClientId());
            iocData.setPassword(generateClientSecret());
            ResponseEntity<ResponseDTO> loginIocData = webClientService.addIocData(orgId, email, iocData, loggedInUser, httpServletRequest);
            log.info("Response: {}", loginIocData);
            if (loginIocData.getStatusCode().is2xxSuccessful()) {
                iocRepository.addIocData(id, iocData.getName(), iocData.getServer_url(), iocData.getWeb_url(), orgId);
            }
            ResponseDTO responseDTO = ScleraUtils.generatePayload(iocData, 200, true);
            userActionLogService.addUserActionLog(loggedInUser, "Ioc", "ADD", "IOC Data Added Successfully,Name:" + iocData.getName(), "success");
            log.info("Adding Ioc Data by organisation Id: {}. Endpoint: {}", orgId, httpServletRequest.getRequestURI());
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
//            } else {
//                log.error("Role not authorized.EndPoint:{}", httpServletRequest.getRequestURI());
//                userActionLogService.addUserActionLog(loggedInUser, "Ioc", "ADD", "Role Not Authorised", "failed");
//                throw new ClientException("Role not Authorised ", 702, httpServletRequest.getRequestURI());
//            }

        } else {
            log.error("Error! Invalid client parameters:EndPoint:{}", httpServletRequest.getRequestURI());
            userActionLogService.addUserActionLog(loggedInUser, "Ioc", "ADD", "Invalid Client Parameters", "failed");
            throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
        }
    }

    public ResponseEntity<?> editIocData(String orgId, String email, IocDto iocData, String
            loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload: OrgId: {}, Email: {}, IocDto: {}, LoggedInUser: {}", orgId, email, iocData, loggedInUser);
        if (orgId != null && email != null && iocData != null) {
//            boolean authorized = scleraRoleCheckUtils.checkRole(loggedInUser, httpServletRequest, "super-admin", "admin", "org-admin", "user", "master-vendor", "vendor", "master-user", "property-admin");
//            if (authorized) {
            iocRepository.editIocData(iocData.getName(), iocData.getServer_url(), iocData.getWeb_url(), iocData.getId(), orgId);
            ResponseDTO responseDTO = ScleraUtils.generatePayload(null, 200, true);
            userActionLogService.addUserActionLog(loggedInUser, "Ioc", "UPDATE", "IOC Data Updated Successfully,Name:" + iocData.getName(), "success");
            log.info("Editing Ioc Data by organisation Id: {} and Ioc Id : {}. Endpoint: {}", orgId, iocData.getId(), httpServletRequest.getRequestURI());
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
//            } else {
//                log.error("Role not authorized.EndPoint:{}", httpServletRequest.getRequestURI());
//                userActionLogService.addUserActionLog(loggedInUser, "Ioc", "UPDATE", "Role Not Authorised", "failed");
//                throw new ClientException("Role not Authorised ", 702, httpServletRequest.getRequestURI());
//            }
        } else {
            log.error("Error! Invalid client parameters:EndPoint:{}", httpServletRequest.getRequestURI());
            userActionLogService.addUserActionLog(loggedInUser, "Ioc", "UPDATE", "Invalid Client Parameters", "failed");
            throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
        }
    }

    public ResponseEntity<?> deleteIocData(String orgId, String email, String iocId, String
            loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload: OrgId: {}, Email: {}, iocId: {}, LoggedInUser: {}", orgId, email, iocId, loggedInUser);
        if (orgId != null && email != null && iocId != null) {
//            boolean authorized = scleraRoleCheckUtils.checkRole(loggedInUser, httpServletRequest, "super-admin", "admin", "org-admin", "user", "master-vendor", "vendor", "master-user", "property-admin");
//            if (authorized) {
            ResponseEntity<ResponseDTO> loginIocData = webClientService.deleteIocData(orgId, email, iocId, loggedInUser, httpServletRequest);
            log.info("Response: {}", loginIocData);
            if (loginIocData.getStatusCode().is2xxSuccessful()) {
                iocRepository.deleteIocData(iocId, orgId);
            }
            ResponseDTO responseDTO = ScleraUtils.generatePayload(null, 200, true);
            userActionLogService.addUserActionLog(loggedInUser, "Ioc", "DELETE", "IOC Data Deleted Successfully", "success");
            log.info("Deleted Ioc Data by organisation Id: {} and Ioc Id : {}. Endpoint: {}", orgId, iocId, httpServletRequest.getRequestURI());
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
//            } else {
//                log.error("Role not authorized.EndPoint:{}", httpServletRequest.getRequestURI());
//                userActionLogService.addUserActionLog(loggedInUser, "Ioc", "DELETE", "Role Not Authorised", "failed");
//                throw new ClientException("Role not Authorised ", 702, httpServletRequest.getRequestURI());
//            }

        } else {
            log.error("Error! Invalid client parameters:EndPoint:{}", httpServletRequest.getRequestURI());
            userActionLogService.addUserActionLog(loggedInUser, "Ioc", "DELETE", "Invalid Client Parameters", "failed");
            throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
        }
    }

    private String generateClientId() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] randomBytes = new byte[20];
        do {
            secureRandom.nextBytes(randomBytes);
        } while (randomBytes[0] == 0);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    private String generateClientSecret() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] randomBytes = new byte[40];
        do {
            secureRandom.nextBytes(randomBytes);
        } while (randomBytes[0] == 0);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    public ResponseEntity<?> getAllOrgIdAndCompanyName(String email, String loggedInUser, HttpServletRequest
            httpServletRequest) {
        log.info("Email: {}, LoggedInUser: {}", email, loggedInUser);
        if (email != null && loggedInUser != null) {
//            boolean authorized = scleraRoleCheckUtils.checkRole(loggedInUser, httpServletRequest, "super-admin", "admin");
//            if (authorized) {
            List<CustomerOrganisationDto> data = customerOrganisationService.getAllOrgIdAndCompanyName(httpServletRequest);
            ResponseDTO responseDTO = ScleraUtils.generatePayload(data, 200, true);
            log.info("Fetching All Organisation Id and Company Name. Endpoint: {}", httpServletRequest.getRequestURI());
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
//            } else {
//                log.error("Role not authorized.EndPoint:{}", httpServletRequest.getRequestURI());
//                throw new ClientException("Role not Authorised ", 702, httpServletRequest.getRequestURI());
//            }
        } else {
            log.error("Error! Invalid client parameters:EndPoint:{}", httpServletRequest.getRequestURI());
            throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
        }

    }


    public void deleteIocDataByOrgId(String orgId, HttpServletRequest httpServletRequest) {
        log.info("Successfully Deleting Ioc data For orgId:{},EndPoint:{}", orgId, httpServletRequest.getRequestURI());
        iocRepository.deleteIocDataByOrgId(orgId);
    }
}
