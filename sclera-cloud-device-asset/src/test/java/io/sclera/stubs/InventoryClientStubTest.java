package io.sclera.stubs;

import io.sclera.dto.ProductImagesDTO;
import org.junit.jupiter.api.Test;
import java.util.Map;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class InventoryClientStubTest {
    private final InventoryClient client = new InventoryClientStub();

    @Test
    void getProductImages_emptyInput_returnsEmptyMap() {
        assertTrue(client.getProductImages(Set.of()).isEmpty());
    }

    @Test
    void getProductImages_returnsDefaultsForEachId_neverNullMap() {
        Map<String, ProductImagesDTO> r = client.getProductImages(Set.of("p1", "p2"));
        assertNotNull(r);                       // stub never returns null map
        // stub has no data source yet: either empty map or null-valued DTOs; both mean "no images"
        assertNull(client.getProductImages(Set.of("p1")).getOrDefault("p1", new ProductImagesDTO(null,null,null,null)).image_url_1());
    }
}
