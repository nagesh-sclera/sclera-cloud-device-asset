package io.sclera.queryrepository;

import io.sclera.dto.DeviceSearchCriteria;
import io.sclera.dto.DeviceSearchCriteria.Cond;
import io.sclera.dto.DeviceSearchCriteria.KeywordSearch;
import io.sclera.models.Device;
import io.sclera.models.DeviceSpecification;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.AbstractQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.hibernate.query.criteria.JpaFrom;
import org.hibernate.query.sqm.tree.SqmJoinType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Type-safe Criteria replacement for the string-built SQL of
 * DeviceSearchService.multipleKeywordSearchSortFilterDevices / ...ForAssetExport / ...Count.
 *
 * Query shape mirrors the original: outer query on Device selecting d.id with
 * "d.id IN (subquery carrying all filter/search predicates and joins)", preserving the
 * dedupe-then-sort behaviour of the legacy SQL. The Count variant counts the same subquery.
 *
 * Documented behaviour fixes vs the legacy SQL (see spec 2026-06-12):
 *  - values are BOUND (no SQL injection; device_ids double-quote PG bug gone);
 *  - REGEXP_REPLACE carries 'g' (legacy PG port stripped only the first special char);
 *  - sorting by created/updated_timestamp no longer emits bigint = '' (PG type error);
 *  - sorting by assignee_email/username/email now joins what it references (legacy outer
 *    query referenced dos./ds. aliases it never joined -> SQL error).
 */
@Component
public class DeviceSearchQueryBuilder {

    /** Java-side analog of strip_specials — identical class as the legacy replaceAll. */
    private static final String JAVA_SPECIALS = "[ -.!\t_+#~`@$%^&*()=;:<>?,/{}|\\\\]";

    private static final String[] SENSOR_STATUS_FIELDS = {
            "monnit_status", "pelican_status", "knx_status", "snmp_object_status",
            "daintree_status", "ecobee_status", "bacnet_status", "lorawan_status",
            "my_devices_status", "measuring_instrument_status", "disruptive_status"};

    private final EntityManager em;

    public DeviceSearchQueryBuilder(EntityManager em) { this.em = em; }

    // ------------------------------------------------------------------ public API

    public List<String> findIds(DeviceSearchCriteria c, int pageNo, int pageSize) {
        TypedQuery<String> q = buildIdQuery(c);
        q.setFirstResult(pageSize * (pageNo - 1));
        q.setMaxResults(pageSize);
        return q.getResultList();
    }

    public List<String> findAllIds(DeviceSearchCriteria c) {
        return buildIdQuery(c).getResultList();
    }

    public long count(DeviceSearchCriteria c) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<Device> d = cq.from(Device.class);
        cq.select(cb.count(d.get("id")));
        cq.where(d.get("id").in(matchingIds(cb, cq, c)));
        return em.createQuery(cq).getSingleResult();
    }

    // ------------------------------------------------------------------ split search/sort/filter family
    // Type-safe replacement for the older split DeviceSearchService.searchDevices / sortDevices /
    // filterDevices / getDeviceInfoByCustomFields string SQL. These share the same scope+condition
    // block as the merged query (decoded via DeviceSearchCriteria) but carry their OWN search/sort/
    // filter semantics (contains-only, no strip; UNION rendered as OR; jsonb array text for search;
    // raw-column sort). Flat queries (all joins are to-one), values BOUND (no injection).

    /** column==null means search-all (custom-fields array OR the standard concat haystack). */
    public record SplitSearch(String column, boolean custom, String value) {}
    public record SplitSort(String column, boolean custom) {}
    public record SplitFilter(String column, boolean custom) {}

    /** searchDevices: scope + the contains search; no SQL ORDER BY (caller fuzzy-ranks). */
    public List<String> searchDeviceIds(DeviceSearchCriteria scope, SplitSearch s, int pageNo, int pageSize) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<String> cq = cb.createQuery(String.class);
        Root<Device> d = cq.from(Device.class);
        cq.select(d.get("id")).distinct(true);
        Ctx ctx = new Ctx(cb, d);
        List<Predicate> ps = new ArrayList<>();
        addScopeAndConditionPredicates(cb, ctx, scope, ps);
        ps.add(splitSearchPredicate(cb, ctx, s));
        cq.where(ps.toArray(new Predicate[0]));
        return paginate(em.createQuery(cq), pageNo, pageSize);
    }

    /** sortDevices: scope + the legacy null-last ordering (ip_address via inet, custom via jsonpath). */
    public List<String> sortDeviceIds(DeviceSearchCriteria scope, SplitSort s, int pageNo, int pageSize) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<String> cq = cb.createQuery(String.class);
        Root<Device> d = cq.from(Device.class);
        cq.select(d.get("id"));
        Ctx ctx = new Ctx(cb, d);
        List<Predicate> ps = new ArrayList<>();
        addScopeAndConditionPredicates(cb, ctx, scope, ps);
        cq.where(ps.toArray(new Predicate[0]));
        cq.orderBy(splitOrders(cb, ctx, s));
        return paginate(em.createQuery(cq), pageNo, pageSize);
    }

    /** filterDevices: scope + each column required present (IS NOT NULL AND <> ''). */
    public List<String> filterDeviceIds(DeviceSearchCriteria scope, List<SplitFilter> filters,
                                        int pageNo, int pageSize) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<String> cq = cb.createQuery(String.class);
        Root<Device> d = cq.from(Device.class);
        cq.select(d.get("id"));
        Ctx ctx = new Ctx(cb, d);
        List<Predicate> ps = new ArrayList<>();
        addScopeAndConditionPredicates(cb, ctx, scope, ps);
        for (SplitFilter f : filters) ps.add(splitFilterPredicate(cb, ctx, f));
        cq.where(ps.toArray(new Predicate[0]));
        return paginate(em.createQuery(cq), pageNo, pageSize);
    }

    /** getDeviceInfoByCustomFields: vdms/docker scope only (no condition) + a single custom-field match. */
    public List<String> customFieldDeviceIds(String vdmsid, String dockername, String key,
                                             String value, int limit) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<String> cq = cb.createQuery(String.class);
        Root<Device> d = cq.from(Device.class);
        cq.select(d.get("id"));
        List<Predicate> ps = new ArrayList<>();
        if (vdmsid != null && !"null".equals(vdmsid)) ps.add(cb.equal(d.get("docker_vdms_id"), vdmsid));
        if (dockername != null && !"all".equals(dockername)) ps.add(cb.equal(d.get("docker_name"), dockername));
        Expression<String> arr = cb.function("custom_field_array_text", String.class,
                d.get("custom_fields"), cb.literal(jsonPathFor(key)));
        ps.add(cb.like(arr, "%" + value + "%"));
        cq.where(ps.toArray(new Predicate[0]));
        TypedQuery<String> q = em.createQuery(cq);
        q.setMaxResults(limit);
        return q.getResultList();
    }

    private List<String> paginate(TypedQuery<String> q, int pageNo, int pageSize) {
        q.setFirstResult(pageSize * (pageNo - 1));
        q.setMaxResults(pageSize);
        return q.getResultList();
    }

    private Predicate splitSearchPredicate(CriteriaBuilder cb, Ctx ctx, SplitSearch s) {
        From<?, Device> d = ctx.d;
        String like = "%" + s.value() + "%";
        if (s.column() == null) {
            // UNION rendered as OR: custom-fields array text match OR the standard concat haystack
            // (no lower/strip — contains-only, faithful to the legacy split search).
            Expression<String> customArr = cb.function("custom_field_array_text", String.class,
                    d.get("custom_fields"), cb.literal("$[*].*"));
            Expression<String> concat = cb.function("concat_ws", String.class, cb.literal(""),
                    d.get("id"),
                    userDataFallback(cb, d, "user_data_name", "display_name"),
                    userDataFallback(cb, d, "user_data_vendor", "vendor"),
                    userDataFallback(cb, d, "user_data_model", "model"),
                    d.get("type"), d.get("ip_address"), d.get("mac_address"),
                    d.get("latitude"), d.get("longitude"), d.get("serial_number"), d.get("warranty"),
                    ctx.location().get("name"), ctx.floor().get("name"), ctx.building().get("name"));
            return cb.or(cb.like(customArr, like), cb.like(concat, like));
        }
        if (s.custom()) {
            Expression<String> arr = cb.function("custom_field_array_text", String.class,
                    d.get("custom_fields"), cb.literal(jsonPathFor(s.column())));
            return cb.like(arr, like);
        }
        return cb.like(resolveString(cb, ctx, s.column()), like);
    }

    private List<Order> splitOrders(CriteriaBuilder cb, Ctx ctx, SplitSort s) {
        List<Order> orders = new ArrayList<>();
        if (s.custom()) {
            Expression<String> v = customFieldText(cb, ctx, s.column());
            orders.add(cb.asc(cb.selectCase().when(cb.or(cb.isNull(v), cb.equal(v, "")), 1).otherwise(0)));
            orders.add(cb.asc(v));
            return orders;
        }
        if ("ip_address".equals(s.column())) {
            Expression<String> ip = ctx.d.get("ip_address");
            orders.add(nullsLast(cb, ip));
            orders.add(cb.asc(cb.function("inet_val", String.class, ip)));
            return orders;
        }
        Expression<?> col = resolveRaw(cb, ctx, s.column());
        orders.add(nullsLast(cb, col));
        orders.add(cb.asc(col));
        return orders;
    }

    private Predicate splitFilterPredicate(CriteriaBuilder cb, Ctx ctx, SplitFilter f) {
        Expression<String> col = f.custom()
                ? customFieldText(cb, ctx, f.column())
                : resolveString(cb, ctx, f.column());
        return cb.and(cb.isNotNull(col), cb.notEqual(col, ""));
    }

    /** Like resolveString but keeps timestamp columns numeric (legacy sort ordered the raw column). */
    private Expression<?> resolveRaw(CriteriaBuilder cb, Ctx ctx, String column) {
        return switch (column == null ? "" : column) {
            case "created_timestamp" -> ctx.d.get("created_timestamp");
            case "updated_timestamp" -> ctx.d.get("updated_timestamp");
            default -> resolveString(cb, ctx, column);
        };
    }

    // ------------------------------------------------------------------ outer query

    private TypedQuery<String> buildIdQuery(DeviceSearchCriteria c) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<String> cq = cb.createQuery(String.class);
        Root<Device> d = cq.from(Device.class);
        cq.select(d.get("id"));
        cq.where(d.get("id").in(matchingIds(cb, cq, c)));
        cq.orderBy(buildOrders(cb, new Ctx(cb, d), c));
        return em.createQuery(cq);
    }

    /** Subquery selecting matching device ids, carrying every filter/search predicate. */
    private Subquery<String> matchingIds(CriteriaBuilder cb, AbstractQuery<?> parent,
                                         DeviceSearchCriteria c) {
        Subquery<String> sub = parent.subquery(String.class);
        Root<Device> d = sub.from(Device.class);
        sub.select(d.get("id"));
        Ctx ctx = new Ctx(cb, d);

        List<Predicate> ps = new ArrayList<>();
        addScopeAndConditionPredicates(cb, ctx, c, ps);
        addColumnFilterPredicates(cb, ctx, c, ps, sub);
        addFeatureFilterPredicates(cb, ctx, c, ps, sub);
        addSearchPredicates(cb, ctx, c, ps);
        sub.where(ps.toArray(new Predicate[0]));
        return sub;
    }

    // ------------------------------------------------------------------ scope + condition

    private void addScopeAndConditionPredicates(CriteriaBuilder cb, Ctx ctx,
                                                DeviceSearchCriteria c, List<Predicate> ps) {
        From<?, Device> d = ctx.d;
        if (c.getVdmsId() != null) ps.add(cb.equal(d.get("docker_vdms_id"), c.getVdmsId()));
        if (c.getDockerName() != null) ps.add(cb.equal(d.get("docker_name"), c.getDockerName()));

        if (c.isOnboardStatusNot3()) {
            // preserved legacy semantics: NULL onboard_status does NOT match "!= 3"
            ps.add(cb.notEqual(d.get("onboard_status"), 3));
        } else if (c.getOnboardStatusEquals() != null) {
            ps.add(cb.equal(d.get("onboard_status"), c.getOnboardStatusEquals()));
        }

        if (c.isVirtualOther()) {
            Expression<Integer> vdt = d.get("virtual_device_type");
            ps.add(cb.and(cb.isNotNull(vdt), cb.notEqual(vdt, 0), cb.notEqual(vdt, 1)));
        }

        if (c.getStatus() != null) ps.add(cb.equal(d.get("status"), c.getStatus()));

        if (c.getMonitor() != null) {
            Expression<Integer> m = d.get("monitor");
            if (c.getMonitor() == 1) ps.add(cb.equal(m, 1));
            else ps.add(cb.or(cb.isNull(m), cb.equal(m, c.getMonitor())));
        }

        if (c.getAssignedStatus() != null) {
            Expression<String> aue = d.get("assigned_user_email");
            if (c.getAssignedStatus() == 0) {
                ps.add(cb.or(cb.isNull(aue), cb.equal(aue, "null")));
            } else {
                // preserved verbatim from legacy SQL (OR of the two arms)
                ps.add(cb.or(cb.isNotNull(aue), cb.notEqual(aue, "null")));
            }
        }

        Expression<Integer> ams = d.get("asset_match_status");
        if (c.getAssetMatchStatus() == null) {
            ps.add(cb.notEqual(ams, 3));
        } else if (c.getAssetMatchStatus() == 3) {
            ps.add(cb.equal(ams, 3));
        } else {
            ps.add(cb.and(cb.equal(ams, c.getAssetMatchStatus()), cb.notEqual(ams, 3)));
        }

        if (!c.getDeviceIds().isEmpty()) {
            ps.add(ctx.d.get("id").in(c.getDeviceIds()));
        }
    }

    // ------------------------------------------------------------------ column filters

    private void addColumnFilterPredicates(CriteriaBuilder cb, Ctx ctx, DeviceSearchCriteria c,
                                           List<Predicate> ps, AbstractQuery<?> sub) {
        for (DeviceSearchCriteria.ColumnFilter f : c.getColumnFilters()) {
            if (f.custom()) {
                Expression<String> v = customFieldText(cb, ctx, f.column());
                if ("is_present".equals(f.condition())) {
                    ps.add(cb.and(cb.isNotNull(v), cb.notEqual(v, ""), cb.notEqual(v, "null")));
                } else if ("is_not_present".equals(f.condition())) {
                    ps.add(cb.or(cb.isNull(v), cb.equal(v, ""), cb.equal(v, "null")));
                }
                continue;
            }
            boolean present = "is_present".equals(f.condition());
            if (!present && !"is_not_present".equals(f.condition())) continue; // legacy: other values ignored
            Object value = f.value();
            if (value != null) {
                ps.add(columnValuePredicate(cb, ctx, f.column(), value, present));
            } else if (present) {
                Expression<String> col = resolveString(cb, ctx, f.column());
                ps.add(cb.and(cb.isNotNull(col), cb.notEqual(col, "")));
            } else {
                Expression<String> col = resolveString(cb, ctx, f.column());
                ps.add(cb.or(cb.isNull(col), cb.equal(col, "")));
            }
        }
    }

    @SuppressWarnings("unchecked")
    private Predicate columnValuePredicate(CriteriaBuilder cb, Ctx ctx, String column,
                                           Object value, boolean present) {
        switch (column) {
            case "assignee_email" -> {
                // legacy: only the is_present arm special-cases assignee_email
                if (present) {
                    return cb.or(cb.equal(ctx.dos().get("assignee_email"), String.valueOf(value)),
                                 cb.equal(ctx.dosa().get("email"), String.valueOf(value)));
                }
                return cb.notEqual(resolveString(cb, ctx, column), String.valueOf(value));
            }
            case "type", "asset_group" -> {
                List<String> values = new ArrayList<>();
                for (Object o : (com.alibaba.fastjson.JSONArray) value) values.add(String.valueOf(o));
                Predicate in = resolveString(cb, ctx, column).in(values);
                return present ? in : in.not();
            }
            case "category" -> {
                com.alibaba.fastjson.JSONObject categories = (com.alibaba.fastjson.JSONObject) value;
                List<Predicate> clauses = new ArrayList<>();
                for (String cat : categories.keySet()) {
                    com.alibaba.fastjson.JSONArray subs = categories.getJSONArray(cat);
                    Predicate p = cb.equal(ctx.d.get("category"), cat);
                    if (subs != null && !subs.isEmpty()) {
                        List<String> subList = new ArrayList<>();
                        for (Object o : subs) subList.add(String.valueOf(o));
                        Predicate subIn = ctx.d.<String>get("sub_category").in(subList);
                        p = cb.and(p, present ? subIn : subIn.not());
                    }
                    clauses.add(p);
                }
                return cb.or(clauses.toArray(new Predicate[0]));
            }
            case "assigned_user_email" -> {
                return present ? cb.equal(ctx.d.get("assigned_user_email"), String.valueOf(value))
                               : cb.notEqual(ctx.d.get("assigned_user_email"), String.valueOf(value));
            }
            case "os_type" -> {
                // legacy special-cases os_type only in the is_present arm; keep symmetric (= / <>)
                return present ? cb.equal(ctx.ds().get("osType"), String.valueOf(value))
                               : cb.notEqual(ctx.ds().get("osType"), String.valueOf(value));
            }
            default -> {
                Expression<String> col = resolveString(cb, ctx, column);
                return present ? cb.equal(col, String.valueOf(value))
                               : cb.notEqual(col, String.valueOf(value));
            }
        }
    }

    /**
     * Criteria analog of updateDeviceSearchColumnName: maps a UI column name to a STRING
     * expression. Non-string columns (timestamps) are stringified via concat_ws so
     * comparisons/LIKEs behave like the legacy implicit text cast.
     */
    private Expression<String> resolveString(CriteriaBuilder cb, Ctx ctx, String column) {
        From<?, Device> d = ctx.d;
        return switch (column) {
            case "id" -> d.get("id");
            case "display_name" -> userDataFallback(cb, d, "user_data_name", "display_name");
            case "vendor" -> userDataFallback(cb, d, "user_data_vendor", "vendor");
            case "model" -> userDataFallback(cb, d, "user_data_model", "model");
            case "type" -> d.get("type");
            case "ip_address" -> d.get("ip_address");
            case "mac_address" -> d.get("mac_address");
            case "location" -> ctx.location().get("name");
            case "floor" -> ctx.floor().get("name");
            case "building" -> ctx.building().get("name");
            case "warranty" -> d.get("warranty");
            case "latitude" -> d.get("latitude");
            case "longitude" -> d.get("longitude");
            case "serial_number" -> d.get("serial_number");
            case "created_timestamp" -> asText(cb, d.get("created_timestamp"));
            case "updated_timestamp" -> asText(cb, d.get("updated_timestamp"));
            case "assignee_email" -> ctx.dos().get("assignee_email");
            case "description" -> d.get("description");
            case "asset_group" -> d.get("asset_group");
            case "category" -> d.get("category");
            case "sub_category" -> d.get("sub_category");
            case "assigned_user_email" -> d.get("assigned_user_email");
            case "username" -> ctx.ds().get("username");
            case "email" -> ctx.ds().get("email");
            default -> userDataFallback(cb, d, "user_data_name", "display_name");
        };
    }

    /** CASE WHEN user_data_x IS NULL OR '' THEN x ELSE user_data_x END (shared, was copied 5x). */
    private Expression<String> userDataFallback(CriteriaBuilder cb, From<?, Device> d,
                                                String userField, String baseField) {
        return cb.<String>selectCase()
                .when(cb.or(cb.isNull(d.get(userField)), cb.equal(d.get(userField), "")),
                      d.get(baseField))
                .otherwise(d.get(userField));
    }

    /** Stringify any expression via concat_ws (PG variadic any -> text). */
    private Expression<String> asText(CriteriaBuilder cb, Expression<?> e) {
        return cb.function("concat_ws", String.class, cb.literal(""), e, cb.literal(""));
    }

    /** custom_field_text(d.custom_fields, '$[*]."key"') with the key bound, not concatenated. */
    private Expression<String> customFieldText(CriteriaBuilder cb, Ctx ctx, String key) {
        return cb.function("custom_field_text", String.class,
                ctx.d.get("custom_fields"), cb.literal(jsonPathFor(key)));
    }

    // ------------------------------------------------------------------ feature filters

    private void addFeatureFilterPredicates(CriteriaBuilder cb, Ctx ctx, DeviceSearchCriteria c,
                                            List<Predicate> ps, AbstractQuery<?> sub) {
        From<?, Device> d = ctx.d;
        for (DeviceSearchCriteria.FeatureFilter f : c.getFeatureFilters()) {
            String cond = f.condition();
            boolean present = "is_present".equals(cond);
            switch (f.name()) {
                case "qrcode" -> {
                    Predicate qc = existsForDevice(cb, sub, d, io.sclera.models.QrCode.class);
                    Predicate cqc = existsForDevice(cb, sub, d, io.sclera.models.ClientQrCode.class);
                    if (present) ps.add(cb.or(qc, cqc));
                    else if ("is_not_present".equals(cond)) ps.add(cb.and(qc.not(), cqc.not()));
                }
                case "barcode" -> {
                    Predicate cbc = existsForDevice(cb, sub, d, io.sclera.models.ClientBarCode.class);
                    if (present) ps.add(cbc);
                    else if ("is_not_present".equals(cond)) ps.add(cbc.not());
                }
                case "nfc" -> {
                    Predicate n = existsForDevice(cb, sub, d, io.sclera.models.Nfc.class);
                    Predicate cn = existsForDevice(cb, sub, d, io.sclera.models.ClientNfc.class);
                    if (present) ps.add(cb.or(n, cn));
                    else if ("is_not_present".equals(cond)) ps.add(cb.and(n.not(), cn.not()));
                }
                case "adc" -> {
                    if (present) ps.add(cb.equal(d.get("source_type"), "adc"));
                    else if ("is_not_present".equals(cond)) ps.add(cb.equal(d.get("source_type"), "vdms"));
                }
                case "record_checklist" -> addCountFeature(cb, d, "record_checklist_count", cond, ps);
                case "document" -> addCountFeature(cb, d, "document_count", cond, ps);
                case "measuring_instrument" -> addCountFeature(cb, d, "measuring_instrument_count", cond, ps);
                case "asset_image_url" -> {
                    Expression<String> url = d.get("asset_image_url");
                    if (present) ps.add(cb.and(cb.isNotNull(url), cb.notEqual(url, "[]")));
                    else if ("is_not_present".equals(cond)) ps.add(cb.or(cb.isNull(url), cb.equal(url, "[]")));
                }
                case "sensor_alert" -> {
                    List<Predicate> arms = new ArrayList<>();
                    for (String field : SENSOR_STATUS_FIELDS) {
                        Expression<String> s = d.get(field);
                        arms.add(present ? cb.equal(s, "alert")
                                         : cb.or(cb.isNull(s), cb.notEqual(s, "alert")));
                    }
                    ps.add(present ? cb.or(arms.toArray(new Predicate[0]))
                                   : cb.and(arms.toArray(new Predicate[0])));
                }
                case "geolocation_status", "image_status", "field_status", "tag_status" -> {
                    Integer v = switch (cond) {
                        case "is_not_present" -> 0;
                        case "is_present" -> 1;
                        case "retag" -> 2;
                        case "not_added_exception" -> 3;
                        default -> null;
                    };
                    if (v != null) ps.add(cb.equal(ctx.dos().get(f.name()), v));
                }
                default -> { /* unknown feature: legacy appends nothing */ }
            }
        }
    }

    private void addCountFeature(CriteriaBuilder cb, From<?, Device> d, String field,
                                 String cond, List<Predicate> ps) {
        Expression<Integer> count = d.get(field);
        if ("is_present".equals(cond)) ps.add(cb.greaterThan(count, 0));
        else if ("is_not_present".equals(cond)) ps.add(cb.or(cb.isNull(count), cb.equal(count, 0)));
    }

    /** EXISTS (SELECT 1 FROM <entity> e WHERE e.device.id = d.id). */
    private Predicate existsForDevice(CriteriaBuilder cb, AbstractQuery<?> parent,
                                      From<?, Device> d, Class<?> entity) {
        Subquery<Integer> ex = parent.subquery(Integer.class);
        Root<?> r = ex.from(entity);
        ex.select(cb.literal(1));
        ex.where(cb.equal(r.get("device").get("id"), d.get("id")));
        return cb.exists(ex);
    }

    // ------------------------------------------------------------------ keyword search

    private void addSearchPredicates(CriteriaBuilder cb, Ctx ctx, DeviceSearchCriteria c,
                                     List<Predicate> ps) {
        for (KeywordSearch ks : c.getSearches()) {
            String strippedTerm = stripSpecials(ks.value()).toLowerCase();
            if (ks.column() == null) {
                ps.add(searchAllPredicate(cb, ctx, ks, strippedTerm));
            } else {
                Expression<String> raw = ks.custom()
                        ? customFieldText(cb, ctx, ks.column())
                        : resolveString(cb, ctx, ks.column());
                Expression<String> hay = cb.function("strip_specials", String.class,
                        cb.lower(asText(cb, raw)));
                ps.add(singleColumnCondition(cb, hay, ks.condition(), strippedTerm));
            }
        }
    }

    /** generateConditionedQuery analog (single standard/custom column). */
    private Predicate singleColumnCondition(CriteriaBuilder cb, Expression<String> hay,
                                            Cond cond, String term) {
        return switch (cond) {
            case CONTAINS -> cb.like(hay, "%" + term + "%");
            case DOES_NOT_CONTAIN -> cb.notLike(hay, "%" + term + "%");
            case EQUAL_TO -> cb.equal(hay, term);
            case NOT_EQUAL_TO -> cb.notEqual(hay, term);
            case STARTS_WITH -> cb.like(hay, term + "%");
            case ENDS_WITH -> cb.like(hay, "%" + term);
        };
    }

    /**
     * Search-all: one big LOWER(CONCAT_WS('±', ...)) haystack over the same columns as the
     * legacy SQL, including the custom-fields injector:
     *   COALESCE(CASE WHEN <custom-array-text matches POSITIVE pattern> THEN <term> ELSE '' END, '')
     * The injector is always POSITIVE — the outer ±-pattern applies the polarity
     * (NOT LIKE for does_not_contain/not_equal_to). This is intentional legacy design;
     * see the spec CORRECTION note.
     */
    private Predicate searchAllPredicate(CriteriaBuilder cb, Ctx ctx, KeywordSearch ks, String term) {
        From<?, Device> d = ctx.d;

        // custom-fields injector
        Expression<String> customArray = cb.function("strip_custom_specials", String.class,
                cb.lower(cb.function("custom_field_array_text", String.class,
                        d.get("custom_fields"), cb.literal("$[*].*"))));
        String positive = switch (ks.condition()) {
            case CONTAINS, DOES_NOT_CONTAIN -> "%" + term + "%";
            case EQUAL_TO, NOT_EQUAL_TO -> "%\"" + term + "\"%";
            case STARTS_WITH -> "%\"" + term + "%";
            case ENDS_WITH -> "%" + term + "\"%";
        };
        Expression<String> injector = cb.coalesce(
                cb.<String>selectCase()
                        .when(cb.like(customArray, positive), term)
                        .otherwise(""),
                cb.literal(""));

        Expression<String> hay = cb.function("strip_specials", String.class, cb.lower(
                cb.function("concat_ws", String.class,
                        cb.literal("±"), cb.literal(""),
                        d.get("id"),
                        userDataFallback(cb, d, "user_data_name", "display_name"),
                        userDataFallback(cb, d, "user_data_vendor", "vendor"),
                        userDataFallback(cb, d, "user_data_model", "model"),
                        d.get("type"), d.get("description"),
                        d.get("ip_address"), d.get("mac_address"),
                        d.get("latitude"), d.get("longitude"),
                        d.get("serial_number"), d.get("warranty"),
                        d.get("created_timestamp"),
                        ctx.location().get("name"), ctx.floor().get("name"), ctx.building().get("name"),
                        ctx.dos().get("assignee_email"), ctx.dosa().get("email"),
                        ctx.ds().get("username"), ctx.ds().get("email"),
                        injector)));

        return switch (ks.condition()) {
            case CONTAINS -> cb.like(hay, "%" + term + "%");
            case DOES_NOT_CONTAIN -> cb.notLike(hay, "%" + term + "%");
            case EQUAL_TO -> cb.like(hay, "%±" + term + "±%");
            case NOT_EQUAL_TO -> cb.notLike(hay, "%±" + term + "±%");
            case STARTS_WITH -> cb.like(hay, "%±" + term + "%");
            case ENDS_WITH -> cb.like(hay, "%" + term + "±%");
        };
    }

    // ------------------------------------------------------------------ sort

    private List<Order> buildOrders(CriteriaBuilder cb, Ctx ctx, DeviceSearchCriteria c) {
        List<Order> orders = new ArrayList<>();
        DeviceSearchCriteria.SortSpec sort = c.getSort();
        if (sort == null) {
            Expression<?> ut = ctx.d.get("updated_timestamp");
            orders.add(nullsLast(cb, ut));
            orders.add(cb.desc(ut));
            orders.add(cb.asc(ctx.d.get("id")));
            return orders;
        }
        if (sort.custom()) {
            Expression<String> v = customFieldText(cb, ctx, sort.column());
            // ORDER BY (v IS NULL OR v = ''), v  -> non-empty values first, missing/empty last
            orders.add(cb.asc(cb.selectCase()
                    .when(cb.or(cb.isNull(v), cb.equal(v, "")), 1).otherwise(0)));
            orders.add(cb.asc(v));
            return orders;
        }
        if ("ip_address".equals(sort.column())) {
            Expression<String> ip = ctx.d.get("ip_address");
            orders.add(nullsLast(cb, ip));
            orders.add(cb.asc(cb.function("inet_val", String.class, ip)));
            return orders;
        }
        if ("created_timestamp".equals(sort.column()) || "updated_timestamp".equals(sort.column())) {
            // legacy appended "col = ''" here -> bigint = '' PG error; numeric columns skip it (fix)
            Expression<?> ts = ctx.d.get(sort.column());
            orders.add(nullsLast(cb, ts));
            orders.add(cb.desc(ts));
            orders.add(cb.asc(ctx.d.get("id")));
            return orders;
        }
        // standard string column (may live on dos/ds/l/f/b — Ctx joins it on demand;
        // legacy outer query did NOT join dos/ds and errored: documented fix)
        Expression<String> col = resolveString(cb, ctx, sort.column());
        orders.add(nullsLast(cb, col));
        orders.add(cb.asc(cb.selectCase().when(cb.equal(col, ""), 1).otherwise(0)));
        orders.add(cb.asc(col));
        return orders;
    }

    private Order nullsLast(CriteriaBuilder cb, Expression<?> e) {
        return cb.asc(cb.selectCase().when(cb.isNull(e), 1).otherwise(0));
    }

    // ------------------------------------------------------------------ shared helpers

    /** Lazily-created LEFT joins off a Device root; used by both inner subquery and outer sort. */
    static final class Ctx {
        final CriteriaBuilder cb;
        final From<?, Device> d;
        private Join<?, ?> dos, dosa, location, floor, building;
        private From<?, ?> ds;

        Ctx(CriteriaBuilder cb, From<?, Device> d) { this.cb = cb; this.d = d; }

        Join<?, ?> dos() {
            if (dos == null) dos = d.join("device_onboard_status", JoinType.LEFT);
            return dos;
        }
        Join<?, ?> dosa() {
            if (dosa == null) dosa = dos().join("device_onboard_status_assignees", JoinType.LEFT);
            return dosa;
        }
        Join<?, ?> location() {
            if (location == null) location = d.join("location", JoinType.LEFT);
            return location;
        }
        Join<?, ?> floor() {
            if (floor == null) floor = location().join("floor", JoinType.LEFT);
            return floor;
        }
        Join<?, ?> building() {
            if (building == null) building = floor().join("building", JoinType.LEFT);
            return building;
        }
        /** Ad-hoc entity LEFT JOIN (Device has no inverse association to DeviceSpecification). */
        From<?, ?> ds() {
            if (ds == null) {
                var j = ((JpaFrom<?, Device>) d).join(DeviceSpecification.class, SqmJoinType.LEFT);
                j.on(cb.equal(j.get("device").get("id"), d.get("id")));
                ds = j;
            }
            return ds;
        }
    }

    /** Java-side special-character strip — must stay identical to the legacy replaceAll. */
    static String stripSpecials(String s) {
        return s == null ? null : s.replaceAll(JAVA_SPECIALS, "");
    }

    /** Builds the bound jsonpath '$[*]."key"' with the key's quotes/backslashes escaped. */
    static String jsonPathFor(String key) {
        return "$[*].\"" + key.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }
}
