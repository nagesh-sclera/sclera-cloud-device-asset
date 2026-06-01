package io.sclera.inspection.service;

import io.sclera.inspection.model.GlobalInspectionRelation;
import io.sclera.inspection.repository.GlobalInspectionRelationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
public class GlobalInspectionRelationService {

    @Autowired
    private GlobalInspectionRelationRepository repo;

    public List<GlobalInspectionRelation> listAll() { return repo.findAll(); }
    public Optional<GlobalInspectionRelation> findById(String id) { return repo.findById(id); }
    public GlobalInspectionRelation save(GlobalInspectionRelation e) { return repo.save(e); }
    public void deleteById(String id) { repo.deleteById(id); }

    public long countByDeviceId(String deviceId) { return repo.countByDeviceId(deviceId); }
    public void deleteByDeviceId(String deviceId) { repo.deleteByDeviceId(deviceId); }

    public void updateDeviceAndIsRemoved(Collection<String> ids) {
        if (ids == null || ids.isEmpty()) return;
        repo.softDeleteByIds(ids);
    }

    public void deleteInBatch(Collection<String> ids) {
        if (ids == null || ids.isEmpty()) return;
        repo.deleteAllById(ids);
    }

    public void updateGlobalInspectionByDeviceId(String primaryDeviceId, String existingDeviceId) {
        if (primaryDeviceId == null || existingDeviceId == null) return;
        repo.reassignDeviceId(existingDeviceId, primaryDeviceId);
    }

    public void updateLocationAndIsRemoved(Collection<String> locationIds) {
        if (locationIds == null || locationIds.isEmpty()) return;
        repo.softDeleteByLocationIds(locationIds);
    }

    /** Stamp updated_email across all rows. */
    public void updateGlobalInspectionRecord(String email) {
        if (email == null) return;
        repo.stampUpdatedEmail(email);
    }
}