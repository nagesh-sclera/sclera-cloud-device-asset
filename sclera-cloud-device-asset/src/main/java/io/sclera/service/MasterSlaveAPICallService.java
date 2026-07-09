package io.sclera.service;

/** Service contract for the matching service class. */
public interface MasterSlaveAPICallService {
    String accessMasterFromSlave(String url, String method, Object body, Object headers);
    String accessSlaveFromMaster(String url, String method, Object body, Object headers);
}
