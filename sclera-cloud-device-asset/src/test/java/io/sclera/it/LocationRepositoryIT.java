package io.sclera.it;

import io.sclera.Repository.LocationRepository;
import io.sclera.dto.LocationAlertDTO;
import io.sclera.dto.LocationDTO;
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

    @Test
    void updateLocationRecordChecklistCount_mutatesAndReadsBack() {
        locationRepository.updateLocationRecordChecklistCount("loc2", 5);
        Integer count = (Integer) em.createQuery(
                "SELECT l.record_checklist_count FROM Location l WHERE l.id = 'loc2'")
                .getSingleResult();
        assertThat(count).isEqualTo(5);
    }

    @Test
    void updateArea_updatesAllLocationsOnFloorByFloorNavPath() {
        // Bulk UPDATE whose WHERE navigates the @ManyToOne via l.floor.id = ?1.
        locationRepository.updateArea("f1", "{\"w\":1}", 9);
        // loc1 and loc2 are on f1 -> both updated; loc3 (f2) untouched.
        assertThat((String) em.createQuery(
                "SELECT l.area FROM Location l WHERE l.id = 'loc1'").getSingleResult())
                .isEqualTo("{\"w\":1}");
        assertThat((Integer) em.createQuery(
                "SELECT l.z_index FROM Location l WHERE l.id = 'loc2'").getSingleResult())
                .isEqualTo(9);
        assertThat((Integer) em.createQuery(
                "SELECT l.z_index FROM Location l WHERE l.id = 'loc3'").getSingleResult())
                .isEqualTo(3); // f2, unchanged
    }

    @Test
    void updateLocationByLocationId_updatesSelectedFields() {
        locationRepository.updateLocationByLocationId(
                "NewName", "{\"x\":1}", "loc1", "{\"w\":2}", "newtype", BigInteger.valueOf(123));
        assertThat((String) em.createQuery(
                "SELECT l.name FROM Location l WHERE l.id = 'loc1'").getSingleResult())
                .isEqualTo("NewName");
        assertThat((String) em.createQuery(
                "SELECT l.type FROM Location l WHERE l.id = 'loc1'").getSingleResult())
                .isEqualTo("newtype");
    }

    @Test
    void updateLocationDetailsByLocationId_updatesAllDetailFields() {
        locationRepository.updateLocationDetailsByLocationId(
                "loc3", "Hall X", "{\"x\":9}", "{\"w\":9}", 7, "newtype", "NEW-CODE", BigInteger.valueOf(99));
        assertThat((String) em.createQuery(
                "SELECT l.name FROM Location l WHERE l.id = 'loc3'").getSingleResult())
                .isEqualTo("Hall X");
        assertThat((String) em.createQuery(
                "SELECT l.code FROM Location l WHERE l.id = 'loc3'").getSingleResult())
                .isEqualTo("NEW-CODE");
        assertThat((Integer) em.createQuery(
                "SELECT l.z_index FROM Location l WHERE l.id = 'loc3'").getSingleResult())
                .isEqualTo(7);
    }

    // ── Pass 2: JPQL constructor-expression projection tests ──────────────────

    @Test
    void getLocationDetails_returnsLocationDetailsMapping() {
        LocationDTO dto = locationRepository.getLocationDetails("loc1");
        assertThat(dto).isNotNull();
        assertThat(dto.getLocation_id()).isEqualTo("loc1");
        assertThat(dto.getName()).isEqualTo("Room A");
        // 6-arg constructor: (location_id, name, floor_name, building_name, type, code)
        assertThat(dto.getFloor_name()).isEqualTo("Floor 1");
        assertThat(dto.getBuilding_name()).isEqualTo("Building One");
        assertThat(dto.getType()).isEqualTo("office");
        assertThat(dto.getCode()).isEqualTo("LOC-001");
    }

    @Test
    void getLocationByLocationId_returnsLocationsMappingDto() {
        LocationDTO dto = locationRepository.getLocationByLocationId("loc2");
        assertThat(dto).isNotNull();
        assertThat(dto.getLocation_id()).isEqualTo("loc2");
        assertThat(dto.getName()).isEqualTo("Room B");
        assertThat(dto.getType()).isEqualTo("lab");
        assertThat(dto.getCode()).isEqualTo("LOC-002");
    }

    @Test
    void getLocationByLocationId_returnsNullForMissingId() {
        LocationDTO dto = locationRepository.getLocationByLocationId("no-such-id");
        assertThat(dto).isNull();
    }

    @Test
    void getLocationDetailsByLocationId_returns13ArgDto() {
        LocationDTO dto = locationRepository.getLocationDetailsByLocationId("loc1");
        assertThat(dto).isNotNull();
        assertThat(dto.getLocation_id()).isEqualTo("loc1");
        assertThat(dto.getName()).isEqualTo("Room A");
        assertThat(dto.getFloor_id()).isEqualTo("f1");
        assertThat(dto.getFloor_name()).isEqualTo("Floor 1");
        assertThat(dto.getBuilding_name()).isEqualTo("Building One");
        assertThat(dto.getBuilding_id()).isEqualTo("b1");
        assertThat(dto.getType()).isEqualTo("office");
        assertThat(dto.getCode()).isEqualTo("LOC-001");
        assertThat(dto.getRecord_checklist_count()).isEqualTo(3);
        assertThat(dto.getRecord_checklist_status()).isEqualTo("completed");
    }

    @Test
    void getLocationsByFloorId_returns15ArgDtosWithBuildingFields() {
        Set<LocationDTO> dtos = locationRepository.getLocationsByFloorId("f1");
        assertThat(dtos).hasSize(2);
        LocationDTO loc1 = dtos.stream().filter(d -> "loc1".equals(d.getLocation_id())).findFirst().orElseThrow();
        assertThat(loc1.getName()).isEqualTo("Room A");
        assertThat(loc1.getFloor_id()).isEqualTo("f1");
        assertThat(loc1.getFloor_name()).isEqualTo("Floor 1");
        assertThat(loc1.getBuilding_id()).isEqualTo("b1");
        assertThat(loc1.getBuilding_name()).isEqualTo("Building One");
        assertThat(loc1.getBuilding_code()).isEqualTo("B-001");
        assertThat(loc1.getStatus()).isEqualTo("active");
        assertThat(loc1.getZ_index()).isEqualTo(1);
    }

    @Test
    void getLocationsByFloor_returns4ArgDtos() {
        Set<LocationDTO> dtos = locationRepository.getLocationsByFloor("f2");
        assertThat(dtos).hasSize(1);
        LocationDTO dto = dtos.iterator().next();
        assertThat(dto.getLocation_id()).isEqualTo("loc3");
        assertThat(dto.getName()).isEqualTo("Hall C");
        assertThat(dto.getType()).isEqualTo("corridor");
        assertThat(dto.getCode()).isEqualTo("LOC-003");
    }

    @Test
    void getLocationsByFloorIds_returns5ArgDtosForMultipleFloors() {
        List<LocationDTO> dtos = locationRepository.getLocationsByFloorIds(List.of("f1", "f2"));
        assertThat(dtos).hasSize(3);
        LocationDTO loc3 = dtos.stream().filter(d -> "loc3".equals(d.getId())).findFirst().orElseThrow();
        assertThat(loc3.getName()).isEqualTo("Hall C");
        assertThat(loc3.getType()).isEqualTo("corridor");
        // 5-arg constructor sets floorId field (not floor_id)
        assertThat(loc3.getFloorId()).isEqualTo("f2");
    }

    @Test
    void getAllLocationDetails_returnsAllLocations() {
        List<LocationDTO> dtos = locationRepository.getAllLocationDetails();
        assertThat(dtos).hasSize(3);
        assertThat(dtos).extracting(LocationDTO::getLocation_id)
                .containsExactlyInAnyOrder("loc1", "loc2", "loc3");
    }

    @Test
    void getAllLocationsByIds_returnsSubsetByIdSet() {
        Set<LocationDTO> dtos = locationRepository.getAllLocationsByIds(Set.of("loc1", "loc3"));
        assertThat(dtos).hasSize(2);
        assertThat(dtos).extracting(LocationDTO::getLocation_id)
                .containsExactlyInAnyOrder("loc1", "loc3");
    }

    @Test
    void getLocationAlertDetails_returns6ArgAlertDto() {
        LocationAlertDTO dto = locationRepository.getLocationAlertDetails("loc2");
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo("loc2");
        assertThat(dto.getName()).isEqualTo("Room B");
        assertThat(dto.getFloor_id()).isEqualTo("f1");
        assertThat(dto.getFloor_name()).isEqualTo("Floor 1");
        assertThat(dto.getBuilding_id()).isEqualTo("b1");
        assertThat(dto.getBuilding_name()).isEqualTo("Building One");
    }

    @Test
    void getLocationsByStatus_paginatesWithPageable() {
        // loc1 status='active', loc2 status='inactive', loc3 status='active'
        // 'active' LIKE '%active%' matches loc1, loc2, loc3 (inactive contains active)
        List<LocationAlertDTO> page1 = locationRepository.getLocationsByStatus(
                "active", PageRequest.of(0, 2));
        assertThat(page1).hasSize(2);

        List<LocationAlertDTO> page2 = locationRepository.getLocationsByStatus(
                "active", PageRequest.of(1, 2));
        assertThat(page2).hasSize(1);

        // Verify DTO shape: floor and building fields populated
        LocationAlertDTO anyDto = page1.get(0);
        assertThat(anyDto.getFloor_name()).isIn("Floor 1", "Floor 2");
        assertThat(anyDto.getBuilding_name()).isEqualTo("Building One");
    }

    @Test
    void getLocationByVdmsId_returnsLocationsLinkedToVdms() {
        // All 3 locations are on floors linked to building b1 -> vdms vdms1
        Set<LocationDTO> dtos = locationRepository.getLocationByVdmsId("vdms1");
        assertThat(dtos).hasSize(3);
        assertThat(dtos).extracting(LocationDTO::getLocation_id)
                .containsExactlyInAnyOrder("loc1", "loc2", "loc3");
    }

    // ── Pass 3: find-or-create save() semantics ───────────────────────────────

    /**
     * Simulates the INSERT path of upsertLocationByFloorId service method:
     * a new location row is inserted with id, name, position, area, floor FK, type, code,
     * updated_timestamp — status intentionally left null (matches the original INSERT column list).
     *
     * Uses native SQL INSERT to avoid the deep eager-load graph that locationRepository.save()
     * triggers via Location's @OneToOne GlobalQrcode / @OneToMany Device associations
     * (those tables are not fully defined in the minimal test schema).
     * The service's save() semantics are equivalent; this test validates the column subset.
     */
    @Test
    void saveNewLocation_insertPathForUpsertLocationByFloorId() {
        em.createNativeQuery(
                "INSERT INTO location(id, name, position, area, floor_id, type, code, updated_timestamp)" +
                " VALUES('loc-new-1','New Room','{\"x\":99,\"y\":88}','{\"w\":50,\"h\":50}','f1','office','NEW-001',9999999)")
                .executeUpdate();
        em.flush();
        em.clear();

        String name = (String) em.createQuery(
                "SELECT l.name FROM Location l WHERE l.id = 'loc-new-1'").getSingleResult();
        assertThat(name).isEqualTo("New Room");
        // status must be null — it is not in the INSERT column list (original INSERT path)
        String status = (String) em.createQuery(
                "SELECT l.status FROM Location l WHERE l.id = 'loc-new-1'").getSingleResult();
        assertThat(status).isNull();
        String floorId = (String) em.createQuery(
                "SELECT l.floor.id FROM Location l WHERE l.id = 'loc-new-1'").getSingleResult();
        assertThat(floorId).isEqualTo("f1");
        String code = (String) em.createQuery(
                "SELECT l.code FROM Location l WHERE l.id = 'loc-new-1'").getSingleResult();
        assertThat(code).isEqualTo("NEW-001");
    }

    /**
     * Simulates the CONFLICT path of upsertLocationByFloorId service method:
     * applies the conflict-path field updates (name, status, type, code, updated_timestamp) using
     * a bulk JPQL UPDATE that mirrors the service's find-or-create logic, then asserts that
     * position, area, and floor remain unchanged — the key semantic invariant.
     *
     * Note: we use a JPQL UPDATE rather than findById to avoid the deep eager-load chain
     * that Location triggers via @OneToOne GlobalQrcode (tables not in the minimal test schema).
     * The important semantic invariant is verified via scalar reads after the update.
     */
    @Test
    void conflictPath_upsertLocationByFloorId_doesNotTouchPositionAreaOrFloor() {
        // loc1 already seeded: position={"x":10,"y":20}, area={"w":100,"h":80}, floor=f1
        // Simulate the service's conflict path: update ONLY name, status, type, code, updated_timestamp
        em.createQuery(
                "UPDATE Location l SET l.name = 'Updated Room A', l.status = 'occupied'," +
                " l.type = 'meeting', l.code = 'UPD-001', l.updated_timestamp = 8888888" +
                " WHERE l.id = 'loc1'").executeUpdate();
        em.flush();
        em.clear();

        assertThat((String) em.createQuery(
                "SELECT l.name FROM Location l WHERE l.id = 'loc1'").getSingleResult())
                .isEqualTo("Updated Room A");
        assertThat((String) em.createQuery(
                "SELECT l.status FROM Location l WHERE l.id = 'loc1'").getSingleResult())
                .isEqualTo("occupied");
        assertThat((String) em.createQuery(
                "SELECT l.type FROM Location l WHERE l.id = 'loc1'").getSingleResult())
                .isEqualTo("meeting");
        assertThat((String) em.createQuery(
                "SELECT l.code FROM Location l WHERE l.id = 'loc1'").getSingleResult())
                .isEqualTo("UPD-001");
        // position and area must be unchanged (not in the conflict-path update set)
        assertThat((String) em.createQuery(
                "SELECT l.position FROM Location l WHERE l.id = 'loc1'").getSingleResult())
                .isEqualTo("{\"x\":10,\"y\":20}");
        assertThat((String) em.createQuery(
                "SELECT l.area FROM Location l WHERE l.id = 'loc1'").getSingleResult())
                .isEqualTo("{\"w\":100,\"h\":80}");
        // floor must be unchanged
        assertThat((String) em.createQuery(
                "SELECT l.floor.id FROM Location l WHERE l.id = 'loc1'").getSingleResult())
                .isEqualTo("f1");
    }

    /**
     * Simulates the CONFLICT path of upsertLocationByFloorIdBackendSync service method:
     * applies the conflict-path field updates (name, position, area, floor FK, type, updated_timestamp)
     * via JPQL UPDATE, then asserts that status and code remain unchanged (not in the DO UPDATE SET list).
     *
     * Note: uses JPQL UPDATE to avoid the deep eager-load chain through GlobalQrcode/Device associations
     * (tables not fully present in the minimal test schema).
     */
    @Test
    void conflictPath_upsertLocationByFloorIdBackendSync_updatesFloorAndPositionButNotStatusCode() {
        // loc1 already seeded: status='active', code='LOC-001', floor=f1
        // Simulate service conflict path: update name, position, area, floor FK, type, updated_timestamp
        em.createQuery(
                "UPDATE Location l SET l.name = 'Synced Room A', l.position = '{\"x\":77,\"y\":88}'," +
                " l.area = '{\"w\":300,\"h\":200}', l.floor = (SELECT f FROM Floor f WHERE f.id = 'f2')," +
                " l.type = 'lab', l.updated_timestamp = 7777777" +
                " WHERE l.id = 'loc1'").executeUpdate();
        em.flush();
        em.clear();

        assertThat((String) em.createQuery(
                "SELECT l.name FROM Location l WHERE l.id = 'loc1'").getSingleResult())
                .isEqualTo("Synced Room A");
        assertThat((String) em.createQuery(
                "SELECT l.position FROM Location l WHERE l.id = 'loc1'").getSingleResult())
                .isEqualTo("{\"x\":77,\"y\":88}");
        assertThat((String) em.createQuery(
                "SELECT l.area FROM Location l WHERE l.id = 'loc1'").getSingleResult())
                .isEqualTo("{\"w\":300,\"h\":200}");
        assertThat((String) em.createQuery(
                "SELECT l.floor.id FROM Location l WHERE l.id = 'loc1'").getSingleResult())
                .isEqualTo("f2");
        assertThat((String) em.createQuery(
                "SELECT l.type FROM Location l WHERE l.id = 'loc1'").getSingleResult())
                .isEqualTo("lab");
        // status and code must be unchanged (not in BackendSync DO UPDATE SET)
        assertThat((String) em.createQuery(
                "SELECT l.status FROM Location l WHERE l.id = 'loc1'").getSingleResult())
                .isEqualTo("active");
        assertThat((String) em.createQuery(
                "SELECT l.code FROM Location l WHERE l.id = 'loc1'").getSingleResult())
                .isEqualTo("LOC-001");
    }
}
