package io.sclera.Repository;

import io.sclera.dto.ProductDTO;
import io.sclera.models.Product_Details;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Product_DetailsRepository extends JpaRepository<Product_Details, String> {
    ProductDTO getProductsImageUrlById(String productId);
}
