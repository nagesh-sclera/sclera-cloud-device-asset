package io.sclera.stubs;

import io.sclera.dto.ProductImagesDTO;
import java.util.Map;
import java.util.Set;

/** Fetches product image data owned by sclera-inventory. Today: stub. Later: Dapr invoke. */
public interface InventoryClient {
    /** @return map productId -> images; ids with no data are absent or map to a null-valued DTO. */
    Map<String, ProductImagesDTO> getProductImages(Set<String> productIds);
}
