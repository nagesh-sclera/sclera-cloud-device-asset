package io.sclera.service;

import io.sclera.Repository.FloorRepository;
import io.sclera.Repository.VdmsRepository;
import io.sclera.client.APICallClient;
import io.sclera.dto.FloorDTO;
import io.sclera.dto.LocationDTO;
import io.sclera.dto.touchscreen.settings.VdmsDTO;
import io.sclera.utils.Utils;
import jakarta.servlet.http.HttpServletRequest;
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
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit coverage for the clean main methods of FloorService: pure string helpers, ADC sync,
 * delete-unlinked, the null-image CRUD paths (image/file/network branches avoided), floor
 * getters with location enrichment, and upsert routing. The image-processing, file-IO and
 * backend-sync methods are deferred (they touch the filesystem/network).
 */
@ExtendWith(MockitoExtension.class)
class FloorServiceTest {

    @Mock FloorRepository floorRepository;
    @Mock LocationService locationservice;
    @Mock Utils utils;
    @Mock VdmsRepository vdmsRepository;
    @Mock APICallClient apicallService;
    @Mock UserActionLogService userActionLogService;

    @InjectMocks FloorService service;

    private FloorDTO floor(String id) {
        FloorDTO f = new FloorDTO();
        f.setFloor_id(id);
        return f;
    }

    // ---- pure string helpers ---------------------------------------------

    @Test
    void compareIds_matchesPresentIdOnly() {
        assertThat(service.compareIds(Set.of("a", "b"), "b")).isTrue();
        assertThat(service.compareIds(Set.of("a", "b"), "z")).isFalse();
    }

    @Test
    void getExtensionByUrl_returnsExtension() {
        assertThat(service.getExtensionByUrl("http://x/y/z.png")).isEqualTo("png");
    }

    @Test
    void getFileNameByImageUrl_returnsLastPathSegment() {
        assertThat(service.getFileNameByImageUrl("http://x/y/z.png")).isEqualTo("z.png");
    }

    // ---- ADC sync ---------------------------------------------------------

    @Test
    void syncFloorToADCServer_syncsWithVdmsDetails() {
        FloorDTO f = floor("f1");
        when(vdmsRepository.getSyncDetailsForADC()).thenReturn(mock(VdmsDTO.class));
        when(apicallService.syncFloorToADC(any(), any(), any(), any())).thenReturn(true);

        service.syncFloorToADCServer("b1", f);

        verify(apicallService).syncFloorToADC(eq("b1"), any(), any(), any());
    }

    @Test
    void syncDeleteFloorToADC_callsDeleteOnApi() {
        when(vdmsRepository.getSyncDetailsForADC()).thenReturn(mock(VdmsDTO.class));
        when(apicallService.deleteFloorFromADC(any(), any(), any(), any())).thenReturn(true);

        service.syncDeleteFloorToADC("f1", "b1");

        verify(apicallService).deleteFloorFromADC(any(), any(), eq("b1"), any());
    }

    // ---- delete unlinked --------------------------------------------------

    @Test
    void deleteUnlikedFloors_deletesEachAndRemovesImageFile() {
        when(floorRepository.getUnlinkedFloorIds()).thenReturn(new LinkedHashSet<>(List.of("f1")));
        when(floorRepository.getImageUrlById("f1")).thenReturn("a.png");

        service.deleteUnlikedFloors();

        verify(floorRepository).deleteById("f1");
        verify(utils).removeFileFromServer(anyString(), eq("f1"), eq("png"));
    }

    // ---- add / update (null-image paths, no file IO) ---------------------

    @Test
    void addFloorByBuildingId_generatesIdAndSyncsWhenRowsAffected() {
        FloorDTO f = floor(null); // no image_url -> no file IO
        when(floorRepository.addFloorByBuildingId(anyString(), any(), any(), isNull(), eq("b1"), any(), any()))
                .thenReturn(1);
        when(vdmsRepository.getSyncDetailsForADC()).thenReturn(mock(VdmsDTO.class));
        when(apicallService.syncFloorToADC(any(), any(), any(), any())).thenReturn(true);

        String id = service.addFloorByBuildingId(f, "b1");

        assertThat(id).isNotNull();
        verify(apicallService).syncFloorToADC(any(), any(), any(), any());
        verify(locationservice).upsertLocationByFloorId(any(), anyString());
    }

    @Test
    void addFloorByBuildingId_noSyncWhenZeroRows() {
        FloorDTO f = floor("f1");
        when(floorRepository.addFloorByBuildingId(eq("f1"), any(), any(), isNull(), eq("b1"), any(), any()))
                .thenReturn(0);

        service.addFloorByBuildingId(f, "b1");

        verify(apicallService, never()).syncFloorToADC(any(), any(), any(), any());
    }

    @Test
    void updateFloorByFloorId_syncsWhenRowsAffected() {
        FloorDTO f = floor("f1");
        when(floorRepository.getImageUrlById("f1")).thenReturn(null); // no local image -> no file IO
        when(floorRepository.updateFloorByFloorId(any(), any(), isNull(), eq("f1"), any(), any())).thenReturn(1);
        when(vdmsRepository.getSyncDetailsForADC()).thenReturn(mock(VdmsDTO.class));
        when(apicallService.syncFloorToADC(any(), any(), any(), any())).thenReturn(true);

        service.updateFloorByFloorId(f, "b1");

        verify(apicallService).syncFloorToADC(any(), any(), any(), any());
        verify(locationservice).upsertLocationByFloorId(any(), eq("f1"));
    }

    @Test
    void updateFloorByFloorId_noSyncWhenZeroRows() {
        FloorDTO f = floor("f1");
        when(floorRepository.getImageUrlById("f1")).thenReturn(null);
        when(floorRepository.updateFloorByFloorId(any(), any(), isNull(), eq("f1"), any(), any())).thenReturn(0);

        service.updateFloorByFloorId(f, "b1");

        verify(apicallService, never()).syncFloorToADC(any(), any(), any(), any());
    }

    // ---- getters with enrichment -----------------------------------------

    @Test
    void getFloorByFloorId_enrichesWithLocations() {
        FloorDTO f = mock(FloorDTO.class);
        when(f.getFloor_id()).thenReturn("f1");
        when(floorRepository.getFloorById("f1")).thenReturn(f);
        Set<LocationDTO> locs = Set.of(mock(LocationDTO.class));
        when(locationservice.getLocationsByFloorId("f1", "v1")).thenReturn(locs);

        assertThat(service.getFloorByFloorId("u", "v1", "f1")).isSameAs(f);
        verify(f).setLocations(locs);
    }

    @Test
    void getFloorByLocationId_floorNull_returnsNull() {
        when(floorRepository.getFloorByLocationId("l1")).thenReturn(null);
        assertThat(service.getFloorByLocationId("l1")).isNull();
    }

    @Test
    void getFloorByLocationId_present_setsSingletonLocation() {
        FloorDTO f = mock(FloorDTO.class);
        when(floorRepository.getFloorByLocationId("l1")).thenReturn(f);
        when(locationservice.getLocationByLocationId("l1")).thenReturn(mock(LocationDTO.class));

        assertThat(service.getFloorByLocationId("l1")).isSameAs(f);
        verify(f).setLocations(any());
    }

    @Test
    void getFloorsDetailsByBuildingId_delegates() {
        Set<FloorDTO> set = Set.of(mock(FloorDTO.class));
        when(floorRepository.getFloorsDetailsByBuildingId("b1")).thenReturn(set);
        assertThat(service.getFloorsDetailsByBuildingId("b1")).isSameAs(set);
    }

    // ---- upsert routing (5-arg) ------------------------------------------

    @Test
    void upsertFloorsByBuildingId_existingFloor_upsertsAndReturnsFloors() {
        FloorDTO f = floor("f1"); // id present, no locations -> UPDATE, no location upsert
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getRequestURI()).thenReturn("/maps/floors");
        when(floorRepository.upsertFloorByBuildingId(eq("f1"), any(), any(), any(), eq("b1"), any()))
                .thenReturn(0); // 0 rows -> no ADC sync

        Set<FloorDTO> result = service.upsertFloorsByBuildingId("u", "v1", "b1", new LinkedHashSet<>(Set.of(f)), req);

        assertThat(result).containsExactly(f);
        verify(floorRepository).upsertFloorByBuildingId(eq("f1"), any(), any(), any(), eq("b1"), any());
    }
}
