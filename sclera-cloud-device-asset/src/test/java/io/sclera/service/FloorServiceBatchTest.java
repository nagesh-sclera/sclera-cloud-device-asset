package io.sclera.service;

import io.sclera.Repository.FloorRepository;
import io.sclera.dto.FloorDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Coverage for FloorService batch/sync helpers not exercised elsewhere: the initial-position JSON
 * parser, paginated batch fetches, the webClient-backed image/tile sync methods, zoom-level
 * delegation, and processFloorDeletions (cloud-image delete + per-floor success/failure logging).
 *
 * DEFERRED: file-IO methods (addFloorImageByFloorId, addFileToServer, getBytesArrayByImageUrl) which
 * touch the local filesystem / network, and the larger upsert/orientation orchestration paths.
 */
@ExtendWith(MockitoExtension.class)
class FloorServiceBatchTest {

    @Mock FloorRepository floorRepository;
    @Mock WebClientService webClientService;
    @Mock LocationService locationservice;
    @Mock UserActionLogService userActionLogService;

    @InjectMocks FloorService service;

    // ---- pure helpers ----

    @Test
    void mapInitialPositionToCoordinates_parsesCorners() {
        String json = "[[\"12.1\",\"77.2\"],[\"12.0\",\"77.3\"]]";
        FloorDTO floor = service.mapInitialPositionToCoordinates(json, "f1");
        assertThat(floor.getTopleft_latitude()).isEqualTo("12.1");
        assertThat(floor.getTopleft_longitude()).isEqualTo("77.2");
        assertThat(floor.getBottomright_latitude()).isEqualTo("12.0");
        assertThat(floor.getBottomright_longitude()).isEqualTo("77.3");
        assertThat(floor.getFloor_id()).isEqualTo("f1");
    }

    @Test
    void mapInitialPositionToCoordinates_nullInput_returnsEmptyDto() {
        FloorDTO floor = service.mapInitialPositionToCoordinates(null, "f1");
        assertThat(floor.getFloor_id()).isNull();
    }

    @Test
    void mapInitialPositionToCoordinates_malformed_swallowsAndReturnsEmpty() {
        FloorDTO floor = service.mapInitialPositionToCoordinates("not-json", "f1");
        // parse failure is caught; corners stay unset
        assertThat(floor.getTopleft_latitude()).isNull();
    }

    @Test
    void removeTimestampFromFileName_stripsTrailingTimestamp() {
        assertThat(service.removeTimestampFromFileName("floor_1700000000")).isEqualTo("floor");
    }

    // ---- pagination ----

    @Test
    void getBatchFloorsByPagination_singlePartialPage_returnsResults() {
        FloorDTO f = mock(FloorDTO.class);
        when(floorRepository.getBatchFloorsByPagination(any(), any())).thenReturn(List.of(f));
        List<FloorDTO> result = service.getBatchFloorsByPagination(Set.of("f1"));
        assertThat(result).containsExactly(f);
    }

    @Test
    void getBatchFloorsByPagination_emptyFirstPage_returnsEmpty() {
        when(floorRepository.getBatchFloorsByPagination(any(), any())).thenReturn(List.of());
        assertThat(service.getBatchFloorsByPagination(Set.of("f1"))).isEmpty();
    }

    @Test
    void getFloorsByBuildingIds_singlePartialPage_returnsResults() {
        FloorDTO f = mock(FloorDTO.class);
        when(floorRepository.getFloorIdsByBuildingIds(any(), any())).thenReturn(List.of(f));
        assertThat(service.getFloorsByBuildingIds(Set.of("b1"))).containsExactly(f);
    }

    // ---- webClient-backed sync ----

    @Test
    void updateFloorImages_persistsResolvedUrls_returnsUnresolved() {
        FloorDTO resolved = mock(FloorDTO.class);
        when(resolved.getImage_url()).thenReturn("http://img/1.png");
        when(resolved.getLocal_image_url()).thenReturn("/local/1.png");
        when(resolved.getFloor_id()).thenReturn("f1");

        FloorDTO unresolved = mock(FloorDTO.class);
        when(unresolved.getImage_url()).thenReturn(null);

        when(webClientService.syncFloorMapImageByFloorId(eq("v1"), any()))
                .thenReturn(List.of(resolved, unresolved));

        List<FloorDTO> response = service.updateFloorImages("v1", List.of());

        verify(floorRepository).updateImageUrls("/local/1.png", "http://img/1.png", "f1");
        assertThat(response).containsExactly(unresolved);
    }

    @Test
    void updateAllFloors_uploadsEachAndAggregates() {
        FloorDTO in = mock(FloorDTO.class);
        FloorDTO out = mock(FloorDTO.class);
        when(webClientService.uploadFloorImages(eq("v1"), any())).thenReturn(List.of(out));
        List<FloorDTO> response = service.updateAllFloors("v1", List.of(in));
        assertThat(response).containsExactly(out);
    }

    @Test
    void syncFloorMapsTiles_updatesZoomOrRefetchesByImagePresence() {
        FloorDTO withImage = mock(FloorDTO.class);
        when(withImage.getImage_url()).thenReturn("http://img/2.png");
        when(withImage.getMin_zoom()).thenReturn("1");
        when(withImage.getMax_zoom()).thenReturn("5");
        when(withImage.getFloor_id()).thenReturn("f2");

        FloorDTO noImage = mock(FloorDTO.class);
        when(noImage.getImage_url()).thenReturn(null);
        when(noImage.getFloor_id()).thenReturn("f3");

        FloorDTO refetched = mock(FloorDTO.class);
        when(floorRepository.getFloorById("f3")).thenReturn(refetched);
        when(webClientService.syncFloorMapTilesFolder(eq("v1"), any()))
                .thenReturn(List.of(withImage, noImage));

        List<FloorDTO> stillMissing = service.syncFloorMapsTiles("v1", List.of());

        verify(floorRepository).updateFloorMapZoomLevels("1", "5", "f2");
        assertThat(stillMissing).containsExactly(refetched);
    }

    @Test
    void updateZoomLevels_delegatesPerFloor() {
        FloorDTO floor = mock(FloorDTO.class);
        when(floor.getFloor_id()).thenReturn("f1");
        when(floor.getMin_zoom()).thenReturn("0");
        when(floor.getMax_zoom()).thenReturn("9");
        service.updateZoomLevels(List.of(floor));
        verify(floorRepository).updateFloorMapZoomLevels("0", "9", "f1");
    }

    // ---- processFloorDeletions ----

    @Test
    void processFloorDeletions_deletesCloudImageThenFloor_logsSuccess() {
        FloorDTO floor = mock(FloorDTO.class);
        when(floor.getImage_url()).thenReturn("http://img/del.png");
        when(floor.getLocal_image_url()).thenReturn(null);
        when(floor.getFloor_id()).thenReturn("f1");
        when(floor.getName()).thenReturn("Ground");

        service.processFloorDeletions("u", "v1", List.of(floor));

        verify(webClientService).deleteFloorMapsByImageUrl(eq("v1"), any());
        verify(locationservice).deleteLocationsByFloorId("f1", "u", true);
        verify(floorRepository).deleteById("f1");
        verify(userActionLogService).addUserAction(eq("u"), eq("maps"), eq("DELETE"),
                any(), eq("success"), eq("floor"), eq("f1"));
    }

    @Test
    void processFloorDeletions_deleteFailure_logsFailed() {
        FloorDTO floor = mock(FloorDTO.class);
        when(floor.getImage_url()).thenReturn(null);
        when(floor.getLocal_image_url()).thenReturn(null);
        when(floor.getFloor_id()).thenReturn("f1");
        when(floor.getName()).thenReturn("Ground");
        doThrow(new RuntimeException("boom"))
                .when(locationservice).deleteLocationsByFloorId(anyString(), anyString(), eq(true));

        service.processFloorDeletions("u", "v1", List.of(floor));

        verify(webClientService, never()).deleteFloorMapsByImageUrl(any(), any());
        verify(userActionLogService).addUserAction(eq("u"), eq("maps"), eq("DELETE"),
                any(), eq("failed"), eq("floor"), eq("f1"));
    }
}
