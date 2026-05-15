package io.sclera.service;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.itextpdf.text.pdf.Barcode;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.sclera.Repository.VdmsRepository;
import io.sclera.dto.*;
import io.sclera.dto.touchscreen.settings.VdmsDTO;
import io.sclera.integration.dto.LocationIntegrationDTO;
import io.sclera.models.*;
import io.sclera.queryrepository.LocationQueryRepository;
import io.sclera.utils.AuthenticationUtils;
import io.sclera.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.uuid.Generators;

import io.sclera.Repository.LocationRepository;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;

@Service
public class LocationService {
    private static final Logger log = LoggerFactory.getLogger(LocationService.class);

    @Autowired
    LocationRepository locationRepository;

    @Autowired
    RecordChecklistService recordChecklistService;

    @Autowired
    PropertyQrcodeService propertyQrcodeService;

    @Autowired
    GlobalQrcodeService globalQrcodeService;

    @Autowired
    DeviceService deviceService;

    @Autowired
    APICallService apicallService;

    @Autowired
    PmsService pmsService;

    @Autowired
    MeasuringInstrumentService measuringInstrumentService;

    @Autowired
    AuthenticationUtils authenticationUtils;

    @Autowired
    UserActionLogService userActionLogService;

    @Autowired
    Utils utils;

    @Autowired
    QrCodeService qrCodeService;

    @Autowired
    ClientQrCodeService clientQrCodeService;

    @Autowired
    NfcService nfcService;

    @Autowired
    ClientNfcService clientNfcService;

    @Autowired
    DataSource dataSource;

    @Autowired
    LocationQueryRepository locationQueryRepository;

    @Autowired
    ClientBarCodeService clientBarCodeService;

    @Autowired
    ArchivedRecordService archivedRecordService;

    @Autowired
    GlobalInspectionRecordService globalInspectionRecordService;

    @Autowired
    InspectionRecordService inspectionRecordService;

    @Autowired
    GlobalChecklistConditionsService globalChecklistConditionsService;

    @Autowired
    VdmsRepository vdmsRepository;


    public void upsertLocationByFloorId(Set<LocationDTO> locations, String floor_id) {
        if (locations != null && locations.size() > 0) {
            Set<String> location_ids = locationRepository.getLocationIdsByFloorId(floor_id);
            if (location_ids != null) {
                for (LocationDTO location : locations) {
                    if (compareIds(location_ids, location.getLocation_id())) {
                        updateLocationByLocationId(location);
                    } else {
                        addLocationByFloorId(location, floor_id);
                    }
                }
            } else {
                for (LocationDTO location : locations) {
                    addLocationByFloorId(location, floor_id);
                }
            }
        }
    }


    public String addLocationByFloorId(LocationDTO locationdto, String floor_id) {
        if (locationdto.getLocation_id() == null) {
            String id = Generators.timeBasedGenerator().generate().toString();
            locationdto.setLocation_id(id);
        }
        BigInteger timestamp = BigInteger.valueOf(System.currentTimeMillis());
        int rowsAffected = locationRepository.addLocationByFloorId(locationdto.getLocation_id(), locationdto.getName(), locationdto.getPosition(), floor_id, locationdto.getArea(), locationdto.getType(), timestamp);
        if(rowsAffected > 0){
            syncLocationToADCServer(List.of(locationdto), floor_id);
        }
        return locationdto.getLocation_id();
    }

    public void syncLocationToADCServer(List<LocationDTO> locationdto, String floor_id) {
        try {
            VdmsDTO vdmsDetails = vdmsRepository.getSyncDetailsForADC();
            for(LocationDTO location : locationdto){
                if(location.getLocation_id() != null && !location.getLocation_id().isEmpty())
                    location.setId(location.getLocation_id());
            }

            Boolean status = apicallService.syncLocationToADC(locationdto, floor_id, vdmsDetails.getCustomer_org_id(), vdmsDetails.getAdc_configuration_id());
            log.info("Location synced to ADC with status: {}", status);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void updateLocationByLocationId(LocationDTO locationdto) {
        BigInteger timestamp = BigInteger.valueOf(System.currentTimeMillis());
        int rowsAffected = locationRepository.updateLocationByLocationId(locationdto.getName(), locationdto.getPosition(), locationdto.getLocation_id(), locationdto.getArea(), locationdto.getType(),timestamp);
        if(rowsAffected > 0)
            syncLocationToADCServer(List.of(locationdto), null);
    }


    public boolean compareIds(Set<String> location_ids, String location_id) {
        return location_ids.stream()
                .anyMatch(l -> l.equals(location_id));
    }


    //delete locations not tagged to device
    public void deleteUnlinkedLocations() {
        Set<String> unlikedLocationIds = locationRepository.getUnlinkedLocationIds();

        for (String locationId : unlikedLocationIds) {
            locationRepository.deleteById(locationId);
        }
    }


    public Boolean checkLocationById(String location_id) {
        if (locationRepository.checkLocationById(location_id) > 0) {
            return true;
        } else {
            return false;
        }
    }

    public String getPositionByLocationId(String location_id) {
        return locationRepository.getPositionByLocationId(location_id);
    }

    public LocationDTO getLocationDetails(String location_id) {
        return locationRepository.getLocationDetails(location_id);
    }

    public void updateLocationRecordChecklistStatus(String location_id, String record_type) {
        try {
            if (location_id != null) {
                String record_checklist_status = recordChecklistService.getRecordChecklistStatusByLocationId(location_id, record_type);
                String checklist_status = "completed";
                if (record_checklist_status.equals("todo")) {
                    checklist_status = "todo";
                }
                locationRepository.updateLocationRecordChecklistStatus(location_id, checklist_status);
            }
        } catch (Exception e) {
            log.error("Error in updating record checklist  status by device id : ", e);
        }
    }

    public void updateLocationRecordChecklistCount(String location_id, String record_type) {
        Integer record_checklist_count = recordChecklistService.getChecklistStatusCountLocationId(location_id, "inspection", record_type);
        locationRepository.updateLocationRecordChecklistCount(location_id, record_checklist_count);

    }

    public void updateLocationRecordChecklistStatusById(String location_id, String record_type) {
        if (location_id != null) {
            this.updateLocationRecordChecklistStatus(location_id, record_type);
            this.updateLocationRecordChecklistCount(location_id, record_type);
        }
    }

    /********************************************* new location changes *******************************/


    public Set<LocationDTO> upsertLocationsByFloorId(String username, String vdms_id, String floor_id, Set<LocationDTO> locations, HttpServletRequest httpServletRequest) {
        String action;
        for (LocationDTO location : locations) {
            action = "UPDATE";
            if (location.getLocation_id() == null) {
                location.setLocation_id(Generators.timeBasedGenerator().generate().toString());
                action = "ADD";
            }
            this.upsertLocationByFloorId(floor_id, location, username, action, httpServletRequest);
        }
        return locations;
    }

    public void upsertLocationByFloorId(String floor_id, LocationDTO location, String username, String action, HttpServletRequest httpServletRequest) {
        try {
            BigInteger timestamp = BigInteger.valueOf(System.currentTimeMillis());
            int rowsAffected = locationRepository.upsertLocationByFloorId(location.getLocation_id(), location.getName(), location.getPosition(), location.getArea(), floor_id, location.getStatus(), location.getType(), location.getCode(), timestamp);
            if(rowsAffected > 0)
                syncLocationToADCServer(List.of(location), floor_id);
            if (action.equals("ADD")) {
                userActionLogService.addUserAction(username, "maps", action, "A Location with name: " + location.getName() + " and id: " + location.getLocation_id() + " is added", "success", "location", location.getLocation_id());
                log.info("endpoint: {}, upsertLocationByFloorId, description: A new location is added, params: location: {} ", httpServletRequest.getRequestURI(), location);

            } else {
                userActionLogService.addUserAction(username, "maps", action, "A Location with name: " + location.getName() + " and id: " + floor_id + " is updated", "success", "location", location.getLocation_id());
                log.info("endpoint: {}, upsertLocationByFloorId, description: A location is updated, params: location: {}", httpServletRequest.getRequestURI(), location);
            }
        } catch (Exception e) {
            if (action.equals("ADD")) {
                userActionLogService.addUserAction(username, "maps", action, "Unable to add Location with name: " + location.getName() + " and  id: " + location.getLocation_id(), "failed", "location", location.getLocation_id());
                log.error("Exception in ADD location, endpoint: {} ,  Error message : ", httpServletRequest.getRequestURI(), e);

            } else {
                userActionLogService.addUserAction(username, "maps", action, "Unable to update Location with name: " + location.getName() + " and floor with id: " + floor_id, "failed", "location", location.getLocation_id());
                log.error("Exception in UPDATE location, endpoint: {} ,  Error message : ", httpServletRequest.getRequestURI(), e);
            }
        }
    }

    @Transactional
    public void deleteLocationsByIds(String email, String vdms_id, Set<String> location_ids, Boolean isSocketCall) {
        for (String location_id : location_ids) {
//            this.deleteLocationByLocationId(location_id, email);
            this.softDeleteLocationByLocationId(location_id, email, isSocketCall);
        }
    }


    private void softDeleteLocationByLocationId(String location_id, String username, Boolean isSocketCall) {
        Location location = locationRepository.findById(location_id).orElse(null);
        if (location == null) {
            log.warn("Location not found for id {}", location_id);
            return;
        }
        List<UserActionLogDTO> userActionLogDTOS = new ArrayList<>();
        List<String> finalRecordChecklistIds = new ArrayList<>();
        List<String> finalGlobalInspectionRelationIds = new ArrayList<>();
        Set<String> finalInspectionrecordIds = new HashSet<>();

        try {
            // delete from measuring instrument location table
            measuringInstrumentService.deleteMeasuringIntrumentLocationsByLocationId(location_id);
            deviceService.updateDeviceLocation(location_id, username);
            propertyQrcodeService.updatePropertyServiceLocations(location_id);
//
//            for(RecordChecklist recordChecklist : location.getRecord_checklist()){
//                UserActionLogDTO userActionLogDTO = new UserActionLogDTO();
//                if(recordChecklist.getRecord_type().equals("checklist") && recordChecklist.getInspection_record() == null){
//                    userActionLogDTO.setType("procedure");
//                    userActionLogDTO.setSub_type("tagged_procedure");
//                } else if (recordChecklist.getRecord_type().equals("checklist") && recordChecklist.getInspection_record() != null) {
//                    userActionLogDTO.setType("inspection");
//                    userActionLogDTO.setSub_type("inspection_checklist");
//                } else if (recordChecklist.getRecord_type().equals("service") && recordChecklist.getInspection_record() == null) {
//                    userActionLogDTO.setType("reactive_service");
//                    userActionLogDTO.setSub_type("service_request");
//                } else if (recordChecklist.getRecord_type().equals("service") && recordChecklist.getInspection_record() != null) {
//                    userActionLogDTO.setSub_type("scheduled_service");
//                    userActionLogDTO.setSub_type("service_checklist");
//                }
//                userActionLogDTO.setStatus("success");
//                userActionLogDTO.setPrimary_id(recordChecklist.getId());
//                userActionLogDTO.setEmail(username);
//                userActionLogDTO.setSecondary_id(location_id);
//                userActionLogDTO.setTable_name("record_checklist");
//                userActionLogDTO.setCreated_timestamp(BigInteger.valueOf(System.currentTimeMillis()));
//                userActionLogDTO.setMessage("Record Checklist "+recordChecklist.getId()+" tagged to location "+location_id+" has been soft deleted");
//                userActionLogDTOS.add(userActionLogDTO);
//                System.out.println("Deleted RecordChecklist Id :"+recordChecklist.getId());
//            }
//
//            for(GlobalInspectionRelation globalInspectionRelation : location.getGlobal_inspection_relation()){
//                UserActionLogDTO userActionLogDTO = new UserActionLogDTO();
//                userActionLogDTO.setPrimary_id(globalInspectionRelation.getId());
//                userActionLogDTO.setEmail(username);
//                userActionLogDTO.setStatus("success");
//                userActionLogDTO.setSecondary_id(location_id);
//                userActionLogDTO.setTable_name("global_inspection_relation");
//                userActionLogDTO.setCreated_timestamp(BigInteger.valueOf(System.currentTimeMillis()));
//                userActionLogDTO.setMessage("Global Inspection Relation "+globalInspectionRelation.getId()+" associated with location "+location_id+ " has been soft deleted");
//                userActionLogDTOS.add(userActionLogDTO);
//                System.out.println("Deleted GlobalInspectionRelation Id :"+globalInspectionRelation.getId());
//            }
//
//            for(GlobalChecklistConditions globalChecklistConditions : location.getGlobal_checklist_conditions()){
//                UserActionLogDTO userActionLogDTO = new UserActionLogDTO();
//                userActionLogDTO.setPrimary_id(globalChecklistConditions.getId());
//                userActionLogDTO.setEmail(username);
//                userActionLogDTO.setStatus("success");
//                userActionLogDTO.setSecondary_id(location_id);
//                userActionLogDTO.setTable_name("global_checklist_conditions");
//                userActionLogDTO.setCreated_timestamp(BigInteger.valueOf(System.currentTimeMillis()));
//                userActionLogDTO.setMessage("Global Inspection Relation "+globalChecklistConditions.getId()+" associated with location "+location_id+ " has been soft deleted");
//                userActionLogDTOS.add(userActionLogDTO);
//                log.info("DELETED global relation id :"+ globalChecklistConditions.getId());
//            }
//
//            Set<String> inspectionRecordIds = location.getRecord_checklist()
//                    .stream()
//                    .map(RecordChecklist::getInspection_record)
//                    .filter(Objects::nonNull)
//                    .filter(record -> !record.getIs_removed())
//                    .map(InspectionRecord::getId)
//                    .collect(Collectors.toSet());
//            finalInspectionrecordIds.addAll(inspectionRecordIds);
//
//            System.out.println("i size"+inspectionRecordIds.size());
//
//            Set<String> recordChecklistIds = location.getRecord_checklist()
//                    .stream()
//                    .map(RecordChecklist::getId)
//                    .collect(Collectors.toSet());
//            finalRecordChecklistIds.addAll(recordChecklistIds);
//
//            System.out.println("r size"+recordChecklistIds.size());
//
//            Set<String> globalInspectionRelationIds = location.getGlobal_inspection_relation()
//                    .stream()
//                    .map(GlobalInspectionRelation::getId)
//                    .collect(Collectors.toSet());
//            finalGlobalInspectionRelationIds.addAll(globalInspectionRelationIds);
//
//            System.out.println("g size"+globalInspectionRelationIds.size());
//
//            Set<String> globalChecklistConditionIds = location.getGlobal_checklist_conditions()
//                    .stream()
//                    .map(GlobalChecklistConditions::getId)
//                    .collect(Collectors.toSet());
//
//            System.out.println("global checklist conditions size"+globalChecklistConditionIds.size());

            recordChecklistService.updateRecordChecklistLocationAndIsRemoved(Collections.emptySet());
            globalInspectionRecordService.updateGlobalInspectionRelationLocationAndIsRemoved(Collections.emptySet());
            globalChecklistConditionsService.updateGlobalChecklistConditionsLocationAndIsRemoved(Collections.emptySet());

            pmsService.updatePmsAttributesByLocationId(location_id);
            locationRepository.deleteById(location_id);
            if(!isSocketCall) {
                syncDeleteLocationToADC(location_id, location.getFloor().getBuilding().getId(), location.getFloor().getId());
            }
            userActionLogService.addUserAction(username, "maps", "DELETE", "A Location with name : " + location.getName() + " and id : " + location_id + " is deleted.", "success", "location", location_id);
        } catch (Exception e) {
            userActionLogService.addUserAction(username, "maps", "DELETE", "Unable to delete Location name : " + location.getName() + " and id : " + location_id, "failed", "location", location_id);
            System.out.println("Unable to delete locations. " + e);
        }

        System.out.println("fi size "+finalInspectionrecordIds.size());
        System.out.println("fr size "+finalRecordChecklistIds.size());
        System.out.println("fg size "+finalGlobalInspectionRelationIds.size());

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            System.out.println("Entered executor service at "+System.currentTimeMillis());
            archivedRecordService.batchUpdateArchivedRecords(userActionLogDTOS);
            recordChecklistService.deleteRecordChecklistInBatch(finalRecordChecklistIds);
            globalInspectionRecordService.deleteGlobalInspectionRelationInBatch(finalGlobalInspectionRelationIds);
            for(String id : finalInspectionrecordIds){
                inspectionRecordService.updateInspectionRecordStatus(username,null,id,false);
                System.out.println("Inspection record status updated while deleting location for id "+id);
            }
            System.out.println("Process completed at "+System.currentTimeMillis());
        });
    }

    public void syncDeleteLocationToADC(String locationId, String buildingId, String floorId) {

        try {
            VdmsDTO vdmsDetails = vdmsRepository.getSyncDetailsForADC();
            List<String> locationIds = Collections.singletonList(locationId);
            Boolean status = apicallService.deleteLocationFromADC(
                    vdmsDetails.getCustomer_org_id(),
                    vdmsDetails.getAdc_configuration_id(),
                    buildingId,
                    floorId,
                    locationIds
            );
            log.info("Location DELETE synced to ADC successfully, Location ID: {}, Status: {}",
                    locationId, status);
        } catch (Exception e) {
            log.error("Exception while syncing deleted location to ADC, Location ID: {}", locationId, e);
        }
    }


    private void deleteLocationByLocationId(String location_id, String username) {
        LocationDTO locationDTO = this.getLocationByLocationId(location_id);
        try {
            // delete from measuring instrument location table
            measuringInstrumentService.deleteMeasuringIntrumentLocationsByLocationId(location_id);
            deviceService.updateDeviceLocation(location_id, username);
            propertyQrcodeService.updatePropertyServiceLocations(location_id);
            recordChecklistService.deleteRecordChecklistByLocationId(location_id);
            globalQrcodeService.deleteGlobalQRCodeByLocationId(location_id);
            pmsService.updatePmsAttributesByLocationId(location_id);
            List<String> imageUrls = recordChecklistService.deleteAllRecordChecklistByLocationId(location_id);
            locationRepository.deleteById(location_id);
            recordChecklistService.deleteAllRecordChecklistImagesByUrls(imageUrls);
            userActionLogService.addUserAction(username, "maps", "DELETE", "A Location with name : " + locationDTO.getName() + " and id : " + location_id + " is deleted.", "success", "location", location_id);

        } catch (Exception e) {
            userActionLogService.addUserAction(username, "maps", "DELETE", "Unable to delete Location name : " + locationDTO.getName() + " and id : " + location_id, "failed", "location", location_id);
        }
    }

    public Set<LocationDTO> getLocationsByVdmsId(String username, String vdms_id) {
        return locationRepository.getLocationByVdmsId(vdms_id);
    }

    public void deleteLocationsByFloorId(String floor_id, String username, Boolean isSocketCall) {
        Set<String> location_ids = locationRepository.getLocationIdsByFloorId(floor_id);
        for (String location_id : location_ids) {
            this.softDeleteLocationByLocationId(location_id, username, isSocketCall);
        }
    }

    public LocationDTO getLocationByLocationId(String location_id) {
        return locationRepository.getLocationByLocationId(location_id);
    }

    public Set<LocationDTO> getLocationsByFloor(String username, String vdms_id, String location_id) {
        return locationRepository.getLocationsByFloor(location_id);
    }


    //to be deleted after backend sync
    public void upsertLocationByFloorIdBackendSync(String floor_id, LocationDTO location) {
        BigInteger timestamp = BigInteger.valueOf(System.currentTimeMillis());
        int rowsAffected = locationRepository.upsertLocationByFloorIdBackendSync(location.getLocation_id(), location.getName(), location.getPosition(), location.getArea(), floor_id, location.getType(), timestamp);
        if(rowsAffected > 0)
            syncLocationToADCServer(List.of(location), floor_id);
    }

    public void updateLocationsDetailsByLocationId(String username, String vdms_id, String floor_id, String location_id, Set<LocationDTO> locations, HttpServletRequest httpServletRequest) {
        for (LocationDTO location : locations) {
            if (location.getLocation_id() != null) {
                try {
                    this.updateLocationDetailsByLocationId(location_id, location);
                    userActionLogService.addUserAction(username, "maps", "UPDATE", "A Location with name: " + location.getName() + " and  id: " + location.getLocation_id() + " is updated", "success", "location", location.getLocation_id());
                    log.info("endpoint: {}, updateLocationsDetailsByLocationId, params: location: {}", httpServletRequest.getRequestURI(), location);

                } catch (Exception e) {
                    userActionLogService.addUserAction(username, "maps", "UPDATE", "Unable to update Location with name: " + location.getName() + " and floor with id: " + floor_id, "failed", "location", location.getLocation_id());
                    log.error("Exception in UPDATE Locations Details By LocationId, endpoint: {} ,  Error message : ", httpServletRequest.getRequestURI(), e);
                }
            }
        }
    }

    public void updateLocationDetailsByLocationId(String location_id, LocationDTO location) {
        BigInteger timestamp = BigInteger.valueOf(System.currentTimeMillis());
        int rowsAffected = locationRepository.updateLocationDetailsByLocationId(location_id, location.getName(), location.getPosition(), location.getArea(), location.getZ_index(), location.getType(), location.getCode(), timestamp);
        location.setLocation_id(location_id);
        if(rowsAffected > 0)
            syncLocationToADCServer(List.of(location), null);
    }

    public String getLocationsCountByFloorId(String username, String vdms_id, String floor_id, String searchkey) {
        return locationRepository.getLocationsCountByFloorId(floor_id, searchkey);
    }

    public void updateArea(String username, String floor_id, String area, Integer z_index, HttpServletRequest httpServletRequest) {
        try {
            locationRepository.updateArea(floor_id, area, z_index);
            userActionLogService.addUserAction(username, "maps", "DELETE", " Area and Z-index for all locations of Floor with id:" + floor_id + " deleted successfully", "success", "floor", floor_id);
            log.info("endpoint: {},delete updateArea,  params: floor_id: {} ", httpServletRequest.getRequestURI(), floor_id);

        } catch (Exception e) {
            userActionLogService.addUserAction(username, "maps", "DELETE", " Unable to delete Area and Z-index for all locations of Floor with id:" + floor_id, "failed", "floor", floor_id);
            log.error("Exception in Deleting updateArea,endpoint: {} ,  Error message : ", httpServletRequest.getRequestURI(), e);
        }
    }

    public LocationDTO getLocationDetailsByLocationId(String username, String vdms_id, String location_id) {
        LocationDTO location = locationRepository.getLocationDetailsByLocationId(location_id);
        try {
            Set<QrCodeDTO> qrCodesTaggedToLocation = qrCodeService.getQrCodesByLocationIds(Collections.singleton(location_id));
            Set<NfcDTO> nfcsTaggedToLocation = nfcService.getNfcsByLocationIds(Collections.singleton(location_id));
            if (!qrCodesTaggedToLocation.isEmpty()) {
                for (QrCodeDTO qrCode : qrCodesTaggedToLocation) {
                    location.setGlobal_qrcode_id(qrCode.getId());
                    break;
                }
            }
            if (!nfcsTaggedToLocation.isEmpty()) {
                for (NfcDTO nfcDTO : nfcsTaggedToLocation) {
                    location.setNfc_id(nfcDTO.getId());
                    break;
                }
            }
        } catch (Exception e) {
            log.error("Exception in get Location Details By Location Id: ", e);
        }
        return location;
    }


    public LocationAlertDTO getLocationAlertDetails(String location_id) {
        return locationRepository.getLocationAlertDetails(location_id);
    }

    public Set<LocationDTO> getAllLocationsPagination(String username, String vdmsid, String group, String searchkey, Integer pageno, Integer pagesize,
                                                      JSONObject filterObject) {
        JSONArray global_checklist_ids = filterObject.getJSONArray("global_checklist_ids");
        JSONArray measuring_instrument_ids = filterObject.getJSONArray("measuring_instrument_ids");
        String building_id = filterObject.getString("building_id");
        String floor_id = filterObject.getString("floor_id");
        String global_inspection_record_id = filterObject.getString("global_inspection_record_id");
        Boolean isTaggedToQrCode = filterObject.getBoolean("isTaggedToQrCode");
        Boolean isTaggedToNfc = filterObject.getBoolean("isTaggedToNfc");
        Boolean isTaggedToBarCode = filterObject.getBoolean("isTaggedToBarCode");
        String inspection_record_id = filterObject.getString("inspection_record_id");
        JSONArray locationIdsTaggedToQrCode = new JSONArray();
        JSONArray locationIdsTaggedToClientQrCode = new JSONArray();
        JSONArray locationIdsTaggedToNfc = new JSONArray();
        JSONArray locationIdsTaggedToClientNfc = new JSONArray();
        JSONArray locationIdsTaggedToClientBarCode = new JSONArray();
        JSONArray types = filterObject.getJSONArray("types") != null ? filterObject.getJSONArray("types") : new JSONArray();
        if (types.isEmpty()) {
            types.add("all");
        }
        Set<LocationDTO> locations;
        if (isTaggedToQrCode != null) {
            locationIdsTaggedToQrCode = qrCodeService.getLocationIdsTaggedToQrCode(vdmsid);
            locationIdsTaggedToClientQrCode = clientQrCodeService.getLocationIdsTaggedToClientQrCode(vdmsid);
            locationIdsTaggedToQrCode.addAll(locationIdsTaggedToClientQrCode);
            if (locationIdsTaggedToQrCode.isEmpty()) {
                locationIdsTaggedToQrCode.add("");
            }
        }
        if (isTaggedToNfc != null) {
            locationIdsTaggedToNfc = nfcService.getLocationIdsTaggedToNfc(vdmsid);
            locationIdsTaggedToClientNfc = clientNfcService.getLocationIdsTaggedToClientNfc(vdmsid);
            locationIdsTaggedToNfc.addAll(locationIdsTaggedToClientNfc);
            if (locationIdsTaggedToNfc.isEmpty()) {
                locationIdsTaggedToNfc.add("");
            }

        }
        if (isTaggedToBarCode != null) {
            locationIdsTaggedToClientBarCode = clientBarCodeService.getLocationIdsTaggedToClientBarCode(vdmsid);
            if (locationIdsTaggedToClientBarCode.isEmpty()) {
                locationIdsTaggedToClientBarCode.add("");
            }

        }
        Integer offset = pagesize * (pageno - 1);
        String sanitized_search_key = searchkey.replaceAll("[ -.!\t_+#~`@$%^&*()=;:<>?,/{}|\\\\]", "").toLowerCase();

        switch (group) {

            case "all": {
                // All Locations
                locations = this.getAllLocationPagination(sanitized_search_key, pagesize, offset, floor_id, isTaggedToQrCode, locationIdsTaggedToQrCode, isTaggedToNfc, locationIdsTaggedToNfc, types, building_id);
                break;
            }
            case "measuring_instrument": {
                // All Locations
                locations = this.getAllMeasuringInstrumentLocationsPagination(sanitized_search_key, pagesize, offset, floor_id, measuring_instrument_ids, isTaggedToQrCode, locationIdsTaggedToQrCode, isTaggedToNfc, locationIdsTaggedToNfc, types, building_id);
                break;
            }
            case "tagged": {
                // Procedures and Reactive services
                locations = this.getAllChecklistLocationsPagination(sanitized_search_key, pagesize, offset, global_checklist_ids, floor_id, isTaggedToQrCode, locationIdsTaggedToQrCode, isTaggedToNfc, locationIdsTaggedToNfc, types, building_id);
                break;
            }
            case "inspection": {
                //Scheduled inspections and Services
                locations = this.getAllInspectionLocationsPagination(sanitized_search_key, pagesize, offset, global_checklist_ids, floor_id, global_inspection_record_id, isTaggedToQrCode, locationIdsTaggedToQrCode, isTaggedToNfc, locationIdsTaggedToNfc, types, building_id);
                break;
            }
            case "record_checklist": {
                //Scheduled inspections and Services
                locations = this.getAllRecordChecklistLocationsPagination(sanitized_search_key, pagesize, offset, global_checklist_ids, floor_id, inspection_record_id, isTaggedToQrCode, locationIdsTaggedToQrCode, isTaggedToNfc, locationIdsTaggedToNfc, types, building_id);
                break;
            }
            case "qrcode": {
                //Qrcode
                locations = this.getAllQrcodeLocationsPagination(sanitized_search_key, pagesize, offset, floor_id, isTaggedToQrCode, locationIdsTaggedToQrCode, isTaggedToNfc, locationIdsTaggedToNfc, types, building_id);
                break;
            }
            case "reactive_service": {
                //reactive service
                locations = this.getAllReactiveServiceLocationsPagination(sanitized_search_key, pagesize, offset, floor_id, isTaggedToQrCode, locationIdsTaggedToQrCode, isTaggedToNfc, locationIdsTaggedToNfc, types, building_id);
                break;
            }
            default:
                return Collections.emptySet();
        }
        return this.getLocationsWithQrCodeDetails(vdmsid, locations);

    }


    private Set<LocationDTO> getAllRecordChecklistLocationsPagination(String searchkey, Integer pagesize, Integer offset, JSONArray global_checklist_ids, String floor_id, String inspection_record_id, Boolean isTaggedToQrCode, JSONArray locationIdsTaggedToQrCode, Boolean isTaggedToNfc,
                                                                      JSONArray locationIdsTaggedToNfc, JSONArray types, String building_id) {
        return locationRepository.getAllRecordChecklistLocationsPagination(searchkey, pagesize, offset, global_checklist_ids, floor_id, inspection_record_id,
                isTaggedToQrCode, locationIdsTaggedToQrCode, isTaggedToNfc, locationIdsTaggedToNfc, types, building_id);
    }

    private Set<LocationDTO> getAllChecklistLocationsPagination(String searchkey, Integer pagesize, Integer offset, JSONArray global_checklist_ids, String floor_id, Boolean isTaggedToQrCode, JSONArray locationIdsTaggedToQrCode, Boolean isTaggedToNfc,
                                                                JSONArray locationIdsTaggedToNfc, JSONArray types, String building_id) {
        return locationRepository.getAllChecklistLocationsPagination(searchkey, pagesize, offset, global_checklist_ids, floor_id, isTaggedToQrCode, locationIdsTaggedToQrCode, isTaggedToNfc, locationIdsTaggedToNfc, types, building_id);
    }

    private Set<LocationDTO> getAllInspectionLocationsPagination(String searchkey, Integer pagesize, Integer offset, JSONArray global_checklist_ids, String floor_id, String global_inspection_record_id, Boolean isTaggedToQrCode, JSONArray locationIdsTaggedToQrCode, Boolean isTaggedToNfc,
                                                                 JSONArray locationIdsTaggedToNfc, JSONArray types, String building_id) {
        return locationRepository.getAllInspectionLocationsPagination(searchkey, pagesize, offset, global_checklist_ids, floor_id, global_inspection_record_id,
                isTaggedToQrCode, locationIdsTaggedToQrCode, isTaggedToNfc, locationIdsTaggedToNfc, types, building_id);
    }

    private Set<LocationDTO> getAllQrcodeLocationsPagination(String searchkey, Integer pagesize, Integer offset, String floor_id, Boolean isTaggedToQrCode, JSONArray locationIdsTaggedToQrCode, Boolean isTaggedToNfc, JSONArray locationIdsTaggedToNfc, JSONArray types, String building_id) {
        return locationRepository.getAllQrcodeLocationsPagination(searchkey, pagesize, offset, floor_id, isTaggedToQrCode, locationIdsTaggedToQrCode,
                isTaggedToNfc, locationIdsTaggedToNfc, types, building_id);
    }

    public Set<LocationDTO> getLocationsWithQrCodeDetails(String vdms_id, Set<LocationDTO> locations) {
        try {
            Set<String> locationIds = new HashSet<>();
            for (LocationDTO location : locations) {
                locationIds.add(location.getLocation_id());
            }
            Set<QrCodeDTO> qrCodesTaggedToLocations = qrCodeService.getQrCodesByLocationIds(locationIds);
            for (LocationDTO location : locations) {
                for (QrCodeDTO qrCodeDTO : qrCodesTaggedToLocations) {
                    if (qrCodeDTO.getLocationId().equals(location.getLocation_id())) {
                        location.setGlobal_qrcode_id(qrCodeDTO.getId());
                        break;
                    }
                }
            }
            Set<NfcDTO> nfcsTaggedToLocations = nfcService.getNfcsByLocationIds(locationIds);
            for (LocationDTO location : locations) {
                for (NfcDTO nfcDTO : nfcsTaggedToLocations) {
                    if (nfcDTO.getLocationId().equals(location.getLocation_id())) {
                        location.setNfc_id(nfcDTO.getId());
                        break;
                    }
                }
            }
            Set<ClientBarCodeDTO> barCodesTaggedToLocations = clientBarCodeService.getBarCodesByLocationIds(locationIds);
            for (LocationDTO location : locations) {
                for (ClientBarCodeDTO clientBarCodeDTO : barCodesTaggedToLocations) {
                    if (clientBarCodeDTO.getLocationId().equals(location.getLocation_id())) {
                        location.setBarcode_id(clientBarCodeDTO.getId());
                        break;
                    }
                }
            }
            return locations;
        } catch (Exception e) {
            log.error("Exception in get Locations With QrCode Details : ", e);
        }
        return null;
    }

    public Set<LocationDTO> getLocationsByFloorId(String floor_id, String vdms_id) {
        Set<LocationDTO> locations = locationRepository.getLocationsByFloorId(floor_id);

        Set<LocationDTO> locationsWithQrCodeDetails = this.getLocationsWithQrCodeDetails(vdms_id, locations);
        if (locationsWithQrCodeDetails != null) {
            return locationsWithQrCodeDetails;
        }
        return locations;
    }

    public Set<LocationDTO> getLocationsByFloorByPagination(String username, String vdms_id, String floor_id, Integer pageno, Integer pagesize,
                                                            String searchKey, JSONObject filterObject, String field, String field_id) {
        Integer offset = pagesize * (pageno - 1);
        String qrCodeCondition = filterObject.getString("qrcode");
        String nfcConditon = filterObject.getString("nfc");
        String barCodeConditon = filterObject.getString("barcode");
        String recordChecklistCondition = filterObject.getString("record_checklist");
        String roomStatusCondition = null;
        String cleanStatus = filterObject.getString("clean_status");
        String occupancyStatus = filterObject.getString("occupancy_status");
        String status = filterObject.getString("status");
        JSONArray locationIdsTaggedToQrCode = new JSONArray();
        JSONArray locationIdsTaggedToClientNfc = new JSONArray();
        JSONArray locationIdsTaggedToClientQrCode = new JSONArray();
        JSONArray locationIdsTaggedToNfc = new JSONArray();
        JSONArray locationIdsTaggedToClientBarCode = new JSONArray();
        Set<String> locationIdsWithRoomStatus = new HashSet<>();
        JSONArray types = filterObject.getJSONArray("types") != null ? filterObject.getJSONArray("types") : new JSONArray();
        if (types.isEmpty()) {
            types.add("all");
        }
        if (qrCodeCondition != null) {
            if (qrCodeCondition.equals("present") || qrCodeCondition.equals("not_present")) {
                locationIdsTaggedToQrCode = qrCodeService.getLocationIdsTaggedToQrCode(vdms_id);
                locationIdsTaggedToClientQrCode = clientQrCodeService.getLocationIdsTaggedToClientQrCode(vdms_id);
                locationIdsTaggedToQrCode.addAll(locationIdsTaggedToClientQrCode);
                if (locationIdsTaggedToQrCode.isEmpty()) {
                    locationIdsTaggedToQrCode.add("");
                }
            }
        }
        if (nfcConditon != null) {
            if (nfcConditon.equals("present") || nfcConditon.equals("not_present")) {
                locationIdsTaggedToNfc = nfcService.getLocationIdsTaggedToNfc(vdms_id);
                locationIdsTaggedToClientNfc = clientNfcService.getLocationIdsTaggedToClientNfc(vdms_id);
                locationIdsTaggedToNfc.addAll(locationIdsTaggedToClientNfc);
                if (locationIdsTaggedToNfc.isEmpty()) {
                    locationIdsTaggedToNfc.add("");
                }
            }
        }
        if (barCodeConditon != null) {
            if (barCodeConditon.equals("present") || barCodeConditon.equals("not_present")) {
                locationIdsTaggedToClientBarCode = clientBarCodeService.getLocationIdsTaggedToClientBarCode(vdms_id);
                if (locationIdsTaggedToClientBarCode.isEmpty()) {
                    locationIdsTaggedToClientBarCode.add("");
                }
            }
        }
//        if (cleanStatus != null && occupancyStatus != null) {
//            roomStatusCondition = "present";
//            locationIdsWithRoomStatus = pmsService.getLocationIdsByRoomStatus(cleanStatus, occupancyStatus);
//            if (locationIdsWithRoomStatus.isEmpty()) {
//                locationIdsWithRoomStatus.add("");
//            }
//        }
        Set<LocationDTO> locations = locationRepository.getLocationsByFloorByPagination(floor_id, searchKey, qrCodeCondition, locationIdsTaggedToQrCode, nfcConditon,
                locationIdsTaggedToNfc, recordChecklistCondition, status, pagesize, offset, types,barCodeConditon,locationIdsTaggedToClientBarCode);
        this.updateLocationDTORoomStatus(locations, field, field_id);
        Set<LocationDTO> locationsWithQrCodeDetails = this.getLocationsWithQrCodeDetails(vdms_id, locations);
        log.info("locations" + locations);
        if (locationsWithQrCodeDetails != null) {
            return locationsWithQrCodeDetails;
        }
        return locations;
    }


    public Integer getQrCodeLocationCountByVdmsId(String vdms_id) {
        JSONArray locationIdsTaggedToQrCode = apicallService.getQrCodeIdsByVdmsIdAndType(vdms_id, "location");
        log.info("locationIdsqrCode" + locationIdsTaggedToQrCode);
        Set<String> locations = new HashSet<>();

        if (locationIdsTaggedToQrCode != null) {
            for (int i = 0; i < locationIdsTaggedToQrCode.size(); i++) {
                locations.add(locationIdsTaggedToQrCode.getString(i));
            }
            locations = locationRepository.getLocationIds(locations);
            return locations.size();
        }

        return 0;
    }


    public Integer getNfcLocationCountByVdmsId(String vdms_id) {
        JSONArray locationIdsTaggedToNfc = apicallService.getNfcIdsByVdmsAndType(vdms_id, "location");
        Set<String> locations = new HashSet<>();
        if (locationIdsTaggedToNfc != null) {
            for (int i = 0; i < locationIdsTaggedToNfc.size(); i++) {
                locations.add(locationIdsTaggedToNfc.getString(i));
            }
            locations = locationRepository.getLocationIds(locations);
            return locations.size();
        }

        return 0;
    }


    private void updateLocationDTORoomStatus(Set<LocationDTO> locations, String field, String field_id) {
        Set<String> locationIds = locations.stream()
                .map(LocationDTO::getLocation_id)
                .collect(Collectors.toSet());
        List<String> locationIdsList = new ArrayList<>();
        locationIdsList.addAll(locationIds);
        Set<RecordChecklistDTO> allRecordChecklist = recordChecklistService.getAllRecordChecklistByBuildings(locationIdsList,Collections.singletonList("all"), Collections.singletonList("all"));

        for (LocationDTO locationDTO : locations) {
            if (field != null) {
                Map<String, Long> defaultCounts = new HashMap<>();
                defaultCounts.put("tagged", 0L);
                defaultCounts.put("inspection", 0L);
                defaultCounts.put("scheduled_services", 0L);
                defaultCounts.put("reactive_services", 0L);
                Map<String, Long> computedCounts = allRecordChecklist.stream()
                        .filter(recordChecklistDTO ->recordChecklistDTO.getLocation_id().equals(locationDTO.getLocation_id()))
                        .collect(Collectors.groupingBy(
                                record -> {
                                    String type = record.getRecord_type();
                                    boolean hasInspectionId = record.getInspection_record_id() != null;
                                    if ((field.equals("all_task") || field.equals("tagged")) && "checklist".equals(type) && !hasInspectionId)
                                        return "tagged";
                                    if ((field.equals("all_task") || field.equals("inspection")) && "checklist".equals(type) && hasInspectionId)
                                        return "inspection";
                                    if ((field.equals("all_task") || field.equals("scheduled_services")) && "service".equals(type) && hasInspectionId)
                                        return "scheduled_services";
                                    if ((field.equals("all_task") || field.equals("reactive_services")) && "service".equals(type) && !hasInspectionId)
                                        return "reactive_services";
                                    return "unknown"; // Exclude unmatched records
                                },
                                Collectors.counting()
                        ));
                defaultCounts.putAll(computedCounts);
                JSONObject counts = new JSONObject();
                System.out.println("defaultCounts count for tagged :" + defaultCounts.get("tagged"));
                if (field.equals("all_task") || field.equals("tagged")) {
                    JSONObject tagged_count = new JSONObject();
                    tagged_count.put("all_count",defaultCounts.get("tagged"));
                    counts.put("tagged_procedure",tagged_count );
                }
                if (field.equals("all_task") || field.equals("reactive_services")) {
                    JSONObject reactive_service_count = new JSONObject();
                    reactive_service_count.put("all_count",defaultCounts.get("reactive_services"));
                    counts.put("service_requests", reactive_service_count);
                }
                if (field.equals("all_task") || field.equals("scheduled_services")) {
                    JSONObject scheduled_service_count = new JSONObject();
                    scheduled_service_count.put("all_count",defaultCounts.get("scheduled_services"));
                    counts.put("service_checklist", scheduled_service_count);
                }
                if (field.equals("all_task") || field.equals("inspection")) {
                    JSONObject inspection_count = new JSONObject();
                    inspection_count.put("all_count",defaultCounts.get("inspection"));
                    counts.put("inspection_checklist", inspection_count);
                }
                locationDTO.setCounts(counts);
            }
        }
        Set<PmsAttributesDTO> pmsAttributesDTOS = pmsService.getPmsAttributesByLocationIds(locationIds);
        for (PmsAttributesDTO pmsAttributesDTO : pmsAttributesDTOS) {
            for (LocationDTO locationDTO : locations) {
//                if (locationDTO.getLocation_id().equals(pmsAttributesDTO.getLocation_id())) {
//                    locationDTO.setOccupancy_status(pmsAttributesDTO.getOccupancy_status());
//                    locationDTO.setClean_status(pmsAttributesDTO.getClean_status());
//                }
            }
        }
    }

    public int searchSortFilterLocationsCount(String username, String vdms_id, String searchKey, JSONObject filterObject) {
        String qrCodeCondition = filterObject.getString("qrcode");
        String barCodeCondition = filterObject.getString("barcode");
        String nfcConditon = filterObject.getString("nfc");
        String recordChecklistCondition = filterObject.getString("record_checklist");
        String roomStatusCondition = null;
        String cleanStatus = filterObject.getString("clean_status");
        String occupancyStatus = filterObject.getString("occupancy_status");
        String status = filterObject.getString("status");
        JSONArray locationIdsTaggedToQrCode = new JSONArray();
        JSONArray locationIdsTaggedToClientQrCode = new JSONArray();
        JSONArray locationIdsTaggedToClientNfc = new JSONArray();
        JSONArray locationIdsTaggedToNfc = new JSONArray();
        JSONArray locationIdsTaggedToBarCode = new JSONArray();
        Set<String> locationIdsWithRoomStatus = new HashSet<>();
        JSONArray types = filterObject.getJSONArray("types") != null ? filterObject.getJSONArray("types") : new JSONArray();
        JSONArray building_ids = filterObject.getJSONArray("building_ids");
        JSONArray floor_ids = filterObject.getJSONArray("floor_ids");
        if (types.isEmpty()) {
            types.add("all");
        }
        if (qrCodeCondition != null) {
            if (qrCodeCondition.equals("present") || qrCodeCondition.equals("not_present")) {
                locationIdsTaggedToQrCode = qrCodeService.getLocationIdsTaggedToQrCode(vdms_id);
                locationIdsTaggedToClientQrCode = clientQrCodeService.getLocationIdsTaggedToClientQrCode(vdms_id);
                locationIdsTaggedToQrCode.addAll(locationIdsTaggedToClientQrCode);
                if (locationIdsTaggedToQrCode.isEmpty()) {
                    locationIdsTaggedToQrCode.add("");
                }
            }
        }

        if (barCodeCondition != null) {
            if (barCodeCondition.equals("present") || barCodeCondition.equals("not_present")) {
                locationIdsTaggedToBarCode = clientBarCodeService.getLocationIdsTaggedToClientBarCode(vdms_id);
                if (locationIdsTaggedToBarCode.isEmpty()) {
                    locationIdsTaggedToBarCode.add("");
                }
            }
        }
        if (nfcConditon != null) {
            if (nfcConditon.equals("present") || nfcConditon.equals("not_present")) {
                locationIdsTaggedToNfc = nfcService.getLocationIdsTaggedToNfc(vdms_id);
                locationIdsTaggedToClientNfc = clientNfcService.getLocationIdsTaggedToClientNfc(vdms_id);
                locationIdsTaggedToNfc.addAll(locationIdsTaggedToClientNfc);
                if (locationIdsTaggedToNfc.isEmpty()) {
                    locationIdsTaggedToNfc.add("");
                }
            }
        }
//        if (cleanStatus != null && occupancyStatus != null) {
//            roomStatusCondition = "present";
//            locationIdsWithRoomStatus = pmsService.getLocationIdsByRoomStatus(cleanStatus, occupancyStatus);
//            if (locationIdsWithRoomStatus.isEmpty()) {
//                locationIdsWithRoomStatus.add("");
//            }
//        }
        return locationRepository.searchSortFilterLocationsCount(floor_ids, searchKey, qrCodeCondition, locationIdsTaggedToQrCode, nfcConditon,
                locationIdsTaggedToNfc, recordChecklistCondition, status, types, building_ids,barCodeCondition,locationIdsTaggedToBarCode);
    }

    public Set<LocationDTO> getAllLocationsByGroup(String username, String vdmsid, JSONObject filter_object, String global_checklist_id, String global_inspection_record_id, String group) {

        String building_id = filter_object.getString("building_id");
        String floor_id = filter_object.getString("floor_id");
        Boolean isTaggedToQrCode = filter_object.getBoolean("is_tagged_to_qrcode");
        Boolean isTaggedToNfc = filter_object.getBoolean("is_tagged_to_nfc");
        String searchkey = filter_object.getString("search_key");
        JSONArray locationIdsTaggedToQrCode = new JSONArray();
        JSONArray locationIdsTaggedToClientQrCode = new JSONArray();
        JSONArray locationIdsTaggedToNfc = new JSONArray();
        JSONArray locationIdsTaggedToClientNfc = new JSONArray();
        JSONArray types = filter_object.getJSONArray("types") != null ? filter_object.getJSONArray("types") : new JSONArray();
        Set<LocationDTO> locations;
        if (types.isEmpty()) {
            types.add("all");
        }
        if (isTaggedToQrCode != null) {
            locationIdsTaggedToQrCode = qrCodeService.getLocationIdsTaggedToQrCode(vdmsid);
            locationIdsTaggedToClientQrCode = clientQrCodeService.getLocationIdsTaggedToClientQrCode(vdmsid);
            locationIdsTaggedToQrCode.addAll(locationIdsTaggedToClientQrCode);
            if (locationIdsTaggedToQrCode.isEmpty()) {
                locationIdsTaggedToQrCode.add("");
            }
        }
        if (isTaggedToNfc != null) {
            locationIdsTaggedToNfc = nfcService.getLocationIdsTaggedToNfc(vdmsid);
            locationIdsTaggedToClientNfc = clientNfcService.getLocationIdsTaggedToClientNfc(vdmsid);
            locationIdsTaggedToNfc.addAll(locationIdsTaggedToClientNfc);
            if (locationIdsTaggedToNfc.isEmpty()) {
                locationIdsTaggedToNfc.add("");
            }
        }

        switch (group) {
            case "tagged": {
                // Procedures and Reactive services
                locations = this.getAllChecklistLocations(searchkey, global_checklist_id, floor_id, isTaggedToQrCode, locationIdsTaggedToQrCode, isTaggedToNfc, locationIdsTaggedToNfc, types, building_id);
                break;
            }
            case "inspection": {
                //Scheduled inspections and Services
                locations = this.getAllInspectionLocations(searchkey, global_checklist_id, floor_id, global_inspection_record_id, isTaggedToQrCode, locationIdsTaggedToQrCode, isTaggedToNfc, locationIdsTaggedToNfc, types, building_id);
                break;
            }
            case "qrcode": {
                //Qrcode
                locations = this.getAllQrcodeLocations(searchkey, floor_id, isTaggedToQrCode, locationIdsTaggedToQrCode, isTaggedToNfc, locationIdsTaggedToNfc, types, building_id);
                break;
            }
            default:
                return Collections.emptySet();
        }
        return locations;

    }


    private Set<LocationDTO> getAllChecklistLocations(String searchkey, String global_checklist_id, String floor_id, Boolean isTaggedToQrCode, JSONArray locationIdsTaggedToQrCode, Boolean isTaggedToNfc, JSONArray locationIdsTaggedToNfc, JSONArray types, String building_id) {
        return locationRepository.getAllChecklistLocations(searchkey, global_checklist_id, floor_id, isTaggedToQrCode, locationIdsTaggedToQrCode, isTaggedToNfc, locationIdsTaggedToNfc, types, building_id);
    }

    private Set<LocationDTO> getAllInspectionLocations(String searchkey, String global_checklist_ids, String floor_id, String global_inspection_record_id, Boolean isTaggedToQrCode, JSONArray locationIdsTaggedToQrCode, Boolean isTaggedToNfc, JSONArray locationIdsTaggedToNfc, JSONArray types, String building_id) {
        return locationRepository.getAllInspectionLocations(searchkey, global_checklist_ids, floor_id, global_inspection_record_id, isTaggedToQrCode, locationIdsTaggedToQrCode, isTaggedToNfc, locationIdsTaggedToNfc, types, building_id);
    }

    private Set<LocationDTO> getAllQrcodeLocations(String searchkey, String floor_id, Boolean isTaggedToQrCode, JSONArray locationIdsTaggedToQrCode, Boolean isTaggedToNfc, JSONArray locationIdsTaggedToNfc, JSONArray types, String building_id) {
        return locationRepository.getAllQrcodeLocations(searchkey, floor_id, isTaggedToQrCode, locationIdsTaggedToQrCode, isTaggedToNfc, locationIdsTaggedToNfc, types, building_id);
    }


    private Set<LocationDTO> getAllLocationPagination(String searchkey, Integer pagesize, Integer offset, String floor_id, Boolean isTaggedToQrCode, JSONArray locationIdsTaggedToQrCode, Boolean isTaggedToNfc, JSONArray locationIdsTaggedToNfc, JSONArray types, String building_id) {
        return locationRepository.getAllLocationPagination(searchkey, pagesize, offset, floor_id, isTaggedToQrCode, locationIdsTaggedToQrCode, isTaggedToNfc, locationIdsTaggedToNfc, types, building_id);
    }

    private Set<LocationDTO> getAllMeasuringInstrumentLocationsPagination(String searchkey, Integer pagesize, Integer offset, String floor_id, JSONArray measuring_instrument_ids, Boolean isTaggedToQrCode, JSONArray locationIdsTaggedToQrCode, Boolean isTaggedToNfc, JSONArray locationIdsTaggedToNfc, JSONArray types, String building_id) {
        return locationRepository.getAllMeasuringInstrumentLocationsPagination(searchkey, pagesize, offset, floor_id, measuring_instrument_ids, isTaggedToQrCode, locationIdsTaggedToQrCode, isTaggedToNfc, locationIdsTaggedToNfc, types, building_id);
    }

    public Set<LocationDTO> getTaggedMeasuringInstrumentLocations(String username, String vdmsid, String measuring_instrument_id) {

        return locationRepository.getLocationDetailsByMeasuringInstrumentId(measuring_instrument_id);

    }

    public List<String> getLocationIdsByFilter(String searchKey, Boolean isTaggedToQrCode, Boolean isTaggedToNfc, List<String> buildingIds, List<String> floorIds, List<String> types) {
        List<String> locationIdsTaggedToQrCode = new ArrayList<>();
        List<String> locationIdsTaggedToClientQrCode = new ArrayList<>();
        List<String> locationIdsTaggedToNfc = new ArrayList<>();
        List<String> locationIdsTaggedToClientNfc = new ArrayList<>();
        String vdmsid = authenticationUtils.getVdms_id();

        if (isTaggedToQrCode != null) {
            locationIdsTaggedToQrCode = (List) qrCodeService.getLocationIdsTaggedToQrCode(vdmsid);
            locationIdsTaggedToClientQrCode = (List) clientQrCodeService.getLocationIdsTaggedToClientQrCode(vdmsid);
            locationIdsTaggedToQrCode.addAll(locationIdsTaggedToClientQrCode);
            if (locationIdsTaggedToQrCode.isEmpty()) {
                locationIdsTaggedToQrCode.add("");
            }
        }
        if (isTaggedToNfc != null) {
            locationIdsTaggedToNfc = (List) nfcService.getLocationIdsTaggedToNfc(vdmsid);
            locationIdsTaggedToClientNfc = (List) clientNfcService.getLocationIdsTaggedToClientNfc(vdmsid);
            locationIdsTaggedToNfc.addAll(locationIdsTaggedToClientNfc);
            if (locationIdsTaggedToNfc.isEmpty()) {
                locationIdsTaggedToNfc.add("");
            }

        }
        return locationRepository.getLocationIdsByFilter(searchKey, isTaggedToQrCode, locationIdsTaggedToQrCode, isTaggedToNfc,
                locationIdsTaggedToNfc, buildingIds, floorIds, types);
    }


    public List<LocationAlertDTO> getLocationsByFilter(String searchKey, Boolean isTaggedToQrCode, Boolean isTaggedToNfc,
                                                       List<String> buildingIds, List<String> floorIds, List<String> locationIds, List<String> types) {
        List<String> locationIdsTaggedToQrCode = new ArrayList<>();
        List<String> locationIdsTaggedToClientQrCode = new ArrayList<>();
        List<String> locationIdsTaggedToClientNfc = new ArrayList<>();
        List<String> locationIdsTaggedToNfc = new ArrayList<>();

        String vdmsid = authenticationUtils.getVdms_id();

        if (isTaggedToQrCode != null) {
            locationIdsTaggedToQrCode = (List) qrCodeService.getLocationIdsTaggedToQrCode(vdmsid);
            locationIdsTaggedToClientQrCode = (List) clientQrCodeService.getLocationIdsTaggedToClientQrCode(vdmsid);
            locationIdsTaggedToQrCode.addAll(locationIdsTaggedToClientQrCode);
            if (locationIdsTaggedToQrCode.isEmpty()) {
                locationIdsTaggedToQrCode.add("");
            }
        }
        if (isTaggedToNfc != null) {
            locationIdsTaggedToNfc = (List) nfcService.getLocationIdsTaggedToNfc(vdmsid);
            locationIdsTaggedToClientNfc = (List) clientNfcService.getLocationIdsTaggedToClientNfc(vdmsid);
            locationIdsTaggedToNfc.addAll(locationIdsTaggedToClientNfc);
            if (locationIdsTaggedToNfc.isEmpty()) {
                locationIdsTaggedToNfc.add("");
            }


        }
        return locationRepository.getLocationsByFilter(searchKey, isTaggedToQrCode, locationIdsTaggedToQrCode, isTaggedToNfc,
                locationIdsTaggedToNfc, buildingIds, floorIds, locationIds, types);
    }


    public List<String> getUniqueLocationTypes(String username, String vdms_id) {
        return locationRepository.getUniqueLocationTypes();
    }

    public int getLocationId(String location_id) {
        return locationRepository.getLocationId(location_id);
    }

    public Set<LocationDTO> getAllLocationsByIds(Set<String> locationIds) {
        return locationRepository.getAllLocationsByIds(locationIds);
    }

    public void multiUpdateLocations(String username, String vdms_id, String floor_id, TagDeviceOrLocationDTO tagDeviceOrLocationDTO, HttpServletRequest httpServletRequest) {

        try {
            int select_all_status = tagDeviceOrLocationDTO.getSelect_all_status();
            JSONObject filter_object = tagDeviceOrLocationDTO.getFilter_object();
            JSONObject general_object = tagDeviceOrLocationDTO.getGeneral_object();
            Set<String> location_ids;
            List<LocationDTO> locationDTOS = new ArrayList<>();
            if (select_all_status == 1) {
                location_ids = getAllLocationIdsByFilter(vdms_id, floor_id, filter_object);
            } else {
                location_ids = utils.getJSONArrayFromJSONStringForSet(general_object.getJSONArray("location_ids").toString(), String.class);
            }
            BigInteger timestamp = BigInteger.valueOf(System.currentTimeMillis());
            int rowsAffected = locationRepository.multiUpdateLocations(location_ids, general_object.getString("name"), general_object.getString("position"), general_object.getString("area"), general_object.getString("z_index"), general_object.getString("type"), general_object.getString("code"),timestamp);
            for(String location_id : location_ids){
                LocationDTO locationDTO = new LocationDTO();
                locationDTO.setLocation_id(location_id);
                locationDTO.setName(general_object.getString("name"));
                locationDTO.setCode(general_object.getString("code"));
                locationDTOS.add(locationDTO);
            }
            if(rowsAffected > 0)
                syncLocationToADCServer(locationDTOS, floor_id);
            for (String location_id : location_ids) {
                try {
                    userActionLogService.addUserAction(username, "maps", "UPDATE", "A Location with name: " + general_object.getString("name") + " and  id: " + location_id + " is updated", "success", "location", location_id);
                    log.info("endpoint: {},update multiUpdateLocations,  params: location_id: {} ", httpServletRequest.getRequestURI(), location_id);

                } catch (Exception e) {
                    userActionLogService.addUserAction(username, "maps", "UPDATE", "Unable to update Location with name: " + general_object.getString("name") + " and  id: " + location_id, "failed", "location", location_id);
                    log.error("Exception in multi Update Locations, endpoint: {} ,  Error message : ", httpServletRequest.getRequestURI(), e);
                }
            }
        } catch (Exception e) {
            log.error("Exception in multi Update Locations : ", e);
        }

    }

    private Set<String> getAllLocationIdsByFilter(String vdms_id, String floor_id, JSONObject filter_object) {
        String qrCodeCondition = filter_object.getString("qrcode");
        String nfcConditon = filter_object.getString("nfc");
        String recordChecklistCondition = filter_object.getString("record_checklist");
        String roomStatusCondition = null;
        String cleanStatus = filter_object.getString("clean_status");
        String occupancyStatus = filter_object.getString("occupancy_status");
        String search_key = filter_object.getString("search_key");
        JSONArray locationIdsTaggedToQrCode = new JSONArray();
        JSONArray locationIdsTaggedToClientQrCode = new JSONArray();
        JSONArray locationIdsTaggedToClientNfc = new JSONArray();
        JSONArray locationIdsTaggedToNfc = new JSONArray();
        Set<String> locationIdsWithRoomStatus = new HashSet<>();
        JSONArray types = filter_object.getJSONArray("types") != null ? filter_object.getJSONArray("types") : new JSONArray();
        if (types.isEmpty()) {
            types.add("all");
        }

        if (qrCodeCondition != null) {
            if (qrCodeCondition.equals("present") || qrCodeCondition.equals("not_present")) {
                locationIdsTaggedToQrCode = qrCodeService.getLocationIdsTaggedToQrCode(vdms_id);
                locationIdsTaggedToClientQrCode = clientQrCodeService.getLocationIdsTaggedToClientQrCode(vdms_id);
                locationIdsTaggedToQrCode.addAll(locationIdsTaggedToClientQrCode);
                if (locationIdsTaggedToQrCode.isEmpty()) {
                    locationIdsTaggedToQrCode.add("");
                }
            }
        }
        if (nfcConditon != null) {
            if (nfcConditon.equals("present") || nfcConditon.equals("not_present")) {
                locationIdsTaggedToNfc = nfcService.getLocationIdsTaggedToNfc(vdms_id);
                locationIdsTaggedToClientNfc = clientNfcService.getLocationIdsTaggedToClientNfc(vdms_id);
                locationIdsTaggedToNfc.addAll(locationIdsTaggedToClientNfc);
                if (locationIdsTaggedToNfc.isEmpty()) {
                    locationIdsTaggedToNfc.add("");
                }
            }
        }
        if (cleanStatus != null && occupancyStatus != null) {
            roomStatusCondition = "present";
            locationIdsWithRoomStatus = pmsService.getLocationIdsByRoomStatus(cleanStatus, occupancyStatus);
            if (locationIdsWithRoomStatus.isEmpty()) {
                locationIdsWithRoomStatus.add("");
            }
        }
        return locationRepository.getAllLocationIdsByFilter(floor_id, search_key, qrCodeCondition, locationIdsTaggedToQrCode, nfcConditon,
                locationIdsTaggedToNfc, recordChecklistCondition, roomStatusCondition, locationIdsWithRoomStatus, types);
    }

    public Integer getLocationStatusCountTs(String status) {
        return locationRepository.getLocationStatusCountTs(status);
    }

    public List<LocationAlertDTO> getLocationsByStatus(String status, Integer pageno, Integer pagesize) {
        Integer offset = pagesize * (pageno - 1);
        return locationRepository.getLocationsByStatus(status, offset, pagesize);
    }

    public Integer getLocationsByStatusCountTs(String status) {
        return locationRepository.getLocationsByStatusCountTs(status);
    }

    public Set<LocationDTO> upsertlocationsdetails(String username, String vdms_id, String floor_id, Set<LocationDTO> locations, HttpServletRequest httpServletRequest) {
        String action;
        for (LocationDTO location : locations) {
            action = "UPDATE";
            if (location.getLocation_id() == null) {
                location.setLocation_id(Generators.timeBasedGenerator().generate().toString());
                action = "ADD";
            }
            this.upsertlocationdetails(floor_id, location, username, action, httpServletRequest);
        }
        return locations;
    }

    public void upsertlocationdetails(String floor_id, LocationDTO location, String username, String action, HttpServletRequest httpServletRequest) {
        try {
            BigInteger timestamp = BigInteger.valueOf(System.currentTimeMillis());
            int rowsAffected = locationRepository.upsertlocationdetails(location.getLocation_id(), location.getName(), location.getPosition(), location.getArea(), floor_id, location.getStatus(), location.getType(), location.getCode(),timestamp);
            if(rowsAffected > 0)
                syncLocationToADCServer(List.of(location), floor_id);
            if (action.equals("ADD")) {
                userActionLogService.addUserAction(username, "maps", action, "A Location with name: " + location.getName() + " and id: " + location.getLocation_id() + " is added", "success", "location", location.getLocation_id());
                log.info("endpoint: {}, upsertlocationdetails, description: A new location is added, params: location: {} ", httpServletRequest.getRequestURI(), location);

            } else {
                userActionLogService.addUserAction(username, "maps", action, "A Location with name: " + location.getName() + " and id: " + floor_id + " is updated", "success", "location", location.getLocation_id());
                log.info("endpoint: {},upsert location details, description: A location is updated, params: location: {}", httpServletRequest.getRequestURI(), location);
            }
        } catch (Exception e) {
            if (action.equals("ADD")) {
                userActionLogService.addUserAction(username, "maps", action, "Unable to add Location with name: " + location.getName() + " and  id: " + location.getLocation_id(), "failed", "location", location.getLocation_id());
                log.error("Exception in add upsert location details, endpoint: {} ,  Error message : ", httpServletRequest.getRequestURI(), e);

            } else {
                userActionLogService.addUserAction(username, "maps", action, "Unable to update Location with name: " + location.getName() + " and floor with id: " + floor_id, "failed", "location", location.getLocation_id());
                log.error("Exception in update upsert location details, endpoint: {} ,  Error message : ", httpServletRequest.getRequestURI(), e);
            }
        }
    }

    public Set<LocationDTO> getLocationsByFilter(String username, String vdms_id, String searchKey,
                                                 JSONObject filterObject, String field, String field_id) { //log.info("getLocationsByFloorByPagination, Params: username: {}, vdms_id: {}, pagemo: {}, pagesize: {}, searchKey: {}, filterObject: {}, field: {}, field_id: {}", username, vdms_id, pageno, pagesize, searchKey, filterObject, field, field_id);
        String qrCodeCondition = filterObject.getString("qrcode");
        String barCodeCondition = filterObject.getString("barcode");
        String nfcConditon = filterObject.getString("nfc");
        String recordChecklistCondition = filterObject.getString("record_checklist");
        String roomStatusCondition = null;
        String cleanStatus = filterObject.getString("clean_status");
        String occupancyStatus = filterObject.getString("occupancy_status");
        String status = filterObject.getString("status");
        JSONArray locationIdsTaggedToQrCode = new JSONArray();
        JSONArray locationIdsTaggedToBarCode = new JSONArray();
        JSONArray locationIdsTaggedToClientQrCode = new JSONArray();
        JSONArray locationIdsTaggedToClientNfc = new JSONArray();
        JSONArray locationIdsTaggedToNfc = new JSONArray();
        Set<String> locationIdsWithRoomStatus = new HashSet<>();
        JSONArray types = filterObject.getJSONArray("types") != null ? filterObject.getJSONArray("types") : new JSONArray();
        JSONArray building_ids = filterObject.getJSONArray("building_ids");
        JSONArray floor_ids = filterObject.getJSONArray("floor_ids");
        if (types.isEmpty()) {
            types.add("all");
        }
        if (qrCodeCondition != null) {
            if (qrCodeCondition.equals("present") || qrCodeCondition.equals("not_present")) {
                locationIdsTaggedToQrCode = qrCodeService.getLocationIdsTaggedToQrCode(vdms_id);
                locationIdsTaggedToClientQrCode = clientQrCodeService.getLocationIdsTaggedToClientQrCode(vdms_id);
                locationIdsTaggedToQrCode.addAll(locationIdsTaggedToClientQrCode);
                if (locationIdsTaggedToQrCode.isEmpty()) {
                    locationIdsTaggedToQrCode.add("");
                }
            }
        }

        if (barCodeCondition != null) {
            if (barCodeCondition.equals("present") || barCodeCondition.equals("not_present")) {
                locationIdsTaggedToBarCode = clientBarCodeService.getLocationIdsTaggedToClientBarCode(vdms_id);
                if (locationIdsTaggedToBarCode.isEmpty()) {
                    locationIdsTaggedToBarCode.add("");
                }
            }
        }
        if (nfcConditon != null) {
            if (nfcConditon.equals("present") || nfcConditon.equals("not_present")) {
                locationIdsTaggedToNfc = nfcService.getLocationIdsTaggedToNfc(vdms_id);
                locationIdsTaggedToClientNfc = clientNfcService.getLocationIdsTaggedToClientNfc(vdms_id);
                locationIdsTaggedToNfc.addAll(locationIdsTaggedToClientNfc);
                if (locationIdsTaggedToNfc.isEmpty()) {
                    locationIdsTaggedToNfc.add("");
                }
            }
        }
//        if (cleanStatus != null && occupancyStatus != null) {
//            roomStatusCondition = "present";
//            locationIdsWithRoomStatus = pmsService.getLocationIdsByRoomStatus(cleanStatus, occupancyStatus);
//            if (locationIdsWithRoomStatus.isEmpty()) {
//                locationIdsWithRoomStatus.add("");
//            }
//        }
        System.out.println("============================================");
        Set<LocationDTO> locations = locationRepository.getAllLocationsByFilter(floor_ids, searchKey, qrCodeCondition, locationIdsTaggedToQrCode, nfcConditon,
                locationIdsTaggedToNfc, recordChecklistCondition, status, types, building_ids,barCodeCondition,locationIdsTaggedToBarCode);
        this.updateLocationDTORoomStatus(locations, field, field_id);
        Set<LocationDTO> locationsWithQrCodeDetails = this.getLocationsWithQrCodeDetails(vdms_id, locations);
        log.info("locations" + locations);
        if (locationsWithQrCodeDetails != null) {
            return locationsWithQrCodeDetails;
        }
        return locations;
    }

    /******************************** Integration **************************************************/

    public List<LocationIntegrationDTO> getIntegrationByLocationId(String locationId) {
        log.warn("[STUB] getIntegrationByLocationId locationId={}", locationId);
        return java.util.Collections.emptyList();
    }

    public Set<LocationDTO> getAllLocationsByFilterByPagination(String username, String vdms_id, Integer pageno, Integer pagesize,
                                                                String searchKey, JSONObject filterObject, String field, String field_id) {
        //log.info("getLocationsByFloorByPagination, Params: username: {}, vdms_id: {}, pagemo: {}, pagesize: {}, searchKey: {}, filterObject: {}, field: {}, field_id: {}", username, vdms_id, pageno, pagesize, searchKey, filterObject, field, field_id);
        Integer offset = pagesize * (pageno - 1);
        String qrCodeCondition = filterObject.getString("qrcode");
        String barCodeCondition = filterObject.getString("barcode");
        String nfcConditon = filterObject.getString("nfc");
        String recordChecklistCondition = filterObject.getString("record_checklist");
        String roomStatusCondition = null;
        String cleanStatus = filterObject.getString("clean_status");
        String occupancyStatus = filterObject.getString("occupancy_status");
        String status = filterObject.getString("status");
        JSONArray locationIdsTaggedToQrCode = new JSONArray();
        JSONArray locationIdsTaggedToBarCode = new JSONArray();
        JSONArray locationIdsTaggedToClientNfc = new JSONArray();
        JSONArray locationIdsTaggedToClientQrCode = new JSONArray();
        JSONArray locationIdsTaggedToNfc = new JSONArray();
        Set<String> locationIdsWithRoomStatus = new HashSet<>();
        JSONArray types = filterObject.getJSONArray("types") != null ? filterObject.getJSONArray("types") : new JSONArray();
        JSONArray building_ids = filterObject.getJSONArray("building_ids");
        JSONArray floor_ids = filterObject.getJSONArray("floor_ids");
        if (types.isEmpty()) {
            types.add("all");
        }
        if (qrCodeCondition != null) {
            if (qrCodeCondition.equals("present") || qrCodeCondition.equals("not_present")) {
                locationIdsTaggedToQrCode = qrCodeService.getLocationIdsTaggedToQrCode(vdms_id);
                locationIdsTaggedToClientQrCode = clientQrCodeService.getLocationIdsTaggedToClientQrCode(vdms_id);
                locationIdsTaggedToQrCode.addAll(locationIdsTaggedToClientQrCode);
                if (locationIdsTaggedToQrCode.isEmpty()) {
                    locationIdsTaggedToQrCode.add("");
                }
            }
        }
        if (barCodeCondition != null) {
            if (barCodeCondition.equals("present") || barCodeCondition.equals("not_present")) {
                locationIdsTaggedToBarCode = clientBarCodeService.getLocationIdsTaggedToClientBarCode(vdms_id);
                if (locationIdsTaggedToBarCode.isEmpty()) {
                    locationIdsTaggedToBarCode.add("");
                }
            }
        }
        if (nfcConditon != null) {
            if (nfcConditon.equals("present") || nfcConditon.equals("not_present")) {
                locationIdsTaggedToNfc = nfcService.getLocationIdsTaggedToNfc(vdms_id);
                locationIdsTaggedToClientNfc = clientNfcService.getLocationIdsTaggedToClientNfc(vdms_id);
                locationIdsTaggedToNfc.addAll(locationIdsTaggedToClientNfc);
                if (locationIdsTaggedToNfc.isEmpty()) {
                    locationIdsTaggedToNfc.add("");
                }
            }
        }
//        if (cleanStatus != null && occupancyStatus != null) {
//            roomStatusCondition = "present";
//            locationIdsWithRoomStatus = pmsService.getLocationIdsByRoomStatus(cleanStatus, occupancyStatus);
//            if (locationIdsWithRoomStatus.isEmpty()) {
//                locationIdsWithRoomStatus.add("");
//            }
//        }
        System.out.println("============================================");
        Set<LocationDTO> locations = locationRepository.getAllLocationsByFilterByPagination(floor_ids, searchKey, qrCodeCondition, locationIdsTaggedToQrCode, nfcConditon,
                locationIdsTaggedToNfc, recordChecklistCondition, status, pagesize, offset, types, building_ids,barCodeCondition,locationIdsTaggedToBarCode);
        this.updateLocationDTORoomStatus(locations, field, field_id);
        Set<LocationDTO> locationsWithQrCodeDetails = this.getLocationsWithQrCodeDetails(vdms_id, locations);
        log.info("locations" + locations);
        if (locationsWithQrCodeDetails != null) {
            return locationsWithQrCodeDetails;
        }
        return locations;
    }

    public void updateAllRecordChecklistStatusInBatchForLocation(List<LocationDTO> updatedLocationStatus) {
        log.info("updateAllRecordChecklistStatusInBatchForDevice");
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement preparedStatementUpdate = connection.prepareStatement(locationQueryRepository.getQueryForUpdateLocationRecordChecklistStatus());
            int batchCounter = 0;
            int maxBatchLimit = 200;
            for (LocationDTO locationDTO : updatedLocationStatus) {
                try {
                    preparedStatementUpdate.setString(1, locationDTO.getRecord_checklist_status());
                    preparedStatementUpdate.setInt(2, locationDTO.getRecord_checklist_count());
                    preparedStatementUpdate.setString(3, locationDTO.getLocation_id());
                    preparedStatementUpdate.addBatch();
                    batchCounter++;
                    if (batchCounter == maxBatchLimit) {
                        preparedStatementUpdate.executeBatch();
                        preparedStatementUpdate.clearBatch();
                        batchCounter = 0;
                    }

                } catch (Exception e) {
                    log.error("Exception in batch update of location record checklist status :", e);
                }
            }
            if (batchCounter > 0) {
                preparedStatementUpdate.executeBatch();
                log.error("Excecuted batch update of: {} location record checklist status ", batchCounter);
            }
            preparedStatementUpdate.close();
        } catch (Exception e) {
            log.error("Exception in batch update of location record checklist status :", e);

        }

    }

    public String getLocationName(String location_id){
        return locationRepository.getLocationName(location_id);
    }
    /******************************** Integration **************************************************/

    private Set<LocationDTO> getAllReactiveServiceLocationsPagination(String searchkey, Integer pagesize, Integer offset, String floor_id, Boolean isTaggedToQrCode, JSONArray locationIdsTaggedToQrCode, Boolean isTaggedToNfc,
                                                                JSONArray locationIdsTaggedToNfc, JSONArray types, String building_id) {
        return locationRepository.getAllReactiveServiceLocationsPagination(searchkey, pagesize, offset, floor_id, isTaggedToQrCode, locationIdsTaggedToQrCode, isTaggedToNfc, locationIdsTaggedToNfc, types, building_id);
    }

}
