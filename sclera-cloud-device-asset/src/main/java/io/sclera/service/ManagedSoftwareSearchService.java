package io.sclera.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.sclera.Repository.ManagedSoftwareRepository;
import io.sclera.dto.ManagedSoftwareDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

@Service
public class ManagedSoftwareSearchService {
    private static final Logger log = LoggerFactory.getLogger(ManagedSoftwareSearchService.class);

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    ManagedSoftwareRepository managedSoftwareRepository;

    public String updateSearchColumnName(String searchColumn) {
        String updateSearchColumn;

        switch (searchColumn) {
//            case "id":
//                updateSearchColumn = "ms.id";
//                break;

//            case "account_manager":
//                updateSearchColumn = "ms.account_manager";
//                break;

//            case "account_owner":
//                updateSearchColumn = "ms.account_owner";
//                break;

//            case "application_id":
//                updateSearchColumn = "ms.application_id";
//                break;

            case "currency":
                updateSearchColumn = "ms.currency";
                break;

            case "name":
                // Concatenate name and application_name
                updateSearchColumn = "CONCAT(ms.name, ms.application_name)";
//                updateSearchColumn = "ms.name";
                break;

            case "status":
                updateSearchColumn = "ms.status";
                break;

            case "subscription_id":
                updateSearchColumn = "ms.subscription_id";
                break;

            case "subscription_end_date":
                updateSearchColumn = "ms.subscription_end_date";
                break;

            case "subscription_start_date":
                updateSearchColumn = "ms.subscription_start_date";
                break;

            case "subscription_type":
                updateSearchColumn = "ms.subscription_type";
                break;

            case "unit_price":
                updateSearchColumn = "ms.unit_price";
                break;

//            case "url":
//                updateSearchColumn = "ms.url";
//                break;

            case "vendor":
                updateSearchColumn = "ms.vendor";
                break;

            case "application_name":
                updateSearchColumn = "ms.application_name";
                break;

            case "application_type":
                updateSearchColumn = "ms.application_type";
                break;

            case "email":
                updateSearchColumn = "ds.email";
                break;

            case "os_type":
                updateSearchColumn = "ds.os_type";
                break;

            default:
                // When column is null, use name as default
                updateSearchColumn = "ms.name";
        }

        return updateSearchColumn;
    }

    // Managed Software filter
    public Set<ManagedSoftwareDTO> searchSortFilterManagedSoftware(String username, String vdmsId, String dockerName,
                                                                   String condition, Integer pageNo, Integer pageSize,
                                                                   JSONObject search_sort_filter_details) {
        try {
            int offset = pageSize * (pageNo - 1);
            Set<String> managedSoftwareIds = new LinkedHashSet<>();

            String searchAndFilterCustomQuery = this.generateSearchAndFilterCustomQuery(search_sort_filter_details);
            String groupByAndSortCustomQuery = this.generateGroupByAndSortCustomQuery(search_sort_filter_details);

            String query = "SELECT ms.id "
                    + "FROM managed_software ms "
                    + "LEFT JOIN device_installed_apps dia ON ms.id = dia.managed_software_id "
                    + "LEFT JOIN device_specification ds ON dia.device_specification_id = ds.id "
                    + "WHERE ms.id IN ( "
                    + "SELECT DISTINCT ms.id "
                    + "FROM managed_software ms "
                    + "LEFT JOIN device_installed_apps dia ON ms.id = dia.managed_software_id "
                    + "LEFT JOIN device_specification ds ON dia.device_specification_id = ds.id "
                    + "WHERE ( "
                    + "  ('" + condition + "' = 'all') "
                    + "  OR ('" + condition + "' = 'active' AND ms.status = 'active') "
                    + "  OR ('" + condition + "' = 'expired' AND ms.status = 'expired') "
                    + "  OR ('" + condition + "' = 'others' AND ms.status NOT IN ('active','expired')) "
                    + ") "
                    + searchAndFilterCustomQuery
                    + ") "
                    + groupByAndSortCustomQuery
                    + " LIMIT " + pageSize + " OFFSET " + offset;

            log.info("CONSTRUCTED FILTER QUERY: {}", query);
            var queryResult = jdbcTemplate.queryForList(query);

            for (Map<String, Object> stringObjectMap : queryResult) {
                managedSoftwareIds.add(String.valueOf(stringObjectMap.get("id")));
            }

//            log.info("Filtered Managed Software IDs: {}", managedSoftwareIds);
            Set<ManagedSoftwareDTO> filteredManagedSoftwares = managedSoftwareRepository.getManagedSoftwareByIdList(managedSoftwareIds);
//            log.info("Filtered Managed Software DTOs: {}", filteredManagedSoftwares);

            if (!filteredManagedSoftwares.isEmpty())
                return filteredManagedSoftwares;

        } catch (Exception e) {
            throw new RuntimeException("Error during search-sort-filter operation: " + e.getMessage(), e);
        }

        log.info("No Managed Software records found for the given search, sort, and filter details");
        return Collections.emptySet();
    }

    // All helper methods for search and filter queries
    public String generateSearchAndFilterCustomQuery(JSONObject searchAndFilterDetails) {
        StringBuilder searchAndFilterCustomQuery = new StringBuilder();

        if (searchAndFilterDetails.containsKey("filter_details") && searchAndFilterDetails.getJSONArray("filter_details") != null) {
            String filterQuery = this.generateFilterQuery(searchAndFilterDetails.getJSONArray("filter_details"));
            searchAndFilterCustomQuery.append(filterQuery);
        }

        if (searchAndFilterDetails.containsKey("search_details") && searchAndFilterDetails.getJSONObject("search_details") != null) {
            String searchQuery = this.generateSearchQuery(searchAndFilterDetails.getJSONObject("search_details"));
            searchAndFilterCustomQuery.append(searchQuery);
        }

        return searchAndFilterCustomQuery.toString();
    }

    public String generateFilterQuery(JSONArray filterDetails) {
        StringBuilder stringBuilder = new StringBuilder();

        if (!filterDetails.isEmpty()) {
            stringBuilder.append(" AND (");

            for (int i = 0; i < filterDetails.size(); i++) {
                stringBuilder.append("( ");
                JSONObject tempMap = filterDetails.getJSONObject(i);

                if (tempMap.get("value") != null) {
                    // Currently there are only 2 filters_details available i.e Assignee (email) and OS Type (os_type)
                    stringBuilder.append(this.updateSearchColumnName(String.valueOf(tempMap.get("column"))))
                            .append(" = '").append(tempMap.get("value"))
                            .append("'");
                } else {
                    stringBuilder.append(this.updateSearchColumnName(String.valueOf(tempMap.get("column"))))
                            .append(" IS NOT NULL AND ").append(this.updateSearchColumnName(String.valueOf(tempMap.get("column"))))
                            .append(" <> ''");
                }

                stringBuilder.append(")");

                if (i < filterDetails.size() - 1) {
                    stringBuilder.append(" AND ");
                }
            }

            stringBuilder.append(")");
        }

        return stringBuilder.toString();
    }

    public String generateSearchQuery(JSONObject searchDetails) {
        StringBuilder stringBuilder = new StringBuilder();
        String searchColumn = String.valueOf(searchDetails.get("column")).replaceAll("\\s", "");
        String searchTerm = searchDetails.getString("value");

        // Handle null or empty search term
        if (searchTerm == null) {
            log.info("Search term is null, skipping search query generation...");
            return "";
        }

        String searchTermWithoutSpecialCharacters = searchTerm.replaceAll("[ -.!\t_+#~`@$%^&*()=;:<>?,/{}|\\\\]", "");
        String updatedSearchColumn = this.updateSearchColumnName(searchColumn);

        StringBuilder searchColumnValue = new StringBuilder();
        StringBuilder searchColumnValueWithoutSpecialCharacters = new StringBuilder();

        if (searchDetails.get("column") == null) {
            searchColumnValue
                    .append("LOWER(CONCAT_WS('±','', ms.name, ms.application_name, ms.application_type, ")
                    .append("ms.vendor, ms.subscription_id, ms.subscription_type, ms.unit_price, ms.currency, ms.subscription_start_date, ")
                    .append("ms.subscription_end_date, ms.status, ds.email, ds.os_type))");

//            searchColumnValue
//                    .append("LOWER(CONCAT_WS('±','', ms.id, ms.name, ms.application_name, ms.application_type, ms.url, ")
//                    .append("ms.vendor, ms.subscription_id, ms.subscription_type, ms.unit_price, ms.currency, ms.subscription_start_date, ")
//                    .append("ms.subscription_end_date, ms.status, ms.application_id, ds.email, ds.os_type))");

        } else {
            searchColumnValue
                    .append("LOWER(CONCAT_WS('',")
                    .append(updatedSearchColumn)
                    .append(",''))");
        }

        searchColumnValueWithoutSpecialCharacters
                .append("REGEXP_REPLACE(")
                .append(searchColumnValue)
                .append(", '[ -.!\t_+#~`@$%^&*()=;:<>?,/{}|\\]', '')");

        // Currently search is done only for "contains"
        stringBuilder
                .append(" AND (").append(searchColumnValueWithoutSpecialCharacters)
                .append(" LIKE LOWER('%")
                .append(searchTermWithoutSpecialCharacters)
                .append("%')")
                .append(" )");

        return stringBuilder.toString();
    }

    // All helper methods for sort query
    public String generateGroupByAndSortCustomQuery(JSONObject sortDetails) {
        StringBuilder stringBuilder = new StringBuilder();

        // Build the SQL GROUP BY clause (for DISTINCT ms.id results)
        stringBuilder.append("GROUP BY ms.id");

        if (sortDetails.containsKey("sort_details") && sortDetails.getJSONObject("sort_details") != null) {
            JSONObject sortDetailsObject = sortDetails.getJSONObject("sort_details");
            String searchColumn = String.valueOf(sortDetailsObject.get("column")).replaceAll("\\s", "");
            String updatedSearchColumn = this.updateSearchColumnName(searchColumn);

            // For joined table columns (email/os_type), use aggregate function MIN() to avoid duplicates
            if (searchColumn.equals("email") || searchColumn.equals("os_type")) {
                updatedSearchColumn = "MIN(" + updatedSearchColumn + ")";
            }

            // Build the SQL ORDER BY clause
            StringBuilder orderByClause = new StringBuilder();
            orderByClause.append("ISNULL(").append(updatedSearchColumn).append("), ") // List all NULL values at the bottom (if-present)
                    .append(updatedSearchColumn).append(" = '', ") // List all empty values in the middle (if-present)
                    .append(updatedSearchColumn); // List all actual values at the top (if-present)

            // Sort by subscription start/end dates
            if (searchColumn.equals("subscription_start_date") || searchColumn.equals("subscription_end_date")) {
                orderByClause.append(" DESC, ms.id"); // Sort all dates in DESC order, ms.id for tie-breaking
            }

            stringBuilder
                    .append(" ORDER BY ")
                    .append(orderByClause);
        } else {
            // When no specific sort column is provided, sort by Managed Software ID
            stringBuilder
                    .append(" ORDER BY ms.id ");
        }

        return stringBuilder.toString();
    }

    // Managed Software filter count
    public String searchSortFilterManagedSoftwareCount(String username, String vdmsId, String dockerName,
                                                       String condition, JSONObject search_sort_filter_details) {
        try {
            String searchAndFilterCustomQuery = this.generateSearchAndFilterCustomQuery(search_sort_filter_details);

            String query = "SELECT COUNT(DISTINCT ms.id) AS count "
                    + "FROM managed_software ms "
                    + "LEFT JOIN device_installed_apps dia ON ms.id = dia.managed_software_id "
                    + "LEFT JOIN device_specification ds ON dia.device_specification_id = ds.id "
                    + "WHERE ( "
                    + "  ('" + condition + "' = 'all') "
                    + "  OR ('" + condition + "' = 'active' AND ms.status = 'active') "
                    + "  OR ('" + condition + "' = 'expired' AND ms.status = 'expired') "
                    + "  OR ('" + condition + "' = 'others' AND ms.status NOT IN ('active','expired')) "
                    + ") "
                    + searchAndFilterCustomQuery;

            log.info("CONSTRUCTED COUNT QUERY: {}", query);
            var queryResult = jdbcTemplate.queryForList(query);

            for (Map<String, Object> stringObjectMap : queryResult) {
                log.info("Managed Software count: {}", stringObjectMap.get("count"));
                return String.valueOf(stringObjectMap.get("count"));
            }

        } catch (Exception e) {
            throw new RuntimeException("Error during search-sort-filter-count operation: " + e.getMessage(), e);
        }

        log.warn("COUNT query did not return any result row, possibly a query or database issue");
        return null;
    }
}
