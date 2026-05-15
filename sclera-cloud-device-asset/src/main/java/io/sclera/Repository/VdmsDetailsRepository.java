package io.sclera.Repository;

import io.sclera.dto.touchscreen.VdmsDetailsDTO;
import io.sclera.models.VdmsDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository
public interface VdmsDetailsRepository extends JpaRepository<VdmsDetails, String> {

    //update vdms details
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO vdms_details (id, weather_city, weather_country_code,weather_latitude,weather_longitude,weather_data,weather_zip_code, weather_units, vdms_id) VALUE (?1, ?2, ?3, ?4,?5,?6,?7,?8,?9)" +
            "ON DUPLICATE KEY UPDATE weather_city=?2, weather_country_code=?3,weather_latitude=?4,weather_longitude=?5,weather_data=?6, weather_zip_code=?7, weather_units=?8", nativeQuery = true)
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
    @Query(value = "INSERT INTO vdms_details (id, layout_data, vdms_id) VALUE (?1, ?2, ?3)" +
            "ON DUPLICATE KEY UPDATE layout_data=?2", nativeQuery = true)
    void upsertVdmsLayoutData(String id, String layout_data, String vdmsid);


    //get vdms layout data
    @Query(nativeQuery = true)
    VdmsDetailsDTO getVdmsLayoutData();

    // get device custom fields data
    @Query(nativeQuery = true)
    VdmsDetailsDTO getVdmsDeviceCustomFields();

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO vdms_details (id, device_custom_fields, vdms_id) VALUE (?1, ?2, ?3) ON DUPLICATE KEY UPDATE device_custom_fields = ?2", nativeQuery = true)
    void upsertVdmsDeviceCustomFields(String id, String device_custom_fields, String vdmsid);

    //update corrigo layout details
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO vdms_details (id, corrigo_layout_data, vdms_id) VALUE (?1, ?2, ?3)" +
            "ON DUPLICATE KEY UPDATE corrigo_layout_data = ?2", nativeQuery = true)
    void upsertCorrigoLayoutData(String id, String corrigo_layout_data, String vdmsid);


}
