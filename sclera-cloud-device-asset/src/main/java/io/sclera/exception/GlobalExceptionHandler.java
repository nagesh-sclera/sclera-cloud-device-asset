package io.sclera.exception;

import io.sclera.integration.dto.ResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigInteger;
import java.util.NoSuchElementException;

/**
 * Catch-all controller advice that logs any otherwise-unhandled exception once and
 * returns a standardized {@link ResponseDTO} error body. Status is derived from the
 * exception type (400 for bad input, 404 for missing resources, 500 otherwise).
 *
 * <p>{@link MaximoException} is intentionally not handled here: its dedicated, more
 * specific {@link MaximoExceptionHandler} takes precedence.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Maps invalid-input exceptions to HTTP 400.
     *
     * @param e   the thrown exception
     * @param req current request, used for the logged/returned path
     * @return a {@link ResponseDTO} body with status 400
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ResponseDTO> handleBadRequest(IllegalArgumentException e, HttpServletRequest req) {
        return build(e, req, HttpStatus.BAD_REQUEST);
    }

    /**
     * Maps missing-resource lookups to HTTP 404.
     *
     * @param e   the thrown exception
     * @param req current request, used for the logged/returned path
     * @return a {@link ResponseDTO} body with status 404
     */
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ResponseDTO> handleNotFound(NoSuchElementException e, HttpServletRequest req) {
        return build(e, req, HttpStatus.NOT_FOUND);
    }

    /**
     * Maps any other unhandled exception to HTTP 500.
     *
     * @param e   the thrown exception
     * @param req current request, used for the logged/returned path
     * @return a {@link ResponseDTO} body with status 500
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseDTO> handleGeneric(Exception e, HttpServletRequest req) {
        return build(e, req, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ResponseDTO> build(Exception e, HttpServletRequest req, HttpStatus status) {
        log.error("{} failed [{}]: {}", req.getRequestURI(), status.value(), e.getMessage(), e);
        ResponseDTO body = new ResponseDTO(
                e.getMessage(),
                status.value(),
                req.getRequestURI(),
                false,
                BigInteger.valueOf(System.currentTimeMillis()));
        return ResponseEntity.status(status).body(body);
    }
}
