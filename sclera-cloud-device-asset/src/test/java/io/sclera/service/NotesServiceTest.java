package io.sclera.service;

import io.sclera.Repository.NotesRepository;
import io.sclera.dto.Product_NotesDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit coverage for NotesService: global/device note upsert branching (existing-id match vs add),
 * id generation in addNoteByDeviceId, compareIds matching, delegates, and the device note-count
 * side effects.
 */
@ExtendWith(MockitoExtension.class)
class NotesServiceTest {

    @Mock NotesRepository notesRepository;
    @Mock DeviceService deviceService;

    @InjectMocks NotesService service;

    private Product_NotesDTO note(String id) {
        Product_NotesDTO n = new Product_NotesDTO();
        n.setId(id);
        n.setTitle("t");
        n.setBody("b");
        return n;
    }

    // ---- compareIds ------------------------------------------------------

    @Test
    void compareIds_matchAndNoMatch() {
        assertThat(service.compareIds(Set.of("a", "b"), "b")).isTrue();
        assertThat(service.compareIds(Set.of("a", "b"), "z")).isFalse();
    }

    // ---- delegates -------------------------------------------------------

    @Test
    void deleteGlobalNotesByDeviceId_delegates() {
        service.deleteGlobalNotesByDeviceId("d1");
        verify(notesRepository).deleteGlobalNotesByDeviceId("d1");
    }

    @Test
    void updateGlobalNoteByNoteIdAndDeviceId_delegates() {
        service.updateGlobalNoteByNoteIdAndDeviceId(note("n1"), "d1");
        verify(notesRepository).updateGlobalNoteByNoteIdAndDeviceId("b", "t", "n1", "d1");
    }

    @Test
    void getNotesCountByDeviceId_delegates() {
        when(notesRepository.getNotesCountByDeviceId("d1")).thenReturn(5);
        assertThat(service.getNotesCountByDeviceId("d1")).isEqualTo(5);
    }

    @Test
    void getNotesByDeviceId_delegates() {
        Set<Product_NotesDTO> notes = Set.of(note("n1"));
        when(notesRepository.getNotesByDeviceId("d1")).thenReturn(notes);
        assertThat(service.getNotesByDeviceId("u", "v", "dock", "d1")).isSameAs(notes);
    }

    @Test
    void deleteNotesByDeviceId_delegates() {
        service.deleteNotesByDeviceId("d1");
        verify(notesRepository).deleteNotesByDeviceId("d1");
    }

    // ---- addGlobalNoteByDeviceId / addNoteByDeviceId ---------------------

    @Test
    void addGlobalNoteByDeviceId_generatesIdAndAdds() {
        service.addGlobalNoteByDeviceId(note(null), "d1");
        verify(notesRepository).addGlobalNotesByDeviceId(any(), eq("b"), eq("t"), eq(1), eq("d1"));
    }

    @Test
    void addNoteByDeviceId_nullId_generatesId() {
        Product_NotesDTO n = note(null);
        String id = service.addNoteByDeviceId(n, "d1");
        assertThat(id).isNotNull();
        verify(notesRepository).addNoteByDeviceId(id, "t", "b", "d1");
    }

    @Test
    void addNoteByDeviceId_existingId_keepsId() {
        String id = service.addNoteByDeviceId(note("n9"), "d1");
        assertThat(id).isEqualTo("n9");
    }

    // ---- upsertGlobalNotesByDeviceId branches ----------------------------

    @Test
    void upsertGlobalNotes_existingMatch_updates() {
        when(notesRepository.getGlobalNotesByDeviceId("d1")).thenReturn(Set.of("n1"));
        service.upsertGlobalNotesByDeviceId(Set.of(note("n1")), "d1");
        verify(notesRepository).updateGlobalNoteByNoteIdAndDeviceId("b", "t", "n1", "d1");
    }

    @Test
    void upsertGlobalNotes_noExisting_addsAll() {
        when(notesRepository.getGlobalNotesByDeviceId("d1")).thenReturn(Set.of());
        service.upsertGlobalNotesByDeviceId(Set.of(note("n1")), "d1");
        verify(notesRepository).addGlobalNotesByDeviceId(any(), eq("b"), eq("t"), eq(1), eq("d1"));
    }

    // ---- upsertNotesByDeviceId branches ----------------------------------

    @Test
    void upsertNotes_existingMatch_updatesAndReturnsNull() {
        when(notesRepository.getNoteIdsByDeviceId("d1")).thenReturn(Set.of("n1"));

        String result = service.upsertNotesByDeviceId("u", "v", "dock", "d1", note("n1"));

        assertThat(result).isNull();
        verify(notesRepository).updateNoteByNoteIdAndDeviceId("t", "b", "n1", "d1");
        verify(deviceService).updateDeviceNotesCount("d1");
    }

    @Test
    void upsertNotes_noExisting_addsAndReturnsId() {
        when(notesRepository.getNoteIdsByDeviceId("d1")).thenReturn(Set.of());

        String result = service.upsertNotesByDeviceId("u", "v", "dock", "d1", note("n5"));

        assertThat(result).isEqualTo("n5");
        verify(notesRepository).addNoteByDeviceId("n5", "t", "b", "d1");
        verify(deviceService).updateDeviceNotesCount("d1");
    }

    // ---- deleteNoteByNoteIdAndDeviceId -----------------------------------

    @Test
    void deleteNoteByNoteIdAndDeviceId_deletesAndUpdatesCount() {
        service.deleteNoteByNoteIdAndDeviceId("u", "v", "dock", "d1", "n1");
        verify(notesRepository).deleteById(any());
        verify(deviceService).updateDeviceNotesCount("d1");
    }
}
