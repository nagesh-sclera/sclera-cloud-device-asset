package io.sclera.service;

import io.sclera.Repository.TechnicianCertificateRepository;
import io.sclera.dto.TechnicianCertificateDTO;
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
 * Unit coverage for TechnicianCertificateService: upsert row-affected collection, create id
 * generation, update null-id skip, delete existing-id resolution, and delegate reads.
 */
@ExtendWith(MockitoExtension.class)
class TechnicianCertificateServiceTest {

    @Mock TechnicianCertificateRepository technicianCertificateRepository;

    @InjectMocks TechnicianCertificateService service;

    private TechnicianCertificateDTO dto(String id) {
        TechnicianCertificateDTO d = mock(TechnicianCertificateDTO.class);
        lenient().when(d.getId()).thenReturn(id);
        return d;
    }

    @Test
    void upsert_null_returnsEmpty() {
        assertThat(service.upsertTechnicianCertificate(null)).isEmpty();
    }

    @Test
    void upsert_rowsAffected_collectsId() {
        when(technicianCertificateRepository.upsertTechnicianCertificate(any(), any(), any(), any(), any()))
                .thenReturn(1);
        assertThat(service.upsertTechnicianCertificate(List.of(dto("c1")))).containsExactly("c1");
    }

    @Test
    void upsert_noRows_doesNotCollectId() {
        when(technicianCertificateRepository.upsertTechnicianCertificate(any(), any(), any(), any(), any()))
                .thenReturn(0);
        assertThat(service.upsertTechnicianCertificate(List.of(dto("c1")))).isEmpty();
    }

    @Test
    void create_setsIdAndPersists() {
        TechnicianCertificateDTO d = dto(null);
        service.createTechnicianCertificate(d);
        verify(d).setId(any());
        verify(technicianCertificateRepository).createTechnicianCertificate(any(), any(), any(), any(), any());
    }

    @Test
    void update_nullId_skips() {
        service.updateTechnicianCertificate(dto(null));
        verify(technicianCertificateRepository, never())
                .updateTechnicianCertificate(any(), any(), any(), any(), any());
    }

    @Test
    void update_withId_persists() {
        service.updateTechnicianCertificate(dto("c1"));
        verify(technicianCertificateRepository).updateTechnicianCertificate(any(), any(), any(), any(), any());
    }

    @Test
    void delete_existing_deletesAndReturnsIds() {
        when(technicianCertificateRepository.findExistingTechnicianCertificatesByIds(any()))
                .thenReturn(Set.of("c1"));
        when(technicianCertificateRepository.deleteTechnicianCertificatesByIds(anySet())).thenReturn(1);

        assertThat(service.deleteTechnicianCertificatesById(List.of(dto("c1")))).containsExactly("c1");
        verify(technicianCertificateRepository).deleteTechnicianCertificatesByIds(anySet());
    }

    @Test
    void delete_noneExisting_returnsEmpty() {
        when(technicianCertificateRepository.findExistingTechnicianCertificatesByIds(any()))
                .thenReturn(Set.of());

        assertThat(service.deleteTechnicianCertificatesById(List.of(dto("c1")))).isEmpty();
        verify(technicianCertificateRepository, never()).deleteTechnicianCertificatesByIds(anySet());
    }

    @Test
    void getAllTechnicianCertificates_delegates() {
        List<TechnicianCertificateDTO> all = List.of(mock(TechnicianCertificateDTO.class));
        when(technicianCertificateRepository.getAllTechnicianCertificates()).thenReturn(all);
        assertThat(service.getAllTechnicianCertificates()).isSameAs(all);
    }

    @Test
    void getTechnicianCertificateById_delegates() {
        TechnicianCertificateDTO d = mock(TechnicianCertificateDTO.class);
        when(technicianCertificateRepository.getTechnicianCertificateById("c1")).thenReturn(d);
        assertThat(service.getTechnicianCertificateById("c1")).isSameAs(d);
    }
}
