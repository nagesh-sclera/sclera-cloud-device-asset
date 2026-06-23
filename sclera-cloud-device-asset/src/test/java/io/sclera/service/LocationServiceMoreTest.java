package io.sclera.service;

import io.sclera.Repository.FloorRepository;
import io.sclera.Repository.LocationRepository;
import io.sclera.Repository.VdmsRepository;
import io.sclera.client.APICallClient;
import io.sclera.client.RecordChecklistClient;
import io.sclera.dto.LocationDTO;
import io.sclera.dto.touchscreen.settings.VdmsDTO;
import io.sclera.models.Floor;
import io.sclera.models.Location;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Additional coverage for LocationService beyond the main test: record-checklist status/count
 * refresh, and the per-floor upsert (ADD insert path + UPDATE conflict path) including the
 * ADC sync and user-action audit logging.
 */
@ExtendWith(MockitoExtension.class)
class LocationServiceMoreTest {

    @Mock LocationRepository locationRepository;
    @Mock FloorRepository floorRepository;
    @Mock VdmsRepository vdmsRepository;
    @Mock APICallClient apicallService;
    @Mock RecordChecklistClient recordChecklistService;
    @Mock UserActionLogService userActionLogService;

    @InjectMocks LocationService service;

    private HttpServletRequest req() {
        HttpServletRequest r = mock(HttpServletRequest.class);
        when(r.getRequestURI()).thenReturn("/api/locations");
        return r;
    }

    private void stubAdcSync() {
        when(vdmsRepository.getSyncDetailsForADC()).thenReturn(mock(VdmsDTO.class));
        when(apicallService.syncLocationToADC(any(), any(), any(), any())).thenReturn(true);
    }

    @Test
    void updateLocationRecordChecklistStatus_todo_storesTodo() {
        when(recordChecklistService.getRecordChecklistStatusByLocationId("l1", "inspection"))
                .thenReturn("todo");
        service.updateLocationRecordChecklistStatus("l1", "inspection");
        verify(locationRepository).updateLocationRecordChecklistStatus("l1", "todo");
    }

    @Test
    void updateLocationRecordChecklistStatus_other_storesCompleted() {
        when(recordChecklistService.getRecordChecklistStatusByLocationId("l1", "inspection"))
                .thenReturn("done");
        service.updateLocationRecordChecklistStatus("l1", "inspection");
        verify(locationRepository).updateLocationRecordChecklistStatus("l1", "completed");
    }

    @Test
    void updateLocationRecordChecklistStatusById_refreshesStatusAndCount() {
        when(recordChecklistService.getRecordChecklistStatusByLocationId("l1", "insp")).thenReturn("todo");
        when(recordChecklistService.getChecklistStatusCountLocationId("l1", "inspection", "insp")).thenReturn(5);

        service.updateLocationRecordChecklistStatusById("l1", "insp");

        verify(locationRepository).updateLocationRecordChecklistStatus("l1", "todo");
        verify(locationRepository).updateLocationRecordChecklistCount("l1", 5);
    }

    @Test
    void upsertLocationsByFloorId_newLocation_insertsAndLogsAdd() {
        LocationDTO loc = new LocationDTO();
        loc.setName("Lobby"); // no id -> ADD / INSERT path
        when(locationRepository.findById(anyString())).thenReturn(Optional.empty());
        when(floorRepository.getReferenceById("f1")).thenReturn(mock(Floor.class));
        stubAdcSync();

        Set<LocationDTO> result = service.upsertLocationsByFloorId("u", "v1", "f1",
                new HashSet<>(Set.of(loc)), req());

        verify(locationRepository).save(any());
        verify(userActionLogService).addUserAction(anyString(), anyString(), eq("ADD"),
                anyString(), eq("success"), anyString(), anyString());
        org.assertj.core.api.Assertions.assertThat(result).hasSize(1);
    }

    @Test
    void upsertLocationByFloorId_existingLocation_updatesAndLogsUpdate() {
        LocationDTO loc = new LocationDTO();
        loc.setLocation_id("l1");
        loc.setName("Lobby");
        when(locationRepository.findById("l1")).thenReturn(Optional.of(mock(Location.class)));
        stubAdcSync();

        service.upsertLocationByFloorId("f1", loc, "u", "UPDATE", req());

        verify(locationRepository).save(any());
        verify(userActionLogService).addUserAction(anyString(), anyString(), eq("UPDATE"),
                anyString(), eq("success"), anyString(), anyString());
    }
}
