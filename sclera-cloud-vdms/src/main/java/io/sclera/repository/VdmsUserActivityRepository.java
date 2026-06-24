package io.sclera.repository;

import io.sclera.model.VdmsUserActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface VdmsUserActivityRepository extends JpaRepository<VdmsUserActivity, String> {

    /**
     * Checks if a user has accessed a specific VDMS within a given time range.
     *
     * @param userId    the ID of the user
     * @param vdmsId    the ID of the VDMS
     * @param startTime the start of the time range (timestamp)
     * @param endTime   the end of the time range (timestamp)
     * @return true if the user has accessed the VDMS within the time range, false otherwise
     */
    @Query(value = "SELECT COUNT(*) FROM vdms_user_activity WHERE user_id = ?1 AND vdms_id = ?2 AND access_time BETWEEN ?3 AND ?4",
           nativeQuery = true)
    long existsByUserIdAndVdmsIdAndAccessTimeBetween(String userId, String vdmsId, long startTime, long endTime);

}
