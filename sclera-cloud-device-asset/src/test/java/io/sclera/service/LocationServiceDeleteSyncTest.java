package io.sclera.service;

import io.sclera.Repository.FloorRepository;
import io.sclera.Repository.LocationRepository;
import io.sclera.Repository.VdmsRepository;
import io.sclera.client.APICallClient;
import io.sclera.dto.LocationDTO;
import io.sclera.dto.touchscreen.settings.VdmsDTO;
import io.sclera.models.Floor;
import io.sclera.models.Location;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Coverage for LocationService ADC-delete sync, per-floor delete (empty path), and the backend-sync
 * upsert (INSERT path) with ADC sync.
 */
@ExtendWith(MockitoExtension.class)
class LocationServiceDeleteSyncTest {

    @Mock LocationRepository locationRepository;
    @Mock FloorRepository floorRepository;
    @Mock VdmsRepository vdmsRepository;
    @Mock APICallClient apicallService;

    @InjectMocks LocationService service;

    @Test
    void syncDeleteLocationToADC_callsDeleteOnApi() {
        when(vdmsRepository.getSyncDetailsForADC()).thenReturn(mock(VdmsDTO.class));
        when(apicallService.deleteLocationFromADC(any(), any(), any(), any(), any())).thenReturn(true);

        service.syncDeleteLocationToADC("l1", "b1", "f1");

        verify(apicallService).deleteLocationFromADC(any(), any(), any(), any(), any());
    }

    @Test
    void deleteLocationsByFloorId_noLocations_doesNothing() {
        when(locationRepository.getLocationIdsByFloorId("f1")).thenReturn(Set.of());

        service.deleteLocationsByFloorId("f1", "u", false);

        verify(locationRepository).getLocationIdsByFloorId("f1");
    }

    @Test
    void upsertLocationByFloorIdBackendSync_newLocation_insertsAndSyncs() {
        LocationDTO loc = new LocationDTO();
        loc.setLocation_id("l1");
        loc.setName("Lobby");
        when(locationRepository.findById("l1")).thenReturn(Optional.empty());
        when(floorRepository.getReferenceById("f1")).thenReturn(mock(Floor.class));
        when(vdmsRepository.getSyncDetailsForADC()).thenReturn(mock(VdmsDTO.class));
        when(apicallService.syncLocationToADC(any(), any(), any(), any())).thenReturn(true);

        service.upsertLocationByFloorIdBackendSync("f1", loc);

        verify(locationRepository).save(any(Location.class));
        verify(apicallService).syncLocationToADC(any(), any(), any(), any());
    }
}
