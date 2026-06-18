package io.sclera.controller.admin;

import io.sclera.service.UserActionLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Network (gateway / docker) management for the POC: list networks for a VDMS,
 * add a new network, and move a device to a different network. A network is a row
 * in the `docker` table; device.docker_name has an FK to docker(name, vdms_id),
 * so a device can only be moved to an existing network.
 */
@RestController
@RequestMapping("/api/v1/sclera-cloud-device-asset-service")
@Tag(name = "Networks", description = "List networks, add a network and move/touch devices between networks for a VDMS.")
public class NetworkController {

    private static final Logger log = LoggerFactory.getLogger(NetworkController.class);

    @PersistenceContext
    private EntityManager em;

    private final UserActionLogService userActionLogService;

    public NetworkController(UserActionLogService userActionLogService) {
        this.userActionLogService = userActionLogService;
    }

    /**
     * Lists the networks (dockers) configured for the given VDMS.
     *
     * @param vdmsId owning VDMS id
     * @return list of networks with their gateway and status fields
     */
    @Operation(summary = "List networks for a VDMS",
            description = "Returns the networks (dockers) configured for the given VDMS with their gateway and status fields.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Networks returned"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @GetMapping("/networks")
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> list(
            @Parameter(description = "Owning VDMS id") @RequestParam("vdms_id") String vdmsId) {
        log.info("list networks vdms_id={}", vdmsId);
        List<Object[]> rows = em.createNativeQuery(
                        "SELECT name, gateway, system_type, internet_status, configuration_status, host " +
                        "FROM docker WHERE vdms_id = :v ORDER BY name")
                .setParameter("v", vdmsId).getResultList();
        List<Map<String, Object>> out = new ArrayList<>();
        for (Object[] r : rows) {
            Map<String, Object> m = new HashMap<>();
            m.put("name", r[0]);
            m.put("gateway", r[1]);
            m.put("system_type", r[2]);
            m.put("internet_status", r[3]);
            m.put("configuration_status", r[4]);
            m.put("master", r[5]);
            out.add(m);
        }
        return out;
    }

    /**
     * Adds a new network (docker) for the given VDMS if one with the same name does not exist.
     *
     * @param vdmsId   owning VDMS id
     * @param username acting user, for the audit log
     * @param body     request body carrying name, gateway and system_type
     * @return the resolved network name, gateway and system type
     */
    @Operation(summary = "Add a network",
            description = "Adds a new network (docker) for the given VDMS, unless a network with the same name already exists.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Network added or already existed"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PostMapping("/network")
    @Transactional
    public Map<String, Object> add(
            @Parameter(description = "Owning VDMS id") @RequestParam("vdms_id") String vdmsId,
            @Parameter(description = "Acting user, for the audit log") @RequestParam(required = false) String username,
            @RequestBody Map<String, String> body) {
        log.info("add network vdms_id={}", vdmsId);
        String name = body.get("name");
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name is required");
        }
        name = name.trim();
        String gateway = body.getOrDefault("gateway", "");
        if (gateway == null) gateway = "";
        String systemType = body.getOrDefault("system_type", "generic");
        if (systemType == null || systemType.isBlank()) systemType = "generic";

        Number cnt = (Number) em.createNativeQuery(
                        "SELECT count(*) FROM docker WHERE name = :n AND vdms_id = :v")
                .setParameter("n", name).setParameter("v", vdmsId).getSingleResult();
        if (cnt.intValue() == 0) {
            em.createNativeQuery("INSERT INTO docker(name, vdms_id, gateway, host, system_type, network_origin, configuration_status, internet_status) " +
                            "VALUES (:n, :v, :g, true, :st, 1, 'not_configured', 'unknown')")
                    .setParameter("n", name).setParameter("v", vdmsId).setParameter("g", gateway).setParameter("st", systemType)
                    .executeUpdate();
            log.info("added network name={} system_type={} vdms={}", name, systemType, vdmsId);
        }
        Map<String, Object> m = new HashMap<>();
        m.put("name", name);
        m.put("gateway", gateway);
        m.put("system_type", systemType);
        return m;
    }

    /**
     * Moves a device to a different (existing) network.
     *
     * @param deviceId   device to move
     * @param dockerName target network (docker) name
     * @param vdms_id    owning VDMS id
     * @param username   acting user, for the audit log
     */
    @Operation(summary = "Move a device to a network",
            description = "Moves a device to a different existing network by updating its docker_name.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Device moved"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PostMapping("/device/{device_id}/network")
    @Transactional
    public void moveDevice(
            @Parameter(description = "Device to move") @PathVariable("device_id") String deviceId,
            @Parameter(description = "Target network (docker) name") @RequestParam("docker_name") String dockerName,
            @Parameter(description = "Owning VDMS id") @RequestParam(required = false) String vdms_id,
            @Parameter(description = "Acting user, for the audit log") @RequestParam(required = false) String username) {
        log.info("moveDevice device_id={} docker_name={}", deviceId, dockerName);
        int n = em.createNativeQuery("UPDATE device SET docker_name = :d, updated_timestamp = :t WHERE id = :id")
                .setParameter("d", dockerName)
                .setParameter("t", System.currentTimeMillis())
                .setParameter("id", deviceId).executeUpdate();
        log.info("moved device {} to network {} ({} rows updated)", deviceId, dockerName, n);
        userActionLogService.addUserAction(username, "asset", "UPDATE",
                "Device " + deviceId + " was moved to network " + dockerName, "success", "network", deviceId);
    }

    /**
     * Stamps a device's updated_timestamp/updated_email so the UI's "Updated Date"
     * refreshes after an edit (the editDeviceByDeviceID query does not touch them).
     *
     * @param deviceId device to stamp
     * @param username acting user, used as updated_email (defaults to "admin")
     * @param vdmsid   owning VDMS id
     */
    @Operation(summary = "Touch a device timestamp",
            description = "Stamps a device's updated_timestamp/updated_email so the UI's \"Updated Date\" refreshes after an edit.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Device touched"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PostMapping("/device/{device_id}/touch")
    @Transactional
    public void touch(
            @Parameter(description = "Device to stamp") @PathVariable("device_id") String deviceId,
            @Parameter(description = "Acting user, used as updated_email") @RequestParam(required = false) String username,
            @Parameter(description = "Owning VDMS id") @RequestParam(required = false) String vdmsid) {
        log.info("touch device_id={}", deviceId);
        String email = (username == null || username.isBlank()) ? "admin" : username;
        em.createNativeQuery("UPDATE device SET updated_timestamp = :t, updated_email = :u WHERE id = :id")
                .setParameter("t", System.currentTimeMillis())
                .setParameter("u", email)
                .setParameter("id", deviceId).executeUpdate();
        log.info("touched device {} (updated_timestamp/email) by {}", deviceId, email);
    }
}
