package io.sclera;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;

/**
 * Spring Boot entry point for the Sclera cloud device-asset microservice.
 */
@SpringBootApplication
public class ScleraCloudDeviceAssetApplication {

    /**
     * Bootstraps and launches the Spring Boot application.
     */
    public static void main(String[] args) {
        SpringApplication.run(ScleraCloudDeviceAssetApplication.class, args);
    }
}
