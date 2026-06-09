package io.sclera.exception;

import io.sclera.integration.dto.ResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import jakarta.servlet.http.HttpServletRequest;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigInteger;

/**
 * Global controller advice that translates {@link MaximoException} instances into a
 * standardized {@link ResponseDTO} error response with HTTP 500 status.
 */
@ControllerAdvice
public class MaximoExceptionHandler {

    /**
     * Handles a {@link MaximoException} by building a {@link ResponseDTO} from the
     * exception details and request URI, returned with HTTP 500 (internal server error).
     */
    @ExceptionHandler(value = MaximoException.class)
    public ResponseEntity<?> handleMaximoException(MaximoException maximoException, HttpServletRequest httpServletRequest) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        maximoException.printStackTrace(pw);

        ResponseDTO responseDTO = new ResponseDTO(
                maximoException.getMessage(),
                maximoException.getErrorCode(),
                httpServletRequest.getRequestURI(),
                false,
                BigInteger.valueOf(System.currentTimeMillis())
        );

        return new ResponseEntity<>(responseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
