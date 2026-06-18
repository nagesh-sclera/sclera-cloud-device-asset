package io.sclera.service;

import com.alibaba.fastjson.JSONArray;
import io.sclera.Repository.FloorRepository;
import io.sclera.Repository.LocationRepository;
import io.sclera.Repository.VdmsRepository;
import io.sclera.client.APICallClient;
import io.sclera.client.RecordChecklistClient;
import io.sclera.models.Floor;
import io.sclera.models.Location;
import io.sclera.dto.LocationAlertDTO;
import io.sclera.dto.LocationDTO;
import io.sclera.dto.touchscreen.settings.VdmsDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
 * Unit coverage for the clean main methods of LocationService. The large JSON-filter
 * pagination methods (getAllLocationsPagination, getLocationsByFloorByPagination,
 * searchSortFilterLocationsCount, getAllLocationsByGroup) are intentionally deferred —
 * they need elaborate JSONObject filter fixtures and warrant their own focused round.
 */
@ExtendWith(MockitoExtension.class)
class LocationServiceTest {

    @Mock LocationRepository locationRepository;
    @Mock FloorRepository floorRepository;
    @Mock VdmsRepository vdmsRepository;
    @Mock APICallClient apicallService;
    @Mock RecordChecklistClient recordChecklistService;

    @InjectMocks LocationService service;

    private LocationDTO loc(String id) {
        LocationDTO l = new LocationDTO();
        l.setLocation_id(id);
        return l;
    }

    // ---- pure / delegate methods -----------------------------------------

    @Test
    void compareIds_matchesPresentIdOnly() {
        assertThat(service.compareIds(Set.of("a", "b"), "b")).isTrue();
        assertThat(service.compareIds(Set.of("a", "b"), "z")).isFalse();
    }

    @Test
    void checkLocationById_trueWhenCountPositive() {
        when(locationRepository.checkLocationById("L1")).thenReturn(1);
        assertThat(service.checkLocationById("L1")).isTrue();
    }

    @Test
    void checkLocationById_falseWhenCountZero() {
        when(locationRepository.checkLocationById("L1")).thenReturn(0);
        assertThat(service.checkLocationById("L1")).isFalse();
    }

    @Test
    void getPositionByLocationId_delegates() {
        when(locationRepository.getPositionByLocationId("L1")).thenReturn("pos");
        assertThat(service.getPositionByLocationId("L1")).isEqualTo("pos");
    }

    @Test
    void getLocationDetails_delegates() {
        LocationDTO l = mock(LocationDTO.class);
        when(locationRepository.getLocationDetails("L1")).thenReturn(l);
        assertThat(service.getLocationDetails("L1")).isSameAs(l);
    }

    @Test
    void getLocationByLocationId_delegates() {
        LocationDTO l = mock(LocationDTO.class);
        when(locationRepository.getLocationByLocationId("L1")).thenReturn(l);
        assertThat(service.getLocationByLocationId("L1")).isSameAs(l);
    }

    @Test
    void getLocationsByVdmsId_delegates() {
        Set<LocationDTO> set = Set.of(mock(LocationDTO.class));
        when(locationRepository.getLocationByVdmsId("v1")).thenReturn(set);
        assertThat(service.getLocationsByVdmsId("u", "v1")).isSameAs(set);
    }

    @Test
    void getLocationsByFloor_delegates() {
        Set<LocationDTO> set = Set.of(mock(LocationDTO.class));
        when(locationRepository.getLocationsByFloor("L1")).thenReturn(set);
        assertThat(service.getLocationsByFloor("u", "v", "L1")).isSameAs(set);
    }

    @Test
    void getLocationsCountByFloorId_delegates() {
        when(locationRepository.getLocationsCountByFloorId("f1", "key")).thenReturn("7");
        assertThat(service.getLocationsCountByFloorId("u", "v", "f1", "key")).isEqualTo("7");
    }

    @Test
    void getLocationAlertDetails_delegates() {
        LocationAlertDTO a = mock(LocationAlertDTO.class);
        when(locationRepository.getLocationAlertDetails("L1")).thenReturn(a);
        assertThat(service.getLocationAlertDetails("L1")).isSameAs(a);
    }

    @Test
    void deleteUnlinkedLocations_deletesEachUnlinked() {
        when(locationRepository.getUnlinkedLocationIds())
                .thenReturn(new LinkedHashSet<>(List.of("a", "b")));
        service.deleteUnlinkedLocations();
        verify(locationRepository).deleteById("a");
        verify(locationRepository).deleteById("b");
    }

    // ---- add / update with ADC sync branch -------------------------------

    @Test
    void addLocationByFloorId_generatesIdAndSyncs() {
        // addLocationByFloorId now does find-or-create save() — always syncs after save
        LocationDTO l = loc(null); // id null -> service generates one
        when(floorRepository.getReferenceById(eq("f1"))).thenReturn(mock(Floor.class));
        when(locationRepository.save(any(Location.class))).thenAnswer(inv -> inv.getArgument(0));
        when(vdmsRepository.getSyncDetailsForADC()).thenReturn(mock(VdmsDTO.class));
        when(apicallService.syncLocationToADC(any(), any(), any(), any())).thenReturn(true);

        String id = service.addLocationByFloorId(l, "f1");

        assertThat(id).isNotNull();
        verify(locationRepository).save(any(Location.class));
        verify(apicallService).syncLocationToADC(any(), eq("f1"), any(), any());
    }

    @Test
    void addLocationByFloorId_withExistingId_savesAndSyncs() {
        // Plain insert path: always saves and syncs (no rowsAffected check in new implementation)
        LocationDTO l = loc("L1");
        when(floorRepository.getReferenceById(eq("f1"))).thenReturn(mock(Floor.class));
        when(locationRepository.save(any(Location.class))).thenAnswer(inv -> inv.getArgument(0));
        when(vdmsRepository.getSyncDetailsForADC()).thenReturn(mock(VdmsDTO.class));
        when(apicallService.syncLocationToADC(any(), any(), any(), any())).thenReturn(true);

        String id = service.addLocationByFloorId(l, "f1");

        assertThat(id).isEqualTo("L1");
        verify(locationRepository).save(any(Location.class));
        verify(apicallService).syncLocationToADC(any(), eq("f1"), any(), any());
    }

    @Test
    void updateLocationByLocationId_syncsWhenRowsAffected() {
        LocationDTO l = loc("L1");
        when(locationRepository.updateLocationByLocationId(any(), any(), eq("L1"), any(), any(), any()))
                .thenReturn(1);
        when(vdmsRepository.getSyncDetailsForADC()).thenReturn(mock(VdmsDTO.class));
        when(apicallService.syncLocationToADC(any(), any(), any(), any())).thenReturn(true);

        service.updateLocationByLocationId(l);

        verify(apicallService).syncLocationToADC(any(), any(), any(), any());
    }

    @Test
    void updateLocationByLocationId_noSyncWhenZeroRows() {
        LocationDTO l = loc("L1");
        when(locationRepository.updateLocationByLocationId(any(), any(), eq("L1"), any(), any(), any()))
                .thenReturn(0);

        service.updateLocationByLocationId(l);

        verify(apicallService, never()).syncLocationToADC(any(), any(), any(), any());
    }

    // ---- record-checklist status / count ---------------------------------

    @Test
    void updateLocationRecordChecklistStatus_todo_setsTodo() {
        when(recordChecklistService.getRecordChecklistStatusByLocationId("L1", "inspection"))
                .thenReturn("todo");
        service.updateLocationRecordChecklistStatus("L1", "inspection");
        verify(locationRepository).updateLocationRecordChecklistStatus("L1", "todo");
    }

    @Test
    void updateLocationRecordChecklistStatus_other_setsCompleted() {
        when(recordChecklistService.getRecordChecklistStatusByLocationId("L1", "inspection"))
                .thenReturn("done");
        service.updateLocationRecordChecklistStatus("L1", "inspection");
        verify(locationRepository).updateLocationRecordChecklistStatus("L1", "completed");
    }

    @Test
    void updateLocationRecordChecklistStatus_nullId_skips() {
        service.updateLocationRecordChecklistStatus(null, "inspection");
        verify(locationRepository, never()).updateLocationRecordChecklistStatus(any(), any());
    }

    @Test
    void updateLocationRecordChecklistCount_delegates() {
        when(recordChecklistService.getChecklistStatusCountLocationId("L1", "inspection", "insp"))
                .thenReturn(4);
        service.updateLocationRecordChecklistCount("L1", "insp");
        verify(locationRepository).updateLocationRecordChecklistCount("L1", 4);
    }

    // ---- qr / nfc tagged-location counts ---------------------------------

    @Test
    void getQrCodeLocationCountByVdmsId_countsResolvedLocations() {
        JSONArray arr = new JSONArray();
        arr.add("loc1");
        when(apicallService.getQrCodeIdsByVdmsIdAndType("v1", "location")).thenReturn(arr);
        when(locationRepository.getLocationIds(any())).thenReturn(Set.of("loc1", "loc2"));

        assertThat(service.getQrCodeLocationCountByVdmsId("v1")).isEqualTo(2);
    }

    @Test
    void getQrCodeLocationCountByVdmsId_zeroWhenNoQrIds() {
        when(apicallService.getQrCodeIdsByVdmsIdAndType("v1", "location")).thenReturn(null);
        assertThat(service.getQrCodeLocationCountByVdmsId("v1")).isEqualTo(0);
    }

    @Test
    void getNfcLocationCountByVdmsId_countsResolvedLocations() {
        JSONArray arr = new JSONArray();
        arr.add("loc1");
        when(apicallService.getNfcIdsByVdmsAndType("v1", "location")).thenReturn(arr);
        when(locationRepository.getLocationIds(any())).thenReturn(Set.of("loc1"));

        assertThat(service.getNfcLocationCountByVdmsId("v1")).isEqualTo(1);
    }

    @Test
    void getNfcLocationCountByVdmsId_zeroWhenNoNfcIds() {
        when(apicallService.getNfcIdsByVdmsAndType("v1", "location")).thenReturn(null);
        assertThat(service.getNfcLocationCountByVdmsId("v1")).isEqualTo(0);
    }
}
