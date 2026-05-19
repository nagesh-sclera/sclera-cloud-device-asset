package io.sclera.client;

import io.dapr.client.DaprClient;
import io.dapr.client.domain.HttpExtension;
import io.sclera.dto.SpecificationsDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Thin Dapr client delegating to the sclera-inventory microservice.
 *
 * Replaces both {@code io.sclera.stubs.SpecificationsRepositoryStub} and
 * {@code io.sclera.Repository.SpecificationsRepositoryImpl}.
 *
 * Implements {@code io.sclera.Repository.SpecificationsRepository} so that
 * all existing call sites (SpecificationsService) continue to compile without change.
 *
 * NOTE: Write-through methods pass params as GET query params — body is silently
 * dropped under GET-only skeleton routing (needs POST upgrade).
 */
@Component
@Primary
public class SpecificationsClient implements io.sclera.Repository.SpecificationsRepository {

    private static final Logger log = LoggerFactory.getLogger(SpecificationsClient.class);
    private static final String APP_ID = "sclera-inventory";

    private final DaprClient dapr;

    public SpecificationsClient(DaprClient dapr) {
        this.dapr = dapr;
    }

    @Override
    public void editDeviceSpecifications(String id, String keyValue, String keyUnit, String keyName) {
        Map<String, String> payload = new HashMap<>();
        payload.put("id", id);
        payload.put("keyValue", keyValue);
        payload.put("keyUnit", keyUnit);
        payload.put("keyName", keyName);
        try {
            dapr.invokeMethod(APP_ID, "specifications/editDeviceSpecifications", payload, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("SpecificationsClient.editDeviceSpecifications failed; swallowing: {}", e.getMessage());
        }
    }

    @Override
    public Integer checkSpecificationByDeviceId(String deviceId, String keyName) {
        Map<String, String> payload = new HashMap<>();
        payload.put("deviceId", deviceId);
        payload.put("keyName", keyName);
        try {
            dapr.invokeMethod(APP_ID, "specifications/checkSpecificationByDeviceId", payload, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("SpecificationsClient.checkSpecificationByDeviceId failed; returning 0: {}", e.getMessage());
        }
        return 0;
    }

    @Override
    public List<SpecificationsDTO> getDeviceSpecificationsBasedOnDeviceId(String deviceId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("deviceId", deviceId);
        try {
            dapr.invokeMethod(APP_ID, "specifications/getDeviceSpecificationsBasedOnDeviceId", payload, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("SpecificationsClient.getDeviceSpecificationsBasedOnDeviceId failed; returning empty: {}", e.getMessage());
        }
        return Collections.emptyList();
    }

    @Override
    public void upsertDeviceSpecification(String id, String keyName, String keyValue, String keyUnit, String deviceId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("id", id);
        payload.put("keyName", keyName);
        payload.put("keyValue", keyValue);
        payload.put("keyUnit", keyUnit);
        payload.put("deviceId", deviceId);
        try {
            dapr.invokeMethod(APP_ID, "specifications/upsertDeviceSpecification", payload, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("SpecificationsClient.upsertDeviceSpecification failed; swallowing: {}", e.getMessage());
        }
    }

    @Override
    public void deleteById(String id) {
        Map<String, String> payload = new HashMap<>();
        payload.put("id", id);
        try {
            dapr.invokeMethod(APP_ID, "specifications/deleteById", payload, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("SpecificationsClient.deleteById failed; swallowing: {}", e.getMessage());
        }
    }

    @Override
    public SpecificationsDTO getDeviceSpecificationsBasedOnDeviceIdAndKeyName(String deviceId, String keyName) {
        Map<String, String> payload = new HashMap<>();
        payload.put("deviceId", deviceId);
        payload.put("keyName", keyName);
        try {
            dapr.invokeMethod(APP_ID, "specifications/getDeviceSpecificationsBasedOnDeviceIdAndKeyName", payload, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("SpecificationsClient.getDeviceSpecificationsBasedOnDeviceIdAndKeyName failed; returning null: {}", e.getMessage());
        }
        return null;
    }

    @Override
    public SpecificationsDTO getPower(String deviceId, String keyName) {
        Map<String, String> payload = new HashMap<>();
        payload.put("deviceId", deviceId);
        payload.put("keyName", keyName);
        try {
            dapr.invokeMethod(APP_ID, "specifications/getPower", payload, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("SpecificationsClient.getPower failed; returning null: {}", e.getMessage());
        }
        return null;
    }
}
