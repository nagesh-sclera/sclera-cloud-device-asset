package io.sclera.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import io.sclera.dto.DeviceDTO;

/** Service contract for {@link io.sclera.service.DeviceSearchService}. */
public interface DeviceSearchService {

    Set<DeviceDTO> searchDevices(String username, String vdmsid, String dockername, String condition, Integer pageNo, Integer pageSize, Map<String, Object> search_details);

    Set<DeviceDTO> sortDevices(String username, String vdmsid, String dockername, String condition, Integer pageno, Integer pagesize, Map<String, Object> sort_details);

    Set<DeviceDTO> filterDevices(String username, String vdmsid, String dockername, String condition, Integer pageno, Integer pagesize, List<Map<String, Object>> filter_details);

    String generateMultiConditionStmt(List<Map<String, Object>> filter_details, String vdms_id, String dockername);

    String updateDeviceSearchColumnName(String searchColumn);

    void getFuzzyValueByDeviceAndSearchString(DeviceDTO device, String search_string);

    Integer getFuzzyValueByBaseStringAndSearchString(Object base_string, String search_string);

    List<DeviceDTO> sortFilteredDevicesByMatchedScore(List<DeviceDTO> devices);

    Set<DeviceDTO> updateFuzzyMatchScore(Set<DeviceDTO> filteredDevices, Map<String, Object> search_details);

    List<DeviceDTO> getDeviceInfoByCustomFields(String username, String vdmsid, String dockername, com.alibaba.fastjson.JSONObject custom_fields);

    Set<DeviceDTO> multipleKeywordSearchSortFilterDevices(String username, String vdmsid, String dockername, String condition, Integer pageno, Integer pagesize, com.alibaba.fastjson.JSONObject search_sort_filter_details, Integer onboard_status);

    String generateFilterCustomQuery(com.alibaba.fastjson.JSONObject filter_details, String vdms_id);

    String generateColumnFilterQuery(com.alibaba.fastjson.JSONArray column_details);

    String generateSearchQuery(com.alibaba.fastjson.JSONObject search_details, com.alibaba.fastjson.JSONObject searhSortFilterDetails);

    String generateSortQuery(com.alibaba.fastjson.JSONObject sort_details);

    Set<DeviceDTO> multipleKeywordSearchSortFilterDevicesForAssetExport(String username, String vdmsid, String dockername, String condition, com.alibaba.fastjson.JSONObject search_sort_filter_details, Integer onboard_status);

    String multipleKeywordSearchSortFilterDevicesCount(String username, String vdmsid, String dockername, String condition, com.alibaba.fastjson.JSONObject search_sort_filter_details, Integer onboard_status);
}
