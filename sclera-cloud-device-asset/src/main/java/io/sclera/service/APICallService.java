package io.sclera.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.sclera.dto.*;
import io.sclera.dto.touchscreen.SnmpValuesDTO;
import io.sclera.dto.touchscreen.settings.UserDTO;
import io.sclera.utils.StubLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/** STUB: replace with remote call to edge-D */
@Service
public class APICallService {

    private static final Logger log = LoggerFactory.getLogger(APICallService.class);

    public List<UserDTO> getUsersByOrgId(String organisation_id, String vdms_id) { return Collections.emptyList(); }
    public List<UserDTO> getAllUserInfoByOrganisationIdAndVdmsId(String org_id, String vdms_id) { return Collections.emptyList(); }

    public Flux<org.json.JSONObject> sendDescription(org.json.JSONObject requestBody, String vdmsId, String technicianId,
                                                     String technicianName, String contactNumber,
                                                     String formattedDateTime, String aiCallLogId) {
        return Flux.empty();
    }

    public ResponseEntity<String> sendCallFlowMail(JSONObject payload) { return ResponseEntity.ok(""); }
    public ResponseEntity<String> sendCallFlowMessage(JSONObject payload) { return ResponseEntity.ok(""); }

    public String getDeviceHostNameByIP(String a, String b) { return null; }
    public ProductDTO getProductDetailsByModelAndMBV(String model, String mbv) { return null; }
    public ProductDTO getProductDetailsByProductId(String productId) { return null; }

    public JSONArray getNfcIdsByVdmsAndType(String vdmsId, String type) { return new JSONArray(); }
    public JSONArray getQrCodeIdsByVdmsIdAndType(String vdmsId, String type) { return new JSONArray(); }
    public void deleteDigitalTwinImageUrl(Set<String> imageUrls, String username, String vdmsId) {}

    public JSONArray getTemporaryProductByIds(Object ids) { return new JSONArray(); }
    public void deleteTemporaryProductByIds(Object ids) {}

    public Boolean syncBuildingToADC(Object dto, String orgId, String configId) { return Boolean.FALSE; }
    public BuildingDTO addSingleBuildingObject(String locationId, String vdmsId) { return null; }
    public Boolean deleteBuildingFromADC(String orgId, String configId, List<String> propertyIds) { return Boolean.FALSE; }
    public List<BuildingDTO> getAllLocations(String vdmsId) { return Collections.emptyList(); }
    public String getFloorPathByFloorId(Object a, String vdmsId, String buildingId, String floorId) { return null; }

    public void generateChatbotMessage(Object query, Object emitter) {}
    public void updateChatbotDeviceData(JSONArray bodyArray) {}

    public Set<ClientBarCodeDTO> getAllClientBarCodeByVdmsId(String vdmsId, int page, int size) { return Collections.emptySet(); }
    public Set<ClientBarCodeDTO> getSyncedClientBarCodeByVdmsId(String vdmsId, int page, int size) { return Collections.emptySet(); }

    public void sendAgentDataToInventory(Object obj) {}
    public Set<DeviceTypesDTO> getUpdatedAssetTypes(Object ts, int page, int size, String search, String vdmsId) { return Collections.emptySet(); }

    public PropertyAddressDTO updatePropertyDetails(String vdmsId) { return null; }
    public void updateVdmsDetailCloud(String vdmsId, VdmsSyncDTO dto) {}
    public String getVendorByMacAddress(String mac) { return null; }
    public void syncAllAttribute(String ip) {}
    public void syncBacnet(String ip) {}
    public void syncSnmpWalk(String ip) {}
    public void snmpInterface(String ip) {}
    public void snmpTopology(String ip) {}
    public void internetConnectivity(String ip) {}
    public List<VendorDTO> getAllVendorsByOrganisationId(String orgId, String vdmsId, String dockerName) { return Collections.emptyList(); }
    public VendorTransferDTO getTransferVendor(String vdmsId, String dockerName) { return new VendorTransferDTO(); }
    public String getCustomerOrgIdByVdmsId(String vdmsId) { return null; }
    public void updateVdmsTranfer(String vdmsId) {}
    public VdmsSyncDTO updateVdmsStatus(String vdmsId) { return null; }
    public void updateQrCodeSyncByVdmsId(String vdmsId) {}
    public void updateNfcSyncByVdmsId(String vdmsId) {}
    public <T> java.util.List<T> getJSONArrayFromJSONString(String json, Class<T> clazz) { return Collections.emptyList(); }
    public void syncSnmpInterfacebyDeviceId(String deviceId, SnmpValuesDTO dto) {}

    public Object fetchMeasuringInstruments() {
        StubLog.warn("APICallService", "fetchMeasuringInstruments", "AP-C1edge");
        return null;
    }
    public Boolean syncLocationToADC(List<LocationDTO> locations,
                                     String orgId, String configId, String vdmsId) {
        StubLog.warn("APICallService", "syncLocationToADC", "AP-C1edge");
        return true;
    }
    public Boolean deleteLocationFromADC(String orgId, String configId, String vdmsId,
                                       String buildingId, java.util.List<String> locationIds) {
        StubLog.warn("APICallService", "deleteLocationFromADC", "AP-C1edge");
        return true;
    }
    public Boolean syncFloorToADC(String orgId, java.util.List<FloorDTO> floors, String configId, String vdmsId) {
        StubLog.warn("APICallService", "syncFloorToADC", "AP-C1edge"); return false;
    }
    public Boolean deleteFloorFromADC(String orgId, String configId, String buildingId, java.util.List<String> floorIds) {
        StubLog.warn("APICallService", "deleteFloorFromADC", "AP-C1edge"); return false;
    }
    public JSONArray getApplicationUsersFromInventory(String vdmsId, String applicationId) {
        StubLog.warn("APICallService", "getApplicationUsersFromInventory", "AP-C1edge"); return new JSONArray();
    }
    public JSONObject getLicenseDetailsFromInventory(String vdmsId, String applicationId) {
        StubLog.warn("APICallService", "getLicenseDetailsFromInventory", "AP-C1edge"); return null;
    }
    public JSONArray getAllInventoryApplications(String vdmsId) {
        StubLog.warn("APICallService", "getAllInventoryApplications", "AP-C1edge"); return new JSONArray();
    }
    public void updateBarCodeSyncByVdmsId(String vdmsId) {
        StubLog.warn("APICallService", "updateBarCodeSyncByVdmsId", "AP-C1edge");
    }
    public java.util.List<io.sclera.dto.TechnicianDTO> getAllTechnicians(String vdmsId) {
        StubLog.warn("APICallService", "getAllTechnicians", "AP-C1edge"); return Collections.emptyList();
    }
    public void resetSyncStatusByTechnicianIds(java.util.Set<String> ids) {
        StubLog.warn("APICallService", "resetSyncStatusByTechnicianIds", "AP-C1edge");
    }
    public java.util.List<io.sclera.dto.TechnicianSkillDTO> getAllTechnicianSkills(String vdmsId) {
        StubLog.warn("APICallService", "getAllTechnicianSkills", "AP-C1edge"); return Collections.emptyList();
    }
    public void resetSyncByTechnicianSkillIds(java.util.Set<String> ids) {
        StubLog.warn("APICallService", "resetSyncByTechnicianSkillIds", "AP-C1edge");
    }
    public java.util.List<io.sclera.dto.TechnicianAvailabilityDTO> getAllTechniciansAvailability(String vdmsId) {
        StubLog.warn("APICallService", "getAllTechniciansAvailability", "AP-C1edge"); return Collections.emptyList();
    }
    public void resetSyncByTechnicianAvailabilityIds(java.util.Set<String> ids) {
        StubLog.warn("APICallService", "resetSyncByTechnicianAvailabilityIds", "AP-C1edge");
    }
    public java.util.List<io.sclera.dto.TechnicianCertificateDTO> getAllTechniciansCertificates(String vdmsId) {
        StubLog.warn("APICallService", "getAllTechniciansCertificates", "AP-C1edge"); return Collections.emptyList();
    }
    public void resetSyncByTechnicianCertificateIds(java.util.Set<String> ids) {
        StubLog.warn("APICallService", "resetSyncByTechnicianCertificateIds", "AP-C1edge");
    }
    public java.util.List<io.sclera.dto.DeviceTechnicianAISuggestionDTO> getAllDeviceTechnicianAISuggestions(String vdmsId) {
        StubLog.warn("APICallService", "getAllDeviceTechnicianAISuggestions", "AP-C1edge"); return Collections.emptyList();
    }
    public void resetSyncByDeviceTechnicianAiSuggestionIds(java.util.Set<String> ids) {
        StubLog.warn("APICallService", "resetSyncByDeviceTechnicianAiSuggestionIds", "AP-C1edge");
    }
    public JSONObject getInventoryItemsByStockOutId(io.sclera.dto.InventoryDeviceSyncDTO dto) {
        StubLog.warn("APICallService", "getInventoryItemsByStockOutId", "AP-C1edge"); return null;
    }
    public void updateTaggedInventoryItems(java.util.Set<io.sclera.dto.DeviceDTO> devices) {
        StubLog.warn("APICallService", "updateTaggedInventoryItems", "AP-C1edge");
    }
    public String getAgentPermissionsByVdmsId(String vdmsId) {
        StubLog.warn("APICallService", "getAgentPermissionsByVdmsId", "AP-C1edge"); return null;
    }
    public JSONArray getAllApplicationUsersFromInventory(String vdmsId) {
        StubLog.warn("APICallService", "getAllApplicationUsersFromInventory", "AP-C1edge"); return new JSONArray();
    }
    public void syncApplicationUsers(java.util.Set<String> ids, String status) {
        StubLog.warn("APICallService", "syncApplicationUsers", "AP-C1edge");
    }
    public void syncApplication(java.util.Set<String> ids, String status) {
        StubLog.warn("APICallService", "syncApplication", "AP-C1edge");
    }

    public void getVdmsAccessToken(String vdmsId, String password) {

    }
}
