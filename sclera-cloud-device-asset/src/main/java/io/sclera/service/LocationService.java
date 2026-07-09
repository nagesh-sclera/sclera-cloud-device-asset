package io.sclera.service;

import com.alibaba.fastjson.JSONObject;
import io.sclera.dto.*;
import io.sclera.integration.dto.LocationIntegrationDTO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Set;

/** Service contract for {@link io.sclera.service.LocationService}. */
public interface LocationService {

    void upsertLocationByFloorId(Set<LocationDTO> locations, String floor_id);

    String addLocationByFloorId(LocationDTO locationdto, String floor_id);

    void syncLocationToADCServer(List<LocationDTO> locationdto, String floor_id);

    void updateLocationByLocationId(LocationDTO locationdto);

    boolean compareIds(Set<String> location_ids, String location_id);

    void deleteUnlinkedLocations();

    Boolean checkLocationById(String location_id);

    String getPositionByLocationId(String location_id);

    LocationDTO getLocationDetails(String location_id);

    void updateLocationRecordChecklistStatus(String location_id, String record_type);

    void updateLocationRecordChecklistCount(String location_id, String record_type);

    void updateLocationRecordChecklistStatusById(String location_id, String record_type);

    Set<LocationDTO> upsertLocationsByFloorId(String username, String vdms_id, String floor_id, Set<LocationDTO> locations, HttpServletRequest httpServletRequest);

    void upsertLocationByFloorId(String floor_id, LocationDTO location, String username, String action, HttpServletRequest httpServletRequest);

    void deleteLocationsByIds(String email, String vdms_id, Set<String> location_ids, Boolean isSocketCall);

    void syncDeleteLocationToADC(String locationId, String buildingId, String floorId);

    Set<LocationDTO> getLocationsByVdmsId(String username, String vdms_id);

    void deleteLocationsByFloorId(String floor_id, String username, Boolean isSocketCall);

    LocationDTO getLocationByLocationId(String location_id);

    Set<LocationDTO> getLocationsByFloor(String username, String vdms_id, String location_id);

    void upsertLocationByFloorIdBackendSync(String floor_id, LocationDTO location);

    void updateLocationsDetailsByLocationId(String username, String vdms_id, String floor_id, String location_id, Set<LocationDTO> locations, HttpServletRequest httpServletRequest);

    void updateLocationDetailsByLocationId(String location_id, LocationDTO location);

    String getLocationsCountByFloorId(String username, String vdms_id, String floor_id, String searchkey);

    void updateArea(String username, String floor_id, String area, Integer z_index, HttpServletRequest httpServletRequest);

    LocationDTO getLocationDetailsByLocationId(String username, String vdms_id, String location_id);

    LocationAlertDTO getLocationAlertDetails(String location_id);

    Set<LocationDTO> getAllLocationsPagination(String username, String vdmsid, String group, String searchkey, Integer pageno, Integer pagesize,
                                              JSONObject filterObject);

    Set<LocationDTO> getLocationsWithQrCodeDetails(String vdms_id, Set<LocationDTO> locations);

    Set<LocationDTO> getLocationsByFloorId(String floor_id, String vdms_id);

    Set<LocationDTO> getLocationsByFloorByPagination(String username, String vdms_id, String floor_id, Integer pageno, Integer pagesize,
                                                    String searchKey, JSONObject filterObject, String field, String field_id);

    Integer getQrCodeLocationCountByVdmsId(String vdms_id);

    Integer getNfcLocationCountByVdmsId(String vdms_id);

    int searchSortFilterLocationsCount(String username, String vdms_id, String searchKey, JSONObject filterObject);

    Set<LocationDTO> getAllLocationsByGroup(String username, String vdmsid, JSONObject filter_object, String global_checklist_id, String global_inspection_record_id, String group);

    Set<LocationDTO> getTaggedMeasuringInstrumentLocations(String username, String vdmsid, String measuring_instrument_id);

    List<String> getLocationIdsByFilter(String searchKey, Boolean isTaggedToQrCode, Boolean isTaggedToNfc, List<String> buildingIds, List<String> floorIds, List<String> types);

    List<LocationAlertDTO> getLocationsByFilter(String searchKey, Boolean isTaggedToQrCode, Boolean isTaggedToNfc,
                                                List<String> buildingIds, List<String> floorIds, List<String> locationIds, List<String> types);

    List<String> getUniqueLocationTypes(String username, String vdms_id);

    int getLocationId(String location_id);

    Set<LocationDTO> getAllLocationsByIds(Set<String> locationIds);

    void multiUpdateLocations(String username, String vdms_id, String floor_id, TagDeviceOrLocationDTO tagDeviceOrLocationDTO, HttpServletRequest httpServletRequest);

    Integer getLocationStatusCountTs(String status);

    List<LocationAlertDTO> getLocationsByStatus(String status, Integer pageno, Integer pagesize);

    Integer getLocationsByStatusCountTs(String status);

    Set<LocationDTO> upsertlocationsdetails(String username, String vdms_id, String floor_id, Set<LocationDTO> locations, HttpServletRequest httpServletRequest);

    void upsertlocationdetails(String floor_id, LocationDTO location, String username, String action, HttpServletRequest httpServletRequest);

    Set<LocationDTO> getLocationsByFilter(String username, String vdms_id, String searchKey,
                                          JSONObject filterObject, String field, String field_id);

    List<LocationIntegrationDTO> getIntegrationByLocationId(String locationId);

    Set<LocationDTO> getAllLocationsByFilterByPagination(String username, String vdms_id, Integer pageno, Integer pagesize,
                                                         String searchKey, JSONObject filterObject, String field, String field_id);

    void updateAllRecordChecklistStatusInBatchForLocation(List<LocationDTO> updatedLocationStatus);

    String getLocationName(String location_id);
}
