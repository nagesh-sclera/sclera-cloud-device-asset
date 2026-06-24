package io.sclera.service;

import com.fasterxml.uuid.Generators;
import io.sclera.dto.*;
import io.sclera.exception.ClientException;
import io.sclera.repository.ClientQrCodeRepository;
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
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@Slf4j
public class ClientQrCodeService {

    @Autowired
    private ClientQrCodeRepository clientQrCodeRepository;
    @Autowired
    private UserActionLogService userActionLogService;
    @Autowired
    private VdmsService vdmsService;
    @Autowired
    private UserActivityService userActivityService;
    @Autowired
    private SocketUtils socketUtils;
    @Autowired
    private WebClientService webClientService;

    public ResponseEntity<?> tagClientQrCode(String orgId, String email, ClientQrCodeDTO clientQrCodeDTO, String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload: ClientQrCodeDTO:{}, OrgId:{}, Email:{}, LoggedInUser:{}", clientQrCodeDTO, orgId, email, loggedInUser);
        BigInteger updatedAt = BigInteger.valueOf(System.currentTimeMillis());
        if (clientQrCodeDTO != null) {
            ClientQrCodeDTO clientQrCodeData = clientQrCodeRepository.getClientQrCodeDetailsByClientQrCodeId(clientQrCodeDTO.getClientQrCodeId());
            if (clientQrCodeData != null) {
                String batchId = Generators.timeBasedGenerator().generate().toString();

                ClientQrCodeDTO existingQrCodeData = clientQrCodeRepository.getClientQrCodeDetailsByClientQrCodeId(clientQrCodeDTO.getClientQrCodeId());
                String existingVdmsId = existingQrCodeData.getVdmsId();
                String newVdmsId = clientQrCodeDTO.getVdmsId();

                if (!existingVdmsId.equals(newVdmsId)) {
                    vdmsService.updateQrCodeSyncByVdmsId(1, newVdmsId, httpServletRequest);
                    vdmsService.updateQrCodeSyncByVdmsId(2, existingVdmsId, httpServletRequest);
                } else {
                    int qrCodeSyncState = vdmsService.getQrCodeSyncByVdmsId(newVdmsId);
                    if (qrCodeSyncState == 2) {
                        vdmsService.updateQrCodeSyncByVdmsId(2, newVdmsId, httpServletRequest);
                    } else {
                        vdmsService.updateQrCodeSyncByVdmsId(1, newVdmsId, httpServletRequest);
                    }
                }
                clientQrCodeRepository.tagClientQrCode(clientQrCodeDTO.getDeviceId(), clientQrCodeDTO.getLocationId(), clientQrCodeDTO.getVdmsId(), updatedAt, loggedInUser, batchId, 1, clientQrCodeDTO.getClientQrCodeId());

                ResponseDTO responseDTO = ScleraUtils.generatePayload("Client Qr-Code Details Updated Successfully", 200, true);
                log.info("Client Qr-Code Details Updated Successfully. EndPoint {}", httpServletRequest.getRequestURI());
                String from = "";
                String to = "";

                if (clientQrCodeDTO.getDeviceId() != null) {
                    if (clientQrCodeData.getDeviceId() != null) {
                        from = "Device ID: " + clientQrCodeData.getDeviceId();
                    } else {
                        from = clientQrCodeData.getLocationId() != null ? "Location ID: " + clientQrCodeData.getLocationId() : "unknown";
                    }
                    to = "Device ID: " + clientQrCodeDTO.getDeviceId();
                } else if (clientQrCodeDTO.getLocationId() != null) {
                    if (clientQrCodeData.getDeviceId() != null) {
                        from = "Device ID: " + clientQrCodeData.getDeviceId();
                    } else {
                        from = clientQrCodeData.getLocationId() != null ? "Location ID: " + clientQrCodeData.getLocationId() : "unknown";
                    }
                    to = "Location ID: " + clientQrCodeDTO.getLocationId();
                }
                VdmsSyncDTO vdmsSyncDTO = VdmsSyncDTO.builder().qr_sync(1).build();
                int isMultiTenant = vdmsService.getMultiTenantCheck(clientQrCodeDTO.getVdmsId());
                if (isMultiTenant == 1) {
                    String awsRegion = vdmsService.getAwsRegionByVdmsId(clientQrCodeDTO.getVdmsId());
                    webClientService.multiTenantSyncApiCall(clientQrCodeDTO.getVdmsId(), vdmsSyncDTO,awsRegion, httpServletRequest);
                } else {
                    socketUtils.invokeWebSocketEndpoint("/topic/vdms/" + clientQrCodeDTO.getVdmsId() + "/sync/data", vdmsSyncDTO);
                }
                userActivityService.addUserActivityLogs(loggedInUser, "client_qr_code", clientQrCodeDTO.getDeviceId() != null ? "device" : "location", "UPDATE", "success", "Client QR code is tagged to " + (clientQrCodeDTO.getDeviceId() != null ? "Device ID: " + clientQrCodeDTO.getDeviceId() : "Location ID: " + clientQrCodeDTO.getLocationId()), clientQrCodeDTO.getDeviceId() != null ? clientQrCodeDTO.getDeviceId() : clientQrCodeDTO.getLocationId(), clientQrCodeDTO.getVdmsId());
                userActionLogService.addUserActionLog(email, "ClientQrCode", "UPDATE", "Client QR code is tagged from " + from + " to " + to + " in VDMS " + clientQrCodeDTO.getVdmsId(), "success");

                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            } else {
                String id = Generators.timeBasedGenerator().generate().toString();
                BigInteger addedAt = BigInteger.valueOf(System.currentTimeMillis());
                clientQrCodeRepository.addClientQrCode(id, clientQrCodeDTO.getClientQrCodeId(), addedAt, email, clientQrCodeDTO.getDeviceId(), clientQrCodeDTO.getLocationId(), clientQrCodeDTO.getVdmsId(), Generators.timeBasedGenerator().generate().toString(), 1);
                vdmsService.updateQrCodeSyncByVdmsId(1, clientQrCodeDTO.getVdmsId(), httpServletRequest);
                VdmsSyncDTO vdmsSyncDTO = VdmsSyncDTO.builder().qr_sync(1).build();
                int isMultiTenant = vdmsService.getMultiTenantCheck(clientQrCodeDTO.getVdmsId());
                if (isMultiTenant == 1) {
                    String awsRegion = vdmsService.getAwsRegionByVdmsId(clientQrCodeDTO.getVdmsId());
                    webClientService.multiTenantSyncApiCall(clientQrCodeDTO.getVdmsId(), vdmsSyncDTO,awsRegion, httpServletRequest);
                } else {
                    socketUtils.invokeWebSocketEndpoint("/topic/vdms/" + clientQrCodeDTO.getVdmsId() + "/sync/data", vdmsSyncDTO);
                }
                ResponseDTO responseDTO = ScleraUtils.generatePayload("Client Qr-Code Details Added Successfully", 200, true);
                log.info("Client Qr-Code Details Added Successfully. EndPoint {}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(email, "ClientQrCode", "ADD", "Client Qr-Code with ID: " + clientQrCodeDTO.getClientQrCodeId() + " added successfully", "success");
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            }
        } else {
            log.error("Invalid client params, EndPoint:  {}", httpServletRequest.getRequestURI());
            userActivityService.addUserActivityLogs(loggedInUser, "client_qr_code", null, "UPDATE", "failed", "Invalid client params", null, clientQrCodeDTO.getVdmsId());
            userActionLogService.addUserActionLog(loggedInUser, "ClientQrCode", "UPDATE", "Invalid client params", "failed");
            throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
        }
    }

    public ClientQrCodeDTO getQrCodeDataByClientQrCodeId(String clientQrCodeId) {
        log.info("Payload: Client-Qr-Code-Id:{}", clientQrCodeId);
        return clientQrCodeRepository.getQrCodeDataByClientQrCodeId(clientQrCodeId);
    }

    public ResponseEntity<?> getClientQrCodeDetailsByVdmsIdAndDeviceId(
            String vdmsId, String deviceId, HttpServletRequest httpServletRequest) {
        log.info("Payload: VdmsId:{}, DeviceId:{}", vdmsId, deviceId);
        List<ClientQrCodeDTO> clientQrCodeDTOS =
                clientQrCodeRepository.getClientQrCodeDetailsByVdmsIdAndDeviceId(vdmsId, deviceId);
        if (clientQrCodeDTOS != null) {
            ResponseDTO responseDTO = ScleraUtils.generatePayload(clientQrCodeDTOS, 200, true);
            log.info(
                    "Fetching ClientQrCode details by vdmsId: {} and deviceId {}, EndPoint: {}",
                    vdmsId,
                    deviceId,
                    httpServletRequest.getRequestURI());
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } else {
            log.error("ClientQrCode does not exist, EndPoint:  {}", httpServletRequest.getRequestURI());
            throw new ClientException(
                    "ClientQrCode does not exist", 799, httpServletRequest.getRequestURI());
        }
    }

    public ResponseEntity<?> getClientQrCodeDetailsByVdmsIdAndLocationId(
            String vdmsId, String locationId, HttpServletRequest httpServletRequest) {
        log.info("Payload: VdmsId:{}, LocationId:{}", vdmsId, locationId);
        List<ClientQrCodeDTO> qrCodeDTO =
                clientQrCodeRepository.getClientQrCodeDetailsByVdmsIdAndLocationId(vdmsId, locationId);
        if (qrCodeDTO != null) {
            ResponseDTO responseDTO = ScleraUtils.generatePayload(qrCodeDTO, 200, true);
            log.info(
                    "Fetching ClientQrCode details by vdmsId: {} and locationId: {},EndPoint: {}",
                    vdmsId,
                    locationId,
                    httpServletRequest.getRequestURI());
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } else {
            log.error("ClientQrCode does not exist, EndPoint:  {}", httpServletRequest.getRequestURI());
            throw new ClientException(
                    "ClientQrCode does not exist", 799, httpServletRequest.getRequestURI());
        }
    }

    public ResponseEntity<?> getClientQrCodeDetailsByClientQrCodeId(String encodedClientQrCodeId, HttpServletRequest httpServletRequest) {
        String clientQrCodeId = decodeInput(encodedClientQrCodeId);
        log.info("Payload: ClientQrCodeId:{}", clientQrCodeId);
        ClientQrCodeDTO clientQrCodeDTO =
                clientQrCodeRepository.getClientQrCodeDetailsByClientQrCodeId(clientQrCodeId);
        log.info("ClientQrCodeDTO: {}", clientQrCodeDTO);
        if (clientQrCodeDTO != null) {
            ResponseDTO responseDTO = ScleraUtils.generatePayload(clientQrCodeDTO, 200, true);
            log.info(
                    "Fetching ClientQrCode details by clientQrCodeId {}, EndPoint :{}",
                    clientQrCodeId,
                    httpServletRequest.getRequestURI());
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } else {
            ResponseDTO responseDTO = ScleraUtils.generatePayload(null, 200, true);
            log.error("ClientQrCode does not exist, EndPoint:  {}", httpServletRequest.getRequestURI());
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        }
    }

    public List<ClientQrCodeDTO> getClientQrCodeDetailsByVdmsIdAndDeviceIds(
            String vdmsId, List<String> clientQrCodeIds) {
        log.info("Payload: VDMS ID:{},Client-Qr-Code-Ids:{}", vdmsId, clientQrCodeIds);
        return clientQrCodeRepository.getClientQrCodeDetailsByVdmsIdAndDeviceIds(
                vdmsId, clientQrCodeIds);
    }

    public List<ClientQrCodeDTO> getClientQrCodeDetailsByVdmsIdAndLocationIds(
            String vdmsId, List<String> clientQrCodeIds) {
        log.info("Payload: VDMS ID:{},Client-Qr-Code-Ids:{}", vdmsId, clientQrCodeIds);
        return clientQrCodeRepository.getClientQrCodeDetailsByVdmsIdAndLocationIds(
                vdmsId, clientQrCodeIds);
    }

    public List<String> getClientQrCodeDeviceIdsByVdmsId(String vdmsId) {
        return clientQrCodeRepository.getClientQrCodeDeviceIdsByVdmsId(vdmsId);
    }

    public List<String> getClientQrCodeLocationIdsByVdmsId(String vdmsId) {
        return clientQrCodeRepository.getClientQrCodeLocationIdsByVdmsId(vdmsId);
    }

    public List<ClientQrCodeDTO> getClientQrCodeTaggedDevicesDetailsByVdmsId(String vdmsId) {
        return clientQrCodeRepository.getClientQrCodeTaggedDevicesDetailsByVdmsId(vdmsId);
    }

    public List<ClientQrCodeDTO> getClientQrCodeTaggedLocationsDetailsByVdmsId(String vdmsId) {
        return clientQrCodeRepository.getClientQrCodeTaggedLocationsDetailsByVdmsId(vdmsId);
    }

    public ResponseEntity<?> importClientQrCode(String orgId, String email, MultipartFile file, String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload: OrgId:{}, Email:{}, LoggedInUser:{}", orgId, email, loggedInUser);
        if (orgId != null && email != null) {
            // convert MultipartFile to ByteArray using MultipartFileDTO
            MultipartFileDTO multipartFileDTO = file != null ? processMultipartFiles(file) : new MultipartFileDTO();
            // convert the MultipartFileDTO back to MultipartFile
            MultipartFile multipartFile = multipartFileDTO.toMultipartFile();
            log.info("Importing users from excel file.");
            this.importClientQrCodeFromExcel(email, multipartFile, loggedInUser, httpServletRequest);

            ResponseDTO responseDTO = ScleraUtils.generatePayload("Client Qr-Code Details Upserted Successfully", 200, true);
            log.info("Client Qr-Code Details Upserted Successfully.");
            userActionLogService.addUserActionLog(email, "ClientQrCode", "ADD", "Client Qr-Code Upserted Successfully", "success");
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);

        } else {
            log.error("Invalid client params.");
            userActivityService.addUserActivityLogs(loggedInUser, "client_qr_code", null, "UPDATE", "failed", "Invalid client params", null, null);
            userActionLogService.addUserActionLog(loggedInUser, "ClientQrCode", "UPDATE", "Invalid client param", "failed");
            throw new ClientException("Invalid client params", 700, null);
        }
    }

    private void importClientQrCodeFromExcel(String email, MultipartFile file, String loggedInUser, HttpServletRequest httpServletRequest) {
        String token = httpServletRequest.getHeader("Authorization");
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Runnable task = () -> {
            try {
                // Load the workbook from the input stream
                Workbook workbook;
                workbook = WorkbookFactory.create(file.getInputStream());

                // Get the sheet by index or name
                Sheet sheet = workbook.getSheetAt(0); // Assuming the first sheet is the one to fetch

                // Find the column indexes based on column names
                int clientQrCodeIdIndex = -1;
                int deviceIdIndex = -1;
                int locationIdIndex = -1;
                int vdmsIdIndex = -1;
                List<String> vdmsIdsForSync1 = new ArrayList<>();
                List<String> vdmsIdsForSync2 = new ArrayList<>();
                List<String> vdmsIds = new ArrayList<>();
                Row headerRow = sheet.getRow(0);
                for (Cell cell : headerRow) {
                    String columnName = cell.getStringCellValue();
                    if (columnName.equalsIgnoreCase("client_qr_code_id")) {
                        clientQrCodeIdIndex = cell.getColumnIndex();
                    } else if (columnName.equalsIgnoreCase("device_id")) {
                        deviceIdIndex = cell.getColumnIndex();
                    } else if (columnName.equalsIgnoreCase("location_id")) {
                        locationIdIndex = cell.getColumnIndex();
                    } else if (columnName.equalsIgnoreCase("vdms_id")) {
                        vdmsIdIndex = cell.getColumnIndex();
                    }
                }

                // Process the sheet data
                if (clientQrCodeIdIndex != -1 && deviceIdIndex != -1 && locationIdIndex != -1 && vdmsIdIndex != -1) {
                    String batchId = Generators.timeBasedGenerator().generate().toString();
                    for (Row row : sheet) {
                        // Skip the header row
                        if (row.getRowNum() == 0) {
                            continue;
                        }
                        Cell ClientQrCodeIdCell = row.getCell(clientQrCodeIdIndex);
                        Cell deviceIdCell = row.getCell(deviceIdIndex);
                        Cell locationIdCell = row.getCell(locationIdIndex);
                        Cell vdmsIdCell = row.getCell(vdmsIdIndex);

                        String clientQrCodeId = (String) this.getCellValue(ClientQrCodeIdCell);
                        String deviceId = (String) this.getCellValue(deviceIdCell);
                        String locationId = (String) this.getCellValue(locationIdCell);
                        String vdmsId = (String) this.getCellValue(vdmsIdCell);

                        if (clientQrCodeId != null && !clientQrCodeId.isEmpty() && vdmsId != null && !vdmsId.isEmpty()) {
                            ClientQrCodeDTO clientQrCodeData = clientQrCodeRepository.getClientQrCodeDetailsByClientQrCodeId(clientQrCodeId);

                            if (clientQrCodeData != null) {
                                BigInteger updatedAt = BigInteger.valueOf(System.currentTimeMillis());
                                String existingVdmsId = clientQrCodeData.getVdmsId();
                                if (!existingVdmsId.equals(vdmsId)) {
                                    if (!vdmsIdsForSync1.contains(vdmsId)) {
                                        vdmsIdsForSync1.add(vdmsId);
                                    }
                                    if (!vdmsIdsForSync2.contains(existingVdmsId)) {
                                        vdmsIdsForSync2.add(existingVdmsId);
                                    }
                                } else {
                                    int qrCodeSyncState = vdmsService.getQrCodeSyncByVdmsId(vdmsId);
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

                                clientQrCodeRepository.tagClientQrCode(deviceId, locationId, vdmsId, updatedAt, loggedInUser, batchId, 1, clientQrCodeId);
                                log.info("Client Qr-Code Details Updated Successfully.");
                                String from = "";
                                String to = "";
                                if (clientQrCodeData.getDeviceId() != null) {
                                    from = "Device ID: " + clientQrCodeData.getDeviceId();
                                } else if (clientQrCodeData.getLocationId() != null) {
                                    from = "Location ID: " + clientQrCodeData.getLocationId();
                                }
                                if (deviceId != null) {
                                    to = "Device ID: " + deviceId;
                                } else if (locationId != null) {
                                    to = "Location ID: " + locationId;
                                }

                                userActionLogService.addUserActionLog(email, "client_qr_code", "UPDATE", "Client QR code is tagged from " + from + " to " + to + " in VDMS " + vdmsId, "success");

                            } else {
                                String id = Generators.timeBasedGenerator().generate().toString();
                                BigInteger addedAt = BigInteger.valueOf(System.currentTimeMillis());
                                //qr code sync change
                                if (!vdmsIdsForSync1.contains(vdmsId)) {
                                    vdmsIdsForSync1.add(vdmsId); // Add new vdmsId for sync
                                }
                                clientQrCodeRepository.addClientQrCode(id, clientQrCodeId, addedAt, email, deviceId, locationId, vdmsId, batchId, 1);

                                log.info("Client Qr-Code Details Added Successfully.");

                                userActionLogService.addUserActionLog(email, "client_qr_code", "ADD", "Client Qr-Code With Id: " + clientQrCodeId + " Added Successfully", "success");
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
                        vdmsService.updateQrCodeSyncByVdmsIds(1, vdmsIdsForSync1);
                    }
                    if (!vdmsIdsForSync2.isEmpty()) {
                        log.info("update qr sync for 2");
                        vdmsService.updateQrCodeSyncByVdmsIds(2, vdmsIdsForSync2);
                    }
                    for (String vdms : vdmsIdsForSync1) {
                        VdmsSyncDTO vdmsSyncDTO = VdmsSyncDTO.builder().qr_sync(1).build();
                        int isMultiTenant = vdmsService.getMultiTenantCheck(vdms);
                        if (isMultiTenant == 1) {
                            String awsRegion = vdmsService.getAwsRegionByVdmsId(vdms);
                            webClientService.multiTenantSyncApiCall(vdms, vdmsSyncDTO,awsRegion, token);
                        } else {
                            socketUtils.invokeWebSocketEndpoint("/topic/vdms/" + vdms + "/sync/data", vdmsSyncDTO);
                        }
                    }
                    for (String vdms : vdmsIdsForSync2) {
                        VdmsSyncDTO vdmsSyncDTO = VdmsSyncDTO.builder().qr_sync(1).build();
                        int isMultiTenant = vdmsService.getMultiTenantCheck(vdms);
                        if (isMultiTenant == 1) {
                            String awsRegion = vdmsService.getAwsRegionByVdmsId(vdms);
                            webClientService.multiTenantSyncApiCall(vdms, vdmsSyncDTO,awsRegion, token);
                        } else {
                            socketUtils.invokeWebSocketEndpoint("/topic/vdms/" + vdms + "/sync/data", vdmsSyncDTO);
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

    public void deleteClientQrCodeByVdmsId(String vdmsId) {
        log.info("VdmsId:{}");
        clientQrCodeRepository.deleteClientQrCodeByVdmsId(vdmsId);
    }

    public Integer getClientQrCodeCountByVdmsID(String vdmsId, HttpServletRequest httpServletRequest) {
        log.info("Fetching Client Qr Code Count By VdmsId:{},Endpoint:{}", vdmsId, httpServletRequest.getRequestURI());
        return clientQrCodeRepository.getClientQrCodeCountByVdmsID(vdmsId);
    }

    public List<ClientQrCodeDTO> getAllClientQrCodeByVdmsId(String vdmsId, int pageSize, int offset) {
        return clientQrCodeRepository.getAllClientQrCodeByVdmsId(vdmsId, pageSize, offset);
    }

    public ResponseEntity<ResponseDTO> getClientQrCodeRecordsByVdmsIdAndLastSyncTime(String orgId, String email, String vdmsId, BigInteger lastSyncTime, String loggedInUser, int pageNo, int pageSize, HttpServletRequest httpServletRequest) {
        log.info("Payload: OrgId: {}, Email: {}, VdmsId: {}, LastSyncTime: {}, LoggedInUser: {},pageNo: {}.pageSize: {}", orgId, email, vdmsId, lastSyncTime, loggedInUser, pageNo, pageSize);

        int offset = pageSize * (pageNo - 1);
        List<ClientQrCodeDTO> clientQrCodeDTOList = clientQrCodeRepository.getClientQrCodeRecordsByVdmsIdAndLastSyncTime(vdmsId, lastSyncTime, pageSize, offset);

        if (clientQrCodeDTOList != null) {
            ResponseDTO responseDTO = ScleraUtils.generatePayload(clientQrCodeDTOList, 200, true);
            log.info("Fetching Client QR Code Records By VdmsId: {} And LastSyncTime: {}. Endpoint: {}", vdmsId, lastSyncTime, httpServletRequest.getRequestURI());
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } else {
            ResponseDTO responseDTO = ScleraUtils.generatePayload(null, 200, true);
            log.error("Client QR Code Records does not exist, EndPoint: {}", httpServletRequest.getRequestURI());
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        }
    }

    public ResponseEntity<?> updateClientQrCodeDetails(String vdmsId, List<ClientQrCodeDTO> clientQrCodeDTOS, HttpServletRequest httpServletRequest) {
        log.info("Payload:ClientQrCodeDTO:{},vdmsId:{}", clientQrCodeDTOS, vdmsId);

        clientQrCodeDTOS.forEach(clientQrCodeDTO -> {
            int checkClientQrCode = clientQrCodeRepository.checkClientQrCodeByVdmsIdAndId(vdmsId, clientQrCodeDTO.getId());
            if (checkClientQrCode == 1) {
                vdmsService.updateQrCodeSyncByVdmsId(1, vdmsId, httpServletRequest);
                VdmsSyncDTO vdmsSyncDTO = VdmsSyncDTO.builder().qr_sync(1).build();
                int isMultiTenant = vdmsService.getMultiTenantCheck(vdmsId);
                if (isMultiTenant == 1) {
                    String awsRegion = vdmsService.getAwsRegionByVdmsId(vdmsId);
                    webClientService.multiTenantSyncApiCall(vdmsId, vdmsSyncDTO,awsRegion, httpServletRequest);
                } else {
                    socketUtils.invokeWebSocketEndpoint("/topic/vdms/" + vdmsId + "/sync/data", vdmsSyncDTO);
                }
                clientQrCodeRepository.updateClientQrCodeDetailsById(clientQrCodeDTO.getDeviceId(), clientQrCodeDTO.getLocationId(), clientQrCodeDTO.getUpdated_time(), clientQrCodeDTO.getUpdated_by(), 1, clientQrCodeDTO.getId());
            } else {
                log.info("Client QrCode not exist for id: {} And vdmsId:{}", clientQrCodeDTO.getId(), vdmsId);
                userActionLogService.addUserActionLog(null, "ClientQrCode", "UPDATE", "Client QrCode not exist for id:" + clientQrCodeDTO.getId() + " And vdmsId:{}" + vdmsId, "failed");
            }
        });
        ResponseDTO responseDTO = ScleraUtils.generatePayload("successfully updated client qr code details", 200, true);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    public int getClientQrCodeCounts(String vdmsId, BigInteger lastSyncTime, HttpServletRequest httpServletRequest) {
        log.info("Payload:vdmsId:{}", vdmsId);
        return clientQrCodeRepository.getClientQrCodeCounts(vdmsId, lastSyncTime);
    }

    public ResponseEntity<ResponseDTO> getUnTaggedClientQrCode(String orgId, String email, String vdmsId, String loggedInUser, @Min(1) @Max(1000) int pageNo, @Min(1) @Max(1000) int pageSize, HttpServletRequest httpServletRequest) {
        int offset = pageSize * (pageNo - 1);
        List<String> ids = clientQrCodeRepository.getUnTaggedClientQrCode(pageSize, offset);
        ResponseDTO responseDTO = ScleraUtils.generatePayload(ids, 200, true);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    public List<ClientQrCodeDTO> getAllSyncClientQrCodeByVdmsId(String vdmsId, int pageSize, int offset) {
        return clientQrCodeRepository.getAllSyncClientQrCodeByVdmsId(vdmsId, pageSize, offset);
    }

    // qr code sync change
    public void updateClientQrCodeSyncByVdmsId(int i, BigInteger updatedTime, String vdmsId, HttpServletRequest httpServletRequest) {
        clientQrCodeRepository.updateClientQrCodeSyncByVdmsId(i, updatedTime, vdmsId);
    }

    public ResponseEntity<ResponseDTO> previewExcelSheet(String vdmsId, MultipartFile file, String loggedInUser) {
        log.info("Scanning Excel Sheet for column structure and VDMS ID consistency to import Client QR-Codes.");
        ResponseDTO responseDTO;
        ClientQrCodeDTO clientQrCodeDTO = new ClientQrCodeDTO();
        String contentType = file.getContentType();
        if (contentType != null) {
            if (contentType.equals("application/vnd.ms-excel") || contentType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) {
                try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
                    Sheet sheet = workbook.getSheetAt(0);
                    Row headerRow = sheet.getRow(0);

                    String[] expectedHeaders = {"client_qr_code_id", "device_id", "location_id", "vdms_id"};
                    for (int i = 0; i < expectedHeaders.length; i++) {
                        Cell cell = headerRow.getCell(i);
                        if (cell == null) {
                            clientQrCodeDTO.setValidationMessage("Missing Column at position " + (i + 1));
                            responseDTO = ScleraUtils.generatePayload(clientQrCodeDTO, 200, true);
                            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                        }
                        String actual = cell.getStringCellValue().trim();
                        if (!actual.equalsIgnoreCase(expectedHeaders[i])) {
                            clientQrCodeDTO.setValidationMessage("Invalid column at position " + (i + 1)
                                    + ": expected '" + expectedHeaders[i] + "', found '" + actual + "'");
                            responseDTO = ScleraUtils.generatePayload(clientQrCodeDTO, 200, true);
                            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                        }
                    }
                    int deviceIdIndex = 1;
                    int locationIdIndex = 2;
                    int vdmsIdIndex = 3;
                    Set<String> vdmsIds = new HashSet<>();

                    boolean hasDeviceId = false;
                    boolean hasLocationId = false;


                    for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                        Row row = sheet.getRow(r);
                        if (row == null) continue;

                        String deviceId = row.getCell(deviceIdIndex) != null ? row.getCell(deviceIdIndex).toString().trim() : "";
                        String locationId = row.getCell(locationIdIndex) != null ? row.getCell(locationIdIndex).toString().trim() : "";

                        if (!deviceId.isEmpty()) hasDeviceId = true;
                        if (!locationId.isEmpty()) hasLocationId = true;
                    }


                    if (hasDeviceId && hasLocationId) {
                        clientQrCodeDTO.setValidationMessage("File cannot contain both device_id and location_id. Use only one type for all rows.");
                        responseDTO = ScleraUtils.generatePayload(clientQrCodeDTO, 200, true);
                        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                    }
                    if (!hasDeviceId && !hasLocationId) {
                        clientQrCodeDTO.setValidationMessage("Either device_id or location_id must be provided for all rows. Both are empty.");
                        responseDTO = ScleraUtils.generatePayload(clientQrCodeDTO, 200, true);
                        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                    }

                    for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                        Row row = sheet.getRow(r);
                        if (row == null) continue;

                        Cell vdmsCell = row.getCell(vdmsIdIndex);
                        String vdmsIdInSheet = vdmsCell != null ? vdmsCell.toString().trim() : "";
                        if (!vdmsId.equals(vdmsIdInSheet)) {
                            clientQrCodeDTO.setValidationMessage("Mismatched vdms_id. Expected: " + vdmsId + ", Found: " + vdmsIdInSheet);
                            responseDTO = ScleraUtils.generatePayload(clientQrCodeDTO, 200, true);
                            return new ResponseEntity<>(responseDTO, HttpStatus.OK);

                        }
                        if (vdmsIdInSheet.isEmpty()) {
                            clientQrCodeDTO.setValidationMessage("Empty vdms_id found at row " + (r + 1));
                            responseDTO = ScleraUtils.generatePayload(clientQrCodeDTO, 200, true);
                            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                        }
                        vdmsIds.add(vdmsIdInSheet);
                        if (vdmsIds.size() > 1) {
                            clientQrCodeDTO.setValidationMessage("Inconsistent vdms_id values found. Expected all rows to have the same vdms_id but found: " + vdmsIds);
                            responseDTO = ScleraUtils.generatePayload(clientQrCodeDTO, 200, true);
                            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                        }

                        if (hasDeviceId) {
                            String deviceId = row.getCell(deviceIdIndex) != null ? row.getCell(deviceIdIndex).toString().trim() : "";
                            if (deviceId.isEmpty()) {
                                clientQrCodeDTO.setValidationMessage("Missing device_id at row " + (r + 1));
                                responseDTO = ScleraUtils.generatePayload(clientQrCodeDTO, 200, true);
                                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                            }
                        } else if (hasLocationId) {
                            String locationId = row.getCell(locationIdIndex) != null ? row.getCell(locationIdIndex).toString().trim() : "";
                            if (locationId.isEmpty()) {
                                clientQrCodeDTO.setValidationMessage("Missing location_id at row " + (r + 1));
                                responseDTO = ScleraUtils.generatePayload(clientQrCodeDTO, 200, true);
                                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                            }
                        }
                    }
                    userActionLogService.addUserActionLog(loggedInUser, "ClientQrCode", "UPDATE", "Excel Sheet preview check for Importing Client QR-Codes have passed.", "success");
                    clientQrCodeDTO.setValidationMessage("Excel Sheet preview check for Importing Client QR-Codes have passed.");
                    clientQrCodeDTO.setIs_validated(1);
                    responseDTO = ScleraUtils.generatePayload(clientQrCodeDTO, 200, true);
                    return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                } catch (Exception e) {
                    responseDTO = ScleraUtils.generatePayload("Error reading Excel file: " + e.getMessage(), 500, false);
                    return ResponseEntity.internalServerError()
                            .body(responseDTO);
                }
            } else {
                log.error("Invalid File Type.");
                userActivityService.addUserActivityLogs(loggedInUser, "client_qr_code", null, "UPDATE", "failed", "Invalid File Type", null, null);
                userActionLogService.addUserActionLog(loggedInUser, "ClientQrCode", "UPDATE", "Invalid File Type", "failed");
                clientQrCodeDTO.setValidationMessage("Invalid File Type. Please input Excel Sheet");
                responseDTO = ScleraUtils.generatePayload(clientQrCodeDTO, 200, true);
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            }
        } else {
            log.error("Invalid client params.");
            userActivityService.addUserActivityLogs(loggedInUser, "client_qr_code", null, "UPDATE", "failed", "Invalid client params", null, null);
            userActionLogService.addUserActionLog(loggedInUser, "ClientQrCode", "UPDATE", "Invalid client params", "failed");
            throw new ClientException("Invalid client params", 700, null);
        }
    }

    public ResponseEntity<ResponseDTO> tagClientQrCodeByVdmsId(String orgId, String vdmsId, List<ClientQrCodeDTO> clientQrCodeDTOList, HttpServletRequest httpServletRequest) {
        log.info("Tagging {} Client QrCodes for vdmsId: {}, orgId: {}", clientQrCodeDTOList.size(), vdmsId, orgId);
        if (!clientQrCodeDTOList.isEmpty() && vdmsId != null) {
            BigInteger updatedAt = BigInteger.valueOf(System.currentTimeMillis());
            String batchId = Generators.timeBasedGenerator().generate().toString();
            for (ClientQrCodeDTO clientQrCodeDTO : clientQrCodeDTOList) {
                clientQrCodeRepository.tagAdcClientQrCode(clientQrCodeDTO.getDeviceId(), clientQrCodeDTO.getLocationId(), vdmsId, updatedAt,
                        clientQrCodeDTO.getUpdatedBy(), batchId, 1, orgId, clientQrCodeDTO.getClientQrCodeId());

                userActivityService.addUserActivityLogs(clientQrCodeDTO.getUpdatedBy(), "client_qr_code", clientQrCodeDTO.getDeviceId() != null ? "device" : "location", "UPDATE", "success", "Client QR code is tagged to " + (clientQrCodeDTO.getDeviceId() != null ? "Device ID: " + clientQrCodeDTO.getDeviceId() : "Location ID: " + clientQrCodeDTO.getLocationId()), clientQrCodeDTO.getDeviceId() != null ? clientQrCodeDTO.getDeviceId() : clientQrCodeDTO.getLocationId(), clientQrCodeDTO.getVdmsId());
                userActionLogService.addUserActionLog(clientQrCodeDTO.getUpdatedBy(), "ClientQrCode", "UPDATE", "Client QR code with id:" + clientQrCodeDTO.getClientQrCodeId() + " is tagged to " + (clientQrCodeDTO.getDeviceId() != null ? "Device ID: " + clientQrCodeDTO.getDeviceId() : "Location ID: " + clientQrCodeDTO.getLocationId()) + " in VDMS " + vdmsId, "success");

            }
            vdmsService.updateQrCodeSyncByVdmsId(1, vdmsId, httpServletRequest);
            VdmsSyncDTO vdmsSyncDTO = VdmsSyncDTO.builder().
                    qr_sync(1).
                    build();
            int isMultiTenant = vdmsService.getMultiTenantCheck(vdmsId);
            if (isMultiTenant == 1) {
                String awsRegion = vdmsService.getAwsRegionByVdmsId(vdmsId);
                webClientService.multiTenantSyncApiCall(vdmsId, vdmsSyncDTO,awsRegion, httpServletRequest);
            } else {
                socketUtils.invokeWebSocketEndpoint("/topic/vdms/" + vdmsId + "/sync/data", vdmsSyncDTO);
            }
            ResponseDTO responseDTO = ScleraUtils.generatePayload("Client QrCode Details has been Updated Successfully", 200, true);
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } else {
            log.error("Invalid client params, EndPoint:  {}", httpServletRequest.getRequestURI());
            userActivityService.addUserActivityLogs(null, "client_qr_code", null, "UPDATE", "failed", "Invalid client params", null, vdmsId);
            userActionLogService.addUserActionLog(null, "ClientQrCode", "UPDATE", "Invalid client params", "failed");
            throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
        }
    }

    public ResponseEntity<ResponseDTO> getAdcCheckByClientQrCodeId(String encodedClientQrCodeId) {
        String clientQrCodeId = decodeInput(encodedClientQrCodeId);
        log.info("Fetching ADC check for ClientQrCodeId: {}", clientQrCodeId);
        ClientQrCodeDTO clientQrCodeDTO = new ClientQrCodeDTO();
        int isIdPresentInDb = clientQrCodeRepository.getClientQrCodeId(clientQrCodeId);
        if (isIdPresentInDb == 0) {
            clientQrCodeDTO.setIsTagged(2);
        } else {
            int isTagged = clientQrCodeRepository.getIsAdcTagged(clientQrCodeId);
            clientQrCodeDTO.setIsTagged(isTagged);
            int count = clientQrCodeRepository.getAdcCheckByClientQrCodeId(clientQrCodeId);
            if (count == 1) {
                clientQrCodeDTO.setIsAdc(true);
            } else {
                clientQrCodeDTO.setIsAdc(false);
            }
        }
        ResponseDTO responseDTO = ScleraUtils.generatePayload(clientQrCodeDTO, 200, true);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    public ResponseEntity<ResponseDTO> addClientQrCode(ClientQrCodeDTO clientQrCodeDTO, HttpServletRequest httpServletRequest) {
        if (clientQrCodeDTO != null) {
            log.info("Adding Client QrCode with details:{}", clientQrCodeDTO);
            String id = Generators.timeBasedGenerator().generate().toString();
            BigInteger addedAt = BigInteger.valueOf(System.currentTimeMillis());
            clientQrCodeRepository.addAdcClientQrCode(id, clientQrCodeDTO.getClientQrCodeId(), addedAt, clientQrCodeDTO.getAddedBy(), clientQrCodeDTO.getDeviceId(), 1);
            userActionLogService.addUserActionLog(clientQrCodeDTO.getAddedBy(), "ClientQrCode", "ADD", "Client QrCode with id: " + clientQrCodeDTO.getClientQrCodeId() + " added successfully", "success");
            ResponseDTO responseDTO = ScleraUtils.generatePayload("Client QrCode added successfully", 200, true);
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } else {
            userActionLogService.addUserActionLog(null, "ClientQrCode", "ADD", "Invalid client params", "failed");
            throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
        }
    }

    public ResponseEntity<ResponseDTO> updateClientQrCodeById(String clientQrCodeId, ClientQrCodeDTO clientQrCodeDTO) {
        log.info("Updating ClientQrCode details for id:{}", clientQrCodeId);
        BigInteger updatedAt = BigInteger.valueOf(System.currentTimeMillis());
        clientQrCodeRepository.updateClientQrCodeById(clientQrCodeDTO.getDeviceId(), updatedAt, clientQrCodeDTO.getUpdatedBy(), clientQrCodeId);
        ResponseDTO responseDTO = ScleraUtils.generatePayload("Client Qrcode has been tagged to device with id: " + clientQrCodeDTO.getDeviceId() + " successfully", 200, true);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    public static String decodeInput(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        String current = input;
        int maxIterations = 5;
        try {
            for (int i = 0; i < maxIterations; i++) {
                if (!current.contains("%")) {
                    break;
                }
                String safeInput = current.replace("+", "%2B");
                String decoded = URLDecoder.decode(safeInput, StandardCharsets.UTF_8);
                if (decoded.equals(current)) {
                    break;
                }

                current = decoded;
            }
            return current;
        } catch (IllegalArgumentException ex) {
            return current;
        } catch (Exception ex) {
            return input;
        }
    }
}
