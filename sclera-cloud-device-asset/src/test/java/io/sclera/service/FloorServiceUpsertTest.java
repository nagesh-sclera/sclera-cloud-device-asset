package io.sclera.service;

import io.sclera.Repository.FloorRepository;
import io.sclera.dto.FloorDTO;
import io.sclera.dto.LocationDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Coverage for FloorService.upsertFloorsByBuildingId - the per-floor upsert loop (ADD insert path +
 * UPDATE path) with audit logging and nested location upsert.
 */
@ExtendWith(MockitoExtension.class)
class FloorServiceUpsertTest {

    @Mock FloorRepository floorRepository;
    @Mock UserActionLogService userActionLogService;
    @Mock LocationService locationservice;

    @InjectMocks FloorService service;

    private HttpServletRequest req() {
        HttpServletRequest r = mock(HttpServletRequest.class);
        when(r.getRequestURI()).thenReturn("/api/floors");
        return r;
    }

    @Test
    void upsertFloorsByBuildingId_newFloor_generatesIdAndLogsAdd() {
        FloorDTO floor = new FloorDTO();
        floor.setName("Ground"); // no id -> ADD
        when(floorRepository.upsertFloorByBuildingId(anyString(), any(), any(), any(), eq("b1"), any()))
                .thenReturn(0); // no rows -> skip ADC sync

        Set<FloorDTO> result = service.upsertFloorsByBuildingId("u", "v1", "b1",
                new HashSet<>(Set.of(floor)), req());

        assertThat(result).hasSize(1);
        assertThat(floor.getFloor_id()).isNotNull();
        verify(userActionLogService).addUserAction(anyString(), anyString(), eq("ADD"),
                anyString(), eq("success"), anyString(), anyString());
    }

    @Test
    void upsertFloorsByBuildingId_existingFloorWithLocations_updatesAndUpsertsLocations() {
        FloorDTO floor = new FloorDTO();
        floor.setFloor_id("f1");
        floor.setName("First");
        floor.setLocations(new HashSet<>(Set.of(mock(LocationDTO.class))));
        when(floorRepository.upsertFloorByBuildingId(eq("f1"), any(), any(), any(), eq("b1"), any()))
                .thenReturn(0);
        when(locationservice.upsertLocationsByFloorId(eq("u"), eq("v1"), eq("f1"), any(), any()))
                .thenReturn(Set.of());

        service.upsertFloorsByBuildingId("u", "v1", "b1", new HashSet<>(Set.of(floor)), req());

        verify(userActionLogService).addUserAction(anyString(), anyString(), eq("UPDATE"),
                anyString(), eq("success"), anyString(), anyString());
        verify(locationservice).upsertLocationsByFloorId(eq("u"), eq("v1"), eq("f1"), any(), any());
    }
}
