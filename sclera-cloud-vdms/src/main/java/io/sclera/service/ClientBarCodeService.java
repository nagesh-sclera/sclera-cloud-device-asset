package io.sclera.service;


import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.fasterxml.uuid.Generators;
import io.sclera.dto.*;
import io.sclera.exception.ClientException;
import io.sclera.repository.ClientBarCodeRepository;
import io.sclera.repository.UserRepository;
import io.sclera.repository.VdmsRepository;
import io.sclera.repository.VdmsVisibilityRepository;
import io.sclera.util.ScleraUtils;
import io.sclera.util.SocketUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigInteger;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@Slf4j
public class ClientBarCodeService {

    @Autowired
    private ClientBarCodeRepository clientBarCodeRepository;

    @Autowired
    private VdmsService vdmsService;

    @Autowired
    private UserActionLogService userActionLogService;

    @Autowired
    private UserActivityService userActivityService;

    @Autowired
    private WebClientService webClientService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProxyProfileService proxyProfileService;

    @Autowired
    private VdmsRepository vdmsRepository;

    @Autowired
    private DockerService dockerService;

    @Autowired
    private VdmsCoordinatesService vdmsCoordinatesService;

    @Autowired
    private VdmsVisibilityRepository vdmsVisibilityRepository;

    @Autowired
    private SocketUtils socketUtils;


    public ResponseEntity<ResponseDTO> getClientBarCodeCountByVdmsId(String vdmsId, HttpServletRequest httpServletRequest) {
        log.info("Payload:VdmsId:{}", vdmsId);
        Integer count = clientBarCodeRepository.getClientBarCodeCountByVdmsId(vdmsId);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("clientBarCodeCount", count);
        ResponseDTO responseDTO = ScleraUtils.generatePayload(jsonObject, 200, true);
        log.info("Successfully Fetching Client Barcode Count:{},EndPoint:{}", count, httpServletRequest.getRequestURI());
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    public ResponseEntity<ResponseDTO> getAllClientBarCodeByVdmsId(String vdmsId, @Min(1) @Max(1000) int pageNo, @Min(1) @Max(1000) int pageSize, HttpServletRequest httpServletRequest) {
        log.info("Payload:VdmsId:{},PageNo:{},PageSize:{}", vdmsId, pageNo, pageSize);
        int offset = pageSize * (pageNo - 1);
        List<ClientBarCodeDTO> clientBarCodeDTOS = clientBarCodeRepository.getAllClientBarCodeByVdmsId(vdmsId, pageSize, offset);
        log.info("Fetching All Client BarCode Data For Vdms:{},Endpoint:{}", vdmsId, httpServletRequest.getRequestURI());
        ResponseDTO responseDTO = ScleraUtils.generatePayload(clientBarCodeDTOS, 200, true);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    public ResponseEntity<ResponseDTO> updateBarCodeSyncByVdmsId(String vdmsId, HttpServletRequest httpServletRequest) {
        log.info("Payload:VdmsId:{}", vdmsId);
        vdmsService.updateBarCodeSyncByVdmsId(0, vdmsId, httpServletRequest);
        clientBarCodeRepository.updateBarCodeSyncByVdmsId(0, vdmsId);
        ResponseDTO responseDTO = ScleraUtils.generatePayload("SuccessFully Updated BarCode Sync", 200, true);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    public ResponseEntity<?> tagClientBarCode(String orgId, String email, ClientBarCodeDTO clientBarCodeDTO, String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload: ClientBarCodeDTO:{}, OrgId:{}, Email:{}, LoggedInUser:{}", clientBarCodeDTO, orgId, email, loggedInUser);
        BigInteger updatedAt = BigInteger.valueOf(System.currentTimeMillis());
        if (clientBarCodeDTO != null) {
            ClientBarCodeDTO clientBarCodeData = clientBarCodeRepository.getClientBarCodeDetailsByClientBarCodeId(clientBarCodeDTO.getClientBarCodeId());
            if (clientBarCodeData != null) {
                String batchId = Generators.timeBasedGenerator().generate().toString();
                clientBarCodeRepository.tagClientBarCode(clientBarCodeDTO.getDeviceId(), clientBarCodeDTO.getLocationId(), clientBarCodeDTO.getVdmsId(),
                        updatedAt, loggedInUser, batchId, 1, clientBarCodeDTO.getClientBarCodeId());
                vdmsService.updateBarCodeSyncByVdmsId(1, clientBarCodeDTO.getVdmsId(), httpServletRequest);
                ResponseDTO responseDTO = ScleraUtils.generatePayload("Client BarCode Details Updated Successfully", 200, true);
                log.info("Client BarCode Details Updated Successfully. EndPoint {}", httpServletRequest.getRequestURI());
                String from = "";
                String to = "";

                if (clientBarCodeDTO.getDeviceId() != null) {
                    if (clientBarCodeData.getDeviceId() != null) {
                        from = "Device ID: " + clientBarCodeData.getDeviceId();
                    } else {
                        from = clientBarCodeData.getLocationId() != null ? "Location ID: " + clientBarCodeData.getLocationId() : "unknown";
                    }
                    to = "Device ID: " + clientBarCodeDTO.getDeviceId();
                } else if (clientBarCodeDTO.getLocationId() != null) {
                    if (clientBarCodeData.getDeviceId() != null) {
                        from = "Device ID: " + clientBarCodeData.getDeviceId();
                    } else {
                        from = clientBarCodeData.getLocationId() != null ? "Location ID: " + clientBarCodeData.getLocationId() : "unknown";
                    }
                    to = "Location ID: " + clientBarCodeDTO.getLocationId();
                }

                VdmsSyncDTO vdmsSyncDTO = VdmsSyncDTO.builder()
                        .barcode_sync(1)
                        .build();
                int isMultiTenant = vdmsService.getMultiTenantCheck(clientBarCodeDTO.getVdmsId());
                if (isMultiTenant == 1) {
                    String awsRegion = vdmsService.getAwsRegionByVdmsId(clientBarCodeDTO.getVdmsId());
                    webClientService.multiTenantSyncApiCall(clientBarCodeDTO.getVdmsId(), vdmsSyncDTO,awsRegion,httpServletRequest);
                } else {
                    socketUtils.invokeWebSocketEndpoint("/topic/vdms/" + clientBarCodeDTO.getVdmsId() + "/sync/data", vdmsSyncDTO);
                }
                userActivityService.addUserActivityLogs(loggedInUser, "client_bar_code", clientBarCodeDTO.getDeviceId() != null ? "device" : "location", "UPDATE", "success", "Client BarCode is tagged to "
                        + (clientBarCodeDTO.getDeviceId() != null ? "Device ID: " + clientBarCodeDTO.getDeviceId() : "Location ID: " + clientBarCodeDTO.getLocationId()), clientBarCodeDTO.getDeviceId() != null
                        ? clientBarCodeDTO.getDeviceId() : clientBarCodeDTO.getLocationId(), clientBarCodeDTO.getVdmsId());
                userActionLogService.addUserActionLog(email, "ClientBarCode", "UPDATE", "Client BarCode is tagged from "
                        + from
                        + " to "
                        + to
                        + " in VDMS "
                        + clientBarCodeDTO.getVdmsId(), "success");

                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            } else {
                String id = Generators.timeBasedGenerator().generate().toString();
                BigInteger addedAt = BigInteger.valueOf(System.currentTimeMillis());
                clientBarCodeRepository.addClientBarCode(id, clientBarCodeDTO.getClientBarCodeId(), addedAt, email, clientBarCodeDTO.getDeviceId(),
                        clientBarCodeDTO.getLocationId(), clientBarCodeDTO.getVdmsId(), Generators.timeBasedGenerator().generate().toString(), 1);
                vdmsService.updateBarCodeSyncByVdmsId(1, clientBarCodeDTO.getVdmsId(), httpServletRequest);
                VdmsSyncDTO vdmsSyncDTO = VdmsSyncDTO.builder()
                        .barcode_sync(1)
                        .build();
                int isMultiTenant = vdmsService.getMultiTenantCheck(clientBarCodeDTO.getVdmsId());
                if (isMultiTenant == 1) {
                    String awsRegion = vdmsService.getAwsRegionByVdmsId(clientBarCodeDTO.getVdmsId());
                    webClientService.multiTenantSyncApiCall(clientBarCodeDTO.getVdmsId(), vdmsSyncDTO,awsRegion,httpServletRequest);
                } else {
                socketUtils.invokeWebSocketEndpoint("/topic/vdms/" + clientBarCodeDTO.getVdmsId() + "/sync/data", vdmsSyncDTO);
                }
                ResponseDTO responseDTO = ScleraUtils.generatePayload("Client BarCode Details Added Successfully", 200, true);
                log.info("Client BarCode Details Added Successfully. EndPoint {}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(email, "ClientBarCode", "ADD", "Client BarCode with ID: " + clientBarCodeDTO.getClientBarCodeId() + " added successfully", "success");
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            }
        } else {
            log.error("Invalid client params, EndPoint:  {}", httpServletRequest.getRequestURI());
            userActivityService.addUserActivityLogs(loggedInUser, "client_bar_code", null, "UPDATE", "failed", "Invalid client params", null, clientBarCodeDTO.getVdmsId());
            userActionLogService.addUserActionLog(loggedInUser, "ClientBarCode", "UPDATE", "Invalid client params", "failed");
            throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
        }
    }

    public ResponseEntity<?> getClientBarCodeProxyProfileByVdmsId(String loggedInUser, JSONObject body, HttpServletRequest httpServletRequest) {
        log.info("JSONObject: {}", body);
        if (body.get("vdmsId") != null || body.get("clientBarCodeId") != null) {
            JSONObject data = new JSONObject();
            // barCode
            boolean isExpired = true;
            if (body.get("token") != null) {
                isExpired = this.checkToken(body.get("token").toString(), httpServletRequest);
                if (isExpired) {
                    // if token is expred
                    String username = loggedInUser;
                    if (username == null) {
                        username = this.getUsername(body.get("token").toString());
                    }
                    log.info("UserName:{}", username);
                    String orgId = vdmsService.getOrgIdByBarCodeId(body.get("clientBarCodeId").toString());
                    log.info("orgId:{}", orgId);
                    if(orgId != null) {
                        ResponseDTO response = webClientService.getUserDetailsAndProxyProfileDetailsByEmail(username, orgId);
                        log.info("Response from Login:{}", response);
                        JSONObject privilege = JSONObject.parseObject(JSON.toJSONString(response.getData()));
                        ClientBarCodeDTO barCodeData = clientBarCodeRepository.getBarCodeDataByClientBarCodeId(body.get("clientBarCodeId").toString());
                        log.info("barCodeData1:" + barCodeData);
                        if (barCodeData != null && barCodeData.getVdmsId() != null) {
                            ProxyProfileDTO proxyProfileData = proxyProfileService.getVdmsProxyProfileDataByVdmsId(barCodeData.getVdmsId(), httpServletRequest);

                            String role = privilege.get("role").toString();
                            String checkAccess = null;
                            if (role.contains("master-user") || role.contains("org-admin")) {
                                checkAccess = userRepository.checkVdmsVisbilityByEmail(barCodeData.getVdmsId(), privilege.get("organisation_id").toString(), username);

                            } else if (role.contains("master-vendor")) {
                                String vdmsId = dockerService.getVdmsAccessByOrganisationIdAndVdmsId(barCodeData.getVdmsId(), privilege.get("organisation_id").toString());
                                if (vdmsId != null && vdmsId.equals(barCodeData.getVdmsId())) {
                                    checkAccess = username;
                                }
                            } else if (role.contains("guest")) {
                                checkAccess = username;
                            } else {
                                checkAccess = vdmsVisibilityRepository.checkVisibleVdmsByEmailAndVdmsId(username, barCodeData.getVdmsId());
                            }
                            if (checkAccess != null && checkAccess.equals(username)) {
                                data.put("vdms_id", barCodeData.getVdmsId());
                                data.put("proxy_profile", proxyProfileData);
                                data.put("location_id", barCodeData.getLocationId());
                                data.put("device_id", barCodeData.getDeviceId());
                                data.put("errorCode", 0);   // has vdms access // barCode linked to vdms
                            } else {
                                data.put("errorCode", 1);  // vdms access denied
                            }
                            if (barCodeData.getLocationId() != null && (privilege.get("organisation_id").toString().equals("99950026"))) { //removed || privilege.get("organisation_id").toString().equals("85161001") for testing
                                data.put("authPageType", 1);
                            }
                        } else {
                            // barCode not linked to vdms
                            data.put("errorCode", 2);
                        }
                        data.put("isAuthenticated", 0);
                        ResponseDTO responseDTO = ScleraUtils.generatePayload(data, 200, false);
                        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                    }else{
                        log.info("BarCode is not tagged with any organisation. EndPoint:{}", httpServletRequest.getRequestURI());
                        data.put("errorCode", 2);
                        data.put("isAuthenticated", 0);
                        log.info("Response:{}", data);
                        ResponseDTO responseDTO = ScleraUtils.generatePayload(data, 200, false);
                        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                    }
                } else {
                    // if token is not expred
                    String username = loggedInUser;
                    if (username == null) {
                        username = this.getUsername(body.get("token").toString());
                    }
                    log.info("UserName:{}", username);
                    String orgId = vdmsService.getOrgIdByBarCodeId(body.get("clientBarCodeId").toString());
                    log.info("orgId:{}", orgId);
                    if(orgId != null) {
                        ResponseDTO response = webClientService.getUserDetailsAndProxyProfileDetailsByEmail(username, orgId);
                        log.info("Response from Login:{}", response);
                        JSONObject privilege = JSONObject.parseObject(JSON.toJSONString(response.getData()));
                        ClientBarCodeDTO barCodeData = clientBarCodeRepository.getBarCodeDataByClientBarCodeId(body.get("clientBarCodeId").toString());
                        log.info("barCodeData2:" + barCodeData);
                        if (barCodeData != null && barCodeData.getVdmsId() != null) {
                            ProxyProfileDTO proxyProfileData = proxyProfileService.getVdmsProxyProfileDataByVdmsId(barCodeData.getVdmsId(), httpServletRequest);
                            String role = privilege.get("role").toString();
                            String checkAccess = null;
                            if (role.contains("master-user") || role.contains("org-admin")) {
                                checkAccess = userRepository.checkVdmsVisbilityByEmail(barCodeData.getVdmsId(), privilege.get("organisation_id").toString(), username);

                            } else if (role.contains("master-vendor")) {
                                String vdmsId = dockerService.getVdmsAccessByOrganisationIdAndVdmsId(barCodeData.getVdmsId(), privilege.get("organisation_id").toString());
                                if (vdmsId != null && vdmsId.equals(barCodeData.getVdmsId())) {
                                    checkAccess = username;
                                }
                            } else if (role.contains("guest")) {
                                checkAccess = username;
                            } else {
                                checkAccess = vdmsVisibilityRepository.checkVisibleVdmsByEmailAndVdmsId(username, barCodeData.getVdmsId());
                            }
                            if (checkAccess != null && checkAccess.equals(username)) {
                                data.put("vdms_id", barCodeData.getVdmsId());
                                String property_name = vdmsRepository.getPropertyNameByVdmsId(barCodeData.getVdmsId());
                                data.put("property_name", property_name);
                                data.put("location_id", barCodeData.getLocationId());
                                data.put("device_id", barCodeData.getDeviceId());
                                data.put("proxy_profile", proxyProfileData);


                                if (body.containsKey("currentPosition")) {
                                    if (body.getString("currentPosition") != null) {
                                        String vdmsId = barCodeData.getVdmsId();
                                        String coordinatesId = vdmsRepository.getCoordinatesVdmsId(vdmsId);
                                        if (coordinatesId != null) {
                                            String coordinates = vdmsCoordinatesService.getCoordinatesById(coordinatesId);
                                            float lat = body.getJSONArray("currentPosition").getFloat(0);
                                            float lng = body.getJSONArray("currentPosition").getFloat(1);
                                            Long result = vdmsCoordinatesService.checkIsPresent(lat, lng, coordinates);

                                            log.info("RESULT : {}", result);

                                            if (result == null) {
                                                data.put("isWithinPremise", 1);
                                            } else {
                                                if (result == 0) {
                                                    data.put("isWithinPremise", 0);
                                                } else {
                                                    data.put("isWithinPremise", 1);
                                                }
                                            }
                                        } else {
                                            data.put("isWithinPremise", 1);
                                        }
                                    } else {
                                        data.put("isWithinPremise", 1);
                                    }

                                } else {
                                    data.put("isWithinPremise", 1);
                                }


                                data.put("errorCode", 0);   // has vdms access // barCode linked to vdms
                            } else {
                                data.put("errorCode", 1);  // vdms access denied
                            }
                        } else {
                            // barCode not linked to vdms
                            data.put("errorCode", 2);
                        }
                        data.put("email", username);
                        data.put("organisation_id", privilege.get("organisation_id"));
//                    data.put("privileges", privilege.get("privileges"));
                        data.put("role", privilege.get("role"));
                        data.put("sclerafx_windows_version", privilege.get("sclerafx_windows_version"));
                        data.put("sclerafx_ubuntu_version", privilege.get("sclerafx_ubuntu_version"));
                        data.put("sclerafx_mac_version", privilege.get("sclerafx_mac_version"));
                        data.put("is_enterprise", privilege.get("is_enterprise"));
                        data.put("terms_and_conditions", privilege.get("terms_and_conditions"));
                        data.put("isAuthenticated", 1);
                        ResponseDTO responseDTO = ScleraUtils.generatePayload(data, 200, false);
                        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                    }else{
                        log.info("BarCode is not tagged with any organisation. EndPoint:{}", httpServletRequest.getRequestURI());
                        data.put("errorCode", 2);
                        data.put("isAuthenticated", 1);
                        log.info("Response:{}", data);
                        ResponseDTO responseDTO = ScleraUtils.generatePayload(data, 200, false);
                        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                    }
                }
            } else {
                //if token not given

                ClientBarCodeDTO barCodeData = clientBarCodeRepository.getBarCodeDataByClientBarCodeId(body.get("clientBarCodeId").toString());
                log.info("barCodeData3:" + barCodeData);
                if (barCodeData != null && barCodeData.getVdmsId() != null) {
                    ProxyProfileDTO proxyProfileData = proxyProfileService.getVdmsProxyProfileDataByVdmsId(barCodeData.getVdmsId(), httpServletRequest);
                    data.put("vdms_id", barCodeData.getVdmsId());
                    data.put("location_id", barCodeData.getLocationId());
                    data.put("device_id", barCodeData.getDeviceId());
                    data.put("proxy_profile", proxyProfileData); // barCode linked to vdms
                    data.put("errorCode", 0);

                    String orgId = vdmsRepository.getOrgIdByVdmsId(barCodeData.getVdmsId());

                    if (barCodeData.getLocationId() != null && orgId != null &&
                            (orgId.equals("99950026") )) { //removed || orgId.equals("85161001") for testing
                        data.put("authPageType", 1);
                    }
                } else {
                    // barCode not linked to vdms
                    data.put("errorCode", 2);
                }
                data.put("isAuthenticated", 0);
                ResponseDTO responseDTO = ScleraUtils.generatePayload(data, 200, false);
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);

            }
        } else {
            log.error("Invalid client param : EndPoint {}", httpServletRequest.getRequestURI());
            userActionLogService.addUserActionLog(null, "ClientBarCode", "ADD", "Invalid Client Parameters", "failed");
            throw new ClientException("Invalid client param", 700, httpServletRequest.getRequestURI());
        }
    }


    public String getUsername(String token) {
        return JWT.decode(token).getClaim("email").asString() != null
                ? JWT.decode(token).getClaim("email").asString()
                : JWT.decode(token).getClaim("sub").asString();
    }


    public Boolean checkToken(String token, HttpServletRequest httpServletRequest) {
        log.info("Payload: token: {}", token);

        if (token != null) {
            try {
                Date expiration = JWT.decode(token).getExpiresAt();
                Instant expirationInstant = expiration.toInstant();
                Instant currentTime = Instant.now();
                boolean isExpired = expirationInstant.isBefore(currentTime);
                return isExpired;
            } catch (JWTDecodeException | SignatureVerificationException exception) {
                return true;
            }

        } else {
            return true;
        }
    }

    public void deleteClientBarCodeByVdmsId(String vdmsId, HttpServletRequest httpServletRequest) {
        clientBarCodeRepository.deleteClientBarCodeByVdmsId(vdmsId);
    }

    public ResponseEntity<?> getClientBarCodeDetailsByVdmsIdAndDeviceId(String vdmsId, String deviceId, HttpServletRequest httpServletRequest) {
        log.info("Payload: VdmsId:{}, DeviceId:{}", vdmsId, deviceId);
        List<ClientBarCodeDTO> clientBarCodeDTOS = clientBarCodeRepository.getClientBarCodeDetailsByVdmsIdAndDeviceId(vdmsId, deviceId);
        if (clientBarCodeDTOS != null) {
            ResponseDTO responseDTO = ScleraUtils.generatePayload(clientBarCodeDTOS, 200, true);
            log.info("Fetching Client BarCode details by vdmsId: {} and deviceId {}, EndPoint: {}", vdmsId, deviceId, httpServletRequest.getRequestURI());
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } else {
            log.error("Client BarCode does not exist, EndPoint:  {}", httpServletRequest.getRequestURI());
            throw new ClientException("Client BarCode does not exist", 799, httpServletRequest.getRequestURI());
        }
    }

    public ResponseEntity<?> getClientBarCodeDetailsByVdmsIdAndLocationId(String vdmsId, String locationId, HttpServletRequest httpServletRequest) {
        log.info("Payload: VdmsId:{}, LocationId:{}", vdmsId, locationId);
        List<ClientBarCodeDTO> clientBarCodeDTOS = clientBarCodeRepository.getClientBarCodeDetailsByVdmsIdAndLocationId(vdmsId, locationId);
        if (clientBarCodeDTOS != null) {
            ResponseDTO responseDTO = ScleraUtils.generatePayload(clientBarCodeDTOS, 200, true);
            log.info("Fetching Client BarCode details by vdmsId: {} and locationId: {},EndPoint: {}", vdmsId, locationId, httpServletRequest.getRequestURI());
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } else {
            log.error("Client BarCode does not exist, EndPoint:  {}", httpServletRequest.getRequestURI());
            throw new ClientException("Client BarCode does not exist", 799, httpServletRequest.getRequestURI());
        }
    }


    public ResponseEntity<?> importClientBarCode(
            String orgId, String email, MultipartFile file, String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload: OrgId:{}, Email:{}, LoggedInUser:{}", orgId, email, loggedInUser);
        if (orgId != null && email != null) {
            String contentType = file.getContentType();

            if (contentType != null) {
                // convert MultipartFile to ByteArray using MultipartFileDTO
                MultipartFileDTO multipartFileDTO = file != null ? processMultipartFiles(file) : new MultipartFileDTO();
                // convert the MultipartFileDTO back to MultipartFile
                MultipartFile multipartFile = multipartFileDTO.toMultipartFile();

                if (contentType.equals("application/vnd.ms-excel") || contentType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) {
                    log.info("Importing users from excel file.");
                    this.importClientBarCodeFromExcel(email, multipartFile, loggedInUser, httpServletRequest);

                    ResponseDTO responseDTO = ScleraUtils.generatePayload("Client Bar-Code Details Upserted Successfully", 200, true);
                    log.info("Client Bar-Code Details Upserted Successfully.");
                    userActionLogService.addUserActionLog(email, "ClientBarCode", "ADD", "Client Bar-Code Upserted Successfully", "success");
                    return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                } else {
                    log.error("Invalid File Type.");
                    userActivityService.addUserActivityLogs(loggedInUser, "client_bar_code", null, "UPDATE", "failed", "Invalid File Type", null, null);
                    userActionLogService.addUserActionLog(loggedInUser, "ClientBarCode", "UPDATE", "Invalid File Type", "failed");
                    throw new ClientException("Invalid File Type", 700, null);
                }
            } else {
                log.error("Invalid client params.");
                userActivityService.addUserActivityLogs(loggedInUser, "client_bar_code", null, "UPDATE", "failed", "Invalid client params", null, null);
                userActionLogService.addUserActionLog(loggedInUser, "ClientBarCode", "UPDATE", "Invalid client param", "failed");
                throw new ClientException("Invalid client params", 700, null);
            }
        } else {
            log.error("Invalid client params.");
            userActivityService.addUserActivityLogs(loggedInUser, "client_bar_code", null, "UPDATE", "failed", "Invalid client params", null, null);
            userActionLogService.addUserActionLog(loggedInUser, "ClientBarCode", "UPDATE", "Invalid client param", "failed");
            throw new ClientException("Invalid client params", 700, null);
        }
    }


    private void importClientBarCodeFromExcel(String email, MultipartFile file, String loggedInUser, HttpServletRequest httpServletRequest) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Runnable task =
                () -> {
                    try {
                        // Load the workbook from the input stream
                        Workbook workbook;
                        workbook = WorkbookFactory.create(file.getInputStream());

                        // Get the sheet by index or name
                        Sheet sheet = workbook.getSheetAt(0); // Assuming the first sheet is the one to fetch

                        // Find the column indexes based on column names
                        int clientBarCodeIdIndex = -1;
                        int deviceIdIndex = -1;
                        int locationIdIndex = -1;
                        int vdmsIdIndex = -1;

                        List<String> vdmsIdsForSync1 = new ArrayList<>();
                        List<String> vdmsIdsForSync2 = new ArrayList<>();

                        Row headerRow = sheet.getRow(0);
                        for (Cell cell : headerRow) {
                            String columnName = cell.getStringCellValue();
                            if (columnName.equalsIgnoreCase("client_bar_code_id")) {
                                clientBarCodeIdIndex = cell.getColumnIndex();
                            } else if (columnName.equalsIgnoreCase("device_id")) {
                                deviceIdIndex = cell.getColumnIndex();
                            } else if (columnName.equalsIgnoreCase("location_id")) {
                                locationIdIndex = cell.getColumnIndex();
                            } else if (columnName.equalsIgnoreCase("vdms_id")) {
                                vdmsIdIndex = cell.getColumnIndex();
                            }
                        }

                        // Process the sheet data
                        if (clientBarCodeIdIndex != -1 && deviceIdIndex != -1 && locationIdIndex != -1 && vdmsIdIndex != -1) {
                            String batchId = Generators.timeBasedGenerator().generate().toString();
                            for (Row row : sheet) {
                                // Skip the header row
                                if (row.getRowNum() == 0) {
                                    continue;
                                }
                                Cell ClientBarCodeIdCell = row.getCell(clientBarCodeIdIndex);
                                Cell deviceIdCell = row.getCell(deviceIdIndex);
                                Cell locationIdCell = row.getCell(locationIdIndex);
                                Cell vdmsIdCell = row.getCell(vdmsIdIndex);

                                String clientBarCodeId = (String) this.getCellValue(ClientBarCodeIdCell);
                                String deviceId = (String) this.getCellValue(deviceIdCell);
                                String locationId = (String) this.getCellValue(locationIdCell);
                                String vdmsId = (String) this.getCellValue(vdmsIdCell);

                                if (clientBarCodeId != null && !clientBarCodeId.isEmpty() && vdmsId != null && !vdmsId.isEmpty()) {
                                    ClientBarCodeDTO clientBarCodeData = clientBarCodeRepository.getClientBarCodeDetailsByClientBarCodeId(clientBarCodeId);

                                    if (clientBarCodeData != null) {
                                        BigInteger updatedAt = BigInteger.valueOf(System.currentTimeMillis());
                                        String existingVdmsId = clientBarCodeData.getVdmsId();
                                        if (!existingVdmsId.equals(vdmsId)) {
                                            if (!vdmsIdsForSync1.contains(vdmsId)) {
                                                vdmsIdsForSync1.add(vdmsId);
                                            }
                                            if (!vdmsIdsForSync2.contains(existingVdmsId)) {
                                                vdmsIdsForSync2.add(existingVdmsId);
                                            }
                                        } else {
                                            int qrCodeSyncState = vdmsService.getBarCodeSyncByVdmsId(vdmsId);
                                            if (qrCodeSyncState == 2) {
                                                if (!vdmsIdsForSync2.contains(vdmsId)) {
                                                    vdmsIdsForSync2.add(vdmsId);
                                                }
                                            } else {
                                                if (!vdmsIdsForSync1.contains(vdmsId)) {
                                                    vdmsIdsForSync1.add(vdmsId);
                                                }
                                            }
                                        }
                                        clientBarCodeRepository.tagClientBarCode(deviceId, locationId, vdmsId, updatedAt, loggedInUser, batchId, 1, clientBarCodeId);
//                                        if (!vdmsIds.contains(vdmsId)) {
//                                            vdmsIds.add(vdmsId);
//                                        }
                                        log.info("Client Bar-Code Details Updated Successfully.");
                                        String from = "";
                                        String to = "";
                                        if (clientBarCodeData.getDeviceId() != null) {
                                            from = "Device ID: " + clientBarCodeData.getDeviceId();
                                        } else if (clientBarCodeData.getLocationId() != null) {
                                            from = "Location ID: " + clientBarCodeData.getLocationId();
                                        }
                                        if (deviceId != null) {
                                            to = "Device ID: " + deviceId;
                                        } else if (locationId != null) {
                                            to = "Location ID: " + locationId;
                                        }

                                        userActionLogService.addUserActionLog(email, "ClientBarCode", "UPDATE", "Client Bar code is tagged from " + from + " to " + to + " in VDMS " + vdmsId, "success");
                                    } else {
                                        String id = Generators.timeBasedGenerator().generate().toString();
                                        BigInteger addedAt = BigInteger.valueOf(System.currentTimeMillis());
                                        if (!vdmsIdsForSync1.contains(vdmsId)) {
                                            vdmsIdsForSync1.add(vdmsId); // Add new vdmsId for sync
                                        }
                                        clientBarCodeRepository.addClientBarCode(id, clientBarCodeId, addedAt, email, deviceId, locationId, vdmsId, batchId, 1);
                                        if (!vdmsIdsForSync1.contains(vdmsId)) {
                                            vdmsIdsForSync1.add(vdmsId); // Add new vdmsId for sync
                                        }
                                        log.info("Client Bar-Code Details Added Successfully.");
                                        userActionLogService.addUserActionLog(email, "ClientBarCode", "ADD", "Client Bar-Code With Id: " + clientBarCodeId + " Added Successfully", "success");
                                    }
                                }
                            }
                            log.info("Before");
                            log.info("1:{}", vdmsIdsForSync1);
                            log.info("2:{}", vdmsIdsForSync2);
                            vdmsIdsForSync1.removeAll(vdmsIdsForSync2);
                            log.info("After");
                            log.info("1:{}", vdmsIdsForSync1);
                            log.info("2:{}", vdmsIdsForSync2);

                            if (!vdmsIdsForSync1.isEmpty()) {
                                log.info("update qr sync for 1");
                                vdmsService.updateBarCodeSyncByVdmsIds(1, vdmsIdsForSync1, httpServletRequest);
                            }
                            if (!vdmsIdsForSync2.isEmpty()) {
                                log.info("update qr sync for 2");
                                vdmsService.updateBarCodeSyncByVdmsIds(2, vdmsIdsForSync2, httpServletRequest);
                            }
                            VdmsSyncDTO vdmsSyncDTO = VdmsSyncDTO.builder()
                                    .barcode_sync(1)
                                    .build();
                            for (String vdmsId : vdmsIdsForSync1) {
                                int isMultiTenant = vdmsService.getMultiTenantCheck(vdmsId);
                                if (isMultiTenant == 1) {
                                    String awsRegion = vdmsService.getAwsRegionByVdmsId(vdmsId);
                                    webClientService.multiTenantSyncApiCall(vdmsId, vdmsSyncDTO,awsRegion,httpServletRequest);
                                } else {
                                    socketUtils.invokeWebSocketEndpoint("/topic/vdms/" + vdmsId + "/sync/data", vdmsSyncDTO);
                                }
                            }
                            for (String vdmsId : vdmsIdsForSync2) {
                                int isMultiTenant = vdmsService.getMultiTenantCheck(vdmsId);
                                if (isMultiTenant == 1) {
                                    String awsRegion = vdmsService.getAwsRegionByVdmsId(vdmsId);
                                    webClientService.multiTenantSyncApiCall(vdmsId, vdmsSyncDTO,awsRegion,httpServletRequest);
                                } else {
                                    socketUtils.invokeWebSocketEndpoint("/topic/vdms/" + vdmsId + "/sync/data", vdmsSyncDTO);
                                }
                            }
                        }
                        workbook.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                        log.error("Error: {}", e.getLocalizedMessage());
                    }
                };
        executorService.execute(task);
        executorService.shutdown();
    }


    private Object getCellValue(Cell cell) {
        if (cell == null) {
            return null;
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue();
                } else {
                    double numericValue = cell.getNumericCellValue();
                    // Convert numeric value to long if it represents an integer, and then to string
                    if (numericValue == (long) numericValue) {
                        return String.valueOf((long) numericValue);
                    } else {
                        return String.valueOf(numericValue);
                    }
                }
            case BOOLEAN:
                return cell.getBooleanCellValue();
            case FORMULA:
                try {
                    return cell.getStringCellValue();
                } catch (IllegalStateException e) {
                    double numericValue = cell.getNumericCellValue();
                    // Convert numeric value to long if it represents an integer, and then to string
                    if (numericValue == (long) numericValue) {
                        return String.valueOf((long) numericValue);
                    } else {
                        return String.valueOf(numericValue);
                    }
                }
            default:
                return null;
        }
    }

    private MultipartFileDTO processMultipartFiles(MultipartFile file) {
        MultipartFileDTO multipartFileDTO = new MultipartFileDTO();

        try {
            String id = Generators.timeBasedGenerator().generate().toString();
            byte[] bytes = file.getBytes();
            String originalFilename = file.getOriginalFilename();
            String name = file.getName();
            String contentType = file.getContentType();

            multipartFileDTO.setId(id);
            multipartFileDTO.setBytes(bytes);
            multipartFileDTO.setOriginalFilename(originalFilename);
            multipartFileDTO.setName(name);
            multipartFileDTO.setContentType(contentType);
        } catch (IOException e) {
            log.error("Error: {}", e.getLocalizedMessage());
            throw new RuntimeException(e);
        }
        return multipartFileDTO;
    }

    public ResponseEntity<?> getClientBarCodeDetailsByClientBarCodeId(String clientBarCodeId, HttpServletRequest httpServletRequest) {
        log.info("Payload: ClientBarCodeId:{}", clientBarCodeId);
        ClientBarCodeDTO clientBarCodeDTO =
                clientBarCodeRepository.getClientBarCodeDetailsByClientBarCodeId(clientBarCodeId);
        log.info("ClientBarCodeDTO: {}", clientBarCodeDTO);
        if (clientBarCodeDTO != null) {
            ResponseDTO responseDTO = ScleraUtils.generatePayload(clientBarCodeDTO, 200, true);
            log.info("Fetching ClientBarCode details by clientBarCodeId {}, EndPoint :{}", clientBarCodeId, httpServletRequest.getRequestURI());
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } else {
            ResponseDTO responseDTO = ScleraUtils.generatePayload(null, 200, true);
            log.error("ClientBarCode does not exist, EndPoint:  {}", httpServletRequest.getRequestURI());
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        }
    }

    public ResponseEntity<ResponseDTO> getClientBarCodeRecordsByVdmsIdAndLastSyncTime(String orgId, String email, String vdmsId, BigInteger lastSyncTime, String loggedInUser, @Min(1) @Max(1000) int pageNo, @Min(1) @Max(1000) int pageSize, HttpServletRequest httpServletRequest) {
        log.info("Payload: OrgId: {}, Email: {}, VdmsId: {}, LastSyncTime: {}, LoggedInUser: {},pageNo:{},pageSize:{}", orgId, email, vdmsId, lastSyncTime, loggedInUser, pageNo, pageSize);
        int offset = pageSize * (pageNo - 1);
        List<ClientBarCodeDTO> barCodeDTOS = clientBarCodeRepository.getBarCodeRecordsByVdmsIdAndLastSyncTime(vdmsId, lastSyncTime, pageSize, offset);

        if (barCodeDTOS != null) {
            ResponseDTO responseDTO = ScleraUtils.generatePayload(barCodeDTOS, 200, true);
            log.info("Fetching QR Code Records By VdmsId: {} And LastSyncTime: {}. Endpoint: {}", vdmsId, lastSyncTime, httpServletRequest.getRequestURI());
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } else {
            ResponseDTO responseDTO = ScleraUtils.generatePayload(null, 200, true);
            log.error("QR Code Records does not exist, EndPoint: {}", httpServletRequest.getRequestURI());
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        }
    }

    public ResponseEntity<ResponseDTO> getBarCodeCountsByVdmsId(String vdmsId, BigInteger lastSyncTime, HttpServletRequest httpServletRequest) {
        log.info("Payload:vdmsId:{},lastSyncTime:{}", vdmsId, lastSyncTime);
        int barCodeCount = clientBarCodeRepository.getBarCodeCountsByVdsId(vdmsId, lastSyncTime);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("BarCodeCount", barCodeCount);

        ResponseDTO responseDTO = ScleraUtils.generatePayload(jsonObject, 200, true);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    public ResponseEntity<ResponseDTO> getSyncedClientBarCodeByVdmsId(String vdmsId, int pageNo, int pageSize, HttpServletRequest httpServletRequest) {
        log.info("Payload:VdmsId:{},PageNo:{},PageSize:{}", vdmsId, pageNo, pageSize);
        int offset = pageSize * (pageNo - 1);
        List<ClientBarCodeDTO> clientBarCodeDTOS = clientBarCodeRepository.getSyncedClientBarCodeByVdmsId(vdmsId, pageSize, offset);
        log.info("Fetching All Client BarCode Data For Vdms:{},Endpoint:{}", vdmsId, httpServletRequest.getRequestURI());
        ResponseDTO responseDTO = ScleraUtils.generatePayload(clientBarCodeDTOS, 200, true);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }


    public ResponseEntity<ResponseDTO> tagClientBarCodeByVdmsId(String orgId, String vdmsId, List<ClientBarCodeDTO> clientBarCodeDTOList, HttpServletRequest httpServletRequest) {
        log.info("Tagging {} Client BarCodes for vdmsId: {}, orgId: {}", clientBarCodeDTOList.size(), vdmsId, orgId);
        if (!clientBarCodeDTOList.isEmpty() && vdmsId != null) {
            BigInteger updatedAt = BigInteger.valueOf(System.currentTimeMillis());
            String batchId = Generators.timeBasedGenerator().generate().toString();
            for (ClientBarCodeDTO clientBarCodeDTO : clientBarCodeDTOList) {
                clientBarCodeRepository.tagAdcClientBarCode(clientBarCodeDTO.getDeviceId(), clientBarCodeDTO.getLocationId(), vdmsId,
                        updatedAt, clientBarCodeDTO.getUpdatedBy(), batchId, 1, orgId, clientBarCodeDTO.getClientBarCodeId());

                userActivityService.addUserActivityLogs(clientBarCodeDTO.getUpdatedBy(), "client_bar_code", clientBarCodeDTO.getDeviceId() != null ? "device" : "location", "UPDATE", "success", "Client BarCode is tagged to "
                        + (clientBarCodeDTO.getDeviceId() != null ? "Device ID: " + clientBarCodeDTO.getDeviceId() : "Location ID: " + clientBarCodeDTO.getLocationId()), clientBarCodeDTO.getDeviceId() != null
                        ? clientBarCodeDTO.getDeviceId() : clientBarCodeDTO.getLocationId(), vdmsId);
                userActionLogService.addUserActionLog(clientBarCodeDTO.getUpdatedBy(), "ClientBarCode", "UPDATE", "Client BarCode with id: " + clientBarCodeDTO.getClientBarCodeId() + "is tagged to " + (clientBarCodeDTO.getDeviceId() != null ? "Device ID: " + clientBarCodeDTO.getDeviceId() : "Location ID: " + clientBarCodeDTO.getLocationId())
                        + " in VDMS " + vdmsId, "success");

            }
            vdmsService.updateBarCodeSyncByVdmsId(1, vdmsId, httpServletRequest);
            VdmsSyncDTO vdmsSyncDTO = VdmsSyncDTO.builder()
                    .barcode_sync(1)
                    .build();
            int isMultiTenant = vdmsService.getMultiTenantCheck(vdmsId);
            if (isMultiTenant == 1) {
                String awsRegion = vdmsService.getAwsRegionByVdmsId(vdmsId);
                webClientService.multiTenantSyncApiCall(vdmsId, vdmsSyncDTO,awsRegion,httpServletRequest);
            } else {
                socketUtils.invokeWebSocketEndpoint("/topic/vdms/" + vdmsId + "/sync/data", vdmsSyncDTO);
            }
            ResponseDTO responseDTO = ScleraUtils.generatePayload("Client BarCode Details has been Updated Successfully", 200, true);
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } else {
            log.error("Invalid client params, EndPoint:  {}", httpServletRequest.getRequestURI());
            userActivityService.addUserActivityLogs(null, "client_bar_code", null, "UPDATE", "failed", "Invalid client params", null, vdmsId);
            userActionLogService.addUserActionLog(null, "ClientBarCode", "UPDATE", "Invalid client params", "failed");
            throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
        }
    }

    public ResponseEntity<ResponseDTO> getAdcCheckByClientBarCodeId(String clientBarCodeId) {
        log.info("Fetching ADC Check by clientBarCodeId: {}", clientBarCodeId);
        ClientBarCodeDTO clientBarCodeDTO = new ClientBarCodeDTO();
        int isIdPresentInDb = clientBarCodeRepository.getClientBarCodeId(clientBarCodeId);
        if (isIdPresentInDb == 0) {
            clientBarCodeDTO.setIsTagged(2);
        } else {
            int isTagged = clientBarCodeRepository.getIsAdcTagged(clientBarCodeId);
            clientBarCodeDTO.setIsTagged(isTagged);
            int count = clientBarCodeRepository.getAdcCheckByClientBarCodeId(clientBarCodeId);
            if (count == 1) {
                clientBarCodeDTO.setIsAdc(true);
            } else {
                clientBarCodeDTO.setIsAdc(false);
            }
        }
        ResponseDTO responseDTO = ScleraUtils.generatePayload(clientBarCodeDTO, 200, true);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    public ResponseEntity<ResponseDTO> addClientBarCode(ClientBarCodeDTO clientBarCodeDTO, HttpServletRequest httpServletRequest) {
        if (clientBarCodeDTO != null) {
            log.info("Adding Client BarCode with details: {}", clientBarCodeDTO);
            String id = Generators.timeBasedGenerator().generate().toString();
            BigInteger addedAt = BigInteger.valueOf(System.currentTimeMillis());
            clientBarCodeRepository.addAdcClientBarCode(id, clientBarCodeDTO.getClientBarCodeId(), addedAt, clientBarCodeDTO.getAddedBy(),clientBarCodeDTO.getDeviceId(),1);
            userActionLogService.addUserActionLog(clientBarCodeDTO.getAddedBy(), "ClientBarCode", "ADD", "Client BarCode with id: " + clientBarCodeDTO.getClientBarCodeId() + " is added successfully", "success");
            ResponseDTO responseDTO = ScleraUtils.generatePayload("Successfully added Client BarCode", 200, true);
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } else {
            userActionLogService.addUserActionLog(null, "ClientBarCode", "ADD", "Invalid client params", "failed");
            throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
        }
    }

    public ResponseEntity<ResponseDTO> updateClientBarCodeById(String clientBarCodeId, ClientBarCodeDTO clientBarCodeDTO) {
        log.info("Updating ClientBarCode for id:{}",clientBarCodeId);
        BigInteger updatedAt = BigInteger.valueOf(System.currentTimeMillis());
        clientBarCodeRepository.updateClientBarCodeById(clientBarCodeDTO.getDeviceId(),clientBarCodeDTO.getUpdatedBy(),updatedAt,clientBarCodeId);
        ResponseDTO responseDTO = ScleraUtils.generatePayload("Client BarCode has been tagged to device with id: " + clientBarCodeDTO.getDeviceId() + " successfully", 200, true);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }
}
