package io.sclera.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// PG-restore: scalar columns referenced by device-listing native queries (loose coupling).
/**
 * JPA entity holding product-catalog image references for a device's product, used to enrich
 * device listings within the asset-management domain.
 */
@Entity
@Table(name = "product_details")
public class Product_Details {
    @Id
    private String id;

    @Column
    private String image_url_1;

    @Column
    private String image_url_2;

    @Column
    private String image_url_3;

    @Column
    private String global_image_url_1;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getImage_url_1() { return image_url_1; }
    public void setImage_url_1(String image_url_1) { this.image_url_1 = image_url_1; }

    public String getImage_url_2() { return image_url_2; }
    public void setImage_url_2(String image_url_2) { this.image_url_2 = image_url_2; }

    public String getImage_url_3() { return image_url_3; }
    public void setImage_url_3(String image_url_3) { this.image_url_3 = image_url_3; }

    public String getGlobal_image_url_1() { return global_image_url_1; }
    public void setGlobal_image_url_1(String global_image_url_1) { this.global_image_url_1 = global_image_url_1; }
}
