package io.sclera.Repository;

import com.alibaba.fastjson.JSONArray;
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
 * Criteria implementation of the multi-join DeviceDTO projection queries (previously
 * {@code @NamedNativeQuery} on Device — deferred because JPQL cannot express a multi-join DTO
 * projection). Wired as a Spring Data custom fragment so DeviceRepository's API is unchanged.
 * All project the 18-column parentdevicemapping (user-data fallback for name/vendor/model);
 * 'all'/'null' sentinels, virtual-device-type buckets, qr/nfc/barcode tag IN-filters, and the
 * CONCAT_WS contains-search are preserved; values are bound.
 */
public class DeviceRepositoryImpl implements DeviceRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    // ------------------------------------------------------------------ public API

    @Override
    public Set<DeviceDTO> getNetworkParentDeviceByPagination(Set<String> dockernames, Set<String> types,
                                                             String searchKey, Integer pagesize, Integer offset,
                                                             Set<String> virtual_device_types) {
        Ctx c = ctx();
        if (dockernames != null && !dockernames.contains("all")) c.ps.add(c.d.get("docker_name").in(dockernames));
        if (types != null && !types.contains("all")) c.ps.add(c.d.get("type").in(types));
        addVirtualType(c, list(virtual_device_types));
        if (searchKey != null && !"null".equals(searchKey)) c.ps.add(c.cb.like(plainHaystack(c), "%" + searchKey + "%"));
        return page(c, pagesize, offset);
    }

    @Override
    public Set<DeviceDTO> getAllParentDeviceByPagination(String searchKey, Integer pagesize, Integer offset) {
        Ctx c = ctx();
        if (searchKey != null && !"null".equals(searchKey)) c.ps.add(c.cb.like(plainHaystack(c), "%" + searchKey + "%"));
        return page(c, pagesize, offset);
    }

    @Override
    public List<DeviceDTO> listAlldevices() {
        Ctx c = ctx();
        Expression<Integer> vdt = c.d.get("virtual_device_type");
        c.ps.add(c.cb.or(c.cb.equal(vdt, 0), c.cb.equal(vdt, 1), c.cb.isNull(vdt)));
        c.cq.where(c.ps.toArray(new Predicate[0]));
        return em.createQuery(c.cq).getResultList();
    }

    @Override
    public Set<DeviceDTO> getAllNetworkParentDevices(JSONArray dockernames, JSONArray types, String searchkey,
                                                     JSONArray virtual_device_types, Boolean isTaggedToQrCode,
                                                     JSONArray deviceIdsTaggedToQrCode, Boolean isTaggedToNfc,
                                                     JSONArray deviceIdsTaggedToNfc) {
        Ctx c = ctx();
        c.ps.add(c.cb.notEqual(c.d.get("asset_match_status"), 3));
        addInFilter(c, "docker_name", list(dockernames));
        addInFilter(c, "type", list(types));
        addVirtualType(c, list(virtual_device_types));
        if (searchkey != null) c.ps.add(c.cb.like(plainHaystack(c), "%" + searchkey + "%"));
        addTagFilter(c, isTaggedToQrCode, list(deviceIdsTaggedToQrCode));
        // NOTE: the legacy native nfc arm switched on ?8 (the id list) rather than ?7 — an apparent
        // typo (a JSONArray can't be a boolean); the clear intent mirrors the qr arm, used here.
        addTagFilter(c, isTaggedToNfc, list(deviceIdsTaggedToNfc));
        c.cq.where(c.ps.toArray(new Predicate[0]));
        return new LinkedHashSet<>(em.createQuery(c.cq).getResultList());
    }

    @Override
    public Set<DeviceDTO> getAllNetworkParentDeviceByPagination(JSONArray dockernames, JSONArray types, String searchkey,
                                                                Integer pagesize, Integer offset,
                                                                JSONArray virtual_device_types, Boolean isTaggedToQrCode,
                                                                JSONArray deviceIdsTaggedToQrCode, Boolean isTaggedToNfc,
                                                                JSONArray deviceIdsTaggedToNfc, Boolean isTaggedToBarCode,
                                                                JSONArray deviceIdsTaggedToClientBarCode) {
        Ctx c = ctx();
        c.ps.add(c.cb.notEqual(c.d.get("asset_match_status"), 3));
        addInFilter(c, "docker_name", list(dockernames));
        addInFilter(c, "type", list(types));
        addVirtualType(c, list(virtual_device_types));
        // strip+lower search (caller passes a pre-sanitised key); guard is the legacy ?3 = 'null'
        if (searchkey != null && !"null".equals(searchkey)) {
            Expression<String> hay = c.cb.function("strip_specials", String.class, c.cb.lower(plainHaystack(c)));
            c.ps.add(c.cb.like(hay, "%" + searchkey + "%"));
        }
        addTagFilter(c, isTaggedToQrCode, list(deviceIdsTaggedToQrCode));
        addTagFilter(c, isTaggedToNfc, list(deviceIdsTaggedToNfc));
        addTagFilter(c, isTaggedToBarCode, list(deviceIdsTaggedToClientBarCode));
        return page(c, pagesize, offset);
    }

    // ------------------------------------------------------------------ shared building blocks

    /** A fresh query + root + LEFT joins + the standard projection + an empty predicate list. */
    private Ctx ctx() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<DeviceDTO> cq = cb.createQuery(DeviceDTO.class);
        Root<Device> d = cq.from(Device.class);
        Join<?, ?> l = d.join("location", JoinType.LEFT);
        Join<?, ?> f = l.join("floor", JoinType.LEFT);
        Join<?, ?> b = f.join("building", JoinType.LEFT);
        cq.select(projection(cb, d, l, f, b));
        return new Ctx(cb, cq, d, l, f, b);
    }

    private Set<DeviceDTO> page(Ctx c, Integer pagesize, Integer offset) {
        if (!c.ps.isEmpty()) c.cq.where(c.ps.toArray(new Predicate[0]));
        TypedQuery<DeviceDTO> q = em.createQuery(c.cq);
        if (offset != null) q.setFirstResult(offset);
        if (pagesize != null) q.setMaxResults(pagesize);
        return new LinkedHashSet<>(q.getResultList());
    }

    private void addInFilter(Ctx c, String attr, List<String> values) {
        if (values != null && !values.contains("all")) c.ps.add(c.d.get(attr).in(values));
    }

    /** ('all' in set) -> no filter; else OR the requested virtual-type buckets (mirrors the CASE arms). */
    private void addVirtualType(Ctx c, List<String> vdts) {
        if (vdts == null || vdts.contains("all")) return;
        Expression<Integer> vdt = c.d.get("virtual_device_type");
        List<Predicate> arms = new ArrayList<>();
        if (vdts.contains("other")) arms.add(c.cb.equal(vdt, 2));
        if (vdts.contains("power_source")) arms.add(c.cb.or(c.cb.equal(vdt, 3), c.cb.equal(vdt, 4)));
        if (vdts.contains("ip")) arms.add(c.cb.or(c.cb.isNull(vdt), c.cb.equal(vdt, 0), c.cb.equal(vdt, 1)));
        c.ps.add(arms.isEmpty() ? c.cb.disjunction() : c.cb.or(arms.toArray(new Predicate[0])));
    }

    /** (flag IS NULL) -> no filter; flag true -> id IN ids; flag false -> id NOT IN ids. */
    private void addTagFilter(Ctx c, Boolean flag, List<String> ids) {
        if (flag == null) return;
        Predicate in = c.d.get("id").in(ids);
        c.ps.add(flag ? in : in.not());
    }

    /** CONCAT_WS('', <the 18 searchable columns, raw>) — contains-search haystack. */
    private Expression<String> plainHaystack(Ctx c) {
        From<?, ?> d = c.d;
        return c.cb.function("concat_ws", String.class, c.cb.literal(""),
                d.get("display_name"), d.get("user_data_name"), d.get("ip_address"), d.get("mac_address"),
                c.l.get("name"), d.get("docker_name"), d.get("vendor"), d.get("user_data_vendor"),
                d.get("latitude"), d.get("longitude"), d.get("warranty"), c.b.get("name"), c.f.get("name"),
                d.get("model"), d.get("user_data_model"), d.get("serial_number"), d.get("custom_fields"),
                d.get("type"));
    }

    /** The 18-column parentdevicemapping projection, with user-data fallback for name/vendor/model. */
    private Selection<DeviceDTO> projection(CriteriaBuilder cb, From<?, ?> d, Join<?, ?> l, Join<?, ?> f, Join<?, ?> b) {
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

    private Expression<String> userDataFallback(CriteriaBuilder cb, From<?, ?> d, String userField, String baseField) {
        return cb.<String>selectCase()
                .when(cb.or(cb.isNull(d.get(userField)), cb.equal(d.get(userField), "")), d.<String>get(baseField))
                .otherwise(d.<String>get(userField));
    }

    private static List<String> list(JSONArray a) {
        if (a == null) return null;
        List<String> out = new ArrayList<>(a.size());
        for (Object o : a) out.add(String.valueOf(o));
        return out;
    }

    private static List<String> list(Set<String> s) {
        return s == null ? null : new ArrayList<>(s);
    }

    /** Per-query mutable context: builder, query, root + the three LEFT joins, predicate accumulator. */
    private static final class Ctx {
        final CriteriaBuilder cb;
        final CriteriaQuery<DeviceDTO> cq;
        final Root<Device> d;
        final Join<?, ?> l, f, b;
        final List<Predicate> ps = new ArrayList<>();

        Ctx(CriteriaBuilder cb, CriteriaQuery<DeviceDTO> cq, Root<Device> d, Join<?, ?> l, Join<?, ?> f, Join<?, ?> b) {
            this.cb = cb; this.cq = cq; this.d = d; this.l = l; this.f = f; this.b = b;
        }
    }
}
