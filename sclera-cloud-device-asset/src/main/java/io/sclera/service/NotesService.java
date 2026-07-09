package io.sclera.service;

import java.util.Set;

import io.sclera.dto.Product_NotesDTO;

/**
 * Service contract for {@link io.sclera.service.NotesService}. Declares the public
 * operations the implementation must provide. Implementation/behaviour is unchanged.
 */
public interface NotesService {

    void deleteGlobalNotesByDeviceId(String device_id);

    void addGlobalNoteByDeviceId(Product_NotesDTO product_note, String device_id);

    void updateGlobalNoteByNoteIdAndDeviceId(Product_NotesDTO product_note, String device_id);

    void upsertGlobalNotesByDeviceId(Set<Product_NotesDTO> product_notes, String device_id);

    String upsertNotesByDeviceId(String username, String vdms_id, String dockername, String device_id, Product_NotesDTO notesdto);

    String addNoteByDeviceId(Product_NotesDTO notesdto, String device_id);

    void updateNoteByNoteIdAndDeviceId(Product_NotesDTO notesdto, String device_id);

    boolean compareIds(Set<String> note_ids, String note_id);

    Integer getNotesCountByDeviceId(String id);

    Set<Product_NotesDTO> getNotesByDeviceId(String username, String vdms_id, String dockername, String device_id);

    void deleteNoteByNoteIdAndDeviceId(String username, String vdms_id, String dockername, String device_id, String note_id);

    void deleteNotesByDeviceId(String device_id);
}
