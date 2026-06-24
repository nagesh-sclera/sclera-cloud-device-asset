package io.sclera.repository;

import io.sclera.dto.CategoryDTO;
import io.sclera.model.SensorCategory;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.List;

@Repository
public interface SensorCategoryRepository extends JpaRepository<SensorCategory, String> {
    @Query(nativeQuery = true)
    List<CategoryDTO> getAllSensorCategory(String searchKey, String sort, int pageSize, int offset);

    @Query(value = "SELECT COUNT(*) FROM sensor_category WHERE name = ?1", nativeQuery = true)
    Integer checkSensorCategoryByName(String name);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO sensor_category (id, name, icon_url,display_name, creation_timestamp) VALUES (?1, ?2, ?3, ?4, ?5)", nativeQuery = true)
    void addSensorCategory(String id, String name, String iconUrl, String displayName, BigInteger creationTimestamp);

    @Query(value = "SELECT COUNT(*) FROM sensor_category WHERE id != ?1 AND name = ?2", nativeQuery = true)
    Integer checkSensorCategoryByIdAndName(String sensorCategoryId, String name);

    @Query(value = "SELECT name FROM sensor_category WHERE id = ?1", nativeQuery = true)
    String getNameById(String sensorCategoryId);

    @Query(value = "SELECT icon_url FROM sensor_category WHERE id = ?1", nativeQuery = true)
    String getImageUrlById(String sensorCategoryId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE sensor_category SET name = ?1, icon_url = ?2, display_name = ?3 WHERE id = ?4", nativeQuery = true)
    void updateSensorCategoryById(String name, String iconUrl, String displayName, String sensorCategoryId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE sensor_category SET icon_url = ?1 WHERE id = ?2", nativeQuery = true)
    void updateSensorCategoryIconById(String url, String sensorCategoryId);

    @Query(value = "SELECT icon_url FROM sensor_category WHERE icon_url != NULL AND id IN ?1", nativeQuery = true)
    List<String> getImageUrlsByIds(List<String> sensorCategoryIds);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM sensor_category WHERE id IN ?1", nativeQuery = true)
    void deleteSensorCategoryByIds(List<String> sensorCategoryIds);

    @Query(value = "SELECT id FROM sensor_category WHERE display_name = ?1", nativeQuery = true)
    String getSensorCategoryIdByDisplayName(String sensorCategoryGroupName);

    @Query(nativeQuery = true)
    List<CategoryDTO> getAllSensorName();

    @Query(value = "SELECT icon_url FROM sensor_category WHERE name = ?1",nativeQuery = true)
    String getIconUrlByCategoryName(String category);
}
