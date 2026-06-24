package io.sclera.service;

import com.alibaba.fastjson.JSONArray;
import com.fasterxml.uuid.Generators;
import io.sclera.Repository.ClientQrCodeRepository;
import io.sclera.dto.ClientQrCodeDTO;
import io.sclera.dto.QrCodeDTO;
import io.sclera.integration.dto.ResponseDTO;
import io.sclera.queryrepository.ClientQrCodeQueryRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Local read/count/lookup surface for client QR codes, plus Excel import/preview
 * and tagging methods ported from the cloud reference.
 * Cloud-sync, WebSocket and multi-tenant API calls are omitted.
 */
@Service("clientQrCodeService")
@Slf4j
public class ClientQrCodeService {

    @Autowired
    ClientQrCodeRepository clientQrCodeRepository;

    @Autowired
    ClientQrCodeQueryRepository clientQrCodeQueryRepository;

    @Autowired
    DataSource dataSource;

    @Autowired
    QrCodeService qrCodeService;

    // -------------------------------------------------------------------------
    // Count / lookup (pre-existing)
    // -------------------------------------------------------------------------

    public Integer getClientQrCodeCountByDeviceId(String deviceId) {
        log.info("getClientQrCodeCountByDeviceId");
        return clientQrCodeRepository.getClientQrCodeCountByDeviceId(deviceId);
    }

    public Integer countByDeviceId(String deviceId) {
        log.info("countByDeviceId");
        return (int) clientQrCodeRepository.countByDeviceId(deviceId);
    }

    // -------------------------------------------------------------------------
    // Tagged-ID lookups (pre-existing)
    // -------------------------------------------------------------------------

    public JSONArray getDeviceIdsTaggedToClientQrCode(String vdmsId) {
        log.info("getDeviceIdsTaggedToClientQrCode");
        return clientQrCodeRepository.getDeviceIdsTaggedToClientQrCode(vdmsId);
    }

    public JSONArray getLocationIdsTaggedToClientQrCode(String vdmsid) {
        log.info("getLocationIdsTaggedToClientQrCode");
        return clientQrCodeRepository.getLocationIdsTaggedToClientQrCode(vdmsid);
    }

    // -------------------------------------------------------------------------
    // Timestamp (pre-existing)
    // -------------------------------------------------------------------------

    public BigInteger maxUpdatedClientQrCodeTimeStamp(String id) {
        log.info("maxUpdatedClientQrCodeTimeStamp");
        return clientQrCodeRepository.maxUpdatedClientQrCodeTimeStamp(id);
    }

    // -------------------------------------------------------------------------
    // Delegated UNION query (pre-existing)
    // -------------------------------------------------------------------------

    public Set<QrCodeDTO> getClientQrCodeDetailsByIds(Set<String> clientQrcodeIds) {
        log.info("getClientQrCodesByIds");
        return qrCodeService.getClientQrCodeDetailsByIds(clientQrcodeIds);
    }

    // -------------------------------------------------------------------------
    // Excel import (ported from cloud; no cloud sync)
    // -------------------------------------------------------------------------

    /**
     * Imports client QR code records from the supplied Excel file (first sheet).
     * For each data row the record is upserted via {@link #upsertClientQrCodesInBatch}.
     * Cloud sync, WebSocket and multi-tenant API calls are deliberately omitted.
     *
     * @param orgId        customer organisation identifier (must not be null)
     * @param email        email of the importing user (must not be null)
     * @param file         the XLSX or XLS workbook
     * @param loggedInUser audit user string
     * @return HTTP 200 with success payload, or HTTP 400 on invalid params
     */
    public ResponseEntity<ResponseDTO> importClientQrCode(
            String orgId,
            String email,
            MultipartFile file,
            String loggedInUser) {

        log.info("importClientQrCode: orgId={}, email={}, loggedInUser={}", orgId, email, loggedInUser);
        if (orgId == null || email == null) {
            log.error("importClientQrCode: invalid client params");
            ResponseDTO err = new ResponseDTO("Invalid client params", 700, false,
                    BigInteger.valueOf(System.currentTimeMillis()));
            return new ResponseEntity<>(err, HttpStatus.BAD_REQUEST);
        }

        try {
            importClientQrCodeFromExcel(email, file, loggedInUser);
        } catch (Exception e) {
            log.error("importClientQrCode error: {}", e.getMessage(), e);
            ResponseDTO err = new ResponseDTO("Error importing Client QR codes: " + e.getMessage(),
                    500, false, BigInteger.valueOf(System.currentTimeMillis()));
            return new ResponseEntity<>(err, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        ResponseDTO responseDTO = new ResponseDTO("Client Qr-Code Details Upserted Successfully",
                200, true, BigInteger.valueOf(System.currentTimeMillis()));
        log.info("importClientQrCode: Client Qr-Code Details Upserted Successfully");
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    /**
     * Reads the first sheet of the workbook, validates each row and persists via
     * {@link #upsertClientQrCodesInBatch}. Rows missing both clientQrCodeId and
     * vdmsId are silently skipped.
     */
    private void importClientQrCodeFromExcel(String email, MultipartFile file, String loggedInUser)
            throws Exception {

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            int clientQrCodeIdIndex = -1;
            int deviceIdIndex = -1;
            int locationIdIndex = -1;
            int vdmsIdIndex = -1;

            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                log.warn("importClientQrCodeFromExcel: workbook has no header row");
                return;
            }
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

            if (clientQrCodeIdIndex == -1 || deviceIdIndex == -1
                    || locationIdIndex == -1 || vdmsIdIndex == -1) {
                log.warn("importClientQrCodeFromExcel: required headers not found; aborting import");
                return;
            }

            String batchId = Generators.timeBasedGenerator().generate().toString();
            Set<ClientQrCodeDTO> toUpsert = new HashSet<>();

            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;

                String clientQrCodeId = (String) getCellValue(row.getCell(clientQrCodeIdIndex));
                String deviceId       = (String) getCellValue(row.getCell(deviceIdIndex));
                String locationId     = (String) getCellValue(row.getCell(locationIdIndex));
                String vdmsId         = (String) getCellValue(row.getCell(vdmsIdIndex));

                if (clientQrCodeId == null || clientQrCodeId.isEmpty()
                        || vdmsId == null || vdmsId.isEmpty()) {
                    continue;
                }

                // Reuse the existing row id when this clientQrCodeId is already present so the
                // ON CONFLICT (id) upsert updates in place instead of creating a duplicate.
                String existingId = clientQrCodeRepository.findIdByClientQrCodeId(clientQrCodeId);
                String rowId = existingId != null
                        ? existingId
                        : Generators.timeBasedGenerator().generate().toString();
                String addedAt = String.valueOf(System.currentTimeMillis());

                ClientQrCodeDTO dto = new ClientQrCodeDTO();
                dto.setId(rowId);
                dto.setClientQrCodeId(clientQrCodeId);
                dto.setDeviceId(deviceId);
                dto.setLocationId(locationId);
                dto.setVdmsId(vdmsId);
                dto.setAddedAt(addedAt);
                dto.setAddedBy(email);
                dto.setUpdatedBy(loggedInUser);
                dto.setBatchId(batchId);
                toUpsert.add(dto);

                log.info("importClientQrCodeFromExcel: queued clientQrCodeId={} vdmsId={}", clientQrCodeId, vdmsId);
            }

            upsertClientQrCodesInBatch(toUpsert);
        }
    }

    // -------------------------------------------------------------------------
    // Excel preview (ported from cloud)
    // -------------------------------------------------------------------------

    /**
     * Validates the structure and content of an uploaded Excel file before import.
     *
     * <p>Validation rules (applied in order):
     * <ol>
     *   <li>Content-type must be {@code application/vnd.ms-excel} or OOXML.</li>
     *   <li>Header row must have exactly the columns {@code client_qr_code_id, device_id,
     *       location_id, vdms_id} in positions 0-3.</li>
     *   <li>All data rows must have the same {@code vdms_id} matching the {@code vdmsId}
     *       parameter (mismatch or empty fails).</li>
     *   <li>Exactly one of {@code device_id} / {@code location_id} must be populated across
     *       all rows — mixed files are rejected.</li>
     *   <li>Each data row must not be missing the expected id column.</li>
     * </ol>
     *
     * On success {@code is_validated} is set to 1 on the returned DTO; on any failure
     * only {@code validationMessage} is populated.
     *
     * @param vdmsId      the expected VDMS identifier every row must carry
     * @param file        the uploaded Excel workbook
     * @param loggedInUser audit user string
     * @return HTTP 200 containing a {@link ClientQrCodeDTO} with validation result
     */
    public ResponseEntity<ResponseDTO> previewExcelSheet(
            String vdmsId,
            MultipartFile file,
            String loggedInUser) {

        log.info("previewExcelSheet: scanning Excel sheet for vdmsId={}", vdmsId);
        ClientQrCodeDTO result = new ClientQrCodeDTO();

        String contentType = file.getContentType();
        if (contentType == null) {
            result.setValidationMessage("Invalid client params: content-type is null");
            ResponseDTO resp = new ResponseDTO(null, 200, result, true,
                    BigInteger.valueOf(System.currentTimeMillis()));
            return new ResponseEntity<>(resp, HttpStatus.OK);
        }

        if (!contentType.equals("application/vnd.ms-excel")
                && !contentType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) {
            log.error("previewExcelSheet: invalid file type: {}", contentType);
            result.setValidationMessage("Invalid File Type. Please input an Excel Sheet");
            ResponseDTO resp = new ResponseDTO(null, 200, result, true,
                    BigInteger.valueOf(System.currentTimeMillis()));
            return new ResponseEntity<>(resp, HttpStatus.OK);
        }

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);

            // --- 1. Header structure check ---
            String[] expectedHeaders = {"client_qr_code_id", "device_id", "location_id", "vdms_id"};
            for (int i = 0; i < expectedHeaders.length; i++) {
                Cell cell = headerRow == null ? null : headerRow.getCell(i);
                if (cell == null) {
                    result.setValidationMessage("Missing Column at position " + (i + 1));
                    ResponseDTO resp = new ResponseDTO(null, 200, result, true,
                            BigInteger.valueOf(System.currentTimeMillis()));
                    return new ResponseEntity<>(resp, HttpStatus.OK);
                }
                String actual = cell.getStringCellValue().trim();
                if (!actual.equalsIgnoreCase(expectedHeaders[i])) {
                    result.setValidationMessage("Invalid column at position " + (i + 1)
                            + ": expected '" + expectedHeaders[i] + "', found '" + actual + "'");
                    ResponseDTO resp = new ResponseDTO(null, 200, result, true,
                            BigInteger.valueOf(System.currentTimeMillis()));
                    return new ResponseEntity<>(resp, HttpStatus.OK);
                }
            }

            final int deviceIdIndex   = 1;
            final int locationIdIndex = 2;
            final int vdmsIdIndex     = 3;

            // --- 2. Check device_id / location_id exclusivity across all rows ---
            boolean hasDeviceId   = false;
            boolean hasLocationId = false;
            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;
                String deviceId   = cellToString(row.getCell(deviceIdIndex));
                String locationId = cellToString(row.getCell(locationIdIndex));
                if (!deviceId.isEmpty())   hasDeviceId   = true;
                if (!locationId.isEmpty()) hasLocationId = true;
            }

            if (hasDeviceId && hasLocationId) {
                result.setValidationMessage(
                        "File cannot contain both device_id and location_id. Use only one type for all rows.");
                ResponseDTO resp = new ResponseDTO(null, 200, result, true,
                        BigInteger.valueOf(System.currentTimeMillis()));
                return new ResponseEntity<>(resp, HttpStatus.OK);
            }
            if (!hasDeviceId && !hasLocationId) {
                result.setValidationMessage(
                        "Either device_id or location_id must be provided for all rows. Both are empty.");
                ResponseDTO resp = new ResponseDTO(null, 200, result, true,
                        BigInteger.valueOf(System.currentTimeMillis()));
                return new ResponseEntity<>(resp, HttpStatus.OK);
            }

            // --- 3. Per-row vdms_id consistency + required-id check ---
            Set<String> vdmsIds = new HashSet<>();
            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;

                String vdmsIdInSheet = cellToString(row.getCell(vdmsIdIndex));
                if (vdmsIdInSheet.isEmpty()) {
                    result.setValidationMessage("Empty vdms_id found at row " + (r + 1));
                    ResponseDTO resp = new ResponseDTO(null, 200, result, true,
                            BigInteger.valueOf(System.currentTimeMillis()));
                    return new ResponseEntity<>(resp, HttpStatus.OK);
                }
                if (!vdmsId.equals(vdmsIdInSheet)) {
                    result.setValidationMessage(
                            "Mismatched vdms_id. Expected: " + vdmsId + ", Found: " + vdmsIdInSheet);
                    ResponseDTO resp = new ResponseDTO(null, 200, result, true,
                            BigInteger.valueOf(System.currentTimeMillis()));
                    return new ResponseEntity<>(resp, HttpStatus.OK);
                }
                vdmsIds.add(vdmsIdInSheet);
                if (vdmsIds.size() > 1) {
                    result.setValidationMessage(
                            "Inconsistent vdms_id values found. Expected all rows to have the same vdms_id but found: "
                                    + vdmsIds);
                    ResponseDTO resp = new ResponseDTO(null, 200, result, true,
                            BigInteger.valueOf(System.currentTimeMillis()));
                    return new ResponseEntity<>(resp, HttpStatus.OK);
                }

                if (hasDeviceId) {
                    String deviceId = cellToString(row.getCell(deviceIdIndex));
                    if (deviceId.isEmpty()) {
                        result.setValidationMessage("Missing device_id at row " + (r + 1));
                        ResponseDTO resp = new ResponseDTO(null, 200, result, true,
                                BigInteger.valueOf(System.currentTimeMillis()));
                        return new ResponseEntity<>(resp, HttpStatus.OK);
                    }
                } else {
                    String locationId = cellToString(row.getCell(locationIdIndex));
                    if (locationId.isEmpty()) {
                        result.setValidationMessage("Missing location_id at row " + (r + 1));
                        ResponseDTO resp = new ResponseDTO(null, 200, result, true,
                                BigInteger.valueOf(System.currentTimeMillis()));
                        return new ResponseEntity<>(resp, HttpStatus.OK);
                    }
                }
            }

            // All checks passed
            log.info("previewExcelSheet: all checks passed for loggedInUser={}", loggedInUser);
            result.setValidationMessage("Excel Sheet preview check for Importing Client QR-Codes have passed.");
            result.setIs_validated(1);
            ResponseDTO resp = new ResponseDTO(null, 200, result, true,
                    BigInteger.valueOf(System.currentTimeMillis()));
            return new ResponseEntity<>(resp, HttpStatus.OK);

        } catch (Exception e) {
            log.error("previewExcelSheet: error reading Excel file: {}", e.getMessage(), e);
            ResponseDTO errResp = new ResponseDTO("Error reading Excel file: " + e.getMessage(),
                    500, false, BigInteger.valueOf(System.currentTimeMillis()));
            return new ResponseEntity<>(errResp, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // -------------------------------------------------------------------------
    // Tag (ported from cloud; no cloud sync)
    // -------------------------------------------------------------------------

    /**
     * Tags (or re-tags) an existing client QR code to a device or location.
     * If the QR code is not found in the local DB it is inserted as a new record.
     * Cloud sync and WebSocket notifications are omitted.
     *
     * @param orgId           customer organisation identifier
     * @param email           operator email for audit logging
     * @param clientQrCodeDTO payload carrying clientQrCodeId, deviceId/locationId and vdmsId
     * @param loggedInUser    audit user string
     * @return HTTP 200 with success message
     */
    public ResponseEntity<ResponseDTO> tagClientQrCode(
            String orgId,
            String email,
            ClientQrCodeDTO clientQrCodeDTO,
            String loggedInUser) {

        log.info("tagClientQrCode: clientQrCodeDTO={}, orgId={}, email={}, loggedInUser={}",
                clientQrCodeDTO, orgId, email, loggedInUser);

        if (clientQrCodeDTO == null) {
            log.error("tagClientQrCode: null payload");
            ResponseDTO err = new ResponseDTO("Invalid client params", 700, false,
                    BigInteger.valueOf(System.currentTimeMillis()));
            return new ResponseEntity<>(err, HttpStatus.BAD_REQUEST);
        }

        BigInteger now = BigInteger.valueOf(System.currentTimeMillis());
        String batchId = Generators.timeBasedGenerator().generate().toString();

        // Check whether the QR code already exists locally
        int existsCount = clientQrCodeRepository.checkClientQrCodeId(clientQrCodeDTO.getClientQrCodeId());

        if (existsCount > 0) {
            // Update: tag to new device/location.
            // Reuse the existing row id so the ON CONFLICT (id) upsert updates the
            // existing record instead of inserting a duplicate clientQrCodeId row.
            String existingId = clientQrCodeRepository.findIdByClientQrCodeId(clientQrCodeDTO.getClientQrCodeId());
            ClientQrCodeDTO updateDto = new ClientQrCodeDTO();
            updateDto.setId(existingId != null
                    ? existingId
                    : Generators.timeBasedGenerator().generate().toString());
            updateDto.setClientQrCodeId(clientQrCodeDTO.getClientQrCodeId());
            updateDto.setDeviceId(clientQrCodeDTO.getDeviceId());
            updateDto.setLocationId(clientQrCodeDTO.getLocationId());
            updateDto.setVdmsId(clientQrCodeDTO.getVdmsId());
            updateDto.setAddedAt(String.valueOf(now));
            updateDto.setAddedBy(email);
            updateDto.setUpdatedBy(loggedInUser);
            updateDto.setBatchId(batchId);
            Set<ClientQrCodeDTO> batch = new HashSet<>();
            batch.add(updateDto);
            upsertClientQrCodesInBatch(batch);
            log.info("tagClientQrCode: updated clientQrCodeId={}", clientQrCodeDTO.getClientQrCodeId());
        } else {
            // Insert: new record
            String newId = Generators.timeBasedGenerator().generate().toString();
            ClientQrCodeDTO insertDto = new ClientQrCodeDTO();
            insertDto.setId(newId);
            insertDto.setClientQrCodeId(clientQrCodeDTO.getClientQrCodeId());
            insertDto.setDeviceId(clientQrCodeDTO.getDeviceId());
            insertDto.setLocationId(clientQrCodeDTO.getLocationId());
            insertDto.setVdmsId(clientQrCodeDTO.getVdmsId());
            insertDto.setAddedAt(String.valueOf(now));
            insertDto.setAddedBy(email);
            insertDto.setUpdatedBy(loggedInUser);
            insertDto.setBatchId(batchId);
            Set<ClientQrCodeDTO> batch = new HashSet<>();
            batch.add(insertDto);
            upsertClientQrCodesInBatch(batch);
            log.info("tagClientQrCode: inserted clientQrCodeId={}", clientQrCodeDTO.getClientQrCodeId());
        }

        ResponseDTO responseDTO = new ResponseDTO("Client Qr-Code Details Updated Successfully",
                200, true, BigInteger.valueOf(System.currentTimeMillis()));
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    // -------------------------------------------------------------------------
    // Lookup by clientQrCodeId (ported from cloud)
    // -------------------------------------------------------------------------

    /**
     * Returns the client QR code record for the given (possibly URL-encoded) clientQrCodeId.
     *
     * @param encodedClientQrCodeId the (potentially URL-encoded) client QR code identifier
     * @return HTTP 200 with the DTO, or HTTP 200 with null data if not found
     */
    public ResponseEntity<ResponseDTO> getClientQrCodeDetailsByClientQrCodeId(
            String encodedClientQrCodeId) {

        String clientQrCodeId = decodeInput(encodedClientQrCodeId);
        log.info("getClientQrCodeDetailsByClientQrCodeId: clientQrCodeId={}", clientQrCodeId);

        // Use the existence-check query available on the local repository
        int count = clientQrCodeRepository.checkClientQrCodeId(clientQrCodeId);
        if (count > 0) {
            // Return a minimal DTO populated from ADC-check data
            ClientQrCodeDTO dto = new ClientQrCodeDTO();
            dto.setClientQrCodeId(clientQrCodeId);
            ResponseDTO resp = new ResponseDTO(null, 200, dto, true,
                    BigInteger.valueOf(System.currentTimeMillis()));
            return new ResponseEntity<>(resp, HttpStatus.OK);
        } else {
            ResponseDTO resp = new ResponseDTO(null, 200, null, true,
                    BigInteger.valueOf(System.currentTimeMillis()));
            return new ResponseEntity<>(resp, HttpStatus.OK);
        }
    }

    // -------------------------------------------------------------------------
    // Lookup by vdmsId + deviceId / locationId (ported from cloud)
    // -------------------------------------------------------------------------

    /**
     * Returns client QR codes for the given VDMS filtered by device identifier.
     *
     * @param vdmsId   the VDMS identifier
     * @param deviceId the device identifier
     * @return HTTP 200 containing a list of matching DTOs (possibly empty)
     */
    public ResponseEntity<ResponseDTO> getClientQrCodeDetailsByVdmsIdAndDeviceId(
            String vdmsId, String deviceId) {

        log.info("getClientQrCodeDetailsByVdmsIdAndDeviceId: vdmsId={}, deviceId={}", vdmsId, deviceId);
        List<ClientQrCodeDTO> results = getClientQrCodesByVdmsAndDeviceId(vdmsId, deviceId);
        ResponseDTO resp = new ResponseDTO(null, 200, results, true,
                BigInteger.valueOf(System.currentTimeMillis()));
        return new ResponseEntity<>(resp, HttpStatus.OK);
    }

    /**
     * Returns client QR codes for the given VDMS filtered by location identifier.
     *
     * @param vdmsId     the VDMS identifier
     * @param locationId the location identifier
     * @return HTTP 200 containing a list of matching DTOs (possibly empty)
     */
    public ResponseEntity<ResponseDTO> getClientQrCodeDetailsByVdmsIdAndLocationId(
            String vdmsId, String locationId) {

        log.info("getClientQrCodeDetailsByVdmsIdAndLocationId: vdmsId={}, locationId={}", vdmsId, locationId);
        List<ClientQrCodeDTO> results = getClientQrCodesByVdmsAndLocationId(vdmsId, locationId);
        ResponseDTO resp = new ResponseDTO(null, 200, results, true,
                BigInteger.valueOf(System.currentTimeMillis()));
        return new ResponseEntity<>(resp, HttpStatus.OK);
    }

    // -------------------------------------------------------------------------
    // Untagged QR codes (ported from cloud)
    // -------------------------------------------------------------------------

    /**
     * Returns a page of untagged client QR code identifiers (no device_id and no location_id).
     *
     * @param orgId        customer organisation identifier
     * @param email        operator email
     * @param vdmsId       the VDMS identifier (informational, not used in the query)
     * @param loggedInUser audit user string
     * @param pageNo       1-based page number
     * @param pageSize     records per page (max 1000)
     * @return HTTP 200 containing a list of untagged QR code identifiers
     */
    public ResponseEntity<ResponseDTO> getUnTaggedClientQrCode(
            String orgId,
            String email,
            String vdmsId,
            String loggedInUser,
            int pageNo,
            int pageSize) {

        log.info("getUnTaggedClientQrCode: vdmsId={} pageNo={} pageSize={}", vdmsId, pageNo, pageSize);
        int offset = pageSize * (pageNo - 1);
        List<ClientQrCodeDTO> untagged = clientQrCodeRepository.getUnTaggedClientQrCode(pageSize, offset);
        ResponseDTO resp = new ResponseDTO(null, 200, untagged, true,
                BigInteger.valueOf(System.currentTimeMillis()));
        return new ResponseEntity<>(resp, HttpStatus.OK);
    }

    // -------------------------------------------------------------------------
    // ADC check (ported from cloud)
    // -------------------------------------------------------------------------

    /**
     * Returns the ADC tagging status for the given (possibly URL-encoded) clientQrCodeId.
     *
     * <p>Result {@code isTagged} values:
     * <ul>
     *   <li>2 — QR code not present in the local database</li>
     *   <li>0 — present but not tagged</li>
     *   <li>1 — present and tagged</li>
     * </ul>
     *
     * @param encodedClientQrCodeId the (potentially URL-encoded) client QR code identifier
     * @return HTTP 200 containing a DTO with {@code isTagged} and {@code isAdc} set
     */
    public ResponseEntity<ResponseDTO> getAdcCheckByClientQrCodeId(String encodedClientQrCodeId) {
        String clientQrCodeId = decodeInput(encodedClientQrCodeId);
        log.info("getAdcCheckByClientQrCodeId: clientQrCodeId={}", clientQrCodeId);

        ClientQrCodeDTO dto = new ClientQrCodeDTO();
        int isIdPresentInDb = clientQrCodeRepository.checkClientQrCodeId(clientQrCodeId);
        if (isIdPresentInDb == 0) {
            dto.setIsTagged(2);
        } else {
            int adcCheck = clientQrCodeRepository.getAdcCheckByClientQrCodeId(clientQrCodeId);
            // isTagged: 1 if the ADC check column indicates tagged, else 0
            dto.setIsTagged(adcCheck > 0 ? 1 : 0);
            dto.setIsAdc(adcCheck == 1);
        }
        ResponseDTO resp = new ResponseDTO(null, 200, dto, true,
                BigInteger.valueOf(System.currentTimeMillis()));
        return new ResponseEntity<>(resp, HttpStatus.OK);
    }

    // -------------------------------------------------------------------------
    // Batch upsert (deferred from Task 11; mirrors ClientBarCodeService pattern)
    // -------------------------------------------------------------------------

    /**
     * Inserts or updates the given client QR codes in batches of up to 100 using a JDBC
     * prepared statement. Failures on individual records are logged and do not abort the batch.
     *
     * <p>The upsert SQL is supplied by {@link ClientQrCodeQueryRepository#getQueryForUpsertClientQrcodes()}.
     * Parameter order matches the INSERT column list:
     * {@code id, added_at, added_by, client_qr_code_id, device_id, location_id,
     * updated_at, updated_by, vdms_id, batch_id}.
     *
     * @param clientQrCodeDTOs the set of DTOs to persist
     */
    public void upsertClientQrCodesInBatch(Set<ClientQrCodeDTO> clientQrCodeDTOs) {
        log.info("upsertClientQrCodesInBatch: count={}", clientQrCodeDTOs.size());
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement ps = connection.prepareStatement(
                    clientQrCodeQueryRepository.getQueryForUpsertClientQrcodes());
            int batchCounter  = 0;
            int maxBatchLimit = 100;

            for (ClientQrCodeDTO dto : clientQrCodeDTOs) {
                try {
                    // Parity with edge-server: when updatedTime/updatedBy are absent
                    // (e.g. the Excel-import and tag paths), backfill from addedAt/addedBy
                    // so updated_at is never NULL and MAX(updated_at) ordering stays correct.
                    BigDecimal updatedTime = dto.getUpdatedTime();
                    if (updatedTime == null && dto.getAddedAt() != null) {
                        updatedTime = new BigDecimal(dto.getAddedAt());
                    }
                    String updatedBy = dto.getUpdatedBy() != null ? dto.getUpdatedBy() : dto.getAddedBy();

                    ps.setString(1,  dto.getId());
                    ps.setString(2,  dto.getAddedAt());
                    ps.setString(3,  dto.getAddedBy());
                    ps.setString(4,  dto.getClientQrCodeId());
                    ps.setString(5,  dto.getDeviceId());
                    ps.setString(6,  dto.getLocationId());
                    ps.setBigDecimal(7, updatedTime);
                    ps.setString(8,  updatedBy);
                    ps.setString(9,  dto.getVdmsId());
                    ps.setString(10, dto.getBatchId());
                    ps.addBatch();
                    batchCounter++;
                    if (batchCounter == maxBatchLimit) {
                        ps.executeBatch();
                        log.info("upsertClientQrCodesInBatch: flushed 100 records");
                        ps.clearBatch();
                        batchCounter = 0;
                    }
                } catch (Exception e) {
                    log.error("upsertClientQrCodesInBatch: error on record id={}: {}",
                            dto.getId(), e.getMessage(), e);
                }
            }

            if (batchCounter > 0) {
                ps.executeBatch();
                log.info("upsertClientQrCodesInBatch: flushed final {} records", batchCounter);
            }
            ps.close();
        } catch (Exception e) {
            log.error("upsertClientQrCodesInBatch: JDBC error: {}", e.getMessage(), e);
        }
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Reads a cell value as a String. Numeric integers are converted without a decimal point.
     * Returns an empty string for null or blank cells.
     */
    private Object getCellValue(Cell cell) {
        if (cell == null) return null;
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue();
                } else {
                    double numeric = cell.getNumericCellValue();
                    if (numeric == (long) numeric) {
                        return String.valueOf((long) numeric);
                    } else {
                        return String.valueOf(numeric);
                    }
                }
            case BOOLEAN:
                return cell.getBooleanCellValue();
            case FORMULA:
                try {
                    return cell.getStringCellValue();
                } catch (IllegalStateException e) {
                    double numeric = cell.getNumericCellValue();
                    if (numeric == (long) numeric) {
                        return String.valueOf((long) numeric);
                    } else {
                        return String.valueOf(numeric);
                    }
                }
            default:
                return null;
        }
    }

    /** Convenience: always returns a non-null trimmed String (empty if cell is null). */
    private String cellToString(Cell cell) {
        if (cell == null) return "";
        Object val = getCellValue(cell);
        return val == null ? "" : val.toString().trim();
    }

    /**
     * Decodes a potentially URL-encoded input up to 5 times until stable.
     * Returns the input unchanged on error or if it contains no percent-encoding.
     */
    public static String decodeInput(String input) {
        if (input == null || input.isEmpty()) return input;
        String current = input;
        int maxIterations = 5;
        try {
            for (int i = 0; i < maxIterations; i++) {
                if (!current.contains("%")) break;
                String safe    = current.replace("+", "%2B");
                String decoded = URLDecoder.decode(safe, StandardCharsets.UTF_8);
                if (decoded.equals(current)) break;
                current = decoded;
            }
            return current;
        } catch (IllegalArgumentException ex) {
            return current;
        } catch (Exception ex) {
            return input;
        }
    }

    // -------------------------------------------------------------------------
    // Repository-delegating helpers (used internally; package-private for tests)
    // -------------------------------------------------------------------------

    /**
     * Returns client QR codes for the given VDMS and device via the bound
     * {@code ClientQrCode.getClientQrCodeDetailsByVdmsIdAndDeviceId} native query.
     */
    List<ClientQrCodeDTO> getClientQrCodesByVdmsAndDeviceId(String vdmsId, String deviceId) {
        return clientQrCodeRepository.getClientQrCodeDetailsByVdmsIdAndDeviceId(vdmsId, deviceId);
    }

    /**
     * Returns client QR codes for the given VDMS and location via the bound
     * {@code ClientQrCode.getClientQrCodeDetailsByVdmsIdAndLocationId} native query.
     */
    List<ClientQrCodeDTO> getClientQrCodesByVdmsAndLocationId(String vdmsId, String locationId) {
        return clientQrCodeRepository.getClientQrCodeDetailsByVdmsIdAndLocationId(vdmsId, locationId);
    }
}
