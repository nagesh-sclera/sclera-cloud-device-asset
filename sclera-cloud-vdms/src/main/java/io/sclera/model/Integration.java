package io.sclera.model;

import io.sclera.dto.IntegrationDTO;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@Getter
@Setter

@SqlResultSetMapping(
        name = "integrationmapping",
        classes = {
                @ConstructorResult(
                        targetClass = IntegrationDTO.class,
                        columns = {
                                @ColumnResult(name = "integration_id" , type = String.class),
                                @ColumnResult(name = "name" , type = String.class),
                                @ColumnResult(name = "description" , type = String.class),
                                @ColumnResult(name = "category" , type = String.class),
                                @ColumnResult(name = "image_url" , type = String.class),
                                @ColumnResult(name = "itemId" , type = String.class)
                        }
                )
        }
)



@SqlResultSetMapping(
        name = "integrationdatamapping",
        classes = {
                @ConstructorResult(
                        targetClass = IntegrationDTO.class,
                        columns = {
                                @ColumnResult(name = "integration_id" , type = String.class),
                                @ColumnResult(name = "name" , type = String.class),
                                @ColumnResult(name = "integration_name" , type = String.class),
                                @ColumnResult(name = "image_url" , type = String.class),
                                @ColumnResult(name = "protocols" , type = String.class),
                                @ColumnResult(name = "frequency" , type = String.class),
                                @ColumnResult(name = "authentications" , type = String.class),
                                @ColumnResult(name = "subscriptions" , type = String.class),
                                @ColumnResult(name = "tag_list" , type = String.class),
                                @ColumnResult(name = "template" , type = String.class),
                                @ColumnResult(name = "itemId" , type = String.class)
                        }
                )
        }
)


@SqlResultSetMapping(
        name = "integrationnamemapping",
        classes = {
                @ConstructorResult(
                        targetClass = IntegrationDTO.class,
                        columns = {
                                @ColumnResult(name = "integration_id" , type = String.class),
                                @ColumnResult(name = "name" , type = String.class)
                        }
                )
        }
)


@NamedNativeQuery(
        name = "Integration.getDistinctIntegrations",
        query = "SELECT id AS integration_id , name ,description ,category ,image_url ,item_id AS itemId " +
                "FROM integration ORDER BY name ASC ",
        resultSetMapping = "integrationmapping"
)

@NamedNativeQuery(
        name = "Integration.getIntegrationNames",
        query = "SELECT id AS integration_id ,name FROM integration ORDER BY name ASC  ",
        resultSetMapping = "integrationnamemapping"
)

@NamedNativeQuery(
        name = "Integration.getIntegrationsByCategory",
        query = "SELECT id AS integration_id , name ,description ,category ,image_url ,item_id AS itemId " +
                "FROM integration WHERE (?1 = 'all') OR (category IN ?1) ORDER BY name ASC ",
        resultSetMapping = "integrationmapping"
)

@NamedNativeQuery(
        name = "Integration.getIntegrationDataByIntegrationId",
        query = "SELECT id AS integration_id , name ,integration_name, image_url ,protocols ," +
                "subscriptions ,frequency ,authentications ,tag_list ,template ,item_id AS itemId " +
                "FROM integration WHERE id = ?1 ",
        resultSetMapping = "integrationdatamapping"
)


public class Integration {

    @Id
    private String id;

    @Column(length = 64)
    private String name;

    @Column(length = 128)
    private String integration_name;

    @Column(columnDefinition = "TEXT")
    private String image_url;

    @Column(length = 128)
    private String protocols;

    @Column(columnDefinition = "TEXT")
    private String subscriptions;

    private String frequency;

    @Column(columnDefinition = "TEXT")
    private String authentications;

    @Column(columnDefinition = "TEXT")
    private String tag_list;

    @Column(length = 128)
    private String template;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 64)
    private String category;

    @Column(length = 64)
    private String itemId;


}
