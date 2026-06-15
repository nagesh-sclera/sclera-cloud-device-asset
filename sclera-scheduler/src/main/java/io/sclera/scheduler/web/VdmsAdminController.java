package io.sclera.scheduler.web;

import io.sclera.scheduler.domain.JobInstanceRepository;
import io.sclera.scheduler.domain.VdmsRegistryEntity;
import io.sclera.scheduler.domain.VdmsRegistryRepository;
import io.sclera.scheduler.service.PerVdmsRegistrar;
import io.sclera.scheduler.web.dto.VdmsRegistryView;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.ZoneId;
import java.util.List;

@RestController
@RequestMapping("/api/vdms")
public class VdmsAdminController {

    private final VdmsRegistryRepository registry;
    private final JobInstanceRepository instances;
    private final PerVdmsRegistrar registrar;

    public VdmsAdminController(VdmsRegistryRepository registry, JobInstanceRepository instances,
                               PerVdmsRegistrar registrar) {
        this.registry = registry;
        this.instances = instances;
        this.registrar = registrar;
    }

    @GetMapping
    public List<VdmsRegistryView> list() {
        return registry.findAll().stream().map(this::toView).toList();
    }

    @GetMapping("/timezones")
    public List<String> timezones() {
        return ZoneId.getAvailableZoneIds().stream().sorted().toList();
    }

    // ---- helpers (used by write endpoints in later tasks) ----

    private VdmsRegistryView toView(VdmsRegistryEntity v) {
        return new VdmsRegistryView(v.getVdmsId(), v.getTimezone(), v.isActive(),
                instances.countByVdmsId(v.getVdmsId()));
    }

    private String validTimezone(String tz) {
        try {
            return ZoneId.of(tz == null ? "" : tz.trim()).getId();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid timezone: " + tz);
        }
    }

    private String requireVdmsId(String vdmsId) {
        String v = vdmsId == null ? "" : vdmsId.trim();
        if (v.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "vdmsId must not be blank");
        return v;
    }

    public record AddVdmsRequest(String vdmsId, String timezone) {}

    @PostMapping
    public org.springframework.http.ResponseEntity<VdmsRegistryView> add(@RequestBody AddVdmsRequest req) {
        String vdmsId = requireVdmsId(req.vdmsId());
        String tz = validTimezone(req.timezone());
        registry.findById(vdmsId).ifPresent(v -> {
            if (v.isActive())
                throw new ResponseStatusException(HttpStatus.CONFLICT, "VDMS already active: " + vdmsId);
        });
        registrar.onVdmsActivated(vdmsId, tz);
        return org.springframework.http.ResponseEntity.status(HttpStatus.CREATED)
                .body(toView(registry.findById(vdmsId).orElseThrow()));
    }

    public record TimezoneRequest(String timezone) {}

    @PutMapping("/{vdmsId}/timezone")
    public VdmsRegistryView editTimezone(@PathVariable String vdmsId, @RequestBody TimezoneRequest req) {
        if (registry.findById(vdmsId).isEmpty())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "no such VDMS: " + vdmsId);
        String tz = validTimezone(req.timezone());
        registrar.reregister(vdmsId, tz);
        return toView(registry.findById(vdmsId).orElseThrow());
    }
}
