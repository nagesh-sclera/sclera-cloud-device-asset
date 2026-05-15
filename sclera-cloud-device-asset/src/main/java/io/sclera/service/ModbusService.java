package io.sclera.service;

import io.sclera.dto.ConditionsDTO;
import io.sclera.dto.ModbusRegisterDTO;
import io.sclera.dto.touchscreen.SensorDTO;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/** STUB: replace with remote call to AP-C2 */
@Service
public class ModbusService {
    public Set<ModbusRegisterDTO> getDeviceModbusRegisters(String a, String b, String c, String d) { return Collections.emptySet(); }
    public List<SensorDTO> getModbusRegistersByDeviceId(String deviceId) { return Collections.emptyList(); }
    public List<ConditionsDTO> listModbusDevicesAlertMessagesByDeviceIds(List<String> ids) { return Collections.emptyList(); }
    public String getDeviceIdByModbusRegisterId(String modbusRegisterId) { return null; }
    public Boolean getModbusRegisterAlertStatusByDeviceId(String deviceId) { return Boolean.FALSE; }
    public Integer getModbusRegistersCountByDeviceId(String deviceId) { return 0; }
    public void updateModbusRegisterDeviceId(String oldId, String newId, Set<String> ids) {}
}
