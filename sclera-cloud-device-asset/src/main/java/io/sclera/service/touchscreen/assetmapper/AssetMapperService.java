package io.sclera.service.touchscreen.assetmapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.sclera.Repository.AssetRepository;
import io.sclera.dto.DeviceDTO;
import io.sclera.dto.touchscreen.assetmapper.AssetDTO;
import io.sclera.service.DeviceService;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Lean port of the asset-mapper spreadsheet import flow (upload -> stage -> save) that backs the
 * UI import wizard. The data layer ({@link AssetRepository}, the {@code asset} staging table and
 * {@link DeviceService#upsertVirtualDeviceByAssetMapper}) already exists in this service; this
 * class wires the upload/list/save orchestration on top of it. Matching / product-mapping /
 * custom-field handling from the full edge-server implementation is intentionally omitted.
 */
@Service
public class AssetMapperService {

    private static final Logger log = LoggerFactory.getLogger(AssetMapperService.class);
    private static final String IMPORT_TYPE = "spreadsheet";

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private DeviceService deviceService;

    /** The Sclera asset fields a source column can be mapped to (right column of the matching screen). */
    private static final List<Map<String, String>> ASSET_FIELDS = List.of(
            field("id", "ID"),
            field("subsystem_parent_id", "Subsystem Parent ID"),
            field("display_name", "Display Name"),
            field("model", "Model"),
            field("vendor", "Vendor"),
            field("type", "Type"),
            field("serial_number", "Serial Number"),
            field("mac_address", "MAC Address"),
            field("ip_address", "IP Address"),
            field("description", "Description"));

    private static Map<String, String> field(String key, String label) {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("key", key);
        m.put("label", label);
        return m;
    }

    /**
     * Parses the uploaded spreadsheet using the supplied field mapping and stages each row in the
     * {@code asset} table. Mirrors the real {@code /upload}: clears any previous staging first.
     *
     * @param file            the uploaded .xlsx
     * @param fieldMappingJson JSON array of {originalKey:[header], deviceKey, index:[colIndex]}
     * @param vdmsId          the VDMS the staged assets belong to
     */
    public ResponseEntity<Void> upload(MultipartFile file, String fieldMappingJson, String vdmsId) {
        try {
            assetRepository.deleteAllRecords();

            ObjectMapper objectMapper = new ObjectMapper();
            List<Map<String, Object>> mappings = objectMapper.readValue(fieldMappingJson, new TypeReference<>() {});

            int staged = 0;
            try (InputStream in = file.getInputStream(); Workbook workbook = WorkbookFactory.create(in)) {
                Sheet sheet = workbook.getSheetAt(0);
                DataFormatter fmt = new DataFormatter();
                // Resolve each source column by its header name (robust against client index drift),
                // falling back to the supplied index. Header and data rows share cell indices.
                Map<String, Integer> headerToCol = new LinkedHashMap<>();
                Row headerRow = sheet.getRow(sheet.getFirstRowNum());
                if (headerRow != null) {
                    for (int c = 0; c < headerRow.getLastCellNum(); c++) {
                        Cell h = headerRow.getCell(c);
                        if (h == null) continue;
                        String name = fmt.formatCellValue(h).trim();
                        if (!name.isEmpty()) headerToCol.putIfAbsent(name, c);
                    }
                }
                for (int r = sheet.getFirstRowNum() + 1; r <= sheet.getLastRowNum(); r++) {
                    Row row = sheet.getRow(r);
                    if (row == null) continue;
                    Map<String, String> values = new LinkedHashMap<>();
                    for (Map<String, Object> mapping : mappings) {
                        String deviceKey = String.valueOf(mapping.get("deviceKey"));
                        if (deviceKey == null || deviceKey.isBlank()
                                || "__ignore__".equals(deviceKey) || "null".equals(deviceKey)) continue;
                        Integer col = headerToCol.get(sourceHeader(mapping));
                        if (col == null) col = columnIndex(mapping);
                        if (col == null) continue;
                        Cell cell = row.getCell(col);
                        String v = cell == null ? "" : fmt.formatCellValue(cell).trim();
                        if (!v.isEmpty()) values.putIfAbsent(deviceKey, v);
                    }
                    if (values.isEmpty()) continue;

                    String id = values.getOrDefault("id", "");
                    if (id.isBlank()) id = UUID.randomUUID().toString();
                    id = id.replaceAll("[^a-zA-Z0-9_-]", "_");

                    String type = values.getOrDefault("type", "");
                    if (type.isBlank()) type = "generic";

                    String parent = values.getOrDefault("subsystem_parent_id", "");
                    if (parent.isBlank() || parent.equals(id)) parent = null;

                    assetRepository.assetUpsert(
                            id,
                            values.getOrDefault("display_name", ""),
                            values.getOrDefault("description", ""),
                            type,
                            values.getOrDefault("mac_address", ""),
                            values.getOrDefault("model", ""),
                            values.getOrDefault("vendor", ""),
                            values.getOrDefault("ip_address", ""),
                            0,                              // network_layer
                            values.getOrDefault("serial_number", ""),
                            values.getOrDefault("warranty", ""),
                            fieldMappingJson,               // original_keys
                            "[]",                           // custom_fields
                            parent,
                            false,                          // is_matched
                            "",                             // matched_products
                            vdmsId,
                            0,                              // subsystem_count
                            IMPORT_TYPE);
                    staged++;
                }
            }
            log.info("asset upload vdms_id={} staged={}", vdmsId, staged);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            log.error("asset upload failed vdms_id={}: {}", vdmsId, e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /** The source header name from a field mapping's {@code originalKey} array. */
    private String sourceHeader(Map<String, Object> mapping) {
        Object key = mapping.get("originalKey");
        if (key instanceof List<?> list && !list.isEmpty() && list.get(0) != null) {
            return String.valueOf(list.get(0)).trim();
        }
        return key == null ? "" : String.valueOf(key).trim();
    }

    /** First column index from a field mapping's {@code index} array. */
    private Integer columnIndex(Map<String, Object> mapping) {
        Object idx = mapping.get("index");
        if (idx instanceof List<?> list && !list.isEmpty() && list.get(0) instanceof Number n) {
            return n.intValue();
        }
        if (idx instanceof Number n) return n.intValue();
        return null;
    }

    /** Returns a page of staged top-level (parent) assets for the preview screen. */
    public ResponseEntity<List<AssetDTO>> getSubSystemParentAssets(Integer pageNo, Integer pageSize, String importType) {
        int offset = pageSize * (pageNo - 1);
        List<AssetDTO> assets = assetRepository.getSubSystemParentAssets(pageSize, offset, importType);
        for (AssetDTO asset : assets) asset.setSubsystems(new ArrayList<>());
        return new ResponseEntity<>(assets, HttpStatus.OK);
    }

    /** Returns the Sclera asset fields available as mapping targets. */
    public ResponseEntity<List<Map<String, String>>> getAssetFields() {
        return new ResponseEntity<>(ASSET_FIELDS, HttpStatus.OK);
    }

    /**
     * Commits the selected staged assets (and any direct subsystem children) into real devices via
     * the existing asset-mapper upsert, then removes them from staging. When {@code assetIds} is
     * null/empty every staged asset of the import type is saved.
     */
    public ResponseEntity<?> saveAssets(String username, String vdmsId, String dockerName,
                                        List<String> assetIds, String importType, String assignee) {
        List<AssetDTO> all = assetRepository.getAllAssets(importType);
        List<AssetDTO> toSave;
        if (assetIds != null && !assetIds.isEmpty()) {
            Set<String> ids = new HashSet<>(assetIds);
            toSave = new ArrayList<>();
            for (AssetDTO a : all) {
                boolean selected = ids.contains(a.getId());
                boolean childOfSelected = a.getSubsystem_parent_id() != null && ids.contains(a.getSubsystem_parent_id());
                if (selected || childOfSelected) toSave.add(a);
            }
        } else {
            toSave = all;
        }

        int saved = 0, failed = 0;
        for (AssetDTO a : toSave) {
            try {
                DeviceDTO device = new DeviceDTO();
                device.setId(a.getId());
                device.setUser_data_name(a.getDisplay_name());
                device.setUser_data_model(a.getModel());
                device.setUser_data_vendor(a.getVendor());
                device.setType(a.getType());
                // Empty MACs need a unique placeholder; keep it short — mac_address is varchar(32).
                String mac = a.getMac_address();
                if (mac == null || mac.isBlank()) {
                    String idPart = a.getId().length() > 8 ? a.getId().substring(0, 8) : a.getId();
                    mac = "IMP-" + idPart;
                }
                device.setMac_address(mac);
                device.setIp_address(a.getIp_address());
                device.setNetwork_layer(a.getNetwork_layer() == null ? null : String.valueOf(a.getNetwork_layer()));
                device.setSerial_number(a.getSerial_number());
                device.setWarranty(a.getWarranty());
                device.setCustom_fields(a.getCustomFields());
                device.setSubsystem_parent_id(a.getSubsystem_parent_id());
                device.setSubsystem_count(a.getSubsystem_count());
                device.setDocker_name(dockerName);
                device.setVdms_id(vdmsId);

                deviceService.upsertVirtualDeviceByAssetMapper(device, username);
                assetRepository.deleteById(a.getId());
                saved++;
            } catch (Exception e) {
                failed++;
                log.warn("saveAssets failed for asset {}: {}", a.getId(), e.getMessage());
            }
        }
        log.info("saveAssets username={} vdms_id={} docker={} saved={} failed={}", username, vdmsId, dockerName, saved, failed);
        return new ResponseEntity<>(Map.of("saved", saved, "failed", failed), HttpStatus.OK);
    }
}
