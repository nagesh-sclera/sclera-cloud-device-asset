package io.sclera.workorder.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SpringDoc OpenAPI configuration.
 *
 * Endpoints:
 *   /v3/api-docs           ── machine-readable spec
 *   /v3/api-docs/maximo-v1 ── v1 group spec (same content)
 *   /swagger-ui.html       ── interactive UI
 *
 * Bearer JWT scheme is declared so the gateway's forwarded auth header shows
 * up in the spec consistently — preserves the existing auth integration for
 * the future frontend phase.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI maximoOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Workorder Service API")
                        .version("1.0.0")
                        .description("Maximo configuration, work-order, and ticket endpoints. " +
                                "Refactored from the Sclera monolith; URL contracts preserved."))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }

    @Bean
    public GroupedOpenApi maximoV1Group() {
        return GroupedOpenApi.builder()
                .group("maximo-v1")
                // Match all endpoints under the service context-path (springdoc strips
                // the /api/v1/workorder-service context-path before matching). The old
                // "/user/**","/api/**" patterns were monolith leftovers and matched
                // nothing after the URL restructure, yielding an empty group.
                .pathsToMatch("/**")
                .build();
    }
}
