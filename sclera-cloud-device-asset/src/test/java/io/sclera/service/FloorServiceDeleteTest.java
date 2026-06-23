package io.sclera.service;

import io.sclera.Repository.FloorRepository;
import io.sclera.dto.FloorDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Coverage for FloorService.deleteFloorMapsForFloors and the deleteFloorsByIds guard that skips
 * deletion when the cloud map-delete returns null.
 */
@ExtendWith(MockitoExtension.class)
class FloorServiceDeleteTest {

    @Mock FloorRepository floorRepository;
    @Mock WebClientService webClientService;

    @InjectMocks FloorService service;

    @Test
    void deleteFloorMapsForFloors_delegatesToWebClient() {
        when(floorRepository.getFloorById("f1")).thenReturn(mock(FloorDTO.class));
        when(webClientService.deleteFloorMapsByImageUrl(eq("v1"), any())).thenReturn("ok");

        assertThat(service.deleteFloorMapsForFloors("v1", Set.of("f1"))).isEqualTo("ok");
    }

    @Test
    void deleteFloorsByIds_nullMapResponse_skipsDeletion() {
        when(floorRepository.getFloorById("f1")).thenReturn(mock(FloorDTO.class));
        when(webClientService.deleteFloorMapsByImageUrl(eq("v1"), any())).thenReturn(null);

        service.deleteFloorsByIds("u", "v1", Set.of("f1"), mock(HttpServletRequest.class));

        verify(floorRepository, never()).deleteById(any());
    }
}
