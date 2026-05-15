package io.sclera.service;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import io.sclera.Repository.Product_DetailsRepository;
import io.sclera.dto.ProductDTO;
import io.sclera.utils.Utils;

@Service
public class Product_DetailsService {



    @Autowired
    Product_DetailsRepository product_detailsRepository;

    @Autowired
    DeviceService deviceService;

    @Autowired
    APICallService apiCallService;

    @Autowired
    Utils utils;

    String absolutePathProductImages = "/home/sclera/images/";

    public Integer checkProductId(String product_id) {
        return 0;
    }

    public void addProductImages(String product_id, String image_url_1, String image_url_2, String image_url_3, String global_image_url_1, String global_image_url_2, String global_image_url_3) {
    }


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
