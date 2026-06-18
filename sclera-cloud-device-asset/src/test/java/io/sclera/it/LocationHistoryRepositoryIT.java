package io.sclera.it;

import io.sclera.Repository.LocationHistoryRepository;
import io.sclera.dto.LocationHistoryDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.math.BigInteger;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@Sql(scripts = "/schema-pg.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = "/seed/location-history-pilot.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/cleanup-location-history-pilot.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@Transactional
class LocationHistoryRepositoryIT extends PostgresJpaIT {

    @Autowired
    LocationHistoryRepository locationHistoryRepository;

    @PersistenceContext
    EntityManager em;

    // ---- getLocationHistory ----

    @Test
    void getLocationHistory_byLocationId_returnsOnlyMatchingRows() {
        Set<LocationHistoryDTO> result = locationHistoryRepository.getLocationHistory("loc-001");
        assertThat(result).hasSize(2);
        assertThat(result).extracting(LocationHistoryDTO::getId)
                .containsExactlyInAnyOrder("lh-001", "lh-002");
    }

    @Test
    void getLocationHistory_fieldsPopulated() {
        Set<LocationHistoryDTO> result = locationHistoryRepository.getLocationHistory("loc-001");
        LocationHistoryDTO lh1 = result.stream()
                .filter(d -> "lh-001".equals(d.getId()))
                .findFirst().orElseThrow();
        assertThat(lh1.getStatus()).isEqualTo("active");
        assertThat(lh1.getType()).isEqualTo("status_change");
        assertThat(lh1.getDescription()).isEqualTo("Activated room");
        assertThat(lh1.getUpdated_timestamp()).isEqualTo(BigInteger.valueOf(1700000001000L));
        assertThat(lh1.getUpdated_email()).isEqualTo("admin@example.com");
        assertThat(lh1.getLocation_id()).isEqualTo("loc-001");
    }

    @Test
    void getLocationHistory_differentLocation_returnsEmpty() {
        Set<LocationHistoryDTO> result = locationHistoryRepository.getLocationHistory("loc-999");
        assertThat(result).isEmpty();
    }

    @Test
    void getLocationHistory_doesNotIncludeOtherLocations() {
        Set<LocationHistoryDTO> result = locationHistoryRepository.getLocationHistory("loc-001");
        assertThat(result).extracting(LocationHistoryDTO::getId).doesNotContain("lh-003");
    }

    // ---- addLocationHistory (native INSERT — verify PG compatibility) ----

    @Test
    void addLocationHistory_insertsNewRow() {
        locationHistoryRepository.addLocationHistory(
                "lh-new", "pending", "creation", "New entry",
                BigInteger.valueOf(1700000099000L), "new@example.com", "loc-001");
        em.flush();
        em.clear();

        // Read back via scalar JPQL — avoid full entity load (Location eager chain)
        List<?> rows = em.createQuery(
                "SELECT lh.id, lh.status, lh.updated_email FROM LocationHistory lh WHERE lh.id = 'lh-new'")
                .getResultList();
        assertThat(rows).hasSize(1);
        Object[] row = (Object[]) rows.get(0);
        assertThat(row[0]).isEqualTo("lh-new");
        assertThat(row[1]).isEqualTo("pending");
        assertThat(row[2]).isEqualTo("new@example.com");
    }
}
