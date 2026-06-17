package io.sclera.it;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.sclera.dto.ManagedSoftwareSearchCriteria;
import io.sclera.queryrepository.ManagedSoftwareSearchQueryBuilder;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Result-asserting integration test for ManagedSoftwareSearchQueryBuilder (the JPA Criteria
 * replacement for ManagedSoftwareSearchService.searchSortFilterManagedSoftware* string SQL),
 * driven against the committed fixture (seed/managed-software-search-it.sql) on a real
 * PostgreSQL 16 Testcontainer.
 *
 * Fixture recap:
 *   msx1 active   -> alice(windows) + bob(linux)   (two installed-app rows -> DISTINCT check)
 *   msx2 expired  -> bob(linux)
 *   msx3 'trial'  -> 'others' bucket
 *   msx4 active   -> no installed apps
 *   msx5 status NULL -> only in 'all'
 */
@Sql(scripts = "/schema-pg.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = "/seed/managed-software-search-it.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/cleanup-managed-software-search-it.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@Transactional
class ManagedSoftwareSearchQueryBuilderIT extends PostgresJpaIT {

    @PersistenceContext
    EntityManager em;

    ManagedSoftwareSearchQueryBuilder qb;

    @BeforeEach
    void init() {
        qb = new ManagedSoftwareSearchQueryBuilder(em);
    }

    private List<String> ids(String condition, JSONObject details) {
        return qb.findIds(ManagedSoftwareSearchCriteria.from(condition, details), 1, 50);
    }

    private List<String> ids(String condition) {
        return ids(condition, new JSONObject());
    }

    // ---------------------------------------------------------------- condition buckets

    @Test
    void all_returnsEveryRow() {
        assertThat(ids("all")).containsExactlyInAnyOrder("msx1", "msx2", "msx3", "msx4", "msx5");
    }

    @Test
    void active_onlyActiveStatus() {
        assertThat(ids("active")).containsExactlyInAnyOrder("msx1", "msx4");
    }

    @Test
    void expired_onlyExpiredStatus() {
        assertThat(ids("expired")).containsExactly("msx2");
    }

    @Test
    void others_statusNotInActiveExpired_excludesNull() {
        // msx3 ('trial') qualifies; msx5 (NULL status) does NOT match NOT IN (...).
        assertThat(ids("others")).containsExactly("msx3");
    }

    // ---------------------------------------------------------------- count

    @Test
    void count_matchesConditionBuckets() {
        assertThat(qb.count(ManagedSoftwareSearchCriteria.from("all", new JSONObject()))).isEqualTo(5);
        assertThat(qb.count(ManagedSoftwareSearchCriteria.from("active", new JSONObject()))).isEqualTo(2);
        assertThat(qb.count(ManagedSoftwareSearchCriteria.from("others", new JSONObject()))).isEqualTo(1);
    }

    // ---------------------------------------------------------------- filters (joined ds columns)

    @Test
    void filterByEmailValue_dedupesDistinctManagedSoftware() {
        // ds.email = 'bob@x.com' reaches msx1 (via second dia row) and msx2; msx1 must appear once.
        JSONObject details = filter("email", "bob@x.com");
        assertThat(ids("all", details)).containsExactlyInAnyOrder("msx1", "msx2");
    }

    @Test
    void filterByOsTypePresent_nullValueMeansNotNullAndNonEmpty() {
        // value==null -> os_type IS NOT NULL AND <> '' ; only the linked rows qualify.
        JSONObject details = filter("os_type", null);
        assertThat(ids("all", details)).containsExactlyInAnyOrder("msx1", "msx2");
    }

    // ---------------------------------------------------------------- keyword search

    @Test
    void searchAll_matchesNameAndVendorHaystack() {
        // search-all builds the strip_specials(concat_ws(...)) haystack incl. the ds join.
        assertThat(ids("all", search(null, "acme"))).containsExactly("msx1");
    }

    @Test
    void searchByVendorColumn() {
        assertThat(ids("all", search("vendor", "beta"))).containsExactly("msx2");
    }

    @Test
    void searchByEmailColumn_usesJoin() {
        assertThat(ids("all", search("email", "alice"))).containsExactly("msx1");
    }

    @Test
    void searchWithQuoteInTerm_isBoundNotInjected() {
        assertThat(ids("all", search(null, "a'b\"c"))).isEmpty();
    }

    // ---------------------------------------------------------------- sort (documented fixes)

    @Test
    void sortByName_concatNameApplicationNameAscending() {
        // concat: AcmeSuite, BetaTool, DeltaPack, Epsilon, GammaApp -> A,B,D,E,G
        assertThat(ids("all", sort("name")))
                .containsExactly("msx1", "msx2", "msx4", "msx5", "msx3");
    }

    @Test
    void sortBySubscriptionEndDate_numericDescNoBigintEmptyStringCrash() {
        // end dates 900,800,700,600 desc then NULL (msx5) last. Legacy 'bigint = ''' crash gone.
        assertThat(ids("all", sort("subscription_end_date")))
                .containsExactly("msx1", "msx2", "msx3", "msx4", "msx5");
    }

    @Test
    void sortByEmail_minAggregateUnderGroupBy() {
        // ISNULL()-free ordering: MIN(email) per ms -> alice(msx1), bob(msx2), then NULLs last.
        List<String> result = ids("all", sort("email"));
        assertThat(result).hasSize(5).startsWith("msx1", "msx2");
    }

    // ---------------------------------------------------------------- pagination

    @Test
    void pagination_defaultSortByIdAscending() {
        ManagedSoftwareSearchCriteria c = ManagedSoftwareSearchCriteria.from("all", new JSONObject());
        assertThat(qb.findIds(c, 1, 2)).containsExactly("msx1", "msx2");
        assertThat(qb.findIds(c, 2, 2)).containsExactly("msx3", "msx4");
    }

    // ---------------------------------------------------------------- JSON builders

    private JSONObject filter(String column, String value) {
        JSONObject f = new JSONObject();
        f.put("column", column);
        f.put("value", value);
        JSONArray arr = new JSONArray();
        arr.add(f);
        JSONObject details = new JSONObject();
        details.put("filter_details", arr);
        return details;
    }

    private JSONObject search(String column, String value) {
        JSONObject s = new JSONObject();
        if (column != null) s.put("column", column);
        s.put("value", value);
        JSONObject details = new JSONObject();
        details.put("search_details", s);
        return details;
    }

    private JSONObject sort(String column) {
        JSONObject s = new JSONObject();
        s.put("column", column);
        JSONObject details = new JSONObject();
        details.put("sort_details", s);
        return details;
    }
}
