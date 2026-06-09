package io.sclera.Repository;

import io.sclera.dto.SpecificationsDTO;
import org.springframework.stereotype.Repository;

import java.util.List;

/** STUB Repository: real impl deferred to Phase 2 */
@Repository
public interface SpecificationsRepository {
    /**
     * Updates the value, unit, and name of the specification with the given identifier.
     *
     * @param id the specification identifier
     * @param keyValue the new value
     * @param keyUnit the new unit
     * @param keyName the new key name
     */
    void editDeviceSpecifications(String id, String keyValue, String keyUnit, String keyName);

    /**
     * Returns whether a specification with the given key name exists for the device.
     *
     * @param deviceId the device identifier
     * @param keyName the specification key name
     * @return a non-zero value if the specification exists, otherwise zero
     */
    Integer checkSpecificationByDeviceId(String deviceId, String keyName);

    /**
     * Returns the specifications belonging to the given device.
     *
     * @param deviceId the device identifier
     * @return the matching specifications
     */
    List<SpecificationsDTO> getDeviceSpecificationsBasedOnDeviceId(String deviceId);

    /**
     * Inserts a device specification, or updates it when the identifier already exists.
     *
     * @param id the specification identifier
     * @param keyName the specification key name
     * @param keyValue the specification value
     * @param keyUnit the specification unit
     * @param deviceId the owning device identifier
     */
    void upsertDeviceSpecification(String id, String keyName, String keyValue, String keyUnit, String deviceId);

    /**
     * Deletes the specification with the given identifier.
     *
     * @param id the specification identifier
     */
    void deleteById(String id);

    /**
     * Returns the specification matching the given device and key name.
     *
     * @param deviceId the device identifier
     * @param keyName the specification key name
     * @return the matching specification
     */
    SpecificationsDTO getDeviceSpecificationsBasedOnDeviceIdAndKeyName(String deviceId, String keyName);

    /**
     * Returns the power specification matching the given device and key name.
     *
     * @param deviceId the device identifier
     * @param keyName the specification key name
     * @return the matching power specification
     */
    SpecificationsDTO getPower(String deviceId, String keyName);
}
