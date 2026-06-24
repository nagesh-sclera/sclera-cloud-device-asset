package io.sclera.dto;

import lombok.*;

import java.math.BigInteger;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ResponseDTO {

    private String errorMessage;
    private Object data;
    private Integer status;
    private String path;
    private Boolean success;
    private BigInteger timestamp;

    public ResponseDTO(String errorMessage, Integer status, String path, Boolean success, BigInteger timestamp) {
        this.errorMessage = errorMessage;
        this.status = status;
        this.path = path;
        this.success = success;
        this.timestamp = timestamp;
    }

    public ResponseDTO(Object data, Integer status, Boolean success, BigInteger timestamp) {
        this.data = data;
        this.status = status;
        this.success = success;
        this.timestamp = timestamp;
    }


}
