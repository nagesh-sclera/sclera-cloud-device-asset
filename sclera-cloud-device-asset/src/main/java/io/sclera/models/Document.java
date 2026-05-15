package io.sclera.models;

import java.math.BigInteger;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.ColumnResult;
import javax.persistence.ConstructorResult;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.NamedNativeQuery;
import javax.persistence.SqlResultSetMapping;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import io.sclera.dto.DocumentMediaDTO;
import org.hibernate.annotations.ColumnDefault;


//getDocuments
@SqlResultSetMapping(
        name = "documentdetailmapping",
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
                                @ColumnResult(name = "encrypted_type", type = Integer.class)

                        })
        })


@NamedNativeQuery(
        name = "Document.getDocuments",
        query = "SELECT do.id , do.name, do.category, do.link, do.description, do.created_email, do.created_timestamp, do.encrypted_type "
                + " FROM document do"
                + " WHERE ?3 ='null' or CONCAT_WS('',do.name,do.category,do.description) LIKE CONCAT('%',?3,'%')"
                + " LIMIT ?1 OFFSET ?2",
        resultSetMapping = "documentdetailmapping"
)


//getDocumentsByDeviceId
@SqlResultSetMapping(
        name = "documentmapping",
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
                                @ColumnResult(name = "device_id", type = String.class),
                                @ColumnResult(name = "encrypted_type", type = Integer.class)

                        })
        })


@NamedNativeQuery(
        name = "Document.getDocumentsByDeviceId",
        query = "SELECT do.id , do.name, do.category, do.link, do.description, do.created_email, do.created_timestamp,dedo.device_id, do.encrypted_type  "
                + "FROM document do "
                + " LEFT JOIN device_document dedo ON dedo.document_id = do.id "
                + " WHERE dedo.device_id = ?1",
        resultSetMapping = "documentmapping"
)

@NamedNativeQuery(
        name = "Document.getDocumentsByDeviceIdByPagination",
        query = "SELECT do.id , do.name, do.category, do.link, do.description, do.created_email, do.created_timestamp,dedo.device_id, do.encrypted_type  "
                + " FROM document do "
                + " LEFT JOIN device_document dedo ON dedo.document_id = do.id "
                + " WHERE dedo.device_id = ?1"
                + " LIMIT ?2 OFFSET ?3",
        resultSetMapping = "documentmapping"
)

//getDocumentsById
@SqlResultSetMapping(
        name = "documentByIdMapping",
        classes = {
                @ConstructorResult(
                        targetClass = DocumentMediaDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = String.class),
                                @ColumnResult(name = "link", type = String.class),
                                @ColumnResult(name = "encrypted_type", type = Integer.class)

                        })
        })
@NamedNativeQuery(
        name = "Document.getDocumentById",
        query = "SELECT do.id AS id , do.link AS link, do.encrypted_type AS encrypted_type "
                + " FROM document do "
                + " WHERE do.id = ?1 ",
        resultSetMapping = "documentByIdMapping"
)

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
