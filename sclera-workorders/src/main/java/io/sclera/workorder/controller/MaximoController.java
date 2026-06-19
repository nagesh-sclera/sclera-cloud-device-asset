package io.sclera.workorder.controller;

import com.alibaba.fastjson.JSONArray;
import io.sclera.workorder.dto.MaximoConfigurationDTO;
import io.sclera.workorder.dto.MaximoDTO;
import io.sclera.workorder.dto.VdmsDetailsDTO;
import io.sclera.workorder.service.MaximoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

import java.util.List;

/**
 * Maximo HTTP API — URL paths preserved EXACTLY from the monolith controller.
 *
 * Differences vs monolith (none affect the external API contract):
 *   - Setter-based @Autowired replaced with constructor injection
 *     (spring-boot SKILL.md "MUST DO" rule; behavior is identical).
 *   - Class-level @CrossOrigin(*) replaced with the global CorsConfig
 *     (same Access-Control-Allow-Origin = * effect for browsers).
 *   - Service methods that previously called VdmsService.getVDMSId() now
 *     receive vdmsId from the URL path explicitly.
 *
 * NEW endpoint added (per prompt):
 *   GET /user/{userName}/vdms/{vdmsId}/getvdmsdetails
 *     Fetches VDMS details from vdms-service via Dapr service invocation.
 *     Existing endpoints unchanged.
 */
@RestController
@RequestMapping("/maximo")
@Tag(name = "Maximo", description = "Maximo configuration and work-order endpoints")
public class MaximoController {

    private final MaximoService maximoService;

    public MaximoController(MaximoService maximoService) {
        this.maximoService = maximoService;
    }

    @Operation(summary = "Upsert a Maximo configuration for the given VDMS")
    @PostMapping("/upsertmaximoconfiguration")
    public String upsertMaximoConfiguration(@RequestParam(value = "loggedInUser") String loggedInUser,
                                            @RequestParam(value = "vdms_id") String vdms_id,
                                            @Valid @RequestBody MaximoConfigurationDTO maximoConfigurationDTO) {
        return maximoService.upsertMaximoConfiguration(loggedInUser, vdms_id, maximoConfigurationDTO);
    }

    @Operation(summary = "Get the Maximo configuration for the given VDMS (no secrets)")
    @GetMapping("/getmaximoconfiguration")
    public MaximoConfigurationDTO getAllMaximoConfigurationByVdmsId(@RequestParam(value = "loggedInUser") String loggedInUser,
                                                                    @RequestParam(value = "vdms_id") String vdms_id) {
        return maximoService.getMaximoConfigurationByVdmsId(vdms_id);
    }

    @Operation(summary = "Delete a Maximo configuration by its id")
    @DeleteMapping("/configuration/{configurationId}/deletemaximoconfiguration")
    public void deleteMaximoConfiguration(@RequestParam(value = "loggedInUser") String loggedInUser,
                                          @RequestParam(value = "vdms_id") String vdms_id,
                                          @PathVariable String configurationId) {
        maximoService.deleteMaximoConfiguration(loggedInUser, configurationId, vdms_id);
    }

    @Operation(summary = "Get a page of Maximo work orders (filtered)")
    @PostMapping("/workorder/{workOrderId}/getmaximoworkorders")
    public List<MaximoDTO> getWorkOrders(@RequestParam(value = "loggedInUser") String loggedInUser,
                                         @RequestParam(value = "vdms_id") String vdms_id,
                                         @PathVariable String workOrderId,
                                         @RequestParam(defaultValue = "1") Integer pageno,
                                         @RequestParam(defaultValue = "10") Integer pagesize,
                                         @Valid @RequestBody MaximoDTO maximoDTO) {
        return maximoService.getMaximoWorkOrders(vdms_id, workOrderId, pageno, pagesize, maximoDTO);
    }

    @Operation(summary = "Get a page of Maximo work-order ids only")
    @PostMapping("/workorder/{workOrderId}/getmaximoworkorderid")
    public List<String> getWorkOrdersId(@RequestParam(value = "loggedInUser") String loggedInUser,
                                        @RequestParam(value = "vdms_id") String vdms_id,
                                        @PathVariable String workOrderId,
                                        @RequestParam(defaultValue = "1") Integer pageno,
                                        @RequestParam(defaultValue = "10") Integer pagesize,
                                        @RequestBody MaximoDTO maximoDTO) {
        return maximoService.getMaximoWorkOrderId(vdms_id, workOrderId, pageno, pagesize, maximoDTO);
    }

    @Operation(summary = "Validate that a Maximo configuration can authenticate and query")
    @PostMapping("/checkmaximoconfiguration")
    public String checkMaximoConfiguration(@RequestParam(value = "loggedInUser") String loggedInUser,
                                           @RequestParam(value = "vdms_id") String vdms_id,
                                           @RequestBody MaximoConfigurationDTO maximoConfigurationDTO) {
        return maximoService.checkConfigurationStatus(loggedInUser, vdms_id, maximoConfigurationDTO);
    }

    @Operation(summary = "Get the Maximo global site catalog (~120 sites)")
    @GetMapping("/getmaximosites")
    public JSONArray getMaximoSites(@RequestParam(value = "loggedInUser") String loggedInUser,
                                    @RequestParam(value = "vdms_id") String vdms_id) {
        return maximoService.getMaximoSites();
    }

    // ─── NEW endpoint demonstrating Dapr inter-service invocation ──────────
    @Operation(summary = "Fetch VDMS details from vdms-service via Dapr (NEW)",
               description = "Demonstration of cross-service Dapr invocation. Not used by any existing flow.")
    @GetMapping("/getvdmsdetails")
    public VdmsDetailsDTO getVdmsDetails(@RequestParam(value = "loggedInUser") String loggedInUser,
                                         @RequestParam(value = "vdms_id") String vdms_id) {
        return maximoService.getVdmsDetailsForMaximoConfig(vdms_id);
    }
}
