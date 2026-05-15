package io.sclera.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
@Repository
@Primary
public class InventoryDeviceRepositoryImpl implements InventoryDeviceRepository {
    private static final Logger log = LoggerFactory.getLogger(InventoryDeviceRepositoryImpl.class);
    @Override public void deleteByDeviceId(String deviceId) { log.warn("[STUB] InventoryDeviceRepository.deleteByDeviceId"); }
}
