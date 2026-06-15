package io.sclera.it;

import io.sclera.Repository.BuildingRepository;
import io.sclera.dto.BuildingDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.math.BigInteger;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@Sql(scripts = "/schema-pg.sql",              executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = "/seed/building-pilot.sql",    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/cleanup-building-pilot.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@Transactional
class BuildingRepositoryIT extends PostgresJpaIT {

    @Autowired
    BuildingRepository buildingRepository;

    @PersistenceContext
    EntityManager em;

    // ── getBuildingIdsByVdmsId ────────────────────────────────────────────────

    @Test
    void getBuildingIdsByVdmsId_returnsIdsForVdms() {
        Set<String> ids = buildingRepository.getBuildingIdsByVdmsId("v1");
        assertThat(ids).containsExactlyInAnyOrder("b1", "b2", "b3");
    }

    @Test
    void getBuildingIdsByVdmsId_unknownVdms_returnsEmpty() {
        assertThat(buildingRepository.getBuildingIdsByVdmsId("no-such-vdms")).isEmpty();
    }

    // ── getUnlinkedBuildingIds ────────────────────────────────────────────────

    @Test
    void getUnlinkedBuildingIds_excludesBuildingsWithFloors() {
        // b1 has floor f1; b2 and b3 have no floor
        Set<String> unlinked = buildingRepository.getUnlinkedBuildingIds();
        assertThat(unlinked).containsExactlyInAnyOrder("b2", "b3");
        assertThat(unlinked).doesNotContain("b1");
    }

    // ── getBuildingByFloorId ──────────────────────────────────────────────────

    @Test
    void getBuildingByFloorId_returnsBuildingForFloor() {
        BuildingDTO dto = buildingRepository.getBuildingByFloorId("f1");
        assertThat(dto).isNotNull();
        assertThat(dto.getBuilding_id()).isEqualTo("b1");
        assertThat(dto.getName()).isEqualTo("Block A");
        assertThat(dto.getVdms_id()).isEqualTo("v1");
        assertThat(dto.getCode()).isEqualTo("BLK-A");
    }

    @Test
    void getBuildingByFloorId_unknownFloor_returnsNull() {
        assertThat(buildingRepository.getBuildingByFloorId("no-such-floor")).isNull();
    }

    // ── getBuildingsByVdmsId ──────────────────────────────────────────────────

    @Test
    void getBuildingsByVdmsId_returnsAllBuildingsForVdms() {
        Set<BuildingDTO> buildings = buildingRepository.getBuildingsByVdmsId("v1");
        assertThat(buildings).hasSize(3);
        assertThat(buildings).extracting(BuildingDTO::getBuilding_id)
                .containsExactlyInAnyOrder("b1", "b2", "b3");
    }

    // ── getBuildingDetailsByBuildingId ────────────────────────────────────────

    @Test
    void getBuildingDetailsByBuildingId_returnsCorrectBuilding() {
        BuildingDTO dto = buildingRepository.getBuildingDetailsByBuildingId("b2");
        assertThat(dto).isNotNull();
        assertThat(dto.getBuilding_id()).isEqualTo("b2");
        assertThat(dto.getName()).isEqualTo("Block B");
        assertThat(dto.getVdms_id()).isEqualTo("v1");
        assertThat(dto.getCode()).isEqualTo("BLK-B");
    }

    // ── getBatchBuildingsByPagination ─────────────────────────────────────────

    @Test
    void getBatchBuildingsByPagination_firstPageReturnsSubset() {
        Set<String> ids = Set.of("b1", "b2", "b3");
        List<BuildingDTO> page = buildingRepository.getBatchBuildingsByPagination(ids, PageRequest.of(0, 2));
        assertThat(page).hasSize(2);
    }

    @Test
    void getBatchBuildingsByPagination_secondPageReturnsRemainder() {
        Set<String> ids = Set.of("b1", "b2", "b3");
        List<BuildingDTO> page = buildingRepository.getBatchBuildingsByPagination(ids, PageRequest.of(1, 2));
        assertThat(page).hasSize(1);
    }

    @Test
    void getBatchBuildingsByPagination_emptyIdSet_returnsEmpty() {
        List<BuildingDTO> page = buildingRepository.getBatchBuildingsByPagination(Set.of(), PageRequest.of(0, 10));
        assertThat(page).isEmpty();
    }

    // ── getBuildingsByVdmsIdADC ───────────────────────────────────────────────

    @Test
    void getBuildingsByVdmsIdADC_5argDto_hasBothIdAndBuildingId() {
        List<BuildingDTO> list = buildingRepository.getBuildingsByVdmsIdADC("v1");
        assertThat(list).hasSize(3);
        // 5-arg ctor: sets both id and building_id to b.id
        list.forEach(dto -> {
            assertThat(dto.getId()).isNotNull();
            assertThat(dto.getBuilding_id()).isEqualTo(dto.getId());
            assertThat(dto.getVdms_id()).isEqualTo("v1");
        });
    }

    // ── updateBuildingByBuildingId ────────────────────────────────────────────

    @Test
    void updateBuildingByBuildingId_mutatesNameAndTimestamp() {
        int rows = buildingRepository.updateBuildingByBuildingId("New Name", "b1", BigInteger.valueOf(9999L));
        assertThat(rows).isEqualTo(1);

        // Use scalar JPQL read-back to avoid loading the full eager graph
        em.flush();
        String newName = em.createQuery(
                "SELECT b.name FROM Building b WHERE b.id = 'b1'", String.class)
                .getSingleResult();
        assertThat(newName).isEqualTo("New Name");
    }
}
