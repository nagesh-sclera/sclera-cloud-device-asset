package io.sclera.exception;

import com.alibaba.fastjson2.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.sclera.dto.ResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.hibernate.exception.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigInteger;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@ControllerAdvice
public class ScleraExceptionHandler {

    @ExceptionHandler(value = ClientException.class)
    public ResponseEntity<?> handleClientException(ClientException clientException) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        clientException.printStackTrace(pw);

        ResponseDTO responseDTO = new ResponseDTO(
                clientException.getMessage(),
                clientException.getStatus(),
                clientException.getPath(),
                false,
                BigInteger.valueOf(System.currentTimeMillis())
        );
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    @ExceptionHandler(value = ServerException.class)
    public ResponseEntity<?> handleServerException(ServerException serverException, HttpServletRequest httpServletRequest) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        serverException.printStackTrace(pw);

        ResponseDTO responseDTO = new ResponseDTO(
                serverException.getMessage(),
                serverException.getStatus(),
                httpServletRequest.getRequestURI(),
                false,
                BigInteger.valueOf(System.currentTimeMillis())
        );
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }


    @ExceptionHandler(value = APICallException.class)
    public ResponseEntity<?> handleAPICallException(APICallException apiCallException, HttpServletRequest httpServletRequest) throws JsonProcessingException {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        apiCallException.printStackTrace(pw);

        ResponseDTO responseDTO;
        if (apiCallException.getType() == 1) {
            responseDTO = new ResponseDTO(
                    apiCallException.getMessage(),
                    apiCallException.getStatus(),
                    httpServletRequest.getRequestURI(),
                    false,
                    BigInteger.valueOf(System.currentTimeMillis())
            );
        } else {
            if (apiCallException.getMessage() != null && !apiCallException.getMessage().isBlank()) {
                responseDTO = JSONObject.parseObject(apiCallException.getMessage(), ResponseDTO.class);
            } else {
                responseDTO = null;
            }
        }
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }


    @ExceptionHandler(value = ConstraintViolationException.class)
    public ResponseEntity<?> handleConstraintViolationException(ConstraintViolationException constraintViolationException, HttpServletRequest httpServletRequest) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        constraintViolationException.printStackTrace(pw);

        ResponseDTO responseDTO = new ResponseDTO(
                "Invalid client params",
                600,
                httpServletRequest.getRequestURI(),
                false,
                BigInteger.valueOf(System.currentTimeMillis())
        );
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

}
