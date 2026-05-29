package io.sclera.integrations.service;

import io.sclera.integrations.model.LorawanConfiguration;
import io.sclera.integrations.model.Lorawan_Sensor;
import io.sclera.integrations.model.Lorawan_Sensor_Attributes;
import io.sclera.integrations.repository.LorawanConfigurationRepository;
import io.sclera.integrations.repository.Lorawan_SensorRepository;
import io.sclera.integrations.repository.Lorawan_Sensor_AttributesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class LorawanService {

    @Autowired
    private LorawanConfigurationRepository configRepo;

    @Autowired
    private Lorawan_SensorRepository sensorRepo;

    @Autowired
    private Lorawan_Sensor_AttributesRepository attrRepo;

    // Configuration CRUD
    public List<LorawanConfiguration> listAllConfigurations() { return configRepo.findAll(); }
    public Optional<LorawanConfiguration> getConfigurationById(String id) { return configRepo.findById(id); }
    public LorawanConfiguration saveConfiguration(LorawanConfiguration c) { return configRepo.save(c); }
    public void deleteConfigurationById(String id) { configRepo.deleteById(id); }

    // Sensor CRUD
    public List<Lorawan_Sensor> listAllSensors() { return sensorRepo.findAll(); }
    public Optional<Lorawan_Sensor> getSensorById(String id) { return sensorRepo.findById(id); }
    public Lorawan_Sensor saveSensor(Lorawan_Sensor s) { return sensorRepo.save(s); }
    public void deleteSensorById(String id) { sensorRepo.deleteById(id); }

    // Sensor attributes CRUD
    public Lorawan_Sensor_Attributes saveSensorAttributes(Lorawan_Sensor_Attributes a) { return attrRepo.save(a); }
    public Optional<Lorawan_Sensor_Attributes> getSensorAttributesById(String id) { return attrRepo.findById(id); }
}
