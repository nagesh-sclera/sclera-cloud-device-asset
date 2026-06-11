package io.sclera.integrations.config;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Collections;
import java.util.List;

/**
 * The integrations service is a walking-skeleton: many stub endpoints declare
 * required @RequestParams (a, b, c, d) that the cloud-device-asset Dapr clients
 * don't supply, so the calls returned 400 and spammed WARN logs (e.g. when the
 * asset detail loads device sensors). These clients ignore the response and fall
 * back to safe defaults, so we turn those 400s into an empty 200 here — silencing
 * the noise without changing behaviour.
 */
@RestControllerAdvice
public class StubParamExceptionHandler {

    @ExceptionHandler({ MissingServletRequestParameterException.class, MethodArgumentTypeMismatchException.class })
    @ResponseStatus(HttpStatus.OK)
    public List<Object> handleMissingParam() {
        return Collections.emptyList();
    }
}
