package io.sclera.dto;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Typed, parse-once representation of the multipleKeywordSearchSortFilter* request
 * (condition string + search_sort_filter_details JSON + onboard_status param).
 * All fastjson handling lives here; DeviceSearchQueryBuilder never sees JSON.
 *
 * Sentinel translation (was inlined into SQL as constant-folding tricks):
 *   monitor 123 -> null (no predicate); onboard 123 -> null; onboard 210 -> "not 3" flag;
 *   vdmsid 'null' -> null; dockername 'all' -> null.
 *
 * Consistency fix (documented): onboardpending/onboardcompleted were previously handled
 * ONLY by the Count variant (count filtered while the page didn't). All variants now share
 * this parser, so the full condition set applies uniformly.
 */
public class DeviceSearchCriteria {

    public enum Cond { CONTAINS, DOES_NOT_CONTAIN, EQUAL_TO, NOT_EQUAL_TO, STARTS_WITH, ENDS_WITH;
        static Cond parse(String s) {
            if (s == null) return CONTAINS;
            return switch (s) {
                case "does_not_contain" -> DOES_NOT_CONTAIN;
                case "equal_to" -> EQUAL_TO;
                case "not_equal_to" -> NOT_EQUAL_TO;
                case "starts_with" -> STARTS_WITH;
                case "ends_with" -> ENDS_WITH;
                default -> CONTAINS;
            };
        }
    }

    /** column==null means search-all. */
    public record KeywordSearch(String column, boolean custom, String value, Cond condition) {}
    /** condition is the raw string: is_present / is_not_present / retag / not_added_exception. */
    public record ColumnFilter(String column, boolean custom, String condition, Object value) {}
    public record FeatureFilter(String name, String condition) {}
    public record SortSpec(String column, boolean custom) {}

    private String vdmsId;
    private String dockerName;
    private Integer monitor;            // null = no predicate; 1 = strict; other = "is null or ="
    private Integer status;
    private boolean virtualOther;       // condition 'other'
    private Integer assignedStatus;     // null / 0 / 1
    private Integer assetMatchStatus;   // null / 0..3
    private Integer onboardStatusEquals;
    private boolean onboardStatusNot3;  // condition 'notonboarded' or param 210
    private List<String> deviceIds = new ArrayList<>();
    private List<ColumnFilter> columnFilters = new ArrayList<>();
    private List<FeatureFilter> featureFilters = new ArrayList<>();
    private List<KeywordSearch> searches = new ArrayList<>();
    private SortSpec sort;

    private DeviceSearchCriteria() {}

    public static DeviceSearchCriteria from(String vdmsid, String dockername, String condition,
                                            JSONObject details, Integer onboardStatusParam) {
        DeviceSearchCriteria c = new DeviceSearchCriteria();
        c.vdmsId = (vdmsid == null || "null".equals(vdmsid)) ? null : vdmsid;
        c.dockerName = (dockername == null || "all".equals(dockername)) ? null : dockername;

        switch (condition == null ? "all" : condition) {
            case "unmonitored" -> c.monitor = 0;
            case "online" -> { c.monitor = 1; c.status = 1; }
            case "offline" -> { c.monitor = 1; c.status = 0; }
            case "other" -> c.virtualOther = true;
            case "matched" -> c.assetMatchStatus = 1;
            case "unmatched" -> c.assetMatchStatus = 0;
            case "verified" -> c.assetMatchStatus = 2;
            case "archived" -> c.assetMatchStatus = 3;
            case "onboarded" -> c.onboardStatusEquals = 3;
            case "notonboarded" -> c.onboardStatusNot3 = true;
            case "onboardpending" -> c.onboardStatusEquals = 1;
            case "onboardcompleted" -> c.onboardStatusEquals = 2;
            case "assigned" -> c.assignedStatus = 1;
            case "unassigned" -> c.assignedStatus = 0;
            default -> { /* "all": no predicates */ }
        }
        // The onboard_status method param applies only when the condition didn't decide it.
        if (!c.onboardStatusNot3 && c.onboardStatusEquals == null
                && onboardStatusParam != null && onboardStatusParam != 123) {
            if (onboardStatusParam == 210) c.onboardStatusNot3 = true;
            else c.onboardStatusEquals = onboardStatusParam;
        }

        if (details != null) {
            JSONArray ids = details.getJSONArray("device_ids");
            if (ids != null) for (int i = 0; i < ids.size(); i++) c.deviceIds.add(ids.getString(i));

            JSONObject filters = details.getJSONObject("filter_details");
            if (filters != null) {
                JSONArray cols = filters.getJSONArray("column_details");
                if (cols != null) for (int i = 0; i < cols.size(); i++) {
                    JSONObject f = cols.getJSONObject(i);
                    c.columnFilters.add(new ColumnFilter(f.getString("column"),
                            Boolean.TRUE.equals(f.getBoolean("custom")),
                            f.getString("condition"), f.get("value")));
                }
                JSONArray feats = filters.getJSONArray("feature_details");
                if (feats != null) for (int i = 0; i < feats.size(); i++) {
                    JSONObject f = feats.getJSONObject(i);
                    c.featureFilters.add(new FeatureFilter(f.getString("name"), f.getString("condition")));
                }
            }

            JSONArray searches = details.getJSONArray("search_details");
            if (searches != null) for (int i = 0; i < searches.size(); i++) {
                JSONObject s = searches.getJSONObject(i);
                String column = s.get("column") == null ? null
                        : String.valueOf(s.get("column")).replaceAll("\\s", "");
                c.searches.add(new KeywordSearch(column,
                        Boolean.TRUE.equals(s.getBoolean("custom")),
                        s.getString("value"), Cond.parse(s.getString("condition"))));
            }

            JSONObject sort = details.getJSONObject("sort_details");
            if (sort != null) {
                c.sort = new SortSpec(String.valueOf(sort.get("column")).replaceAll("\\s", ""),
                        Boolean.TRUE.equals(sort.getBoolean("custom")));
            }
        }
        return c;
    }

    public String getVdmsId() { return vdmsId; }
    public String getDockerName() { return dockerName; }
    public Integer getMonitor() { return monitor; }
    public Integer getStatus() { return status; }
    public boolean isVirtualOther() { return virtualOther; }
    public Integer getAssignedStatus() { return assignedStatus; }
    public Integer getAssetMatchStatus() { return assetMatchStatus; }
    public Integer getOnboardStatusEquals() { return onboardStatusEquals; }
    public boolean isOnboardStatusNot3() { return onboardStatusNot3; }
    public List<String> getDeviceIds() { return deviceIds; }
    public List<ColumnFilter> getColumnFilters() { return columnFilters; }
    public List<FeatureFilter> getFeatureFilters() { return featureFilters; }
    public List<KeywordSearch> getSearches() { return searches; }
    public SortSpec getSort() { return sort; }
}
