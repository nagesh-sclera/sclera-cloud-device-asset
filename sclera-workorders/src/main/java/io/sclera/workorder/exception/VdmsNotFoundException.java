package io.sclera.workorder.exception;

/**
 * Thrown when vdms-service responds 404 to a Dapr-invoked lookup.
 * Used only by the NEW sample method that fetches VDMS details across the
 * Dapr boundary. Existing flows do not produce this exception.
 */
public class VdmsNotFoundException extends RuntimeException {

    private final String vdmsId;

    public VdmsNotFoundException(String vdmsId) {
        super("VDMS not found: " + vdmsId);
        this.vdmsId = vdmsId;
    }

    public String getVdmsId() {
        return vdmsId;
    }
}
