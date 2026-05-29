package io.sclera.inspection.service;

import io.sclera.inspection.model.GlobalChecklist;
import io.sclera.inspection.repository.GlobalChecklistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GlobalChecklistService {

    @Autowired
    private GlobalChecklistRepository repo;

    public List<GlobalChecklist> listAll() { return repo.findAll(); }
    public Optional<GlobalChecklist> findById(String id) { return repo.findById(id); }
    public GlobalChecklist save(GlobalChecklist e) { return repo.save(e); }
    public void deleteById(String id) { repo.deleteById(id); }

    public long countByDeviceId(String deviceId) { return repo.countByDeviceId(deviceId); }

    public void deleteByDeviceId(String deviceId) { repo.deleteByDeviceId(deviceId); }

    public void updateDeviceGlobalChecklistDeviceId(String oldId, String newId) {
        if (oldId == null || newId == null) return;
        repo.reassignDeviceId(oldId, newId);
    }
}