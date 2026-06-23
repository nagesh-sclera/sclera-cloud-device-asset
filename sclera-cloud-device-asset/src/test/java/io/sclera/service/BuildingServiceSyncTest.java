package io.sclera.service;

import io.sclera.Repository.BuildingRepository;
import io.sclera.Repository.VdmsRepository;
import io.sclera.client.APICallClient;
import io.sclera.client.RecordChecklistClient;
import io.sclera.dto.BuildingDTO;
import io.sclera.dto.FloorDTO;
import io.sclera.dto.LocationDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Coverage for BuildingService.syncLocationsFromBackend - the one-time cloud->local migration that
 * upserts buildings/floors/locations and computes the locally-present-but-cloud-missing set to
 * delete. Covers the empty path (null result) and a mismatched-building path.
 */
@ExtendWith(MockitoExtension.class)
class BuildingServiceSyncTest {

    @Mock BuildingRepository buildingRepository;
    @Mock FloorService floorservice;
    @Mock APICallClient apiCallService;
    @Mock VdmsRepository vdmsRepository;
    @Mock RecordChecklistClient recordChecklistService;
    @Mock UserActionLogService userActionLogService;
    @Mock LocationService locationService;

    @InjectMocks BuildingService service;

    @Test
    void syncLocationsFromBackend_noLocalBuildings_returnsNull() {
        when(apiCallService.getAllLocations("VDMS400")).thenReturn(List.of());
        when(buildingRepository.getBuildingsByVdmsId("VDMS400")).thenReturn(Set.of());
        when(recordChecklistService.getAllRecordChecklistByBuildings(any(), any(), any()))
                .thenReturn(Set.of());

        assertThat(service.syncLocationsFromBackend(mock(HttpServletRequest.class))).isNull();
    }

    @Test
    void syncLocationsFromBackend_vdmsBuildingMissingFromCloud_flaggedForDeletion() {
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getRequestURI()).thenReturn("/api/sync");

        // cloud has building b1 (with one floor + location)
        LocationDTO loc = mock(LocationDTO.class);
        when(loc.getLocation_id()).thenReturn("l1");
        FloorDTO floor = mock(FloorDTO.class);
        when(floor.getFloor_id()).thenReturn("f1");
        when(floor.getLocations()).thenReturn(Set.of(loc));
        BuildingDTO cloudBuilding = mock(BuildingDTO.class);
        when(cloudBuilding.getBuilding_id()).thenReturn("b1");
        when(cloudBuilding.getFloors()).thenReturn(Set.of(floor));
        when(apiCallService.getAllLocations("VDMS400")).thenReturn(List.of(cloudBuilding));
        when(buildingRepository.upsertBuildingsByVdmsId(eq("b1"), any(), eq("VDMS400"), any(), any()))
                .thenReturn(0); // no rows -> skip ADC sync
        when(floorservice.upsertFloorImageByBuildingFromBackend(floor)).thenReturn("http://img/f1");
        when(apiCallService.getFloorPathByFloorId(null, "VDMS400", "b1", "f1")).thenReturn("/maps/f1");

        // local VDMS has a different building b2 (missing from cloud -> delete)
        BuildingDTO vdmsBuilding = mock(BuildingDTO.class);
        when(vdmsBuilding.getBuilding_id()).thenReturn("b2");
        when(buildingRepository.getBuildingsByVdmsId("VDMS400")).thenReturn(Set.of(vdmsBuilding));
        when(recordChecklistService.getAllRecordChecklistByBuildings(any(), any(), any()))
                .thenReturn(Set.of());
        when(floorservice.getFloorsByBuildingId(null, null, "b2", null, null)).thenReturn(Set.of());

        Map<String, Object> result = service.syncLocationsFromBackend(req);

        assertThat(result).isNotNull();
        assertThat((Set<?>) result.get("buildings")).hasSize(1);
    }
}
