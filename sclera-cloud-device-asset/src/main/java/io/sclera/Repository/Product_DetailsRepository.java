package io.sclera.Repository;

import io.sclera.dto.ProductDTO;
import io.sclera.models.Product_Details;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Manages persistence and querying of {@link Product_Details} entities.
 */
@Repository
public interface Product_DetailsRepository extends JpaRepository<Product_Details, String> {
    /**
     * Returns the image URL details of the product with the given id.
     *
     * @param productId product identifier
     * @return the product's image URL details
     */
    ProductDTO getProductsImageUrlById(String productId);
}
