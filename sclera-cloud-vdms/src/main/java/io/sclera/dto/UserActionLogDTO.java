package io.sclera.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.math.BigInteger;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserActionLogDTO {
    private String id;
    private String email;
    private String type;
    private String action;
    private long created_timestamp;
    private String message;
    private String status;
    private Integer count;
    private String primary_id;
    private String vdmsId;

    public UserActionLogDTO(String email, String type, String action, long created_timestamp, String message, String status) {
        this.email = email;
        this.type = type;
        this.action = action;
        this.created_timestamp = created_timestamp;
        this.message = message;
        this.status = status;
    }

}