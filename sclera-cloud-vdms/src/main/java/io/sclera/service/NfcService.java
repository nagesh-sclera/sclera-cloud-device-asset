package io.sclera.service;


import com.alibaba.fastjson2.JSONObject;
import com.fasterxml.uuid.Generators;
import io.sclera.dto.ClientNfcDTO;
import io.sclera.dto.NfcDTO;
import io.sclera.dto.ResponseDTO;
import io.sclera.dto.VdmsSyncDTO;
import io.sclera.exception.ClientException;
import io.sclera.repository.NfcRepository;
import io.sclera.util.ScleraRoleCheckUtils;
import io.sclera.util.ScleraUtils;
import io.sclera.util.SocketUtils;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class NfcService {
    @Autowired
    private UserActivityService userActivityService;
    @Autowired
    private NfcRepository nfcRepository;
    @Autowired
    private ScleraRoleCheckUtils scleraRoleCheckUtils;
    @Autowired
    private UserActionLogService userActionLogService;
    @Autowired
    private ClientNfcService clientNfcService;
    @Autowired
    private VdmsService vdmsService;
    @Autowired
    private SocketUtils socketUtils;
    @Autowired
    private WebClientService webClientService;


    public ResponseEntity<?> addNFC(NfcDTO nfcDTO, String email, String vdmsId, String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload : NfcDTO {},email {}, vdmsId:{}, loggedInUser {}", nfcDTO, email, vdmsId, loggedInUser);
        if (email != null && vdmsId != null && loggedInUser != null) {
            boolean access = scleraRoleCheckUtils.checkRole(loggedInUser, httpServletRequest, "super-admin", "admin", "master-user", "org-admin", "user", "master-vendor", "vendor", "property-admin");
            if (access) {
                String id = Generators.timeBasedGenerator().generate().toString();
                nfcRepository.addNFC(id, nfcDTO.getLocationId(), nfcDTO.getDeviceId(), nfcDTO.getUid(), vdmsId, System.currentTimeMillis(), email, 1);
                vdmsService.updateNfcSyncByVdmsId(1, vdmsId, httpServletRequest);
                ResponseDTO responseDTO = ScleraUtils.generatePayload(null, 200, true);
                if (nfcDTO.getDeviceId() != null) {

                    userActivityService.addUserActivityLogs(loggedInUser, "nfc", "device", "ADD", "success",
                            "A device With Id:" + nfcDTO.getDeviceId() + " Is Tagged", nfcDTO.getDeviceId(), nfcDTO.getVdmsId());

                    userActionLogService.addUserActionLog(loggedInUser, "NFC", "ADD",
                            "A device With Id:" + nfcDTO.getDeviceId() + " Is Tagged", "success");

                } else {

                    userActivityService.addUserActivityLogs(loggedInUser, "nfc", "location", "ADD", "success",
                            "A location With Id:" + nfcDTO.getLocationId() + " Is Tagged", nfcDTO.getLocationId(), nfcDTO.getVdmsId());

                    userActionLogService.addUserActionLog(loggedInUser, "NFC", "ADD",
                            "A location With Id:" + nfcDTO.getLocationId() + " Is Tagged", "success");

                }
                VdmsSyncDTO vdmsSyncDTO = VdmsSyncDTO.builder()
                        .nfc_sync(1)
                        .build();
                int isMultiTenant = vdmsService.getMultiTenantCheck(vdmsId);
                if (isMultiTenant == 1) {
                    String awsRegion = vdmsService.getAwsRegionByVdmsId(vdmsId);
                    webClientService.multiTenantSyncApiCall(vdmsId, vdmsSyncDTO,awsRegion,httpServletRequest);
                } else {
                    socketUtils.invokeWebSocketEndpoint("/topic/vdms/" + vdmsId + "/sync/data", vdmsSyncDTO);
                }
                log.info("Added NFC details to database successfully. EndPoint: {}", httpServletRequest.getRequestURI());
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            } else {
                log.error("Role not authorized.EndPoint:{}", httpServletRequest.getRequestURI());
                userActivityService.addUserActivityLogs(loggedInUser, "nfc", null, "ADD", "failed",
                        "Role Not Authorized", null, nfcDTO.getVdmsId());
                userActionLogService.addUserActionLog(loggedInUser, "NFC", "ADD", "Role Not Authorized", "failed");
                throw new ClientException("Role not Authorised ", 702, httpServletRequest.getRequestURI());
            }
        } else {
            log.error("Invalid client param : EndPoint {}", httpServletRequest.getRequestURI());
            userActivityService.addUserActivityLogs(loggedInUser, "nfc", null, "ADD", "failed",
                    "Invalid client param", null, nfcDTO.getVdmsId());
            userActionLogService.addUserActionLog(loggedInUser, "NFC", "ADD", "Invalid client param", "failed");
            throw new ClientException("Invalid client param : EndPoint ;{}", 700, httpServletRequest.getRequestURI());
        }
    }

    public ResponseEntity<?> getNfcDetailsByDeviceIdAndVdmsId(String deviceId, String vdmsId, String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload : deviceId {} , vdmsId {} , loggedInUser {}", deviceId, vdmsId, loggedInUser);
        if (deviceId != null && vdmsId != null && loggedInUser != null) {
            boolean access = scleraRoleCheckUtils.checkRole(loggedInUser, httpServletRequest, "super-admin", "admin", "master-user", "org-admin", "user", "master-vendor", "vendor", "property-admin");
            if (access) {
                List<NfcDTO> nfcDTOS = nfcRepository.getNfcDetailsByDeviceIdAndVdmsId(deviceId, vdmsId);

                List<ClientNfcDTO> clientNfcDetails = clientNfcService.getClientNfcDetailsByVdmsIdAndDeviceIds(vdmsId, List.of(deviceId));
                if (!clientNfcDetails.isEmpty()) {
                    for (int i = 0; i < clientNfcDetails.size(); i++) {

                        NfcDTO clientNfcDTO = new NfcDTO();
                        clientNfcDTO.setId(clientNfcDetails.get(i).getNfc_id());
                        clientNfcDTO.setLocationId(clientNfcDetails.get(i).getLocationId());
                        clientNfcDTO.setDeviceId(clientNfcDetails.get(i).getDeviceId());
                        clientNfcDTO.setVdmsId(clientNfcDetails.get(i).getVdmsId());
                        clientNfcDTO.setCreatedBy(clientNfcDetails.get(i).getCreatedBy());
                        clientNfcDTO.setCreationTime(clientNfcDetails.get(i).getCreationTime());
                        nfcDTOS.add(clientNfcDTO);

                    }
                }


                ResponseDTO responseDTO = ScleraUtils.generatePayload(nfcDTOS, 200, true);
                log.info("Fetching NFC info from db by deviceId {} ,vdmsId {} ,EndPoint: {}", deviceId, vdmsId, httpServletRequest.getRequestURI());
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            } else {
                log.error("Role not authorized.EndPoint:{}", httpServletRequest.getRequestURI());
                throw new ClientException("Role not Authorised ", 702, httpServletRequest.getRequestURI());
            }
        } else {
            log.error("Invalid client param : EndPoint {}", httpServletRequest.getRequestURI());
            throw new ClientException("Invalid client param : EndPoint ;{}", 700, httpServletRequest.getRequestURI());
        }

    }

    public ResponseEntity<?> getNfcDetailsByLocationIdAndVdmsId(String locationId, String vdmsId, String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload : locationId {} , vdmsId {} , loggedInUser {} ", locationId, vdmsId, loggedInUser);
        if (locationId != null && vdmsId != null && loggedInUser != null) {
            boolean access = scleraRoleCheckUtils.checkRole(loggedInUser, httpServletRequest, "super-admin", "admin", "master-user", "org-admin", "user", "master-vendor", "vendor", "property-admin");
            if (access) {
                List<NfcDTO> nfcDTOS = nfcRepository.getNfcDetailsByLocationIdAndVdmsId(locationId, vdmsId);

                List<ClientNfcDTO> clientNfcDetails = clientNfcService.getClientNfcDetailsByVdmsIdAndLocationIds(vdmsId, List.of(locationId));
                if (!clientNfcDetails.isEmpty()) {
                    for (int i = 0; i < clientNfcDetails.size(); i++) {

                        NfcDTO clientNfcDTO = new NfcDTO();
                        clientNfcDTO.setId(clientNfcDetails.get(i).getNfc_id());
                        clientNfcDTO.setLocationId(clientNfcDetails.get(i).getLocationId());
                        clientNfcDTO.setDeviceId(clientNfcDetails.get(i).getDeviceId());
                        clientNfcDTO.setVdmsId(clientNfcDetails.get(i).getVdmsId());
                        clientNfcDTO.setCreatedBy(clientNfcDetails.get(i).getCreatedBy());
                        clientNfcDTO.setCreationTime(clientNfcDetails.get(i).getCreationTime());
                        nfcDTOS.add(clientNfcDTO);

                    }
                }


                ResponseDTO responseDTO = ScleraUtils.generatePayload(nfcDTOS, 200, true);
                log.info("Fetching NFC info from db by locationId {} ,vdmsId {} ,EndPoint: {}", locationId, vdmsId, httpServletRequest.getRequestURI());
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            } else {
                log.error("Role not authorized.EndPoint:{}", httpServletRequest.getRequestURI());
                throw new ClientException("Role not Authorised ", 702, httpServletRequest.getRequestURI());
            }
        } else {
            log.error("Invalid client param : EndPoint {}", httpServletRequest.getRequestURI());
            throw new ClientException("Invalid client param : EndPoint ;{}", 700, httpServletRequest.getRequestURI());
        }
    }

    public void deleteNfcByVdmsId(String vdmsId, HttpServletRequest httpServletRequest) {
        log.info("Successfully Deleting Nfc data For vdms_Id:{},EndPoint:{}", vdmsId, httpServletRequest.getRequestURI());
        nfcRepository.deleteNfcDataByVdmsId(vdmsId);
    }

    public ResponseEntity<ResponseDTO> getNfcRecordsByVdmsId(String orgId, String email, String vdmsId, String loggedInUser, @Min(1) @Max(1000) int pageNo, @Min(1) @Max(1000) int pageSize, HttpServletRequest httpServletRequest) {
        log.info(
                "Payload: OrgId: {}, Email: {}, VdmsId: {}, LoggedInUser: {},pageNo:{},pageSize:{}",
                orgId,
                email,
                vdmsId,
                loggedInUser,
                pageNo,
                pageSize);

        int offset = pageSize * (pageNo - 1);
        List<NfcDTO> nfcDTOList =
                nfcRepository.getNfcRecordsByVdmsId(vdmsId, pageSize, offset);

        if (nfcDTOList != null) {
            ResponseDTO responseDTO = ScleraUtils.generatePayload(nfcDTOList, 200, true);
            log.info(
                    "Fetching NFC Records By VdmsId: {} Endpoint: {}",
                    vdmsId,
                    httpServletRequest.getRequestURI());
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } else {
            ResponseDTO responseDTO = ScleraUtils.generatePayload(null, 200, true);
            log.error(
                    "NFC Records does not exist, EndPoint: {}",
                    httpServletRequest.getRequestURI());
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        }

    }

    public ResponseEntity<ResponseDTO> updateNfcDetails(String vdmsId, List<NfcDTO> nfcDTOS, HttpServletRequest httpServletRequest) {
        log.info("Payload:NfcDTO:{},vdmsId:{}", nfcDTOS, vdmsId);

        nfcDTOS.forEach(nfcDTO -> {
            int checkNfc = nfcRepository.checkNfcId(nfcDTO.getId());
            if (checkNfc == 1) {
                NfcDTO existingNfcDetails = nfcRepository.getNfcDetailsById(nfcDTO.getId());
                String existingVdmsId = existingNfcDetails.getVdmsId();
                if (!existingVdmsId.equals(vdmsId)) {
                    vdmsService.updateNfcSyncByVdmsId(1, vdmsId, httpServletRequest);
                    vdmsService.updateQrCodeSyncByVdmsId(2, existingVdmsId, httpServletRequest);
                } else {
                    int nfcSyncState = vdmsService.getNfcSyncStateByVdmsId(vdmsId);
                    if (nfcSyncState == 2) {
                        vdmsService.updateNfcSyncByVdmsId(2, vdmsId, httpServletRequest);
                    } else {
                        vdmsService.updateNfcSyncByVdmsId(1, vdmsId, httpServletRequest);
                    }
                }
                VdmsSyncDTO vdmsSyncDTO = VdmsSyncDTO.builder()
                        .nfc_sync(1)
                        .build();
                int isMultiTenant = vdmsService.getMultiTenantCheck(vdmsId);
                if (isMultiTenant == 1) {
                    String awsRegion = vdmsService.getAwsRegionByVdmsId(vdmsId);
                    webClientService.multiTenantSyncApiCall(vdmsId, vdmsSyncDTO,awsRegion,httpServletRequest);
                } else {
                    socketUtils.invokeWebSocketEndpoint("/topic/vdms/" + vdmsId + "/sync/data", vdmsSyncDTO);
                }
                nfcRepository.updateNfcDetailsById(nfcDTO.getDeviceId(), nfcDTO.getLocationId(), nfcDTO.getId());
            } else {
                log.info("nfc id :{} not exist ", nfcDTO.getId());
                userActionLogService.addUserActionLog("successfully updated nfc details", "NFC", "UPDATE", "nfc not exist for id:" + nfcDTO.getId(), "failed");
            }
        });
        ResponseDTO responseDTO = ScleraUtils.generatePayload(null, 200, true);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    public ResponseEntity<ResponseDTO> getNfcCounts(String vdmsId, HttpServletRequest httpServletRequest) {
        log.info("Payload:vdmsId:{}", vdmsId);
        int nfcCounts = nfcRepository.getNfcCountsByVdmsId(vdmsId);
        int clientNfcCounts = clientNfcService.getClientNfcCounts(vdmsId, httpServletRequest);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("nfcCount", nfcCounts);
        jsonObject.put("clientNfcCount", clientNfcCounts);
        ResponseDTO responseDTO = ScleraUtils.generatePayload(jsonObject, 200, true);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    public ResponseEntity<ResponseDTO> getUnTaggedNfc( String vdmsId, String loggedInUser, @Min(1) @Max(1000) int pageNo, @Min(1) @Max(1000) int pageSize, HttpServletRequest httpServletRequest) {
        int offset = pageSize * (pageNo - 1);
        List<String> ids = nfcRepository.getUnTaggedNfc( pageSize, offset);
        ResponseDTO responseDTO = ScleraUtils.generatePayload(ids, 200, true);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }
}
