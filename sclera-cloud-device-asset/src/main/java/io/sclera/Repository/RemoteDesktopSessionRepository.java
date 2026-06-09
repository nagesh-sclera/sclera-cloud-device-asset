package io.sclera.Repository;

import org.springframework.stereotype.Repository;

/** STUB Repository: real impl deferred to Phase 2 */
@Repository
public interface RemoteDesktopSessionRepository {
    // Methods added on demand by compile loop.

    /**
     * Deletes the remote desktop sessions associated with the given device.
     *
     * @param deviceId the device identifier
     */
    void deleteByDeviceId(String deviceId);

}
