package io.sclera.integrations.service;

import io.sclera.integrations.model.PelicanSensor;
import io.sclera.integrations.repository.PelicanSensorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PelicanService {

    @Autowired
    private PelicanSensorRepository repo;

    public List<PelicanSensor> listAll() { return repo.findAll(); }
    public Optional<PelicanSensor> findById(String id) { return repo.findById(id); }
    public PelicanSensor save(PelicanSensor s) { return repo.save(s); }
    public void deleteById(String id) { repo.deleteById(id); }
}
