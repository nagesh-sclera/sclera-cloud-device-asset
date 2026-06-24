package io.sclera.service;


import com.fasterxml.uuid.Generators;
import io.sclera.dto.*;
import io.sclera.exception.ClientException;
import io.sclera.repository.ClientNfcRepository;
import io.sclera.util.ScleraRoleCheckUtils;
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@Slf4j
public class ClientNfcService {

    @Autowired
    private UserActivityService userActivityService;
    @Autowired
    private ClientNfcRepository clientNfcRepository;
    @Autowired
    private ScleraRoleCheckUtils scleraRoleCheckUtils;
    @Autowired
    private UserActionLogService userActionLogService;
    @Autowired
    private VdmsService vdmsService;
    @Autowired
    private SocketUtils socketUtils;
    @Autowired
    private WebClientService webClientService;

    public ResponseEntity<?> getClientNfcDetailsByDeviceIdAndVdmsId(String deviceId, String vdmsId, String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload : deviceId {} , vdmsId {} , loggedInUser {}", deviceId, vdmsId, loggedInUser);
        if (deviceId != null && vdmsId != null && loggedInUser != null) {
            boolean access = scleraRoleCheckUtils.checkRole(loggedInUser, httpServletRequest, "super-admin", "admin", "master-user", "org-admin", "user", "master-vendor", "vendor", "property-admin");
            if (access) {
                List<ClientNfcDTO> nfcDTOS = clientNfcRepository.getClientNfcDetailsByDeviceIdAndVdmsId(deviceId, vdmsId);
                ResponseDTO responseDTO = ScleraUtils.generatePayload(nfcDTOS, 200, true);
                log.info("Fetching Client NFC info from db by deviceId {} ,vdmsId {} ,EndPoint: {}", deviceId, vdmsId, httpServletRequest.getRequestURI());
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

    public ResponseEntity<?> getClientNfcDetailsByLocationIdAndVdmsId(String locationId, String vdmsId, String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload : locationId {} , vdmsId {} , loggedInUser {} ", locationId, vdmsId, loggedInUser);
        if (locationId != null && vdmsId != null && loggedInUser != null) {
            boolean access = scleraRoleCheckUtils.checkRole(loggedInUser, httpServletRequest, "super-admin", "admin", "master-user", "org-admin", "user", "master-vendor", "vendor", "property-admin");
            if (access) {
                List<ClientNfcDTO> nfcDTOS = clientNfcRepository.getClientNfcDetailsByLocationIdAndVdmsId(locationId, vdmsId);
                ResponseDTO responseDTO = ScleraUtils.generatePayload(nfcDTOS, 200, true);
                log.info("Fetching Client NFC info from db by locationId {} ,vdmsId {} ,EndPoint: {}", locationId, vdmsId, httpServletRequest.getRequestURI());
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

    public void deleteClientNfcByVdmsId(String vdmsId, HttpServletRequest httpServletRequest) {
        log.info("Successfully Deleting Client Nfc data For vdms_Id:{},EndPoint:{}", vdmsId, httpServletRequest.getRequestURI());
        clientNfcRepository.deleteClientNfcDataByVdmsId(vdmsId);
    }

    public ResponseEntity<?> importClientNfc(String orgId, String email, MultipartFile file, String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload: OrgId:{}, Email:{}, LoggedInUser:{}", orgId, email, loggedInUser);
        if (orgId != null && email != null) {
            //convert MultipartFile to ByteArray using MultipartFileDTO
            MultipartFileDTO multipartFileDTO = file != null ? processMultipartFiles(file) : new MultipartFileDTO();
            //convert the MultipartFileDTO back to MultipartFile
            MultipartFile multipartFile = multipartFileDTO.toMultipartFile();
            log.info("Importing users from excel file.");
            this.importClientNfcFromExcel(email, multipartFile, loggedInUser, httpServletRequest);

            ResponseDTO responseDTO = ScleraUtils.generatePayload("Client  NFC Details Upserted Successfully", 200, true);
            log.info("Client  NFC Details Upserted Successfully.");
            userActionLogService.addUserActionLog(email, "ClientNfc", "ADD",
                    "Client NFC Upserted Successfully", "success");
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);

        } else {
            log.error("Invalid client params.");
            userActivityService.addUserActivityLogs(loggedInUser, "client_nfc", null, "UPDATE", "failed",
                    "Invalid client params", null, null);
            userActionLogService.addUserActionLog(loggedInUser, "ClientNfc", "UPDATE", "Invalid client param", "failed");
            throw new ClientException("Invalid client params", 700, null);
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


    private void importClientNfcFromExcel(String email, MultipartFile file, String loggedInUser, HttpServletRequest httpServletRequest) {
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
                int clientNfcIdIndex = -1;
                int deviceIdIndex = -1;
                int locationIdIndex = -1;
                int vdmsIdIndex = -1;
                List<String> vdmsIdsForSync1 = new ArrayList<>();
                List<String> vdmsIdsForSync2 = new ArrayList<>();
                Row headerRow = sheet.getRow(0);
                for (Cell cell : headerRow) {
                    String columnName = cell.getStringCellValue();
                    if (columnName.equalsIgnoreCase("nfc_id")) {
                        clientNfcIdIndex = cell.getColumnIndex();
                    } else if (columnName.equalsIgnoreCase("device_id")) {
                        deviceIdIndex = cell.getColumnIndex();
                    } else if (columnName.equalsIgnoreCase("location_id")) {
                        locationIdIndex = cell.getColumnIndex();
                    } else if (columnName.equalsIgnoreCase("vdms_id")) {
                        vdmsIdIndex = cell.getColumnIndex();
                    }
                }

                // Process the sheet data
                if (clientNfcIdIndex != -1 && deviceIdIndex != -1 && locationIdIndex != -1 && vdmsIdIndex != -1) {
                    String batchId = Generators.timeBasedGenerator().generate().toString();
                    for (Row row : sheet) {
                        // Skip the header row
                        if (row.getRowNum() == 0) {
                            continue;
                        }
                        Cell ClientNfcIdCell = row.getCell(clientNfcIdIndex);
                        Cell deviceIdCell = row.getCell(deviceIdIndex);
                        Cell locationIdCell = row.getCell(locationIdIndex);
                        Cell vdmsIdCell = row.getCell(vdmsIdIndex);

                        String clientNfcId = (String) this.getCellValue(ClientNfcIdCell);
                        String deviceId = (String) this.getCellValue(deviceIdCell);
                        String locationId = (String) this.getCellValue(locationIdCell);
                        String vdmsId = (String) this.getCellValue(vdmsIdCell);

                        if (clientNfcId != null && !clientNfcId.isEmpty() && vdmsId != null && !vdmsId.isEmpty()) {
                            int isNfc = clientNfcRepository.checkNfcId(clientNfcId);
                            if (isNfc == 1) {
                                ClientNfcDTO existingNfcDTO = clientNfcRepository.getClientNfcDetailsByNfcId(clientNfcId);
                                String existingVdmsId = existingNfcDTO.getVdmsId();
                                if (!existingVdmsId.equals(vdmsId)) {
                                    if (!vdmsIdsForSync1.contains(vdmsId)) {
                                        vdmsIdsForSync1.add(vdmsId);
                                    }
                                    if (!vdmsIdsForSync2.contains(existingVdmsId)) {
                                        vdmsIdsForSync2.add(existingVdmsId);
                                    }
                                } else {
                                    int nfcSyncStatus = vdmsService.getNfcSyncStateByVdmsId(vdmsId);
                                    if (nfcSyncStatus == 2) {
                                        if (!vdmsIdsForSync2.contains(vdmsId)) {
                                            vdmsIdsForSync2.add(vdmsId);
                                        }
                                    } else {
                                        if (!vdmsIdsForSync1.contains(vdmsId)) {
                                            vdmsIdsForSync1.add(vdmsId);
                                        }
                                    }
                                }
                                clientNfcRepository.tagClientNfc(deviceId, locationId, vdmsId, batchId, 1, clientNfcId);

                                log.info("Client NFC Details Updated Successfully.");
                                if (deviceId != null) {
                                    userActivityService.addUserActivityLogs(loggedInUser, "client_nfc", "device", "UPDATE", "success",
                                            "A device With Id:" + deviceId + " Is Tagged", deviceId,
                                            vdmsId);
                                    userActionLogService.addUserActionLog(email, "client_nfc", "UPDATE",
                                            "A device With Id:" + deviceId + " Is Tagged", "success");
                                } else {
                                    userActivityService.addUserActivityLogs(loggedInUser, "client_nfc", "location", "UPDATE", "success",
                                            "A location With Id:" + locationId + " Is Tagged", locationId,
                                            vdmsId);
                                    userActionLogService.addUserActionLog(email, "client_nfc", "UPDATE",
                                            "A location With Id:" + locationId + " Is Tagged", "success");
                                }
                            } else {
                                String id = Generators.timeBasedGenerator().generate().toString();
                                BigInteger addedAt = BigInteger.valueOf(System.currentTimeMillis());
                                if (!vdmsIdsForSync1.contains(vdmsId)) {
                                    vdmsIdsForSync1.add(vdmsId);
                                }
                                clientNfcRepository.addClientNFC(id, clientNfcId, addedAt, email, deviceId, locationId, vdmsId, batchId, 1);
                                log.info("Client NFC Details Added Successfully.");
                                userActionLogService.addUserActionLog(email, "client_nfc", "ADD",
                                        "Client NFC With Id: " + clientNfcId + " Added Successfully", "success");
                            }
                        }
                    }
                    if (!vdmsIdsForSync1.isEmpty()) {
                        vdmsService.updateNfcSyncByVdmsIds(1, vdmsIdsForSync1);
                    }
                    if (!vdmsIdsForSync2.isEmpty()) {
                        vdmsService.updateNfcSyncByVdmsIds(2, vdmsIdsForSync2);
                    }
                    VdmsSyncDTO vdmsSyncDTO = VdmsSyncDTO.builder()
                            .nfc_sync(1)
                            .build();
                    for (String vdms : vdmsIdsForSync1) {
                        int isMultiTenant = vdmsService.getMultiTenantCheck(vdms);
                        if (isMultiTenant == 1) {
                            String awsRegion = vdmsService.getAwsRegionByVdmsId(vdms);
                            webClientService.multiTenantSyncApiCall(vdms, vdmsSyncDTO, awsRegion, token);
                        } else {
                            log.info("TRIGGERED SOCKET");
                            socketUtils.invokeWebSocketEndpoint("/topic/vdms/" + vdms + "/sync/data", vdmsSyncDTO);
                        }
                    }
                    for (String vdms : vdmsIdsForSync2) {
                        int isMultiTenant = vdmsService.getMultiTenantCheck(vdms);
                        if (isMultiTenant == 1) {
                            String awsRegion = vdmsService.getAwsRegionByVdmsId(vdms);
                            webClientService.multiTenantSyncApiCall(vdms, vdmsSyncDTO, awsRegion, token);
                        } else {
                            log.info("TRIGGERED SOCKET");
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

    public List<ClientNfcDTO> getClientNfcDetailsByVdmsIdAndDeviceIds(String vdmsId, List<String> deviceId) {
        log.info("Payload: VDMS ID:{},Device Ids:{}", vdmsId, deviceId);
        return clientNfcRepository.getClientNfcDetailsByVdmsIdAndDeviceIds(vdmsId, deviceId);
    }

    public List<ClientNfcDTO> getClientNfcDetailsByVdmsIdAndLocationIds(String vdmsId, List<String> locationId) {
        log.info("Payload: VDMS ID:{},Location Ids:{}", vdmsId, locationId);
        return clientNfcRepository.getClientNfcDetailsByVdmsIdAndLocationIds(vdmsId, locationId);
    }

    public List<ClientNfcDTO> getClientNfcDetailsByVdmsIdTaggedByDevice(String vdmsId) {
        log.info("Payload: VDMS ID:{}", vdmsId);
        return clientNfcRepository.getClientNfcDetailsByVdmsIdTaggedByDevice(vdmsId);
    }

    public List<ClientNfcDTO> getClientNfcDetailsByVdmsIdTaggedByLocation(String vdmsId) {
        log.info("Payload: VDMS ID:{}", vdmsId);
        return clientNfcRepository.getClientNfcDetailsByVdmsIdTaggedByLocation(vdmsId);
    }

    public List<String> getClientNfcIdsByVdmsIdAndDevice(String vdmsId) {
        log.info("Payload: VDMS ID:{}", vdmsId);

        return clientNfcRepository.getClientNfcIdsByVdmsIdAndDevice(vdmsId);
    }

    public List<String> getClientNfcIdsByVdmsIdAndlocation(String vdmsId) {
        log.info("Payload: VDMS ID:{}", vdmsId);

        return clientNfcRepository.getClientNfcIdsByVdmsIdAndlocation(vdmsId);
    }

    public void deleteClientNfcInfoByDeviceIdsAndVdmsId(List<String> taggedIds, String vdmsId) {
        log.info("Payload: VDMS ID:{},Device Ids:{}", vdmsId, taggedIds);
        clientNfcRepository.deleteClientNfcInfoByDeviceIdsAndVdmsId(taggedIds, vdmsId);
    }

    public void deleteClientNfcInfoByLocationIdsAndVdmsId(List<String> taggedIds, String vdmsId) {
        log.info("Payload: VDMS ID:{},Device Ids:{}", vdmsId, taggedIds);
        clientNfcRepository.deleteClientNfcInfoByLocationIdsAndVdmsId(taggedIds, vdmsId);
    }


    public ClientNfcDTO getClientNfcDetailsByNfcId(String nfc_id) {
        log.info("Payload: Nfc Id:{}", nfc_id);
        return clientNfcRepository.getClientNfcDetailsByNfcId(nfc_id);
    }

    public List<ClientNfcDTO> getAllClientNfcByVdmsId(String vdmsId, int pageSize, int offset) {
        return clientNfcRepository.getClientNfcRecordsByVdmsId(vdmsId, pageSize, offset);
    }

    public Integer getClientNfcCountByVdmsID(String vdmsId, HttpServletRequest httpServletRequest) {
        log.info("Fetching Client Nfc Count By VdmsId:{},Endpoint:{}", vdmsId, httpServletRequest.getRequestURI());
        return clientNfcRepository.getClientNfcCounts(vdmsId);
    }

    public ResponseEntity<ResponseDTO> getClientNfcRecordsByVdmsId(String orgId, String email, String vdmsId, String loggedInUser, int pageNo, int pageSize, HttpServletRequest httpServletRequest) {
        log.info("Payload: OrgId: {}, Email: {}, VdmsId: {}, LoggedInUser: {},pageNo: {}.pageSize: {}", orgId, email, vdmsId, loggedInUser, pageNo, pageSize);
        int offset = pageSize * (pageNo - 1);
        List<ClientNfcDTO> clientNfcDTOList = clientNfcRepository.getClientNfcRecordsByVdmsId(vdmsId, pageSize, offset);

        if (clientNfcDTOList != null) {
            ResponseDTO responseDTO = ScleraUtils.generatePayload(clientNfcDTOList, 200, true);
            log.info("Fetching Client NFC Records By VdmsId: {}  Endpoint: {}", vdmsId, httpServletRequest.getRequestURI());
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } else {
            ResponseDTO responseDTO = ScleraUtils.generatePayload(null, 200, true);
            log.error("ClientNFC Records does not exist, EndPoint: {}", httpServletRequest.getRequestURI());
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        }
    }

    public ResponseEntity<ResponseDTO> updateClientNfcDetails(String vdmsId, List<ClientNfcDTO> clientNfcDTOS, HttpServletRequest httpServletRequest) {
        log.info("Payload:ClientNfcDTO:{},vdmsId:{}", clientNfcDTOS, vdmsId);

        clientNfcDTOS.forEach(clientNfcDTO -> {
            int checkClientQrCode = clientNfcRepository.checkClientNfcByVdmsIdAndId(vdmsId, clientNfcDTO.getId());
            if (checkClientQrCode == 1) {
                vdmsService.updateNfcSyncByVdmsId(1, vdmsId, httpServletRequest);
                VdmsSyncDTO vdmsSyncDTO = VdmsSyncDTO.builder()
                        .nfc_sync(1)
                        .build();
                clientNfcRepository.updateClientNfcDetailsById(clientNfcDTO.getDeviceId(), clientNfcDTO.getLocationId(), clientNfcDTO.getNfc_id(), 1, clientNfcDTO.getId());
                int isMultiTenant = vdmsService.getMultiTenantCheck(vdmsId);
                if (isMultiTenant == 1) {
                    String awsRegion = vdmsService.getAwsRegionByVdmsId(vdmsId);
                    webClientService.multiTenantSyncApiCall(vdmsId, vdmsSyncDTO,awsRegion, httpServletRequest);
                } else {
                    socketUtils.invokeWebSocketEndpoint("/topic/vdms/" + vdmsId + "/sync/data", vdmsSyncDTO);
                }
            } else {
                log.info("Client NFC not exist for id: {} And vdmsId:{}", clientNfcDTO.getId(), vdmsId);
                userActionLogService.addUserActionLog(null, "ClientNfc", "UPDATE", "Client NFC not exist for id:" + clientNfcDTO.getId() + " And vdmsId:{}" + vdmsId, "failed");
            }
        });
        ResponseDTO responseDTO = ScleraUtils.generatePayload("Successfully updated Client Nfc details", 200, true);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    public int getClientNfcCounts(String vdmsId, HttpServletRequest httpServletRequest) {
        return clientNfcRepository.getClientNfcCounts(vdmsId);
    }

    public ResponseEntity<ResponseDTO> getUnTaggedClientNfc(String orgId, String email, String vdmsId, String loggedInUser, @Min(1) @Max(1000) int pageNo, @Min(1) @Max(1000) int pageSize, HttpServletRequest httpServletRequest) {
        int offset = pageSize * (pageNo - 1);
        List<String> ids = clientNfcRepository.getUnTaggedClientNfc(pageSize, offset);
        ResponseDTO responseDTO = ScleraUtils.generatePayload(ids, 200, true);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    public void updateClientNfcSyncByVdmsId(int i, BigInteger creationTime, String vdmsId) {
        clientNfcRepository.updateClientNfcSyncByVdmsId(i, creationTime, vdmsId);
    }

    public List<ClientNfcDTO> getAllSyncClientNfcByVdmsId(String vdmsId, int pageSize, int offset) {
        return clientNfcRepository.getSyncClientNfcRecordsByVdmsId(vdmsId, pageSize, offset);
    }

    public ResponseEntity<ResponseDTO> previewExcelSheet(String vdmsId, MultipartFile file, String loggedInUser) {
        log.info("Scanning Excel Sheet for column structure and VDMS ID consistency to import Client NFCs.");
        ResponseDTO responseDTO;
        ClientNfcDTO clientNfcDTO = new ClientNfcDTO();
        String contentType = file.getContentType();
        if (contentType != null) {
            if (contentType.equals("application/vnd.ms-excel") || contentType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) {
                try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
                    Sheet sheet = workbook.getSheetAt(0);
                    Row headerRow = sheet.getRow(0);

                    String[] expectedHeaders = {"nfc_id", "device_id", "location_id", "vdms_id"};
                    for (int i = 0; i < expectedHeaders.length; i++) {
                        Cell cell = headerRow.getCell(i);
                        if (cell == null) {
                            clientNfcDTO.setValidationMessage("Missing Column at position " + (i + 1));
                            responseDTO = ScleraUtils.generatePayload(clientNfcDTO, 200, true);
                            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                        }
                        String actual = cell.getStringCellValue().trim();
                        if (!actual.equalsIgnoreCase(expectedHeaders[i])) {
                            clientNfcDTO.setValidationMessage("Invalid column at position " + (i + 1)
                                    + ": expected '" + expectedHeaders[i] + "', found '" + actual + "'");
                            responseDTO = ScleraUtils.generatePayload(clientNfcDTO, 200, true);
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
                        clientNfcDTO.setValidationMessage("File cannot contain both device_id and location_id. Use only one type for all rows.");
                        responseDTO = ScleraUtils.generatePayload(clientNfcDTO, 200, true);
                        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                    }
                    if (!hasDeviceId && !hasLocationId) {
                        clientNfcDTO.setValidationMessage("Either device_id or location_id must be provided for all rows. Both are empty.");
                        responseDTO = ScleraUtils.generatePayload(clientNfcDTO, 200, true);
                        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                    }

                    for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                        Row row = sheet.getRow(r);
                        if (row == null) continue;

                        Cell vdmsCell = row.getCell(vdmsIdIndex);
                        String vdmsIdInSheet = vdmsCell != null ? vdmsCell.toString().trim() : "";
                        if (!vdmsId.equals(vdmsIdInSheet)) {
                            clientNfcDTO.setValidationMessage("Mismatched vdms_id. Expected: " + vdmsId + ", Found: " + vdmsIdInSheet);
                            responseDTO = ScleraUtils.generatePayload(clientNfcDTO, 200, true);
                            return new ResponseEntity<>(responseDTO, HttpStatus.OK);

                        }
                        if (vdmsIdInSheet.isEmpty()) {
                            clientNfcDTO.setValidationMessage("Empty vdms_id found at row " + (r + 1));
                            responseDTO = ScleraUtils.generatePayload(clientNfcDTO, 200, true);
                            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                        }
                        vdmsIds.add(vdmsIdInSheet);
                        if (vdmsIds.size() > 1) {
                            clientNfcDTO.setValidationMessage("Inconsistent vdms_id values found. Expected all rows to have the same vdms_id but found: " + vdmsIds);
                            responseDTO = ScleraUtils.generatePayload(clientNfcDTO, 200, true);
                            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                        }

                        if (hasDeviceId) {
                            String deviceId = row.getCell(deviceIdIndex) != null ? row.getCell(deviceIdIndex).toString().trim() : "";
                            if (deviceId.isEmpty()) {
                                clientNfcDTO.setValidationMessage("Missing device_id at row " + (r + 1));
                                responseDTO = ScleraUtils.generatePayload(clientNfcDTO, 200, true);
                                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                            }
                        } else if (hasLocationId) {
                            String locationId = row.getCell(locationIdIndex) != null ? row.getCell(locationIdIndex).toString().trim() : "";
                            if (locationId.isEmpty()) {
                                clientNfcDTO.setValidationMessage("Missing location_id at row " + (r + 1));
                                responseDTO = ScleraUtils.generatePayload(clientNfcDTO, 200, true);
                                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                            }
                        }
                    }
                    userActionLogService.addUserActionLog(loggedInUser, "ClientNfc", "UPDATE", "Excel Sheet preview check for Importing Client NFC have passed.", "success");
                    clientNfcDTO.setValidationMessage("Excel Sheet preview check for Importing Client NFC have passed.");
                    clientNfcDTO.setIs_validated(1);
                    responseDTO = ScleraUtils.generatePayload(clientNfcDTO, 200, true);
                    return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                } catch (Exception e) {
                    responseDTO = ScleraUtils.generatePayload("Error reading Excel file: " + e.getMessage(), 500, false);
                    return ResponseEntity.internalServerError()
                            .body(responseDTO);
                }
            } else {
                log.error("Invalid File Type.");
                userActivityService.addUserActivityLogs(loggedInUser, "client_nfc", null, "UPDATE", "failed", "Invalid File Type", null, null);
                userActionLogService.addUserActionLog(loggedInUser, "ClientNfc", "UPDATE", "Invalid File Type", "failed");
                clientNfcDTO.setValidationMessage("Invalid File Type. Please input Excel Sheet");
                responseDTO = ScleraUtils.generatePayload(clientNfcDTO, 200, true);
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            }
        } else {
            log.error("Invalid client params.");
            userActivityService.addUserActivityLogs(loggedInUser, "client_nfc", null, "UPDATE", "failed", "Invalid client params", null, null);
            userActionLogService.addUserActionLog(loggedInUser, "ClientNfc", "UPDATE", "Invalid client params", "failed");
            throw new ClientException("Invalid client params", 700, null);
        }
    }

    public ResponseEntity<ResponseDTO> tagClientNfcByVdmsId(String orgId, String vdmsId, List<ClientNfcDTO> clientNfcDTOList, HttpServletRequest httpServletRequest) {
        log.info("Tagging {} Client Nfc's for vdmsId: {}, orgId: {}", clientNfcDTOList.size(), vdmsId, orgId);
        if (!clientNfcDTOList.isEmpty() && vdmsId != null) {
            BigInteger updatedAt = BigInteger.valueOf(System.currentTimeMillis());
            String batchId = Generators.timeBasedGenerator().generate().toString();
            for (ClientNfcDTO clientNfcDTO : clientNfcDTOList) {
                clientNfcRepository.tagAdcClientNfc(clientNfcDTO.getDeviceId(), clientNfcDTO.getLocationId(), vdmsId, updatedAt, clientNfcDTO.getUpdatedBy(),
                        batchId, 1, orgId, clientNfcDTO.getNfc_id());

                userActivityService.addUserActivityLogs(clientNfcDTO.getUpdatedBy(), "client_nfc", clientNfcDTO.getDeviceId() != null ? "device" : "location", "UPDATE", "success",
                        "Client Nfc is tagged to " + (clientNfcDTO.getDeviceId() != null ? "Device ID: " + clientNfcDTO.getDeviceId() : "Location ID: " + clientNfcDTO.getLocationId()), clientNfcDTO.getDeviceId() != null ? clientNfcDTO.getDeviceId() : clientNfcDTO.getLocationId(), vdmsId);
                userActionLogService.addUserActionLog(clientNfcDTO.getUpdatedBy(), "ClientNfc", "UPDATE",
                        "Client Nfc is tagged to " + (clientNfcDTO.getDeviceId() != null ? "Device ID: " + clientNfcDTO.getDeviceId() : "Location ID: " + clientNfcDTO.getLocationId()), "success");

            }
            vdmsService.updateNfcSyncByVdmsId(1, vdmsId, httpServletRequest);
            VdmsSyncDTO vdmsSyncDTO = VdmsSyncDTO.builder()
                    .nfc_sync(1)
                    .build();
            int isMultiTenant = vdmsService.getMultiTenantCheck(vdmsId);
            if (isMultiTenant == 1) {
                String awsRegion = vdmsService.getAwsRegionByVdmsId(vdmsId);
                webClientService.multiTenantSyncApiCall(vdmsId, vdmsSyncDTO,awsRegion, httpServletRequest);
            } else {
                socketUtils.invokeWebSocketEndpoint("/topic/vdms/" + vdmsId + "/sync/data", vdmsSyncDTO);
            }
            ResponseDTO responseDTO = ScleraUtils.generatePayload("Client Nfc Details has been Updated Successfully", 200, true);
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } else {
            log.error("Invalid client params, EndPoint:  {}", httpServletRequest.getRequestURI());
            userActivityService.addUserActivityLogs(null, "client_nfc", null, "UPDATE", "failed", "Invalid client params", null, vdmsId);
            userActionLogService.addUserActionLog(null, "ClientNfc", "UPDATE", "Invalid client param", "failed");
            throw new ClientException("Invalid client params", 700, null);
        }
    }

    public ResponseEntity<ResponseDTO> getAdcCheckByClientNfcId(String clientNfcId) {
        log.info("Fetching ADC Check By ClientNfcId: {}", clientNfcId);
        ClientNfcDTO clientNfcDTO = new ClientNfcDTO();
        int isIdPresentInDb = clientNfcRepository.getClientNfcId(clientNfcId);
        if (isIdPresentInDb == 0) {
            clientNfcDTO.setIsTagged(2);
        } else {
            int isTagged = clientNfcRepository.getIsAdcTagged(clientNfcId);
            clientNfcDTO.setIsTagged(isTagged);
            int count = clientNfcRepository.getAdcCheckByClientNfcId(clientNfcId);
            if (count == 1) {
                clientNfcDTO.setIsAdc(true);
            } else {
                clientNfcDTO.setIsAdc(false);
            }
        }
        ResponseDTO responseDTO = ScleraUtils.generatePayload(clientNfcDTO, 200, true);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    public ResponseEntity<ResponseDTO> addClientNfc(ClientNfcDTO clientNfcDTO, HttpServletRequest httpServletRequest) {
        if (clientNfcDTO != null) {
            log.info("Adding Client Nfc with details: {}", clientNfcDTO);
            String id = Generators.timeBasedGenerator().generate().toString();
            BigInteger addedAt = BigInteger.valueOf(System.currentTimeMillis());
            clientNfcRepository.addAdcClientNfc(id, clientNfcDTO.getNfc_id(), addedAt, clientNfcDTO.getCreatedBy(), clientNfcDTO.getDeviceId(), 1);
            userActionLogService.addUserActionLog(clientNfcDTO.getCreatedBy(), "ClientNfc", "ADD", "Client NFC With Id: " + clientNfcDTO.getNfc_id() + " Added Successfully", "success");
            ResponseDTO responseDTO = ScleraUtils.generatePayload("Client NFC added successfully", 200, true);
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } else {
            userActionLogService.addUserActionLog(null, "ClientNfc", "ADD", "Invalid client params", "failed");
            throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
        }
    }

    public ResponseEntity<ResponseDTO> updateClientNfcById(String clientNfcId, ClientNfcDTO clientNfcDTO) {
        log.info("Updating ClientNfc details for id:{}", clientNfcId);
        BigInteger updatedAt = BigInteger.valueOf(System.currentTimeMillis());
        clientNfcRepository.updateClientNfcById(clientNfcDTO.getDeviceId(), clientNfcDTO.getUpdatedBy(), updatedAt, clientNfcId);
        ResponseDTO responseDTO = ScleraUtils.generatePayload("ClientNfc has been tagged to device with id:" + clientNfcDTO.getDeviceId() + "successfully", 200, true);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    public ResponseEntity<?> getClientNfcDetailsByClientNfcId(String clientNfcId, HttpServletRequest httpServletRequest) {
        log.info("Fetching ClientNfc details for clientNfcId: {}, Endpoint: {}", clientNfcId, httpServletRequest.getRequestURI());
        if (clientNfcId != null) {
            ClientNfcDTO clientNfcDTO = clientNfcRepository.getClientNfcDetailsByNfcId(clientNfcId);
            ResponseDTO responseDTO = ScleraUtils.generatePayload(clientNfcDTO, 200, true);
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } else {
            log.error("Invalid client params, EndPoint:  {}", httpServletRequest.getRequestURI());
            throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
        }
    }
}