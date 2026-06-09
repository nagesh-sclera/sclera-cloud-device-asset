package io.sclera.interfaces;

import io.sclera.dto.ProductDTO;

/** Service contract for {@link io.sclera.service.Product_DetailsService}. */
public interface Product_DetailsServiceInterface {

    Integer checkProductId(String product_id);

    void addProductImages(String product_id, String image_url_1, String image_url_2, String image_url_3, String global_image_url_1, String global_image_url_2, String global_image_url_3);

    void upsertProductDetail(ProductDTO db_product);
}
