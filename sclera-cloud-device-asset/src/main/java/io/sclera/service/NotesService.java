package io.sclera.service;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.uuid.Generators;

import io.sclera.Repository.NotesRepository;
import io.sclera.dto.Product_NotesDTO;
import io.sclera.models.Device;
import io.sclera.models.compositeclass.NoteIds;
import io.sclera.interfaces.NotesServiceInterface;

/**
 * Manages notes attached to devices, covering both per-device notes and global
 * notes. Handles creation, update, upsert, retrieval, counting, and deletion,
 * generating time-based identifiers for new notes where required.
 * <p>
 * Persists data through {@link NotesRepository} and keeps the device's note
 * count in sync via {@link DeviceService}.
 */
@Service
public class NotesService implements NotesServiceInterface {

	@Autowired
	NotesRepository notesRepository;

	@Autowired
	DeviceService deviceService;

	/**
	 * Deletes all global notes associated with the given device.
	 *
	 * @param device_id the identifier of the device whose global notes are removed
	 */
	public void deleteGlobalNotesByDeviceId(String device_id)
	{
		notesRepository.deleteGlobalNotesByDeviceId(device_id);
	}

	/**
	 * Adds a new global note for the given device, assigning a generated
	 * time-based identifier.
	 *
	 * @param product_note the note content to persist
	 * @param device_id    the identifier of the device the note belongs to
	 */
	public void addGlobalNoteByDeviceId(Product_NotesDTO product_note, String device_id)
	{
		String id = Generators.timeBasedGenerator().generate().toString();
		notesRepository.addGlobalNotesByDeviceId(id,product_note.getBody() ,product_note.getTitle() ,
				1 ,device_id);	
	}


	/**
	 * Updates an existing global note identified by its note id for the given device.
	 *
	 * @param product_note the note content carrying the id, title, and body to apply
	 * @param device_id    the identifier of the device the note belongs to
	 */
	public void updateGlobalNoteByNoteIdAndDeviceId(Product_NotesDTO product_note ,String device_id)
	{
		notesRepository.updateGlobalNoteByNoteIdAndDeviceId(product_note.getBody() ,product_note.getTitle() ,product_note.getId() ,device_id);
	}


	/**
	 * Upserts a set of global notes for the given device, updating those whose ids
	 * already exist and adding the rest.
	 *
	 * @param product_notes the notes to add or update
	 * @param device_id     the identifier of the device the notes belong to
	 */
	public void upsertGlobalNotesByDeviceId(Set<Product_NotesDTO> product_notes, String device_id)
	{
		Set<String> product_note_ids = notesRepository.getGlobalNotesByDeviceId(device_id);
		if(product_note_ids != null && product_note_ids.size() > 0)
		{
			for(Product_NotesDTO product_note : product_notes)
			{
				if(compareIds(product_note_ids, product_note.getId()))
				{
					updateGlobalNoteByNoteIdAndDeviceId(product_note, device_id);
				}
				else
				{
					addGlobalNoteByDeviceId(product_note, device_id);
				}
			}
		}
		else
		{
			for(Product_NotesDTO product_note : product_notes)
			{
				addGlobalNoteByDeviceId(product_note, device_id);
			}
		}
	}

	/**
	 * Upserts a single note for the given device, updating it when its id already
	 * exists or adding it otherwise, then refreshes the device's note count.
	 *
	 * @param username   the requesting user
	 * @param vdms_id    the VDMS identifier
	 * @param dockername the docker container name
	 * @param device_id  the identifier of the device the note belongs to
	 * @param notesdto   the note content to add or update
	 * @return the id of the newly added note, or {@code null} when an existing note was updated
	 */
	public String upsertNotesByDeviceId(String username, String vdms_id, String dockername, String device_id ,Product_NotesDTO notesdto)
	{
		Set<String> note_ids = notesRepository.getNoteIdsByDeviceId(device_id);
		String result;
		if(note_ids != null && note_ids.size() > 0)  
		{
			if(compareIds(note_ids, notesdto.getId()))
			{
				updateNoteByNoteIdAndDeviceId(notesdto ,device_id);
				result = null;
			}
			else
			{
				result = addNoteByDeviceId(notesdto ,device_id);
			}
		}
		else
		{
			result = addNoteByDeviceId(notesdto, device_id);
		}

		//update device notes count
		deviceService.updateDeviceNotesCount(device_id);

		return result;
	}







	/**
	 * Adds a new note for the given device, generating a time-based identifier when
	 * the note does not already carry one.
	 *
	 * @param notesdto  the note content to persist
	 * @param device_id the identifier of the device the note belongs to
	 * @return the id of the added note
	 */
	public String addNoteByDeviceId(Product_NotesDTO notesdto, String device_id) {
		if(notesdto.getId() == null)
		{
			String id = Generators.timeBasedGenerator().generate().toString();
			notesdto.setId(id);
		}
		notesRepository.addNoteByDeviceId(notesdto.getId() ,notesdto.getTitle() ,notesdto.getBody() ,device_id);
		return notesdto.getId();
	}

	/**
	 * Updates an existing note identified by its note id for the given device.
	 *
	 * @param notesdto  the note content carrying the id, title, and body to apply
	 * @param device_id the identifier of the device the note belongs to
	 */
	public void updateNoteByNoteIdAndDeviceId(Product_NotesDTO notesdto ,String device_id) {
		notesRepository.updateNoteByNoteIdAndDeviceId(notesdto.getTitle() ,notesdto.getBody() ,notesdto.getId() ,device_id);
	}

	/**
	 * Determines whether the given note id is present in the supplied set of note ids.
	 *
	 * @param note_ids the set of existing note ids to search
	 * @param note_id  the note id to look for
	 * @return {@code true} if the note id is found, {@code false} otherwise
	 */
	public boolean compareIds(Set<String> note_ids , String note_id)
	{
		return	note_ids.stream()
				.anyMatch(n -> n.equals(note_id));
	}

	/**
	 * Returns the number of notes recorded for the given device.
	 *
	 * @param id the identifier of the device
	 * @return the count of notes for the device
	 */
	public Integer getNotesCountByDeviceId(String id) {
		return notesRepository.getNotesCountByDeviceId(id);
	}

	/**
	 * Retrieves all notes for the given device.
	 *
	 * @param username   the requesting user
	 * @param vdms_id    the VDMS identifier
	 * @param dockername the docker container name
	 * @param device_id  the identifier of the device
	 * @return the set of notes belonging to the device
	 */
	public Set<Product_NotesDTO> getNotesByDeviceId(String username, String vdms_id, String dockername, String device_id) {
		return notesRepository.getNotesByDeviceId(device_id);
	}

	/**
	 * Deletes the note identified by its note id from the given device and refreshes
	 * the device's note count.
	 *
	 * @param username   the requesting user
	 * @param vdms_id    the VDMS identifier
	 * @param dockername the docker container name
	 * @param device_id  the identifier of the device the note belongs to
	 * @param note_id    the identifier of the note to delete
	 */
	public void deleteNoteByNoteIdAndDeviceId(String username, String vdms_id, String dockername, String device_id,
			String note_id) {
		//		notesRepository.deleteNoteByNoteIdAndDeviceId(note_id ,device_id);
		notesRepository.deleteById(new NoteIds(note_id, new Device(device_id)));

		//update device notes count
		deviceService.updateDeviceNotesCount(device_id);
	}


	/**
	 * Deletes all notes associated with the given device.
	 *
	 * @param device_id the identifier of the device whose notes are removed
	 */
	public void deleteNotesByDeviceId(String device_id)
	{
		notesRepository.deleteNotesByDeviceId(device_id);
	}



}