package io.sclera.it;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

/** Proves the FunctionContributor-registered PG functions are callable from HQL/Criteria. */
@Sql(scripts = "/schema-pg.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = "/seed/device-search-it.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/cleanup-device-search-it.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@Transactional
class PgFunctionContributorIT extends PostgresJpaIT {

    @PersistenceContext
    EntityManager em;

    @Test
    void customFieldText_extractsValueByKey() {
        String dept = em.createQuery(
                "SELECT custom_field_text(d.custom_fields, '$[*].\"department\"') "
                + "FROM Device d WHERE d.id = 'dsx1'", String.class).getSingleResult();
        assertThat(dept).isEqualTo("Engineering");
    }

    @Test
    void customFieldText_missingKey_isNull() {
        String v = em.createQuery(
                "SELECT custom_field_text(d.custom_fields, '$[*].\"nope\"') "
                + "FROM Device d WHERE d.id = 'dsx1'", String.class).getSingleResult();
        assertThat(v).isNull();
    }

    @Test
    void customFieldArrayText_returnsJsonArrayText() {
        String arr = em.createQuery(
                "SELECT custom_field_array_text(d.custom_fields, '$[*].*') "
                + "FROM Device d WHERE d.id = 'dsx1'", String.class).getSingleResult();
        assertThat(arr).contains("\"Engineering\"").contains("\"alice\"");
    }

    @Test
    void stripSpecials_haystackClass_stripsAllOccurrencesIncludingQuotes() {
        // 'g' flag fix: ALL specials stripped, not just the first.
        // Haystack class has the space->dot range, so '"' and '-' are stripped too.
        String s = em.createQuery(
                "SELECT strip_specials('a-b.c\"d e_f') FROM Device d WHERE d.id = 'dsx1'",
                String.class).getSingleResult();
        assertThat(s).isEqualTo("abcdef");
    }

    @Test
    void stripCustomSpecials_quotesSurvive() {
        // Custom-fields class has NO range: '"' must SURVIVE (patterns like %"t"% depend on it).
        String s = em.createQuery(
                "SELECT strip_custom_specials('a-b\"c\"_d') FROM Device d WHERE d.id = 'dsx1'",
                String.class).getSingleResult();
        assertThat(s).isEqualTo("ab\"c\"d");
    }

    @Test
    void stripSpecials_nullInput_isNull() {
        String s = em.createQuery(
                "SELECT strip_specials(d.warranty) FROM Device d WHERE d.id = 'dsx1'",
                String.class).getSingleResult();
        assertThat(s).isNull();   // dsx1 has no warranty value seeded -> NULL propagates
    }

    @Test
    void inetVal_castsForOrdering() {
        java.util.List<String> ips = em.createQuery(
                "SELECT d.ip_address FROM Device d WHERE d.ip_address IS NOT NULL "
                + "AND d.id LIKE 'dsx%' ORDER BY inet_val(d.ip_address)", String.class)
                .getResultList();
        assertThat(ips).containsExactly("9.1.1.1", "10.0.0.2", "10.0.0.10"); // numeric, not lexicographic
    }
}
