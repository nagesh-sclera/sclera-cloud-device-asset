package io.sclera.Repository;

import java.math.BigInteger;
import java.util.List;
import java.util.Set;

import javax.transaction.Transactional;

import io.sclera.dto.*;
import io.sclera.dto.touchscreen.SensorDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.sclera.models.MeasuringInstrument;

@Repository
public interface MeasuringInstrumentRepository extends JpaRepository<MeasuringInstrument, String> {


    @Modifying
    @Transactional
    @Query(value = "UPDATE measuring_instrument SET attribute = "
            + "CASE "
            + "WHEN (JSON_EXTRACT(attribute, \"$.parameter_1_protocol\") = ?1 AND JSON_EXTRACT(attribute, \"$.parameter_1_primary_id\") = ?2 AND JSON_EXTRACT(attribute, \"$.parameter_1_secondary_id\") = ?3) THEN JSON_SET(attribute, \"$.parameter_1_value\", ?4) "
            + "WHEN (JSON_EXTRACT(attribute, \"$.parameter_2_protocol\") = ?1 AND JSON_EXTRACT(attribute, \"$.parameter_2_primary_id\") = ?2 AND JSON_EXTRACT(attribute, \"$.parameter_2_secondary_id\") = ?3) THEN JSON_SET(attribute, \"$.parameter_2_value\", ?4) "
            + "WHEN (JSON_EXTRACT(attribute, \"$.parameter_3_protocol\") = ?1 AND JSON_EXTRACT(attribute, \"$.parameter_3_primary_id\") = ?2 AND JSON_EXTRACT(attribute, \"$.parameter_3_secondary_id\") = ?3) THEN JSON_SET(attribute, \"$.parameter_3_value\", ?4) "
            + "WHEN (JSON_EXTRACT(attribute, \"$.parameter_4_protocol\") = ?1 AND JSON_EXTRACT(attribute, \"$.parameter_4_primary_id\") = ?2 AND JSON_EXTRACT(attribute, \"$.parameter_4_secondary_id\") = ?3) THEN JSON_SET(attribute, \"$.parameter_4_value\", ?4) "
            + "WHEN (JSON_EXTRACT(attribute, \"$.parameter_5_protocol\") = ?1 AND JSON_EXTRACT(attribute, \"$.parameter_5_primary_id\") = ?2 AND JSON_EXTRACT(attribute, \"$.parameter_5_secondary_id\") = ?3) THEN JSON_SET(attribute, \"$.parameter_5_value\", ?4) "
            + "ELSE attribute "
            + "END ", nativeQuery = true)
    void updateMeasuringIntrumentParametersByIds(String protocol, String primary_id, String secondary_id, String value);


//
//	@Modifying
//	@Transactional
//	@Query(value = "UPDATE measuring_instrument SET parameter = "
//			+ "CASE "
//			+ "WHEN (JSON_EXTRACT(parameter, \"$.parameter_1_protocol\") = ?1 AND JSON_EXTRACT(parameter, \"$.parameter_1_primary_id\") = ?2 AND JSON_EXTRACT(parameter, \"$.parameter_1_secondary_id\") = ?3) "
//			+ "THEN (JSON_SET(JSON_SET(parameter, \"$.parameter_1_value\", ?4), \"$.parameter_1_unit\", ?5))  "
//			+ "WHEN (JSON_EXTRACT(parameter, \"$.parameter_2_protocol\") = ?1 AND JSON_EXTRACT(parameter, \"$.parameter_2_primary_id\") = ?2 AND JSON_EXTRACT(parameter, \"$.parameter_2_secondary_id\") = ?3) "
//			+ "THEN (JSON_SET(JSON_SET(parameter, \"$.parameter_2_value\", ?4), \"$.parameter_2_unit\", ?5)) "
//			+ "WHEN (JSON_EXTRACT(parameter, \"$.parameter_3_protocol\") = ?1 AND JSON_EXTRACT(parameter, \"$.parameter_3_primary_id\") = ?2 AND JSON_EXTRACT(parameter, \"$.parameter_3_secondary_id\") = ?3) "
//			+ "THEN (JSON_SET(JSON_SET(parameter, \"$.parameter_3_value\", ?4), \"$.parameter_3_unit\", ?5)) "
//			+ "WHEN (JSON_EXTRACT(parameter, \"$.parameter_4_protocol\") = ?1 AND JSON_EXTRACT(parameter, \"$.parameter_4_primary_id\") = ?2 AND JSON_EXTRACT(parameter, \"$.parameter_4_secondary_id\") = ?3) "
//			+ "THEN (JSON_SET(JSON_SET(parameter, \"$.parameter_4_value\", ?4), \"$.parameter_4_unit\", ?5)) "
//			+ "WHEN (JSON_EXTRACT(parameter, \"$.parameter_5_protocol\") = ?1 AND JSON_EXTRACT(parameter, \"$.parameter_5_primary_id\") = ?2 AND JSON_EXTRACT(parameter, \"$.parameter_5_secondary_id\") = ?3) "
//			+ "THEN (JSON_SET(JSON_SET(parameter, \"$.parameter_5_value\", ?4), \"$.parameter_5_unit\", ?5)) "
//			+ "ELSE parameter "
//			+ "END ", nativeQuery = true)
//	void updateMeasuringIntrumentParametersByIds(String protocol, String primary_id, String secondary_id, String value, String unit);


    @Modifying
    @Transactional
    @Query(value = "INSERT INTO measuring_instrument( id , type, name , description , calculation_type , attribute, parameter, category, value, unit, tags, device_id, sensor_type, sub_category, digital_twin_position, scale_type) VALUES (?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8, ?9, ?10, ?11, ?12, ?13, ?17, ?18, ?19) "
            + "ON DUPLICATE KEY UPDATE type = ?2 , name = ?3, description = ?4, calculation_type = ?5, attribute =?6 , parameter =?7, category = ?8,  unit = ?10, tags = ?11, sensor_type = ?13,show_on_map = ?14, show_on_scan = ?15, measuring_entity = ?16, sub_category = ?17, digital_twin_position = ?18, scale_type = ?19", nativeQuery = true)
    void upsertInstrument(String id, String type, String name, String description, String calculation_type,
                          String attribute, String parameter, String category, String value, String unit, String tags,
                          String device_id, String sensor_type, Integer show_on_map, Integer show_on_scan, String measuring_entity,
                          String sub_category, String digital_twin_position, String scale_type);


    @Query(nativeQuery = true)
    Set<MeasuringInstrumentDTO> getInstrumentByDeviceId(String device_id);


    @Modifying
    @Transactional
    @Query(value = "DELETE FROM measuring_instrument WHERE id = ?1", nativeQuery = true)
    void deleteInstrumentById(String instrument_id);

    @Query(nativeQuery = true)
    MeasuringInstrumentDTO getInstrumentByInstrumentId(String id);


    @Modifying
    @Transactional
    @Query(value = "UPDATE measuring_instrument SET value = ?2, timestamp = ?3 WHERE id = ?1", nativeQuery = true)
    void updateInstrumentValueById(String measuingInstrument_id, String value, BigInteger timestamp);


    @Modifying
    @Transactional
    @Query(value = "SELECT DISTINCT id from measuring_instrument WHERE "
            + "(JSON_EXTRACT(attribute,\"$.parameter_1_protocol\") = ?1 AND JSON_EXTRACT(attribute,\"$.parameter_1_primary_id\") = ?2 AND JSON_EXTRACT(attribute,\"$.parameter_1_secondary_id\") = ?3) OR "
            + "(JSON_EXTRACT(attribute,\"$.parameter_2_protocol\") = ?1 AND JSON_EXTRACT(attribute,\"$.parameter_2_primary_id\") = ?2 AND JSON_EXTRACT(attribute,\"$.parameter_2_secondary_id\") = ?3) OR "
            + "(JSON_EXTRACT(attribute,\"$.parameter_3_protocol\") = ?1 AND JSON_EXTRACT(attribute,\"$.parameter_3_primary_id\") = ?2 AND JSON_EXTRACT(attribute,\"$.parameter_3_secondary_id\") = ?3) OR "
            + "(JSON_EXTRACT(attribute,\"$.parameter_4_protocol\") = ?1 AND JSON_EXTRACT(attribute,\"$.parameter_4_primary_id\") = ?2 AND JSON_EXTRACT(attribute,\"$.parameter_4_secondary_id\") = ?3) OR "
            + "(JSON_EXTRACT(attribute,\"$.parameter_5_protocol\") = ?1 AND JSON_EXTRACT(attribute,\"$.parameter_5_primary_id\") = ?2 AND JSON_EXTRACT(attribute,\"$.parameter_5_secondary_id\") = ?3) ", nativeQuery = true)
    List<String> listIdByMeasuringParameter(String protocol, String primary_id, String secondary_id);

    //get measuring device count
    @Query(value = "SELECT COUNT(*) FROM measuring_instrument WHERE device_id = ?1", nativeQuery = true)
    Integer getInstrumentCountByDeviceId(String device_id);

    //used for backend syncing
    @Modifying
    @Transactional
    @Query(value = "UPDATE measuring_instrument SET name = ?1, description = ?2, calculation_type=?3, attribute=?4, parameter=?5, category=?6, value=?7, unit=?8, tags=?9,timestamp=?10 WHERE type=?11 AND id=?12 ", nativeQuery = true)
    Integer syncMeasuringInstrument(String name, String description, String calculation_type, String attribute, String parameter, String category, String value, String unit, String tags, BigInteger timestamp, String type, String id);

    //used for backend syncing
    @Modifying
    @Transactional
    @Query(value = "UPDATE measuring_instrument SET calculation_type=?1, parameter=?2, category=?3, unit=?4, tags=?5 WHERE type=?6 AND id=?7 ", nativeQuery = true)
    Integer syncMeasuringInstrumentExceptAttributes(String calculation_type, String parameter, String category, String unit, String tags, String type, String id);

    @Transactional
    @Query(value = "SELECT DISTINCT(UPPER(mi.sensor_type)) FROM measuring_instrument mi "
            + "JOIN device d ON mi.device_id = d.id AND d.monitor = 1 "
            + "JOIN location l ON d.location_id = l.id "
            + "JOIN floor f ON l.floor_id = f.id WHERE (?1 = 'all' or f.id = ?1) AND mi.sensor_type IS NOT NULL AND mi.show_on_map = 1", nativeQuery = true)
    Set<String> getUniqueSensorCategoryByFloor(String floorid);

    @Query(nativeQuery = true)
    Set<CategorySensorDTO> getSensorCategoryByFloor(String floorid, String category);

    @Query(value = " SELECT COUNT(*) FROM measuring_instrument mi "
            + " LEFT JOIN device d ON mi.device_id = d.id "
            + " LEFT JOIN location l ON d.location_id = l.id "
            + " JOIN floor f ON l.floor_id = f.id WHERE f.id = ?1 AND mi.sensor_type = ?2 AND mi.show_on_map = 1  AND d.monitor = 1 ", nativeQuery = true)
    Integer getSensorCategoryByFloorCount(String floorid, String category);

    @Query(nativeQuery = true)
    List<CategorySensorDTO> getSensorCategoryByFloorPagination(String floorid, String category, Integer pagesize, Integer offset);

    //Update measuring instrument Sensor Alert
    @Modifying
    @Transactional
    @Query(value = "UPDATE measuring_instrument SET alert = ?2 WHERE id = ?1", nativeQuery = true)
    void updateMeasuringInstrumentSensorAlert(String measuring_instrument_id, Boolean newAlert);

    //Get MeasuringInstrument count for touchscreen
    @Query(value = "SELECT COUNT(*) FROM measuring_instrument mi LEFT JOIN device d ON mi.device_id = d.id WHERE"
            + " mi.alert = ?1 AND mi.device_id IS NOT NULL AND d.monitor = 1", nativeQuery = true)
    Integer getMeasuringInstrumentAlertSensorCount(boolean alert);

    //get measuring instrument sensor by id for all required platforms
    @Query(nativeQuery = true)
    MeasuringInstrumentDetailsDTO getMeasuringInstrumentSensorDetailsById(String measuring_instrument_id);

    //Update measuring instrument Sensor User Data Value
    @Modifying
    @Transactional
    @Query(value = "UPDATE measuring_instrument SET user_data_value = ?2 WHERE id = ?1", nativeQuery = true)
    void updateMeasuringinstrumentSensorUserDataValue(String measuring_instrument_id, String user_data_value);

    // get device id by measuring instrument id
    @Query(value = "SELECT device_id FROM measuring_instrument WHERE  id = ?1", nativeQuery = true)
    String getDeviceIdByMeasuringInstrumentSensorId(String measuring_instrument_id);

    // get measuring instrument count by device id based on alert status
    @Query(value = "SELECT COUNT(*) FROM measuring_instrument WHERE device_id = ?1 AND alert = ?2", nativeQuery = true)
    Integer getMeasuringInstrumentAlertCountDeviceId(String device_id, boolean alert);

    @Query(value = "SELECT mi.value FROM measuring_instrument mi WHERE mi.id = ?1", nativeQuery = true)
    String getMeasuringInstrumentSensorCurrentValue(String measuring_instrument_id);

    @Query(nativeQuery = true)
    MeasuringInstrumentDTO getMeasuringInstrumentSensorById(String measuring_instrument_id);

    @Query(nativeQuery = true)
    List<MeasuringInstrumentDTO> getAllMeasuringInstrumentDeviceByPagination(String searchkey, Integer pagesize, Integer offset);

    @Query(nativeQuery = true)
    List<SensorDTO> getmeasuringInstrumentsByDeviceId(String device_id);

    @Query(nativeQuery = true)
    Set<AnalyticSensorDTO> getAnalyticsMeasuringInstruments(String category, String searchkey, Integer pagesize, Integer offset, String report_template_id);

    @Query(nativeQuery = true)
    List<ConditionsDTO> listMeasuringIntrumentDevicesAlertMessagesByDevice(List<String> ids);

    @Query(nativeQuery = true)
    SensorAlertDTO getMeasuringInstrumentAlertDetails(String measuring_instrument_id);

    @Query(nativeQuery = true)
    Set<SensorDTO> getSensorByDeviceId(String deviceid);

    @Query(nativeQuery = true)
    Set<SensorDTO> getSensorByLocationId(String locationid);

    @Modifying
    @Transactional
    @Query(value = "UPDATE measuring_instrument SET value = ?2, timestamp = ?3, attribute = ?4 WHERE id = ?1", nativeQuery = true)
    void updateInstrumentValueAndAttributeById(String measuingInstrument_id, String value, BigInteger timestamp, String attribute);


    @Query(nativeQuery = true)
    List<MeasuringInstrumentDTO> getDaintreeMeasuringInstruments();

    @Query(nativeQuery = true)
    List<CategorySensorDTO> getSensorCategoryByLocationPagination(String locationid, String category, Integer pagesize, Integer offset);

    @Query(value = " SELECT COUNT(*) FROM measuring_instrument mi "
            + " LEFT JOIN device d ON mi.device_id = d.id "
            + " LEFT JOIN location l ON d.location_id = l.id "
            + " JOIN floor f ON l.floor_id = f.id WHERE l.id = ?1 AND mi.sensor_type = ?2 AND mi.show_on_map = 1  AND d.monitor = 1 ", nativeQuery = true)
    Integer getSensorCategoryByLocationCount(String locationid, String category);

    @Query(nativeQuery = true)
    AnalyticSensorDTO getMeasuringInstrumentsByTemplateId(String measuring_instrument_id, String searchkey, String report_attribute_id);

    @Query(value = "SELECT COUNT(*) FROM measuring_instrument_location WHERE measuring_instrument_id = ?1 AND location_id = ?2", nativeQuery = true)
    int checkMeasuringInstrumentsExists(String measuring_instrument_id, String location_id);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO measuring_instrument_location (measuring_instrument_id , location_id) VALUE (?1,?2)", nativeQuery = true)
    void upsertMeasuringInstrumentLocations(String measuring_instrument_id, String location_id);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM measuring_instrument_location WHERE measuring_instrument_id = ?1 AND location_id = ?2", nativeQuery = true)
    void untagLocationsFromMeasuringInstruments(String id, String location_id);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM measuring_instrument_location WHERE location_id = ?1", nativeQuery = true)
    void deleteMeasuringIntrumentLocationsByLocationId(String location_id);

    @Query(nativeQuery = true)
    Set<SensorDTO> getIntegrationSensorByLocationId(String locationid);

    @Query(nativeQuery = true)
    List<MeasuringInstrumentDTO> getSiemensMeasuringInstruments();

    @Query(nativeQuery = true)
    List<MeasuringInstrumentDTO> getMeasuringInstrumentsByDeviceId(String deviceId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE measuring_instrument SET device_id = ?1 WHERE (device_id IS NOT NULL) AND device_id =?2 ", nativeQuery = true)
    void updateMeasuringInstrumentDeviceId(String device_id, String existing_device_id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE measuring_instrument SET attribute = ?2 WHERE id = ?1", nativeQuery = true)
    void updateInstrumentAttributeById(String measuingInstrument_id, String attribute);


    @Modifying
    @Transactional
    @Query(value = "UPDATE measuring_instrument SET attribute = " +
            "  JSON_SET(attribute, CONCAT('$.parameter_', ?1, '_value'), ?2) " +
            "WHERE id = ?3", nativeQuery = true)
    Integer updateMeasuringInstrumentParametersValuesByIds(int parameterIndex, String value, String id);


    @Modifying
    @Transactional
    @Query(value = "UPDATE measuring_instrument SET digital_twin_position = NULL WHERE device_id = ?1", nativeQuery = true)
    void deleteDigitalTwinPositions(String device_id);


    @Modifying
    @Transactional
    @Query(value = "UPDATE measuring_instrument SET attribute = " +
            "JSON_SET(" +
            "JSON_SET(" +
            "JSON_SET(attribute, CONCAT('$.parameter_', ?1, '_primary_id'), ?2), " +
            "CONCAT('$.parameter_', ?1, '_secondary_id'), ?3), " +
            "CONCAT('$.parameter_', ?1, '_tertiary_id'), ?4) " +
            "WHERE id = ?5", nativeQuery = true)
    void updateMeasuringInstrumentPrimaryAndSecondaryId(int parameterIndex, String primaryId, String secondaryId, String tertiaryId, String id);


    @Query(nativeQuery = true)
    Set<MeasuringInstrumentDTO> getAllInstrument();


    @Query(value = "SELECT mi.id FROM measuring_instrument mi" +
            " LEFT JOIN measuring_instrument_attributes mia ON mia.measuring_instrument_id = mi.id" +
            " WHERE mia.protocol = ?1 AND mia.primary_id IN ?2", nativeQuery = true)
    Set<String> getMeasuringInstrumentIdsByProtocolAndPrimaryIds(String protocol, Set<String> primaryIds);


    @Modifying
    @Transactional
    @Query(value = "UPDATE measuring_instrument SET scale_type=?1 ,sensor_type= ?2 ,sub_category=?3 WHERE id=?4", nativeQuery = true)
    void updateScaleTypeAndSensorTypeById(String ScaleType, String SensorType, String subCategory, String id);


    @Query(value = "SELECT COUNT(*) FROM measuring_instrument WHERE ?1='all' OR type = ?1", nativeQuery = true)
    Integer getMeasuringInstrumentCountByType(String type);


    @Query(value = "SELECT COUNT(*) FROM measuring_instrument mi LEFT JOIN measuring_instrument_attributes mia ON mia.measuring_instrument_id = mi.id where mia.type = 'manual' ", nativeQuery = true)
    int getTotalManualAttributesCountofMeasuringInstruments();


}
