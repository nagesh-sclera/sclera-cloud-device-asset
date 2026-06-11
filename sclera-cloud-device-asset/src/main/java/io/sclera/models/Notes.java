package io.sclera.models;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import io.sclera.models.compositeclass.NoteIds;

import jakarta.persistence.*;

/**
 * JPA entity representing a free-text note attached to a device, optionally marked global,
 * used to capture user annotations within the asset-management domain.
 */
// @NamedNativeQuery("Notes.getNotesByDeviceId") and its @SqlResultSetMapping("notemapping") removed —
// getNotesByDeviceId() converted to a JPQL constructor expression in NotesRepository.
@Entity
@IdClass(NoteIds.class)
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class , property = "id" , scope = Notes.class)
public class Notes {

	@Id
	private String id;


	@Column(length = 128)
	private String title;

	private String body;

	@Column(length = 64)
	private String type;

	@Column(columnDefinition = "integer default 0")
	private Integer is_global;

	@MapsId
	@ManyToOne
	private Device device;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Device getDevice() {
		return device;
	}

	public void setDevice(Device device) {
		this.device = device;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Integer getIs_global() {
		return is_global;
	}

	public void setIs_global(Integer is_global) {
		this.is_global = is_global;
	}



}
