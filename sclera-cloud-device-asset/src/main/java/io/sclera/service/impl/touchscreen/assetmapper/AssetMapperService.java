package io.sclera.service.impl.touchscreen.assetmapper;
import io.sclera.service.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.sclera.Repository.AssetRepository;
import io.sclera.Repository.DeviceRepository;
import io.sclera.dto.DeviceDTO;
import io.sclera.dto.touchscreen.assetmapper.AssetDTO;
import io.sclera.models.Asset;
import io.sclera.dto.touchscreen.VdmsDetailsDTO;
import io.sclera.service.DeviceService;
import io.sclera.service.UserActionLogService;
import io.sclera.service.VdmsService;
import org.springframework.data.domain.PageRequest;
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

    /** Asset entity columns a mapping can target directly; anything else is treated as a custom field. */
    private static final Set<String> KNOWN_ASSET_KEYS = Set.of(
            "id", "display_name", "description", "type", "mac_address", "model", "vendor",
            "ip_address", "network_layer", "serial_number", "warranty", "subsystem_parent_id");

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private UserActionLogService userActionLogService;

    @Autowired
    private VdmsService vdmsService;

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
                    List<Map<String, String>> customFields = new ArrayList<>();
                    List<Map<String, Object>> originalKeys = new ArrayList<>();
                    boolean anyValue = false;
                    for (Map<String, Object> mapping : mappings) {
                        String deviceKey = String.valueOf(mapping.get("deviceKey"));
                        if (deviceKey == null || deviceKey.isBlank()
                                || "__ignore__".equals(deviceKey) || "null".equals(deviceKey)) continue;
                        Integer col = headerToCol.get(sourceHeader(mapping));
                        if (col == null) col = columnIndex(mapping);
                        if (col == null) continue;
                        Cell cell = row.getCell(col);
                        String v = cell == null ? "" : fmt.formatCellValue(cell).trim();
                        if (!v.isEmpty()) anyValue = true;
                        // Custom field when the client flags it, or the target isn't a known Asset column.
                        boolean custom = Boolean.TRUE.equals(mapping.get("isCustom")) || !KNOWN_ASSET_KEYS.contains(deviceKey);
                        Map<String, Object> ok = new LinkedHashMap<>();
                        ok.put("device", deviceKey);
                        ok.put("custom", custom);
                        originalKeys.add(ok);
                        if (custom) {
                            // custom_fields is a JSON array of single-key {column: value} objects.
                            Map<String, String> cf = new LinkedHashMap<>();
                            cf.put(deviceKey, v);
                            customFields.add(cf);
                        } else if (!v.isEmpty()) {
                            values.putIfAbsent(deviceKey, v);
                        }
                    }
                    if (!anyValue) continue;

                    String id = values.getOrDefault("id", "");
                    if (id.isBlank()) id = UUID.randomUUID().toString();
                    id = id.replaceAll("[^a-zA-Z0-9_-]", "_");

                    String type = values.getOrDefault("type", "");
                    if (type.isBlank()) type = "generic";

                    String parent = values.getOrDefault("subsystem_parent_id", "");
                    if (parent.isBlank() || parent.equals(id)) parent = null;

                    Asset asset = new Asset();
                    asset.setId(id);
                    asset.setDisplay_name(values.getOrDefault("display_name", ""));
                    asset.setDescription(values.getOrDefault("description", ""));
                    asset.setType(type);
                    asset.setMac_address(values.getOrDefault("mac_address", ""));
                    asset.setModel(values.getOrDefault("model", ""));
                    asset.setVendor(values.getOrDefault("vendor", ""));
                    asset.setIp_address(values.getOrDefault("ip_address", ""));
                    asset.setNetwork_layer(0);
                    asset.setSerial_number(values.getOrDefault("serial_number", ""));
                    asset.setWarranty(values.getOrDefault("warranty", ""));
                    asset.setOriginalKeys(objectMapper.writeValueAsString(originalKeys));
                    asset.setCustomFields(objectMapper.writeValueAsString(customFields));
                    asset.setSubsystem_parent_id(parent);
                    asset.setIsMatched(false);
                    asset.setMatchedProductIds("");
                    asset.setSubsystem_count(0);
                    asset.setImport_type(IMPORT_TYPE);
                    // JPA upsert-by-id (save merges existing rows). NOTE: the vdms association is not
                    // set here — vdmsId was the old native upsert's docker_vdms_id and no asset query
                    // filters by vdms; revisit if vdms scoping becomes required.
                    assetRepository.save(asset);
                    staged++;
                }
            }
            registerImportedCustomFields(mappings, vdmsId);
            log.info("asset upload vdms_id={} staged={}", vdmsId, staged);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            log.error("asset upload failed vdms_id={}: {}", vdmsId, e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Registers the imported custom-field columns on the VDMS device-custom-fields list so they
     * surface as configurable device fields/filters. Mirrors the asset-mapper "save custom keys"
     * step from the full implementation; deduped by column. Best-effort — never fails the import.
     */
    private void registerImportedCustomFields(List<Map<String, Object>> mappings, String vdmsId) {
        try {
            List<Map<String, Object>> imported = new ArrayList<>();
            for (Map<String, Object> mapping : mappings) {
                String deviceKey = String.valueOf(mapping.get("deviceKey"));
                if (deviceKey == null || deviceKey.isBlank()
                        || "__ignore__".equals(deviceKey) || "null".equals(deviceKey)) continue;
                boolean custom = Boolean.TRUE.equals(mapping.get("isCustom")) || !KNOWN_ASSET_KEYS.contains(deviceKey);
                if (!custom) continue;
                String key = deviceKey;
                Object originalKey = mapping.get("originalKey");
                if (originalKey instanceof List<?> l && !l.isEmpty() && l.get(0) != null) key = String.valueOf(l.get(0));
                Map<String, Object> entry = new LinkedHashMap<>();
                entry.put("key", key);
                entry.put("column", deviceKey);
                entry.put("custom", true);
                imported.add(entry);
            }
            if (imported.isEmpty()) return;

            ObjectMapper om = new ObjectMapper();
            VdmsDetailsDTO details = vdmsService.getVdmsDeviceCustomFields();
            if (details != null && details.getDevice_custom_fields() != null
                    && !details.getDevice_custom_fields().isBlank()) {
                List<Map<String, Object>> existing = om.readValue(details.getDevice_custom_fields(),
                        new TypeReference<List<Map<String, Object>>>() {});
                Set<String> existingCols = new HashSet<>();
                for (Map<String, Object> f : existing) {
                    Object c = f.get("column");
                    if (c != null) existingCols.add(String.valueOf(c).trim().toLowerCase());
                }
                for (Map<String, Object> f : imported) {
                    String col = String.valueOf(f.get("column")).trim().toLowerCase();
                    if (existingCols.add(col)) existing.add(f);
                }
                details.setDevice_custom_fields(om.writeValueAsString(existing));
                details.setVdms_id(vdmsId);
                vdmsService.upsertVdmsDeviceCustomFields(details);
            } else {
                VdmsDetailsDTO d = details != null ? details : new VdmsDetailsDTO();
                d.setVdms_id(vdmsId);
                d.setDevice_custom_fields(om.writeValueAsString(imported));
                vdmsService.upsertVdmsDeviceCustomFields(d);
            }
        } catch (Exception e) {
            log.warn("registerImportedCustomFields failed vdms_id={}: {}", vdmsId, e.getMessage());
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
        List<AssetDTO> assets = assetRepository.getSubSystemParentAssets(importType, PageRequest.of(pageNo - 1, pageSize));
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

                boolean isNew = !deviceRepository.existsById(device.getId());
                deviceService.upsertVirtualDeviceByAssetMapper(device, username);
                assetRepository.deleteById(a.getId());
                saved++;
                if (isNew) {
                    String assetLabel = device.getUser_data_name() != null ? device.getUser_data_name() : device.getId();
                    userActionLogService.addUserAction(username, "asset", "IMPORT",
                            "Asset imported: " + assetLabel + " (id: " + device.getId() + ")",
                            "success", "asset_info", device.getId());
                }
            } catch (Exception e) {
                failed++;
                log.warn("saveAssets failed for asset {}: {}", a.getId(), e.getMessage());
            }
        }
        log.info("saveAssets username={} vdms_id={} docker={} saved={} failed={}", username, vdmsId, dockerName, saved, failed);
        return new ResponseEntity<>(Map.of("saved", saved, "failed", failed), HttpStatus.OK);
    }
}
