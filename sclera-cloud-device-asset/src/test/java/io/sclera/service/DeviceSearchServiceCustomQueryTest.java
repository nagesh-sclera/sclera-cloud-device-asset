package io.sclera.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Coverage for the private orchestrator generateMultipleKeywordSearchAndFilterCustomQuery, which
 * stitches the filter fragment (generateFilterCustomQuery) and per-keyword search fragments
 * (generateSearchQuery) into a single WHERE clause. Pure (no collaborators) -> invoked via reflection.
 */
class DeviceSearchServiceCustomQueryTest {

    private final DeviceSearchService service = new DeviceSearchService();

    private String build(JSONObject details) {
        return (String) ReflectionTestUtils.invokeMethod(
                service, "generateMultipleKeywordSearchAndFilterCustomQuery", details, "v1");
    }

    @Test
    void combinesFilterAndSearchFragments() {
        JSONObject feature = new JSONObject();
        feature.put("name", "adc");
        feature.put("condition", "is_present");
        JSONArray featureDetails = new JSONArray();
        featureDetails.add(feature);
        JSONObject filterDetails = new JSONObject();
        filterDetails.put("feature_details", featureDetails);

        JSONObject search = new JSONObject();
        search.put("value", "printer");
        search.put("condition", "contains");
        JSONArray searchDetails = new JSONArray();
        searchDetails.add(search);

        JSONObject details = new JSONObject();
        details.put("filter_details", filterDetails);
        details.put("search_details", searchDetails);

        String q = build(details);

        assertThat(q).contains("d.source_type = 'adc'")  // from filter fragment
                .contains("LIKE LOWER('%printer%')");      // from search fragment
    }

    @Test
    void filterOnly_noSearch() {
        JSONObject filterDetails = new JSONObject();
        filterDetails.put("column_details", new JSONArray());
        JSONObject details = new JSONObject();
        details.put("filter_details", filterDetails);
        // no search_details key -> only the filter branch runs
        assertThat(build(details)).isEqualTo("");
    }

    @Test
    void emptyDetails_returnsEmpty() {
        assertThat(build(new JSONObject())).isEmpty();
    }
}
