package io.sclera.integrations.service;

import io.sclera.integrations.model.KNXGroup;
import io.sclera.integrations.model.KNXInterface;
import io.sclera.integrations.repository.KNXGroupRepository;
import io.sclera.integrations.repository.KNXInterfaceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class KNXService {

    @Autowired
    private KNXGroupRepository groupRepo;

    @Autowired
    private KNXInterfaceRepository interfaceRepo;

    // --- KNXGroup CRUD ---
    public List<KNXGroup> listAllGroups() { return groupRepo.findAll(); }
    public Optional<KNXGroup> getGroupById(String id) { return groupRepo.findById(id); }
    public KNXGroup saveGroup(KNXGroup g) { return groupRepo.save(g); }
    public void deleteGroupById(String id) { groupRepo.deleteById(id); }

    // --- KNXInterface CRUD ---
    public List<KNXInterface> listAllInterfaces() { return interfaceRepo.findAll(); }
    public Optional<KNXInterface> getInterfaceById(String id) { return interfaceRepo.findById(id); }
    public KNXInterface saveInterface(KNXInterface i) { return interfaceRepo.save(i); }
    public void deleteInterfaceById(String id) { interfaceRepo.deleteById(id); }
}
