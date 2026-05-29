package io.sclera.inspection.repository;

import io.sclera.inspection.model.RecordChecklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import jakarta.transaction.Transactional;

import java.util.Collection;
import java.util.List;

@Repository
public interface RecordChecklistRepository extends JpaRepository<RecordChecklist, String> {

    @Query("SELECT COUNT(e) FROM RecordChecklist e WHERE e.device_id = :deviceId")
    long countByDeviceId(@Param("deviceId") String deviceId);

    @Query("SELECT e FROM RecordChecklist e WHERE e.device_id = :deviceId")
    List<RecordChecklist> findByDeviceId(@Param("deviceId") String deviceId);

    @Modifying
    @Transactional
    @Query("DELETE FROM RecordChecklist e WHERE e.device_id = :deviceId")
    int deleteByDeviceId(@Param("deviceId") String deviceId);

    @Modifying
    @Transactional
    @Query("UPDATE RecordChecklist e SET e.device_id = :newId WHERE e.device_id = :oldId AND e.id IN :ids")
    int reassignDeviceIdForIds(@Param("ids") Collection<String> ids, @Param("oldId") String oldId, @Param("newId") String newId);

    @Modifying
    @Transactional
    @Query("UPDATE RecordChecklist e SET e.device_id = :newId WHERE e.id IN :ids")
    int reassignDeviceIdForIdsNoOld(@Param("ids") Collection<String> ids, @Param("newId") String newId);

    @Modifying
    @Transactional
    @Query("UPDATE RecordChecklist e SET e.device_id = null, e.is_removed = 1 WHERE e.id IN :ids")
    int softDeleteByIds(@Param("ids") Collection<String> ids);

    @Modifying
    @Transactional
    @Query("UPDATE RecordChecklist e SET e.location_id = null, e.is_removed = 1 WHERE e.location_id IN :locationIds")
    int softDeleteByLocationIds(@Param("locationIds") Collection<String> locationIds);

    @Modifying
    @Transactional
    @Query("DELETE FROM RecordChecklist e WHERE e.location_id = :locationId")
    int deleteByLocationId(@Param("locationId") String locationId);

    @Query("SELECT e.id FROM RecordChecklist e WHERE e.location_id = :locationId")
    List<String> findIdsByLocationId(@Param("locationId") String locationId);

    @Query("SELECT e.image_urls FROM RecordChecklist e WHERE e.device_id = :deviceId AND e.image_urls IS NOT NULL")
    List<String> findImageUrlsByDeviceId(@Param("deviceId") String deviceId);

    @Query("SELECT e.status FROM RecordChecklist e WHERE e.device_id = :deviceId AND e.status IS NOT NULL")
    List<String> findStatusesByDeviceId(@Param("deviceId") String deviceId);

    @Query("SELECT COUNT(e) FROM RecordChecklist e WHERE e.device_id = :deviceId AND e.status = :status")
    long countByDeviceIdAndStatus(@Param("deviceId") String deviceId, @Param("status") String status);

    @Query("SELECT e.status FROM RecordChecklist e WHERE e.location_id = :locationId AND e.status IS NOT NULL")
    List<String> findStatusesByLocationId(@Param("locationId") String locationId);

    @Query("SELECT COUNT(e) FROM RecordChecklist e WHERE e.location_id = :locationId AND e.status = :status")
    long countByLocationIdAndStatus(@Param("locationId") String locationId, @Param("status") String status);

    @Query("SELECT e.id FROM RecordChecklist e WHERE " +
            "(:buildingIds IS NULL OR e.building_id IN :buildingIds) AND " +
            "(:floorIds IS NULL OR e.floor_id IN :floorIds) AND " +
            "(:locationIds IS NULL OR e.location_id IN :locationIds)")
    List<String> findIdsByBuildingsFloorsLocations(@Param("buildingIds") Collection<String> buildingIds,
                                                    @Param("floorIds") Collection<String> floorIds,
                                                    @Param("locationIds") Collection<String> locationIds);

    @Modifying
    @Transactional
    @Query("UPDATE RecordChecklist e SET e.updated_email = :email")
    int stampUpdatedEmail(@Param("email") String email);
}