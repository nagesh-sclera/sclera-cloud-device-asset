package io.sclera.Repository;
import io.sclera.dto.SpecificationsDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import java.util.Collections;
import java.util.List;
@Repository
@Primary
public class SpecificationsRepositoryImpl implements SpecificationsRepository {
    private static final Logger log = LoggerFactory.getLogger(SpecificationsRepositoryImpl.class);
    @Override public void editDeviceSpecifications(String id, String keyValue, String keyUnit, String keyName) { log.warn("[STUB] SpecificationsRepository.editDeviceSpecifications"); }
    @Override public Integer checkSpecificationByDeviceId(String deviceId, String keyName) { log.warn("[STUB] SpecificationsRepository.checkSpecificationByDeviceId"); return 0; }
    @Override public List<SpecificationsDTO> getDeviceSpecificationsBasedOnDeviceId(String deviceId) { log.warn("[STUB] SpecificationsRepository.getDeviceSpecificationsBasedOnDeviceId"); return Collections.emptyList(); }
    @Override public void upsertDeviceSpecification(String id, String keyName, String keyValue, String keyUnit, String deviceId) { log.warn("[STUB] SpecificationsRepository.upsertDeviceSpecification"); }
    @Override public void deleteById(String id) { log.warn("[STUB] SpecificationsRepository.deleteById"); }
    @Override public SpecificationsDTO getDeviceSpecificationsBasedOnDeviceIdAndKeyName(String deviceId, String keyName) { log.warn("[STUB] SpecificationsRepository.getDeviceSpecificationsBasedOnDeviceIdAndKeyName"); return null; }
    @Override public SpecificationsDTO getPower(String deviceId, String keyName) { log.warn("[STUB] SpecificationsRepository.getPower"); return null; }
}
