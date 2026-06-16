package io.sclera.Repository;

import io.sclera.dto.DeviceDTO;

import java.util.Set;

/**
 * Custom-fragment methods for DeviceRepository whose multi-join DeviceDTO projection could not
 * be expressed in JPQL; implemented with JPA Criteria in {@link DeviceRepositoryImpl}.
 */
public interface DeviceRepositoryCustom {

    /** Page of network parent devices scoped by dockers/types/virtual-type, with a free-text search. */
    Set<DeviceDTO> getNetworkParentDeviceByPagination(Set<String> dockernames, Set<String> types,
                                                      String searchKey, Integer pagesize, Integer offset,
                                                      Set<String> virtual_device_types);

    /** Page of all parent devices matching the search key. */
    Set<DeviceDTO> getAllParentDeviceByPagination(String searchKey, Integer pagesize, Integer offset);
}
