package io.sclera.inspection.repository;

import io.sclera.inspection.model.InspectionRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import jakarta.transaction.Transactional;

import java.util.Collection;
import java.util.List;

@Repository
public interface InspectionRecordRepository extends JpaRepository<InspectionRecord, String> {

    @Query("SELECT COUNT(e) FROM InspectionRecord e WHERE e.device_id = :deviceId")
    long countByDeviceId(@Param("deviceId") String deviceId);

    @Query("SELECT e FROM InspectionRecord e WHERE e.device_id = :deviceId")
    List<InspectionRecord> findByDeviceId(@Param("deviceId") String deviceId);

    @Modifying
    @Transactional
    @Query("DELETE FROM InspectionRecord e WHERE e.device_id = :deviceId")
    int deleteByDeviceId(@Param("deviceId") String deviceId);

    @Modifying
    @Transactional
    @Query("UPDATE InspectionRecord r SET r.status = :status WHERE r.id = :id")
    int updateStatusById(@Param("id") String id, @Param("status") String status);

    @Modifying
    @Transactional
    @Query("UPDATE InspectionRecord r SET r.status = 'archived' WHERE r.device_id IN :deviceIds")
    int archiveByDeviceIds(@Param("deviceIds") Collection<String> deviceIds);

    @Modifying
    @Transactional
    @Query("UPDATE InspectionRecord r SET r.updated_email = :email")
    int stampUpdatedEmail(@Param("email") String email);
}