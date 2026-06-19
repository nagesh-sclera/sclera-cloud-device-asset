package io.sclera.workorder.exception;

import io.sclera.workorder.dto.ResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.math.BigInteger;
import java.util.stream.Collectors;

/**
 * Ported verbatim from the monolith. Catches {@link MaximoException} and
 * returns HTTP 500 with the same {@link ResponseDTO} JSON envelope shape
 * as before. Stack-trace capture into a StringWriter is preserved even though
 * the result isn't currently put into the response — matches existing
 * behavior in case downstream consumers grep the logs.
 *
 * Migration note: Spring Boot 3 moved {@code javax.servlet} → {@code jakarta.servlet}.
 * This is the only mechanical change versus the monolith source.
 */
@ControllerAdvice
public class MaximoExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(MaximoExceptionHandler.class);

    @ExceptionHandler(value = MaximoException.class)
    public ResponseEntity<?> handleMaximoException(MaximoException maximoException, HttpServletRequest httpServletRequest) {
        log.error("Maximo error at {} (code={}): {}", httpServletRequest.getRequestURI(),
                maximoException.getErrorCode(), maximoException.getMessage());
        ResponseDTO responseDTO = new ResponseDTO(
                maximoException.getMessage(),
                maximoException.getErrorCode(),
                httpServletRequest.getRequestURI(),
                false,
                BigInteger.valueOf(System.currentTimeMillis())
        );

        return new ResponseEntity<>(responseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * NEW handlers — only for the NEW cross-service Dapr invocation method.
     * Existing flows never produce these exceptions.
     */
    @ExceptionHandler(VdmsNotFoundException.class)
    public ResponseEntity<?> handleVdmsNotFound(VdmsNotFoundException ex, HttpServletRequest req) {
        log.warn("VDMS not found at {}: {}", req.getRequestURI(), ex.getMessage());
        ResponseDTO body = new ResponseDTO(
                ex.getMessage(),
                404,
                req.getRequestURI(),
                false,
                BigInteger.valueOf(System.currentTimeMillis())
        );
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(VdmsUnavailableException.class)
    public ResponseEntity<?> handleVdmsUnavailable(VdmsUnavailableException ex, HttpServletRequest req) {
        log.error("VDMS service unavailable at {}: {}", req.getRequestURI(), ex.getMessage());
        ResponseDTO body = new ResponseDTO(
                ex.getMessage(),
                503,
                req.getRequestURI(),
                false,
                BigInteger.valueOf(System.currentTimeMillis())
        );
        return new ResponseEntity<>(body, HttpStatus.SERVICE_UNAVAILABLE);
    }

    /**
     * Converts Bean Validation failures on {@code @Valid @RequestBody} arguments into a
     * structured HTTP 400, instead of letting them fall through to Spring's default error.
     *
     * @param ex  the validation exception holding per-field errors
     * @param req the current request (for the path field)
     * @return a 400 response in the standard {@link ResponseDTO} envelope, message listing each field error
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));
        log.warn("Validation failed at {}: {}", req.getRequestURI(),
                message.isEmpty() ? "Validation failed" : message);
        ResponseDTO body = new ResponseDTO(
                message.isEmpty() ? "Validation failed" : message,
                400,
                req.getRequestURI(),
                false,
                BigInteger.valueOf(System.currentTimeMillis())
        );
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    /**
     * Converts a missing required query parameter (e.g. the now-mandatory
     * {@code loggedInUser} / {@code vdms_id}) into a structured HTTP 400 instead of
     * Spring's default error page.
     *
     * @param ex  the missing-parameter exception
     * @param req the current request (for the path field)
     * @return a 400 response in the standard {@link ResponseDTO} envelope
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<?> handleMissingParam(MissingServletRequestParameterException ex, HttpServletRequest req) {
        String message = "Required parameter '" + ex.getParameterName() + "' is missing";
        log.warn("Bad request at {}: {}", req.getRequestURI(), message);
        ResponseDTO body = new ResponseDTO(
                message,
                400,
                req.getRequestURI(),
                false,
                BigInteger.valueOf(System.currentTimeMillis())
        );
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }
}
