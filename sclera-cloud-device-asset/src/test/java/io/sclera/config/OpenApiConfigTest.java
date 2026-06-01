package io.sclera.config;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OpenApiConfigTest {

    @Test
    void customOpenAPI_definesApiMetadata() {
        OpenAPI openAPI = new OpenApiConfig().customOpenAPI();

        assertThat(openAPI).isNotNull();
        assertThat(openAPI.getInfo()).isNotNull();
        assertThat(openAPI.getInfo().getTitle())
                .isEqualTo("Sclera Cloud Device Asset API");
        assertThat(openAPI.getInfo().getVersion())
                .isEqualTo("1.0.0");
        assertThat(openAPI.getInfo().getDescription())
                .isEqualTo("REST API for the Sclera cloud device/asset management service.");
    }
}
