package io.sclera.integrations.service;

import io.sclera.integrations.model.Datahoist;
import io.sclera.integrations.repository.DatahoistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DatahoistService {

    @Autowired
    private DatahoistRepository repo;

    public List<Datahoist> listAll() { return repo.findAll(); }
    public Optional<Datahoist> findById(String id) { return repo.findById(id); }
    public Datahoist save(Datahoist d) { return repo.save(d); }
    public void deleteById(String id) { repo.deleteById(id); }
}
