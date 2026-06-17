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
import org.springframework.stereotype.Service;


import io.sclera.dto.DeviceDTO;
import io.sclera.dto.DeviceSearchCriteria;
import io.sclera.interfaces.DeviceSearchServiceInterface;
import io.sclera.queryrepository.DeviceSearchQueryBuilder;
import me.xdrop.fuzzywuzzy.FuzzySearch;

/**
 * Searches, sorts, and filters devices via the type-safe {@link DeviceSearchQueryBuilder}
 * (JPA Criteria), then hydrates and fuzzy-ranks the results through the device service.
 */
@Service
public class DeviceSearchService implements DeviceSearchServiceInterface {

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
