package io.sclera.Repository;

import com.alibaba.fastjson.JSONArray;
import io.sclera.dto.*;
import io.sclera.dto.touchscreen.*;
import io.sclera.dto.touchscreen.assetmapper.AssetDTO;
import io.sclera.dto.touchscreen.assetmapper.AssetDeviceDTO;
import io.sclera.models.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface DeviceRepository extends JpaRepository<Device, String> {

    @Query(nativeQuery = true)
    Set<DeviceDTO> listAllDevicebyVdmsidAndDockerName(String vdmsid, String dockername);


    @Query(nativeQuery = true)
    Set<DeviceDTO> getfilterdevices(String vdmsid, String dockername, String searchKey, Integer virtual_device_type,
                                    Integer status, Integer monitor, Integer asset_match_status, Integer pageSize, Integer offset, Integer assigned_status);


    @Query(nativeQuery = true)
    DeviceDTO getDeviceByDeviceId(String device_id);

    @Query(value = "SELECT COUNT(id) FROM device WHERE mac_address =?1  AND docker_vdms_id =?2  AND  docker_name =?3", nativeQuery = true)
    int checkDeviceByDeviceId(String mac_address, String vdms_id, String docker_name);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO device(id, docker_vdms_id, docker_name, ip_address, status, mac_address, last_seen_on, display_name, vendor, created_timestamp, user_data_name, type, description, custom_fields, created_email,asset_group) VALUES(?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8, ?9,?10,?11,?12,?13,?14,?15,?16)", nativeQuery = true)
    void insertDevice(String id, String vdms_id, String docker_name, String ip_address, Integer status,
                      String mac_address, String last_seen_on, String display_name, String vendor, BigInteger created_timestamp,
                      String user_data_name, String type, String description, String customFields, String created_email, String asset_group);


    @Modifying
    @Transactional
    @Query(value = "UPDATE device SET ip_address =?1 , status =?2  , last_seen_on = ?3,  display_name = ?4, vendor = ?5, snmp_parent = ?6  WHERE docker_vdms_id = ?7 AND docker_name =?8 AND  mac_address = ?9 ", nativeQuery = true)
    void updateDevice(String ip_address, Integer status, String last_seen_on, String display_name, String vendor,
                      String snmp_parent, String vdms_id, String docker_name, String mac_address);

    @Modifying
    @Transactional
    @Query(value = "UPDATE device SET monitor = ?4,  network_layer = ?5, user_data_model = ?6,model = ?6, user_data_name = ?7, type = ?8, user_data_vendor = ?9, vendor = ?9, parent = ?10, remote_access = ?11, warranty = ?12, product_id = ?13,"
            + " location_id = ?14, email_alert = ?15, sms_alert = ?16, popup_notification = ?17, serial_number = ?18, local_vendor_email_alert = ?19,"
            + " local_vendor_sms_alert = ?20, subsystem_parent_id = ?21, custom_fields = IFNULL(?22, custom_fields), description = ?23, asset_match_status = ?24, asset_group = ?25, category = ?26, sub_category = ?27, location_status = ?28, "
            + " cost_value = ?29, assigned_user_email = ?30, ai_call = ?31, cost_unit = ?32, is_dnd_enabled = ?33, operational_status = ?34, adc_json = JSON_MERGE_PATCH(adc_json, CAST(?35 AS JSON)) "
            + " WHERE docker_vdms_id = ?2 AND docker_name = ?3 AND id = ?1", nativeQuery = true)
    int editDeviceByDeviceID(String device_id, String vdmsid, String dockername, Integer monitor, String network_layer, String user_data_model,
                             String user_data_name, String type, String user_data_vendor, String parent, Integer remote_access,
                             String warranty, String product_id, String location_id, Integer email_alert, Integer sms_alert,
                             Integer popup_notification, String serial_number, Integer local_vendor_email_alert, Integer local_vendor_sms_alert,
                             String subsystem_parent_id, String custom_fields, String description, Integer asset_match_status, String asset_group, String category, String sub_category, String location_status,
                             BigDecimal cost_value, String assigned_user_email, Boolean ai_call, String cost_unit, Boolean is_dnd_enabled, String operational_status, String adc_json);

    @Query(value = "SELECT product_id FROM device WHERE id = ?1", nativeQuery = true)
    String getProductIdByDeviceId(String device_id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE device SET global_vendor_id = IFNULL(?1 ,global_vendor_id) ,local_vendor_id = IFNULL(?2 ,local_vendor_id) ,"
            + "other_vendor_1_id = IFNULL(?3 ,other_vendor_1_id) , other_vendor_2_id = IFNULL(?4 ,other_vendor_2_id) ,"
            + "other_vendor_3_id = IFNULL(?5,other_vendor_3_id) WHERE id = ?6 ", nativeQuery = true)
    Integer updateDeviceVendorsByDeviceID(String global_vendor_id, String local_vendor_id, String other_vendor_1_id,
                                          String other_vendor_2_id, String other_vendor_3_id, String device_id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE device \n" + "	SET global_vendor_id = CASE\n"
            + "							WHEN ?1 = 'global' THEN ?2\n"
            + "							ELSE global_vendor_id\n" + "							END,\n" + "\n"
            + "		local_vendor_id = CASE\n" + "							WHEN ?1 = 'local' THEN ?2\n"
            + "							ELSE local_vendor_id\n" + "							END,\n" + "		\n"
            + "		other_vendor_1_id = CASE\n" + "							WHEN ?1 = 'other_1' THEN  ?2\n"
            + "							ELSE other_vendor_1_id\n" + "							END,\n" + "		\n"
            + "		other_vendor_2_id = CASE\n" + "							WHEN ?1 = 'other_2' THEN ?2\n"
            + "							ELSE other_vendor_2_id\n" + "							END,\n" + "		\n"
            + "		other_vendor_3_id = CASE\n" + "							WHEN ?1 = 'other_3' THEN ?2\n"
            + "							ELSE other_vendor_3_id\n" + "							END\n"
            + "		WHERE id = ?3", nativeQuery = true)
    void linkVendorByVendorIdAndDeviceId(String vendor_type, String id, String device_id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE device\n" + "	SET global_vendor_id = CASE\n"
            + "							WHEN global_vendor_id = ?1 AND ?2 = 'global' THEN global_vendor_id = NULL\n"
            + "							ELSE global_vendor_id\n" + "							END,\n" + "\n"
            + "	local_vendor_id = 		CASE\n"
            + "							WHEN local_vendor_id = ?1 AND ?2 = 'local' THEN local_vendor_id = NULL\n"
            + "							ELSE local_vendor_id\n" + "							END,				\n"
            + "\n" + "	other_vendor_1_id = 	CASE\n"
            + "							WHEN other_vendor_1_id = ?1 AND ?2 = 'other_1' THEN other_vendor_1_id = NULL\n"
            + "							ELSE other_vendor_1_id\n" + "							END,\n" + "\n"
            + "	other_vendor_2_id = 	CASE\n"
            + "							WHEN other_vendor_2_id = ?1 AND ?2 = 'other_2' THEN other_vendor_2_id = NULL\n"
            + "							ELSE other_vendor_2_id\n" + "							END,	\n" + "\n"
            + "	other_vendor_3_id = 	CASE\n"
            + "							WHEN other_vendor_3_id = ?1 AND ?2 = 'other_3' THEN other_vendor_3_id = NULL\n"
            + "							ELSE other_vendor_3_id\n" + "							END	\n"
            + "	WHERE id = ?3", nativeQuery = true)
    void unlinkVendorByVendorIdAndDeviceId(String phoneaccount, String vendor_type, String device_id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE device SET user_data_name = IFNULL(?2 ,user_data_name), user_data_model = IFNULL(?3 ,user_data_model) ,user_data_vendor = IFNULL(?4 ,user_data_vendor) ,"
            + "type = IFNULL(?5 ,type) ,warranty = IFNULL(?6 ,warranty) ,network_layer = IFNULL(?7 ,network_layer) ,"
            + "location_id = IF(?8 = 'null' or ?8 = '', NULL, IFNULL(?8, location_id)), parent = IFNULL(?9 ,parent) ,product_id = IFNULL(?10 ,product_id) ,"
            + "monitor = IFNULL(?11 ,monitor) ,remote_access = IFNULL(?12 ,remote_access) ,global_vendor_id = IFNULL(?13 ,global_vendor_id) ,"
            + "local_vendor_id = IFNULL(?14 ,local_vendor_id) ,other_vendor_1_id = IFNULL(?15 ,other_vendor_1_id) ,"
            + "other_vendor_2_id = IFNULL(?16 ,other_vendor_2_id) ,other_vendor_3_id = IFNULL(?17 ,other_vendor_3_id), "
            + "email_alert = IFNULL(?18 ,email_alert), sms_alert = IFNULL(?19 ,sms_alert), popup_notification = IFNULL(?20 ,popup_notification),"
            + "local_vendor_email_alert = IFNULL(?21 ,local_vendor_email_alert), local_vendor_sms_alert = IFNULL(?22 ,local_vendor_sms_alert), "
            + "subsystem_parent_id = IFNULL(?23 ,subsystem_parent_id), description = IFNULL(?24, description), asset_match_status = IFNULL(?25, asset_match_status), custom_fields = IFNULL(?26, custom_fields), docker_name = ?27, asset_group = IFNULL(?28, asset_group), category = IFNULL(?29, category), sub_category = IFNULL(?30, sub_category), location_status = IF( ?8 IS NULL, location_status, NULL) "
            + "WHERE id = ?1", nativeQuery = true)
    void quickUpdate(String id, String user_data_name, String user_data_model, String user_data_vendor, String type,
                     String warranty, String network_layer, String location_id, String parent_device_id, String product_id,
                     Integer monitor, Integer remote_access, String global_vendor_id, String local_vendor_id,
                     String other_vendor_1_id, String other_vendor_2_id, String other_vendor_3_id, Integer email_alert,
                     Integer sms_alert, Integer popup_notification, Integer local_vendor_email_alert, Integer local_vendor_sms_alert,
                     String subsystem_parent_id, String description, Integer asset_match_status, String custom_fields, String docker_name, String asset_group, String category, String sub_category);

    @Query(nativeQuery = true)
    Set<DeviceDTO> getDeviceNamesByVdmsIdAndDockerName(String vdmsid, String dockername);

//	@Modifying
//	@Transactional
//	@Query(value = "INSERT INTO device(id,ip_address,mac_address,user_data_name,user_data_model,user_data_vendor,type,"
//			+ "location_id,network_layer,parent,snmp_parent,monitor,docker_name,docker_vdms_id,last_seen_on,warranty,status,"
//			+ "email_alert, sms_alert, popup_notification, virtual_device_type, serial_number)"
//			+ "VALUE(?1,?2,?3,?4,?5,?6,?7,?8,?9,?10,?11,?12,?13,?14,?15,?16,?17,?18,?19,?20,?21,?22)", nativeQuery = true)
//	void addVirtualDevice(String final_device_id, String ip_address, String mac_address, String user_data_name,
//			String user_data_model, String user_data_vendor, String type, String location_id, String network_layer,
//			String parent, String snmp_parent, Integer monitor, String docker_name, String vdms_id, String last_seen_on,
//			String warranty, Integer status, Integer email_alert, Integer sms_alert, Integer popup_notification,
//			Integer virtual_device_type, String serial_number);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO device(id,ip_address,mac_address,user_data_name,user_data_model,user_data_vendor,type,"
            + "location_id,network_layer,parent,snmp_parent,monitor,docker_name,docker_vdms_id,last_seen_on,warranty,status,"
            + "email_alert, sms_alert, popup_notification, virtual_device_type, serial_number, local_vendor_email_alert,local_vendor_sms_alert, "
            + "subsystem_parent_id, description, asset_match_status, created_timestamp, created_email,asset_group, category, sub_category, cost_value, assigned_user_email, ai_call, cost_unit, is_dnd_enabled, operational_status)"
            + "VALUE(?1,?2,?3,?4,?5,?6,?7,?8,?9,?10,?11,?12,?13,?14,?15,?16,?17,?18,?19,?20,?21,?22, ?23, ?24, ?25, ?26, ?27, ?28,?29,?30, ?31, ?32, ?33, ?34, ?35, ?36, ?37, ?38)", nativeQuery = true)
    void addVirtualDevice(String final_device_id, String ip_address, String mac_address, String user_data_name,
                          String user_data_model, String user_data_vendor, String type, String location_id, String network_layer,
                          String parent, String snmp_parent, Integer monitor, String docker_name, String vdms_id, String last_seen_on,
                          String warranty, Integer status, Integer email_alert, Integer sms_alert, Integer popup_notification,
                          Integer virtual_device_type, String serial_number, Integer local_vendor_email_alert, Integer local_vendor_sms_alert,
                          String subsystem_parent_id, String description, Integer asset_match_status, BigInteger created_timestamp, String created_email, String asset_group, String category, String sub_category,
                          BigDecimal cost_value, String assigned_user_email, Boolean ai_call, String cost_unit, Boolean is_dnd_enabled, String operational_status);

    @Modifying
    @Transactional
    @Query(value = "UPDATE device SET monitor = ?2, location_id = ?3, network_layer = ?4, user_data_model = ?5, type = ?6, "
            + "user_data_vendor = ?7, user_data_name = ?8, parent = ?9, remote_access = ?10, product_id = ?11, warranty = ?12, "
            + "ip_address = ?13, email_alert = ?14, sms_alert = ?15, popup_notification = ?16, virtual_device_type = ?17, "
            + "serial_number = ?18, local_vendor_email_alert = ?19, local_vendor_sms_alert = ?20, docker_name = ?21, subsystem_parent_id = ?22, "
            + "custom_fields = IFNULL(?23, custom_fields), description = ?24, asset_match_status = ?25,asset_group = ?26, category = ?27, "
            + "sub_category = ?28, location_status = ?29, cost_value = ?30, assigned_user_email = ?31, ai_call = ?32, cost_unit = ?33, is_dnd_enabled = ?34, "
            + "operational_status = ?35, adc_json = JSON_MERGE_PATCH(adc_json, CAST(?36 AS JSON)), model = ?5, vendor = ?7  "
            + "WHERE id = ?1", nativeQuery = true)
    void editVirtualDeviceByVirtualDeviceId(String virtual_device_id, Integer monitor, String location_id, String network_layer,
                                            String user_data_model, String type, String user_data_vendor, String user_data_name, String parent,
                                            Integer remote_access, String product_id, String warranty, String ip_address, Integer email_alert,
                                            Integer sms_alert, Integer popup_notification, Integer virtual_device_type, String serial_number,
                                            Integer local_vendor_email_alert, Integer local_vendor_sms_alert, String docker_name, String subsystem_parent_id, String custom_fields, String description,
                                            Integer asset_match_status, String asset_group, String category, String sub_category, String location_status, BigDecimal cost_value, String assigned_user_email,
                                            Boolean ai_call, String cost_unit, Boolean is_dnd_enabled, String operational_status, String adc_json);


    // delete is done by cascade delete, if this query not required can be deleted
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM device WHERE id = ?1 AND is_virtual = true", nativeQuery = true)
    void deleteVirtualDeviceByeviceId(String virtual_device_id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE device SET user_data_name = ?2 ,user_data_model = ?3 ,user_data_vendor = ?4 ,type = ?5 ,"
            + "network_layer = ?6 ,location_id = ?7 ,parent = ?8 ,warranty = ?9 ,monitor = ?10 ,remote_access = ?11 ,"
            + "product_id = ?12 ,global_vendor_id = ?13 ,local_vendor_id = ?14 ,other_vendor_1_id = ?15 ,"
            + "other_vendor_2_id = ?16 ,other_vendor_3_id = ?17, email_alert = ?18, sms_alert = ?19, popup_notification = ?20, "
            + "serial_number = ?21, local_vendor_email_alert = ?22, local_vendor_sms_alert = ?23, docker_name = ?24, subsystem_parent_id = ?25, "
            + "custom_fields = IFNULL(?26, custom_fields), description = ?27, asset_match_status = ?28, asset_group = ?29, category = ?30, sub_category = ?31, location_status = IF( ?7 IS NULL, location_status, NULL)  "
            + "WHERE id = ?1 ", nativeQuery = true)
    void multiDeviceUpdateByDeviceId(String id, String user_data_name, String user_data_model, String user_data_vendor,
                                     String type, String network_layer, String location_id, String parent, String warranty, Integer monitor,
                                     Integer remote_access, String product_id, String global_vendor_id, String local_vendor_id,
                                     String other_vendor_1_id, String other_vendor_2_id, String other_vendor_3_id, Integer email_alert,
                                     Integer sms_alert, Integer popup_notification, String serial_number, Integer local_vendor_email_alert,
                                     Integer local_vendor_sms_alert, String docker_name, String subsystem_parent_id, String custom_fields, String description,
                                     Integer asset_match_status, String asset_group, String category, String sub_category);


    @Transactional
    @Modifying
    @Query(value = "UPDATE device SET status = ?1, last_seen_on = ?2 WHERE id = ?3", nativeQuery = true)
    void updateVirtualDeviceStatus(Integer status, BigInteger timestamp, String virtual_device_id);

    @Query(nativeQuery = true)
    List<DeviceDTO> listAllVirtualdevices();

    // Get Alert Device Info

    @Query(nativeQuery = true)
    AlertDTO getDeviceAlertInfoByDeviceId(String device_id);

    // update snmp count
    @Transactional
    @Modifying
    @Query(value = "UPDATE device SET snmp_count = ?2 WHERE id = ?1", nativeQuery = true)
    void updateDeviceSnmpCount(String device_id, Integer snmp_count);

    // update snmp status
    @Transactional
    @Modifying
    @Query(value = "UPDATE device SET snmp_status = ?2 WHERE id = ?1", nativeQuery = true)
    void updateDeviceSnmpStatus(String device_id, String snmp_status);

    // update interface count
    @Transactional
    @Modifying
    @Query(value = "UPDATE device SET interface_count = ?2 WHERE id = ?1", nativeQuery = true)
    void updateDeviceInterfaceCount(String device_id, Integer interface_count);

    // update notes count
    @Transactional
    @Modifying
    @Query(value = "UPDATE device SET notes_count = ?2 WHERE id = ?1", nativeQuery = true)
    void updateDeviceNotesCount(String device_id, Integer notes_count);

    // update ticket count
    @Transactional
    @Modifying
    @Query(value = "UPDATE device SET ticket_count = ?2 WHERE id = ?1", nativeQuery = true)
    void updateDeviceTicketCount(String device_id, Integer ticket_count);

    // update ticket status
    @Transactional
    @Modifying
    @Query(value = "UPDATE device SET ticket_status = ?2 WHERE id = ?1", nativeQuery = true)
    void updateDeviceTicketStatus(String device_id, String ticket_status);

    // update bacnet count
    @Transactional
    @Modifying
    @Query(value = "UPDATE device SET bacnet_count = ?2 WHERE id = ?1", nativeQuery = true)
    void updateDeviceBacnetCount(String device_id, Integer bacnet_count);

    // update bacnet status
    @Transactional
    @Modifying
    @Query(value = "UPDATE device SET bacnet_status = ?2 WHERE id = ?1", nativeQuery = true)
    void updateDeviceBacnetStatus(String device_id, String bacnet_status);

    // update lorawan count
    @Transactional
    @Modifying
    @Query(value = "UPDATE device SET lorawan_count = ?2 WHERE id = ?1", nativeQuery = true)
    void updateDeviceLorawanCount(String device_id, Integer lorawan_count);

    // update lorawan status
    @Transactional
    @Modifying
    @Query(value = "UPDATE device SET lorawan_status = ?2 WHERE id = ?1", nativeQuery = true)
    void updateDeviceLorawanStatus(String device_id, String lorawan_status);

    // update disruptive count
    @Transactional
    @Modifying
    @Query(value = "UPDATE device SET disruptive_count = ?2 WHERE id = ?1", nativeQuery = true)
    void updateDeviceDisruptiveCount(String device_id, Integer disruptive_count);

    // update disruptive status
    @Transactional
    @Modifying
    @Query(value = "UPDATE device SET disruptive_status = ?2 WHERE id = ?1", nativeQuery = true)
    void updateDeviceDisruptiveStatus(String device_id, String disruptive_status);

    // update my devices count
    @Modifying
    @Transactional
    @Query(value = "UPDATE device SET my_devices_count = ?2 WHERE id = ?1", nativeQuery = true)
    void updateDeviceMyDevicesCount(String device_id, Integer my_devices_count);

    // update my devices status
    @Modifying
    @Transactional
    @Query(value = "UPDATE device SET my_devices_status = ?2 WHERE id = ?1", nativeQuery = true)
    void updateDeviceMyDevicesStatus(String device_id, String my_devices_status);

    // update monnit count
    @Modifying
    @Transactional
    @Query(value = "UPDATE device SET monnit_count = ?2 WHERE id = ?1", nativeQuery = true)
    void updateDeviceMonnitCount(String device_id, Integer monnit_count);

    // update monnit status
    @Modifying
    @Transactional
    @Query(value = "UPDATE device SET monnit_status = ?2 WHERE id = ?1", nativeQuery = true)
    void updateDeviceMonnitStatus(String device_id, String monnit_status);

    // update pelican count
    @Modifying
    @Transactional
    @Query(value = "UPDATE device SET pelican_count = ?2 WHERE id = ?1", nativeQuery = true)
    void updateDevicePelicanCount(String device_id, Integer pelican_count);

    // update  pelican status
    @Modifying
    @Transactional
    @Query(value = "UPDATE device SET pelican_status = ?2 WHERE id = ?1", nativeQuery = true)
    void updateDevicePelicanStatus(String device_id, String pelican_status);

    // update knx count
    @Transactional
    @Modifying
    @Query(value = "UPDATE device SET knx_count = ?2 WHERE id = ?1", nativeQuery = true)
    void updateDeviceKNXCount(String device_id, Integer knx_count);

    // update  knx status
    @Modifying
    @Transactional
    @Query(value = "UPDATE device SET knx_status = ?2 WHERE id = ?1", nativeQuery = true)
    void updateDeviceKNXStatus(String device_id, String knx_status);

    // update measure count
    @Transactional
    @Modifying
    @Query(value = "UPDATE device SET measuring_instrument_count = ?2 WHERE id = ?1", nativeQuery = true)
    void updateDeviceMeasureCount(String device_id, Integer measuring_instrument_count);

    // update documents count
    @Transactional
    @Modifying
    @Query(value = "UPDATE device SET document_count = ?2 WHERE id = ?1", nativeQuery = true)
    void updateDeviceDocumentsCount(String device_id, Integer document_count);


    // update media count
    @Transactional
    @Modifying
    @Query(value = "UPDATE device SET media_count = ?2 WHERE id = ?1", nativeQuery = true)
    void updateDeviceMediaCount(String device_id, Integer media_count);

    // update checklists count
    @Transactional
    @Modifying
    @Query(value = "UPDATE device SET checklist_template_count = ?2 WHERE id = ?1", nativeQuery = true)
    void updateDeviceCheckListsCount(String device_id, Integer checklist_template_count);

    //update snmp object count
    @Transactional
    @Modifying
    @Query(value = "UPDATE device SET snmp_object_count = ?2 WHERE id = ?1", nativeQuery = true)
    void updateDeviceSnmpObjectCount(String device_id, Integer snmp_object_count);

    //update snmp object status
    @Transactional
    @Modifying
    @Query(value = "UPDATE device SET snmp_object_status = ?2 WHERE id = ?1", nativeQuery = true)
    void updateDeviceSnmpObjectStatus(String device_id, String snmp_object_status);

    //Get All Parent Device by Pagination
//	@Query(nativeQuery = true)
//	Set<DeviceDTO> getNetworkParentDeviceByPagination(Set<String> dockernames, String searchKey,Integer pagesize, Integer offset);

    @Query(nativeQuery = true)
    Set<DeviceDTO> getNetworkParentDeviceByPagination(Set<String> dockernames, Set<String> types, String searchKey, Integer pagesize, Integer offset, Set<String> virtual_device_types);


    //Get Network Parent Device by Pagination
    @Query(nativeQuery = true)
    Set<DeviceDTO> getAllParentDeviceByPagination(String searchKey, Integer pagesize, Integer offset);

    //Get Parent Device Name by Id
    @Query(value = "Select IF((d.user_data_name IS NULL or d.user_data_name = ''), d.display_name, d.user_data_name) from device d where d.id = ?1", nativeQuery = true)
    String getParentDeviceNameById(String parent_device_id);

    //new get method with subsystem parent devices get
    @Query(nativeQuery = true)
    Set<DeviceDTO> getSubsystemParentDevicesByPagination(String vdmsid, String dockername, Integer virtual_device_type,
                                                         Integer status, Integer monitor, Integer asset_match_status, Integer pagesize, Integer offset, Integer onboard_status, Integer assigned_status, String assignee);

    @Query(nativeQuery = true)
    Set<DeviceDTO> getSubsystemParentDevices(String vdmsid, String dockername, Integer virtual_device_type, Integer status, Integer monitor, Integer asset_match_status, Integer onboard_status, Integer assigned_status);

    //new get method with subsystem devices get
    @Query(nativeQuery = true)
    Set<DeviceDTO> getSubsystemDevicesByPagination(String device_id, Integer pagesize, Integer offset, String assignee);

    @Modifying
    @Transactional
    @Query(value = "UPDATE device SET subsystem_count = ?2 WHERE id = ?1", nativeQuery = true)
    void updateSubsystemCount(String device_id, Integer subsystem_count);

    @Query(value = "SELECT COUNT(*) FROM device WHERE subsystem_parent_id= ?1", nativeQuery = true)
    Integer getSubsystemCount(String device_id);

    //get sub system parent id
    @Query(value = "SELECT subsystem_parent_id FROM device WHERE id = ?1 ", nativeQuery = true)
    String getSubsystemParentId(String id);

    //get devices by ids
    @Query(nativeQuery = true)
    Set<DeviceDTO> getDevicesByIdList(Set<String> device_ids);

    @Query(value = "SELECT id FROM device WHERE subsystem_parent_id = ?1", nativeQuery = true)
    List<String> getDevicesBySubSystemParentId(String device_id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE device SET subsystem_parent_id = ?2 WHERE id = ?1", nativeQuery = true)
    void updateSubsystemParentDevice(String device_id, String subsystem_parent_id);
    // Touchscreen
    // Repository*********************************************************************************************************************************************************************************8

    //To be removed after new pagination api works
    @Query(nativeQuery = true)
    Set<DeviceListDTO> listDevicesTs(String networkname, String buildingid, String floorid, String locationid,
                                     Integer devicestatus);

    //Added Pagination for listDevices
    @Query(nativeQuery = true)
    Set<DeviceListDTO> listDevicesByPaginationTs(String networkname, String buildingid, String floorid, String locationid, Integer status, Integer pagesize, Integer offset, Integer virtual_device_type);

    @Modifying
    @Transactional
    @Query(value = "UPDATE device SET snmp_parent = ?3, type = IFNULL(?4, type) WHERE docker_name = ?1 AND id = ?2", nativeQuery = true)
    void updateSnmpParent(String dockername, String id, String snmp_parent, String device_type);


    //to be removed after pagination api works
    @Query(nativeQuery = true)
    Set<DeviceListDTO> listofflinedeviceByParentTs();

    //Added pagination for listofflinedeviceByParentTs
    @Query(nativeQuery = true)
    Set<DeviceListDTO> listofflinedeviceByParentByPaginationTs(Integer pagesize, Integer offset);

    @Query(nativeQuery = true)
    DeviceListDTO DeviceInfoById(String deviceId);

    // API FROM BACKEND

    @Query(nativeQuery = true)
    DeviceDetailsDTO getDeviceInfoById(String deviceid);

    @Transactional
    @Modifying
    @Query(value = "INSERT INTO device (ip_address, mac_address , status, last_seen_on, docker_vdms_id, docker_name, id, vendor, created_timestamp, created_email ) VALUES (?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8, ?9, ?10)", nativeQuery = true)
    void insertDeviceStatus(String ip_address, String mac_address, Integer status, BigInteger last_seen_on,
                            String vdms_id, String docker_name, String id, String vendor, BigInteger created_timestamp, String created_email);

    @Transactional
    @Modifying
    @Query(value = "UPDATE device SET ip_address = IFNULL(?1, ip_address), mac_address = ?2, status = IFNULL(?3, status), last_seen_on = IFNULL(?4, last_seen_on) WHERE docker_vdms_id = ?5 AND docker_name =?6 AND id = ?7 ", nativeQuery = true)
    void updateDeviceStatus(String ip_address, String mac_address, Integer status, BigInteger last_seen_on,
                            String vdms_id, String docker_name, String id);

    @Query(nativeQuery = true)
    List<DeviceMonitorDTO> getDeviceListMonitor(String dockername);

    @Query(nativeQuery = true)
    List<DeviceMonitorDTO> getDeviceListMonitorIp(String dockername);

    @Query(value = "SELECT COUNT(id) FROM device WHERE status = ?1 AND monitor = 1 AND asset_match_status != 3  AND (onboard_status IS NULL OR (onboard_status != 1 AND onboard_status != 2))", nativeQuery = true)
    int onlineOfflineCount(Integer i);

    @Query(nativeQuery = true)
    List<SnmpValuesDTO> getDeviceListSnmp(String dockername);

    // Get Single Device Info By Device Id for Snmp Sync
    @Query(nativeQuery = true)
    SnmpValuesDTO getDeviceSnmpByDeviceId(String dockername, String device_id);

    @Transactional
    @Query(value = "SELECT status FROM device WHERE id = ?1", nativeQuery = true)
    Integer getDeviceStatus(String deviceId);

    @Transactional
    @Query(value = "SELECT parent FROM device WHERE id = ?1", nativeQuery = true)
    String getDeviceparent(String deviceId);

    @Transactional
    @Query(value = "SELECT snmp_parent FROM device WHERE id = ?1", nativeQuery = true)
    String getDeviceSnmpparent(String deviceId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE device\n" + "	SET global_vendor_id = CASE\n"
            + "							WHEN global_vendor_id = ?1 THEN global_vendor_id = NULL\n"
            + "							ELSE global_vendor_id\n" + "							END,\n" + "\n"
            + "	local_vendor_id = 		CASE\n"
            + "							WHEN local_vendor_id = ?1 THEN local_vendor_id = NULL\n"
            + "							ELSE local_vendor_id\n" + "							END,				\n"
            + "\n" + "	other_vendor_1_id = 	CASE\n"
            + "							WHEN other_vendor_1_id = ?1 THEN other_vendor_1_id = NULL\n"
            + "							ELSE other_vendor_1_id\n" + "							END,\n" + "\n"
            + "	other_vendor_2_id = 	CASE\n"
            + "							WHEN other_vendor_2_id = ?1 THEN other_vendor_2_id = NULL\n"
            + "							ELSE other_vendor_2_id\n" + "							END,	\n" + "\n"
            + "	other_vendor_3_id = 	CASE\n"
            + "							WHEN other_vendor_3_id = ?1 THEN other_vendor_3_id = NULL\n"
            + "							ELSE other_vendor_3_id\n" + "							END	\n"
            + "	WHERE  docker_vdms_id = ?2", nativeQuery = true)
    void unLinkVendorByVendorIdAndVdmsId(String vendor_id, String vdmsid);

    @Modifying
    @Transactional
    @Query(value = "UPDATE device SET display_name = COALESCE(?2, 'Generic') WHERE id = ?1", nativeQuery = true)
    void updateDevicesDisplayNameById(String id, String display_name);

    @Modifying
    @Transactional
    @Query(value = "UPDATE device SET vendor = ?2 WHERE id = ?1", nativeQuery = true)
    void updateDeviceVendorById(String id, String vendor);

    @Transactional
    @Query(nativeQuery = true)
    List<DeviceDTO> listAlldevices();

    @Transactional
    @Query(value = "SELECT COUNT(id) FROM device WHERE status = ?2 AND monitor = 1 AND (?1 = 'all' or docker_name = ?1) AND asset_match_status != 3 AND IF('all' = ?3, true , assigned_user_email = ?3) ", nativeQuery = true)
    Integer onlineOfflineCountByDocker(String dockername, int i, String assignee);

    @Transactional
    @Query(value = "SELECT COUNT(id) FROM device WHERE (?1 = 'all' or docker_name = ?1) AND (monitor = 0 OR monitor IS NULL) AND asset_match_status != 3 AND IF('all' = ?2, true , assigned_user_email = ?2) ", nativeQuery = true)
    Integer unmonitorCountByDocker(String dockername, String assignee);

    @Transactional
    @Query(value = "SELECT COUNT(id) FROM device WHERE (?1 = 'all' or docker_name = ?1) AND monitor = 1  AND asset_match_status != 3 AND IF('all' = ?2, true , assigned_user_email = ?2) ", nativeQuery = true)
    Integer monitorCountByDocker(String dockername, String assignee);

    @Transactional
    @Query(value = "SELECT COUNT(id) FROM device WHERE (?1 = 'all' or docker_name = ?1) AND (assigned_user_email IS NOT NULL or assigned_user_email != 'null')  AND asset_match_status != 3 AND IF('all' = ?2, true , assigned_user_email = ?2) ", nativeQuery = true)
    Integer assignedCountByDocker(String dockername, String assignee);


    @Query(value = "SELECT COUNT(id) FROM device WHERE (?1 = 'all' or docker_name = ?1) AND (assigned_user_email IS NULL or assigned_user_email = 'null')  AND asset_match_status != 3 AND IF('all' = ?2, true , assigned_user_email = ?2) ", nativeQuery = true)
    Integer unAssignedCountByDocker(String dockername, String assignee);
    // other device count
    @Transactional
    @Query(value = "SELECT COUNT(id) FROM device WHERE (?1 = 'all' or docker_name = ?1) AND monitor = 1 AND ( virtual_device_type IS NOT NULL AND (virtual_device_type != 0 AND virtual_device_type != 1 ) ) AND asset_match_status != 3 ", nativeQuery = true)
    Integer otherDeviceCountByDocker(String dockername);

    // other device count
    @Transactional
    @Query(value = "SELECT COUNT(id) FROM device WHERE (?1 = 'all' or docker_name = ?1) AND monitor = 1 AND ( virtual_device_type IS NOT NULL AND (virtual_device_type != 0 AND virtual_device_type != 1 ) ) AND asset_match_status != 3 AND IF('all' = ?2, true , assigned_user_email = ?2) ", nativeQuery = true)
    Integer otherDeviceCountByDockerAssignee(String dockername, String assignee);

    // matched/unmatched device count
    @Transactional
    @Query(value = "SELECT COUNT(id) FROM device WHERE (?1 = 'all' or docker_name = ?1) AND asset_match_status = ?2 AND IF('all' = ?3, true , assigned_user_email = ?3)", nativeQuery = true)
    Integer getMatchedUnmatchedDeviceCountByDocker(String dockername, int i, String assignee);

    @Transactional
    @Query(value = "SELECT COUNT(id) FROM device WHERE (?1 = 'all' or docker_name = ?1)  AND asset_match_status != 3 AND  (onboard_status != 3) AND IF('all' = ?2, true , assigned_user_email = ?2) ", nativeQuery = true)
    Integer getNotOnboardedDeviceCountByDocker(String dockername, String assignee);

    @Transactional
    @Query(value = "SELECT COUNT(id) FROM device WHERE (?1 = 'all' or docker_name = ?1)   AND asset_match_status != 3 AND (onboard_status = 3) AND IF('all' = ?2, true , assigned_user_email = ?2) ", nativeQuery = true)
    Integer getOnboardedDeviceCountByDocker(String dockername, String assignee);


    // get monitored device count
    @Transactional
    @Query(value = "SELECT COUNT(id) FROM device WHERE (?1 = 'all' or docker_name = ?1) AND monitor = 1 AND asset_match_status != 3", nativeQuery = true)
    Integer getMonitoredDeviceCountByDocker(String docker_name);

    //get all device count by docker
    @Transactional
    @Query(value = "SELECT COUNT(id) FROM device WHERE (?1 = 'all' or docker_name = ?1) AND asset_match_status != 3 ", nativeQuery = true)
    Integer getAllDeviceCountByDocker(String docker_name);

    //get all device count by docker
    @Transactional
    @Query(value = "SELECT COUNT(id) FROM device WHERE (?1 = 'all' or docker_name = ?1) AND asset_match_status != 3 AND IF('all' = ?2, true , assigned_user_email = ?2) ", nativeQuery = true)
    Integer getAllDeviceCountByDockerAssignee(String docker_name, String assignee);

    @Transactional
    @Query(nativeQuery = true)
    List<DeviceDTO> listTopologyDevicesByDockerName(String dockername, String device_id);

    @Transactional
    @Query(nativeQuery = true)
    List<DeviceTopologyDTO> listTopologyDevices(String dockername, String device_id);

    @Query(nativeQuery = true)
    Set<DevicedataDTO> getAllDevices();

    // get unique device types
    @Transactional
    @Query(value = "SELECT DISTINCT(d.type) FROM device d LEFT JOIN docker do ON (do.name = d.docker_name AND do.vdms_id = d.docker_vdms_id) LEFT JOIN location l ON l.id = d.location_id LEFT JOIN floor f ON f.id = l.floor_id WHERE (?1 = 'all' or d.docker_name = ?1) AND (?2 = 'null' or f.id = ?2) AND d.type IS NOT NULL AND d.monitor = 1 AND d.asset_match_status != 3 ORDER BY d.type", nativeQuery = true)
    List<String> getUniqueDeviceTypes(String network_name, String floor_id);

    @Transactional
    @Query(value = "SELECT DISTINCT(d.asset_group) FROM device d LEFT JOIN docker do ON (do.name = d.docker_name AND do.vdms_id = d.docker_vdms_id) WHERE (?1 = 'all' or d.docker_name = ?1) AND d.asset_group IS NOT NULL ORDER BY d.asset_group", nativeQuery = true)
    List<String> getUniqueAssetGroups(String network_name);

    @Transactional
    @Query(value = "SELECT DISTINCT(d.category) FROM device d LEFT JOIN docker do ON (do.name = d.docker_name AND do.vdms_id = d.docker_vdms_id) WHERE (?1 = 'all' or d.docker_name = ?1) AND d.category IS NOT NULL ORDER BY d.category", nativeQuery = true)
    List<String> getUniqueCategory(String network_name);

    @Transactional
    @Query(value = "SELECT DISTINCT(d.sub_category) FROM device d LEFT JOIN docker do ON (do.name = d.docker_name AND do.vdms_id = d.docker_vdms_id) WHERE (?1 = 'all' or d.docker_name = ?1) AND d.category = ?2 AND d.sub_category IS NOT NULL ORDER BY d.sub_category", nativeQuery = true)
    List<String> getUniqueSubCategory(String network_name, String category);

    @Transactional
    @Query(value = "SELECT DISTINCT(d.assigned_user_email) FROM device d LEFT JOIN docker do ON (do.name = d.docker_name AND do.vdms_id = d.docker_vdms_id) WHERE d.docker_vdms_id = ?1 AND (?2 = 'all' or d.docker_name = ?2) AND d.assigned_user_email IS NOT NULL ORDER BY d.assigned_user_email", nativeQuery = true)
    List<String> getUniqueAssignedUserEmail(String vdms_id, String network_name);

    // Get Devices by types
    @Query(nativeQuery = true)
    List<DeviceSensorsDTO> getDevicesByType(String network_name, String floor_id, Set<String> types);

    @Query(nativeQuery = true)
    List<DeviceSensorsDTO> getDevicesByTypePagination(String network_name, String floor_id, Set<String> types, Integer pagesize,
                                                      Integer offset);


    @Query(value = "SELECT COUNT(*) FROM device d "
            + " LEFT JOIN docker do ON (do.name = d.docker_name AND do.vdms_id = d.docker_vdms_id) "
            + " LEFT JOIN location l ON d.location_id = l.id "
            + " LEFT JOIN floor f ON l.floor_id = f.id "
            + " WHERE (?1 = 'all' or d.docker_name = ?1) AND (?2 = 'null' or f.id = ?2) AND d.type IN ?3 AND d.monitor = 1 AND d.asset_match_status != 3", nativeQuery = true)
    String getDevicesByTypeCount(String network_name, String floor_id, Set<String> types);


    @Modifying
    @Transactional
    @Query(value = "UPDATE device SET position = ?2, location_id = ?3 , latitude = ?4, longitude = ?5, asset_match_status = ?6 WHERE id = ?1", nativeQuery = true)
    void updateDevicePosition(String id, String position, String location_id, String latitude, String longitude, Integer asset_match_status);

    @Modifying
    @Transactional
    @Query(nativeQuery = true)
    List<DeviceDTO> listDevicebyDockerIntegration(String dockername);


    @Modifying
    @Transactional
    @Query(value = "UPDATE device SET parent = ?2, user_connection_type = IFNULL(?3, user_connection_type) WHERE id = ?1", nativeQuery = true)
    void updateDeviceParent(String id, String parent, String user_connection_type);

    @Modifying
    @Transactional
    @Query(value = "UPDATE device SET parent = ?2 WHERE docker_name = ?1", nativeQuery = true)
    void resetDeviceParentByDockername(String dockername, String parent);

    @Transactional
    @Query(value = "SELECT DISTINCT(d.id) FROM device d JOIN device_ip_address dip ON d.id = dip.device_id WHERE dip.ip_address = ?1 LIMIT 1", nativeQuery = true)
    String getGatewayIdFromGatewayIp(String gatewayip);

    @Transactional
    @Modifying
    @Query(value = "UPDATE device SET asset_match_status = ?1, updated_email = ?3, updated_timestamp = ?4  WHERE id = ?2", nativeQuery = true)
    void updateDeviceAssetStatus(int assetMatchStatus, String deviceId, String updated_email, BigInteger updated_timestamp);

    /*****************************************Asset Mapper Queries*********************************************************/

    @Query(nativeQuery = true)
    List<AssetDeviceDTO> findByModel(String model);

    @Query(nativeQuery = true)
    List<AssetDeviceDTO> findByVendor(String vendor);

    @Query(nativeQuery = true)
    List<AssetDeviceDTO> findByDisplayName(String display_name);

    @Query(nativeQuery = true)
    List<AssetDeviceDTO> getPaginatedDevices(Integer pageSize, Integer offset);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO device (id, docker_name, docker_vdms_id, user_data_name, user_data_model, user_data_vendor, type, mac_address, ip_address,"
            + " network_layer, serial_number, warranty, custom_fields, subsystem_parent_id, virtual_device_type, monitor, subsystem_count,created_timestamp,  created_email) VALUE (?1, ?2, ?3, ?4,?5,?6,?7,?8,?9, ?10, ?11, ?12, ?13, ?14, ?15, ?16, ?17,?18,?19)"
            + " ON DUPLICATE KEY UPDATE user_data_name = IFNULL(?4, user_data_name), user_data_model = IFNULL(?5,user_data_model),"
            + " user_data_vendor = IFNULL(?6,user_data_vendor), type = IFNULL(?7, type), mac_address = IFNULL(?8, mac_address),"
            + " ip_address = IFNULL(?9, ip_address), network_layer = IFNULL(?10, network_layer), serial_number = IFNULL(?11, serial_number),"
            + " warranty = IFNULL(?12, warranty), custom_fields = IFNULL(?13, custom_fields)", nativeQuery = true)
    void upsertVirtualDeviceByAssetMapper(String id, String docker_name, String vdms_id, String user_data_name, String user_data_model, String user_data_vendor,
                                          String type, String mac_address, String ip_address, String network_layer, String serial_number, String warranty,
                                          String custom_fields, String subsystem_parent_id, Integer virtual_device_type, Integer monitor, Integer subsystem_count,
                                          BigInteger created_timestamp, String created_email);

    //get unmatched devices by pagination
    @Query(nativeQuery = true)
    List<AssetDTO> getUntaggedProductDevicesByPagination(Integer pageSize, Integer offset);

    @Transactional
    @Query(value = "SELECT COUNT(*) FROM device d WHERE d.product_id IS NULL", nativeQuery = true)
    Integer getUntaggedProductDevicesCount();

    @Modifying
    @Transactional
    @Query(value = "UPDATE device SET matched_product_ids = ?2 where id = ?1", nativeQuery = true)
    void updateDeviceMatchedProductIds(String device_id, String matched_product_ids);

    //get devices by device ids for asset mapper
    @Query(nativeQuery = true)
    List<AssetDTO> getAssetMapperDevicesByIdList(List<String> device_ids);

    //get sub system devices by device id for asset mapper
    @Query(nativeQuery = true)
    List<AssetDTO> getAssetMapperSubSystemDevicesById(String device_id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE device SET product_id = ?2, user_data_model = ?3, user_data_name = ?4, user_data_vendor = ?5, type = ?6, network_layer = ?7 where id = ?1", nativeQuery = true)
    void updateDeviceProductDetails(String id, String product_id, String user_data_model, String user_data_name, String user_data_vendor, String type, String network_layer);

    //get asset mapper device by id
    @Query(nativeQuery = true)
    AssetDTO getAssetMapperDeviceById(String device_id);

    /*****************************************Asset Mapper Queries*********************************************************/


    //list devices for snmp discovery
    @Query(nativeQuery = true)
    List<DeviceDTO> getAllDeviceByVdmsIdAndDockerName(String vdmsid, String dockername, Integer pagesize, Integer offset);


    //get device status count
    @Query(value = "SELECT COUNT(d.id) FROM device d"
            + " Left JOIN location l ON d.location_id = l.id"
            + " Left JOIN floor f ON l.floor_id = f.id"
            + " Left JOIN building b ON f.building_id = b.id"
            + " WHERE d.status = ?1 AND d.asset_match_status != 3  AND d.monitor = 1 AND (?2 = 'null' or d.docker_name = ?2) AND"
            + " (?3 = 'null' or b.id = ?3) AND (?4 = 'null' or f.id = ?4) AND (?5 = 'null' or l.id = ?5)", nativeQuery = true)
    int getDeviceStatusCountTS(Integer status, String networkname, String buildingid, String floorid, String locationid);

    //get monitored device count
    @Query(value = "SELECT COUNT(d.id) FROM device d"
            + " Left JOIN location l ON d.location_id = l.id"
            + " Left JOIN floor f ON l.floor_id = f.id"
            + " Left JOIN building b ON f.building_id = b.id"
            + " WHERE d.monitor = 1 AND d.asset_match_status != 3 AND (?1 = 'null' or d.docker_name = ?1) AND (?2 = 'null' or b.id = ?2) AND"
            + " (?3 = 'null' or f.id = ?3) AND (?4 = 'null' or l.id = ?4)", nativeQuery = true)
    Integer getMonitoredDevicesCountByDocker(String docker_name, String buildingid, String floorid, String locationid);

    @Query(value = "SELECT COUNT(d.id) FROM device d"
            + " LEFT JOIN location l ON d.location_id = l.id"
            + " LEFT JOIN floor f ON l.floor_id = f.id"
            + " LEFT JOIN building b ON f.building_id = b.id"
            + " WHERE (?1 = 'null' or d.docker_name = ?1) AND d.monitor = 1 AND d.asset_match_status != 3 AND  "
            + " (d.virtual_device_type IS NOT NULL AND (d.virtual_device_type != 0 AND d.virtual_device_type != 1)) "
            + " AND (?2 = 'null' or b.id = ?2) AND (?3 = 'null' or f.id = ?3) AND (?4 = 'null' or l.id = ?4)", nativeQuery = true)
    Integer otherDevicesCountByDocker(String dockername, String building_id, String floorid, String locationid);

    // update measuring instrument status
    @Modifying
    @Transactional
    @Query(value = "UPDATE device SET measuring_instrument_status = ?2 WHERE id = ?1", nativeQuery = true)
    void updateDeviceMeasuringInstrumentStatus(String device_id, String measuring_instrument_status);

    @Modifying
    @Transactional
    @Query(value = "UPDATE device SET latitude = ?2, longitude = ?3, position = ?4 WHERE id = ?1", nativeQuery = true)
    void updateDeviceCoordinates(String device_id, String latitude, String longitude, String position);

    @Query(nativeQuery = true)
    DeviceDTO getDeviceDetails(String checklist_id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE device SET record_checklist_status = ?2 WHERE id = ?1", nativeQuery = true)
    void updateDeviceRecordChecklistStatus(String device_id, String checklist_status);

    @Modifying
    @Transactional
    @Query(value = "UPDATE device SET record_checklist_count = ?2 WHERE id = ?1", nativeQuery = true)
    void updateDeviceRecordChecklistCount(String device_id, Integer record_checklist_count);


    @Modifying
    @Transactional
    @Query(value = "UPDATE device SET location_id = NULL, latitude = ?2, longitude = ?3, position = ?4 WHERE location_id = ?1", nativeQuery = true)
    void updateDeviceLocation(String location_id, String latitude, String longitude, String position);

    @Transactional
    @Modifying
    @Query(value = "UPDATE device SET daintree_count = ?1 WHERE id = ?2", nativeQuery = true)
    void updateDeviceDaintreeDevicesCount(Integer daintree_count, String device_id);

    @Transactional
    @Modifying
    @Query(value = "SELECT id from device WHERE (?1 = 'all' or docker_name = ?1) AND (bacnet_status = 'alert' or disruptive_status = 'alert' or lorawan_status = 'alert' or my_devices_status = 'alert' or "
            + "monnit_status = 'alert' or pelican_status = 'alert' or knx_status = 'alert' or measuring_instrument_status = 'alert' or daintree_status = 'alert' or modbus_status = 'alert') ", nativeQuery = true)
    List<String> listDevicesByAlertStatus(String dockername);

    @Transactional
    @Modifying
    @Query(value = "UPDATE device SET daintree_status = ?2 WHERE id = ?1", nativeQuery = true)
    void updateDeviceDaintreeStatus(String device_id, String daintree_status);

    @Transactional
    @Modifying
    @Query(value = "UPDATE device SET model = NULL, user_data_model = NULL, user_data_vendor = NULL, product_id = NULL WHERE docker_name = ?1", nativeQuery = true)
    void modelResetbyDockerName(String docker_name);

    @Query(nativeQuery = true)
    DeviceAlertDTO getDeviceAlertInfoById(String device_id);

    @Transactional
    @Modifying
    @Query(value = "UPDATE device SET qrcode_count = ?1 WHERE id = ?2", nativeQuery = true)
    void updateDeviceQrcodeCount(Integer qrcode_count, String device_id);

    @Query(nativeQuery = true)
    RoomStatusDTO getRoomStatusByDeviceId(String deviceid);

    @Query(nativeQuery = true)
    List<DeviceMonitorSpaceDTO> getRoomStatusByLocationId(String locationid);


    @Modifying
    @Transactional
    @Query(value = "UPDATE device SET asset_image_url = ?2 where id = ?1", nativeQuery = true)
    void updateAssetImage(String id, String asset_image_url);

    @Query(value = " SELECT asset_image_url FROM device WHERE id = ?1", nativeQuery = true)
    String getAssetImageUrls(String device_id);

    @Query(nativeQuery = true)
    Set<DeviceDTO> getAllChecklistDevicesPagination(String searchkey, Integer pagesize, Integer offset, JSONArray dockernames, JSONArray types, JSONArray global_checklist_ids, JSONArray virtual_device_types, Boolean isTaggedToQrCode, JSONArray deviceIdsTaggedToQrCode, Boolean isTaggedToNfc, JSONArray deviceIdsTaggedToNfc, Boolean isTaggedToBarCode, JSONArray deviceIdsTaggedToClientBarCode);

    @Query(nativeQuery = true)
    Set<DeviceDTO> getAllInspectionDevicesPagination(String searchkey, Integer pagesize, Integer offset, JSONArray dockernames, JSONArray types, JSONArray global_checklist_ids, String global_inspection_record_id, JSONArray virtual_device_types, Boolean isTaggedToQrCode, JSONArray deviceIdsTaggedToQrCode, Boolean isTaggedToNfc, JSONArray deviceIdsTaggedToNfc, Boolean isTaggedToBarCode, JSONArray deviceIdsTaggedToClientBarCode);

    @Query(nativeQuery = true)
    Set<DeviceDTO> getAllQrcodeDevicesPagination(String searchkey, Integer pagesize, Integer offset, JSONArray dockernames, JSONArray types, JSONArray virtual_device_types, Boolean isTaggedToQrCode, JSONArray deviceIdsTaggedToQrCode, Boolean isTaggedToNfc, JSONArray deviceIdsTaggedToNfc, Boolean isTaggedToBarCode, JSONArray deviceIdsTaggedToClientBarCode);

    @Query(nativeQuery = true)
    Set<DeviceDTO> getAllNetworkParentDeviceByPagination(JSONArray dockernames, JSONArray types, String searchkey, Integer pagesize, Integer offset, JSONArray virtual_device_types, Boolean isTaggedToQrCode, JSONArray deviceIdsTaggedToQrCode, Boolean isTaggedToNfc, JSONArray deviceIdsTaggedToNfc, Boolean isTaggedToBarCode, JSONArray deviceIdsTaggedToClientBarCode);


    @Query(nativeQuery = true)
    DeviceAlertDTO getDeviceConditionAlertInfoById(String device_id);

    @Query(nativeQuery = true)
    Set<DeviceDTO> listAllDeviceByVdmsId(String vdms_id);

    // update ecobee count
    @Modifying
    @Transactional
    @Query(value = "UPDATE device SET ecobee_count = ?2 WHERE id = ?1", nativeQuery = true)
    void updateDeviceEcobeeCount(String device_id, Integer ecobee_count);

    // update  ecobee status
    @Modifying
    @Transactional
    @Query(value = "UPDATE device SET ecobee_status = ?2 WHERE id = ?1", nativeQuery = true)
    void updateDeviceEcobeeStatus(String device_id, String ecobee_status);

    @Query(nativeQuery = true)
    Set<DeviceDTO> getFilterVirtualDevicesByPagination(String searchKey, Integer pageSize, Integer offset, Set<String> dockernames, Set<String> types, Set<String> virtual_device_types);

    @Query(nativeQuery = true)
    List<DeviceDTO> getDeviceDetailsByDeviceIdList(Set<String> device_ids);

    @Transactional
    @Modifying
    @Query(value = "UPDATE device SET modbus_count = ?2 WHERE id = ?1", nativeQuery = true)
    void updateDeviceModbusCount(String device_id, Integer modbus_count);

    @Transactional
    @Modifying
    @Query(value = "UPDATE device SET modbus_status = ?2 WHERE id = ?1", nativeQuery = true)
    void updateDeviceModbusStatus(String device_id, String modbus_status);

    @Query(nativeQuery = true)
    Set<DeviceDTO> getAssetsByLocationId(String location_id, Integer pagesize, Integer offset);

    @Transactional
    @Modifying
    @Query(value = "UPDATE device SET reboot_status = ?2 WHERE id = ?1", nativeQuery = true)
    void updateDeviceRebootStatus(String deviceId, String status);

    @Query(value = "SELECT reboot_status from device WHERE id = ?1", nativeQuery = true)
    String getDeviceRebootStatus(String vdmsid);


    @Query(nativeQuery = true)
    Set<DeviceDTO> getAllChecklistDevices(String searchkey, JSONArray dockernames, JSONArray types, String global_checklist_id, JSONArray virtual_device_types, Boolean isTaggedToQrCode, JSONArray deviceIdsTaggedToQrCode, Boolean isTaggedToNfc, JSONArray deviceIdsTaggedToNfc);

    @Query(nativeQuery = true)
    Set<DeviceDTO> getAllInspectionDevices(String searchkey, JSONArray dockernames, JSONArray types, String global_checklist_id, String global_inspection_record_id, JSONArray virtual_device_types, Boolean isTaggedToQrCode, JSONArray deviceIdsTaggedToQrCode, Boolean isTaggedToNfc, JSONArray deviceIdsTaggedToNfc);

    @Query(nativeQuery = true)
    Set<DeviceDTO> getAllQrcodeDevices(String searchkey, JSONArray dockernames, JSONArray types, JSONArray virtual_device_types, Boolean isTaggedToQrCode, JSONArray deviceIdsTaggedToQrCode, Boolean isTaggedToNfc, JSONArray deviceIdsTaggedToNfc);

    @Query(nativeQuery = true)
    Set<DeviceDTO> getAllNetworkParentDevices(JSONArray dockernames, JSONArray types, String searchkey, JSONArray virtual_device_types, Boolean isTaggedToQrCode, JSONArray deviceIdsTaggedToQrCode, Boolean isTaggedToNfc, JSONArray deviceIdsTaggedToNfc);

    @Query(value = "SELECT d.id FROM device d LEFT JOIN location l ON l.id = d.location_id" +
            " LEFT JOIN floor f ON l.floor_id = f.id" +
            " LEFT JOIN building b ON f.building_id = b.id" +
            " WHERE d.asset_match_status != 3 AND ('all' IN ?1 or d.docker_name IN ?1) AND" +
            " ('all' IN ?2 or d.type IN ?2)" +
            " AND ('all' IN ?4 or (IF ('other' IN ?4, (d.virtual_device_type = 2), NULL)) or (IF ('power_source' IN ?4, (d.virtual_device_type = 3 or d.virtual_device_type = 4), NULL)) or (IF ('ip' IN ?4, (d.virtual_device_type IS NULL or d.virtual_device_type = 0 or d.virtual_device_type = 1), NULL)))" +
            " AND (?3 IS NULL or CONCAT_WS('', d.display_name, d.user_data_name, d.ip_address, d.mac_address, l.name, d.docker_name, d.vendor, d.user_data_vendor,d.latitude, d.longitude, d.warranty, b.name, f.name, d.model, d.user_data_model, d.serial_number, d.custom_fields,d.type) LIKE CONCAT('%',?3,'%')) " +
            " AND (?5 IS NULL OR IF(?5, d.id IN ?6 , d.id NOT IN ?6))" +
            " AND (?7 IS NULL OR IF(?8, d.id IN ?8 , d.id NOT IN ?8))" +
            " AND ('all' IN ?9 OR l.id IN ?9)", nativeQuery = true)
    List<String> getDeviceIds(List<String> dockerNames, List<String> types, String searchKey, List<String> virtual_device_types,
                              Boolean isTaggedToQrCode, List<String> deviceIdsTaggedToQrCode, Boolean isTaggedToNfc,
                              List<String> deviceIdsTaggedToNfc, List<String> locationIds);


    @Query(nativeQuery = true)
    List<DeviceDTO> getDevicesByFilter(List<String> dockerNames, List<String> types, String searchKey, List<String> virtual_device_types, Boolean isTaggedToQrCode, List<String> deviceIdsTaggedToQrCode, Boolean isTaggedToNfc, List<String> deviceIdsTaggedToNfc, List<String> locationIds, List<String> deviceIds);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO device(id, location_id, model, vendor, description, docker_name, docker_vdms_id, asset_match_status, " +
            "created_timestamp, created_email, asset_group, virtual_device_type, monitor, user_data_name)" +
            "VALUES (?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8, ?9, ?10, ?11, ?12, ?13, ?14)", nativeQuery = true)
    void addAssetOnboardedDevices(String id, String location_id, String model, String vendor, String description, String docker_name, String vdmsid, Integer asset_match_status,
                                  BigInteger created_timestamp, String created_email, String asset_group, Integer virtual_device_type, Integer monitor, String user_data_name);

    @Modifying
    @Transactional
    @Query(value = "UPDATE device SET onboard_status = ?2, updated_timestamp = ?3 WHERE id IN ?1", nativeQuery = true)
    void updateOnboardAssetStatus(Set<String> device_ids, Integer onboard_status, BigInteger updated_timestamp);

    @Modifying
    @Transactional
    @Query(value = "UPDATE device SET asset_ocr_image_url = ?2 where id = ?1", nativeQuery = true)
    void updateAssetOcrImage(String device_id, String asset_ocr_image_url);

    @Query(value = " SELECT asset_ocr_image_url FROM device WHERE id = ?1", nativeQuery = true)
    String getAssetOcrImageUrls(String device_id);

    @Transactional
    @Query(value = "SELECT COUNT(id) FROM device WHERE (?1 = 'all' or docker_name = ?1) AND monitor = 1 AND asset_match_status != 3 AND onboard_status = ?2", nativeQuery = true)
    Integer getAssetOnboardCount(String dockername, int onboard_status);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO device(id, docker_vdms_id, docker_name, ip_address, status, mac_address, last_seen_on, display_name, vendor, created_timestamp, user_data_name, type, description, "
            + " custom_fields, created_email,asset_group,onboard_status, monitor, virtual_device_type) VALUES(?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8, ?9,?10,?11,?12,?13,?14,?15,?16,?17,?18, ?19)", nativeQuery = true)
    void addDevice(String id, String vdms_id, String docker_name, String ip_address, Integer status,
                   String mac_address, String last_seen_on, String display_name, String vendor, BigInteger created_timestamp,
                   String user_data_name, String type, String description, String customFields, String created_email, String asset_group, Integer onboard_status, Integer monitor, Integer virtual_device_type);

    @Query(nativeQuery = true)
    Set<DeviceDTO> getDevicesByLocationId(String location_id);

    @Query(nativeQuery = true)
    DeviceDTO getDeviceByDeviceIdNew(String device_id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE device SET latitude = ?2, longitude = ?3, position = ?4, location_id = ?5 WHERE id = ?1", nativeQuery = true)
    void updateDeviceCoordinatesAndLocationId(String device_id, String latitude, String longitude, String position, String location_id);

    @Query(nativeQuery = true)
    List<DeviceDTO> getAllDeviceByVdmsIdAndDockerNameWithoutPagination(String vdmsid, String dockername);

    @Modifying
    @Transactional
    @Query(value = "UPDATE device SET digital_twin_image_url = ?1 WHERE id = ?2", nativeQuery = true)
    void updateDigitalTwinImageUrlById(String digital_twin_image_url, String device_id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE device SET digital_twin_image_url = NULL WHERE id = ?1", nativeQuery = true)
    void deleteDigitalTwinImageUrl(String device_id);

    @Query(value = " SELECT digital_twin_image_url FROM device WHERE id = ?1", nativeQuery = true)
    String getDigitalTwinImageUrl(String device_id);


    @Modifying
    @Transactional
    @Query(value = "UPDATE device SET poly_lens_count = ?2 WHERE id = ?1", nativeQuery = true)
    void updatePolyLensDeviceCount(String deviceId, Integer polyLensDeviceCount);


    @Modifying
    @Transactional
    @Query(value = "UPDATE device SET mqtt_device_count = ?2 WHERE id = ?1", nativeQuery = true)
    void updateMqttDeviceDeviceCount(String deviceId, Integer mqttDeviceCount);


    @Query(nativeQuery = true)
    Set<DeviceDTO> getAllRecordChecklistDevicesPagination(String searchkey, Integer pagesize, Integer offset, JSONArray dockernames, JSONArray types, JSONArray global_checklist_ids, String inspection_record_id, JSONArray virtual_device_types, Boolean isTaggedToQrCode, JSONArray deviceIdsTaggedToQrCode, Boolean isTaggedToNfc, JSONArray deviceIdsTaggedToNfc, Boolean isTaggedToBarCode, JSONArray deviceIdsTaggedToClientBarCode);

    @Query(nativeQuery = true)
    Set<DeviceDTO> getDeviceDetailsByIdList(Set<String> device_ids);

    @Query(value = "SELECT COUNT(*) FROM device", nativeQuery = true)
    Integer getAllDeviceCount();


    @Query(value = "SELECT id FROM device WHERE digital_twin_image_url IS NOT NULL", nativeQuery = true)
    List<String> getDeviceIdsWithDigitalTwinImageUrl();


    @Transactional
    @Query(value = "SELECT DISTINCT(d.type) FROM device d ", nativeQuery = true)
    List<String> getAllUniqueDeviceTypes();

    @Modifying
    @Transactional
    @Query(value = "UPDATE device d SET d.type = ?1 WHERE d.type LIKE CONCAT(?2, '%') ", nativeQuery = true)
    void updateTypeByType(String type, String idPrefix);

    @Modifying
    @Transactional
    @Query(value = "UPDATE device d SET d.type ='generic' WHERE d.type IS NULL ", nativeQuery = true)
    Integer setTypeGeneric();

    @Modifying
    @Transactional
    @Query(value = "UPDATE device d SET d.type = ?2 WHERE d.type = ?1 ", nativeQuery = true)
    void updateDeviceType(String type, String updateType);

    @Query(nativeQuery = true)
    DeviceDTO getDeviceByMeasuringInstrumentId(String measuringInstrumentId);


    @Query(nativeQuery = true)
    List<DeviceDTO> browseAiCallFlowDevicesWithSearch(String vdmsid, String dockername, Integer offset, Integer pagesize, String sanitizedSearchKey);


    @Modifying
    @Transactional
    @Query(value = "UPDATE device SET is_dnd_enabled = ?2 WHERE id = ?1", nativeQuery = true)
    void toggleDndStatus(String device_id, Boolean is_dnd_enabled);

    @Query(value = "SELECT  IF(d.user_data_name IS NULL OR d.user_data_name = '', d.display_name, d.user_data_name) as name FROM device d WHERE d.id = ?1", nativeQuery = true)
    String getDeviceNameById(String deviceId);

    @Query(nativeQuery = true)
    DeviceDTO getDeviceById(String deviceId);

    // HAM Asset import //
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO device(id,ip_address,mac_address,user_data_name,user_data_model,user_data_vendor,type,"
            + "location_id,network_layer,parent,snmp_parent,monitor,docker_name,docker_vdms_id,last_seen_on,warranty,status,"
            + "email_alert, sms_alert, popup_notification, virtual_device_type, serial_number, local_vendor_email_alert,local_vendor_sms_alert, "
            + "subsystem_parent_id, description, asset_match_status, created_timestamp, created_email,asset_group, category, sub_category, cost_value, assigned_user_email, ai_call, cost_unit, is_dnd_enabled, custom_fields)"
            + "VALUE(?1,?2,?3,?4,?5,?6,?7,?8,?9,?10,?11,?12,?13,?14,?15,?16,?17,?18,?19,?20,?21,?22, ?23, ?24, ?25, ?26, ?27, ?28,?29,?30, ?31, ?32, ?33, ?34, ?35, ?36, ?37, ?38)", nativeQuery = true)
    void moveVirtualDevice(String final_device_id, String ip_address, String mac_address, String user_data_name,
                           String user_data_model, String user_data_vendor, String type, String location_id, String network_layer,
                           String parent, String snmp_parent, Integer monitor, String docker_name, String vdms_id, String last_seen_on,
                           String warranty, Integer status, Integer email_alert, Integer sms_alert, Integer popup_notification,
                           Integer virtual_device_type, String serial_number, Integer local_vendor_email_alert, Integer local_vendor_sms_alert,
                           String subsystem_parent_id, String description, Integer asset_match_status, BigInteger created_timestamp, String created_email, String asset_group, String category, String sub_category,
                           BigDecimal cost_value, String assigned_user_email, Boolean ai_call, String cost_unit, Boolean is_dnd_enabled, String custom_fields);


    @Transactional
    @Modifying
    @Query(value = "UPDATE device SET operational_status = ?2 WHERE id = ?1", nativeQuery = true)
    void updateDeviceOperationalStatus(String device_id, String operationalStatus);

    @Transactional
    @Modifying
    @Query(value = "UPDATE device SET assigned_user_email = ?2 WHERE id = ?1", nativeQuery = true)
    void updateAssignedUserEmail(String deviceId, String assignedUserEmail);


    @Query(nativeQuery = true)
    DeviceDTO getDeviceInfoFromDb(String deviceId);


    @Modifying
    @Transactional
    @Query(value = "INSERT INTO device(id, user_data_name, monitor, docker_name, docker_vdms_id, warranty, virtual_device_type, serial_number, " +
            " asset_match_status,created_timestamp, created_email,asset_group,assigned_user_email,mac_address, ip_address, status,last_seen_on, user_data_model, subsystem_parent_id) VALUES(?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8, ?9,?10,?11,?12,?13,?14,?15, ?16,?17, ?18, ?19)", nativeQuery = true)
    void addVirtualDeviceFromInventory(String finalDeviceId, String userDataName, Integer monitor, String dockerName,
                                       String vdmsId, String warranty, Integer virtualDeviceType, String serialNumber,
                                       Integer assetMatchStatus, BigInteger createdTimestamp, String email, String assetGroup,
                                       String assignedUserEmail, String macAddress, String ipAddress, Integer status, String lastSeenOn, String userDataModel, String subsystemParentId);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO inventory_device(device_id, tracking_id) VALUES(?1, ?2)", nativeQuery = true)
    void tagInventoryDevices(String deviceId, String inventoryTrackingId);

    @Query(value = "SELECT * FROM device WHERE serial_number = ?1 LIMIT 1", nativeQuery = true)
    Device findDeviceIdBySerialNumber(String serialNumber);

    @Query(value = "SELECT DISTINCT docker_name FROM device d WHERE d.docker_vdms_id = ?1 AND d.ai_call = true AND d.docker_name IS NOT NULL AND (?2 = 'null'  or LOWER(REGEXP_REPLACE(CONCAT_WS('', d.docker_name), '[ -.!\t_+#~`@$%^&*()=;:<>?,/{}|\\\\ ]' , '')) LIKE CONCAT('%',?2,'%'))", nativeQuery = true)
    Set<String> listAiEnabledDockers(String vdmsid, String sanitizedSearchKey);


    @Query(value = "SELECT ai_call FROM device WHERE id = ?1", nativeQuery = true)
    Boolean checkAiCallEnableStatus(String id);

    @Query(value = "SELECT is_dnd_enabled FROM device WHERE id = ?1", nativeQuery = true)
    Boolean checkAiCallDndEnabled(String id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE device d SET d.is_dnd_enabled=1 WHERE d.id=?1", nativeQuery = true)
    void updateDeviceDndEnabledStatus(String id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE device d SET d.dnd_timestamp=?2 WHERE d.id=?1", nativeQuery = true)
    void updateDeviceDndTimestamp(String id,BigInteger dndTimestamp);

    @Query(nativeQuery = true)
    List<DeviceDTO> getAiCallAndDeviceStatus(String deviceId);

    @Query(nativeQuery = true)
    List<DeviceDTO> getDndDevices(boolean b);

    @Modifying
    @Transactional
    @Query(value = "UPDATE device SET is_dnd_enabled = ?2 WHERE id = ?1", nativeQuery = true)
    void updateDndStatus(String id, boolean b);

    @Modifying
    @Transactional
    @Query(value = "UPDATE device SET system_dnd_enabled = 1 WHERE id = ?1", nativeQuery = true)
    void updateSystemDndEnabled(String id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE device SET system_dnd_enabled = ?2 WHERE id = ?1", nativeQuery = true)
    void updateSystemDndDisabled(String id, boolean b);

    @Query(nativeQuery = true)
    DeviceDTO getDeviceDndAndSystemDndStatus(String deviceId, Boolean isDndEnabled);

    @Query(value = "SELECT IF(d.user_data_model IS NULL OR d.user_data_model = '', d.model, d.user_data_model) as model FROM device d WHERE d.id = ?1", nativeQuery = true)
    String getModelById(String deviceId);

    List<Device> findByStatus(int status);

    @Query(value = "SELECT * FROM device WHERE user_data_name = ?1 AND subsystem_parent_id = ?2", nativeQuery = true)
    Optional<Device> findByDisplayNameAndSubsystemParentId(String displayName, String parentId);


    @Query(nativeQuery = true)
    List<DeviceDTO> getAllDeviceCustomDetailsPaginated(String searchKey, Integer pageSize, Integer offset, List<String> excludeDeviceIds, String dockerName);

    @Query(nativeQuery = true)
    List<DeviceDTO> getAllDeviceCustomDetails(String searchKey, List<String> excludeDeviceIds, String dockerName);

    @Query(nativeQuery = true)
    List<DeviceDTO> getDeviceCustomDetailsPaginated(String searchKey, Integer pageSize, Integer offset, List<String> deviceIds);

    @Query(nativeQuery = true)
    List<DeviceDTO> getDeviceCustomDetails(String searchKey, List<String> deviceIds);

    @Query(nativeQuery = true)
    List<DeviceDTO> getDeviceCustomDetailsByIds(String searchKey, List<String> deviceIds, List<String> models, List<String> assetCategory);

    @Query(nativeQuery = true)
    List<DeviceDTO> getDeviceCustomDetailsByIdsPaginated(String searchKey, Integer pageSize, Integer offset, List<String> deviceIds, List<String> models, List<String> assetCategory);


    @Query(value = "SELECT d.id " +
            "FROM device d " +
            "WHERE ( ?2 = 'all' OR d.docker_name = ?2 ) " +
            "AND (d.virtual_device_type IS NULL OR d.virtual_device_type = 0) " +
            "AND CONCAT_WS('', d.display_name, d.user_data_name) LIKE CONCAT('%', ?1, '%') " +
            "ORDER BY d.id",
            nativeQuery = true)
    List<String> getAllDeviceIdsSearchKeySelectAll(String searchKey, String dockerName);


    @Query(value = "SELECT d.id " +
            "FROM device d " +
            "WHERE ( ?1 = 'all' OR d.docker_name = ?1 ) " +
            "AND (d.virtual_device_type IS NULL OR d.virtual_device_type = 0) ",
            nativeQuery = true)
    List<String> getAllDeviceIdsSelectAll(String dockerName);


    @Query(value = "SELECT d.id " +
            "FROM device d " +
            "WHERE ( ?4 = 'all' OR d.docker_name = ?4 ) " +
            "AND (d.virtual_device_type IS NULL OR d.virtual_device_type = 0) " +
            "AND CONCAT_WS('', d.display_name, d.user_data_name) LIKE CONCAT('%', ?1, '%') " +
            "ORDER BY d.id " +
            "LIMIT ?2 OFFSET ?3",
            nativeQuery = true)
    List<String> getAllDeviceIdsSearchKeyPaginated(String searchKey, Integer pageSize, Integer offset, String dockerName);


    @Query(value = "SELECT d.id " +
            "FROM device d " +
            "WHERE ( ?3 = 'all' OR d.docker_name = ?3 ) " +
            "AND (d.virtual_device_type IS NULL OR d.virtual_device_type = 0) " +
            "ORDER BY d.id " +
            "LIMIT ?1 OFFSET ?2",
            nativeQuery = true)
    List<String> getAllDeviceIdsPaginated(Integer pageSize, Integer offset, String dockerName);

    @Query(nativeQuery = true)
    Set<DeviceDTO> getAllBarCodeDevicesPagination(String searchkey, Integer pagesize, Integer offset, JSONArray dockernames, JSONArray types, JSONArray virtualDeviceTypes, Boolean isTaggedToQrCode, JSONArray deviceIdsTaggedToQrCode, Boolean isTaggedToNfc, JSONArray deviceIdsTaggedToNfc, Boolean isTaggedToBarCode, JSONArray deviceIdsTaggedToClientBarCode);

    @Query(nativeQuery = true)
    DeviceDTO getAllDeviceImages(String deviceId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE device SET asset_tag_images_url = ?2 where id = ?1", nativeQuery = true)
    void updateAssetTagImages(String id, String jsonString);
}


