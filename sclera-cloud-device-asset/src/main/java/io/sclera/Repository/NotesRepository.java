package io.sclera.Repository;

import java.util.Set;

import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.sclera.dto.Product_NotesDTO;
import io.sclera.models.Notes;
import io.sclera.models.compositeclass.NoteIds;

/**
 * Manages persistence and querying of {@link Notes} entities associated with devices.
 */
@Repository
public interface NotesRepository extends JpaRepository<Notes, NoteIds> {

	/**
	 * Returns the number of notes belonging to the given device.
	 *
	 * @param id device identifier
	 * @return count of notes for the device
	 */
	@Query("SELECT COUNT(n) FROM Notes n WHERE n.device.id = ?1")
	Integer getNotesCountByDeviceId(String id);


	/**
	 * Deletes all global notes for the given device.
	 *
	 * @param device_id device identifier
	 */
	@Modifying(clearAutomatically = true)
	@Transactional
	@Query("DELETE FROM Notes n WHERE n.device.id = ?1 AND n.is_global = 1")
	void deleteGlobalNotesByDeviceId(String device_id);

	/**
	 * Inserts a global note for the given device.
	 *
	 * @param note_id note identifier
	 * @param body note body
	 * @param title note title
	 * @param is_global flag marking the note as global
	 * @param device_id device identifier
	 */
	// NOT CONVERTED — stays native: plain INSERT (no upsert conflict logic, already PG-valid)
	@Modifying
	@Transactional
	@Query(value = "INSERT INTO notes(id,body,title,is_global,device_id) VALUES(?1,?2,?3,?4,?5)" , nativeQuery = true)
	void addGlobalNotesByDeviceId(String note_id, String body, String title, Integer is_global, String device_id);

	/**
	 * Returns the identifiers of the global notes for the given device.
	 *
	 * @param device_id device identifier
	 * @return identifiers of the device's global notes
	 */
	@Query("SELECT n.id FROM Notes n WHERE n.device.id = ?1 AND n.is_global = 1")
	Set<String> getGlobalNotesByDeviceId(String device_id);

	/**
	 * Updates the body and title of a global note for the given device.
	 *
	 * @param body new note body
	 * @param title new note title
	 * @param id note identifier
	 * @param device_id device identifier
	 */
	@Modifying(clearAutomatically = true)
	@Transactional
	@Query("UPDATE Notes n SET n.body = ?1, n.title = ?2 WHERE n.id = ?3 AND n.device.id = ?4 AND n.is_global = 1")
	void updateGlobalNoteByNoteIdAndDeviceId(String body, String title, String id, String device_id);

	/**
	 * Returns the identifiers of the non-global notes for the given device.
	 *
	 * @param device_id device identifier
	 * @return identifiers of the device's non-global notes
	 */
	@Query("SELECT n.id FROM Notes n WHERE n.device.id = ?1 AND n.is_global = 0")
	Set<String> getNoteIdsByDeviceId(String device_id);

	/**
	 * Updates the title and body of a note for the given device.
	 *
	 * @param title new note title
	 * @param body new note body
	 * @param id note identifier
	 * @param device_id device identifier
	 */
	@Modifying(clearAutomatically = true)
	@Transactional
	@Query("UPDATE Notes n SET n.title = ?1, n.body = ?2 WHERE n.id = ?3 AND n.device.id = ?4")
	void updateNoteByNoteIdAndDeviceId(String title, String body, String id, String device_id);

	/**
	 * Inserts a note for the given device.
	 *
	 * @param id note identifier
	 * @param title note title
	 * @param body note body
	 * @param device_id device identifier
	 */
	// NOT CONVERTED — stays native: plain INSERT (no upsert conflict logic, already PG-valid)
	@Modifying
	@Transactional
	@Query(value = "INSERT INTO notes(id,title,body,device_id) VALUES(?1,?2,?3,?4)" , nativeQuery = true)
	void addNoteByDeviceId(String id, String title, String body, String device_id);

	/**
	 * Returns the notes for the given device.
	 *
	 * @param device_id device identifier
	 * @return notes belonging to the device
	 */
	@Query("SELECT new io.sclera.dto.Product_NotesDTO(n.id, n.title, n.body, n.device.id, n.is_global) FROM Notes n WHERE n.device.id = ?1")
	Set<Product_NotesDTO> getNotesByDeviceId(String device_id);

	/**
	 * Deletes a specific note for the given device.
	 *
	 * @param note_id note identifier
	 * @param device_id device identifier
	 */
	//delete is done by cascade delete, if this query not required can be deleted
	@Modifying(clearAutomatically = true)
	@Transactional
	@Query("DELETE FROM Notes n WHERE n.id = ?1 AND n.device.id = ?2")
	void deleteNoteByNoteIdAndDeviceId(String note_id, String device_id);

	/**
	 * Deletes all notes for the given device.
	 *
	 * @param device_id device identifier
	 */
	@Modifying(clearAutomatically = true)
	@Transactional
	@Query("DELETE FROM Notes n WHERE n.device.id = ?1")
	void deleteNotesByDeviceId(String device_id);

}
