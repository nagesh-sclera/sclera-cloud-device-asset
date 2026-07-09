package io.sclera.service;

import io.sclera.dto.InterfaceDTO;
import java.util.List;

/** Service contract for {@link io.sclera.service.InterfaceService}. */
public interface InterfaceService {
    Integer getInterfaceCountByDevice(String deviceId);

    List<InterfaceDTO> listDeviceSnmpInterfaceByDeviceId(String a, String b, String c, String d);

    void deleteInterfaceByDeviceId(String deviceId);

    void updateInterfaceDeviceId(String oldId, String newId);
}
