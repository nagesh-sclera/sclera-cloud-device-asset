package io.sclera.Repository;

import org.springframework.stereotype.Repository;

/** STUB Repository: real impl deferred to Phase 2 */
@Repository
public interface ClientQrCodeRepository {
    Integer countByDeviceId(String deviceId);
}
