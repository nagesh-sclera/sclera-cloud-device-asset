package io.sclera.integrations.service;

import io.sclera.integrations.model.DaintreeDevice;
import io.sclera.integrations.repository.DaintreeDeviceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DaintreeService {

    @Autowired
    private DaintreeDeviceRepository repo;

    public List<DaintreeDevice> listAll() { return repo.findAll(); }
    public Optional<DaintreeDevice> findById(String id) { return repo.findById(id); }
    public DaintreeDevice save(DaintreeDevice d) { return repo.save(d); }
    public void deleteById(String id) { repo.deleteById(id); }
}
