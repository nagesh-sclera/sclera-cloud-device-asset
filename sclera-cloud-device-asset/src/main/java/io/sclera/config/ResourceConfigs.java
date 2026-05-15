package io.sclera.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

@Configuration
@EnableWebMvc
public class ResourceConfigs implements WebMvcConfigurer {
	 private static final String[] CLASS_PATH_RESOURCE_LOCATIONS = {
	            "file:/home/sclera/images/"
//			 "file:/home/rajath/Desktop/ts_images/"

	    };

	 
	    @Override
	    public void addResourceHandlers(ResourceHandlerRegistry registry) {
	        registry.addResourceHandler("/images/**")
	                .addResourceLocations(CLASS_PATH_RESOURCE_LOCATIONS)
	                .setCacheControl(CacheControl.noCache().cachePrivate())
	                .resourceChain(true)
	                
	                .addResolver(new PathResourceResolver());
	        
	    }
	    
	    
//	    CacheControl x;
	    
}
