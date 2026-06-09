package io.sclera.controller.admin;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.sclera.dto.Product_NotesDTO;
import io.sclera.service.NotesService;

/**
 * REST endpoints for managing free-text notes attached to a device.
 * Delegates all persistence and business logic to {@link NotesService}.
 */
@RestController
@CrossOrigin(origins="*",allowedHeaders="*")
@RequestMapping("/api/v1/sclera-cloud-device-asset-service")
public class NoteController {

	@Autowired
	NotesService notesService;


	/**
	 * Creates or updates a note for the given device.
	 *
	 * @param username   owning user
	 * @param vdmsid     owning VDMS id
	 * @param dockername owning docker/container name
	 * @param device_id  device the note belongs to
	 * @param notesdto   note payload to upsert
	 * @return identifier or status of the upserted note
	 */
	@RequestMapping(method = RequestMethod.POST ,value = "/docker/{dockername}/device/{device_id}/note")
	public String upsertNotesByDeviceId(@RequestParam String username ,@RequestParam String vdmsid ,@PathVariable String dockername ,@PathVariable String device_id ,@RequestBody Product_NotesDTO notesdto)
	{
		return notesService.upsertNotesByDeviceId(username ,vdmsid ,dockername ,device_id ,notesdto);
	}
	
	/**
	 * Returns all notes recorded against the given device.
	 *
	 * @param username   owning user
	 * @param vdmsid     owning VDMS id
	 * @param dockername owning docker/container name
	 * @param device_id  device whose notes are fetched
	 * @return set of notes for the device
	 */
	@RequestMapping(method = RequestMethod.GET ,value = "/docker/{dockername}/device/{device_id}/notes")
	public Set<Product_NotesDTO> getNotesByDeviceId(@RequestParam String username ,@RequestParam String vdmsid ,@PathVariable String dockername ,@PathVariable String device_id)
	{
		return notesService.getNotesByDeviceId(username ,vdmsid ,dockername ,device_id);
	}
	
	/**
	 * Deletes a single note from the given device.
	 *
	 * @param username   owning user
	 * @param vdmsid     owning VDMS id
	 * @param dockername owning docker/container name
	 * @param device_id  device the note belongs to
	 * @param note_id    note to delete
	 */
	@RequestMapping(method = RequestMethod.DELETE ,value = "/docker/{dockername}/device/{device_id}/note/{note_id}")
	public void deleteNoteByNoteIdAndDeviceId(@RequestParam String username ,@RequestParam String vdmsid ,@PathVariable String dockername ,@PathVariable String device_id ,@PathVariable String note_id)
	{
		notesService.deleteNoteByNoteIdAndDeviceId(username ,vdmsid ,dockername ,device_id ,note_id);
	}
	
	
}
