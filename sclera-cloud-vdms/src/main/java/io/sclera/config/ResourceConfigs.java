package io.sclera.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableWebMvc
public class ResourceConfigs implements WebMvcConfigurer {


    @Autowired
    private ResourceUrlConfig resourceUrlConfig;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(resourceUrlConfig.getPathPatterns())
                .addResourceLocations(resourceUrlConfig.getResourceLocations())
                .setCacheControl(CacheControl
                        .maxAge(0, TimeUnit.SECONDS)
                        .mustRevalidate())
                .resourceChain(true)
                .addResolver(new PathResourceResolver());
    }





}

