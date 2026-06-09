package io.sclera.interfaces;

import io.sclera.dto.DeviceTypesDTO;
import java.util.*;

/** Service contract for {@link io.sclera.service.DeviceTypeService}. */
public interface DeviceTypeServiceInterface {

    void upsertDeviceType(List<DeviceTypesDTO> deviceTypes);

    void syncAndUpsertDeviceTypes(String vdmsId);

    void batchUpdateDeviceTypes(Set<DeviceTypesDTO> deviceTypesDTOS);

    List<DeviceTypesDTO> getAllDeviceTypes();
}
