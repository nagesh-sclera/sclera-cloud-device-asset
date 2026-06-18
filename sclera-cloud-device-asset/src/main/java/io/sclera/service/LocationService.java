package io.sclera.service;
import io.sclera.client.APICallClient;
import io.sclera.interfaces.LocationServiceInterface;

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
import io.sclera.Repository.VdmsRepository;
import io.sclera.client.PmsClient;
import io.sclera.client.PropertyQrcodeClient;
import io.sclera.dto.*;
import io.sclera.dto.touchscreen.settings.VdmsDTO;
import io.sclera.integration.dto.LocationIntegrationDTO;
import io.sclera.models.*;
import io.sclera.queryrepository.LocationQueryRepository;
import io.sclera.utils.AuthenticationUtils;
import io.sclera.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.fasterxml.uuid.Generators;

import io.sclera.Repository.LocationRepository;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import javax.sql.DataSource;

/**
 * Manages locations within floors, including add/update/soft-delete operations, ADC server
 * synchronization, record-checklist status and counts, paginated and filtered location queries, and
 * enrichment of locations with their tagged QR code, NFC and barcode details.
 */
@Service
public class LocationService implements LocationServiceInterface {
    private static final Logger log = LoggerFactory.getLogger(LocationService.class);

    @Autowired
    LocationRepository locationRepository;

    @Autowired
    io.sclera.client.RecordChecklistClient recordChecklistService;

    @Autowired
    PropertyQrcodeClient propertyQrcodeService;

    @Autowired
    io.sclera.client.GlobalQrcodeClient globalQrcodeService;

    @Autowired
    DeviceService deviceService;

    @Autowired
    APICallClient apicallService;

    @Autowired
    PmsClient pmsService;

    @Autowired
    MeasuringInstrumentService measuringInstrumentService;

    @Autowired
    AuthenticationUtils authenticationUtils;

    @Autowired
    UserActionLogService userActionLogService;

    @Autowired
    Utils utils;

    @Autowired
    io.sclera.client.QrCodeClient qrCodeService;

    @Autowired
    io.sclera.client.ClientQrCodeClient clientQrCodeService;

    @Autowired
    io.sclera.client.NfcClient nfcService;

    @Autowired
    io.sclera.client.ClientNfcClient clientNfcService;

    @Autowired
    DataSource dataSource;

    @Autowired
    LocationQueryRepository locationQueryRepository;

    @Autowired
    ClientBarCodeService clientBarCodeService;

    @Autowired
    io.sclera.client.ArchivedRecordClient archivedRecordService;

    @Autowired
    io.sclera.client.GlobalInspectionRecordClient globalInspectionRecordService;

    @Autowired
    io.sclera.client.InspectionRecordClient inspectionRecordService;

    @Autowired
    io.sclera.client.GlobalChecklistConditionsClient globalChecklistConditionsService;

    @Autowired
    VdmsRepository vdmsRepository;

    @Autowired
    io.sclera.Repository.FloorRepository floorRepository;


    /**
     * Inserts or updates the given locations under a floor, choosing add or update per location
     * based on whether its id already exists for that floor.
     */
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


    /**
     * Adds a new location to a floor, generating an id when absent and syncing it to the ADC server.
     * Plain INSERT semantics — always creates a new entity with id, name, position, area, type,
     * updated_timestamp, and floor FK. No conflict/update path.
     *
     * @return the location id of the added location
     */
    @Transactional
    public String addLocationByFloorId(LocationDTO locationdto, String floor_id) {
        if (locationdto.getLocation_id() == null) {
            String id = Generators.timeBasedGenerator().generate().toString();
            locationdto.setLocation_id(id);
        }
        BigInteger timestamp = BigInteger.valueOf(System.currentTimeMillis());
        Location location = new Location();
        location.setId(locationdto.getLocation_id());
        location.setName(locationdto.getName());
        location.setPosition(locationdto.getPosition());
        location.setArea(locationdto.getArea());
        location.setType(locationdto.getType());
        location.setUpdated_timestamp(timestamp);
        location.setFloor(floorRepository.getReferenceById(floor_id));
        locationRepository.save(location);
        syncLocationToADCServer(List.of(locationdto), floor_id);
        return locationdto.getLocation_id();
    }

    /**
     * Pushes the given locations to the ADC server using the stored ADC sync configuration.
     */
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

    /**
     * Updates a location's details by its id and syncs the change to the ADC server.
     */
    public void updateLocationByLocationId(LocationDTO locationdto) {
        BigInteger timestamp = BigInteger.valueOf(System.currentTimeMillis());
        int rowsAffected = locationRepository.updateLocationByLocationId(locationdto.getName(), locationdto.getPosition(), locationdto.getLocation_id(), locationdto.getArea(), locationdto.getType(),timestamp);
        if(rowsAffected > 0)
            syncLocationToADCServer(List.of(locationdto), null);
    }


    /**
     * Returns true when the given location id is present in the supplied set of ids.
     */
    public boolean compareIds(Set<String> location_ids, String location_id) {
        return location_ids.stream()
                .anyMatch(l -> l.equals(location_id));
    }


    //delete locations not tagged to device
    /**
     * Deletes every location that is not tagged to a device.
     */
    public void deleteUnlinkedLocations() {
        Set<String> unlikedLocationIds = locationRepository.getUnlinkedLocationIds();

        for (String locationId : unlikedLocationIds) {
            locationRepository.deleteById(locationId);
        }
    }


    /**
     * Returns true when a location with the given id exists.
     */
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

    /**
     * Recomputes and stores a location's record-checklist status (todo or completed) for the given
     * record type.
     */
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

    /**
     * Recomputes and stores a location's record-checklist count for the given record type.
     */
    public void updateLocationRecordChecklistCount(String location_id, String record_type) {
        Integer record_checklist_count = recordChecklistService.getChecklistStatusCountLocationId(location_id, "inspection", record_type);
        locationRepository.updateLocationRecordChecklistCount(location_id, record_checklist_count);

    }

    /**
     * Refreshes both the record-checklist status and count for a location.
     */
    public void updateLocationRecordChecklistStatusById(String location_id, String record_type) {
        if (location_id != null) {
            this.updateLocationRecordChecklistStatus(location_id, record_type);
            this.updateLocationRecordChecklistCount(location_id, record_type);
        }
    }

    /********************************************* new location changes *******************************/


    /**
     * Inserts or updates the supplied locations for a floor, assigning ids and ADD/UPDATE actions as
     * needed and logging each user action.
     *
     * @return the locations with their ids populated
     */
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

    /**
     * Upserts a single location for a floor, syncing to the ADC server and logging a success or
     * failure user action for the given ADD or UPDATE action.
     *
     * INSERT path (new row): sets id, name, position, area, floor FK, type, code, updated_timestamp
     *   (status left null — matches original ON CONFLICT … INSERT col list).
     * CONFLICT path (existing row): sets name, status, type, code, updated_timestamp ONLY
     *   (position, area, floor are NOT changed — matches original DO UPDATE SET list).
     */
    @Transactional
    public void upsertLocationByFloorId(String floor_id, LocationDTO location, String username, String action, HttpServletRequest httpServletRequest) {
        try {
            BigInteger timestamp = BigInteger.valueOf(System.currentTimeMillis());
            boolean isNew;
            Location entity = locationRepository.findById(location.getLocation_id()).orElse(null);
            if (entity == null) {
                // INSERT path
                isNew = true;
                entity = new Location();
                entity.setId(location.getLocation_id());
                entity.setName(location.getName());
                entity.setPosition(location.getPosition());
                entity.setArea(location.getArea());
                entity.setFloor(floorRepository.getReferenceById(floor_id));
                entity.setType(location.getType());
                entity.setCode(location.getCode());
                entity.setUpdated_timestamp(timestamp);
                // status left null per original INSERT column list
            } else {
                // CONFLICT path: update name, status, type, code, updated_timestamp ONLY
                isNew = false;
                entity.setName(location.getName());
                entity.setStatus(location.getStatus());
                entity.setType(location.getType());
                entity.setCode(location.getCode());
                entity.setUpdated_timestamp(timestamp);
            }
            locationRepository.save(entity);
            syncLocationToADCServer(List.of(location), floor_id);
            if (isNew) {
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

    /**
     * Soft-deletes each location in the given set of ids.
     */
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
            log.debug("{}", "Unable to delete locations. " + e);
        }

        log.debug("{}", "fi size "+finalInspectionrecordIds.size());
        log.debug("{}", "fr size "+finalRecordChecklistIds.size());
        log.debug("{}", "fg size "+finalGlobalInspectionRelationIds.size());

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            log.debug("{}", "Entered executor service at "+System.currentTimeMillis());
            archivedRecordService.batchUpdateArchivedRecords(userActionLogDTOS);
            recordChecklistService.deleteRecordChecklistInBatch(finalRecordChecklistIds);
            globalInspectionRecordService.deleteGlobalInspectionRelationInBatch(finalGlobalInspectionRelationIds);
            for(String id : finalInspectionrecordIds){
                inspectionRecordService.updateInspectionRecordStatus(username,null,id,false);
                log.debug("{}", "Inspection record status updated while deleting location for id "+id);
            }
            log.debug("{}", "Process completed at "+System.currentTimeMillis());
        });
    }

    /**
     * Notifies the ADC server that the given location has been deleted, using the stored ADC sync
     * configuration. Errors are logged and swallowed.
     */
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

    /**
     * Soft-deletes all locations belonging to the given floor.
     */
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
    /**
     * Backend-sync upsert of a single location for a floor, syncing to the ADC server when a row is
     * affected.
     *
     * INSERT path (new row): sets id, name, position, area, floor FK, type, updated_timestamp.
     * CONFLICT path (existing row): sets name, position, area, floor FK, type, updated_timestamp
     *   (status and code are NOT changed — matches original DO UPDATE SET list which has no status/code).
     */
    @Transactional
    public void upsertLocationByFloorIdBackendSync(String floor_id, LocationDTO location) {
        BigInteger timestamp = BigInteger.valueOf(System.currentTimeMillis());
        Location entity = locationRepository.findById(location.getLocation_id()).orElse(null);
        if (entity == null) {
            // INSERT path
            entity = new Location();
            entity.setId(location.getLocation_id());
            entity.setName(location.getName());
            entity.setPosition(location.getPosition());
            entity.setArea(location.getArea());
            entity.setFloor(floorRepository.getReferenceById(floor_id));
            entity.setType(location.getType());
            entity.setUpdated_timestamp(timestamp);
        } else {
            // CONFLICT path: update name, position, area, floor FK, type, updated_timestamp
            entity.setName(location.getName());
            entity.setPosition(location.getPosition());
            entity.setArea(location.getArea());
            entity.setFloor(floorRepository.getReferenceById(floor_id));
            entity.setType(location.getType());
            entity.setUpdated_timestamp(timestamp);
        }
        locationRepository.save(entity);
        syncLocationToADCServer(List.of(location), floor_id);
    }

    /**
     * Updates the details of each supplied location that has an id, logging a success or failure
     * user action per location.
     */
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

    /**
     * Updates a single location's details (name, position, area, z-index, type, code) and syncs the
     * change to the ADC server.
     */
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

    /**
     * Sets the area and z-index for all locations of the given floor, logging the user action.
     */
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

    /**
     * Returns a location's details with its tagged global QR code id and NFC id populated, if any.
     */
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

    /**
     * Returns a paginated set of locations for the requested group (all, measuring instrument,
     * tagged, inspection, record checklist, qrcode or reactive service), applying the search key and
     * QR/NFC/barcode tagging filters, and enriching the results with QR code details.
     */
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

    /**
     * Enriches each location with the id of its tagged QR code, NFC and barcode, returning the
     * enriched set or null on error.
     */
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

    /**
     * Returns the locations of a floor enriched with their QR code, NFC and barcode details.
     */
    public Set<LocationDTO> getLocationsByFloorId(String floor_id, String vdms_id) {
        Set<LocationDTO> locations = locationRepository.getLocationsByFloorId(floor_id);

        Set<LocationDTO> locationsWithQrCodeDetails = this.getLocationsWithQrCodeDetails(vdms_id, locations);
        if (locationsWithQrCodeDetails != null) {
            return locationsWithQrCodeDetails;
        }
        return locations;
    }

    /**
     * Returns a paginated set of locations for a floor filtered by QR/NFC/barcode presence, record
     * checklist and status, enriched with room-status counts and QR code details.
     */
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


    /**
     * Returns the number of locations tagged to a QR code for the given VDMS.
     */
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


    /**
     * Returns the number of locations tagged to an NFC tag for the given VDMS.
     */
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
                log.debug("{}", "defaultCounts count for tagged :" + defaultCounts.get("tagged"));
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

    /**
     * Returns the count of locations matching the search key and QR/NFC/barcode, record checklist,
     * status, type, building and floor filters.
     */
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

    /**
     * Returns the (non-paginated) locations for the requested group (tagged, inspection or qrcode),
     * applying the search key and QR/NFC tagging filters.
     */
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

    /**
     * Returns the locations tagged to the given measuring instrument.
     */
    public Set<LocationDTO> getTaggedMeasuringInstrumentLocations(String username, String vdmsid, String measuring_instrument_id) {

        return locationRepository.getLocationDetailsByMeasuringInstrumentId(measuring_instrument_id);

    }

    /**
     * Returns the ids of locations matching the search key, QR/NFC tagging filters and building,
     * floor and type filters.
     */
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


    /**
     * Returns location alert details matching the search key, QR/NFC tagging filters and building,
     * floor, location and type filters.
     */
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

    /**
     * Updates common fields of multiple locations at once, resolving the target ids either from the
     * filter (when select-all is set) or from the supplied id list, syncing to the ADC server and
     * logging a user action per location.
     */
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

    /**
     * Returns a page of location alert details having the given status.
     */
    public List<LocationAlertDTO> getLocationsByStatus(String status, Integer pageno, Integer pagesize) {
        return locationRepository.getLocationsByStatus(status, PageRequest.of(pageno - 1, pagesize));
    }

    public Integer getLocationsByStatusCountTs(String status) {
        return locationRepository.getLocationsByStatusCountTs(status);
    }

    /**
     * Inserts or updates the supplied location details for a floor, assigning ids and ADD/UPDATE
     * actions as needed.
     *
     * @return the locations with their ids populated
     */
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

    /**
     * Upserts a single location's details for a floor, syncing to the ADC server and logging a
     * success or failure user action for the given ADD or UPDATE action.
     *
     * INSERT path (new row): sets id, name, position, area, floor FK, type, code, updated_timestamp
     *   (status left null — matches original INSERT column list).
     * CONFLICT path (existing row): sets name, status, type, code, area, position, updated_timestamp
     *   (floor FK is NOT changed — matches original DO UPDATE SET list which omits floor_id).
     */
    @Transactional
    public void upsertlocationdetails(String floor_id, LocationDTO location, String username, String action, HttpServletRequest httpServletRequest) {
        try {
            BigInteger timestamp = BigInteger.valueOf(System.currentTimeMillis());
            boolean isNew;
            Location entity = locationRepository.findById(location.getLocation_id()).orElse(null);
            if (entity == null) {
                // INSERT path
                isNew = true;
                entity = new Location();
                entity.setId(location.getLocation_id());
                entity.setName(location.getName());
                entity.setPosition(location.getPosition());
                entity.setArea(location.getArea());
                entity.setFloor(floorRepository.getReferenceById(floor_id));
                entity.setType(location.getType());
                entity.setCode(location.getCode());
                entity.setUpdated_timestamp(timestamp);
                // status left null per original INSERT column list
            } else {
                // CONFLICT path: update name, status, type, code, area, position, updated_timestamp ONLY
                isNew = false;
                entity.setName(location.getName());
                entity.setStatus(location.getStatus());
                entity.setType(location.getType());
                entity.setCode(location.getCode());
                entity.setArea(location.getArea());
                entity.setPosition(location.getPosition());
                entity.setUpdated_timestamp(timestamp);
            }
            locationRepository.save(entity);
            syncLocationToADCServer(List.of(location), floor_id);
            if (isNew) {
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

    /**
     * Returns the (non-paginated) locations matching the search key and QR/NFC/barcode, record
     * checklist, status, type, building and floor filters, enriched with room-status counts and QR
     * code details.
     */
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
        log.debug("{}", "============================================");
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

    /**
     * Returns the integrations for a location. Currently returns an empty list pending delegation to
     * the integration service.
     */
    public List<LocationIntegrationDTO> getIntegrationByLocationId(String locationId) {
        // TODO: delegate to integration service via Dapr when available
        return java.util.Collections.emptyList();
    }

    /**
     * Returns a paginated set of locations matching the search key and QR/NFC/barcode, record
     * checklist, status, type, building and floor filters, enriched with room-status counts and QR
     * code details.
     */
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
        log.debug("{}", "============================================");
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

    /**
     * Batch-updates the record-checklist status and count for the given locations using a single
     * prepared statement flushed in batches of 200.
     */
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
