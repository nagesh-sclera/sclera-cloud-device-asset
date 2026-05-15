package io.sclera.models;

import io.sclera.dto.DeviceTypesDTO;

import javax.persistence.*;
import java.math.BigInteger;

@SqlResultSetMapping(
        name = "deviceTypesMapping",
        classes = @ConstructorResult(
                targetClass = DeviceTypesDTO.class,
                columns = {
                        @ColumnResult(name = "id", type = String.class),
                        @ColumnResult(name = "name", type = String.class),
                        @ColumnResult(name = "updatedTimestamp", type = BigInteger.class)
                }
        )
)

@NamedNativeQuery(
        name = "DeviceTypes.getAllDeviceTypes",
        query = "SELECT dt.id, dt.name, dt.updated_timestamp AS updatedTimestamp " +
                "FROM device_types dt ",
        resultSetMapping = "deviceTypesMapping"
)


@SqlResultSetMapping(
        name = "deviceTypesUpdateMapping",
        classes = @ConstructorResult(
                targetClass = DeviceTypesDTO.class,
                columns = {
                        @ColumnResult(name = "name", type = String.class),
                        @ColumnResult(name = "oldName", type = String.class)
                }
        )
)

@NamedNativeQuery(
        name = "DeviceTypes.getAllUpdatedDeviceTypes",
        query = "SELECT dt.name, dt.old_name AS oldName " +
                "FROM device_types dt WHERE dt.old_name IS NOT NULL ",
        resultSetMapping = "deviceTypesUpdateMapping"
)



@Entity
public class DeviceTypes {
    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "name")
    private String name;

    @Column(name = "updated_timestamp")
    private BigInteger updatedTimestamp;

    @Column(name = "old_name")
    private String oldName;
}
