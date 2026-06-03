package io.sclera.service;

import io.sclera.Repository.TechnicianAvailabilityRepository;
import io.sclera.dto.TechnicianAvailabilityDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit coverage for TechnicianAvailabilityService: upsert row-affected collection, delete
 * existing-id resolution, create/update row-count validation, and delegate reads.
 */
@ExtendWith(MockitoExtension.class)
class TechnicianAvailabilityServiceTest {

    @Mock TechnicianAvailabilityRepository technicianAvailabilityRepository;

    @InjectMocks TechnicianAvailabilityService service;

    private TechnicianAvailabilityDTO dto(String id) {
        TechnicianAvailabilityDTO d = mock(TechnicianAvailabilityDTO.class);
        lenient().when(d.getId()).thenReturn(id);
        return d;
    }

    // ---- upsert ----------------------------------------------------------

    @Test
    void upsert_null_returnsEmpty() {
        assertThat(service.upsertTechnicianAvailability(null)).isEmpty();
    }

    @Test
    void upsert_rowsAffected_collectsId() {
        when(technicianAvailabilityRepository.upsertTechnicianAvailability(
                any(), any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(1);

        assertThat(service.upsertTechnicianAvailability(List.of(dto("a1")))).containsExactly("a1");
    }

    @Test
    void upsert_noRows_doesNotCollectId() {
        when(technicianAvailabilityRepository.upsertTechnicianAvailability(
                any(), any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(0);

        assertThat(service.upsertTechnicianAvailability(List.of(dto("a1")))).isEmpty();
    }

    // ---- delete ----------------------------------------------------------

    @Test
    void delete_null_returnsEmpty() {
        assertThat(service.deleteTechnicianAvailabilityById(null)).isEmpty();
    }

    @Test
    void delete_existing_deletesAndReturnsIds() {
        when(technicianAvailabilityRepository.findExistingTechnicianAvailabilityByIds(any()))
                .thenReturn(Set.of("a1"));
        when(technicianAvailabilityRepository.deleteTechnicianAvailabilityByIds(anySet())).thenReturn(1);

        assertThat(service.deleteTechnicianAvailabilityById(List.of(dto("a1")))).containsExactly("a1");
        verify(technicianAvailabilityRepository).deleteTechnicianAvailabilityByIds(anySet());
    }

    @Test
    void delete_noneExisting_returnsEmpty() {
        when(technicianAvailabilityRepository.findExistingTechnicianAvailabilityByIds(any()))
                .thenReturn(Set.of());

        assertThat(service.deleteTechnicianAvailabilityById(List.of(dto("a1")))).isEmpty();
        verify(technicianAvailabilityRepository, never()).deleteTechnicianAvailabilityByIds(anySet());
    }

    // ---- create ----------------------------------------------------------

    @Test
    void create_success_setsIdAndPersists() {
        when(technicianAvailabilityRepository.createTechnicianAvailability(
                any(), any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(1);

        TechnicianAvailabilityDTO d = dto(null);
        service.createTechnicianAvailability(d);

        verify(d).setId(any());
        verify(technicianAvailabilityRepository).createTechnicianAvailability(
                any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void create_noRows_throws() {
        when(technicianAvailabilityRepository.createTechnicianAvailability(
                any(), any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(0);

        assertThatThrownBy(() -> service.createTechnicianAvailability(dto(null)))
                .isInstanceOf(RuntimeException.class);
    }

    // ---- update ----------------------------------------------------------

    @Test
    void update_nullId_skips() {
        service.updateTechnicianAvailability(dto(null));
        verify(technicianAvailabilityRepository, never()).updateTechnicianAvailability(
                any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void update_withId_persists() {
        when(technicianAvailabilityRepository.updateTechnicianAvailability(
                any(), any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(1);

        service.updateTechnicianAvailability(dto("a1"));

        verify(technicianAvailabilityRepository).updateTechnicianAvailability(
                any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    // ---- reads -----------------------------------------------------------

    @Test
    void getTechnicianAvailabilityById_delegates() {
        TechnicianAvailabilityDTO d = mock(TechnicianAvailabilityDTO.class);
        when(technicianAvailabilityRepository.getTechnicianAvailabilityById("a1")).thenReturn(d);
        assertThat(service.getTechnicianAvailabilityById("a1", null)).isSameAs(d);
    }

    @Test
    void getAvailabilityInRange_delegates() {
        List<TechnicianAvailabilityDTO> list = List.of(mock(TechnicianAvailabilityDTO.class));
        when(technicianAvailabilityRepository.getTechnicianAvailabilityInRange("t1", "s", "e")).thenReturn(list);
        assertThat(service.getAvailabilityInRange("t1", "s", "e", null)).isSameAs(list);
    }
}
