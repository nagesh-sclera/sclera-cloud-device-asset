package io.sclera.service;

import io.sclera.Repository.TechnicianSkillRepository;
import io.sclera.dto.TechnicianSkillDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit coverage for TechnicianSkillService: upsert row-affected collection, create id generation,
 * update null-id skip, delete existing-id resolution, and delegate reads.
 */
@ExtendWith(MockitoExtension.class)
class TechnicianSkillServiceTest {

    @Mock TechnicianSkillRepository technicianSkillRepository;

    @InjectMocks TechnicianSkillService service;

    private TechnicianSkillDTO dto(String id) {
        TechnicianSkillDTO d = mock(TechnicianSkillDTO.class);
        lenient().when(d.getId()).thenReturn(id);
        return d;
    }

    @Test
    void upsert_null_returnsEmpty() {
        assertThat(service.upsertTechnicianSkill(null)).isEmpty();
    }

    @Test
    void upsert_rowsAffected_collectsId() {
        when(technicianSkillRepository.upsertTechnicianSkill(
                any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(1);
        assertThat(service.upsertTechnicianSkill(List.of(dto("s1")))).containsExactly("s1");
    }

    @Test
    void upsert_noRows_doesNotCollectId() {
        when(technicianSkillRepository.upsertTechnicianSkill(
                any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(0);
        assertThat(service.upsertTechnicianSkill(List.of(dto("s1")))).isEmpty();
    }

    @Test
    void create_setsIdAndPersists() {
        TechnicianSkillDTO d = dto(null);
        service.createTechnicianSkill(d);
        verify(d).setId(any());
        verify(technicianSkillRepository).createTechnicianSkill(
                any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void update_nullId_skips() {
        service.updateTechnicianSkill(dto(null));
        verify(technicianSkillRepository, never()).updateTechnicianSkill(
                any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void update_withId_persists() {
        service.updateTechnicianSkill(dto("s1"));
        verify(technicianSkillRepository).updateTechnicianSkill(
                any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void delete_existing_deletesAndReturnsIds() {
        when(technicianSkillRepository.findExistingTechnicianSkillsByIds(any())).thenReturn(Set.of("s1"));
        when(technicianSkillRepository.deleteTechnicianSkillsByIds(anySet())).thenReturn(1);

        assertThat(service.deleteTechnicianSkillsById(List.of(dto("s1")))).containsExactly("s1");
        verify(technicianSkillRepository).deleteTechnicianSkillsByIds(anySet());
    }

    @Test
    void delete_noneExisting_returnsEmpty() {
        when(technicianSkillRepository.findExistingTechnicianSkillsByIds(any())).thenReturn(Set.of());

        assertThat(service.deleteTechnicianSkillsById(List.of(dto("s1")))).isEmpty();
        verify(technicianSkillRepository, never()).deleteTechnicianSkillsByIds(anySet());
    }

    @Test
    void getTechnicianSkillById_delegates() {
        TechnicianSkillDTO d = mock(TechnicianSkillDTO.class);
        when(technicianSkillRepository.getTechnicianSkillById("s1")).thenReturn(d);
        assertThat(service.getTechnicianSkillById("s1")).isSameAs(d);
    }

    @Test
    void getAllTechnicianSkill_delegates() {
        List<TechnicianSkillDTO> all = List.of(mock(TechnicianSkillDTO.class));
        when(technicianSkillRepository.getAllTechnicianSkill()).thenReturn(all);
        assertThat(service.getAllTechnicianSkill()).isSameAs(all);
    }
}
