package io.sclera.Repository;

import io.sclera.dto.SpecificationsDTO;
import org.springframework.stereotype.Repository;

import java.util.List;

/** STUB Repository: real impl deferred to Phase 2 */
@Repository
public interface SpecificationsRepository {
    void editDeviceSpecifications(String id, String keyValue, String keyUnit, String keyName);
    Integer checkSpecificationByDeviceId(String deviceId, String keyName);
    List<SpecificationsDTO> getDeviceSpecificationsBasedOnDeviceId(String deviceId);
    void upsertDeviceSpecification(String id, String keyName, String keyValue, String keyUnit, String deviceId);
    void deleteById(String id);
    SpecificationsDTO getDeviceSpecificationsBasedOnDeviceIdAndKeyName(String deviceId, String keyName);
    SpecificationsDTO getPower(String deviceId, String keyName);
}
