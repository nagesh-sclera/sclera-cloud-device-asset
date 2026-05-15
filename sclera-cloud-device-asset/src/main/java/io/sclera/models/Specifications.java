
package io.sclera.models;


import io.sclera.dto.SpecificationsDTO;

import javax.persistence.*;

import java.util.Set;


@SqlResultSetMapping(
        name = "specificationsdatamapping",
        classes = {
                @ConstructorResult(
                        targetClass = SpecificationsDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = String.class),
                                @ColumnResult(name = "key_name", type = String.class),
                                @ColumnResult(name = "key_value", type = String.class),
                                @ColumnResult(name = "key_unit", type = String.class),
                                @ColumnResult(name = "device_id", type = String.class)

                        })
        })


@NamedNativeQuery(
        name = "Specifications.getDeviceSpecificationsBasedOnDeviceId",
        query = "SELECT  sc.id, sc.key_name, sc.key_value, sc.key_unit, sc.device_id FROM specifications sc WHERE sc.device_id = ?1 ",
        resultSetMapping = "specificationsdatamapping"
)


@NamedNativeQuery(
        name = "Specifications.getDeviceSpecificationsBasedOnDeviceIdAndKeyName",
        query = "SELECT  sc.id, sc.key_name, sc.key_value, sc.key_unit, sc.device_id FROM specifications sc WHERE sc.device_id = ?1 AND sc.key_name = ?2 ",
        resultSetMapping = "specificationsdatamapping"
)


@NamedNativeQuery(
        name = "Specifications.getPower",
        query = "SELECT  sc.id, sc.key_name, sc.key_value, sc.key_unit, sc.device_id FROM specifications sc WHERE sc.device_id = ?1 AND (sc.key_name = ?2)",
        resultSetMapping = "specificationsdatamapping"
)

@Entity
public class Specifications {

    @Id
    private String id;

    @Column
    private String key_name;

    @Column
    private String key_value;

    @Column
    private String key_unit;


    @ManyToOne
    private Device device;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "specifications")
    private Set<ConnectedDevices> connected_devices;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getKey_name() {
        return key_name;
    }

    public void setKey_name(String key_name) {
        this.key_name = key_name;
    }

    public String getKey_value() {
        return key_value;
    }

    public void setKey_value(String key_value) {
        this.key_value = key_value;
    }

    public String getKey_unit() {
        return key_unit;
    }

    public void setKey_unit(String key_unit) {
        this.key_unit = key_unit;
    }

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    public Set<ConnectedDevices> getConnected_devices() {
        return connected_devices;
    }

    public void setConnected_devices(Set<ConnectedDevices> connected_devices) {
        this.connected_devices = connected_devices;
    }


}
