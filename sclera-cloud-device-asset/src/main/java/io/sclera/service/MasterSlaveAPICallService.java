package io.sclera.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** STUB: replace with remote call to edge-D */
@Service
public class MasterSlaveAPICallService {

    private static final Logger log = LoggerFactory.getLogger(MasterSlaveAPICallService.class);

    public String accessMasterFromSlave(String url, String method, Object body, Object headers) {
        log.warn("STUB: accessMasterFromSlave called with url={}", url);
        return null;
    }

    public String accessSlaveFromMaster(String url, String method, Object body, Object headers) {
        log.warn("STUB: accessSlaveFromMaster called with url={}", url);
        return null;
    }
}
