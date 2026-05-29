package io.sclera.integrations.service;

import io.sclera.integrations.model.ConnectedDevices;
import io.sclera.integrations.repository.ConnectedDevicesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ConnectedDevicesService {

    @Autowired
    private ConnectedDevicesRepository repo;

    public List<ConnectedDevices> listAll() { return repo.findAll(); }
    public Optional<ConnectedDevices> findById(String id) { return repo.findById(id); }
    public ConnectedDevices save(ConnectedDevices c) { return repo.save(c); }
    public void deleteById(String id) { repo.deleteById(id); }
    public void deleteBySpecificationsId(String specificationsId) {
        repo.findAll().stream()
                .filter(cd -> specificationsId.equals(cd.getSpecifications_id())
                        || specificationsId.equals(cd.getConnected_specifications_id()))
                .forEach(cd -> repo.delete(cd));
    }
}
