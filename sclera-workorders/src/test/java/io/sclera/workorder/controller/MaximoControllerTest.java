package io.sclera.workorder.controller;

import com.alibaba.fastjson.JSONArray;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.sclera.workorder.dto.MaximoConfigurationDTO;
import io.sclera.workorder.dto.MaximoDTO;
import io.sclera.workorder.dto.VdmsDetailsDTO;
import io.sclera.workorder.exception.MaximoExceptionHandler;
import io.sclera.workorder.exception.VdmsNotFoundException;
import io.sclera.workorder.service.MaximoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import io.sclera.workorder.exception.MaximoException;
import io.sclera.workorder.exception.VdmsUnavailableException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = MaximoController.class)
@Import(MaximoExceptionHandler.class)
class MaximoControllerTest {

    @Autowired private MockMvc mvc;
    private final ObjectMapper mapper = new ObjectMapper();

    @MockitoBean private MaximoService maximoService;

    @Test
    void upsert_routesPathVarsToService() throws Exception {
        MaximoConfigurationDTO body = new MaximoConfigurationDTO(
                "cfg-1", "name", "https://maximo.example.com/oslc", "https://maximo.example.com/auth", "c", "s", "[]");
        when(maximoService.upsertMaximoConfiguration(eq("user1"), eq("vdms-A"), any()))
                .thenReturn("success");

        mvc.perform(post("/maximo/upsertmaximoconfiguration")
                        .param("loggedInUser", "user1")
                        .param("vdms_id", "vdms-A")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(body)))
                .andExpect(status().isOk());
    }

    @Test
    void upsert_rejectsInvalidConfigWith400() throws Exception {
        // blank name and a non-URL serverUrl must be rejected by Bean Validation
        MaximoConfigurationDTO bad = new MaximoConfigurationDTO(
                "cfg-1", "", "not-a-url", "https://maximo.example.com/auth", "", "s", "[]");

        mvc.perform(post("/maximo/upsertmaximoconfiguration")
                        .param("loggedInUser", "user1")
                        .param("vdms_id", "vdms-A")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(bad)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getConfiguration_returnsDtoJson() throws Exception {
        when(maximoService.getMaximoConfigurationByVdmsId("v1"))
                .thenReturn(new MaximoConfigurationDTO("c1", "n", "srv", "auth", "[]"));

        mvc.perform(get("/maximo/getmaximoconfiguration").param("loggedInUser", "user1").param("vdms_id", "v1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("c1"))
                .andExpect(jsonPath("$.serverUrl").value("srv"))
                // 5-arg ctor leaves clientId / clientSecret unset → omitted by @JsonInclude(NON_NULL)
                .andExpect(jsonPath("$.clientId").doesNotExist())
                .andExpect(jsonPath("$.clientSecret").doesNotExist());
    }

    @Test
    void deleteConfiguration_returns200() throws Exception {
        mvc.perform(delete("/maximo/configuration/cfg-1/deletemaximoconfiguration")
                        .param("loggedInUser", "user1")
                        .param("vdms_id", "v1"))
                .andExpect(status().isOk());
    }

    @Test
    void getWorkOrders_acceptsBodyAndPagingParams() throws Exception {
        MaximoDTO body = new MaximoDTO();
        body.setWonum("123");
        when(maximoService.getMaximoWorkOrders(eq("v1"), eq("all"), eq(2), eq(20), any()))
                .thenReturn(Collections.singletonList(body));

        mvc.perform(post("/maximo/workorder/all/getmaximoworkorders")
                        .param("loggedInUser", "user1")
                        .param("vdms_id", "v1")
                        .param("pageno", "2")
                        .param("pagesize", "20")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].wonum").value("123"));
    }

    @Test
    void getMaximoSites_returnsJsonArrayPayload() throws Exception {
        JSONArray sites = new JSONArray();
        com.alibaba.fastjson.JSONObject site = new com.alibaba.fastjson.JSONObject();
        site.put("siteid", "AA");
        site.put("description", "Kazakhstan, Almaty");
        sites.add(site);
        when(maximoService.getMaximoSites()).thenReturn(sites);

        mvc.perform(get("/maximo/getmaximosites").param("loggedInUser", "user1").param("vdms_id", "v1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].siteid").value("AA"));
    }

    // ── Exception handler coverage ─────────────────────────────────────────

    @Test
    void maximoException_returns500WithErrorCode() throws Exception {
        when(maximoService.getMaximoWorkOrders(any(), any(), anyInt(), anyInt(), any()))
                .thenThrow(new MaximoException("Maximo API error", 500, "/test"));

        mvc.perform(post("/maximo/workorder/all/getmaximoworkorders")
                        .param("loggedInUser", "user1")
                        .param("vdms_id", "v1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").value(500))
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void vdmsUnavailableException_returns503() throws Exception {
        when(maximoService.getVdmsDetailsForMaximoConfig("v1"))
                .thenThrow(new VdmsUnavailableException("Dapr sidecar down", new RuntimeException()));

        mvc.perform(get("/maximo/getvdmsdetails").param("loggedInUser", "user1").param("vdms_id", "v1"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.errorCode").value(503))
                .andExpect(jsonPath("$.success").value(false));
    }

    // ── NEW endpoint coverage ───────────────────────────────────────────────

    @Test
    void getVdmsDetails_returns200WithDtoPayload() throws Exception {
        VdmsDetailsDTO dto = new VdmsDetailsDTO();
        dto.setId("v1");
        dto.setName("Site A");
        when(maximoService.getVdmsDetailsForMaximoConfig("v1")).thenReturn(dto);

        mvc.perform(get("/maximo/getvdmsdetails").param("loggedInUser", "user1").param("vdms_id", "v1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("v1"))
                .andExpect(jsonPath("$.name").value("Site A"));
    }

    @Test
    void getWorkOrdersId_returnsList() throws Exception {
        when(maximoService.getMaximoWorkOrderId(eq("v1"), eq("all"), eq(1), eq(10), any()))
                .thenReturn(java.util.List.of("WO-1", "WO-2"));

        mvc.perform(post("/maximo/workorder/all/getmaximoworkorderid")
                        .param("loggedInUser", "user1")
                        .param("vdms_id", "v1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("WO-1"))
                .andExpect(jsonPath("$[1]").value("WO-2"));
    }

    @Test
    void checkMaximoConfiguration_returnsServiceResult() throws Exception {
        when(maximoService.checkConfigurationStatus(eq("user1"), eq("v1"), any()))
                .thenReturn("success");

        mvc.perform(post("/maximo/checkmaximoconfiguration")
                        .param("loggedInUser", "user1")
                        .param("vdms_id", "v1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(
                                new MaximoConfigurationDTO("c1", "n", "srv", "auth", "cid", "sec", "[]"))))
                .andExpect(status().isOk());
    }

    @Test
    void getVdmsDetails_returns404WhenVdmsNotFound() throws Exception {
        when(maximoService.getVdmsDetailsForMaximoConfig("missing"))
                .thenThrow(new VdmsNotFoundException("missing"));

        mvc.perform(get("/maximo/getvdmsdetails").param("loggedInUser", "user1").param("vdms_id", "missing"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value(404))
                .andExpect(jsonPath("$.success").value(false));
    }

    // ── Negative cases: mandatory params & body validation ────────────────────

    @Test
    void upsert_missingLoggedInUser_returns400() throws Exception {
        MaximoConfigurationDTO valid = new MaximoConfigurationDTO(
                "cfg-1", "name", "https://maximo.example.com/oslc", "https://maximo.example.com/auth", "c", "s", "[]");

        mvc.perform(post("/maximo/upsertmaximoconfiguration")
                        .param("vdms_id", "vdms-A")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(valid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value(400))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message", containsString("loggedInUser")));
    }

    @Test
    void upsert_missingVdmsId_returns400() throws Exception {
        MaximoConfigurationDTO valid = new MaximoConfigurationDTO(
                "cfg-1", "name", "https://maximo.example.com/oslc", "https://maximo.example.com/auth", "c", "s", "[]");

        mvc.perform(post("/maximo/upsertmaximoconfiguration")
                        .param("loggedInUser", "user1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(valid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value(400))
                .andExpect(jsonPath("$.message", containsString("vdms_id")));
    }

    @Test
    void upsert_invalidConfig_returns400WithFieldErrors() throws Exception {
        // blank name and a non-URL serverUrl must each be reported. clientId is NOT
        // validated here — a null/blank clientId is a valid credential-preserving update.
        MaximoConfigurationDTO bad = new MaximoConfigurationDTO(
                "cfg-1", "", "not-a-url", "https://maximo.example.com/auth", "", "s", "[]");

        mvc.perform(post("/maximo/upsertmaximoconfiguration")
                        .param("loggedInUser", "user1")
                        .param("vdms_id", "vdms-A")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(bad)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value(400))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message", containsString("name")))
                .andExpect(jsonPath("$.message", containsString("serverUrl")));
    }

    @Test
    void upsert_nullCredentials_returns200_preservingStoredValues() throws Exception {
        // Edit flow: the frontend sends clientId/clientSecret as null to keep the stored
        // credentials (the keep-on-null rule in the repository). This must NOT 400.
        MaximoConfigurationDTO preserving = new MaximoConfigurationDTO(
                "cfg-1", "name", "https://maximo.example.com/oslc",
                "https://maximo.example.com/auth", null, null, "[]");
        when(maximoService.upsertMaximoConfiguration(eq("user1"), eq("vdms-A"), any()))
                .thenReturn("success");

        mvc.perform(post("/maximo/upsertmaximoconfiguration")
                        .param("loggedInUser", "user1")
                        .param("vdms_id", "vdms-A")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(preserving)))
                .andExpect(status().isOk());
    }
}
