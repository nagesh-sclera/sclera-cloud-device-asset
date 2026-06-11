package io.sclera.it;

import io.sclera.Repository.LocationRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@Sql(scripts = "/schema-pg.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = "/seed/location-pilot.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/cleanup-location-pilot.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@Transactional
class LocationRepositoryIT extends PostgresJpaIT {

    @Autowired
    LocationRepository locationRepository;

    @PersistenceContext
    EntityManager em;

    @Test
    void contextLoadsAndRepositoryAutowires() {
        assertThat(locationRepository).isNotNull();
        assertThat(locationRepository.count()).isEqualTo(3);
    }

    @Test
    void getLocationIdsByFloorId_returnsIdsOnFloor() {
        Set<String> ids = locationRepository.getLocationIdsByFloorId("f1");
        assertThat(ids).containsExactlyInAnyOrder("loc1", "loc2");
    }

    @Test
    void getPositionByLocationId_returnsPosition() {
        String pos = locationRepository.getPositionByLocationId("loc1");
        assertThat(pos).isEqualTo("{\"x\":10,\"y\":20}");
    }

    @Test
    void getLocationName_returnsName() {
        String name = locationRepository.getLocationName("loc2");
        assertThat(name).isEqualTo("Room B");
    }

    @Test
    void getUniqueLocationTypes_returnsDistinctTypes() {
        List<String> types = locationRepository.getUniqueLocationTypes();
        assertThat(types).containsExactlyInAnyOrder("office", "lab", "corridor");
    }

    @Test
    void getLocationId_countsByExactId() {
        assertThat(locationRepository.getLocationId("loc1")).isEqualTo(1);
        assertThat(locationRepository.getLocationId("nonexistent")).isEqualTo(0);
    }

    @Test
    void getLocationIds_returnsSubsetOfExistingIds() {
        Set<String> ids = locationRepository.getLocationIds(Set.of("loc1", "loc3", "nonexistent"));
        assertThat(ids).containsExactlyInAnyOrder("loc1", "loc3");
    }

    @Test
    void checkLocationById_returns1WhenExists_0WhenNot() {
        assertThat(locationRepository.checkLocationById("loc1")).isEqualTo(1);
        assertThat(locationRepository.checkLocationById("ghost")).isEqualTo(0);
    }

    @Test
    void getUnlinkedLocationIds_excludesDeviceLinkedLocations() {
        // loc1 is linked to dev1; loc2 and loc3 are unlinked
        Set<String> unlinked = locationRepository.getUnlinkedLocationIds();
        assertThat(unlinked).containsExactlyInAnyOrder("loc2", "loc3");
        assertThat(unlinked).doesNotContain("loc1");
    }

    @Test
    void getLocationStatusCountTs_countsByStatusSubstring() {
        // 'active' matches 'active' (loc1), 'inactive' (loc2), and 'active' (loc3) = 3
        Integer count = locationRepository.getLocationStatusCountTs("active");
        assertThat(count).isEqualTo(3);
    }

    @Test
    void getLocationsCountByFloorId_returnsStringCount() {
        String count = locationRepository.getLocationsCountByFloorId("f1", "null");
        assertThat(count).isEqualTo("2");
    }

    @Test
    void getLocationsCountByFloorId_withSearchkey_filters() {
        // Both 'Room A' and 'Room B' match "Room"
        String count = locationRepository.getLocationsCountByFloorId("f1", "Room");
        assertThat(count).isEqualTo("2");
    }

    @Test
    void getLocationIdbyLocationName_returnsFirstMatch() {
        String id = locationRepository.getLocationIdbyLocationName("Room A");
        assertThat(id).isEqualTo("loc1");
    }

    @Test
    void getLocationIdbyLocationName_returnsNullWhenNotFound() {
        String id = locationRepository.getLocationIdbyLocationName("Nonexistent");
        assertThat(id).isNull();
    }

    @Test
    void updateLocationRecordChecklistStatus_mutatesAndReadsBack() {
        locationRepository.updateLocationRecordChecklistStatus("loc2", "done");
        // Use a scalar JPQL read to avoid a full entity load (findById eagerly joins
        // floor/global_qrcode/device chains which are not in the minimal test schema).
        // clearAutomatically=true on the @Modifying flushes the L1 cache before this read.
        String status = (String) em.createQuery(
                "SELECT l.record_checklist_status FROM Location l WHERE l.id = 'loc2'")
                .getSingleResult();
        assertThat(status).isEqualTo("done");
    }
}
