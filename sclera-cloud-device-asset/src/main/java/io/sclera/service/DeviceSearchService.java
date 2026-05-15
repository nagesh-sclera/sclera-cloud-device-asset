package io.sclera.service;

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
import me.xdrop.fuzzywuzzy.FuzzySearch;

@Service
public class DeviceSearchService {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    DeviceService deviceService;

    @Autowired
    APICallService apiCallService;

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
    public Set<DeviceDTO> searchDevices(String username, String vdmsid, String dockername, String condition, Integer pageNo, Integer pageSize, Map<String, Object> search_details) {
        try {

            Integer virtual_device_type = null;
            Integer status = null;
            Integer monitor = 123;
            Integer asset_match_status = null;
            Integer offset = pageSize * (pageNo - 1);

            if (condition.equals("all")) {
                System.out.println("inside all" + virtual_device_type + status + monitor + offset + pageNo);
            } else if (condition.equals("unmonitored")) {
                System.out.println("outsidxse all" + virtual_device_type + status + monitor + offset + pageNo);
                monitor = 0;
            } else if (condition.equals("online")) {

                monitor = 1;
                status = 1;
                System.out.println("Inside Online" + monitor + status);

            } else if (condition.equals("offline")) {
                monitor = 1;
                status = 0;
            } else if (condition.equals("other")) {
                virtual_device_type = 123;
            } else if (condition.equals("matched")) {
                asset_match_status = 1;
            } else if (condition.equals("unmatched")) {
                asset_match_status = 0;
            } else if (condition.equals("verified")) {
                asset_match_status = 2;
            } else if (condition.equals("archived")) {
                asset_match_status = 3;
            }

            Set<String> device_ids = new HashSet<>();
            if (search_details.get("value") == null) {
                return deviceService.getfilterdevices(username, vdmsid, dockername, condition, "null", pageNo, pageSize); //search key is given as null to get all devices based on condition
            } else {
                if (search_details.get("column") == null) {

                    String query = "SELECT t1.* "
                            + "FROM "
                            + "((SELECT d.id, d.location_id, d.docker_name, d.docker_vdms_id, d.virtual_device_type, d.status, d.monitor, d.asset_match_status "
                            + "FROM device d "
                            + "WHERE JSON_EXTRACT(d.custom_fields,('$[*].*')) "
                            + "LIKE CONCAT('%','" + search_details.get("value") + "','%')) "
                            + "UNION "
                            + "(SELECT d1.id, d1.location_id, d1.docker_name, d1.docker_vdms_id, d1.virtual_device_type, d1.status, d1.monitor, d1.asset_match_status "
                            + "FROM device d1 "
                            + "LEFT JOIN location l ON l.id = d1.location_id "
                            + "LEFT JOIN floor f ON f.id = l.floor_id "
                            + "LEFT JOIN building b ON b.id = f.building_id "
                            + "WHERE CONCAT_WS('',d1.id, IF(d1.user_data_name IS NULL or d1.user_data_name = '', d1.display_name, d1.user_data_name), "
                            + "IF(d1.user_data_vendor IS NULL or d1.user_data_vendor = '', d1.vendor, d1.user_data_vendor), "
                            + "IF(d1.user_data_model IS NULL or d1.user_data_model = '', d1.model, d1.user_data_model), d1.type, d1.ip_address,"
                            + "d1.mac_address, d1.latitude, d1.longitude, d1.serial_number, d1.warranty, l.name, f.name, b.name) "
                            + "LIKE CONCAT('%','" + search_details.get("value") + "','%'))) AS t1 "
                            + "WHERE ('" + vdmsid + "' = 'null' or t1.docker_vdms_id = '" + vdmsid
                            + "') AND ('" + dockername + "' = 'all' or  t1.docker_name = '" + dockername + "') "
                            + "AND (" + virtual_device_type + " IS NULL or IF(" + virtual_device_type + " = 123, (virtual_device_type IS NOT NULL AND (virtual_device_type != 0 AND virtual_device_type != 1)),NULL)) "
                            + "AND (" + status + " IS NULL or t1.status = " + status + ") "
                            + "AND (" + monitor + " = 123  or IF(" + monitor + " = 1," + monitor + " = t1.monitor,t1.monitor IS NULL or " + monitor + " = t1.monitor)) "
                            + "AND ((" + asset_match_status + " IS NULL and t1.asset_match_status != 3) or "
                            + "IF(" + asset_match_status + " = 3, t1.asset_match_status = " + asset_match_status + ", "
                            + "(t1.asset_match_status = " + asset_match_status + " and t1.asset_match_status != 3))) "
                            + "LIMIT " + pageSize + " OFFSET " + offset;


                    //					System.out.println("query--1:\n"+query);

                    var queryResult = jdbcTemplate.queryForList(query);
                    for (Map<String, Object> stringObjectMap : queryResult) {
                        device_ids.add(String.valueOf(stringObjectMap.get("id")));
                    }
                } else {
                    String searchColumn = String.valueOf(search_details.get("column")).replaceAll("\\s", "");
                    if ((Boolean) search_details.get("custom")) {
                        String query = "SELECT id FROM device "
                                + " WHERE ('" + vdmsid + "' = 'null' or docker_vdms_id = '" + vdmsid
                                + "') AND ('" + dockername + "' = 'all' or  docker_name = '" + dockername + "') "
                                + "AND (" + virtual_device_type + " IS NULL or IF(" + virtual_device_type + " = 123, (virtual_device_type IS NOT NULL AND (virtual_device_type != 0 AND virtual_device_type != 1)),NULL)) "
                                + "AND (" + status + " IS NULL or status = " + status + ") "
                                + "AND (" + monitor + " = 123  or IF(" + monitor + " = 1," + monitor + " = monitor,monitor IS NULL or " + monitor + " = monitor)) "
                                + "AND ((" + asset_match_status + " IS NULL and asset_match_status != 3) or "
                                + "IF(" + asset_match_status + " = 3, asset_match_status = " + asset_match_status + ", "
                                + "(asset_match_status = " + asset_match_status + " and asset_match_status != 3))) "
                                + "AND JSON_UNQUOTE(JSON_EXTRACT(custom_fields,CONCAT(\"$[*].\",\"" + searchColumn
                                + "\"))) LIKE CONCAT('%','" + search_details.get("value") + "','%') LIMIT "
                                + pageSize + " OFFSET " + offset;

                        var queryResult = jdbcTemplate.queryForList(query);
                        for (Map<String, Object> stringObjectMap : queryResult) {
                            device_ids.add(String.valueOf(stringObjectMap.get("id")));
                        }
                    } else {

                        String updatedSearchColumn = this.updateDeviceSearchColumnName(searchColumn);

                        String query = "SELECT  d.id FROM device d "
                                + "LEFT JOIN location l ON l.id = d.location_id "
                                + "LEFT JOIN floor f ON f.id = l.floor_id "
                                + "LEFT JOIN building b ON b.id = f.building_id "
                                + "WHERE ('" + vdmsid + "' = 'null' or d.docker_vdms_id = '" + vdmsid
                                + "') AND ('" + dockername + "' = 'all' or  d.docker_name = '" + dockername + "') "
                                + "AND (" + virtual_device_type + " IS NULL or IF(" + virtual_device_type + " = 123, (virtual_device_type IS NOT NULL AND (virtual_device_type != 0 AND virtual_device_type != 1)),NULL)) "
                                + "AND (" + status + " IS NULL or d.status = " + status + ") "
                                + "AND (" + monitor + " = 123  or IF(" + monitor + " = 1," + monitor + " = d.monitor,d.monitor IS NULL or " + monitor + " = d.monitor)) "
                                + "AND ((" + asset_match_status + " IS NULL and d.asset_match_status != 3) or "
                                + "IF(" + asset_match_status + " = 3, d.asset_match_status = " + asset_match_status + ", "
                                + "(d.asset_match_status = " + asset_match_status + " and d.asset_match_status != 3))) "
                                + "AND " + updatedSearchColumn + " LIKE '%" + search_details.get("value") + "%' "
                                + "LIMIT " + pageSize + " OFFSET " + offset;

                        //						System.out.println("query--3:\n"+query);
                        var queryResult = jdbcTemplate.queryForList(query);
                        for (Map<String, Object> stringObjectMap : queryResult) {
                            device_ids.add(String.valueOf(stringObjectMap.get("id")));
                        }
                    }
                }
            }


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
    public Set<DeviceDTO> sortDevices(String username, String vdmsid, String dockername, String condition, Integer pageno,
                                      Integer pagesize, Map<String, Object> sort_details) {
        try {
            Integer virtual_device_type = null;
            Integer status = null;
            Integer monitor = 123;
            Integer asset_match_status = null;

            if (condition.equals("all")) {
                System.out.println("inside all");
            } else if (condition.equals("unmonitored")) {
                monitor = 0;
            } else if (condition.equals("online")) {

                monitor = 1;
                status = 1;
                System.out.println("Inside Online" + monitor + status);

            } else if (condition.equals("offline")) {
                monitor = 1;
                status = 0;
            } else if (condition.equals("other")) {
                virtual_device_type = 123;
            } else if (condition.equals("matched")) {
                asset_match_status = 1;
            } else if (condition.equals("unmatched")) {
                asset_match_status = 0;
            } else if (condition.equals("verified")) {
                asset_match_status = 2;
            } else if (condition.equals("archived")) {
                asset_match_status = 3;
            }

            Integer offset = pagesize * (pageno - 1);
            String searchColumn = String.valueOf(sort_details.get("column")).replaceAll("\\s", "");
            Set<String> device_ids = new LinkedHashSet<>();

            if ((Boolean) sort_details.get("custom")) {
                String query = "SELECT "
                        + "id"
                        + " FROM device "
                        + "WHERE ('" + vdmsid + "' = 'null' OR docker_vdms_id = '" + vdmsid + "') "
                        + "AND ('" + dockername + "' = 'all' OR docker_name= '" + dockername + "') "
                        + "AND (" + virtual_device_type + " IS NULL or IF(" + virtual_device_type + " = 123, (virtual_device_type IS NOT NULL AND (virtual_device_type != 0 AND virtual_device_type != 1)),NULL)) "
                        + "AND (" + status + " IS NULL or status = " + status + ") "
                        + "AND (" + monitor + " = 123  or IF(" + monitor + " = 1," + monitor + " = monitor,monitor IS NULL or " + monitor + " = monitor)) "
                        + "AND ((" + asset_match_status + " IS NULL and asset_match_status != 3) or "
                        + "IF(" + asset_match_status + " = 3, asset_match_status = " + asset_match_status + ", "
                        + "(asset_match_status = " + asset_match_status + " and asset_match_status != 3))) "
                        + "ORDER BY (custom_fields->>'$[*]." + searchColumn + "' IS NULL OR custom_fields->>'$[*]." + searchColumn + "' = '[\"\"]'), "
                        + "custom_fields->>'$[*]." + searchColumn + "' LIMIT " + pagesize + " OFFSET " + offset;

                System.out.println("SORT QUERY WITH CUSTOM COLUMN " + query);

                var queryResult = jdbcTemplate.queryForList(query);
                for (Map<String, Object> stringObjectMap : queryResult) {
                    System.out.println(stringObjectMap);
                    device_ids.add(String.valueOf(stringObjectMap.get("id")));
                }
            } else {
                String updatedSearchColumn = this.updateDeviceSearchColumnName(searchColumn);

                if (searchColumn.equals("ip_address")) {
//                    updatedSearchColumn = "INET_ATON(" + updatedSearchColumn + ")";
                    updatedSearchColumn = "ISNULL(INET_ATON(" + updatedSearchColumn + ")),INET_ATON(" + updatedSearchColumn + ") ";
                } else {
                    updatedSearchColumn = "ISNULL(" + updatedSearchColumn + ")," + updatedSearchColumn + " ";
                }

                String query = "SELECT d.id FROM device d "
                        + "LEFT JOIN location l ON l.id = d.location_id "
                        + "LEFT JOIN floor f ON f.id = l.floor_id "
                        + "LEFT JOIN building b ON b.id = f.building_id "
                        + "WHERE ('" + vdmsid + "' = 'null' or d.docker_vdms_id = '" + vdmsid
                        + "') AND ('" + dockername + "' = 'all' or  d.docker_name = '" + dockername + "') "
                        + "AND (" + virtual_device_type + " IS NULL or IF(" + virtual_device_type + " = 123, (virtual_device_type IS NOT NULL AND (virtual_device_type != 0 AND virtual_device_type != 1)),NULL)) "
                        + "AND (" + status + " IS NULL or d.status = " + status + ") "
                        + "AND (" + monitor + " = 123  or IF(" + monitor + " = 1," + monitor + " = monitor, monitor IS NULL or " + monitor + " = monitor)) "
                        + "AND ((" + asset_match_status + " IS NULL and asset_match_status != 3) or "
                        + "IF(" + asset_match_status + " = 3, asset_match_status = " + asset_match_status + ", "
                        + "(asset_match_status = " + asset_match_status + " and asset_match_status != 3))) "
                        + "ORDER BY " + updatedSearchColumn
                        + "LIMIT " + pagesize + " OFFSET " + offset;

                System.out.println("SORT QUERY WITH SIMPLE COLUMN " + query);

                var queryResult = jdbcTemplate.queryForList(query);
                for (Map<String, Object> stringObjectMap : queryResult) {
                    System.out.println(stringObjectMap);
                    device_ids.add(String.valueOf(stringObjectMap.get("id")));
                }
            }

            return deviceService.getDevicesByIdList(vdmsid, device_ids);
        } catch (Exception e) {
            System.out.println(e);
        }
        return null;
    }

    /**************************************************Sort Devices Method***********************************************************/
    /**************************************************Filter Devices Method***********************************************************/
    public Set<DeviceDTO> filterDevices(String username, String vdmsid, String dockername, String condition, Integer pageno,
                                        Integer pagesize, List<Map<String, Object>> filter_details) {
        try {
            Integer virtual_device_type = null;
            Integer status = null;
            Integer monitor = 123;
            Integer asset_match_status = null;

            if (condition.equals("all")) {
                System.out.println("inside all");
            } else if (condition.equals("unmonitored")) {
                monitor = 0;
            } else if (condition.equals("online")) {

                monitor = 1;
                status = 1;
                System.out.println("Inside Online" + monitor + status);

            } else if (condition.equals("offline")) {
                monitor = 1;
                status = 0;
            } else if (condition.equals("other")) {
                virtual_device_type = 123;
            } else if (condition.equals("matched")) {
                asset_match_status = 1;
            } else if (condition.equals("unmatched")) {
                asset_match_status = 0;
            } else if (condition.equals("verified")) {
                asset_match_status = 2;
            } else if (condition.equals("archived")) {
                asset_match_status = 3;
            }

            Integer offset = pagesize * (pageno - 1);
            Set<String> device_ids = new HashSet<>();
            String query = "SELECT d.id FROM device d "
                    + "LEFT JOIN location l ON l.id = d.location_id "
                    + "LEFT JOIN floor f ON f.id = l.floor_id "
                    + "LEFT JOIN building b ON b.id = f.building_id "
                    + "WHERE ('" + vdmsid + "' = 'null' or d.docker_vdms_id = '" + vdmsid
                    + "') AND ('" + dockername + "' = 'all' or  d.docker_name = '" + dockername + "') "
                    + "AND (" + virtual_device_type + " IS NULL or IF(" + virtual_device_type + " = 123, (virtual_device_type IS NOT NULL AND (virtual_device_type != 0 AND virtual_device_type != 1)),NULL)) "
                    + "AND (" + status + " IS NULL or d.status = " + status + ") "
                    + "AND (" + monitor + " = 123  or IF(" + monitor + " = 1," + monitor + " = monitor, monitor IS NULL or " + monitor + " = monitor)) "
                    + "AND ((" + asset_match_status + " IS NULL and asset_match_status != 3) or "
                    + "IF(" + asset_match_status + " = 3, asset_match_status = " + asset_match_status + ", "
                    + "(asset_match_status = " + asset_match_status + " and asset_match_status != 3))) AND"
                    + generateMultiConditionStmt(filter_details, vdmsid, dockername)
                    + "LIMIT " + pagesize + " OFFSET " + offset;

            System.out.println("CONSTRUCTED QUERY: " + query);

            var queryResult = jdbcTemplate.queryForList(query);
            for (Map<String, Object> stringObjectMap : queryResult) {
                System.out.println(stringObjectMap);
                device_ids.add(String.valueOf(stringObjectMap.get("id")));
            }
            return deviceService.getDevicesByIdList(vdmsid, device_ids);

        } catch (Exception e) {
            System.out.println(e);
        }
        return null;
    }

    public String generateMultiConditionStmt(List<Map<String, Object>> filter_details, String vdms_id, String dockername) {
        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < filter_details.size(); i++) {
            Map<String, Object> tempMap = filter_details.get(i);
            if ((Boolean) filter_details.get(i).get("custom")) {
                stringBuilder
                        .append(" JSON_UNQUOTE(JSON_EXTRACT(JSON_EXTRACT(custom_fields,'$[*].")
                        .append(tempMap.get("column"))
                        .append("'),'$[0]'")
                        .append("))")
                        .append(" IS NOT NULL AND ")
                        .append(" JSON_UNQUOTE(JSON_EXTRACT(JSON_EXTRACT(custom_fields,'$[*].")
                        .append(tempMap.get("column"))
                        .append("'),'$[0]'")
                        .append("))")
                        .append(" <> '' ");
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

    public String updateDeviceSearchColumnName(String searchColumn) {
        String updateSearchColumn;
        switch (searchColumn) {
            case "id": {
                updateSearchColumn = "d.id";
                break;
            }
            case "display_name": {
                updateSearchColumn = "IF(d.user_data_name IS NULL or d.user_data_name = '', d.display_name, d.user_data_name)";
                break;
            }
            case "vendor": {
                updateSearchColumn = "IF(d.user_data_vendor IS NULL or d.user_data_vendor = '', d.vendor, d.user_data_vendor)";
                break;
            }
            case "model": {
                updateSearchColumn = "IF(d.user_data_model IS NULL or d.user_data_model = '', d.model, d.user_data_model)";
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
                updateSearchColumn = "IF(d.user_data_name IS NULL or d.user_data_name = '', d.display_name, d.user_data_name)";
                break;
            }
        }

        return updateSearchColumn;
    }

    /********************************************Fuzzy Search*****************************************************************************/

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

    public List<DeviceDTO> getDeviceInfoByCustomFields(String username, String vdmsid, String dockername, com.alibaba.fastjson.JSONObject custom_fields) {

        String query = "SELECT id FROM device "
                + " WHERE ('" + vdmsid + "' = 'null' or docker_vdms_id = '" + vdmsid
                + "') AND ('" + dockername + "' = 'all' or  docker_name = '" + dockername + "') "
                + "AND JSON_UNQUOTE(JSON_EXTRACT(custom_fields,CONCAT(\"$[*].\",\"" + custom_fields.getString("key")
                + "\"))) LIKE CONCAT('%','" + custom_fields.getString("value") + "','%') LIMIT 1";

        var queryResult = jdbcTemplate.queryForList(query);

        System.out.println(queryResult);

        List<DeviceDTO> devices = new ArrayList<>();

        for (Map<String, Object> stringObjectMap : queryResult) {
            try {
                devices.add(deviceService.getDeviceByDeviceId(username, vdmsid, dockername, String.valueOf(stringObjectMap.get("id"))));
            } catch (Exception e) {
                System.out.println(e);
            }
        }

        return devices;
    }

    /***********************************************Get Device Info By Custom Fields*******************************************************/

    /****************************************************Multiple Keyword Search Sort Filter Merged*********************************************************/

    public Set<DeviceDTO> multipleKeywordSearchSortFilterDevices(String username, String vdmsid, String dockername, String condition,
                                                                 Integer pageno, Integer pagesize,
                                                                 com.alibaba.fastjson.JSONObject search_sort_filter_details, Integer onboard_status) {
        try {
            Integer virtual_device_type = null;
            Integer status = null;
            Integer assgined_status = null;
            int monitor = 123;
            Integer asset_match_status = null;
            switch (condition) {
                case "all":
                    System.out.println("inside all");
                    break;
                case "unmonitored":
                    monitor = 0;
                    break;
                case "online":
                    monitor = 1;
                    status = 1;
                    System.out.println("Inside Online" + monitor + status);
                    break;
                case "offline":
                    monitor = 1;
                    status = 0;
                    break;
                case "other":
                    virtual_device_type = 123;
                    break;
                case "matched":
                    asset_match_status = 1;
                    break;
                case "unmatched":
                    asset_match_status = 0;
                    break;
                case "verified":
                    asset_match_status = 2;
                    break;
                case "archived":
                    asset_match_status = 3;
                    break;
                case "onboarded":
                    onboard_status = 3;
                    break;
                case "notonboarded":
                    onboard_status = 210;
                    break;
                case "assigned":
                    assgined_status = 1;
                    break;
                case "unassigned":
                    assgined_status = 0;
                    break;

            }
            String searchAndFilterCustomQuery = this.generateMultipleKeywordSearchAndFilterCustomQuery(search_sort_filter_details, vdmsid);
            String generateDeviceIdsFilterCustomQuery = this.generateDeviceIdsFilterCustomQuery(search_sort_filter_details);
            int offset = pagesize * (pageno - 1);
            Set<String> device_ids = new LinkedHashSet<>();
            String sortQuery = generateSortQuery(search_sort_filter_details);
            String query = "SELECT d.id FROM device d LEFT JOIN location l ON l.id = d.location_id LEFT JOIN floor f ON f.id = l.floor_id LEFT JOIN building b ON b.id = f.building_id where d.id IN "
                    + " (SELECT  d.id FROM device d "
                    + "LEFT JOIN device_onboard_status dos ON d.id = dos.device_id "
                    + "LEFT JOIN device_onboard_status_assignee dosa ON dos.id = dosa.device_onboard_status_id "
                    + "LEFT JOIN qr_code qc ON d.id = qc.device_id "
                    + "LEFT JOIN client_qr_code cqc ON d.id = cqc.device_id "
                    + "LEFT JOIN client_bar_code cbc ON d.id = cbc.device_id "
                    + "LEFT JOIN nfc ON d.id = nfc.device_id "
                    + "LEFT JOIN client_nfc cnfc ON d.id = cnfc.device_id "
                    + "LEFT JOIN location l ON l.id = d.location_id "
                    + "LEFT JOIN floor f ON f.id = l.floor_id "
                    + "LEFT JOIN building b ON b.id = f.building_id "
                    + "LEFT JOIN device_specification ds ON ds.device_id = d.id "
                    + "WHERE ('" + vdmsid + "' = 'null' or d.docker_vdms_id = '" + vdmsid
                    + "') AND ('" + dockername + "' = 'all' or  d.docker_name = '" + dockername + "') "
                    + "AND ((" + onboard_status + " = 123) or IF(" + onboard_status + " = 210, d.onboard_status != 3 ,(" + onboard_status + " = d.onboard_status ))) "
                    + "AND (" + virtual_device_type + " IS NULL or IF(" + virtual_device_type + " = 123, (virtual_device_type IS NOT NULL AND (virtual_device_type != 0 AND virtual_device_type != 1)),NULL)) "
                    + "AND (" + status + " IS NULL or d.status = " + status + ") "
                    + "AND (" + monitor + " = 123  or IF(" + monitor + " = 1," + monitor + " = monitor, monitor IS NULL or " + monitor + " = monitor)) "
                    + "AND (" + assgined_status + " IS NULL or IF(" + assgined_status + " = 0, (d.assigned_user_email IS NULL or d.assigned_user_email = 'null'), ( d.assigned_user_email IS NOT NULL or d.assigned_user_email != 'null')))"
                    + "AND ((" + asset_match_status + " IS NULL and asset_match_status != 3) or "
                    + "IF(" + asset_match_status + " = 3, asset_match_status = " + asset_match_status + ", "
                    + "(asset_match_status = " + asset_match_status + " and asset_match_status != 3))) "
                    + generateDeviceIdsFilterCustomQuery
                    + searchAndFilterCustomQuery
                    + ") " + sortQuery
                    + " LIMIT " + pagesize + " OFFSET " + offset;
            System.out.println("CONSTRUCTED QUERY: " + query);
            var queryResult = jdbcTemplate.queryForList(query);
            for (Map<String, Object> stringObjectMap : queryResult) {
                device_ids.add(String.valueOf(stringObjectMap.get("id")));
            }
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

    public String generateColumnFilterQuery(com.alibaba.fastjson.JSONArray column_details) {
        StringBuilder stringBuilder = new StringBuilder();
        if (column_details.size() > 0) {
            stringBuilder.append(" AND (");
            for (int i = 0; i < column_details.size(); i++) {
                stringBuilder.append("( ");
                com.alibaba.fastjson.JSONObject tempMap = column_details.getJSONObject(i);
                if ((Boolean) tempMap.get("custom")) {
                    if (tempMap.get("condition").equals("is_present")) {
                        stringBuilder
                                .append(" JSON_UNQUOTE(JSON_EXTRACT(JSON_EXTRACT(custom_fields,'$[*].")
                                .append(tempMap.get("column"))
                                .append("'),'$[0]'")
                                .append("))")
                                .append(" <> 'null' AND ")
                                .append(" JSON_UNQUOTE(JSON_EXTRACT(JSON_EXTRACT(custom_fields,'$[*].")
                                .append(tempMap.get("column"))
                                .append("'),'$[0]'")
                                .append("))")
                                .append(" <> ''");

                    } else if (tempMap.get("condition").equals("is_not_present")) {
                        stringBuilder
                                .append(" JSON_UNQUOTE(JSON_EXTRACT(JSON_EXTRACT(custom_fields,'$[*].")
                                .append(tempMap.get("column"))
                                .append("'),'$[0]'")
                                .append("))")
                                .append(" = 'null' OR ")
                                .append(" JSON_UNQUOTE(JSON_EXTRACT(JSON_EXTRACT(custom_fields,'$[*].")
                                .append(tempMap.get("column"))
                                .append("'),'$[0]'")
                                .append("))")
                                .append("='' OR")
                                .append(" JSON_UNQUOTE(JSON_EXTRACT(JSON_EXTRACT(custom_fields,'$[*].")
                                .append(tempMap.get("column"))
                                .append("'),'$[0]'")
                                .append("))")
                                .append(" IS NULL");

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

    public String generateSearchQuery(com.alibaba.fastjson.JSONObject search_details, com.alibaba.fastjson.JSONObject searhSortFilterDetails) {
        StringBuilder stringBuilder = new StringBuilder();
        String searchColumn = String.valueOf(search_details.get("column")).replaceAll("\\s", "");
        String searchTerm = search_details.getString("value");
        String searchTermWithoutSpecialCharacters = searchTerm.replaceAll("[ -.!\t_+#~`@$%^&*()=;:<>?,/{}|\\\\]", "");
        String updatedSearchColumn = this.updateDeviceSearchColumnName(searchColumn);
        StringBuilder searchColumnValue = new StringBuilder();
        StringBuilder searchColumnValueWithoutSpecialCharacters = new StringBuilder();

        if (search_details.get("column") == null) {
            searchColumnValue.append("LOWER(CONCAT_WS('±','',d.id, IF(d.user_data_name IS NULL or d.user_data_name = '', d.display_name, d.user_data_name),")
                    .append("IF(d.user_data_vendor IS NULL or d.user_data_vendor = '', d.vendor, d.user_data_vendor), ")
                    .append("IF(d.user_data_model IS NULL or d.user_data_model = '', d.model, d.user_data_model), d.type, d.description,  ")
                    .append("d.ip_address, d.mac_address, d.latitude, d.longitude, d.serial_number, d.warranty,  d.created_timestamp,l.name, f.name, " +
                            "b.name, dos.assignee_email , dosa.email, ds.username, ds.email,COALESCE(IF(LOWER(REGEXP_REPLACE(JSON_EXTRACT(d.custom_fields, '$[*].*'), '[-.!\t_+#~`@$%^&*()=;:<>?,/{}|\\' ]', ''))" + this.generateConditionedQueryForCustomFields(search_details) + ",\"" + searchTermWithoutSpecialCharacters + "\",''), ''),''))");
        } else {
            if ((Boolean) search_details.get("custom")) {
                searchColumnValue
                        .append("LOWER(CONCAT_WS('',JSON_UNQUOTE(JSON_EXTRACT(JSON_EXTRACT(custom_fields,CONCAT(\"$[*].\",\"")
                        .append(searchColumn)
                        .append("\")), '$[0]')),''))");
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

    public String generateSortQuery(com.alibaba.fastjson.JSONObject sort_details) {
        StringBuilder stringBuilder = new StringBuilder();
        if (sort_details.containsKey("sort_details") && sort_details.getJSONObject("sort_details") != null) {
            com.alibaba.fastjson.JSONObject sort_details_object = sort_details.getJSONObject("sort_details");
            String searchColumn = String.valueOf(sort_details_object.get("column")).replaceAll("\\s", "");
            String updatedSearchColumn = this.updateDeviceSearchColumnName(searchColumn);

            if ((Boolean) sort_details_object.get("custom")) {
                stringBuilder
                        .append(" ORDER BY (custom_fields->>'$[*].")
                        .append(searchColumn)
                        .append("' IS NULL OR custom_fields->>'$[*].")
                        .append(searchColumn)
                        .append("' = '[\"\"]'), custom_fields->>'$[*].")
                        .append(searchColumn)
                        .append("'");
            } else {
                if (searchColumn.equals("ip_address")) {
                    updatedSearchColumn = "ISNULL(INET_ATON(" + updatedSearchColumn + ")),INET_ATON(" + updatedSearchColumn + ") ";
                } else {
                    updatedSearchColumn = "ISNULL(" + updatedSearchColumn + ")," + updatedSearchColumn + " = '',  " + updatedSearchColumn + " ";
                    if (searchColumn.equals("created_timestamp") || searchColumn.equals("updated_timestamp")) {
                        updatedSearchColumn = updatedSearchColumn + " DESC, d.id";
                    }
                }

                stringBuilder
                        .append(" ORDER BY ")
                        .append(updatedSearchColumn);
            }
        } else {
            stringBuilder
                    .append(" ORDER BY ISNULL(d.updated_timestamp), d.updated_timestamp DESC, d.id ");
        }

        return stringBuilder.toString();
    }

    public Set<DeviceDTO> multipleKeywordSearchSortFilterDevicesForAssetExport(String username, String vdmsid, String dockername, String condition,
                                                                               com.alibaba.fastjson.JSONObject search_sort_filter_details, Integer onboard_status) {
        try {
            Integer virtual_device_type = null;
            Integer status = null;
            Integer monitor = 123;
            Integer asset_match_status = null;
            Integer assigned_status = null;
            if (condition.equals("all")) {
                System.out.println("inside all");
            } else if (condition.equals("unmonitored")) {
                monitor = 0;
            } else if (condition.equals("online")) {
                monitor = 1;
                status = 1;
                System.out.println("Inside Online" + monitor + status);

            } else if (condition.equals("offline")) {
                monitor = 1;
                status = 0;
            } else if (condition.equals("other")) {
                virtual_device_type = 123;
            } else if (condition.equals("matched")) {
                asset_match_status = 1;
            } else if (condition.equals("unmatched")) {
                asset_match_status = 0;
            } else if (condition.equals("verified")) {
                asset_match_status = 2;
            } else if (condition.equals("archived")) {
                asset_match_status = 3;
            } else if (condition.equals("onboarded")) {
                onboard_status = 3;
            } else if (condition.equals("notonboarded")) {
                onboard_status = 210;
            } else if (condition.equals("assigned")) {
                assigned_status =1;
            } else if (condition.equals("unassigned")) {
                assigned_status =0;
            }
            String searchAndFilterCustomQuery = this.generateMultipleKeywordSearchAndFilterCustomQuery(search_sort_filter_details, vdmsid);
            String generateDeviceIdsFilterCustomQuery = this.generateDeviceIdsFilterCustomQuery(search_sort_filter_details);
            Set<String> device_ids = new LinkedHashSet<>();
            String sortQuery = generateSortQuery(search_sort_filter_details);
            String query = "SELECT d.id FROM device d LEFT JOIN location l ON l.id = d.location_id LEFT JOIN floor f ON f.id = l.floor_id LEFT JOIN building b ON b.id = f.building_id where d.id IN "
                    + "  (SELECT  d.id FROM device d "
                    + "LEFT JOIN device_onboard_status dos ON d.id = dos.device_id "
                    + "LEFT JOIN device_onboard_status_assignee dosa ON dos.id = dosa.device_onboard_status_id "
                    + "LEFT JOIN qr_code qc ON d.id = qc.device_id "
                    + "LEFT JOIN client_qr_code cqc ON d.id = cqc.device_id "
                    + "LEFT JOIN client_bar_code cbc ON d.id = cbc.device_id "
                    + "LEFT JOIN nfc ON d.id = nfc.device_id "
                    + "LEFT JOIN client_nfc cnfc ON d.id = cnfc.device_id "
                    + "LEFT JOIN location l ON l.id = d.location_id "
                    + "LEFT JOIN floor f ON f.id = l.floor_id "
                    + "LEFT JOIN building b ON b.id = f.building_id "
                    + "LEFT JOIN device_specification ds ON ds.device_id = d.id "
                    + "WHERE ('" + vdmsid + "' = 'null' or d.docker_vdms_id = '" + vdmsid
                    + "') AND ('" + dockername + "' = 'all' or  d.docker_name = '" + dockername + "') "
                    + "AND ((" + onboard_status + " = 123) or IF(" + onboard_status + " = 210, d.onboard_status != 3 ,(" + onboard_status + " = d.onboard_status ))) "
                    + "AND (" + virtual_device_type + " IS NULL or IF(" + virtual_device_type + " = 123, (virtual_device_type IS NOT NULL AND (virtual_device_type != 0 AND virtual_device_type != 1)),NULL)) "
                    + "AND (" + status + " IS NULL or d.status = " + status + ") "
                    + "AND (" + assigned_status + " IS NULL or IF(" + assigned_status + " = 0, (d.assigned_user_email IS NULL or d.assigned_user_email = 'null'), ( d.assigned_user_email IS NOT NULL or d.assigned_user_email != 'null')))"
                    + "AND (" + monitor + " = 123  or IF(" + monitor + " = 1," + monitor + " = monitor, monitor IS NULL or " + monitor + " = monitor)) "
                    + "AND ((" + asset_match_status + " IS NULL and asset_match_status != 3) or "
                    + "IF(" + asset_match_status + " = 3, asset_match_status = " + asset_match_status + ", "
                    + "(asset_match_status = " + asset_match_status + " and asset_match_status != 3))) "
                    + generateDeviceIdsFilterCustomQuery
                    + searchAndFilterCustomQuery
                    + ") " + sortQuery;
            System.out.println("CONSTRUCTED QUERY: " + query);
            var queryResult = jdbcTemplate.queryForList(query);
            for (Map<String, Object> stringObjectMap : queryResult) {
                device_ids.add(String.valueOf(stringObjectMap.get("id")));
            }
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

    public String multipleKeywordSearchSortFilterDevicesCount(String username, String vdmsid, String dockername, String condition,
                                                              com.alibaba.fastjson.JSONObject search_sort_filter_details, Integer onboard_status) {
        try {
            Integer virtual_device_type = null;
            Integer status = null;
            Integer monitor = 123;
            Integer asset_match_status = null;
            Integer assigned_status = null;

            if (condition.equals("all")) {
                System.out.println("inside all");
            } else if (condition.equals("unmonitored")) {
                monitor = 0;
            } else if (condition.equals("online")) {

                monitor = 1;
                status = 1;
                System.out.println("Inside Online" + monitor + status);

            } else if (condition.equals("offline")) {
                monitor = 1;
                status = 0;
            } else if (condition.equals("other")) {
                virtual_device_type = 123;
            } else if (condition.equals("matched")) {
                asset_match_status = 1;
            } else if (condition.equals("unmatched")) {
                asset_match_status = 0;
            } else if (condition.equals("verified")) {
                asset_match_status = 2;
            } else if (condition.equals("archived")) {
                asset_match_status = 3;
            } else if (condition.equals("onboarded")) {
                onboard_status = 3;
            } else if (condition.equals("notonboarded")) {
                onboard_status = 210;
            } else if (condition.equals("onboardpending")) {
                onboard_status = 1;
            } else if (condition.equals("onboardcompleted")) {
                onboard_status = 2;
            } else if (condition.equals("assigned")) {
                assigned_status =1;
            } else if (condition.equals("unassigned")) {
                assigned_status =0;
            }

            String searchAndFilterCustomQuery = this.generateMultipleKeywordSearchAndFilterCustomQuery(search_sort_filter_details, vdmsid);
            String generateDeviceIdsFilterCustomQuery = this.generateDeviceIdsFilterCustomQuery(search_sort_filter_details);


            if (searchAndFilterCustomQuery == null) {
                searchAndFilterCustomQuery = "";
            }

            String query = "SELECT COUNT(DISTINCT d.id) AS count FROM device d "
                    + "LEFT JOIN device_onboard_status dos ON d.id = dos.device_id "
                    + "LEFT JOIN device_onboard_status_assignee dosa ON dos.id = dosa.device_onboard_status_id "
                    + "LEFT JOIN qr_code qc ON d.id = qc.device_id "
                    + "LEFT JOIN client_qr_code cqc ON d.id = cqc.device_id "
                    + "LEFT JOIN client_bar_code cbc ON d.id = cbc.device_id "
                    + "LEFT JOIN nfc  ON d.id = nfc.device_id "
                    + "LEFT JOIN client_nfc cnfc ON d.id = cnfc.device_id "
                    + "LEFT JOIN location l ON l.id = d.location_id "
                    + "LEFT JOIN floor f ON f.id = l.floor_id "
                    + "LEFT JOIN building b ON b.id = f.building_id "
                    + "LEFT JOIN device_specification ds ON ds.device_id = d.id "
                    + "WHERE ('" + vdmsid + "' = 'null' or d.docker_vdms_id = '" + vdmsid
                    + "') AND ('" + dockername + "' = 'all' or  d.docker_name = '" + dockername + "') "
                    + "AND ((" + onboard_status + " = 123) or IF(" + onboard_status + " = 210, d.onboard_status != 3 ,(" + onboard_status + " = d.onboard_status ))) "
                    + "AND (" + virtual_device_type + " IS NULL or IF(" + virtual_device_type + " = 123, (virtual_device_type IS NOT NULL AND (virtual_device_type != 0 AND virtual_device_type != 1)),NULL)) "
                    + "AND (" + status + " IS NULL or d.status = " + status + ") "
                    + "AND (" + assigned_status + " IS NULL or IF(" + assigned_status + " = 0, (d.assigned_user_email IS NULL or d.assigned_user_email = 'null'), ( d.assigned_user_email IS NOT NULL or d.assigned_user_email != 'null')))"
                    + "AND (" + monitor + " = 123  or IF(" + monitor + " = 1," + monitor + " = monitor, monitor IS NULL or " + monitor + " = monitor)) "
                    + "AND ((" + asset_match_status + " IS NULL and asset_match_status != 3) or "
                    + "IF(" + asset_match_status + " = 3, asset_match_status = " + asset_match_status + ", "
                    + "(asset_match_status = " + asset_match_status + " and asset_match_status != 3))) "
                    + generateDeviceIdsFilterCustomQuery
                    + searchAndFilterCustomQuery;

            System.out.println("CONSTRUCTED COUNT QUERY: " + query);

            var queryResult = jdbcTemplate.queryForList(query);

            for (Map<String, Object> stringObjectMap : queryResult) {
                return String.valueOf(stringObjectMap.get("count"));
            }
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
