package io.sclera.inspection.repository;

import io.sclera.inspection.model.GlobalInspectionRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import jakarta.transaction.Transactional;

import java.util.Collection;
import java.util.List;

@Repository
public interface GlobalInspectionRelationRepository extends JpaRepository<GlobalInspectionRelation, String> {

    @Query("SELECT COUNT(e) FROM GlobalInspectionRelation e WHERE e.device_id = :deviceId")
    long countByDeviceId(@Param("deviceId") String deviceId);

    @Query("SELECT e FROM GlobalInspectionRelation e WHERE e.device_id = :deviceId")
    List<GlobalInspectionRelation> findByDeviceId(@Param("deviceId") String deviceId);

    @Modifying
    @Transactional
    @Query("DELETE FROM GlobalInspectionRelation e WHERE e.device_id = :deviceId")
    int deleteByDeviceId(@Param("deviceId") String deviceId);

    @Modifying
    @Transactional
    @Query("UPDATE GlobalInspectionRelation e SET e.device_id = :newId WHERE e.device_id = :oldId")
    int reassignDeviceId(@Param("oldId") String oldId, @Param("newId") String newId);

    @Modifying
    @Transactional
    @Query("UPDATE GlobalInspectionRelation e SET e.device_id = null, e.is_removed = 1 WHERE e.id IN :ids")
    int softDeleteByIds(@Param("ids") Collection<String> ids);

    @Modifying
    @Transactional
    @Query("UPDATE GlobalInspectionRelation e SET e.location_id = null, e.is_removed = 1 WHERE e.location_id IN :locationIds")
    int softDeleteByLocationIds(@Param("locationIds") Collection<String> locationIds);

    @Modifying
    @Transactional
    @Query("UPDATE GlobalInspectionRelation e SET e.updated_email = :email")
    int stampUpdatedEmail(@Param("email") String email);
}