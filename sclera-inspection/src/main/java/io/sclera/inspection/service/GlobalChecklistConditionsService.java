package io.sclera.inspection.service;

import io.sclera.inspection.model.GlobalChecklistConditions;
import io.sclera.inspection.repository.GlobalChecklistConditionsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
public class GlobalChecklistConditionsService {

    @Autowired
    private GlobalChecklistConditionsRepository repo;

    public List<GlobalChecklistConditions> listAll() { return repo.findAll(); }
    public Optional<GlobalChecklistConditions> findById(String id) { return repo.findById(id); }
    public GlobalChecklistConditions save(GlobalChecklistConditions e) { return repo.save(e); }
    public void deleteById(String id) { repo.deleteById(id); }

    public long countByDeviceId(String deviceId) { return repo.countByDeviceId(deviceId); }
    public void deleteByDeviceId(String deviceId) { repo.deleteByDeviceId(deviceId); }

    public void updateDeviceAndIsRemoved(Collection<String> ids) {
        if (ids == null || ids.isEmpty()) return;
        repo.softDeleteByIds(ids);
    }

    public void updateLocationAndIsRemoved(Collection<String> locationIds) {
        if (locationIds == null || locationIds.isEmpty()) return;
        repo.softDeleteByLocationIds(locationIds);
    }
}