package io.sclera.vdms.controller;

import io.sclera.vdms.dto.VdmsDTO;
import io.sclera.vdms.model.UserActionLog;
import io.sclera.vdms.model.Vdms;
import io.sclera.vdms.repository.UserActionLogRepository;
import io.sclera.vdms.repository.VdmsJpaRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@CrossOrigin(origins = "*")
@RestController
public class VdmsController {

    private final VdmsJpaRepository repo;
    private final UserActionLogRepository auditRepo;

    public VdmsController(VdmsJpaRepository repo, UserActionLogRepository auditRepo) {
        this.repo = repo;
        this.auditRepo = auditRepo;
    }

    @GetMapping("/vdms/id")
    public ResponseEntity<Map<String, String>> getVdmsId() {
        Optional<Vdms> opt = repo.findFirst();
        return opt.map(v -> ResponseEntity.ok(Map.of("vdmsId", v.getId())))
                  .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/vdms/details")
    public ResponseEntity<VdmsDTO> getDetails() {
        return repo.findFirst().map(v -> ResponseEntity.ok(toFullDto(v)))
                   .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/vdms/master")
    public ResponseEntity<Map<String, Integer>> getMaster() {
        return repo.findFirst()
                   .map(v -> ResponseEntity.ok(Map.of("isMaster", v.getIs_master() != null ? v.getIs_master() : 0)))
                   .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/vdms/has-secondary-device")
    public ResponseEntity<Map<String, Integer>> getHasSecondaryDevice() {
        return repo.findFirst()
                   .map(v -> ResponseEntity.ok(Map.of("hasSecondaryDevice", v.getHas_secondary_device() != null ? v.getHas_secondary_device() : 0)))
                   .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/vdms/secondary-device-id")
    public ResponseEntity<Map<String, String>> getSecondaryDeviceId() {
        Optional<Vdms> opt = repo.findFirst();
        if (!opt.isPresent()) return ResponseEntity.notFound().build();
        String secId = opt.get().getSecondary_device_id();
        return ResponseEntity.ok(Map.of("secondaryDeviceId", secId != null ? secId : ""));
    }

    @GetMapping("/vdms/customer-org-id/{vdmsId}")
    public ResponseEntity<Map<String, String>> getCustomerOrgId(@PathVariable String vdmsId) {
        Optional<Vdms> opt = repo.findById(vdmsId);
        if (!opt.isPresent()) return ResponseEntity.notFound().build();
        String orgId = opt.get().getCustomer_org_id();
        return ResponseEntity.ok(Map.of("customerOrgId", orgId != null ? orgId : ""));
    }

    @GetMapping("/vdms/sync-details-for-adc")
    public ResponseEntity<VdmsDTO> getSyncDetailsForAdc() {
        Optional<Vdms> opt = repo.findFirst();
        if (!opt.isPresent()) return ResponseEntity.notFound().build();
        Vdms v = opt.get();
        VdmsDTO dto = new VdmsDTO();
        dto.setId(v.getId());
        dto.setCustomer_org_id(v.getCustomer_org_id());
        dto.setAdc_configuration_id(v.getAdc_configuration_id());
        dto.setZip(v.getZip());
        return ResponseEntity.ok(dto);
    }

    /** GET /vdms/active → [{ "vdmsId": ..., "timezone": ... }] for all active VDMS.
     *  Consumed by the scheduler's startup-sync (Dapr service-invocation). */
    @GetMapping("/vdms/active")
    public List<Map<String, String>> getActive() {
        return repo.findAllActive().stream()
            .map(v -> Map.of(
                "vdmsId", v.getId() != null ? v.getId() : "",
                "timezone", v.getTimezone() != null ? v.getTimezone() : ""))
            .toList();
    }

    /**
     * GET /vdms/audit-log?vdmsId={vdmsId}&page=0&size=20
     *
     * Returns device audit log entries for a VDMS instance, newest first.
     * size is capped at 100.
     */
    @GetMapping("/vdms/audit-log")
    public List<UserActionLog> getAuditLog(
            @RequestParam(required = false) String vdmsId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        int safeSize = Math.min(size, 100);
        Pageable pageable = PageRequest.of(page, safeSize);
        var p = (vdmsId == null || vdmsId.isBlank())
            ? auditRepo.findAllByOrderByCreatedAtDesc(pageable)
            : auditRepo.findByVdmsIdOrderByCreatedAtDesc(vdmsId, pageable);
        return p != null ? p.getContent() : List.of();
    }

    private VdmsDTO toFullDto(Vdms v) {
        VdmsDTO dto = new VdmsDTO();
        dto.setId(v.getId()); dto.setProperty_name(v.getProperty_name());
        dto.setActivation_status(v.getActivation_status()); dto.setStatus(v.getStatus());
        dto.setLocation(v.getLocation()); dto.setTimezone(v.getTimezone());
        dto.setActivation_timestamp(v.getActivation_timestamp()); dto.setDeployment_type(v.getDeployment_type());
        dto.setAddress(v.getAddress()); dto.setCity(v.getCity());
        dto.setCountry(v.getCountry()); dto.setState(v.getState());
        dto.setZip(v.getZip()); dto.setImage_url(v.getImage_url());
        dto.setLatitude(v.getLatitude()); dto.setLongitude(v.getLongitude());
        dto.setRegion(v.getRegion()); dto.setCustomer_org_id(v.getCustomer_org_id());
        dto.setAdc_configuration_id(v.getAdc_configuration_id()); dto.setIs_master(v.getIs_master());
        dto.setHas_secondary_device(v.getHas_secondary_device()); dto.setSecondary_device_id(v.getSecondary_device_id());
        dto.setMaster_ip(v.getMaster_ip()); dto.setSlave_ip(v.getSlave_ip());
        return dto;
    }
}
