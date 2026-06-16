package io.sclera.Repository;

import io.sclera.dto.DeviceDTO;
import io.sclera.models.Device;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Selection;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Criteria implementation of the multi-join DeviceDTO projection pagination queries (previously
 * {@code @NamedNativeQuery} on Device, deferred because JPQL cannot express a multi-join DTO
 * projection). Wired as a Spring Data custom fragment, so DeviceRepository's public API is
 * unchanged. Behaviour-preserving vs the native SQL: same projection (user-data fallback for
 * name/vendor/model), same LEFT joins, same 'all'/'null' sentinel handling, same CONCAT_WS
 * contains-search, bound parameters.
 */
public class DeviceRepositoryImpl implements DeviceRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Set<DeviceDTO> getNetworkParentDeviceByPagination(Set<String> dockernames, Set<String> types,
                                                             String searchKey, Integer pagesize, Integer offset,
                                                             Set<String> virtual_device_types) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<DeviceDTO> cq = cb.createQuery(DeviceDTO.class);
        Root<Device> d = cq.from(Device.class);
        Join<?, ?> l = d.join("location", JoinType.LEFT);
        Join<?, ?> f = l.join("floor", JoinType.LEFT);
        Join<?, ?> b = f.join("building", JoinType.LEFT);
        cq.select(projection(cb, d, l, f, b));

        List<Predicate> ps = new ArrayList<>();
        if (dockernames != null && !dockernames.contains("all")) ps.add(d.get("docker_name").in(dockernames));
        if (types != null && !types.contains("all")) ps.add(d.get("type").in(types));
        addVirtualTypePredicate(cb, d, virtual_device_types, ps);
        addSearchPredicate(cb, d, l, f, b, searchKey, ps);
        if (!ps.isEmpty()) cq.where(ps.toArray(new Predicate[0]));
        return paginate(cq, pagesize, offset);
    }

    @Override
    public Set<DeviceDTO> getAllParentDeviceByPagination(String searchKey, Integer pagesize, Integer offset) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<DeviceDTO> cq = cb.createQuery(DeviceDTO.class);
        Root<Device> d = cq.from(Device.class);
        Join<?, ?> l = d.join("location", JoinType.LEFT);
        Join<?, ?> f = l.join("floor", JoinType.LEFT);
        Join<?, ?> b = f.join("building", JoinType.LEFT);
        cq.select(projection(cb, d, l, f, b));

        List<Predicate> ps = new ArrayList<>();
        addSearchPredicate(cb, d, l, f, b, searchKey, ps);
        if (!ps.isEmpty()) cq.where(ps.toArray(new Predicate[0]));
        return paginate(cq, pagesize, offset);
    }

    /** The 18-column parentdevicemapping projection, with user-data fallback for name/vendor/model. */
    private Selection<DeviceDTO> projection(CriteriaBuilder cb, From<?, ?> d,
                                            Join<?, ?> l, Join<?, ?> f, Join<?, ?> b) {
        return cb.construct(DeviceDTO.class,
                d.get("id"),
                userDataFallback(cb, d, "user_data_name", "display_name"),
                d.get("ip_address"),
                d.get("status"),
                d.get("type"),
                userDataFallback(cb, d, "user_data_vendor", "vendor"),
                d.get("mac_address"),
                l.get("name"),
                d.get("docker_name"),
                d.get("latitude"),
                d.get("longitude"),
                d.get("warranty"),
                b.get("name"),
                f.get("name"),
                userDataFallback(cb, d, "user_data_model", "model"),
                d.get("serial_number"),
                d.get("custom_fields"),
                d.get("asset_group"));
    }

    /** ('all' in set) -> no filter; else OR the requested virtual-type buckets (mirrors the CASE arms). */
    private void addVirtualTypePredicate(CriteriaBuilder cb, Root<Device> d, Set<String> vdts, List<Predicate> ps) {
        if (vdts == null || vdts.contains("all")) return;
        Expression<Integer> vdt = d.get("virtual_device_type");
        List<Predicate> arms = new ArrayList<>();
        if (vdts.contains("other")) arms.add(cb.equal(vdt, 2));
        if (vdts.contains("power_source")) arms.add(cb.or(cb.equal(vdt, 3), cb.equal(vdt, 4)));
        if (vdts.contains("ip")) arms.add(cb.or(cb.isNull(vdt), cb.equal(vdt, 0), cb.equal(vdt, 1)));
        ps.add(arms.isEmpty() ? cb.disjunction() : cb.or(arms.toArray(new Predicate[0])));
    }

    /** searchKey == null/'null' -> no filter; else CONCAT_WS(...) LIKE %term% (contains, case-sensitive). */
    private void addSearchPredicate(CriteriaBuilder cb, From<?, ?> d, Join<?, ?> l, Join<?, ?> f, Join<?, ?> b,
                                    String searchKey, List<Predicate> ps) {
        if (searchKey == null || "null".equals(searchKey)) return;
        Expression<String> hay = cb.function("concat_ws", String.class, cb.literal(""),
                d.get("display_name"), d.get("user_data_name"), d.get("ip_address"), d.get("mac_address"),
                l.get("name"), d.get("docker_name"), d.get("vendor"), d.get("user_data_vendor"),
                d.get("latitude"), d.get("longitude"), d.get("warranty"), b.get("name"), f.get("name"),
                d.get("model"), d.get("user_data_model"), d.get("serial_number"), d.get("custom_fields"),
                d.get("type"));
        ps.add(cb.like(hay, "%" + searchKey + "%"));
    }

    private Expression<String> userDataFallback(CriteriaBuilder cb, From<?, ?> d, String userField, String baseField) {
        return cb.<String>selectCase()
                .when(cb.or(cb.isNull(d.get(userField)), cb.equal(d.get(userField), "")), d.<String>get(baseField))
                .otherwise(d.<String>get(userField));
    }

    private Set<DeviceDTO> paginate(CriteriaQuery<DeviceDTO> cq, Integer pagesize, Integer offset) {
        TypedQuery<DeviceDTO> q = em.createQuery(cq);
        if (offset != null) q.setFirstResult(offset);
        if (pagesize != null) q.setMaxResults(pagesize);
        return new LinkedHashSet<>(q.getResultList());
    }
}
