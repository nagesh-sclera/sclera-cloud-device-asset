package io.sclera.service;

import io.sclera.Repository.FloorRepository;
import io.sclera.client.RecordChecklistClient;
import io.sclera.dto.FloorDTO;
import io.sclera.dto.RecordChecklistDTO;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Additional coverage for FloorService beyond the main test: pure URL extension parsing, the
 * path get/update delegates with audit logging, and the record-checklist count enrichment of
 * getFloorsByBuildingId (field set vs null).
 */
@ExtendWith(MockitoExtension.class)
class FloorServiceMoreTest {

    @Mock FloorRepository floorRepository;
    @Mock UserActionLogService userActionLogService;
    @Mock RecordChecklistClient recordChecklistService;

    @InjectMocks FloorService service;

    @Test
    void getImageExtensionByImageUrl_extractsExtension() {
        assertThat(service.getImageExtensionByImageUrl("http://x/floor.png")).isEqualTo("png");
    }

    @Test
    void getFloorPathByFloorId_delegates() {
        when(floorRepository.getFloorPathByFloorId("f1")).thenReturn("/maps/f1");
        assertThat(service.getFloorPathByFloorId("u", "v", "f1")).isEqualTo("/maps/f1");
    }

    @Test
    void updatePathByFloorId_updatesLogsAndReturnsNewPath() {
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getRequestURI()).thenReturn("/api/floors");
        when(floorRepository.getFloorPathByFloorId("f1")).thenReturn("/maps/new");

        String result = service.updatePathByFloorId("u", "f1", "/maps/new", req);

        assertThat(result).isEqualTo("/maps/new");
        verify(floorRepository).updatePathByFloorId("/maps/new", "f1");
        verify(userActionLogService).addUserAction(eq("u"), eq("maps"), eq("UPDATE"),
                any(), eq("success"), eq("floor"), eq("f1"));
    }

    @Test
    void getFloorsByBuildingId_allTaskField_setsCounts() {
        FloorDTO floor = mock(FloorDTO.class);
        when(floor.getFloor_id()).thenReturn("f1");
        when(floorRepository.getFloorsByBuildingId("b1")).thenReturn(new HashSet<>(Set.of(floor)));
        RecordChecklistDTO rc = mock(RecordChecklistDTO.class);
        when(rc.getFloor_id()).thenReturn("f1");
        when(rc.getRecord_type()).thenReturn("checklist");
        when(rc.getInspection_record_id()).thenReturn(null); // -> tagged
        when(recordChecklistService.getAllRecordChecklistByBuildings(any(), any(), any()))
                .thenReturn(new HashSet<>(Set.of(rc)));

        Set<FloorDTO> result = service.getFloorsByBuildingId("u", "v", "b1", "all_task", null);

        assertThat(result).hasSize(1);
        verify(floor).setCounts(any());
    }

    @Test
    void getFloorsByBuildingId_nullField_noCounts() {
        FloorDTO floor = mock(FloorDTO.class);
        when(floor.getFloor_id()).thenReturn("f1");
        when(floorRepository.getFloorsByBuildingId("b1")).thenReturn(new HashSet<>(Set.of(floor)));
        when(recordChecklistService.getAllRecordChecklistByBuildings(any(), any(), any()))
                .thenReturn(Set.of());

        service.getFloorsByBuildingId("u", "v", "b1", null, null);

        verify(floor, never()).setCounts(any());
    }
}
