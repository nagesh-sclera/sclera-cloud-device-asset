package io.sclera.integration.dto;

import java.math.BigInteger;

/** STUB: replace with proper AP-C2 integration response */
public class ResponseDTO {
    private Object data;
    private boolean success;
    private String message;
    private int status;
    private BigInteger timestamp;

    public ResponseDTO() {}

    /** 4-arg constructor: (message, status, success, timestamp) */
    public ResponseDTO(String message, int status, boolean success, BigInteger timestamp) {
        this.message = message;
        this.status = status;
        this.success = success;
        this.timestamp = timestamp;
    }

    /** 5-arg constructor: (message, status, data, success, timestamp) */
    public ResponseDTO(String message, int status, Object data, boolean success, BigInteger timestamp) {
        this.message = message;
        this.status = status;
        this.data = data;
        this.success = success;
        this.timestamp = timestamp;
    }

    public Object getData() { return data; }
    public void setData(Object data) { this.data = data; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public BigInteger getTimestamp() { return timestamp; }
    public void setTimestamp(BigInteger timestamp) { this.timestamp = timestamp; }
}
