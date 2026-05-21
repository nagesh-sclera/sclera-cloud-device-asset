package io.sclera.edge.controller;

import io.sclera.edge.defaults.Defaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/apicall")
public class ApiCallController {

    @GetMapping("/getUsersByOrgId")
    public List<String> getUsersByOrgId(
            @RequestParam(required = false) String organisationId,
            @RequestParam(required = false) String vdmsId) {
        return Defaults.emptyList();
    }

    @GetMapping("/getAllUserInfoByOrganisationIdAndVdmsId")
    public List<String> getAllUserInfoByOrganisationIdAndVdmsId(
            @RequestParam(required = false) String orgId,
            @RequestParam(required = false) String vdmsId) {
        return Defaults.emptyList();
    }

    @PostMapping("/sendCallFlowMessage")
    public String sendCallFlowMessage(@RequestBody(required = false) String body) {
        return Defaults.NULL_STRING;
    }

    @GetMapping("/getDeviceHostNameByIP")
    public String getDeviceHostNameByIP(
            @RequestParam(required = false) String a,
            @RequestParam(required = false) String b) {
        return Defaults.NULL_STRING;
    }

    @GetMapping("/getProductDetailsByModelAndMBV")
    public String getProductDetailsByModelAndMBV(
            @RequestParam(required = false) String model,
            @RequestParam(required = false) String mbv) {
        return Defaults.NULL_STRING;
    }

    @GetMapping("/getProductDetailsByProductId")
    public String getProductDetailsByProductId(
            @RequestParam(required = false) String productId) {
        return Defaults.NULL_STRING;
    }

    @GetMapping("/getNfcIdsByVdmsAndType")
    public String getNfcIdsByVdmsAndType(
            @RequestParam(required = false) String vdmsId,
            @RequestParam(required = false) String type) {
        return Defaults.NULL_STRING;
    }

    @GetMapping("/getQrCodeIdsByVdmsIdAndType")
    public String getQrCodeIdsByVdmsIdAndType(
            @RequestParam(required = false) String vdmsId,
            @RequestParam(required = false) String type) {
        return Defaults.NULL_STRING;
    }

    @PostMapping("/deleteDigitalTwinImageUrl")
    public void deleteDigitalTwinImageUrl(@RequestBody(required = false) String body) {
        // no-op
    }

    @GetMapping("/getTemporaryProductByIds")
    public String getTemporaryProductByIds(@RequestParam(required = false) String ids) {
        return Defaults.NULL_STRING;
    }

    @PostMapping("/deleteTemporaryProductByIds")
    public void deleteTemporaryProductByIds(@RequestBody(required = false) String body) {
        // no-op
    }

    @PostMapping("/addSingleBuildingObject")
    public String addSingleBuildingObject(
            @RequestBody(required = false) String body,
            @RequestParam(required = false) String locationId,
            @RequestParam(required = false) String vdmsId) {
        return Defaults.NULL_STRING;
    }

    @PostMapping("/deleteBuildingFromADC")
    public Boolean deleteBuildingFromADC(@RequestBody(required = false) String body) {
        return Defaults.FALSE;
    }

    @GetMapping("/getAllLocations")
    public List<String> getAllLocations(@RequestParam(required = false) String vdmsId) {
        return Defaults.emptyList();
    }

    @GetMapping("/getFloorPathByFloorId")
    public String getFloorPathByFloorId(@RequestParam(required = false) String floorId) {
        return Defaults.NULL_STRING;
    }

    @GetMapping("/generateChatbotMessage")
    public void generateChatbotMessage(@RequestParam(required = false) String query) {
        // no-op
    }

    @PostMapping("/updateChatbotDeviceData")
    public void updateChatbotDeviceData(@RequestBody(required = false) String body) {
        // no-op
    }

    @GetMapping("/getAllClientBarCodeByVdmsId")
    public Set<String> getAllClientBarCodeByVdmsId(
            @RequestParam(required = false) String vdmsId,
            @RequestParam(required = false) String page,
            @RequestParam(required = false) String size) {
        return Defaults.emptySet();
    }

    @GetMapping("/getSyncedClientBarCodeByVdmsId")
    public Set<String> getSyncedClientBarCodeByVdmsId(
            @RequestParam(required = false) String vdmsId,
            @RequestParam(required = false) String page,
            @RequestParam(required = false) String size) {
        return Defaults.emptySet();
    }

    @PostMapping("/sendAgentDataToInventory")
    public void sendAgentDataToInventory(@RequestBody(required = false) String body) {
        // no-op
    }

    @GetMapping("/getUpdatedAssetTypes")
    public Set<String> getUpdatedAssetTypes(@RequestParam(required = false) String vdmsId) {
        return Defaults.emptySet();
    }

    @PostMapping("/updatePropertyDetails")
    public String updatePropertyDetails(
            @RequestBody(required = false) String body,
            @RequestParam(required = false) String vdmsId) {
        return Defaults.NULL_STRING;
    }

    @PostMapping("/updateVdmsDetailCloud")
    public void updateVdmsDetailCloud(@RequestBody(required = false) String body) {
        // no-op
    }

    @GetMapping("/getVendorByMacAddress")
    public String getVendorByMacAddress(@RequestParam(required = false) String mac) {
        return Defaults.NULL_STRING;
    }

    @PostMapping("/syncAllAttribute")
    public void syncAllAttribute(
            @RequestBody(required = false) String body,
            @RequestParam(required = false) String ip) {
        // no-op
    }

    @PostMapping("/syncBacnet")
    public void syncBacnet(
            @RequestBody(required = false) String body,
            @RequestParam(required = false) String ip) {
        // no-op
    }

    @PostMapping("/syncSnmpWalk")
    public void syncSnmpWalk(
            @RequestBody(required = false) String body,
            @RequestParam(required = false) String ip) {
        // no-op
    }

    @GetMapping("/snmpInterface")
    public void snmpInterface(@RequestParam(required = false) String ip) {
        // no-op
    }

    @GetMapping("/snmpTopology")
    public void snmpTopology(@RequestParam(required = false) String ip) {
        // no-op
    }

    @GetMapping("/internetConnectivity")
    public void internetConnectivity(@RequestParam(required = false) String ip) {
        // no-op
    }

    @GetMapping("/getAllVendorsByOrganisationId")
    public List<String> getAllVendorsByOrganisationId(
            @RequestParam(required = false) String orgId,
            @RequestParam(required = false) String vdmsId,
            @RequestParam(required = false) String dockerName) {
        return Defaults.emptyList();
    }

    @GetMapping("/getTransferVendor")
    public String getTransferVendor(
            @RequestParam(required = false) String vdmsId,
            @RequestParam(required = false) String dockerName) {
        return Defaults.NULL_STRING;
    }

    @GetMapping("/getCustomerOrgIdByVdmsId")
    public String getCustomerOrgIdByVdmsId(@RequestParam(required = false) String vdmsId) {
        return Defaults.NULL_STRING;
    }

    @PostMapping("/updateVdmsTranfer")
    public void updateVdmsTranfer(
            @RequestBody(required = false) String body,
            @RequestParam(required = false) String vdmsId) {
        // no-op
    }

    @PostMapping("/updateVdmsStatus")
    public String updateVdmsStatus(
            @RequestBody(required = false) String body,
            @RequestParam(required = false) String vdmsId) {
        return Defaults.NULL_STRING;
    }

    @PostMapping("/updateQrCodeSyncByVdmsId")
    public void updateQrCodeSyncByVdmsId(
            @RequestBody(required = false) String body,
            @RequestParam(required = false) String vdmsId) {
        // no-op
    }

    @PostMapping("/updateNfcSyncByVdmsId")
    public void updateNfcSyncByVdmsId(
            @RequestBody(required = false) String body,
            @RequestParam(required = false) String vdmsId) {
        // no-op
    }

    @PostMapping("/syncSnmpInterfacebyDeviceId")
    public void syncSnmpInterfacebyDeviceId(@RequestBody(required = false) String body) {
        // no-op
    }

    @GetMapping("/fetchMeasuringInstruments")
    public String fetchMeasuringInstruments() {
        return Defaults.NULL_STRING;
    }

    @PostMapping("/syncLocationToADC")
    public Boolean syncLocationToADC(@RequestBody(required = false) String body) {
        return Defaults.FALSE;
    }

    @PostMapping("/deleteLocationFromADC")
    public Boolean deleteLocationFromADC(@RequestBody(required = false) String body) {
        return Defaults.FALSE;
    }

    @PostMapping("/syncFloorToADC")
    public Boolean syncFloorToADC(@RequestBody(required = false) String body) {
        return Defaults.FALSE;
    }

    @PostMapping("/deleteFloorFromADC")
    public Boolean deleteFloorFromADC(@RequestBody(required = false) String body) {
        return Defaults.FALSE;
    }

    @GetMapping("/getApplicationUsersFromInventory")
    public String getApplicationUsersFromInventory(
            @RequestParam(required = false) String vdmsId,
            @RequestParam(required = false) String applicationId) {
        return Defaults.NULL_STRING;
    }

    @GetMapping("/getLicenseDetailsFromInventory")
    public String getLicenseDetailsFromInventory(
            @RequestParam(required = false) String vdmsId,
            @RequestParam(required = false) String applicationId) {
        return Defaults.NULL_STRING;
    }

    @GetMapping("/getAllInventoryApplications")
    public String getAllInventoryApplications(@RequestParam(required = false) String vdmsId) {
        return Defaults.NULL_STRING;
    }

    @PostMapping("/updateBarCodeSyncByVdmsId")
    public void updateBarCodeSyncByVdmsId(
            @RequestBody(required = false) String body,
            @RequestParam(required = false) String vdmsId) {
        // no-op
    }

    @GetMapping("/getAllTechnicians")
    public List<String> getAllTechnicians(@RequestParam(required = false) String vdmsId) {
        return Defaults.emptyList();
    }

    @GetMapping("/resetSyncStatusByTechnicianIds")
    public void resetSyncStatusByTechnicianIds(@RequestParam(required = false) String ids) {
        // no-op
    }

    @GetMapping("/getAllTechnicianSkills")
    public List<String> getAllTechnicianSkills(@RequestParam(required = false) String vdmsId) {
        return Defaults.emptyList();
    }

    @GetMapping("/resetSyncByTechnicianSkillIds")
    public void resetSyncByTechnicianSkillIds(@RequestParam(required = false) String ids) {
        // no-op
    }

    @GetMapping("/getAllTechniciansAvailability")
    public List<String> getAllTechniciansAvailability(@RequestParam(required = false) String vdmsId) {
        return Defaults.emptyList();
    }

    @GetMapping("/resetSyncByTechnicianAvailabilityIds")
    public void resetSyncByTechnicianAvailabilityIds(@RequestParam(required = false) String ids) {
        // no-op
    }

    @GetMapping("/getAllTechniciansCertificates")
    public List<String> getAllTechniciansCertificates(@RequestParam(required = false) String vdmsId) {
        return Defaults.emptyList();
    }

    @GetMapping("/resetSyncByTechnicianCertificateIds")
    public void resetSyncByTechnicianCertificateIds(@RequestParam(required = false) String ids) {
        // no-op
    }

    @GetMapping("/getAllDeviceTechnicianAISuggestions")
    public List<String> getAllDeviceTechnicianAISuggestions(@RequestParam(required = false) String vdmsId) {
        return Defaults.emptyList();
    }

    @GetMapping("/resetSyncByDeviceTechnicianAiSuggestionIds")
    public void resetSyncByDeviceTechnicianAiSuggestionIds(@RequestParam(required = false) String ids) {
        // no-op
    }

    @GetMapping("/getInventoryItemsByStockOutId")
    public String getInventoryItemsByStockOutId(@RequestParam(required = false) String dto) {
        return Defaults.NULL_STRING;
    }

    @PostMapping("/updateTaggedInventoryItems")
    public void updateTaggedInventoryItems(@RequestBody(required = false) String body) {
        // no-op
    }

    @GetMapping("/getAgentPermissionsByVdmsId")
    public String getAgentPermissionsByVdmsId(@RequestParam(required = false) String vdmsId) {
        return Defaults.NULL_STRING;
    }

    @GetMapping("/getAllApplicationUsersFromInventory")
    public String getAllApplicationUsersFromInventory(@RequestParam(required = false) String vdmsId) {
        return Defaults.NULL_STRING;
    }

    @PostMapping("/syncApplicationUsers")
    public void syncApplicationUsers(@RequestBody(required = false) String body) {
        // no-op
    }

    @PostMapping("/syncApplication")
    public void syncApplication(@RequestBody(required = false) String body) {
        // no-op
    }
}
