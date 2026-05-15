package io.sclera.models;


import io.sclera.dto.GlobalQrcodeDTO;

import javax.persistence.*;


@SqlResultSetMapping(
        name = "globalqrlocationmapping",
        classes = {
                @ConstructorResult(
                        targetClass = GlobalQrcodeDTO.class,
                        columns = {
                                @ColumnResult(name = "id",type = String.class),
                                @ColumnResult(name = "image_url",type = String.class),
                                @ColumnResult(name = "location_id",type = String.class),
                                @ColumnResult(name = "location_name",type = String.class),
                                @ColumnResult(name = "floor_name",type = String.class),
                                @ColumnResult(name = "building_name",type = String.class)
                        })
        })


//to be removed after pagination api works
@NamedNativeQuery(
        name = "GlobalQrcode.getGlobalQrCodeLocation",
        query = "SELECT gq.id , gq.image_url, gq.location_id, l.name as location_name, f.name as floor_name, b.name as building_name "
                + " FROM global_qrcode gq "
                + " LEFT JOIN location l ON l.id = gq.location_id"
                + " LEFT JOIN floor f ON l.floor_id = f.id"
                + " LEFT JOIN building b ON f.building_id = b.id "
                + " WHERE gq.device_id IS NULL AND (?1 = 'null' or CONCAT_WS('' , l.name , f.name, b.name) LIKE CONCAT('%' ,?1, '%')) AND (?4 = 'all' or b.id = ?4) AND (?5 = 'all' or f.id = ?5) AND gq.location_id IS NOT NULL"
                + " LIMIT ?2 OFFSET ?3",
        resultSetMapping = "globalqrlocationmapping")

@NamedNativeQuery(
        name = "GlobalQrcode.getGlobalQrcodeLocationDetails",
        query = "SELECT gq.id , gq.image_url, gq.location_id, l.name as location_name, f.name as floor_name, b.name as building_name "
                + " FROM global_qrcode gq "
                + " LEFT JOIN location l ON l.id = gq.location_id"
                + " LEFT JOIN floor f ON l.floor_id = f.id"
                + " LEFT JOIN building b ON f.building_id = b.id "
                + " WHERE gq.device_id IS NULL AND ('all' IN ?1 or b.id IN ?1) AND ('all' IN ?2 or f.id IN ?2) AND gq.location_id IS NOT NULL ",
        resultSetMapping = "globalqrlocationmapping")




@SqlResultSetMapping(
        name = "globalqrdevicemapping",
        classes = {
                @ConstructorResult(
                        targetClass = GlobalQrcodeDTO.class,
                        columns = {
                                @ColumnResult(name = "id",type = String.class),
                                @ColumnResult(name = "image_url",type = String.class),
                                @ColumnResult(name = "device_id",type = String.class),
                                @ColumnResult(name = "device_name",type = String.class),
                                @ColumnResult(name = "docker_name",type = String.class),
                                @ColumnResult(name = "location_name",type = String.class),
                                @ColumnResult(name = "floor_name",type = String.class),
                                @ColumnResult(name = "building_name",type = String.class)
                        })
        })


//to be removed after pagination api works
@NamedNativeQuery(
        name = "GlobalQrcode.getGlobalQrCodeDevice",
        query = "SELECT gq.id , gq.image_url, gq.device_id , IF(d.user_data_name IS NULL OR d.user_data_name = '', d.display_name, d.user_data_name) as device_name, d.docker_name , l.name as location_name, f.name as floor_name, b.name as building_name "
                + " FROM global_qrcode gq "
                + " LEFT JOIN device d on d.id = gq.device_id"
                + " LEFT JOIN location l ON l.id = d.location_id"
                + " LEFT JOIN floor f ON l.floor_id = f.id"
                + " LEFT JOIN building b ON f.building_id = b.id "
                + " WHERE gq.location_id IS NULL AND (?1 = 'null' or CONCAT_WS('' , l.name , f.name, b.name, d.display_name, d.user_data_name) LIKE CONCAT('%' ,?1, '%')) AND ('all' IN ?4 or d.type IN ?4) AND gq.device_id IS NOT NULL"
                + " LIMIT ?2 OFFSET ?3",
        resultSetMapping = "globalqrdevicemapping")


@NamedNativeQuery(
        name = "GlobalQrcode.getGlobalQrCodesByIds",
        query = "SELECT gq.id , gq.image_url, gq.device_id , IF(d.user_data_name IS NULL OR d.user_data_name = '', d.display_name, d.user_data_name) as device_name, d.docker_name , l.name as location_name, f.name as floor_name, b.name as building_name "
                + " FROM global_qrcode gq "
                + " LEFT JOIN device d on d.id = gq.device_id"
                + " LEFT JOIN location l ON l.id = d.location_id OR l.id = gq.location_id"
                + " LEFT JOIN floor f ON l.floor_id = f.id"
                + " LEFT JOIN building b ON f.building_id = b.id "
                + " WHERE gq.id in ( ?1 )",
        resultSetMapping = "globalqrdevicemapping")

@NamedNativeQuery(
        name = "GlobalQrcode.getGlobalQrcodeDeviceDetails",
        query = "SELECT gq.id , gq.image_url, gq.device_id , IF(d.user_data_name IS NULL OR d.user_data_name = '', d.display_name, d.user_data_name)  as device_name, d.docker_name ,gq.location_id, l.name as location_name, f.name as floor_name, b.name as building_name "
                + " FROM global_qrcode gq "
                + " LEFT JOIN device d on d.id = gq.device_id"
                + " LEFT JOIN location l ON l.id = gq.location_id"
                + " LEFT JOIN floor f ON l.floor_id = f.id"
                + " LEFT JOIN building b ON f.building_id = b.id "
                + " WHERE gq.location_id IS NULL AND ('all' IN ?1 or d.docker_name IN ?1) AND ('all' IN ?2 or d.type IN ?2) AND gq.device_id IS NOT NULL ",
        resultSetMapping = "globalqrdevicemapping")


@SqlResultSetMapping(
        name = "globalqrmapping",
        classes = {
                @ConstructorResult(
                        targetClass = GlobalQrcodeDTO.class,
                        columns = {
                                @ColumnResult(name = "id",type = String.class),
                                @ColumnResult(name = "image_url",type = String.class),
                                @ColumnResult(name = "location_id",type = String.class),
                                @ColumnResult(name = "device_id",type = String.class),
                                @ColumnResult(name = "docker_name",type = String.class)
                        })
        })


@NamedNativeQuery(
        name = "GlobalQrcode.getQrcodeDetail",
        query = "SELECT gq.id , gq.image_url, gq.location_id , gq.device_id , d.docker_name  "
                + " FROM global_qrcode gq "
                + " LEFT JOIN device d on d.id = gq.device_id"
                + " WHERE ( 'null' = ?1 or gq.id = ?1 ) AND ( 'null' = ?2 or gq.location_id = ?2) AND ('null' = ?3 or gq.device_id = ?3) ",
        resultSetMapping = "globalqrmapping")




@SqlResultSetMapping(
        name = "globalqrcodemapping",
        classes = {
                @ConstructorResult(
                        targetClass = GlobalQrcodeDTO.class,
                        columns = {
                                @ColumnResult(name = "id",type = String.class),
                                @ColumnResult(name = "image_url",type = String.class),
                                @ColumnResult(name = "device_id",type = String.class),
                                @ColumnResult(name = "device_name",type = String.class),
                                @ColumnResult(name = "docker_name",type = String.class),
                                @ColumnResult(name = "location_id",type = String.class),
                                @ColumnResult(name = "location_name",type = String.class),
                                @ColumnResult(name = "floor_name",type = String.class),
                                @ColumnResult(name = "building_name",type = String.class)
                        })
        })


@NamedNativeQuery(
        name = "GlobalQrcode.getGlobalQrcodeById",
        query = "SELECT gq.id , gq.image_url, gq.device_id,"
                + " IF(d.user_data_name IS NULL OR d.user_data_name = '', d.display_name, d.user_data_name) as device_name, d.docker_name ,"
                + " gq.location_id, l.name as location_name, f.name as floor_name, b.name as building_name"
                + " FROM global_qrcode gq "
                + " LEFT JOIN device d on d.id = gq.device_id"
                + " LEFT JOIN location l ON l.id = d.location_id"
                + " LEFT JOIN floor f ON l.floor_id = f.id"
                + " LEFT JOIN building b ON f.building_id = b.id "
                + " WHERE gq.id = ?1",
        resultSetMapping = "globalqrcodemapping")

@NamedNativeQuery(
        name = "GlobalQrcode.getGlobalQrcodes",
        query = "SELECT gq.id , gq.image_url, gq.device_id, "
                + " IF(d.user_data_name IS NULL OR d.user_data_name = '', d.display_name, d.user_data_name) as device_name,"
                + " d.docker_name, gq.location_id, l.name as location_name, f.name as floor_name, b.name as building_name "
                + " FROM global_qrcode gq "
                + " LEFT JOIN device d on d.id = gq.device_id"
                + " LEFT JOIN location l ON l.id = gq.location_id OR l.id = d.location_id"
                + " LEFT JOIN floor f ON l.floor_id = f.id"
                + " LEFT JOIN building b ON f.building_id = b.id "
                + " WHERE (?1 = 'null' or CONCAT_WS('' , l.name , f.name, b.name, d.display_name, d.user_data_name) LIKE CONCAT('%' ,?1, '%'))  "
                + " LIMIT ?2 OFFSET ?3",
        resultSetMapping = "globalqrcodemapping")

@NamedNativeQuery(
        name = "GlobalQrcode.getUntaggedGlobalQrcodes",
        query = "SELECT gq.id , gq.image_url, gq.device_id, "
                + " IF(d.user_data_name IS NULL OR d.user_data_name = '', d.display_name, d.user_data_name) as device_name,"
                + " d.docker_name, gq.location_id, l.name as location_name, f.name as floor_name, b.name as building_name "
                + " FROM global_qrcode gq "
                + " LEFT JOIN device d on d.id = gq.device_id"
                + " LEFT JOIN location l ON l.id = gq.location_id OR l.id = d.location_id"
                + " LEFT JOIN floor f ON l.floor_id = f.id"
                + " LEFT JOIN building b ON f.building_id = b.id "
                + " WHERE (?1 = 'null' or CONCAT_WS('' , l.name , f.name, b.name, d.display_name, d.user_data_name) LIKE CONCAT('%' ,?1, '%')) AND"
                + " gq.device_id IS NULL AND gq.location_id IS NULL"
                + " LIMIT ?2 OFFSET ?3",
        resultSetMapping = "globalqrcodemapping")


@NamedNativeQuery(
        name = "GlobalQrcode.getGlobalQrcodeDetails",
        query = "SELECT gq.id , gq.image_url, gq.device_id, "
                + " IF(d.user_data_name IS NULL OR d.user_data_name = '', d.display_name, d.user_data_name) as device_name,"
                + " d.docker_name, gq.location_id, l.name as location_name, f.name as floor_name, b.name as building_name "
                + " FROM global_qrcode gq "
                + " LEFT JOIN device d on d.id = gq.device_id"
                + " LEFT JOIN location l ON l.id = gq.location_id OR l.id = d.location_id"
                + " LEFT JOIN floor f ON l.floor_id = f.id"
                + " LEFT JOIN building b ON f.building_id = b.id ",
        resultSetMapping = "globalqrcodemapping")

@NamedNativeQuery(
        name = "GlobalQrcode.getUntaggedGlobalQrcodeDetails",
        query = "SELECT gq.id , gq.image_url, gq.device_id, "
                + " IF(d.user_data_name IS NULL OR d.user_data_name = '', d.display_name, d.user_data_name) as device_name,"
                + " d.docker_name, gq.location_id, l.name as location_name, f.name as floor_name, b.name as building_name "
                + " FROM global_qrcode gq "
                + " LEFT JOIN device d on d.id = gq.device_id"
                + " LEFT JOIN location l ON l.id = gq.location_id OR l.id = d.location_id"
                + " LEFT JOIN floor f ON l.floor_id = f.id"
                + " LEFT JOIN building b ON f.building_id = b.id "
                + " WHERE gq.device_id IS NULL AND gq.location_id IS NULL",
        resultSetMapping = "globalqrcodemapping")



@Entity
public class GlobalQrcode {

    @Id
    private String id;

    private String image_url;

    @OneToOne
    private Location location;

    @OneToOne
    private Device device;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }
}
