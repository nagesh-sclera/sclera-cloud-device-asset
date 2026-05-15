package io.sclera.Repository;

import org.springframework.stereotype.Repository;

/** STUB Repository: real impl deferred to Phase 2 */
@Repository
public interface HistoryRepository {
    void deleteByDeviceId(String deviceId);

}
