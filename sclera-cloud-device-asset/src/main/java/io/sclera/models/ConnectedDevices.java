
package io.sclera.models;


import io.sclera.dto.ConnectedDevicesDTO;
import io.sclera.dto.PowerSourceConnectionsDTO;

import javax.persistence.*;


@Entity
@SqlResultSetMapping(
        name = "connecteddevicedetailsmapping",
        classes = {
                @ConstructorResult(
                        targetClass = ConnectedDevicesDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = String.class),
                                @ColumnResult(name = "connected_specifications_id", type = String.class),
                                @ColumnResult(name = "key_name", type = String.class),
                                @ColumnResult(name = "device_id", type = String.class),
                                @ColumnResult(name = "connected_device", type = String.class)


                        }
                )
        }
)


@NamedNativeQuery(
        name = "ConnectedDevices.getConnectedSpecificationsByDeviceId",
        query = "SELECT cd.specifications_id as id, spc.device_id as device_id, spc.key_name as key_name, sp.device_id as connected_device, cd.connected_specifications_id as connected_specifications_id"
                + " FROM connected_devices cd"
                + " LEFT JOIN specifications sp ON sp.id = cd.connected_specifications_id"
                + " LEFT JOIN specifications spc ON spc.id = cd.specifications_id"
                + " WHERE sp.device_id = ?1 ",
        resultSetMapping = "connecteddevicedetailsmapping"
)


@NamedNativeQuery(
        name = "ConnectedDevices.getAllInputConnectedSpecifications",
        query = "SELECT cd.connected_specifications_id as id, spc.device_id as connected_device, sp.key_name as key_name, cd.specifications_id as connected_specifications_id, sp.device_id as device_id"
                + " FROM connected_devices cd"
                + " LEFT JOIN specifications sp ON sp.id = cd.connected_specifications_id"
                + " LEFT JOIN specifications spc ON spc.id = cd.specifications_id"
                + " WHERE sp.device_id = ?1 ",
        resultSetMapping = "connecteddevicedetailsmapping"
)


@NamedNativeQuery(
        name = "ConnectedDevices.getAllOutputConnectedSpecifications",
        query = "SELECT cd.specifications_id as id, sp.device_id as connected_device, spc.key_name as key_name, cd.connected_specifications_id as connected_specifications_id, spc.device_id as device_id"
                + " FROM connected_devices cd"
                + " LEFT JOIN specifications sp ON sp.id = cd.connected_specifications_id"
                + " LEFT JOIN specifications spc ON spc.id = cd.specifications_id"
                + " WHERE spc.device_id = ?1 ",
        resultSetMapping = "connecteddevicedetailsmapping"
)


@SqlResultSetMapping(
        name = "powersourcetopologymapping",
        classes = {
                @ConstructorResult(
                        targetClass = PowerSourceConnectionsDTO.class,
                        columns = {
                                @ColumnResult(name = "source_device_id", type = String.class),
                                @ColumnResult(name = "target_device_id", type = String.class),
                                @ColumnResult(name = "source_specifications_name", type = String.class),
                                @ColumnResult(name = "target_specifications_name", type = String.class),
                                @ColumnResult(name = "source_specifications_id", type = String.class),
                                @ColumnResult(name = "target_specifications_id", type = String.class)

                        }
                )
        }
)


@NamedNativeQuery(
        name = "ConnectedDevices.getPowerSourceTopologyForDevice",
        query = "SELECT cd.specifications_id as source_specifications_id, cd.connected_specifications_id as target_specifications_id, spc.device_id as source_device_id, spc.key_name as source_specifications_name, sp.device_id as target_device_id, sp.key_name as target_specifications_name"
                + " FROM connected_devices cd"
                + " LEFT JOIN specifications sp ON sp.id = cd.connected_specifications_id"
                + " LEFT JOIN specifications spc ON spc.id = cd.specifications_id"
                + " WHERE spc.device_id IN ?1",
        resultSetMapping = "powersourcetopologymapping"
)

@NamedNativeQuery(
        name = "ConnectedDevices.getPowerSourceTopologyByPagination",
        query = "SELECT cd.specifications_id as source_specifications_id, cd.connected_specifications_id as target_specifications_id, spc.device_id as source_device_id, spc.key_name as source_specifications_name, sp.device_id as target_device_id, sp.key_name as target_specifications_name"
                + " FROM connected_devices cd"
                + " LEFT JOIN specifications sp ON sp.id = cd.connected_specifications_id"
                + " LEFT JOIN specifications spc ON spc.id = cd.specifications_id"
                + " LIMIT ?1 OFFSET ?2",
        resultSetMapping = "powersourcetopologymapping"
)

@SqlResultSetMapping(
        name = "loadcalculationmapping",
        classes = {
                @ConstructorResult(
                        targetClass = ConnectedDevicesDTO.class,
                        columns = {
                                @ColumnResult(name = "key_name", type = String.class),
                                @ColumnResult(name = "device_id", type = String.class)

                        }
                )
        }
)

@NamedNativeQuery(
        name = "ConnectedDevices.getAllConnectedDevicesForLoadCalculation",
        query = "SELECT cd.connected_specifications_id as id, spc.device_id as device_id, spc.key_name FROM connected_devices cd"
                + " LEFT JOIN specifications sp ON sp.id = cd.specifications_id"
                + " LEFT JOIN specifications spc ON spc.id = cd.connected_specifications_id"
                + " WHERE cd.specifications_id = ?1",
        resultSetMapping = "loadcalculationmapping"
)


@NamedNativeQuery(
        name = "ConnectedDevices.getConnectedDevicesSpecifications",
        query = "SELECT cd.connected_specifications_id as id, spc.device_id as connected_device, sp.key_name as key_name, cd.specifications_id as connected_specifications_id, sp.device_id as device_id"
                + " FROM connected_devices cd"
                + " LEFT JOIN specifications sp ON sp.id = cd.connected_specifications_id"
                + " LEFT JOIN specifications spc ON spc.id = cd.specifications_id"
                + " WHERE spc.id = ?1 LIMIT ?2 OFFSET ?3",
        resultSetMapping = "connecteddevicedetailsmapping"
)


public class ConnectedDevices {

    @Id
    private String id;

    @Column
    private String connected_specifications_id;


    @ManyToOne
    private Specifications specifications;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getConnected_specifications_id() {
        return connected_specifications_id;
    }

    public void setConnected_specifications_id(String connected_specifications_id) {
        this.connected_specifications_id = connected_specifications_id;
    }

    public Specifications getSpecifications() {
        return specifications;
    }

    public void setSpecifications(Specifications specifications) {
        this.specifications = specifications;
    }


}
