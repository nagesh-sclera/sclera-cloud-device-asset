package io.sclera.models;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import org.hibernate.annotations.ColumnDefault;

import jakarta.persistence.*;
import java.math.BigInteger;
import java.util.Set;


/**
 * JPA entity representing a media document or file (such as a manual or image) that can be
 * linked to one or more devices within the asset-management domain.
 */
@Entity
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = Media.class)
public class Media {

    @Id
    private String id;

    private String name;

    private String description;

    private String category;

    private String link;

    private String created_email;

    private BigInteger created_timestamp;

    private String extension;

    @ManyToMany(mappedBy = "media")
    private Set<Device> device;

    @Column(name = "source_type")
    @ColumnDefault("'vdms'")
    private String source_type;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getCreated_email() {
        return created_email;
    }

    public void setCreated_email(String created_email) {
        this.created_email = created_email;
    }

    public BigInteger getCreated_timestamp() {
        return created_timestamp;
    }

    public void setCreated_timestamp(BigInteger created_timestamp) {
        this.created_timestamp = created_timestamp;
    }

    public Set<Device> getDevice() {
        return device;
    }

    public void setDevice(Set<Device> device) {
        this.device = device;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }


}
