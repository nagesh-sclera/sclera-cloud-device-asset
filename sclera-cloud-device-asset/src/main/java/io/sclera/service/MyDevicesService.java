package io.sclera.service;

import com.alibaba.fastjson.JSONObject;
import io.sclera.dto.ConditionsDTO;
import io.sclera.dto.MyDevicesCompanyDTO;
import io.sclera.dto.MyDevicesSensorAttributesDTO;
import io.sclera.dto.MyDevicesSensorDTO;
import io.sclera.dto.touchscreen.SensorDTO;
import io.sclera.utils.StubLog;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/** STUB: edge-only MyDevices service - replace with remote call */
@Service
public class MyDevicesService {

    public void startMyDevicesService() {}
    public void upsertMyDevicesCompany(String username, String vdmsid, MyDevicesCompanyDTO myDevicesCompany) {}
    public void updateMyDevicesEventData(JSONObject myDevicesEventData) {}
    public List<MyDevicesCompanyDTO> getMyDevicesCompanies(String vdmsId, Integer page, Integer size) { return Collections.emptyList(); }
    public List<MyDevicesSensorDTO> getMyDevicesSensors(String vdmsId, String companyId, Integer page, Integer size) { return Collections.emptyList(); }
    public void deleteMyDevicesCompany(String username, String vdmsid, String id) {
        StubLog.warn("MyDevicesService", "deleteMyDevicesCompany", "AP-C3");
    }
    public void deleteMyDevicesSensor(String id) {}

    public String getDeviceIdByMyDevicesSensorId(String sensorId) {
        StubLog.warn("MyDevicesService", "getDeviceIdByMyDevicesSensorId", "AP-C3");
        return null;
    }
    public Integer getMyDevicesSensorCountByDeviceId(String deviceId) {
        StubLog.warn("MyDevicesService", "getMyDevicesSensorCountByDeviceId", "AP-C3");
        return 0;
    }
    public Boolean getMyDevicesSensorAlertStatusByDeviceId(String deviceId) {
        StubLog.warn("MyDevicesService", "getMyDevicesSensorAlertStatusByDeviceId", "AP-C3");
        return null;
    }
    public Set<MyDevicesSensorDTO> getDeviceMyDevicesSensors(String vdmsId, String companyId, String deviceId) {
        StubLog.warn("MyDevicesService", "getDeviceMyDevicesSensors", "AP-C3");
        return Collections.emptySet();
    }
    public List<SensorDTO> getMydevicesSensorsByDeviceId(String deviceId) {
        StubLog.warn("MyDevicesService", "getMydevicesSensorsByDeviceId", "AP-C3");
        return Collections.emptyList();
    }
    public Collection<? extends ConditionsDTO> listmydevicesDeviceAlertMessagesByDeviceIds(List<String> deviceIds) {
        StubLog.warn("MyDevicesService", "listmydevicesDeviceAlertMessagesByDeviceIds", "AP-C3");
        return Collections.emptyList();
    }
    public void updateMyDevicesSensorDeviceId(String oldDeviceId, String newDeviceId, Set<String> sensorIds) {
        StubLog.warn("MyDevicesService", "updateMyDevicesSensorDeviceId", "AP-C3");
    }

    // Missing methods called from MyDevicesController
    public List<MyDevicesCompanyDTO> getAllMyDevicesCompanies(String username, String vdmsId) {
        StubLog.warn("MyDevicesService", "getAllMyDevicesCompanies", "AP-C3");
        return Collections.emptyList();
    }
    public Set<MyDevicesCompanyDTO> getMyDevicesCompaniesPagination(String username, String vdmsId, String searchkey, Integer pageno, Integer pagesize) {
        StubLog.warn("MyDevicesService", "getMyDevicesCompaniesPagination", "AP-C3");
        return Collections.emptySet();
    }
    public MyDevicesSensorDTO getMyDevicesSensor(String username, String vdmsId, String sensorId) {
        StubLog.warn("MyDevicesService", "getMyDevicesSensor", "AP-C3");
        return null;
    }
    public void updateMyDevicesSensors(String username, String vdmsId, List<MyDevicesSensorDTO> sensors) {
        StubLog.warn("MyDevicesService", "updateMyDevicesSensors", "AP-C3");
    }
    public void deleteMyDevicesSensors(String username, String vdmsId, List<MyDevicesSensorDTO> sensors) {
        StubLog.warn("MyDevicesService", "deleteMyDevicesSensors", "AP-C3");
    }
    public void updateDeviceMyDevicesSensors(String username, String vdmsId, List<MyDevicesSensorDTO> sensors) {
        StubLog.warn("MyDevicesService", "updateDeviceMyDevicesSensors", "AP-C3");
    }
    public void deleteDeviceMyDevicesSensors(String username, String vdmsId, List<MyDevicesSensorDTO> sensors) {
        StubLog.warn("MyDevicesService", "deleteDeviceMyDevicesSensors", "AP-C3");
    }
    public List<MyDevicesSensorDTO> getAllMyDevicesSensors(String username, String vdmsId) {
        StubLog.warn("MyDevicesService", "getAllMyDevicesSensors", "AP-C3");
        return Collections.emptyList();
    }
    public List<MyDevicesSensorDTO> getAllMyDevicesSensorsByPagination(String username, String vdmsId, String searchkey, Integer pageno, Integer pagesize) {
        StubLog.warn("MyDevicesService", "getAllMyDevicesSensorsByPagination", "AP-C3");
        return Collections.emptyList();
    }
    public Set<MyDevicesSensorDTO> getMyDevicesSensorsByPagination(String username, String vdmsId, String companyId, String searchkey, Integer pageno, Integer pagesize) {
        StubLog.warn("MyDevicesService", "getMyDevicesSensorsByPagination", "AP-C3");
        return Collections.emptySet();
    }
    public void updateMyDevicesSensorAttributes(String username, String vdmsId, List<MyDevicesSensorAttributesDTO> attrs) {
        StubLog.warn("MyDevicesService", "updateMyDevicesSensorAttributes", "AP-C3");
    }
}
