package io.sclera.exception;

import io.sclera.integration.dto.ResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigInteger;

@ControllerAdvice
public class MaximoExceptionHandler {

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
