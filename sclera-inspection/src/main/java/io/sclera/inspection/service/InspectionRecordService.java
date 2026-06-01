package io.sclera.inspection.service;

import io.sclera.inspection.model.InspectionRecord;
import io.sclera.inspection.repository.InspectionRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
public class InspectionRecordService {

    @Autowired
    private InspectionRecordRepository repo;

    public List<InspectionRecord> listAll() { return repo.findAll(); }
    public Optional<InspectionRecord> findById(String id) { return repo.findById(id); }
    public InspectionRecord save(InspectionRecord e) { return repo.save(e); }
    public void deleteById(String id) { repo.deleteById(id); }

    public long countByDeviceId(String deviceId) {
        return repo.countByDeviceId(deviceId);
    }

    public void deleteByDeviceId(String deviceId) {
        repo.deleteByDeviceId(deviceId);
    }

    public void updateInspectionRecordStatus(String id, boolean status) {
        if (id == null) return;
        repo.updateStatusById(id, status ? "archived" : "open");
    }

    public void updateInspectionStatusOnDeviceArchive(Collection<String> deviceIds) {
        if (deviceIds == null || deviceIds.isEmpty()) return;
        repo.archiveByDeviceIds(deviceIds);
    }

    public void updateInspectionRecord(String email) {
        if (email == null) return;
        repo.stampUpdatedEmail(email);
    }
}