package io.sclera.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class StubLog {
    private static final Logger log = LoggerFactory.getLogger("io.sclera.stubs");
    private StubLog() {}
    public static void warn(String stubClass, String method, String target) {
        log.warn("STUB CALL: class={} method={} target_microservice={}", stubClass, method, target);
    }
}
