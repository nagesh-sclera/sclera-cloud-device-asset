package io.sclera.it;

import com.alibaba.fastjson.JSONObject;
import io.sclera.dto.DeviceSearchCriteria;
import io.sclera.queryrepository.DeviceSearchQueryBuilder;
import io.sclera.queryrepository.DeviceSearchQueryBuilder.SplitFilter;
import io.sclera.queryrepository.DeviceSearchQueryBuilder.SplitSearch;
import io.sclera.queryrepository.DeviceSearchQueryBuilder.SplitSort;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Result-asserting integration test for the split search/sort/filter family added to
 * DeviceSearchQueryBuilder (searchDeviceIds / sortDeviceIds / filterDeviceIds /
 * customFieldDeviceIds) — the JPA Criteria replacement for the older split
 * DeviceSearchService.searchDevices / sortDevices / filterDevices / getDeviceInfoByCustomFields
 * string SQL. Driven against the committed device-search fixture on a PostgreSQL 16 Testcontainer.
 *
 * Note: the split search is contains-only and CASE-SENSITIVE (no LOWER/strip), faithful to the
 * legacy SQL — distinct from the merged search-all which lowercases + strips.
 */
@Sql(scripts = "/schema-pg.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = "/seed/device-search-it.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/cleanup-device-search-it.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@Transactional
class DeviceSplitSearchQueryBuilderIT extends PostgresJpaIT {

    @PersistenceContext
    EntityManager em;

    DeviceSearchQueryBuilder qb;

    @BeforeEach
    void init() {
        qb = new DeviceSearchQueryBuilder(em);
    }

    private DeviceSearchCriteria scope(String condition) {
        return DeviceSearchCriteria.from("v1", "dock1", condition, new JSONObject(), 123);
    }

    // ---------------------------------------------------------------- search (contains)

    @Test
    void searchAll_standardConcatHaystack() {
        // 'My-Alpha' (dsx1 user_data_name) lives in the concat haystack; case-sensitive match.
        assertThat(qb.searchDeviceIds(scope("all"), new SplitSearch(null, false, "Alpha"), 1, 50))
                .containsExactly("dsx1");
    }

    @Test
    void searchAll_customFieldsArm() {
        // 'Engineering' lives only in dsx1's custom_fields -> proves the custom-array OR arm.
        assertThat(qb.searchDeviceIds(scope("all"), new SplitSearch(null, false, "Engineering"), 1, 50))
                .containsExactly("dsx1");
    }

    @Test
    void searchAll_scopeExcludesArchivedAndForeign() {
        // 'Device' is in the display names of dsx2/dsx3/dsx5; dsx4 (archived) and dsx9 (v2) excluded.
        assertThat(qb.searchDeviceIds(scope("all"), new SplitSearch(null, false, "Device"), 1, 50))
                .containsExactlyInAnyOrder("dsx2", "dsx3", "dsx5");
    }

    @Test
    void searchColumn_standard_vendor() {
        assertThat(qb.searchDeviceIds(scope("all"), new SplitSearch("vendor", false, "Cisco"), 1, 50))
                .containsExactly("dsx1");
    }

    @Test
    void searchColumn_custom_department() {
        assertThat(qb.searchDeviceIds(scope("all"), new SplitSearch("department", true, "Finance"), 1, 50))
                .containsExactly("dsx2");
    }

    // ---------------------------------------------------------------- sort

    @Test
    void sortStandard_displayNameUserDataFallback() {
        // Beta, Epsilon, Gamma, My-Alpha -> B,E,G,M
        assertThat(qb.sortDeviceIds(scope("all"), new SplitSort("display_name", false), 1, 50))
                .containsExactly("dsx2", "dsx5", "dsx3", "dsx1");
    }

    @Test
    void sortStandard_ipAddressInetOrder() {
        // inet order 9.1.1.1 < 10.0.0.2 < 10.0.0.10 ; dsx3 (null ip) last.
        assertThat(qb.sortDeviceIds(scope("all"), new SplitSort("ip_address", false), 1, 50))
                .hasSize(4).startsWith("dsx5", "dsx1", "dsx2");
    }

    @Test
    void sortCustom_departmentJsonPath() {
        // Engineering(dsx1) < Finance(dsx2) ; dsx3/dsx5 (no department) last.
        assertThat(qb.sortDeviceIds(scope("all"), new SplitSort("department", true), 1, 50))
                .hasSize(4).startsWith("dsx1", "dsx2");
    }

    // ---------------------------------------------------------------- filter (present)

    @Test
    void filterStandard_ipPresent() {
        assertThat(qb.filterDeviceIds(scope("all"), List.of(new SplitFilter("ip_address", false)), 1, 50))
                .containsExactlyInAnyOrder("dsx1", "dsx2", "dsx5");
    }

    @Test
    void filterCustom_departmentPresent() {
        assertThat(qb.filterDeviceIds(scope("all"), List.of(new SplitFilter("department", true)), 1, 50))
                .containsExactlyInAnyOrder("dsx1", "dsx2");
    }

    @Test
    void filterMultiple_ipAndVendorBothPresent() {
        // ip present {dsx1,dsx2,dsx5} AND vendor present {dsx1,dsx2} -> {dsx1,dsx2}
        assertThat(qb.filterDeviceIds(scope("all"),
                List.of(new SplitFilter("ip_address", false), new SplitFilter("vendor", false)), 1, 50))
                .containsExactlyInAnyOrder("dsx1", "dsx2");
    }

    // ---------------------------------------------------------------- custom-field lookup

    @Test
    void customFieldDeviceIds_matchesKeyValueWithinScope() {
        assertThat(qb.customFieldDeviceIds("v1", "dock1", "department", "Engineering", 1))
                .containsExactly("dsx1");
    }

    @Test
    void customFieldDeviceIds_foreignVdmsExcluded() {
        // dsx1's custom dept=Engineering exists but is in v1; scoping to v2 yields nothing.
        assertThat(qb.customFieldDeviceIds("v2", "dock2", "department", "Engineering", 1)).isEmpty();
    }
}
