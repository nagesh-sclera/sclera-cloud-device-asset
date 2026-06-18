package io.sclera.it;

import io.sclera.Repository.FloorRepository;
import io.sclera.dto.FloorDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@Sql(scripts = "/schema-pg.sql",             executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = "/seed/floor-pilot.sql",      executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/cleanup-floor-pilot.sql",   executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@Transactional
class FloorRepositoryIT extends PostgresJpaIT {

    @Autowired
    FloorRepository floorRepository;

    @PersistenceContext
    EntityManager em;

    // -----------------------------------------------------------------------
    // Scalar reads
    // -----------------------------------------------------------------------

    @Test
    void contextLoadsAndRepositoryAutowires() {
        assertThat(floorRepository).isNotNull();
        assertThat(floorRepository.count()).isEqualTo(3);
    }

    @Test
    void getFloorIdsByBuildingId_returnsAllFloorsForBuilding() {
        Set<String> ids = floorRepository.getFloorIdsByBuildingId("b-floor-test");
        assertThat(ids).containsExactlyInAnyOrder("f-floor-1", "f-floor-2", "f-floor-3");
    }

    @Test
    void getFloorIdsByBuildingId_returnsEmptyForUnknownBuilding() {
        Set<String> ids = floorRepository.getFloorIdsByBuildingId("no-such-building");
        assertThat(ids).isEmpty();
    }

    @Test
    void getImageUrlById_returnsCorrectUrl() {
        String url = floorRepository.getImageUrlById("f-floor-1");
        assertThat(url).isEqualTo("http://example.com/f1.png");
    }

    @Test
    void getFloorPathByFloorId_returnsPath() {
        String path = floorRepository.getFloorPathByFloorId("f-floor-2");
        assertThat(path).isEqualTo("/path/to/f2");
    }

    @Test
    void getUnlinkedFloorIds_excludesFloorWithLocation() {
        // f-floor-1 has a location; f-floor-2 and f-floor-3 do not
        Set<String> unlinked = floorRepository.getUnlinkedFloorIds();
        assertThat(unlinked).containsExactlyInAnyOrder("f-floor-2", "f-floor-3");
        assertThat(unlinked).doesNotContain("f-floor-1");
    }

    // -----------------------------------------------------------------------
    // Projections
    // -----------------------------------------------------------------------

    @Test
    void getFloorsByBuildingId_returnsFloorIdAndName() {
        Set<FloorDTO> floors = floorRepository.getFloorsByBuildingId("b-floor-test");
        assertThat(floors).hasSize(3);
        assertThat(floors).extracting(FloorDTO::getFloor_id)
                .containsExactlyInAnyOrder("f-floor-1", "f-floor-2", "f-floor-3");
        // Verify name is populated (2-arg ctor sets floor_id + name)
        FloorDTO groundFloor = floors.stream()
                .filter(f -> "f-floor-1".equals(f.getFloor_id()))
                .findFirst().orElseThrow();
        assertThat(groundFloor.getName()).isEqualTo("Ground Floor");
    }

    @Test
    void getFloorsByBuildingId_withAllWildcard_returnsAllFloors() {
        Set<FloorDTO> floors = floorRepository.getFloorsByBuildingId("all");
        assertThat(floors).hasSize(3);
    }

    @Test
    void getFloorById_returnsFullProjection() {
        FloorDTO dto = floorRepository.getFloorById("f-floor-1");
        assertThat(dto).isNotNull();
        // 9-arg ctor: floor_id, name, initial_position, image_url, building_id, angle, min_zoom, max_zoom, local_image_url
        assertThat(dto.getFloor_id()).isEqualTo("f-floor-1");
        assertThat(dto.getName()).isEqualTo("Ground Floor");
        assertThat(dto.getBuilding_id()).isEqualTo("b-floor-test");
        assertThat(dto.getAngle()).isEqualTo(0);
        assertThat(dto.getMin_zoom()).isEqualTo("5");
        assertThat(dto.getMax_zoom()).isEqualTo("20");
        assertThat(dto.getLocal_image_url()).isEqualTo("/local/f1.png");
        assertThat(dto.getImage_url()).isEqualTo("http://example.com/f1.png");
    }

    @Test
    void getFloorsDetailsByBuildingId_returnsFullProjections() {
        Set<FloorDTO> floors = floorRepository.getFloorsDetailsByBuildingId("b-floor-test");
        assertThat(floors).hasSize(3);
        FloorDTO dto = floors.stream()
                .filter(f -> "f-floor-2".equals(f.getFloor_id()))
                .findFirst().orElseThrow();
        assertThat(dto.getAngle()).isEqualTo(45);
        assertThat(dto.getBuilding_id()).isEqualTo("b-floor-test");
    }

    @Test
    void getFloor_returnsIdAndNameOnly() {
        FloorDTO dto = floorRepository.getFloor("f-floor-3");
        assertThat(dto).isNotNull();
        assertThat(dto.getFloor_id()).isEqualTo("f-floor-3");
        assertThat(dto.getName()).isEqualTo("Second Floor");
        // 2-arg ctor: building_id field is not set
        assertThat(dto.getBuilding_id()).isNull();
    }

    @Test
    void getFloorByLocationId_returnsFloorForLocation() {
        FloorDTO dto = floorRepository.getFloorByLocationId("loc-floor-1");
        assertThat(dto).isNotNull();
        assertThat(dto.getFloor_id()).isEqualTo("f-floor-1");
        assertThat(dto.getName()).isEqualTo("Ground Floor");
    }

    @Test
    void getBatchFloorsByPagination_paginatesCorrectly() {
        Set<String> allIds = Set.of("f-floor-1", "f-floor-2", "f-floor-3");
        // Page 0, size 2 → first 2 floors
        List<FloorDTO> page0 = floorRepository.getBatchFloorsByPagination(allIds, PageRequest.of(0, 2));
        assertThat(page0).hasSize(2);
        // Page 1, size 2 → remaining 1 floor
        List<FloorDTO> page1 = floorRepository.getBatchFloorsByPagination(allIds, PageRequest.of(1, 2));
        assertThat(page1).hasSize(1);
        // All results together cover all 3 ids
        List<FloorDTO> allPages = new java.util.ArrayList<>(page0);
        allPages.addAll(page1);
        assertThat(allPages).extracting(FloorDTO::getFloor_id)
                .containsExactlyInAnyOrder("f-floor-1", "f-floor-2", "f-floor-3");
    }

    // -----------------------------------------------------------------------
    // Bulk updates — mutate then scalar read-back via JPQL
    // -----------------------------------------------------------------------

    @Test
    void updateFloorByFloorId_integerAngle_mutatesFields() {
        BigInteger ts = BigInteger.valueOf(9999999L);
        int rows = floorRepository.updateFloorByFloorId("Renamed Floor", "newpos", "http://new.png",
                "f-floor-1", 180, ts);
        assertThat(rows).isEqualTo(1);

        // Read back via scalar JPQL — do NOT use findById (eager building chain)
        String name = (String) em.createQuery(
                "SELECT f.name FROM Floor f WHERE f.id = 'f-floor-1'").getSingleResult();
        assertThat(name).isEqualTo("Renamed Floor");

        Integer angle = (Integer) em.createQuery(
                "SELECT f.angle FROM Floor f WHERE f.id = 'f-floor-1'").getSingleResult();
        assertThat(angle).isEqualTo(180);

        BigInteger updatedTs = (BigInteger) em.createQuery(
                "SELECT f.updatedTimestamp FROM Floor f WHERE f.id = 'f-floor-1'").getSingleResult();
        assertThat(updatedTs).isEqualTo(ts);
    }

    @Test
    void updatePathByFloorId_mutatesPath() {
        floorRepository.updatePathByFloorId("/new/path/f2", "f-floor-2");

        String path = (String) em.createQuery(
                "SELECT f.path FROM Floor f WHERE f.id = 'f-floor-2'").getSingleResult();
        assertThat(path).isEqualTo("/new/path/f2");
    }

    @Test
    void updateFloorMapZoomLevels_mutatesZoom() {
        floorRepository.updateFloorMapZoomLevels("3", "22", "f-floor-3");

        String minZoom = (String) em.createQuery(
                "SELECT f.min_zoom FROM Floor f WHERE f.id = 'f-floor-3'").getSingleResult();
        String maxZoom = (String) em.createQuery(
                "SELECT f.max_zoom FROM Floor f WHERE f.id = 'f-floor-3'").getSingleResult();
        assertThat(minZoom).isEqualTo("3");
        assertThat(maxZoom).isEqualTo("22");
    }

    @Test
    void updateLocalImageUrl_mutatesLocalUrl() {
        floorRepository.updateLocalImageUrl("/new/local/f1.png", "f-floor-1");

        String localUrl = (String) em.createQuery(
                "SELECT f.local_image_url FROM Floor f WHERE f.id = 'f-floor-1'").getSingleResult();
        assertThat(localUrl).isEqualTo("/new/local/f1.png");
    }
}
