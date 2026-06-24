package io.sclera.repository;

import io.sclera.dto.SubCategoryDTO;
import io.sclera.model.SensorSubcategory;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.List;

@Repository
public interface SensorSubcategoryRepository extends JpaRepository<SensorSubcategory, String> {

    @Query(nativeQuery = true)
    List<SubCategoryDTO> getSensorSubCategoryBySensorCategoryId(String sensorCategoryId, String searchKey, String sort);

    @Query(value = "SELECT COUNT(*) FROM sensor_subcategory WHERE name = ?1 AND sensor_category_id = ?2", nativeQuery = true)
    Integer checkSensorSubCategoryByNameAndCategoryId(String name, String sensorCategoryId);


    @Modifying
    @Transactional
    @Query(value = "INSERT INTO sensor_subcategory (id, name, icon_url, display_name, creation_timestamp, sensor_category_id) " +
            "VALUES (?1, ?2, ?3, ?4, ?5, ?6)", nativeQuery = true)
    void addSensorSubCategoryByCategoryId(String sensorSubCategoryId, String name, String iconUrl, String displayName, BigInteger creationTimestamp, String sensorCategoryId);

    @Query(value = "SELECT COUNT(*) FROM sensor_subcategory WHERE ((id != ?1 AND name = ?2) AND sensor_category_id = ?3)", nativeQuery = true)
    Integer checkSensorSubCategoryByIdAndName(String sensorSubCategoryId, String name, String sensorCategoryId);

    @Query(value = "SELECT name FROM sensor_subcategory WHERE id = ?1", nativeQuery = true)
    String getNameById(String sensorSubCategoryId);

    @Query(value = "SELECT icon_url FROM sensor_subcategory WHERE id = ?1", nativeQuery = true)
    String getImageUrlById(String sensorSubCategoryId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE sensor_subcategory SET name = ?1, icon_url = ?2, display_name = ?3 WHERE id = ?4 AND sensor_category_id = ?5", nativeQuery = true)
    void updateSubCategoryByCategoryAndSubcategoryId(String name, String iconUrl, String displayName,
                                                     String sensorSubCategoryId, String sensorCategoryId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE sensor_subcategory SET icon_url = ?1 WHERE id = ?2 AND sensor_category_id = ?3", nativeQuery = true)
    void updateSensorSubCategoryIconById(String url, String sensorSubCategoryId, String sensorCategoryId);


    @Query(value = "SELECT icon_url FROM sensor_subcategory WHERE icon_url != NULL AND id IN ?1", nativeQuery = true)
    List<String> getSensorImageUrlsByIds(List<String> sensorSubCategoryIds);


    @Modifying
    @Transactional
    @Query(value = "DELETE FROM sensor_subcategory WHERE sensor_category_id = ?1 AND id IN ?2", nativeQuery = true)
    void deleteSensorSubcategoryBySensorCategoryAndSensorSubcategoryIds(String sensorCategoryId, List<String> sensorSubCategoryIds);

    @Query(value = "SELECT SUBSTRING_INDEX(icon_url, '/', -1) FROM sensor_subcategory WHERE sensor_category_id = ?1", nativeQuery = true)
    List<String> getImageUrlBySensorCategoryId(String sensorCategoryId);


    @Modifying
    @Transactional
    @Query(value = "DELETE FROM sensor_subcategory WHERE sensor_category_id = ?1", nativeQuery = true)
    void deleteSensorSubcategoryByCategoryId(String sensorCategoryId);

    @Query(nativeQuery = true)
    List<SubCategoryDTO> getSensorSubCategoryBySensorCategoryName(String sensorCategoryName, String key, String sort);
}
