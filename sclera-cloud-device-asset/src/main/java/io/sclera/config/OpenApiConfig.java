package io.sclera.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Defines the OpenAPI document metadata (title/version/description) for this service.
 * springdoc auto-detects this {@link OpenAPI} bean and merges it into the spec served at
 * /v3/api-docs (rendered by Swagger UI at /swagger-ui/index.html).
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("Sclera Cloud Device Asset API")
                .version("1.0.0")
                .description("REST API for the Sclera cloud device/asset management service."));
    }
}
