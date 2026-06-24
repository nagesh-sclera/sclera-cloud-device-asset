package io.sclera.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class APICallException extends RuntimeException {

    private Integer status;
    private Integer type;

    public APICallException(String message, Integer status ,Integer type) {
        super(message);
        this.status = status;
        this.type = type;
    }

    public APICallException(String message ,Integer type) {
        super(message);
        this.type = type;
    }
}
