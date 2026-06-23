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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Closes the last NotesService gap: the update branch of upsertNotesByDeviceId, where an
 * existing note id matches and the note is updated (rather than added).
 */
@ExtendWith(MockitoExtension.class)
class NotesServiceUpdateBranchTest {

    @Mock NotesRepository notesRepository;
    @Mock DeviceService deviceService;

    @InjectMocks NotesService service;

    @Test
    void upsertNotesByDeviceId_existingNoteId_updatesAndReturnsNull() {
        Product_NotesDTO note = mock(Product_NotesDTO.class);
        when(note.getId()).thenReturn("n1");
        when(notesRepository.getNoteIdsByDeviceId("d1")).thenReturn(Set.of("n1"));

        String result = service.upsertNotesByDeviceId("u", "v", "dock", "d1", note);

        assertThat(result).isNull();
        verify(notesRepository).updateNoteByNoteIdAndDeviceId(note.getTitle(), note.getBody(), "n1", "d1");
        verify(deviceService).updateDeviceNotesCount("d1");
    }
}
