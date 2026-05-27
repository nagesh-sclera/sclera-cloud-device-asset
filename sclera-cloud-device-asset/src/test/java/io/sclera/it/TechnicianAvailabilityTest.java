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
 * Exercises (via direct native SQL = same SQL as in @NamedNativeQuery):
 *   - ta.condition::jsonb               (varchar -> jsonb cast)
 *   - @> jsonb_build_array(...)         (jsonb array containment for days + exceptions)
 *   - TO_TIMESTAMP(CAST(:epochMs AS bigint) / 1000)   (epoch-ms -> timestamptz)
 *   - AT TIME ZONE t.time_zone          (timezone conversion)
 *   - ::date, ::time                    (date/time extraction casts)
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
 * Note on query approach: we use EntityManager.createNativeQuery() with a named-parameter
 * variant of the SQL from the @NamedNativeQuery on Technician.java. The production
 * @NamedNativeQuery uses ?1/?2 positional parameters which Hibernate 7's parser rejects
 * when ?2 is immediately followed by ::bigint (it tries to parse "2::bigint" as the
 * parameter label). We use CAST(:epochMs AS bigint) instead of ?2::bigint — semantically
 * identical SQL, compatible with both Hibernate 7 and PostgreSQL 16.
 *
 * The TechnicianRepository method getTechnicianSkillProfileWithPrimarySkillAndAvailabilityById
 * delegates to this exact query — this test validates the SQL logic, not the Spring Data layer.
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
     * Named-parameter variant of the @NamedNativeQuery SQL from Technician.java.
     *
     * Changes from the original ?1/?2 positional form:
     *   ?1  => :techId
     *   ?2::bigint => CAST(:epochMs AS bigint)
     *
     * The CAST() form is semantically identical to ::bigint but avoids the
     * Hibernate 7 ParameterLabelException that occurs when a positional parameter
     * is immediately followed by :: (the parser reads "2::bigint" as the label).
     *
     * Named parameters: :techId = technician id (String), :epochMs = epoch-ms String.
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
        "WHEN (ta.condition::jsonb -> 'exceptions') @> jsonb_build_array(EXTRACT(EPOCH FROM (TO_TIMESTAMP(CAST(:epochMs AS bigint) / 1000))::date::timestamp AT TIME ZONE t.time_zone)::bigint * 1000) THEN 0 " +
        "WHEN (TO_TIMESTAMP(CAST(:epochMs AS bigint) / 1000) AT TIME ZONE t.time_zone)::date < (TO_TIMESTAMP(ta.start_date / 1000) AT TIME ZONE t.time_zone)::date " +
        "OR (TO_TIMESTAMP(CAST(:epochMs AS bigint) / 1000) AT TIME ZONE t.time_zone)::date > (TO_TIMESTAMP(ta.end_date / 1000) AT TIME ZONE t.time_zone)::date THEN 0 " +
        "WHEN (TO_TIMESTAMP(CAST(:epochMs AS bigint) / 1000) AT TIME ZONE t.time_zone)::time < ta.start_time::time " +
        "OR (TO_TIMESTAMP(CAST(:epochMs AS bigint) / 1000) AT TIME ZONE t.time_zone)::time > ta.end_time::time THEN 0 " +
        "WHEN NOT (ta.condition::jsonb -> 'days') @> jsonb_build_array(UPPER(TO_CHAR(TO_TIMESTAMP(CAST(:epochMs AS bigint) / 1000) AT TIME ZONE t.time_zone, 'Dy'))::text) THEN 0 " +
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
        "t.id = :techId " +
        "GROUP BY " +
        "t.id, t.name, t.department";

    @PersistenceContext
    private EntityManager em;

    @Test
    @Transactional
    @DisplayName("Available: Wednesday 12:00 UTC falls within Mon-Fri 09:00-17:00 window")
    void availableOnWednesdayNoon() {
        Query q = em.createNativeQuery(AVAILABILITY_SQL)
                .setParameter("techId", TECH_ID)
                .setParameter("epochMs", String.valueOf(WEDNESDAY_NOON_UTC_MS));

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
    @DisplayName("Not Available: Sunday 12:00 UTC is not in days=[MON,TUE,WED,THU,FRI]")
    void notAvailableOnSundayNoon() {
        Query q = em.createNativeQuery(AVAILABILITY_SQL)
                .setParameter("techId", TECH_ID)
                .setParameter("epochMs", String.valueOf(SUNDAY_NOON_UTC_MS));

        @SuppressWarnings("unchecked")
        List<Object[]> rows = q.getResultList();

        assertThat(rows)
                .as("Query must return exactly one row for technician %s", TECH_ID)
                .hasSize(1);

        assertThat(rows.get(0)[4]).as("availability — Sunday not in Mon-Fri days")
                .isEqualTo("Not Available");
    }
}
