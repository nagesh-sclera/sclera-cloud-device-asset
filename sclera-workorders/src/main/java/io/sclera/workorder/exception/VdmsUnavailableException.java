package io.sclera.workorder.exception;

/**
 * Thrown when the Dapr sidecar cannot reach vdms-service (sidecar down, app
 * down, transport error, 5xx response). Used only by the NEW sample method
 * — existing Maximo flows do not call vdms-service so they cannot produce
 * this exception.
 */
public class VdmsUnavailableException extends RuntimeException {

    public VdmsUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
