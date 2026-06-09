package io.sclera.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** STUB: edge-only proxy service */
@Service
public class ProxyService {
    private static final Logger log = LoggerFactory.getLogger(ProxyService.class);

    /**
     * Stub for verifying and restarting the proxy client for the given VDMS; logs a warning only.
     */
    public void verifyAndRestartProxyClient(String vdmsId) {
        log.warn("STUB: verifyAndRestartProxyClient called");
    }

    /**
     * Stub for synchronizing the proxy server for the given VDMS; logs a warning only.
     */
    public void syncProxyServer(String vdmsId) {
        log.warn("STUB: syncProxyServer called");
    }

    /**
     * Stub for synchronizing the proxy client for the given VDMS; logs a warning only.
     */
    public void syncProxyClient(String vdmsId) {
        log.warn("STUB: syncProxyClient called");
    }
}
