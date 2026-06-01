package io.sclera.inspection.service;

import io.sclera.inspection.model.CheckListRecord;
import io.sclera.inspection.model.CheckListTemplate;
import io.sclera.inspection.repository.CheckListRecordRepository;
import io.sclera.inspection.repository.CheckListTemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ChecklistService {

    @Autowired
    private CheckListTemplateRepository templateRepo;

    @Autowired
    private CheckListRecordRepository recordRepo;

    // --- CheckListTemplate ---
    public List<CheckListTemplate> listAllTemplates() { return templateRepo.findAll(); }
    public Optional<CheckListTemplate> getTemplateById(String id) { return templateRepo.findById(id); }
    public CheckListTemplate saveTemplate(CheckListTemplate t) { return templateRepo.save(t); }
    public void deleteTemplateById(String id) { templateRepo.deleteById(id); }
    public long getCheckListTemplatesCountByDeviceId(String deviceId) {
        return templateRepo.findAll().stream().filter(t -> deviceId != null && deviceId.equals(t.getDevice_id())).count();
    }

    // --- CheckListRecord ---
    public List<CheckListRecord> listAllRecords() { return recordRepo.findAll(); }
    public Optional<CheckListRecord> getRecordById(String id) { return recordRepo.findById(id); }
    public CheckListRecord saveRecord(CheckListRecord r) { return recordRepo.save(r); }
    public void deleteRecordById(String id) { recordRepo.deleteById(id); }
}