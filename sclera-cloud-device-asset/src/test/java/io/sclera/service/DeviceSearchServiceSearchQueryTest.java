package io.sclera.service;

import com.alibaba.fastjson.JSONObject;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Coverage for DeviceSearchService.generateSearchQuery - the pure full-text search fragment builder
 * across its all-columns, custom-column, and standard-column branches and the condition operators.
 */
class DeviceSearchServiceSearchQueryTest {

    private final DeviceSearchService service = new DeviceSearchService();

    @Test
    void generateSearchQuery_allColumns_containsBuildsConcatLike() {
        JSONObject search = new JSONObject();
        search.put("value", "printer");
        search.put("condition", "contains");
        // no "column" key -> all-columns path

        String q = service.generateSearchQuery(search, new JSONObject());

        assertThat(q).contains("AND (").contains("CONCAT_WS").contains("LIKE LOWER('%printer%')");
    }

    @Test
    void generateSearchQuery_customColumn_usesJsonbPath() {
        JSONObject search = new JSONObject();
        search.put("column", "AssetTag");
        search.put("custom", true);
        search.put("value", "abc");
        search.put("condition", "contains");

        String q = service.generateSearchQuery(search, new JSONObject());

        assertThat(q).contains("jsonb_path_query_first").contains("LIKE LOWER('%abc%')");
    }

    @Test
    void generateSearchQuery_standardColumn_equalTo() {
        JSONObject search = new JSONObject();
        search.put("column", "vendor");
        search.put("custom", false);
        search.put("value", "acme");
        search.put("condition", "equal_to");

        String q = service.generateSearchQuery(search, new JSONObject());

        // standard-column equal_to uses "= LOWER('acme')" (the ± markers are only in the all-columns path)
        assertThat(q).contains("AND (").contains("= LOWER('acme')");
    }
}
