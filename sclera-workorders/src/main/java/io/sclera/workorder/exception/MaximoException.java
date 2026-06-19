package io.sclera.workorder.exception;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Maximo-specific runtime exception with HTTP-style error code and request path.
 *
 * Ported verbatim from the monolith — same fields, same Lombok annotations,
 * same constructor signature. Used in particular for 401 token-expiry detection
 * inside {@link io.sclera.workorder.service.MaximoService} work-order calls.
 */
@Getter
@Setter
@ToString
public class MaximoException extends RuntimeException {

    private String message;
    private Integer errorCode;
    private String path;

    public MaximoException(String message, Integer errorCode, String path) {
        super(message);
        this.message = message;
        this.errorCode = errorCode;
        this.path = path;
    }
}
