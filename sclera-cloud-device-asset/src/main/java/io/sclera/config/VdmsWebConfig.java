package io.sclera.config;

import io.sclera.web.VdmsContextInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Registers {@link VdmsContextInterceptor} so every API request resolves its VDMS
 * into the request-scoped {@code VdmsContext}. Separate from {@code ResourceConfigs}
 * (which handles static resources) to keep one responsibility per configurer.
 */
@Configuration
public class VdmsWebConfig implements WebMvcConfigurer {

    private final VdmsContextInterceptor vdmsContextInterceptor;

    public VdmsWebConfig(VdmsContextInterceptor vdmsContextInterceptor) {
        this.vdmsContextInterceptor = vdmsContextInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(vdmsContextInterceptor)
                .addPathPatterns("/api/v1/sclera-cloud-device-asset-service/**");
    }
}
