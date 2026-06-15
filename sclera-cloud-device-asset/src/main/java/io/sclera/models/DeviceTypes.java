package io.sclera.models;

import jakarta.persistence.*;
import java.math.BigInteger;

/**
 * Represents a category of device (device type) along with its display name and rename history.
 * Used to classify devices and to track renaming of device types across the asset catalogue.
 */
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
