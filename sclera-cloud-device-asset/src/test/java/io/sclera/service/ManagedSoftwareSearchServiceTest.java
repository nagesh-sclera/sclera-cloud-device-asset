package io.sclera.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.sclera.Repository.ManagedSoftwareRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit coverage for the pure query-string generation methods of ManagedSoftwareSearchService:
 * column mapping, filter/search/sort clause builders. The JdbcTemplate-backed
 * searchSortFilter methods are deferred.
 */
@ExtendWith(MockitoExtension.class)
class ManagedSoftwareSearchServiceTest {

    @Mock JdbcTemplate jdbcTemplate;
    @Mock ManagedSoftwareRepository managedSoftwareRepository;

    @InjectMocks ManagedSoftwareSearchService service;

    // ---- updateSearchColumnName ------------------------------------------

    @Test
    void updateSearchColumnName_mapsKnownColumns() {
        assertThat(service.updateSearchColumnName("currency")).isEqualTo("ms.currency");
        assertThat(service.updateSearchColumnName("status")).isEqualTo("ms.status");
        assertThat(service.updateSearchColumnName("vendor")).isEqualTo("ms.vendor");
        assertThat(service.updateSearchColumnName("email")).isEqualTo("ds.email");
        assertThat(service.updateSearchColumnName("os_type")).isEqualTo("ds.os_type");
    }

    @Test
    void updateSearchColumnName_name_returnsConcat() {
        assertThat(service.updateSearchColumnName("name")).isEqualTo("CONCAT(ms.name, ms.application_name)");
    }

    @Test
    void updateSearchColumnName_unknown_returnsDefaultName() {
        assertThat(service.updateSearchColumnName("totally_unknown")).isEqualTo("ms.name");
    }

    // ---- generateFilterQuery ---------------------------------------------

    @Test
    void generateFilterQuery_empty_returnsEmptyString() {
        assertThat(service.generateFilterQuery(new JSONArray())).isEmpty();
    }

    @Test
    void generateFilterQuery_withValue_buildsEquality() {
        JSONObject filter = new JSONObject();
        filter.put("column", "status");
        filter.put("value", "active");
        JSONArray filters = new JSONArray();
        filters.add(filter);

        assertThat(service.generateFilterQuery(filters)).contains("ms.status = 'active'");
    }

    @Test
    void generateFilterQuery_nullValue_buildsNotNullAndNotEmpty() {
        JSONObject filter = new JSONObject();
        filter.put("column", "vendor");
        JSONArray filters = new JSONArray();
        filters.add(filter);

        assertThat(service.generateFilterQuery(filters))
                .contains("ms.vendor IS NOT NULL").contains("ms.vendor <> ''");
    }

    // ---- generateSearchQuery ---------------------------------------------

    @Test
    void generateSearchQuery_nullTerm_returnsEmpty() {
        JSONObject search = new JSONObject();
        search.put("column", "name");
        assertThat(service.generateSearchQuery(search)).isEmpty();
    }

    @Test
    void generateSearchQuery_withTerm_buildsLikeClause() {
        JSONObject search = new JSONObject();
        search.put("column", "name");
        search.put("value", "acme");

        assertThat(service.generateSearchQuery(search)).contains("LIKE LOWER('%acme%')");
    }

    // ---- generateGroupByAndSortCustomQuery -------------------------------

    @Test
    void generateGroupByAndSortCustomQuery_noSort_groupsAndOrdersById() {
        assertThat(service.generateGroupByAndSortCustomQuery(new JSONObject()))
                .contains("GROUP BY ms.id").contains("ORDER BY ms.id");
    }

    @Test
    void generateGroupByAndSortCustomQuery_withSort_appendsOrderByColumn() {
        JSONObject sortInner = new JSONObject();
        sortInner.put("column", "status");
        JSONObject sortDetails = new JSONObject();
        sortDetails.put("sort_details", sortInner);

        assertThat(service.generateGroupByAndSortCustomQuery(sortDetails))
                .contains("ORDER BY").contains("ms.status");
    }

    // ---- generateSearchAndFilterCustomQuery ------------------------------

    @Test
    void generateSearchAndFilterCustomQuery_combinesFilter() {
        JSONObject filter = new JSONObject();
        filter.put("column", "status");
        filter.put("value", "active");
        JSONArray filters = new JSONArray();
        filters.add(filter);
        JSONObject details = new JSONObject();
        details.put("filter_details", filters);

        assertThat(service.generateSearchAndFilterCustomQuery(details)).contains("ms.status = 'active'");
    }
}
