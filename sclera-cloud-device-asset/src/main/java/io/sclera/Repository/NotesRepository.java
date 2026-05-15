package io.sclera.Repository;

import java.util.Set;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.sclera.dto.Product_NotesDTO;
import io.sclera.models.Notes;
import io.sclera.models.compositeclass.NoteIds;

@Repository
public interface NotesRepository extends JpaRepository<Notes, NoteIds> {

	@Query(value = "SELECT COUNT(*) FROM notes WHERE device_id = ?1", nativeQuery = true)
	Integer getNotesCountByDeviceId(String id);
	

	@Modifying
	@Transactional
	@Query(value = "DELETE FROM notes WHERE device_id = ?1 AND is_global = 1" , nativeQuery = true)
	void deleteGlobalNotesByDeviceId(String device_id);

	@Modifying
	@Transactional
	@Query(value = "INSERT INTO notes(id,body,title,is_global,device_id) VALUE(?1,?2,?3,?4,?5)" , nativeQuery = true)
	void addGlobalNotesByDeviceId(String note_id, String body, String title, Integer is_global, String device_id);

	@Query(value = "SELECT id FROM notes WHERE device_id = ?1 AND is_global = 1" , nativeQuery = true)
	Set<String> getGlobalNotesByDeviceId(String device_id);

	@Modifying
	@Transactional
	@Query(value = "UPDATE notes SET body = ?1 ,title = ?2 WHERE id = ?3 AND device_id = ?4 AND is_global = 1" , nativeQuery = true)
	void updateGlobalNoteByNoteIdAndDeviceId(String body, String title, String id ,String device_id);

	@Query(value = "SELECT id FROM notes WHERE device_id= ?1 AND is_global = 0" , nativeQuery = true)
	Set<String> getNoteIdsByDeviceId(String device_id);

	@Modifying
	@Transactional
	@Query(value = "UPDATE notes SET title = ?1 ,body = ?2 WHERE id = ?3 AND device_id = ?4" , nativeQuery = true)
	void updateNoteByNoteIdAndDeviceId(String title, String body, String id ,String device_id);

	@Modifying
	@Transactional
	@Query(value = "INSERT INTO notes(id,title,body,device_id) VALUE(?1,?2,?3,?4)" , nativeQuery = true)
	void addNoteByDeviceId(String id, String title, String body, String device_id);

	@Query(nativeQuery = true)
	Set<Product_NotesDTO> getNotesByDeviceId(String device_id);

	//delete is done by cascade delete, if this query not required can be deleted
	@Modifying
	@Transactional
	@Query(value = "DELETE FROM notes WHERE id = ?1 AND device_id = ?2" , nativeQuery = true)
	void deleteNoteByNoteIdAndDeviceId(String note_id ,String device_id);

	@Modifying
	@Transactional
	@Query(value = "DELETE FROM notes WHERE device_id = ?1" , nativeQuery = true)
	void deleteNotesByDeviceId(String device_id);

}
