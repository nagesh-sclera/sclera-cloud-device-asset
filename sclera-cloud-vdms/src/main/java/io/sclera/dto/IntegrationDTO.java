package io.sclera.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@NoArgsConstructor
@ToString
public class IntegrationDTO {

    private String integration_id;
    private String name;
    private String integration_name;
    private String image_url;
    private String protocols;
    private String frequency;
    private String authentication_data;
    private String authentications;
    private String subscriptions;
    private String tag_list;
    private String template;
    private String description;
    private String category;
    private String base64image;
    private String extension;
    private String itemId;


    public IntegrationDTO(String integration_id, String name, String description, String category, String image_url,String itemId) {
        this.integration_id = integration_id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.image_url = image_url;
        this.itemId = itemId;
    }

    public IntegrationDTO(String integration_id, String name, String integration_name, String image_url, String protocols, String frequency, String authentications, String subscriptions, String tag_list, String template ,String itemId) {
        this.integration_id = integration_id;
        this.name = name;
        this.integration_name = integration_name;
        this.image_url = image_url;
        this.protocols = protocols;
        this.frequency = frequency;
        this.authentications = authentications;
        this.subscriptions = subscriptions;
        this.tag_list = tag_list;
        this.template = template;
        this.itemId = itemId;
    }

    public IntegrationDTO(String integration_id, String name) {
        this.integration_id = integration_id;
        this.name = name;
    }
}
