package io.sclera.exception;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ServerException extends RuntimeException {

    private String message;
    private Integer status;
    private String path;

    public ServerException(String message, Integer status, String path) {
        super(message);
        this.message = message;
        this.status = status;
        this.path = path;
    }

}
