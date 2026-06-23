package io.sclera.service;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Coverage for DeviceSearchService.generateMultiConditionStmt - the pure presence-filter builder
 * over custom (jsonb) and standard columns, including the AND joiner between conditions.
 */
class DeviceSearchServiceMultiConditionTest {

    private final DeviceSearchService service = new DeviceSearchService();

    private Map<String, Object> cond(boolean custom, String column) {
        Map<String, Object> m = new HashMap<>();
        m.put("custom", custom);
        m.put("column", column);
        return m;
    }

    @Test
    void generateMultiConditionStmt_standardColumn_buildsPresenceCheck() {
        String q = service.generateMultiConditionStmt(List.of(cond(false, "vendor")), "v1", "dock");
        assertThat(q).contains("IS NOT NULL AND").contains("<> ''");
    }

    @Test
    void generateMultiConditionStmt_customColumn_buildsJsonbPresenceCheck() {
        String q = service.generateMultiConditionStmt(List.of(cond(true, "AssetTag")), "v1", "dock");
        assertThat(q).contains("jsonb_path_query_first").contains("IS NOT NULL");
    }

    @Test
    void generateMultiConditionStmt_multipleConditions_joinsWithAnd() {
        String q = service.generateMultiConditionStmt(
                List.of(cond(false, "vendor"), cond(true, "AssetTag")), "v1", "dock");
        assertThat(q).contains("jsonb_path_query_first");
        // the inter-condition joiner is present (more than one trailing "AND ")
        assertThat(q.split("AND ").length).isGreaterThan(2);
    }
}
