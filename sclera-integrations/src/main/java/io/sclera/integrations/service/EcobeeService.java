package io.sclera.integrations.service;

import io.sclera.integrations.model.EcobeeSensor;
import io.sclera.integrations.repository.EcobeeSensorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EcobeeService {

    @Autowired
    private EcobeeSensorRepository repo;

    public List<EcobeeSensor> listAll() { return repo.findAll(); }
    public Optional<EcobeeSensor> findById(String id) { return repo.findById(id); }
    public EcobeeSensor save(EcobeeSensor s) { return repo.save(s); }
    public void deleteById(String id) { repo.deleteById(id); }
}
