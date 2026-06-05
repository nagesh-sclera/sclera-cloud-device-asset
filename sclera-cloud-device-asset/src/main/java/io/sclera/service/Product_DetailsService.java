package io.sclera.service;
import io.sclera.client.APICallClient;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import io.sclera.Repository.Product_DetailsRepository;
import io.sclera.dto.ProductDTO;
import io.sclera.utils.Utils;

/**
 * Manages product detail records and their associated product images.
 *
 * <p>Persists and removes product metadata through {@link Product_DetailsRepository},
 * collaborates with {@link DeviceService} and {@link APICallClient} for device and
 * remote data, and relies on {@link Utils} for server-side image file handling.
 */
@Service
public class Product_DetailsService {



    @Autowired
    Product_DetailsRepository product_detailsRepository;

    @Autowired
    DeviceService deviceService;

    @Autowired
    APICallClient apiCallService;

    @Autowired
    Utils utils;

    String absolutePathProductImages = "/home/sclera/images/";

    /**
     * Checks whether a product exists for the given identifier.
     *
     * @param product_id the product identifier to check
     * @return the result of the existence check
     */
    public Integer checkProductId(String product_id) {
        return 0;
    }

    /**
     * Associates local and global image URLs with the given product.
     *
     * @param product_id the product identifier
     * @param image_url_1 the first local image URL
     * @param image_url_2 the second local image URL
     * @param image_url_3 the third local image URL
     * @param global_image_url_1 the first global image URL
     * @param global_image_url_2 the second global image URL
     * @param global_image_url_3 the third global image URL
     */
    public void addProductImages(String product_id, String image_url_1, String image_url_2, String image_url_3, String global_image_url_1, String global_image_url_2, String global_image_url_3) {
    }


    /**
     * Inserts a new product detail record or updates the existing one.
     *
     * @param db_product the product detail to persist
     */
    public void upsertProductDetail(ProductDTO db_product)
    {
    }

 void deleteProductDetailsById(String productId){
        ProductDTO productImages = product_detailsRepository.getProductsImageUrlById(productId);
        product_detailsRepository.deleteById(productId);
        if(productImages.getImage_url_1() != null){
            utils.removeFileFromServer(absolutePathProductImages, productId+"_1", utils.getFileExtensionByFileUrl(productImages.getImage_url_1()));
        }
        if(productImages.getImage_url_2() != null) {
            utils.removeFileFromServer(absolutePathProductImages, productId + "_2", utils.getFileExtensionByFileUrl(productImages.getImage_url_2()));
        }
        if(productImages.getImage_url_3() != null) {
            utils.removeFileFromServer(absolutePathProductImages, productId + "_3", utils.getFileExtensionByFileUrl(productImages.getImage_url_3()));
        }
    }

}
