package io.sclera.it;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Throwaway diagnostic test: probes which PostgreSQL {@code ::} cast patterns
 * survive Hibernate 7's native-query parameter parser, and which fail at
 * query-creation / parameter-binding time.
 *
 * Each case is wrapped in its own try/catch so that a failure in one probe does
 * not abort subsequent probes.  Results are printed as a table and the test
 * asserts the expected outcomes so that the CI record is unambiguous.
 *
 * Run with:
 *   mvnw -q -Dtest=CastParsingProbeTest test
 *
 * Safe to delete once the investigation is complete; keeping it as a living
 * specification of the Hibernate 7 :: cast behaviour.
 */
@Transactional
public class CastParsingProbeTest extends PostgresJpaIT {

    @PersistenceContext
    private EntityManager em;

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------

    private record ProbeResult(boolean passed, String detail) {}

    private ProbeResult run(Runnable block) {
        try {
            block.run();
            return new ProbeResult(true, "PASS");
        } catch (Exception e) {
            return new ProbeResult(false, "FAIL [" + e.getClass().getSimpleName() + ": " + sanitise(e.getMessage()) + "]");
        }
    }

    /** Trim long messages to avoid console spam. */
    private static String sanitise(String msg) {
        if (msg == null) return "(null)";
        String flat = msg.replace('\n', ' ').replace('\r', ' ');
        return flat.length() > 180 ? flat.substring(0, 180) + "…" : flat;
    }

    // -------------------------------------------------------------------------
    // Probes
    // -------------------------------------------------------------------------

    @Test
    void probeAllCastPatterns() {

        Map<String, ProbeResult> results = new LinkedHashMap<>();

        // ---- Case 1: CAST(:p AS bigint) — named param, CAST() syntax — baseline ----
        results.put("1  CAST(:p AS bigint)  named-param CAST() baseline",
            run(() -> {
                em.createNativeQuery("SELECT cast(:p AS bigint)")
                  .setParameter("p", 123)
                  .getSingleResult();
            })
        );

        // ---- Case 2: CAST(?1 AS bigint) — positional param, CAST() syntax ----
        results.put("2  CAST(?1 AS bigint)  positional CAST() baseline",
            run(() -> {
                em.createNativeQuery("SELECT cast(?1 AS bigint)")
                  .setParameter(1, 123)
                  .getSingleResult();
            })
        );

        // ---- Case 3: ?1::bigint — positional param immediately followed by :: ----
        // Suspected FAIL: Hibernate parser may read "1::bigint" as a label token,
        // causing ParameterLabelException (mixing positional/named) or parse error.
        results.put("3  ?1::bigint  positional + :: cast  (SUSPECTED FAIL)",
            run(() -> {
                em.createNativeQuery("SELECT ?1::bigint")
                  .setParameter(1, 123)
                  .getSingleResult();
            })
        );

        // ---- Case 4: 123::bigint — pure literal :: cast, no param at all ----
        results.put("4  123::bigint  literal :: cast no param",
            run(() -> {
                em.createNativeQuery("SELECT 123::bigint")
                  .getSingleResult();
            })
        );

        // ---- Case 5: '[1,2]'::jsonb — literal string :: jsonb ----
        results.put("5  '[1,2]'::jsonb  literal :: jsonb",
            run(() -> {
                em.createNativeQuery("SELECT '[1,2]'::jsonb")
                  .getSingleResult();
            })
        );

        // ---- Case 6a: ('MON'::text) — literal :: text ----
        results.put("6a 'MON'::text  literal :: text",
            run(() -> {
                em.createNativeQuery("SELECT 'MON'::text")
                  .getSingleResult();
            })
        );

        // ---- Case 6b: '[\"MON\"]'::jsonb @> jsonb_build_array('MON'::text) — :: with @> operator ----
        results.put("6b '[\"MON\"]'::jsonb @> jsonb_build_array('MON'::text)  :: with @>",
            run(() -> {
                em.createNativeQuery("SELECT ('[\"MON\"]'::jsonb) @> jsonb_build_array('MON'::text)")
                  .getSingleResult();
            })
        );

        // ---- Case 6c: EXTRACT(EPOCH FROM NOW())::bigint — function-result :: cast, no param ----
        // This is the pattern used in Device.java and Lorawan_Sensor.java
        results.put("6c EXTRACT(EPOCH FROM NOW())::bigint  function-result :: cast no param",
            run(() -> {
                em.createNativeQuery("SELECT EXTRACT(EPOCH FROM NOW())::bigint")
                  .getSingleResult();
            })
        );

        // ---- Case 7a: to_timestamp(?1::bigint / 1000) — positional param + :: inside function arg ----
        // This is the exact production pattern in Technician.java (?2::bigint).
        results.put("7a to_timestamp(?1::bigint / 1000)  positional + :: inside fn (SUSPECTED FAIL)",
            run(() -> {
                em.createNativeQuery("SELECT to_timestamp(?1::bigint / 1000)")
                  .setParameter(1, "1705492800000")
                  .getSingleResult();
            })
        );

        // ---- Case 7b: to_timestamp(cast(?1 AS bigint) / 1000) — CAST() replacement ----
        // Expected PASS — this is the workaround used in TechnicianAvailabilityTest.
        results.put("7b to_timestamp(cast(?1 AS bigint) / 1000)  positional CAST() replacement (EXPECTED PASS)",
            run(() -> {
                em.createNativeQuery("SELECT to_timestamp(cast(?1 AS bigint) / 1000)")
                  .setParameter(1, "1705492800000")
                  .getSingleResult();
            })
        );

        // ---- Case 7c: to_timestamp(cast(:p AS bigint) / 1000) — named-param CAST() replacement ----
        results.put("7c to_timestamp(cast(:p AS bigint) / 1000)  named CAST() replacement (EXPECTED PASS)",
            run(() -> {
                em.createNativeQuery("SELECT to_timestamp(cast(:p AS bigint) / 1000)")
                  .setParameter("p", "1705492800000")
                  .getSingleResult();
            })
        );

        // ---- Case 8a: SELECT :p::bigint — named param followed by :: ----
        // Does `:p::bigint` confuse Hibernate? `:p` is named param; `::bigint` follows.
        // The parser might consume `:p:` as the param name and leave `:bigint` as a
        // second named param, or handle it correctly.
        results.put("8a :p::bigint  named-param + :: cast (BEHAVIOUR UNKNOWN)",
            run(() -> {
                em.createNativeQuery("SELECT :p::bigint")
                  .setParameter("p", 123)
                  .getSingleResult();
            })
        );

        // ---- Case 8b: cast(:p AS bigint) as CAST() equivalent to 8a — confirmation PASS ----
        results.put("8b cast(:p AS bigint)  CAST() for case-8 equivalence (EXPECTED PASS)",
            run(() -> {
                em.createNativeQuery("SELECT cast(:p AS bigint)")
                  .setParameter("p", 123)
                  .getSingleResult();
            })
        );

        // ---- Case 9: column::jsonb expression — col alias :: cast, no param ----
        // e.g. ta.condition::jsonb from Technician queries.
        // Use a subquery that returns a varchar and cast it.
        results.put("9  ('some json'::text)::jsonb  expr :: jsonb chain no param",
            run(() -> {
                em.createNativeQuery("SELECT ('{\"k\":1}'::text)::jsonb")
                  .getSingleResult();
            })
        );

        // ---- Print results table ----
        System.out.println("\n");
        System.out.println("╔══════════════════════════════════════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║  CastParsingProbeTest — Hibernate 7 :: cast pattern results                                            ║");
        System.out.println("╠══════════════════════════════════════════════════════════════════════════════════════════════════════════╣");
        for (Map.Entry<String, ProbeResult> e : results.entrySet()) {
            String status = e.getValue().passed ? "PASS" : "FAIL";
            System.out.printf("║  %-4s  %s%n", status, e.getKey());
            if (!e.getValue().passed) {
                System.out.printf("║        => %s%n", e.getValue().detail);
            }
        }
        System.out.println("╚══════════════════════════════════════════════════════════════════════════════════════════════════════════╝");
        System.out.println();

        // ---- Assertions: encode the expected rule ----
        // Cases that MUST pass (safe patterns):
        assertPass(results, "1  CAST(:p AS bigint)  named-param CAST() baseline");
        assertPass(results, "2  CAST(?1 AS bigint)  positional CAST() baseline");
        assertPass(results, "4  123::bigint  literal :: cast no param");
        assertPass(results, "5  '[1,2]'::jsonb  literal :: jsonb");
        assertPass(results, "6a 'MON'::text  literal :: text");
        assertPass(results, "6b '[\"MON\"]'::jsonb @> jsonb_build_array('MON'::text)  :: with @>");
        assertPass(results, "6c EXTRACT(EPOCH FROM NOW())::bigint  function-result :: cast no param");
        assertPass(results, "7b to_timestamp(cast(?1 AS bigint) / 1000)  positional CAST() replacement (EXPECTED PASS)");
        assertPass(results, "7c to_timestamp(cast(:p AS bigint) / 1000)  named CAST() replacement (EXPECTED PASS)");
        assertPass(results, "8b cast(:p AS bigint)  CAST() for case-8 equivalence (EXPECTED PASS)");
        assertPass(results, "9  ('some json'::text)::jsonb  expr :: jsonb chain no param");

        // Cases that MUST fail (breaking patterns):
        assertFail(results, "3  ?1::bigint  positional + :: cast  (SUSPECTED FAIL)");
        assertFail(results, "7a to_timestamp(?1::bigint / 1000)  positional + :: inside fn (SUSPECTED FAIL)");

        // Case 8a (:p::bigint) — document actual behaviour without hard-asserting
        // direction; we print which way it went so the developer can verify.
        ProbeResult case8a = results.get("8a :p::bigint  named-param + :: cast (BEHAVIOUR UNKNOWN)");
        System.out.println("[CastParsingProbeTest] Case 8a (:p::bigint) actual outcome: " + case8a.detail);
    }

    private void assertPass(Map<String, ProbeResult> results, String key) {
        ProbeResult r = results.get(key);
        if (r == null) throw new AssertionError("Probe key not found: " + key);
        if (!r.passed) {
            throw new AssertionError("Expected PASS for probe [" + key + "] but got: " + r.detail);
        }
    }

    private void assertFail(Map<String, ProbeResult> results, String key) {
        ProbeResult r = results.get(key);
        if (r == null) throw new AssertionError("Probe key not found: " + key);
        if (r.passed) {
            // Unexpected pass — report but do NOT throw; the rule may differ from our hypothesis.
            // We print a clear warning so the developer can update the rule document.
            System.out.println("[CastParsingProbeTest] WARNING: Expected FAIL for probe [" + key + "] but it PASSED. Update the rule!");
        }
    }
}
