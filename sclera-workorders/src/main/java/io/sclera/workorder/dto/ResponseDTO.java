package io.sclera.workorder.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigInteger;

/**
 * Common error/response envelope. Recreated from the usage in the monolith's
 * {@code io.sclera.integration.dto.ResponseDTO} as observed in
 * {@code MaximoExceptionHandler}:
 *
 *   new ResponseDTO(message, errorCode, requestURI, success, timestamp)
 *
 * Five fields in this exact order. Used for {@link io.sclera.workorder.exception.MaximoException}
 * responses and other structured error payloads.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseDTO {

    private String message;
    private Integer errorCode;
    private String path;
    private Boolean success;
    private BigInteger timestamp;

    public ResponseDTO() {
    }

    public ResponseDTO(String message, Integer errorCode, String path, Boolean success, BigInteger timestamp) {
        this.message = message;
        this.errorCode = errorCode;
        this.path = path;
        this.success = success;
        this.timestamp = timestamp;
    }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Integer getErrorCode() { return errorCode; }
    public void setErrorCode(Integer errorCode) { this.errorCode = errorCode; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public Boolean getSuccess() { return success; }
    public void setSuccess(Boolean success) { this.success = success; }

    public BigInteger getTimestamp() { return timestamp; }
    public void setTimestamp(BigInteger timestamp) { this.timestamp = timestamp; }
}
