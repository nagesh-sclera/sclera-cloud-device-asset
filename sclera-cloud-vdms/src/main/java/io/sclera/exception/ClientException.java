package io.sclera.exception;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ClientException extends  RuntimeException {

    private String message;
    private Integer status;
    private String path;


    public ClientException(String message , Integer status , String path) {
        super(message);
        this.message = message;
        this.status = status;
        this.path = path;
    }


}
