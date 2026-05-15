package io.sclera.dto;

public class Product_NotesDTO {

	private String id;
	private String title;
	private String body;
	private String product_id;
	private String device_id;
	private Integer is_global;
	
	public Integer getIs_global() {
		return is_global;
	}
	public void setIs_global(Integer is_global) {
		this.is_global = is_global;
	}
	public String getDevice_id() {
		return device_id;
	}
	public void setDevice_id(String device_id) {
		this.device_id = device_id;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
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
	public String getProduct_id() {
		return product_id;
	}
	public void setProduct_id(String product_id) {
		this.product_id = product_id;
	}
	
	public Product_NotesDTO() {}
	
	public Product_NotesDTO(String id, String title, String body, String device_id ,Integer is_global) {
		super();
		this.id = id;
		this.title = title;
		this.body = body;
		this.device_id = device_id;
		this.is_global = is_global;
	}
	
	@Override
	public String toString() {
		return "Product_NotesDTO [id=" + id + ", title=" + title + ", body=" + body + ", product_id=" + product_id
				+ ", device_id=" + device_id + ", is_global=" + is_global + "]";
	}
	
	
	
	
	
	
}
