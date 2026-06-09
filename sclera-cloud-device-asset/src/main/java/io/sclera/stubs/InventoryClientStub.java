package io.sclera.stubs;

import io.sclera.dto.ProductImagesDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.Set;

/**
 * STUB for sclera-inventory product images. Returns no data (DB-per-service: product_details
 * left this schema). Replace the body with DaprClient.invokeMethod("sclera-inventory", ...)
 * when inventory exposes the endpoint. Per CLAUDE.md stubs never throw.
 */
@Component
public class InventoryClientStub implements InventoryClient {
    private static final Logger log = LoggerFactory.getLogger(InventoryClientStub.class);

    @Override
    public Map<String, ProductImagesDTO> getProductImages(Set<String> productIds) {
        if (productIds == null || productIds.isEmpty()) return Map.of();
        log.warn("STUB InventoryClient.getProductImages({} ids) returning no data (Dapr owner 'sclera-inventory' not wired)", productIds.size());
        return Map.of();
    }
}
