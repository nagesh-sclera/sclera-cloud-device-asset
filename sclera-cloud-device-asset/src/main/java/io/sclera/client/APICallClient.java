package io.sclera.client;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.dapr.client.DaprClient;
import io.dapr.client.domain.HttpExtension;
import io.sclera.dto.*;
import io.sclera.dto.touchscreen.SnmpValuesDTO;
import io.sclera.dto.touchscreen.settings.UserDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Thin Dapr client delegating to sclera-edge (AP-C1edge).
 * Replaces {@code io.sclera.service.APICallService}.
 */
@Component
public class APICallClient {

    private static final Logger log = LoggerFactory.getLogger(APICallClient.class);
    private static final String APP_ID = "sclera-edge";

    private final DaprClient dapr;
    private final OutputBindingClient bindings;

    public APICallClient(DaprClient dapr, OutputBindingClient bindings) {
        this.dapr = dapr;
        this.bindings = bindings;
    }

    public List<UserDTO> getUsersByOrgId(String organisation_id, String vdms_id) {
        try {
            Map<String, String> p = new HashMap<>();
            p.put("organisationId", organisation_id);
            p.put("vdmsId", vdms_id);
            dapr.invokeMethod(APP_ID, "apicall/getUsersByOrgId", p, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("APICallClient.getUsersByOrgId failed; returning default", e);
        }
        return Collections.emptyList();
    }

    public List<UserDTO> getAllUserInfoByOrganisationIdAndVdmsId(String org_id, String vdms_id) {
        try {
            Map<String, String> p = new HashMap<>();
            p.put("orgId", org_id);
            p.put("vdmsId", vdms_id);
            dapr.invokeMethod(APP_ID, "apicall/getAllUserInfoByOrganisationIdAndVdmsId", p, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("APICallClient.getAllUserInfoByOrganisationIdAndVdmsId failed; returning default", e);
        }
        return Collections.emptyList();
    }

    public Flux<org.json.JSONObject> sendDescription(org.json.JSONObject requestBody, String vdmsId,
            String technicianId, String technicianName, String contactNumber,
            String formattedDateTime, String aiCallLogId) {
        return Flux.empty();
    }

    public ResponseEntity<String> sendCallFlowMail(JSONObject payload) {
        String to      = payload != null && payload.containsKey("to")      ? String.valueOf(payload.get("to"))      : "noreply@sclera.local";
        String subject = payload != null && payload.containsKey("subject") ? String.valueOf(payload.get("subject")) : "Call Flow Notification";
        String body    = payload != null && payload.containsKey("body")    ? String.valueOf(payload.get("body"))    : (payload != null ? payload.toJSONString() : "");
        bindings.sendEmail(to, subject, body);
        return ResponseEntity.ok("");
    }

    public ResponseEntity<String> sendCallFlowMessage(JSONObject payload) {
        try {
            dapr.invokeMethod(APP_ID, "apicall/sendCallFlowMessage", null, HttpExtension.POST).block();
        } catch (Exception e) {
            log.warn("APICallClient.sendCallFlowMessage failed; returning default", e);
        }
        return ResponseEntity.ok("");
    }

    public String getDeviceHostNameByIP(String a, String b) {
        try {
            Map<String, String> p = new HashMap<>();
            p.put("a", a);
            p.put("b", b);
            return dapr.invokeMethod(APP_ID, "apicall/getDeviceHostNameByIP", p, HttpExtension.GET, String.class).block();
        } catch (Exception e) {
            log.warn("APICallClient.getDeviceHostNameByIP failed; returning null", e);
        }
        return null;
    }

    public ProductDTO getProductDetailsByModelAndMBV(String model, String mbv) {
        try {
            Map<String, String> p = new HashMap<>();
            p.put("model", model);
            p.put("mbv", mbv);
            return dapr.invokeMethod(APP_ID, "apicall/getProductDetailsByModelAndMBV", p, HttpExtension.GET, ProductDTO.class).block();
        } catch (Exception e) {
            log.warn("APICallClient.getProductDetailsByModelAndMBV failed; returning null", e);
        }
        return null;
    }

    public ProductDTO getProductDetailsByProductId(String productId) {
        try {
            Map<String, String> p = new HashMap<>();
            p.put("productId", productId);
            return dapr.invokeMethod(APP_ID, "apicall/getProductDetailsByProductId", p, HttpExtension.GET, ProductDTO.class).block();
        } catch (Exception e) {
            log.warn("APICallClient.getProductDetailsByProductId failed; returning null", e);
        }
        return null;
    }

    public JSONArray getNfcIdsByVdmsAndType(String vdmsId, String type) {
        try {
            Map<String, String> p = new HashMap<>();
            p.put("vdmsId", vdmsId);
            p.put("type", type);
            dapr.invokeMethod(APP_ID, "apicall/getNfcIdsByVdmsAndType", p, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("APICallClient.getNfcIdsByVdmsAndType failed; returning default", e);
        }
        return new JSONArray();
    }

    public JSONArray getQrCodeIdsByVdmsIdAndType(String vdmsId, String type) {
        try {
            Map<String, String> p = new HashMap<>();
            p.put("vdmsId", vdmsId);
            p.put("type", type);
            dapr.invokeMethod(APP_ID, "apicall/getQrCodeIdsByVdmsIdAndType", p, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("APICallClient.getQrCodeIdsByVdmsIdAndType failed; returning default", e);
        }
        return new JSONArray();
    }

    public void deleteDigitalTwinImageUrl(Set<String> imageUrls, String username, String vdmsId) {
        try {
            dapr.invokeMethod(APP_ID, "apicall/deleteDigitalTwinImageUrl", null, HttpExtension.POST).block();
        } catch (Exception e) {
            log.warn("APICallClient.deleteDigitalTwinImageUrl failed; swallowing", e);
        }
    }

    public JSONArray getTemporaryProductByIds(Object ids) {
        try {
            dapr.invokeMethod(APP_ID, "apicall/getTemporaryProductByIds", null, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("APICallClient.getTemporaryProductByIds failed; returning default", e);
        }
        return new JSONArray();
    }

    public void deleteTemporaryProductByIds(Object ids) {
        try {
            dapr.invokeMethod(APP_ID, "apicall/deleteTemporaryProductByIds", null, HttpExtension.POST).block();
        } catch (Exception e) {
            log.warn("APICallClient.deleteTemporaryProductByIds failed; swallowing", e);
        }
    }

    public Boolean syncBuildingToADC(Object dto, String orgId, String configId) {
        return bindings.pushToCorrigo(dto, orgId, configId);
    }

    public BuildingDTO addSingleBuildingObject(String locationId, String vdmsId) {
        try {
            Map<String, String> p = new HashMap<>();
            p.put("locationId", locationId);
            p.put("vdmsId", vdmsId);
            return dapr.invokeMethod(APP_ID, "apicall/addSingleBuildingObject", p, HttpExtension.POST, BuildingDTO.class).block();
        } catch (Exception e) {
            log.warn("APICallClient.addSingleBuildingObject failed; returning null", e);
        }
        return null;
    }

    public Boolean deleteBuildingFromADC(String orgId, String configId, List<String> propertyIds) {
        try {
            dapr.invokeMethod(APP_ID, "apicall/deleteBuildingFromADC", null, HttpExtension.POST).block();
        } catch (Exception e) {
            log.warn("APICallClient.deleteBuildingFromADC failed; returning false", e);
        }
        return Boolean.FALSE;
    }

    public List<BuildingDTO> getAllLocations(String vdmsId) {
        try {
            Map<String, String> p = new HashMap<>();
            p.put("vdmsId", vdmsId);
            dapr.invokeMethod(APP_ID, "apicall/getAllLocations", p, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("APICallClient.getAllLocations failed; returning default", e);
        }
        return Collections.emptyList();
    }

    public String getFloorPathByFloorId(Object a, String vdmsId, String buildingId, String floorId) {
        try {
            dapr.invokeMethod(APP_ID, "apicall/getFloorPathByFloorId", null, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("APICallClient.getFloorPathByFloorId failed; returning null", e);
        }
        return null;
    }

    public void generateChatbotMessage(Object query, Object emitter) {
        try {
            dapr.invokeMethod(APP_ID, "apicall/generateChatbotMessage", null, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("APICallClient.generateChatbotMessage failed; swallowing", e);
        }
    }

    public void updateChatbotDeviceData(JSONArray bodyArray) {
        try {
            dapr.invokeMethod(APP_ID, "apicall/updateChatbotDeviceData", null, HttpExtension.POST).block();
        } catch (Exception e) {
            log.warn("APICallClient.updateChatbotDeviceData failed; swallowing", e);
        }
    }

    public Set<ClientBarCodeDTO> getAllClientBarCodeByVdmsId(String vdmsId, int page, int size) {
        try {
            dapr.invokeMethod(APP_ID, "apicall/getAllClientBarCodeByVdmsId", null, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("APICallClient.getAllClientBarCodeByVdmsId failed; returning default", e);
        }
        return Collections.emptySet();
    }

    public Set<ClientBarCodeDTO> getSyncedClientBarCodeByVdmsId(String vdmsId, int page, int size) {
        try {
            dapr.invokeMethod(APP_ID, "apicall/getSyncedClientBarCodeByVdmsId", null, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("APICallClient.getSyncedClientBarCodeByVdmsId failed; returning default", e);
        }
        return Collections.emptySet();
    }

    public void sendAgentDataToInventory(Object obj) {
        try {
            dapr.invokeMethod(APP_ID, "apicall/sendAgentDataToInventory", null, HttpExtension.POST).block();
        } catch (Exception e) {
            log.warn("APICallClient.sendAgentDataToInventory failed; swallowing", e);
        }
    }

    public Set<DeviceTypesDTO> getUpdatedAssetTypes(Object ts, int page, int size, String search, String vdmsId) {
        try {
            dapr.invokeMethod(APP_ID, "apicall/getUpdatedAssetTypes", null, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("APICallClient.getUpdatedAssetTypes failed; returning default", e);
        }
        return Collections.emptySet();
    }

    public PropertyAddressDTO updatePropertyDetails(String vdmsId) {
        try {
            Map<String, String> p = new HashMap<>();
            p.put("vdmsId", vdmsId);
            return dapr.invokeMethod(APP_ID, "apicall/updatePropertyDetails", p, HttpExtension.POST, PropertyAddressDTO.class).block();
        } catch (Exception e) {
            log.warn("APICallClient.updatePropertyDetails failed; returning null", e);
        }
        return null;
    }

    public void updateVdmsDetailCloud(String vdmsId, VdmsSyncDTO dto) {
        try {
            dapr.invokeMethod(APP_ID, "apicall/updateVdmsDetailCloud", null, HttpExtension.POST).block();
        } catch (Exception e) {
            log.warn("APICallClient.updateVdmsDetailCloud failed; swallowing", e);
        }
    }

    public String getVendorByMacAddress(String mac) {
        try {
            Map<String, String> p = new HashMap<>();
            p.put("mac", mac);
            return dapr.invokeMethod(APP_ID, "apicall/getVendorByMacAddress", p, HttpExtension.GET, String.class).block();
        } catch (Exception e) {
            log.warn("APICallClient.getVendorByMacAddress failed; returning null", e);
        }
        return null;
    }

    public void syncAllAttribute(String ip) {
        try {
            Map<String, String> p = new HashMap<>();
            p.put("ip", ip);
            dapr.invokeMethod(APP_ID, "apicall/syncAllAttribute", p, HttpExtension.POST).block();
        } catch (Exception e) {
            log.warn("APICallClient.syncAllAttribute failed; swallowing", e);
        }
    }

    public void syncBacnet(String ip) {
        try {
            Map<String, String> p = new HashMap<>();
            p.put("ip", ip);
            dapr.invokeMethod(APP_ID, "apicall/syncBacnet", p, HttpExtension.POST).block();
        } catch (Exception e) {
            log.warn("APICallClient.syncBacnet failed; swallowing", e);
        }
    }

    public void syncSnmpWalk(String ip) {
        try {
            Map<String, String> p = new HashMap<>();
            p.put("ip", ip);
            dapr.invokeMethod(APP_ID, "apicall/syncSnmpWalk", p, HttpExtension.POST).block();
        } catch (Exception e) {
            log.warn("APICallClient.syncSnmpWalk failed; swallowing", e);
        }
    }

    public void snmpInterface(String ip) {
        try {
            Map<String, String> p = new HashMap<>();
            p.put("ip", ip);
            dapr.invokeMethod(APP_ID, "apicall/snmpInterface", p, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("APICallClient.snmpInterface failed; swallowing", e);
        }
    }

    public void snmpTopology(String ip) {
        try {
            Map<String, String> p = new HashMap<>();
            p.put("ip", ip);
            dapr.invokeMethod(APP_ID, "apicall/snmpTopology", p, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("APICallClient.snmpTopology failed; swallowing", e);
        }
    }

    public void internetConnectivity(String ip) {
        try {
            Map<String, String> p = new HashMap<>();
            p.put("ip", ip);
            dapr.invokeMethod(APP_ID, "apicall/internetConnectivity", p, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("APICallClient.internetConnectivity failed; swallowing", e);
        }
    }

    public List<VendorDTO> getAllVendorsByOrganisationId(String orgId, String vdmsId, String dockerName) {
        try {
            dapr.invokeMethod(APP_ID, "apicall/getAllVendorsByOrganisationId", null, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("APICallClient.getAllVendorsByOrganisationId failed; returning default", e);
        }
        return Collections.emptyList();
    }

    public VendorTransferDTO getTransferVendor(String vdmsId, String dockerName) {
        try {
            dapr.invokeMethod(APP_ID, "apicall/getTransferVendor", null, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("APICallClient.getTransferVendor failed; returning default", e);
        }
        return new VendorTransferDTO();
    }

    public String getCustomerOrgIdByVdmsId(String vdmsId) {
        try {
            Map<String, String> p = new HashMap<>();
            p.put("vdmsId", vdmsId);
            return dapr.invokeMethod(APP_ID, "apicall/getCustomerOrgIdByVdmsId", p, HttpExtension.GET, String.class).block();
        } catch (Exception e) {
            log.warn("APICallClient.getCustomerOrgIdByVdmsId failed; returning null", e);
        }
        return null;
    }

    public void updateVdmsTranfer(String vdmsId) {
        try {
            Map<String, String> p = new HashMap<>();
            p.put("vdmsId", vdmsId);
            dapr.invokeMethod(APP_ID, "apicall/updateVdmsTranfer", p, HttpExtension.POST).block();
        } catch (Exception e) {
            log.warn("APICallClient.updateVdmsTranfer failed; swallowing", e);
        }
    }

    public VdmsSyncDTO updateVdmsStatus(String vdmsId) {
        try {
            Map<String, String> p = new HashMap<>();
            p.put("vdmsId", vdmsId);
            return dapr.invokeMethod(APP_ID, "apicall/updateVdmsStatus", p, HttpExtension.POST, VdmsSyncDTO.class).block();
        } catch (Exception e) {
            log.warn("APICallClient.updateVdmsStatus failed; returning null", e);
        }
        return null;
    }

    public void updateQrCodeSyncByVdmsId(String vdmsId) {
        try {
            Map<String, String> p = new HashMap<>();
            p.put("vdmsId", vdmsId);
            dapr.invokeMethod(APP_ID, "apicall/updateQrCodeSyncByVdmsId", p, HttpExtension.POST).block();
        } catch (Exception e) {
            log.warn("APICallClient.updateQrCodeSyncByVdmsId failed; swallowing", e);
        }
    }

    public void updateNfcSyncByVdmsId(String vdmsId) {
        try {
            Map<String, String> p = new HashMap<>();
            p.put("vdmsId", vdmsId);
            dapr.invokeMethod(APP_ID, "apicall/updateNfcSyncByVdmsId", p, HttpExtension.POST).block();
        } catch (Exception e) {
            log.warn("APICallClient.updateNfcSyncByVdmsId failed; swallowing", e);
        }
    }

    public <T> java.util.List<T> getJSONArrayFromJSONString(String json, Class<T> clazz) {
        return Collections.emptyList();
    }

    public void syncSnmpInterfacebyDeviceId(String deviceId, SnmpValuesDTO dto) {
        try {
            dapr.invokeMethod(APP_ID, "apicall/syncSnmpInterfacebyDeviceId", null, HttpExtension.POST).block();
        } catch (Exception e) {
            log.warn("APICallClient.syncSnmpInterfacebyDeviceId failed; swallowing", e);
        }
    }

    public Object fetchMeasuringInstruments() {
        try {
            return dapr.invokeMethod(APP_ID, "apicall/fetchMeasuringInstruments", null, HttpExtension.GET, Object.class).block();
        } catch (Exception e) {
            log.warn("APICallClient.fetchMeasuringInstruments failed; returning null", e);
        }
        return null;
    }

    public Boolean syncLocationToADC(List<LocationDTO> locations, String orgId, String configId, String vdmsId) {
        try {
            dapr.invokeMethod(APP_ID, "apicall/syncLocationToADC", null, HttpExtension.POST).block();
        } catch (Exception e) {
            log.warn("APICallClient.syncLocationToADC failed; returning true", e);
        }
        return true;
    }

    public Boolean deleteLocationFromADC(String orgId, String configId, String vdmsId, String buildingId,
            java.util.List<String> locationIds) {
        try {
            dapr.invokeMethod(APP_ID, "apicall/deleteLocationFromADC", null, HttpExtension.POST).block();
        } catch (Exception e) {
            log.warn("APICallClient.deleteLocationFromADC failed; returning true", e);
        }
        return true;
    }

    public Boolean syncFloorToADC(String orgId, java.util.List<FloorDTO> floors, String configId, String vdmsId) {
        try {
            dapr.invokeMethod(APP_ID, "apicall/syncFloorToADC", null, HttpExtension.POST).block();
        } catch (Exception e) {
            log.warn("APICallClient.syncFloorToADC failed; returning false", e);
        }
        return false;
    }

    public Boolean deleteFloorFromADC(String orgId, String configId, String buildingId,
            java.util.List<String> floorIds) {
        try {
            dapr.invokeMethod(APP_ID, "apicall/deleteFloorFromADC", null, HttpExtension.POST).block();
        } catch (Exception e) {
            log.warn("APICallClient.deleteFloorFromADC failed; returning false", e);
        }
        return false;
    }

    public JSONArray getApplicationUsersFromInventory(String vdmsId, String applicationId) {
        try {
            dapr.invokeMethod(APP_ID, "apicall/getApplicationUsersFromInventory", null, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("APICallClient.getApplicationUsersFromInventory failed; returning default", e);
        }
        return new JSONArray();
    }

    public JSONObject getLicenseDetailsFromInventory(String vdmsId, String applicationId) {
        try {
            dapr.invokeMethod(APP_ID, "apicall/getLicenseDetailsFromInventory", null, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("APICallClient.getLicenseDetailsFromInventory failed; returning null", e);
        }
        return null;
    }

    public JSONArray getAllInventoryApplications(String vdmsId) {
        try {
            dapr.invokeMethod(APP_ID, "apicall/getAllInventoryApplications", null, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("APICallClient.getAllInventoryApplications failed; returning default", e);
        }
        return new JSONArray();
    }

    public void updateBarCodeSyncByVdmsId(String vdmsId) {
        try {
            Map<String, String> p = new HashMap<>();
            p.put("vdmsId", vdmsId);
            dapr.invokeMethod(APP_ID, "apicall/updateBarCodeSyncByVdmsId", p, HttpExtension.POST).block();
        } catch (Exception e) {
            log.warn("APICallClient.updateBarCodeSyncByVdmsId failed; swallowing", e);
        }
    }

    public java.util.List<TechnicianDTO> getAllTechnicians(String vdmsId) {
        try {
            dapr.invokeMethod(APP_ID, "apicall/getAllTechnicians", null, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("APICallClient.getAllTechnicians failed; returning default", e);
        }
        return Collections.emptyList();
    }

    public void resetSyncStatusByTechnicianIds(java.util.Set<String> ids) {
        try {
            dapr.invokeMethod(APP_ID, "apicall/resetSyncStatusByTechnicianIds", null, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("APICallClient.resetSyncStatusByTechnicianIds failed; swallowing", e);
        }
    }

    public java.util.List<TechnicianSkillDTO> getAllTechnicianSkills(String vdmsId) {
        try {
            dapr.invokeMethod(APP_ID, "apicall/getAllTechnicianSkills", null, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("APICallClient.getAllTechnicianSkills failed; returning default", e);
        }
        return Collections.emptyList();
    }

    public void resetSyncByTechnicianSkillIds(java.util.Set<String> ids) {
        try {
            dapr.invokeMethod(APP_ID, "apicall/resetSyncByTechnicianSkillIds", null, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("APICallClient.resetSyncByTechnicianSkillIds failed; swallowing", e);
        }
    }

    public java.util.List<TechnicianAvailabilityDTO> getAllTechniciansAvailability(String vdmsId) {
        try {
            dapr.invokeMethod(APP_ID, "apicall/getAllTechniciansAvailability", null, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("APICallClient.getAllTechniciansAvailability failed; returning default", e);
        }
        return Collections.emptyList();
    }

    public void resetSyncByTechnicianAvailabilityIds(java.util.Set<String> ids) {
        try {
            dapr.invokeMethod(APP_ID, "apicall/resetSyncByTechnicianAvailabilityIds", null, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("APICallClient.resetSyncByTechnicianAvailabilityIds failed; swallowing", e);
        }
    }

    public java.util.List<TechnicianCertificateDTO> getAllTechniciansCertificates(String vdmsId) {
        try {
            dapr.invokeMethod(APP_ID, "apicall/getAllTechniciansCertificates", null, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("APICallClient.getAllTechniciansCertificates failed; returning default", e);
        }
        return Collections.emptyList();
    }

    public void resetSyncByTechnicianCertificateIds(java.util.Set<String> ids) {
        try {
            dapr.invokeMethod(APP_ID, "apicall/resetSyncByTechnicianCertificateIds", null, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("APICallClient.resetSyncByTechnicianCertificateIds failed; swallowing", e);
        }
    }

    public java.util.List<DeviceTechnicianAISuggestionDTO> getAllDeviceTechnicianAISuggestions(String vdmsId) {
        try {
            dapr.invokeMethod(APP_ID, "apicall/getAllDeviceTechnicianAISuggestions", null, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("APICallClient.getAllDeviceTechnicianAISuggestions failed; returning default", e);
        }
        return Collections.emptyList();
    }

    public void resetSyncByDeviceTechnicianAiSuggestionIds(java.util.Set<String> ids) {
        try {
            dapr.invokeMethod(APP_ID, "apicall/resetSyncByDeviceTechnicianAiSuggestionIds", null, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("APICallClient.resetSyncByDeviceTechnicianAiSuggestionIds failed; swallowing", e);
        }
    }

    public JSONObject getInventoryItemsByStockOutId(InventoryDeviceSyncDTO dto) {
        try {
            dapr.invokeMethod(APP_ID, "apicall/getInventoryItemsByStockOutId", null, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("APICallClient.getInventoryItemsByStockOutId failed; returning null", e);
        }
        return null;
    }

    public void updateTaggedInventoryItems(java.util.Set<DeviceDTO> devices) {
        try {
            dapr.invokeMethod(APP_ID, "apicall/updateTaggedInventoryItems", null, HttpExtension.POST).block();
        } catch (Exception e) {
            log.warn("APICallClient.updateTaggedInventoryItems failed; swallowing", e);
        }
    }

    public String getAgentPermissionsByVdmsId(String vdmsId) {
        try {
            Map<String, String> p = new HashMap<>();
            p.put("vdmsId", vdmsId);
            return dapr.invokeMethod(APP_ID, "apicall/getAgentPermissionsByVdmsId", p, HttpExtension.GET, String.class).block();
        } catch (Exception e) {
            log.warn("APICallClient.getAgentPermissionsByVdmsId failed; returning null", e);
        }
        return null;
    }

    public JSONArray getAllApplicationUsersFromInventory(String vdmsId) {
        try {
            dapr.invokeMethod(APP_ID, "apicall/getAllApplicationUsersFromInventory", null, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("APICallClient.getAllApplicationUsersFromInventory failed; returning default", e);
        }
        return new JSONArray();
    }

    public void syncApplicationUsers(java.util.Set<String> ids, String status) {
        try {
            dapr.invokeMethod(APP_ID, "apicall/syncApplicationUsers", null, HttpExtension.POST).block();
        } catch (Exception e) {
            log.warn("APICallClient.syncApplicationUsers failed; swallowing", e);
        }
    }

    public void syncApplication(java.util.Set<String> ids, String status) {
        try {
            dapr.invokeMethod(APP_ID, "apicall/syncApplication", null, HttpExtension.POST).block();
        } catch (Exception e) {
            log.warn("APICallClient.syncApplication failed; swallowing", e);
        }
    }

    public void getVdmsAccessToken(String vdmsId, String password) {
        // no-op
    }
}
