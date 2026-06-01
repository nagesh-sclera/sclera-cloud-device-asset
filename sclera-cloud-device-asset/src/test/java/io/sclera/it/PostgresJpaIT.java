package io.sclera.it;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Reusable base for JPA integration tests backed by a real PostgreSQL 16 Testcontainer.
 *
 * Design decisions:
 *  - @SpringBootTest(classes=JpaTestConfig.class): loads only the JPA slice.
 *    Spring Boot 4.0 removed @DataJpaTest; JpaTestConfig provides the equivalent.
 *  - Container started in a static initializer: guarantees the container is up BEFORE
 *    any Spring @DynamicPropertySource or condition-evaluation code fires. This avoids
 *    the "Mapped port can only be obtained after the container is started" failure.
 *  - @DynamicPropertySource: wires the container JDBC URL/credentials into Spring's
 *    datasource properties so DataSourceAutoConfiguration builds the HikariCP pool.
 *  - JVM timezone: set to UTC via Surefire argLine (-Duser.timezone=UTC in pom.xml).
 *    PostgreSQL 16 rejects "Asia/Calcutta" (legacy Olson alias) during connection
 *    startup; UTC is universally accepted.
 */
@SpringBootTest(classes = JpaTestConfig.class)
public abstract class PostgresJpaIT {

    static final PostgreSQLContainer<?> POSTGRES;

    static {
        // Start the container eagerly so it is running before Spring context init.
        POSTGRES = new PostgreSQLContainer<>("postgres:16")
                .withDatabaseName("sclera_test")
                .withUsername("sclera")
                .withPassword("sclera_test");
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void overrideDataSourceProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url",            POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username",       POSTGRES::getUsername);
        registry.add("spring.datasource.password",       POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name",
                () -> "org.postgresql.Driver");
    }
}
