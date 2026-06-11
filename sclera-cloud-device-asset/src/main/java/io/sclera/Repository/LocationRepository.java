package io.sclera.Repository;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import jakarta.transaction.Transactional;

import com.alibaba.fastjson.JSONArray;
import io.sclera.dto.LocationAlertDTO;
import io.sclera.dto.LocationDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.sclera.dto.touchscreen.DeviceMonitorSpaceDTO;
import io.sclera.models.Location;


/**
 * Spring Data repository for {@code Location} entities, providing persistence,
 * filtering, pagination, and tag-association queries for locations.
 */
@Repository
public interface LocationRepository extends JpaRepository<Location, String> {


    /**
     * Returns the monitor space status entries matching the given space type, building name, and floor name.
     *
     * @param spacetype the space type filter
     * @param buildingname the building name filter
     * @param floorname the floor name filter
     * @return the matching space status entries
     */
    // NOT CONVERTED — stays native (PG-translation track): DeviceMonitorSpaceDTO projection via @NamedNativeQuery with multi-table CASE WHEN joins
    @Query(nativeQuery = true)
    ArrayList<DeviceMonitorSpaceDTO> listAllSpaceStatus(String spacetype, String buildingname, String floorname);

    /**
     * Returns the identifiers of all locations on the given floor.
     *
     * @param floor_id the floor identifier
     * @return the matching location identifiers
     */
    @Query("SELECT l.id FROM Location l WHERE l.floor.id = ?1")
    Set<String> getLocationIdsByFloorId(String floor_id);

    /**
     * Inserts a new location on the given floor.
     *
     * @param location_id the location identifier
     * @param name the location name
     * @param position the location position
     * @param floor_id the owning floor identifier
     * @param area the location area
     * @param type the location type
     * @param updated_timestamp the update timestamp
     * @return the number of rows inserted
     */
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO location(id,name,position,floor_id, area, type, updated_timestamp ) VALUES(?1,?2,?3,?4,?5,?6,?7)", nativeQuery = true)
    int addLocationByFloorId(String location_id, String name, String position, String floor_id, String area, String type, BigInteger updated_timestamp);


    /**
     * Updates the name, position, area, type, and timestamp of the given location.
     *
     * @param name the new name
     * @param position the new position
     * @param location_id the location identifier
     * @param area the new area
     * @param type the new type
     * @param updated_timestamp the update timestamp
     * @return the number of rows updated
     */
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE Location l SET l.name = ?1, l.position = ?2, l.area = ?4, l.type = ?5, l.updated_timestamp = ?6 WHERE l.id = ?3")
    int updateLocationByLocationId(String name, String position, String location_id, String area, String type, BigInteger updated_timestamp);

    /**
     * Alters the location table to widen the position column.
     */
    @Modifying
    @Transactional
    // NOT CONVERTED — stays native (PG-translation track): runtime DDL ALTER TABLE MODIFY (MySQL-only); should be removed, not converted
    @Query(value = "ALTER TABLE location MODIFY position varchar(128)", nativeQuery = true)
    void modifytable();

    /**
     * Returns the identifiers of locations that are not tagged to any device.
     *
     * @return the unlinked location identifiers
     */
    //get locations not tagged to a device
    @Query("SELECT l.id FROM Location l WHERE l.id NOT IN (SELECT d.location.id FROM Device d WHERE d.location IS NOT NULL)")
    Set<String> getUnlinkedLocationIds();

    /**
     * Returns whether a location with the given identifier exists.
     *
     * @param location_id the location identifier
     * @return 1 if the location exists, otherwise 0
     */
    @Query("SELECT CASE WHEN COUNT(l) > 0 THEN 1 ELSE 0 END FROM Location l WHERE l.id = ?1")
    Integer checkLocationById(String location_id);

    /**
     * Returns the position of the location with the given identifier.
     *
     * @param location_id the location identifier
     * @return the location position
     */
    @Query("SELECT l.position FROM Location l WHERE l.id = ?1")
    String getPositionByLocationId(String location_id);


    /**
     * Returns the detailed record of the location with the given identifier.
     *
     * @param location_id the location identifier
     * @return the matching location details
     */
    @Query(nativeQuery = true)
    LocationDTO getLocationDetails(String location_id);

    /**
     * Updates the record checklist status of the given location.
     *
     * @param location_id the location identifier
     * @param checklist_status the new checklist status
     */
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE Location l SET l.record_checklist_status = ?2 WHERE l.id = ?1")
    void updateLocationRecordChecklistStatus(String location_id, String checklist_status);

    /**
     * Updates the record checklist count of the given location.
     *
     * @param location_id the location identifier
     * @param record_checklist_count the new checklist count
     */
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE Location l SET l.record_checklist_count = ?2 WHERE l.id = ?1")
    void updateLocationRecordChecklistCount(String location_id, Integer record_checklist_count);


    /***************************************************To be deleted*****************************************************************/
    /**
     * Returns the identifier of the first location with the given name.
     *
     * @param name the location name
     * @return the matching location identifier
     */
    default String getLocationIdbyLocationName(String name) {
        List<String> ids = findLocationIdsByName(name);
        return ids.isEmpty() ? null : ids.get(0);
    }

    @Query("SELECT l.id FROM Location l WHERE l.name = ?1")
    List<String> findLocationIdsByName(String name);
    /******************************************************To be deleted**************************************************************/


    /****************************************************************************** new location changes ***************************************/

    /**
     * Inserts a location on the given floor, or updates its name, status, type, code, and timestamp on identifier conflict.
     *
     * @param id the location identifier
     * @param name the location name
     * @param position the location position
     * @param area the location area
     * @param floor_id the owning floor identifier
     * @param status the location status
     * @param type the location type
     * @param code the location code
     * @param updated_timestamp the update timestamp
     * @return the number of rows affected
     */
    @Modifying
    @Transactional
    // PG-port: ON DUPLICATE KEY -> ON CONFLICT (id) DO UPDATE SET (VALUES->EXCLUDED)
    @Query(value = "INSERT INTO location(id,name,position,area,floor_id, type, code,updated_timestamp ) VALUES(?1,?2,?3,?4,?5,?7,?8,?9) ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name, status=?6, type = EXCLUDED.type, code = EXCLUDED.code, updated_timestamp = EXCLUDED.updated_timestamp", nativeQuery = true)
    int upsertLocationByFloorId(String id, String name, String position, String area, String floor_id, String status, String type, String code, BigInteger updated_timestamp);

    /**
     * Returns the locations belonging to the given VDMS.
     *
     * @param vdms_id the VDMS identifier
     * @return the matching locations
     */
    @Query(nativeQuery = true)
    Set<LocationDTO> getLocationByVdmsId(String vdms_id);

    /**
     * Returns the locations on the given floor.
     *
     * @param floor_id the floor identifier
     * @return the matching locations
     */
    @Query(nativeQuery = true)
    Set<LocationDTO> getLocationsByFloorId(String floor_id);

    /**
     * Returns the locations on the given floor.
     *
     * @param floor_id the floor identifier
     * @return the matching locations
     */
    @Query(nativeQuery = true)
    Set<LocationDTO> getLocationsByFloor(String floor_id);

    /**
     * Returns the locations belonging to any of the given floors.
     *
     * @param floorIds the floor identifiers
     * @return the matching locations
     */
    @Query(nativeQuery = true)
    List<LocationDTO> getLocationsByFloorIds(List<String> floorIds);

    /**
     * Returns a paginated, filtered set of locations on the given floor.
     *
     * @param floor_id the floor identifier
     * @param searchkey the search key to match
     * @param qrcodeCondition the QR-code tagging condition
     * @param locationIdsTaggedToQrCode the location identifiers tagged to a QR code
     * @param nfcCondition the NFC tagging condition
     * @param locationIdsTaggedToNfc the location identifiers tagged to NFC
     * @param procedureCondition the procedure condition
     * @param status the location status filter
     * @param pageSize the maximum number of rows to return
     * @param offset the starting row offset
     * @param types the location type filters
     * @param barCodeCondition the barcode tagging condition
     * @param locationIdsTaggedToBarCode the location identifiers tagged to a barcode
     * @return the matching locations for the page
     */
    // NOT CONVERTED — stays native (PG-translation track): JSONArray IN-params + tagging CASE WHEN conditions, no portable JPQL form
    @Query(nativeQuery = true)
    Set<LocationDTO> getLocationsByFloorByPagination(String floor_id, String searchkey, String qrcodeCondition, JSONArray locationIdsTaggedToQrCode, String nfcCondition, JSONArray locationIdsTaggedToNfc, String procedureCondition, String status, Integer pageSize, Integer offset, JSONArray types,String barCodeCondition,JSONArray locationIdsTaggedToBarCode);

    /**
     * Returns the location with the given identifier.
     *
     * @param location_id the location identifier
     * @return the matching location
     */
    @Query(nativeQuery = true)
    LocationDTO getLocationByLocationId(String location_id);

    /**
     * Updates the area and z-index of all locations on the given floor.
     *
     * @param floor_id the floor identifier
     * @param area the new area
     * @param z_index the new z-index
     */
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE Location l SET l.area = ?2, l.z_index = ?3 WHERE l.floor.id = ?1")
    void updateArea(String floor_id, String area, Integer z_index);

    /**
     * Updates the full set of mutable detail fields of the given location.
     *
     * @param id the location identifier
     * @param name the new name
     * @param position the new position
     * @param area the new area
     * @param z_index the new z-index
     * @param type the new type
     * @param code the new code
     * @param updated_timestamp the update timestamp
     * @return the number of rows updated
     */
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE Location l SET l.name = ?2, l.position = ?3, l.area = ?4, l.z_index = ?5, l.type = ?6, l.code = ?7, l.updated_timestamp = ?8 WHERE l.id = ?1")
    int updateLocationDetailsByLocationId(String id, String name, String position, String area, Integer z_index, String type, String code, BigInteger updated_timestamp);

    /**
     * Inserts a location on the given floor, or updates all of its fields on identifier conflict (backend sync).
     *
     * @param id the location identifier
     * @param name the location name
     * @param position the location position
     * @param area the location area
     * @param floor_id the owning floor identifier
     * @param type the location type
     * @param updated_timestamp the update timestamp
     * @return the number of rows affected
     */
    //to be deleted after backend sync
    @Modifying
    @Transactional
    // PG-port: ON DUPLICATE KEY -> ON CONFLICT (id) DO UPDATE SET (VALUES->EXCLUDED)
    @Query(value = "INSERT INTO location(id,name,position,area,floor_id, type, updated_timestamp ) VALUES(?1,?2,?3,?4,?5, ?6, ?7 ) ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name, position = EXCLUDED.position, area = EXCLUDED.area, floor_id = EXCLUDED.floor_id, type = EXCLUDED.type, updated_timestamp = EXCLUDED.updated_timestamp", nativeQuery = true)
    int upsertLocationByFloorIdBackendSync(String id, String name, String position, String area, String floor_id, String type, BigInteger updated_timestamp);

    /**
     * Returns the number of locations on the given floor matching the search key.
     *
     * @param floor_id the floor identifier
     * @param searchkey the search key to match
     * @return the matching location count
     */
    default String getLocationsCountByFloorId(String floor_id, String searchkey) {
        return String.valueOf(countLocationsByFloorId(floor_id, searchkey));
    }

    @Query("SELECT COUNT(l) FROM Location l WHERE l.floor.id = ?1 AND (?2 = 'null' OR CONCAT(COALESCE(l.name, '')) LIKE CONCAT('%', ?2, '%'))")
    Long countLocationsByFloorId(String floor_id, String searchkey);

    /**
     * Returns the detailed record of the location with the given identifier.
     *
     * @param location_id the location identifier
     * @return the matching location details
     */
    @Query(nativeQuery = true)
    LocationDTO getLocationDetailsByLocationId(String location_id);

    /**
     * Returns the alert details of the location with the given identifier.
     *
     * @param location_id the location identifier
     * @return the matching location alert details
     */
    @Query(nativeQuery = true)
    LocationAlertDTO getLocationAlertDetails(String location_id);


    /**
     * Returns a paginated, filtered set of locations associated with the given global checklists.
     *
     * @param searchkey the search key to match
     * @param pagesize the maximum number of rows to return
     * @param offset the starting row offset
     * @param global_checklist_ids the global checklist identifiers
     * @param floor_id the floor identifier filter
     * @param isTaggedToQrCode whether to filter by QR-code tagging
     * @param locationIdsTaggedToQrCode the location identifiers tagged to a QR code
     * @param isTaggedToNfc whether to filter by NFC tagging
     * @param locationIdsTaggedToNfc the location identifiers tagged to NFC
     * @param types the location type filters
     * @param building_id the building identifier filter
     * @return the matching locations for the page
     */
    // NOT CONVERTED — stays native (PG-translation track): JSONArray IN + multi-join dynamic filters
    @Query(nativeQuery = true)
    Set<LocationDTO> getAllChecklistLocationsPagination(String searchkey, Integer pagesize, Integer offset, JSONArray global_checklist_ids, String floor_id,
                                                        Boolean isTaggedToQrCode,
                                                        JSONArray locationIdsTaggedToQrCode, Boolean isTaggedToNfc,
                                                        JSONArray locationIdsTaggedToNfc, JSONArray types, String building_id);

    /**
     * Returns a paginated, filtered set of inspection locations for the given checklists and inspection record.
     *
     * @param searchkey the search key to match
     * @param pagesize the maximum number of rows to return
     * @param offset the starting row offset
     * @param global_checklist_ids the global checklist identifiers
     * @param floor_id the floor identifier filter
     * @param global_inspection_record_id the global inspection record identifier
     * @param isTaggedToQrCode whether to filter by QR-code tagging
     * @param locationIdsTaggedToQrCode the location identifiers tagged to a QR code
     * @param isTaggedToNfc whether to filter by NFC tagging
     * @param locationIdsTaggedToNfc the location identifiers tagged to NFC
     * @param types the location type filters
     * @param building_id the building identifier filter
     * @return the matching locations for the page
     */
    // NOT CONVERTED — stays native (PG-translation track): JSONArray IN + multi-join dynamic filters
    @Query(nativeQuery = true)
    Set<LocationDTO> getAllInspectionLocationsPagination(String searchkey, Integer pagesize, Integer offset, JSONArray global_checklist_ids, String floor_id,
                                                         String global_inspection_record_id, Boolean isTaggedToQrCode, JSONArray locationIdsTaggedToQrCode,
                                                         Boolean isTaggedToNfc, JSONArray locationIdsTaggedToNfc, JSONArray types, String building_id);

    /**
     * Returns a paginated, filtered set of QR-code locations.
     *
     * @param searchkey the search key to match
     * @param pagesize the maximum number of rows to return
     * @param offset the starting row offset
     * @param floor_id the floor identifier filter
     * @param isTaggedToQrCode whether to filter by QR-code tagging
     * @param locationIdsTaggedToQrCode the location identifiers tagged to a QR code
     * @param isTaggedToNfc whether to filter by NFC tagging
     * @param locationIdsTaggedToNfc the location identifiers tagged to NFC
     * @param types the location type filters
     * @param building_id the building identifier filter
     * @return the matching locations for the page
     */
    // NOT CONVERTED — stays native (PG-translation track): JSONArray IN + multi-join dynamic filters
    @Query(nativeQuery = true)
    Set<LocationDTO> getAllQrcodeLocationsPagination(String searchkey, Integer pagesize, Integer offset, String floor_id, Boolean isTaggedToQrCode,
                                                     JSONArray locationIdsTaggedToQrCode, Boolean isTaggedToNfc, JSONArray locationIdsTaggedToNfc, JSONArray types, String building_id);

    /**
     * Returns the subset of the supplied identifiers that exist as locations.
     *
     * @param ids the candidate location identifiers
     * @return the identifiers found in the table
     */
    @Query("SELECT l.id FROM Location l WHERE l.id IN ?1")
    Set<String> getLocationIds(Set<String> ids);

    /**
     * Returns the detailed records of all locations.
     *
     * @return all location details
     */
    @Query(nativeQuery = true)
    List<LocationDTO> getAllLocationDetails();

    /**
     * Returns the number of locations matching the combined search, sort, and filter criteria.
     *
     * @param floor_ids the floor identifier filters, or "all"
     * @param searchkey the search key to match
     * @param qrcodeCondition the QR-code tagging condition
     * @param locationIdsTaggedToQrCode the location identifiers tagged to a QR code
     * @param nfcCondition the NFC tagging condition
     * @param locationIdsTaggedToNfc the location identifiers tagged to NFC
     * @param procedureCondition the procedure/checklist condition
     * @param status the location status filter
     * @param types the location type filters, or "all"
     * @param building_ids the building identifier filters, or "all"
     * @param barCodeCondition the barcode tagging condition
     * @param locationIdsTaggedToBarCode the location identifiers tagged to a barcode
     * @return the matching location count
     */
    // NOT CONVERTED — stays native (PG-translation track): multi-join (floor/building) + JSONArray IN + 'all' IN ?n + CASE WHEN dynamics
    // PG-port: IF->CASE WHEN
    @Query(value = "SELECT COUNT(*) "
            + " FROM location l "
            + " LEFT JOIN floor f ON l.floor_id = f.id"
            + " LEFT JOIN building b ON f.building_id = b.id"
            + " WHERE ('all' IN ?10 OR f.building_id IN ?10) AND ('all' IN ?1 OR l.floor_id IN ?1) AND (?2 = 'null' OR CONCAT_WS('' , l.name) LIKE CONCAT('%' ,?2, '%'))"
            + " AND (?3 IS NULL OR CASE WHEN ?3 = 'present' THEN l.id IN ?4 ELSE l.id NOT IN ?4 END)"
            + " AND (?5 IS NULL OR CASE WHEN ?5 = 'present' THEN l.id IN ?6 ELSE l.id NOT IN ?6 END)"
            + " AND (?11 IS NULL OR CASE WHEN ?11 = 'present' THEN l.id IN ?12 ELSE l.id NOT IN ?12 END)"
            + " AND (?7 IS NULL OR CASE WHEN ?7 = 'present' THEN l.record_checklist_count>0 ELSE l.record_checklist_count=0 OR l.record_checklist_count IS NULL END)"
            + " AND (?8 IS NULL OR l.status = ?8) AND ('all' IN ?9 OR l.type IN ?9)", nativeQuery = true)
    int searchSortFilterLocationsCount(JSONArray floor_ids, String searchkey, String qrcodeCondition, JSONArray locationIdsTaggedToQrCode, String nfcCondition, JSONArray locationIdsTaggedToNfc, String procedureCondition, String status, JSONArray types, JSONArray building_ids,String barCodeCondition,JSONArray locationIdsTaggedToBarCode);

    /**
     * Returns the checklist locations matching the given filters (non-paginated).
     *
     * @param searchkey the search key to match
     * @param globalChecklistId the global checklist identifier
     * @param floorId the floor identifier filter
     * @param isTaggedToQrCode whether to filter by QR-code tagging
     * @param locationIdsTaggedToQrCode the location identifiers tagged to a QR code
     * @param isTaggedToNfc whether to filter by NFC tagging
     * @param locationIdsTaggedToNfc the location identifiers tagged to NFC
     * @param types the location type filters
     * @param building_id the building identifier filter
     * @return the matching locations
     */
    // NOT CONVERTED — stays native (PG-translation track): JSONArray IN + multi-join dynamic filter
    @Query(nativeQuery = true)
    Set<LocationDTO> getAllChecklistLocations(String searchkey, String globalChecklistId, String floorId, Boolean isTaggedToQrCode, JSONArray locationIdsTaggedToQrCode, Boolean isTaggedToNfc, JSONArray locationIdsTaggedToNfc, JSONArray types, String building_id);

    /**
     * Returns the inspection locations matching the given filters (non-paginated).
     *
     * @param searchkey the search key to match
     * @param globalChecklistId the global checklist identifier
     * @param floorId the floor identifier filter
     * @param globalInspectionRecordId the global inspection record identifier
     * @param isTaggedToQrCode whether to filter by QR-code tagging
     * @param locationIdsTaggedToQrCode the location identifiers tagged to a QR code
     * @param isTaggedToNfc whether to filter by NFC tagging
     * @param locationIdsTaggedToNfc the location identifiers tagged to NFC
     * @param types the location type filters
     * @param building_id the building identifier filter
     * @return the matching locations
     */
    // NOT CONVERTED — stays native (PG-translation track): JSONArray IN + multi-join dynamic filter
    @Query(nativeQuery = true)
    Set<LocationDTO> getAllInspectionLocations(String searchkey, String globalChecklistId, String floorId, String globalInspectionRecordId, Boolean isTaggedToQrCode, JSONArray locationIdsTaggedToQrCode, Boolean isTaggedToNfc, JSONArray locationIdsTaggedToNfc, JSONArray types, String building_id);

    /**
     * Returns the QR-code locations matching the given filters (non-paginated).
     *
     * @param searchkey the search key to match
     * @param floorId the floor identifier filter
     * @param isTaggedToQrCode whether to filter by QR-code tagging
     * @param locationIdsTaggedToQrCode the location identifiers tagged to a QR code
     * @param isTaggedToNfc whether to filter by NFC tagging
     * @param locationIdsTaggedToNfc the location identifiers tagged to NFC
     * @param types the location type filters
     * @param building_id the building identifier filter
     * @return the matching locations
     */
    // NOT CONVERTED — stays native (PG-translation track): JSONArray IN + multi-join dynamic filter
    @Query(nativeQuery = true)
    Set<LocationDTO> getAllQrcodeLocations(String searchkey, String floorId, Boolean isTaggedToQrCode, JSONArray locationIdsTaggedToQrCode, Boolean isTaggedToNfc, JSONArray locationIdsTaggedToNfc, JSONArray types, String building_id);

    /**
     * Returns the locations associated with the given measuring instrument.
     *
     * @param measuring_instrument_id the measuring instrument identifier
     * @return the matching locations
     */
    // NOT CONVERTED — stays native (PG-translation track): projection via join to measuring-instrument mapping
    @Query(nativeQuery = true)
    Set<LocationDTO> getLocationDetailsByMeasuringInstrumentId(String measuring_instrument_id);

    /**
     * Returns a paginated, filtered set of all locations.
     *
     * @param searchkey the search key to match
     * @param pagesize the maximum number of rows to return
     * @param offset the starting row offset
     * @param floorId the floor identifier filter
     * @param isTaggedToQrCode whether to filter by QR-code tagging
     * @param locationIdsTaggedToQrCode the location identifiers tagged to a QR code
     * @param isTaggedToNfc whether to filter by NFC tagging
     * @param locationIdsTaggedToNfc the location identifiers tagged to NFC
     * @param types the location type filters
     * @param building_id the building identifier filter
     * @return the matching locations for the page
     */
    // NOT CONVERTED — stays native (PG-translation track): JSONArray IN + multi-join dynamic filter
    @Query(nativeQuery = true)
    Set<LocationDTO> getAllLocationPagination(String searchkey, Integer pagesize, Integer offset, String floorId, Boolean isTaggedToQrCode, JSONArray locationIdsTaggedToQrCode, Boolean isTaggedToNfc, JSONArray locationIdsTaggedToNfc, JSONArray types, String building_id);


    /**
     * Returns a paginated, filtered set of locations associated with the given measuring instruments.
     *
     * @param searchkey the search key to match
     * @param pagesize the maximum number of rows to return
     * @param offset the starting row offset
     * @param floorId the floor identifier filter
     * @param measuringInstrumentIds the measuring instrument identifiers
     * @param isTaggedToQrCode whether to filter by QR-code tagging
     * @param locationIdsTaggedToQrCode the location identifiers tagged to a QR code
     * @param isTaggedToNfc whether to filter by NFC tagging
     * @param locationIdsTaggedToNfc the location identifiers tagged to NFC
     * @param types the location type filters
     * @param building_id the building identifier filter
     * @return the matching locations for the page
     */
    // NOT CONVERTED — stays native (PG-translation track): JSONArray IN + multi-join dynamic filter
    @Query(nativeQuery = true)
    Set<LocationDTO> getAllMeasuringInstrumentLocationsPagination(String searchkey, Integer pagesize, Integer offset, String floorId, JSONArray measuringInstrumentIds, Boolean isTaggedToQrCode, JSONArray locationIdsTaggedToQrCode, Boolean isTaggedToNfc, JSONArray locationIdsTaggedToNfc, JSONArray types, String building_id);


    /**
     * Returns the identifiers of locations matching the given filters.
     *
     * @param searchKey the search key to match
     * @param isTaggedToQrCode whether to filter by QR-code tagging
     * @param locationIdsTaggedToQrCode the location identifiers tagged to a QR code
     * @param isTaggedToNfc whether to filter by NFC tagging
     * @param locationIdsTaggedToNfc the location identifiers tagged to NFC
     * @param buildingIds the building identifier filters, or "all"
     * @param floorIds the floor identifier filters, or "all"
     * @param types the location type filters, or "all"
     * @return the matching location identifiers
     */
    // NOT CONVERTED — stays native (PG-translation track): multi-join + JSONArray + 'all' IN + CASE WHEN
    // PG-port: IF->CASE WHEN
    @Query(value = "SELECT l.id FROM location l" +
            " LEFT JOIN floor f ON l.floor_id = f.id" +
            " LEFT JOIN building b ON f.building_id = b.id" +
            " WHERE (?1 IS NULL or CONCAT_WS('' , l.name ) LIKE CONCAT('%' ,?1, '%'))" +
            " AND (?2 IS NULL OR CASE WHEN ?2 THEN l.id IN ?3 ELSE l.id NOT IN ?3 END)" +
            " AND (?4 IS NULL OR CASE WHEN ?4 THEN l.id IN ?5 ELSE l.id NOT IN ?5 END)" +
            " AND (('all' IN ?6) or f.building_id IN ?6)" +
            " AND (('all' IN ?7) or l.floor_id IN ?7)  AND (('all' IN ?8) or l.type IN ?8)", nativeQuery = true)
    List<String> getLocationIdsByFilter(String searchKey, Boolean isTaggedToQrCode, List<String> locationIdsTaggedToQrCode,
                                        Boolean isTaggedToNfc, List<String> locationIdsTaggedToNfc, List<String> buildingIds, List<String> floorIds, List<String> types);

    /**
     * Returns the alert-detail records of locations matching the given filters.
     *
     * @param searchKey the search key to match
     * @param isTaggedToQrCode whether to filter by QR-code tagging
     * @param locationIdsTaggedToQrCode the location identifiers tagged to a QR code
     * @param isTaggedToNfc whether to filter by NFC tagging
     * @param locationIdsTaggedToNfc the location identifiers tagged to NFC
     * @param buildingIds the building identifier filters
     * @param floorIds the floor identifier filters
     * @param locationIds the location identifier filters
     * @param types the location type filters
     * @return the matching location alert details
     */
    // NOT CONVERTED — stays native (PG-translation track): multi-join + JSONArray + 'all' IN + CASE WHEN
    @Query(nativeQuery = true)
    List<LocationAlertDTO> getLocationsByFilter(String searchKey, Boolean isTaggedToQrCode, List<String> locationIdsTaggedToQrCode,
                                                Boolean isTaggedToNfc, List<String> locationIdsTaggedToNfc, List<String> buildingIds,
                                                List<String> floorIds, List<String> locationIds, List<String> types);


    /**
     * Returns the distinct non-null location types.
     *
     * @return the unique location types
     */
    @Query("SELECT DISTINCT l.type FROM Location l WHERE l.type IS NOT NULL")
    List<String> getUniqueLocationTypes();

    /**
     * Returns the number of locations matching the given identifier.
     *
     * @param id the location identifier
     * @return the matching count
     */
    @Query("SELECT COUNT(l.id) FROM Location l WHERE l.id = ?1")
    int getLocationId(String id);

    /**
     * Returns the locations matching the given identifiers.
     *
     * @param locationIds the location identifiers
     * @return the matching locations
     */
    @Query(nativeQuery = true)
    Set<LocationDTO> getAllLocationsByIds(Set<String> locationIds);

    /**
     * Updates the given locations, leaving each field unchanged when its supplied value is null.
     *
     * @param locationIds the location identifiers
     * @param name the new name, or null to retain
     * @param position the new position, or null to retain
     * @param area the new area, or null to retain
     * @param z_index the new z-index, or null to retain
     * @param type the new type, or null to retain
     * @param code the new code, or null to retain
     * @param updated_timestamp the update timestamp, or null to retain
     * @return the number of rows updated
     */
    @Modifying
    @Transactional
    // NOT CONVERTED — stays native (PG-translation track): already PG-portable COALESCE; JPQL rejects the String/Integer z_index COALESCE mix
    // PG-port: IFNULL->COALESCE
    @Query(value = "UPDATE location SET name = COALESCE(?2, name), position = COALESCE(?3, position), area = COALESCE(?4, area), z_index = COALESCE(?5, z_index), type = COALESCE(?6, type),  code = COALESCE(?7, code), updated_timestamp = COALESCE(?8, updated_timestamp)  WHERE id IN ?1", nativeQuery = true)
    int multiUpdateLocations(Set<String> locationIds, String name, String position, String area, String z_index, String type, String code, BigInteger updated_timestamp);

    /**
     * Returns the identifiers of locations on the given floor matching the supplied filters.
     *
     * @param floorId the floor identifier
     * @param searchKey the search key to match
     * @param qrCodeCondition the QR-code tagging condition
     * @param locationIdsTaggedToQrCode the location identifiers tagged to a QR code
     * @param nfcConditon the NFC tagging condition
     * @param locationIdsTaggedToNfc the location identifiers tagged to NFC
     * @param recordChecklistCondition the record checklist condition
     * @param roomStatusCondition the room status condition
     * @param locationIdsWithRoomStatus the location identifiers with a matching room status
     * @param types the location type filters, or "all"
     * @return the matching location identifiers
     */
    // NOT CONVERTED — stays native (PG-translation track): multi-join + JSONArray IN + CASE WHEN
    // PG-port: IF->CASE WHEN
    @Query(value =
            "  SELECT  l.id FROM location l LEFT JOIN floor f ON l.floor_id = f.id" +
                    "  LEFT JOIN building b ON f.building_id = b.id" +
                    "  WHERE l.floor_id = ?1 AND (?2 IS NULL OR CONCAT_WS('' , l.name) LIKE CONCAT('%' ,?2, '%')) " +
                    "  AND (?3 IS NULL OR CASE WHEN ?3 = 'present' THEN l.id IN ?4 ELSE l.id NOT IN ?4 END)" +
                    "  AND (?5 IS NULL OR CASE WHEN ?5 = 'present' THEN l.id IN ?6 ELSE l.id NOT IN ?6 END)" +
                    "  AND (?7 IS NULL OR CASE WHEN ?7 = 'present' THEN l.record_checklist_count>0 ELSE l.record_checklist_count=0 OR l.record_checklist_count IS NULL END)" +
                    "  AND (?8 IS NULL OR l.id IN ?9) AND ('all' IN ?10 OR l.type IN ?10) ", nativeQuery = true)
    Set<String> getAllLocationIdsByFilter(String floorId, String searchKey, String qrCodeCondition, JSONArray locationIdsTaggedToQrCode, String nfcConditon, JSONArray locationIdsTaggedToNfc, String recordChecklistCondition, String roomStatusCondition, Set<String> locationIdsWithRoomStatus, JSONArray types);


    /**
     * Returns the number of locations whose status matches the given value (touchscreen).
     *
     * @param status the status substring to match
     * @return the matching location count
     */
    @Query("SELECT COUNT(l) FROM Location l WHERE l.status LIKE CONCAT('%', ?1, '%')")
    Integer getLocationStatusCountTs(String status);

    /**
     * Returns a paginated list of location alert details matching the given status.
     *
     * @param status the status to match
     * @param offset the starting row offset
     * @param pagesize the maximum number of rows to return
     * @return the matching location alert details for the page
     */
    @Query(nativeQuery = true)
    List<LocationAlertDTO> getLocationsByStatus(String status, Integer offset, Integer pagesize);

    /**
     * Returns the number of locations whose status matches the given value (touchscreen).
     *
     * @param status the status substring to match
     * @return the matching location count
     */
    @Query("SELECT COUNT(l) FROM Location l WHERE l.status LIKE CONCAT('%', ?1, '%')")
    Integer getLocationsByStatusCountTs(String status);

    /**
     * Inserts a location, or updates its detail fields on identifier conflict.
     *
     * @param id the location identifier
     * @param name the location name
     * @param position the location position
     * @param area the location area
     * @param floor_id the owning floor identifier
     * @param status the location status
     * @param type the location type
     * @param code the location code
     * @param updated_timestamp the update timestamp
     * @return the number of rows affected
     */
    @Modifying
    @Transactional
    // PG-port: ON DUPLICATE KEY -> ON CONFLICT (id) DO UPDATE SET (VALUES->EXCLUDED)
    @Query(value = "INSERT INTO location(id,name,position,area,floor_id, type, code, updated_timestamp) VALUES(?1,?2,?3,?4,?5,?7,?8,?9) ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name, status=?6, type = EXCLUDED.type, code = EXCLUDED.code, area = EXCLUDED.area, position = EXCLUDED.position, updated_timestamp = EXCLUDED.updated_timestamp", nativeQuery = true)
    int upsertlocationdetails(String id, String name, String position, String area, String floor_id, String status, String type, String code, BigInteger updated_timestamp);


    /**
     * Returns the locations in the given building and floor matching the supplied filters.
     *
     * @param building_id the building identifier
     * @param floor_id the floor identifier
     * @param searchKey the search key to match
     * @param qrCodeCondition the QR-code tagging condition
     * @param locationIdsTaggedToQrCode the location identifiers tagged to a QR code
     * @param nfcConditon the NFC tagging condition
     * @param locationIdsTaggedToNfc the location identifiers tagged to NFC
     * @param recordChecklistCondition the record checklist condition
     * @param status the location status filter
     * @param types the location type filters
     * @return the matching locations
     */
    // NOT CONVERTED — stays native (PG-translation track): JSONArray IN + multi-join dynamic filter
    @Query(nativeQuery = true)
    Set<LocationDTO> getLocationsByBuildingAndFloor(String building_id, String floor_id, String searchKey, String qrCodeCondition, JSONArray locationIdsTaggedToQrCode, String nfcConditon, JSONArray locationIdsTaggedToNfc, String recordChecklistCondition, String status, JSONArray types);

    /******************************************* Integration *********************************************************/
    // getIntegrationByLocationId stubbed in LocationService (Bucket-D integration removed)

    /**
     * Returns a paginated, filtered set of record-checklist locations for the given checklists and inspection record.
     *
     * @param searchkey the search key to match
     * @param pagesize the maximum number of rows to return
     * @param offset the starting row offset
     * @param global_checklist_ids the global checklist identifiers
     * @param floor_id the floor identifier filter
     * @param inspection_record_id the inspection record identifier
     * @param isTaggedToQrCode whether to filter by QR-code tagging
     * @param locationIdsTaggedToQrCode the location identifiers tagged to a QR code
     * @param isTaggedToNfc whether to filter by NFC tagging
     * @param locationIdsTaggedToNfc the location identifiers tagged to NFC
     * @param types the location type filters
     * @param building_id the building identifier filter
     * @return the matching locations for the page
     */
    // NOT CONVERTED — stays native (PG-translation track): JSONArray IN + multi-join dynamic filters
    @Query(nativeQuery = true)
    Set<LocationDTO> getAllRecordChecklistLocationsPagination(String searchkey, Integer pagesize, Integer offset, JSONArray global_checklist_ids, String floor_id,
                                                              String inspection_record_id, Boolean isTaggedToQrCode, JSONArray locationIdsTaggedToQrCode,
                                                              Boolean isTaggedToNfc, JSONArray locationIdsTaggedToNfc, JSONArray types, String building_id);


    /**
     * Returns all locations matching the combined search and filter criteria (non-paginated).
     *
     * @param floor_ids the floor identifier filters
     * @param searchkey the search key to match
     * @param qrcodeCondition the QR-code tagging condition
     * @param locationIdsTaggedToQrCode the location identifiers tagged to a QR code
     * @param nfcCondition the NFC tagging condition
     * @param locationIdsTaggedToNfc the location identifiers tagged to NFC
     * @param procedureCondition the procedure/checklist condition
     * @param status the location status filter
     * @param types the location type filters
     * @param building_ids the building identifier filters
     * @param barCodeCondition the barcode tagging condition
     * @param locationIdsTaggedToBarCode the location identifiers tagged to a barcode
     * @return the matching locations
     */
    // NOT CONVERTED — stays native (PG-translation track): JSONArray IN + multi-join dynamic filter
    @Query(nativeQuery = true)
    Set<LocationDTO> getAllLocationsByFilter(JSONArray floor_ids, String searchkey, String qrcodeCondition, JSONArray locationIdsTaggedToQrCode, String nfcCondition, JSONArray locationIdsTaggedToNfc, String procedureCondition, String status, JSONArray types, JSONArray building_ids, String barCodeCondition, JSONArray locationIdsTaggedToBarCode);

    /**
     * Returns a paginated set of locations matching the combined search and filter criteria.
     *
     * @param floor_ids the floor identifier filters
     * @param searchkey the search key to match
     * @param qrcodeCondition the QR-code tagging condition
     * @param locationIdsTaggedToQrCode the location identifiers tagged to a QR code
     * @param nfcCondition the NFC tagging condition
     * @param locationIdsTaggedToNfc the location identifiers tagged to NFC
     * @param procedureCondition the procedure/checklist condition
     * @param status the location status filter
     * @param pageSize the maximum number of rows to return
     * @param offset the starting row offset
     * @param types the location type filters
     * @param building_ids the building identifier filters
     * @param barCodeCondition the barcode tagging condition
     * @param locationIdsTaggedToBarCode the location identifiers tagged to a barcode
     * @return the matching locations for the page
     */
    // NOT CONVERTED — stays native (PG-translation track): JSONArray IN + multi-join dynamic filter
    @Query(nativeQuery = true)
    Set<LocationDTO> getAllLocationsByFilterByPagination(JSONArray floor_ids, String searchkey, String qrcodeCondition, JSONArray locationIdsTaggedToQrCode, String nfcCondition, JSONArray locationIdsTaggedToNfc, String procedureCondition, String status, Integer pageSize, Integer offset, JSONArray types, JSONArray building_ids,String barCodeCondition,  JSONArray locationIdsTaggedToBarCode);

    /**
     * Returns the name of the location with the given identifier.
     *
     * @param locationId the location identifier
     * @return the location name
     */
    @Query("SELECT l.name FROM Location l WHERE l.id = ?1")
    String getLocationName(String locationId);

    /**
     * Returns a paginated, filtered set of reactive-service locations.
     *
     * @param searchkey the search key to match
     * @param pagesize the maximum number of rows to return
     * @param offset the starting row offset
     * @param floor_id the floor identifier filter
     * @param isTaggedToQrCode whether to filter by QR-code tagging
     * @param locationIdsTaggedToQrCode the location identifiers tagged to a QR code
     * @param isTaggedToNfc whether to filter by NFC tagging
     * @param locationIdsTaggedToNfc the location identifiers tagged to NFC
     * @param types the location type filters
     * @param building_id the building identifier filter
     * @return the matching locations for the page
     */
    // NOT CONVERTED — stays native (PG-translation track): JSONArray IN + multi-join dynamic filters
    @Query(nativeQuery = true)
    Set<LocationDTO> getAllReactiveServiceLocationsPagination(String searchkey, Integer pagesize, Integer offset,  String floor_id,
                                                        Boolean isTaggedToQrCode,
                                                        JSONArray locationIdsTaggedToQrCode, Boolean isTaggedToNfc,
                                                        JSONArray locationIdsTaggedToNfc, JSONArray types, String building_id);
}
