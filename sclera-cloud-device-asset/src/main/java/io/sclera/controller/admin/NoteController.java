package io.sclera.controller.admin;

import java.util.Set;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
@Tag(name = "Notes", description = "Upsert, read and delete free-text notes attached to a device.")
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
	@Operation(summary = "Upsert a note for a device",
			description = "Creates or updates a note for the given device and records the action in the audit log.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Note upserted"),
			@ApiResponse(responseCode = "400", description = "Invalid request payload"),
			@ApiResponse(responseCode = "500", description = "Unexpected server error")
	})
	@PostMapping("/docker/{dockername}/device/{device_id}/note")
	public String upsertNotesByDeviceId(
			@Parameter(description = "Owning user") @RequestParam String username,
			@Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
			@Parameter(description = "Owning docker/container name") @PathVariable String dockername,
			@Parameter(description = "Device the note belongs to") @PathVariable String device_id,
			@RequestBody Product_NotesDTO notesdto)
	{
		log.info("upsertNotesByDeviceId username={} vdmsid={} dockername={} device_id={}", username, vdmsid, dockername, device_id);
		String id = notesService.upsertNotesByDeviceId(username ,vdmsid ,dockername ,device_id ,notesdto);
		String title = notesdto != null && notesdto.getTitle() != null ? notesdto.getTitle() : "";
		userActionLogService.addUserAction(username, "asset", "ADD", "A Note '" + title + "' was added to device " + device_id, "success", "note", device_id);
		return id;
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
	@Operation(summary = "Get notes for a device",
			description = "Returns all notes recorded against the given device.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Notes returned"),
			@ApiResponse(responseCode = "404", description = "Device not found"),
			@ApiResponse(responseCode = "500", description = "Unexpected server error")
	})
	@GetMapping("/docker/{dockername}/device/{device_id}/notes")
	public Set<Product_NotesDTO> getNotesByDeviceId(
			@Parameter(description = "Owning user") @RequestParam String username,
			@Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
			@Parameter(description = "Owning docker/container name") @PathVariable String dockername,
			@Parameter(description = "Device whose notes are fetched") @PathVariable String device_id)
	{
		log.info("getNotesByDeviceId username={} vdmsid={} dockername={} device_id={}", username, vdmsid, dockername, device_id);
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
	@Operation(summary = "Delete a note from a device",
			description = "Deletes a single note from the given device and records the action in the audit log.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Note deleted"),
			@ApiResponse(responseCode = "404", description = "Note not found"),
			@ApiResponse(responseCode = "500", description = "Unexpected server error")
	})
	@DeleteMapping("/docker/{dockername}/device/{device_id}/note/{note_id}")
	public void deleteNoteByNoteIdAndDeviceId(
			@Parameter(description = "Owning user") @RequestParam String username,
			@Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
			@Parameter(description = "Owning docker/container name") @PathVariable String dockername,
			@Parameter(description = "Device the note belongs to") @PathVariable String device_id,
			@Parameter(description = "Note to delete") @PathVariable String note_id)
	{
		log.info("deleteNoteByNoteIdAndDeviceId username={} vdmsid={} dockername={} device_id={} note_id={}", username, vdmsid, dockername, device_id, note_id);
		notesService.deleteNoteByNoteIdAndDeviceId(username ,vdmsid ,dockername ,device_id ,note_id);
		userActionLogService.addUserAction(username, "asset", "DELETE", "A Note was removed from device " + device_id, "success", "note", device_id);
	}


}
