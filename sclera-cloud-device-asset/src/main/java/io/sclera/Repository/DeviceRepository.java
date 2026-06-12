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

import jakarta.transaction.Transactional;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Manages persistence and querying of {@link Device} entities across networks, dockers, and VDMS instances.
 */
@Repository
public interface DeviceRepository extends JpaRepository<Device, String> {

    /**
     * Returns all devices belonging to the given VDMS and docker.
     *
     * @param vdmsid the VDMS identifier
     * @param dockername the docker name
     * @return the matching device projections
     */
    // NOT CONVERTED — stays native (PG-translation track): multi-join DeviceDTO projection (named-query delegation to Device.java @NamedNativeQuery)
    @Query(nativeQuery = true)
    Set<DeviceDTO> listAllDevicebyVdmsidAndDockerName(String vdmsid, String dockername);


    /**
     * Returns a filtered, paginated set of devices for the given VDMS and docker.
     *
     * @param vdmsid the VDMS identifier
     * @param dockername the docker name
     * @param searchKey the search filter term
     * @param virtual_device_type the virtual device type filter
     * @param status the status filter
     * @param monitor the monitor flag filter
     * @param asset_match_status the asset match status filter
     * @param pageSize the maximum number of devices to return
     * @param offset the number of devices to skip
     * @param assigned_status the assigned status filter
     * @return the matching device projections
     */
    // NOT CONVERTED — stays native (PG-translation track): multi-join DeviceDTO projection (named-query delegation to Device.java @NamedNativeQuery)
    @Query(nativeQuery = true)
    Set<DeviceDTO> getfilterdevices(String vdmsid, String dockername, String searchKey, Integer virtual_device_type,
                                    Integer status, Integer monitor, Integer asset_match_status, Integer pageSize, Integer offset, Integer assigned_status);


    /**
     * Returns the device with the given identifier.
     *
     * @param device_id the device identifier
     * @return the matching device projection
     */
    // NOT CONVERTED — stays native (PG-translation track): multi-join DeviceDTO projection (named-query delegation to Device.java @NamedNativeQuery)
    @Query(nativeQuery = true)
    DeviceDTO getDeviceByDeviceId(String device_id);

    /**
     * Counts devices matching the given MAC address within the given VDMS and docker.
     *
     * @param mac_address the MAC address
     * @param vdms_id the VDMS identifier
     * @param docker_name the docker name
     * @return the number of matching devices
     */
    // NOT CONVERTED — stays native (PG-translation track): docker_name/docker_vdms_id are FK columns of the @ManyToOne Docker relation, not scalar @Column fields
    @Query(value = "SELECT COUNT(id) FROM device WHERE mac_address =?1  AND docker_vdms_id =?2  AND  docker_name =?3", nativeQuery = true)
    int checkDeviceByDeviceId(String mac_address, String vdms_id, String docker_name);

    /**
     * Inserts a new device record.
     *
     * @param id the device identifier
     * @param vdms_id the owning VDMS identifier
     * @param docker_name the docker name
     * @param ip_address the device IP address
     * @param status the device status
     * @param mac_address the device MAC address
     * @param last_seen_on the last-seen timestamp
     * @param display_name the device display name
     * @param vendor the device vendor
     * @param created_timestamp the creation timestamp
     * @param user_data_name the user-supplied device name
     * @param type the device type
     * @param description the device description
     * @param customFields the device custom fields
     * @param created_email the creating user's email
     * @param asset_group the asset group
     */
    @Modifying
    @Transactional
    // NOT CONVERTED — stays native (PG-translation track): plain INSERT
    @Query(value = "INSERT INTO device(id, docker_vdms_id, docker_name, ip_address, status, mac_address, last_seen_on, display_name, vendor, created_timestamp, user_data_name, type, description, custom_fields, created_email,asset_group) VALUES(?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8, ?9,?10,?11,?12,?13,?14,?15,?16)", nativeQuery = true)
    void insertDevice(String id, String vdms_id, String docker_name, String ip_address, Integer status,
                      String mac_address, BigInteger last_seen_on, String display_name, String vendor, BigInteger created_timestamp,
                      String user_data_name, String type, String description, String customFields, String created_email, String asset_group);


    /**
     * Updates the status, network, and vendor fields of a device identified by VDMS, docker, and MAC address.
     *
     * @param ip_address the device IP address
     * @param status the device status
     * @param last_seen_on the last-seen timestamp
     * @param display_name the device display name
     * @param vendor the device vendor
     * @param snmp_parent the SNMP parent reference
     * @param vdms_id the VDMS identifier
     * @param docker_name the docker name
     * @param mac_address the device MAC address
     */
    @Modifying
    @Transactional
    // NOT CONVERTED — stays native (PG-translation track): WHERE clause uses docker_vdms_id/docker_name (FK columns of @ManyToOne Docker)
    @Query(value = "UPDATE device SET ip_address =?1 , status =?2  , last_seen_on = ?3,  display_name = ?4, vendor = ?5, snmp_parent = ?6  WHERE docker_vdms_id = ?7 AND docker_name =?8 AND  mac_address = ?9 ", nativeQuery = true)
    void updateDevice(String ip_address, Integer status, BigInteger last_seen_on, String display_name, String vendor,
                      String snmp_parent, String vdms_id, String docker_name, String mac_address);

    /**
     * Edits the full set of editable fields of a device identified by VDMS, docker, and device id,
     * shallow-merging the supplied ADC JSON into the existing value.
     *
     * @return the number of rows affected
     */
    @Modifying
    @Transactional
    // NOT CONVERTED — stays native (PG-translation track): sets adc_json (jsonb/JSON column) and location_id (@ManyToOne FK)
    // PG-port: IFNULL->COALESCE
    // PG-port: JSON_MERGE_PATCH -> jsonb || (shallow merge; flat-object patch assumed — top-level scalar fields only;
    //          if nested-object patches or null-to-remove semantics are ever needed, revisit with a jsonb_merge_patch() plpgsql function)
    @Query(value = "UPDATE device SET monitor = ?4,  network_layer = ?5, user_data_model = ?6,model = ?6, user_data_name = ?7, type = ?8, user_data_vendor = ?9, vendor = ?9, parent = ?10, remote_access = ?11, warranty = ?12, product_id = ?13,"
            + " location_id = ?14, email_alert = ?15, sms_alert = ?16, popup_notification = ?17, serial_number = ?18, local_vendor_email_alert = ?19,"
            + " local_vendor_sms_alert = ?20, subsystem_parent_id = ?21, custom_fields = COALESCE(?22, custom_fields), description = ?23, asset_match_status = ?24, asset_group = ?25, category = ?26, sub_category = ?27, location_status = ?28, "
            + " cost_value = ?29, assigned_user_email = ?30, ai_call = ?31, cost_unit = ?32, is_dnd_enabled = ?33, operational_status = ?34, adc_json = (COALESCE(adc_json::text, '{}')::jsonb || CAST(?35 AS jsonb))::jsonb "
            + " WHERE docker_vdms_id = ?2 AND docker_name = ?3 AND id = ?1", nativeQuery = true)
    int editDeviceByDeviceID(String device_id, String vdmsid, String dockername, Integer monitor, String network_layer, String user_data_model,
                             String user_data_name, String type, String user_data_vendor, String parent, Integer remote_access,
                             String warranty, String product_id, String location_id, Integer email_alert, Integer sms_alert,
                             Integer popup_notification, String serial_number, Integer local_vendor_email_alert, Integer local_vendor_sms_alert,
                             String subsystem_parent_id, String custom_fields, String description, Integer asset_match_status, String asset_group, String category, String sub_category, String location_status,
                             BigDecimal cost_value, String assigned_user_email, Boolean ai_call, String cost_unit, Boolean is_dnd_enabled, String operational_status, String adc_json);

    /**
     * Returns the product id linked to the given device.
     *
     * @param device_id the device identifier
     * @return the product id, or {@code null} if none is set
     */
    // NOT CONVERTED — stays native (PG-translation track): product_id column has no @Column field on Device entity (dropped in DB-per-service refactoring)
    @Query(value = "SELECT product_id FROM device WHERE id = ?1", nativeQuery = true)
    String getProductIdByDeviceId(String device_id);

    /**
     * Updates the vendor associations of a device, keeping existing values where the supplied ones are null.
     *
     * @param global_vendor_id the global vendor id, or {@code null} to keep the current value
     * @param local_vendor_id the local vendor id, or {@code null} to keep the current value
     * @param other_vendor_1_id the first other-vendor id, or {@code null} to keep the current value
     * @param other_vendor_2_id the second other-vendor id, or {@code null} to keep the current value
     * @param other_vendor_3_id the third other-vendor id, or {@code null} to keep the current value
     * @param device_id the device identifier
     * @return the number of rows affected
     */
    @Modifying
    @Transactional
    // NOT CONVERTED — stays native (PG-translation track): sets global_vendor_id/local_vendor_id/other_vendor_N_id which are @JoinColumn FK columns of @ManyToOne Phonebook relations
    // PG-port: IFNULL->COALESCE
    @Query(value = "UPDATE device SET global_vendor_id = COALESCE(?1 ,global_vendor_id) ,local_vendor_id = COALESCE(?2 ,local_vendor_id) ,"
            + "other_vendor_1_id = COALESCE(?3 ,other_vendor_1_id) , other_vendor_2_id = COALESCE(?4 ,other_vendor_2_id) ,"
            + "other_vendor_3_id = COALESCE(?5,other_vendor_3_id) WHERE id = ?6 ", nativeQuery = true)
    Integer updateDeviceVendorsByDeviceID(String global_vendor_id, String local_vendor_id, String other_vendor_1_id,
                                          String other_vendor_2_id, String other_vendor_3_id, String device_id);

    /**
     * Links a vendor of the given type to a device by setting the matching vendor column.
     *
     * @param vendor_type the vendor type ({@code global}, {@code local}, {@code other_1}, {@code other_2}, or {@code other_3})
     * @param id the vendor id to assign
     * @param device_id the device identifier
     */
    @Modifying
    @Transactional
    // NOT CONVERTED — stays native (PG-translation track): sets global_vendor_id/local_vendor_id/other_vendor_N_id which are @JoinColumn FK columns of @ManyToOne Phonebook relations
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

    /**
     * Unlinks a vendor from a device by clearing the matching vendor column for the given vendor type.
     *
     * @param phoneaccount the vendor id to unlink
     * @param vendor_type the vendor type ({@code global}, {@code local}, {@code other_1}, {@code other_2}, or {@code other_3})
     * @param device_id the device identifier
     */
    @Modifying
    @Transactional
    // NOT CONVERTED — stays native (PG-translation track): sets @ManyToOne Phonebook FK columns; also 'THEN global_vendor_id = NULL' is broken MySQL syntax that must be corrected to 'THEN NULL' in PG migration
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

    /**
     * Applies a partial quick update to a device, retaining existing values where the supplied ones are null.
     *
     * @param id the device identifier
     */
    @Modifying
    @Transactional
    // NOT CONVERTED — stays native (PG-translation track): sets location_id (@ManyToOne FK), global_vendor_id/local_vendor_id/other_vendor_N_id (@ManyToOne Phonebook FKs)
    // PG-port: IFNULL->COALESCE / IF->CASE WHEN
    @Query(value = "UPDATE device SET user_data_name = COALESCE(?2 ,user_data_name), user_data_model = COALESCE(?3 ,user_data_model) ,user_data_vendor = COALESCE(?4 ,user_data_vendor) ,"
            + "type = COALESCE(?5 ,type) ,warranty = COALESCE(?6 ,warranty) ,network_layer = COALESCE(?7 ,network_layer) ,"
            + "location_id = CASE WHEN ?8 = 'null' OR ?8 = '' THEN NULL ELSE COALESCE(?8, location_id) END, parent = COALESCE(?9 ,parent) ,product_id = COALESCE(?10 ,product_id) ,"
            + "monitor = COALESCE(?11 ,monitor) ,remote_access = COALESCE(?12 ,remote_access) ,global_vendor_id = COALESCE(?13 ,global_vendor_id) ,"
            + "local_vendor_id = COALESCE(?14 ,local_vendor_id) ,other_vendor_1_id = COALESCE(?15 ,other_vendor_1_id) ,"
            + "other_vendor_2_id = COALESCE(?16 ,other_vendor_2_id) ,other_vendor_3_id = COALESCE(?17 ,other_vendor_3_id), "
            + "email_alert = COALESCE(?18 ,email_alert), sms_alert = COALESCE(?19 ,sms_alert), popup_notification = COALESCE(?20 ,popup_notification),"
            + "local_vendor_email_alert = COALESCE(?21 ,local_vendor_email_alert), local_vendor_sms_alert = COALESCE(?22 ,local_vendor_sms_alert), "
            + "subsystem_parent_id = COALESCE(?23 ,subsystem_parent_id), description = COALESCE(?24, description), asset_match_status = COALESCE(?25, asset_match_status), custom_fields = COALESCE(?26, custom_fields), docker_name = ?27, asset_group = COALESCE(?28, asset_group), category = COALESCE(?29, category), sub_category = COALESCE(?30, sub_category), location_status = CASE WHEN ?8 IS NULL THEN location_status ELSE NULL END "
            + "WHERE id = ?1", nativeQuery = true)
    void quickUpdate(String id, String user_data_name, String user_data_model, String user_data_vendor, String type,
                     String warranty, String network_layer, String location_id, String parent_device_id, String product_id,
                     Integer monitor, Integer remote_access, String global_vendor_id, String local_vendor_id,
                     String other_vendor_1_id, String other_vendor_2_id, String other_vendor_3_id, Integer email_alert,
                     Integer sms_alert, Integer popup_notification, Integer local_vendor_email_alert, Integer local_vendor_sms_alert,
                     String subsystem_parent_id, String description, Integer asset_match_status, String custom_fields, String docker_name, String asset_group, String category, String sub_category);

    /**
     * Returns the device names for the given VDMS and docker.
     *
     * @param vdmsid the VDMS identifier
     * @param dockername the docker name
     * @return the matching device projections
     */
    // NOT CONVERTED — stays native (PG-translation track): multi-join DeviceDTO projection (named-query delegation to Device.java @NamedNativeQuery)
    @Query(nativeQuery = true)
    Set<DeviceDTO> getDeviceNamesByVdmsIdAndDockerName(String vdmsid, String dockername);

//	@Modifying
//	@Transactional
//	@Query(value = "INSERT INTO device(id,ip_address,mac_address,user_data_name,user_data_model,user_data_vendor,type,"
//			+ "location_id,network_layer,parent,snmp_parent,monitor,docker_name,docker_vdms_id,last_seen_on,warranty,status,"
//			+ "email_alert, sms_alert, popup_notification, virtual_device_type, serial_number)"
//			+ "VALUES(?1,?2,?3,?4,?5,?6,?7,?8,?9,?10,?11,?12,?13,?14,?15,?16,?17,?18,?19,?20,?21,?22)", nativeQuery = true)
//	void addVirtualDevice(String final_device_id, String ip_address, String mac_address, String user_data_name,
//			String user_data_model, String user_data_vendor, String type, String location_id, String network_layer,
//			String parent, String snmp_parent, Integer monitor, String docker_name, String vdms_id, String last_seen_on,
//			String warranty, Integer status, Integer email_alert, Integer sms_alert, Integer popup_notification,
//			Integer virtual_device_type, String serial_number);

    /**
     * Inserts a new virtual device with its full set of network, vendor, and lifecycle fields.
     *
     * @param final_device_id the device identifier
     */
    @Modifying
    @Transactional
    // NOT CONVERTED — stays native (PG-translation track): plain INSERT
    @Query(value = "INSERT INTO device(id,ip_address,mac_address,user_data_name,user_data_model,user_data_vendor,type,"
            + "location_id,network_layer,parent,snmp_parent,monitor,docker_name,docker_vdms_id,last_seen_on,warranty,status,"
            + "email_alert, sms_alert, popup_notification, virtual_device_type, serial_number, local_vendor_email_alert,local_vendor_sms_alert, "
            + "subsystem_parent_id, description, asset_match_status, created_timestamp, created_email,asset_group, category, sub_category, cost_value, assigned_user_email, ai_call, cost_unit, is_dnd_enabled, operational_status)"
            + "VALUES(?1,?2,?3,?4,?5,?6,?7,?8,?9,?10,?11,?12,?13,?14,?15,?16,?17,?18,?19,?20,?21,?22, ?23, ?24, ?25, ?26, ?27, ?28,?29,?30, ?31, ?32, ?33, ?34, ?35, ?36, ?37, ?38)", nativeQuery = true)
    void addVirtualDevice(String final_device_id, String ip_address, String mac_address, String user_data_name,
                          String user_data_model, String user_data_vendor, String type, String location_id, String network_layer,
                          String parent, String snmp_parent, Integer monitor, String docker_name, String vdms_id, String last_seen_on,
                          String warranty, Integer status, Integer email_alert, Integer sms_alert, Integer popup_notification,
                          Integer virtual_device_type, String serial_number, Integer local_vendor_email_alert, Integer local_vendor_sms_alert,
                          String subsystem_parent_id, String description, Integer asset_match_status, BigInteger created_timestamp, String created_email, String asset_group, String category, String sub_category,
                          BigDecimal cost_value, String assigned_user_email, Boolean ai_call, String cost_unit, Boolean is_dnd_enabled, String operational_status);

    /**
     * Edits the full set of editable fields of a virtual device, shallow-merging the supplied ADC JSON into the existing value.
     *
     * @param virtual_device_id the virtual device identifier
     */
    @Modifying
    @Transactional
    // NOT CONVERTED — stays native (PG-translation track): sets adc_json (jsonb/JSON column) and location_id (@ManyToOne FK)
    // PG-port: IFNULL->COALESCE
    // PG-port: JSON_MERGE_PATCH -> jsonb || (shallow merge; flat-object patch assumed — top-level scalar fields only;
    //          if nested-object patches or null-to-remove semantics are ever needed, revisit with a jsonb_merge_patch() plpgsql function)
    @Query(value = "UPDATE device SET monitor = ?2, location_id = ?3, network_layer = ?4, user_data_model = ?5, type = ?6, "
            + "user_data_vendor = ?7, user_data_name = ?8, parent = ?9, remote_access = ?10, product_id = ?11, warranty = ?12, "
            + "ip_address = ?13, email_alert = ?14, sms_alert = ?15, popup_notification = ?16, virtual_device_type = ?17, "
            + "serial_number = ?18, local_vendor_email_alert = ?19, local_vendor_sms_alert = ?20, docker_name = ?21, subsystem_parent_id = ?22, "
            + "custom_fields = COALESCE(?23, custom_fields), description = ?24, asset_match_status = ?25,asset_group = ?26, category = ?27, "
            + "sub_category = ?28, location_status = ?29, cost_value = ?30, assigned_user_email = ?31, ai_call = ?32, cost_unit = ?33, is_dnd_enabled = ?34, "
            + "operational_status = ?35, adc_json = (COALESCE(adc_json::text, '{}')::jsonb || CAST(?36 AS jsonb))::jsonb, model = ?5, vendor = ?7  "
            + "WHERE id = ?1", nativeQuery = true)
    void editVirtualDeviceByVirtualDeviceId(String virtual_device_id, Integer monitor, String location_id, String network_layer,
                                            String user_data_model, String type, String user_data_vendor, String user_data_name, String parent,
                                            Integer remote_access, String product_id, String warranty, String ip_address, Integer email_alert,
                                            Integer sms_alert, Integer popup_notification, Integer virtual_device_type, String serial_number,
                                            Integer local_vendor_email_alert, Integer local_vendor_sms_alert, String docker_name, String subsystem_parent_id, String custom_fields, String description,
                                            Integer asset_match_status, String asset_group, String category, String sub_category, String location_status, BigDecimal cost_value, String assigned_user_email,
                                            Boolean ai_call, String cost_unit, Boolean is_dnd_enabled, String operational_status, String adc_json);


    /**
     * Deletes the virtual device with the given identifier.
     *
     * @param virtual_device_id the virtual device identifier
     */
    // delete is done by cascade delete, if this query not required can be deleted
    @Modifying
    @Transactional
    // NOT CONVERTED — stays native (PG-translation track): is_virtual column has no @Column field on Device entity
    @Query(value = "DELETE FROM device WHERE id = ?1 AND is_virtual = true", nativeQuery = true)
    void deleteVirtualDeviceByeviceId(String virtual_device_id);

    /**
     * Applies a multi-device bulk update to the device with the given id.
     *
     * @param id the device identifier
     */
    @Modifying
    @Transactional
    // NOT CONVERTED — stays native (PG-translation track): sets location_id (@ManyToOne FK), global_vendor_id/local_vendor_id/other_vendor_N_id (@ManyToOne Phonebook FKs)
    // PG-port: IFNULL->COALESCE / IF->CASE WHEN
    @Query(value = "UPDATE device SET user_data_name = ?2 ,user_data_model = ?3 ,user_data_vendor = ?4 ,type = ?5 ,"
            + "network_layer = ?6 ,location_id = ?7 ,parent = ?8 ,warranty = ?9 ,monitor = ?10 ,remote_access = ?11 ,"
            + "product_id = ?12 ,global_vendor_id = ?13 ,local_vendor_id = ?14 ,other_vendor_1_id = ?15 ,"
            + "other_vendor_2_id = ?16 ,other_vendor_3_id = ?17, email_alert = ?18, sms_alert = ?19, popup_notification = ?20, "
            + "serial_number = ?21, local_vendor_email_alert = ?22, local_vendor_sms_alert = ?23, docker_name = ?24, subsystem_parent_id = ?25, "
            + "custom_fields = COALESCE(?26, custom_fields), description = ?27, asset_match_status = ?28, asset_group = ?29, category = ?30, sub_category = ?31, location_status = CASE WHEN ?7 IS NULL THEN location_status ELSE NULL END  "
            + "WHERE id = ?1 ", nativeQuery = true)
    void multiDeviceUpdateByDeviceId(String id, String user_data_name, String user_data_model, String user_data_vendor,
                                     String type, String network_layer, String location_id, String parent, String warranty, Integer monitor,
                                     Integer remote_access, String product_id, String global_vendor_id, String local_vendor_id,
                                     String other_vendor_1_id, String other_vendor_2_id, String other_vendor_3_id, Integer email_alert,
                                     Integer sms_alert, Integer popup_notification, String serial_number, Integer local_vendor_email_alert,
                                     Integer local_vendor_sms_alert, String docker_name, String subsystem_parent_id, String custom_fields, String description,
                                     Integer asset_match_status, String asset_group, String category, String sub_category);


    /**
     * Updates the status and last-seen timestamp of a virtual device.
     *
     * @param status the device status
     * @param timestamp the last-seen timestamp
     * @param virtual_device_id the virtual device identifier
     */
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Device d SET d.status = ?1, d.last_seen_on = ?2 WHERE d.id = ?3")
    void updateVirtualDeviceStatus(Integer status, BigInteger timestamp, String virtual_device_id);

    /**
     * Returns all virtual devices.
     *
     * @return the virtual device projections
     */
    // NOT CONVERTED — stays native (PG-translation track): multi-join DeviceDTO projection (named-query delegation to Device.java @NamedNativeQuery)
    @Query(nativeQuery = true)
    List<DeviceDTO> listAllVirtualdevices();

    // Get Alert Device Info

    /**
     * Returns alert information for the given device.
     *
     * @param device_id the device identifier
     * @return the alert projection
     */
    // NOT CONVERTED — stays native (PG-translation track): multi-join DeviceDTO projection (named-query delegation to Device.java @NamedNativeQuery)
    @Query(nativeQuery = true)
    AlertDTO getDeviceAlertInfoByDeviceId(String device_id);

    /**
     * Updates the SNMP object count of a device.
     *
     * @param device_id the device identifier
     * @param snmp_count the SNMP count to set
     */
    // update snmp count
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Device d SET d.snmp_count = ?2 WHERE d.id = ?1")
    void updateDeviceSnmpCount(String device_id, Integer snmp_count);

    /**
     * Updates the SNMP status of a device.
     *
     * @param device_id the device identifier
     * @param snmp_status the SNMP status to set
     */
    // update snmp status
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Device d SET d.snmp_status = ?2 WHERE d.id = ?1")
    void updateDeviceSnmpStatus(String device_id, String snmp_status);

    /**
     * Updates the interface count of a device.
     *
     * @param device_id the device identifier
     * @param interface_count the interface count to set
     */
    // update interface count
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Device d SET d.interface_count = ?2 WHERE d.id = ?1")
    void updateDeviceInterfaceCount(String device_id, Integer interface_count);

    /**
     * Updates the notes count of a device.
     *
     * @param device_id the device identifier
     * @param notes_count the notes count to set
     */
    // update notes count
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Device d SET d.notes_count = ?2 WHERE d.id = ?1")
    void updateDeviceNotesCount(String device_id, Integer notes_count);

    /**
     * Updates the ticket count of a device.
     *
     * @param device_id the device identifier
     * @param ticket_count the ticket count to set
     */
    // update ticket count
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Device d SET d.ticket_count = ?2 WHERE d.id = ?1")
    void updateDeviceTicketCount(String device_id, Integer ticket_count);

    /**
     * Updates the ticket status of a device.
     *
     * @param device_id the device identifier
     * @param ticket_status the ticket status to set
     */
    // update ticket status
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Device d SET d.ticket_status = ?2 WHERE d.id = ?1")
    void updateDeviceTicketStatus(String device_id, String ticket_status);

    /**
     * Updates the BACnet object count of a device.
     *
     * @param device_id the device identifier
     * @param bacnet_count the BACnet count to set
     */
    // update bacnet count
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Device d SET d.bacnet_count = ?2 WHERE d.id = ?1")
    void updateDeviceBacnetCount(String device_id, Integer bacnet_count);

    /**
     * Updates the BACnet status of a device.
     *
     * @param device_id the device identifier
     * @param bacnet_status the BACnet status to set
     */
    // update bacnet status
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Device d SET d.bacnet_status = ?2 WHERE d.id = ?1")
    void updateDeviceBacnetStatus(String device_id, String bacnet_status);

    /**
     * Updates the LoRaWAN sensor count of a device.
     *
     * @param device_id the device identifier
     * @param lorawan_count the LoRaWAN count to set
     */
    // update lorawan count
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Device d SET d.lorawan_count = ?2 WHERE d.id = ?1")
    void updateDeviceLorawanCount(String device_id, Integer lorawan_count);

    /**
     * Updates the LoRaWAN status of a device.
     *
     * @param device_id the device identifier
     * @param lorawan_status the LoRaWAN status to set
     */
    // update lorawan status
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Device d SET d.lorawan_status = ?2 WHERE d.id = ?1")
    void updateDeviceLorawanStatus(String device_id, String lorawan_status);

    /**
     * Updates the Disruptive sensor count of a device.
     *
     * @param device_id the device identifier
     * @param disruptive_count the Disruptive count to set
     */
    // update disruptive count
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Device d SET d.disruptive_count = ?2 WHERE d.id = ?1")
    void updateDeviceDisruptiveCount(String device_id, Integer disruptive_count);

    /**
     * Updates the Disruptive status of a device.
     *
     * @param device_id the device identifier
     * @param disruptive_status the Disruptive status to set
     */
    // update disruptive status
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Device d SET d.disruptive_status = ?2 WHERE d.id = ?1")
    void updateDeviceDisruptiveStatus(String device_id, String disruptive_status);

    /**
     * Updates the myDevices sensor count of a device.
     *
     * @param device_id the device identifier
     * @param my_devices_count the myDevices count to set
     */
    // update my devices count
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE Device d SET d.my_devices_count = ?2 WHERE d.id = ?1")
    void updateDeviceMyDevicesCount(String device_id, Integer my_devices_count);

    /**
     * Updates the myDevices status of a device.
     *
     * @param device_id the device identifier
     * @param my_devices_status the myDevices status to set
     */
    // update my devices status
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE Device d SET d.my_devices_status = ?2 WHERE d.id = ?1")
    void updateDeviceMyDevicesStatus(String device_id, String my_devices_status);

    /**
     * Updates the Monnit sensor count of a device.
     *
     * @param device_id the device identifier
     * @param monnit_count the Monnit count to set
     */
    // update monnit count
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE Device d SET d.monnit_count = ?2 WHERE d.id = ?1")
    void updateDeviceMonnitCount(String device_id, Integer monnit_count);

    /**
     * Updates the Monnit status of a device.
     *
     * @param device_id the device identifier
     * @param monnit_status the Monnit status to set
     */
    // update monnit status
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE Device d SET d.monnit_status = ?2 WHERE d.id = ?1")
    void updateDeviceMonnitStatus(String device_id, String monnit_status);

    /**
     * Updates the Pelican sensor count of a device.
     *
     * @param device_id the device identifier
     * @param pelican_count the Pelican count to set
     */
    // update pelican count
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE Device d SET d.pelican_count = ?2 WHERE d.id = ?1")
    void updateDevicePelicanCount(String device_id, Integer pelican_count);

    /**
     * Updates the Pelican status of a device.
     *
     * @param device_id the device identifier
     * @param pelican_status the Pelican status to set
     */
    // update  pelican status
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE Device d SET d.pelican_status = ?2 WHERE d.id = ?1")
    void updateDevicePelicanStatus(String device_id, String pelican_status);

    /**
     * Updates the KNX object count of a device.
     *
     * @param device_id the device identifier
     * @param knx_count the KNX count to set
     */
    // update knx count
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Device d SET d.knx_count = ?2 WHERE d.id = ?1")
    void updateDeviceKNXCount(String device_id, Integer knx_count);

    /**
     * Updates the KNX status of a device.
     *
     * @param device_id the device identifier
     * @param knx_status the KNX status to set
     */
    // update  knx status
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE Device d SET d.knx_status = ?2 WHERE d.id = ?1")
    void updateDeviceKNXStatus(String device_id, String knx_status);

    /**
     * Updates the measuring instrument count of a device.
     *
     * @param device_id the device identifier
     * @param measuring_instrument_count the measuring instrument count to set
     */
    // update measure count
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Device d SET d.measuring_instrument_count = ?2 WHERE d.id = ?1")
    void updateDeviceMeasureCount(String device_id, Integer measuring_instrument_count);

    /**
     * Updates the document count of a device.
     *
     * @param device_id the device identifier
     * @param document_count the document count to set
     */
    // update documents count
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Device d SET d.document_count = ?2 WHERE d.id = ?1")
    void updateDeviceDocumentsCount(String device_id, Integer document_count);


    /**
     * Updates the media count of a device.
     *
     * @param device_id the device identifier
     * @param media_count the media count to set
     */
    // update media count
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Device d SET d.media_count = ?2 WHERE d.id = ?1")
    void updateDeviceMediaCount(String device_id, Integer media_count);

    /**
     * Updates the checklist template count of a device.
     *
     * @param device_id the device identifier
     * @param checklist_template_count the checklist template count to set
     */
    // update checklists count
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Device d SET d.checklist_template_count = ?2 WHERE d.id = ?1")
    void updateDeviceCheckListsCount(String device_id, Integer checklist_template_count);

    /**
     * Updates the SNMP object count of a device.
     *
     * @param device_id the device identifier
     * @param snmp_object_count the SNMP object count to set
     */
    //update snmp object count
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Device d SET d.snmp_object_count = ?2 WHERE d.id = ?1")
    void updateDeviceSnmpObjectCount(String device_id, Integer snmp_object_count);

    /**
     * Updates the SNMP object status of a device.
     *
     * @param device_id the device identifier
     * @param snmp_object_status the SNMP object status to set
     */
    //update snmp object status
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Device d SET d.snmp_object_status = ?2 WHERE d.id = ?1")
    void updateDeviceSnmpObjectStatus(String device_id, String snmp_object_status);

    //Get All Parent Device by Pagination
//	@Query(nativeQuery = true)
//	Set<DeviceDTO> getNetworkParentDeviceByPagination(Set<String> dockernames, String searchKey,Integer pagesize, Integer offset);

    /**
     * Returns a page of network parent devices for the given dockers and types.
     *
     * @param dockernames the docker names to match
     * @param types the device types to match
     * @param searchKey the search filter term
     * @param pagesize the maximum number of devices to return
     * @param offset the number of devices to skip
     * @param virtual_device_types the virtual device types to match
     * @return the matching device projections
     */
    // NOT CONVERTED — stays native (PG-translation track): multi-join DeviceDTO projection (named-query delegation to Device.java @NamedNativeQuery)
    @Query(nativeQuery = true)
    Set<DeviceDTO> getNetworkParentDeviceByPagination(Set<String> dockernames, Set<String> types, String searchKey, Integer pagesize, Integer offset, Set<String> virtual_device_types);


    /**
     * Returns a page of all parent devices matching the search key.
     *
     * @param searchKey the search filter term
     * @param pagesize the maximum number of devices to return
     * @param offset the number of devices to skip
     * @return the matching device projections
     */
    //Get Network Parent Device by Pagination
    // NOT CONVERTED — stays native (PG-translation track): multi-join DeviceDTO projection (named-query delegation to Device.java @NamedNativeQuery)
    @Query(nativeQuery = true)
    Set<DeviceDTO> getAllParentDeviceByPagination(String searchKey, Integer pagesize, Integer offset);

    /**
     * Returns the display name of the parent device, falling back to the display name when no user-supplied name exists.
     *
     * @param parent_device_id the parent device identifier
     * @return the resolved parent device name
     */
    //Get Parent Device Name by Id
    // PG-port: IF->CASE WHEN
    @Query("SELECT CASE WHEN (d.user_data_name IS NULL OR d.user_data_name = '') THEN d.display_name ELSE d.user_data_name END FROM Device d WHERE d.id = ?1")
    String getParentDeviceNameById(String parent_device_id);

    /**
     * Returns a page of subsystem parent devices matching the given filters.
     *
     * @return the matching device projections
     */
    //new get method with subsystem parent devices get
    // NOT CONVERTED — stays native (PG-translation track): multi-join DeviceDTO projection (named-query delegation to Device.java @NamedNativeQuery)
    @Query(nativeQuery = true)
    Set<DeviceDTO> getSubsystemParentDevicesByPagination(String vdmsid, String dockername, Integer virtual_device_type,
                                                         Integer status, Integer monitor, Integer asset_match_status, Integer pagesize, Integer offset, Integer onboard_status, Integer assigned_status, String assignee);

    /**
     * Returns the subsystem parent devices matching the given filters.
     *
     * @return the matching device projections
     */
    // NOT CONVERTED — stays native (PG-translation track): multi-join DeviceDTO projection (named-query delegation to Device.java @NamedNativeQuery)
    @Query(nativeQuery = true)
    Set<DeviceDTO> getSubsystemParentDevices(String vdmsid, String dockername, Integer virtual_device_type, Integer status, Integer monitor, Integer asset_match_status, Integer onboard_status, Integer assigned_status);

    /**
     * Returns a page of subsystem devices for the given parent device.
     *
     * @param device_id the parent device identifier
     * @param pagesize the maximum number of devices to return
     * @param offset the number of devices to skip
     * @param assignee the assignee filter
     * @return the matching device projections
     */
    //new get method with subsystem devices get
    // NOT CONVERTED — stays native (PG-translation track): multi-join DeviceDTO projection (named-query delegation to Device.java @NamedNativeQuery)
    @Query(nativeQuery = true)
    Set<DeviceDTO> getSubsystemDevicesByPagination(String device_id, Integer pagesize, Integer offset, String assignee);

    /**
     * Updates the subsystem count of a device.
     *
     * @param device_id the device identifier
     * @param subsystem_count the subsystem count to set
     */
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE Device d SET d.subsystem_count = ?2 WHERE d.id = ?1")
    void updateSubsystemCount(String device_id, Integer subsystem_count);

    /**
     * Counts the subsystem devices belonging to the given parent device.
     *
     * @param device_id the parent device identifier
     * @return the number of subsystem devices
     */
    // NOT CONVERTED — stays native (PG-translation track): JPQL COUNT returns Long, but signature is Integer; would need a default wrapper
    @Query(value = "SELECT COUNT(*) FROM device WHERE subsystem_parent_id= ?1", nativeQuery = true)
    Integer getSubsystemCount(String device_id);

    /**
     * Returns the subsystem parent id of the given device.
     *
     * @param id the device identifier
     * @return the subsystem parent id, or {@code null} if none is set
     */
    //get sub system parent id
    @Query("SELECT d.subsystem_parent_id FROM Device d WHERE d.id = ?1")
    String getSubsystemParentId(String id);

    /**
     * Returns the devices matching the given ids, preserving the supplied order.
     *
     * @param device_ids the device identifiers to match
     * @return the matching device projections
     */
    //get devices by ids
    // PG-port: MySQL FIELD()->array_position; param must be a SQL array, not a Collection.
    // NOT CONVERTED — stays native (PG-translation track): multi-join DeviceDTO projection (named-query delegation to Device.java @NamedNativeQuery)
    @Query(nativeQuery = true)
    Set<DeviceDTO> getDevicesByIdList(String[] device_ids);

    /**
     * Returns the ids of devices whose subsystem parent is the given device.
     *
     * @param device_id the parent device identifier
     * @return the matching device ids
     */
    @Query("SELECT d.id FROM Device d WHERE d.subsystem_parent_id = ?1")
    List<String> getDevicesBySubSystemParentId(String device_id);

    /**
     * Sets the subsystem parent of the given device.
     *
     * @param device_id the device identifier
     * @param subsystem_parent_id the subsystem parent id to set
     */
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE Device d SET d.subsystem_parent_id = ?2 WHERE d.id = ?1")
    void updateSubsystemParentDevice(String device_id, String subsystem_parent_id);
    // Touchscreen
    // Repository*********************************************************************************************************************************************************************************8

    /**
     * Returns the touchscreen device list filtered by network, building, floor, location, and status.
     *
     * @return the matching device list projections
     */
    //To be removed after new pagination api works
    // NOT CONVERTED — stays native (PG-translation track): multi-join DeviceDTO projection (named-query delegation to Device.java @NamedNativeQuery)
    @Query(nativeQuery = true)
    Set<DeviceListDTO> listDevicesTs(String networkname, String buildingid, String floorid, String locationid,
                                     Integer devicestatus);

    /**
     * Returns a page of touchscreen devices filtered by network, building, floor, location, and status.
     *
     * @return the matching device list projections
     */
    //Added Pagination for listDevices
    // NOT CONVERTED — stays native (PG-translation track): multi-join DeviceDTO projection (named-query delegation to Device.java @NamedNativeQuery)
    @Query(nativeQuery = true)
    Set<DeviceListDTO> listDevicesByPaginationTs(String networkname, String buildingid, String floorid, String locationid, Integer status, Integer pagesize, Integer offset, Integer virtual_device_type);

    /**
     * Updates the SNMP parent and optionally the type of a device within a docker.
     *
     * @param dockername the docker name
     * @param id the device identifier
     * @param snmp_parent the SNMP parent reference
     * @param device_type the device type, or {@code null} to keep the current value
     */
    @Modifying(clearAutomatically = true)
    @Transactional
    // PG-port: IFNULL->COALESCE
    @Query("UPDATE Device d SET d.snmp_parent = ?3, d.type = COALESCE(?4, d.type) WHERE d.docker.name = ?1 AND d.id = ?2")
    void updateSnmpParent(String dockername, String id, String snmp_parent, String device_type);


    /**
     * Returns the offline devices grouped by parent for the touchscreen view.
     *
     * @return the matching device list projections
     */
    //to be removed after pagination api works
    // NOT CONVERTED — stays native (PG-translation track): multi-join DeviceDTO projection (named-query delegation to Device.java @NamedNativeQuery)
    @Query(nativeQuery = true)
    Set<DeviceListDTO> listofflinedeviceByParentTs();

    /**
     * Returns a page of offline devices grouped by parent for the touchscreen view.
     *
     * @param pagesize the maximum number of devices to return
     * @param offset the number of devices to skip
     * @return the matching device list projections
     */
    //Added pagination for listofflinedeviceByParentTs
    // NOT CONVERTED — stays native (PG-translation track): multi-join DeviceDTO projection (named-query delegation to Device.java @NamedNativeQuery)
    @Query(nativeQuery = true)
    Set<DeviceListDTO> listofflinedeviceByParentByPaginationTs(Integer pagesize, Integer offset);

    /**
     * Returns the touchscreen device list entry for the given device.
     *
     * @param deviceId the device identifier
     * @return the matching device list projection
     */
    // NOT CONVERTED — stays native (PG-translation track): multi-join DeviceDTO projection (named-query delegation to Device.java @NamedNativeQuery)
    @Query(nativeQuery = true)
    DeviceListDTO DeviceInfoById(String deviceId);

    // API FROM BACKEND

    /**
     * Returns detailed device information for the given device.
     *
     * @param deviceid the device identifier
     * @return the matching device details projection
     */
    // NOT CONVERTED — stays native (PG-translation track): multi-join DeviceDTO projection (named-query delegation to Device.java @NamedNativeQuery)
    @Query(nativeQuery = true)
    DeviceDetailsDTO getDeviceInfoById(String deviceid);

    /**
     * Inserts a minimal device record capturing its status and network identity.
     *
     * @param ip_address the device IP address
     * @param mac_address the device MAC address
     * @param status the device status
     * @param last_seen_on the last-seen timestamp
     * @param vdms_id the VDMS identifier
     * @param docker_name the docker name
     * @param id the device identifier
     * @param vendor the device vendor
     * @param created_timestamp the creation timestamp
     * @param created_email the creating user's email
     */
    @Transactional
    @Modifying
    // NOT CONVERTED — stays native (PG-translation track): plain INSERT
    @Query(value = "INSERT INTO device (ip_address, mac_address , status, last_seen_on, docker_vdms_id, docker_name, id, vendor, created_timestamp, created_email ) VALUES (?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8, ?9, ?10)", nativeQuery = true)
    void insertDeviceStatus(String ip_address, String mac_address, Integer status, BigInteger last_seen_on,
                            String vdms_id, String docker_name, String id, String vendor, BigInteger created_timestamp, String created_email);

    /**
     * Updates the status and network fields of a device, retaining existing values where the supplied ones are null.
     *
     * @param ip_address the device IP address, or {@code null} to keep the current value
     * @param mac_address the device MAC address
     * @param status the device status, or {@code null} to keep the current value
     * @param last_seen_on the last-seen timestamp, or {@code null} to keep the current value
     * @param vdms_id the VDMS identifier
     * @param docker_name the docker name
     * @param id the device identifier
     */
    @Transactional
    @Modifying
    // NOT CONVERTED — stays native (PG-translation track): WHERE clause uses docker_vdms_id/docker_name (FK columns of @ManyToOne Docker with composite PK)
    // PG-port: IFNULL->COALESCE
    @Query(value = "UPDATE device SET ip_address = COALESCE(?1, ip_address), mac_address = ?2, status = COALESCE(?3, status), last_seen_on = COALESCE(?4, last_seen_on) WHERE docker_vdms_id = ?5 AND docker_name =?6 AND id = ?7 ", nativeQuery = true)
    void updateDeviceStatus(String ip_address, String mac_address, Integer status, BigInteger last_seen_on,
                            String vdms_id, String docker_name, String id);

    /**
     * Returns the monitor entries for devices in the given docker.
     *
     * @param dockername the docker name
     * @return the matching monitor projections
     */
    // NOT CONVERTED — stays native (PG-translation track): multi-join DeviceDTO projection (named-query delegation to Device.java @NamedNativeQuery)
    @Query(nativeQuery = true)
    List<DeviceMonitorDTO> getDeviceListMonitor(String dockername);

    /**
     * Returns the IP-based monitor entries for devices in the given docker.
     *
     * @param dockername the docker name
     * @return the matching monitor projections
     */
    // NOT CONVERTED — stays native (PG-translation track): multi-join DeviceDTO projection (named-query delegation to Device.java @NamedNativeQuery)
    @Query(nativeQuery = true)
    List<DeviceMonitorDTO> getDeviceListMonitorIp(String dockername);

    /**
     * Counts monitored, asset-matched devices with the given status that are not mid-onboarding.
     *
     * @param i the device status to match
     * @return the number of matching devices
     */
    // NOT CONVERTED — stays native (PG-translation track): JPQL COUNT returns Long but return type is int; signature preservation requires wrapper complexity
    @Query(value = "SELECT COUNT(id) FROM device WHERE status = ?1 AND monitor = 1 AND asset_match_status != 3  AND (onboard_status IS NULL OR (onboard_status != 1 AND onboard_status != 2))", nativeQuery = true)
    int onlineOfflineCount(Integer i);

    /**
     * Returns the SNMP values for devices in the given docker.
     *
     * @param dockername the docker name
     * @return the matching SNMP value projections
     */
    // NOT CONVERTED — stays native (PG-translation track): multi-join DeviceDTO projection (named-query delegation to Device.java @NamedNativeQuery)
    @Query(nativeQuery = true)
    List<SnmpValuesDTO> getDeviceListSnmp(String dockername);

    /**
     * Returns the SNMP values for a single device, used during SNMP sync.
     *
     * @param dockername the docker name
     * @param device_id the device identifier
     * @return the matching SNMP value projection
     */
    // Get Single Device Info By Device Id for Snmp Sync
    // NOT CONVERTED — stays native (PG-translation track): multi-join DeviceDTO projection (named-query delegation to Device.java @NamedNativeQuery)
    @Query(nativeQuery = true)
    SnmpValuesDTO getDeviceSnmpByDeviceId(String dockername, String device_id);

    /**
     * Returns the status of the given device.
     *
     * @param deviceId the device identifier
     * @return the device status
     */
    @Transactional
    @Query("SELECT d.status FROM Device d WHERE d.id = ?1")
    Integer getDeviceStatus(String deviceId);

    /**
     * Returns the parent reference of the given device.
     *
     * @param deviceId the device identifier
     * @return the parent reference
     */
    @Transactional
    @Query("SELECT d.parent FROM Device d WHERE d.id = ?1")
    String getDeviceparent(String deviceId);

    /**
     * Returns the SNMP parent reference of the given device.
     *
     * @param deviceId the device identifier
     * @return the SNMP parent reference
     */
    @Transactional
    @Query("SELECT d.snmp_parent FROM Device d WHERE d.id = ?1")
    String getDeviceSnmpparent(String deviceId);

    /**
     * Clears any vendor association matching the given vendor id across all devices in a VDMS.
     *
     * @param vendor_id the vendor id to unlink
     * @param vdmsid the VDMS identifier
     */
    @Modifying
    @Transactional
    // NOT CONVERTED — stays native (PG-translation track): sets @ManyToOne Phonebook FK columns; 'THEN x = NULL' broken MySQL syntax to fix in PG migration
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

    /**
     * Updates the display name of a device, defaulting to {@code Generic} when none is supplied.
     *
     * @param id the device identifier
     * @param display_name the display name to set, or {@code null} to default to {@code Generic}
     */
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE Device d SET d.display_name = COALESCE(?2, 'Generic') WHERE d.id = ?1")
    void updateDevicesDisplayNameById(String id, String display_name);

    /**
     * Updates the vendor of a device.
     *
     * @param id the device identifier
     * @param vendor the vendor to set
     */
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE Device d SET d.vendor = ?2 WHERE d.id = ?1")
    void updateDeviceVendorById(String id, String vendor);

    /**
     * Returns all devices.
     *
     * @return the device projections
     */
    @Transactional
    // NOT CONVERTED — stays native (PG-translation track): multi-join DeviceDTO projection (named-query delegation to Device.java @NamedNativeQuery)
    @Query(nativeQuery = true)
    List<DeviceDTO> listAlldevices();

    /**
     * Counts monitored, asset-matched devices with the given status, optionally scoped by docker and assignee.
     *
     * @param dockername the docker name, or {@code all} for all dockers
     * @param i the device status to match
     * @param assignee the assignee email, or {@code all} for all assignees
     * @return the number of matching devices
     */
    @Transactional
    // NOT CONVERTED — stays native (PG-translation track): assigned_user_email is @ManyToOne User FK (@JoinColumn); JPQL navigation d.user.email would fail for null user rows; keep native
    // PG-port: IF->CASE WHEN
    @Query(value = "SELECT COUNT(id) FROM device WHERE status = ?2 AND monitor = 1 AND (?1 = 'all' or docker_name = ?1) AND asset_match_status != 3 AND CASE WHEN 'all' = ?3 THEN true ELSE assigned_user_email = ?3 END ", nativeQuery = true)
    Integer onlineOfflineCountByDocker(String dockername, int i, String assignee);

    /**
     * Counts unmonitored, asset-matched devices, optionally scoped by docker and assignee.
     *
     * @param dockername the docker name, or {@code all} for all dockers
     * @param assignee the assignee email, or {@code all} for all assignees
     * @return the number of matching devices
     */
    @Transactional
    // NOT CONVERTED — stays native (PG-translation track): assigned_user_email is @ManyToOne User FK (@JoinColumn); JPQL navigation d.user.email would fail for null user rows; keep native
    // PG-port: IF->CASE WHEN
    @Query(value = "SELECT COUNT(id) FROM device WHERE (?1 = 'all' or docker_name = ?1) AND (monitor = 0 OR monitor IS NULL) AND asset_match_status != 3 AND CASE WHEN 'all' = ?2 THEN true ELSE assigned_user_email = ?2 END ", nativeQuery = true)
    Integer unmonitorCountByDocker(String dockername, String assignee);

    /**
     * Counts monitored, asset-matched devices, optionally scoped by docker and assignee.
     *
     * @param dockername the docker name, or {@code all} for all dockers
     * @param assignee the assignee email, or {@code all} for all assignees
     * @return the number of matching devices
     */
    @Transactional
    // NOT CONVERTED — stays native (PG-translation track): assigned_user_email is @ManyToOne User FK (@JoinColumn); JPQL navigation d.user.email would fail for null user rows; keep native
    // PG-port: IF->CASE WHEN
    @Query(value = "SELECT COUNT(id) FROM device WHERE (?1 = 'all' or docker_name = ?1) AND monitor = 1  AND asset_match_status != 3 AND CASE WHEN 'all' = ?2 THEN true ELSE assigned_user_email = ?2 END ", nativeQuery = true)
    Integer monitorCountByDocker(String dockername, String assignee);

    /**
     * Counts assigned, asset-matched devices, optionally scoped by docker and assignee.
     *
     * @param dockername the docker name, or {@code all} for all dockers
     * @param assignee the assignee email, or {@code all} for all assignees
     * @return the number of matching devices
     */
    @Transactional
    // NOT CONVERTED — stays native (PG-translation track): assigned_user_email is @ManyToOne User FK (@JoinColumn); JPQL navigation d.user.email would fail for null user rows; keep native
    // PG-port: IF->CASE WHEN
    @Query(value = "SELECT COUNT(id) FROM device WHERE (?1 = 'all' or docker_name = ?1) AND (assigned_user_email IS NOT NULL or assigned_user_email != 'null')  AND asset_match_status != 3 AND CASE WHEN 'all' = ?2 THEN true ELSE assigned_user_email = ?2 END ", nativeQuery = true)
    Integer assignedCountByDocker(String dockername, String assignee);


    /**
     * Counts unassigned, asset-matched devices, optionally scoped by docker and assignee.
     *
     * @param dockername the docker name, or {@code all} for all dockers
     * @param assignee the assignee email, or {@code all} for all assignees
     * @return the number of matching devices
     */
    // NOT CONVERTED — stays native (PG-translation track): assigned_user_email is @ManyToOne User FK (@JoinColumn); JPQL navigation d.user.email would fail for null user rows; keep native
    // PG-port: IF->CASE WHEN
    @Query(value = "SELECT COUNT(id) FROM device WHERE (?1 = 'all' or docker_name = ?1) AND (assigned_user_email IS NULL or assigned_user_email = 'null')  AND asset_match_status != 3 AND CASE WHEN 'all' = ?2 THEN true ELSE assigned_user_email = ?2 END ", nativeQuery = true)
    Integer unAssignedCountByDocker(String dockername, String assignee);

    /**
     * Counts monitored, asset-matched "other" virtual devices, optionally scoped by docker.
     *
     * @param dockername the docker name, or {@code all} for all dockers
     * @return the number of matching devices
     */
    // other device count
    @Transactional
    @Query("SELECT COUNT(d) FROM Device d WHERE ('all' = ?1 OR d.docker.name = ?1) AND d.monitor = 1 AND d.virtual_device_type IS NOT NULL AND d.virtual_device_type <> 0 AND d.virtual_device_type <> 1 AND d.asset_match_status <> 3")
    Integer otherDeviceCountByDocker(String dockername);

    /**
     * Counts monitored, asset-matched "other" virtual devices, optionally scoped by docker and assignee.
     *
     * @param dockername the docker name, or {@code all} for all dockers
     * @param assignee the assignee email, or {@code all} for all assignees
     * @return the number of matching devices
     */
    // other device count
    @Transactional
    // NOT CONVERTED — stays native (PG-translation track): assigned_user_email is @ManyToOne User FK (@JoinColumn); JPQL navigation d.user.email would fail for null user rows; keep native
    // PG-port: IF->CASE WHEN
    @Query(value = "SELECT COUNT(id) FROM device WHERE (?1 = 'all' or docker_name = ?1) AND monitor = 1 AND ( virtual_device_type IS NOT NULL AND (virtual_device_type != 0 AND virtual_device_type != 1 ) ) AND asset_match_status != 3 AND CASE WHEN 'all' = ?2 THEN true ELSE assigned_user_email = ?2 END ", nativeQuery = true)
    Integer otherDeviceCountByDockerAssignee(String dockername, String assignee);

    /**
     * Counts devices with the given asset match status, optionally scoped by docker and assignee.
     *
     * @param dockername the docker name, or {@code all} for all dockers
     * @param i the asset match status to match
     * @param assignee the assignee email, or {@code all} for all assignees
     * @return the number of matching devices
     */
    // matched/unmatched device count
    @Transactional
    // NOT CONVERTED — stays native (PG-translation track): assigned_user_email is @ManyToOne User FK (@JoinColumn); JPQL navigation d.user.email would fail for null user rows; keep native
    // PG-port: IF->CASE WHEN
    @Query(value = "SELECT COUNT(id) FROM device WHERE (?1 = 'all' or docker_name = ?1) AND asset_match_status = ?2 AND CASE WHEN 'all' = ?3 THEN true ELSE assigned_user_email = ?3 END", nativeQuery = true)
    Integer getMatchedUnmatchedDeviceCountByDocker(String dockername, int i, String assignee);

    /**
     * Counts not-yet-onboarded, asset-matched devices, optionally scoped by docker and assignee.
     *
     * @param dockername the docker name, or {@code all} for all dockers
     * @param assignee the assignee email, or {@code all} for all assignees
     * @return the number of matching devices
     */
    @Transactional
    // NOT CONVERTED — stays native (PG-translation track): assigned_user_email is @ManyToOne User FK (@JoinColumn); JPQL navigation d.user.email would fail for null user rows; keep native
    // PG-port: IF->CASE WHEN
    @Query(value = "SELECT COUNT(id) FROM device WHERE (?1 = 'all' or docker_name = ?1)  AND asset_match_status != 3 AND  (onboard_status != 3) AND CASE WHEN 'all' = ?2 THEN true ELSE assigned_user_email = ?2 END ", nativeQuery = true)
    Integer getNotOnboardedDeviceCountByDocker(String dockername, String assignee);

    /**
     * Counts onboarded, asset-matched devices, optionally scoped by docker and assignee.
     *
     * @param dockername the docker name, or {@code all} for all dockers
     * @param assignee the assignee email, or {@code all} for all assignees
     * @return the number of matching devices
     */
    @Transactional
    // NOT CONVERTED — stays native (PG-translation track): assigned_user_email is @ManyToOne User FK (@JoinColumn); JPQL navigation d.user.email would fail for null user rows; keep native
    // PG-port: IF->CASE WHEN
    @Query(value = "SELECT COUNT(id) FROM device WHERE (?1 = 'all' or docker_name = ?1)   AND asset_match_status != 3 AND (onboard_status = 3) AND CASE WHEN 'all' = ?2 THEN true ELSE assigned_user_email = ?2 END ", nativeQuery = true)
    Integer getOnboardedDeviceCountByDocker(String dockername, String assignee);


    /**
     * Counts monitored, asset-matched devices, optionally scoped by docker.
     *
     * @param docker_name the docker name, or {@code all} for all dockers
     * @return the number of matching devices
     */
    // get monitored device count
    @Transactional
    @Query("SELECT COUNT(d) FROM Device d WHERE ('all' = ?1 OR d.docker.name = ?1) AND d.monitor = 1 AND d.asset_match_status <> 3")
    Integer getMonitoredDeviceCountByDocker(String docker_name);

    /**
     * Counts asset-matched devices, optionally scoped by docker.
     *
     * @param docker_name the docker name, or {@code all} for all dockers
     * @return the number of matching devices
     */
    //get all device count by docker
    @Transactional
    @Query("SELECT COUNT(d) FROM Device d WHERE ('all' = ?1 OR d.docker.name = ?1) AND d.asset_match_status <> 3")
    Integer getAllDeviceCountByDocker(String docker_name);

    /**
     * Counts asset-matched devices, optionally scoped by docker and assignee.
     *
     * @param docker_name the docker name, or {@code all} for all dockers
     * @param assignee the assignee email, or {@code all} for all assignees
     * @return the number of matching devices
     */
    //get all device count by docker
    @Transactional
    // NOT CONVERTED — stays native (PG-translation track): assigned_user_email is @ManyToOne User FK (@JoinColumn); JPQL navigation d.user.email would fail for null user rows; keep native
    // PG-port: IF->CASE WHEN
    @Query(value = "SELECT COUNT(id) FROM device WHERE (?1 = 'all' or docker_name = ?1) AND asset_match_status != 3 AND CASE WHEN 'all' = ?2 THEN true ELSE assigned_user_email = ?2 END ", nativeQuery = true)
    Integer getAllDeviceCountByDockerAssignee(String docker_name, String assignee);

    /**
     * Returns the topology devices for the given docker rooted at the given device.
     *
     * @param dockername the docker name
     * @param device_id the root device identifier
     * @return the matching device projections
     */
    @Transactional
    // NOT CONVERTED — stays native (PG-translation track): multi-join DeviceDTO projection (named-query delegation to Device.java @NamedNativeQuery)
    @Query(nativeQuery = true)
    List<DeviceDTO> listTopologyDevicesByDockerName(String dockername, String device_id);

    /**
     * Returns the topology of devices for the given docker rooted at the given device.
     *
     * @param dockername the docker name
     * @param device_id the root device identifier
     * @return the matching topology projections
     */
    @Transactional
    // NOT CONVERTED — stays native (PG-translation track): multi-join DeviceDTO projection (named-query delegation to Device.java @NamedNativeQuery)
    @Query(nativeQuery = true)
    List<DeviceTopologyDTO> listTopologyDevices(String dockername, String device_id);

    /**
     * Returns a lightweight projection of all devices.
     *
     * @return the device data projections
     */
    // NOT CONVERTED — stays native (PG-translation track): multi-join DeviceDTO projection (named-query delegation to Device.java @NamedNativeQuery)
    @Query(nativeQuery = true)
    Set<DevicedataDTO> getAllDevices();

    /**
     * Returns the distinct monitored device types for the given network and floor.
     *
     * @param network_name the docker name, or {@code all} for all dockers
     * @param floor_id the floor identifier, or {@code null} for all floors
     * @return the distinct device types
     */
    // get unique device types
    @Transactional
    // NOT CONVERTED — stays native (PG-translation track): multi-join COUNT/DISTINCT query
    @Query(value = "SELECT DISTINCT(d.type) FROM device d LEFT JOIN docker dk ON (dk.name = d.docker_name AND dk.vdms_id = d.docker_vdms_id) LEFT JOIN location l ON l.id = d.location_id LEFT JOIN floor f ON f.id = l.floor_id WHERE (?1 = 'all' or d.docker_name = ?1) AND (?2 = 'null' or f.id = ?2) AND d.type IS NOT NULL AND d.monitor = 1 AND d.asset_match_status != 3 ORDER BY d.type", nativeQuery = true)
    List<String> getUniqueDeviceTypes(String network_name, String floor_id);

    /**
     * Returns the distinct asset groups for the given network.
     *
     * @param network_name the docker name, or {@code all} for all dockers
     * @return the distinct asset groups
     */
    @Transactional
    // NOT CONVERTED — stays native (PG-translation track): multi-join COUNT/DISTINCT query
    @Query(value = "SELECT DISTINCT(d.asset_group) FROM device d LEFT JOIN docker dk ON (dk.name = d.docker_name AND dk.vdms_id = d.docker_vdms_id) WHERE (?1 = 'all' or d.docker_name = ?1) AND d.asset_group IS NOT NULL ORDER BY d.asset_group", nativeQuery = true)
    List<String> getUniqueAssetGroups(String network_name);

    /**
     * Returns the distinct categories for the given network.
     *
     * @param network_name the docker name, or {@code all} for all dockers
     * @return the distinct categories
     */
    @Transactional
    // NOT CONVERTED — stays native (PG-translation track): multi-join COUNT/DISTINCT query
    @Query(value = "SELECT DISTINCT(d.category) FROM device d LEFT JOIN docker dk ON (dk.name = d.docker_name AND dk.vdms_id = d.docker_vdms_id) WHERE (?1 = 'all' or d.docker_name = ?1) AND d.category IS NOT NULL ORDER BY d.category", nativeQuery = true)
    List<String> getUniqueCategory(String network_name);

    /**
     * Returns the distinct sub-categories for the given network and category.
     *
     * @param network_name the docker name, or {@code all} for all dockers
     * @param category the category to match
     * @return the distinct sub-categories
     */
    @Transactional
    // NOT CONVERTED — stays native (PG-translation track): multi-join COUNT/DISTINCT query
    @Query(value = "SELECT DISTINCT(d.sub_category) FROM device d LEFT JOIN docker dk ON (dk.name = d.docker_name AND dk.vdms_id = d.docker_vdms_id) WHERE (?1 = 'all' or d.docker_name = ?1) AND d.category = ?2 AND d.sub_category IS NOT NULL ORDER BY d.sub_category", nativeQuery = true)
    List<String> getUniqueSubCategory(String network_name, String category);

    /**
     * Returns the distinct assigned user emails for the given VDMS and network.
     *
     * @param vdms_id the VDMS identifier
     * @param network_name the docker name, or {@code all} for all dockers
     * @return the distinct assigned user emails
     */
    @Transactional
    // NOT CONVERTED — stays native (PG-translation track): multi-join COUNT/DISTINCT query
    @Query(value = "SELECT DISTINCT(d.assigned_user_email) FROM device d LEFT JOIN docker dk ON (dk.name = d.docker_name AND dk.vdms_id = d.docker_vdms_id) WHERE d.docker_vdms_id = ?1 AND (?2 = 'all' or d.docker_name = ?2) AND d.assigned_user_email IS NOT NULL ORDER BY d.assigned_user_email", nativeQuery = true)
    List<String> getUniqueAssignedUserEmail(String vdms_id, String network_name);

    /**
     * Returns the sensor-bearing devices of the given types for a network and floor.
     *
     * @param network_name the docker name, or {@code all} for all dockers
     * @param floor_id the floor identifier, or {@code null} for all floors
     * @param types the device types to match
     * @return the matching sensor device projections
     */
    // Get Devices by types
    // NOT CONVERTED — stays native (PG-translation track): multi-join DeviceDTO projection (named-query delegation to Device.java @NamedNativeQuery)
    @Query(nativeQuery = true)
    List<DeviceSensorsDTO> getDevicesByType(String network_name, String floor_id, Set<String> types);

    /**
     * Returns a page of sensor-bearing devices of the given types for a network and floor.
     *
     * @param network_name the docker name, or {@code all} for all dockers
     * @param floor_id the floor identifier, or {@code null} for all floors
     * @param types the device types to match
     * @param pagesize the maximum number of devices to return
     * @param offset the number of devices to skip
     * @return the matching sensor device projections
     */
    // NOT CONVERTED — stays native (PG-translation track): multi-join DeviceDTO projection (named-query delegation to Device.java @NamedNativeQuery)
    @Query(nativeQuery = true)
    List<DeviceSensorsDTO> getDevicesByTypePagination(String network_name, String floor_id, Set<String> types, Integer pagesize,
                                                      Integer offset);


    /**
     * Counts the monitored, asset-matched devices of the given types for a network and floor.
     *
     * @param network_name the docker name, or {@code all} for all dockers
     * @param floor_id the floor identifier, or {@code null} for all floors
     * @param types the device types to match
     * @return the number of matching devices
     */
    // NOT CONVERTED — stays native (PG-translation track): multi-join COUNT/DISTINCT query
    @Query(value = "SELECT COUNT(*) FROM device d "
            + " LEFT JOIN docker dk ON (dk.name = d.docker_name AND dk.vdms_id = d.docker_vdms_id) "
            + " LEFT JOIN location l ON d.location_id = l.id "
            + " LEFT JOIN floor f ON l.floor_id = f.id "
            + " WHERE (?1 = 'all' or d.docker_name = ?1) AND (?2 = 'null' or f.id = ?2) AND d.type IN ?3 AND d.monitor = 1 AND d.asset_match_status != 3", nativeQuery = true)
    String getDevicesByTypeCount(String network_name, String floor_id, Set<String> types);


    /**
     * Updates the position, location, coordinates, and asset match status of a device.
     *
     * @param id the device identifier
     * @param position the device position
     * @param location_id the location identifier
     * @param latitude the latitude
     * @param longitude the longitude
     * @param asset_match_status the asset match status
     */
    @Modifying
    @Transactional
    // NOT CONVERTED — stays native (PG-translation track): location_id is the FK column of the @ManyToOne Location relation, not a scalar @Column field
    @Query(value = "UPDATE device SET position = ?2, location_id = ?3 , latitude = ?4, longitude = ?5, asset_match_status = ?6 WHERE id = ?1", nativeQuery = true)
    void updateDevicePosition(String id, String position, String location_id, String latitude, String longitude, Integer asset_match_status);

    /**
     * Returns the devices for the given docker in the integration view.
     *
     * @param dockername the docker name
     * @return the matching device projections
     */
    @Modifying
    @Transactional
    // NOT CONVERTED — stays native (PG-translation track): multi-join DeviceDTO projection (named-query delegation to Device.java @NamedNativeQuery)
    @Query(nativeQuery = true)
    List<DeviceDTO> listDevicebyDockerIntegration(String dockername);


    /**
     * Updates the parent of a device, retaining the existing user connection type when none is supplied.
     *
     * @param id the device identifier
     * @param parent the parent reference to set
     * @param user_connection_type the user connection type, or {@code null} to keep the current value
     */
    @Modifying(clearAutomatically = true)
    @Transactional
    // PG-port: IFNULL->COALESCE
    @Query("UPDATE Device d SET d.parent = ?2, d.user_connection_type = COALESCE(?3, d.user_connection_type) WHERE d.id = ?1")
    void updateDeviceParent(String id, String parent, String user_connection_type);

    /**
     * Resets the parent reference for all devices in the given docker.
     *
     * @param dockername the docker name
     * @param parent the parent reference to set
     */
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE Device d SET d.parent = ?2 WHERE d.docker.name = ?1")
    void resetDeviceParentByDockername(String dockername, String parent);

    /**
     * Returns the id of a device carrying the given gateway IP address.
     *
     * @param gatewayip the gateway IP address
     * @return the matching device id
     */
    @Transactional
    // NOT CONVERTED — stays native (PG-translation track): multi-table JOIN with LIMIT; JPQL has no LIMIT keyword and would need Pageable
    @Query(value = "SELECT DISTINCT(d.id) FROM device d JOIN device_ip_address dip ON d.id = dip.device_id WHERE dip.ip_address = ?1 LIMIT 1", nativeQuery = true)
    String getGatewayIdFromGatewayIp(String gatewayip);

    /**
     * Updates the asset match status and audit fields of a device.
     *
     * @param assetMatchStatus the asset match status to set
     * @param deviceId the device identifier
     * @param updated_email the updating user's email
     * @param updated_timestamp the update timestamp
     */
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Device d SET d.asset_match_status = ?1, d.updated_email = ?3, d.updated_timestamp = ?4 WHERE d.id = ?2")
    void updateDeviceAssetStatus(int assetMatchStatus, String deviceId, String updated_email, BigInteger updated_timestamp);

    /*****************************************Asset Mapper Queries*********************************************************/

    /**
     * Returns asset-device match candidates for the given model.
     *
     * @param model the model to match
     * @return the matching asset-device projections
     */
    // NOT CONVERTED — stays native (PG-translation track): multi-join DeviceDTO projection (named-query delegation to Device.java @NamedNativeQuery)
    @Query(nativeQuery = true)
    List<AssetDeviceDTO> findByModel(String model);

    /**
     * Returns asset-device match candidates for the given vendor.
     *
     * @param vendor the vendor to match
     * @return the matching asset-device projections
     */
    // NOT CONVERTED — stays native (PG-translation track): multi-join DeviceDTO projection (named-query delegation to Device.java @NamedNativeQuery)
    @Query(nativeQuery = true)
    List<AssetDeviceDTO> findByVendor(String vendor);

    /**
     * Returns asset-device match candidates for the given display name.
     *
     * @param display_name the display name to match
     * @return the matching asset-device projections
     */
    // NOT CONVERTED — stays native (PG-translation track): multi-join DeviceDTO projection (named-query delegation to Device.java @NamedNativeQuery)
    @Query(nativeQuery = true)
    List<AssetDeviceDTO> findByDisplayName(String display_name);

    /**
     * Returns a page of asset-device match candidates.
     *
     * @param pageSize the maximum number of devices to return
     * @param offset the number of devices to skip
     * @return the matching asset-device projections
     */
    // NOT CONVERTED — stays native (PG-translation track): multi-join DeviceDTO projection (named-query delegation to Device.java @NamedNativeQuery)
    @Query(nativeQuery = true)
    List<AssetDeviceDTO> getPaginatedDevices(Integer pageSize, Integer offset);

    /**
     * Inserts a virtual device for the asset mapper, merging user-supplied fields on id conflict.
     *
     * @param id the device identifier
     */
    @Modifying
    @Transactional
    // NOT CONVERTED — stays native (PG-translation track): ON CONFLICT upsert
    // PG-port: ON DUPLICATE KEY -> ON CONFLICT (id) DO UPDATE SET (VALUES->EXCLUDED); IFNULL->COALESCE; VALUE->VALUES
    @Query(value = "INSERT INTO device (id, docker_name, docker_vdms_id, user_data_name, user_data_model, user_data_vendor, type, mac_address, ip_address,"
            + " network_layer, serial_number, warranty, custom_fields, subsystem_parent_id, virtual_device_type, monitor, subsystem_count,created_timestamp,  created_email) VALUES (?1, ?2, ?3, ?4,?5,?6,?7,?8,?9, ?10, ?11, ?12, ?13, ?14, ?15, ?16, ?17,?18,?19)"
            + " ON CONFLICT (id) DO UPDATE SET user_data_name = COALESCE(EXCLUDED.user_data_name, device.user_data_name), user_data_model = COALESCE(EXCLUDED.user_data_model, device.user_data_model),"
            + " user_data_vendor = COALESCE(EXCLUDED.user_data_vendor, device.user_data_vendor), type = COALESCE(EXCLUDED.type, device.type), mac_address = COALESCE(EXCLUDED.mac_address, device.mac_address),"
            + " ip_address = COALESCE(EXCLUDED.ip_address, device.ip_address), network_layer = COALESCE(EXCLUDED.network_layer, device.network_layer), serial_number = COALESCE(EXCLUDED.serial_number, device.serial_number),"
            + " warranty = COALESCE(EXCLUDED.warranty, device.warranty), custom_fields = COALESCE(EXCLUDED.custom_fields, device.custom_fields)", nativeQuery = true)
    void upsertVirtualDeviceByAssetMapper(String id, String docker_name, String vdms_id, String user_data_name, String user_data_model, String user_data_vendor,
                                          String type, String mac_address, String ip_address, String network_layer, String serial_number, String warranty,
                                          String custom_fields, String subsystem_parent_id, Integer virtual_device_type, Integer monitor, Integer subsystem_count,
                                          BigInteger created_timestamp, String created_email);

    /**
     * Returns a page of devices not yet tagged to a product.
     *
     * @param pageSize the maximum number of devices to return
     * @param offset the number of devices to skip
     * @return the matching asset projections
     */
    //get unmatched devices by pagination
    @Query(nativeQuery = true)
    List<AssetDTO> getUntaggedProductDevicesByPagination(Integer pageSize, Integer offset);

    /**
     * Counts the devices not yet tagged to a product.
     *
     * @return the number of untagged devices
     */
    @Transactional
    // NOT CONVERTED — stays native (PG-translation track): product_id column has no @Column field on Device entity
    @Query(value = "SELECT COUNT(*) FROM device d WHERE d.product_id IS NULL", nativeQuery = true)
    Integer getUntaggedProductDevicesCount();

    /**
     * Updates the matched product ids of a device.
     *
     * @param device_id the device identifier
     * @param matched_product_ids the matched product ids to store
     */
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE Device d SET d.matched_product_ids = ?2 WHERE d.id = ?1")
    void updateDeviceMatchedProductIds(String device_id, String matched_product_ids);

    /**
     * Returns the asset-mapper devices matching the given ids.
     *
     * @param device_ids the device identifiers to match
     * @return the matching asset projections
     */
    //get devices by device ids for asset mapper
    // NOT CONVERTED — stays native (PG-translation track): multi-join DeviceDTO projection (named-query delegation to Device.java @NamedNativeQuery)
    @Query(nativeQuery = true)
    List<AssetDTO> getAssetMapperDevicesByIdList(List<String> device_ids);

    /**
     * Returns the asset-mapper subsystem devices for the given parent device.
     *
     * @param device_id the parent device identifier
     * @return the matching asset projections
     */
    //get sub system devices by device id for asset mapper
    // NOT CONVERTED — stays native (PG-translation track): multi-join DeviceDTO projection (named-query delegation to Device.java @NamedNativeQuery)
    @Query(nativeQuery = true)
    List<AssetDTO> getAssetMapperSubSystemDevicesById(String device_id);

    /**
     * Updates the product, model, name, vendor, type, and network layer of a device.
     *
     * @param id the device identifier
     * @param product_id the product id to set
     * @param user_data_model the model to set
     * @param user_data_name the name to set
     * @param user_data_vendor the vendor to set
     * @param type the type to set
     * @param network_layer the network layer to set
     */
    @Modifying
    @Transactional
    // NOT CONVERTED — stays native (PG-translation track): product_id column has no @Column field on Device entity
    @Query(value = "UPDATE device SET product_id = ?2, user_data_model = ?3, user_data_name = ?4, user_data_vendor = ?5, type = ?6, network_layer = ?7 where id = ?1", nativeQuery = true)
    void updateDeviceProductDetails(String id, String product_id, String user_data_model, String user_data_name, String user_data_vendor, String type, String network_layer);

    /**
     * Returns the asset-mapper device with the given id.
     *
     * @param device_id the device identifier
     * @return the matching asset projection
     */
    //get asset mapper device by id
    // NOT CONVERTED — stays native (PG-translation track): multi-join DeviceDTO projection (named-query delegation to Device.java @NamedNativeQuery)
    @Query(nativeQuery = true)
    AssetDTO getAssetMapperDeviceById(String device_id);

    /*****************************************Asset Mapper Queries*********************************************************/


    /**
     * Returns a page of devices for the given VDMS and docker, used in SNMP discovery.
     *
     * @param vdmsid the VDMS identifier
     * @param dockername the docker name
     * @param pagesize the maximum number of devices to return
     * @param offset the number of devices to skip
     * @return the matching device projections
     */
    //list devices for snmp discovery
    // NOT CONVERTED — stays native (PG-translation track): multi-join DeviceDTO projection (named-query delegation to Device.java @NamedNativeQuery)
    @Query(nativeQuery = true)
    List<DeviceDTO> getAllDeviceByVdmsIdAndDockerName(String vdmsid, String dockername, Integer pagesize, Integer offset);


    /**
     * Counts monitored, asset-matched devices with the given status, scoped by network, building, floor, and location.
     *
     * @param status the device status to match
     * @param networkname the docker name, or {@code null} for all dockers
     * @param buildingid the building identifier, or {@code null} for all buildings
     * @param floorid the floor identifier, or {@code null} for all floors
     * @param locationid the location identifier, or {@code null} for all locations
     * @return the number of matching devices
     */
    //get device status count
    // NOT CONVERTED — stays native (PG-translation track): multi-join COUNT/DISTINCT query
    @Query(value = "SELECT COUNT(d.id) FROM device d"
            + " Left JOIN location l ON d.location_id = l.id"
            + " Left JOIN floor f ON l.floor_id = f.id"
            + " Left JOIN building b ON f.building_id = b.id"
            + " WHERE d.status = ?1 AND d.asset_match_status != 3  AND d.monitor = 1 AND (?2 = 'null' or d.docker_name = ?2) AND"
            + " (?3 = 'null' or b.id = ?3) AND (?4 = 'null' or f.id = ?4) AND (?5 = 'null' or l.id = ?5)", nativeQuery = true)
    int getDeviceStatusCountTS(Integer status, String networkname, String buildingid, String floorid, String locationid);

    /**
     * Counts monitored, asset-matched devices scoped by docker, building, floor, and location.
     *
     * @param docker_name the docker name, or {@code null} for all dockers
     * @param buildingid the building identifier, or {@code null} for all buildings
     * @param floorid the floor identifier, or {@code null} for all floors
     * @param locationid the location identifier, or {@code null} for all locations
     * @return the number of matching devices
     */
    //get monitored device count
    // NOT CONVERTED — stays native (PG-translation track): multi-join COUNT/DISTINCT query
    @Query(value = "SELECT COUNT(d.id) FROM device d"
            + " Left JOIN location l ON d.location_id = l.id"
            + " Left JOIN floor f ON l.floor_id = f.id"
            + " Left JOIN building b ON f.building_id = b.id"
            + " WHERE d.monitor = 1 AND d.asset_match_status != 3 AND (?1 = 'null' or d.docker_name = ?1) AND (?2 = 'null' or b.id = ?2) AND"
            + " (?3 = 'null' or f.id = ?3) AND (?4 = 'null' or l.id = ?4)", nativeQuery = true)
    Integer getMonitoredDevicesCountByDocker(String docker_name, String buildingid, String floorid, String locationid);

    /**
     * Counts monitored, asset-matched "other" virtual devices scoped by docker, building, floor, and location.
     *
     * @param dockername the docker name, or {@code null} for all dockers
     * @param building_id the building identifier, or {@code null} for all buildings
     * @param floorid the floor identifier, or {@code null} for all floors
     * @param locationid the location identifier, or {@code null} for all locations
     * @return the number of matching devices
     */
    // NOT CONVERTED — stays native (PG-translation track): multi-join COUNT/DISTINCT query
    @Query(value = "SELECT COUNT(d.id) FROM device d"
            + " LEFT JOIN location l ON d.location_id = l.id"
            + " LEFT JOIN floor f ON l.floor_id = f.id"
            + " LEFT JOIN building b ON f.building_id = b.id"
            + " WHERE (?1 = 'null' or d.docker_name = ?1) AND d.monitor = 1 AND d.asset_match_status != 3 AND  "
            + " (d.virtual_device_type IS NOT NULL AND (d.virtual_device_type != 0 AND d.virtual_device_type != 1)) "
            + " AND (?2 = 'null' or b.id = ?2) AND (?3 = 'null' or f.id = ?3) AND (?4 = 'null' or l.id = ?4)", nativeQuery = true)
    Integer otherDevicesCountByDocker(String dockername, String building_id, String floorid, String locationid);

    /**
     * Updates the measuring instrument status of a device.
     *
     * @param device_id the device identifier
     * @param measuring_instrument_status the measuring instrument status to set
     */
    // update measuring instrument status
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE Device d SET d.measuring_instrument_status = ?2 WHERE d.id = ?1")
    void updateDeviceMeasuringInstrumentStatus(String device_id, String measuring_instrument_status);

    /**
     * Updates the coordinates and position of a device.
     *
     * @param device_id the device identifier
     * @param latitude the latitude
     * @param longitude the longitude
     * @param position the position
     */
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE Device d SET d.latitude = ?2, d.longitude = ?3, d.position = ?4 WHERE d.id = ?1")
    void updateDeviceCoordinates(String device_id, String latitude, String longitude, String position);

    /**
     * Returns the device linked to the given checklist.
     *
     * @param checklist_id the checklist identifier
     * @return the matching device projection
     */
    // NOT CONVERTED — stays native (PG-translation track): multi-join DeviceDTO projection (named-query delegation to Device.java @NamedNativeQuery)
    @Query(nativeQuery = true)
    DeviceDTO getDeviceDetails(String checklist_id);

    /**
     * Updates the record-checklist status of a device.
     *
     * @param device_id the device identifier
     * @param checklist_status the record-checklist status to set
     */
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE Device d SET d.record_checklist_status = ?2 WHERE d.id = ?1")
    void updateDeviceRecordChecklistStatus(String device_id, String checklist_status);

    /**
     * Updates the record-checklist count of a device.
     *
     * @param device_id the device identifier
     * @param record_checklist_count the record-checklist count to set
     */
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE Device d SET d.record_checklist_count = ?2 WHERE d.id = ?1")
    void updateDeviceRecordChecklistCount(String device_id, Integer record_checklist_count);


    /**
     * Clears the location and resets coordinates for all devices in the given location.
     *
     * @param location_id the location identifier
     * @param latitude the latitude to set
     * @param longitude the longitude to set
     * @param position the position to set
     */
    @Modifying
    @Transactional
    // NOT CONVERTED — stays native (PG-translation track): location_id is FK of the @ManyToOne Location relation
    @Query(value = "UPDATE device SET location_id = NULL, latitude = ?2, longitude = ?3, position = ?4 WHERE location_id = ?1", nativeQuery = true)
    void updateDeviceLocation(String location_id, String latitude, String longitude, String position);

    /**
     * Updates the Daintree device count of a device.
     *
     * @param daintree_count the Daintree count to set
     * @param device_id the device identifier
     */
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Device d SET d.daintree_count = ?1 WHERE d.id = ?2")
    void updateDeviceDaintreeDevicesCount(Integer daintree_count, String device_id);

    /**
     * Returns the ids of devices in alert across any sensor type for the given docker.
     *
     * @param dockername the docker name, or {@code all} for all dockers
     * @return the matching device ids
     */
    @Transactional
    @Query("SELECT d.id FROM Device d WHERE ('all' = ?1 OR d.docker.name = ?1) AND (d.bacnet_status = 'alert' OR d.disruptive_status = 'alert' OR d.lorawan_status = 'alert' OR d.my_devices_status = 'alert' OR d.monnit_status = 'alert' OR d.pelican_status = 'alert' OR d.knx_status = 'alert' OR d.measuring_instrument_status = 'alert' OR d.daintree_status = 'alert' OR d.modbus_status = 'alert')")
    List<String> listDevicesByAlertStatus(String dockername);

    /**
     * Updates the Daintree status of a device.
     *
     * @param device_id the device identifier
     * @param daintree_status the Daintree status to set
     */
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Device d SET d.daintree_status = ?2 WHERE d.id = ?1")
    void updateDeviceDaintreeStatus(String device_id, String daintree_status);

    /**
     * Clears the model, user-supplied model and vendor, and product id for all devices in the given docker.
     *
     * @param docker_name the docker name
     */
    @Transactional
    @Modifying
    // NOT CONVERTED — stays native (PG-translation track): product_id column has no @Column field on Device entity; also docker_name is FK column of @ManyToOne Docker
    @Query(value = "UPDATE device SET model = NULL, user_data_model = NULL, user_data_vendor = NULL, product_id = NULL WHERE docker_name = ?1", nativeQuery = true)
    void modelResetbyDockerName(String docker_name);

    /**
     * Returns detailed alert information for the given device.
     *
     * @param device_id the device identifier
     * @return the matching device alert projection
     */
    // NOT CONVERTED — stays native (PG-translation track): multi-join DeviceDTO projection (named-query delegation to Device.java @NamedNativeQuery)
    @Query(nativeQuery = true)
    DeviceAlertDTO getDeviceAlertInfoById(String device_id);

    /**
     * Updates the QR-code tag count of a device.
     *
     * @param qrcode_count the QR-code count to set
     * @param device_id the device identifier
     */
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Device d SET d.qrcode_count = ?1 WHERE d.id = ?2")
    void updateDeviceQrcodeCount(Integer qrcode_count, String device_id);

    /**
     * Returns the room status for the given device.
     *
     * @param deviceid the device identifier
     * @return the matching room status projection
     */
    // NOT CONVERTED — stays native (PG-translation track): multi-join DeviceDTO projection (named-query delegation to Device.java @NamedNativeQuery)
    @Query(nativeQuery = true)
    RoomStatusDTO getRoomStatusByDeviceId(String deviceid);

    /**
     * Returns the room status by space for the given location.
     *
     * @param locationid the location identifier
     * @return the matching room status projections
     */
    // NOT CONVERTED — stays native (PG-translation track): multi-join DeviceDTO projection (named-query delegation to Device.java @NamedNativeQuery)
    @Query(nativeQuery = true)
    List<DeviceMonitorSpaceDTO> getRoomStatusByLocationId(String locationid);


    /**
     * Updates the asset image URL of a device.
     *
     * @param id the device identifier
     * @param asset_image_url the asset image URL to set
     */
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE Device d SET d.asset_image_url = ?2 WHERE d.id = ?1")
    void updateAssetImage(String id, String asset_image_url);

    /**
     * Returns the asset image URLs for the given device.
     *
     * @param device_id the device identifier
     * @return the asset image URLs
     */
    @Query("SELECT d.asset_image_url FROM Device d WHERE d.id = ?1")
    String getAssetImageUrls(String device_id);

    /**
     * Returns a page of devices for the checklist view filtered by docker, type, checklist, and tag associations.
     *
     * @return the matching device projections
     */
    // NOT CONVERTED — stays native (PG-translation track): multi-join DeviceDTO projection (named-query delegation to Device.java @NamedNativeQuery)
    @Query(nativeQuery = true)
    Set<DeviceDTO> getAllChecklistDevicesPagination(String searchkey, Integer pagesize, Integer offset, JSONArray dockernames, JSONArray types, JSONArray global_checklist_ids, JSONArray virtual_device_types, Boolean isTaggedToQrCode, JSONArray deviceIdsTaggedToQrCode, Boolean isTaggedToNfc, JSONArray deviceIdsTaggedToNfc, Boolean isTaggedToBarCode, JSONArray deviceIdsTaggedToClientBarCode);

    /**
     * Returns a page of devices for the inspection view filtered by docker, type, checklist, record, and tag associations.
     *
     * @return the matching device projections
     */
    // NOT CONVERTED — stays native (PG-translation track): multi-join DeviceDTO projection (named-query delegation to Device.java @NamedNativeQuery)
    @Query(nativeQuery = true)
    Set<DeviceDTO> getAllInspectionDevicesPagination(String searchkey, Integer pagesize, Integer offset, JSONArray dockernames, JSONArray types, JSONArray global_checklist_ids, String global_inspection_record_id, JSONArray virtual_device_types, Boolean isTaggedToQrCode, JSONArray deviceIdsTaggedToQrCode, Boolean isTaggedToNfc, JSONArray deviceIdsTaggedToNfc, Boolean isTaggedToBarCode, JSONArray deviceIdsTaggedToClientBarCode);

    /**
     * Returns a page of devices for the QR-code view filtered by docker, type, and tag associations.
     *
     * @return the matching device projections
     */
    // NOT CONVERTED — stays native (PG-translation track): multi-join DeviceDTO projection (named-query delegation to Device.java @NamedNativeQuery)
    @Query(nativeQuery = true)
    Set<DeviceDTO> getAllQrcodeDevicesPagination(String searchkey, Integer pagesize, Integer offset, JSONArray dockernames, JSONArray types, JSONArray virtual_device_types, Boolean isTaggedToQrCode, JSONArray deviceIdsTaggedToQrCode, Boolean isTaggedToNfc, JSONArray deviceIdsTaggedToNfc, Boolean isTaggedToBarCode, JSONArray deviceIdsTaggedToClientBarCode);

    /**
     * Returns a page of network parent devices filtered by docker, type, and tag associations.
     *
     * @return the matching device projections
     */
    // NOT CONVERTED — stays native (PG-translation track): multi-join DeviceDTO projection (named-query delegation to Device.java @NamedNativeQuery)
    @Query(nativeQuery = true)
    Set<DeviceDTO> getAllNetworkParentDeviceByPagination(JSONArray dockernames, JSONArray types, String searchkey, Integer pagesize, Integer offset, JSONArray virtual_device_types, Boolean isTaggedToQrCode, JSONArray deviceIdsTaggedToQrCode, Boolean isTaggedToNfc, JSONArray deviceIdsTaggedToNfc, Boolean isTaggedToBarCode, JSONArray deviceIdsTaggedToClientBarCode);


    /**
     * Returns condition-based alert information for the given device.
     *
     * @param device_id the device identifier
     * @return the matching device alert projection
     */
    // NOT CONVERTED — stays native (PG-translation track): multi-join DeviceDTO projection (named-query delegation to Device.java @NamedNativeQuery)
    @Query(nativeQuery = true)
    DeviceAlertDTO getDeviceConditionAlertInfoById(String device_id);

    /**
     * Returns all devices belonging to the given VDMS.
     *
     * @param vdms_id the VDMS identifier
     * @return the matching device projections
     */
    // NOT CONVERTED — stays native (PG-translation track): multi-join DeviceDTO projection (named-query delegation to Device.java @NamedNativeQuery)
    @Query(nativeQuery = true)
    Set<DeviceDTO> listAllDeviceByVdmsId(String vdms_id);

    /**
     * Updates the ecobee sensor count of a device.
     *
     * @param device_id the device identifier
     * @param ecobee_count the ecobee count to set
     */
    // update ecobee count
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE Device d SET d.ecobee_count = ?2 WHERE d.id = ?1")
    void updateDeviceEcobeeCount(String device_id, Integer ecobee_count);

    /**
     * Updates the ecobee status of a device.
     *
     * @param device_id the device identifier
     * @param ecobee_status the ecobee status to set
     */
    // update  ecobee status
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE Device d SET d.ecobee_status = ?2 WHERE d.id = ?1")
    void updateDeviceEcobeeStatus(String device_id, String ecobee_status);

    /**
     * Returns a filtered page of virtual devices.
     *
     * @return the matching device projections
     */
    // NOT CONVERTED — stays native (PG-translation track): multi-join DeviceDTO projection (named-query delegation to Device.java @NamedNativeQuery)
    @Query(nativeQuery = true)
    Set<DeviceDTO> getFilterVirtualDevicesByPagination(String searchKey, Integer pageSize, Integer offset, Set<String> dockernames, Set<String> types, Set<String> virtual_device_types);

    /**
     * Returns the device details for the given device ids.
     *
     * @param device_ids the device identifiers to match
     * @return the matching device projections
     */
    // NOT CONVERTED — stays native (PG-translation track): multi-join DeviceDTO projection (named-query delegation to Device.java @NamedNativeQuery)
    @Query(nativeQuery = true)
    List<DeviceDTO> getDeviceDetailsByDeviceIdList(Set<String> device_ids);

    /**
     * Updates the Modbus register count of a device.
     *
     * @param device_id the device identifier
     * @param modbus_count the Modbus count to set
     */
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Device d SET d.modbus_count = ?2 WHERE d.id = ?1")
    void updateDeviceModbusCount(String device_id, Integer modbus_count);

    /**
     * Updates the Modbus status of a device.
     *
     * @param device_id the device identifier
     * @param modbus_status the Modbus status to set
     */
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Device d SET d.modbus_status = ?2 WHERE d.id = ?1")
    void updateDeviceModbusStatus(String device_id, String modbus_status);

    /**
     * Returns a page of asset devices for the given location.
     *
     * @param location_id the location identifier
     * @param pagesize the maximum number of devices to return
     * @param offset the number of devices to skip
     * @return the matching device projections
     */
    // NOT CONVERTED — stays native (PG-translation track): multi-join DeviceDTO projection (named-query delegation to Device.java @NamedNativeQuery)
    @Query(nativeQuery = true)
    Set<DeviceDTO> getAssetsByLocationId(String location_id, Integer pagesize, Integer offset);

    /**
     * Updates the reboot status of a device.
     *
     * @param deviceId the device identifier
     * @param status the reboot status to set
     */
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Device d SET d.reboot_status = ?2 WHERE d.id = ?1")
    void updateDeviceRebootStatus(String deviceId, String status);

    /**
     * Returns the reboot status of the given device.
     *
     * @param vdmsid the device identifier
     * @return the reboot status
     */
    @Query("SELECT d.reboot_status FROM Device d WHERE d.id = ?1")
    String getDeviceRebootStatus(String vdmsid);


    /**
     * Returns the devices for the checklist view filtered by docker, type, checklist, and tag associations.
     *
     * @return the matching device projections
     */
    // NOT CONVERTED — stays native (PG-translation track): multi-join DeviceDTO projection (named-query delegation to Device.java @NamedNativeQuery)
    @Query(nativeQuery = true)
    Set<DeviceDTO> getAllChecklistDevices(String searchkey, JSONArray dockernames, JSONArray types, String global_checklist_id, JSONArray virtual_device_types, Boolean isTaggedToQrCode, JSONArray deviceIdsTaggedToQrCode, Boolean isTaggedToNfc, JSONArray deviceIdsTaggedToNfc);

    /**
     * Returns the devices for the inspection view filtered by docker, type, checklist, record, and tag associations.
     *
     * @return the matching device projections
     */
    // NOT CONVERTED — stays native (PG-translation track): multi-join DeviceDTO projection (named-query delegation to Device.java @NamedNativeQuery)
    @Query(nativeQuery = true)
    Set<DeviceDTO> getAllInspectionDevices(String searchkey, JSONArray dockernames, JSONArray types, String global_checklist_id, String global_inspection_record_id, JSONArray virtual_device_types, Boolean isTaggedToQrCode, JSONArray deviceIdsTaggedToQrCode, Boolean isTaggedToNfc, JSONArray deviceIdsTaggedToNfc);

    /**
     * Returns the devices for the QR-code view filtered by docker, type, and tag associations.
     *
     * @return the matching device projections
     */
    // NOT CONVERTED — stays native (PG-translation track): multi-join DeviceDTO projection (named-query delegation to Device.java @NamedNativeQuery)
    @Query(nativeQuery = true)
    Set<DeviceDTO> getAllQrcodeDevices(String searchkey, JSONArray dockernames, JSONArray types, JSONArray virtual_device_types, Boolean isTaggedToQrCode, JSONArray deviceIdsTaggedToQrCode, Boolean isTaggedToNfc, JSONArray deviceIdsTaggedToNfc);

    /**
     * Returns the network parent devices filtered by docker, type, and tag associations.
     *
     * @return the matching device projections
     */
    // NOT CONVERTED — stays native (PG-translation track): multi-join DeviceDTO projection (named-query delegation to Device.java @NamedNativeQuery)
    @Query(nativeQuery = true)
    Set<DeviceDTO> getAllNetworkParentDevices(JSONArray dockernames, JSONArray types, String searchkey, JSONArray virtual_device_types, Boolean isTaggedToQrCode, JSONArray deviceIdsTaggedToQrCode, Boolean isTaggedToNfc, JSONArray deviceIdsTaggedToNfc);

    /**
     * Returns the ids of devices matching the given docker, type, virtual type, search, tag, and location filters.
     *
     * @return the matching device ids
     */
    // NOT CONVERTED — stays native (PG-translation track): CONCAT_WS-based search, CASE WHEN virtual_device_type filters, multi-join
    // PG-port: IF->CASE WHEN
    @Query(value = "SELECT d.id FROM device d LEFT JOIN location l ON l.id = d.location_id" +
            " LEFT JOIN floor f ON l.floor_id = f.id" +
            " LEFT JOIN building b ON f.building_id = b.id" +
            " WHERE d.asset_match_status != 3 AND ('all' IN ?1 or d.docker_name IN ?1) AND" +
            " ('all' IN ?2 or d.type IN ?2)" +
            " AND ('all' IN ?4 or (CASE WHEN 'other' IN ?4 THEN d.virtual_device_type = 2 ELSE NULL END) or (CASE WHEN 'power_source' IN ?4 THEN (d.virtual_device_type = 3 or d.virtual_device_type = 4) ELSE NULL END) or (CASE WHEN 'ip' IN ?4 THEN (d.virtual_device_type IS NULL or d.virtual_device_type = 0 or d.virtual_device_type = 1) ELSE NULL END))" +
            " AND (?3 IS NULL or CONCAT_WS('', d.display_name, d.user_data_name, d.ip_address, d.mac_address, l.name, d.docker_name, d.vendor, d.user_data_vendor,d.latitude, d.longitude, d.warranty, b.name, f.name, d.model, d.user_data_model, d.serial_number, d.custom_fields,d.type) LIKE CONCAT('%',?3,'%')) " +
            " AND (?5 IS NULL OR CASE WHEN ?5 THEN d.id IN ?6 ELSE d.id NOT IN ?6 END)" +
            " AND (?7 IS NULL OR CASE WHEN ?8 THEN d.id IN ?8 ELSE d.id NOT IN ?8 END)" +
            " AND ('all' IN ?9 OR l.id IN ?9)", nativeQuery = true)
    List<String> getDeviceIds(List<String> dockerNames, List<String> types, String searchKey, List<String> virtual_device_types,
                              Boolean isTaggedToQrCode, List<String> deviceIdsTaggedToQrCode, Boolean isTaggedToNfc,
                              List<String> deviceIdsTaggedToNfc, List<String> locationIds);


    /**
     * Returns the devices matching the given filter parameters and device id list.
     *
     * @return the matching device projections
     */
    // NOT CONVERTED — stays native (PG-translation track): multi-join DeviceDTO projection (named-query delegation to Device.java @NamedNativeQuery)
    @Query(nativeQuery = true)
    List<DeviceDTO> getDevicesByFilter(List<String> dockerNames, List<String> types, String searchKey, List<String> virtual_device_types, Boolean isTaggedToQrCode, List<String> deviceIdsTaggedToQrCode, Boolean isTaggedToNfc, List<String> deviceIdsTaggedToNfc, List<String> locationIds, List<String> deviceIds);

    /**
     * Inserts a device created during asset onboarding.
     *
     * @param id the device identifier
     */
    @Modifying
    @Transactional
    // NOT CONVERTED — stays native (PG-translation track): plain INSERT
    @Query(value = "INSERT INTO device(id, location_id, model, vendor, description, docker_name, docker_vdms_id, asset_match_status, " +
            "created_timestamp, created_email, asset_group, virtual_device_type, monitor, user_data_name)" +
            "VALUES (?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8, ?9, ?10, ?11, ?12, ?13, ?14)", nativeQuery = true)
    void addAssetOnboardedDevices(String id, String location_id, String model, String vendor, String description, String docker_name, String vdmsid, Integer asset_match_status,
                                  BigInteger created_timestamp, String created_email, String asset_group, Integer virtual_device_type, Integer monitor, String user_data_name);

    /**
     * Updates the onboard status of the given devices.
     *
     * @param device_ids the device identifiers
     * @param onboard_status the onboard status to set
     * @param updated_timestamp the update timestamp
     */
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE Device d SET d.onboard_status = ?2, d.updated_timestamp = ?3 WHERE d.id IN ?1")
    void updateOnboardAssetStatus(Set<String> device_ids, Integer onboard_status, BigInteger updated_timestamp);

    /**
     * Updates the asset OCR image URL of a device.
     *
     * @param device_id the device identifier
     * @param asset_ocr_image_url the asset OCR image URL to set
     */
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE Device d SET d.asset_ocr_image_url = ?2 WHERE d.id = ?1")
    void updateAssetOcrImage(String device_id, String asset_ocr_image_url);

    /**
     * Returns the asset OCR image URLs for the given device.
     *
     * @param device_id the device identifier
     * @return the asset OCR image URLs
     */
    @Query("SELECT d.asset_ocr_image_url FROM Device d WHERE d.id = ?1")
    String getAssetOcrImageUrls(String device_id);

    /**
     * Counts monitored, asset-matched devices with the given onboard status, optionally scoped by docker.
     *
     * @param dockername the docker name, or {@code all} for all dockers
     * @param onboard_status the onboard status to match
     * @return the number of matching devices
     */
    @Transactional
    @Query("SELECT COUNT(d) FROM Device d WHERE ('all' = ?1 OR d.docker.name = ?1) AND d.monitor = 1 AND d.asset_match_status <> 3 AND d.onboard_status = ?2")
    Integer getAssetOnboardCount(String dockername, int onboard_status);

    /**
     * Inserts a device with its onboard status and monitoring flags.
     *
     * @param id the device identifier
     */
    @Modifying
    @Transactional
    // NOT CONVERTED — stays native (PG-translation track): plain INSERT
    @Query(value = "INSERT INTO device(id, docker_vdms_id, docker_name, ip_address, status, mac_address, last_seen_on, display_name, vendor, created_timestamp, user_data_name, type, description, "
            + " custom_fields, created_email,asset_group,onboard_status, monitor, virtual_device_type) VALUES(?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8, ?9,?10,?11,?12,?13,?14,?15,?16,?17,?18, ?19)", nativeQuery = true)
    void addDevice(String id, String vdms_id, String docker_name, String ip_address, Integer status,
                   String mac_address, BigInteger last_seen_on, String display_name, String vendor, BigInteger created_timestamp,
                   String user_data_name, String type, String description, String customFields, String created_email, String asset_group, Integer onboard_status, Integer monitor, Integer virtual_device_type);

    /**
     * Returns the devices located at the given location.
     *
     * @param location_id the location identifier
     * @return the matching device projections
     */
    // NOT CONVERTED — stays native (PG-translation track): multi-join DeviceDTO projection (named-query delegation to Device.java @NamedNativeQuery)
    @Query(nativeQuery = true)
    Set<DeviceDTO> getDevicesByLocationId(String location_id);

    /**
     * Returns the device with the given identifier using the newer projection.
     *
     * @param device_id the device identifier
     * @return the matching device projection
     */
    // NOT CONVERTED — stays native (PG-translation track): multi-join DeviceDTO projection (named-query delegation to Device.java @NamedNativeQuery)
    @Query(nativeQuery = true)
    DeviceDTO getDeviceByDeviceIdNew(String device_id);

    /**
     * Updates the coordinates, position, and location of a device.
     *
     * @param device_id the device identifier
     * @param latitude the latitude
     * @param longitude the longitude
     * @param position the position
     * @param location_id the location identifier
     */
    @Modifying
    @Transactional
    // NOT CONVERTED — stays native (PG-translation track): location_id is FK of @ManyToOne Location relation
    @Query(value = "UPDATE device SET latitude = ?2, longitude = ?3, position = ?4, location_id = ?5 WHERE id = ?1", nativeQuery = true)
    void updateDeviceCoordinatesAndLocationId(String device_id, String latitude, String longitude, String position, String location_id);

    /**
     * Returns all devices for the given VDMS and docker without pagination.
     *
     * @param vdmsid the VDMS identifier
     * @param dockername the docker name
     * @return the matching device projections
     */
    // NOT CONVERTED — stays native (PG-translation track): multi-join DeviceDTO projection (named-query delegation to Device.java @NamedNativeQuery)
    @Query(nativeQuery = true)
    List<DeviceDTO> getAllDeviceByVdmsIdAndDockerNameWithoutPagination(String vdmsid, String dockername);

    /**
     * Sets the digital twin image URL of a device.
     *
     * @param digital_twin_image_url the digital twin image URL to set
     * @param device_id the device identifier
     */
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE Device d SET d.digital_twin_image_url = ?1 WHERE d.id = ?2")
    void updateDigitalTwinImageUrlById(String digital_twin_image_url, String device_id);

    /**
     * Clears the digital twin image URL of a device.
     *
     * @param device_id the device identifier
     */
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE Device d SET d.digital_twin_image_url = NULL WHERE d.id = ?1")
    void deleteDigitalTwinImageUrl(String device_id);

    /**
     * Returns the digital twin image URL of the given device.
     *
     * @param device_id the device identifier
     * @return the digital twin image URL
     */
    @Query("SELECT d.digital_twin_image_url FROM Device d WHERE d.id = ?1")
    String getDigitalTwinImageUrl(String device_id);


    /**
     * Updates the Poly Lens device count of a device.
     *
     * @param deviceId the device identifier
     * @param polyLensDeviceCount the Poly Lens device count to set
     */
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE Device d SET d.poly_lens_count = ?2 WHERE d.id = ?1")
    void updatePolyLensDeviceCount(String deviceId, Integer polyLensDeviceCount);


    /**
     * Updates the MQTT device count of a device.
     *
     * @param deviceId the device identifier
     * @param mqttDeviceCount the MQTT device count to set
     */
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE Device d SET d.mqtt_count = ?2 WHERE d.id = ?1")
    void updateMqttDeviceDeviceCount(String deviceId, Integer mqttDeviceCount);


    /**
     * Returns a page of devices for the record-checklist view filtered by docker, type, checklist, record, and tag associations.
     *
     * @return the matching device projections
     */
    // NOT CONVERTED — stays native (PG-translation track): multi-join DeviceDTO projection (named-query delegation to Device.java @NamedNativeQuery)
    @Query(nativeQuery = true)
    Set<DeviceDTO> getAllRecordChecklistDevicesPagination(String searchkey, Integer pagesize, Integer offset, JSONArray dockernames, JSONArray types, JSONArray global_checklist_ids, String inspection_record_id, JSONArray virtual_device_types, Boolean isTaggedToQrCode, JSONArray deviceIdsTaggedToQrCode, Boolean isTaggedToNfc, JSONArray deviceIdsTaggedToNfc, Boolean isTaggedToBarCode, JSONArray deviceIdsTaggedToClientBarCode);

    /**
     * Returns the device details for the given device ids.
     *
     * @param device_ids the device identifiers to match
     * @return the matching device projections
     */
    // NOT CONVERTED — stays native (PG-translation track): multi-join DeviceDTO projection (named-query delegation to Device.java @NamedNativeQuery)
    @Query(nativeQuery = true)
    Set<DeviceDTO> getDeviceDetailsByIdList(Set<String> device_ids);

    /**
     * Counts all devices.
     *
     * @return the total number of devices
     */
    @Query("SELECT COUNT(d) FROM Device d")
    Integer getAllDeviceCount();


    /**
     * Returns the ids of devices that have a digital twin image URL.
     *
     * @return the matching device ids
     */
    @Query("SELECT d.id FROM Device d WHERE d.digital_twin_image_url IS NOT NULL")
    List<String> getDeviceIdsWithDigitalTwinImageUrl();


    /**
     * Returns the distinct device types across all devices.
     *
     * @return the distinct device types
     */
    @Transactional
    @Query("SELECT DISTINCT d.type FROM Device d")
    List<String> getAllUniqueDeviceTypes();

    /**
     * Updates the type of devices whose type starts with the given prefix.
     *
     * @param type the new type to set
     * @param idPrefix the type prefix to match
     */
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE Device d SET d.type = ?1 WHERE d.type LIKE CONCAT(?2, '%')")
    void updateTypeByType(String type, String idPrefix);

    /**
     * Sets the type to {@code generic} for devices with no type.
     *
     * @return the number of rows affected
     */
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE Device d SET d.type = 'generic' WHERE d.type IS NULL")
    Integer setTypeGeneric();

    /**
     * Updates devices of the given type to a new type.
     *
     * @param type the existing type to match
     * @param updateType the new type to set
     */
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE Device d SET d.type = ?2 WHERE d.type = ?1")
    void updateDeviceType(String type, String updateType);

    /**
     * Returns the device associated with the given measuring instrument.
     *
     * @param measuringInstrumentId the measuring instrument identifier
     * @return the matching device projection
     */
    // NOT CONVERTED — stays native (PG-translation track): multi-join DeviceDTO projection (named-query delegation to Device.java @NamedNativeQuery)
    @Query(nativeQuery = true)
    DeviceDTO getDeviceByMeasuringInstrumentId(String measuringInstrumentId);


    /**
     * Returns a page of AI-call-flow devices for the given VDMS and docker matching the search key.
     *
     * @param vdmsid the VDMS identifier
     * @param dockername the docker name
     * @param offset the number of devices to skip
     * @param pagesize the maximum number of devices to return
     * @param sanitizedSearchKey the sanitized search filter term
     * @return the matching device projections
     */
    // NOT CONVERTED — stays native (PG-translation track): multi-join DeviceDTO projection (named-query delegation to Device.java @NamedNativeQuery)
    @Query(nativeQuery = true)
    List<DeviceDTO> browseAiCallFlowDevicesWithSearch(String vdmsid, String dockername, Integer offset, Integer pagesize, String sanitizedSearchKey);


    /**
     * Toggles the do-not-disturb flag of a device.
     *
     * @param device_id the device identifier
     * @param is_dnd_enabled the do-not-disturb flag to set
     */
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE Device d SET d.is_dnd_enabled = ?2 WHERE d.id = ?1")
    void toggleDndStatus(String device_id, Boolean is_dnd_enabled);

    /**
     * Returns the resolved name of a device, falling back to the display name when no user-supplied name exists.
     *
     * @param deviceId the device identifier
     * @return the resolved device name
     */
    // PG-port: IF->CASE WHEN
    @Query("SELECT CASE WHEN (d.user_data_name IS NULL OR d.user_data_name = '') THEN d.display_name ELSE d.user_data_name END FROM Device d WHERE d.id = ?1")
    String getDeviceNameById(String deviceId);

    /**
     * Returns the device with the given identifier.
     *
     * @param deviceId the device identifier
     * @return the matching device projection
     */
    // NOT CONVERTED — stays native (PG-translation track): multi-join DeviceDTO projection (named-query delegation to Device.java @NamedNativeQuery)
    @Query(nativeQuery = true)
    DeviceDTO getDeviceById(String deviceId);

    /**
     * Inserts a virtual device imported via HAM asset import.
     *
     * @param final_device_id the device identifier
     */
    // HAM Asset import //
    @Modifying
    @Transactional
    // NOT CONVERTED — stays native (PG-translation track): plain INSERT
    @Query(value = "INSERT INTO device(id,ip_address,mac_address,user_data_name,user_data_model,user_data_vendor,type,"
            + "location_id,network_layer,parent,snmp_parent,monitor,docker_name,docker_vdms_id,last_seen_on,warranty,status,"
            + "email_alert, sms_alert, popup_notification, virtual_device_type, serial_number, local_vendor_email_alert,local_vendor_sms_alert, "
            + "subsystem_parent_id, description, asset_match_status, created_timestamp, created_email,asset_group, category, sub_category, cost_value, assigned_user_email, ai_call, cost_unit, is_dnd_enabled, custom_fields)"
            + "VALUES(?1,?2,?3,?4,?5,?6,?7,?8,?9,?10,?11,?12,?13,?14,?15,?16,?17,?18,?19,?20,?21,?22, ?23, ?24, ?25, ?26, ?27, ?28,?29,?30, ?31, ?32, ?33, ?34, ?35, ?36, ?37, ?38)", nativeQuery = true)
    void moveVirtualDevice(String final_device_id, String ip_address, String mac_address, String user_data_name,
                           String user_data_model, String user_data_vendor, String type, String location_id, String network_layer,
                           String parent, String snmp_parent, Integer monitor, String docker_name, String vdms_id, String last_seen_on,
                           String warranty, Integer status, Integer email_alert, Integer sms_alert, Integer popup_notification,
                           Integer virtual_device_type, String serial_number, Integer local_vendor_email_alert, Integer local_vendor_sms_alert,
                           String subsystem_parent_id, String description, Integer asset_match_status, BigInteger created_timestamp, String created_email, String asset_group, String category, String sub_category,
                           BigDecimal cost_value, String assigned_user_email, Boolean ai_call, String cost_unit, Boolean is_dnd_enabled, String custom_fields);


    /**
     * Updates the operational status of a device.
     *
     * @param device_id the device identifier
     * @param operationalStatus the operational status to set
     */
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Device d SET d.operational_status = ?2 WHERE d.id = ?1")
    void updateDeviceOperationalStatus(String device_id, String operationalStatus);

    /**
     * Updates the assigned user email of a device.
     *
     * @param deviceId the device identifier
     * @param assignedUserEmail the assigned user email to set
     */
    @Transactional
    @Modifying
    // NOT CONVERTED — stays native (PG-translation track): assigned_user_email is @JoinColumn FK of @ManyToOne User relation; JPQL cannot SET a relation from a bare email string
    @Query(value = "UPDATE device SET assigned_user_email = ?2 WHERE id = ?1", nativeQuery = true)
    void updateAssignedUserEmail(String deviceId, String assignedUserEmail);


    /**
     * Returns device information for the given device.
     *
     * @param deviceId the device identifier
     * @return the matching device projection
     */
    // NOT CONVERTED — stays native (PG-translation track): multi-join DeviceDTO projection (named-query delegation to Device.java @NamedNativeQuery)
    @Query(nativeQuery = true)
    DeviceDTO getDeviceInfoFromDb(String deviceId);


    /**
     * Inserts a virtual device created from an inventory record.
     *
     * @param finalDeviceId the device identifier
     */
    @Modifying
    @Transactional
    // NOT CONVERTED — stays native (PG-translation track): plain INSERT
    @Query(value = "INSERT INTO device(id, user_data_name, monitor, docker_name, docker_vdms_id, warranty, virtual_device_type, serial_number, " +
            " asset_match_status,created_timestamp, created_email,asset_group,assigned_user_email,mac_address, ip_address, status,last_seen_on, user_data_model, subsystem_parent_id) VALUES(?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8, ?9,?10,?11,?12,?13,?14,?15, ?16,?17, ?18, ?19)", nativeQuery = true)
    void addVirtualDeviceFromInventory(String finalDeviceId, String userDataName, Integer monitor, String dockerName,
                                       String vdmsId, String warranty, Integer virtualDeviceType, String serialNumber,
                                       Integer assetMatchStatus, BigInteger createdTimestamp, String email, String assetGroup,
                                       String assignedUserEmail, String macAddress, String ipAddress, Integer status, String lastSeenOn, String userDataModel, String subsystemParentId);

    /**
     * Tags a device to an inventory tracking record.
     *
     * @param deviceId the device identifier
     * @param inventoryTrackingId the inventory tracking identifier
     */
    @Modifying
    @Transactional
    // NOT CONVERTED — stays native (PG-translation track): plain INSERT
    @Query(value = "INSERT INTO inventory_device(device_id, tracking_id) VALUES(?1, ?2)", nativeQuery = true)
    void tagInventoryDevices(String deviceId, String inventoryTrackingId);

    /**
     * Returns the first device matching the given serial number.
     *
     * @param serialNumber the serial number
     * @return the matching device, or {@code null} if none exists
     */
    // NOT CONVERTED — stays native (PG-translation track): full Device entity load would drag in huge eager graph (docker, location, global_qrcode, device_onboard_status, etc.) via SELECT *; LIMIT has no JPQL equivalent
    @Query(value = "SELECT * FROM device WHERE serial_number = ?1 LIMIT 1", nativeQuery = true)
    Device findDeviceIdBySerialNumber(String serialNumber);

    /**
     * Returns the distinct docker names with AI-call enabled for the given VDMS, optionally filtered by search key.
     *
     * @param vdmsid the VDMS identifier
     * @param sanitizedSearchKey the sanitized search filter term, or {@code null} for no filter
     * @return the matching docker names
     */
    // NOT CONVERTED — stays native (PG-translation track): REGEXP_REPLACE-based search normalization
    @Query(value = "SELECT DISTINCT docker_name FROM device d WHERE d.docker_vdms_id = ?1 AND d.ai_call = true AND d.docker_name IS NOT NULL AND (?2 = 'null'  or LOWER(REGEXP_REPLACE(CONCAT_WS('', d.docker_name), '[ -.!\t_+#~`@$%^&*()=;:<>?,/{}|\\\\ ]' , '')) LIKE CONCAT('%',?2,'%'))", nativeQuery = true)
    Set<String> listAiEnabledDockers(String vdmsid, String sanitizedSearchKey);


    /**
     * Returns whether AI-call is enabled for the given device.
     *
     * @param id the device identifier
     * @return the AI-call enabled flag
     */
    @Query("SELECT d.ai_call FROM Device d WHERE d.id = ?1")
    Boolean checkAiCallEnableStatus(String id);

    /**
     * Returns whether do-not-disturb is enabled for the given device.
     *
     * @param id the device identifier
     * @return the do-not-disturb flag
     */
    @Query("SELECT d.is_dnd_enabled FROM Device d WHERE d.id = ?1")
    Boolean checkAiCallDndEnabled(String id);

    /**
     * Enables the do-not-disturb flag for the given device.
     *
     * @param id the device identifier
     */
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE Device d SET d.is_dnd_enabled = true WHERE d.id = ?1")
    void updateDeviceDndEnabledStatus(String id);

    /**
     * Sets the do-not-disturb timestamp of the given device.
     *
     * @param id the device identifier
     * @param dndTimestamp the do-not-disturb timestamp to set
     */
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE Device d SET d.dnd_timestamp = ?2 WHERE d.id = ?1")
    void updateDeviceDndTimestamp(String id, BigInteger dndTimestamp);

    /**
     * Returns the AI-call and status information for the given device.
     *
     * @param deviceId the device identifier
     * @return the matching device projections
     */
    // NOT CONVERTED — stays native (PG-translation track): multi-join DeviceDTO projection (named-query delegation to Device.java @NamedNativeQuery)
    @Query(nativeQuery = true)
    List<DeviceDTO> getAiCallAndDeviceStatus(String deviceId);

    /**
     * Returns the devices matching the given do-not-disturb flag.
     *
     * @param b the do-not-disturb flag to match
     * @return the matching device projections
     */
    // NOT CONVERTED — stays native (PG-translation track): multi-join DeviceDTO projection (named-query delegation to Device.java @NamedNativeQuery)
    @Query(nativeQuery = true)
    List<DeviceDTO> getDndDevices(boolean b);

    /**
     * Updates the do-not-disturb flag of a device.
     *
     * @param id the device identifier
     * @param b the do-not-disturb flag to set
     */
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE Device d SET d.is_dnd_enabled = ?2 WHERE d.id = ?1")
    void updateDndStatus(String id, boolean b);

    /**
     * Enables the system-level do-not-disturb flag for the given device.
     *
     * @param id the device identifier
     */
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE Device d SET d.system_dnd_enabled = true WHERE d.id = ?1")
    void updateSystemDndEnabled(String id);

    /**
     * Updates the system-level do-not-disturb flag of a device.
     *
     * @param id the device identifier
     * @param b the system do-not-disturb flag to set
     */
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE Device d SET d.system_dnd_enabled = ?2 WHERE d.id = ?1")
    void updateSystemDndDisabled(String id, boolean b);

    /**
     * Returns the device and system do-not-disturb status for the given device.
     *
     * @param deviceId the device identifier
     * @param isDndEnabled the do-not-disturb flag to match
     * @return the matching device projection
     */
    // NOT CONVERTED — stays native (PG-translation track): multi-join DeviceDTO projection (named-query delegation to Device.java @NamedNativeQuery)
    @Query(nativeQuery = true)
    DeviceDTO getDeviceDndAndSystemDndStatus(String deviceId, Boolean isDndEnabled);

    /**
     * Returns the resolved model of a device, falling back to the model when no user-supplied model exists.
     *
     * @param deviceId the device identifier
     * @return the resolved model
     */
    // PG-port: IF->CASE WHEN
    @Query("SELECT CASE WHEN (d.user_data_model IS NULL OR d.user_data_model = '') THEN d.model ELSE d.user_data_model END FROM Device d WHERE d.id = ?1")
    String getModelById(String deviceId);

    /**
     * Returns the devices with the given status.
     *
     * @param status the device status
     * @return the matching devices
     */
    List<Device> findByStatus(int status);

    /**
     * Returns the device matching the given user-supplied name and subsystem parent.
     *
     * @param displayName the user-supplied device name
     * @param parentId the subsystem parent identifier
     * @return the matching device, if present
     */
    // NOT CONVERTED — stays native (PG-translation track): SELECT * loads full Device entity eager graph; use derived method or scalar projection instead
    @Query(value = "SELECT * FROM device WHERE user_data_name = ?1 AND subsystem_parent_id = ?2", nativeQuery = true)
    Optional<Device> findByDisplayNameAndSubsystemParentId(String displayName, String parentId);


    /**
     * Returns a page of device custom details, excluding the given device ids.
     *
     * @return the matching device projections
     */
    // NOT CONVERTED — stays native (PG-translation track): multi-join DeviceDTO projection (named-query delegation to Device.java @NamedNativeQuery)
    @Query(nativeQuery = true)
    List<DeviceDTO> getAllDeviceCustomDetailsPaginated(String searchKey, Integer pageSize, Integer offset, List<String> excludeDeviceIds, String dockerName);

    /**
     * Returns the device custom details, excluding the given device ids.
     *
     * @return the matching device projections
     */
    // NOT CONVERTED — stays native (PG-translation track): multi-join DeviceDTO projection (named-query delegation to Device.java @NamedNativeQuery)
    @Query(nativeQuery = true)
    List<DeviceDTO> getAllDeviceCustomDetails(String searchKey, List<String> excludeDeviceIds, String dockerName);

    /**
     * Returns a page of device custom details for the given device ids.
     *
     * @return the matching device projections
     */
    // NOT CONVERTED — stays native (PG-translation track): multi-join DeviceDTO projection (named-query delegation to Device.java @NamedNativeQuery)
    @Query(nativeQuery = true)
    List<DeviceDTO> getDeviceCustomDetailsPaginated(String searchKey, Integer pageSize, Integer offset, List<String> deviceIds);

    /**
     * Returns the device custom details for the given device ids.
     *
     * @return the matching device projections
     */
    // NOT CONVERTED — stays native (PG-translation track): multi-join DeviceDTO projection (named-query delegation to Device.java @NamedNativeQuery)
    @Query(nativeQuery = true)
    List<DeviceDTO> getDeviceCustomDetails(String searchKey, List<String> deviceIds);

    /**
     * Returns the device custom details for the given device ids, models, and asset categories.
     *
     * @return the matching device projections
     */
    // NOT CONVERTED — stays native (PG-translation track): multi-join DeviceDTO projection (named-query delegation to Device.java @NamedNativeQuery)
    @Query(nativeQuery = true)
    List<DeviceDTO> getDeviceCustomDetailsByIds(String searchKey, List<String> deviceIds, List<String> models, List<String> assetCategory);

    /**
     * Returns a page of device custom details for the given device ids, models, and asset categories.
     *
     * @return the matching device projections
     */
    // NOT CONVERTED — stays native (PG-translation track): multi-join DeviceDTO projection (named-query delegation to Device.java @NamedNativeQuery)
    @Query(nativeQuery = true)
    List<DeviceDTO> getDeviceCustomDetailsByIdsPaginated(String searchKey, Integer pageSize, Integer offset, List<String> deviceIds, List<String> models, List<String> assetCategory);


    /**
     * Returns the ids of all physical devices matching the search key, optionally scoped by docker.
     *
     * @param searchKey the search filter term
     * @param dockerName the docker name, or {@code all} for all dockers
     * @return the matching device ids
     */
    // NOT CONVERTED — stays native (PG-translation track): CONCAT_WS-based search normalization
    @Query(value = "SELECT d.id " +
            "FROM device d " +
            "WHERE ( ?2 = 'all' OR d.docker_name = ?2 ) " +
            "AND (d.virtual_device_type IS NULL OR d.virtual_device_type = 0) " +
            "AND CONCAT_WS('', d.display_name, d.user_data_name) LIKE CONCAT('%', ?1, '%') " +
            "ORDER BY d.id",
            nativeQuery = true)
    List<String> getAllDeviceIdsSearchKeySelectAll(String searchKey, String dockerName);


    /**
     * Returns the ids of all physical devices, optionally scoped by docker.
     *
     * @param dockerName the docker name, or {@code all} for all dockers
     * @return the matching device ids
     */
    @Query("SELECT d.id FROM Device d WHERE ('all' = ?1 OR d.docker.name = ?1) AND (d.virtual_device_type IS NULL OR d.virtual_device_type = 0)")
    List<String> getAllDeviceIdsSelectAll(String dockerName);


    /**
     * Returns a page of physical device ids matching the search key, optionally scoped by docker.
     *
     * @param searchKey the search filter term
     * @param pageSize the maximum number of ids to return
     * @param offset the number of ids to skip
     * @param dockerName the docker name, or {@code all} for all dockers
     * @return the matching device ids
     */
    // NOT CONVERTED — stays native (PG-translation track): CONCAT_WS search + LIMIT/OFFSET
    @Query(value = "SELECT d.id " +
            "FROM device d " +
            "WHERE ( ?4 = 'all' OR d.docker_name = ?4 ) " +
            "AND (d.virtual_device_type IS NULL OR d.virtual_device_type = 0) " +
            "AND CONCAT_WS('', d.display_name, d.user_data_name) LIKE CONCAT('%', ?1, '%') " +
            "ORDER BY d.id " +
            "LIMIT ?2 OFFSET ?3",
            nativeQuery = true)
    List<String> getAllDeviceIdsSearchKeyPaginated(String searchKey, Integer pageSize, Integer offset, String dockerName);


    /**
     * Returns a page of physical device ids, optionally scoped by docker.
     *
     * @param pageSize the maximum number of ids to return
     * @param offset the number of ids to skip
     * @param dockerName the docker name, or {@code all} for all dockers
     * @return the matching device ids
     */
    // NOT CONVERTED — stays native (PG-translation track): LIMIT/OFFSET requires Pageable in JPQL; signature change would break callers
    @Query(value = "SELECT d.id " +
            "FROM device d " +
            "WHERE ( ?3 = 'all' OR d.docker_name = ?3 ) " +
            "AND (d.virtual_device_type IS NULL OR d.virtual_device_type = 0) " +
            "ORDER BY d.id " +
            "LIMIT ?1 OFFSET ?2",
            nativeQuery = true)
    List<String> getAllDeviceIdsPaginated(Integer pageSize, Integer offset, String dockerName);

    /**
     * Returns a page of devices for the bar-code view filtered by docker, type, and tag associations.
     *
     * @return the matching device projections
     */
    // NOT CONVERTED — stays native (PG-translation track): multi-join DeviceDTO projection (named-query delegation to Device.java @NamedNativeQuery)
    @Query(nativeQuery = true)
    Set<DeviceDTO> getAllBarCodeDevicesPagination(String searchkey, Integer pagesize, Integer offset, JSONArray dockernames, JSONArray types, JSONArray virtualDeviceTypes, Boolean isTaggedToQrCode, JSONArray deviceIdsTaggedToQrCode, Boolean isTaggedToNfc, JSONArray deviceIdsTaggedToNfc, Boolean isTaggedToBarCode, JSONArray deviceIdsTaggedToClientBarCode);

    /**
     * Returns the image references for the given device.
     *
     * @param deviceId the device identifier
     * @return the matching device projection
     */
    // NOT CONVERTED — stays native (PG-translation track): multi-join DeviceDTO projection (named-query delegation to Device.java @NamedNativeQuery)
    @Query(nativeQuery = true)
    DeviceDTO getAllDeviceImages(String deviceId);

    /**
     * Updates the asset tag images URL of a device.
     *
     * @param id the device identifier
     * @param jsonString the asset tag images URL JSON to set
     */
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE Device d SET d.asset_tag_images_url = ?2 WHERE d.id = ?1")
    void updateAssetTagImages(String id, String jsonString);

    /**
     * DB-per-service helper: returns (id, product_id) pairs for the given device ids,
     * used to map device -> product for InventoryClient enrichment of image URLs.
     */
    // NOT CONVERTED — stays native (PG-translation track): product_id column has no @Column field on Device entity
    @Query(value = "SELECT id, product_id FROM device WHERE id IN (:ids)", nativeQuery = true)
    List<Object[]> findDeviceProductIdRows(@Param("ids") Set<String> ids);
}


