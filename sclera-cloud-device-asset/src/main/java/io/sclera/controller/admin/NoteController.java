package io.sclera.controller.admin;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import io.sclera.service.UserActionLogService;

/**
 * REST endpoints for managing free-text notes attached to a device.
 * Delegates all persistence and business logic to {@link NotesService}.
 */
@RestController
@CrossOrigin(origins="*",allowedHeaders="*")
@RequestMapping("/api/v1/sclera-cloud-device-asset-service")
public class NoteController {

	private static final Logger log = LoggerFactory.getLogger(NoteController.class);

	@Autowired
	NotesService notesService;

	@Autowired
	UserActionLogService userActionLogService;


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
		log.info("upsertNotesByDeviceId username={} vdmsid={} dockername={} device_id={}", username, vdmsid, dockername, device_id);
		try {
			String id = notesService.upsertNotesByDeviceId(username ,vdmsid ,dockername ,device_id ,notesdto);
			String title = notesdto != null && notesdto.getTitle() != null ? notesdto.getTitle() : "";
			userActionLogService.addUserAction(username, "asset", "ADD", "A Note '" + title + "' was added to device " + device_id, "success", "note", device_id);
			return id;
		} catch (Exception e) {
			log.error("upsertNotesByDeviceId failed username={} vdmsid={} dockername={} device_id={}: {}", username, vdmsid, dockername, device_id, e.getMessage(), e);
			throw e;
		}
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
		log.info("getNotesByDeviceId username={} vdmsid={} dockername={} device_id={}", username, vdmsid, dockername, device_id);
		try {
			return notesService.getNotesByDeviceId(username ,vdmsid ,dockername ,device_id);
		} catch (Exception e) {
			log.error("getNotesByDeviceId failed username={} vdmsid={} dockername={} device_id={}: {}", username, vdmsid, dockername, device_id, e.getMessage(), e);
			throw e;
		}
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
		log.info("deleteNoteByNoteIdAndDeviceId username={} vdmsid={} dockername={} device_id={} note_id={}", username, vdmsid, dockername, device_id, note_id);
		try {
			notesService.deleteNoteByNoteIdAndDeviceId(username ,vdmsid ,dockername ,device_id ,note_id);
			userActionLogService.addUserAction(username, "asset", "DELETE", "A Note was removed from device " + device_id, "success", "note", device_id);
		} catch (Exception e) {
			log.error("deleteNoteByNoteIdAndDeviceId failed username={} vdmsid={} dockername={} device_id={} note_id={}: {}", username, vdmsid, dockername, device_id, note_id, e.getMessage(), e);
			throw e;
		}
	}
	
	
}
