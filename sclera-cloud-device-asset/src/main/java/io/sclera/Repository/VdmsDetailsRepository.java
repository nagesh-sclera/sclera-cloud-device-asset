package io.sclera.Repository;

import io.sclera.dto.touchscreen.VdmsDetailsDTO;
import io.sclera.models.VdmsDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;

@Repository
public interface VdmsDetailsRepository extends JpaRepository<VdmsDetails, String> {

    //update vdms details
    @Modifying
    @Transactional
    // PG-port: ON DUPLICATE KEY -> ON CONFLICT (id) DO UPDATE SET (VALUES->EXCLUDED); VALUE->VALUES
    @Query(value = "INSERT INTO vdms_details (id, weather_city, weather_country_code,weather_latitude,weather_longitude,weather_data,weather_zip_code, weather_units, vdms_id) VALUES (?1, ?2, ?3, ?4,?5,?6,?7,?8,?9) " +
            "ON CONFLICT (id) DO UPDATE SET weather_city=EXCLUDED.weather_city, weather_country_code=EXCLUDED.weather_country_code, weather_latitude=EXCLUDED.weather_latitude, weather_longitude=EXCLUDED.weather_longitude, weather_data=EXCLUDED.weather_data, weather_zip_code=EXCLUDED.weather_zip_code, weather_units=EXCLUDED.weather_units", nativeQuery = true)
    void upsertWeatherData(String id, String weather_city, String weather_country_code, String weather_latitude, String weather_longitude, String weather_data,String weather_zip_code, String weather_units, String vdmsid);

    //get weather details
    @Query(nativeQuery = true)
    VdmsDetailsDTO getWeatherData();

    //get vdms details id
    @Transactional
    @Query(value = "SELECT id from vdms_details", nativeQuery = true)
    String getVdmsDetailsId();

    //update vdms details
    @Modifying
    @Transactional
    // PG-port: ON DUPLICATE KEY -> ON CONFLICT (id) DO UPDATE SET (VALUES->EXCLUDED); VALUE->VALUES
    @Query(value = "INSERT INTO vdms_details (id, layout_data, vdms_id) VALUES (?1, ?2, ?3) " +
            "ON CONFLICT (id) DO UPDATE SET layout_data=EXCLUDED.layout_data", nativeQuery = true)
    void upsertVdmsLayoutData(String id, String layout_data, String vdmsid);


    //get vdms layout data
    @Query(nativeQuery = true)
    VdmsDetailsDTO getVdmsLayoutData();

    // get device custom fields data
    @Query(nativeQuery = true)
    VdmsDetailsDTO getVdmsDeviceCustomFields();

    @Modifying
    @Transactional
    // PG-port: ON DUPLICATE KEY -> ON CONFLICT (id) DO UPDATE SET (VALUES->EXCLUDED); VALUE->VALUES
    @Query(value = "INSERT INTO vdms_details (id, device_custom_fields, vdms_id) VALUES (?1, ?2, ?3) ON CONFLICT (id) DO UPDATE SET device_custom_fields = EXCLUDED.device_custom_fields", nativeQuery = true)
    void upsertVdmsDeviceCustomFields(String id, String device_custom_fields, String vdmsid);

    //update corrigo layout details
    @Modifying
    @Transactional
    // PG-port: ON DUPLICATE KEY -> ON CONFLICT (id) DO UPDATE SET (VALUES->EXCLUDED); VALUE->VALUES
    @Query(value = "INSERT INTO vdms_details (id, corrigo_layout_data, vdms_id) VALUES (?1, ?2, ?3) " +
            "ON CONFLICT (id) DO UPDATE SET corrigo_layout_data = EXCLUDED.corrigo_layout_data", nativeQuery = true)
    void upsertCorrigoLayoutData(String id, String corrigo_layout_data, String vdmsid);


}
