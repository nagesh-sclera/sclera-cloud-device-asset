package io.sclera.workorder.util;

import com.alibaba.fastjson.JSONArray;
import io.sclera.workorder.dto.MaximoDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MaximoUtilsTest {

    private MaximoUtils utils;

    @BeforeEach
    void setUp() {
        utils = new MaximoUtils();
    }

    // ── No filters ──────────────────────────────────────────────────────────

    @Test
    void buildParams_noFilters_omitsWhereClause() {
        Map<String, String> params = utils.buildParams("all", null, 1, 10);

        assertThat(params).doesNotContainKey("oslc.where");
    }

    @Test
    void buildParams_alwaysIncludesPaginationOrderingAndSelect() {
        Map<String, String> params = utils.buildParams("all", null, 3, 25);

        assertThat(params.get("pageno")).isEqualTo("3");
        assertThat(params.get("oslc.pageSize")).isEqualTo("25");
        assertThat(params.get("oslc.orderBy")).isEqualTo("-wonum");
        assertThat(params).containsKey("oslc.select");
    }

    @Test
    void buildParams_selectIncludesExpectedFields() {
        Map<String, String> params = utils.buildParams("all", null, 1, 10);

        String select = params.get("oslc.select");
        assertThat(select).contains("wonum", "description", "status", "assetnum", "siteid");
    }

    // ── workOrderId filter ───────────────────────────────────────────────────

    @Test
    void buildParams_withExactWorkOrderId_buildsExactWonumClause() {
        Map<String, String> params = utils.buildParams("WO-123", null, 1, 10);

        assertThat(params.get("oslc.where")).isEqualTo("wonum=\"WO-123\" and istask=false");
    }

    @Test
    void buildParams_withWorkOrderIdAll_omitsWhereClause() {
        Map<String, String> params = utils.buildParams("all", null, 1, 10);

        assertThat(params).doesNotContainKey("oslc.where");
    }

    // ── DTO-based filters ────────────────────────────────────────────────────

    @Test
    void buildParams_withDtoWonum_appliesWildcard() {
        MaximoDTO dto = new MaximoDTO();
        dto.setWonum("1234");

        Map<String, String> params = utils.buildParams(null, dto, 1, 10);

        assertThat(params.get("oslc.where")).isEqualTo("wonum=\"%1234%\" and istask=false");
    }

    @Test
    void buildParams_withStatus_appendsExactStatusClause() {
        MaximoDTO dto = new MaximoDTO();
        dto.setStatus("APPR");

        Map<String, String> params = utils.buildParams("all", dto, 1, 10);

        assertThat(params.get("oslc.where")).isEqualTo("status=\"APPR\" and istask=false");
    }

    @Test
    void buildParams_withAssetnum_appliesWildcard() {
        MaximoDTO dto = new MaximoDTO();
        dto.setAssetnum("PUMP");

        Map<String, String> params = utils.buildParams("all", dto, 1, 10);

        assertThat(params.get("oslc.where")).isEqualTo("assetnum=\"%PUMP%\" and istask=false");
    }

    @Test
    void buildParams_withSchedstart_appendsExactClause() {
        MaximoDTO dto = new MaximoDTO();
        dto.setSchedstart("2025-01-01");

        Map<String, String> params = utils.buildParams("all", dto, 1, 10);

        assertThat(params.get("oslc.where")).isEqualTo("schedstart=\"2025-01-01\" and istask=false");
    }

    @Test
    void buildParams_withDescription_appliesWildcard() {
        MaximoDTO dto = new MaximoDTO();
        dto.setDescription("pump repair");

        Map<String, String> params = utils.buildParams("all", dto, 1, 10);

        assertThat(params.get("oslc.where")).isEqualTo("description=\"%pump repair%\" and istask=false");
    }

    @Test
    void buildParams_withCalcpriority_appendsNumericClause() {
        MaximoDTO dto = new MaximoDTO();
        dto.setCalcpriority(3);

        Map<String, String> params = utils.buildParams("all", dto, 1, 10);

        assertThat(params.get("oslc.where")).isEqualTo("calcpriority=3 and istask=false");
    }

    @Test
    void buildParams_withLocation_appliesWildcard() {
        MaximoDTO dto = new MaximoDTO();
        dto.setLocation("PLANT-A");

        Map<String, String> params = utils.buildParams("all", dto, 1, 10);

        assertThat(params.get("oslc.where")).isEqualTo("location=\"%PLANT-A%\" and istask=false");
    }

    @Test
    void buildParams_withSiteid_parsesJsonObjectArrayAndBuildsInClause() {
        MaximoDTO dto = new MaximoDTO();
        dto.setSiteid("[{\"siteid\":\"IR\"},{\"siteid\":\"OR\"}]");

        Map<String, String> params = utils.buildParams("all", dto, 1, 10);

        assertThat(params.get("oslc.where")).isEqualTo("siteID in [\"IR\",\"OR\"] and istask=false");
    }

    @Test
    void buildParams_withSingleSiteid_producesCorrectInClause() {
        MaximoDTO dto = new MaximoDTO();
        dto.setSiteid("[{\"siteid\":\"DL-F68\"}]");

        Map<String, String> params = utils.buildParams("all", dto, 1, 10);

        assertThat(params.get("oslc.where")).isEqualTo("siteID in [\"DL-F68\"] and istask=false");
    }

    @Test
    void buildParams_withInvalidSiteidJson_throwsRuntimeException() {
        MaximoDTO dto = new MaximoDTO();
        dto.setSiteid("not-valid-json");

        assertThatThrownBy(() -> utils.buildParams("all", dto, 1, 10))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Error parsing siteId JSON");
    }

    @Test
    void buildParams_withMultipleFilters_joinsAllWithAnd() {
        MaximoDTO dto = new MaximoDTO();
        dto.setStatus("APPR");
        dto.setAssetnum("PUMP");

        Map<String, String> params = utils.buildParams("all", dto, 1, 10);

        String where = params.get("oslc.where");
        assertThat(where).contains("status=\"APPR\"");
        assertThat(where).contains("assetnum=\"%PUMP%\"");
        assertThat(where).contains(" and ");
        assertThat(where).endsWith("and istask=false");
    }

    @Test
    void buildParams_workOrderIdTakesPrecedenceOverDtoWonum() {
        MaximoDTO dto = new MaximoDTO();
        dto.setWonum("ignored");

        Map<String, String> params = utils.buildParams("WO-999", dto, 1, 10);

        assertThat(params.get("oslc.where")).startsWith("wonum=\"WO-999\"");
    }

    // ── Getters / setters ────────────────────────────────────────────────────

    @Test
    void token_roundTrip() {
        utils.setToken("v1", "tok-123");
        assertThat(utils.getToken("v1")).isEqualTo("tok-123");
    }

    @Test
    void token_isolatedPerVdms() {
        utils.setToken("v1", "tok-A");
        utils.setToken("v2", "tok-B");
        assertThat(utils.getToken("v1")).isEqualTo("tok-A");
        assertThat(utils.getToken("v2")).isEqualTo("tok-B");
    }

    @Test
    void tokenCreatedAt_roundTrip() {
        utils.setTokenCreatedAt("v1", 999L);
        assertThat(utils.getTokenCreatedAt("v1")).isEqualTo(999L);
    }

    @Test
    void serverUrl_roundTrip() {
        utils.setServerUrl("v1", "https://maximo/oslc");
        assertThat(utils.getServerUrl("v1")).isEqualTo("https://maximo/oslc");
    }

    @Test
    void serverUrl_isolatedPerVdms() {
        utils.setServerUrl("v1", "https://maximo1/oslc");
        utils.setServerUrl("v2", "https://maximo2/oslc");
        assertThat(utils.getServerUrl("v1")).isEqualTo("https://maximo1/oslc");
        assertThat(utils.getServerUrl("v2")).isEqualTo("https://maximo2/oslc");
    }

    @Test
    void siteId_roundTrip() {
        utils.setSiteId("v1", "IR");
        assertThat(utils.getSiteId("v1")).isEqualTo("IR");
    }

    // ── getSites ─────────────────────────────────────────────────────────────

    @Test
    void getSites_returnsNonEmptyArray() {
        JSONArray sites = utils.getSites();

        assertThat(sites.size()).isGreaterThan(0);
    }

    @Test
    void getSites_containsExpectedSiteIds() {
        JSONArray sites = utils.getSites();

        boolean hasIR = false, hasOR = false, hasDlF68 = false;
        for (int i = 0; i < sites.size(); i++) {
            String id = sites.getJSONObject(i).getString("siteid");
            if ("IR".equals(id)) hasIR = true;
            if ("OR".equals(id)) hasOR = true;
            if ("DL-F68".equals(id)) hasDlF68 = true;
        }
        assertThat(hasIR).as("IR site").isTrue();
        assertThat(hasOR).as("OR site").isTrue();
        assertThat(hasDlF68).as("DL-F68 site").isTrue();
    }

    @Test
    void getSites_eachEntryHasSiteIdAndDescription() {
        JSONArray sites = utils.getSites();

        for (int i = 0; i < sites.size(); i++) {
            var site = sites.getJSONObject(i);
            assertThat(site.getString("siteid")).as("site[%d].siteid", i).isNotBlank();
            assertThat(site.getString("description")).as("site[%d].description", i).isNotBlank();
        }
    }
}
