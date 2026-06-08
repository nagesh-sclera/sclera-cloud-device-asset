package io.sclera.dto;

/** Subset of product_details fields this service displays; owned by sclera-inventory. */
public record ProductImagesDTO(String image_url_1, String image_url_2,
                               String image_url_3, String global_image_url_1) {}
