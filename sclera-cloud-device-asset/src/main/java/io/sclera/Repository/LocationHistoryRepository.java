package io.sclera.Repository;

import io.sclera.dto.LocationHistoryDTO;
import io.sclera.models.LocationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.math.BigInteger;
import java.util.Set;

@Repository
public interface LocationHistoryRepository extends JpaRepository<LocationHistory, String> {

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO location_history(id, status,type, description, updated_timestamp, updated_email, location_id) VALUE (?1, ?2, ?3, ?4, ?5, ?6, ?7) ", nativeQuery = true)
    void addLocationHistory(String id, String status, String type, String description, BigInteger updated_timestamp, String updated_email, String location_id);

    @Query(nativeQuery = true)
    Set<LocationHistoryDTO> getLocationHistory(String location_id);

}
