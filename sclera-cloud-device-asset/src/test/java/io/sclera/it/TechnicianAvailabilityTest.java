package io.sclera.it;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Result-asserting integration test for the ported PostgreSQL native query:
 *   Technician.getTechnicianSkillProfileWithPrimarySkillAndAvailabilityById
 *
 * Exercises the ACTUAL source query from Technician.java @NamedNativeQuery via
 * EntityManager.createNativeQuery() with POSITIONAL parameters (?1, ?2).
 * This directly proves that Hibernate 7 accepts the CAST(?2 AS bigint) form
 * without throwing ParameterLabelException (the old ?2::bigint form would throw).
 *
 * SQL features exercised:
 *   - ta.condition::jsonb               (varchar -> jsonb cast, param-free, safe)
 *   - @> jsonb_build_array(...)         (jsonb array containment for days + exceptions)
 *   - TO_TIMESTAMP(CAST(?2 AS bigint) / 1000)  (positional param + CAST, NOT ?2::bigint)
 *   - AT TIME ZONE t.time_zone          (timezone conversion)
 *   - ::date, ::time                    (date/time extraction casts, param-free, safe)
 *   - TO_CHAR(..., 'Dy') + UPPER()      (abbreviated day name, uppercased)
 *
 * Epoch-ms anchor points (all UTC, technician timezone = 'UTC'):
 *   Available:     2024-01-17T12:00:00Z  = 1705492800000  (Wednesday, 12:00 UTC)
 *                  TO_CHAR(Wed,'Dy')='Wed' => UPPER => 'WED' IS in ["MON".."FRI"]
 *                  time 12:00 is between 09:00 and 17:00
 *                  => AVAILABLE
 *
 *   Not Available: 2024-01-14T12:00:00Z  = 1705233600000  (Sunday,    12:00 UTC)
 *                  TO_CHAR(Sun,'Dy')='Sun' => UPPER => 'SUN' NOT in ["MON".."FRI"]
 *                  => NOT AVAILABLE
 *
 * Schema: /schema-pg.sql (BEFORE_TEST_CLASS, once per class)
 * Seed:   /seed/technician-availability.sql (BEFORE_TEST_METHOD, per test)
 * Cleanup: /cleanup-technician-availability.sql (AFTER_TEST_METHOD)
 *
 * Query approach: ACTUAL source SQL copied verbatim from Technician.java @NamedNativeQuery,
 * run via createNativeQuery() with setParameter(1, ...) / setParameter(2, ...) (1-based
 * positional parameters). No rewriting — this is exactly what Spring Data JPA executes
 * at runtime. Proves no ParameterLabelException on the CAST(?N AS bigint) form.
 */
@Sql(
    scripts        = "/schema-pg.sql",
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS
)
@Sql(
    scripts        = "/seed/technician-availability.sql",
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
@Sql(
    scripts        = "/cleanup-technician-availability.sql",
    executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
)
public class TechnicianAvailabilityTest extends PostgresJpaIT {

    // Wednesday 2024-01-17 12:00:00 UTC — within Mon-Fri window, time 09:00-17:00
    private static final long WEDNESDAY_NOON_UTC_MS = 1705492800000L;

    // Sunday 2024-01-14 12:00:00 UTC — outside Mon-Fri window
    private static final long SUNDAY_NOON_UTC_MS = 1705233600000L;

    private static final String TECH_ID = "tech-001";

    /**
     * ACTUAL source SQL from Technician.java @NamedNativeQuery
     * "Technician.getTechnicianSkillProfileWithPrimarySkillAndAvailabilityById",
     * copied verbatim.
     *
     * Positional parameters (1-based, as Hibernate native query requires):
     *   ?1 = technician id (String)
     *   ?2 = epoch-ms as String (passed to CAST(?2 AS bigint))
     *
     * This is the CAST(?N AS bigint) form, NOT the old ?N::bigint form.
     * Hibernate 7 accepts CAST(?N AS bigint) without ParameterLabelException.
     * The old ?2::bigint threw: parser saw "2::bigint" as the parameter label.
     */
    private static final String AVAILABILITY_SQL =
        "SELECT " +
        "t.id AS id, " +
        "t.name AS name, " +
        "t.department AS department, " +
        "MAX( " +
        "CASE " +
        "WHEN ts.type = 'primary' THEN ts.name " +
        "ELSE NULL " +
        "END " +
        ") AS primarySkill, " +
        "CASE " +
        "WHEN COUNT(ta.id) = 0 THEN 'Not Available' " +
        "WHEN MAX( " +
        "CASE " +
        "WHEN (ta.condition::jsonb -> 'exceptions') @> jsonb_build_array(EXTRACT(EPOCH FROM (TO_TIMESTAMP(CAST(?2 AS bigint) / 1000))::date::timestamp AT TIME ZONE t.time_zone)::bigint * 1000) THEN 0 " +
        "WHEN (TO_TIMESTAMP(CAST(?2 AS bigint) / 1000) AT TIME ZONE t.time_zone)::date < (TO_TIMESTAMP(ta.start_date / 1000) AT TIME ZONE t.time_zone)::date " +
        "OR (TO_TIMESTAMP(CAST(?2 AS bigint) / 1000) AT TIME ZONE t.time_zone)::date > (TO_TIMESTAMP(ta.end_date / 1000) AT TIME ZONE t.time_zone)::date THEN 0 " +
        "WHEN (TO_TIMESTAMP(CAST(?2 AS bigint) / 1000) AT TIME ZONE t.time_zone)::time < ta.start_time::time " +
        "OR (TO_TIMESTAMP(CAST(?2 AS bigint) / 1000) AT TIME ZONE t.time_zone)::time > ta.end_time::time THEN 0 " +
        "WHEN NOT (ta.condition::jsonb -> 'days') @> jsonb_build_array(UPPER(TO_CHAR(TO_TIMESTAMP(CAST(?2 AS bigint) / 1000) AT TIME ZONE t.time_zone, 'Dy'))::text) THEN 0 " +
        "ELSE 1 " +
        "END " +
        ") = 1 THEN 'Available' " +
        "ELSE 'Not Available' " +
        "END AS availability " +
        "FROM " +
        "technician t " +
        "LEFT JOIN technician_skill ts ON t.id = ts.technician_id " +
        "LEFT JOIN technician_availability ta ON t.id = ta.technician_id " +
        "WHERE " +
        "t.id = ?1 " +
        "GROUP BY " +
        "t.id, t.name, t.department ";

    @PersistenceContext
    private EntityManager em;

    @Test
    @Transactional
    @DisplayName("Available: Wednesday 12:00 UTC falls within Mon-Fri 09:00-17:00 window (positional params, CAST form)")
    void availableOnWednesdayNoon() {
        // Positional params: ?1 = techId, ?2 = epochMs (as String for CAST(?2 AS bigint))
        // Must NOT throw ParameterLabelException — proves CAST(?2 AS bigint) is accepted by Hibernate 7
        Query q = em.createNativeQuery(AVAILABILITY_SQL)
                .setParameter(1, TECH_ID)
                .setParameter(2, String.valueOf(WEDNESDAY_NOON_UTC_MS));

        @SuppressWarnings("unchecked")
        List<Object[]> rows = q.getResultList();

        assertThat(rows)
                .as("Query must return exactly one row for technician %s", TECH_ID)
                .hasSize(1);

        Object[] row = rows.get(0);
        // columns: id, name, department, primarySkill, availability
        assertThat(row[0]).as("id").isEqualTo(TECH_ID);
        assertThat(row[3]).as("primarySkill")
                .isEqualTo("Network Diagnostics");
        assertThat(row[4]).as("availability — Wednesday 12:00 UTC within Mon-Fri 09:00-17:00")
                .isEqualTo("Available");
    }

    @Test
    @Transactional
    @DisplayName("Not Available: Sunday 12:00 UTC is not in days=[MON,TUE,WED,THU,FRI] (positional params, CAST form)")
    void notAvailableOnSundayNoon() {
        // Positional params: ?1 = techId, ?2 = epochMs (as String for CAST(?2 AS bigint))
        // Must NOT throw ParameterLabelException — proves CAST(?2 AS bigint) is accepted by Hibernate 7
        Query q = em.createNativeQuery(AVAILABILITY_SQL)
                .setParameter(1, TECH_ID)
                .setParameter(2, String.valueOf(SUNDAY_NOON_UTC_MS));

        @SuppressWarnings("unchecked")
        List<Object[]> rows = q.getResultList();

        assertThat(rows)
                .as("Query must return exactly one row for technician %s", TECH_ID)
                .hasSize(1);

        assertThat(rows.get(0)[4]).as("availability — Sunday not in Mon-Fri days")
                .isEqualTo("Not Available");
    }
}
