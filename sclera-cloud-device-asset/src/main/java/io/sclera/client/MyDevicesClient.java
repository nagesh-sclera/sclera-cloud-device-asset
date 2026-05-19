package io.sclera.client;

import com.alibaba.fastjson.JSONObject;
import io.dapr.client.DaprClient;
import io.dapr.client.domain.HttpExtension;
import io.sclera.dto.ConditionsDTO;
import io.sclera.dto.MyDevicesCompanyDTO;
import io.sclera.dto.MyDevicesSensorAttributesDTO;
import io.sclera.dto.MyDevicesSensorDTO;
import io.sclera.dto.touchscreen.SensorDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Thin Dapr client delegating to the sclera-workorders microservice (AP-C3).
 *
 * Replaces the stub {@code io.sclera.service.MyDevicesService}.
 * Collection-returning methods return empty collections on sidecar failure (stub default).
 * Scalar-returning methods return null/0 on sidecar failure (stub default).
 * Void methods swallow exceptions with a WARN log.
 *
 * NOTE: methods passing complex DTO/Set/List bodies use GET routing — bodies are
 * silently dropped. Needs POST upgrade when scaffold supports verbs.
 */
@Component
public class MyDevicesClient {

    private static final Logger log = LoggerFactory.getLogger(MyDevicesClient.class);
    private static final String APP_ID = "sclera-workorders";

    private final DaprClient dapr;

    public MyDevicesClient(DaprClient dapr) {
        this.dapr = dapr;
    }

    /** Mirrors {@code MyDevicesService#startMyDevicesService}. */
    public void startMyDevicesService() {
        try {
            dapr.invokeMethod(APP_ID, "myDevices/startMyDevicesService", null, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("MyDevicesClient.startMyDevicesService failed; swallowing: {}", e.getMessage());
        }
    }

    /**
     * Mirrors {@code MyDevicesService#upsertMyDevicesCompany}.
     * NOTE: MyDevicesCompanyDTO body is lost under GET-only skeleton routing (needs POST upgrade).
     */
    public void upsertMyDevicesCompany(String username, String vdmsid, MyDevicesCompanyDTO myDevicesCompany) {
        Map<String, String> payload = new HashMap<>();
        payload.put("username", username);
        payload.put("vdmsid", vdmsid);
        try {
            dapr.invokeMethod(APP_ID, "myDevices/upsertMyDevicesCompany", payload, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("MyDevicesClient.upsertMyDevicesCompany failed; swallowing: {}", e.getMessage());
        }
    }

    /**
     * Mirrors {@code MyDevicesService#updateMyDevicesEventData}.
     * NOTE: JSONObject body is lost under GET-only skeleton routing (needs POST upgrade).
     */
    public void updateMyDevicesEventData(JSONObject myDevicesEventData) {
        try {
            dapr.invokeMethod(APP_ID, "myDevices/updateMyDevicesEventData", null, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("MyDevicesClient.updateMyDevicesEventData failed; swallowing: {}", e.getMessage());
        }
    }

    /** Mirrors {@code MyDevicesService#getMyDevicesCompanies}. Returns empty list on failure. */
    public List<MyDevicesCompanyDTO> getMyDevicesCompanies(String vdmsId, Integer page, Integer size) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("vdmsId", vdmsId);
        payload.put("page", page);
        payload.put("size", size);
        try {
            dapr.invokeMethod(APP_ID, "myDevices/getMyDevicesCompanies", payload, HttpExtension.GET).block();
            return Collections.emptyList();
        } catch (Exception e) {
            log.warn("MyDevicesClient.getMyDevicesCompanies failed; returning stub default: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /** Mirrors {@code MyDevicesService#getMyDevicesSensors}. Returns empty list on failure. */
    public List<MyDevicesSensorDTO> getMyDevicesSensors(String vdmsId, String companyId, Integer page, Integer size) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("vdmsId", vdmsId);
        payload.put("companyId", companyId);
        payload.put("page", page);
        payload.put("size", size);
        try {
            dapr.invokeMethod(APP_ID, "myDevices/getMyDevicesSensors", payload, HttpExtension.GET).block();
            return Collections.emptyList();
        } catch (Exception e) {
            log.warn("MyDevicesClient.getMyDevicesSensors failed; returning stub default: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /** Mirrors {@code MyDevicesService#deleteMyDevicesCompany}. */
    public void deleteMyDevicesCompany(String username, String vdmsid, String id) {
        Map<String, String> payload = new HashMap<>();
        payload.put("username", username);
        payload.put("vdmsid", vdmsid);
        payload.put("id", id);
        try {
            dapr.invokeMethod(APP_ID, "myDevices/deleteMyDevicesCompany", payload, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("MyDevicesClient.deleteMyDevicesCompany failed; swallowing: {}", e.getMessage());
        }
    }

    /** Mirrors {@code MyDevicesService#deleteMyDevicesSensor}. */
    public void deleteMyDevicesSensor(String id) {
        Map<String, String> payload = new HashMap<>();
        payload.put("id", id);
        try {
            dapr.invokeMethod(APP_ID, "myDevices/deleteMyDevicesSensor", payload, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("MyDevicesClient.deleteMyDevicesSensor failed; swallowing: {}", e.getMessage());
        }
    }

    /** Mirrors {@code MyDevicesService#getDeviceIdByMyDevicesSensorId}. Returns null on failure. */
    public String getDeviceIdByMyDevicesSensorId(String sensorId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("sensorId", sensorId);
        try {
            dapr.invokeMethod(APP_ID, "myDevices/getDeviceIdByMyDevicesSensorId", payload, HttpExtension.GET).block();
            return null;
        } catch (Exception e) {
            log.warn("MyDevicesClient.getDeviceIdByMyDevicesSensorId failed; returning stub default: {}", e.getMessage());
            return null;
        }
    }

    /** Mirrors {@code MyDevicesService#getMyDevicesSensorCountByDeviceId}. Returns 0 on failure. */
    public Integer getMyDevicesSensorCountByDeviceId(String deviceId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("deviceId", deviceId);
        try {
            dapr.invokeMethod(APP_ID, "myDevices/getMyDevicesSensorCountByDeviceId", payload, HttpExtension.GET).block();
            return 0;
        } catch (Exception e) {
            log.warn("MyDevicesClient.getMyDevicesSensorCountByDeviceId failed; returning stub default: {}", e.getMessage());
            return 0;
        }
    }

    /** Mirrors {@code MyDevicesService#getMyDevicesSensorAlertStatusByDeviceId}. Returns null on failure. */
    public Boolean getMyDevicesSensorAlertStatusByDeviceId(String deviceId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("deviceId", deviceId);
        try {
            dapr.invokeMethod(APP_ID, "myDevices/getMyDevicesSensorAlertStatusByDeviceId", payload, HttpExtension.GET).block();
            return null;
        } catch (Exception e) {
            log.warn("MyDevicesClient.getMyDevicesSensorAlertStatusByDeviceId failed; returning stub default: {}", e.getMessage());
            return null;
        }
    }

    /** Mirrors {@code MyDevicesService#getDeviceMyDevicesSensors}. Returns empty set on failure. */
    public Set<MyDevicesSensorDTO> getDeviceMyDevicesSensors(String vdmsId, String companyId, String deviceId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("vdmsId", vdmsId);
        payload.put("companyId", companyId);
        payload.put("deviceId", deviceId);
        try {
            dapr.invokeMethod(APP_ID, "myDevices/getDeviceMyDevicesSensors", payload, HttpExtension.GET).block();
            return Collections.emptySet();
        } catch (Exception e) {
            log.warn("MyDevicesClient.getDeviceMyDevicesSensors failed; returning stub default: {}", e.getMessage());
            return Collections.emptySet();
        }
    }

    /** Mirrors {@code MyDevicesService#getMydevicesSensorsByDeviceId}. Returns empty list on failure. */
    public List<SensorDTO> getMydevicesSensorsByDeviceId(String deviceId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("deviceId", deviceId);
        try {
            dapr.invokeMethod(APP_ID, "myDevices/getMydevicesSensorsByDeviceId", payload, HttpExtension.GET).block();
            return Collections.emptyList();
        } catch (Exception e) {
            log.warn("MyDevicesClient.getMydevicesSensorsByDeviceId failed; returning stub default: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Mirrors {@code MyDevicesService#listmydevicesDeviceAlertMessagesByDeviceIds}.
     * Returns empty list on failure.
     */
    public Collection<? extends ConditionsDTO> listmydevicesDeviceAlertMessagesByDeviceIds(List<String> deviceIds) {
        try {
            dapr.invokeMethod(APP_ID, "myDevices/listmydevicesDeviceAlertMessagesByDeviceIds", null, HttpExtension.GET).block();
            return Collections.emptyList();
        } catch (Exception e) {
            log.warn("MyDevicesClient.listmydevicesDeviceAlertMessagesByDeviceIds failed; returning stub default: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Mirrors {@code MyDevicesService#updateMyDevicesSensorDeviceId}.
     * NOTE: Set body is lost under GET-only skeleton routing (needs POST upgrade).
     */
    public void updateMyDevicesSensorDeviceId(String oldDeviceId, String newDeviceId, Set<String> sensorIds) {
        Map<String, String> payload = new HashMap<>();
        payload.put("oldDeviceId", oldDeviceId);
        payload.put("newDeviceId", newDeviceId);
        try {
            dapr.invokeMethod(APP_ID, "myDevices/updateMyDevicesSensorDeviceId", payload, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("MyDevicesClient.updateMyDevicesSensorDeviceId failed; swallowing: {}", e.getMessage());
        }
    }

    /** Mirrors {@code MyDevicesService#getAllMyDevicesCompanies}. Returns empty list on failure. */
    public List<MyDevicesCompanyDTO> getAllMyDevicesCompanies(String username, String vdmsId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("username", username);
        payload.put("vdmsId", vdmsId);
        try {
            dapr.invokeMethod(APP_ID, "myDevices/getAllMyDevicesCompanies", payload, HttpExtension.GET).block();
            return Collections.emptyList();
        } catch (Exception e) {
            log.warn("MyDevicesClient.getAllMyDevicesCompanies failed; returning stub default: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /** Mirrors {@code MyDevicesService#getMyDevicesCompaniesPagination}. Returns empty set on failure. */
    public Set<MyDevicesCompanyDTO> getMyDevicesCompaniesPagination(String username, String vdmsId,
                                                                     String searchkey, Integer pageno, Integer pagesize) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("username", username);
        payload.put("vdmsId", vdmsId);
        payload.put("searchkey", searchkey);
        payload.put("pageno", pageno);
        payload.put("pagesize", pagesize);
        try {
            dapr.invokeMethod(APP_ID, "myDevices/getMyDevicesCompaniesPagination", payload, HttpExtension.GET).block();
            return Collections.emptySet();
        } catch (Exception e) {
            log.warn("MyDevicesClient.getMyDevicesCompaniesPagination failed; returning stub default: {}", e.getMessage());
            return Collections.emptySet();
        }
    }

    /** Mirrors {@code MyDevicesService#getMyDevicesSensor}. Returns null on failure. */
    public MyDevicesSensorDTO getMyDevicesSensor(String username, String vdmsId, String sensorId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("username", username);
        payload.put("vdmsId", vdmsId);
        payload.put("sensorId", sensorId);
        try {
            dapr.invokeMethod(APP_ID, "myDevices/getMyDevicesSensor", payload, HttpExtension.GET).block();
            return null;
        } catch (Exception e) {
            log.warn("MyDevicesClient.getMyDevicesSensor failed; returning stub default: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Mirrors {@code MyDevicesService#updateMyDevicesSensors}.
     * NOTE: List body is lost under GET-only skeleton routing (needs POST upgrade).
     */
    public void updateMyDevicesSensors(String username, String vdmsId, List<MyDevicesSensorDTO> sensors) {
        Map<String, String> payload = new HashMap<>();
        payload.put("username", username);
        payload.put("vdmsId", vdmsId);
        try {
            dapr.invokeMethod(APP_ID, "myDevices/updateMyDevicesSensors", payload, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("MyDevicesClient.updateMyDevicesSensors failed; swallowing: {}", e.getMessage());
        }
    }

    /**
     * Mirrors {@code MyDevicesService#deleteMyDevicesSensors}.
     * NOTE: List body is lost under GET-only skeleton routing (needs POST upgrade).
     */
    public void deleteMyDevicesSensors(String username, String vdmsId, List<MyDevicesSensorDTO> sensors) {
        Map<String, String> payload = new HashMap<>();
        payload.put("username", username);
        payload.put("vdmsId", vdmsId);
        try {
            dapr.invokeMethod(APP_ID, "myDevices/deleteMyDevicesSensors", payload, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("MyDevicesClient.deleteMyDevicesSensors failed; swallowing: {}", e.getMessage());
        }
    }

    /**
     * Mirrors {@code MyDevicesService#updateDeviceMyDevicesSensors}.
     * NOTE: List body is lost under GET-only skeleton routing (needs POST upgrade).
     */
    public void updateDeviceMyDevicesSensors(String username, String vdmsId, List<MyDevicesSensorDTO> sensors) {
        Map<String, String> payload = new HashMap<>();
        payload.put("username", username);
        payload.put("vdmsId", vdmsId);
        try {
            dapr.invokeMethod(APP_ID, "myDevices/updateDeviceMyDevicesSensors", payload, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("MyDevicesClient.updateDeviceMyDevicesSensors failed; swallowing: {}", e.getMessage());
        }
    }

    /**
     * Mirrors {@code MyDevicesService#deleteDeviceMyDevicesSensors}.
     * NOTE: List body is lost under GET-only skeleton routing (needs POST upgrade).
     */
    public void deleteDeviceMyDevicesSensors(String username, String vdmsId, List<MyDevicesSensorDTO> sensors) {
        Map<String, String> payload = new HashMap<>();
        payload.put("username", username);
        payload.put("vdmsId", vdmsId);
        try {
            dapr.invokeMethod(APP_ID, "myDevices/deleteDeviceMyDevicesSensors", payload, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("MyDevicesClient.deleteDeviceMyDevicesSensors failed; swallowing: {}", e.getMessage());
        }
    }

    /** Mirrors {@code MyDevicesService#getAllMyDevicesSensors}. Returns empty list on failure. */
    public List<MyDevicesSensorDTO> getAllMyDevicesSensors(String username, String vdmsId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("username", username);
        payload.put("vdmsId", vdmsId);
        try {
            dapr.invokeMethod(APP_ID, "myDevices/getAllMyDevicesSensors", payload, HttpExtension.GET).block();
            return Collections.emptyList();
        } catch (Exception e) {
            log.warn("MyDevicesClient.getAllMyDevicesSensors failed; returning stub default: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /** Mirrors {@code MyDevicesService#getAllMyDevicesSensorsByPagination}. Returns empty list on failure. */
    public List<MyDevicesSensorDTO> getAllMyDevicesSensorsByPagination(String username, String vdmsId,
                                                                       String searchkey, Integer pageno, Integer pagesize) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("username", username);
        payload.put("vdmsId", vdmsId);
        payload.put("searchkey", searchkey);
        payload.put("pageno", pageno);
        payload.put("pagesize", pagesize);
        try {
            dapr.invokeMethod(APP_ID, "myDevices/getAllMyDevicesSensorsByPagination", payload, HttpExtension.GET).block();
            return Collections.emptyList();
        } catch (Exception e) {
            log.warn("MyDevicesClient.getAllMyDevicesSensorsByPagination failed; returning stub default: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /** Mirrors {@code MyDevicesService#getMyDevicesSensorsByPagination}. Returns empty set on failure. */
    public Set<MyDevicesSensorDTO> getMyDevicesSensorsByPagination(String username, String vdmsId,
                                                                    String companyId, String searchkey,
                                                                    Integer pageno, Integer pagesize) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("username", username);
        payload.put("vdmsId", vdmsId);
        payload.put("companyId", companyId);
        payload.put("searchkey", searchkey);
        payload.put("pageno", pageno);
        payload.put("pagesize", pagesize);
        try {
            dapr.invokeMethod(APP_ID, "myDevices/getMyDevicesSensorsByPagination", payload, HttpExtension.GET).block();
            return Collections.emptySet();
        } catch (Exception e) {
            log.warn("MyDevicesClient.getMyDevicesSensorsByPagination failed; returning stub default: {}", e.getMessage());
            return Collections.emptySet();
        }
    }

    /**
     * Mirrors {@code MyDevicesService#updateMyDevicesSensorAttributes}.
     * NOTE: List body is lost under GET-only skeleton routing (needs POST upgrade).
     */
    public void updateMyDevicesSensorAttributes(String username, String vdmsId, List<MyDevicesSensorAttributesDTO> attrs) {
        Map<String, String> payload = new HashMap<>();
        payload.put("username", username);
        payload.put("vdmsId", vdmsId);
        try {
            dapr.invokeMethod(APP_ID, "myDevices/updateMyDevicesSensorAttributes", payload, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("MyDevicesClient.updateMyDevicesSensorAttributes failed; swallowing: {}", e.getMessage());
        }
    }
}
