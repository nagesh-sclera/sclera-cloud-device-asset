package io.sclera.service;

import io.sclera.Repository.DeviceTechnicianAISuggestionRepository;
import io.sclera.dto.DeviceTechnicianAISuggestionDTO;
import io.sclera.dto.TechnicianDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit coverage for DeviceTechnicianAISuggestionService: upsert row-affected collection, create id
 * generation, update null-id skip, delegate reads, and the JSON-decoded skill-profile suggestion
 * lookup (empty input and per-technician enrichment with null filtering).
 */
@ExtendWith(MockitoExtension.class)
class DeviceTechnicianAISuggestionServiceTest {

    @Mock DeviceTechnicianAISuggestionRepository deviceTechnicianAISuggestionRepository;
    @Mock TechnicianService technicianService;

    @InjectMocks DeviceTechnicianAISuggestionService service;

    private DeviceTechnicianAISuggestionDTO dto(String id) {
        DeviceTechnicianAISuggestionDTO d = mock(DeviceTechnicianAISuggestionDTO.class);
        lenient().when(d.getId()).thenReturn(id);
        return d;
    }

    @Test
    void upsert_null_returnsEmpty() {
        assertThat(service.upsertTechnicianSuggestion(null)).isEmpty();
    }

    @Test
    void upsert_rowsAffected_collectsId() {
        when(deviceTechnicianAISuggestionRepository.upsertTechnicianSuggestion(any(), any(), any(), any()))
                .thenReturn(1);
        assertThat(service.upsertTechnicianSuggestion(List.of(dto("s1")))).containsExactly("s1");
    }

    @Test
    void upsert_noRows_doesNotCollectId() {
        when(deviceTechnicianAISuggestionRepository.upsertTechnicianSuggestion(any(), any(), any(), any()))
                .thenReturn(0);
        assertThat(service.upsertTechnicianSuggestion(List.of(dto("s1")))).isEmpty();
    }

    @Test
    void create_setsIdAndPersists() {
        DeviceTechnicianAISuggestionDTO d = dto(null);
        service.createTechnicianSuggestion(d, null);
        verify(d).setId(any());
        verify(deviceTechnicianAISuggestionRepository).createTechnicianSuggestion(any(), any(), any(), any());
    }

    @Test
    void update_nullId_skips() {
        service.updateTechnicianSuggestion(dto(null), null);
        verify(deviceTechnicianAISuggestionRepository, never())
                .updateTechnicianSuggestion(any(), any(), any(), any());
    }

    @Test
    void update_withId_persists() {
        service.updateTechnicianSuggestion(dto("s1"), null);
        verify(deviceTechnicianAISuggestionRepository).updateTechnicianSuggestion(any(), any(), any(), any());
    }

    @Test
    void getAlldevicetechnician_delegates() {
        List<DeviceTechnicianAISuggestionDTO> all = List.of(mock(DeviceTechnicianAISuggestionDTO.class));
        when(deviceTechnicianAISuggestionRepository.getAlldevicetechnician()).thenReturn(all);
        assertThat(service.getAlldevicetechnician(null)).isSameAs(all);
    }

    // ---- getDeviceTechnicianAISuggestionsByDeviceType --------------------

    @Test
    void suggestionsByDeviceType_nullJson_returnsEmpty() {
        when(deviceTechnicianAISuggestionRepository.getDeviceTechnicianAISuggestionByDeviceType("printer", "v1"))
                .thenReturn(null);
        assertThat(service.getDeviceTechnicianAISuggestionsByDeviceType("printer", "v1", null)).isEmpty();
    }

    @Test
    void suggestionsByDeviceType_parsesJsonAndEnriches_filteringNulls() {
        when(deviceTechnicianAISuggestionRepository.getDeviceTechnicianAISuggestionByDeviceType("printer", "v1"))
                .thenReturn("[\"t1\",\"t2\"]");
        TechnicianDTO t1 = mock(TechnicianDTO.class);
        when(technicianService.getTechnicianSkillProfileWithPrimarySkillAndAvailabilityById("t1")).thenReturn(t1);
        when(technicianService.getTechnicianSkillProfileWithPrimarySkillAndAvailabilityById("t2")).thenReturn(null);

        List<TechnicianDTO> result = service.getDeviceTechnicianAISuggestionsByDeviceType("printer", "v1", null);

        assertThat(result).containsExactly(t1);
    }
}
