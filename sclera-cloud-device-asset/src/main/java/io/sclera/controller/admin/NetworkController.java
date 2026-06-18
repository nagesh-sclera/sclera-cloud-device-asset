package io.sclera.controller.admin;

import io.sclera.service.UserActionLogService;
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
public class NetworkController {

    private static final Logger log = LoggerFactory.getLogger(NetworkController.class);

    @PersistenceContext
    private EntityManager em;

    private final UserActionLogService userActionLogService;

    public NetworkController(UserActionLogService userActionLogService) {
        this.userActionLogService = userActionLogService;
    }

    @GetMapping("/networks")
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> list(@RequestParam("vdms_id") String vdmsId) {
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

    @PostMapping("/network")
    @Transactional
    public Map<String, Object> add(@RequestParam("vdms_id") String vdmsId,
                                   @RequestParam(required = false) String username,
                                   @RequestBody Map<String, String> body) {
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

    @PostMapping("/device/{device_id}/network")
    @Transactional
    public void moveDevice(@PathVariable("device_id") String deviceId,
                           @RequestParam("docker_name") String dockerName,
                           @RequestParam(required = false) String vdms_id,
                           @RequestParam(required = false) String username) {
        int n = em.createNativeQuery("UPDATE device SET docker_name = :d, updated_timestamp = :t WHERE id = :id")
                .setParameter("d", dockerName)
                .setParameter("t", System.currentTimeMillis())
                .setParameter("id", deviceId).executeUpdate();
        log.info("moved device {} to network {} ({} rows updated)", deviceId, dockerName, n);
        userActionLogService.addUserAction(username, "asset", "UPDATE",
                "Device " + deviceId + " was moved to network " + dockerName, "success", "network", deviceId);
    }

    // Stamp updated_timestamp/updated_email — the editDeviceByDeviceID query does
    // not touch them, so the UI calls this after an edit to refresh "Updated Date".
    @PostMapping("/device/{device_id}/touch")
    @Transactional
    public void touch(@PathVariable("device_id") String deviceId,
                      @RequestParam(required = false) String username,
                      @RequestParam(required = false) String vdmsid) {
        String email = (username == null || username.isBlank()) ? "admin" : username;
        em.createNativeQuery("UPDATE device SET updated_timestamp = :t, updated_email = :u WHERE id = :id")
                .setParameter("t", System.currentTimeMillis())
                .setParameter("u", email)
                .setParameter("id", deviceId).executeUpdate();
        log.info("touched device {} (updated_timestamp/email) by {}", deviceId, email);
    }
}
