package io.sclera.Repository;

import java.math.BigInteger;
import java.util.List;
import java.util.Set;

import jakarta.transaction.Transactional;

import io.sclera.dto.*;
import io.sclera.dto.touchscreen.SensorDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.sclera.models.MeasuringInstrument;

/**
 * Manages persistence and querying of {@link MeasuringInstrument} entities.
 */
@Repository
public interface MeasuringInstrumentRepository extends JpaRepository<MeasuringInstrument, String> {


    /**
     * Updates the matching parameter value within an instrument's attribute JSON when its protocol and identifiers match.
     *
     * @param protocol the parameter protocol to match
     * @param primary_id the parameter primary identifier to match
     * @param secondary_id the parameter secondary identifier to match
     * @param value the new parameter value to set
     */
    // PG-port: JSON_EXTRACT(col,'$.k')=?N -> col::jsonb ->> 'k' = ?N (text comparison);
    //          JSON_SET(col,'$.k',?4) -> jsonb_set(col::jsonb,'{k}',to_jsonb(CAST(?4 AS text)))::text.
    //          attribute column is text; cast to ::jsonb for operators, cast result back to text.
    //          Pattern validated with direct psql SELECT on jsonb literals.
    @Modifying
    @Transactional
    @Query(value = "UPDATE measuring_instrument SET attribute = "
            + "CASE "
            + "WHEN (attribute::jsonb ->> 'parameter_1_protocol' = ?1 AND attribute::jsonb ->> 'parameter_1_primary_id' = ?2 AND attribute::jsonb ->> 'parameter_1_secondary_id' = ?3) THEN jsonb_set(attribute::jsonb, '{parameter_1_value}', to_jsonb(CAST(?4 AS text)))::text "
            + "WHEN (attribute::jsonb ->> 'parameter_2_protocol' = ?1 AND attribute::jsonb ->> 'parameter_2_primary_id' = ?2 AND attribute::jsonb ->> 'parameter_2_secondary_id' = ?3) THEN jsonb_set(attribute::jsonb, '{parameter_2_value}', to_jsonb(CAST(?4 AS text)))::text "
            + "WHEN (attribute::jsonb ->> 'parameter_3_protocol' = ?1 AND attribute::jsonb ->> 'parameter_3_primary_id' = ?2 AND attribute::jsonb ->> 'parameter_3_secondary_id' = ?3) THEN jsonb_set(attribute::jsonb, '{parameter_3_value}', to_jsonb(CAST(?4 AS text)))::text "
            + "WHEN (attribute::jsonb ->> 'parameter_4_protocol' = ?1 AND attribute::jsonb ->> 'parameter_4_primary_id' = ?2 AND attribute::jsonb ->> 'parameter_4_secondary_id' = ?3) THEN jsonb_set(attribute::jsonb, '{parameter_4_value}', to_jsonb(CAST(?4 AS text)))::text "
            + "WHEN (attribute::jsonb ->> 'parameter_5_protocol' = ?1 AND attribute::jsonb ->> 'parameter_5_primary_id' = ?2 AND attribute::jsonb ->> 'parameter_5_secondary_id' = ?3) THEN jsonb_set(attribute::jsonb, '{parameter_5_value}', to_jsonb(CAST(?4 AS text)))::text "
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


    /**
     * Inserts a measuring instrument record, or updates it when the identifier already exists.
     *
     * @param id the instrument identifier
     * @param type the instrument type
     * @param name the instrument name
     * @param description the instrument description
     * @param calculation_type the calculation type
     * @param attribute the attribute JSON
     * @param parameter the parameter JSON
     * @param category the instrument category
     * @param value the current value (set only on insert)
     * @param unit the measurement unit
     * @param tags the instrument tags
     * @param device_id the associated device identifier (set only on insert)
     * @param sensor_type the sensor type
     * @param show_on_map whether the instrument is shown on the map
     * @param show_on_scan whether the instrument is shown on scan
     * @param measuring_entity the measuring entity
     * @param sub_category the instrument sub-category
     * @param digital_twin_position the digital twin position
     * @param scale_type the scale type
     */
    @Modifying
    @Transactional
    // PG-port: ON DUPLICATE KEY -> ON CONFLICT (id) DO UPDATE SET (VALUES->EXCLUDED)
    @Query(value = "INSERT INTO measuring_instrument( id , type, name , description , calculation_type , attribute, parameter, category, value, unit, tags, device_id, sensor_type, sub_category, digital_twin_position, scale_type) VALUES (?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8, ?9, ?10, ?11, ?12, ?13, ?17, ?18, ?19) "
            + "ON CONFLICT (id) DO UPDATE SET type = EXCLUDED.type, name = EXCLUDED.name, description = EXCLUDED.description, calculation_type = EXCLUDED.calculation_type, attribute = EXCLUDED.attribute, parameter = EXCLUDED.parameter, category = EXCLUDED.category, unit = EXCLUDED.unit, tags = EXCLUDED.tags, sensor_type = EXCLUDED.sensor_type, show_on_map = ?14, show_on_scan = ?15, measuring_entity = ?16, sub_category = EXCLUDED.sub_category, digital_twin_position = EXCLUDED.digital_twin_position, scale_type = EXCLUDED.scale_type", nativeQuery = true)
    void upsertInstrument(String id, String type, String name, String description, String calculation_type,
                          String attribute, String parameter, String category, String value, String unit, String tags,
                          String device_id, String sensor_type, Integer show_on_map, Integer show_on_scan, String measuring_entity,
                          String sub_category, String digital_twin_position, String scale_type);


    /**
     * Returns the instruments associated with the given device.
     *
     * @param device_id the device identifier
     * @return the set of matching instruments
     */
    // NOT CONVERTED — stays native (PG-translation track): @NamedNativeQuery projection with multi-join (device/location/floor/building).
    @Query(nativeQuery = true)
    Set<MeasuringInstrumentDTO> getInstrumentByDeviceId(String device_id);


    /**
     * Deletes the instrument with the given identifier.
     *
     * @param instrument_id the instrument identifier
     */
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("DELETE FROM MeasuringInstrument mi WHERE mi.id = ?1")
    void deleteInstrumentById(String instrument_id);

    /**
     * Returns the instrument with the given identifier.
     *
     * @param id the instrument identifier
     * @return the matching instrument
     */
    // CONVERTED to JPQL + MapStruct: single-table projection (instrumentmapping subset);
    // load the entity, then map only the previously-projected columns via MapStruct.
    @Query("SELECT mi FROM MeasuringInstrument mi WHERE mi.id = :id")
    MeasuringInstrument findEntityById(@org.springframework.data.repository.query.Param("id") String id);

    default MeasuringInstrumentDTO getInstrumentByInstrumentId(String id) {
        MeasuringInstrument mi = findEntityById(id);
        return mi == null ? null : io.sclera.mapper.MeasuringInstrumentMapperHolder.MAPPER.toDto(mi);
    }


    /**
     * Updates the value and timestamp of the instrument with the given identifier.
     *
     * @param measuingInstrument_id the instrument identifier
     * @param value the new value
     * @param timestamp the new epoch timestamp
     */
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE MeasuringInstrument mi SET mi.value = ?2, mi.timestamp = ?3 WHERE mi.id = ?1")
    void updateInstrumentValueById(String measuingInstrument_id, String value, BigInteger timestamp);


    /**
     * Returns the identifiers of instruments having any parameter matching the given protocol and identifiers.
     *
     * @param protocol the parameter protocol to match
     * @param primary_id the parameter primary identifier to match
     * @param secondary_id the parameter secondary identifier to match
     * @return the matching instrument identifiers
     */
    // PG-port: JSON_EXTRACT(col,'$.k')=?N -> col::jsonb ->> 'k' = ?N (text comparison).
    //          attribute column is text; ::jsonb cast is on the column side (safe).
    //          Pattern validated with direct psql SELECT on jsonb literals.
    @Modifying
    @Transactional
    @Query(value = "SELECT DISTINCT id from measuring_instrument WHERE "
            + "(attribute::jsonb ->> 'parameter_1_protocol' = ?1 AND attribute::jsonb ->> 'parameter_1_primary_id' = ?2 AND attribute::jsonb ->> 'parameter_1_secondary_id' = ?3) OR "
            + "(attribute::jsonb ->> 'parameter_2_protocol' = ?1 AND attribute::jsonb ->> 'parameter_2_primary_id' = ?2 AND attribute::jsonb ->> 'parameter_2_secondary_id' = ?3) OR "
            + "(attribute::jsonb ->> 'parameter_3_protocol' = ?1 AND attribute::jsonb ->> 'parameter_3_primary_id' = ?2 AND attribute::jsonb ->> 'parameter_3_secondary_id' = ?3) OR "
            + "(attribute::jsonb ->> 'parameter_4_protocol' = ?1 AND attribute::jsonb ->> 'parameter_4_primary_id' = ?2 AND attribute::jsonb ->> 'parameter_4_secondary_id' = ?3) OR "
            + "(attribute::jsonb ->> 'parameter_5_protocol' = ?1 AND attribute::jsonb ->> 'parameter_5_primary_id' = ?2 AND attribute::jsonb ->> 'parameter_5_secondary_id' = ?3) ", nativeQuery = true)
    List<String> listIdByMeasuringParameter(String protocol, String primary_id, String secondary_id);

    /**
     * Returns the number of instruments associated with the given device.
     *
     * @param device_id the device identifier
     * @return the instrument count
     */
    //get measuring device count
    long countByDevice_Id(String device_id);

    /**
     * Synchronizes the full set of mutable fields of an instrument matched by type and identifier.
     *
     * @param name the new name
     * @param description the new description
     * @param calculation_type the new calculation type
     * @param attribute the new attribute JSON
     * @param parameter the new parameter JSON
     * @param category the new category
     * @param value the new value
     * @param unit the new unit
     * @param tags the new tags
     * @param timestamp the new epoch timestamp
     * @param type the instrument type to match
     * @param id the instrument identifier to match
     * @return the number of rows updated
     */
    //used for backend syncing
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE MeasuringInstrument mi SET mi.name = ?1, mi.description = ?2, mi.calculation_type = ?3, mi.attribute = ?4, mi.parameter = ?5, mi.category = ?6, mi.value = ?7, mi.unit = ?8, mi.tags = ?9, mi.timestamp = ?10 WHERE mi.type = ?11 AND mi.id = ?12")
    Integer syncMeasuringInstrument(String name, String description, String calculation_type, String attribute, String parameter, String category, String value, String unit, String tags, BigInteger timestamp, String type, String id);

    /**
     * Synchronizes an instrument's fields excluding its attribute JSON, matched by type and identifier.
     *
     * @param calculation_type the new calculation type
     * @param parameter the new parameter JSON
     * @param category the new category
     * @param unit the new unit
     * @param tags the new tags
     * @param type the instrument type to match
     * @param id the instrument identifier to match
     * @return the number of rows updated
     */
    //used for backend syncing
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE MeasuringInstrument mi SET mi.calculation_type = ?1, mi.parameter = ?2, mi.category = ?3, mi.unit = ?4, mi.tags = ?5 WHERE mi.type = ?6 AND mi.id = ?7")
    Integer syncMeasuringInstrumentExceptAttributes(String calculation_type, String parameter, String category, String unit, String tags, String type, String id);

    /**
     * Returns the distinct uppercase sensor types of monitored, map-visible instruments on the given floor.
     *
     * @param floorid the floor identifier, or "all" for every floor
     * @return the set of distinct sensor types
     */
    // NOT CONVERTED — stays native (PG-translation track): 4-table JOIN with DISTINCT(UPPER(...)) and integer-boolean mix (show_on_map=1, monitor=1); PG-compatible as-is.
    @Transactional
    @Query(value = "SELECT DISTINCT(UPPER(mi.sensor_type)) FROM measuring_instrument mi "
            + "JOIN device d ON mi.device_id = d.id AND d.monitor = 1 "
            + "JOIN location l ON d.location_id = l.id "
            + "JOIN floor f ON l.floor_id = f.id WHERE (?1 = 'all' or f.id = ?1) AND mi.sensor_type IS NOT NULL AND mi.show_on_map = 1", nativeQuery = true)
    Set<String> getUniqueSensorCategoryByFloor(String floorid);

    /**
     * Returns the sensors of a given category on the given floor.
     *
     * @param floorid the floor identifier
     * @param category the sensor category
     * @return the set of matching sensors
     */
    // NOT CONVERTED — stays native (PG-translation track): @NamedNativeQuery projection with CASE WHEN, COALESCE, multi-join.
    @Query(nativeQuery = true)
    Set<CategorySensorDTO> getSensorCategoryByFloor(String floorid, String category);

    /**
     * Returns the number of monitored, map-visible sensors of a given category on the given floor.
     *
     * @param floorid the floor identifier
     * @param category the sensor category
     * @return the matching sensor count
     */
    // NOT CONVERTED — stays native (PG-translation track): 4-table JOIN COUNT with integer-boolean mix (show_on_map=1, monitor=1); PG-compatible as-is.
    @Query(value = " SELECT COUNT(*) FROM measuring_instrument mi "
            + " LEFT JOIN device d ON mi.device_id = d.id "
            + " LEFT JOIN location l ON d.location_id = l.id "
            + " JOIN floor f ON l.floor_id = f.id WHERE f.id = ?1 AND mi.sensor_type = ?2 AND mi.show_on_map = 1  AND d.monitor = 1 ", nativeQuery = true)
    Integer getSensorCategoryByFloorCount(String floorid, String category);

    /**
     * Returns a paginated list of sensors of a given category on the given floor.
     *
     * @param floorid the floor identifier
     * @param category the sensor category
     * @param pagesize the maximum number of rows to return
     * @param offset the starting row offset
     * @return the matching sensors for the page
     */
    // NOT CONVERTED — stays native (PG-translation track): @NamedNativeQuery projection with CASE WHEN, COALESCE, LIMIT/OFFSET.
    @Query(nativeQuery = true)
    List<CategorySensorDTO> getSensorCategoryByFloorPagination(String floorid, String category, Integer pagesize, Integer offset);

    /**
     * Updates the alert flag of the instrument with the given identifier.
     *
     * @param measuring_instrument_id the instrument identifier
     * @param newAlert the new alert flag value
     */
    //Update measuring instrument Sensor Alert
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE MeasuringInstrument mi SET mi.alert = ?2 WHERE mi.id = ?1")
    void updateMeasuringInstrumentSensorAlert(String measuring_instrument_id, Boolean newAlert);

    /**
     * Returns the number of device-bound, monitored instruments matching the given alert state.
     *
     * @param alert the alert state to match
     * @return the matching instrument count
     */
    //Get MeasuringInstrument count for touchscreen
    @Query("SELECT COUNT(mi) FROM MeasuringInstrument mi WHERE mi.alert = ?1 AND mi.device IS NOT NULL AND mi.device.monitor = 1")
    Integer getMeasuringInstrumentAlertSensorCount(boolean alert);

    /**
     * Returns the detailed sensor record for the instrument with the given identifier.
     *
     * @param measuring_instrument_id the instrument identifier
     * @return the instrument sensor details
     */
    // NOT CONVERTED — stays native (PG-translation track): @NamedNativeQuery projection with complex multi-join, CASE WHEN x5, CAST(NULL AS varchar).
    //get measuring instrument sensor by id for all required platforms
    @Query(nativeQuery = true)
    MeasuringInstrumentDetailsDTO getMeasuringInstrumentSensorDetailsById(String measuring_instrument_id);

    /**
     * Updates the user data value of the instrument with the given identifier.
     *
     * @param measuring_instrument_id the instrument identifier
     * @param user_data_value the new user data value
     */
    //Update measuring instrument Sensor User Data Value
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE MeasuringInstrument mi SET mi.user_data_value = ?2 WHERE mi.id = ?1")
    void updateMeasuringinstrumentSensorUserDataValue(String measuring_instrument_id, String user_data_value);

    /**
     * Returns the device identifier associated with the given instrument.
     *
     * @param measuring_instrument_id the instrument identifier
     * @return the associated device identifier
     */
    // get device id by measuring instrument id
    @Query("SELECT mi.device.id FROM MeasuringInstrument mi WHERE mi.id = ?1")
    String getDeviceIdByMeasuringInstrumentSensorId(String measuring_instrument_id);

    /**
     * Returns the number of instruments on the given device matching the given alert state.
     *
     * @param device_id the device identifier
     * @param alert the alert state to match
     * @return the matching instrument count
     */
    // get measuring instrument count by device id based on alert status
    long countByDevice_IdAndAlert(String device_id, boolean alert);

    /**
     * Returns the current value of the instrument with the given identifier.
     *
     * @param measuring_instrument_id the instrument identifier
     * @return the current value
     */
    @Query("SELECT mi.value FROM MeasuringInstrument mi WHERE mi.id = ?1")
    String getMeasuringInstrumentSensorCurrentValue(String measuring_instrument_id);

    /**
     * Returns the instrument with the given identifier.
     *
     * @param measuring_instrument_id the instrument identifier
     * @return the matching instrument
     */
    // NOT CONVERTED — stays native (PG-translation track): @NamedNativeQuery projection with CASE WHEN, multi-join.
    @Query(nativeQuery = true)
    MeasuringInstrumentDTO getMeasuringInstrumentSensorById(String measuring_instrument_id);

    /**
     * Returns a paginated list of device-bound instruments matching a search key.
     *
     * @param searchkey the search key to match
     * @param pagesize the maximum number of rows to return
     * @param offset the starting row offset
     * @return the matching instruments for the page
     */
    // NOT CONVERTED — stays native (PG-translation track): @NamedNativeQuery projection with CONCAT_WS, CASE WHEN, LIMIT/OFFSET.
    @Query(nativeQuery = true)
    List<MeasuringInstrumentDTO> getAllMeasuringInstrumentDeviceByPagination(String searchkey, Integer pagesize, Integer offset);

    /**
     * Returns the sensors associated with the given device.
     *
     * @param device_id the device identifier
     * @return the matching sensors
     */
    // NOT CONVERTED — stays native (PG-translation track): @NamedNativeQuery projection with CASE WHEN x2, COALESCE, multi-join.
    @Query(nativeQuery = true)
    List<SensorDTO> getmeasuringInstrumentsByDeviceId(String device_id);

    /**
     * Returns a paginated set of analytics instruments filtered by category, search key, and report template.
     *
     * @param category the sensor category
     * @param searchkey the search key to match
     * @param pagesize the maximum number of rows to return
     * @param offset the starting row offset
     * @param report_template_id the report template identifier
     * @return the matching analytics sensors for the page
     */
    // NOT CONVERTED — stays native (PG-translation track): @NamedNativeQuery projection with CONCAT_WS, CASE WHEN x3, LIMIT/OFFSET, multi-join.
    @Query(nativeQuery = true)
    Set<AnalyticSensorDTO> getAnalyticsMeasuringInstruments(String category, String searchkey, Integer pagesize, Integer offset, String report_template_id);

    /**
     * Returns the alert message conditions for instruments belonging to the given devices.
     *
     * @param ids the device identifiers
     * @return the matching alert message conditions
     */
    // NOT CONVERTED — stays native (PG-translation track): @NamedNativeQuery projection joining conditions table; already PG-ported (boolean col = true).
    @Query(nativeQuery = true)
    List<ConditionsDTO> listMeasuringIntrumentDevicesAlertMessagesByDevice(List<String> ids);

    /**
     * Returns the alert details for the instrument with the given identifier.
     *
     * @param measuring_instrument_id the instrument identifier
     * @return the instrument alert details
     */
    // NOT CONVERTED — stays native (PG-translation track): @NamedNativeQuery projection with CASE WHEN, already PG-ported.
    @Query(nativeQuery = true)
    SensorAlertDTO getMeasuringInstrumentAlertDetails(String measuring_instrument_id);

    /**
     * Returns the sensors associated with the given device.
     *
     * @param deviceid the device identifier
     * @return the set of matching sensors
     */
    // NOT CONVERTED — stays native (PG-translation track): @NamedNativeQuery projection with CASE WHEN, COALESCE, already PG-ported.
    @Query(nativeQuery = true)
    Set<SensorDTO> getSensorByDeviceId(String deviceid);

    /**
     * Returns the sensors associated with the given location.
     *
     * @param locationid the location identifier
     * @return the set of matching sensors
     */
    // NOT CONVERTED — stays native (PG-translation track): @NamedNativeQuery projection with CASE WHEN, COALESCE, already PG-ported.
    @Query(nativeQuery = true)
    Set<SensorDTO> getSensorByLocationId(String locationid);

    /**
     * Updates the value, timestamp, and attribute JSON of the instrument with the given identifier.
     *
     * @param measuingInstrument_id the instrument identifier
     * @param value the new value
     * @param timestamp the new epoch timestamp
     * @param attribute the new attribute JSON
     */
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE MeasuringInstrument mi SET mi.value = ?2, mi.timestamp = ?3, mi.attribute = ?4 WHERE mi.id = ?1")
    void updateInstrumentValueAndAttributeById(String measuingInstrument_id, String value, BigInteger timestamp, String attribute);


    /**
     * Returns all Daintree-sourced measuring instruments.
     *
     * @return the Daintree instruments
     */
    // NOT CONVERTED — stays native (PG-translation track): @NamedNativeQuery projection joining measuring_instrument_attributes on protocol='daintree'.
    @Query(nativeQuery = true)
    List<MeasuringInstrumentDTO> getDaintreeMeasuringInstruments();

    /**
     * Returns a paginated list of sensors of a given category at the given location.
     *
     * @param locationid the location identifier
     * @param category the sensor category
     * @param pagesize the maximum number of rows to return
     * @param offset the starting row offset
     * @return the matching sensors for the page
     */
    // NOT CONVERTED — stays native (PG-translation track): @NamedNativeQuery projection with CASE WHEN, COALESCE, LIMIT/OFFSET.
    @Query(nativeQuery = true)
    List<CategorySensorDTO> getSensorCategoryByLocationPagination(String locationid, String category, Integer pagesize, Integer offset);

    /**
     * Returns the number of monitored, map-visible sensors of a given category at the given location.
     *
     * @param locationid the location identifier
     * @param category the sensor category
     * @return the matching sensor count
     */
    // NOT CONVERTED — stays native (PG-translation track): 4-table JOIN COUNT with integer-boolean mix (show_on_map=1, monitor=1); PG-compatible as-is.
    @Query(value = " SELECT COUNT(*) FROM measuring_instrument mi "
            + " LEFT JOIN device d ON mi.device_id = d.id "
            + " LEFT JOIN location l ON d.location_id = l.id "
            + " JOIN floor f ON l.floor_id = f.id WHERE l.id = ?1 AND mi.sensor_type = ?2 AND mi.show_on_map = 1  AND d.monitor = 1 ", nativeQuery = true)
    Integer getSensorCategoryByLocationCount(String locationid, String category);

    /**
     * Returns the analytics instrument matching the given identifier, search key, and report attribute.
     *
     * @param measuring_instrument_id the instrument identifier
     * @param searchkey the search key to match
     * @param report_attribute_id the report attribute identifier
     * @return the matching analytics sensor
     */
    // NOT CONVERTED — stays native (PG-translation track): @NamedNativeQuery projection with CASE WHEN x2, CONCAT_WS, multi-join.
    @Query(nativeQuery = true)
    AnalyticSensorDTO getMeasuringInstrumentsByTemplateId(String measuring_instrument_id, String searchkey, String report_attribute_id);

    /**
     * Returns whether the given instrument is tagged to the given location.
     *
     * @param measuring_instrument_id the instrument identifier
     * @param location_id the location identifier
     * @return the number of matching tag rows
     */
    // NOT CONVERTED — stays native (PG-translation track): queries the @ManyToMany join table directly; no entity for the join row.
    @Query(value = "SELECT COUNT(*) FROM measuring_instrument_location WHERE measuring_instrument_id = ?1 AND location_id = ?2", nativeQuery = true)
    int checkMeasuringInstrumentsExists(String measuring_instrument_id, String location_id);

    /**
     * Tags the given instrument to the given location.
     *
     * @param measuring_instrument_id the instrument identifier
     * @param location_id the location identifier
     */
    // NOT CONVERTED — stays native (PG-translation track): plain INSERT on @ManyToMany join table; no entity for the join row.
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO measuring_instrument_location (measuring_instrument_id , location_id) VALUES (?1,?2)", nativeQuery = true)
    void upsertMeasuringInstrumentLocations(String measuring_instrument_id, String location_id);

    /**
     * Removes the tag linking the given instrument to the given location.
     *
     * @param id the instrument identifier
     * @param location_id the location identifier
     */
    // NOT CONVERTED — stays native (PG-translation track): plain DELETE on @ManyToMany join table; no entity for the join row.
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM measuring_instrument_location WHERE measuring_instrument_id = ?1 AND location_id = ?2", nativeQuery = true)
    void untagLocationsFromMeasuringInstruments(String id, String location_id);

    /**
     * Removes all instrument tags for the given location.
     *
     * @param location_id the location identifier
     */
    // NOT CONVERTED — stays native (PG-translation track): plain DELETE on @ManyToMany join table; no entity for the join row.
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM measuring_instrument_location WHERE location_id = ?1", nativeQuery = true)
    void deleteMeasuringIntrumentLocationsByLocationId(String location_id);

    /**
     * Returns the integration sensors associated with the given location.
     *
     * @param locationid the location identifier
     * @return the set of matching integration sensors
     */
    // NOT CONVERTED — stays native (PG-translation track): @NamedNativeQuery projection with CASE WHEN, already PG-ported.
    @Query(nativeQuery = true)
    Set<SensorDTO> getIntegrationSensorByLocationId(String locationid);

    /**
     * Returns all Siemens-sourced measuring instruments.
     *
     * @return the Siemens instruments
     */
    // NOT CONVERTED — stays native (PG-translation track): @NamedNativeQuery projection joining measuring_instrument_attributes on protocol='siemens'.
    @Query(nativeQuery = true)
    List<MeasuringInstrumentDTO> getSiemensMeasuringInstruments();

    /**
     * Returns the measuring instruments associated with the given device.
     *
     * @param deviceId the device identifier
     * @return the matching instruments
     */
    // NOT CONVERTED — stays native (PG-translation track): @NamedNativeQuery projection joining measuring_instrument_attributes on protocol='siemens'.
    @Query(nativeQuery = true)
    List<MeasuringInstrumentDTO> getMeasuringInstrumentsByDeviceId(String deviceId);

    /**
     * Reassigns instruments from one device identifier to another.
     *
     * @param device_id the new device identifier
     * @param existing_device_id the current device identifier to match
     */
    // NOT CONVERTED — stays native (PG-translation track): device_id is the FK column backing
    // the @ManyToOne Device relation; JPQL cannot SET a relation column from a scalar id.
    @Modifying
    @Transactional
    @Query(value = "UPDATE measuring_instrument SET device_id = ?1 WHERE (device_id IS NOT NULL) AND device_id = ?2", nativeQuery = true)
    void updateMeasuringInstrumentDeviceId(String device_id, String existing_device_id);

    /**
     * Updates the attribute JSON of the instrument with the given identifier.
     *
     * @param measuingInstrument_id the instrument identifier
     * @param attribute the new attribute JSON
     */
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE MeasuringInstrument mi SET mi.attribute = ?2 WHERE mi.id = ?1")
    void updateInstrumentAttributeById(String measuingInstrument_id, String attribute);


    /**
     * Updates the value of the indexed parameter within an instrument's attribute JSON.
     *
     * @param parameterIndex the 1-based parameter index
     * @param value the new parameter value
     * @param id the instrument identifier
     * @return the number of rows updated
     */
    // PG-port: JSON_SET(col, CONCAT('$.parameter_',?1,'_value'), ?2) ->
    //          jsonb_set(col::jsonb, ARRAY['parameter_' || CAST(?1 AS text) || '_value'], to_jsonb(CAST(?2 AS text)))::text.
    //          Dynamic path built with ARRAY[...] expression; CAST used for param-adjacent conversions (no ?N::type).
    //          Pattern validated with direct psql SELECT on jsonb literals.
    @Modifying
    @Transactional
    @Query(value = "UPDATE measuring_instrument SET attribute = " +
            "  jsonb_set(attribute::jsonb, ARRAY['parameter_' || CAST(?1 AS text) || '_value'], to_jsonb(CAST(?2 AS text)))::text " +
            "WHERE id = ?3", nativeQuery = true)
    Integer updateMeasuringInstrumentParametersValuesByIds(int parameterIndex, String value, String id);


    /**
     * Clears the digital twin position of all instruments on the given device.
     *
     * @param device_id the device identifier
     */
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE MeasuringInstrument mi SET mi.digital_twin_position = NULL WHERE mi.device.id = ?1")
    void deleteDigitalTwinPositions(String device_id);


    /**
     * Updates the primary, secondary, and tertiary identifiers of the indexed parameter within an instrument's attribute JSON.
     *
     * @param parameterIndex the 1-based parameter index
     * @param primaryId the new primary identifier
     * @param secondaryId the new secondary identifier
     * @param tertiaryId the new tertiary identifier
     * @param id the instrument identifier
     */
    // PG-port: Nested JSON_SET with dynamic CONCAT paths ->
    //          jsonb_set(jsonb_set(jsonb_set(col::jsonb, ARRAY[...primary...], to_jsonb(CAST(?2 AS text))),
    //                                                    ARRAY[...secondary...], to_jsonb(CAST(?3 AS text))),
    //                                                    ARRAY[...tertiary...], to_jsonb(CAST(?4 AS text)))::text.
    //          CAST used for all param-adjacent conversions (no ?N::type).
    //          Pattern validated with direct psql SELECT on jsonb literals (3-level nested jsonb_set confirmed).
    @Modifying
    @Transactional
    @Query(value = "UPDATE measuring_instrument SET attribute = " +
            "jsonb_set(" +
            "jsonb_set(" +
            "jsonb_set(attribute::jsonb, ARRAY['parameter_' || CAST(?1 AS text) || '_primary_id'], to_jsonb(CAST(?2 AS text))), " +
            "ARRAY['parameter_' || CAST(?1 AS text) || '_secondary_id'], to_jsonb(CAST(?3 AS text))), " +
            "ARRAY['parameter_' || CAST(?1 AS text) || '_tertiary_id'], to_jsonb(CAST(?4 AS text)))::text " +
            "WHERE id = ?5", nativeQuery = true)
    void updateMeasuringInstrumentPrimaryAndSecondaryId(int parameterIndex, String primaryId, String secondaryId, String tertiaryId, String id);


    /**
     * Returns all measuring instruments.
     *
     * @return the set of all instruments
     */
    // NOT CONVERTED — stays native (PG-translation track): @NamedNativeQuery projection (measuringInstrumentAttributeMapping — attribute/scale_type/sensor_type/sub_category subset).
    @Query(nativeQuery = true)
    Set<MeasuringInstrumentDTO> getAllInstrument();


    /**
     * Returns the identifiers of instruments having an attribute matching the given protocol and any of the given primary identifiers.
     *
     * @param protocol the attribute protocol to match
     * @param primaryIds the candidate primary identifiers
     * @return the matching instrument identifiers
     */
    @Query("SELECT mi.id FROM MeasuringInstrument_Attributes mia JOIN mia.measuring_instrument mi WHERE mia.protocol = ?1 AND mia.primary_id IN ?2")
    Set<String> getMeasuringInstrumentIdsByProtocolAndPrimaryIds(String protocol, Set<String> primaryIds);


    /**
     * Updates the scale type, sensor type, and sub-category of the instrument with the given identifier.
     *
     * @param ScaleType the new scale type
     * @param SensorType the new sensor type
     * @param subCategory the new sub-category
     * @param id the instrument identifier
     */
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE MeasuringInstrument mi SET mi.scale_type = ?1, mi.sensor_type = ?2, mi.sub_category = ?3 WHERE mi.id = ?4")
    void updateScaleTypeAndSensorTypeById(String ScaleType, String SensorType, String subCategory, String id);


    /**
     * Returns the number of instruments of the given type, or all instruments when "all" is supplied.
     *
     * @param type the instrument type to match, or "all" for every type
     * @return the matching instrument count
     */
    @Query("SELECT COUNT(mi) FROM MeasuringInstrument mi WHERE ?1 = 'all' OR mi.type = ?1")
    Integer getMeasuringInstrumentCountByType(String type);


    /**
     * Returns the total number of manual attributes across all measuring instruments.
     *
     * @return the manual attribute count
     */
    @Query("SELECT COUNT(mia) FROM MeasuringInstrument_Attributes mia WHERE mia.type = 'manual'")
    int getTotalManualAttributesCountofMeasuringInstruments();


}
