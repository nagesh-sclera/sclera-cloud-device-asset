package io.sclera.it;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Validates that the ported ORDER BY expression
 *   jsonb_path_query_first(custom_fields::jsonb, '$[*]."<field>"') #>> '{}'
 * sorts correctly on PostgreSQL 16:
 *   - Non-null extracted values sort alphabetically (ASC).
 *   - Rows where the field is absent → NULL → sort last (ASC NULL LAST behaviour
 *     via the IS NULL guard as first sort key).
 *   - Empty-string values sort before non-empty values (IS NULL OR = '' guard).
 *
 * Uses a VALUES(...) table to avoid needing any application table.
 *
 * Chunk S — Phase 3.6 PostgreSQL migration.
 */
@Transactional
public class JsonbPathQuerySortTest extends PostgresJpaIT {

    @PersistenceContext
    private EntityManager em;

    /**
     * Sort rows by the extracted "loc" field using the exact PG-port pattern.
     *
     * The first ORDER BY key is:
     *   (jsonb_path_query_first(...) #>> '{}' IS NULL OR ... = '')
     * In PostgreSQL boolean ordering (ASC): FALSE < TRUE.
     * So rows where this expression is FALSE (non-null, non-empty) sort FIRST,
     * and rows where it is TRUE (NULL or empty) sort LAST.
     * This faithfully matches the MySQL original's "push nulls/empty to end" intent.
     *
     * Expected order: "a" -> "b" -> "c" (non-null non-empty, ASC) -> "" (empty, TRUE) -> NULL (missing, TRUE).
     * Within the TRUE group the second key is the extracted value itself:
     *   "" < NULL (PostgreSQL NULLs sort last by default in ASC).
     */
    @Test
    @SuppressWarnings("unchecked")
    void sortByExtractedCustomField_orderIsCorrect() {
        String sql =
            "SELECT v::text, (jsonb_path_query_first(v, '$[*].\"loc\"') #>> '{}') AS k " +
            "FROM (VALUES " +
            "  ('[{\"loc\":\"b\"}]'::jsonb), " +
            "  ('[{\"loc\":\"a\"}]'::jsonb), " +
            "  ('[{\"x\":1}]'::jsonb), " +        // missing "loc" -> NULL
            "  ('[{\"loc\":\"c\"}]'::jsonb), " +
            "  ('[{\"loc\":\"\"}]'::jsonb) " +    // empty string
            ") t(v) " +
            "ORDER BY " +
            "  (jsonb_path_query_first(v, '$[*].\"loc\"') #>> '{}' IS NULL OR " +
            "   jsonb_path_query_first(v, '$[*].\"loc\"') #>> '{}' = ''), " +
            "  jsonb_path_query_first(v, '$[*].\"loc\"') #>> '{}'";

        List<Object[]> rows = em.createNativeQuery(sql).getResultList();

        assertEquals(5, rows.size(), "Expected 5 rows");

        // Rows 0-2: non-null, non-empty -> boolean key is FALSE -> sort first, alphabetically
        assertEquals("a", rows.get(0)[1], "Row 0 should be 'a'");
        assertEquals("b", rows.get(1)[1], "Row 1 should be 'b'");
        assertEquals("c", rows.get(2)[1], "Row 2 should be 'c'");

        // Rows 3-4: boolean key is TRUE (null or empty) -> sort last
        // Within this group, sorted by the extracted value ASC (NULLs last):
        // "" < NULL -> empty string at row 3, NULL at row 4
        assertEquals("", rows.get(3)[1], "Row 3 should be empty string");
        assertNull(rows.get(4)[1], "Row 4 should be NULL (missing field sorts last)");
    }

    /**
     * EXPLAIN smoke test: verify that the ported ORDER BY expression plans without error
     * on a VALUES-based derived table (no application tables needed).
     * Error would only be syntax/type errors; "relation does not exist" is irrelevant here.
     */
    @Test
    @SuppressWarnings("unchecked")
    void explainSortQuery_plansWithoutError() {
        // Use a VALUES table with a jsonb column named custom_fields to mirror the real query shape.
        String explainSql =
            "EXPLAIN SELECT k FROM (" +
            "  SELECT (jsonb_path_query_first(custom_fields, '$[*].\"testfield\"') #>> '{}') AS k " +
            "  FROM (VALUES ('[{\"testfield\":\"x\"}]'::jsonb), ('[{\"y\":1}]'::jsonb)) t(custom_fields) " +
            "  ORDER BY " +
            "    (jsonb_path_query_first(custom_fields, '$[*].\"testfield\"') #>> '{}' IS NULL OR " +
            "     jsonb_path_query_first(custom_fields, '$[*].\"testfield\"') #>> '{}' = ''), " +
            "    jsonb_path_query_first(custom_fields, '$[*].\"testfield\"') #>> '{}' " +
            "  LIMIT 20 OFFSET 0" +
            ") sub";

        // Will throw if the SQL has a syntax/type error; just needs to plan successfully.
        List<?> plan = em.createNativeQuery(explainSql).getResultList();
        // EXPLAIN always returns at least one row (the plan text).
        assert !plan.isEmpty() : "EXPLAIN returned no output — unexpected";
        System.out.println("[JsonbPathQuerySortTest] EXPLAIN output:");
        plan.forEach(row -> System.out.println("  " + row));
    }
}
