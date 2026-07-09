package io.sclera.service.impl;
import io.sclera.service.*;

import io.sclera.service.MasterSlaveAPICallService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** STUB: replace with remote call to edge-D */
@Service
public class MasterSlaveAPICallServiceImpl implements MasterSlaveAPICallService {

    private static final Logger log = LoggerFactory.getLogger(MasterSlaveAPICallServiceImpl.class);

    /**
     * Proxies a request from a slave node to the master node. Stub logs a warning and returns null.
     */
    public String accessMasterFromSlave(String url, String method, Object body, Object headers) {
        log.warn("STUB: accessMasterFromSlave called with url={}", url);
        return null;
    }

    /**
     * Proxies a request from the master node to a slave node. Stub logs a warning and returns null.
     */
    public String accessSlaveFromMaster(String url, String method, Object body, Object headers) {
        log.warn("STUB: accessSlaveFromMaster called with url={}", url);
        return null;
    }
}
