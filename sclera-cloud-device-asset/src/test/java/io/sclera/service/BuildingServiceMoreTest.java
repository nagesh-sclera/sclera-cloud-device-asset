package io.sclera.service;

import io.sclera.Repository.BuildingRepository;
import io.sclera.Repository.VdmsRepository;
import io.sclera.client.APICallClient;
import io.sclera.client.RecordChecklistClient;
import io.sclera.dto.BuildingDTO;
import io.sclera.dto.FloorDTO;
import io.sclera.dto.RecordChecklistDTO;
import io.sclera.dto.touchscreen.settings.VdmsDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
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
 * Additional coverage for the heavier BuildingService orchestration methods that the primary
 * {@link BuildingServiceTest} deferred: the batch upsert loop (add/update routing + audit logging),
 * the record-checklist count enrichment, individual building deletion with floor/ADC cascade, and
 * the floor-map migration helpers.
 */
@ExtendWith(MockitoExtension.class)
class BuildingServiceMoreTest {

    @Mock BuildingRepository buildingRepository;
    @Mock FloorService floorservice;
    @Mock APICallClient apiCallService;
    @Mock APICallClient apicallService;
    @Mock VdmsRepository vdmsRepository;
    @Mock RecordChecklistClient recordChecklistService;
    @Mock UserActionLogService userActionLogService;
    @Mock WebClientService webClientService;

    @InjectMocks BuildingService service;

    // ---- upsertBuildingsByVdmsId (batch loop + private upsertBuildingById) ----

    @Test
    void upsertBuildings_addBranch_generatesIdLogsAddAndUpsertsFloors() {
        BuildingDTO building = new BuildingDTO();
        building.setName("HQ");
        building.setFloors(new HashSet<>(Set.of(mock(FloorDTO.class))));
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getRequestURI()).thenReturn("/api/buildings");
        when(buildingRepository.upsertBuildingsByVdmsId(anyString(), any(), eq("v1"), any(), any()))
                .thenReturn(0); // no rows -> skip ADC sync
        when(floorservice.upsertFloorsByBuildingId(eq("u"), eq("v1"), anyString(), any(), any()))
                .thenReturn(Set.of());

        Set<BuildingDTO> result = service.upsertBuildingsByVdmsId("u", "v1",
                new HashSet<>(Set.of(building)), req);

        assertThat(result).hasSize(1);
        assertThat(building.getBuilding_id()).isNotNull(); // id generated for ADD
        verify(userActionLogService).addUserAction(anyString(), anyString(), eq("ADD"),
                anyString(), anyString(), anyString(), anyString());
        verify(floorservice).upsertFloorsByBuildingId(eq("u"), eq("v1"), anyString(), any(), any());
    }

    @Test
    void upsertBuildings_updateBranch_logsUpdateAndSkipsFloorsWhenNull() {
        BuildingDTO building = new BuildingDTO();
        building.setBuilding_id("b1");
        building.setName("HQ");
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getRequestURI()).thenReturn("/api/buildings");
        when(buildingRepository.upsertBuildingsByVdmsId(eq("b1"), any(), eq("v1"), any(), any()))
                .thenReturn(0);

        service.upsertBuildingsByVdmsId("u", "v1", new HashSet<>(Set.of(building)), req);

        verify(userActionLogService).addUserAction(anyString(), anyString(), eq("UPDATE"),
                anyString(), anyString(), anyString(), anyString());
        verify(floorservice, never()).upsertFloorsByBuildingId(any(), any(), any(), any(), any());
    }

    // ---- getBuildingsByVdmsId with field -> count enrichment ----

    @Test
    void getBuildingsByVdmsId_allTaskField_setsCounts() {
        BuildingDTO b = mock(BuildingDTO.class);
        when(b.getBuilding_id()).thenReturn("b1");
        RecordChecklistDTO rc = mock(RecordChecklistDTO.class);
        when(rc.getBuilding_id()).thenReturn("b1");
        when(rc.getRecord_type()).thenReturn("checklist");
        when(rc.getInspection_record_id()).thenReturn(null); // -> "tagged"
        when(buildingRepository.getBuildingsByVdmsId("v1")).thenReturn(new HashSet<>(Set.of(b)));
        when(recordChecklistService.getAllRecordChecklistByBuildings(any(), any(), any()))
                .thenReturn(new HashSet<>(Set.of(rc)));

        Set<BuildingDTO> result = service.getBuildingsByVdmsId("v1", "all_task", null);

        assertThat(result).hasSize(1);
        verify(b).setCounts(any());
    }

    // ---- deleteBuildingsByIds -> deleteBuildingById success cascade ----

    @Test
    void deleteBuildingsByIds_deletesFloorsBuildingAndSyncsToAdc() {
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getRequestURI()).thenReturn("/api/buildings");
        when(floorservice.getFloorsDetailsByBuildingId("b1")).thenReturn(Set.of());
        when(webClientService.deleteFloorMapsByImageUrl(eq("v1"), any())).thenReturn("ok");
        BuildingDTO bd = mock(BuildingDTO.class);
        when(bd.getBuilding_id()).thenReturn("b1");
        when(bd.getName()).thenReturn("HQ");
        when(buildingRepository.getBuildingDetailsByBuildingId("b1")).thenReturn(bd);
        when(vdmsRepository.getSyncDetailsForADC()).thenReturn(mock(VdmsDTO.class));
        when(apicallService.deleteBuildingFromADC(any(), any(), any())).thenReturn(true);

        service.deleteBuildingsByIds("u", "v1", Set.of("b1"), req);

        verify(floorservice).deleteFloorsByBuildingId(eq("v1"), eq("b1"), eq("u"), any());
        verify(buildingRepository).deleteById("b1");
        verify(apicallService).deleteBuildingFromADC(any(), any(), any());
    }

    @Test
    void deleteBuildingsByIds_whenMapDeleteReturnsNull_skipsDeletion() {
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getRequestURI()).thenReturn("/api/buildings");
        when(floorservice.getFloorsDetailsByBuildingId("b1")).thenReturn(Set.of());
        when(webClientService.deleteFloorMapsByImageUrl(eq("v1"), any())).thenReturn(null);

        service.deleteBuildingsByIds("u", "v1", Set.of("b1"), req);

        verify(buildingRepository, never()).deleteById(any());
    }

    // ---- floor-map migration helpers ----

    @Test
    void syncFloorMaps_collectsFloorsWithImages() {
        BuildingDTO b = mock(BuildingDTO.class);
        when(b.getBuilding_id()).thenReturn("b1");
        when(buildingRepository.getBuildingsByVdmsId("v1")).thenReturn(new HashSet<>(Set.of(b)));
        when(recordChecklistService.getAllRecordChecklistByBuildings(any(), any(), any()))
                .thenReturn(Set.of());
        FloorDTO floor = mock(FloorDTO.class);
        when(floor.getImage_url()).thenReturn("http://img/f1.png");
        when(floor.getFloor_id()).thenReturn("f1");
        when(floor.getLocal_image_url()).thenReturn(null); // -> local falls back to image_url
        when(floorservice.getFloorsDetailsByBuildingId("b1")).thenReturn(new HashSet<>(Set.of(floor)));

        Set<FloorDTO> result = service.syncFloorMaps("v1");

        assertThat(result).hasSize(1);
    }

    @Test
    void syncFloorMapsTiles_noBuildings_returnsEmpty() {
        when(buildingRepository.getBuildingsByVdmsId("VDMS400")).thenReturn(new HashSet<>());
        when(recordChecklistService.getAllRecordChecklistByBuildings(any(), any(), any()))
                .thenReturn(Set.of());

        List<FloorDTO> result = service.syncFloorMapsTiles();

        assertThat(result).isEmpty();
    }
}
