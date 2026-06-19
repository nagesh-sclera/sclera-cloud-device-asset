package io.sclera.workorder.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import java.math.BigInteger;

/**
 * Request/response DTO for native tickets (incident and service).
 *
 * <p>Validation here is intentionally permissive — length caps and e-mail format
 * only — because the same DTO is reused for create, update, and read across
 * several flows; no field is marked required to avoid rejecting existing payloads.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TicketDTO {

    private String id;
    @Size(max = 255) private String number;
    @Size(max = 64) private String status;
    private BigInteger timestamp;
    @Size(max = 4000) private String user_message;
    @Size(max = 4000) private String vendor_message;
    @Size(max = 64) private String type;
    @Size(max = 4000) private String response;
    @Size(max = 255) private String device_id;
    @Email(message = "vendor_email must be a valid e-mail")
    @Size(max = 320) private String vendor_email;
    @Size(max = 255) private String docker_name;
    @Size(max = 8000) private String description;
    @Size(max = 255) private String category;
    @Size(max = 64) private String request_type;
    @Size(max = 320) private String created_by;
    private BigInteger created_at;
    @Size(max = 320) private String updated_by;
    private BigInteger updated_at;
    @Size(max = 320) private String closed_by;
    private BigInteger closed_at;

    @Email(message = "assignee_user_email must be a valid e-mail")
    @Size(max = 320) private String assignee_user_email;
    @Size(max = 255) private String name;


    public TicketDTO() {
    }

    public TicketDTO(String id, String number, String status, BigInteger timestamp, String user_message, String vendor_message, String type,
                     String response, String device_id) {
        super();
        this.id = id;
        this.number = number;
        this.status = status;
        this.timestamp = timestamp;
        this.user_message = user_message;
        this.vendor_message = vendor_message;
        this.type = type;
        this.response = response;
        this.device_id = device_id;
    }

    public TicketDTO(String id, String number, String status, BigInteger timestamp, String user_message, String vendor_message, String type,
                     String response, String device_id, String docker_name) {
        super();
        this.id = id;
        this.number = number;
        this.status = status;
        this.timestamp = timestamp;
        this.user_message = user_message;
        this.vendor_message = vendor_message;
        this.type = type;
        this.response = response;
        this.device_id = device_id;
        this.docker_name = docker_name;
    }

    public TicketDTO(String id, String number, String status, String user_message, String type, String description, String category, String request_type, String created_by, BigInteger created_at, String updated_by, BigInteger updated_at, String closed_by, BigInteger closed_at, String device_id, String user_email, String name) {
        this.id = id;
        this.number = number;
        this.status = status;
        this.user_message = user_message;
        this.type = type;
        this.description = description;
        this.category = category;
        this.request_type = request_type;
        this.created_by = created_by;
        this.created_at = created_at;
        this.updated_by = updated_by;
        this.updated_at = updated_at;
        this.closed_by = closed_by;
        this.closed_at = closed_at;
        this.device_id = device_id;
        this.assignee_user_email = user_email;
        this.name = name;
    }

    public String getUser_message() {
        return user_message;
    }

    public void setUser_message(String user_message) {
        this.user_message = user_message;
    }

    public String getVendor_message() {
        return vendor_message;
    }

    public void setVendor_message(String vendor_message) {
        this.vendor_message = vendor_message;
    }

    public String getVendor_email() {
        return vendor_email;
    }

    public void setVendor_email(String vendor_email) {
        this.vendor_email = vendor_email;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigInteger getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(BigInteger timestamp) {
        this.timestamp = timestamp;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDevice_id() {
        return device_id;
    }

    public void setDevice_id(String device_id) {
        this.device_id = device_id;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getDocker_name() {
        return docker_name;
    }

    public void setDocker_name(String docker_name) {
        this.docker_name = docker_name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getRequest_type() {
        return request_type;
    }

    public void setRequest_type(String request_type) {
        this.request_type = request_type;
    }

    public String getCreated_by() {
        return created_by;
    }

    public void setCreated_by(String created_by) {
        this.created_by = created_by;
    }

    public BigInteger getCreated_at() {
        return created_at;
    }

    public void setCreated_at(BigInteger created_at) {
        this.created_at = created_at;
    }

    public String getUpdated_by() {
        return updated_by;
    }

    public void setUpdated_by(String updated_by) {
        this.updated_by = updated_by;
    }

    public BigInteger getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(BigInteger updated_at) {
        this.updated_at = updated_at;
    }

    public String getClosed_by() {
        return closed_by;
    }

    public void setClosed_by(String closed_by) {
        this.closed_by = closed_by;
    }

    public BigInteger getClosed_at() {
        return closed_at;
    }

    public void setClosed_at(BigInteger closed_at) {
        this.closed_at = closed_at;
    }

    public String getAssignee_user_email() {
        return assignee_user_email;
    }

    public void setAssignee_user_email(String assignee_user_email) {
        this.assignee_user_email = assignee_user_email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }



    //	public TicketDTO(String id, String number, String status, BigInteger timestamp, String user_message, String vendor_message, String type,
//			String response ,String device_id, String vendor_email) {
//		super();
//		this.id = id;
//		this.number = number;
//		this.status = status;
//		this.timestamp = timestamp;
//		this.user_message = user_message;
//		this.vendor_message = vendor_message;
//		this.type = type;
//		this.response = response;
//		this.device_id = device_id;
//		this.vendor_email = vendor_email;
//	}


    @Override
    public String toString() {
        return "TicketDTO{" +
                "id='" + id + '\'' +
                ", number='" + number + '\'' +
                ", status='" + status + '\'' +
                ", timestamp=" + timestamp +
                ", user_message='" + user_message + '\'' +
                ", vendor_message='" + vendor_message + '\'' +
                ", type='" + type + '\'' +
                ", response='" + response + '\'' +
                ", device_id='" + device_id + '\'' +
                ", vendor_email='" + vendor_email + '\'' +
                ", docker_name='" + docker_name + '\'' +
                ", description='" + description + '\'' +
                ", category='" + category + '\'' +
                ", request_type='" + request_type + '\'' +
                ", created_by='" + created_by + '\'' +
                ", created_at=" + created_at +
                ", updated_by='" + updated_by + '\'' +
                ", updated_at=" + updated_at +
                ", closed_by='" + closed_by + '\'' +
                ", closed_at=" + closed_at +
                ", assignee_user_email='" + assignee_user_email + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
