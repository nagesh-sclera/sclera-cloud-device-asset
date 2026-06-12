package io.sclera.Repository;

import io.sclera.dto.touchscreen.VdmsDetailsDTO;
import io.sclera.models.VdmsDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;

/**
 * Manages persistence and querying of {@link VdmsDetails} entities.
 */
@Repository
public interface VdmsDetailsRepository extends JpaRepository<VdmsDetails, String> {

    /**
     * Inserts or updates the weather configuration columns of a VDMS details row.
     *
     * @param id the VDMS details primary key
     * @param weather_city the configured weather city
     * @param weather_country_code the configured weather country code
     * @param weather_latitude the configured weather latitude
     * @param weather_longitude the configured weather longitude
     * @param weather_data the cached weather payload
     * @param weather_zip_code the configured weather zip code
     * @param weather_units the configured weather units
     * @param vdmsid the associated VDMS identifier
     */
    @Modifying
    @Transactional
    // NOT CONVERTED — stays native: plain INSERT … ON CONFLICT upsert already valid PostgreSQL
    @Query(value = "INSERT INTO vdms_details (id, weather_city, weather_country_code,weather_latitude,weather_longitude,weather_data,weather_zip_code, weather_units, vdms_id) VALUES (?1, ?2, ?3, ?4,?5,?6,?7,?8,?9) " +
            "ON CONFLICT (id) DO UPDATE SET weather_city=EXCLUDED.weather_city, weather_country_code=EXCLUDED.weather_country_code, weather_latitude=EXCLUDED.weather_latitude, weather_longitude=EXCLUDED.weather_longitude, weather_data=EXCLUDED.weather_data, weather_zip_code=EXCLUDED.weather_zip_code, weather_units=EXCLUDED.weather_units", nativeQuery = true)
    void upsertWeatherData(String id, String weather_city, String weather_country_code, String weather_latitude, String weather_longitude, String weather_data,String weather_zip_code, String weather_units, String vdmsid);

    /**
     * Returns the stored weather details.
     *
     * @return the weather details projection
     */
    @Query("SELECT new io.sclera.dto.touchscreen.VdmsDetailsDTO(" +
            "vd.id, vd.weather_city, vd.weather_zip_code, vd.weather_country_code, " +
            "vd.weather_latitude, vd.weather_longitude, vd.weather_data, vd.weather_units, vd.vdms.id) " +
            "FROM VdmsDetails vd")
    VdmsDetailsDTO getWeatherData();

    /**
     * Returns the VDMS details row identifier.
     *
     * @return the VDMS details id
     */
    @Transactional
    @Query("SELECT vd.id FROM VdmsDetails vd")
    String getVdmsDetailsId();

    /**
     * Inserts or updates the layout data of a VDMS details row.
     *
     * @param id the VDMS details primary key
     * @param layout_data the layout payload to persist
     * @param vdmsid the associated VDMS identifier
     */
    @Modifying
    @Transactional
    // NOT CONVERTED — stays native: plain INSERT … ON CONFLICT upsert already valid PostgreSQL
    @Query(value = "INSERT INTO vdms_details (id, layout_data, vdms_id) VALUES (?1, ?2, ?3) " +
            "ON CONFLICT (id) DO UPDATE SET layout_data=EXCLUDED.layout_data", nativeQuery = true)
    void upsertVdmsLayoutData(String id, String layout_data, String vdmsid);


    /**
     * Returns the stored VDMS layout data.
     *
     * @return the VDMS layout data projection
     */
    @Query("SELECT new io.sclera.dto.touchscreen.VdmsDetailsDTO(" +
            "vd.id, vd.layout_data, vd.vdms.id, vd.corrigo_layout_data) " +
            "FROM VdmsDetails vd")
    VdmsDetailsDTO getVdmsLayoutData();

    /**
     * Returns the stored VDMS device custom fields data.
     *
     * @return the device custom fields projection
     */
    @Query("SELECT new io.sclera.dto.touchscreen.VdmsDetailsDTO(vd.id, vd.device_custom_fields) " +
            "FROM VdmsDetails vd")
    VdmsDetailsDTO getVdmsDeviceCustomFields();

    /**
     * Inserts or updates the device custom fields of a VDMS details row.
     *
     * @param id the VDMS details primary key
     * @param device_custom_fields the device custom fields payload to persist
     * @param vdmsid the associated VDMS identifier
     */
    @Modifying
    @Transactional
    // NOT CONVERTED — stays native: plain INSERT … ON CONFLICT upsert already valid PostgreSQL
    @Query(value = "INSERT INTO vdms_details (id, device_custom_fields, vdms_id) VALUES (?1, ?2, ?3) ON CONFLICT (id) DO UPDATE SET device_custom_fields = EXCLUDED.device_custom_fields", nativeQuery = true)
    void upsertVdmsDeviceCustomFields(String id, String device_custom_fields, String vdmsid);

    /**
     * Inserts or updates the Corrigo layout data of a VDMS details row.
     *
     * @param id the VDMS details primary key
     * @param corrigo_layout_data the Corrigo layout payload to persist
     * @param vdmsid the associated VDMS identifier
     */
    @Modifying
    @Transactional
    // NOT CONVERTED — stays native: plain INSERT … ON CONFLICT upsert already valid PostgreSQL
    @Query(value = "INSERT INTO vdms_details (id, corrigo_layout_data, vdms_id) VALUES (?1, ?2, ?3) " +
            "ON CONFLICT (id) DO UPDATE SET corrigo_layout_data = EXCLUDED.corrigo_layout_data", nativeQuery = true)
    void upsertCorrigoLayoutData(String id, String corrigo_layout_data, String vdmsid);


}
