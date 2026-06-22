package io.sclera.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.sclera.dto.MaximoDTO;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit coverage for MaximoUtils: OSLC query-parameter building across the various filter
 * branches, the static supported-site catalogue, and session/token state accessors.
 */
class MaximoUtilsTest {

    private final MaximoUtils maximoUtils = new MaximoUtils();

    @Test
    void sessionState_roundTrips() {
        maximoUtils.setToken("tok");
        maximoUtils.setTokenCreatedAt(123L);
        maximoUtils.setServerUrl("https://maximo");
        maximoUtils.setSiteId("SC");
        assertEquals("tok", maximoUtils.getToken());
        assertEquals(123L, maximoUtils.getTokenCreatedAt());
        assertEquals("https://maximo", maximoUtils.getServerUrl());
        assertEquals("SC", maximoUtils.getSiteId());
    }

    @Test
    void buildParams_alwaysSetsSelectPagingAndOrder() {
        Map<String, String> params = maximoUtils.buildParams("all", null, 2, 50);
        assertNotNull(params.get("oslc.select"));
        assertTrue(params.get("oslc.select").contains("wonum"));
        assertEquals("50", params.get("oslc.pageSize"));
        assertEquals("-wonum", params.get("oslc.orderBy"));
        assertEquals("2", params.get("pageno"));
        // "all" + no DTO -> no where clause at all
        assertFalse(params.containsKey("oslc.where"));
    }

    @Test
    void buildParams_filtersByExplicitWorkOrderId() {
        Map<String, String> params = maximoUtils.buildParams("WO123", null, 1, 20);
        assertEquals("wonum=\"WO123\" and istask=false", params.get("oslc.where"));
    }

    @Test
    void buildParams_filtersByStatusAndAssetnum() {
        MaximoDTO dto = new MaximoDTO();
        dto.setStatus("WAPPR");
        dto.setAssetnum("PUMP");
        String where = maximoUtils.buildParams(null, dto, 1, 20).get("oslc.where");
        assertTrue(where.contains("status=\"WAPPR\""), where);
        assertTrue(where.contains("assetnum=\"%PUMP%\""), where);
        assertTrue(where.contains(" and "), where);
        assertTrue(where.endsWith("istask=false"), where);
    }

    @Test
    void buildParams_filtersByCalcpriority() {
        MaximoDTO dto = new MaximoDTO();
        dto.setCalcpriority(3);
        String where = maximoUtils.buildParams(null, dto, 1, 20).get("oslc.where");
        assertTrue(where.contains("calcpriority=3"), where);
    }

    @Test
    void buildParams_buildsSiteIdInClauseFromJson() {
        MaximoDTO dto = new MaximoDTO();
        dto.setSiteid("[{\"siteid\":\"AA\"},{\"siteid\":\"BJ\"}]");
        String where = maximoUtils.buildParams(null, dto, 1, 20).get("oslc.where");
        assertTrue(where.contains("siteID in [\"AA\",\"BJ\"]"), where);
    }

    @Test
    void getSites_returnsCatalogueWithKnownEntry() {
        JSONArray sites = maximoUtils.getSites();
        assertTrue(sites.size() > 150, "expected the full site catalogue");
        JSONObject first = sites.getJSONObject(0);
        assertEquals("AA", first.getString("siteid"));
        assertEquals("Kazakhstan, Almaty", first.getString("description"));
    }

    @Test
    void convertListOfMapToJsonArray_mapsEachEntry() {
        Map<String, String> site = new HashMap<>();
        site.put("siteid", "SC");
        site.put("description", "Santa Clara");
        JSONArray result = MaximoUtils.convertListOfMapToJsonArray(List.of(site));
        assertEquals(1, result.size());
        assertEquals("SC", result.getJSONObject(0).getString("siteid"));
    }
}
