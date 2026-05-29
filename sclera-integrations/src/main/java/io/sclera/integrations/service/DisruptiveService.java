package io.sclera.integrations.service;

import io.sclera.integrations.model.DisruptiveSensor;
import io.sclera.integrations.repository.DisruptiveSensorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DisruptiveService {

    @Autowired
    private DisruptiveSensorRepository repo;

    public List<DisruptiveSensor> listAll() { return repo.findAll(); }
    public Optional<DisruptiveSensor> findById(String id) { return repo.findById(id); }
    public DisruptiveSensor save(DisruptiveSensor s) { return repo.save(s); }
    public void deleteById(String id) { repo.deleteById(id); }
}
