package io.sclera.queryrepository;

import io.sclera.dto.ManagedSoftwareSearchCriteria;
import io.sclera.dto.ManagedSoftwareSearchCriteria.ColumnFilter;
import io.sclera.dto.ManagedSoftwareSearchCriteria.KeywordSearch;
import io.sclera.models.DeviceInstalledApps;
import io.sclera.models.ManagedSoftware;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.AbstractQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.From;
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
 * ManagedSoftwareSearchService.searchSortFilterManagedSoftware / ...Count.
 *
 * <p>Query shape mirrors the original: outer query on ManagedSoftware selecting ms.id with
 * "ms.id IN (subquery carrying the condition/filter/search predicates)", GROUP BY ms.id, and
 * the legacy null-last / empty-middle / value ordering. device_installed_apps and
 * device_specification are LEFT-joined lazily, only when an email/os_type column is referenced
 * (filter, search, search-all haystack, or sort).</p>
 *
 * <p>Documented behaviour fixes vs the legacy SQL:</p>
 * <ul>
 *   <li>values are BOUND (no SQL injection);</li>
 *   <li>the haystack strip reuses {@code strip_specials} which carries the 'g' flag — the legacy
 *       REGEXP_REPLACE omitted it and stripped only the FIRST special character;</li>
 *   <li>sorting by subscription_start/end_date no longer emits {@code bigint = ''} (a PG type
 *       error) and orders by the numeric column directly;</li>
 *   <li>{@code ISNULL(col)} — a MySQL-only function that does not exist in PostgreSQL — is
 *       replaced by a portable {@code CASE WHEN col IS NULL} ordering, so the sort path no
 *       longer throws on PG.</li>
 * </ul>
 */
@Component
public class ManagedSoftwareSearchQueryBuilder {

    /** Java-side special-character strip — identical char class to the legacy replaceAll. */
    private static final String JAVA_SPECIALS = "[ -.!\t_+#~`@$%^&*()=;:<>?,/{}|\\\\]";

    private final EntityManager em;

    public ManagedSoftwareSearchQueryBuilder(EntityManager em) { this.em = em; }

    // ------------------------------------------------------------------ public API

    public List<String> findIds(ManagedSoftwareSearchCriteria c, int pageNo, int pageSize) {
        TypedQuery<String> q = buildIdQuery(c);
        q.setFirstResult(pageSize * (pageNo - 1));
        q.setMaxResults(pageSize);
        return q.getResultList();
    }

    public long count(ManagedSoftwareSearchCriteria c) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<ManagedSoftware> ms = cq.from(ManagedSoftware.class);
        cq.select(cb.count(ms.get("id")));
        cq.where(ms.get("id").in(matchingIds(cb, cq, c)));
        return em.createQuery(cq).getSingleResult();
    }

    // ------------------------------------------------------------------ outer query

    private TypedQuery<String> buildIdQuery(ManagedSoftwareSearchCriteria c) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<String> cq = cb.createQuery(String.class);
        Root<ManagedSoftware> ms = cq.from(ManagedSoftware.class);
        cq.select(ms.get("id"));
        cq.where(ms.get("id").in(matchingIds(cb, cq, c)));
        cq.groupBy(ms.get("id"));
        cq.orderBy(buildOrders(cb, new Ctx(cb, ms), c));
        return em.createQuery(cq);
    }

    /** Subquery selecting matching managed-software ids, carrying condition/filter/search. */
    private Subquery<String> matchingIds(CriteriaBuilder cb, AbstractQuery<?> parent,
                                         ManagedSoftwareSearchCriteria c) {
        Subquery<String> sub = parent.subquery(String.class);
        Root<ManagedSoftware> ms = sub.from(ManagedSoftware.class);
        sub.select(ms.get("id"));
        Ctx ctx = new Ctx(cb, ms);

        List<Predicate> ps = new ArrayList<>();
        addConditionPredicate(cb, ctx, c, ps);
        addFilterPredicates(cb, ctx, c, ps);
        addSearchPredicate(cb, ctx, c, ps);
        sub.where(ps.toArray(new Predicate[0]));
        return sub;
    }

    // ------------------------------------------------------------------ condition

    private void addConditionPredicate(CriteriaBuilder cb, Ctx ctx,
                                       ManagedSoftwareSearchCriteria c, List<Predicate> ps) {
        Expression<String> status = ctx.ms.get("status");
        switch (c.getCondition()) {
            case ACTIVE -> ps.add(cb.equal(status, "active"));
            case EXPIRED -> ps.add(cb.equal(status, "expired"));
            case OTHERS -> ps.add(cb.not(status.in("active", "expired")));
            case ALL -> { /* no predicate */ }
        }
    }

    // ------------------------------------------------------------------ filters

    private void addFilterPredicates(CriteriaBuilder cb, Ctx ctx,
                                     ManagedSoftwareSearchCriteria c, List<Predicate> ps) {
        for (ColumnFilter f : c.getFilters()) {
            Expression<String> col = resolveString(cb, ctx, f.column());
            if (f.value() != null) {
                ps.add(cb.equal(col, String.valueOf(f.value())));
            } else {
                ps.add(cb.and(cb.isNotNull(col), cb.notEqual(col, "")));
            }
        }
    }

    // ------------------------------------------------------------------ keyword search (contains only)

    private void addSearchPredicate(CriteriaBuilder cb, Ctx ctx,
                                    ManagedSoftwareSearchCriteria c, List<Predicate> ps) {
        KeywordSearch ks = c.getSearch();
        if (ks == null) return;
        String term = stripSpecials(ks.value()).toLowerCase();
        Expression<String> hay = ks.column() == null
                ? searchAllHaystack(cb, ctx)
                : cb.function("strip_specials", String.class,
                              cb.lower(asText(cb, resolveString(cb, ctx, ks.column()))));
        ps.add(cb.like(hay, "%" + term + "%"));
    }

    /** LOWER(CONCAT_WS('±','', <all searchable columns incl. ds.email/os_type>)) stripped. */
    private Expression<String> searchAllHaystack(CriteriaBuilder cb, Ctx ctx) {
        From<?, ?> ms = ctx.ms;
        return cb.function("strip_specials", String.class, cb.lower(
                cb.function("concat_ws", String.class,
                        cb.literal("±"), cb.literal(""),
                        ms.get("name"), ms.get("applicationName"), ms.get("applicationType"),
                        ms.get("vendor"), ms.get("subscriptionId"), ms.get("subscriptionType"),
                        ms.get("unitPrice"), ms.get("currency"),
                        ms.get("subscriptionStartDate"), ms.get("subscriptionEndDate"),
                        ms.get("status"), ctx.ds().get("email"), ctx.ds().get("osType"))));
    }

    // ------------------------------------------------------------------ sort

    private List<Order> buildOrders(CriteriaBuilder cb, Ctx ctx, ManagedSoftwareSearchCriteria c) {
        List<Order> orders = new ArrayList<>();
        ManagedSoftwareSearchCriteria.SortSpec sort = c.getSort();
        if (sort == null) {
            orders.add(cb.asc(ctx.ms.get("id")));
            return orders;
        }
        String column = sort.column();
        if ("subscription_start_date".equals(column) || "subscription_end_date".equals(column)) {
            // legacy appended "col = ''" here -> bigint = '' PG error; order by the numeric column
            Expression<?> ts = ctx.ms.get("subscription_start_date".equals(column)
                    ? "subscriptionStartDate" : "subscriptionEndDate");
            orders.add(nullsLast(cb, ts));
            orders.add(cb.desc(ts));
            orders.add(cb.asc(ctx.ms.get("id")));
            return orders;
        }
        if ("email".equals(column) || "os_type".equals(column)) {
            // joined column -> aggregate with MIN under GROUP BY ms.id (mirrors legacy MIN())
            Expression<String> min = cb.least(
                    "email".equals(column) ? ctx.ds().<String>get("email") : ctx.ds().<String>get("osType"));
            orders.add(nullsLast(cb, min));
            orders.add(emptyMiddle(cb, min));
            orders.add(cb.asc(min));
            return orders;
        }
        Expression<String> col = resolveString(cb, ctx, column);
        orders.add(nullsLast(cb, col));
        orders.add(emptyMiddle(cb, col));
        orders.add(cb.asc(col));
        return orders;
    }

    private Order nullsLast(CriteriaBuilder cb, Expression<?> e) {
        return cb.asc(cb.selectCase().when(cb.isNull(e), 1).otherwise(0));
    }

    private Order emptyMiddle(CriteriaBuilder cb, Expression<String> e) {
        return cb.asc(cb.selectCase().when(cb.equal(e, ""), 1).otherwise(0));
    }

    // ------------------------------------------------------------------ shared helpers

    /**
     * Criteria analog of updateSearchColumnName: maps a UI column name to a STRING expression.
     * Non-string columns (unit_price, subscription dates) are stringified via concat_ws so
     * comparisons/LIKEs behave like the legacy implicit text cast.
     */
    private Expression<String> resolveString(CriteriaBuilder cb, Ctx ctx, String column) {
        From<?, ?> ms = ctx.ms;
        return switch (column == null ? "name" : column) {
            case "currency" -> ms.get("currency");
            // legacy maps "name" -> CONCAT(ms.name, ms.application_name); PG CONCAT() treats NULL as
            // empty, so coalesce each arm (Hibernate renders concat as ||, which would propagate NULL).
            case "name" -> cb.concat(cb.coalesce(ms.<String>get("name"), ""),
                                     cb.coalesce(ms.<String>get("applicationName"), ""));
            case "status" -> ms.get("status");
            case "subscription_id" -> ms.get("subscriptionId");
            case "subscription_end_date" -> asText(cb, ms.get("subscriptionEndDate"));
            case "subscription_start_date" -> asText(cb, ms.get("subscriptionStartDate"));
            case "subscription_type" -> ms.get("subscriptionType");
            case "unit_price" -> asText(cb, ms.get("unitPrice"));
            case "vendor" -> ms.get("vendor");
            case "application_name" -> ms.get("applicationName");
            case "application_type" -> ms.get("applicationType");
            case "email" -> ctx.ds().get("email");
            case "os_type" -> ctx.ds().get("osType");
            default -> ms.get("name");
        };
    }

    /** Stringify any expression via concat_ws (PG variadic any -> text). */
    private Expression<String> asText(CriteriaBuilder cb, Expression<?> e) {
        return cb.function("concat_ws", String.class, cb.literal(""), e, cb.literal(""));
    }

    /** Lazily-created LEFT joins off a ManagedSoftware root (no inverse association exists). */
    static final class Ctx {
        final CriteriaBuilder cb;
        final From<?, ManagedSoftware> ms;
        private From<?, ?> dia;
        private From<?, ?> ds;

        Ctx(CriteriaBuilder cb, From<?, ManagedSoftware> ms) { this.cb = cb; this.ms = ms; }

        /** Ad-hoc LEFT JOIN device_installed_apps ON dia.managed_software_id = ms.id. */
        From<?, ?> dia() {
            if (dia == null) {
                var j = ((JpaFrom<?, ManagedSoftware>) ms).join(DeviceInstalledApps.class, SqmJoinType.LEFT);
                j.on(cb.equal(j.get("managedSoftware").get("id"), ms.get("id")));
                dia = j;
            }
            return dia;
        }

        /** LEFT JOIN device_specification via dia.deviceSpecification association. */
        From<?, ?> ds() {
            if (ds == null) ds = dia().join("deviceSpecification", JoinType.LEFT);
            return ds;
        }
    }

    /** Java-side special-character strip — must stay identical to the legacy replaceAll. */
    static String stripSpecials(String s) {
        return s == null ? null : s.replaceAll(JAVA_SPECIALS, "");
    }
}
