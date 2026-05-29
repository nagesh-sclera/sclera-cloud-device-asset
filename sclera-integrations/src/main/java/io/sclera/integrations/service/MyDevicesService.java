package io.sclera.integrations.service;

import io.sclera.integrations.model.MyDevicesCompany;
import io.sclera.integrations.model.MyDevicesSensor;
import io.sclera.integrations.repository.MyDevicesCompanyRepository;
import io.sclera.integrations.repository.MyDevicesSensorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MyDevicesService {

    @Autowired
    private MyDevicesCompanyRepository companyRepo;

    @Autowired
    private MyDevicesSensorRepository sensorRepo;

    // --- Company CRUD ---
    public List<MyDevicesCompany> listAllCompanies() { return companyRepo.findAll(); }
    public Optional<MyDevicesCompany> getCompanyById(String id) { return companyRepo.findById(id); }
    public MyDevicesCompany upsertCompany(MyDevicesCompany c) { return companyRepo.save(c); }
    public void deleteCompanyById(String id) { companyRepo.deleteById(id); }

    // --- Sensor CRUD ---
    public List<MyDevicesSensor> listAllSensors() { return sensorRepo.findAll(); }
    public Optional<MyDevicesSensor> getSensorById(String id) { return sensorRepo.findById(id); }
    public MyDevicesSensor saveSensor(MyDevicesSensor s) { return sensorRepo.save(s); }
    public void deleteSensorById(String id) { sensorRepo.deleteById(id); }

    /** Lookup owning device_id from a sensor id. */
    public String getDeviceIdByMyDevicesSensorId(String sensorId) {
        return sensorRepo.findById(sensorId).map(MyDevicesSensor::getDevice_id).orElse(null);
    }
}
