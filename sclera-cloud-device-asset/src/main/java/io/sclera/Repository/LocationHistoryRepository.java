package io.sclera.Repository;

import io.sclera.dto.LocationHistoryDTO;
import io.sclera.models.LocationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;
import java.math.BigInteger;
import java.util.Set;

/**
 * Manages persistence and querying of {@link LocationHistory} entities.
 */
@Repository
public interface LocationHistoryRepository extends JpaRepository<LocationHistory, String> {

    /**
     * Inserts a new location history record.
     *
     * @param id the location history identifier
     * @param status the status recorded for the location
     * @param type the type of history entry
     * @param description the description of the change
     * @param updated_timestamp the epoch timestamp of the update
     * @param updated_email the email of the user who made the update
     * @param location_id the identifier of the associated location
     */
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO location_history(id, status,type, description, updated_timestamp, updated_email, location_id) VALUES (?1, ?2, ?3, ?4, ?5, ?6, ?7) ", nativeQuery = true)
    void addLocationHistory(String id, String status, String type, String description, BigInteger updated_timestamp, String updated_email, String location_id);

    /**
     * Returns the location history entries for a given location.
     *
     * @param location_id the identifier of the location
     * @return the set of location history entries
     */
    @Query(nativeQuery = true)
    Set<LocationHistoryDTO> getLocationHistory(String location_id);

}
