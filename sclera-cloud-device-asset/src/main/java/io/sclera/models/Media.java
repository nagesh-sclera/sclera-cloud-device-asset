package io.sclera.models;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import io.sclera.dto.DocumentMediaDTO;
import org.hibernate.annotations.ColumnDefault;

import javax.persistence.*;
import java.math.BigInteger;
import java.util.Set;


@SqlResultSetMapping(
        name = "mediadetailmapping",
        classes = {
                @ConstructorResult(
                        targetClass = DocumentMediaDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = String.class),
                                @ColumnResult(name = "name", type = String.class),
                                @ColumnResult(name = "category", type = String.class),
                                @ColumnResult(name = "link", type = String.class),
                                @ColumnResult(name = "description", type = String.class),
                                @ColumnResult(name = "created_email", type = String.class),
                                @ColumnResult(name = "created_timestamp", type = BigInteger.class)

                        })
        })

@NamedNativeQuery(
        name = "Media.getMedias",
        query = "SELECT me.id , me.name, me.category, me.link, me.description, me.created_email, me.created_timestamp "
                + " FROM media me"
                + " WHERE (?3 = 'null' or CONCAT_WS('', me.name,me.category,me.description) LIKE CONCAT('%',?3,'%'))"
                + " LIMIT ?1 OFFSET ?2",
        resultSetMapping = "mediadetailmapping"
)

@SqlResultSetMapping(
        name = "mediamapping",
        classes = {
                @ConstructorResult(
                        targetClass = DocumentMediaDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = String.class),
                                @ColumnResult(name = "name", type = String.class),
                                @ColumnResult(name = "category", type = String.class),
                                @ColumnResult(name = "link", type = String.class),
                                @ColumnResult(name = "description", type = String.class),
                                @ColumnResult(name = "created_email", type = String.class),
                                @ColumnResult(name = "created_timestamp", type = BigInteger.class),
                                @ColumnResult(name = "device_id", type = String.class)

                        })
        })


@NamedNativeQuery(
        name = "Media.getMediasByDeviceId",
        query = "SELECT me.id , me.name, me.category, me.link, me.description, me.created_email, me.created_timestamp,demo.device_id "
                + "FROM media me"
                + " LEFT JOIN device_media demo ON demo.media_id = me.id "
                + " WHERE demo.device_id = ?1",
        resultSetMapping = "mediamapping"
)


@NamedNativeQuery(
        name = "Media.getMediasByDeviceIdByPagination",
        query = "SELECT me.id , me.name, me.category, me.link, me.description, me.created_email, me.created_timestamp,demo.device_id "
                + "FROM media me"
                + " LEFT JOIN device_media demo ON demo.media_id = me.id "
                + " WHERE demo.device_id = ?1"
                + " LIMIT ?2 OFFSET ?3",
        resultSetMapping = "mediamapping"
)


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
