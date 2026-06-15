package io.sclera.models;

import io.sclera.dto.AssetFieldDTO;
import org.hibernate.annotations.ColumnDefault;

import jakarta.persistence.*;
import java.math.BigInteger;

/**
 * JPA entity defining a configurable asset field (custom or global), describing its name, type,
 * tooltip, default value, options, and display section. Used to drive dynamic asset metadata forms.
 */
@Entity
public class AssetField {
    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "name")
    private String name;

    @Column(name = "type")
    private String type;

    @Column(name = "tool_tip")
    private String toolTip;

    @Column(name = "default_value")
    private String defaultValue;

    @Column(name = "is_active")
    @ColumnDefault("true")
    private Boolean isActive;

    @Column(name = "options", columnDefinition = "TEXT")
    private String options;

    @Column(name = "is_deleted")
    @ColumnDefault("false")
    private Boolean isDeleted;

    @Column(name = "show_in_section", columnDefinition = "INT DEFAULT 0")
    private Integer showInSection;

    @Column(name = "created_at")
    private BigInteger createdAt;

}
