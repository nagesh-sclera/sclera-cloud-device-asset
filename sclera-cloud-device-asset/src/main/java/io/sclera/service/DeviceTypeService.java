package io.sclera.service;

import io.sclera.Repository.DeviceRepository;
import io.sclera.Repository.DeviceTypesRepository;
import io.sclera.dto.DeviceTypesDTO;
import io.sclera.queryrepository.DeviceTypeQueryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
public class DeviceTypeService {
    private static final Logger log = LoggerFactory.getLogger(DeviceTypeService.class);

    private final DeviceTypesRepository deviceTypesRepository;
    @Autowired
    APICallService apiCallService;
    @Autowired
    DataSource dataSource;
    @Autowired
    DeviceTypeQueryRepository deviceTypesQueryRepository;
    @Autowired
    DeviceRepository deviceRepository;
    public DeviceTypeService(DeviceTypesRepository deviceTypesRepository) {
        this.deviceTypesRepository = deviceTypesRepository;
    }
    public void upsertDeviceType(List<DeviceTypesDTO> deviceTypes) {
        if (deviceTypes == null || deviceTypes.isEmpty()) {
            log.warn("No device types provided for upsert.");
            return;
        }

        try {
            List<DeviceTypesDTO> existingDeviceTypes = deviceTypesRepository.getAllDeviceTypes();
            if (existingDeviceTypes == null) {
                existingDeviceTypes = Collections.emptyList();
            }

            Map<String, BigInteger> existingMap = existingDeviceTypes.stream()
                    .filter(dt -> dt.getId() != null) // avoid null keys
                    .collect(Collectors.toMap(
                            DeviceTypesDTO::getId,
                            DeviceTypesDTO::getUpdatedTimestamp,
                            (a, b) -> a // keep first if duplicates exist
                    ));

            for (DeviceTypesDTO deviceType : deviceTypes) {
                if (deviceType == null || deviceType.getId() == null) {
                    log.warn("Skipping null or invalid device type entry: {}", deviceType);
                    continue;
                }

                BigInteger existingLastModified = existingMap.get(deviceType.getId());

                if (existingLastModified != null
                        && deviceType.getUpdatedTimestamp() != null
                        &&(existingLastModified.compareTo(deviceType.getUpdatedTimestamp()) >= 0)) {
                    log.info("Device type with id {} is up to date. Skipping upsert.", deviceType.getId());
                    continue;
                }

                deviceTypesRepository.upsert(
                        deviceType.getId(),
                        deviceType.getName(),
                        deviceType.getUpdatedTimestamp()
                );
            }

        } catch (Exception e) {
            log.error("Error upserting device types: {}", e.getMessage(), e); // log stack trace too
        }
    }

    public void syncAndUpsertDeviceTypes(String vdmsId) {
        try {
            this.updateDeviceTypesTable(vdmsId);
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            executorService.execute(() -> {
                this.updateDeviceTypes(vdmsId);
            });
            executorService.shutdown();
        } catch (Exception e) {
            log.error("Error syncing/upserting device types: {}", e.getMessage(), e);
        }
    }

    private void updateDeviceTypes(String vdmsId) {
        List<DeviceTypesDTO> updatedDeviceTypes = deviceTypesRepository.getAllUpdatedDeviceTypes(vdmsId);
        log.info("Total updated device types to process: {}", updatedDeviceTypes.size());
        if(updatedDeviceTypes == null || updatedDeviceTypes.isEmpty()){
            log.info("No updated device types to process.");
            return;
        }
        for(DeviceTypesDTO dto : updatedDeviceTypes){
            log.info("Updated DeviceType - Name: {}, Old Name: {}", dto.getName(), dto.getOldName());
            deviceRepository.updateDeviceType(dto.getOldName(), dto.getName());
            deviceTypesRepository.deleteOldName(dto.getOldName());
        }
    }

    private void updateDeviceTypesTable(String vdmsId){
        log.info("updateDeviceTypes");
        Set<DeviceTypesDTO> deviceTypesDTOS = new HashSet<>();
        int pageNumber = 1;
        int pageSize = 100;
        BigInteger maxUpdatedTimestamp = deviceTypesRepository.findMaxUpdatedTimestamp();
        log.info("Max updated timestamp in DB: {}", maxUpdatedTimestamp);
        while (true) {
            Set<DeviceTypesDTO> deviceTypes = apiCallService.getUpdatedAssetTypes(maxUpdatedTimestamp,pageNumber,pageSize,"", vdmsId);

            // apply updatedTimestamp fallback logic
            if (deviceTypes == null || deviceTypes.isEmpty()) {
                log.info("No more device types to fetch from API. Exiting pagination loop.");
                break;
            }
            log.info("Fetched device types from API on page {}", pageNumber);
            deviceTypesDTOS.addAll(deviceTypes);

            if (deviceTypes.size() < pageSize) {
                break;
            } else {
                pageNumber++;
            }
        }

        log.info("total device types size: {} ", deviceTypesDTOS.size());
        this.batchUpdateDeviceTypes(deviceTypesDTOS);
    }

    public void batchUpdateDeviceTypes(Set<DeviceTypesDTO> deviceTypesDTOS){
        log.info("batchUpdateDeviceTypes");
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement preparedStatementUpdate = connection.prepareStatement(deviceTypesQueryRepository.getQueryForUpsertDeviceTypesInBatch());
            int batchCounter = 0;
            int maxBatchLimit = 100;
            log.info("Preparing to upsert DeviceType");
            for (DeviceTypesDTO deviceTypesDTO : deviceTypesDTOS) {
                try {

                    preparedStatementUpdate.setString(1, deviceTypesDTO.getId());
                    preparedStatementUpdate.setString(2, deviceTypesDTO.getName());
                    if (deviceTypesDTO.getUpdatedTimestamp() == null && deviceTypesDTO.getCreationTimestamp() != null) {
                        preparedStatementUpdate.setBigDecimal(3, new BigDecimal(deviceTypesDTO.getCreationTimestamp()));
                    } else {
                        preparedStatementUpdate.setBigDecimal(3, new BigDecimal(deviceTypesDTO.getUpdatedTimestamp()));
                    }
                    preparedStatementUpdate.addBatch();
                    batchCounter++;
                    if (batchCounter == maxBatchLimit) {
                        preparedStatementUpdate.executeBatch();
                        log.info("added 100 DeviceTypes in a batch");
                        preparedStatementUpdate.clearBatch();
                        batchCounter = 0;
                    }
                } catch (Exception e) {
                    log.error("Exception in batch update of DeviceTypes", e);
                }
            }
            if (batchCounter > 0) {
                preparedStatementUpdate.executeBatch();
                log.info("Excecuted batch update of: {} DeviceTypes ", batchCounter);
            }
            connection.close();
            preparedStatementUpdate.close();
        } catch (Exception e) {
            log.error("Exception in batch update of DeviceTypes", e);

        }
    }

    public List<DeviceTypesDTO> getAllDeviceTypes() {
        try {
            return deviceTypesRepository.getAllDeviceTypes();
        } catch (Exception e) {
            log.error("Error fetching device types: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }


}
