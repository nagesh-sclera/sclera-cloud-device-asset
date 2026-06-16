package io.sclera.service;
import io.sclera.client.APICallClient;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;


import io.sclera.dto.DeviceDTO;
import io.sclera.dto.DeviceSearchCriteria;
import io.sclera.interfaces.DeviceSearchServiceInterface;
import io.sclera.queryrepository.DeviceSearchQueryBuilder;
import me.xdrop.fuzzywuzzy.FuzzySearch;

/**
 * Builds and executes dynamic SQL for searching, sorting, and filtering devices, including custom
 * field queries and fuzzy matching, and returns the resulting devices via the device service.
 */
@Service
public class DeviceSearchService implements DeviceSearchServiceInterface {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    DeviceService deviceService;

    @Autowired
    APICallClient apiCallService;

    @Autowired
    private DeviceSearchQueryBuilder deviceSearchQueryBuilder;

    /**************************************************Search Devices Method************************************************************/
    //sort method for now not used
    //	public Set<DeviceDTO> searchDevices(String username, String vdmsid, String dockername,String condition, Integer pageNo, Integer pageSize, Map<String, Object> search_details) {
    //		try {
    //
    //			Integer virtual_device_type = null;
    //			Integer status = null;
    //			Integer monitor = 123;
    //			Integer asset_match_status = null;
    //			Integer offset = pageSize * (pageNo -1);
    //
    //			if(condition.equals("all"))
    //			{
    //				System.out.println("inside all" + virtual_device_type + status + monitor + offset + pageNo);
    //			}else if(condition.equals("unmonitored"))
    //			{
    //				System.out.println("outsidxse all" + virtual_device_type + status + monitor + offset + pageNo);
    //				monitor = 0;
    //			}else if (condition.equals("online") )
    //			{
    //
    //				monitor = 1;
    //				status = 1;
    //				System.out.println("Inside Online" + monitor + status);
    //
    //			}
    //			else if (condition.equals("offline"))
    //			{
    //				monitor = 1;
    //				status = 0;
    //			}else if(condition.equals("other"))
    //			{
    //				virtual_device_type = 2;
    //			}
    //			else if(condition.equals("matched")){
    //				asset_match_status = 1;
    //			}
    //			else if(condition.equals("unmatched")){
    //				asset_match_status = 0;
    //			}
    //
    //			String searchColumn = String.valueOf(search_details.get("column")).replaceAll("\\s", "");
    //			String sortColumn = String.valueOf(search_details.get("sort_column")).replaceAll("\\s", "");
    //			String sortOrder = "ASC";
    //
    //			String updatedSearchColumn = this.updateDeviceSearchColumnName(searchColumn);
    //			String updatedSortColumn = this.updateDeviceSearchColumnName(sortColumn);
    //
    //			if((Boolean) search_details.get("sort_column_custom"))
    //			{
    //				updatedSortColumn = "JSON_UNQUOTE(JSON_EXTRACT(d.custom_fields,CONCAT(\"$[*].\",\"" + sortColumn + "\")))";
    //			}
    //
    //
    //			if(!((Boolean) search_details.get("sort_order")))
    //			{
    //				sortOrder = "DESC";
    //			}
    //
    //
    //
    //			Set<String> device_ids = new HashSet<>();
    //			if(search_details.get("value") == null)
    //			{
    //				String query = "SELECT  d.id FROM device d "
    //						+ "LEFT JOIN location l ON l.id = d.location_id "
    //						+ "LEFT JOIN floor f ON f.id = l.floor_id "
    //						+ "LEFT JOIN building b ON b.id = f.building_id "
    //						+ "WHERE ('" + vdmsid + "' = 'null' or d.docker_vdms_id = '" + vdmsid
    //						+ "') AND ('" + dockername + "' = 'all' or  d.docker_name = '" + dockername + "') "
    //						+ "AND (" + virtual_device_type + " IS NULL or d.virtual_device_type = " + virtual_device_type + ") "
    //						+ "AND (" + status + " IS NULL or d.status = " + status + ") "
    //						+ "AND (" + monitor + " = 123  or IF(" + monitor + " = 1," + monitor + " = d.monitor,d.monitor IS NULL or " + monitor + " = d.monitor)) "
    //						+ "AND (" + asset_match_status + " IS NULL or d.asset_match_status = " + asset_match_status + ") "
    //						+ "ORDER BY "+ updatedSortColumn + " " + sortOrder
    //						+ " LIMIT " + pageSize + " OFFSET " + offset;
    //
    //				System.out.println("query-- with no search :\n"+query);
    //				var queryResult = jdbcTemplate.queryForList(query);
    //				for (Map<String, Object> stringObjectMap : queryResult) {
    //					device_ids.add(String.valueOf(stringObjectMap.get("id")));
    //				}
    //
    //				return deviceService.getDevicesByIdList(device_ids);
    //			}
    //			else
    //			{
    //				if (search_details.get("column") == null) {
    //
    //					String query = "SELECT t1.* "
    //							+ "FROM "
    //							+ "((SELECT d.id, d.location_id, d.docker_name, d.docker_vdms_id, d.virtual_device_type, d.status, d.monitor, d.asset_match_status "
    //							+ "FROM device d "
    //							+ "WHERE JSON_EXTRACT(d.custom_fields,('$[*].*')) "
    //							+ "LIKE CONCAT('%','" + search_details.get("value") + "','%')) "
    //							+ "UNION "
    //							+ "(SELECT d1.id, d1.location_id, d1.docker_name, d1.docker_vdms_id, d1.virtual_device_type, d1.status, d1.monitor, d1.asset_match_status "
    //							+ "FROM device d1 "
    //							+ "LEFT JOIN location l ON l.id = d1.location_id "
    //							+ "LEFT JOIN floor f ON f.id = l.floor_id "
    //							+ "LEFT JOIN building b ON b.id = f.building_id "
    //							+ "WHERE CONCAT_WS('',d1.id, d1.display_name, d1.user_data_name, d1.vendor, d1.user_data_vendor, d1.model, d1.user_data_model, d1.ip_address, "
    //							+ "d1.mac_address, d1.latitude, d1.longitude, d1.serial_number, d1.warranty, l.name, f.name, b.name) "
    //							+ "LIKE CONCAT('%','" + search_details.get("value") + "','%'))) AS t1 "
    //							+ "WHERE ('" + vdmsid + "' = 'null' or t1.docker_vdms_id = '" + vdmsid
    //							+ "') AND ('" + dockername + "' = 'all' or  t1.docker_name = '" + dockername + "') "
    //							+ "AND (" + virtual_device_type + " IS NULL or t1.virtual_device_type = " + virtual_device_type + ") "
    //							+ "AND (" + status + " IS NULL or t1.status = " + status + ") "
    //							+ "AND (" + monitor + " = 123  or IF(" + monitor + " = 1," + monitor + " = t1.monitor,t1.monitor IS NULL or " + monitor + " = t1.monitor)) "
    //							+ "AND (" + asset_match_status + " IS NULL or t1.asset_match_status = " + asset_match_status + ") "
    //							+ "ORDER BY " + updatedSortColumn + " " + sortOrder
    //							+ " LIMIT " + pageSize + " OFFSET " + offset;
    //
    ////					System.out.println("query--1:\n"+query);
    //
    //					var queryResult = jdbcTemplate.queryForList(query);
    //					for (Map<String, Object> stringObjectMap : queryResult) {
    //						device_ids.add(String.valueOf(stringObjectMap.get("id")));
    //					}
    //				} else {
    //					if ((Boolean) search_details.get("custom")) {
    //						String query = "SELECT d.id FROM device "
    //								+ " WHERE ('" + vdmsid + "' = 'null' or d.docker_vdms_id = '" + vdmsid
    //								+ "') AND ('" + dockername + "' = 'all' or  d.docker_name = '" + dockername + "') "
    //								+ "AND (" + virtual_device_type + " IS NULL or d.virtual_device_type = " + virtual_device_type + ") "
    //								+ "AND (" + status + " IS NULL or d.status = " + status + ") "
    //								+ "AND (" + monitor + " = 123  or IF(" + monitor + " = 1," + monitor + " = d.monitor,d.monitor IS NULL or " + monitor + " = d.monitor)) "
    //								+ "AND (" + asset_match_status + " IS NULL or d.asset_match_status = " + asset_match_status + ") "
    //								+ "AND JSON_UNQUOTE(JSON_EXTRACT(d.custom_fields,CONCAT(\"$[*].\",\"" + searchColumn
    //								+ "\"))) LIKE CONCAT('%','" + search_details.get("value") + "','%') "
    //								+ "ORDER BY " +  updatedSortColumn + " " + sortOrder
    //								+ " LIMIT " + pageSize + " OFFSET " + offset;
    //
    //						var queryResult = jdbcTemplate.queryForList(query);
    //						for (Map<String, Object> stringObjectMap : queryResult) {
    //							device_ids.add(String.valueOf(stringObjectMap.get("id")));
    //						}
    //					} else {
    //						String query = "SELECT  d.id FROM device d "
    //								+ "LEFT JOIN location l ON l.id = d.location_id "
    //								+ "LEFT JOIN floor f ON f.id = l.floor_id "
    //								+ "LEFT JOIN building b ON b.id = f.building_id "
    //								+ "WHERE ('" + vdmsid + "' = 'null' or d.docker_vdms_id = '" + vdmsid
    //								+ "') AND ('" + dockername + "' = 'all' or  d.docker_name = '" + dockername + "') "
    //								+ "AND (" + virtual_device_type + " IS NULL or d.virtual_device_type = " + virtual_device_type + ") "
    //								+ "AND (" + status + " IS NULL or d.status = " + status + ") "
    //								+ "AND (" + monitor + " = 123  or IF(" + monitor + " = 1," + monitor + " = d.monitor,d.monitor IS NULL or " + monitor + " = d.monitor)) "
    //								+ "AND (" + asset_match_status + " IS NULL or d.asset_match_status = " + asset_match_status + ") "
    //								+ "AND " + updatedSearchColumn + " LIKE '%" + search_details.get("value") + "%' "
    //								+ "ORDER BY " + updatedSortColumn + " " + sortOrder
    //								+ " LIMIT " + pageSize + " OFFSET " + offset;
    //
    ////						System.out.println("query--3:\n"+query);
    //						var queryResult = jdbcTemplate.queryForList(query);
    //						for (Map<String, Object> stringObjectMap : queryResult) {
    //							device_ids.add(String.valueOf(stringObjectMap.get("id")));
    //						}
    //					}
    //				}
    //			}
    //
    //
    ////			System.out.println("Device Ids " + device_ids);
    //
    //			// get all device information
    //			Set<DeviceDTO> filteredDevices = deviceService.getDevicesByIdList(device_ids);
    //
    //			//update fuzzy score
    //			Set<DeviceDTO> fuzzyScoreUpdatedDevices = this.updateFuzzyMatchScore(filteredDevices, search_details);
    //
    //			if(fuzzyScoreUpdatedDevices != null)
    //			{
    //				List<DeviceDTO> filtered_devices = new ArrayList<DeviceDTO>(fuzzyScoreUpdatedDevices);
    //				filtered_devices = this.sortFilteredDevicesByMatchedScore(filtered_devices);
    //				Set<DeviceDTO> sortedFilteredDevices = new LinkedHashSet<DeviceDTO>(filtered_devices);
    //				return sortedFilteredDevices;
    //			}
    //
    //			return null;
    //		} catch (Exception e) {
    //			System.out.println(e);
    //			return null;
    //		}
    //	}

    //search method without sort
    /**
     * Searches devices within a VDMS and docker scope filtered by a status condition and the given
     * search details, then ranks the matches by fuzzy score. Returns null on error.
     *
     * @return the matched devices ordered by descending fuzzy match score
     */
    public Set<DeviceDTO> searchDevices(String username, String vdmsid, String dockername, String condition, Integer pageNo, Integer pageSize, Map<String, Object> search_details) {
        try {

            // PG-port/Criteria: condition decode + the UNION/CONCAT/jsonb_path search SQL replaced by
            // DeviceSearchQueryBuilder.searchDeviceIds (bound params, JSON via registered funcs). The
            // value==null early return and the post-fetch fuzzy re-rank workflow are unchanged.
            Set<String> device_ids = new HashSet<>();
            if (search_details.get("value") == null) {
                return deviceService.getfilterdevices(username, vdmsid, dockername, condition, "null", pageNo, pageSize); //search key is given as null to get all devices based on condition
            }
            DeviceSearchCriteria scope = DeviceSearchCriteria.from(
                    vdmsid, dockername, condition, new com.alibaba.fastjson.JSONObject(), 123);
            String searchColumn = search_details.get("column") == null ? null
                    : String.valueOf(search_details.get("column")).replaceAll("\\s", "");
            boolean searchCustom = Boolean.TRUE.equals(search_details.get("custom"));
            device_ids.addAll(deviceSearchQueryBuilder.searchDeviceIds(scope,
                    new DeviceSearchQueryBuilder.SplitSearch(searchColumn, searchCustom,
                            String.valueOf(search_details.get("value"))),
                    pageNo, pageSize));


            //			System.out.println("Device Ids " + device_ids);

            // get all device information
//			Set<DeviceDTO> filteredDevices = deviceService.getDevicesByIdList(device_ids);
            Set<DeviceDTO> filteredDevices = new HashSet<>(deviceService.getDevicesByIdList(vdmsid, device_ids));

            //update fuzzy score
            Set<DeviceDTO> fuzzyScoreUpdatedDevices = this.updateFuzzyMatchScore(filteredDevices, search_details);

            if (fuzzyScoreUpdatedDevices != null) {
                List<DeviceDTO> filtered_devices = new ArrayList<DeviceDTO>(fuzzyScoreUpdatedDevices);
                filtered_devices = this.sortFilteredDevicesByMatchedScore(filtered_devices);
                Set<DeviceDTO> sortedFilteredDevices = new LinkedHashSet<DeviceDTO>(filtered_devices);
                return sortedFilteredDevices;
            }

            return null;
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }
    /**************************************************Search Devices Method************************************************************/

    /**************************************************Search Parent Devices Method************************************************************/
//	public Set<DeviceDTO> searchParentDevices(String username, String vdmsid, String searchKey,Integer pageno,
//			Integer pagesize, Set<String> dockernames,Set<String> types, Map<String, Object> search_details) {
//		try {
//			Integer offset = pagesize * (pageno -1);
//			String searchColumn = String.valueOf(search_details.get("column")).replaceAll("\\s", "");
//			String sortColumn = String.valueOf(search_details.get("sort_column")).replaceAll("\\s", "");
//			String sortOrder = "ASC";
//
//			String updatedSearchColumn = this.updateDeviceSearchColumnName(searchColumn);
//			String updatedSortColumn = this.updateDeviceSearchColumnName(sortColumn);
//
//			String sqlDockerNames = String.join(",", Collections.nCopies(dockernames.size(), "'" + dockernames.iterator().next() + "'"));
//			String sqlTypes = String.join(",", Collections.nCopies(types.size(), "'" + types.iterator().next() + "'"));
//
//			String sqlDockerNameAll = "'" + dockernames.iterator().next() + "'";
//			String sqlTypeAll = "'" + types.iterator().next() + "'";
//
//			if((Boolean) search_details.get("sort_order"))
//			{
//				sortOrder = "DESC";
//			}
//
//			if((Boolean) search_details.get("sort_column_custom"))
//			{
//				updatedSortColumn = "JSON_UNQUOTE(JSON_EXTRACT(d.custom_fields,CONCAT(\"$[*].\",\"" + sortColumn + "\")))";
//			}
//
//
//			Set<String> device_ids = new HashSet<>();
//			if(search_details.get("value") == null)
//			{
//				String query = "SELECT d.id FROM device d "
//						+ "LEFT JOIN location l ON l.id = d.location_id "
//						+ "LEFT JOIN floor f ON f.id = l.floor_id "
//						+ "LEFT JOIN building b ON b.id = f.building_id "
//						+ "WHERE ('all' = " + sqlDockerNameAll + " or d.docker_name IN (" + sqlDockerNames + ")) "
//						+ "AND ('all' = " + sqlTypeAll + " or d.type IN (" + sqlTypes + ")) "
//						+ "ORDER BY "+ updatedSortColumn + " " + sortOrder
//						+ " LIMIT " + pagesize + " OFFSET " + offset;
//
//				System.out.println("query-- with no search parent :\n"+query);
//				var queryResult = jdbcTemplate.queryForList(query);
//
//				for (Map<String, Object> stringObjectMap : queryResult) {
//					device_ids.add(String.valueOf(stringObjectMap.get("id")));
//				}
//
//				return deviceService.getDevicesByIdList(device_ids);
//			}
//			else
//			{
//				if (search_details.get("column") == null) {
//
//					String query = "SELECT t1.* "
//							+ "FROM "
//							+ "((SELECT d.id, d1.docker_name, d1.type "
//							+ "FROM device d "
//							+ "WHERE JSON_EXTRACT(d.custom_fields,('$[*].*')) "
//							+ "LIKE CONCAT('%','" + search_details.get("value") + "','%')) "
//							+ "UNION "
//							+ "(SELECT d1.id, d1.docker_name, d1.type "
//							+ "FROM device d1 "
//							+ "LEFT JOIN location l ON l.id = d1.location_id "
//							+ "LEFT JOIN floor f ON f.id = l.floor_id "
//							+ "LEFT JOIN building b ON b.id = f.building_id "
//							+ "WHERE CONCAT_WS('',d1.id, IF(d1.user_data_name IS NULL or d1.user_data_name = '', d1.display_name, d1.user_data_name), "
//							+ "IF(d1.user_data_vendor IS NULL or d1.user_data_vendor = '', d1.vendor, d1.user_data_vendor), "
//							+ "IF(d1.user_data_model IS NULL or d1.user_data_model = '', d1.model, d1.user_data_model), d1.ip_address,"
//							+ "d1.mac_address, d1.latitude, d1.longitude, d1.serial_number, d1.warranty, l.name, f.name, b.name) "
//							+ "LIKE CONCAT('%','" + search_details.get("value") + "','%'))) AS t1 "
//							+ "WHERE ('all' = " + sqlDockerNameAll + " or t1.docker_name IN (" + sqlDockerNames + ")) "
//							+ "AND ('all' = " + sqlTypeAll + " or t1.type IN (" + sqlTypes + ")) "
//							+ "ORDER BY "+ updatedSortColumn + " " + sortOrder
//							+ " LIMIT " + pagesize + " OFFSET " + offset;
//
//					//					System.out.println("query--1:\n"+query);
//
//					var queryResult = jdbcTemplate.queryForList(query);
//					for (Map<String, Object> stringObjectMap : queryResult) {
//						device_ids.add(String.valueOf(stringObjectMap.get("id")));
//					}
//				} else {
//					if ((Boolean) search_details.get("custom")) {
//						String query = "SELECT id FROM device "
//								+ "WHERE ('all' = " + sqlDockerNameAll + " or d.docker_name IN (" + sqlDockerNames + ")) "
//								+ "AND ('all' = " + sqlTypeAll + " or d.type IN (" + sqlTypes + ")) "
//								+ "AND JSON_UNQUOTE(JSON_EXTRACT(custom_fields,CONCAT(\"$[*].\",\"" + searchColumn
//								+ "\"))) LIKE CONCAT('%','" + search_details.get("value") + "','%') "
//								+ "ORDER BY "+ updatedSortColumn + " " + sortOrder
//								+ " LIMIT " + pagesize + " OFFSET " + offset;
//
//						var queryResult = jdbcTemplate.queryForList(query);
//						for (Map<String, Object> stringObjectMap : queryResult) {
//							device_ids.add(String.valueOf(stringObjectMap.get("id")));
//						}
//					} else {
//						String query = "SELECT  d.id FROM device d "
//								+ "LEFT JOIN location l ON l.id = d.location_id "
//								+ "LEFT JOIN floor f ON f.id = l.floor_id "
//								+ "LEFT JOIN building b ON b.id = f.building_id "
//								+ "WHERE ('all' = " + sqlDockerNameAll + " or t1.docker_name IN (" + sqlDockerNames + ")) "
//								+ "AND ('all' = " + sqlTypeAll + " or t1.type IN (" + sqlTypes + ")) "
//								+ "AND " + updatedSearchColumn + " LIKE '%" + search_details.get("value") + "%' "
//								+ "ORDER BY "+ updatedSortColumn + " " + sortOrder
//								+ " LIMIT " + pagesize + " OFFSET " + offset;
//
//						//						System.out.println("query--3:\n"+query);
//						var queryResult = jdbcTemplate.queryForList(query);
//						for (Map<String, Object> stringObjectMap : queryResult) {
//							device_ids.add(String.valueOf(stringObjectMap.get("id")));
//						}
//					}
//				}
//			}
//
//			return deviceService.getDevicesByIdList(device_ids);
//		} catch (Exception e) {
//			System.out.println(e);
//			return null;
//		}
//	}
    /**************************************************Search Parent Devices Method************************************************************/

    /**************************************************Sort Devices Method***********************************************************/
    /**
     * Returns devices within a VDMS and docker scope filtered by a status condition and sorted by
     * the requested column (standard or custom field). Returns null on error.
     */
    public Set<DeviceDTO> sortDevices(String username, String vdmsid, String dockername, String condition, Integer pageno,
                                      Integer pagesize, Map<String, Object> sort_details) {
        try {
            // PG-port/Criteria: condition decode + ORDER BY string SQL replaced by
            // DeviceSearchQueryBuilder.sortDeviceIds (ISNULL->CASE, INET_ATON->::inet via inet_val,
            // custom-field jsonpath sort). LinkedHashSet preserves the sorted id order.
            DeviceSearchCriteria scope = DeviceSearchCriteria.from(
                    vdmsid, dockername, condition, new com.alibaba.fastjson.JSONObject(), 123);
            String searchColumn = String.valueOf(sort_details.get("column")).replaceAll("\\s", "");
            boolean sortCustom = Boolean.TRUE.equals(sort_details.get("custom"));
            Set<String> device_ids = new LinkedHashSet<>(deviceSearchQueryBuilder.sortDeviceIds(
                    scope, new DeviceSearchQueryBuilder.SplitSort(searchColumn, sortCustom),
                    pageno, pagesize));

            return deviceService.getDevicesByIdList(vdmsid, device_ids);
        } catch (Exception e) {
            System.out.println(e);
        }
        return null;
    }

    /**************************************************Sort Devices Method***********************************************************/
    /**************************************************Filter Devices Method***********************************************************/
    /**
     * Returns devices within a VDMS and docker scope filtered by a status condition and by the
     * presence of the supplied columns (standard or custom fields). Returns null on error.
     */
    public Set<DeviceDTO> filterDevices(String username, String vdmsid, String dockername, String condition, Integer pageno,
                                        Integer pagesize, List<Map<String, Object>> filter_details) {
        try {
            // PG-port/Criteria: condition decode + filter-present string SQL replaced by
            // DeviceSearchQueryBuilder.filterDeviceIds (each column required IS NOT NULL AND <> '',
            // custom via jsonb_path_query_first).
            DeviceSearchCriteria scope = DeviceSearchCriteria.from(
                    vdmsid, dockername, condition, new com.alibaba.fastjson.JSONObject(), 123);
            List<DeviceSearchQueryBuilder.SplitFilter> filters = new ArrayList<>();
            for (Map<String, Object> f : filter_details) {
                filters.add(new DeviceSearchQueryBuilder.SplitFilter(
                        String.valueOf(f.get("column")), Boolean.TRUE.equals(f.get("custom"))));
            }
            Set<String> device_ids = new HashSet<>(deviceSearchQueryBuilder.filterDeviceIds(
                    scope, filters, pageno, pagesize));
            return deviceService.getDevicesByIdList(vdmsid, device_ids);

        } catch (Exception e) {
            System.out.println(e);
        }
        return null;
    }

    /**
     * Builds the SQL fragment that requires each supplied filter column (standard or custom field)
     * to be non-null and non-empty, joined with AND.
     *
     * @return the generated SQL condition fragment
     */
    public String generateMultiConditionStmt(List<Map<String, Object>> filter_details, String vdms_id, String dockername) {
        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < filter_details.size(); i++) {
            Map<String, Object> tempMap = filter_details.get(i);
            if ((Boolean) filter_details.get(i).get("custom")) {
                // PG-port: jsonb_path_query_first(col::jsonb,'$[*]."field"')#>>'{}' IS NOT NULL / <> '' — validated via direct psql SELECT
                stringBuilder
                        .append(" (jsonb_path_query_first(custom_fields::jsonb, '$[*].\"")
                        .append(tempMap.get("column"))
                        .append("\"') #>> '{}') IS NOT NULL AND ")
                        .append(" (jsonb_path_query_first(custom_fields::jsonb, '$[*].\"")
                        .append(tempMap.get("column"))
                        .append("\"') #>> '{}') <> '' ");
            } else {
                stringBuilder.append(" ").append(this.updateDeviceSearchColumnName(String.valueOf(tempMap.get("column")))).
                        append(" IS NOT NULL AND ").append(this.updateDeviceSearchColumnName(String.valueOf(tempMap.get("column")))).
                        append(" <> '' ");
            }
            if (i != filter_details.size() - 1) {
                stringBuilder.append("AND ");
            }
        }
        return stringBuilder.toString();
    }

    /**************************************************Filter Devices Method***********************************************************/

    /**
     * Maps a logical device search column name to its qualified SQL column expression, defaulting
     * to the display name expression for unknown columns.
     *
     * @return the SQL column expression for the given logical column
     */
    public String updateDeviceSearchColumnName(String searchColumn) {
        String updateSearchColumn;
        switch (searchColumn) {
            case "id": {
                updateSearchColumn = "d.id";
                break;
            }
            case "display_name": {
                // PG-port: IF(c,a,b) -> CASE WHEN c THEN a ELSE b END
                updateSearchColumn = "CASE WHEN d.user_data_name IS NULL or d.user_data_name = '' THEN d.display_name ELSE d.user_data_name END";
                break;
            }
            case "vendor": {
                // PG-port: IF(c,a,b) -> CASE WHEN c THEN a ELSE b END
                updateSearchColumn = "CASE WHEN d.user_data_vendor IS NULL or d.user_data_vendor = '' THEN d.vendor ELSE d.user_data_vendor END";
                break;
            }
            case "model": {
                // PG-port: IF(c,a,b) -> CASE WHEN c THEN a ELSE b END
                updateSearchColumn = "CASE WHEN d.user_data_model IS NULL or d.user_data_model = '' THEN d.model ELSE d.user_data_model END";
                break;
            }
            case "type": {
                updateSearchColumn = "d.type";
                break;
            }
            case "ip_address": {
                updateSearchColumn = "d.ip_address";
                break;
            }
            case "mac_address": {
                updateSearchColumn = "d.mac_address";
                break;
            }
            case "location": {
                updateSearchColumn = "l.name";
                break;
            }
            case "floor": {
                updateSearchColumn = "f.name";
                break;
            }
            case "building": {
                updateSearchColumn = "b.name";
                break;
            }
            case "warranty": {
                updateSearchColumn = "d.warranty";
                break;
            }
            case "latitude": {
                updateSearchColumn = "d.latitude";
                break;
            }
            case "longitude": {
                updateSearchColumn = "d.longitude";
                break;
            }
            case "serial_number": {
                updateSearchColumn = "d.serial_number";
                break;
            }
            case "created_timestamp": {
                updateSearchColumn = "d.created_timestamp";
                break;
            }
            case "assignee_email": {
                updateSearchColumn = "dos.assignee_email";
                break;
            }
            case "updated_timestamp": {
                updateSearchColumn = "d.updated_timestamp";
                break;
            }
            case "description": {
                updateSearchColumn = "d.description";
                break;
            }
            case "asset_group":{
                updateSearchColumn = "d.asset_group";
                break;
            }
            case "category":{
                updateSearchColumn = "d.category";
                break;
            }
            case "sub_category":{
                updateSearchColumn = "d.sub_category";
                break;
            }
            case "assigned_user_email":{
                updateSearchColumn = "d.assigned_user_email";
                break;
            }
            case "username":{
                updateSearchColumn = "ds.username";
                break;
            }
            case "email":{
                updateSearchColumn = "ds.email";
                break;
            }
            default: {
                // PG-port: IF(c,a,b) -> CASE WHEN c THEN a ELSE b END
                updateSearchColumn = "CASE WHEN d.user_data_name IS NULL or d.user_data_name = '' THEN d.display_name ELSE d.user_data_name END";
                break;
            }
        }

        return updateSearchColumn;
    }

    /********************************************Fuzzy Search*****************************************************************************/

    /**
     * Computes the best fuzzy match between the search string and the device's standard and custom
     * fields, then sets the device's matched score, matched column, and matched info accordingly.
     */
    public void getFuzzyValueByDeviceAndSearchString(DeviceDTO device, String search_string) {
        String result = "";
        int fuzzy_value = 0;
        Map<String, Integer> fuzzy_result = new HashMap<String, Integer>();
        Map<String, String> default_map = new HashMap<String, String>();

        default_map.put("id", "Id");
        default_map.put("display_name", "Name");
        default_map.put("user_data_name", "User Name");
        default_map.put("vendor", "Vendor");
        default_map.put("user_data_vendor", "User Vendor");
        default_map.put("mac_address", "Mac Address");
        default_map.put("ip_address", "IP Address");
        default_map.put("model", "Model");
        default_map.put("user_data_model", "User Model");
        default_map.put("location", "Location");
        default_map.put("floor", "Floor");
        default_map.put("building", "Building");
        default_map.put("warranty", "Warranty");
        default_map.put("latitude", "Latitude");
        default_map.put("longitude", "Longitude");
        default_map.put("serial_number", "Serial Number");
        default_map.put("type", "Type");
        default_map.put("created_timestamp", "Created Timestamp");

        for (Map.Entry<String, String> entry : default_map.entrySet()) {
            try {
                Field field = device.getClass().getDeclaredField(entry.getKey());
                field.setAccessible(true);
                fuzzy_result.put(entry.getValue(), this.getFuzzyValueByBaseStringAndSearchString(field.get(device), search_string));
            } catch (Exception e) {
                System.out.println(e);
            }
        }

        if (device.getCustom_fields() != null) {
            JSONArray json;
            try {
                json = new JSONArray(device.getCustom_fields().toString());
                if (json != null && json.length() > 0) {
                    for (int i = 0; i < json.length(); i++) {
                        JSONObject json_object = json.getJSONObject(i);
                        json_object.keys().forEachRemaining(key -> {
                            try {
                                Object value = json_object.get(key.toString());
                                fuzzy_result.put(key.toString(), this.getFuzzyValueByBaseStringAndSearchString(value, search_string));
                            } catch (Exception e) {
                                System.out.println(e);
                            }
                        });
                    }
                }
            } catch (Exception e) {
                System.out.println(e);
            }

        }


        for (Map.Entry<String, Integer> entry : fuzzy_result.entrySet()) {
            if (entry.getValue() > fuzzy_value) {
                if (entry.getKey().equals("User Vendor")) {
                    result = "Vendor";
                } else if (entry.getKey().equals("User Model")) {
                    result = "Model";
                } else if (entry.getKey().equals("User Name")) {
                    result = "Name";
                } else {
                    result = entry.getKey();
                }
                fuzzy_value = entry.getValue();
            }
        }

        //		System.out.println("result :" + result);
        if (result.length() > 0) {
            device.setMatched_score(fuzzy_value);
            device.setMatched_column(result);
            device.setMatched_info(result + " - " + String.valueOf(fuzzy_value) + "%");
        } else {
            device.setMatched_score(fuzzy_value);
            device.setMatched_info("");
        }

        //		System.out.println(device.toString());
    }


    /**
     * Returns the case-insensitive fuzzy match ratio between the base value and search string, or 0
     * when the base value is null or matching fails.
     */
    public Integer getFuzzyValueByBaseStringAndSearchString(Object base_string, String search_string) {
        try {
            if (base_string != null) {
                return FuzzySearch.ratio(search_string.toString().toLowerCase().trim(), base_string.toString().toLowerCase().trim());
            }
        } catch (Exception e) {
            System.out.println("Failed to Fetch the fuzzy Score");
        }
        return 0;
    }

    /**
     * Sorts the given devices in descending order of their matched fuzzy score.
     *
     * @return the devices sorted by descending matched score
     */
    public List<DeviceDTO> sortFilteredDevicesByMatchedScore(List<DeviceDTO> devices) {
        try {
            if (devices.size() > 0) {
                Comparator<DeviceDTO> compareById = (DeviceDTO o1, DeviceDTO o2) -> o1.getMatched_score().compareTo(o2.getMatched_score());
                Collections.sort(devices, compareById.reversed());
                return devices;
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        return devices;
    }

    /**
     * Updates the fuzzy match score on each device based on the search details, handling all-column,
     * custom-field, and specific-column searches.
     *
     * @return the same devices with their match scores populated
     */
    public Set<DeviceDTO> updateFuzzyMatchScore(Set<DeviceDTO> filteredDevices, Map<String, Object> search_details) {
        for (DeviceDTO filteredDevice : filteredDevices) {
            try {
                if (search_details.get("value") != null) {
                    if (search_details.get("column") == null) {
                        this.getFuzzyValueByDeviceAndSearchString(filteredDevice, search_details.get("value").toString());
                    } else {
                        String searchColumn = String.valueOf(search_details.get("column")).replaceAll("\\s", "");
                        if ((Boolean) search_details.get("custom")) {
                            if (filteredDevice.getCustom_fields() != null) {
                                try {
                                    JSONArray json = new JSONArray(filteredDevice.getCustom_fields().toString());
                                    if (json != null && json.length() > 0) {
                                        for (int i = 0; i < json.length(); i++) {
                                            JSONObject json_object = json.getJSONObject(i);
                                            if (json_object.has(searchColumn)) {
                                                int fuzzy_value = this.getFuzzyValueByBaseStringAndSearchString(json_object.getString(searchColumn), search_details.get("value").toString());

                                                filteredDevice.setMatched_score(fuzzy_value);
                                                filteredDevice.setMatched_column(searchColumn);
                                                filteredDevice.setMatched_info(searchColumn + " - " + String.valueOf(fuzzy_value) + "%");
                                            }
                                        }
                                    }
                                } catch (Exception e) {
                                    System.out.println(e);
                                }

                            }

                        } else {

                            Map<String, String> default_map = new HashMap<String, String>();

                            default_map.put("id", "Id");
                            default_map.put("display_name", "Name");
                            default_map.put("user_data_name", "Name");
                            default_map.put("vendor", "Vendor");
                            default_map.put("user_data_vendor", "Vendor");
                            default_map.put("mac_address", "Mac Address");
                            default_map.put("ip_address", "IP Address");
                            default_map.put("model", "Model");
                            default_map.put("user_data_model", "Model");
                            default_map.put("location", "Location");
                            default_map.put("floor", "Floor");
                            default_map.put("building", "Building");
                            default_map.put("warranty", "Warranty");
                            default_map.put("latitude", "Latitude");
                            default_map.put("longitude", "Longitude");
                            default_map.put("serial_number", "Serial Number");

                            Field field = filteredDevice.getClass().getDeclaredField(searchColumn);
                            field.setAccessible(true);

                            int fuzzy_value = this.getFuzzyValueByBaseStringAndSearchString(field.get(filteredDevice), search_details.get("value").toString());

                            if (searchColumn.equals("vendor")) {
                                int new_fuzzy_value = this.getFuzzyValueByBaseStringAndSearchString(filteredDevice.getUser_data_vendor(), search_details.get("value").toString());
                                if (new_fuzzy_value > fuzzy_value) {
                                    fuzzy_value = new_fuzzy_value;
                                }
                            } else if (searchColumn.equals("model")) {
                                int new_fuzzy_value = this.getFuzzyValueByBaseStringAndSearchString(filteredDevice.getUser_data_model(), search_details.get("value").toString());
                                if (new_fuzzy_value > fuzzy_value) {
                                    fuzzy_value = new_fuzzy_value;
                                }
                            } else if (searchColumn.equals("display_name")) {
                                int new_fuzzy_value = this.getFuzzyValueByBaseStringAndSearchString(filteredDevice.getUser_data_name(), search_details.get("value").toString());
                                if (new_fuzzy_value > fuzzy_value) {
                                    fuzzy_value = new_fuzzy_value;
                                }
                            }

                            filteredDevice.setMatched_score(fuzzy_value);
                            filteredDevice.setMatched_column(default_map.get(searchColumn));
                            filteredDevice.setMatched_info(default_map.get(searchColumn) + " - " + String.valueOf(fuzzy_value) + "%");
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println(e);
            }
        }

        return filteredDevices;
    }

    /********************************************Fuzzy Search*****************************************************************************/

    /***********************************************Get Device Info By Custom Fields*******************************************************/

    /**
     * Finds a single device within the VDMS and docker scope whose custom field matches the given
     * key/value pair and returns its full device details.
     *
     * @return a list containing the matched device's details, or empty when none match
     */
    public List<DeviceDTO> getDeviceInfoByCustomFields(String username, String vdmsid, String dockername, com.alibaba.fastjson.JSONObject custom_fields) {

        // PG-port/Criteria: custom-field match SQL replaced by DeviceSearchQueryBuilder.customFieldDeviceIds.
        List<String> ids = deviceSearchQueryBuilder.customFieldDeviceIds(
                vdmsid, dockername, custom_fields.getString("key"), custom_fields.getString("value"), 1);

        List<DeviceDTO> devices = new ArrayList<>();
        for (String id : ids) {
            try {
                devices.add(deviceService.getDeviceByDeviceId(username, vdmsid, dockername, id));
            } catch (Exception e) {
                System.out.println(e);
            }
        }

        return devices;
    }

    /***********************************************Get Device Info By Custom Fields*******************************************************/

    /****************************************************Multiple Keyword Search Sort Filter Merged*********************************************************/

    /**
     * Performs a combined, paginated search, sort, and filter over devices using the supplied
     * details and onboarding/status condition, applying fuzzy ranking when searching without an
     * explicit sort. Returns null on error.
     */
    public Set<DeviceDTO> multipleKeywordSearchSortFilterDevices(String username, String vdmsid, String dockername, String condition,
                                                                 Integer pageno, Integer pagesize,
                                                                 com.alibaba.fastjson.JSONObject search_sort_filter_details, Integer onboard_status) {
        try {
            // PG-port/Criteria (2026-06-12): dynamic SQL replaced by type-safe DeviceSearchQueryBuilder.
            // Documented fixes vs the old string SQL: bound params (no injection / no device_ids
            // double-quote PG error), REGEXP_REPLACE 'g' flag, timestamp-sort bigint='' crash gone,
            // assignee/username sort joins what it references, onboardpending/onboardcompleted now
            // honoured here too. The id-fetch -> getDevicesByIdList -> fuzzy re-rank workflow is unchanged.
            DeviceSearchCriteria criteria = DeviceSearchCriteria.from(
                    vdmsid, dockername, condition, search_sort_filter_details, onboard_status);
            Set<String> device_ids = new LinkedHashSet<>(
                    deviceSearchQueryBuilder.findIds(criteria, pageno, pagesize));
            Set<DeviceDTO> searchSortFilteredDevices = deviceService.getDevicesByIdList(vdmsid, device_ids);
            if (search_sort_filter_details.getJSONObject("sort_details") != null) {
                return searchSortFilteredDevices;
            } else {
                if (search_sort_filter_details.getJSONArray("search_details") != null) {
                    com.alibaba.fastjson.JSONArray search_details = search_sort_filter_details.getJSONArray("search_details");
                    Map<String, Object> fuzzySearchDetails = new HashMap<>();
                    fuzzySearchDetails.put("column", search_details.getJSONObject(0).get("column"));
                    fuzzySearchDetails.put("custom", search_details.getJSONObject(0).get("custom"));
                    fuzzySearchDetails.put("value", search_details.getJSONObject(0).get("value"));
                    //update fuzzy score
                    Set<DeviceDTO> fuzzyScoreUpdatedDevices = this.updateFuzzyMatchScore(searchSortFilteredDevices, fuzzySearchDetails);
                    if (fuzzyScoreUpdatedDevices != null) {
                        List<DeviceDTO> filtered_devices = new ArrayList<DeviceDTO>(fuzzyScoreUpdatedDevices);
                        filtered_devices = this.sortFilteredDevicesByMatchedScore(filtered_devices);
                        return new LinkedHashSet<DeviceDTO>(filtered_devices);
                    }
                } else {
                    return searchSortFilteredDevices;
                }

            }
        } catch (Exception e) {
            System.out.println(e);
        }
        return null;
    }

    private String generateMultipleKeywordSearchAndFilterCustomQuery(com.alibaba.fastjson.JSONObject searchAndFilterDetails, String vdms_id) {
        StringBuilder searchAndFilterCustomQuery = new StringBuilder();


        if (searchAndFilterDetails.containsKey("filter_details") && searchAndFilterDetails.getJSONObject("filter_details") != null) {
            String filterCustomQuery = generateFilterCustomQuery(searchAndFilterDetails.getJSONObject("filter_details"), vdms_id);
            searchAndFilterCustomQuery.append(filterCustomQuery);
        }

        if (searchAndFilterDetails.containsKey("search_details") && searchAndFilterDetails.getJSONArray("search_details") != null) {
            for (int i = 0; i < searchAndFilterDetails.getJSONArray("search_details").size(); i++) {
                String searchQuery = generateSearchQuery(searchAndFilterDetails.getJSONArray("search_details").getJSONObject(i), searchAndFilterDetails);
                searchAndFilterCustomQuery.append(searchQuery);
            }
        }

//        if (searchAndFilterDetails.containsKey("sort_details") && searchAndFilterDetails.getJSONObject("sort_details") != null) {
//            String sortQuery = generateSortQuery(searchAndFilterDetails.getJSONObject("sort_details"));
//            searchAndFilterCustomQuery.append(sortQuery);
//        }


        return searchAndFilterCustomQuery.toString();
    }

    /**
     * Builds the SQL filter fragment from the column and feature filter details.
     *
     * @return the generated SQL filter fragment
     */
    public String generateFilterCustomQuery(com.alibaba.fastjson.JSONObject filter_details, String vdms_id) {
        StringBuilder stringBuilder = new StringBuilder();
        if (filter_details.getJSONArray("column_details") != null) {
            String columnFilterQuery = generateColumnFilterQuery(filter_details.getJSONArray("column_details"));
            stringBuilder.append(columnFilterQuery);
        }
        if (filter_details.getJSONArray("feature_details") != null) {
            String featureFilterQuery = generateFeatureFilterQuery(filter_details.getJSONArray("feature_details"), vdms_id);
            stringBuilder.append(featureFilterQuery);
        }
        if (filter_details.getString("onboard_details") != null) {
//            String onboardDataFilterQuery = generateOnboardDataFilterQuery(filter_details.getString("onboard_details"), vdms_id);
//            stringBuilder.append(onboardDataFilterQuery);
        }
        return stringBuilder.toString();
    }

    /**
     * Builds the SQL fragment that filters devices by presence or absence of the given columns,
     * handling custom fields and special columns such as assignee email, type, asset group,
     * category/sub-category, and OS type.
     *
     * @return the generated SQL column filter fragment
     */
    public String generateColumnFilterQuery(com.alibaba.fastjson.JSONArray column_details) {
        StringBuilder stringBuilder = new StringBuilder();
        if (column_details.size() > 0) {
            stringBuilder.append(" AND (");
            for (int i = 0; i < column_details.size(); i++) {
                stringBuilder.append("( ");
                com.alibaba.fastjson.JSONObject tempMap = column_details.getJSONObject(i);
                if ((Boolean) tempMap.get("custom")) {
                    if (tempMap.get("condition").equals("is_present")) {
                        // PG-port: jsonb_path_query_first(col::jsonb,'$[*]."field"')#>>'{}' IS NOT NULL / <> '' / <> 'null'
                        // Restored MySQL '<> null' arm: #>>'{}' returns text 'null' (not SQL NULL) for JSON string "null", so IS NOT NULL alone is insufficient
                        stringBuilder
                                .append(" (jsonb_path_query_first(custom_fields::jsonb, '$[*].\"")
                                .append(tempMap.get("column"))
                                .append("\"') #>> '{}') IS NOT NULL AND ")
                                .append(" (jsonb_path_query_first(custom_fields::jsonb, '$[*].\"")
                                .append(tempMap.get("column"))
                                .append("\"') #>> '{}') <> '' AND ")
                                .append(" (jsonb_path_query_first(custom_fields::jsonb, '$[*].\"")
                                .append(tempMap.get("column"))
                                .append("\"') #>> '{}') <> 'null'");

                    } else if (tempMap.get("condition").equals("is_not_present")) {
                        // PG-port: jsonb_path_query_first(col::jsonb,'$[*]."field"')#>>'{}' IS NULL OR = '' OR = 'null'
                        // Restored MySQL '= null' arm: #>>'{}' returns text 'null' (not SQL NULL) for JSON string "null"; dropped arm mis-classified those rows
                        stringBuilder
                                .append(" (jsonb_path_query_first(custom_fields::jsonb, '$[*].\"")
                                .append(tempMap.get("column"))
                                .append("\"') #>> '{}') IS NULL OR ")
                                .append(" (jsonb_path_query_first(custom_fields::jsonb, '$[*].\"")
                                .append(tempMap.get("column"))
                                .append("\"') #>> '{}') = '' OR ")
                                .append(" (jsonb_path_query_first(custom_fields::jsonb, '$[*].\"")
                                .append(tempMap.get("column"))
                                .append("\"') #>> '{}') = 'null'");

                    }

                } else {
                    if (tempMap.get("condition").equals("is_present")) {
                        if (tempMap.get("value") != null) {
                            if (tempMap.get("column").equals("assignee_email")) {
                                stringBuilder.append("dos.assignee_email").
                                        append(" = '").append(tempMap.get("value")).
                                        append("' OR dosa.email = '").append(tempMap.get("value")).append("'");

                            } else if (tempMap.get("column").equals("type")) {
                                com.alibaba.fastjson.JSONArray asset_types = tempMap.getJSONArray("value");
                                stringBuilder.append(this.updateDeviceSearchColumnName(String.valueOf(tempMap.get("column")))).
                                        append((" IN ("));
                                for (int j = 0; j < asset_types.size(); j++) {
                                    String asset_type = null;
                                    asset_type = asset_types.getString(j);
                                    stringBuilder.append(" '").append(asset_type).append("' ");
                                    if (j < asset_types.size() - 1) {
                                        stringBuilder.append(",");
                                    }
                                }
                                stringBuilder.append(")");
                            } else if (tempMap.get("column").equals("asset_group")) {
                                com.alibaba.fastjson.JSONArray asset_groups = tempMap.getJSONArray("value");
                                stringBuilder.append(this.updateDeviceSearchColumnName(String.valueOf(tempMap.get("column")))).
                                        append((" IN ("));
                                for (int j = 0; j < asset_groups.size(); j++) {
                                    String asset_group = null;
                                    asset_group = asset_groups.getString(j);
                                    stringBuilder.append(" '").append(asset_group).append("' ");
                                    if (j < asset_groups.size() - 1) {
                                        stringBuilder.append(",");
                                    }
                                }
                                stringBuilder.append(")");
                            } else if (tempMap.get("column").equals("category")){
                                com.alibaba.fastjson.JSONObject categoryMap = tempMap.getJSONObject("value");
                                List<String> categoryClauses = new ArrayList<>();

                                for (String categoryKey : categoryMap.keySet()) {
                                    com.alibaba.fastjson.JSONArray subCategories = categoryMap.getJSONArray(categoryKey);
                                    StringBuilder categoryClause = new StringBuilder();

                                    categoryClause.append("(")
                                            .append(this.updateDeviceSearchColumnName(String.valueOf(tempMap.get("column"))))
                                            .append(" = '").append(categoryKey).append("'");

                                    if (subCategories != null && subCategories.size() > 0) {
                                        categoryClause.append(" AND ")
                                                .append(this.updateDeviceSearchColumnName("sub_category"))
                                                .append(" IN (");

                                        for (int j = 0; j < subCategories.size(); j++) {
                                            categoryClause.append("'").append(subCategories.getString(j)).append("'");
                                            if (j < subCategories.size() - 1) {
                                                categoryClause.append(", ");
                                            }
                                        }

                                        categoryClause.append(")");
                                    }

                                    categoryClause.append(")");
                                    categoryClauses.add(categoryClause.toString());
                                }

                                stringBuilder.append(String.join(" OR ", categoryClauses));

                            } else if(tempMap.get("column").equals("assigned_user_email")){
                                stringBuilder.append("d.assigned_user_email").
                                        append(" = '").append(tempMap.get("value")).
                                        append("'");
                            } else if(tempMap.get("column").equals("os_type")){
                                stringBuilder.append("ds.os_type").
                                        append(" = '").append(tempMap.get("value")).
                                        append("'");
                            }else {
                                stringBuilder.append(this.updateDeviceSearchColumnName(String.valueOf(tempMap.get("column")))).
                                        append(" = '").append(tempMap.get("value")).
                                        append("'");
                            }
                        } else {
                            stringBuilder.append(this.updateDeviceSearchColumnName(String.valueOf(tempMap.get("column")))).
                                    append(" IS NOT NULL AND ").append(this.updateDeviceSearchColumnName(String.valueOf(tempMap.get("column")))).
                                    append(" <> ''");
                        }

                    } else if (tempMap.get("condition").equals("is_not_present")) {
                        if (tempMap.get("value") != null) {
                            if (tempMap.get("column").equals("type")) {
                                com.alibaba.fastjson.JSONArray asset_types = tempMap.getJSONArray("value");
                                stringBuilder.append(this.updateDeviceSearchColumnName(String.valueOf(tempMap.get("column")))).
                                        append((" NOT IN ("));
                                for (int j = 0; j < asset_types.size(); j++) {
                                    String asset_type = null;
                                    asset_type = asset_types.getString(j);
                                    stringBuilder.append(" '").append(asset_type).append("' ");
                                    if (j < asset_types.size() - 1) {
                                        stringBuilder.append(",");
                                    }
                                }
                                stringBuilder.append(")");
                            } else if (tempMap.get("column").equals("asset_group")){
                                com.alibaba.fastjson.JSONArray asset_groups = tempMap.getJSONArray("value");
                                stringBuilder.append(this.updateDeviceSearchColumnName(String.valueOf(tempMap.get("column")))).
                                        append((" NOT IN ("));
                                for (int j = 0; j < asset_groups.size(); j++) {
                                    String asset_group = null;
                                    asset_group = asset_groups.getString(j);
                                    stringBuilder.append(" '").append(asset_group).append("' ");
                                    if (j < asset_groups.size() - 1) {
                                        stringBuilder.append(",");
                                    }
                                }
                                stringBuilder.append(")");
                            } else if (tempMap.get("column").equals("category")){
                                com.alibaba.fastjson.JSONObject categoryMap = tempMap.getJSONObject("value");
                                List<String> categoryClauses = new ArrayList<>();

                                for (String categoryKey : categoryMap.keySet()) {
                                    com.alibaba.fastjson.JSONArray subCategories = categoryMap.getJSONArray(categoryKey);
                                    StringBuilder categoryClause = new StringBuilder();

                                    categoryClause.append("(")
                                            .append(this.updateDeviceSearchColumnName(String.valueOf(tempMap.get("column"))))
                                            .append(" = '").append(categoryKey).append("'");

                                    if (subCategories != null && subCategories.size() > 0) {
                                        categoryClause.append(" AND ")
                                                .append(this.updateDeviceSearchColumnName("sub_category"))
                                                .append(" NOT IN (");

                                        for (int j = 0; j < subCategories.size(); j++) {
                                            categoryClause.append("'").append(subCategories.getString(j)).append("'");
                                            if (j < subCategories.size() - 1) {
                                                categoryClause.append(", ");
                                            }
                                        }

                                        categoryClause.append(")");
                                    }

                                    categoryClause.append(")");
                                    categoryClauses.add(categoryClause.toString());
                                }

                                stringBuilder.append(String.join(" OR ", categoryClauses));

                            }else {
                                stringBuilder.append(this.updateDeviceSearchColumnName(String.valueOf(tempMap.get("column")))).
                                        append("<>'").append(tempMap.get("value")).
                                        append("'");
                            }
                        } else {
                            stringBuilder.append(this.updateDeviceSearchColumnName(String.valueOf(tempMap.get("column")))).
                                    append(" IS NULL OR ").append(this.updateDeviceSearchColumnName(String.valueOf(tempMap.get("column")))).
                                    append(" = ''");
                        }
                    }
                }
                stringBuilder.append(")");
                if (i < column_details.size() - 1) {
                    stringBuilder.append(" AND ");
                }
            }
            stringBuilder.append(")");

        }

        return stringBuilder.toString();
    }

    /**
     * Builds the SQL search fragment for a single keyword, matching across all columns or a specific
     * standard or custom field and applying the requested condition (contains, equals, starts with,
     * etc.).
     *
     * @return the generated SQL search fragment
     */
    public String generateSearchQuery(com.alibaba.fastjson.JSONObject search_details, com.alibaba.fastjson.JSONObject searhSortFilterDetails) {
        StringBuilder stringBuilder = new StringBuilder();
        String searchColumn = String.valueOf(search_details.get("column")).replaceAll("\\s", "");
        String searchTerm = search_details.getString("value");
        String searchTermWithoutSpecialCharacters = searchTerm.replaceAll("[ -.!\t_+#~`@$%^&*()=;:<>?,/{}|\\\\]", "");
        String updatedSearchColumn = this.updateDeviceSearchColumnName(searchColumn);
        StringBuilder searchColumnValue = new StringBuilder();
        StringBuilder searchColumnValueWithoutSpecialCharacters = new StringBuilder();

        if (search_details.get("column") == null) {
            // PG-port: IF(c,a,b) -> CASE WHEN c THEN a ELSE b END (three IF occurrences already done above)
            // PG-port: JSON_EXTRACT(col,'$[*].*') -> jsonb_path_query_array(col::jsonb,'$[*].*')::text; IF(...) -> CASE WHEN ... THEN ... ELSE '' END; validated via direct psql SELECT
            searchColumnValue.append("LOWER(CONCAT_WS('±','',d.id, CASE WHEN d.user_data_name IS NULL or d.user_data_name = '' THEN d.display_name ELSE d.user_data_name END,")
                    .append("CASE WHEN d.user_data_vendor IS NULL or d.user_data_vendor = '' THEN d.vendor ELSE d.user_data_vendor END, ")
                    .append("CASE WHEN d.user_data_model IS NULL or d.user_data_model = '' THEN d.model ELSE d.user_data_model END, d.type, d.description,  ")
                    .append("d.ip_address, d.mac_address, d.latitude, d.longitude, d.serial_number, d.warranty,  d.created_timestamp,l.name, f.name, " +
                            "b.name, dos.assignee_email , dosa.email, ds.username, ds.email,COALESCE(CASE WHEN LOWER(REGEXP_REPLACE(jsonb_path_query_array(d.custom_fields::jsonb, '$[*].*')::text, '[-.!\\t_+#~`@$%^&*()=;:<>?,/{}|\\'' ]', ''))" + this.generateConditionedQueryForCustomFields(search_details) + " THEN '" + searchTermWithoutSpecialCharacters + "' ELSE '' END, ''),''))");
        } else {
            if ((Boolean) search_details.get("custom")) {
                // PG-port: jsonb_path_query_first(col::jsonb,'$[*]."field"')#>>'{}' inside LOWER(CONCAT_WS(...)) — validated via direct psql SELECT
                searchColumnValue
                        .append("LOWER(CONCAT_WS('',(jsonb_path_query_first(custom_fields::jsonb, '$[*].\"")
                        .append(searchColumn)
                        .append("\"') #>> '{}'),''))");
            } else {
                searchColumnValue
                        .append("LOWER(CONCAT_WS('',")
                        .append(updatedSearchColumn)
                        .append(",''))");
            }
        }
        searchColumnValueWithoutSpecialCharacters
                .append("REGEXP_REPLACE(")
                .append(searchColumnValue)
                .append(", '[ -.!\t_+#~`@$%^&*()=;:<>?,/{}|\\]', '')");
        if (search_details.get("column") == null) {
            stringBuilder
                    .append(" AND (").append(searchColumnValueWithoutSpecialCharacters)
                    .append(this.generateConditionedQueryForAll(search_details))
                    .append(" )");
        } else {
            stringBuilder
                    .append(" AND (").append(searchColumnValueWithoutSpecialCharacters)
                    .append(this.generateConditionedQuery(search_details))
                    .append(" )");
        }

        return stringBuilder.toString();
    }

    private String generateConditionedQueryForAll(com.alibaba.fastjson.JSONObject search_details) {
        StringBuilder stringBuilder = new StringBuilder();
        String searchTermWithoutSpecialCharacters = search_details.getString("value").replaceAll("[ -.!\t_+#~`@$%^&*()=;:<>?,/{}|\\\\]", "");

        if (search_details.containsKey("condition") && search_details.getString("condition") != null) {
            switch (search_details.getString("condition")) {
                /*By Default the search is done for contains*/
                case "contains": {
                    stringBuilder
                            .append(" LIKE LOWER('%")
                            .append(searchTermWithoutSpecialCharacters)
                            .append("%')");
                    break;
                }
                //DONE
                case "does_not_contain": {
                    stringBuilder
                            .append(" NOT LIKE LOWER('%")
                            .append(searchTermWithoutSpecialCharacters)
                            .append("%')");
                    break;
                }
                //DONE
                case "equal_to": {
                    stringBuilder
                            .append(" LIKE LOWER('%±")
                            .append(searchTermWithoutSpecialCharacters)
                            .append("±%')");
                    break;
                }
                case "not_equal_to": {
                    stringBuilder
                            .append(" NOT LIKE LOWER('%±")
                            .append(searchTermWithoutSpecialCharacters)
                            .append("±%')");
                    break;
                }
                case "starts_with": {
                    stringBuilder
                            .append(" LIKE LOWER('%±")
                            .append(searchTermWithoutSpecialCharacters)
                            .append("%')");
                    break;
                }
                case "ends_with": {
                    stringBuilder
                            .append(" LIKE LOWER('%")
                            .append(searchTermWithoutSpecialCharacters)
                            .append("±%')");
                    break;
                }
                default:
                    System.out.println("Entered default case in search condition");
                    break;
            }
        }

        if (stringBuilder.toString().equalsIgnoreCase("")) {
            stringBuilder
                    .append(" LIKE '%")
                    .append(searchTermWithoutSpecialCharacters)
                    .append("%'");
        }

        return stringBuilder.toString();
    }


    private String generateConditionedQueryForCustomFields(com.alibaba.fastjson.JSONObject search_details) {
        StringBuilder stringBuilder = new StringBuilder();
        String searchTermWithoutSpecialCharacters = search_details.getString("value").replaceAll("[ -.!\t_+#~`@$%^&*()=;:<>?,/{}|\\\\]", "");

        if (search_details.containsKey("condition") && search_details.getString("condition") != null) {
            switch (search_details.getString("condition")) {
                /*By Default the search is done for contains*/

                case "contains": {
                    stringBuilder
                            .append(" LIKE LOWER('%")
                            .append(searchTermWithoutSpecialCharacters)
                            .append("%')");
                    break;
                }
                //DONE
                case "does_not_contain": {
                    stringBuilder
                            .append(" LIKE LOWER('%")
                            .append(searchTermWithoutSpecialCharacters)
                            .append("%')");
                    break;
                }
                case "equal_to": {
                    stringBuilder
                            .append(" LIKE LOWER('%\"")
                            .append(searchTermWithoutSpecialCharacters)
                            .append("\"%')");
                    break;
                }
                case "not_equal_to": {
                    stringBuilder
                            .append(" LIKE LOWER('%\"")
                            .append(searchTermWithoutSpecialCharacters)
                            .append("\"%')");
                    break;
                }
                case "starts_with": {
                    stringBuilder
                            .append(" LIKE LOWER('%\"")
                            .append(searchTermWithoutSpecialCharacters)
                            .append("%')");
                    break;
                }
                case "ends_with": {
                    stringBuilder
                            .append(" LIKE LOWER('%")
                            .append(searchTermWithoutSpecialCharacters)
                            .append("\"%')");
                    break;
                }
                default:
                    System.out.println("Entered default case in search condition");
                    break;
            }
        }


        if (stringBuilder.toString().equalsIgnoreCase("")) {
            stringBuilder
                    .append(" LIKE '%")
                    .append(searchTermWithoutSpecialCharacters)
                    .append("%'");
        }

        return stringBuilder.toString();
    }

    /**
     * Builds the SQL ORDER BY fragment from the sort details, supporting standard columns, custom
     * fields, numeric IP ordering, and a default order by updated timestamp.
     *
     * @return the generated SQL ORDER BY fragment
     */
    public String generateSortQuery(com.alibaba.fastjson.JSONObject sort_details) {
        StringBuilder stringBuilder = new StringBuilder();
        if (sort_details.containsKey("sort_details") && sort_details.getJSONObject("sort_details") != null) {
            com.alibaba.fastjson.JSONObject sort_details_object = sort_details.getJSONObject("sort_details");
            String searchColumn = String.valueOf(sort_details_object.get("column")).replaceAll("\\s", "");
            String updatedSearchColumn = this.updateDeviceSearchColumnName(searchColumn);

            if ((Boolean) sort_details_object.get("custom")) {
                // PG-port: ORDER BY MySQL ->>'$[*].field' -> jsonb_path_query_first(...)#>>'{}' sort key
                stringBuilder
                        .append(" ORDER BY (jsonb_path_query_first(custom_fields::jsonb, '$[*].\"")
                        .append(searchColumn)
                        .append("\"') #>> '{}' IS NULL OR jsonb_path_query_first(custom_fields::jsonb, '$[*].\"")
                        .append(searchColumn)
                        .append("\"') #>> '{}' = ''), jsonb_path_query_first(custom_fields::jsonb, '$[*].\"")
                        .append(searchColumn)
                        .append("\"') #>> '{}'");
            } else {
                if (searchColumn.equals("ip_address")) {
                    // PG-port: INET_ATON(col) -> col::inet for numeric IP ordering (column cast)
                    updatedSearchColumn = "(" + updatedSearchColumn + " IS NULL)," + updatedSearchColumn + "::inet "; // PG-port: ISNULL->IS NULL; INET_ATON(col)->col::inet for numeric IP sort (column cast, Hibernate-safe; throws on invalid IP string)
                } else {
                    // PG-port: ISNULL(x) -> (x IS NULL)
                    updatedSearchColumn = "(" + updatedSearchColumn + " IS NULL)," + updatedSearchColumn + " = '',  " + updatedSearchColumn + " ";
                    if (searchColumn.equals("created_timestamp") || searchColumn.equals("updated_timestamp")) {
                        updatedSearchColumn = updatedSearchColumn + " DESC, d.id";
                    }
                }

                stringBuilder
                        .append(" ORDER BY ")
                        .append(updatedSearchColumn);
            }
        } else {
            // PG-port: ISNULL(x) -> (x IS NULL)
            stringBuilder
                    .append(" ORDER BY (d.updated_timestamp IS NULL), d.updated_timestamp DESC, d.id ");
        }

        return stringBuilder.toString();
    }

    /**
     * Like the paginated combined search/sort/filter but without pagination, intended for asset
     * export. Applies fuzzy ranking when searching without an explicit sort. Returns null on error.
     */
    public Set<DeviceDTO> multipleKeywordSearchSortFilterDevicesForAssetExport(String username, String vdmsid, String dockername, String condition,
                                                                               com.alibaba.fastjson.JSONObject search_sort_filter_details, Integer onboard_status) {
        try {
            // PG-port/Criteria (2026-06-12): same builder as the paged variant, without pagination.
            DeviceSearchCriteria criteria = DeviceSearchCriteria.from(
                    vdmsid, dockername, condition, search_sort_filter_details, onboard_status);
            Set<String> device_ids = new LinkedHashSet<>(deviceSearchQueryBuilder.findAllIds(criteria));
            Set<DeviceDTO> searchSortFilteredDevices = deviceService.getDevicesByIdList(vdmsid, device_ids);
            if (search_sort_filter_details.getJSONObject("sort_details") != null) {
                return searchSortFilteredDevices;
            } else {
                if (search_sort_filter_details.getJSONArray("search_details") != null) {
                    com.alibaba.fastjson.JSONArray search_details = search_sort_filter_details.getJSONArray("search_details");
                    Map<String, Object> fuzzySearchDetails = new HashMap<>();
                    fuzzySearchDetails.put("column", search_details.getJSONObject(0).get("column"));
                    fuzzySearchDetails.put("custom", search_details.getJSONObject(0).get("custom"));
                    fuzzySearchDetails.put("value", search_details.getJSONObject(0).get("value"));
                    //update fuzzy score
                    Set<DeviceDTO> fuzzyScoreUpdatedDevices = this.updateFuzzyMatchScore(searchSortFilteredDevices, fuzzySearchDetails);
                    if (fuzzyScoreUpdatedDevices != null) {
                        List<DeviceDTO> filtered_devices = new ArrayList<DeviceDTO>(fuzzyScoreUpdatedDevices);
                        filtered_devices = this.sortFilteredDevicesByMatchedScore(filtered_devices);
                        return new LinkedHashSet<DeviceDTO>(filtered_devices);
                    }
                } else {
                    return searchSortFilteredDevices;
                }
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        return null;
    }

    /**
     * Returns the total count of devices matching the combined search and filter criteria and the
     * onboarding/status condition. Returns null on error.
     *
     * @return the matching device count as a string, or null on error
     */
    public String multipleKeywordSearchSortFilterDevicesCount(String username, String vdmsid, String dockername, String condition,
                                                              com.alibaba.fastjson.JSONObject search_sort_filter_details, Integer onboard_status) {
        try {
            // PG-port/Criteria (2026-06-12): COUNT of the same builder subquery.
            DeviceSearchCriteria criteria = DeviceSearchCriteria.from(
                    vdmsid, dockername, condition, search_sort_filter_details, onboard_status);
            return String.valueOf(deviceSearchQueryBuilder.count(criteria));
        } catch (Exception e) {
            System.out.println(e);
        }
        return null;
    }

    private String generateConditionedQuery(com.alibaba.fastjson.JSONObject search_details) {
        StringBuilder stringBuilder = new StringBuilder();
        String searchTermWithoutSpecialCharacters = search_details.getString("value").replaceAll("[ -.!\t_+#~`@$%^&*()=;:<>?,/{}|\\\\]", "");

        if (search_details.containsKey("condition") && search_details.getString("condition") != null) {
            switch (search_details.getString("condition")) {
                /*By Default the search is done for contains*/
                case "contains": {
                    stringBuilder
                            .append(" LIKE LOWER('%")
                            .append(searchTermWithoutSpecialCharacters)
                            .append("%')");
                    break;
                }
                //DONE
                case "does_not_contain": {
                    stringBuilder
                            .append(" NOT LIKE LOWER('%")
                            .append(searchTermWithoutSpecialCharacters)
                            .append("%')");
                    break;
                }
                //DONE
                case "equal_to": {
                    stringBuilder
                            .append(" = LOWER('")
                            .append(searchTermWithoutSpecialCharacters)
                            .append("')");
                    break;
                }
                case "not_equal_to": {
                    stringBuilder
                            .append(" <> LOWER('")
                            .append(searchTermWithoutSpecialCharacters)
                            .append("')");
                    break;
                }
                case "starts_with": {
                    stringBuilder
                            .append(" LIKE LOWER('")
                            .append(searchTermWithoutSpecialCharacters)
                            .append("%')");
                    break;
                }
                case "ends_with": {
                    stringBuilder
                            .append(" LIKE LOWER('%")
                            .append(searchTermWithoutSpecialCharacters)
                            .append("')");
                    break;
                }
                default:
                    System.out.println("Entered default case in search condition");
                    break;
            }
        }

        if (stringBuilder.toString().equalsIgnoreCase("")) {
            stringBuilder
                    .append(" LIKE '%")
                    .append(searchTermWithoutSpecialCharacters)
                    .append("%'");
        }

        return stringBuilder.toString();
    }

    private String generateDeviceIdsFilterCustomQuery(com.alibaba.fastjson.JSONObject searchSortFilterDetails) {
        StringBuilder stringBuilder = new StringBuilder();
        if (searchSortFilterDetails.containsKey("device_ids") && searchSortFilterDetails.getJSONArray("device_ids") != null && searchSortFilterDetails.getJSONArray("device_ids").size() > 0) {
            for (int i = 0; i < searchSortFilterDetails.getJSONArray("device_ids").size(); i++) {
                stringBuilder.append("\"");
                stringBuilder.append(searchSortFilterDetails.getJSONArray("device_ids").get(i));
                stringBuilder.append("\"");
                if (i < searchSortFilterDetails.getJSONArray("device_ids").size() - 1) {
                    stringBuilder.append(",");
                }
            }
            return " AND (d.id IN (" + stringBuilder + "))";
        }
        return "";
    }


    private String generateFeatureFilterQuery(com.alibaba.fastjson.JSONArray feature_details, String vdmsid) {
        StringBuilder featureFilterQuery = new StringBuilder();
        for (int i = 0; i < feature_details.size(); i++) {
            com.alibaba.fastjson.JSONObject feature = feature_details.getJSONObject(i);
            String feature_name = feature.getString("name");
            String feature_option = feature.getString("condition");
            if (feature_name.equals("qrcode")) {
                featureFilterQuery.append(" AND ");
                if (feature_option.equals("is_present")) {
                    featureFilterQuery.append(" (qc.device_id IS NOT NULL OR cqc.device_id IS NOT NULL)");
                } else if (feature_option.equals("is_not_present")) {
                    featureFilterQuery.append(" (qc.device_id IS NULL AND cqc.device_id IS NULL)");
                }
            }
            if (feature_name.equals("barcode")) {
                featureFilterQuery.append(" AND ");
                if (feature_option.equals("is_present")) {
                    featureFilterQuery.append(" (cbc.device_id IS NOT NULL) ");
                } else if (feature_option.equals("is_not_present")) {
                    featureFilterQuery.append(" cbc.device_id IS NULL ");
                }
            }
            if (feature_name.equals("adc")) {
                featureFilterQuery.append(" AND ");
                if (feature_option.equals("is_present")) {
                    featureFilterQuery.append(" (d.source_type = 'adc') ");
                } else if (feature_option.equals("is_not_present")) {
                    featureFilterQuery.append(" (d.source_type = 'vdms') ");
                }
            }
            if (feature_name.equals("nfc")) {
                featureFilterQuery.append(" AND ");
                if (feature_option.equals("is_present")) {
                    featureFilterQuery.append(" (nfc.device_id IS NOT NULL OR cnfc.device_id IS NOT NULL)");
                } else if (feature_option.equals("is_not_present")) {
                    featureFilterQuery.append(" (nfc.device_id IS NULL AND cnfc.device_id IS NULL)");
                }
            }
            if (feature_name.equals("record_checklist")) {
                if (feature_option.equals("is_present")) {
                    featureFilterQuery.append(" AND  d.record_checklist_count>0");
                } else if (feature_option.equals("is_not_present")) {
                    featureFilterQuery.append(" AND (d.record_checklist_count IS NULL OR d.record_checklist_count=0)");
                }
            }
            if (feature_name.equals("document")) {
                if (feature_option.equals("is_present")) {
                    featureFilterQuery.append(" AND d.document_count>0");
                } else if (feature_option.equals("is_not_present")) {
                    featureFilterQuery.append(" AND (d.document_count IS NULL OR d.document_count=0)");
                }
            }
            if (feature_name.equals("asset_image_url")) {
                if (feature_option.equals("is_present")) {
                    featureFilterQuery.append(" AND (d.asset_image_url IS NOT NULL AND d.asset_image_url <> '[]')");
                } else if (feature_option.equals("is_not_present")) {
                    featureFilterQuery.append(" AND (d.asset_image_url IS NULL OR d.asset_image_url = '[]')");
                }
            }

            if (feature_name.equals("measuring_instrument")) {
                if (feature_option.equals("is_present")) {
                    featureFilterQuery.append(" AND d.measuring_instrument_count>0");
                } else if (feature_option.equals("is_not_present")) {
                    featureFilterQuery.append(" AND (d.measuring_instrument_count IS NULL OR d.measuring_instrument_count=0)");
                }
            }
            if (feature_name.equals("sensor_alert")) {
                if (feature_option.equals("is_present")) {
                    featureFilterQuery.append(" AND (d.monnit_status='alert' OR d.pelican_status='alert' OR d.knx_status='alert'" +
                            " OR d.snmp_object_status='alert' OR d.daintree_status='alert' OR d.ecobee_status='alert' OR d.bacnet_status='alert'" +
                            " OR d.lorawan_status ='alert' OR d.my_devices_status='alert' OR d.measuring_instrument_status='alert' OR d.disruptive_status='alert')");
                } else if (feature_option.equals("is_not_present")) {
                    featureFilterQuery.append(" AND ((d.monnit_status IS NULL OR d.monnit_status<>'alert') AND (d.pelican_status IS NULL OR d.pelican_status<>'alert')" +
                            " AND (d.knx_status IS NULL OR d.knx_status<>'alert') AND (d.snmp_object_status IS NULL OR d.snmp_object_status<>'alert')" +
                            " AND (d.daintree_status IS NULL OR d.daintree_status<>'alert') AND (d.ecobee_status IS NULL OR d.ecobee_status<>'alert')" +
                            " AND (d.bacnet_status IS NULL OR d.bacnet_status<>'alert') AND (d.lorawan_status IS NULL OR d.lorawan_status<>'alert')" +
                            " AND (d.my_devices_status IS NULL OR d.my_devices_status<>'alert') AND (d.measuring_instrument_status IS NULL OR d.measuring_instrument_status<>'alert')" +
                            " AND (d.disruptive_status IS NULL OR d.disruptive_status<>'alert'))");
                }
            }

            if (feature_name.equals("geolocation_status")) {
                switch (feature_option) {
                    case "is_not_present":
                        featureFilterQuery.append(" AND dos.geolocation_status = 0");
                        break;
                    case "is_present":
                        featureFilterQuery.append(" AND dos.geolocation_status = 1");
                        break;
                    case "retag":
                        featureFilterQuery.append(" AND dos.geolocation_status = 2");
                        break;
                    case "not_added_exception":
                        featureFilterQuery.append(" AND dos.geolocation_status = 3");
                        break;
                }
            }
            if (feature_name.equals("image_status")) {
                switch (feature_option) {
                    case "is_not_present":
                        featureFilterQuery.append(" AND dos.image_status = 0");
                        break;
                    case "is_present":
                        featureFilterQuery.append(" AND dos.image_status = 1");
                        break;
                    case "retag":
                        featureFilterQuery.append(" AND dos.image_status = 2");
                        break;
                    case "not_added_exception":
                        featureFilterQuery.append(" AND dos.image_status = 3");
                        break;
                }
            }
            if (feature_name.equals("field_status")) {
                switch (feature_option) {
                    case "is_not_present":
                        featureFilterQuery.append(" AND dos.field_status = 0");
                        break;
                    case "is_present":
                        featureFilterQuery.append(" AND  dos.field_status = 1");
                        break;
                    case "retag":
                        featureFilterQuery.append(" AND dos.field_status = 2");
                        break;
                    case "not_added_exception":
                        featureFilterQuery.append(" AND dos.field_status = 3");
                        break;
                }
            }
            if (feature_name.equals("tag_status")) {
                switch (feature_option) {
                    case "is_not_present":
                        featureFilterQuery.append(" AND dos.tag_status = 0");
                        break;
                    case "is_present":
                        featureFilterQuery.append(" AND dos.tag_status = 1");
                        break;
                    case "retag":
                        featureFilterQuery.append(" AND dos.tag_status = 2");
                        break;
                    case "not_added_exception":
                        featureFilterQuery.append(" AND dos.tag_status = 3");
                        break;
                }
            }

        }

        return featureFilterQuery.toString();
    }

//    private String generateOnboardDataFilterQuery(String onboard_status, String vdmsId) {
//        StringBuilder stringBuilder = new StringBuilder();
//        stringBuilder.append(" AND (");
//        if (onboard_status.equals("completed")) {
//            stringBuilder.append("(d.asset_image_url IS NOT NULL AND d.asset_image_url <> '[]') AND ( ");
//            stringBuilder.append(generateQrCodeFilterQuery(vdmsId, true));
//            stringBuilder.append(" OR ");
//            stringBuilder.append(generateNFCFilterQuery(vdmsId, true));
//            stringBuilder.append(" ) ");
//            stringBuilder.append(" AND ((d.model IS NOT NULL AND d.model <> '' ) OR (d.user_data_model IS NOT NULL AND d.user_data_model <> '') ) AND");
//            stringBuilder.append(" ((d.vendor IS NOT NULL AND d.vendor <> '' ) OR (d.user_data_vendor IS NOT NULL AND d.user_data_vendor <> '')) AND ");
//            stringBuilder.append(" (d.serial_number IS NOT NULL AND d.serial_number <> '' ) AND ");
//            stringBuilder.append(" d.latitude IS NOT NULL AND ");
//            stringBuilder.append(" d.longitude IS NOT NULL AND ");
//            stringBuilder.append(" d.position IS NOT NULL ");
//        } else if (onboard_status.equals("not_completed")) {
//            stringBuilder.append("(d.asset_image_url IS NULL OR d.asset_image_url = '[]') OR ( ");
//            stringBuilder.append(generateQrCodeFilterQuery(vdmsId, false));
//            stringBuilder.append(" AND ");
//            stringBuilder.append(generateNFCFilterQuery(vdmsId, false));
//            stringBuilder.append(" ) ");
//            stringBuilder.append(" OR ((d.model IS NULL OR d.model = '') AND (d.user_data_model IS NULL OR d.user_data_model = '')) OR ");
//            stringBuilder.append(" ((d.vendor IS NULL OR d.vendor = '') AND (d.user_data_vendor IS NULL OR d.user_data_vendor = '')) OR ");
//            stringBuilder.append("(d.serial_number IS NULL OR d.serial_number = '') OR ");
//            stringBuilder.append("d.latitude IS NULL OR ");
//            stringBuilder.append("d.longitude IS  NULL OR ");
//            stringBuilder.append("d.position IS  NULL  ");
//        }
//        stringBuilder.append(")");
//        return stringBuilder.toString();
//    }
//
//    private String generateQrCodeFilterQuery(String vdms_id, Boolean isTaggedToQrCode) {
//        StringBuilder stringBuilder = new StringBuilder();
//        com.alibaba.fastjson.JSONArray devicesTaggedToQrCode = apiCallService.getDevicesOrLocationsTaggedToQrCode(vdms_id, "device");
//        if (devicesTaggedToQrCode != null) {
//            for (int i = 0; i < devicesTaggedToQrCode.size(); i++) {
//                stringBuilder.append("\"");
//                com.alibaba.fastjson.JSONObject device = devicesTaggedToQrCode.getJSONObject(i);
//                stringBuilder.append(device.get("deviceId"));
//                stringBuilder.append("\"");
//                if (i < devicesTaggedToQrCode.size() - 1) {
//                    stringBuilder.append(",");
//                }
//            }
//
//            if (devicesTaggedToQrCode.size() == 0) {
//                stringBuilder.append("\"\"");
//            }
//
//            if (isTaggedToQrCode) {
//                return " (d.id IN (" + stringBuilder + ")) ";
//            } else {
//                return " (d.id NOT IN (" + stringBuilder + ")) ";
//            }
//        }
//        return "";
//    }
//
//    private String generateNFCFilterQuery(String vdms_id, Boolean isTaggedToNFC) {
//        StringBuilder stringBuilder = new StringBuilder();
//        com.alibaba.fastjson.JSONArray devicesTaggedToNFC = apiCallService.getDevicesOrLocationsTaggedToNFC(vdms_id, "device");
//        if (devicesTaggedToNFC != null) {
//            for (int i = 0; i < devicesTaggedToNFC.size(); i++) {
//                stringBuilder.append("\"");
//                com.alibaba.fastjson.JSONObject device = devicesTaggedToNFC.getJSONObject(i);
//                stringBuilder.append(device.get("deviceId"));
//                stringBuilder.append("\"");
//                if (i < devicesTaggedToNFC.size() - 1) {
//                    stringBuilder.append(",");
//                }
//            }
//            if (devicesTaggedToNFC.size() == 0) {
//                stringBuilder.append("\"\"");
//            }
//            if (isTaggedToNFC) {
//                return " (d.id IN (" + stringBuilder + "))";
//            } else {
//                return " (d.id NOT IN (" + stringBuilder + "))";
//            }
//        }
//        return "";
//    }
    /****************************************************Multiple Keyword Search Sort Filter Merged*********************************************************/
}
