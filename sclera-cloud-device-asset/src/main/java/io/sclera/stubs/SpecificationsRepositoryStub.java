package io.sclera.stubs;

import io.sclera.Repository.SpecificationsRepository;
import io.sclera.dto.SpecificationsDTO;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class SpecificationsRepositoryStub implements SpecificationsRepository {

    @Override
    public void editDeviceSpecifications(String id, String keyValue, String keyUnit, String keyName) {
    }

    @Override
    public Integer checkSpecificationByDeviceId(String deviceId, String keyName) {
        return 0;
    }

    @Override
    public List<SpecificationsDTO> getDeviceSpecificationsBasedOnDeviceId(String deviceId) {
        return Collections.emptyList();
    }

    @Override
    public void upsertDeviceSpecification(String id, String keyName, String keyValue, String keyUnit, String deviceId) {
    }

    @Override
    public void deleteById(String id) {
    }

    @Override
    public SpecificationsDTO getDeviceSpecificationsBasedOnDeviceIdAndKeyName(String deviceId, String keyName) {
        return null;
    }

    @Override
    public SpecificationsDTO getPower(String deviceId, String keyName) {
        return null;
    }
}
