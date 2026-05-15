package io.sclera.Repository;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.transaction.Transactional;

import com.alibaba.fastjson.JSONArray;
import io.sclera.dto.LocationAlertDTO;
import io.sclera.dto.LocationDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.sclera.dto.touchscreen.DeviceMonitorSpaceDTO;
import io.sclera.models.Location;


@Repository
public interface LocationRepository extends JpaRepository<Location, String> {


    @Query(nativeQuery = true)
    ArrayList<DeviceMonitorSpaceDTO> listAllSpaceStatus(String spacetype, String buildingname, String floorname);

    @Query(value = "SELECT id FROM location WHERE floor_id = ?1", nativeQuery = true)
    Set<String> getLocationIdsByFloorId(String floor_id);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO location(id,name,position,floor_id, area, type, updated_timestamp ) VALUE(?1,?2,?3,?4,?5,?6,?7)", nativeQuery = true)
    int addLocationByFloorId(String location_id, String name, String position, String floor_id, String area, String type, BigInteger updated_timestamp);


    @Modifying
    @Transactional
    @Query(value = "UPDATE location SET name = ?1 ,position = ?2, area = ?4, type = ?5, updated_timestamp = ?6  WHERE id = ?3", nativeQuery = true)
    int updateLocationByLocationId(String name, String position, String location_id, String area, String type, BigInteger updated_timestamp);

    @Modifying
    @Transactional
    @Query(value = "ALTER TABLE location MODIFY position varchar(128)", nativeQuery = true)
    void modifytable();

    //get locations not tagged to a device
    @Query(value = "SELECT id FROM location l WHERE id NOT IN (SELECT d.location_id FROM device d WHERE l.id = d.location_id)", nativeQuery = true)
    Set<String> getUnlinkedLocationIds();

    @Transactional
    @Query(value = "SELECT EXISTS(SELECT * FROM location where id = ?1)", nativeQuery = true)
    Integer checkLocationById(String location_id);

    @Query(value = "SELECT position FROM location where id = ?1", nativeQuery = true)
    String getPositionByLocationId(String location_id);


    @Query(nativeQuery = true)
    LocationDTO getLocationDetails(String location_id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE location SET record_checklist_status = ?2 WHERE id = ?1", nativeQuery = true)
    void updateLocationRecordChecklistStatus(String location_id, String checklist_status);

    @Modifying
    @Transactional
    @Query(value = "UPDATE location SET record_checklist_count = ?2 WHERE id = ?1", nativeQuery = true)
    void updateLocationRecordChecklistCount(String location_id, Integer record_checklist_count);


    /***************************************************To be deleted*****************************************************************/
    @Query(value = "SELECT id FROM location where name = ?1 LIMIT 1", nativeQuery = true)
    String getLocationIdbyLocationName(String name);
    /******************************************************To be deleted**************************************************************/


    /****************************************************************************** new location changes ***************************************/

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO location(id,name,position,area,floor_id, type, code,updated_timestamp ) VALUES(?1,?2,?3,?4,?5,?7,?8,?9) ON DUPLICATE KEY UPDATE name = ?2, status=?6, type = ?7, code = ?8, updated_timestamp = ?9  ", nativeQuery = true)
    int upsertLocationByFloorId(String id, String name, String position, String area, String floor_id, String status, String type, String code, BigInteger updated_timestamp);

    @Query(nativeQuery = true)
    Set<LocationDTO> getLocationByVdmsId(String vdms_id);

    @Query(nativeQuery = true)
    Set<LocationDTO> getLocationsByFloorId(String floor_id);

    @Query(nativeQuery = true)
    Set<LocationDTO> getLocationsByFloor(String floor_id);

    @Query(nativeQuery = true)
    List<LocationDTO> getLocationsByFloorIds(List<String> floorIds);

    @Query(nativeQuery = true)
    Set<LocationDTO> getLocationsByFloorByPagination(String floor_id, String searchkey, String qrcodeCondition, JSONArray locationIdsTaggedToQrCode, String nfcCondition, JSONArray locationIdsTaggedToNfc, String procedureCondition, String status, Integer pageSize, Integer offset, JSONArray types,String barCodeCondition,JSONArray locationIdsTaggedToBarCode);

    @Query(nativeQuery = true)
    LocationDTO getLocationByLocationId(String location_id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE location SET area = ?2, z_index = ?3 WHERE floor_id = ?1", nativeQuery = true)
    void updateArea(String floor_id, String area, Integer z_index);

    @Modifying
    @Transactional
    @Query(value = "UPDATE location SET name = ?2, position = ?3, area = ?4, z_index = ?5, type = ?6, code = ?7, updated_timestamp = ?8 WHERE id = ?1", nativeQuery = true)
    int updateLocationDetailsByLocationId(String id, String name, String position, String area, Integer z_index, String type, String code, BigInteger updated_timestamp);

    //to be deleted after backend sync
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO location(id,name,position,area,floor_id, type, updated_timestamp ) VALUES(?1,?2,?3,?4,?5, ?6, ?7 ) ON DUPLICATE KEY UPDATE name = ?2, position = ?3, area = ?4,floor_id = ?5, type =?6, updated_timestamp = ?7 ", nativeQuery = true)
    int upsertLocationByFloorIdBackendSync(String id, String name, String position, String area, String floor_id, String type, BigInteger updated_timestamp);

    @Query(value = "SELECT COUNT(*) FROM location WHERE floor_id = ?1 AND (?2 = 'null' or CONCAT_WS('' , name ) LIKE CONCAT('%' ,?2, '%')) ", nativeQuery = true)
    String getLocationsCountByFloorId(String floor_id, String searchkey);

    @Query(nativeQuery = true)
    LocationDTO getLocationDetailsByLocationId(String location_id);

    @Query(nativeQuery = true)
    LocationAlertDTO getLocationAlertDetails(String location_id);


    @Query(nativeQuery = true)
    Set<LocationDTO> getAllChecklistLocationsPagination(String searchkey, Integer pagesize, Integer offset, JSONArray global_checklist_ids, String floor_id,
                                                        Boolean isTaggedToQrCode,
                                                        JSONArray locationIdsTaggedToQrCode, Boolean isTaggedToNfc,
                                                        JSONArray locationIdsTaggedToNfc, JSONArray types, String building_id);

    @Query(nativeQuery = true)
    Set<LocationDTO> getAllInspectionLocationsPagination(String searchkey, Integer pagesize, Integer offset, JSONArray global_checklist_ids, String floor_id,
                                                         String global_inspection_record_id, Boolean isTaggedToQrCode, JSONArray locationIdsTaggedToQrCode,
                                                         Boolean isTaggedToNfc, JSONArray locationIdsTaggedToNfc, JSONArray types, String building_id);

    @Query(nativeQuery = true)
    Set<LocationDTO> getAllQrcodeLocationsPagination(String searchkey, Integer pagesize, Integer offset, String floor_id, Boolean isTaggedToQrCode,
                                                     JSONArray locationIdsTaggedToQrCode, Boolean isTaggedToNfc, JSONArray locationIdsTaggedToNfc, JSONArray types, String building_id);

    @Query(value = "SELECT id FROM location where id IN ?1", nativeQuery = true)
    Set<String> getLocationIds(Set<String> ids);

    @Query(nativeQuery = true)
    List<LocationDTO> getAllLocationDetails();

    @Query(value = "SELECT COUNT(*) "
            + " FROM location l "
            + " LEFT JOIN floor f ON l.floor_id = f.id"
            + " LEFT JOIN building b ON f.building_id = b.id"
            + " WHERE ('all' IN ?10 OR f.building_id IN ?10) AND ('all' IN ?1 OR l.floor_id IN ?1) AND (?2 = 'null' OR CONCAT_WS('' , l.name) LIKE CONCAT('%' ,?2, '%'))"
            + " AND (?3 IS NULL OR IF(?3 = 'present', l.id IN ?4, l.id NOT IN ?4))"
            + " AND (?5 IS NULL OR IF(?5 = 'present', l.id IN ?6, l.id NOT IN ?6))"
            + " AND (?11 IS NULL OR IF(?11 = 'present', l.id IN ?12, l.id NOT IN ?12))"
            + " AND (?7 IS NULL OR IF(?7 = 'present', l.record_checklist_count>0, l.record_checklist_count=0 OR l.record_checklist_count IS NULL))"
            + " AND (?8 IS NULL OR l.status = ?8) AND ('all' IN ?9 OR l.type IN ?9)", nativeQuery = true)
    int searchSortFilterLocationsCount(JSONArray floor_ids, String searchkey, String qrcodeCondition, JSONArray locationIdsTaggedToQrCode, String nfcCondition, JSONArray locationIdsTaggedToNfc, String procedureCondition, String status, JSONArray types, JSONArray building_ids,String barCodeCondition,JSONArray locationIdsTaggedToBarCode);

    @Query(nativeQuery = true)
    Set<LocationDTO> getAllChecklistLocations(String searchkey, String globalChecklistId, String floorId, Boolean isTaggedToQrCode, JSONArray locationIdsTaggedToQrCode, Boolean isTaggedToNfc, JSONArray locationIdsTaggedToNfc, JSONArray types, String building_id);

    @Query(nativeQuery = true)
    Set<LocationDTO> getAllInspectionLocations(String searchkey, String globalChecklistId, String floorId, String globalInspectionRecordId, Boolean isTaggedToQrCode, JSONArray locationIdsTaggedToQrCode, Boolean isTaggedToNfc, JSONArray locationIdsTaggedToNfc, JSONArray types, String building_id);

    @Query(nativeQuery = true)
    Set<LocationDTO> getAllQrcodeLocations(String searchkey, String floorId, Boolean isTaggedToQrCode, JSONArray locationIdsTaggedToQrCode, Boolean isTaggedToNfc, JSONArray locationIdsTaggedToNfc, JSONArray types, String building_id);

    @Query(nativeQuery = true)
    Set<LocationDTO> getLocationDetailsByMeasuringInstrumentId(String measuring_instrument_id);

    @Query(nativeQuery = true)
    Set<LocationDTO> getAllLocationPagination(String searchkey, Integer pagesize, Integer offset, String floorId, Boolean isTaggedToQrCode, JSONArray locationIdsTaggedToQrCode, Boolean isTaggedToNfc, JSONArray locationIdsTaggedToNfc, JSONArray types, String building_id);


    @Query(nativeQuery = true)
    Set<LocationDTO> getAllMeasuringInstrumentLocationsPagination(String searchkey, Integer pagesize, Integer offset, String floorId, JSONArray measuringInstrumentIds, Boolean isTaggedToQrCode, JSONArray locationIdsTaggedToQrCode, Boolean isTaggedToNfc, JSONArray locationIdsTaggedToNfc, JSONArray types, String building_id);


    @Query(value = "SELECT l.id FROM location l" +
            " LEFT JOIN floor f ON l.floor_id = f.id" +
            " LEFT JOIN building b ON f.building_id = b.id" +
            " WHERE (?1 IS NULL or CONCAT_WS('' , l.name ) LIKE CONCAT('%' ,?1, '%'))" +
            " AND (?2 IS NULL OR IF(?2, l.id IN ?3, l.id NOT IN ?3))" +
            " AND (?4 IS NULL OR IF(?4, l.id IN ?5, l.id NOT IN ?5))" +
            " AND (('all' IN ?6) or f.building_id IN ?6)" +
            " AND (('all' IN ?7) or l.floor_id IN ?7)  AND (('all' IN ?8) or l.type IN ?8)", nativeQuery = true)
    List<String> getLocationIdsByFilter(String searchKey, Boolean isTaggedToQrCode, List<String> locationIdsTaggedToQrCode,
                                        Boolean isTaggedToNfc, List<String> locationIdsTaggedToNfc, List<String> buildingIds, List<String> floorIds, List<String> types);

    @Query(nativeQuery = true)
    List<LocationAlertDTO> getLocationsByFilter(String searchKey, Boolean isTaggedToQrCode, List<String> locationIdsTaggedToQrCode,
                                                Boolean isTaggedToNfc, List<String> locationIdsTaggedToNfc, List<String> buildingIds,
                                                List<String> floorIds, List<String> locationIds, List<String> types);


    @Query(value = "SELECT DISTINCT type FROM location WHERE type IS NOT NULL", nativeQuery = true)
    List<String> getUniqueLocationTypes();

    @Query(value = "SELECT COUNT(id) FROM location where id = ?1", nativeQuery = true)
    int getLocationId(String id);

    @Query(nativeQuery = true)
    Set<LocationDTO> getAllLocationsByIds(Set<String> locationIds);

    @Modifying
    @Transactional
    @Query(value = "UPDATE location SET name = IFNULL(?2, name), position = IFNULL(?3, position), area = IFNULL(?4, area), z_index = IFNULL(?5, z_index), type = IFNULL(?6, type),  code = IFNULL(?7, code), updated_timestamp = IFNULL(?8, updated_timestamp)  WHERE id IN ?1", nativeQuery = true)
    int multiUpdateLocations(Set<String> locationIds, String name, String position, String area, String z_index, String type, String code, BigInteger updated_timestamp);

    @Query(value =
            "  SELECT  l.id FROM location l LEFT JOIN floor f ON l.floor_id = f.id" +
                    "  LEFT JOIN building b ON f.building_id = b.id" +
                    "  WHERE l.floor_id = ?1 AND (?2 IS NULL OR CONCAT_WS('' , l.name) LIKE CONCAT('%' ,?2, '%')) " +
                    "  AND (?3 IS NULL OR IF(?3 = 'present', l.id IN ?4, l.id NOT IN ?4))" +
                    "  AND (?5 IS NULL OR IF(?5 = 'present', l.id IN ?6, l.id NOT IN ?6))" +
                    "  AND (?7 IS NULL OR IF(?7 = 'present', l.record_checklist_count>0, l.record_checklist_count=0 OR l.record_checklist_count IS NULL))" +
                    "  AND (?8 IS NULL OR l.id IN ?9) AND ('all' IN ?10 OR l.type IN ?10) ", nativeQuery = true)
    Set<String> getAllLocationIdsByFilter(String floorId, String searchKey, String qrCodeCondition, JSONArray locationIdsTaggedToQrCode, String nfcConditon, JSONArray locationIdsTaggedToNfc, String recordChecklistCondition, String roomStatusCondition, Set<String> locationIdsWithRoomStatus, JSONArray types);


    @Query(value = "SELECT COUNT(*) FROM location l WHERE l.status LIKE CONCAT('%',?1,'%')", nativeQuery = true)
    Integer getLocationStatusCountTs(String status);

    @Query(nativeQuery = true)
    List<LocationAlertDTO> getLocationsByStatus(String status, Integer offset, Integer pagesize);

    @Query(value = "SELECT COUNT(*)"
            + " FROM location l "
            + " WHERE  l.status LIKE CONCAT('%',?1,'%')", nativeQuery = true)
    Integer getLocationsByStatusCountTs(String status);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO location(id,name,position,area,floor_id, type, code, updated_timestamp) VALUES(?1,?2,?3,?4,?5,?7,?8,?9) ON DUPLICATE KEY UPDATE name = ?2, status=?6, type = ?7, code = ?8, area =?4, position = ?3, updated_timestamp = ?9 ", nativeQuery = true)
    int upsertlocationdetails(String id, String name, String position, String area, String floor_id, String status, String type, String code, BigInteger updated_timestamp);


    @Query(nativeQuery = true)
    Set<LocationDTO> getLocationsByBuildingAndFloor(String building_id, String floor_id, String searchKey, String qrCodeCondition, JSONArray locationIdsTaggedToQrCode, String nfcConditon, JSONArray locationIdsTaggedToNfc, String recordChecklistCondition, String status, JSONArray types);

    /******************************************* Integration *********************************************************/
    // getIntegrationByLocationId stubbed in LocationService (Bucket-D integration removed)

    @Query(nativeQuery = true)
    Set<LocationDTO> getAllRecordChecklistLocationsPagination(String searchkey, Integer pagesize, Integer offset, JSONArray global_checklist_ids, String floor_id,
                                                              String inspection_record_id, Boolean isTaggedToQrCode, JSONArray locationIdsTaggedToQrCode,
                                                              Boolean isTaggedToNfc, JSONArray locationIdsTaggedToNfc, JSONArray types, String building_id);


    @Query(nativeQuery = true)
    Set<LocationDTO> getAllLocationsByFilter(JSONArray floor_ids, String searchkey, String qrcodeCondition, JSONArray locationIdsTaggedToQrCode, String nfcCondition, JSONArray locationIdsTaggedToNfc, String procedureCondition, String status, JSONArray types, JSONArray building_ids, String barCodeCondition, JSONArray locationIdsTaggedToBarCode);

    @Query(nativeQuery = true)
    Set<LocationDTO> getAllLocationsByFilterByPagination(JSONArray floor_ids, String searchkey, String qrcodeCondition, JSONArray locationIdsTaggedToQrCode, String nfcCondition, JSONArray locationIdsTaggedToNfc, String procedureCondition, String status, Integer pageSize, Integer offset, JSONArray types, JSONArray building_ids,String barCodeCondition,  JSONArray locationIdsTaggedToBarCode);

    @Query(value = "SELECT name FROM location where id = ?1",nativeQuery = true)
    String getLocationName(String locationId);

    @Query(nativeQuery = true)
    Set<LocationDTO> getAllReactiveServiceLocationsPagination(String searchkey, Integer pagesize, Integer offset,  String floor_id,
                                                        Boolean isTaggedToQrCode,
                                                        JSONArray locationIdsTaggedToQrCode, Boolean isTaggedToNfc,
                                                        JSONArray locationIdsTaggedToNfc, JSONArray types, String building_id);
}
