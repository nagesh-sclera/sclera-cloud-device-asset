package io.sclera.integrations.service;

import io.sclera.integrations.model.ModbusRegister;
import io.sclera.integrations.repository.ModbusRegisterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ModbusService {

    @Autowired
    private ModbusRegisterRepository repo;

    public List<ModbusRegister> listAll() { return repo.findAll(); }
    public Optional<ModbusRegister> findById(String id) { return repo.findById(id); }
    public ModbusRegister save(ModbusRegister r) { return repo.save(r); }
    public void deleteById(String id) { repo.deleteById(id); }
}
