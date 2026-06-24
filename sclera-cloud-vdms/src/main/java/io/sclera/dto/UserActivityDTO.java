package io.sclera.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.math.BigInteger;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserActivityDTO {
    private String id;
    private String email;
    private String type;
    private String sub_type;
    private String action;
    private BigInteger created_timestamp;
    private String message;
    private String status;
    private Integer count;
    private String primary_id;
    private String vdmsId;

    public UserActivityDTO(String email, String type, String sub_type, String action, BigInteger created_timestamp, String message,
                           String status, String primary_id, String vdmsId) {
        this.email = email;
        this.type = type;
        this.sub_type = sub_type;
        this.action = action;
        this.created_timestamp = created_timestamp;
        this.message = message;
        this.status = status;
        this.primary_id = primary_id;
        this.vdmsId = vdmsId;
    }

    public UserActivityDTO(String id, String email, String type, String action, BigInteger created_timestamp,
                           String message, String status, String sub_type) {
        this.id = id;
        this.email = email;
        this.type = type;
        this.action = action;
        this.created_timestamp = created_timestamp;
        this.message = message;
        this.status = status;
        this.sub_type = sub_type;
    }

    public UserActivityDTO(Integer count) {
        this.count = count;
    }

}
