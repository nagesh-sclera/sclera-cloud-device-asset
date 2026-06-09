package io.sclera.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Logging helper that emits a standardized WARN message whenever a Bucket-C stub is invoked.
 */
public final class StubLog {
    private static final Logger log = LoggerFactory.getLogger("io.sclera.stubs");
    private StubLog() {}
    /**
     * Logs a WARN entry recording the stub class, method and target microservice for a stubbed call.
     */
    public static void warn(String stubClass, String method, String target) {
        log.warn("STUB CALL: class={} method={} target_microservice={}", stubClass, method, target);
    }
}
