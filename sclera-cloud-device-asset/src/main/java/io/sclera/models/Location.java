package io.sclera.models;

import java.math.BigInteger;
import java.util.Set;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import io.sclera.dto.LocationAlertDTO;
import io.sclera.dto.LocationDTO;
import io.sclera.dto.touchscreen.DeviceMonitorSpaceDTO;
// removed: Bucket-D import io.sclera.integration.dto.LocationIntegrationDTO
import org.hibernate.annotations.ColumnDefault;

@Entity
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = Location.class)
@Table(indexes = {
        @Index(name = "idx_location_zindex_id", columnList = "z_index, id")
})

@SqlResultSetMapping(
        name = "spacestatusmapping",
        classes = {
                @ConstructorResult(
                        targetClass = DeviceMonitorSpaceDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = String.class),
                                @ColumnResult(name = "spacename", type = String.class),
                                @ColumnResult(name = "status", type = Integer.class),
                                @ColumnResult(name = "position", type = String.class),
                                @ColumnResult(name = "initial_position", type = String.class),
                                @ColumnResult(name = "image_url", type = String.class),
                                @ColumnResult(name = "sensorstatus", type = Integer.class),
                                @ColumnResult(name = "angle", type = String.class),
                                @ColumnResult(name = "area", type = String.class)
                        }
                )

        }
)

@NamedNativeQuery(
        name = "Location.listAllSpaceStatus",
        query = "SELECT "
                + " CASE"
                + " WHEN 'buildings' = ?1 THEN b.name "
                + " WHEN 'floors' = ?1 THEN f.name "
                + " WHEN 'locations' = ?1 THEN l.name "
                + " END AS 'spacename' ,"
                + " CASE"
                + " WHEN 'buildings' = ?1 THEN b.id "
                + " WHEN 'floors' = ?1 THEN f.id "
                + " WHEN 'locations' = ?1 THEN l.id "
                + " END AS 'id' ,"
                + " CASE"
                + " WHEN 'locations' = ?1 THEN l.position "
                + " END AS 'position' ,"
                + " CASE"
                + " WHEN 'locations' = ?1 THEN l.area "
                + " END AS 'area' ,"
                + " CASE"
                + " WHEN 'floors' = ?1 THEN f.image_url "
                + " END AS 'image_url' ,"
                + " CASE"
                + " WHEN 'floors' = ?1 THEN f.initial_position "
                + " END AS 'initial_position' ,"
                + " CASE"
                + " WHEN 'floors' = ?1 THEN f.angle "
                + " END AS 'angle' ,"
                + " d.status,"
                + " IF(d.bacnet_status = 'alert' OR d.lorawan_status = 'alert' OR d.disruptive_status = 'alert' OR d.my_devices_status = 'alert' OR"
                + " d.monnit_status = 'alert' OR d.pelican_status = 'alert' OR d.knx_status = 'alert' OR d.snmp_object_status = 'alert' OR d.measuring_instrument_status = 'alert', 1, IF(d.bacnet_count > 0 OR d.lorawan_count > 0  OR d.disruptive_count > 0  OR"
                + " d.my_devices_count > 0  OR d.monnit_count > 0  OR d.pelican_count > 0 OR d.knx_count > 0 OR d.snmp_object_count > 0 OR d.measuring_instrument_count > 0, 0, NULL)) as sensorstatus"
                + " FROM location l"
                + " JOIN device d on d.location_id = l.id && d.monitor = 1 "
                + " JOIN floor f on l.floor_id = f.id "
                + " JOIN building b on f.building_id = b.id "
                + " WHERE ( 'null' = ?2 or b.id = ?2) AND ( 'null' = ?3 or f.id = ?3)",
        resultSetMapping = "spacestatusmapping"
)

@SqlResultSetMapping(
        name = "locationdetailsmapping",
        classes = {
                @ConstructorResult(
                        targetClass = LocationDTO.class,
                        columns = {
                                @ColumnResult(name = "location_id", type = String.class),
                                @ColumnResult(name = "name", type = String.class),
                                @ColumnResult(name = "floor_name", type = String.class),
                                @ColumnResult(name = "building_name", type = String.class),
                                @ColumnResult(name = "type", type = String.class),
                                @ColumnResult(name = "code", type = String.class)
                        }
                )
        }
)


@NamedNativeQuery(
        name = "Location.getLocationDetails",
        query = "SELECT  l.id as location_id, l.name ,b.name as building_name, f.name as floor_name, l.type, l.code"
                + " FROM location l "
                + " LEFT JOIN floor f ON l.floor_id = f.id"
                + " LEFT JOIN building b ON f.building_id = b.id"
                + " WHERE l.id = ?1",
        resultSetMapping = "locationdetailsmapping"
)

@NamedNativeQuery(
        name = "Location.getAllLocationPagination",
        query = "SELECT l.id AS location_id ,l.name, b.name as building_name, f.name as floor_name, l.type, l.code "
                + " FROM location l "
                + " LEFT JOIN floor f ON l.floor_id = f.id "
                + " LEFT JOIN building b ON f.building_id = b.id "
                + " WHERE ('all' = ?10 OR f.building_id = ?10) AND ('all' = ?4 OR l.floor_id = ?4) AND (?1 = 'null' or  LOWER(REGEXP_REPLACE(CONCAT_WS('' , l.name ), '[ -.!\t_+#~`@$%^&*()=;:<>?,/{}|\\\\ ]' , '')) LIKE CONCAT('%' ,?1, '%'))"
                + " AND (?5 IS NULL OR IF(?5, l.id IN ?6, l.id NOT IN ?6))"
                + " AND (?7 IS NULL OR IF(?7, l.id IN ?8, l.id NOT IN ?8))"
                + " AND ('all' IN ?9 OR l.type IN ?9)"
                + " LIMIT ?2 OFFSET ?3",
        resultSetMapping = "locationdetailsmapping"
)

/************************************************************************** new location changes *********************************************************/

@SqlResultSetMapping(
        name = "locationmapping",
        classes = {
                @ConstructorResult(
                        targetClass = LocationDTO.class,
                        columns = {
                                @ColumnResult(name = "location_id", type = String.class),
                                @ColumnResult(name = "name", type = String.class),
                                @ColumnResult(name = "position", type = String.class),
                                @ColumnResult(name = "area", type = String.class),
                                @ColumnResult(name = "floor_id", type = String.class),
                                @ColumnResult(name = "status", type = String.class),
                                @ColumnResult(name = "z_index", type = Integer.class),
                                @ColumnResult(name = "record_checklist_count", type = Integer.class),
                                @ColumnResult(name = "record_checklist_status", type = String.class),
                                @ColumnResult(name = "type", type = String.class),
                                @ColumnResult(name = "floor_name", type = String.class),
                                @ColumnResult(name = "building_id", type = String.class),
                                @ColumnResult(name = "building_name", type = String.class),
                                @ColumnResult(name = "code", type = String.class),
                                @ColumnResult(name = "building_code", type = String.class)
                        }
                )
        }
)

@NamedNativeQuery(
        name = "Location.getLocationsByFloorId",
        query = "SELECT l.id AS location_id ,l.name ,l.position, l.area ,l.floor_id, l.z_index, l.status, l.record_checklist_count, l.record_checklist_status, l.type, l.code, f.name as floor_name, b.id as building_id, b.name as building_name, b.code as building_code "
                + " FROM location l WHERE l.floor_id = ?1",
        resultSetMapping = "locationmapping"
)

@NamedNativeQuery(
        name = "Location.getLocationsByFloorByPagination",
        query = "SELECT l.id AS location_id ,l.name ,l.position, l.area ,l.floor_id, l.z_index, l.status,  l.record_checklist_count, l.record_checklist_status, l.type, l.code, f.name as floor_name, b.id as building_id, b.name as building_name, b.code as building_code "
                + " FROM location l  "
                + " LEFT JOIN floor f ON l.floor_id = f.id"
                + " LEFT JOIN building b ON f.building_id = b.id"
                + " WHERE l.floor_id = ?1 AND (?2 = 'null' OR CONCAT_WS('' , l.name) LIKE CONCAT('%' ,?2, '%'))"
                + " AND (?3 IS NULL OR IF(?3 = 'present', l.id IN ?4, l.id NOT IN ?4))"
                + " AND (?5 IS NULL OR IF(?5 = 'present', l.id IN ?6, l.id NOT IN ?6))"
                + " AND (?12 IS NULL OR IF(?12 = 'present', l.id IN ?13, l.id NOT IN ?13))"
                + " AND (?7 IS NULL OR IF(?7 = 'present', l.record_checklist_count>0, l.record_checklist_count=0 OR l.record_checklist_count IS NULL))"
                + " AND (?8 IS NULL OR l.status = ?8) AND ('all' IN ?11 OR l.type IN ?11)  ORDER BY l.z_index, l.id  ASC LIMIT ?9 OFFSET ?10",
        resultSetMapping = "locationmapping"
)
@NamedNativeQuery(
        name = "Location.getAllLocationsByFilter",
        query = "SELECT l.id AS location_id ,l.name ,l.position, l.area ,l.floor_id, l.z_index, l.status,  l.record_checklist_count, l.record_checklist_status, l.type, l.code, f.name as floor_name, b.id as building_id, b.name as building_name, b.code as building_code "
                + " FROM location l  "
                + " FORCE INDEX (idx_location_zindex_id)"
                + " LEFT JOIN floor f ON l.floor_id = f.id"
                + " LEFT JOIN building b ON f.building_id = b.id"
                + " WHERE ('all' IN ?10 OR f.building_id IN ?10) AND ('all' IN ?1 OR l.floor_id IN ?1) AND (?2 = 'null' OR CONCAT_WS('' , l.name) LIKE CONCAT('%' ,?2, '%'))"
                + " AND (?3 IS NULL OR IF(?3 = 'present', l.id IN ?4, l.id NOT IN ?4))"
                + " AND (?5 IS NULL OR IF(?5 = 'present', l.id IN ?6, l.id NOT IN ?6))"
                + " AND (?11 IS NULL OR IF(?11 = 'present', l.id IN ?12, l.id NOT IN ?12))"
                + " AND (?7 IS NULL OR IF(?7 = 'present', l.record_checklist_count>0, l.record_checklist_count=0 OR l.record_checklist_count IS NULL))"
                + " AND (?8 IS NULL OR l.status = ?8) AND ('all' IN ?9 OR l.type IN ?9)  ORDER BY l.z_index, l.id  ASC ",
        resultSetMapping = "locationmapping"
)
@NamedNativeQuery(
        name = "Location.getAllLocationsByFilterByPagination",
        query = "SELECT l.id AS location_id ,l.name ,l.position, l.area ,l.floor_id, l.z_index, l.status,  l.record_checklist_count, l.record_checklist_status, l.type, l.code, f.name as floor_name, b.id as building_id, b.name as building_name, b.code as building_code "
                + " FROM location l  "
                + " FORCE INDEX (idx_location_zindex_id)"
                + " LEFT JOIN floor f ON l.floor_id = f.id"
                + " LEFT JOIN building b ON f.building_id = b.id"
                + " WHERE ('all' IN ?12 OR f.building_id IN ?12) AND ('all' IN ?1 OR l.floor_id IN ?1) AND (?2 = 'null' OR CONCAT_WS('' , l.name) LIKE CONCAT('%' ,?2, '%'))"
                + " AND (?3 IS NULL OR IF(?3 = 'present', l.id IN ?4, l.id NOT IN ?4))"
                + " AND (?5 IS NULL OR IF(?5 = 'present', l.id IN ?6, l.id NOT IN ?6))"
                + " AND (?13 IS NULL OR IF(?13 = 'present', l.id IN ?14, l.id NOT IN ?14))"
                + " AND (?7 IS NULL OR IF(?7 = 'present', l.record_checklist_count>0, l.record_checklist_count=0 OR l.record_checklist_count IS NULL))"
                + " AND (?8 IS NULL OR l.status = ?8) AND ('all' IN ?11 OR l.type IN ?11)  ORDER BY l.z_index, l.id  ASC LIMIT ?9 OFFSET ?10",
        resultSetMapping = "locationmapping"
)

@SqlResultSetMapping(
        name = "locationsmapping",
        classes = {
                @ConstructorResult(
                        targetClass = LocationDTO.class,
                        columns = {
                                @ColumnResult(name = "location_id", type = String.class),
                                @ColumnResult(name = "name", type = String.class),
                                @ColumnResult(name = "type", type = String.class),
                                @ColumnResult(name = "code", type = String.class)

                        }
                )
        }
)

@NamedNativeQuery(
        name = "Location.getLocationsByFloor",
        query = "SELECT l.id AS location_id ,l.name, l.type, l.code  FROM location l WHERE l.floor_id = ?1",
        resultSetMapping = "locationsmapping"
)

@NamedNativeQuery(
        name = "Location.getLocationByVdmsId",
        query = "SELECT l.id AS location_id ,l.name, l.type, l.code FROM location l LEFT JOIN floor f ON f.id = l.floor_id LEFT JOIN building b ON b.id = f.building_id WHERE b.vdms_id = ?1",
        resultSetMapping = "locationsmapping"
)

@NamedNativeQuery(
        name = "Location.getLocationByLocationId",
        query = "SELECT l.id AS location_id, l.name, l.type, l.code FROM location l WHERE l.id = ?1",
        resultSetMapping = "locationsmapping"
)

@NamedNativeQuery(
        name = "Location.getAllLocationDetails",
        query = "SELECT l.id AS location_id, l.name, l.type, l.code"
                + " FROM location l ",
        resultSetMapping = "locationsmapping"
)

@NamedNativeQuery(
        name = "Location.getAllLocationsByIds",
        query = "SELECT l.id AS location_id, l.name, l.type, l.code"
                + " FROM location l where l.id IN ?1",
        resultSetMapping = "locationsmapping"
)

@SqlResultSetMapping(
        name = "locationalertinfomapping",
        classes = {
                @ConstructorResult(
                        targetClass = LocationAlertDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = String.class),
                                @ColumnResult(name = "name", type = String.class),
                                @ColumnResult(name = "floor_id", type = String.class),
                                @ColumnResult(name = "floor_name", type = String.class),
                                @ColumnResult(name = "building_id", type = String.class),
                                @ColumnResult(name = "building_name", type = String.class)
                        }
                )
        }
)

@NamedNativeQuery(
        name = "Location.getLocationAlertDetails",
        query = "SELECT l.id, l.name, f.id as floor_id, f.name as floor_name, b.id as building_id,"
                + " b.name as building_name"
                + " FROM location l "
                + " LEFT JOIN floor f ON l.floor_id = f.id"
                + " LEFT JOIN building b ON f.building_id = b.id"
                + " WHERE l.id = ?1",
        resultSetMapping = "locationalertinfomapping"
)

@NamedNativeQuery(
        name = "Location.getLocationsByFilter",
        query = "SELECT l.id, l.name, f.id as floor_id, f.name as floor_name, b.id as building_id,"
                + " b.name as building_name"
                + " FROM location l"
                + " LEFT JOIN floor f ON l.floor_id = f.id"
                + " LEFT JOIN building b ON f.building_id = b.id"
                + " WHERE (?1 IS NULL or CONCAT_WS('' , l.name ) LIKE CONCAT('%' ,?1, '%'))"
                + " AND (?2 IS NULL OR IF(?2, l.id IN ?3, l.id NOT IN ?3))"
                + " AND (?4 IS NULL OR IF(?4, l.id IN ?5, l.id NOT IN ?5))"
                + " AND (('all' IN ?6) or f.building_id IN ?6)"
                + " AND (('all' IN ?7) or l.floor_id IN ?7)"
                + " AND (('all' IN ?8) or l.id IN ?8)",
        resultSetMapping = "locationalertinfomapping"
)

@NamedNativeQuery(
        name = "Location.getLocationsByStatus",
        query = "SELECT l.id, l.name, f.id as floor_id, f.name as floor_name, b.id as building_id,"
                + " b.name as building_name"
                + " FROM location l "
                + " LEFT JOIN floor f ON l.floor_id = f.id"
                + " LEFT JOIN building b ON f.building_id = b.id"
                + " WHERE l.status LIKE CONCAT('%',?1,'%')"
                + " LIMIT ?3  OFFSET ?2",
        resultSetMapping = "locationalertinfomapping"
)

@SqlResultSetMapping(
        name = "locationdetailsbymeasuringinstrumentid",
        classes = {
                @ConstructorResult(
                        targetClass = LocationDTO.class,
                        columns = {
                                @ColumnResult(name = "location_id", type = String.class),
                                @ColumnResult(name = "name", type = String.class),
                                @ColumnResult(name = "position", type = String.class),
                                @ColumnResult(name = "area", type = String.class),
                                @ColumnResult(name = "floor_id", type = String.class),
                                @ColumnResult(name = "z_index", type = Integer.class),
                                @ColumnResult(name = "floor_name", type = String.class),
                                @ColumnResult(name = "building_name", type = String.class),
                                @ColumnResult(name = "measuring_instrument_id", type = String.class),
                                @ColumnResult(name = "type", type = String.class)

                        }
                )
        }
)


@NamedNativeQuery(
        name = "Location.getLocationDetailsByMeasuringInstrumentId",
        query = "SELECT l.id AS location_id ,l.name ,l.position ,l.area ,l.floor_id, l.z_index, b.name as building_name, f.name as floor_name, mi.id as measuring_instrument_id, l.type FROM location l "
                + "LEFT JOIN measuring_instrument_location mil ON mil.location_id = l.id "
                + "LEFT JOIN measuring_instrument mi ON mi.id = mil.measuring_instrument_id "
                + "LEFT JOIN floor f ON l.floor_id = f.id "
                + "LEFT JOIN building b ON f.building_id = b.id WHERE mi.id = ?1",
        resultSetMapping = "locationdetailsbymeasuringinstrumentid"
)


@SqlResultSetMapping(
        name = "locationdetailsbylocationidmapping",
        classes = {
                @ConstructorResult(
                        targetClass = LocationDTO.class,
                        columns = {
                                @ColumnResult(name = "location_id", type = String.class),
                                @ColumnResult(name = "name", type = String.class),
                                @ColumnResult(name = "position", type = String.class),
                                @ColumnResult(name = "area", type = String.class),
                                @ColumnResult(name = "floor_id", type = String.class),
                                @ColumnResult(name = "z_index", type = Integer.class),
                                @ColumnResult(name = "floor_name", type = String.class),
                                @ColumnResult(name = "building_name", type = String.class),
                                @ColumnResult(name = "record_checklist_count", type = Integer.class),
                                @ColumnResult(name = "record_checklist_status", type = String.class),
                                @ColumnResult(name = "building_id", type = String.class),
                                @ColumnResult(name = "type", type = String.class),
                                @ColumnResult(name = "code", type = String.class)

                        }
                )
        }
)

@NamedNativeQuery(
        name = "Location.getLocationDetailsByLocationId",
        query = "SELECT l.id AS location_id ,l.name ,l.position ,l.area ,l.floor_id, l.z_index, b.name as building_name, f.name as floor_name , l.record_checklist_count, l.record_checklist_status, b.id as building_id, l.type, l.code FROM location l "
                + "LEFT JOIN floor f ON l.floor_id = f.id "
                + "LEFT JOIN building b ON f.building_id = b.id WHERE l.id = ?1",
        resultSetMapping = "locationdetailsbylocationidmapping"
)

@SqlResultSetMapping(
        name = "isaddedlocationsmapping",
        classes = {
                @ConstructorResult(
                        targetClass = LocationDTO.class,
                        columns = {
                                @ColumnResult(name = "location_id", type = String.class),
                                @ColumnResult(name = "name", type = String.class),
                                @ColumnResult(name = "is_added", type = Integer.class),
                                @ColumnResult(name = "building_name", type = String.class),
                                @ColumnResult(name = "floor_name", type = String.class),
                                @ColumnResult(name = "type", type = String.class)

                        }
                )
        }
)


// getAllMeasuringInstrumentLocationsPagination

@NamedNativeQuery(
        name = "Location.getAllRecordChecklistLocationsPagination",
        query = "SELECT l.id AS location_id ,l.name,  b.name as building_name, f.name as floor_name, IF(rc.location_id = l.id, 1, 0) as is_added, l.type"
                + " FROM location l"
                + " LEFT JOIN floor f ON l.floor_id = f.id "
                + " LEFT JOIN building b ON f.building_id = b.id "
                + " LEFT JOIN record_checklist rc ON rc.location_id = l.id AND rc.global_checklist_id IN ?4 AND rc.inspection_record_id = ?6 AND rc.is_removed = 0 "
                + " WHERE ('all' = ?12 OR f.building_id = ?12) AND ('all' = ?5 OR l.floor_id = ?5) AND (?1 = 'null' or LOWER(REGEXP_REPLACE(CONCAT_WS('' , l.name ), '[ -.!\t_+#~`@$%^&*()=;:<>?,/{}|\\\\ ]' , '')) LIKE CONCAT('%' ,?1, '%'))"
                + " AND (?7 IS NULL OR IF(?7, l.id IN ?8, l.id NOT IN ?8))"
                + " AND (?9 IS NULL OR IF(?9, l.id IN ?10, l.id NOT IN ?10))"
                + " AND ('all' IN ?11 OR l.type IN ?11)"
                + " ORDER BY TRIM(l.name) ASC, l.id"
                + " LIMIT ?2 OFFSET ?3",
        resultSetMapping = "isaddedlocationsmapping"
)


@NamedNativeQuery(
        name = "Location.getAllMeasuringInstrumentLocationsPagination",
        query = "SELECT l.id AS location_id ,l.name, b.name as building_name, f.name as floor_name, IF(mil.location_id = l.id, 1, 0) as is_added, l.type "
                + " FROM location l "
                + " LEFT JOIN floor f ON l.floor_id = f.id "
                + " LEFT JOIN building b ON f.building_id = b.id "
                + " LEFT JOIN measuring_instrument_location mil ON mil.location_id = l.id AND mil.measuring_instrument_id IN ?5 "
                + " WHERE ('all' = ?11 OR f.building_id = ?11) AND ('all' = ?4 OR l.floor_id = ?4) AND (?1 = 'null' or LOWER(REGEXP_REPLACE(CONCAT_WS('' , l.name ), '[ -.!\t_+#~`@$%^&*()=;:<>?,/{}|\\\\ ]' , '')) LIKE CONCAT('%' ,?1, '%'))"
                + " AND (?6 IS NULL OR IF(?6, l.id IN ?7, l.id NOT IN ?7))"
                + " AND (?8 IS NULL OR IF(?8, l.id IN ?9, l.id NOT IN ?9))"
                + " AND ('all' IN ?10 OR l.type IN ?10)"
                + " ORDER BY TRIM(l.name) ASC, l.id"
                + " LIMIT ?2 OFFSET ?3",
        resultSetMapping = "isaddedlocationsmapping"
)


@NamedNativeQuery(
        name = "Location.getAllChecklistLocationsPagination",
        query = "SELECT l.id AS location_id ,l.name ,  b.name as building_name, f.name as floor_name, IF(lgc.location_id = l.id, 1, 0) as is_added, l.type"
                + " FROM location l "
                + " LEFT JOIN floor f ON l.floor_id = f.id "
                + " LEFT JOIN building b ON f.building_id = b.id "
                + " LEFT JOIN location_global_checklist lgc ON lgc.location_id = l.id AND lgc.global_checklist_id IN ?4 AND lgc.is_removed = 0 "
                + " WHERE ('all' = ?11 OR f.building_id = ?11) AND ('all' = ?5 OR l.floor_id = ?5) AND (?1 = 'null' or LOWER(REGEXP_REPLACE(CONCAT_WS('' , l.name ), '[ -.!\t_+#~`@$%^&*()=;:<>?,/{}|\\\\ ]' , '')) LIKE CONCAT('%' ,?1, '%'))"
                + " AND (?6 IS NULL OR IF(?6, l.id IN ?7, l.id NOT IN ?7))"
                + " AND (?8 IS NULL OR IF(?8, l.id IN ?9, l.id NOT IN ?9))"
                + " AND ('all' IN ?10 OR l.type IN ?10)"
                + " ORDER BY TRIM(l.name) ASC, l.id"
                + " LIMIT ?2 OFFSET ?3",
        resultSetMapping = "isaddedlocationsmapping"
)

@NamedNativeQuery(
        name = "Location.getAllInspectionLocationsPagination",
        query = "SELECT l.id AS location_id ,l.name,  b.name as building_name, f.name as floor_name, IF(gir.location_id = l.id, 1, 0) as is_added, l.type"
                + " FROM location l"
                + " LEFT JOIN floor f ON l.floor_id = f.id "
                + " LEFT JOIN building b ON f.building_id = b.id "
                + " LEFT JOIN global_inspection_relation gir ON gir.location_id = l.id AND gir.global_checklist_id IN ?4 AND gir.global_inspection_record_id = ?6 AND gir.is_removed = 0 "
                + " WHERE ('all' = ?12 OR f.building_id = ?12) AND ('all' = ?5 OR l.floor_id = ?5) AND (?1 = 'null' or LOWER(REGEXP_REPLACE(CONCAT_WS('' , l.name ), '[ -.!\t_+#~`@$%^&*()=;:<>?,/{}|\\\\ ]' , '')) LIKE CONCAT('%' ,?1, '%'))"
                + " AND (?7 IS NULL OR IF(?7, l.id IN ?8, l.id NOT IN ?8))"
                + " AND (?9 IS NULL OR IF(?9, l.id IN ?10, l.id NOT IN ?10))"
                + " AND ('all' IN ?11 OR l.type IN ?11)"
                + " ORDER BY TRIM(l.name) ASC, l.id"
                + " LIMIT ?2 OFFSET ?3",
        resultSetMapping = "isaddedlocationsmapping"
)


@NamedNativeQuery(
        name = "Location.getAllQrcodeLocationsPagination",
        query = "SELECT l.id AS location_id ,l.name ,  b.name as building_name, f.name as floor_name, IF(gr.location_id = l.id, 1, 0) as is_added, l.type"
                + " FROM location l "
                + " LEFT JOIN floor f ON l.floor_id = f.id "
                + " LEFT JOIN building b ON f.building_id = b.id "
                + " LEFT JOIN global_qrcode gr ON gr.location_id = l.id"
                + " WHERE ('all' = ?10 OR f.building_id = ?10) AND ('all' = ?4 OR l.floor_id = ?4) AND (?1 = 'null' or CONCAT_WS('' , l.name ) LIKE CONCAT('%' ,?1, '%'))"
                + " AND (?5 IS NULL OR IF(?5, l.id IN ?6, l.id NOT IN ?6))"
                + " AND (?7 IS NULL OR IF(?7, l.id IN ?8, l.id NOT IN ?8))"
                + " AND ('all' IN ?9 OR l.type IN ?9)"
                + " ORDER BY TRIM(l.name) ASC, l.id"
                + " LIMIT ?2 OFFSET ?3",
        resultSetMapping = "isaddedlocationsmapping"
)
@NamedNativeQuery(
        name = "Location.getAllChecklistLocations",
        query = "SELECT l.id AS location_id ,l.name ,  b.name as building_name, f.name as floor_name, IF(lgc.location_id = l.id, 1, 0) as is_added, l.type"
                + " FROM location l"
                + " LEFT JOIN floor f ON l.floor_id = f.id "
                + " LEFT JOIN building b ON f.building_id = b.id "
                + " LEFT JOIN location_global_checklist lgc ON lgc.location_id = l.id AND lgc.global_checklist_id = ?2 AND lgc.is_removed = 0 "
                + " WHERE ('all' = ?9 OR f.building_id = ?9) AND ('all' = ?3 OR l.floor_id = ?3) AND (?1 IS NULL or CONCAT_WS('' , l.name ) LIKE CONCAT('%' ,?1, '%'))"
                + " AND (?4 IS NULL OR IF(?4, l.id IN ?5, l.id NOT IN ?5))"
                + " AND (?6 IS NULL OR IF(?6, l.id IN ?7, l.id NOT IN ?7))"
                + " AND ('all' IN ?8 OR l.type IN ?8)"
                + " ORDER BY TRIM(l.name) ASC, l.id",
        resultSetMapping = "isaddedlocationsmapping"
)
@NamedNativeQuery(
        name = "Location.getAllInspectionLocations",
        query = "SELECT l.id AS location_id ,l.name,  b.name as building_name, f.name as floor_name, IF(gir.location_id = l.id, 1, 0) as is_added, l.type "
                + " FROM location l "
                + " LEFT JOIN floor f ON l.floor_id = f.id "
                + " LEFT JOIN building b ON f.building_id = b.id "
                + " LEFT JOIN global_inspection_relation gir ON gir.location_id = l.id AND gir.global_checklist_id = ?2 AND gir.global_inspection_record_id = ?4 AND gir.is_removed = 0 "
                + " WHERE ('all' = ?10 OR f.building_id = ?10) AND ('all' = ?3 OR l.floor_id = ?3) AND (?1 IS NULL or CONCAT_WS('' , l.name ) LIKE CONCAT('%' ,?1, '%'))"
                + " AND (?5 IS NULL OR IF(?5, l.id IN ?6, l.id NOT IN ?6))"
                + " AND (?7 IS NULL OR IF(?8, l.id IN ?8, l.id NOT IN ?8))"
                + " AND ('all' IN ?9 OR l.type IN ?9)"
                + " ORDER BY TRIM(l.name) ASC, l.id",
        resultSetMapping = "isaddedlocationsmapping"
)
@NamedNativeQuery(
        name = "Location.getAllQrcodeLocations",
        query = "SELECT l.id AS location_id ,l.name , b.name as building_name, f.name as floor_name, IF(gr.location_id = l.id, 1, 0) as is_added, l.type"
                + " FROM location l "
                + " LEFT JOIN floor f ON l.floor_id = f.id "
                + " LEFT JOIN building b ON f.building_id = b.id "
                + " LEFT JOIN global_qrcode gr ON gr.location_id = l.id"
                + " LEFT JOIN global_qrcode gr ON gr.location_id = l.id"
                + " WHERE ('all' = ?8 OR f.building_id = ?8) AND ('all' = ?2 OR l.floor_id = ?2) AND (?1 IS NULL or CONCAT_WS('' , l.name ) LIKE CONCAT('%' ,?1, '%'))"
                + " AND (?3 IS NULL OR IF(?3, l.id IN ?4, l.id NOT IN ?4))"
                + " AND (?5 IS NULL OR IF(?5, l.id IN ?6, l.id NOT IN ?6))"
                + " AND ('all' IN ?7 OR l.type IN ?7)"
                + " ORDER BY TRIM(l.name) ASC, l.id",
        resultSetMapping = "isaddedlocationsmapping"
)

// is_added field is not required for Reactive Service flow, so it is hardcoded to 0
@NamedNativeQuery(
        name = "Location.getAllReactiveServiceLocationsPagination",
        query ="SELECT l.id AS location_id, l.name, b.name AS building_name, f.name AS floor_name, 0 AS is_added, l.type " +
                        "FROM global_checklist gc " +
                        " JOIN location_global_checklist lgc ON lgc.global_checklist_id = gc.id  JOIN location l " +
                        " ON lgc.location_id = l.id " +
                        "LEFT JOIN floor f ON l.floor_id = f.id " +
                        "LEFT JOIN building b ON f.building_id = b.id " +
                        "WHERE gc.record_type = 'service' " +
                        "AND ('all' = ?10 OR f.building_id = ?10) AND ('all' = ?4 OR l.floor_id = ?4) AND (?1 = 'null' or CONCAT_WS('' , l.name ) LIKE CONCAT('%' ,?1, '%')) " +
                        "AND (?5 IS NULL OR IF(?5, l.id IN ?6, l.id NOT IN ?6)) " +
                        "AND (?7 IS NULL OR IF(?7, l.id IN ?8, l.id NOT IN ?8)) " +
                        "AND ('all' IN ?9 OR l.type IN ?9) " +
                         "GROUP BY l.id " +
                         "ORDER BY TRIM(l.name) ASC, l.id " +
                        "LIMIT ?2 OFFSET ?3",
        resultSetMapping = "isaddedlocationsmapping"
)


@SqlResultSetMapping(
        name = "exportlocationmapping",
        classes = {
                @ConstructorResult(
                        targetClass = LocationDTO.class,
                        columns = {
                                @ColumnResult(name = "location_id", type = String.class),
                                @ColumnResult(name = "name", type = String.class),
                                @ColumnResult(name = "floor_id", type = String.class),
                                @ColumnResult(name = "floor_name", type = String.class),
                                @ColumnResult(name = "building_name", type = String.class),
                                @ColumnResult(name = "building_id", type = String.class),
                                @ColumnResult(name = "type", type = String.class),
                                @ColumnResult(name = "code", type = String.class),
                                @ColumnResult(name = "building_code", type = String.class)
                        }
                )
        }
)
@NamedNativeQuery(
        name = "Location.getLocationsByBuildingAndFloor",
        query = "SELECT l.id AS location_id ,l.name ,l.floor_id, f.name as floor_name, b.name as building_name, b.id as building_id, l.type, l.code, b.code as building_code  "
                + " FROM location l  "
                + " LEFT JOIN floor f ON l.floor_id = f.id"
                + " LEFT JOIN building b ON f.building_id = b.id"
                + " WHERE ('all' IN (?1) OR f.building_id IN (?1)) AND ('all' IN (?2) OR l.floor_id IN (?2)) AND (?3 = 'null' OR CONCAT_WS('' , l.name) LIKE CONCAT('%' ,?3, '%'))"
                + " AND (?4 IS NULL OR IF(?4 = 'present', l.id IN ?5, l.id NOT IN ?5))"
                + " AND (?6 IS NULL OR IF(?6 = 'present', l.id IN ?7, l.id NOT IN ?7))"
                + " AND (?8 IS NULL OR IF(?8 = 'present', l.record_checklist_count>0, l.record_checklist_count=0 OR l.record_checklist_count IS NULL))"
                + " AND (?9 IS NULL OR l.status = ?9) AND ('all' IN ?10 OR l.type IN ?10)",
        resultSetMapping = "exportlocationmapping"
)

// removed: Bucket-D integration mapping (locationintegrationbylocationidmapping) and query Location.getIntegrationByLocationId

@SqlResultSetMapping(
        name = "locationsadcmapping",
        classes = {
                @ConstructorResult(
                        targetClass = LocationDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = String.class),
                                @ColumnResult(name = "name", type = String.class),
                                @ColumnResult(name = "type", type = String.class),
                                @ColumnResult(name = "code", type = String.class),
                                @ColumnResult(name = "floorId", type = String.class)

                        }
                )
        }
)

@NamedNativeQuery(
        name = "Location.getLocationsByFloorIds",
        query = "SELECT l.id AS id ,l.name, l.type, l.code, l.floor_id AS floorId  FROM location l WHERE l.floor_id IN ?1",
        resultSetMapping = "locationsadcmapping"
)

/**************************************** new location changes ***************************/

public class Location {

    @Id
    private String id;

    @Column(length = 128)
    private String name;

    @Column(length = 128)
    private String position;

    @ManyToOne
    private Floor floor;

    @Column(columnDefinition = "TEXT")
    private String area;


    @Column(length = 32)
    private String record_checklist_status;

    @Column(columnDefinition = "integer default 0")
    private Integer z_index;

    @Column(length = 32)
    private Integer record_checklist_count;

    private String status;

    @Column(length = 128)
    private BigInteger updated_timestamp;

    @Column
    @ColumnDefault("'vdms'")
    private String source_type;

    @OneToMany(mappedBy = "location")
    private Set<Device> device;

    // removed: relation to Bucket-C entity PropertyQrcode (AP-C4)
    @OneToOne(cascade = CascadeType.ALL, mappedBy = "location")
    private GlobalQrcode global_qrcode;

    // removed: relation to Bucket-C entity RecordChecklist (AP-C4)
    // removed: relation to Bucket-C entity GlobalChecklist (AP-C4)
    // removed: relation to Bucket-C entity GlobalInspectionRelation (AP-C4)

    @ManyToMany(mappedBy = "location")
    private Set<MeasuringInstrument> measuring_instrument;

    // removed: relation to Bucket-C entity PmsAttributes (AP-C2)

    @Column(length = 128, columnDefinition = "varchar(128) default 'generic'")
    private String type;

    @Column(length = 128)
    private String code;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "location")
    private Set<Nfc> nfc;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "location")
    private Set<QrCode> qrcode;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "location")
    private Set<ClientNfc> client_nfc;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "location")
    private Set<ClientQrCode> client_qrcode;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "location")
    private Set<LocationHistory> location_history;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "location")
    private Set<ClientBarCode> client_barcode;

    // removed: relation to Bucket-C entity GlobalChecklistConditions (AP-C4)

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Floor getFloor() {
        return floor;
    }

    public void setFloor(Floor floor) {
        this.floor = floor;
    }

    public Set<Device> getDevice() {
        return device;
    }

    public void setDevice(Set<Device> device) {
        this.device = device;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public GlobalQrcode getGlobal_qrcode() {
        return global_qrcode;
    }

    public void setGlobal_qrcode(GlobalQrcode global_qrcode) {
        this.global_qrcode = global_qrcode;
    }

    public String getRecord_checklist_status() {
        return record_checklist_status;
    }

    public void setRecord_checklist_status(String record_checklist_status) {
        this.record_checklist_status = record_checklist_status;
    }

    public Integer getRecord_checklist_count() {
        return record_checklist_count;
    }

    public void setRecord_checklist_count(Integer record_checklist_count) {
        this.record_checklist_count = record_checklist_count;
    }

    public Integer getZ_index() {
        return z_index;
    }

    public void setZ_index(Integer z_index) {
        this.z_index = z_index;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Set<MeasuringInstrument> getMeasuring_instrument() {
        return measuring_instrument;
    }

    public void setMeasuring_instrument(Set<MeasuringInstrument> measuring_instrument) {
        this.measuring_instrument = measuring_instrument;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Set<Nfc> getNfc() {
        return nfc;
    }

    public void setNfc(Set<Nfc> nfc) {
        this.nfc = nfc;
    }

    public Set<QrCode> getQrcode() {
        return qrcode;
    }

    public void setQrcode(Set<QrCode> qrcode) {
        this.qrcode = qrcode;
    }

    public Set<ClientNfc> getClient_nfc() {
        return client_nfc;
    }

    public void setClient_nfc(Set<ClientNfc> client_nfc) {
        this.client_nfc = client_nfc;
    }

    public Set<ClientQrCode> getClient_qrcode() {
        return client_qrcode;
    }

    public void setClient_qrcode(Set<ClientQrCode> client_qrcode) {
        this.client_qrcode = client_qrcode;
    }

    public Set<LocationHistory> getLocation_history() {
        return location_history;
    }

    public void setLocation_history(Set<LocationHistory> location_history) {
        this.location_history = location_history;
    }

    public Set<ClientBarCode> getClient_barcode() {
        return client_barcode;
    }

    public void setClient_barcode(Set<ClientBarCode> client_barcode) {
        this.client_barcode = client_barcode;
    }

}