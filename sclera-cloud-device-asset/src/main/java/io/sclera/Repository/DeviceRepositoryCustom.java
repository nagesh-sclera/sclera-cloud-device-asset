package io.sclera.Repository;

import com.alibaba.fastjson.JSONArray;
import io.sclera.dto.DeviceDTO;

import java.util.List;
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

    /** All IP/standard parent devices (virtual_device_type null/0/1). */
    List<DeviceDTO> listAlldevices();

    /** Network parent devices (non-archived) scoped by dockers/types/virtual-type + qr/nfc tags. */
    Set<DeviceDTO> getAllNetworkParentDevices(JSONArray dockernames, JSONArray types, String searchkey,
                                              JSONArray virtual_device_types, Boolean isTaggedToQrCode,
                                              JSONArray deviceIdsTaggedToQrCode, Boolean isTaggedToNfc,
                                              JSONArray deviceIdsTaggedToNfc);

    /** Paginated network parent devices (non-archived) scoped by dockers/types/virtual-type + qr/nfc/barcode tags. */
    Set<DeviceDTO> getAllNetworkParentDeviceByPagination(JSONArray dockernames, JSONArray types, String searchkey,
                                                         Integer pagesize, Integer offset, JSONArray virtual_device_types,
                                                         Boolean isTaggedToQrCode, JSONArray deviceIdsTaggedToQrCode,
                                                         Boolean isTaggedToNfc, JSONArray deviceIdsTaggedToNfc,
                                                         Boolean isTaggedToBarCode, JSONArray deviceIdsTaggedToClientBarCode);
}
