package io.sclera.integrations.service;

import io.sclera.integrations.model.Bacnet_Attributes;
import io.sclera.integrations.model.Bacnet_Device;
import io.sclera.integrations.model.Bacnet_Object;
import io.sclera.integrations.repository.Bacnet_AttributesRepository;
import io.sclera.integrations.repository.Bacnet_DeviceRepository;
import io.sclera.integrations.repository.Bacnet_ObjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BacnetService {

    @Autowired
    private Bacnet_DeviceRepository deviceRepo;

    @Autowired
    private Bacnet_ObjectRepository objectRepo;

    @Autowired
    private Bacnet_AttributesRepository attributesRepo;

    // --- Bacnet_Device CRUD ---
    public List<Bacnet_Device> listAllBacnetDevices() { return deviceRepo.findAll(); }
    public Optional<Bacnet_Device> getBacnetDeviceById(String id) { return deviceRepo.findById(id); }
    public Bacnet_Device saveBacnetDevice(Bacnet_Device d) { return deviceRepo.save(d); }
    public void deleteBacnetDeviceById(String id) { deviceRepo.deleteById(id); }

    // --- Bacnet_Object CRUD + queries ---
    public Bacnet_Object saveBacnetObject(Bacnet_Object o) { return objectRepo.save(o); }
    public long getBacnetObjectCountByDeviceId(String deviceId) { return objectRepo.countByDeviceId(deviceId); }
    public List<String> getBacnetObjectIdsByDeviceId(String deviceId) { return objectRepo.findIdsByDeviceId(deviceId); }

    /** Composite-PK lookup: given (bacnetDeviceId, bacnetObjectId), return owning device_id. */
    public String getDeviceIdByBacnetObjectId(String bacnetDeviceId, String bacnetObjectId) {
        // Bacnet_Object PK is composite (id + bacnet_device). Direct findById needs the IdClass instance.
        // For now fall back to a list scan in absence of a more specific @Query.
        return objectRepo.findAll().stream()
                .filter(o -> bacnetObjectId.equals(o.getId()) && o.getBacnet_device() != null
                        && bacnetDeviceId.equals(o.getBacnet_device().getId()))
                .map(Bacnet_Object::getDevice_id)
                .findFirst()
                .orElse(null);
    }

    // --- Bacnet_Attributes CRUD ---
    public Bacnet_Attributes saveBacnetAttributes(Bacnet_Attributes a) { return attributesRepo.save(a); }
    public Optional<Bacnet_Attributes> getBacnetAttributesById(String id) { return attributesRepo.findById(id); }
}
