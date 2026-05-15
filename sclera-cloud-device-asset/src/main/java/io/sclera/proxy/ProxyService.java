package io.sclera.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** STUB: edge-only proxy service */
@Service
public class ProxyService {
    private static final Logger log = LoggerFactory.getLogger(ProxyService.class);

    public void verifyAndRestartProxyClient(String vdmsId) {
        log.warn("STUB: verifyAndRestartProxyClient called");
    }

    public void syncProxyServer(String vdmsId) {
        log.warn("STUB: syncProxyServer called");
    }

    public void syncProxyClient(String vdmsId) {
        log.warn("STUB: syncProxyClient called");
    }
}
