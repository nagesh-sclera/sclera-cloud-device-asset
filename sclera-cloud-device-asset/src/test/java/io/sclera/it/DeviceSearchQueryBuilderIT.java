package io.sclera.it;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.sclera.dto.DeviceSearchCriteria;
import io.sclera.queryrepository.DeviceSearchQueryBuilder;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Result-asserting integration test for DeviceSearchQueryBuilder (the JPA Criteria
 * replacement for DeviceSearchService.multipleKeywordSearchSortFilter* string SQL),
 * driven against the committed 6-device fixture (seed/device-search-it.sql) on a real
 * PostgreSQL 16 Testcontainer.
 *
 * The builder is instantiated directly with the test EntityManager — it is a thin
 * @Component over EntityManager, so no Spring wiring is needed for it specifically.
 *
 * Fixture recap (all in vdms 'v1' / docker 'dock1' except dsx9 in 'v2'/'dock2'):
 *   dsx1 online(mon1,st1)  matched(ams1)   onboarded(3)   assigned(alice)  custom dept=Engineering
 *   dsx2 offline(mon1,st0) unmatched(ams0) notonboarded(1) unassigned(NULL) custom dept=Finance
 *   dsx3 unmonitored(mon0) verified(ams2)  onboard NULL   assigned='null'(literal)
 *   dsx4 archived(ams3)    mon1            onboard NULL    -> excluded from every non-archived view
 *   dsx5 virtual 'other'(vdt5) mon NULL    unmatched(ams0)
 *   dsx9 FOREIGN vdms 'v2' -> must never appear in v1-scoped results
 */
@Sql(scripts = "/schema-pg.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = "/seed/device-search-it.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/cleanup-device-search-it.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@Transactional
class DeviceSearchQueryBuilderIT extends PostgresJpaIT {

    @PersistenceContext
    EntityManager em;

    DeviceSearchQueryBuilder qb;

    @BeforeEach
    void init() {
        qb = new DeviceSearchQueryBuilder(em);
    }

    /** condition-only criteria, scoped to v1/dock1, no onboard-status param filter (123). */
    private DeviceSearchCriteria cond(String condition) {
        return DeviceSearchCriteria.from("v1", "dock1", condition, new JSONObject(), 123);
    }

    private List<String> ids(String condition) {
        return qb.findAllIds(cond(condition));
    }

    // ---------------------------------------------------------------- scope + condition

    @Test
    void all_returnsScopedNonArchived() {
        // 'all' applies only the default asset_match_status != 3 rule -> dsx4 (archived) out,
        // dsx9 (foreign vdms) out by scope.
        assertThat(ids("all")).containsExactlyInAnyOrder("dsx1", "dsx2", "dsx3", "dsx5");
    }

    @Test
    void foreignVdmsDeviceNeverLeaksIntoScopedResult() {
        assertThat(ids("all")).doesNotContain("dsx9");
        // and positively: scoping to v2 returns only the foreign device
        assertThat(qb.findAllIds(DeviceSearchCriteria.from("v2", "dock2", "all", new JSONObject(), 123)))
                .containsExactly("dsx9");
    }

    @Test
    void online_monitorAndStatusBoth1() {
        assertThat(ids("online")).containsExactly("dsx1");
    }

    @Test
    void offline_monitor1Status0() {
        assertThat(ids("offline")).containsExactly("dsx2");
    }

    @Test
    void unmonitored_monitor0OrNull() {
        assertThat(ids("unmonitored")).containsExactlyInAnyOrder("dsx3", "dsx5");
    }

    @Test
    void other_virtualDeviceTypeNotNullNot0Not1() {
        assertThat(ids("other")).containsExactly("dsx5");
    }

    @Test
    void assetMatchStatusBuckets() {
        assertThat(ids("matched")).containsExactly("dsx1");
        assertThat(ids("unmatched")).containsExactlyInAnyOrder("dsx2", "dsx5");
        assertThat(ids("verified")).containsExactly("dsx3");
        // archived is the ONLY view that surfaces ams=3
        assertThat(ids("archived")).containsExactly("dsx4");
    }

    @Test
    void onboarded_exactOnboardStatus3() {
        assertThat(ids("onboarded")).containsExactly("dsx1");
    }

    @Test
    void notonboarded_excludesNullOnboardStatus() {
        // onboard_status != 3 matches dsx2 (1) and dsx3 (2).
        // Preserved legacy semantics: '!= 3' does NOT match NULL, so dsx5 (NULL
        // onboard_status, otherwise eligible via ams=0) is correctly absent.
        assertThat(ids("notonboarded"))
                .containsExactlyInAnyOrder("dsx2", "dsx3")
                .doesNotContain("dsx5");
    }

    @Test
    void unassigned_treatsLiteralNullStringAsUnassigned() {
        // dsx2 (NULL), dsx5 (NULL) and dsx3 (literal 'null') all count as unassigned;
        // dsx4 (NULL) is filtered out separately by the ams != 3 default.
        assertThat(ids("unassigned")).containsExactlyInAnyOrder("dsx2", "dsx3", "dsx5");
    }

    @Test
    void onboardStatusParam_appliesWhenConditionDoesNotDecide() {
        // condition 'all' leaves onboard undecided -> the method param (3) filters to dsx1.
        assertThat(qb.findAllIds(DeviceSearchCriteria.from("v1", "dock1", "all", new JSONObject(), 3)))
                .containsExactly("dsx1");
    }

    // ---------------------------------------------------------------- count variant

    @Test
    void count_matchesFindAllIdsSize() {
        assertThat(qb.count(cond("all"))).isEqualTo(4);
        assertThat(qb.count(cond("archived"))).isEqualTo(1);
    }

    // ---------------------------------------------------------------- pagination + default sort

    @Test
    void pagination_defaultSortIsUpdatedTimestampDescThenId() {
        // updated_timestamp: dsx1=500, dsx2=400, dsx3=300, dsx5=100 -> desc order
        DeviceSearchCriteria c = cond("all");
        assertThat(qb.findIds(c, 1, 2)).containsExactly("dsx1", "dsx2");
        assertThat(qb.findIds(c, 2, 2)).containsExactly("dsx3", "dsx5");
    }

    // ---------------------------------------------------------------- sort (documented fixes)

    @Test
    void sortByCreatedTimestamp_noBigintEmptyStringCrash() {
        // Legacy appended "col = ''" for timestamp sorts -> 'bigint = empty string' PG error.
        // created_timestamp: dsx5=500, dsx3=300, dsx2=200, dsx1=100 -> desc.
        DeviceSearchCriteria c = sortBy("created_timestamp", false);
        assertThat(qb.findAllIds(c)).containsExactly("dsx5", "dsx3", "dsx2", "dsx1");
    }

    @Test
    void sortByUsername_joinsDeviceSpecification() {
        // Documented fix: legacy outer query referenced ds.* without joining it -> SQL error.
        // Only dsx1 has a device_specification (username winuser01); nulls sort last.
        List<String> result = qb.findAllIds(sortBy("username", false));
        assertThat(result).hasSize(4).startsWith("dsx1");
    }

    @Test
    void sortByDisplayName_userDataFallbackAscending() {
        // resolved names: dsx2 'Beta Device', dsx5 'Epsilon Device',
        //                 dsx3 'Gamma!! Device', dsx1 'My-Alpha' (user_data_name)
        assertThat(qb.findAllIds(sortBy("display_name", false)))
                .containsExactly("dsx2", "dsx5", "dsx3", "dsx1");
    }

    private DeviceSearchCriteria sortBy(String column, boolean custom) {
        JSONObject details = new JSONObject();
        JSONObject sort = new JSONObject();
        sort.put("column", column);
        sort.put("custom", custom);
        details.put("sort_details", sort);
        return DeviceSearchCriteria.from("v1", "dock1", "all", details, 123);
    }

    // ---------------------------------------------------------------- keyword search (PG functions)

    @Test
    void searchAll_matchesAcrossHaystack_exercisesStripAndJoins() {
        // search-all builds the big strip_specials(concat_ws(...)) haystack including the
        // dos/dosa/device_specification joins and the custom-fields injector.
        // 'My-Alpha' (dsx1 user_data_name) strips to 'myalpha' -> contains 'alpha'.
        assertThat(qb.findAllIds(searchAll("alpha", null, false))).containsExactly("dsx1");
    }

    @Test
    void searchCustomField_usesJsonbPathQuery() {
        // custom_field_text(custom_fields, '$[*]."department"') -> dsx2 has dept=Finance.
        assertThat(qb.findAllIds(searchAll("Finance", "department", true)))
                .containsExactly("dsx2");
    }

    @Test
    void searchWithQuoteInTerm_isBoundNotInjected() {
        // A value containing a quote must not break the query (bound param, not concatenated).
        // No fixture matches; the point is it executes cleanly and returns empty.
        assertThat(qb.findAllIds(searchAll("a'b\"c", null, false))).isEmpty();
    }

    /** Build a single-keyword search criteria. column==null means search-all. */
    private DeviceSearchCriteria searchAll(String value, String column, boolean custom) {
        JSONObject details = new JSONObject();
        JSONArray searches = new JSONArray();
        JSONObject s = new JSONObject();
        if (column != null) {
            s.put("column", column);
            s.put("custom", custom);
        }
        s.put("value", value);
        searches.add(s);
        details.put("search_details", searches);
        return DeviceSearchCriteria.from("v1", "dock1", "all", details, 123);
    }
}
