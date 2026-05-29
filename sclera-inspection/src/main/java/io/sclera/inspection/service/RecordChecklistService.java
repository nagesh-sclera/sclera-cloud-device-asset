package io.sclera.inspection.service;

import io.sclera.inspection.model.RecordChecklist;
import io.sclera.inspection.repository.RecordChecklistRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RecordChecklistService {

    private static final Logger log = LoggerFactory.getLogger(RecordChecklistService.class);

    @Autowired
    private RecordChecklistRepository repo;

    /** Root prefix images must live under (safety check; prevents path-traversal). */
    @Value("${sclera.checklist.image-root:/tmp/sclera/images}")
    private String imageRoot;

    public List<RecordChecklist> listAll() { return repo.findAll(); }
    public Optional<RecordChecklist> findById(String id) { return repo.findById(id); }
    public RecordChecklist save(RecordChecklist e) { return repo.save(e); }
    public void deleteById(String id) { repo.deleteById(id); }

    public long countByDeviceId(String deviceId) { return repo.countByDeviceId(deviceId); }

    public void updateDeviceAndIsRemoved(Collection<String> ids) {
        if (ids == null || ids.isEmpty()) return;
        repo.softDeleteByIds(ids);
    }

    public void deleteInBatch(Collection<String> ids) {
        if (ids == null || ids.isEmpty()) return;
        repo.deleteAllById(ids);
    }

    /** Hard-delete all record-checklist rows for a device; returns the collected image URLs. */
    public List<String> deleteAllByDeviceId(String deviceId) {
        if (deviceId == null) return new ArrayList<>();
        List<String> imageCsvs = repo.findImageUrlsByDeviceId(deviceId);
        List<String> urls = imageCsvs.stream()
                .filter(s -> s != null && !s.isBlank())
                .flatMap(s -> Arrays.stream(s.split(",")))
                .map(String::trim).filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
        repo.deleteByDeviceId(deviceId);
        return urls;
    }

    public String getStatusByDeviceId(String deviceId, String filter) {
        if (deviceId == null) return null;
        List<String> statuses = repo.findStatusesByDeviceId(deviceId);
        return statuses.isEmpty() ? null : statuses.get(0);
    }

    public long getStatusCountByDeviceId(String deviceId, String status) {
        if (deviceId == null) return 0;
        if (status == null) return repo.countByDeviceId(deviceId);
        return repo.countByDeviceIdAndStatus(deviceId, status);
    }

    public void updateRecordChecklistByDeviceId(Collection<String> ids, String oldId, String newId) {
        if (ids == null || ids.isEmpty() || newId == null) return;
        if (oldId == null) {
            repo.reassignDeviceIdForIdsNoOld(ids, newId);
        } else {
            repo.reassignDeviceIdForIds(ids, oldId, newId);
        }
    }

    public String getStatusByLocationId(String locationId, String filter) {
        if (locationId == null) return null;
        List<String> statuses = repo.findStatusesByLocationId(locationId);
        return statuses.isEmpty() ? null : statuses.get(0);
    }

    public long getStatusCountByLocationId(String locationId, String status) {
        if (locationId == null) return 0;
        if (status == null) return 0;
        return repo.countByLocationIdAndStatus(locationId, status);
    }

    public void updateLocationAndIsRemoved(Collection<String> locationIds) {
        if (locationIds == null || locationIds.isEmpty()) return;
        repo.softDeleteByLocationIds(locationIds);
    }

    public void deleteByLocationId(String locationId) {
        if (locationId == null) return;
        repo.deleteByLocationId(locationId);
    }

    public List<String> deleteAllByLocationId(String locationId) {
        if (locationId == null) return new ArrayList<>();
        List<String> ids = repo.findIdsByLocationId(locationId);
        repo.deleteByLocationId(locationId);
        return ids;
    }

    /** Stamp updated_email on all record-checklist rows. */
    public void updateRecordChecklist(String email) {
        if (email == null) return;
        repo.stampUpdatedEmail(email);
    }

    /** Filter by building/floor/location columns. */
    public Set<String> getAllByBuildings(Collection<String> buildingIds, Collection<String> floorIds, Collection<String> locationIds) {
        Collection<String> b = (buildingIds == null || buildingIds.isEmpty()) ? null : buildingIds;
        Collection<String> f = (floorIds == null || floorIds.isEmpty()) ? null : floorIds;
        Collection<String> l = (locationIds == null || locationIds.isEmpty()) ? null : locationIds;
        if (b == null && f == null && l == null) return Collections.emptySet();
        return new HashSet<>(repo.findIdsByBuildingsFloorsLocations(b, f, l));
    }

    /**
     * Delete image files referenced by the given URLs. Only deletes paths that resolve under {@link #imageRoot}
     * (safety check to prevent path-traversal). URLs that are remote (http/https) or escape the root are skipped.
     */
    public void deleteImagesByUrls(Collection<String> urls) {
        if (urls == null || urls.isEmpty()) return;
        Path root;
        try { root = Paths.get(imageRoot).toAbsolutePath().normalize(); }
        catch (Exception e) { log.warn("invalid imageRoot {}: {}", imageRoot, e.toString()); return; }
        for (String url : urls) {
            if (url == null || url.isBlank()) continue;
            try {
                Path target = resolveLocalPath(url, root);
                if (target == null) continue;
                if (!target.normalize().startsWith(root)) {
                    log.warn("skip image outside root: {}", target);
                    continue;
                }
                Files.deleteIfExists(target);
            } catch (Exception e) {
                log.warn("failed to delete image {}: {}", url, e.toString());
            }
        }
    }

    /** Map a stored URL/path to a local file path under the image root. Returns null for remote URLs. */
    private Path resolveLocalPath(String url, Path root) throws URISyntaxException {
        String trimmed = url.trim();
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            URI uri = new URI(trimmed);
            String p = uri.getPath();
            if (p == null) return null;
            return root.resolve(p.startsWith("/") ? p.substring(1) : p).normalize();
        }
        if (trimmed.startsWith("/")) {
            // Absolute filesystem path — only honour it if it's under root
            Path abs = Paths.get(trimmed).normalize();
            return abs;
        }
        return root.resolve(trimmed).normalize();
    }
}