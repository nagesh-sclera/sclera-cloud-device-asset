package io.sclera.service;

import io.sclera.dto.MqttDeviceDTO;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.Set;

/** STUB: replace with remote call to AP-C2 */
@Service
public class MqttService {
    public Set<MqttDeviceDTO> getAllMqttDevices(String a, String b, String c, String d) { return Collections.emptySet(); }
    public Integer getMqttDeviceCountByDeviceId(String deviceId) { return 0; }
}
