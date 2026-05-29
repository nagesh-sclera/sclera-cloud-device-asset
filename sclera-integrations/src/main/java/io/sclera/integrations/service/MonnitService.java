package io.sclera.integrations.service;

import io.sclera.integrations.model.Monnit_Sensor;
import io.sclera.integrations.repository.Monnit_SensorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MonnitService {

    @Autowired
    private Monnit_SensorRepository repo;

    public List<Monnit_Sensor> listAll() { return repo.findAll(); }
    public Optional<Monnit_Sensor> findById(String id) { return repo.findById(id); }
    public Monnit_Sensor save(Monnit_Sensor s) { return repo.save(s); }
    public void deleteById(String id) { repo.deleteById(id); }
}
