package io.sclera.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.sclera.dto.ManagedSoftwareDTO;

import java.util.Set;

/** Service contract for {@link io.sclera.service.ManagedSoftwareSearchService}. */
public interface ManagedSoftwareSearchService {

    String updateSearchColumnName(String searchColumn);

    Set<ManagedSoftwareDTO> searchSortFilterManagedSoftware(String username, String vdmsId, String dockerName,
                                                            String condition, Integer pageNo, Integer pageSize,
                                                            JSONObject search_sort_filter_details);

    String generateSearchAndFilterCustomQuery(JSONObject searchAndFilterDetails);

    String generateFilterQuery(JSONArray filterDetails);

    String generateSearchQuery(JSONObject searchDetails);

    String generateGroupByAndSortCustomQuery(JSONObject sortDetails);

    String searchSortFilterManagedSoftwareCount(String username, String vdmsId, String dockerName,
                                                String condition, JSONObject search_sort_filter_details);
}
