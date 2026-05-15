package io.sclera.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigInteger;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DocumentMediaDTO {

	private String id;
	private String name;
	private String category;
	private String link;
	private String description;
	private String created_email;
	private BigInteger created_timestamp;
	
	private String device_id;

	private Integer encrypted_type;

	public Integer getEncrypted_type() {
		return encrypted_type;
	}

	public void setEncrypted_type(Integer encrypted_type) {
		this.encrypted_type = encrypted_type;
	}

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public String getLink() {
		return link;
	}
	public void setLink(String link) {
		this.link = link;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getCreated_email() {
		return created_email;
	}
	public void setCreated_email(String created_email) {
		this.created_email = created_email;
	}
	public BigInteger getCreated_timestamp() {
		return created_timestamp;
	}
	public void setCreated_timestamp(BigInteger created_timestamp) {
		this.created_timestamp = created_timestamp;
	}
	public String getDevice_id() {
		return device_id;
	}
	public void setDevice_id(String device_id) {
		this.device_id = device_id;
	}
	public DocumentMediaDTO() {
		super();
		// TODO Auto-generated constructor stub
	}
	public DocumentMediaDTO(String id, String name, String category, String link, String description, String created_email,
			BigInteger created_timestamp,Integer encrypted_type) {
		super();
		this.id = id;
		this.name = name;
		this.category = category;
		this.link = link;
		this.description = description;
		this.created_email = created_email;
		this.created_timestamp = created_timestamp;
		this.encrypted_type = encrypted_type;
	}
	
	public DocumentMediaDTO(String id, String name, String category, String link, String description, String created_email,
			BigInteger created_timestamp,String device_id, Integer encrypted_type) {
		super();
		this.id = id;
		this.name = name;
		this.category = category;
		this.link = link;
		this.description = description;
		this.created_email = created_email;
		this.created_timestamp = created_timestamp;
		this.device_id = device_id;
		this.encrypted_type = encrypted_type;
	}


	public DocumentMediaDTO(String id, String link, Integer encrypted_type) {
		this.id = id;
		this.link = link;
		this.encrypted_type = encrypted_type;
	}

	public DocumentMediaDTO(String id, String name, String category, String link, String description, String created_email,
							BigInteger created_timestamp){
		this.id = id;
		this.name = name;
		this.category = category;
		this.link = link;
		this.description = description;
		this.created_email = created_email;
		this.created_timestamp = created_timestamp;
	}

	public DocumentMediaDTO(String id, String name, String category, String link, String description, String created_email,
							BigInteger created_timestamp,String device_id) {
		super();
		this.id = id;
		this.name = name;
		this.category = category;
		this.link = link;
		this.description = description;
		this.created_email = created_email;
		this.created_timestamp = created_timestamp;
		this.device_id = device_id;
	}
}
