package io.sclera.service;

import io.sclera.Repository.AiCallLogHistoryRepository;
import io.sclera.Repository.AiCallLogRepository;
import io.sclera.Repository.TechnicianAvailabilityRepository;
import io.sclera.Repository.TechnicianCertificateRepository;
import io.sclera.Repository.TechnicianRepository;
import io.sclera.Repository.TechnicianSkillRepository;
import io.sclera.dto.TechnicianCertificateDTO;
import io.sclera.dto.TechnicianDTO;
import io.sclera.dto.TechnicianSkillDTO;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit coverage for the clean main methods of TechnicianService: delegates, upsert/delete
 * branches, detail enrichment, device tagging, and tagged-technician profile retrieval. The
 * createTechnician/updateTechnician sub-service orchestration is deferred.
 */
@ExtendWith(MockitoExtension.class)
class TechnicianServiceTest {

    @Mock TechnicianRepository technicianRepository;
    @Mock TechnicianAvailabilityService technicianAvailabilityService;
    @Mock TechnicianCertificateService technicianCertificateService;
    @Mock TechnicianSkillService technicianSkillService;
    @Mock TechnicianSkillRepository technicianSkillRepository;
    @Mock TechnicianCertificateRepository technicianCertificateRepository;
    @Mock TechnicianAvailabilityRepository technicianAvailabilityRepository;
    @Mock AiCallLogRepository aiCallLogRepository;
    @Mock AiCallLogHistoryRepository aiCallLogHistoryRepository;

    @InjectMocks TechnicianService service;

    // ---- delegates --------------------------------------------------------

    @Test
    void getTechnicianById_delegates() {
        TechnicianDTO t = mock(TechnicianDTO.class);
        when(technicianRepository.getTechnicianById("t1")).thenReturn(t);
        assertThat(service.getTechnicianById("t1", null)).isSameAs(t);
    }

    @Test
    void getAllTechnician_delegates() {
        List<TechnicianDTO> list = List.of(mock(TechnicianDTO.class));
        when(technicianRepository.getAllTechnician()).thenReturn(list);
        assertThat(service.getAllTechnician()).isSameAs(list);
    }

    @Test
    void getAllTechniciansEmail_delegates() {
        List<Set> list = List.of(Set.of("a@b.com"));
        when(technicianRepository.getAllTechniciansEmail()).thenReturn(list);
        assertThat(service.getAllTechniciansEmail()).isSameAs(list);
    }

    @Test
    void getTechnicianNameById_delegates() {
        when(technicianRepository.getTechnicianNameById("t1")).thenReturn("Alice");
        assertThat(service.getTechnicianNameById("t1")).isEqualTo("Alice");
    }

    @Test
    void getUniqueTechnicianDepartments_delegates() {
        List<String> depts = List.of("HVAC", "Electrical");
        when(technicianRepository.getUniqueTechnicianDepartments()).thenReturn(depts);
        assertThat(service.getUniqueTechnicianDepartments()).isSameAs(depts);
    }

    // ---- upsert branches --------------------------------------------------

    @Test
    void upsertTechnician_rowsAffected_collectsId() {
        TechnicianDTO t = mock(TechnicianDTO.class);
        when(t.getId()).thenReturn("t1");
        when(technicianRepository.upsertTechnician(any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any())).thenReturn(1);

        assertThat(service.upsertTechnician(List.of(t))).containsExactly("t1");
    }

    @Test
    void upsertTechnician_zeroRows_returnsEmpty() {
        TechnicianDTO t = mock(TechnicianDTO.class);
        when(technicianRepository.upsertTechnician(any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any())).thenReturn(0);

        assertThat(service.upsertTechnician(List.of(t))).isEmpty();
    }

    @Test
    void upsertTechnician_nullList_returnsEmpty() {
        assertThat(service.upsertTechnician(null)).isEmpty();
    }

    // ---- findExisting / delete -------------------------------------------

    @Test
    void findExistingTechniciansByIds_delegatesWhenNonEmpty() {
        when(technicianRepository.findExistingTechniciansByIds(List.of("t1"))).thenReturn(Set.of("t1"));
        assertThat(service.findExistingTechniciansByIds(List.of("t1"))).containsExactly("t1");
    }

    @Test
    void findExistingTechniciansByIds_emptyIds_returnsEmptyWithoutRepo() {
        assertThat(service.findExistingTechniciansByIds(List.of())).isEmpty();
        verify(technicianRepository, never()).findExistingTechniciansByIds(any());
    }

    @Test
    void deleteTechniciansById_existing_deletesRelatedRecordsAndReturnsIds() {
        TechnicianDTO t = mock(TechnicianDTO.class);
        when(t.getId()).thenReturn("t1");
        when(technicianRepository.findExistingTechniciansByIds(any())).thenReturn(Set.of("t1"));

        Set<String> deleted = service.deleteTechniciansById(List.of(t));

        assertThat(deleted).containsExactly("t1");
        verify(technicianSkillRepository).deleteTechnicianSkillsByTechnicianIds(Set.of("t1"));
        verify(technicianAvailabilityRepository).deleteTechnicianAvailabilityByTechnicianIds(Set.of("t1"));
        verify(technicianCertificateRepository).deleteTechnicianCertificatesByTechnicianIds(Set.of("t1"));
        verify(technicianRepository).deleteTechniciansByIds(Set.of("t1"));
    }

    @Test
    void deleteTechniciansById_noExisting_returnsEmptyAndDeletesNothing() {
        TechnicianDTO t = mock(TechnicianDTO.class);
        when(t.getId()).thenReturn("t1");
        when(technicianRepository.findExistingTechniciansByIds(any())).thenReturn(Set.of());

        assertThat(service.deleteTechniciansById(List.of(t))).isEmpty();
        verify(technicianRepository, never()).deleteTechniciansByIds(any());
    }

    // ---- detail enrichment -----------------------------------------------

    @Test
    void getTechnicianDetailsById_present_enrichesWithSkillsAndCertificates() {
        TechnicianDTO t = mock(TechnicianDTO.class);
        when(technicianRepository.getTechnicianById("t1")).thenReturn(t);
        List<TechnicianSkillDTO> skills = List.of(mock(TechnicianSkillDTO.class));
        List<TechnicianCertificateDTO> certs = List.of(mock(TechnicianCertificateDTO.class));
        when(technicianSkillRepository.getSkillsByTechnicianId("t1")).thenReturn(skills);
        when(technicianCertificateRepository.getCertificatesByTechnicianId("t1")).thenReturn(certs);

        assertThat(service.getTechnicianDetailsById("t1")).isSameAs(t);
        verify(t).setTechnicianSkillDto(skills);
        verify(t).setTechnicianCertificateDtos(certs);
    }

    @Test
    void getTechnicianDetailsById_null_throws() {
        when(technicianRepository.getTechnicianById("t1")).thenReturn(null);
        assertThatThrownBy(() -> service.getTechnicianDetailsById("t1"))
                .isInstanceOf(RuntimeException.class);
    }

    // ---- skill-profile delegates (formatted-time arg) --------------------

    @Test
    void getTechnicianSkillProfile_delegatesWithFormattedTime() {
        TechnicianDTO t = mock(TechnicianDTO.class);
        when(technicianRepository.getTechnicianSkillProfileWithPrimarySkillAndAvailabilityById(eq("t1"), anyString()))
                .thenReturn(t);
        assertThat(service.getTechnicianSkillProfileWithPrimarySkillAndAvailabilityById("t1")).isSameAs(t);
    }

    @Test
    void getAllTechnicianSkillProfiles_computesOffsetAndDelegates() {
        List<TechnicianDTO> list = List.of(mock(TechnicianDTO.class));
        // page=2, size=10 -> offset = 10
        when(technicianRepository.getAllTechnicianSkillProfilesWithPrimarySkillAndAvailability(anyString(), eq(10), eq(10)))
                .thenReturn(list);
        assertThat(service.getAllTechnicianSkillProfilesWithPrimarySkillAndAvailability(10, 2)).isSameAs(list);
    }

    @Test
    void getAllTechniciansByFilterByPagination_computesOffsetAndDelegates() {
        List<TechnicianDTO> list = List.of(mock(TechnicianDTO.class));
        when(technicianRepository.getAllTechniciansByFilterByPagination(anyString(), eq(10), eq(10), eq("idf"), eq("dep"), eq("av")))
                .thenReturn(list);
        assertThat(service.getAllTechniciansByFilterByPagination(10, 2, "idf", "dep", "av")).isSameAs(list);
    }

    @Test
    void getAllTechnicianNamesAndIds_computesOffsetAndDelegates() {
        List<TechnicianDTO> list = List.of(mock(TechnicianDTO.class));
        when(technicianRepository.getAllTechnicianNamesAndIds(eq(10), eq(10), eq("key"))).thenReturn(list);
        assertThat(service.getAllTechnicianNamesAndIds(2, 10, "key")).isSameAs(list);
    }

    // ---- tag / untag ------------------------------------------------------

    @Test
    void tagTechniciansToDevice_tagsEachTechnician() {
        service.tagTechniciansToDevice("d1", List.of("t1", "t2"));
        verify(technicianRepository).tagTechniciansToDevice("t1", "d1");
        verify(technicianRepository).tagTechniciansToDevice("t2", "d1");
    }

    @Test
    void tagTechniciansToDevice_emptyIds_doesNothing() {
        service.tagTechniciansToDevice("d1", List.of());
        verify(technicianRepository, never()).tagTechniciansToDevice(any(), any());
    }

    @Test
    void unTagTechniciansFromDevice_untagsEachTechnician() {
        service.unTagTechniciansFromDevice("d1", List.of("t1"));
        verify(technicianRepository).unTagTechniciansFromDevice("t1", "d1");
    }

    // ---- tagged-technician profile retrieval -----------------------------

    @Test
    void getAllTechniciansByDeviceId_returnsTaggedProfiles() {
        when(technicianRepository.getAllTaggedTechnicianIds("d1")).thenReturn(List.of("t1"));
        TechnicianDTO t = mock(TechnicianDTO.class);
        when(technicianRepository.getTechnicianSkillProfileWithPrimarySkillAndAvailabilityById(eq("t1"), anyString()))
                .thenReturn(t);

        assertThat(service.getAllTechniciansByDeviceId("d1")).containsExactly(t);
    }

    @Test
    void getAllTechniciansByDeviceId_noTaggedIds_returnsEmpty() {
        when(technicianRepository.getAllTaggedTechnicianIds("d1")).thenReturn(List.of());
        assertThat(service.getAllTechniciansByDeviceId("d1")).isEmpty();
    }

    @Test
    void getAllAvailableTechnicianByDeviceId_filtersToAvailableOnly() {
        when(technicianRepository.getAllTaggedTechnicianIds("d1")).thenReturn(List.of("t1", "t2"));
        TechnicianDTO available = mock(TechnicianDTO.class);
        when(available.getAvailability()).thenReturn("Available");
        TechnicianDTO busy = mock(TechnicianDTO.class);
        when(busy.getAvailability()).thenReturn("Busy");
        when(technicianRepository.getTechnicianSkillProfileWithPrimarySkillAndAvailabilityById(eq("t1"), anyString()))
                .thenReturn(available);
        when(technicianRepository.getTechnicianSkillProfileWithPrimarySkillAndAvailabilityById(eq("t2"), anyString()))
                .thenReturn(busy);

        assertThat(service.getAllAvailableTechnicianByDeviceId("d1")).containsExactly(available);
    }
}
