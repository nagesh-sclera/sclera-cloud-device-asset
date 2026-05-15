package io.sclera.service;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.uuid.Generators;

import io.sclera.Repository.NotesRepository;
import io.sclera.dto.Product_NotesDTO;
import io.sclera.models.Device;
import io.sclera.models.compositeclass.NoteIds;

@Service
public class NotesService {

	@Autowired
	NotesRepository notesRepository;

	@Autowired
	DeviceService deviceService;

	public void deleteGlobalNotesByDeviceId(String device_id) 
	{
		notesRepository.deleteGlobalNotesByDeviceId(device_id);
	}

	public void addGlobalNoteByDeviceId(Product_NotesDTO product_note, String device_id) 
	{
		String id = Generators.timeBasedGenerator().generate().toString();
		notesRepository.addGlobalNotesByDeviceId(id,product_note.getBody() ,product_note.getTitle() ,
				1 ,device_id);	
	}


	public void updateGlobalNoteByNoteIdAndDeviceId(Product_NotesDTO product_note ,String device_id)
	{
		notesRepository.updateGlobalNoteByNoteIdAndDeviceId(product_note.getBody() ,product_note.getTitle() ,product_note.getId() ,device_id);
	}


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







	public String addNoteByDeviceId(Product_NotesDTO notesdto, String device_id) {
		if(notesdto.getId() == null)
		{
			String id = Generators.timeBasedGenerator().generate().toString();
			notesdto.setId(id);
		}
		notesRepository.addNoteByDeviceId(notesdto.getId() ,notesdto.getTitle() ,notesdto.getBody() ,device_id);
		return notesdto.getId();
	}

	public void updateNoteByNoteIdAndDeviceId(Product_NotesDTO notesdto ,String device_id) {
		notesRepository.updateNoteByNoteIdAndDeviceId(notesdto.getTitle() ,notesdto.getBody() ,notesdto.getId() ,device_id);
	}

	public boolean compareIds(Set<String> note_ids , String note_id)
	{
		return	note_ids.stream()
				.anyMatch(n -> n.equals(note_id));
	}

	public Integer getNotesCountByDeviceId(String id) {
		return notesRepository.getNotesCountByDeviceId(id);
	}

	public Set<Product_NotesDTO> getNotesByDeviceId(String username, String vdms_id, String dockername, String device_id) {
		return notesRepository.getNotesByDeviceId(device_id);
	}

	public void deleteNoteByNoteIdAndDeviceId(String username, String vdms_id, String dockername, String device_id,
			String note_id) {
		//		notesRepository.deleteNoteByNoteIdAndDeviceId(note_id ,device_id);
		notesRepository.deleteById(new NoteIds(note_id, new Device(device_id)));

		//update device notes count
		deviceService.updateDeviceNotesCount(device_id);
	}


	public void deleteNotesByDeviceId(String device_id) 
	{
		notesRepository.deleteNotesByDeviceId(device_id);
	}



}