package io.sclera.interfaces;

/** Service contract for the matching service class. */
public interface MasterSlaveAPICallServiceInterface {
    String accessMasterFromSlave(String url, String method, Object body, Object headers);
    String accessSlaveFromMaster(String url, String method, Object body, Object headers);
}
