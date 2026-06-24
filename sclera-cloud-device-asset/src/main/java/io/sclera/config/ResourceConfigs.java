package io.sclera.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.util.concurrent.TimeUnit;

/**
 * Registers a static resource handler that serves files under {@code /images/**} from the local
 * image directory with private, no-cache headers.
 */
@Configuration
@EnableWebMvc
public class ResourceConfigs implements WebMvcConfigurer {
	 private static final String[] CLASS_PATH_RESOURCE_LOCATIONS = {
	            "file:/home/sclera/images/"
//			 "file:/home/rajath/Desktop/ts_images/"

	    };

	 
	    /**
	     * Maps the {@code /images/**} URL pattern to the configured filesystem image location,
	     * applying no-cache private cache control and a path resource resolver.
	     *
	     * @param registry the resource handler registry to configure
	     */
	    @Override
	    public void addResourceHandlers(ResourceHandlerRegistry registry) {
	        registry.addResourceHandler("/images/**")
	                .addResourceLocations(CLASS_PATH_RESOURCE_LOCATIONS)
	                .setCacheControl(CacheControl.noCache().cachePrivate())
	                .resourceChain(true)

	                .addResolver(new PathResourceResolver());

	        // Serves the HTML JavaDoc bundled into the jar at build time (see maven-javadoc-plugin
	        // in pom.xml) under /javadoc/**. An explicit handler is required because @EnableWebMvc
	        // disables Spring Boot's default classpath:/static/ resource mapping.
	        registry.addResourceHandler("/javadoc/**")
	                .addResourceLocations("classpath:/static/javadoc/apidocs/")
	                .setCacheControl(CacheControl.maxAge(1, TimeUnit.HOURS).cachePublic())
	                .resourceChain(true)
	                .addResolver(new PathResourceResolver());

	        // Serves the bundled QR-code manual test harness (classpath:/static/qr-test/) under
	        // /qr-test/**. Explicit handler required because @EnableWebMvc disables the default
	        // classpath:/static/ mapping. Developer/QA testing page only.
	        registry.addResourceHandler("/qr-test/**")
	                .addResourceLocations("classpath:/static/qr-test/")
	                .setCacheControl(CacheControl.noCache().cachePrivate())
	                .resourceChain(true)
	                .addResolver(new PathResourceResolver());

	    }
	    
	    
//	    CacheControl x;
	    
}
