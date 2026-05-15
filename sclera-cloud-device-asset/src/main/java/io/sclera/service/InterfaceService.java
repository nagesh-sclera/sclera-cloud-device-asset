package io.sclera.service;

import io.sclera.dto.InterfaceDTO;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.List;

/** STUB: replace with remote call to AP-C3 */
@Service
public class InterfaceService {
    public Integer getInterfaceCountByDevice(String deviceId) { return 0; }
    public List<InterfaceDTO> listDeviceSnmpInterfaceByDeviceId(String a, String b, String c, String d) { return Collections.emptyList(); }
    public void deleteInterfaceByDeviceId(String deviceId) {}
    public void updateInterfaceDeviceId(String oldId, String newId) {}
}
