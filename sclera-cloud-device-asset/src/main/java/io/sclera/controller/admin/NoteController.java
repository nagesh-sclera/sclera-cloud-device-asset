package io.sclera.controller.admin;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import io.sclera.dto.Product_NotesDTO;
import io.sclera.service.NotesService;

@RestController
@CrossOrigin(origins="*",allowedHeaders="*")
public class NoteController {

	@Autowired
	NotesService notesService;
	
	
	@RequestMapping(method = RequestMethod.POST ,value = "/user/{username}/vdms/{vdmsid}/docker/{dockername}/device/{device_id}/note")
	public String upsertNotesByDeviceId(@PathVariable String username ,@PathVariable String vdmsid ,@PathVariable String dockername ,@PathVariable String device_id ,@RequestBody Product_NotesDTO notesdto)
	{
		return notesService.upsertNotesByDeviceId(username ,vdmsid ,dockername ,device_id ,notesdto);
	}
	
	@RequestMapping(method = RequestMethod.GET ,value = "/user/{username}/vdms/{vdmsid}/docker/{dockername}/device/{device_id}/notes")
	public Set<Product_NotesDTO> getNotesByDeviceId(@PathVariable String username ,@PathVariable String vdmsid ,@PathVariable String dockername ,@PathVariable String device_id)
	{
		return notesService.getNotesByDeviceId(username ,vdmsid ,dockername ,device_id);
	}
	
	@RequestMapping(method = RequestMethod.DELETE ,value = "/user/{username}/vdms/{vdmsid}/docker/{dockername}/device/{device_id}/note/{note_id}")
	public void deleteNoteByNoteIdAndDeviceId(@PathVariable String username ,@PathVariable String vdmsid ,@PathVariable String dockername ,@PathVariable String device_id ,@PathVariable String note_id)
	{
		notesService.deleteNoteByNoteIdAndDeviceId(username ,vdmsid ,dockername ,device_id ,note_id);
	}
	
	
}
