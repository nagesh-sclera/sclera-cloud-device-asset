package io.sclera.repository;


import io.sclera.dto.AlertScheduleDTO;
import io.sclera.model.AlertSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;

import java.util.List;


@Repository
public interface AlertScheduleRepository extends JpaRepository<AlertSchedule, String> {

//
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM alert_schedule WHERE email = ?1", nativeQuery = true)
    void deleteAlertScheduleByEmail(String email);

    @Modifying
    @Transactional
    @Query(value = "DELETE ap FROM alert_schedule ap JOIN user u ON ap.email = u.email WHERE u.customer_org_id = ?1", nativeQuery = true)
    void deleteAlertScheduleByOrgId(String orgId);

    @Query(nativeQuery = true)
    List<AlertScheduleDTO> getAllAlertScheduler();
}
