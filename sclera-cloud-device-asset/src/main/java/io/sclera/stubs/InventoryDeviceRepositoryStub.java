package io.sclera.stubs;

import io.sclera.Repository.InventoryDeviceRepository;
import org.springframework.stereotype.Component;

@Component
public class InventoryDeviceRepositoryStub implements InventoryDeviceRepository {

    @Override
    public void deleteByDeviceId(String deviceId) {
    }
}
