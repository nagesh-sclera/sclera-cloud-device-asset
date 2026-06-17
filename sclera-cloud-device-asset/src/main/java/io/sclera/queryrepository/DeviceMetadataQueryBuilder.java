package io.sclera.queryrepository;

import io.sclera.models.Device;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Type-safe Criteria replacement for the dynamic native SQL behind the asset-filter dropdown
 * lookups (DeviceMetadataController): the distinct, non-empty values of a whitelisted device
 * column, excluding archived devices (asset_match_status = 3), optionally scoped to a VDMS.
 *
 * <p>The column was previously concatenated into a native SQL string; here it selects a metamodel
 * attribute instead (still guarded by the whitelist), so nothing is inlined and the VDMS scope is
 * a bound parameter.</p>
 */
@Component
public class DeviceMetadataQueryBuilder {

    /** Columns allowed for the distinct lookup. */
    private static final Set<String> ALLOWED = Set.of("type", "asset_group", "category");

    private final EntityManager em;

    public DeviceMetadataQueryBuilder(EntityManager em) { this.em = em; }

    /** Distinct non-empty values of {@code col}, archived excluded, ordered ascending. */
    public List<String> distinctColumnValues(String col, String vdmsId) {
        if (!ALLOWED.contains(col)) return List.of();

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<String> cq = cb.createQuery(String.class);
        Root<Device> d = cq.from(Device.class);
        Expression<String> column = d.get(col);
        cq.select(column).distinct(true);

        List<Predicate> ps = new ArrayList<>();
        ps.add(cb.isNotNull(column));
        ps.add(cb.notEqual(column, ""));
        Expression<Integer> ams = d.get("asset_match_status");
        ps.add(cb.or(cb.isNull(ams), cb.notEqual(ams, 3)));
        if (vdmsId != null && !vdmsId.isBlank()) {
            ps.add(cb.equal(d.get("docker_vdms_id"), vdmsId));
        }
        cq.where(ps.toArray(new Predicate[0]));
        cq.orderBy(cb.asc(column));
        return em.createQuery(cq).getResultList();
    }
}
