package io.sclera.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Coverage for DeviceSearchService's pure SQL-fragment generators (no collaborators used):
 * generateSortQuery across its custom/ip/timestamp/default branches, and the column-filter
 * query builder for custom present/absent conditions.
 */
class DeviceSearchServiceQueryTest {

    private final DeviceSearchService service = new DeviceSearchService();

    private JSONObject sortDetails(String column, boolean custom) {
        JSONObject inner = new JSONObject();
        inner.put("column", column);
        inner.put("custom", custom);
        JSONObject outer = new JSONObject();
        outer.put("sort_details", inner);
        return outer;
    }

    @Test
    void generateSortQuery_noSortDetails_defaultsToUpdatedTimestamp() {
        String q = service.generateSortQuery(new JSONObject());
        assertThat(q).contains("ORDER BY (d.updated_timestamp IS NULL)")
                .contains("d.updated_timestamp DESC, d.id");
    }

    @Test
    void generateSortQuery_customColumn_usesJsonbPath() {
        String q = service.generateSortQuery(sortDetails("myField", true));
        assertThat(q).contains("ORDER BY").contains("jsonb_path_query_first(custom_fields::jsonb");
    }

    @Test
    void generateSortQuery_ipAddress_castsToInet() {
        String q = service.generateSortQuery(sortDetails("ip_address", false));
        assertThat(q).contains("ORDER BY").contains("::inet");
    }

    @Test
    void generateSortQuery_timestampColumn_appendsDescAndId() {
        String q = service.generateSortQuery(sortDetails("created_timestamp", false));
        assertThat(q).contains("ORDER BY").contains("DESC, d.id");
    }

    @Test
    void generateSortQuery_plainColumn_nullsLast() {
        String q = service.generateSortQuery(sortDetails("name", false));
        assertThat(q).contains("ORDER BY").contains("IS NULL),");
    }

    @Test
    void generateColumnFilterQuery_empty_returnsEmpty() {
        assertThat(service.generateColumnFilterQuery(new JSONArray())).isEmpty();
    }

    @Test
    void generateColumnFilterQuery_customIsPresent_buildsJsonbNotNull() {
        JSONObject col = new JSONObject();
        col.put("custom", true);
        col.put("condition", "is_present");
        col.put("column", "warranty");
        JSONArray arr = new JSONArray();
        arr.add(col);

        String q = service.generateColumnFilterQuery(arr);

        assertThat(q).contains("AND (").contains("jsonb_path_query_first").contains("IS NOT NULL");
    }

    @Test
    void generateColumnFilterQuery_customIsNotPresent_buildsJsonbNull() {
        JSONObject col = new JSONObject();
        col.put("custom", true);
        col.put("condition", "is_not_present");
        col.put("column", "warranty");
        JSONArray arr = new JSONArray();
        arr.add(col);

        String q = service.generateColumnFilterQuery(arr);

        assertThat(q).contains("jsonb_path_query_first").contains("IS NULL OR");
    }

    @Test
    void generateFilterCustomQuery_columnDetailsOnly_delegatesToColumnFilter() {
        JSONObject col = new JSONObject();
        col.put("custom", true);
        col.put("condition", "is_present");
        col.put("column", "warranty");
        JSONArray columnDetails = new JSONArray();
        columnDetails.add(col);
        JSONObject filterDetails = new JSONObject();
        filterDetails.put("column_details", columnDetails);

        String q = service.generateFilterCustomQuery(filterDetails, "v1");

        assertThat(q).contains("jsonb_path_query_first");
    }
}
