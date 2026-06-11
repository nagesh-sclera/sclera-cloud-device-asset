package io.sclera.service;

import io.sclera.Repository.BuildingRepository;
import io.sclera.Repository.VdmsRepository;
import io.sclera.client.APICallClient;
import io.sclera.client.RecordChecklistClient;
import io.sclera.dto.BuildingDTO;
import io.sclera.dto.FloorDTO;
import io.sclera.dto.touchscreen.settings.VdmsDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit coverage for the clean main methods of BuildingService: CRUD with ADC-sync branches,
 * upsert routing, and delegates. The heavy backend-sync orchestration and the per-building
 * record-checklist count computation (non-null field path) are deferred.
 */
@ExtendWith(MockitoExtension.class)
class BuildingServiceTest {

    @Mock BuildingRepository buildingRepository;
    @Mock FloorService floorservice;
    @Mock APICallClient apiCallService;   // matches field name
    @Mock APICallClient apicallService;   // matches the second field name
    @Mock VdmsRepository vdmsRepository;
    @Mock RecordChecklistClient recordChecklistService;

    @InjectMocks BuildingService service;

    private BuildingDTO building(String id) {
        BuildingDTO b = new BuildingDTO();
        b.setBuilding_id(id);
        return b;
    }

    // ---- pure / delegate --------------------------------------------------

    @Test
    void compareIds_matchesPresentIdOnly() {
        assertThat(service.compareIds(Set.of("a", "b"), "b")).isTrue();
        assertThat(service.compareIds(Set.of("a", "b"), "z")).isFalse();
    }

    @Test
    void deleteUnlinkedBuildings_deletesEach() {
        when(buildingRepository.getUnlinkedBuildingIds())
                .thenReturn(new LinkedHashSet<>(List.of("b1", "b2")));
        service.deleteUnlinkedBuildings();
        verify(buildingRepository).deleteById("b1");
        verify(buildingRepository).deleteById("b2");
    }

    @Test
    void updateFloorMaps_delegatesToFloorService() {
        List<FloorDTO> in = List.of(mock(FloorDTO.class));
        List<FloorDTO> out = List.of(mock(FloorDTO.class));
        when(floorservice.updateFloorImages("v1", in)).thenReturn(out);
        assertThat(service.updateFloorMaps("v1", in)).isSameAs(out);
    }

    // ---- ADC sync ---------------------------------------------------------

    @Test
    void syncBuildingToADCServer_syncsWithVdmsDetails() {
        BuildingDTO b = building("b1");
        when(vdmsRepository.getSyncDetailsForADC()).thenReturn(mock(VdmsDTO.class));
        when(apiCallService.syncBuildingToADC(any(), any(), any())).thenReturn(true);

        service.syncBuildingToADCServer(b);

        verify(apiCallService).syncBuildingToADC(any(), any(), any());
    }

    @Test
    void syncDeleteBuildingToADC_callsDeleteOnApi() {
        when(vdmsRepository.getSyncDetailsForADC()).thenReturn(mock(VdmsDTO.class));
        when(apicallService.deleteBuildingFromADC(any(), any(), any())).thenReturn(true);

        service.syncDeleteBuildingToADC("b1");

        verify(apicallService).deleteBuildingFromADC(any(), any(), any());
    }

    // ---- update / add with sync branch -----------------------------------

    @Test
    void updateBuildingByBuildingId_syncsWhenRowsAffectedAndUpsertsFloors() {
        BuildingDTO b = building("b1");
        when(buildingRepository.updateBuildingByBuildingId(any(), eq("b1"), any())).thenReturn(1);
        when(vdmsRepository.getSyncDetailsForADC()).thenReturn(mock(VdmsDTO.class));
        when(apiCallService.syncBuildingToADC(any(), any(), any())).thenReturn(true);

        service.updateBuildingByBuildingId(b);

        verify(apiCallService).syncBuildingToADC(any(), any(), any());
        verify(floorservice).upsertFloorsByBuildingId(any(), eq("b1"));
    }

    @Test
    void updateBuildingByBuildingId_noSyncWhenZeroRows() {
        BuildingDTO b = building("b1");
        when(buildingRepository.updateBuildingByBuildingId(any(), eq("b1"), any())).thenReturn(0);

        service.updateBuildingByBuildingId(b);

        verify(apiCallService, never()).syncBuildingToADC(any(), any(), any());
        verify(floorservice).upsertFloorsByBuildingId(any(), eq("b1"));
    }

    @Test
    void addBuildingByVdmsId_generatesIdAndSyncsWhenRowsAffected() {
        BuildingDTO b = building(null);
        when(buildingRepository.addBuildingByVdmsId(anyString(), any(), eq("v1"), any())).thenReturn(1);
        when(vdmsRepository.getSyncDetailsForADC()).thenReturn(mock(VdmsDTO.class));
        when(apiCallService.syncBuildingToADC(any(), any(), any())).thenReturn(true);

        String id = service.addBuildingByVdmsId(b, "v1");

        assertThat(id).isNotNull();
        verify(apiCallService).syncBuildingToADC(any(), any(), any());
        verify(floorservice).upsertFloorsByBuildingId(any(), anyString());
    }

    @Test
    void addBuildingByVdmsId_noSyncWhenZeroRows() {
        BuildingDTO b = building("b1");
        when(buildingRepository.addBuildingByVdmsId(eq("b1"), any(), eq("v1"), any())).thenReturn(0);

        service.addBuildingByVdmsId(b, "v1");

        verify(apiCallService, never()).syncBuildingToADC(any(), any(), any());
    }

    // ---- upsert routing ---------------------------------------------------

    @Test
    void upsertBuildingByVdmsId_existingId_routesToUpdate() {
        BuildingDTO b = building("b1");
        when(buildingRepository.getBuildingIdsByVdmsId("v1")).thenReturn(Set.of("b1"));
        when(buildingRepository.updateBuildingByBuildingId(any(), eq("b1"), any())).thenReturn(0);

        service.upsertBuildingByVdmsId(b, "v1");

        verify(buildingRepository).updateBuildingByBuildingId(any(), eq("b1"), any());
        verify(buildingRepository, never()).addBuildingByVdmsId(any(), any(), any(), any());
    }

    @Test
    void upsertBuildingByVdmsId_noExistingIds_routesToAdd() {
        BuildingDTO b = building("b1");
        when(buildingRepository.getBuildingIdsByVdmsId("v1")).thenReturn(Set.of());
        when(buildingRepository.addBuildingByVdmsId(eq("b1"), any(), eq("v1"), any())).thenReturn(0);

        service.upsertBuildingByVdmsId(b, "v1");

        verify(buildingRepository).addBuildingByVdmsId(eq("b1"), any(), eq("v1"), any());
        verify(buildingRepository, never()).updateBuildingByBuildingId(any(), any(), any());
    }

    // ---- synclocationbyId branch -----------------------------------------

    @Test
    void synclocationbyId_upsertsWhenBuildingReturned() {
        when(apiCallService.addSingleBuildingObject("l1", "v1")).thenReturn(building("b1"));
        when(buildingRepository.getBuildingIdsByVdmsId("v1")).thenReturn(Set.of());
        when(buildingRepository.addBuildingByVdmsId(eq("b1"), any(), eq("v1"), any())).thenReturn(0);

        service.synclocationbyId("l1", "v1");

        verify(buildingRepository).addBuildingByVdmsId(eq("b1"), any(), eq("v1"), any());
    }

    @Test
    void synclocationbyId_nullBuilding_doesNotUpsert() {
        when(apiCallService.addSingleBuildingObject("l1", "v1")).thenReturn(null);

        service.synclocationbyId("l1", "v1");

        verify(buildingRepository, never()).getBuildingIdsByVdmsId(any());
    }

    // ---- getBuildingByLocationId branch ----------------------------------

    @Test
    void getBuildingByLocationId_floorNull_returnsNull() {
        when(floorservice.getFloorByLocationId("l1")).thenReturn(null);
        assertThat(service.getBuildingByLocationId("u", "v1", "l1")).isNull();
    }

    @Test
    void getBuildingByLocationId_floorPresent_returnsBuildingWithFloor() {
        FloorDTO floor = mock(FloorDTO.class);
        when(floor.getFloor_id()).thenReturn("f1");
        when(floorservice.getFloorByLocationId("l1")).thenReturn(floor);
        BuildingDTO b = mock(BuildingDTO.class);
        when(buildingRepository.getBuildingByFloorId("f1")).thenReturn(b);

        assertThat(service.getBuildingByLocationId("u", "v1", "l1")).isSameAs(b);
        verify(b).setFloors(any());
    }

    // ---- getBatchBuildingsByPagination -----------------------------------

    @Test
    void getBatchBuildingsByPagination_returnsSingleBatchAndStops() {
        Set<String> ids = Set.of("b1");
        List<BuildingDTO> batch = List.of(mock(BuildingDTO.class)); // size < pageSize -> stop
        when(buildingRepository.getBatchBuildingsByPagination(ids, PageRequest.of(0, 500))).thenReturn(batch);

        assertThat(service.getBatchBuildingsByPagination(ids)).hasSize(1);
    }

    // ---- getBuildingsByVdmsId (null field -> no count computation) --------

    @Test
    void getBuildingsByVdmsId_nullField_returnsBuildingsWithoutCounts() {
        BuildingDTO b = mock(BuildingDTO.class);
        when(b.getBuilding_id()).thenReturn("b1");
        Set<BuildingDTO> buildings = Set.of(b);
        when(buildingRepository.getBuildingsByVdmsId("v1")).thenReturn(buildings);
        when(recordChecklistService.getAllRecordChecklistByBuildings(any(), any(), any())).thenReturn(Set.of());

        assertThat(service.getBuildingsByVdmsId("v1", null, null)).isSameAs(buildings);
        verify(b, never()).setCounts(any());
    }

    // ---- deleteBuildingsByIdsSync branches -------------------------------

    @Test
    void deleteBuildingsByIdsSync_noBuildings_returnsEarly() {
        Set<String> ids = Set.of("b1");
        when(buildingRepository.getBatchBuildingsByPagination(ids, PageRequest.of(0, 500))).thenReturn(List.of());

        service.deleteBuildingsByIdsSync("u", "v1", ids);

        verify(buildingRepository, never()).deleteAllById(any());
    }

    @Test
    void deleteBuildingsByIdsSync_withBuildings_deletesFloorsAndBuildings() {
        Set<String> ids = Set.of("b1");
        when(buildingRepository.getBatchBuildingsByPagination(ids, PageRequest.of(0, 500)))
                .thenReturn(List.of(mock(BuildingDTO.class)));
        when(floorservice.getFloorsByBuildingIds(ids)).thenReturn(List.of());

        service.deleteBuildingsByIdsSync("u", "v1", ids);

        verify(floorservice).processFloorDeletions(eq("u"), eq("v1"), any());
        verify(buildingRepository).deleteAllById(ids);
    }
}
