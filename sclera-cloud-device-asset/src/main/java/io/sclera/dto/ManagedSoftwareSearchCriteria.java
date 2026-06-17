package io.sclera.dto;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Typed, parse-once representation of the searchSortFilterManagedSoftware* request
 * (condition string + search_sort_filter_details JSON). All fastjson handling lives
 * here; {@code ManagedSoftwareSearchQueryBuilder} never sees JSON.
 *
 * <p>Mirrors the parse-once design of {@link DeviceSearchCriteria}. The legacy string-SQL
 * helpers on {@code ManagedSoftwareSearchService} (updateSearchColumnName / generate*) are
 * left in place (unused by the converted path) pending a separate cleanup, exactly as the
 * device-search conversion left its generators.</p>
 */
public class ManagedSoftwareSearchCriteria {

    /** Status condition over managed_software.status. */
    public enum StatusCond {
        ALL, ACTIVE, EXPIRED, OTHERS;
        static StatusCond parse(String s) {
            return switch (s == null ? "all" : s) {
                case "active" -> ACTIVE;
                case "expired" -> EXPIRED;
                case "others" -> OTHERS;
                default -> ALL;
            };
        }
    }

    /** value==null means the legacy "IS NOT NULL AND <> ''" present-and-non-empty check. */
    public record ColumnFilter(String column, Object value) {}
    /** column==null means search-all (the big CONCAT_WS haystack). */
    public record KeywordSearch(String column, String value) {}
    public record SortSpec(String column) {}

    private StatusCond condition = StatusCond.ALL;
    private final List<ColumnFilter> filters = new ArrayList<>();
    private KeywordSearch search;
    private SortSpec sort;

    private ManagedSoftwareSearchCriteria() {}

    public static ManagedSoftwareSearchCriteria from(String condition, JSONObject details) {
        ManagedSoftwareSearchCriteria c = new ManagedSoftwareSearchCriteria();
        c.condition = StatusCond.parse(condition);

        if (details != null) {
            JSONArray filterDetails = details.getJSONArray("filter_details");
            if (filterDetails != null) {
                for (int i = 0; i < filterDetails.size(); i++) {
                    JSONObject f = filterDetails.getJSONObject(i);
                    c.filters.add(new ColumnFilter(asColumn(f.get("column")), f.get("value")));
                }
            }

            JSONObject searchDetails = details.getJSONObject("search_details");
            // legacy: a null search term skips search-query generation entirely
            if (searchDetails != null && searchDetails.getString("value") != null) {
                String column = searchDetails.get("column") == null ? null
                        : String.valueOf(searchDetails.get("column")).replaceAll("\\s", "");
                c.search = new KeywordSearch(column, searchDetails.getString("value"));
            }

            JSONObject sortDetails = details.getJSONObject("sort_details");
            if (sortDetails != null) {
                c.sort = new SortSpec(String.valueOf(sortDetails.get("column")).replaceAll("\\s", ""));
            }
        }
        return c;
    }

    /** Legacy used String.valueOf(column) (so a null column folds to the "name" default downstream). */
    private static String asColumn(Object column) {
        return String.valueOf(column);
    }

    public StatusCond getCondition() { return condition; }
    public List<ColumnFilter> getFilters() { return filters; }
    public KeywordSearch getSearch() { return search; }
    public SortSpec getSort() { return sort; }
}
