package io.sclera.models;

import java.math.BigInteger;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import org.hibernate.annotations.ColumnDefault;


/**
 * Represents a document or media file (e.g. manual, image, or attachment) that can be associated with one
 * or more devices. Used to store and retrieve supporting documentation for assets, including encryption and
 * source metadata.
 */
@Entity
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = Document.class)
public class Document {

    @Id
    private String id;

    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String category;

    private String link;

    private String created_email;

    private BigInteger created_timestamp;

    @ManyToMany(mappedBy = "document")
    private Set<Device> device;

    @Column(name = "encrypted_type")
    private Integer encrypted_type;

    @Column(name = "source_type")
    @ColumnDefault("'vdms'")
    private String source_type;

    public Integer getEncrypted_type() {
        return encrypted_type;
    }

    public void setEncrypted_type(Integer encrypted_type) {
        this.encrypted_type = encrypted_type;
    }

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


}
