package io.sclera.models;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import io.sclera.dto.Product_NotesDTO;
import io.sclera.models.compositeclass.NoteIds;

import javax.persistence.*;

@Entity
@IdClass(NoteIds.class)
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class , property = "id" , scope = Notes.class)

@SqlResultSetMapping(
		name = "notemapping",
        classes = {
                @ConstructorResult(
                        targetClass = Product_NotesDTO.class,
                        columns = {
                        		@ColumnResult(name = "id",type = String.class),
                        		@ColumnResult(name = "title",type = String.class),
                        		@ColumnResult(name = "body",type = String.class),
                        		@ColumnResult(name = "device_id",type = String.class),
                        		@ColumnResult(name = "is_global",type = Integer.class)
                        		   }
                        )
                }
        )





@NamedNativeQuery(
		name = "Notes.getNotesByDeviceId",
        query = "SELECT id ,title ,body ,device_id ,is_global FROM notes WHERE device_id = ?1",
        resultSetMapping = "notemapping"	

)





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
